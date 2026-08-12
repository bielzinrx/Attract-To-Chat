package com.bielzinrx.attracttochat.engine;

import com.bielzinrx.attracttochat.AttractToChat;
import com.bielzinrx.attracttochat.mixin.SlimeMoveControlAccessorMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import java.util.EnumSet;
import java.util.UUID;

public class MoveToSoundGoal extends Goal {
    public enum InvestigationStartResult {
        STARTED(true, "investigation started"),
        VILLAGER_SAFETY(false, "villager safety has priority"),
        TROLL_TARGET_LOCKED(false, "higher-priority Troll Mode target is active"),
        COMBAT_PRIORITY(false, "combat target has priority"),
        NO_REACHABLE_DESTINATION(false, "no standable destination below the sound source"),
        PATH_NOT_CREATED(false, "navigation could not create a path");

        private final boolean started;
        private final String debugReason;

        InvestigationStartResult(boolean started, String debugReason) {
            this.started = started;
            this.debugReason = debugReason;
        }

        public boolean started() {
            return started;
        }

        public String debugReason() {
            return debugReason;
        }
    }

    private final Mob mob;
    private int baseTicks;
    private BlockPos targetPos;
    private UUID playerUUID;
    private int timeout;
    private double currentSpeed;
    private double currentRangeSq;
    private int recalcDelay;
    private boolean lockedToTrollTarget;
    private boolean blockTarget;
    private boolean urgentShout;

    private boolean preserveVillagerSafetyState;

    private int particleCooldown;

    private static final double ARRIVAL_DIST_SQ = 6.25;

    private static final int NEAR_SOUND_LINGER_TICKS = 40;

    private boolean followLivePlayer;

