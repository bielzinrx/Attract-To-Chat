package com.bielzinrx.attracttochat.fatigue;

import net.minecraft.nbt.CompoundTag;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FatigueTracker {

    private static final String NBT_KEY = "AttractToChatFatigueEnd";
    private static final long MILLIS_PER_TICK = 50L;
    private static final long MAX_FATIGUE_MS = 6000 * MILLIS_PER_TICK;
    private static final Map<UUID, Long> FATIGUE_END_TIME = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger("AttractToChat-FatigueNBT");

    private FatigueTracker() {}

    public static int getFatigueTicks(UUID playerId) {
        if (playerId == null) return 0;
        long end = FATIGUE_END_TIME.getOrDefault(playerId, 0L);
        long now = System.currentTimeMillis();
        if (end <= now) {
            FATIGUE_END_TIME.remove(playerId);
            return 0;
        }
        return (int) Math.ceil((end - now) / (double) MILLIS_PER_TICK);
    }

    public static int getFatigueSeconds(UUID playerId) {
        return getFatigueTicks(playerId) / 20;
    }

    public static boolean isFatigued(UUID playerId) {
        return getFatigueTicks(playerId) > 0;
    }

    public static void addFatigue(UUID playerId, int ticks) {
        if (playerId == null || ticks <= 0) return;
        long now  = System.currentTimeMillis();
        long cur  = FATIGUE_END_TIME.getOrDefault(playerId, now);
        if (cur < now) cur = now;
        long delta = Math.min((long) ticks * MILLIS_PER_TICK, MAX_FATIGUE_MS);
        FATIGUE_END_TIME.put(playerId, Math.min(cur + delta, now + MAX_FATIGUE_MS));
    }

    public static void reduceFatigue(UUID playerId, int ticks) {
        if (playerId == null || ticks <= 0) return;
        long now = System.currentTimeMillis();
        long end = FATIGUE_END_TIME.getOrDefault(playerId, 0L);
        if (end <= now) return;
        long newEnd = end - Math.min((long) ticks * MILLIS_PER_TICK, MAX_FATIGUE_MS);

        if (newEnd <= now) FATIGUE_END_TIME.remove(playerId);
        else FATIGUE_END_TIME.put(playerId, newEnd);
    }

    public static void clear(UUID playerId)  { if (playerId != null) FATIGUE_END_TIME.remove(playerId); }
    public static void clearAll()            { FATIGUE_END_TIME.clear(); }

    public static void saveForPlayer(UUID playerId, CompoundTag tag) {
        if (playerId == null || tag == null) return;
        try {
            long end = FATIGUE_END_TIME.getOrDefault(playerId, 0L);
            if (end > System.currentTimeMillis()) {
                tag.putLong(NBT_KEY, end);
            }
        } catch (RuntimeException exception) {
            LOGGER.debug("Skipped AttractToChat fatigue NBT save because the player tag was not writable.", exception);
        }
    }

    public static void loadForPlayer(UUID playerId, CompoundTag tag) {
        if (playerId == null || tag == null) return;
        try {
            if (tag.contains(NBT_KEY)) {
                long end = tag.getLong(NBT_KEY);
                long now = System.currentTimeMillis();
                if (end > now && end <= now + MAX_FATIGUE_MS) {
                    FATIGUE_END_TIME.put(playerId, end);
                } else {
                    FATIGUE_END_TIME.remove(playerId);
                }
            }
        } catch (RuntimeException exception) {
            FATIGUE_END_TIME.remove(playerId);
            LOGGER.debug("Ignored malformed AttractToChat fatigue NBT for player {}.", playerId, exception);
        }
    }
}
