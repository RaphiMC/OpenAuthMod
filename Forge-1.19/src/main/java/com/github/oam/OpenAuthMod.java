package com.github.oam;

import com.github.oam.data.SignedNonce;
import com.google.common.primitives.Longs;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Crypt;
import net.minecraft.util.Signer;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.concurrent.Callable;

@Mod("oam")
public class OpenAuthMod extends ModernOpenAuthModPlatform {

    private static OpenAuthMod INSTANCE;

    public static OpenAuthMod getInstance() {
        return INSTANCE;
    }


    private final Minecraft mc = Minecraft.getInstance();
    private Connection connection;

    public OpenAuthMod() {
        INSTANCE = this;
    }


    public boolean handlePlayCustomPayload(final Connection connection, final ResourceLocation channel, final FriendlyByteBuf data) throws IOException {
        this.connection = connection;

        return this.handlePlayCustomPayload(channel.toString(), ByteBufUtil.getBytes(data));
    }

    public boolean handleLoginCustomPayload(final Connection connection, final ResourceLocation channel, final int queryId, final FriendlyByteBuf data) throws IOException {
        this.connection = connection;

        return this.handleLoginCustomPayload(channel.toString(), queryId, ByteBufUtil.getBytes(data));
    }


    @Override
    protected void sendResponse(int id, byte[] data) {
        if (this.connection.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId() == 0) {
            final FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(Unpooled.buffer());
            friendlyByteBuf.writeVarInt(id);
            friendlyByteBuf.writeBytes(data);
            this.connection.send(new ServerboundCustomPayloadPacket(new ResourceLocation(SharedConstants.DATA_CHANNEL), friendlyByteBuf));
        } else {
            this.connection.send(new ServerboundCustomQueryPacket(id, new FriendlyByteBuf(Unpooled.wrappedBuffer(data))));
        }
    }

    @Override
    protected void openConfirmScreen(String title, String subTitle, Callable<Void> yesCallback, Callable<Void> noCallback) {
        this.mc.execute(() -> {
            final Screen parentScreen = this.mc.screen;
            this.mc.setScreen(new ConfirmScreen(success -> {
                try {
                    if (success) yesCallback.call();
                    else noCallback.call();
                } catch (Throwable e) {
                    e.printStackTrace();
                    this.connection.channel().close();
                }
                this.mc.setScreen(parentScreen);
            }, Component.literal(title), Component.literal(subTitle)));
        });
    }

    @Override
    protected boolean joinServer(String serverIdHash) {
        try {
            this.mc.getMinecraftSessionService().joinServer(this.mc.getUser().getGameProfile(), this.mc.getUser().getAccessToken(), serverIdHash);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    protected SignedNonce signNonce(byte[] nonce) {
        try {
            final Signer signer = this.mc.getProfileKeyPairManager().signer();
            if (signer == null) return null;

            final long salt = Crypt.SaltSupplier.getLong();
            final byte[] signature = signer.sign(updater -> {
                updater.update(nonce);
                updater.update(Longs.toByteArray(salt));
            });
            return new SignedNonce(salt, signature);
        } catch (Throwable e) {
            return null;
        }
    }

}