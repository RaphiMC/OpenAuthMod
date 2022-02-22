package com.github.oam;

import com.github.oam.utils.MinecraftDataInputStream;
import com.github.oam.utils.MinecraftDataOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

public abstract class OpenAuthModPlatform {

    public static final String OAM_CHANNEL = "openauthmod:join";

    protected boolean handleCustomPayloadPacket(final String channel, final byte[] data) throws IOException {
        if (channel.equals(OAM_CHANNEL)) {
            final MinecraftDataInputStream in = new MinecraftDataInputStream(new ByteArrayInputStream(data));
            final String serverHash = in.readString(64);
            this.requestAuth(serverHash, -1);
            return true;
        }
        return false;
    }

    protected boolean handleQueryRequestPacket(final String channel, final int queryId, final byte[] data) throws IOException {
        if (channel.equals(OAM_CHANNEL)) {
            final MinecraftDataInputStream in = new MinecraftDataInputStream(new ByteArrayInputStream(data));
            final String serverHash = in.readString(64);
            this.requestAuth(serverHash, queryId);
            return true;
        }
        return false;
    }

    private void requestAuth(final String serverHash, final int queryId) {
        this.openConfirmScreen("Allow Open Auth Mod authentication?", "This will allow the proxy to authenticate as you in a Minecraft Server.", () -> {
            this.sendResponse(this.joinServer(serverHash), queryId);
            return null;
        }, () -> {
            this.sendResponse(false, queryId);
            return null;
        });
    }

    private void sendResponse(final boolean success, final int queryId) throws IOException {
        if (this.isInPlayState()) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final MinecraftDataOutputStream out = new MinecraftDataOutputStream(baos);
            out.writeBoolean(success);
            this.sendCustomPayloadPacket(OAM_CHANNEL, baos.toByteArray());
        } else {
            this.sendQueryResponsePacket(queryId, success ? new byte[0] : null);
        }
    }

    protected abstract void sendCustomPayloadPacket(final String channel, final byte[] data);

    protected abstract void sendQueryResponsePacket(final int queryId, final byte[] data);

    protected abstract void openConfirmScreen(final String title, final String subTitle, final Callable<Void> yesCallback, final Callable<Void> noCallback);

    protected abstract boolean joinServer(final String serverHash);

    protected abstract boolean isInPlayState();

}
