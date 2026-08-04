package com.bielzinrx.attracttochat.mixin;

import com.bielzinrx.attracttochat.engine.AtcEngine;
import com.bielzinrx.attracttochat.fatigue.FatigueTracker;
import com.bielzinrx.attracttochat.i18n.ServerTranslations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "updateOptions", at = @At("TAIL"))
    private void atc_captureClientLanguage(ServerboundClientInformationPacket packet, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ServerTranslations.rememberPlayerLanguage(player.getUUID(), packet.language());
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void atc_saveFatigue(CompoundTag nbt, CallbackInfo ci) {
        java.util.UUID id = ((ServerPlayer) (Object) this).getUUID();
        FatigueTracker.saveForPlayer(id, nbt);
        AtcEngine.saveMuteForPlayer(id, nbt);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void atc_loadFatigue(CompoundTag nbt, CallbackInfo ci) {
        java.util.UUID id = ((ServerPlayer) (Object) this).getUUID();
        FatigueTracker.loadForPlayer(id, nbt);
        AtcEngine.loadMuteForPlayer(id, nbt);
    }
}
