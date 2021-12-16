package com.github.oam.mixins;

import com.github.oam.OpenAuthMod;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Inject(method = "onCustomPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"), cancellable = true)
    private void handleOAM(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (packet.channel.equals(OpenAuthMod.OAM_CHANNEL)) {
            OpenAuthMod.handlePlayCustomPayload(packet.channel, packet.data);
            ci.cancel();
        }
    }

}
