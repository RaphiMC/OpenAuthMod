package com.github.oam.data;

public class SignedNonce {

    private final long salt;
    private final byte[] signature;

    public SignedNonce(final long salt, final byte[] signature) {
        this.salt = salt;
        this.signature = signature;
    }

    public long getSalt() {
        return this.salt;
    }

    public byte[] getSignature() {
        return this.signature;
    }

}
