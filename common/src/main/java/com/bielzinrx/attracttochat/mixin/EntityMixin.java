package com.bielzinrx.attracttochat.mixin;

import com.bielzinrx.attracttochat.engine.AtcEngine;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "remove", at = @At("HEAD"))
    private void atc_unregisterOnRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level.isClientSide()) return;
        if (self instanceof Mob) {
            AtcEngine.unregisterMob(self.getUUID());
        }
    }
}
