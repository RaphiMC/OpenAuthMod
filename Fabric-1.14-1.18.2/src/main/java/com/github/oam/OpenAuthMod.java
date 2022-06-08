package com.github.oam;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.concurrent.Callable;

public class OpenAuthMod extends ModernOpenAuthModPlatform implements ClientModInitializer {

    private static OpenAuthMod INSTANCE;
    public static final boolean MULTICONNECT_LOADED = FabricLoader.getInstance().isModLoaded("multiconnect");

    public static OpenAuthMod getInstance() {
        return INSTANCE;
    }


    private final MinecraftClient mc = MinecraftClient.getInstance();
    private ClientConnection clientConnection;

    public OpenAuthMod() {
        INSTANCE = this;
    }

    @Override
    public void onInitializeClient() {
        if (MULTICONNECT_LOADED) {
            throw new IllegalStateException("OpenAuthMod is currently not compatible with multiconnect!");
        }
    }


    public boolean handlePlayCustomPayload(final ClientConnection clientConnection, final Identifier channel, final PacketByteBuf data) throws IOException {
        this.clientConnection = clientConnection;

        return this.handlePlayCustomPayload(channel.toString(), ByteBufUtil.getBytes(data));
    }

    public boolean handleLoginCustomPayload(final ClientConnection clientConnection, final Identifier channel, final int queryId, final PacketByteBuf data) throws IOException {
        this.clientConnection = clientConnection;

        return this.handleLoginCustomPayload(channel.toString(), queryId, ByteBufUtil.getBytes(data));
    }


    @Override
    protected void sendResponse(int id, byte[] data) {
        if (this.clientConnection.channel.attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get().getId() == 0) {
            final PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
            packetByteBuf.writeVarInt(id);
            packetByteBuf.writeBytes(data);
            this.clientConnection.send(new CustomPayloadC2SPacket(new Identifier(SharedConstants.DATA_CHANNEL), packetByteBuf));
        } else {
            this.clientConnection.send(new LoginQueryResponseC2SPacket(id, new PacketByteBuf(Unpooled.wrappedBuffer(data))));
        }
    }

    @Override
    protected void openConfirmScreen(String title, String subTitle, Callable<Void> yesCallback, Callable<Void> noCallback) {
        this.mc.execute(() -> {
            final Screen parentScreen = mc.currentScreen;
            this.mc.openScreen(new ConfirmScreen(success -> {
                try {
                    if (success) yesCallback.call();
                    else noCallback.call();
                } catch (Throwable e) {
                    e.printStackTrace();
                    this.clientConnection.channel.close();
                }
                this.mc.openScreen(parentScreen);
            }, new LiteralText(title), new LiteralText(subTitle)));
        });
    }

    @Override
    protected boolean joinServer(String serverIdHash) {
        try {
            this.mc.getSessionService().joinServer(this.mc.getSession().getProfile(), this.mc.getSession().getAccessToken(), serverIdHash);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

}