    public MoveToSoundGoal(Mob mob, int baseTicks) {
        this.mob = mob;
        this.baseTicks = baseTicks;
        this.currentSpeed = 1.0;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public InvestigationStartResult setTarget(BlockPos pos, MessageScore score, double range, UUID playerUUID,
            double speed, boolean trollTarget, boolean blockTarget) {

        if (mob instanceof EnderMan) {
            trollTarget = false;
        }

        if (shouldVillagerPrioritizeSafety(mob)) {
            yieldToVillagerSafety();
            return InvestigationStartResult.VILLAGER_SAFETY;
        }
        preserveVillagerSafetyState = false;

        if (lockedToTrollTarget && !trollTarget && resolveTargetPlayer() != null) {
            return InvestigationStartResult.TROLL_TARGET_LOCKED;
        }

        if (!trollTarget && hasCombatTarget()) {
            return InvestigationStartResult.COMBAT_PRIORITY;
        }

        this.blockTarget = blockTarget;
        this.playerUUID = playerUUID;
        this.currentSpeed = speed > 0.0 && !Double.isNaN(speed) && !Double.isInfinite(speed) ? speed : 1.0;

        this.lockedToTrollTarget = trollTarget && !blockTarget;

        this.followLivePlayer = false;
        BlockPos safeTarget = sanitizeTarget(pos, range);
        if (safeTarget == null) {
            clearSoundInvestigation();
            return InvestigationStartResult.NO_REACHABLE_DESTINATION;
        }
        this.targetPos = safeTarget;

        boolean isShout = "shout".equals(score.factor);
        this.urgentShout = isShout;

        this.timeout = this.baseTicks;

        double tolerance = 5.0;
        this.currentRangeSq = (range + tolerance) * (range + tolerance);
        this.recalcDelay = 0;

        this.particleCooldown = 0;

        boolean started;
        if (mob instanceof Villager villager) {
            started = setVillagerWalkTarget(villager);
        } else if (mob instanceof Slime) {
            started = directSlime();
        } else {
            started = safeNavigateToTarget();
        }

        if (!started) {
            clearSoundInvestigation();
            return InvestigationStartResult.PATH_NOT_CREATED;
        }

        if (!this.lockedToTrollTarget && this.targetPos != null) {
            AtcEngine.trySpawnPathParticles(mob, this.targetPos, true);
        }
        return InvestigationStartResult.STARTED;
    }

    public UUID getTrackedPlayerId() {
        return playerUUID;
    }

    public BlockPos getInvestigationTarget() {
        return targetPos;
    }

    public void updateBaseTicks(int baseTicks) {
        this.baseTicks = Math.max(20, baseTicks);
    }

    @Override
    public boolean canUse() {
        if (targetPos == null || timeout <= 0) return false;
        if (shouldVillagerPrioritizeSafety(mob)) {
            yieldToVillagerSafety();
            return false;
        }

        if (lockedToTrollTarget) {
            ServerPlayer player = resolveTargetPlayer();

            if (player == null) {
                clearSoundInvestigation();
                return false;
            }
            BlockPos safeTarget = sanitizeTarget(player.blockPosition(), Math.sqrt(currentRangeSq));
            if (safeTarget == null) {
                clearSoundInvestigation();
                return false;
            }
            targetPos = safeTarget;
            mob.setTarget(player);
            return true;
        }

        if (followLivePlayer && playerUUID != null && resolveTargetPlayer() == null) {
            clearSoundInvestigation();
            return false;
        }

        if (!isTargetLoadedAndValid()) {
            clearSoundInvestigation();
            return false;
        }

        if (shouldYieldToCombat()) {
            clearSoundInvestigation();
            return false;
        }

        double distSq = mob.blockPosition().distSqr(targetPos);

        if (distSq > currentRangeSq) {
            clearSoundInvestigation();
            return false;
        }

        if (!blockTarget && !lockedToTrollTarget && distSq <= ARRIVAL_DIST_SQ) {
            clampNearLingerTimeout();
            return timeout > 0;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPos == null || timeout <= 0) return false;
        if (shouldVillagerPrioritizeSafety(mob)) {
            yieldToVillagerSafety();
            return false;
        }
        if (lockedToTrollTarget) {
            ServerPlayer trollPlayer = resolveTargetPlayer();
            if (trollPlayer == null) {
                clearSoundInvestigation();
                return false;
            }

            if (mob.blockPosition().distSqr(trollPlayer.blockPosition()) > currentRangeSq) {
                clearSoundInvestigation();
                return false;
            }
            return true;
        }
        if (followLivePlayer && playerUUID != null && resolveTargetPlayer() == null) {
            clearSoundInvestigation();
            return false;
        }
        if (!isTargetLoadedAndValid()) {
            clearSoundInvestigation();
            return false;
        }
        if (shouldYieldToCombat()) {
            clearSoundInvestigation();
            return false;
        }

        double distSq = mob.blockPosition().distSqr(targetPos);

        if (lockedToTrollTarget) {
            return true;
        }

        if (distSq > ARRIVAL_DIST_SQ) return true;
        clampNearLingerTimeout();
        return timeout > 0;
    }

    private void clampNearLingerTimeout() {
        if (timeout > NEAR_SOUND_LINGER_TICKS) {
            timeout = NEAR_SOUND_LINGER_TICKS;
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {

        return targetPos != null && timeout > 0;
    }

    @Override
    public void tick() {
        try {
            tickBody();
        } catch (Throwable t) {

            AttractToChat.LOGGER.warn(
                "MoveToSoundGoal tick failed for {} at {}: {}",
                mob.getType(), AtcEngine.formatCoordinates(targetPos), t.toString());
            clearSoundInvestigation();
        }
    }

    private void tickBody() {
        timeout--;
        if (recalcDelay > 0) recalcDelay--;

        if (shouldVillagerPrioritizeSafety(mob)) {
            yieldToVillagerSafety();
            return;
        }

        if (shouldYieldToCombat()) {
            clearSoundInvestigation();
            return;
        }

        if (lockedToTrollTarget) {
            ServerPlayer player = resolveTargetPlayer();
            if (player == null) {
                clearSoundInvestigation();
                return;
            }
            BlockPos safeTarget = sanitizeTarget(player.blockPosition(), Math.sqrt(currentRangeSq));
            if (safeTarget == null) {
                clearSoundInvestigation();
                return;
            }
            targetPos = safeTarget;
            mob.setTarget(player);
        }

        if (!isTargetLoadedAndValid() || targetPos == null) {
            clearSoundInvestigation();
            return;
        }

        maybeSpawnPathParticles();

        final BlockPos lookAt = targetPos;

        mob.getLookControl().setLookAt(
            lookAt.getX() + 0.5, lookAt.getY(), lookAt.getZ() + 0.5);

        if (mob.blockPosition().distSqr(lookAt) > ARRIVAL_DIST_SQ) {
            if (mob instanceof Villager villager) {
                if (recalcDelay <= 0) setVillagerWalkTarget(villager);
            } else if (mob instanceof Slime) {
                directSlime();
            }
            if (recalcDelay <= 0 && isTargetLoadedAndValid()) {
                if (!(mob instanceof Slime) && !(mob instanceof Villager)
                        && (mob.getNavigation().isDone() || mob.getRandom().nextInt(15) == 0)) {
                    safeNavigateToTarget();
                }
                recalcDelay = 20 + mob.getRandom().nextInt(21);
            }
        } else {
            safeStopNavigation();
        }
    }

    private void maybeSpawnPathParticles() {
        if (lockedToTrollTarget || targetPos == null) return;
        if (particleCooldown > 0) {
            particleCooldown--;
            return;
        }
        AtcEngine.trySpawnPathParticles(mob, targetPos, false);

        particleCooldown = urgentShout ? 6 : 8;
    }

    private BlockPos sanitizeTarget(BlockPos pos, double range) {
        if (pos == null || !(mob.level() instanceof ServerLevel level)) return null;
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;
        int y = Math.max(minY, Math.min(maxY, pos.getY()));
        BlockPos clamped = new BlockPos(pos.getX(), y, pos.getZ()).immutable();
        if (!level.isLoaded(clamped)) return null;

        if (AtcEngine.usesAirNavigation(mob)) {
            return clamped;
        }

        if (!blockTarget) {
            BlockPos ground = findStandableBelow(level, clamped, groundSearchDepth(range));
            if (ground == null) return null;
            clamped = ground;
        }
        return clamped;
    }

    private static int groundSearchDepth(double hearingRange) {
        if (!Double.isFinite(hearingRange)) return 8;
        return Math.max(8, (int) Math.ceil(Math.max(0.0, hearingRange) + 5.0));
    }

    private BlockPos findStandableBelow(ServerLevel level, BlockPos origin, int maxSearchDepth) {
        int feetY = GroundTargetResolver.findFeetY(
            origin.getY(), level.getMinBuildHeight(), maxSearchDepth,
            floorY -> isStandableFloor(level, new BlockPos(origin.getX(), floorY, origin.getZ())));
        return feetY == GroundTargetResolver.NO_TARGET
            ? null
            : new BlockPos(origin.getX(), feetY, origin.getZ()).immutable();
    }

    private static boolean isStandableFloor(ServerLevel level, BlockPos floorPos) {
        BlockPos feetPos = floorPos.above();
        BlockPos headPos = feetPos.above();
        if (floorPos.getY() < level.getMinBuildHeight()
                || headPos.getY() >= level.getMaxBuildHeight()
                || !level.isLoaded(floorPos)
                || !level.isLoaded(headPos)) {
            return false;
        }
        return !level.getBlockState(floorPos).isAir()
            && !level.getBlockState(floorPos).getCollisionShape(level, floorPos).isEmpty()
            && level.getBlockState(feetPos).getCollisionShape(level, feetPos).isEmpty()
            && level.getBlockState(headPos).getCollisionShape(level, headPos).isEmpty();
    }

    private boolean isTargetLoadedAndValid() {
        return targetPos != null && (mob.level() instanceof ServerLevel level)
            && targetPos.getY() >= level.getMinBuildHeight()
            && targetPos.getY() < level.getMaxBuildHeight()
            && level.isLoaded(targetPos)

            && hasNearbyChunks(level, targetPos, 1)
            && hasNearbyChunks(level, mob.blockPosition(), 1);
    }

    private static boolean hasNearbyChunks(ServerLevel level, BlockPos pos, int radius) {
        return level.hasChunksAt(
            pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
            pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius);
    }

    private boolean safeNavigateToTarget() {
        if (!isTargetLoadedAndValid()) {
            clearSoundInvestigation();
            return false;
        }

        if (shouldYieldToCombat()) {
            clearSoundInvestigation();
            return false;
        }
        double x = targetPos.getX() + 0.5D;

        double y = AtcEngine.usesAirNavigation(mob)
            ? targetPos.getY() + 0.5D
            : targetPos.getY();
        double z = targetPos.getZ() + 0.5D;
        if (mob.blockPosition().distSqr(targetPos) <= ARRIVAL_DIST_SQ) {
            return true;
        }
        try {

            if (mob.getNavigation().moveTo(x, y, z, currentSpeed)) {
                return true;
            }
        } catch (Throwable ex) {

            AttractToChat.LOGGER.warn("Skipped unsafe path calculation for sound investigation: mob={}, target={}, reason={}",
                mob.getType(), AtcEngine.formatCoordinates(targetPos), ex.toString());

        }
        return false;
    }

    private void safeStopNavigation() {
        try {
            mob.getNavigation().stop();
        } catch (RuntimeException ex) {
            AttractToChat.LOGGER.warn("Skipped unsafe navigation stop for sound investigation: mob={}, reason={}",
                mob.getType(), ex.toString());
        }
    }

    public boolean isLockedToTrollTarget() {
        return lockedToTrollTarget && resolveTargetPlayer() != null;
    }

    private ServerPlayer resolveTargetPlayer() {
        if (playerUUID == null || mob.level().getServer() == null) return null;
        ServerPlayer player = mob.level().getServer().getPlayerList().getPlayer(playerUUID);
        return player != null && player.isAlive() && player.level() == mob.level() ? player : null;
    }

    public static boolean shouldVillagerPrioritizeSafety(Mob candidate) {
        if (!(candidate instanceof Villager villager) || !villager.isAlive()) return false;
        if (villager.getBrain().isActive(Activity.PANIC)) return true;
        return villager.getBrain().getMemory(MemoryModuleType.NEAREST_HOSTILE)
            .filter(hostile -> hostile instanceof Zombie && hostile.isAlive())
            .isPresent();
    }

    private boolean hasCombatTarget() {
        return mob.getTarget() != null && mob.getTarget().isAlive();
    }

    private boolean shouldYieldToCombat() {
        if (lockedToTrollTarget) return false;
        return hasCombatTarget();
    }

    public void clearSoundInvestigation() {
        clearSoundInvestigation(false);
    }

    private void yieldToVillagerSafety() {
        if (targetPos == null && timeout <= 0) return;
        preserveVillagerSafetyState = true;
        clearSoundInvestigation(true);
    }

    private void clearSoundInvestigation(boolean preserveVillagerBrain) {

        ServerPlayer troll = lockedToTrollTarget ? resolveTargetPlayer() : null;
        if (troll != null && mob.getTarget() == troll) {
            mob.setTarget(null);
        }
        if (playerUUID != null && mob.getTarget() != null
                && playerUUID.equals(mob.getTarget().getUUID())
                && !lockedToTrollTarget) {
            mob.setTarget(null);
        }
        targetPos = null;
        timeout = 0;
        lockedToTrollTarget = false;
        blockTarget = false;
        urgentShout = false;
        followLivePlayer = false;
        playerUUID = null;
        recalcDelay = 0;
        if (!preserveVillagerBrain && mob instanceof Villager villager) {
            villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        }
        if (!preserveVillagerBrain) {
            safeStopNavigation();
        }
    }

    @Deprecated
    private void updateTargetTowardLivePlayer() {

    }

    private boolean directSlime() {
        if (targetPos == null) return false;
        double dx = targetPos.getX() + 0.5 - mob.getX();
        double dz = targetPos.getZ() + 0.5 - mob.getZ();
        float yRot = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        if (mob.getMoveControl() instanceof SlimeMoveControlAccessorMixin control) {
            control.atc_setDirection(yRot, true);
            control.atc_setWantedMovement(currentSpeed);
            return true;
        } else {

            try {
                mob.getMoveControl().setWantedPosition(
                    targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, currentSpeed);
                return true;
            } catch (Throwable ignored) {
                return false;
            }
        }
    }

    private boolean setVillagerWalkTarget(Villager villager) {
        if (targetPos == null) return false;
        if (shouldVillagerPrioritizeSafety(villager)) {
            yieldToVillagerSafety();
            return false;
        }
        float minSpeed = urgentShout ? 0.60f : 0.35f;
        float maxSpeed = urgentShout ? 0.75f : 0.60f;
        float brainSpeed = (float) Math.max(minSpeed,
            Math.min(maxSpeed, currentSpeed * (urgentShout ? 0.50 : 0.42)));
        villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
            new WalkTarget(targetPos, brainSpeed, 2));
        return true;
    }

    @Override
    public void stop() {
        boolean preserveVillagerBrain = preserveVillagerSafetyState
            || shouldVillagerPrioritizeSafety(mob);
        ServerPlayer player = resolveTargetPlayer();
        if (lockedToTrollTarget && player != null && mob.getTarget() == player) {
            mob.setTarget(null);
        }
        targetPos = null;
        timeout = 0;
        lockedToTrollTarget = false;
        blockTarget = false;
        urgentShout = false;
        followLivePlayer = false;
        playerUUID = null;
        recalcDelay = 0;
        if (!preserveVillagerBrain && mob instanceof Villager villager) {
            villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        }
        if (!preserveVillagerBrain) {
            safeStopNavigation();
        }
        preserveVillagerSafetyState = false;
    }

}
