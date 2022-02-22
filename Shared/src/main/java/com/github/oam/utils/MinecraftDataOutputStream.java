package com.github.oam.utils;

import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MinecraftDataOutputStream extends DataOutputStream {

    /**
     * Creates a MinecraftDataOutputStream to write data to the specified
     * underlying output stream. The counter <code>written</code> is
     * set to zero.
     *
     * @param out the underlying output stream, to be saved for later
     *            use.
     * @see FilterOutputStream#out
     */
    public MinecraftDataOutputStream(final OutputStream out) {
        super(out);
    }

    public void writeVarInt(int input) throws IOException {
        while ((input & -128) != 0) {
            this.writeByte(input & 127 | 128);
            input >>>= 7;
        }
        this.writeByte(input);
    }

    public void writeString(final String string) throws IOException {
        final byte[] data = string.getBytes(StandardCharsets.UTF_8);

        if (data.length > Short.MAX_VALUE) {
            throw new IllegalStateException("String too big (was " + data.length + " bytes encoded, max " + Short.MAX_VALUE + ")");
        } else {
            this.writeVarInt(data.length);
            this.write(data);
        }
    }

}
