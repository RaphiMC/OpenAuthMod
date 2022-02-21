package com.github.oam;

import com.github.oam.utils.IntTo3ByteCodec;
import com.github.oam.utils.ReflectionUtils;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.network.*;
import net.minecraftforge.fml.common.Mod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Mod(modid = "oam", version = "2.0.0", acceptedMinecraftVersions = "*")
public class OpenAuthMod {

    private static final String OAM_CPL_CHANNEL = "openauthmod:join";
    private static final byte[] OAM_MAGIC = new byte[]{2, 20, 12, 3};
    private static final String OAM_MAGIC_STRING = new String(OAM_MAGIC, StandardCharsets.UTF_8);
    private static final int OAM_MAGIC_INT;

    static {
        try {
            OAM_MAGIC_INT = -new DataInputStream(new ByteArrayInputStream(OAM_MAGIC)).readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ByteArrayOutputStream compressionDataStream = null;

    public static boolean handlePacket(final NetworkManager networkManager, final Packet<?> packet) {
        if (packet.getClass().getSimpleName().endsWith("CustomPayload")) {
            String channel = ReflectionUtils.getField(packet, String.class, 0);
            if (OAM_CPL_CHANNEL.equals(channel)) {
                final PacketBuffer buffer = ReflectionUtils.getField(packet, PacketBuffer.class, 0);
                final String serverHash = buffer.readString(64);
                requestAuth(networkManager, serverHash, true);
                return false;
            }
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
            final String[] parts = rawReason.split("\n");
            if (parts.length == 3 && Arrays.equals(parts[2].getBytes(), OAM_MAGIC)) {
                requestAuth(networkManager, parts[1], false);
                return false;
            }
        } else if (packet.getClass().getName().contains("EnableCompression")) {
            final int threshold = ReflectionUtils.getField(packet, int.class, 0);
            if (threshold < 0) {
                if (compressionDataStream == null && threshold == OAM_MAGIC_INT) { // start
                    compressionDataStream = new ByteArrayOutputStream();
                    return false;
                } else if (compressionDataStream != null && threshold == OAM_MAGIC_INT) { // end
                    final String serverHash = new BigInteger(compressionDataStream.toByteArray()).toString(16);
                    compressionDataStream = null;
                    requestAuth(networkManager, serverHash, false);
                    return false;
                }
                if (compressionDataStream != null) { // data
                    try {
                        compressionDataStream.write(IntTo3ByteCodec.decode(threshold));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private static void requestAuth(final NetworkManager networkManager, final String serverHash, final boolean ingame) {
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                final GuiScreen parentScreen = Minecraft.getMinecraft().currentScreen;
                final GuiYesNo guiYesNo = new GuiYesNo(new GuiYesNoCallback() {
                    @Override
                    public void confirmClicked(boolean result, int id) {
                        if (result) {
                            joinServer(networkManager, serverHash, ingame);
                        } else {
                            sendResponse(networkManager, false, ingame);
                        }
                        Minecraft.getMinecraft().displayGuiScreen(parentScreen);
                    }
                }, "Allow Open Auth Mod authentication?", "This will allow the proxy to authenticate as you in a Minecraft Server.", 0);
                Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                    @Override
                    public void run() {
                        Minecraft.getMinecraft().displayGuiScreen(guiYesNo);
                    }
                });
            }
        });
    }

    private static void joinServer(final NetworkManager networkManager, final String serverHash, final boolean ingame) {
        final Minecraft mc = Minecraft.getMinecraft();
        try {
            mc.getSessionService().joinServer(mc.getSession().getProfile(), mc.getSession().getToken(), serverHash);
            sendResponse(networkManager, true, ingame);
        } catch (Throwable t) {
            sendResponse(networkManager, false, ingame);
        }
    }

    private static void sendResponse(final NetworkManager networkManager, boolean success, final boolean ingame) {
        try {
            if (ingame) {
                Class<?> customPayloadPacket = ReflectionUtils.getClass("net.minecraft.network.play.client.C17PacketCustomPayload", "net.minecraft.network.play.client.CPacketCustomPayload");
                Packet<?> packet = (Packet<?>) customPayloadPacket.newInstance();
                PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
                buf.writeBoolean(success);
                ReflectionUtils.setField(packet, OAM_CPL_CHANNEL, String.class, 0);
                ReflectionUtils.setField(packet, buf, PacketBuffer.class, 0);
                networkManager.sendPacket(packet);
            } else {
                Packet<?> loginStartPacket = EnumConnectionState.LOGIN.getPacket(EnumPacketDirection.SERVERBOUND, 0);
                ReflectionUtils.setField(loginStartPacket, new GameProfile(null, OAM_MAGIC_STRING + success), GameProfile.class, 0);
                networkManager.sendPacket(loginStartPacket);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            networkManager.channel().close();
        }
    }

}
