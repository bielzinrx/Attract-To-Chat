package com.bielzinrx.attracttochat;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber
public class AttractToChatConfig {
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ServerConfig SERVER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SERVER = new ServerConfig(builder);
        SERVER_SPEC = builder.build();
    }

    public static class ServerConfig {
        public final ForgeConfigSpec.DoubleValue hearingRange;
        public final ForgeConfigSpec.IntValue scanCooldownTicks;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> enabledEntities;
        public final ForgeConfigSpec.IntValue forgetTargetAfterSeconds;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            hearingRange = builder
                    .comment("Distância máxima que os mobs podem 'ouvir' o chat")
                    .defineInRange("hearingRange", 30.0, 1.0, 256.0);
            scanCooldownTicks = builder
                    .comment("Cooldown mínimo em ticks entre varreduras de mobs")
                    .defineInRange("scanCooldownTicks", 20, 1, 1200);
            enabledEntities = builder
                    .comment("Lista de IDs dos mobs que reagem ao chat")
                    .defineListAllowEmpty(Collections.singletonList("enabledEntities"),
                            () -> List.of("minecraft:zombie", "minecraft:skeleton", "minecraft:creeper"),
                            o -> o instanceof String);
            forgetTargetAfterSeconds = builder
                    .comment("Tempo em segundos para o mob esquecer o ponto da mensagem")
                    .defineInRange("forgetTargetAfterSeconds", 5, 1, 3600);
            builder.pop();
        }
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    }
}
