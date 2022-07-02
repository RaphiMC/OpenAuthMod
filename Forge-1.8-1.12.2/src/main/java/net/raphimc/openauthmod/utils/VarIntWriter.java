package net.raphimc.openauthmod.utils;

import net.minecraft.network.PacketBuffer;

public class VarIntWriter {

    public static PacketBuffer writeVarInt(final PacketBuffer packetBuffer, int input) {
        while ((input & -128) != 0) {
            packetBuffer.writeByte(input & 127 | 128);
            input >>>= 7;
        }
        packetBuffer.writeByte(input);
        return packetBuffer;
    }

}
