package com.bielzinrx.attracttochat.engine;

import java.lang.ref.WeakReference;
import net.minecraft.world.entity.Mob;

public final class MobGoalData {
    private final WeakReference<Mob> mobRef;
    private final WeakReference<MoveToSoundGoal> goalRef;

    public MobGoalData(Mob mob, MoveToSoundGoal goal) {
        this.mobRef = new WeakReference<>(mob);
        this.goalRef = new WeakReference<>(goal);
    }

    public Mob mob() {
        return mobRef.get();
    }

    public MoveToSoundGoal goal() {
        return goalRef.get();
    }
}
