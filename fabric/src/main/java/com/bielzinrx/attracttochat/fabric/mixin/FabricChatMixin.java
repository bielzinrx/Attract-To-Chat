package com.bielzinrx.attracttochat.fabric.mixin;

import com.bielzinrx.attracttochat.engine.AtcEngine;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class FabricChatMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
    private void atc_onChat(ServerboundChatPacket packet, CallbackInfo ci) {
        String content = packet.message();
        if (content == null || content.isEmpty()) return;

        if (AtcEngine.handleChatCancellable(player, content)) {
            ci.cancel();
            return;
        }

        AtcEngine.handleChatAfter(player, content);
    }
}
