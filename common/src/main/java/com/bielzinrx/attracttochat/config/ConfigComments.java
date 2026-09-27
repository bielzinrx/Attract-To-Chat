package com.bielzinrx.attracttochat.config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfigComments {

    public static final String HEADER_KEY = "#";

    private static final Map<String, String> COMMENTS = buildComments();

    private ConfigComments() {}

    public static String header() {
        return "Attract to Chat config. '#' fields are comments, regenerated on save - edit values freely. "
            + "All changes apply live. In-game: /atc config info <option>";
    }

    public static String forField(String field) {
        return COMMENTS.get(field);
    }

    private static Map<String, String> buildComments() {
        Map<String, String> comments = new LinkedHashMap<>();
        comments.put("configVersion", "Migration schema version. Do not edit.");
        comments.put("enabledEntities",
            "Mobs that hear chat, as 'modid:entityid'; '!' prefix excludes; empty list = off. Default: hostiles. /atc entity");
        comments.put("ignoredPlayers", "Voices that never attract. '@a' = everyone. /atc ignore add|remove");
        comments.put("trollPlayers",
            "Troll Mode marks: voices pull harder (trollSpeedMultiplier), exempt from anti-spam. /atc trollmode");
        comments.put("clientParticles", "UUID -> particles on/off per player (needs the client mod). /atc client particles");
        comments.put("enableVocalFatigue",
            "Default off. Shout too much -> brief mute + curious mobs. Milk clears, honey helps, death resets.");
        comments.put("enableAntiSpam",
            "Default off. Chat bursts pause attraction; messages stay visible. Troll players exempt.");
        comments.put("enableCapsFeature", "Default on. CAPS words are heard farther (+capsRangeBonus each).");
        comments.put("debugMode", "Logs who said what and which mobs heard. Support aid; noisy in production.");
        comments.put("hearingRange", "Blocks normal chat carries (default 30, 0-500). 0 = normal chat silent.");
        comments.put("capsRangeBonus", "Extra blocks per CAPS word (default 5, 0-100). 'HELLO WORLD' = 40 by default.");
        comments.put("mobSpeedBase", "Walk-speed multiplier near the sound (default 1.2, 0.1-3.0). 1.0 = normal pace.");
        comments.put("mobSpeedMax", "Multiplier cap for far sounds (default 2.0, up to 4.0); scales with distance.");
        comments.put("trollSpeedMultiplier", "Pull speed toward Troll Mode players (default 2.5, 1.0-8.0).");
        comments.put("forgetTargetAfterSeconds", "Search time before giving up (default 20, 1-300 s).");
        comments.put("scanCooldownTicks", "Gap between scans per player (default 40 = 2 s, 1-1200 ticks).");
        comments.put("antiSpamMaxMessages", "Messages per window before pause (default 3, 0-50; needs enableAntiSpam).");
        comments.put("antiSpamWindowSeconds", "Sliding window length (default 8, 1-120 s; needs enableAntiSpam).");
        comments.put("traumaThreshold", "Trauma needed to go hoarse (default 1000, min 1); decays with calm chat.");
        comments.put("muteDurationTicks", "Fatigue mute length (default 600 = 30 s; 20 ticks = 1 s).");
        comments.put("customPresets", "Presets from /atc preset custom save; store only the managed fields.");
        comments.put("presetRestorePoint", "Undo state for /atc preset undo. Do not edit.");
        return comments;
    }
}
