package net.raphimc.openauthmod;

import net.raphimc.openauthmod.data.SignedNonce;
import net.raphimc.openauthmod.utils.MinecraftDataInputStream;
import net.raphimc.openauthmod.utils.MinecraftDataOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

public abstract class OpenAuthModPlatform {

    protected boolean handleOpenAuthModRequest(final String channel, final int id, final MinecraftDataInputStream data) throws IOException {
        if (channel.equals(SharedConstants.JOIN_CHANNEL)) {
            final String serverIdHash = data.readString(64);
            this.confirmAuth(serverIdHash, id);
            return true;
        } else if (channel.equals(SharedConstants.SIGN_NONCE_CHANNEL)) {
            final byte[] nonce = data.readByteArray();
            final SignedNonce signedNonce = this.signNonce(nonce);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final MinecraftDataOutputStream out = new MinecraftDataOutputStream(baos);
            out.writeBoolean(signedNonce != null);
            if (signedNonce != null) {
                out.writeLong(signedNonce.getSalt());
                out.writeByteArray(signedNonce.getSignature());
            }
            this.sendResponse(id, baos.toByteArray());
            return true;
        }
        return false;
    }

    private void confirmAuth(final String serverIdHash, final int id) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final MinecraftDataOutputStream out = new MinecraftDataOutputStream(baos);
        this.openConfirmScreen("Allow Open Auth Mod authentication?", "This will allow the proxy to authenticate as you on a Minecraft Server.", () -> {
            out.writeBoolean(this.joinServer(serverIdHash));
            this.sendResponse(id, baos.toByteArray());
            return null;
        }, () -> {
            out.writeBoolean(false);
            this.sendResponse(id, baos.toByteArray());
            return null;
        });
    }

    protected abstract void sendResponse(final int id, final byte[] data);

    protected abstract void openConfirmScreen(final String title, final String subTitle, final Callable<Void> yesCallback, final Callable<Void> noCallback);

    protected abstract boolean joinServer(final String serverIdHash);

    protected SignedNonce signNonce(final byte[] nonce) {
        return null;
    }

}
