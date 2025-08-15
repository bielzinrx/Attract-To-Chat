package com.bielzinrx.attracttochat;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class MoveToMessageGoal extends Goal {
    private final Mob mob;
    private Vec3 targetPos;
    private long heardGameTime;
    private final double speed;

    public MoveToMessageGoal(Mob mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    public void setTarget(Vec3 pos, long gameTime) {
        this.targetPos = pos;
        this.heardGameTime = gameTime;
    }

    @Override
    public boolean canUse() {
        if (targetPos == null) return false;
        // Se já passou do tempo configurado, esquece
        long forgetTicks = AttractToChatConfig.SERVER.forgetTargetAfterSeconds.get() * 20L;
        if (mob.level().getGameTime() - heardGameTime > forgetTicks) {
            targetPos = null;
            return false;
        }
        // Só anda se estiver longe o suficiente
        double distSq = mob.distanceToSqr(targetPos.x, targetPos.y, targetPos.z);
        return distSq > 4.0;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPos == null) return false;
        long forgetTicks = AttractToChatConfig.SERVER.forgetTargetAfterSeconds.get() * 20L;
        if (mob.level().getGameTime() - heardGameTime > forgetTicks) {
            targetPos = null;
            return false;
        }
        double distSq = mob.distanceToSqr(targetPos.x, targetPos.y, targetPos.z);
        return distSq > 4.0;
    }

    @Override
    public void start() {
        if (targetPos != null) {
            mob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
        }
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        targetPos = null;
    }

    @Override
    public void tick() {
        if (targetPos != null) {
            double distSq = mob.distanceToSqr(targetPos.x, targetPos.y, targetPos.z);
            if (distSq > 4.0) {
                mob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
            }
        }
    }
}
