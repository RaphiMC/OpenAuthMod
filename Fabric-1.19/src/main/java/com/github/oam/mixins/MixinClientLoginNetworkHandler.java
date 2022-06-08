package com.github.oam.mixins;

import com.github.oam.OpenAuthMod;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(ClientLoginNetworkHandler.class)
public abstract class MixinClientLoginNetworkHandler {

    @Shadow
    @Final
    private ClientConnection connection;

    @Inject(method = "onQueryRequest", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", remap = false), cancellable = true)
    private void handleOpenAuthModPackets(LoginQueryRequestS2CPacket packet, CallbackInfo ci) throws IOException {
        if (OpenAuthMod.getInstance().handleLoginCustomPayload(this.connection, packet.channel, packet.queryId, packet.payload)) {
            ci.cancel();
        }
    }

}
