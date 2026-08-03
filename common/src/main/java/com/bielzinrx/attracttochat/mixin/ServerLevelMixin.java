package com.bielzinrx.attracttochat.mixin;

import com.bielzinrx.attracttochat.engine.AtcEngine;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "addEntity", at = @At("TAIL"))
    private void atc_onAddEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        if (!(entity instanceof Mob mob)) return;
        if (mob.isRemoved()) return;

        AtcEngine.ensureMobGoal(mob);
    }
}
