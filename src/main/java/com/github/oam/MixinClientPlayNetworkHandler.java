package com.github.oam;

import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow
    private MinecraftClient client;

    @Shadow
    @Final
    private ClientConnection connection;

    @Inject(method = "onCustomPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"), cancellable = true)
    public void handleOAM(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (packet.channel.toString().equals("openauthmod:join")) {
            ci.cancel();

            final Screen parentScreen = this.client.currentScreen;
            final String serverHash = packet.data.readString();
            this.client.execute(() -> {
                this.client.openScreen(new ConfirmScreen(success -> {
                    final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    if (success) {
                        try {
                            this.client.getSessionService().joinServer(this.client.getSession().getProfile(), this.client.getSession().getAccessToken(), serverHash);
                            buf.writeBoolean(true);
                        } catch (Throwable t) {
                            buf.writeBoolean(false);
                        }
                    } else {
                        buf.writeBoolean(false);
                    }
                    this.client.openScreen(parentScreen);
                    this.connection.send(new CustomPayloadC2SPacket(packet.channel, buf));
                }, new LiteralText("Allow Open Auth Mod authentication?"), new LiteralText("This will allow the proxy to authenticate as you in a Minecraft Server.")));
            });
        }
    }

}
