package com.bielzinrx.attracttochat.i18n;

import com.bielzinrx.attracttochat.AttractToChat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerTranslations {
    private static final String FALLBACK_LANGUAGE = "en_us";
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en_us", "pt_br", "es_es");
    private static final Map<String, Map<String, String>> TRANSLATIONS = loadTranslations();
    private static final Map<UUID, String> PLAYER_LANGUAGES = new ConcurrentHashMap<>();
    private static final Set<String> REPORTED_MISSING_KEYS = ConcurrentHashMap.newKeySet();
    private static final Set<String> REPORTED_FORMAT_ERRORS = ConcurrentHashMap.newKeySet();

    private ServerTranslations() {}

    public static Component component(ServerPlayer player, String key, Object... args) {
        return Component.literal(translate(languageOf(player), key, args));
    }

    public static Component component(CommandSourceStack source, String key, Object... args) {
        return Component.literal(translate(languageOf(source), key, args));
    }

    public static String translate(ServerPlayer player, String key, Object... args) {
        return translate(languageOf(player), key, args);
    }

    public static String translate(CommandSourceStack source, String key, Object... args) {
        return translate(languageOf(source), key, args);
    }

    public static String translate(String language, String key, Object... args) {
        String normalizedLanguage = normalizeLanguage(language);
        String template = findTemplate(normalizedLanguage, key);
        if (template == null) {
            if (REPORTED_MISSING_KEYS.add(key)) {
                AttractToChat.LOGGER.error("[AttractToChat] Missing server translation key: {}", key);
            }
            return "[ATC] Missing translation: " + key;
        }

        if (args == null || args.length == 0) {
            return template;
        }

        Object[] normalizedArgs = Arrays.stream(args)
            .map(ServerTranslations::normalizeArgument)
            .toArray();
        try {
            return String.format(Locale.ROOT, template, normalizedArgs);
        } catch (RuntimeException exception) {
            String reportKey = normalizedLanguage + ":" + key;
            if (REPORTED_FORMAT_ERRORS.add(reportKey)) {
                AttractToChat.LOGGER.error(
                    "[AttractToChat] Invalid translation format for {} in {}: {}",
                    key, normalizedLanguage, template, exception);
            }
            return template;
        }
    }

    public static String languageOf(CommandSourceStack source) {
        Entity entity = source == null ? null : source.getEntity();
        return entity instanceof ServerPlayer player ? languageOf(player) : FALLBACK_LANGUAGE;
    }

    public static String languageOf(ServerPlayer player) {
        if (player == null) {
            return FALLBACK_LANGUAGE;
        }
        return PLAYER_LANGUAGES.getOrDefault(player.getUUID(), FALLBACK_LANGUAGE);
    }

    public static void rememberPlayerLanguage(UUID playerId, String language) {
        if (playerId != null) {
            PLAYER_LANGUAGES.put(playerId, normalizeLanguage(language));
        }
    }

    public static void forgetPlayer(UUID playerId) {
        if (playerId != null) {
            PLAYER_LANGUAGES.remove(playerId);
        }
    }

    public static void clearPlayerLanguages() {
        PLAYER_LANGUAGES.clear();
    }

    private static String findTemplate(String language, String key) {
        Map<String, String> selected = TRANSLATIONS.get(language);
        if (selected != null && selected.containsKey(key)) {
            return selected.get(key);
        }
        Map<String, String> fallback = TRANSLATIONS.get(FALLBACK_LANGUAGE);
        return fallback == null ? null : fallback.get(key);
    }

    private static Object normalizeArgument(Object argument) {
        if (argument instanceof Component component) {
            return component.getString();
        }
        return argument;
    }

    private static String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return FALLBACK_LANGUAGE;
        }
        String normalized = language.toLowerCase(Locale.ROOT).replace('-', '_');
        if (SUPPORTED_LANGUAGES.contains(normalized)) {
            return normalized;
        }
        if (normalized.startsWith("pt_")) {
            return "pt_br";
        }
        if (normalized.startsWith("es_")) {
            return "es_es";
        }
        return FALLBACK_LANGUAGE;
    }

    private static Map<String, Map<String, String>> loadTranslations() {
        Map<String, Map<String, String>> languages = new HashMap<>();
        for (String language : SUPPORTED_LANGUAGES) {
            languages.put(language, loadLanguage(language));
        }
        return Map.copyOf(languages);
    }

    private static Map<String, String> loadLanguage(String language) {
        String resource = "/assets/attracttochat/lang/" + language + ".json";
        try (InputStream stream = ServerTranslations.class.getResourceAsStream(resource)) {
            if (stream == null) {
                AttractToChat.LOGGER.error("[AttractToChat] Missing language resource: {}", resource);
                return Map.of();
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                Map<String, String> translations = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    if (entry.getValue().isJsonPrimitive()) {
                        translations.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }
                return Map.copyOf(translations);
            }
        } catch (Exception exception) {
            AttractToChat.LOGGER.error(
                "[AttractToChat] Failed to load server language resource: {}", resource, exception);
            return Map.of();
        }
    }
}
