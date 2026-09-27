package com.bielzinrx.attracttochat.fabric.mixin;

import com.bielzinrx.attracttochat.fabric.AtcFabricChat;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundChatPacket.class)
public class ServerboundChatPacketMixin {

    @Inject(method = "handle(Lnet/minecraft/network/protocol/game/ServerGamePacketListener;)V",
            at = @At("HEAD"), cancellable = true)
    private void atc_onChatPacket(net.minecraft.network.protocol.game.ServerGamePacketListener listener,
            CallbackInfo ci) {
        String message = ((ServerboundChatPacket) (Object) this).message();
        if (AtcFabricChat.onChatPacket(listener, message)) {
            ci.cancel();
        }
    }
}
