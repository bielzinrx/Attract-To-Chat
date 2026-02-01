package com.bielzinrx.attracttochat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração do AttractToChat
 * 
 * Tipo COMMON:
 * - Recarrega automaticamente quando o arquivo é salvo
 * - Permite editar no cliente em singleplayer
 * - Salvo em: config/attracttochat-common.toml
 * 
 * @version 2.6.0
 */
public class AttractToChatConfig {
    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec SPEC;

    static {
        final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class CommonConfig {
        
        // ==================== CONFIGURAÇÕES GERAIS ====================
        public final ForgeConfigSpec.DoubleValue hearingRange;
        public final ForgeConfigSpec.DoubleValue capsRangeBonus;
        public final ForgeConfigSpec.IntValue scanCooldownTicks;
        public final ForgeConfigSpec.IntValue forgetTargetAfterSeconds;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> enabledEntities;
        public final ForgeConfigSpec.BooleanValue debugMode;

        // ==================== CONFIGURAÇÕES VISUAIS ====================
        public final ForgeConfigSpec.BooleanValue showAttractionParticles;
        public final ForgeConfigSpec.BooleanValue showAttractionFeedback;

        // ==================== CONFIGURAÇÕES DE FADIGA ====================
        public final ForgeConfigSpec.DoubleValue fatigueChanceMultiplier;
        public final ForgeConfigSpec.IntValue fatigueDurationBase;
        public final ForgeConfigSpec.IntValue fatiguePenalty;
        
        // ==================== CONFIGURAÇÕES DE CURA ====================
        public final ForgeConfigSpec.IntValue honeyRelief;
        public final ForgeConfigSpec.IntValue waterRelief;
        public final ForgeConfigSpec.IntValue stewRelief;
        public final ForgeConfigSpec.IntValue poisonWorsen;

        CommonConfig(ForgeConfigSpec.Builder builder) {
            
            // ========== SEÇÃO: GENERAL ==========
            builder.comment(
                "═══════════════════════════════════════════════════════════════",
                "                    ATTRACT TO CHAT CONFIG",
                "═══════════════════════════════════════════════════════════════",
                "",
                "Configurações gerais do mod.",
                "General mod settings."
            ).push("general");

            hearingRange = builder
                    .comment(
                        "",
                        "Raio base (em blocos) que os mobs podem 'ouvir' mensagens.",
                        "Base range (in blocks) that mobs can 'hear' messages.",
                        "Default: 30.0"
                    )
                    .defineInRange("hearingRange", 30.0, 0.0, 500.0);

            capsRangeBonus = builder
                    .comment(
                        "",
                        "Blocos extras de alcance por letra MAIÚSCULA na mensagem.",
                        "Extra range (in blocks) per UPPERCASE letter in the message.",
                        "Default: 5.0"
                    )
                    .defineInRange("capsRangeBonus", 5.0, 0.0, 100.0);

            scanCooldownTicks = builder
                    .comment(
                        "",
                        "Cooldown (em ticks) entre mensagens para atrair mobs.",
                        "Cooldown (in ticks) between messages to attract mobs.",
                        "20 ticks = 1 segundo | 20 ticks = 1 second",
                        "Default: 20"
                    )
                    .defineInRange("scanCooldownTicks", 20, 1, 1200);

            forgetTargetAfterSeconds = builder
                    .comment(
                        "",
                        "Tempo base (em segundos) que o mob persegue o alvo.",
                        "Base time (in seconds) that the mob pursues the target.",
                        "CAPS LOCK aumenta este tempo | CAPS LOCK increases this time",
                        "Default: 5"
                    )
                    .defineInRange("forgetTargetAfterSeconds", 5, 1, 300);

            enabledEntities = builder
                    .comment(
                        "",
                        "Lista de entidades que podem ser atraídas pelo chat.",
                        "List of entities that can be attracted by chat.",
                        "Formato: 'modid:entity_name' | Format: 'modid:entity_name'",
                        "Use /attractreload para aplicar mudanças sem reiniciar.",
                        "Use /attractreload to apply changes without restarting."
                    )
                    .defineList("enabledEntities",
                            Arrays.asList(
                                "minecraft:zombie", 
                                "minecraft:skeleton", 
                                "minecraft:creeper",
                                "minecraft:spider",
                                "minecraft:enderman",
                                "minecraft:husk",
                                "minecraft:drowned",
                                "minecraft:stray",
                                "minecraft:phantom"
                            ),
                            obj -> obj instanceof String);

            debugMode = builder
                    .comment(
                        "",
                        "Modo debug persistente (salvo na config).",
                        "Persistent debug mode (saved in config).",
                        "Use /attractdebug para alternar em runtime.",
                        "Use /attractdebug to toggle at runtime."
                    )
                    .define("debugMode", false);

            builder.pop();

            // ========== SEÇÃO: VISUAL ==========
            builder.comment(
                "",
                "═══════════════════════════════════════════════════════════════",
                "                     CONFIGURAÇÕES VISUAIS",
                "                       VISUAL SETTINGS",
                "═══════════════════════════════════════════════════════════════"
            ).push("visual");

            showAttractionParticles = builder
                    .comment(
                        "",
                        "Mostrar partículas quando mobs são atraídos.",
                        "Show particles when mobs are attracted.",
                        "Default: true"
                    )
                    .define("showAttractionParticles", true);

            showAttractionFeedback = builder
                    .comment(
                        "",
                        "Mostrar mensagem na action bar quando mobs são atraídos.",
                        "Show action bar message when mobs are attracted.",
                        "Default: true"
                    )
                    .define("showAttractionFeedback", true);

            builder.pop();

            // ========== SEÇÃO: VOCAL FATIGUE ==========
            builder.comment(
                "",
                "═══════════════════════════════════════════════════════════════",
                "                    SISTEMA DE FADIGA VOCAL",
                "                    VOCAL FATIGUE SYSTEM",
                "═══════════════════════════════════════════════════════════════",
                "",
                "Gritar demais (CAPS LOCK) pode causar fadiga vocal!",
                "Shouting too much (CAPS LOCK) can cause vocal fatigue!"
            ).push("vocal_fatigue");

            fatigueChanceMultiplier = builder
                    .comment(
                        "",
                        "Multiplicador de chance de fadiga por letra maiúscula.",
                        "Fatigue chance multiplier per uppercase letter.",
                        "Fórmula: chance = (uppercase_count * multiplier) / 100",
                        "Formula: chance = (uppercase_count * multiplier) / 100",
                        "Default: 1.5"
                    )
                    .defineInRange("fatigueChanceMultiplier", 1.5, 0.0, 100.0);

            fatigueDurationBase = builder
                    .comment(
                        "",
                        "Duração base da fadiga vocal em segundos.",
                        "Base duration of vocal fatigue in seconds.",
                        "Default: 30"
                    )
                    .defineInRange("fatigueDurationBase", 30, 1, 600);

            fatiguePenalty = builder
                    .comment(
                        "",
                        "Penalidade (em segundos) por tentar falar com fadiga.",
                        "Penalty (in seconds) for trying to speak with fatigue.",
                        "Default: 10"
                    )
                    .defineInRange("fatiguePenalty", 10, 1, 600);

            builder.pop();

            // ========== SEÇÃO: HEALING ==========
            builder.comment(
                "",
                "═══════════════════════════════════════════════════════════════",
                "                    CURA DA FADIGA VOCAL",
                "                    VOCAL FATIGUE HEALING",
                "═══════════════════════════════════════════════════════════════",
                "",
                "Diferentes líquidos curam diferentes quantidades de fadiga.",
                "Different liquids heal different amounts of fatigue.",
                "Valores em segundos | Values in seconds"
            ).push("healing");

            honeyRelief = builder
                    .comment(
                        "",
                        "Segundos de alívio ao beber mel.",
                        "Seconds of relief when drinking honey.",
                        "Default: 60"
                    )
                    .defineInRange("honeyRelief", 60, 1, 600);

            waterRelief = builder
                    .comment(
                        "",
                        "Segundos de alívio ao beber água.",
                        "Seconds of relief when drinking water.",
                        "Default: 10"
                    )
                    .defineInRange("waterRelief", 10, 1, 600);

            stewRelief = builder
                    .comment(
                        "",
                        "Segundos de alívio ao comer sopas.",
                        "Seconds of relief when eating stews.",
                        "Default: 15"
                    )
                    .defineInRange("stewRelief", 15, 1, 600);

            poisonWorsen = builder
                    .comment(
                        "",
                        "Segundos de PENALIDADE ao beber poções ruins.",
                        "Seconds of PENALTY when drinking bad potions.",
                        "(Veneno, Dano, etc.)",
                        "(Poison, Harming, etc.)",
                        "Default: 60"
                    )
                    .defineInRange("poisonWorsen", 60, 1, 600);

            builder.pop();
        }
    }
}
