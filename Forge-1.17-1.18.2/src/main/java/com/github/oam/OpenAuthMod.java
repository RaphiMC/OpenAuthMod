package com.github.oam;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.concurrent.Callable;

@Mod("oam")
public class OpenAuthMod extends OpenAuthModPlatform {

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

        return this.handleCustomPayloadPacket(channel.toString(), ByteBufUtil.getBytes(data));
    }

    public boolean handleQueryRequest(final Connection connection, final ResourceLocation channel, final int queryId, final FriendlyByteBuf data) throws IOException {
        this.connection = connection;

        return this.handleQueryRequestPacket(channel.toString(), queryId, ByteBufUtil.getBytes(data));
    }

    @Override
    protected void sendCustomPayloadPacket(String channel, byte[] data) {
        this.connection.send(new ServerboundCustomPayloadPacket(new ResourceLocation(channel), new FriendlyByteBuf(Unpooled.wrappedBuffer(data))));
    }

    @Override
    protected void sendQueryResponsePacket(int queryId, byte[] data) {
        this.connection.send(new ServerboundCustomQueryPacket(queryId, data != null ? new FriendlyByteBuf(Unpooled.wrappedBuffer(data)) : null));
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
            }, new TextComponent(title), new TextComponent(subTitle)));
        });
    }

    @Override
    protected boolean joinServer(String serverHash) {
        try {
            this.mc.getMinecraftSessionService().joinServer(this.mc.getUser().getGameProfile(), this.mc.getUser().getAccessToken(), serverHash);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    protected boolean isInPlayState() {
        return ConnectionProtocol.PLAY.equals(this.connection.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get());
    }

}
