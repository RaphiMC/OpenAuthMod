package com.github.oam;

import com.github.oam.multiconnect_compat.MultiConnectCompat;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class OpenAuthMod implements ClientModInitializer {

    public static final Identifier OAM_CHANNEL = new Identifier("openauthmod:join");
    public static final boolean MULTICONNECT_LOADED = FabricLoader.getInstance().isModLoaded("multiconnect");

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    @Override
    public void onInitializeClient() {
        if (MULTICONNECT_LOADED) {
            MultiConnectCompat.allowOAMCustomPayloads();
        }
    }

    public static void handlePlayCustomPayload(final Identifier channel, final PacketByteBuf data) {
        final Screen parentScreen = mc.currentScreen;
        final String serverHash = data.readString();
        mc.execute(() -> {
            mc.openScreen(new ConfirmScreen(success -> {
                final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                if (success) {
                    try {
                        mc.getSessionService().joinServer(mc.getSession().getProfile(), mc.getSession().getAccessToken(), serverHash);
                        buf.writeBoolean(true);
                    } catch (Throwable t) {
                        buf.writeBoolean(false);
                    }
                } else {
                    buf.writeBoolean(false);
                }
                mc.openScreen(parentScreen);
                if (OpenAuthMod.MULTICONNECT_LOADED) {
                    MultiConnectCompat.sendPacket(mc.getNetworkHandler(), channel, buf);
                } else {
                    mc.getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(channel, buf));
                }
            }, new LiteralText("Allow Open Auth Mod authentication?"), new LiteralText("This will allow the proxy to authenticate as you in a Minecraft Server.")));
        });
    }

}
