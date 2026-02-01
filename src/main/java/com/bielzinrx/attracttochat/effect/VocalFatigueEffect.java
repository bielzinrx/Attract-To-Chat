package com.bielzinrx.attracttochat.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class VocalFatigueEffect extends MobEffect {
    public VocalFatigueEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false; // Não precisa rodar lógica a cada tick, apenas bloqueamos ações
    }
}