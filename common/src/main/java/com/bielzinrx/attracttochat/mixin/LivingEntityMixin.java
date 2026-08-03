package com.bielzinrx.attracttochat.mixin;

import com.bielzinrx.attracttochat.engine.AtcEngine;
import com.bielzinrx.attracttochat.fatigue.FatigueTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "eat", at = @At("HEAD"))
    private void onEat(Level level, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (level.isClientSide()) return;

        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer player) {

            if (stack.is(Items.HONEY_BOTTLE)) {
                FatigueTracker.reduceFatigue(player.getUUID(), 1200);
            }
        }
    }

    @Inject(method = "die", at = @At("TAIL"))
    private void onDie(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level.isClientSide()) return;
        if (self instanceof ServerPlayer player) {
            FatigueTracker.clear(player.getUUID());
            AtcEngine.clearMute(player.getUUID());
        } else if (self instanceof Mob) {
            AtcEngine.unregisterMob(self.getUUID());
        }
    }
}
