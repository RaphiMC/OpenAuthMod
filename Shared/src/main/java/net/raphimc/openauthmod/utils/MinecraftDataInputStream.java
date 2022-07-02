package net.raphimc.openauthmod.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MinecraftDataInputStream extends DataInputStream {

    /**
     * Creates a MinecraftDataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public MinecraftDataInputStream(final InputStream in) {
        super(in);
    }

    public int readVarInt() throws IOException {
        int i = 0;
        int j = 0;

        while (true) {
            final byte b0 = this.readByte();
            i |= (b0 & 127) << j++ * 7;

            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
            if ((b0 & 128) != 128) {
                break;
            }
        }

        return i;
    }

    public byte[] readByteArray() throws IOException {
        int i = this.readVarInt();
        byte[] bs = new byte[i];
        this.readFully(bs);
        return bs;
    }

    public String readString(final int maxLength) throws IOException {
        final int l = this.readVarInt();

        if (l > maxLength * 4) {
            throw new IllegalStateException("The received encoded string buffer length is longer than maximum allowed (" + l + " > " + maxLength * 4 + ")");
        } else if (l < 0) {
            throw new IllegalStateException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            final byte[] data = new byte[l];
            this.readFully(data);
            final String s = new String(data, StandardCharsets.UTF_8);

            if (s.length() > maxLength) {
                throw new IllegalStateException("The received string length is longer than maximum allowed (" + l + " > " + maxLength + ")");
            } else {
                return s;
            }
        }
    }

}
