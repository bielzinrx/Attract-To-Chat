package com.bielzinrx.attracttochat.mixin;

import com.bielzinrx.attracttochat.engine.AtcEngine;
import com.bielzinrx.attracttochat.fatigue.FatigueTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MilkBucketItem.class)
public class MilkBucketItemMixin {

    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void atc_onMilkDrink(ItemStack stack, Level level, LivingEntity entity,
            CallbackInfoReturnable<ItemStack> cir) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) return;
        FatigueTracker.clear(player.getUUID());
        AtcEngine.clearMute(player.getUUID());
    }
}
