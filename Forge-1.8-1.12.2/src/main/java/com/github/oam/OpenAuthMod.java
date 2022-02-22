package com.github.oam;

import com.github.oam.utils.ReflectionUtils;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.network.*;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.concurrent.Callable;

@Mod(modid = "oam", version = "2.0.0", acceptedMinecraftVersions = "*")
public class OpenAuthMod extends OpenAuthModPlatform {

    public static OpenAuthMod INSTANCE;

    private NetworkManager networkManager;

    public OpenAuthMod() {
        INSTANCE = this;
    }

    public boolean handlePacket(final NetworkManager networkManager, final Packet<?> packet) throws IOException {
        this.networkManager = networkManager;

        if (packet.getClass().getSimpleName().endsWith("CustomPayload")) {
            final String channel = ReflectionUtils.getField(packet, String.class, 0);
            final PacketBuffer buffer = ReflectionUtils.getField(packet, PacketBuffer.class, 0);
            buffer.markReaderIndex();
            final byte[] data = new byte[buffer.readableBytes()];
            buffer.readBytes(data);
            buffer.resetReaderIndex();

            return this.handleCustomPayloadPacket(channel, data);
        } else if (packet.getClass().getName().contains("Disconnect") && packet.getClass().getName().contains("login")) {
            String rawReason;
            try {
                final Class<?> clazz = ReflectionUtils.getClass("net.minecraft.util.IChatComponent", "net.minecraft.util.text.ITextComponent");
                final Object component = ReflectionUtils.getField(packet, clazz, 0);
                rawReason = (String) ReflectionUtils.getMethod(component, "getUnformattedText", "func_150260_c").invoke(component);
            } catch (Throwable t) {
                t.printStackTrace();
                rawReason = "";
            }

            return this.handleDisconnectPacket(rawReason);
        } else if (packet.getClass().getName().endsWith("EnableCompression")) {
            return this.handleSetCompressionPacket(ReflectionUtils.getField(packet, int.class, 0));
        }

        return false;
    }

    @Override
    protected void sendCustomPayloadPacket(String channel, byte[] data) {
        try {
            final Packet<?> packet = (Packet<?>) ReflectionUtils.getClass("net.minecraft.network.play.client.C17PacketCustomPayload", "net.minecraft.network.play.client.CPacketCustomPayload").newInstance();
            final PacketBuffer buf = new PacketBuffer(Unpooled.wrappedBuffer(data));
            ReflectionUtils.setField(packet, channel, String.class, 0);
            ReflectionUtils.setField(packet, buf, PacketBuffer.class, 0);
            networkManager.sendPacket(packet);
        } catch (Throwable e) {
            e.printStackTrace();
            networkManager.channel().close();
        }
    }

    @Override
    protected void sendLoginHelloPacket(String username) {
        try {
            final Packet<?> loginStartPacket = EnumConnectionState.LOGIN.getPacket(EnumPacketDirection.SERVERBOUND, 0);
            ReflectionUtils.setField(loginStartPacket, new GameProfile(null, username), GameProfile.class, 0);
            networkManager.sendPacket(loginStartPacket);
        } catch (Throwable e) {
            e.printStackTrace();
            networkManager.channel().close();
        }
    }

    @Override
    protected void openConfirmScreen(String title, String subTitle, Callable<Void> yesCallback, Callable<Void> noCallback) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            final GuiScreen parentScreen = Minecraft.getMinecraft().currentScreen;
            final GuiYesNo guiYesNo = new GuiYesNo((result, id) -> {
                try {
                    final Void unused = result ? yesCallback.call() : noCallback.call();
                } catch (Throwable e) {
                    e.printStackTrace();
                    networkManager.channel().close();
                }
                Minecraft.getMinecraft().displayGuiScreen(parentScreen);
            }, title, subTitle, 0);
            Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(guiYesNo));
        });
    }

    @Override
    protected boolean joinServer(String serverHash) {
        final Minecraft mc = Minecraft.getMinecraft();
        try {
            mc.getSessionService().joinServer(mc.getSession().getProfile(), mc.getSession().getToken(), serverHash);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    protected boolean isInPlayState() {
        return EnumConnectionState.PLAY.equals(this.networkManager.channel().attr(NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get());
    }

}
