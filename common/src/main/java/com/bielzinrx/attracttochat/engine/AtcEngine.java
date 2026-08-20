package com.bielzinrx.attracttochat.engine;

import com.bielzinrx.attracttochat.config.AttractToChatConfig;
import com.bielzinrx.attracttochat.client.ClientPresence;
import com.bielzinrx.attracttochat.i18n.ServerTranslations;
import com.bielzinrx.attracttochat.fatigue.FatigueTracker;
import com.bielzinrx.attracttochat.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class AtcEngine {

    public static final Logger LOGGER = LoggerFactory.getLogger("AttractToChat-Engine");

    private static final Map<UUID, MobGoalData>  MOB_GOAL_DATA    = new ConcurrentHashMap<>();
    private static final Map<UUID, Long>         PLAYER_COOLDOWNS = new ConcurrentHashMap<>();

    private static final Map<UUID, Deque<Long>>  PLAYER_MESSAGE_WINDOW = new ConcurrentHashMap<>();
    private static final Map<UUID, Long>         MUTED_UNTIL      = new ConcurrentHashMap<>();
    private static final Map<UUID, Long>         MUTED_UNTIL_WALL = new ConcurrentHashMap<>();
    private static final String                  MUTE_NBT_KEY     = "AttractToChatMuteEnd";
    private static final Map<UUID, PlayerStats>  PLAYER_STATS     = new ConcurrentHashMap<>();
    private static final Set<String>             ENABLED_ENTITIES = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<String>             IGNORED_PLAYERS  = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<String>             TROLL_PLAYERS    = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static final Set<UUID> ENABLE_PARTICLES = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static long serverTicks = 0;
    private static final double MIN_EFFECTIVE_CHAT_RANGE = 0.0;
    private static final double MAX_EFFECTIVE_HEARING_RANGE = 500.0;
    private static final double MAX_TRAUMA_RANGE = 64.0;
    private static final int MAX_STANDARD_TARGETS = 50;
    private static final int MAX_TRAUMA_TARGETS = 16;

    private static final double PARTICLE_VIEW_RANGE_SQ = 48.0 * 48.0;

    private static final int MAX_SOLID_MUFFLE_BLOCKS = 8;

    private AtcEngine() {}

    public static void onServerTick() {
        serverTicks++;
        if (serverTicks % 20 == 0 && AttractToChatConfig.reloadIfChanged()) {

            setDebugModeOverride(null);
            refreshCaches();
            clearSoundInvestigations();
            clearAntiSpamState();
            if (!AttractToChatConfig.COMMON.enableVocalFatigue.get()) {
                clearVocalFatigueState();
            }
            LOGGER.info("Applied live AttractToChat config changes to runtime caches.");
        }
        if (serverTicks % 200 == 0) {
            MOB_GOAL_DATA.entrySet().removeIf(entry -> {
                Mob mob = entry.getValue().mob();
                return mob == null || mob.isRemoved();
            });
        }
    }

    public static void refreshCaches() {
        refreshEntityRules();
        refreshPlayerRules();
        refreshClientPreferences();
        refreshGoalTiming();
    }

    public static void refreshEntityRules() {
        ENABLED_ENTITIES.clear();
        List<String> eConfigs = AttractToChatConfig.COMMON.enabledEntities.get();
        if (eConfigs != null) ENABLED_ENTITIES.addAll(eConfigs);
        reconcileLoadedMobs();
    }

    public static void refreshPlayerRules() {
        IGNORED_PLAYERS.clear();
        TROLL_PLAYERS.clear();
        List<String> iConfigs = AttractToChatConfig.COMMON.ignoredPlayers.get();
        if (iConfigs != null) {
            for (String name : iConfigs) {
                if (name != null) IGNORED_PLAYERS.add(name.toLowerCase(Locale.ROOT));
            }
        }
        List<String> tConfigs = AttractToChatConfig.COMMON.trollPlayers.get();
        if (tConfigs != null) {
            for (String name : tConfigs) {
                if (name != null) TROLL_PLAYERS.add(name.toLowerCase(Locale.ROOT));
            }
        }
    }

    public static void refreshClientPreferences() {
        ENABLE_PARTICLES.clear();
        List<String> particlesOptIn = AttractToChatConfig.COMMON.clientParticlesOptIn.get();
        if (particlesOptIn != null) {
            for (String raw : particlesOptIn) {
                try { ENABLE_PARTICLES.add(UUID.fromString(raw)); }
                catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public static void refreshGoalTiming() {
        int baseTicks = AttractToChatConfig.COMMON.forgetTargetAfterSeconds.get() * 20;
        MOB_GOAL_DATA.values().forEach(data -> {
            if (data != null && data.goal() != null) {
                data.goal().updateBaseTicks(baseTicks);
            }
        });
    }

    public static boolean shouldProcessChat(ServerPlayer player, String message) {
        if (player == null || !player.isAlive()) return false;
        if (isIgnored(player)) return false;
        if (message == null || message.trim().isEmpty()) return false;
        char first = message.trim().charAt(0);
        return first != '!' && first != '@' && first != '#' && first != '/';
    }


    public static boolean isVocallyMuted(UUID id) {
        return AttractToChatConfig.COMMON.enableVocalFatigue.get() && isMuted(id);
    }

    public static boolean handleChatCancellable(ServerPlayer player, String message) {
        if (player == null || !player.isAlive()) return false;
        if (isVocallyMuted(player.getUUID())) {
            if (!isTrollPlayer(player)) {
                player.displayClientMessage(ServerTranslations.component(
                    player, "message.attracttochat.fatigue.exhausted_self"), true);
            }
            return true;
        }
        return false;
    }

    public static void handleChatAfter(ServerPlayer player, String message) {
        if (handleChatCancellable(player, message)) return;
        if (!shouldProcessChat(player, message)) return;
        processChat(player, message);
    }

    public static void onPlayerDisconnect(UUID uuid) {
        PLAYER_COOLDOWNS.remove(uuid);
        PLAYER_MESSAGE_WINDOW.remove(uuid);
        ServerTranslations.forgetPlayer(uuid);
        ClientPresence.forget(uuid);

        PLAYER_STATS.remove(uuid);

        clearInvestigationsForPlayer(uuid);
    }

    public static void onServerStop() {
        MOB_GOAL_DATA.clear();
        PLAYER_STATS.clear();
        PLAYER_COOLDOWNS.clear();
        PLAYER_MESSAGE_WINDOW.clear();
        ClientPresence.clear();
        ServerTranslations.clearPlayerLanguages();

        serverTicks = 0;
        debugModeOverride = null;
        ENABLE_PARTICLES.clear();
    }

    public static int clearInvestigationsForPlayer(UUID playerId) {
        if (playerId == null) return 0;
        int cleared = 0;
        for (MobGoalData data : MOB_GOAL_DATA.values()) {
            if (data == null || data.goal() == null) continue;
            if (playerId.equals(data.goal().getTrackedPlayerId())) {
                data.goal().clearSoundInvestigation();
                cleared++;
            }
        }
        return cleared;
    }

    public static boolean isIgnored(ServerPlayer player) {
        return player != null && isIgnoredPlayerName(player.getName().getString());
    }

    public static boolean isTrollPlayer(ServerPlayer player) {
        return player != null && isTrollPlayerName(player.getName().getString());
    }

    static boolean isIgnoredPlayerName(String name) {
        return IGNORED_PLAYERS.contains("@a")
            || name != null && IGNORED_PLAYERS.contains(name.toLowerCase(Locale.ROOT));
    }

    static boolean isTrollPlayerName(String name) {
        return name != null && TROLL_PLAYERS.contains(name.toLowerCase(Locale.ROOT));
    }

    public static void ensureMobGoal(Mob mob) {
        if (mob == null || mob.isRemoved() || !isAttractableMob(mob) || !isEntityEnabled(mob)) return;
        MobGoalData existing = MOB_GOAL_DATA.get(mob.getUUID());
        if (existing != null && existing.goal() != null) return;

        MoveToSoundGoal goal = new MoveToSoundGoal(
            mob, AttractToChatConfig.COMMON.forgetTargetAfterSeconds.get() * 20);
        Platform.getHelper().getGoalSelector(mob).addGoal(1, goal);
        MOB_GOAL_DATA.put(mob.getUUID(), new MobGoalData(mob, goal));
    }

    public static boolean isAttractableMob(Mob mob) {
        if (mob == null || mob.isRemoved() || !mob.isAlive()) return false;
        if (mob.isNoAi()) return false;
        if (!mob.isEffectiveAi()) return false;
        return true;
    }

    public static boolean usesAirNavigation(Mob mob) {
        if (mob == null) return false;
        return mob instanceof FlyingMob
            || mob instanceof FlyingAnimal
            || mob.getNavigation() instanceof FlyingPathNavigation;
    }

    public static int clearSoundInvestigations() {
        int cleared = 0;
        for (MobGoalData data : MOB_GOAL_DATA.values()) {
            if (data != null && data.goal() != null) {
                data.goal().clearSoundInvestigation();
                cleared++;
            }
        }
        return cleared;
    }

    public static void clearAntiSpamState() {
        PLAYER_COOLDOWNS.clear();
        PLAYER_MESSAGE_WINDOW.clear();
    }

    public static boolean tryAcceptScan(ServerPlayer player, boolean trollPlayer) {
        if (player == null) return false;
        if (trollPlayer || !AttractToChatConfig.COMMON.enableAntiSpam.get()) return true;
        long remain = getAntiSpamWaitSeconds(player.getUUID());
        if (remain > 0) {
            player.displayClientMessage(
                ServerTranslations.component(player, "message.attracttochat.antispam.wait", remain), true);
            return false;
        }
        return true;
    }

    public static long getAntiSpamWaitSeconds(UUID uuid) {
        if (uuid == null || !AttractToChatConfig.COMMON.enableAntiSpam.get()) return 0L;

        Long last = PLAYER_COOLDOWNS.get(uuid);
        int cooldownTicks = AttractToChatConfig.COMMON.scanCooldownTicks.get();
        if (last != null && (serverTicks - last) < cooldownTicks) {
            return (cooldownTicks - (serverTicks - last) + 19) / 20;
        }

        int maxMessages = AttractToChatConfig.COMMON.antiSpamMaxMessages.get();
        int windowSeconds = AttractToChatConfig.COMMON.antiSpamWindowSeconds.get();
        if (maxMessages > 0 && windowSeconds > 0) {
            long windowTicks = windowSeconds * 20L;
            Deque<Long> window = PLAYER_MESSAGE_WINDOW.get(uuid);
            if (window != null) {
                while (!window.isEmpty() && (serverTicks - window.peekFirst()) >= windowTicks) {
                    window.pollFirst();
                }
                if (window.size() >= maxMessages) {
                    Long oldest = window.peekFirst();
                    if (oldest != null) {
                        long remainTicks = windowTicks - (serverTicks - oldest);
                        return Math.max(1L, (remainTicks + 19) / 20);
                    }
                }
            }
        }
        return 0L;
    }

    private static void recordAcceptedMessage(UUID uuid) {
        recordAcceptedScan(uuid);
    }

    public static void recordAcceptedScan(UUID uuid) {
        if (uuid == null) return;
        PLAYER_COOLDOWNS.put(uuid, serverTicks);
        int maxMessages = AttractToChatConfig.COMMON.antiSpamMaxMessages.get();
        if (maxMessages <= 0) return;
        Deque<Long> window = PLAYER_MESSAGE_WINDOW.computeIfAbsent(uuid,
            k -> new ConcurrentLinkedDeque<>());
        window.addLast(serverTicks);
        long windowTicks = AttractToChatConfig.COMMON.antiSpamWindowSeconds.get() * 20L;
        while (!window.isEmpty() && (serverTicks - window.peekFirst()) >= windowTicks) {
            window.pollFirst();
        }
        while (window.size() > Math.max(1, maxMessages * 2)) {
            window.pollFirst();
        }
    }

    public static void clearVocalFatigueState() {
        MUTED_UNTIL.clear();
        MUTED_UNTIL_WALL.clear();
        FatigueTracker.clearAll();
    }

    public static void unregisterMob(UUID uuid) {
        MobGoalData data = MOB_GOAL_DATA.remove(uuid);
        if (data == null) return;
        Mob mob = data.mob();
        MoveToSoundGoal goal = data.goal();
        if (mob != null && goal != null) {
            Platform.getHelper().getGoalSelector(mob).removeGoal(goal);
        }
    }

    public static MobGoalData getMobGoalData(UUID uuid) {
        return MOB_GOAL_DATA.get(uuid);
    }

    private static boolean isMuted(UUID id) {
        if (id == null) return false;
        Long wall = MUTED_UNTIL_WALL.get(id);
        if (wall != null) {
            long now = System.currentTimeMillis();
            if (now < wall) return true;
            MUTED_UNTIL_WALL.remove(id, wall);
            MUTED_UNTIL.remove(id);
            return false;
        }
        Long until = MUTED_UNTIL.get(id);
        if (until == null) return false;
        if (serverTicks < until) return true;
        MUTED_UNTIL.remove(id, until);
        return false;
    }

    public static void saveMuteForPlayer(UUID playerId, net.minecraft.nbt.CompoundTag tag) {
        if (playerId == null || tag == null) return;
        Long wall = MUTED_UNTIL_WALL.get(playerId);
        if (wall == null) {
            Long ticksUntil = MUTED_UNTIL.get(playerId);
            if (ticksUntil != null && ticksUntil > serverTicks) {
                wall = System.currentTimeMillis() + (ticksUntil - serverTicks) * 50L;
            }
        }
        if (wall != null && wall > System.currentTimeMillis()) {
            tag.putLong(MUTE_NBT_KEY, wall);
        }
    }

    public static void loadMuteForPlayer(UUID playerId, net.minecraft.nbt.CompoundTag tag) {
        if (playerId == null || tag == null || !tag.contains(MUTE_NBT_KEY)) return;
        long end = tag.getLong(MUTE_NBT_KEY);
        long now = System.currentTimeMillis();
        long maxMs = 60L * 60L * 1000L;
        if (end > now && end <= now + maxMs) {
            MUTED_UNTIL_WALL.put(playerId, end);
            long remainingTicks = Math.max(1L, (end - now) / 50L);
            MUTED_UNTIL.put(playerId, serverTicks + remainingTicks);
        } else {
            MUTED_UNTIL_WALL.remove(playerId);
            MUTED_UNTIL.remove(playerId);
        }
    }

    private static void processChat(ServerPlayer player, String message) {
        if (!shouldProcessChat(player, message)) return;
        if (AttractToChatConfig.COMMON.enableVocalFatigue.get() && isMuted(player.getUUID())) {
            return;
        }
        UUID uuid = player.getUUID();
        boolean trollPlayer = isTrollPlayer(player);

        if (!tryAcceptScan(player, trollPlayer)) return;

        MessageScore score = new MessageScore(message, uuid);

        if (AttractToChatConfig.COMMON.enableVocalFatigue.get()) {
            if (applyVocalFatigue(player, score)) return;
        }

        recordAcceptedMessage(uuid);

        double range = computeEffectiveChatRange(score);
        if (trollPlayer) range *= 4.0;
        range = clampEffectiveHearingRange(range);

        if (isDebugMode()) {
            LOGGER.info("[ATC-Debug] Chat from {}: loudness={}, saturation={}, range={}",
                player.getName().getString(), score.loudness, score.saturation, range);
        }
        int[] attracted = attractMobsAtPosition((ServerLevel) player.level, player.blockPosition(), range, score);
        PLAYER_STATS.computeIfAbsent(uuid, k -> new PlayerStats()).record(attracted.length, score.caps);

        if (isDebugMode()) {
            sendDebugFeedback(player, range, attracted.length, score.caps);
        }
    }

    private static double computeEffectiveChatRange(MessageScore score) {
        double configuredRange = AttractToChatConfig.COMMON.hearingRange.get();
        double capsBonus = score.caps * AttractToChatConfig.COMMON.capsRangeBonus.get();

        double loudnessBonus = Math.max(0.0, score.loudness - 1.0) * configuredRange * 0.55;
        return clampEffectiveHearingRange(configuredRange + loudnessBonus + capsBonus);
    }

    private static double clampEffectiveHearingRange(double range) {
        if (!Double.isFinite(range)) return MIN_EFFECTIVE_CHAT_RANGE;
        return Math.max(MIN_EFFECTIVE_CHAT_RANGE, Math.min(MAX_EFFECTIVE_HEARING_RANGE, range));
    }

    public static boolean applyVocalFatigue(ServerPlayer player, MessageScore score) {
        if (!AttractToChatConfig.COMMON.enableVocalFatigue.get() || score.loudness <= 1.8) return false;

        UUID uuid = player.getUUID();
        int fatigue = (int)(AttractToChatConfig.COMMON.muteDurationTicks.get() * Math.pow(score.loudness, 0.5));
        int current = FatigueTracker.getFatigueTicks(uuid);
        FatigueTracker.addFatigue(uuid, fatigue);
        if (current + fatigue >= AttractToChatConfig.COMMON.traumaThreshold.get()) {
            triggerVocalTrauma(player);
            return true;
        }
        return false;
    }

    private static void triggerVocalTrauma(ServerPlayer player) {
        long muteTicks = AttractToChatConfig.COMMON.muteDurationTicks.get();
        MUTED_UNTIL.put(player.getUUID(), serverTicks + muteTicks);
        MUTED_UNTIL_WALL.put(player.getUUID(), System.currentTimeMillis() + muteTicks * 50L);
        FatigueTracker.clear(player.getUUID());

        double traumaRange = Math.min(MAX_TRAUMA_RANGE,
            Math.max(AttractToChatConfig.COMMON.hearingRange.get(),
                AttractToChatConfig.COMMON.hearingRange.get() * 1.5));
        attractMobsAtPosition((ServerLevel) player.level, player.blockPosition(),
            traumaRange, new MessageScore("vocal strain", player.getUUID()),
            false, MAX_TRAUMA_TARGETS);

        if (!isTrollPlayer(player)) {
            player.displayClientMessage(
                ServerTranslations.component(player, "message.attracttochat.fatigue.alert"), true);
        }

        LOGGER.debug("Player [{}] triggered vocal trauma.", player.getUUID());
    }

    private static Boolean debugModeOverride = null;

    public static boolean isDebugMode() {
        if (debugModeOverride != null) return debugModeOverride;
        return AttractToChatConfig.COMMON.debugMode.get();
    }
    public static void setDebugModeOverride(Boolean v) { debugModeOverride = v; }

    public static int[] attractMobsAtPosition(ServerLevel level, BlockPos target, double range, MessageScore score) {
        return attractMobsAtPosition(level, target, range, score, false, MAX_STANDARD_TARGETS);
    }


    public static double computeAttractNavSpeed(MessageScore score, boolean trollTarget, boolean blockTarget) {
        double base = AttractToChatConfig.COMMON.mobSpeedBase.get();
        double max = AttractToChatConfig.COMMON.mobSpeedMax.get();
        if (trollTarget) {
            double mult = AttractToChatConfig.COMMON.trollSpeedMultiplier.get();
            base = Math.min(4.0, base * mult);
            max = Math.min(5.0, Math.max(base, max * mult));
        }
        double sat = score != null ? score.saturation : 0.0;
        return Math.min(base + sat * 0.5, max);
    }

    private static int[] attractMobsAtPosition(ServerLevel level, BlockPos target, double range,
            MessageScore score, boolean blockTarget, int maxTargets) {
        List<Integer> ids = new ArrayList<>();
        ServerPlayer sourcePlayer = score.playerUUID == null
            ? null
            : level.getServer().getPlayerList().getPlayer(score.playerUUID);
        boolean trollTarget = isTrollPlayer(sourcePlayer);
        double capped = clampEffectiveHearingRange(range);
        double sq     = capped * capped;
        AABB area     = new AABB(target).inflate(capped);
        boolean shout = "shout".equals(score.factor);

        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, area,
            mob -> isAttractableMob(mob)
                && isEntityEnabled(mob)
                && (!(mob instanceof Villager villager)
                    || ((!villager.isSleeping() || shout)
                        && !MoveToSoundGoal.shouldVillagerPrioritizeSafety(villager)))
                && mob.blockPosition().distSqr(target) <= sq
                && canHearThroughTerrain(level, target, mob.blockPosition(), capped));

        mobs.sort(Comparator.comparingDouble(mob -> mob.blockPosition().distSqr(target)));

        int attempted = 0;
        for (Mob mob : mobs) {
            if (attempted >= Math.max(1, maxTargets)) break;

            if (MoveToSoundGoal.shouldVillagerPrioritizeSafety(mob)) continue;

            if (shout && mob instanceof Villager villager && villager.isSleeping()) {
                villager.stopSleeping();
            }

            MobGoalData data = MOB_GOAL_DATA.get(mob.getUUID());
            if (data == null || data.goal() == null) {
                ensureMobGoal(mob);
                data = MOB_GOAL_DATA.get(mob.getUUID());
            }

            if (data != null && data.goal() != null) {
                if (data.goal().isLockedToTrollTarget() && !trollTarget) continue;

                if (!trollTarget && mob.getTarget() != null && mob.getTarget().isAlive()) continue;

                if (mob instanceof EnderMan enderman && "shout".equals(score.factor)) {
                    teleportEndermanToSound(enderman, target, blockTarget);
                }

                double dynamicSpeed = computeAttractNavSpeed(score, trollTarget, blockTarget);

                attempted++;
                MoveToSoundGoal.InvestigationStartResult result = data.goal().setTarget(
                    target, score, capped, score.playerUUID,
                    dynamicSpeed, trollTarget, blockTarget);

                if (result.started()) {
                    ids.add(mob.getId());
                }

                if (isDebugMode()) {
                    String entityId = Platform.getHelper().getEntityTypeId(mob.getType());
                    BlockPos destination = data.goal().getInvestigationTarget();
                    if (result.started() && destination != null) {
                        LOGGER.info("[ATC-Debug] {} -> investigation started at {}",
                            entityId, formatCoordinates(destination));
                    } else {
                        LOGGER.info("[ATC-Debug] {} -> investigation failed: {}",
                            entityId, result.debugReason());
                    }
                }
            }
        }

        if (isDebugMode()) {
            LOGGER.info("[ATC-Debug] Heard by {} mobs; attempted: {}; investigations started: {}; sound source: {}",
                mobs.size(), attempted, ids.size(), formatCoordinates(target));
        }

        return ids.stream().mapToInt(i -> i).toArray();
    }

    private static boolean canHearThroughTerrain(ServerLevel level, BlockPos source,
            BlockPos listener, double maxRange) {
        if (level == null || source == null || listener == null) return false;
        if (source.distSqr(listener) <= 4.0) return true;

        Vec3 from = Vec3.atCenterOf(source).add(0.0, 0.2, 0.0);
        Vec3 to = Vec3.atCenterOf(listener).add(0.0, 0.6, 0.0);
        int solidHits = countSolidBlocksBetween(level, from, to);
        if (solidHits <= 0) return true;

        double distance = Math.sqrt(source.distSqr(listener));

        double muffledRange = maxRange * Math.max(0.15, 1.0 - (solidHits / (double) MAX_SOLID_MUFFLE_BLOCKS));
        if (solidHits >= MAX_SOLID_MUFFLE_BLOCKS) return false;
        return distance <= muffledRange;
    }

    private static int countSolidBlocksBetween(ServerLevel level, Vec3 from, Vec3 to) {

        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 0.5) return 0;
        int steps = Math.min(64, Math.max(4, (int) Math.ceil(length)));
        double inv = 1.0 / steps;
        int solids = 0;
        BlockPos last = null;
        for (int i = 1; i < steps; i++) {
            double t = i * inv;
            BlockPos pos = new BlockPos(
                net.minecraft.util.Mth.floor(from.x + dx * t),
                net.minecraft.util.Mth.floor(from.y + dy * t),
                net.minecraft.util.Mth.floor(from.z + dz * t));
            if (pos.equals(last) || !level.isLoaded(pos)) continue;
            last = pos;
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && !state.getCollisionShape(level, pos).isEmpty()
                    && state.isCollisionShapeFullBlock(level, pos)) {
                solids++;
                if (solids >= MAX_SOLID_MUFFLE_BLOCKS) break;
            }
        }
        return solids;
    }

    private static final int PATH_PARTICLE_MAX_POINTS = 40;

    private static final double PATH_PARTICLE_STEP = 0.65;

    public static void trySpawnPathParticles(Mob mob, BlockPos targetPos, boolean burst) {
        if (mob == null || targetPos == null || !(mob.level instanceof ServerLevel level)) return;
        if (!AttractToChatConfig.COMMON.showParticles.get()) return;

        double[] xs = new double[PATH_PARTICLE_MAX_POINTS];
        double[] ys = new double[PATH_PARTICLE_MAX_POINTS];
        double[] zs = new double[PATH_PARTICLE_MAX_POINTS];
        int count = sampleInvestigationPath(mob, targetPos, xs, ys, zs);
        if (count <= 0) return;

        double midX = xs[count / 2];
        double midY = ys[count / 2];
        double midZ = zs[count / 2];

        for (ServerPlayer viewer : level.players()) {
            if (viewer.distanceToSqr(midX, midY, midZ) > PARTICLE_VIEW_RANGE_SQ
                    || !Platform.getHelper().hasClientMod(viewer)
                    || !isParticlesEnabled(viewer.getUUID())) {
                continue;
            }
            for (int i = 0; i < count; i++) {
                level.sendParticles(viewer, ParticleTypes.END_ROD,
                    true, xs[i], ys[i], zs[i], 1, 0.02, 0.03, 0.02, 0.0);
                if (burst || (i % 2) == 0) {
                    level.sendParticles(viewer, ParticleTypes.SOUL_FIRE_FLAME,
                        true, xs[i], ys[i] - 0.05, zs[i], 1, 0.04, 0.02, 0.04, 0.0);
                }
            }
            double tx = targetPos.getX() + 0.5;
            double ty = targetPos.getY() + 0.35;
            double tz = targetPos.getZ() + 0.5;
            level.sendParticles(viewer, ParticleTypes.NOTE,
                true, tx, ty, tz, burst ? 6 : 3, 0.20, 0.15, 0.20, 1.0);
            level.sendParticles(viewer, ParticleTypes.END_ROD,
                true, tx, ty + 0.4, tz, burst ? 8 : 4, 0.12, 0.25, 0.12, 0.02);
        }
    }

    private static int sampleInvestigationPath(Mob mob, BlockPos targetPos,
            double[] xs, double[] ys, double[] zs) {
        try {
            var path = mob.getNavigation().getPath();
            if (path != null && path.getNodeCount() > 0) {
                int start = Math.max(0, path.getNextNodeIndex());
                int nodeCount = path.getNodeCount();
                int remaining = nodeCount - start;
                if (remaining > 0) {
                    int stride = Math.max(1, remaining / PATH_PARTICLE_MAX_POINTS);
                    int written = 0;
                    for (int i = start; i < nodeCount && written < PATH_PARTICLE_MAX_POINTS; i += stride) {
                        var node = path.getNode(i);
                        xs[written] = node.x + 0.5;
                        ys[written] = node.y + 0.25;
                        zs[written] = node.z + 0.5;
                        written++;
                    }
                    if (written > 0 && written < PATH_PARTICLE_MAX_POINTS) {
                        var last = path.getNode(nodeCount - 1);
                        if (xs[written - 1] != last.x + 0.5 || zs[written - 1] != last.z + 0.5) {
                            xs[written] = last.x + 0.5;
                            ys[written] = last.y + 0.25;
                            zs[written] = last.z + 0.5;
                            written++;
                        }
                    }
                    if (written > 0) return written;
                }
            }
        } catch (Throwable ignored) {

        }

        double x0 = mob.getX();
        double y0 = mob.getY() + 0.20;
        double z0 = mob.getZ();
        double x1 = targetPos.getX() + 0.5;
        double y1 = targetPos.getY() + 0.25;
        double z1 = targetPos.getZ() + 0.5;
        double dx = x1 - x0;
        double dy = y1 - y0;
        double dz = z1 - z0;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist < 0.4) {
            xs[0] = x0;
            ys[0] = y0;
            zs[0] = z0;
            return 1;
        }
        int steps = Math.min(PATH_PARTICLE_MAX_POINTS,
            Math.max(3, (int) Math.ceil(dist / PATH_PARTICLE_STEP)));
        for (int i = 0; i < steps; i++) {
            double t = i / (double) (steps - 1);
            xs[i] = x0 + dx * t;
            ys[i] = y0 + dy * t;
            zs[i] = z0 + dz * t;
        }
        return steps;
    }

    @Deprecated
    public static void trySpawnHeardBurst(Mob mob) {
        if (mob != null) {
            trySpawnPathParticles(mob, mob.blockPosition(), true);
        }
    }

    @Deprecated
    public static void trySpawnInvestigationTrail(Mob mob) {
        if (mob != null) {
            trySpawnPathParticles(mob, mob.blockPosition(), false);
        }
    }

    private static class PlayerStats {
        int totalMessages, totalMobs, totalCaps;
        void record(int mobs, int caps) {
            totalMessages++;
            totalMobs += mobs;
            totalCaps += caps;
        }
    }

    public static int[] getStats(UUID uuid) {
        PlayerStats s = PLAYER_STATS.getOrDefault(uuid, new PlayerStats());
        return new int[]{ s.totalMessages, s.totalMobs, s.totalCaps };
    }

    public static boolean isEntityEnabled(Mob mob) {
        String id = Platform.getHelper().getEntityTypeId(mob.getType());
        if (ENABLED_ENTITIES.isEmpty()) return false;
        boolean exclusionMode = ENABLED_ENTITIES.stream().allMatch(value -> value.startsWith("!"));
        return exclusionMode ? !ENABLED_ENTITIES.contains("!" + id) : ENABLED_ENTITIES.contains(id);
    }


    public static boolean isParticlesEnabled(UUID id) {
        return id != null && ENABLE_PARTICLES.contains(id);
    }

    public static boolean setParticlesEnabled(UUID id, boolean enabled) {
        return setUuidPreference(id, enabled, ENABLE_PARTICLES,
            AttractToChatConfig.COMMON.clientParticlesOptIn);
    }

    public static void clearMute(UUID id) {
        if (id != null) {
            MUTED_UNTIL.remove(id);
            MUTED_UNTIL_WALL.remove(id);
        }
    }

    private static boolean setUuidPreference(UUID id, boolean enabled, Set<UUID> liveSet,
            AttractToChatConfig.ConfigValue<List<String>> configList) {
        if (id == null) return false;
        if (liveSet.contains(id) == enabled) return true;
        if (enabled) {
            liveSet.add(id);
        } else {
            liveSet.remove(id);
        }
        List<String> values = new ArrayList<>(configList.get());
        String raw = id.toString();
        if (enabled && !values.contains(raw)) {
            values.add(raw);
        } else if (!enabled) {
            values.removeIf(raw::equalsIgnoreCase);
        }
        configList.set(values);
        boolean saved = AttractToChatConfig.save();
        if (!saved) refreshClientPreferences();
        return saved;
    }


    private static void sendDebugFeedback(ServerPlayer player, double range,
            int attracted, int caps) {
        player.displayClientMessage(ServerTranslations.component(
            player, "message.attracttochat.debug_info", range, attracted, caps), true);
    }

    private static void teleportEndermanToSound(EnderMan enderman, BlockPos target,
            boolean blockTarget) {
        if (!(enderman.level instanceof ServerLevel level) || target == null) return;
        BlockPos center = blockTarget ? target.above() : target;
        BlockPos safe = findSafeTeleportNear(level, center, 4);
        if (safe == null) {
            LOGGER.debug("Skipped enderman teleport: no safe landing near {}", formatCoordinates(center));
            return;
        }
        enderman.teleportTo(safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5);
    }

    private static BlockPos findSafeTeleportNear(ServerLevel level, BlockPos center, int radius) {
        if (isSafeStand(level, center)) return center;
        for (int r = 1; r <= radius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    for (int dy = 0; dy <= 2; dy++) {
                        BlockPos up = center.offset(dx, dy, dz);
                        if (isSafeStand(level, up)) return up;
                        if (dy > 0) {
                            BlockPos down = center.offset(dx, -dy, dz);
                            if (isSafeStand(level, down)) return down;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean isSafeStand(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null || !level.hasChunksAt(pos, pos.above())) return false;
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());
        BlockState ground = level.getBlockState(pos.below());
        return feet.getCollisionShape(level, pos).isEmpty()
            && head.getCollisionShape(level, pos.above()).isEmpty()
            && !ground.getCollisionShape(level, pos.below()).isEmpty()
            && !ground.isAir();
    }

    public static String formatCoordinates(BlockPos pos) {
        if (pos == null) return "(unknown)";
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    private static void reconcileLoadedMobs() {
        net.minecraft.server.MinecraftServer server =
            com.bielzinrx.attracttochat.AttractToChat.getServer();
        if (server == null) return;

        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof Mob mob)) continue;
                if (isAttractableMob(mob) && isEntityEnabled(mob)) {
                    ensureMobGoal(mob);
                } else {
                    unregisterMob(mob.getUUID());
                }
            }
        }
    }
}
