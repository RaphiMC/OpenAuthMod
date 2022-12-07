package net.raphimc.openauthmod;

import com.google.common.primitives.Longs;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.encryption.Signer;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.raphimc.openauthmod.data.SignedNonce;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;

public class OpenAuthMod extends ModernOpenAuthModPlatform implements ClientModInitializer {

    private static OpenAuthMod INSTANCE;

    public static OpenAuthMod getInstance() {
        return INSTANCE;
    }


    private final MinecraftClient mc = MinecraftClient.getInstance();
    private ClientConnection clientConnection;

    public OpenAuthMod() {
        INSTANCE = this;
    }

    @Override
    public void onInitializeClient() {
    }


    public boolean handlePlayCustomPayload(final ClientConnection clientConnection, final Identifier channel, final PacketByteBuf data) throws IOException {
        this.clientConnection = clientConnection;

        return this.handlePlayCustomPayload(channel.toString(), ByteBufUtil.getBytes(data));
    }

    public boolean handleLoginCustomPayload(final ClientConnection clientConnection, final Identifier channel, final int queryId, final PacketByteBuf data) throws IOException {
        this.clientConnection = clientConnection;

        return this.handleLoginCustomPayload(channel.toString(), queryId, ByteBufUtil.getBytes(data));
    }


    @Override
    protected void sendResponse(int id, byte[] data) {
        if (this.clientConnection.channel.attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get().getId() == 0) {
            final PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
            packetByteBuf.writeVarInt(id);
            packetByteBuf.writeBytes(data);
            this.clientConnection.send(new CustomPayloadC2SPacket(new Identifier(SharedConstants.DATA_CHANNEL), packetByteBuf));
        } else {
            this.clientConnection.send(new LoginQueryResponseC2SPacket(id, new PacketByteBuf(Unpooled.wrappedBuffer(data))));
        }
    }

    @Override
    protected void openConfirmScreen(String title, String subTitle, Callable<Void> yesCallback, Callable<Void> noCallback) {
        if (this.clientConnection.channel.attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get().getId() == 2) {
            if (this.clientConnection.channel.remoteAddress() instanceof InetSocketAddress address) {
                if (address.getAddress().isLoopbackAddress()) {
                    try {
                        yesCallback.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.clientConnection.channel.close();
                    }
                    return;
                }
            }
        }

        this.mc.execute(() -> {
            final Screen parentScreen = mc.currentScreen;
            this.mc.setScreen(new ConfirmScreen(success -> {
                try {
                    if (success) yesCallback.call();
                    else noCallback.call();
                } catch (Throwable e) {
                    e.printStackTrace();
                    this.clientConnection.channel.close();
                }
                this.mc.setScreen(parentScreen);
            }, Text.literal(title), Text.literal(subTitle)));
        });
    }

    @Override
    protected boolean joinServer(String serverIdHash) {
        try {
            this.mc.getSessionService().joinServer(this.mc.getSession().getProfile(), this.mc.getSession().getAccessToken(), serverIdHash);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    protected SignedNonce signNonce(byte[] nonce) {
        try {
            final Signer signer = this.mc.getProfileKeys().getSigner();
            if (signer == null) return null;

            final long salt = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
            final byte[] signature = signer.sign(updater -> {
                updater.update(nonce);
                updater.update(Longs.toByteArray(salt));
            });
            return new SignedNonce(salt, signature);
        } catch (Throwable e) {
            return null;
        }
    }

}
