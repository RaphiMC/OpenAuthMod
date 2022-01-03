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
            if (!event.getChannel().equals(OpenAuthMod.OAM_CHANNEL)) return;
            OpenAuthMod.handlePlayCustomPayload(event.getNetworkHandler(), event.getChannel(), event.getData());
        });
        MultiConnectAPI.instance().addClientboundStringCustomPayloadListener(event -> {
            if (!event.getChannel().equals(OpenAuthMod.OAM_CHANNEL.toString())) return;
            OpenAuthMod.handlePlayCustomPayload(event.getNetworkHandler(), new Identifier(event.getChannel()), event.getData());
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
