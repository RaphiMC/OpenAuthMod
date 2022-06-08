package com.github.oam;

import com.github.oam.utils.IntTo3ByteCodec;
import com.github.oam.utils.MinecraftDataInputStream;

import java.io.*;
import java.util.Arrays;
import java.util.Base64;

public abstract class LegacyOpenAuthModPlatform extends OpenAuthModPlatform {

    protected boolean handlePlayCustomPayload(final String channel, final byte[] data) throws IOException {
        if (channel.startsWith(SharedConstants.BASE_CHANNEL)) {
            final MinecraftDataInputStream in = new MinecraftDataInputStream(new ByteArrayInputStream(data));
            return this.handleOpenAuthModRequest(channel, in.readVarInt(), in);
        }
        return false;
    }

    protected boolean handleLoginDisconnect(final String reason) throws IOException {
        final String[] parts = reason.split("\n");
        if (parts.length == 3 && Arrays.equals(parts[2].getBytes(), SharedConstants.LEGACY_MAGIC_BYTES)) {
            final MinecraftDataInputStream in = new MinecraftDataInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(parts[1])));
            return this.handleOpenAuthModRequest(in.readString(20), in.readVarInt(), in);
        }
        return false;
    }

    private ByteArrayOutputStream compressionDataStream = null;

    protected boolean handleLoginSetCompression(final int threshold) throws IOException {
        if (threshold < 0) {
            if (this.compressionDataStream == null && threshold == SharedConstants.LEGACY_MAGIC_INT) { // begin stream
                this.compressionDataStream = new ByteArrayOutputStream();
                return true;
            } else if (this.compressionDataStream != null && threshold == SharedConstants.LEGACY_MAGIC_INT) { // end stream
                final MinecraftDataInputStream in = new MinecraftDataInputStream(new ByteArrayInputStream(this.compressionDataStream.toByteArray()));
                this.compressionDataStream = null;
                this.handleOpenAuthModRequest(in.readString(20), in.readVarInt(), in);
                return true;
            } else if (this.compressionDataStream != null) { // data part
                this.compressionDataStream.write(IntTo3ByteCodec.decode(threshold));
                return true;
            }
        }
        return false;
    }

}
