package com.bielzinrx.attracttochat.config;

import java.util.List;
import java.util.function.Supplier;

/**
 * Single source of truth for every numeric/boolean ATC option shown by
 * /atc config list and /atc config info. Each entry pairs the live config
 * value with its default and valid range, plus a lang key that carries the
 * human explanation (unit, example and tip) in the player's language.
 */
public final class OptionCatalog {

    public record Option(String id, Supplier<Object> value, String defaultValue,
            double min, double max, boolean numeric) {

        public String langKey() {
            return "option.attracttochat." + id;
        }

        public String currentValueText() {
            Object current = value.get();
            if (current instanceof Double d) {
                return d == Math.floor(d) && !Double.isInfinite(d)
                    ? String.valueOf((long) (double) d)
                    : String.valueOf(d);
            }
            if (current instanceof Boolean b) {
                return b ? "ON" : "OFF";
            }
            return String.valueOf(current);
        }
    }

    public static final List<Option> OPTIONS = List.of(
        new Option("hearingRange", () -> AttractToChatConfig.COMMON.hearingRange.get(), "30", 0.0, 500.0, true),
        new Option("capsRangeBonus", () -> AttractToChatConfig.COMMON.capsRangeBonus.get(), "5", 0.0, 100.0, true),
        new Option("mobSpeedBase", () -> AttractToChatConfig.COMMON.mobSpeedBase.get(), "1.2", 0.1, 3.0, true),
        new Option("mobSpeedMax", () -> AttractToChatConfig.COMMON.mobSpeedMax.get(), "2.0", 0.1, 4.0, true),
        new Option("trollSpeedMultiplier", () -> AttractToChatConfig.COMMON.trollSpeedMultiplier.get(), "2.5", 1.0, 8.0, true),
        new Option("forgetTargetAfterSeconds", () -> AttractToChatConfig.COMMON.forgetTargetAfterSeconds.get(), "20", 1.0, 300.0, true),
        new Option("scanCooldownTicks", () -> AttractToChatConfig.COMMON.scanCooldownTicks.get(), "40", 1.0, 1200.0, true),
        new Option("antiSpamMaxMessages", () -> AttractToChatConfig.COMMON.antiSpamMaxMessages.get(), "3", 0.0, 50.0, true),
        new Option("antiSpamWindowSeconds", () -> AttractToChatConfig.COMMON.antiSpamWindowSeconds.get(), "8", 1.0, 120.0, true),
        new Option("traumaThreshold", () -> AttractToChatConfig.COMMON.traumaThreshold.get(), "1000", 1.0, 100000.0, true),
        new Option("muteDurationTicks", () -> AttractToChatConfig.COMMON.muteDurationTicks.get(), "600", 1.0, 72000.0, true),
        new Option("enableVocalFatigue", () -> AttractToChatConfig.COMMON.enableVocalFatigue.get(), "OFF", 0.0, 0.0, false),
        new Option("enableAntiSpam", () -> AttractToChatConfig.COMMON.enableAntiSpam.get(), "OFF", 0.0, 0.0, false),
        new Option("enableCapsFeature", () -> AttractToChatConfig.COMMON.enableCapsFeature.get(), "ON", 0.0, 0.0, false),
        new Option("showParticles", () -> AttractToChatConfig.COMMON.showParticles.get(), "ON", 0.0, 0.0, false),
        new Option("debugMode", () -> AttractToChatConfig.COMMON.debugMode.get(), "OFF", 0.0, 0.0, false)
    );

    private OptionCatalog() {}

    public static Option byId(String id) {
        if (id == null || id.isBlank()) return null;
        String normalized = id.trim().toLowerCase(java.util.Locale.ROOT);
        for (Option option : OPTIONS) {
            if (option.id().equalsIgnoreCase(normalized)) return option;
        }
        return null;
    }

    public static List<String> ids() {
        return OPTIONS.stream().map(Option::id).toList();
    }
}
