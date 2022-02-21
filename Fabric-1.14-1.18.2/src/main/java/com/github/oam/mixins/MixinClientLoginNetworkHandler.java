package com.github.oam.mixins;

import com.github.oam.OpenAuthMod;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLoginNetworkHandler.class)
public abstract class MixinClientLoginNetworkHandler {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private ClientConnection connection;

    @Shadow
    @Nullable
    protected abstract Text joinServerSession(String serverId);

    @Inject(method = "onQueryRequest", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", remap = false), cancellable = true)
    private void handleOAM(LoginQueryRequestS2CPacket packet, CallbackInfo ci) {
        if (packet.channel.equals(OpenAuthMod.OAM_CHANNEL)) {
            ci.cancel();

            final Screen parentScreen = this.client.currentScreen;
            final String serverHash = packet.payload.readString();
            this.client.execute(() -> {
                this.client.openScreen(new ConfirmScreen(success -> {
                    if (success) {
                        if (this.joinServerSession(serverHash) == null) {
                            this.connection.send(new LoginQueryResponseC2SPacket(packet.queryId, new PacketByteBuf(Unpooled.buffer())));
                        } else {
                            this.connection.send(new LoginQueryResponseC2SPacket(packet.queryId, null));
                        }
                    } else {
                        this.connection.send(new LoginQueryResponseC2SPacket(packet.queryId, null));
                    }
                    this.client.openScreen(parentScreen);
                }, new LiteralText("Allow Open Auth Mod authentication?"), new LiteralText("This will allow the proxy to authenticate as you in a Minecraft Server.")));
            });
        }
    }

}
