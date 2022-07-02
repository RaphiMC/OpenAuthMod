package net.raphimc.openauthmod;

import net.raphimc.openauthmod.utils.MinecraftDataInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public abstract class ModernOpenAuthModPlatform extends OpenAuthModPlatform {

    protected boolean handlePlayCustomPayload(final String channel, final byte[] data) throws IOException {
        if (channel.startsWith(SharedConstants.BASE_CHANNEL)) {
            final MinecraftDataInputStream in = new MinecraftDataInputStream(new ByteArrayInputStream(data));
            return this.handleOpenAuthModRequest(channel, in.readVarInt(), in);
        }
        return false;
    }

    protected boolean handleLoginCustomPayload(final String channel, final int id, final byte[] data) throws IOException {
        if (channel.startsWith(SharedConstants.BASE_CHANNEL)) {
            return this.handleOpenAuthModRequest(channel, id, new MinecraftDataInputStream(new ByteArrayInputStream(data)));
        }
        return false;
    }

}
