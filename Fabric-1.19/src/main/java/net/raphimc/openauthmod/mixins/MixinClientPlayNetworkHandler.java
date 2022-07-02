package net.raphimc.openauthmod.mixins;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.raphimc.openauthmod.OpenAuthMod;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(value = ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow
    @Final
    private ClientConnection connection;

    @Inject(method = "onCustomPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"), cancellable = true)
    private void handleOpenAuthModPackets(CustomPayloadS2CPacket packet, CallbackInfo ci) throws IOException {
        if (OpenAuthMod.getInstance().handlePlayCustomPayload(this.connection, packet.channel, packet.data)) {
            ci.cancel();
        }
    }

}
