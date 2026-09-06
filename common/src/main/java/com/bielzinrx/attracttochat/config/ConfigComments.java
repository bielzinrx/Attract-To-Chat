package com.bielzinrx.attracttochat.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Human-readable "#" comments written into attracttochat-common.json.
 * The loader ignores these keys; the saver regenerates them on every write,
 * so they always describe the current schema. Keep each entry short enough
 * to read as one JSON line, but detailed: what it does, unit, default,
 * valid range and a practical example where it helps.
 */
public final class ConfigComments {

    public static final String HEADER_KEY = "#";

    private static final Map<String, String> COMMENTS = buildComments();

    private ConfigComments() {}

    public static String header() {
        return "Attract to Chat - server configuration. "
            + "Lines starting with '#' are comments written by the mod: they are ignored when loading "
            + "and regenerated on every save, so you can edit values freely. "
            + "All changes apply live, no restart needed. "
            + "In-game reference: /atc config list, /atc config info <option>, /atc help config.";
    }

    public static String forField(String field) {
        return COMMENTS.get(field);
    }

    private static Map<String, String> buildComments() {
        Map<String, String> comments = new LinkedHashMap<>();
        comments.put("configVersion",
            "Internal schema version used to migrate old config files. Do not edit.");
        comments.put("enabledEntities",
            "Mobs that react to chat. Format 'modid:entityid', one per entry. "
            + "Prefix an entry with '!' to exclude it (example: \"!minecraft:creeper\" keeps creepers calm). "
            + "An empty list turns the feature off; a list with only '!' entries means 'everything except these'. "
            + "Default: hostile mobs (zombie, skeleton, creeper, ...). "
            + "In-game: /atc entity add|remove|list.");
        comments.put("ignoredPlayers",
            "Players whose chat never attracts mobs. Use /atc ignore add <player> in-game; "
            + "'@a' ignores everyone. Example: [\"Steve\"] or [\"@a\"].");
        comments.put("trollPlayers",
            "Troll Mode list: mobs chase these players' voices harder and faster "
            + "(see trollSpeedMultiplier) and these players bypass anti-spam. "
            + "In-game: /atc trollmode add|remove|list.");
        comments.put("clientParticles",
            "Per-player particle preference set by /atc client particles enable|disable. "
            + "Keys are player UUIDs, value true = that player sees investigation particles "
            + "(requires the client mod). The server master switch is showParticles below.");
        comments.put("enableVocalFatigue",
            "Vocal fatigue (default OFF): shout too much (CAPS, !!!) and you go hoarse - "
            + "muted for muteDurationTicks while nearby mobs come to investigate. "
            + "Milk clears it instantly, honey helps, dying resets it. "
            + "Shout trauma builds up and decays over time (see traumaThreshold).");
        comments.put("enableAntiSpam",
            "Anti-spam (default OFF): more than antiSpamMaxMessages within antiSpamWindowSeconds "
            + "pauses attraction for scanCooldownTicks. The message still appears in chat - "
            + "it just stops attracting mobs. Troll Mode players are exempt.");
        comments.put("enableCapsFeature",
            "CAPS loudness (default ON): TYPING IN CAPS is heard from hearingRange "
            + "plus capsRangeBonus per CAPS word. Set false to treat CAPS as normal chat.");
        comments.put("debugMode",
            "Verbose console logging of every attraction scan: who said what, which mobs heard it "
            + "and at what range. Great for support tickets, noisy for production servers.");
        comments.put("showParticles",
            "Master switch for investigation particles (END_ROD trail + NOTE burst at the target). "
            + "Players with the client mod can toggle their own view with /atc client particles.");
        comments.put("hearingRange",
            "How far mobs hear normal chat, in blocks (default 30, range 0-500). "
            + "Example: 30 means a zombie 30 blocks away turns and walks toward you. "
            + "0 disables attraction for normal chat. In-game: /atc config hearingrange <value>.");
        comments.put("capsRangeBonus",
            "Extra hearing range per CAPS word, in blocks (default 5, range 0-100). "
            + "Example: 'HELLO WORLD' (2 CAPS words) is heard from 30 + 2x5 = 40 blocks with defaults.");
        comments.put("mobSpeedBase",
            "Mob speed while investigating a sound, as a multiplier of normal walk speed "
            + "(default 1.2, range 0.1-3.0). 1.0 = normal pace, 2.0 = double.");
        comments.put("mobSpeedMax",
            "Top investigation speed multiplier for far targets (default 2.0, "
            + "range mobSpeedBase-4.0). The farther the sound, the closer speed gets to this cap.");
        comments.put("trollSpeedMultiplier",
            "Speed multiplier for mobs drawn to a Troll Mode player (default 2.5, range 1.0-8.0). "
            + "Stacks with the distance scaling above.");
        comments.put("forgetTargetAfterSeconds",
            "Seconds a mob keeps searching the sound spot before giving up and "
            + "returning to normal AI (default 20, range 1-300).");
        comments.put("scanCooldownTicks",
            "Minimum gap between attraction scans per player, in ticks "
            + "(default 40 = 2 seconds, range 1-1200). 20 ticks = 1 second.");
        comments.put("antiSpamMaxMessages",
            "Messages allowed inside the anti-spam window before attraction pauses "
            + "(default 3, range 0-50). Only used when enableAntiSpam is true.");
        comments.put("antiSpamWindowSeconds",
            "Size of the anti-spam sliding window, in seconds (default 8, range 1-120).");
        comments.put("traumaThreshold",
            "Shout trauma needed to trigger vocal fatigue (default 1000, minimum 1). "
            + "Lower values make players go hoarse sooner. Trauma decays while chatting calmly.");
        comments.put("muteDurationTicks",
            "How long vocal fatigue mutes a player, in ticks (default 600 = 30 seconds, minimum 1). "
            + "20 ticks = 1 second.");
        comments.put("customPresets",
            "Presets saved with /atc preset custom save <name>. Each one stores only the "
            + "preset-managed fields; everything else is left untouched when applied.");
        comments.put("presetRestorePoint",
            "Internal state used by /atc preset undo. Do not edit.");
        return comments;
    }
}
