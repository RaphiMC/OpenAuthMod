package net.raphimc.openauthmod;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.client.CCustomPayloadLoginPacket;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.concurrent.Callable;

@Mod("openauthmod")
public class OpenAuthMod extends ModernOpenAuthModPlatform {

    private static OpenAuthMod INSTANCE;

    public static OpenAuthMod getInstance() {
        return INSTANCE;
    }


    private final Minecraft mc = Minecraft.getInstance();
    private NetworkManager networkManager;

    public OpenAuthMod() {
        INSTANCE = this;
    }


    public boolean handlePlayCustomPayload(final NetworkManager networkManager, final ResourceLocation channel, final PacketBuffer data) throws IOException {
        this.networkManager = networkManager;

        return this.handlePlayCustomPayload(channel.toString(), ByteBufUtil.getBytes(data));
    }

    public boolean handleLoginCustomPayload(final NetworkManager networkManager, final ResourceLocation channel, final int queryId, final PacketBuffer data) throws IOException {
        this.networkManager = networkManager;

        return this.handleLoginCustomPayload(channel.toString(), queryId, ByteBufUtil.getBytes(data));
    }


    @Override
    protected void sendResponse(int id, byte[] data) {
        if (this.networkManager.channel().attr(NetworkManager.ATTRIBUTE_PROTOCOL).get().getId() == 0) {
            final PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
            packetBuffer.writeVarInt(id);
            packetBuffer.writeBytes(data);
            this.networkManager.send(new CCustomPayloadPacket(new ResourceLocation(SharedConstants.DATA_CHANNEL), packetBuffer));
        } else {
            this.networkManager.send(new CCustomPayloadLoginPacket(id, new PacketBuffer(Unpooled.wrappedBuffer(data))));
        }
    }

    @Override
    protected void openConfirmScreen(String title, String subTitle, Callable<Void> yesCallback, Callable<Void> noCallback) {
        this.mc.execute(() -> {
            final Screen parentScreen = this.mc.screen;
            this.mc.setScreen(new ConfirmScreen(success -> {
                try {
                    if (success) yesCallback.call();
                    else noCallback.call();
                } catch (Throwable e) {
                    e.printStackTrace();
                    this.networkManager.channel().close();
                }
                this.mc.setScreen(parentScreen);
            }, new StringTextComponent(title), new StringTextComponent(subTitle)));
        });
    }

    @Override
    protected boolean joinServer(String serverIdHash) {
        try {
            this.mc.getMinecraftSessionService().joinServer(this.mc.getUser().getGameProfile(), this.mc.getUser().getAccessToken(), serverIdHash);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

}
