package com.github.oam;

import com.github.oam.multiconnect_compat.MultiConnectCompat;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.concurrent.Callable;

public class OpenAuthMod extends OpenAuthModPlatform implements ClientModInitializer {

    public static OpenAuthMod INSTANCE;
    public static final boolean MULTICONNECT_LOADED = FabricLoader.getInstance().isModLoaded("multiconnect");

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private ClientConnection clientConnection;

    public OpenAuthMod() {
        INSTANCE = this;
    }

    @Override
    public void onInitializeClient() {
        if (MULTICONNECT_LOADED) {
            MultiConnectCompat.allowOAMCustomPayloads();
        }
    }

    public boolean handlePlayCustomPayload(final ClientConnection clientConnection, final Identifier channel, final PacketByteBuf data) throws IOException {
        this.clientConnection = clientConnection;

        return this.handleCustomPayloadPacket(channel.toString(), ByteBufUtil.getBytes(data));
    }

    public boolean handleQueryRequest(final ClientConnection clientConnection, final Identifier channel, final int queryId, final PacketByteBuf data) throws IOException {
        this.clientConnection = clientConnection;

        return this.handleQueryRequestPacket(channel.toString(), queryId, ByteBufUtil.getBytes(data));
    }

    @Override
    protected void sendCustomPayloadPacket(String channel, byte[] data) {
        if (OpenAuthMod.MULTICONNECT_LOADED) {
            MultiConnectCompat.sendPacket((ClientPlayNetworkHandler) this.clientConnection.getPacketListener(), new Identifier(channel), new PacketByteBuf(Unpooled.wrappedBuffer(data)));
        } else {
            this.clientConnection.send(new CustomPayloadC2SPacket(new Identifier(channel), new PacketByteBuf(Unpooled.wrappedBuffer(data))));
        }
    }

    @Override
    protected void sendQueryResponsePacket(int queryId, byte[] data) {
        this.clientConnection.send(new LoginQueryResponseC2SPacket(queryId, data != null ? new PacketByteBuf(Unpooled.wrappedBuffer(data)) : null));
    }

    @Override
    protected void openConfirmScreen(String title, String subTitle, Callable<Void> yesCallback, Callable<Void> noCallback) {
        mc.execute(() -> {
            final Screen parentScreen = mc.currentScreen;
            mc.openScreen(new ConfirmScreen(success -> {
                try {
                    final Void unused = success ? yesCallback.call() : noCallback.call();
                } catch (Throwable e) {
                    e.printStackTrace();
                    this.clientConnection.channel.close();
                }
                mc.openScreen(parentScreen);
            }, new LiteralText(title), new LiteralText(subTitle)));
        });
    }

    @Override
    protected boolean joinServer(String serverHash) {
        try {
            mc.getSessionService().joinServer(mc.getSession().getProfile(), mc.getSession().getAccessToken(), serverHash);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    protected boolean isInPlayState() {
        return NetworkState.PLAY.equals(this.clientConnection.channel.attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get());
    }

}
