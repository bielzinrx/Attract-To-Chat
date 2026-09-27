package com.bielzinrx.attracttochat.mixin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class LivingEntityMixinTargetsTest {
    @Test
    void injectedTargetsKeepTheSignaturesTheMixinsAssume() throws Exception {
        var die = LivingEntity.class.getDeclaredMethod("die", DamageSource.class);
        assertEquals(void.class, die.getReturnType(), "die must stay void for CallbackInfo");

        var eat = LivingEntity.class.getDeclaredMethod("eat", Level.class, ItemStack.class);
        assertEquals(ItemStack.class, eat.getReturnType(), "eat must return ItemStack");
    }
}
