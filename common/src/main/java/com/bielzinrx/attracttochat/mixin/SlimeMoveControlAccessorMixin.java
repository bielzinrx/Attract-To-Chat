package com.bielzinrx.attracttochat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.world.entity.monster.Slime$SlimeMoveControl")
public interface SlimeMoveControlAccessorMixin {
    @Invoker("setDirection")
    void atc_setDirection(float yRot, boolean aggressive);

    @Invoker("setWantedMovement")
    void atc_setWantedMovement(double speed);
}
