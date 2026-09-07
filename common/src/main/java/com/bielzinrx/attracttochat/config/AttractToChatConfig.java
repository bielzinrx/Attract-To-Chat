package com.bielzinrx.attracttochat.config;

import com.bielzinrx.attracttochat.platform.Platform;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.CRC32;

public final class AttractToChatConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("AttractToChat-Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int CONFIG_VERSION = 16;
    private static final int SAFE_DEFAULTS_VERSION = 4;
    private static final int EXPLICIT_ENTITY_LIST_VERSION = 7;
    private static final int HOSTILE_DEFAULTS_VERSION = 8;
    private static final int PARTICLES_PREFERENCE_VERSION = 16;

    public static final List<String> DEFAULT_ENTITIES = List.of(
        "minecraft:zombie", "minecraft:zombie_villager", "minecraft:husk",
        "minecraft:drowned", "minecraft:skeleton", "minecraft:stray",
        "minecraft:wither_skeleton", "minecraft:creeper", "minecraft:spider",
        "minecraft:cave_spider", "minecraft:enderman", "minecraft:witch",
        "minecraft:slime", "minecraft:magma_cube", "minecraft:blaze",
        "minecraft:ghast", "minecraft:phantom", "minecraft:pillager",
        "minecraft:vindicator", "minecraft:evoker", "minecraft:vex",
        "minecraft:ravager", "minecraft:hoglin", "minecraft:zoglin"
    );

    private static final Set<String> NON_CONFIGURABLE_ENTITY_IDS = Set.of(
        "minecraft:armor_stand", "minecraft:item", "minecraft:experience_orb",
        "minecraft:area_effect_cloud", "minecraft:painting", "minecraft:item_frame",
        "minecraft:glow_item_frame", "minecraft:leash_knot", "minecraft:end_crystal",
        "minecraft:marker", "minecraft:block_display", "minecraft:item_display",
        "minecraft:text_display", "minecraft:interaction", "minecraft:falling_block",
        "minecraft:tnt", "minecraft:arrow", "minecraft:spectral_arrow",
        "minecraft:trident", "minecraft:snowball", "minecraft:egg",
        "minecraft:ender_pearl", "minecraft:eye_of_ender", "minecraft:potion",
        "minecraft:experience_bottle", "minecraft:firework_rocket", "minecraft:fireball",
        "minecraft:small_fireball", "minecraft:dragon_fireball", "minecraft:wither_skull",
        "minecraft:shulker_bullet", "minecraft:llama_spit", "minecraft:evoker_fangs",
        "minecraft:fishing_bobber", "minecraft:lightning_bolt", "minecraft:player"
    );
    public static final Common COMMON = new Common();
    public static final Spec SPEC = new Spec();

    private AttractToChatConfig() {}

    public static class ConfigValue<T> {
        private T value;
        public ConfigValue(T defaultValue) { this.value = defaultValue; }
        public T get() { return value; }
        public void set(T value) { this.value = value; }
    }

    public static class Common {
        public final ConfigValue<List<String>> enabledEntities = new ConfigValue<>(new ArrayList<>(DEFAULT_ENTITIES));
        public final ConfigValue<List<String>> ignoredPlayers = new ConfigValue<>(new ArrayList<>());
        public final ConfigValue<List<String>> trollPlayers = new ConfigValue<>(new ArrayList<>());
        public final ConfigValue<Map<String, Boolean>> clientParticles = new ConfigValue<>(new LinkedHashMap<>());

        public final ConfigValue<Boolean> enableVocalFatigue = new ConfigValue<>(false);

        public final ConfigValue<Boolean> enableAntiSpam = new ConfigValue<>(false);

        public final ConfigValue<Boolean> enableCapsFeature = new ConfigValue<>(true);
        public final ConfigValue<Boolean> debugMode = new ConfigValue<>(false);

        public final ConfigValue<Boolean> showParticles = new ConfigValue<>(true);

        public final ConfigValue<Double> hearingRange = new ConfigValue<>(30.0);
        public final ConfigValue<Double> capsRangeBonus = new ConfigValue<>(5.0);
        public final ConfigValue<Double> mobSpeedBase = new ConfigValue<>(1.2);
        public final ConfigValue<Double> mobSpeedMax = new ConfigValue<>(2.0);

        public final ConfigValue<Double> trollSpeedMultiplier = new ConfigValue<>(2.5);

        public final ConfigValue<Integer> forgetTargetAfterSeconds = new ConfigValue<>(20);

        public final ConfigValue<Integer> scanCooldownTicks = new ConfigValue<>(40);

        public final ConfigValue<Integer> antiSpamMaxMessages = new ConfigValue<>(3);

        public final ConfigValue<Integer> antiSpamWindowSeconds = new ConfigValue<>(8);

        public final ConfigValue<Integer> traumaThreshold = new ConfigValue<>(1000);
        public final ConfigValue<Long> muteDurationTicks = new ConfigValue<>(600L);

    }

    private static final class PresetManagedState {
        double hearingRange;
        double capsRangeBonus;
        boolean enableVocalFatigue;
        boolean enableAntiSpam;
        int scanCooldownTicks;
        boolean showParticles;

        List<String> enabledEntities;
    }

    private static final class PresetRestorePoint {
        String presetName;
        PresetManagedState before;
        PresetManagedState applied;
        boolean touchesParticles;
        boolean touchesEntities;
    }

    public static final class PresetUndoResult {
        private final boolean available;
        private final String presetName;
        private final int restoredFields;
        private final int preservedManualFields;

        private PresetUndoResult(boolean available, String presetName,
                int restoredFields, int preservedManualFields) {
            this.available = available;
            this.presetName = presetName;
            this.restoredFields = restoredFields;
            this.preservedManualFields = preservedManualFields;
        }

        public boolean isAvailable() { return available; }
        public String getPresetName() { return presetName; }
        public int getRestoredFields() { return restoredFields; }
        public int getPreservedManualFields() { return preservedManualFields; }
    }

    private static final Set<String> BUILT_IN_PRESETS = Set.of("safe", "casual", "chaos", "silent");
    private static final int MAX_CUSTOM_PRESETS = 64;
    private static final Map<String, PresetManagedState> customPresets = new LinkedHashMap<>();
    private static PresetRestorePoint presetRestorePoint;

    private static String normalizePresetName(String presetName) {
        return presetName == null ? "" : presetName.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public static boolean isBuiltInPreset(String presetName) {
        return BUILT_IN_PRESETS.contains(normalizePresetName(presetName));
    }

    public static synchronized boolean isKnownPreset(String presetName) {
        String normalized = normalizePresetName(presetName);
        return BUILT_IN_PRESETS.contains(normalized) || customPresets.containsKey(normalized);
    }

    public static boolean isValidCustomPresetName(String presetName) {
        String normalized = normalizePresetName(presetName);
        return !BUILT_IN_PRESETS.contains(normalized)
            && normalized.matches("[a-z0-9][a-z0-9_-]{0,31}");
    }

    public static synchronized boolean hasCustomPreset(String presetName) {
        return customPresets.containsKey(normalizePresetName(presetName));
    }

    public static synchronized List<String> getPresetNames() {
        List<String> names = new ArrayList<>(List.of("safe", "casual", "chaos", "silent"));
        names.addAll(customPresets.keySet());
        return names;
    }

    public static synchronized List<String> getCustomPresetNames() {
        return new ArrayList<>(customPresets.keySet());
    }

    public static synchronized boolean saveCustomPreset(String presetName, boolean overwrite) {
        String normalized = normalizePresetName(presetName);
        if (!isValidCustomPresetName(normalized)) return false;
        if (!overwrite && customPresets.containsKey(normalized)) return false;
        if (!customPresets.containsKey(normalized) && customPresets.size() >= MAX_CUSTOM_PRESETS) return false;
        customPresets.put(normalized, snapshotPresetManagedState());
        return true;
    }

    public static synchronized boolean deleteCustomPreset(String presetName) {
        return customPresets.remove(normalizePresetName(presetName)) != null;
    }

    public static synchronized boolean renameCustomPreset(String oldName, String newName) {
        String oldNormalized = normalizePresetName(oldName);
        String newNormalized = normalizePresetName(newName);
        if (!customPresets.containsKey(oldNormalized) || !isValidCustomPresetName(newNormalized)
                || customPresets.containsKey(newNormalized)) {
            return false;
        }
        PresetManagedState state = customPresets.remove(oldNormalized);
        customPresets.put(newNormalized, state);
        return true;
    }

    public static synchronized boolean applyPresetValues(String presetName) {
        String normalized = normalizePresetName(presetName);
        if (!isKnownPreset(normalized)) return false;

        PresetManagedState before = snapshotPresetManagedState();
        PresetManagedState target = presetTargetState(normalized, before);
        boolean customPreset = customPresets.containsKey(normalized);
        boolean touchesParticles = customPreset || "silent".equals(normalized);
        boolean touchesEntities = customPreset;
        PresetRestorePoint existing = sanitizePresetRestorePoint(presetRestorePoint);
        if (existing == null || !existing.presetName.equals(normalized)
                || !matchesAppliedPreset(existing)
                || !samePresetManagedState(existing.applied, target, touchesParticles, touchesEntities)) {
            PresetRestorePoint point = new PresetRestorePoint();
            point.presetName = normalized;
            point.before = before;
            point.applied = copyPresetManagedState(target);
            point.touchesParticles = touchesParticles;
            point.touchesEntities = touchesEntities;
            presetRestorePoint = point;
        } else {
            presetRestorePoint = existing;
        }

        applyPresetManagedState(target, touchesParticles, touchesEntities);
        validateValues();
        return true;
    }

    public static synchronized PresetUndoResult undoLastPresetChanges() {
        PresetRestorePoint point = sanitizePresetRestorePoint(presetRestorePoint);
        if (point == null) {
            presetRestorePoint = null;
            return new PresetUndoResult(false, "", 0, 0);
        }

        int restored = 0;
        int preserved = 0;

        if (sameDouble(COMMON.hearingRange.get(), point.applied.hearingRange)) {
            if (!sameDouble(COMMON.hearingRange.get(), point.before.hearingRange)) {
                COMMON.hearingRange.set(point.before.hearingRange);
                restored++;
            }
        } else preserved++;

        if (sameDouble(COMMON.capsRangeBonus.get(), point.applied.capsRangeBonus)) {
            if (!sameDouble(COMMON.capsRangeBonus.get(), point.before.capsRangeBonus)) {
                COMMON.capsRangeBonus.set(point.before.capsRangeBonus);
                restored++;
            }
        } else preserved++;

        if (COMMON.enableVocalFatigue.get() == point.applied.enableVocalFatigue) {
            if (COMMON.enableVocalFatigue.get() != point.before.enableVocalFatigue) {
                COMMON.enableVocalFatigue.set(point.before.enableVocalFatigue);
                restored++;
            }
        } else preserved++;

        if (COMMON.enableAntiSpam.get() == point.applied.enableAntiSpam) {
            if (COMMON.enableAntiSpam.get() != point.before.enableAntiSpam) {
                COMMON.enableAntiSpam.set(point.before.enableAntiSpam);
                restored++;
            }
        } else preserved++;

        if (COMMON.scanCooldownTicks.get() == point.applied.scanCooldownTicks) {
            if (COMMON.scanCooldownTicks.get() != point.before.scanCooldownTicks) {
                COMMON.scanCooldownTicks.set(point.before.scanCooldownTicks);
                restored++;
            }
        } else preserved++;

        if (point.touchesParticles) {
            if (COMMON.showParticles.get() == point.applied.showParticles) {
                if (COMMON.showParticles.get() != point.before.showParticles) {
                    COMMON.showParticles.set(point.before.showParticles);
                    restored++;
                }
            } else preserved++;
        }

        if (point.touchesEntities) {
            List<String> currentEntities = COMMON.enabledEntities.get();
            if (sameEntityList(currentEntities, point.applied.enabledEntities)) {
                if (!sameEntityList(currentEntities, point.before.enabledEntities)) {
                    COMMON.enabledEntities.set(new ArrayList<>(point.before.enabledEntities));
                    restored++;
                }
            } else preserved++;
        }

        presetRestorePoint = null;
        validateValues();
        return new PresetUndoResult(true, point.presetName, restored, preserved);
    }

    public static synchronized int resetPresetManagedFieldsToDefaults() {
        int changed = 0;
        if (!sameDouble(COMMON.hearingRange.get(), 30.0)) { COMMON.hearingRange.set(30.0); changed++; }
        if (!sameDouble(COMMON.capsRangeBonus.get(), 5.0)) { COMMON.capsRangeBonus.set(5.0); changed++; }
        if (COMMON.enableVocalFatigue.get()) { COMMON.enableVocalFatigue.set(false); changed++; }
        if (COMMON.enableAntiSpam.get()) { COMMON.enableAntiSpam.set(false); changed++; }
        if (COMMON.scanCooldownTicks.get() != 40) { COMMON.scanCooldownTicks.set(40); changed++; }
        if (!COMMON.showParticles.get()) { COMMON.showParticles.set(true); changed++; }
        presetRestorePoint = null;
        validateValues();
        return changed;
    }

    public static synchronized String getUndoablePresetName() {
        PresetRestorePoint point = sanitizePresetRestorePoint(presetRestorePoint);
        if (point == null) {
            presetRestorePoint = null;
            return null;
        }
        return point.presetName;
    }

    public static class Spec {
        public boolean save() {
            return AttractToChatConfig.save();
        }
    }

    private static class ConfigData {
        int configVersion = CONFIG_VERSION;
        List<String> enabledEntities = COMMON.enabledEntities.get();
        List<String> ignoredPlayers = COMMON.ignoredPlayers.get();
        List<String> trollPlayers = COMMON.trollPlayers.get();
        Map<String, Boolean> clientParticles = COMMON.clientParticles.get();
        List<String> clientParticlesOptIn;
        boolean enableVocalFatigue = COMMON.enableVocalFatigue.get();
        boolean enableAntiSpam = COMMON.enableAntiSpam.get();
        boolean enableCapsFeature = COMMON.enableCapsFeature.get();
        boolean debugMode = COMMON.debugMode.get();
        boolean showParticles = COMMON.showParticles.get();
        double hearingRange = COMMON.hearingRange.get();
        double capsRangeBonus = COMMON.capsRangeBonus.get();
        double mobSpeedBase = COMMON.mobSpeedBase.get();
        double mobSpeedMax = COMMON.mobSpeedMax.get();
        double trollSpeedMultiplier = COMMON.trollSpeedMultiplier.get();
        int forgetTargetAfterSeconds = COMMON.forgetTargetAfterSeconds.get();
        int scanCooldownTicks = COMMON.scanCooldownTicks.get();
        int antiSpamMaxMessages = COMMON.antiSpamMaxMessages.get();
        int antiSpamWindowSeconds = COMMON.antiSpamWindowSeconds.get();
        int traumaThreshold = COMMON.traumaThreshold.get();
        long muteDurationTicks = COMMON.muteDurationTicks.get();
        Map<String, PresetManagedState> customPresets;
        PresetRestorePoint presetRestorePoint;
    }

    private static ConfigData lastPersistedData = snapshotCurrent();
    private static volatile long lastObservedFingerprint = Long.MIN_VALUE;

    public static synchronized void load() {
        Path configPath = getConfigPath();

        if (COMMON.enabledEntities.get() == null || COMMON.enabledEntities.get().isEmpty()) {
            COMMON.enabledEntities.set(buildDefaultEntityList());
        }
        lastPersistedData = snapshotCurrent();

        if (Files.exists(configPath)) {
            try {
                byte[] bytes = Files.readAllBytes(configPath);
                lastObservedFingerprint = fingerprint(bytes);
                boolean saveAfterMigration = applyConfigBytes(bytes);
                LOGGER.info("AttractToChat configuration loaded successfully.");
                if (saveAfterMigration) save();
            } catch (Exception e) {
                restoreSnapshot(lastPersistedData);
                LOGGER.error("Failed to parse AttractToChat config, keeping the last valid settings.", e);
            }
        } else {
            COMMON.enabledEntities.set(buildDefaultEntityList());
            save();
        }
    }

    public static synchronized boolean reloadIfChanged() {
        Path configPath = getConfigPath();
        if (!Files.exists(configPath)) return false;

        try {
            byte[] bytes = Files.readAllBytes(configPath);
            long fingerprint = fingerprint(bytes);
            if (fingerprint == lastObservedFingerprint) return false;

            lastObservedFingerprint = fingerprint;
            ConfigData previous = snapshotCurrent();
            try {
                boolean saveAfterMigration = applyConfigBytes(bytes);
                if (saveAfterMigration) save();
                LOGGER.info("AttractToChat configuration hot-reloaded from disk.");
                return true;
            } catch (Exception e) {
                restoreSnapshot(previous);
                LOGGER.error("Ignored invalid live edit in attracttochat-common.json; keeping the previous valid settings.", e);
                return false;
            }
        } catch (Exception e) {
            LOGGER.debug("Could not check AttractToChat config for live changes.", e);
            return false;
        }
    }

    private static boolean applyConfigBytes(byte[] bytes) {
        JsonObject root = GSON.fromJson(new String(bytes, StandardCharsets.UTF_8), JsonObject.class);
        ConfigData data = root == null ? null : GSON.fromJson(root, ConfigData.class);
        if (data == null) throw new IllegalArgumentException("Config JSON is empty or invalid");

        int loadedVersion = root.has("configVersion") ? root.get("configVersion").getAsInt() : 0;
        boolean migratedLegacyDefaults = loadedVersion < SAFE_DEFAULTS_VERSION;
        boolean needsRewrite = loadedVersion < CONFIG_VERSION;

        List<String> normalizedEntities = data.enabledEntities == null
            ? null
            : normalizeEntities(data.enabledEntities);
        List<String> loadedEntities = normalizedEntities == null
            ? buildDefaultEntityList()
            : filterAttractableEntities(normalizedEntities);

        if (data.enabledEntities != null && !loadedEntities.equals(data.enabledEntities)) {
            needsRewrite = true;
        }

        if (loadedVersion < EXPLICIT_ENTITY_LIST_VERSION && loadedEntities.isEmpty()) {
            loadedEntities = buildDefaultEntityList();
            needsRewrite = true;
        }

        if (loadedVersion < HOSTILE_DEFAULTS_VERSION
            && loadedVersion >= EXPLICIT_ENTITY_LIST_VERSION
            && normalizedEntities != null
            && normalizedEntities.equals(buildLegacyAllEntityList())) {
            loadedEntities = buildDefaultEntityList();
            needsRewrite = true;
            LOGGER.info("Replaced the overly broad v7 entity defaults with the hostile-mob allow-list.");
        }
        COMMON.enabledEntities.set(loadedEntities);

        if (data.ignoredPlayers != null) COMMON.ignoredPlayers.set(sanitizeNames(data.ignoredPlayers));
        if (data.trollPlayers != null) COMMON.trollPlayers.set(sanitizeNames(data.trollPlayers));
        if (loadedVersion < PARTICLES_PREFERENCE_VERSION) {
            COMMON.clientParticles.set(migrateLegacyParticlesPreference(root));
            needsRewrite = true;
            LOGGER.info("Migrated AttractToChat particle preferences to the per-player on/off model.");
        } else if (root.has("clientParticles") && data.clientParticles != null) {
            COMMON.clientParticles.set(sanitizeParticlesPreference(data.clientParticles));
        } else {
            COMMON.clientParticles.set(new LinkedHashMap<>());
            needsRewrite = true;
        }
        COMMON.enableVocalFatigue.set(migratedLegacyDefaults ? false : data.enableVocalFatigue);
        COMMON.enableAntiSpam.set(migratedLegacyDefaults ? false : data.enableAntiSpam);
        if (root.has("enableCapsFeature")) {
            COMMON.enableCapsFeature.set(data.enableCapsFeature);
        } else {

            COMMON.enableCapsFeature.set(true);
            needsRewrite = true;
        }
        COMMON.debugMode.set(data.debugMode);
        COMMON.showParticles.set(data.showParticles);
        COMMON.hearingRange.set(data.hearingRange);
        COMMON.capsRangeBonus.set(data.capsRangeBonus);
        COMMON.mobSpeedBase.set(data.mobSpeedBase);
        COMMON.mobSpeedMax.set(data.mobSpeedMax);
        if (root.has("trollSpeedMultiplier")) COMMON.trollSpeedMultiplier.set(data.trollSpeedMultiplier);
        COMMON.forgetTargetAfterSeconds.set(data.forgetTargetAfterSeconds);
        COMMON.scanCooldownTicks.set(data.scanCooldownTicks);
        if (root.has("antiSpamMaxMessages")) COMMON.antiSpamMaxMessages.set(data.antiSpamMaxMessages);
        if (root.has("antiSpamWindowSeconds")) COMMON.antiSpamWindowSeconds.set(data.antiSpamWindowSeconds);
        COMMON.traumaThreshold.set(data.traumaThreshold);
        COMMON.muteDurationTicks.set(data.muteDurationTicks);
        customPresets.clear();
        customPresets.putAll(sanitizeCustomPresets(data.customPresets));
        presetRestorePoint = sanitizePresetRestorePoint(data.presetRestorePoint);
        validateValues();
        lastPersistedData = snapshotCurrent();

        if (migratedLegacyDefaults) {
            LOGGER.info("Migrated AttractToChat config to version {} with safe default toggles.", CONFIG_VERSION);
        }
        return needsRewrite;
    }

    public static synchronized boolean save() {
        validateValues();
        ConfigData pendingData = snapshotCurrent();
        Path configPath = getConfigPath();
        Path tempPath = configPath.resolveSibling(configPath.getFileName() + ".tmp");
        try {
            Files.createDirectories(configPath.getParent());
            JsonObject tree = GSON.toJsonTree(pendingData, ConfigData.class).getAsJsonObject();
            JsonObject commented = new JsonObject();
            commented.addProperty(ConfigComments.HEADER_KEY, ConfigComments.header());
            for (Map.Entry<String, JsonElement> entry : tree.entrySet()) {
                String comment = ConfigComments.forField(entry.getKey());
                if (comment != null) {
                    commented.addProperty("# " + entry.getKey(), comment);
                }
                commented.add(entry.getKey(), entry.getValue());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(tempPath, StandardCharsets.UTF_8)) {
                GSON.toJson(commented, writer);
            }
            try {
                Files.move(tempPath, configPath, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tempPath, configPath, StandardCopyOption.REPLACE_EXISTING);
            }
            lastPersistedData = pendingData;
            lastObservedFingerprint = fingerprint(Files.readAllBytes(configPath));
            return true;
        } catch (Exception e) {
            restoreSnapshot(lastPersistedData);
            LOGGER.error("Failed to write AttractToChat configuration to disk.", e);
            try {
                Files.deleteIfExists(tempPath);
            } catch (Exception cleanupError) {
                LOGGER.debug("Failed to clean temporary AttractToChat config file.", cleanupError);
            }
            return false;
        }
    }

    private static ConfigData snapshotCurrent() {
        ConfigData snapshot = new ConfigData();
        snapshot.enabledEntities = new ArrayList<>(COMMON.enabledEntities.get());
        snapshot.ignoredPlayers = new ArrayList<>(COMMON.ignoredPlayers.get());
        snapshot.trollPlayers = new ArrayList<>(COMMON.trollPlayers.get());
        snapshot.clientParticles = new LinkedHashMap<>(COMMON.clientParticles.get());
        snapshot.customPresets = copyCustomPresets(customPresets);
        snapshot.presetRestorePoint = copyPresetRestorePoint(presetRestorePoint);
        return snapshot;
    }

    private static void restoreSnapshot(ConfigData snapshot) {
        if (snapshot == null) return;
        COMMON.enabledEntities.set(new ArrayList<>(snapshot.enabledEntities));
        COMMON.ignoredPlayers.set(new ArrayList<>(snapshot.ignoredPlayers));
        COMMON.trollPlayers.set(new ArrayList<>(snapshot.trollPlayers));
        COMMON.clientParticles.set(new LinkedHashMap<>(snapshot.clientParticles));
        COMMON.enableVocalFatigue.set(snapshot.enableVocalFatigue);
        COMMON.enableAntiSpam.set(snapshot.enableAntiSpam);
        COMMON.enableCapsFeature.set(snapshot.enableCapsFeature);
        COMMON.debugMode.set(snapshot.debugMode);
        COMMON.showParticles.set(snapshot.showParticles);
        COMMON.hearingRange.set(snapshot.hearingRange);
        COMMON.capsRangeBonus.set(snapshot.capsRangeBonus);
        COMMON.mobSpeedBase.set(snapshot.mobSpeedBase);
        COMMON.mobSpeedMax.set(snapshot.mobSpeedMax);
        COMMON.trollSpeedMultiplier.set(snapshot.trollSpeedMultiplier);
        COMMON.forgetTargetAfterSeconds.set(snapshot.forgetTargetAfterSeconds);
        COMMON.scanCooldownTicks.set(snapshot.scanCooldownTicks);
        COMMON.antiSpamMaxMessages.set(snapshot.antiSpamMaxMessages);
        COMMON.antiSpamWindowSeconds.set(snapshot.antiSpamWindowSeconds);
        COMMON.traumaThreshold.set(snapshot.traumaThreshold);
        COMMON.muteDurationTicks.set(snapshot.muteDurationTicks);
        customPresets.clear();
        customPresets.putAll(copyCustomPresets(snapshot.customPresets));
        presetRestorePoint = copyPresetRestorePoint(snapshot.presetRestorePoint);
    }

    private static PresetManagedState snapshotPresetManagedState() {
        PresetManagedState state = new PresetManagedState();
        state.hearingRange = COMMON.hearingRange.get();
        state.capsRangeBonus = COMMON.capsRangeBonus.get();
        state.enableVocalFatigue = COMMON.enableVocalFatigue.get();
        state.enableAntiSpam = COMMON.enableAntiSpam.get();
        state.scanCooldownTicks = COMMON.scanCooldownTicks.get();
        state.showParticles = COMMON.showParticles.get();
        state.enabledEntities = new ArrayList<>(COMMON.enabledEntities.get());
        return state;
    }

    private static PresetManagedState presetTargetState(String presetName, PresetManagedState base) {
        PresetManagedState custom = customPresets.get(normalizePresetName(presetName));
        if (custom != null) return copyPresetManagedState(custom);

        PresetManagedState target = copyPresetManagedState(base);
        switch (normalizePresetName(presetName)) {
            case "safe" -> {
                target.hearingRange = 24.0;
                target.capsRangeBonus = 4.0;
                target.enableVocalFatigue = false;
                target.enableAntiSpam = false;
                target.scanCooldownTicks = 20;
            }
            case "casual" -> {
                target.hearingRange = 32.0;
                target.capsRangeBonus = 6.0;
                target.enableVocalFatigue = false;
                target.enableAntiSpam = false;
                target.scanCooldownTicks = 15;
            }
            case "chaos" -> {
                target.hearingRange = 60.0;
                target.capsRangeBonus = 14.0;
                target.enableVocalFatigue = false;
                target.enableAntiSpam = false;
                target.scanCooldownTicks = 5;
            }
            case "silent" -> {
                target.hearingRange = 30.0;
                target.capsRangeBonus = 5.0;
                target.enableVocalFatigue = false;
                target.enableAntiSpam = false;
                target.scanCooldownTicks = 20;
                target.showParticles = false;
            }
            default -> throw new IllegalArgumentException("Unknown ATC preset: " + presetName);
        }
        return target;
    }

    private static PresetRestorePoint sanitizePresetRestorePoint(PresetRestorePoint source) {
        if (source == null || source.before == null || source.applied == null
                || normalizePresetName(source.presetName).isEmpty()) return null;
        PresetRestorePoint clean = new PresetRestorePoint();
        clean.presetName = normalizePresetName(source.presetName);
        clean.before = sanitizePresetManagedState(source.before);
        clean.applied = sanitizePresetManagedState(source.applied);
        clean.touchesParticles = source.touchesParticles;
        clean.touchesEntities = source.touchesEntities;
        return clean;
    }

    private static PresetManagedState sanitizePresetManagedState(PresetManagedState source) {
        if (source == null) return null;
        PresetManagedState clean = copyPresetManagedState(source);
        clean.hearingRange = clamp(clean.hearingRange, 0.0, 500.0);
        clean.capsRangeBonus = clamp(clean.capsRangeBonus, 0.0, 100.0);
        clean.scanCooldownTicks = Math.max(1, Math.min(1200, clean.scanCooldownTicks));

        clean.enabledEntities = source.enabledEntities == null
            ? new ArrayList<>(COMMON.enabledEntities.get())
            : filterAttractableEntities(normalizeEntities(source.enabledEntities));
        return clean;
    }

    private static Map<String, PresetManagedState> sanitizeCustomPresets(
            Map<String, PresetManagedState> source) {
        Map<String, PresetManagedState> clean = new LinkedHashMap<>();
        if (source == null) return clean;
        for (Map.Entry<String, PresetManagedState> entry : source.entrySet()) {
            if (clean.size() >= MAX_CUSTOM_PRESETS) break;
            String name = normalizePresetName(entry.getKey());
            if (!isValidCustomPresetName(name) || entry.getValue() == null || clean.containsKey(name)) continue;
            clean.put(name, sanitizePresetManagedState(entry.getValue()));
        }
        return clean;
    }

    private static Map<String, PresetManagedState> copyCustomPresets(
            Map<String, PresetManagedState> source) {
        Map<String, PresetManagedState> copy = new LinkedHashMap<>();
        if (source == null) return copy;
        for (Map.Entry<String, PresetManagedState> entry : source.entrySet()) {
            copy.put(entry.getKey(), copyPresetManagedState(entry.getValue()));
        }
        return copy;
    }

    private static PresetRestorePoint copyPresetRestorePoint(PresetRestorePoint source) {
        if (source == null) return null;
        PresetRestorePoint copy = new PresetRestorePoint();
        copy.presetName = source.presetName;
        copy.before = copyPresetManagedState(source.before);
        copy.applied = copyPresetManagedState(source.applied);
        copy.touchesParticles = source.touchesParticles;
        copy.touchesEntities = source.touchesEntities;
        return copy;
    }

    private static PresetManagedState copyPresetManagedState(PresetManagedState source) {
        if (source == null) return null;
        PresetManagedState copy = new PresetManagedState();
        copy.hearingRange = source.hearingRange;
        copy.capsRangeBonus = source.capsRangeBonus;
        copy.enableVocalFatigue = source.enableVocalFatigue;
        copy.enableAntiSpam = source.enableAntiSpam;
        copy.scanCooldownTicks = source.scanCooldownTicks;
        copy.showParticles = source.showParticles;
        copy.enabledEntities = source.enabledEntities == null
            ? new ArrayList<>()
            : new ArrayList<>(source.enabledEntities);
        return copy;
    }

    private static void applyPresetManagedState(PresetManagedState state, boolean includeParticles,
            boolean includeEntities) {
        COMMON.hearingRange.set(state.hearingRange);
        COMMON.capsRangeBonus.set(state.capsRangeBonus);
        COMMON.enableVocalFatigue.set(state.enableVocalFatigue);
        COMMON.enableAntiSpam.set(state.enableAntiSpam);
        COMMON.scanCooldownTicks.set(state.scanCooldownTicks);
        if (includeParticles) COMMON.showParticles.set(state.showParticles);
        if (includeEntities) COMMON.enabledEntities.set(new ArrayList<>(state.enabledEntities));
    }

    private static boolean matchesAppliedPreset(PresetRestorePoint point) {
        if (point == null || point.applied == null) return false;
        if (!sameDouble(COMMON.hearingRange.get(), point.applied.hearingRange)) return false;
        if (!sameDouble(COMMON.capsRangeBonus.get(), point.applied.capsRangeBonus)) return false;
        if (COMMON.enableVocalFatigue.get() != point.applied.enableVocalFatigue) return false;
        if (COMMON.enableAntiSpam.get() != point.applied.enableAntiSpam) return false;
        if (COMMON.scanCooldownTicks.get() != point.applied.scanCooldownTicks) return false;
        if (point.touchesParticles && COMMON.showParticles.get() != point.applied.showParticles) return false;
        return !point.touchesEntities || sameEntityList(COMMON.enabledEntities.get(), point.applied.enabledEntities);
    }

    private static boolean samePresetManagedState(PresetManagedState left, PresetManagedState right,
            boolean includeParticles, boolean includeEntities) {
        if (left == null || right == null) return false;
        if (!sameDouble(left.hearingRange, right.hearingRange)) return false;
        if (!sameDouble(left.capsRangeBonus, right.capsRangeBonus)) return false;
        if (left.enableVocalFatigue != right.enableVocalFatigue) return false;
        if (left.enableAntiSpam != right.enableAntiSpam) return false;
        if (left.scanCooldownTicks != right.scanCooldownTicks) return false;
        if (includeParticles && left.showParticles != right.showParticles) return false;
        return !includeEntities || sameEntityList(left.enabledEntities, right.enabledEntities);
    }

    private static boolean sameEntityList(List<String> left, List<String> right) {
        if (left == null || right == null) return left == right;
        return left.equals(right);
    }

    private static boolean sameDouble(double left, double right) {
        return Double.compare(left, right) == 0;
    }

    private static void validateValues() {
        COMMON.hearingRange.set(clamp(COMMON.hearingRange.get(), 0.0, 500.0));
        COMMON.capsRangeBonus.set(clamp(COMMON.capsRangeBonus.get(), 0.0, 100.0));
        COMMON.mobSpeedBase.set(clamp(COMMON.mobSpeedBase.get(), 0.1, 3.0));
        COMMON.mobSpeedMax.set(clamp(COMMON.mobSpeedMax.get(), COMMON.mobSpeedBase.get(), 4.0));
        COMMON.trollSpeedMultiplier.set(clamp(COMMON.trollSpeedMultiplier.get(), 1.0, 8.0));
        COMMON.forgetTargetAfterSeconds.set(Math.max(1, Math.min(300, COMMON.forgetTargetAfterSeconds.get())));
        COMMON.scanCooldownTicks.set(Math.max(1, Math.min(1200, COMMON.scanCooldownTicks.get())));
        COMMON.antiSpamMaxMessages.set(Math.max(0, Math.min(50, COMMON.antiSpamMaxMessages.get())));
        COMMON.antiSpamWindowSeconds.set(Math.max(1, Math.min(120, COMMON.antiSpamWindowSeconds.get())));
        COMMON.traumaThreshold.set(Math.max(1, COMMON.traumaThreshold.get()));
        COMMON.muteDurationTicks.set(Math.max(1L, COMMON.muteDurationTicks.get()));
    }

    public static Stream<ResourceLocation> configurableEntityIds() {
        return BuiltInRegistries.ENTITY_TYPE.keySet().stream()
            .filter(AttractToChatConfig::isConfigurableEntityId)
            .sorted(Comparator.comparing(ResourceLocation::toString));
    }

    public static boolean isConfigurableEntityId(ResourceLocation id) {
        return isLegacyBroadConfigurableEntityId(id);
    }

    private static boolean isLegacyBroadConfigurableEntityId(ResourceLocation id) {
        if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) return false;
        String raw = id.toString();
        String path = id.getPath();
        if (NON_CONFIGURABLE_ENTITY_IDS.contains(raw)) return false;
        return !path.equals("boat")
            && !path.endsWith("_boat")
            && !path.endsWith("_chest_boat")
            && !path.endsWith("_raft")
            && !path.endsWith("_chest_raft")
            && !path.equals("minecart")
            && !path.endsWith("_minecart");
    }

    private static List<String> buildDefaultEntityList() {
        List<String> availableDefaults = DEFAULT_ENTITIES.stream()
            .map(ResourceLocation::tryParse)
            .filter(id -> id != null && isConfigurableEntityId(id))
            .map(ResourceLocation::toString)
            .toList();
        return new ArrayList<>(availableDefaults.isEmpty() ? DEFAULT_ENTITIES : availableDefaults);
    }

    private static List<String> buildLegacyAllEntityList() {
        return BuiltInRegistries.ENTITY_TYPE.keySet().stream()
            .filter(AttractToChatConfig::isLegacyBroadConfigurableEntityId)
            .sorted(Comparator.comparing(ResourceLocation::toString))
            .map(ResourceLocation::toString)
            .toList();
    }

    private static Path getConfigPath() {
        return Platform.getHelper().getConfigDir().resolve("attracttochat-common.json");
    }

    private static long fingerprint(byte[] bytes) {
        CRC32 crc = new CRC32();
        crc.update(bytes);
        return (crc.getValue() << 32) ^ (bytes.length & 0xffffffffL);
    }

    private static List<String> normalizeEntities(List<String> values) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value == null) continue;
            String normalized = value.trim().toLowerCase(java.util.Locale.ROOT);
            if (normalized.isEmpty()) continue;

            boolean exclude = normalized.startsWith("!");
            String body = exclude ? normalized.substring(1).trim() : normalized;
            if (body.isEmpty()) continue;
            if (!body.contains(":")) body = "minecraft:" + body;
            ResourceLocation id = ResourceLocation.tryParse(body);
            if (id == null) {
                LOGGER.warn("Ignoring invalid entity id in ATC config: {}", value);
                continue;
            }
            normalized = exclude ? "!" + id : id.toString();
            if (!result.contains(normalized)) result.add(normalized);
        }
        return result;
    }

    private static List<String> filterAttractableEntities(List<String> values) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            boolean exclude = value.startsWith("!");
            String body = exclude ? value.substring(1) : value;
            ResourceLocation id = ResourceLocation.tryParse(body);
            if (id == null || !isConfigurableEntityId(id)) {
                LOGGER.warn("Ignoring unknown, projectile, vehicle or utility entity in ATC config: {}", value);
                continue;
            }
            String normalized = exclude ? "!" + id : id.toString();
            if (!result.contains(normalized)) result.add(normalized);
        }
        return result;
    }

    private static Map<String, Boolean> migrateLegacyParticlesPreference(JsonObject root) {
        Map<String, Boolean> migrated = new LinkedHashMap<>();
        if (root.has("clientParticlesOptIn") && root.get("clientParticlesOptIn").isJsonArray()) {
            for (var element : root.getAsJsonArray("clientParticlesOptIn")) {
                if (!element.isJsonPrimitive()) continue;
                try {
                    String normalized = java.util.UUID.fromString(element.getAsString().trim()).toString();
                    migrated.put(normalized, Boolean.TRUE);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return migrated;
    }

    private static Map<String, Boolean> sanitizeParticlesPreference(Map<String, Boolean> values) {
        Map<String, Boolean> result = new LinkedHashMap<>();
        if (values == null) return result;
        for (Map.Entry<String, Boolean> entry : values.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            try {
                String normalized = java.util.UUID.fromString(entry.getKey().trim()).toString();
                result.put(normalized, entry.getValue());
            } catch (IllegalArgumentException ignored) {}
        }
        return result;
    }

    private static List<String> sanitizeNames(List<String> values) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank() && !result.contains(value.trim())) {
                result.add(value.trim());
            }
        }
        return result;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
