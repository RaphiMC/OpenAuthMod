package com.github.oam.multiconnect_compat;

import com.github.oam.OpenAuthMod;
import net.earthcomputer.multiconnect.api.MultiConnectAPI;
import net.earthcomputer.multiconnect.api.Protocols;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class MultiConnectCompat {

    public static void allowOAMCustomPayloads() {
        MultiConnectAPI.instance().addClientboundIdentifierCustomPayloadListener(event -> {
            if (!event.getChannel().toString().equals(OpenAuthMod.OAM_CHANNEL)) return;
            try {
                OpenAuthMod.getInstance().handlePlayCustomPayload(event.getNetworkHandler().getConnection(), event.getChannel(), event.getData());
            } catch (Throwable e) {
                e.printStackTrace();
                event.getNetworkHandler().getConnection().channel.close();
            }
        });
        MultiConnectAPI.instance().addClientboundStringCustomPayloadListener(event -> {
            if (!event.getChannel().equals(OpenAuthMod.OAM_CHANNEL)) return;
            try {
                OpenAuthMod.getInstance().handlePlayCustomPayload(event.getNetworkHandler().getConnection(), new Identifier(event.getChannel()), event.getData());
            } catch (Throwable e) {
                e.printStackTrace();
                event.getNetworkHandler().getConnection().channel.close();
            }
        });
    }

    public static void sendPacket(final ClientPlayNetworkHandler networkHandler, final Identifier channel, final PacketByteBuf data) {
        if (MultiConnectAPI.instance().getProtocolVersion() <= Protocols.V1_12_2) {
            MultiConnectAPI.instance().forceSendStringCustomPayload(networkHandler, channel.toString(), data);
        } else {
            MultiConnectAPI.instance().forceSendCustomPayload(networkHandler, channel, data);
        }
    }

}
