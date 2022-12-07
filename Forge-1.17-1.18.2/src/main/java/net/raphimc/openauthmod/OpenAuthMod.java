package net.raphimc.openauthmod;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;

@Mod("openauthmod")
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
        if (this.connection.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId() == 2) {
            if (this.connection.channel().remoteAddress() instanceof InetSocketAddress address) {
                if (address.getAddress().isLoopbackAddress()) {
                    try {
                        yesCallback.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.connection.channel().close();
                    }
                    return;
                }
            }
        }

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
            }, new TextComponent(title), new TextComponent(subTitle)));
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

}
