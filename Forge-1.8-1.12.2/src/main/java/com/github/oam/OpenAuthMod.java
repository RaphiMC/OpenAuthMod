package com.github.oam;

import com.github.oam.utils.ReflectionUtils;
import com.github.oam.utils.VarIntWriter;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.network.*;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

@Mod(modid = "oam", useMetadata = true, clientSideOnly = true)
public class OpenAuthMod extends LegacyOpenAuthModPlatform {

    private static OpenAuthMod INSTANCE;

    public static OpenAuthMod getInstance() {
        return INSTANCE;
    }


    private final Minecraft mc = Minecraft.getMinecraft();
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

            return this.handlePlayCustomPayload(channel, data);
        } else if (packet.getClass().getName().contains("login") && packet.getClass().getName().contains("Disconnect")) {
            String rawReason;
            try {
                final Class<?> clazz = ReflectionUtils.getClass("net.minecraft.util.IChatComponent", "net.minecraft.util.text.ITextComponent");
                final Object component = ReflectionUtils.getField(packet, clazz, 0);
                rawReason = (String) ReflectionUtils.getMethod(component, "getUnformattedText", "func_150260_c").invoke(component);
            } catch (Throwable t) {
                t.printStackTrace();
                rawReason = "";
            }

            return this.handleLoginDisconnect(rawReason);
        } else if (packet.getClass().getSimpleName().endsWith("EnableCompression")) {
            return this.handleLoginSetCompression(ReflectionUtils.getField(packet, int.class, 0));
        }

        return false;
    }


    @Override
    protected void sendResponse(int id, byte[] data) {
        try {
            if (this.networkManager.channel().attr(NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get().getId() == 0) {
                final Packet<?> customPayloadPacket = (Packet<?>) ReflectionUtils.getClass("net.minecraft.network.play.client.C17PacketCustomPayload", "net.minecraft.network.play.client.CPacketCustomPayload").newInstance();
                final PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
                VarIntWriter.writeVarInt(packetBuffer, id);
                packetBuffer.writeBytes(data);
                ReflectionUtils.setField(customPayloadPacket, SharedConstants.DATA_CHANNEL, String.class, 0);
                ReflectionUtils.setField(customPayloadPacket, packetBuffer, PacketBuffer.class, 0);
                this.networkManager.sendPacket(customPayloadPacket);
            } else {
                final Packet<?> encryptionResponsePacket = EnumConnectionState.LOGIN.getPacket(EnumPacketDirection.SERVERBOUND, 1);
                final PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
                VarIntWriter.writeVarInt(packetBuffer, id);
                packetBuffer.writeBytes(data);
                final byte[] packetBufferData = new byte[packetBuffer.readableBytes()];
                packetBuffer.readBytes(packetBufferData);
                packetBuffer.release();
                ReflectionUtils.setField(encryptionResponsePacket, packetBufferData, byte[].class, 0);
                ReflectionUtils.setField(encryptionResponsePacket, SharedConstants.DATA_CHANNEL.getBytes(StandardCharsets.UTF_8), byte[].class, 1);
                networkManager.sendPacket(encryptionResponsePacket);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            this.networkManager.channel().close();
        }
    }

    @Override
    protected void openConfirmScreen(String title, String subTitle, Callable<Void> yesCallback, Callable<Void> noCallback) {
        this.mc.addScheduledTask(() -> {
            final GuiScreen parentScreen = this.mc.currentScreen;
            this.mc.displayGuiScreen(new GuiYesNo((result, id) -> {
                try {
                    if (result) yesCallback.call();
                    else noCallback.call();
                } catch (Throwable e) {
                    e.printStackTrace();
                    this.networkManager.channel().close();
                }
                this.mc.displayGuiScreen(parentScreen);
            }, title, subTitle, 0));
        });
    }

    @Override
    protected boolean joinServer(String serverIdHash) {
        try {
            this.mc.getSessionService().joinServer(this.mc.getSession().getProfile(), this.mc.getSession().getToken(), serverIdHash);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

}
