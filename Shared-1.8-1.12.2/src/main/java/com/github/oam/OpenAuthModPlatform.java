package com.github.oam;

import com.github.oam.utils.IntTo3ByteCodec;
import com.github.oam.utils.MinecraftDataInputStream;
import com.github.oam.utils.MinecraftDataOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Callable;

public abstract class OpenAuthModPlatform {

    public static final String OAM_CHANNEL = "openauthmod:join";

    public static final byte[] OAM_MAGIC_BYTES = new byte[]{2, 20, 12, 3};
    public static final String OAM_MAGIC_STRING = new String(OAM_MAGIC_BYTES, StandardCharsets.UTF_8);
    public static final int OAM_MAGIC_INT = new BigInteger(OAM_MAGIC_BYTES).intValueExact();

    private ByteArrayOutputStream compressionDataStream = null;

    protected boolean handleCustomPayloadPacket(final String channel, final byte[] data) throws IOException {
        if (channel.equals(OAM_CHANNEL)) {
            final MinecraftDataInputStream in = new MinecraftDataInputStream(new ByteArrayInputStream(data));
            final String serverHash = in.readString(64);
            this.requestAuth(serverHash);
            return true;
        }
        return false;
    }

    protected boolean handleDisconnectPacket(final String reason) {
        final String[] parts = reason.split("\n");
        if (parts.length == 3 && Arrays.equals(parts[2].getBytes(), OAM_MAGIC_BYTES)) {
            this.requestAuth(parts[1]);
            return true;
        }
        return false;
    }

    protected boolean handleSetCompressionPacket(final int threshold) throws IOException {
        if (threshold < 0) {
            if (this.compressionDataStream == null && threshold == OAM_MAGIC_INT) { // begin
                this.compressionDataStream = new ByteArrayOutputStream();
                return true;
            } else if (this.compressionDataStream != null && threshold == OAM_MAGIC_INT) { // end
                final String serverHash = new BigInteger(this.compressionDataStream.toByteArray()).toString(16);
                this.compressionDataStream = null;
                this.requestAuth(serverHash);
                return true;
            } else if (this.compressionDataStream != null) { // data
                this.compressionDataStream.write(IntTo3ByteCodec.decode(threshold));
                return true;
            }
        }
        return false;
    }

    private void requestAuth(final String serverHash) {
        this.openConfirmScreen("Allow Open Auth Mod authentication?", "This will allow the proxy to authenticate as you in a Minecraft Server.", () -> {
            this.sendResponse(this.joinServer(serverHash));
            return null;
        }, () -> {
            this.sendResponse(false);
            return null;
        });
    }

    private void sendResponse(final boolean success) throws IOException {
        if (this.isInPlayState()) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final MinecraftDataOutputStream out = new MinecraftDataOutputStream(baos);
            out.writeBoolean(success);
            this.sendCustomPayloadPacket(OAM_CHANNEL, baos.toByteArray());
        } else {
            this.sendLoginHelloPacket(OAM_MAGIC_STRING + success);
        }
    }

    protected abstract void sendCustomPayloadPacket(final String channel, final byte[] data);

    protected abstract void sendLoginHelloPacket(final String username);

    protected abstract void openConfirmScreen(final String title, final String subTitle, final Callable<Void> yesCallback, final Callable<Void> noCallback);

    protected abstract boolean joinServer(final String serverHash);

    protected abstract boolean isInPlayState();

}
