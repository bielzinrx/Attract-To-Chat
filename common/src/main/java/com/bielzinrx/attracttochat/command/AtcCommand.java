package com.bielzinrx.attracttochat.command;

import com.bielzinrx.attracttochat.AttractToChat;
import com.bielzinrx.attracttochat.config.AttractToChatConfig;
import com.bielzinrx.attracttochat.engine.AtcEngine;
import com.bielzinrx.attracttochat.i18n.ServerTranslations;
import com.bielzinrx.attracttochat.platform.Platform;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class AtcCommand {

    private static final Object ENTITY_SUGGESTION_CACHE_LOCK = new Object();
    private static volatile MinecraftServer entitySuggestionCacheServer;
    private static volatile int entitySuggestionCacheRegistrySize = -1;
    private static volatile List<ResourceLocation> entitySuggestionCache = List.of();

    private AtcCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("atc")
            .then(Commands.literal("debug")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    boolean now = !AtcEngine.isDebugMode();
                    AtcEngine.setDebugModeOverride(now);
                    feedback(ctx.getSource(), now ? "message.attracttochat.command.debug_on" : "message.attracttochat.command.debug_off");
                    return 1;
                })
                .then(Commands.argument("state", StringArgumentType.word())
                    .suggests((ctx, b) -> { b.suggest("on"); b.suggest("off"); return b.buildFuture(); })
                    .executes(ctx -> {
                        String s = StringArgumentType.getString(ctx, "state");
                        if (!s.equalsIgnoreCase("on") && !s.equalsIgnoreCase("off")) {
                            feedback(ctx.getSource(), "message.attracttochat.command.invalid_state", s);
                            return 0;
                        }
                        boolean v = s.equalsIgnoreCase("on");
                        if (AtcEngine.isDebugMode() == v) {
                            feedback(ctx.getSource(), v
                                ? "message.attracttochat.command.debug_already_on"
                                : "message.attracttochat.command.debug_already_off");
                            return 1;
                        }
                        AtcEngine.setDebugModeOverride(v);
                        feedback(ctx.getSource(), v ? "message.attracttochat.command.debug_on" : "message.attracttochat.command.debug_off");
                        return 1;
                    })))
            .then(Commands.literal("entity")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("add")
                    .then(Commands.argument("id", ResourceLocationArgument.id())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(
                            attractableEntityIds(ctx.getSource()), builder))
                        .executes(ctx -> {
                            String eid = ResourceLocationArgument.getId(ctx, "id").toString();
                            if (!isAttractableEntityId(ctx.getSource(), eid)) {
                                feedback(ctx.getSource(), "message.attracttochat.command.entity_not_pathfinding", eid);
                                return 0;
                            }
                            List<String> list = ensureMutable(AttractToChatConfig.COMMON.enabledEntities.get());
                            boolean exclusionMode = !list.isEmpty()
                                && list.stream().allMatch(value -> value.startsWith("!"));
                            if (list.remove("!" + eid)) {
                                AttractToChatConfig.COMMON.enabledEntities.set(list);
                                if (!saveConfig(ctx.getSource(), AtcEngine::refreshEntityRules)) return 0;
                                feedback(ctx.getSource(), "message.attracttochat.command.entity_added", eid);
                            } else if (exclusionMode || list.contains(eid)) {
                                feedback(ctx.getSource(), "message.attracttochat.command.entity_exists", eid);
                            } else {
                                list.add(eid);
                                AttractToChatConfig.COMMON.enabledEntities.set(list);
                                if (!saveConfig(ctx.getSource(), AtcEngine::refreshEntityRules)) return 0;
                                feedback(ctx.getSource(), "message.attracttochat.command.entity_added", eid);
                            }
                            return 1;
                        })))
                .then(Commands.literal("remove")
                    .then(Commands.argument("id", ResourceLocationArgument.id())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(
                            entityRemoveSuggestions(ctx.getSource()), builder))
                        .executes(ctx -> {
                            String eid = ResourceLocationArgument.getId(ctx, "id").toString();
                            List<String> list = ensureMutable(AttractToChatConfig.COMMON.enabledEntities.get());
                            boolean exclusionMode = !list.isEmpty() && list.stream().allMatch(value -> value.startsWith("!"));
                            if (exclusionMode && !list.contains("!" + eid)) {
                                if (!isAttractableEntityId(ctx.getSource(), eid)) {
                                    feedback(ctx.getSource(), "message.attracttochat.command.entity_not_pathfinding", eid);
                                    return 0;
                                }
                                list.add("!" + eid);
                                AttractToChatConfig.COMMON.enabledEntities.set(list);
                                if (!saveConfig(ctx.getSource(), AtcEngine::refreshEntityRules)) return 0;
                                feedback(ctx.getSource(), "message.attracttochat.command.entity_removed", eid);
                            } else if (!exclusionMode && list.remove(eid)) {
                                AttractToChatConfig.COMMON.enabledEntities.set(list);
                                if (!saveConfig(ctx.getSource(), AtcEngine::refreshEntityRules)) return 0;
                                feedback(ctx.getSource(), "message.attracttochat.command.entity_removed", eid);
                            } else {
                                feedback(ctx.getSource(), "message.attracttochat.command.entity_not_found", eid);
                            }
                            return 1;
                        })))
                .then(Commands.literal("list").executes(ctx -> {
                    List<String> entities = AttractToChatConfig.COMMON.enabledEntities.get();
                    boolean exclusionMode = !entities.isEmpty()
                        && entities.stream().allMatch(value -> value.startsWith("!"));
                    if (exclusionMode) {
                        long enabledCount = attractableEntityIds(ctx.getSource())
                            .filter(id -> !entities.contains("!" + id))
                            .count();
                        String excluded = String.join(", ", entities.stream()
                            .map(value -> value.startsWith("!") ? value.substring(1) : value)
                            .toList());
                        feedback(ctx.getSource(), "message.attracttochat.command.entity_list",
                            enabledCount, ServerTranslations.translate(
                                ctx.getSource(), "message.attracttochat.command.entity_all_except", excluded));
                    } else {
                        feedback(ctx.getSource(), "message.attracttochat.command.entity_list",
                            entities.size(), entities.isEmpty()
                                ? ServerTranslations.translate(ctx.getSource(), "message.attracttochat.command.entity_none")
                                : String.join(", ", entities));
                    }
                    return 1;
                })))
            .then(Commands.literal("ignore")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("add")
                    .then(Commands.argument("player_name", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ignoreAddSuggestions(ctx.getSource()), builder))
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "player_name");
                            List<String> list = ensureMutable(AttractToChatConfig.COMMON.ignoredPlayers.get());
                            if (containsIgnoreCase(list, name)) {
                                feedback(ctx.getSource(), "message.attracttochat.command.ignore_exists", name);
                            } else {
                                list.add(name);
                                AttractToChatConfig.COMMON.ignoredPlayers.set(list);
                                if (!saveConfig(ctx.getSource(), AtcEngine::refreshPlayerRules)) return 0;
                                if (name.equalsIgnoreCase("@a")) {
                                    int cleared = AtcEngine.clearSoundInvestigations();
                                    feedback(ctx.getSource(), "message.attracttochat.command.ignore_all_added", cleared);
                                } else {
                                    for (ServerPlayer p : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                                        if (p.getName().getString().equalsIgnoreCase(name)) {
                                            AtcEngine.clearInvestigationsForPlayer(p.getUUID());
                                            break;
                                        }
                                    }
                                    feedback(ctx.getSource(), "message.attracttochat.command.ignore_added", name);
                                }
                            }
                            return 1;
                        })))
                .then(Commands.literal("remove")
                    .then(Commands.argument("player_name", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(AttractToChatConfig.COMMON.ignoredPlayers.get(), builder))
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "player_name");
                            List<String> list = ensureMutable(AttractToChatConfig.COMMON.ignoredPlayers.get());
                            if (removeIgnoreCase(list, name)) {
                                AttractToChatConfig.COMMON.ignoredPlayers.set(list);
                                if (!saveConfig(ctx.getSource(), AtcEngine::refreshPlayerRules)) return 0;
                                if (name.equalsIgnoreCase("@a")) {
                                    feedback(ctx.getSource(), "message.attracttochat.command.ignore_all_removed");
                                } else {
                                    feedback(ctx.getSource(), "message.attracttochat.command.ignore_removed", name);
                                }
                            } else {
                                feedback(ctx.getSource(), "message.attracttochat.command.ignore_not_found", name);
                            }
                            return 1;
                        }))))

            .then(Commands.literal("feature")
                .requires(src -> src.hasPermission(2))
                .then(buildCapsFeatureBranch())
                .then(buildFatigueBranch())
                .then(buildAntispamBranch()))

            .then(Commands.literal("client")
                .requires(src -> src.getEntity() instanceof ServerPlayer player
                    && Platform.getHelper().hasClientMod(player))
                .then(Commands.literal("particles")
                    .then(Commands.literal("enable").executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        if (AtcEngine.isParticlesEnabled(sp.getUUID())) {
                            sp.displayClientMessage(ServerTranslations.component(
                                sp, "message.attracttochat.command.client_particles_already_enabled"), true);
                            return 1;
                        }
                        if (!AtcEngine.setParticlesEnabled(sp.getUUID(), true)) {
                            sp.displayClientMessage(ServerTranslations.component(
                                sp, "message.attracttochat.command.config_save_failed"), true);
                            return 0;
                        }
                        if (!AttractToChatConfig.COMMON.showParticles.get()) {
                            sp.displayClientMessage(ServerTranslations.component(
                                sp, "message.attracttochat.command.client_particles_enabled_server_off"), true);
                            return 1;
                        }
                        sp.displayClientMessage(ServerTranslations.component(
                            sp, "message.attracttochat.command.client_particles_enabled"), true);
                        return 1;
                    }))
                    .then(Commands.literal("disable").executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        if (!AtcEngine.isParticlesEnabled(sp.getUUID())) {
                            sp.displayClientMessage(ServerTranslations.component(
                                sp, "message.attracttochat.command.client_particles_already_disabled"), true);
                            return 1;
                        }
                        if (!AtcEngine.setParticlesEnabled(sp.getUUID(), false)) {
                            sp.displayClientMessage(ServerTranslations.component(
                                sp, "message.attracttochat.command.config_save_failed"), true);
                            return 0;
                        }
                        sp.displayClientMessage(ServerTranslations.component(
                            sp, "message.attracttochat.command.client_particles_disabled"), true);
                        return 1;
                    }))))
            .then(Commands.literal("trollmode")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("add")
                    .then(Commands.argument("player_name", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.asList(ctx.getSource().getServer().getPlayerNames()), builder))
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "player_name");
                            List<String> list = ensureMutable(AttractToChatConfig.COMMON.trollPlayers.get());
                            if (containsIgnoreCase(list, name)) {
                                feedback(ctx.getSource(), "message.attracttochat.command.troll_exists", name);
                                return 0;
                            }
                            list.add(name);
                            AttractToChatConfig.COMMON.trollPlayers.set(list);
                            if (!saveConfig(ctx.getSource(), AtcEngine::refreshPlayerRules)) return 0;
                            feedback(ctx.getSource(), "message.attracttochat.command.troll_added", name);
                            return 1;
                        })))
                .then(Commands.literal("remove")
                    .then(Commands.argument("player_name", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(AttractToChatConfig.COMMON.trollPlayers.get(), builder))
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "player_name");
                            List<String> list = ensureMutable(AttractToChatConfig.COMMON.trollPlayers.get());
                            if (!removeIgnoreCase(list, name)) {
                                feedback(ctx.getSource(), "message.attracttochat.command.troll_not_found", name);
                                return 0;
                            }
                            AttractToChatConfig.COMMON.trollPlayers.set(list);
                            if (!saveConfig(ctx.getSource(), AtcEngine::refreshPlayerRules)) return 0;
                            ServerPlayer online = ctx.getSource().getServer()
                                .getPlayerList().getPlayerByName(name);
                            if (online != null) {
                                AtcEngine.clearInvestigationsForPlayer(online.getUUID());
                            } else {
                                for (ServerPlayer p : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                                    if (p.getName().getString().equalsIgnoreCase(name)) {
                                        AtcEngine.clearInvestigationsForPlayer(p.getUUID());
                                        break;
                                    }
                                }
                            }
                            feedback(ctx.getSource(), "message.attracttochat.command.troll_removed", name);
                            return 1;
                        })))
                .then(Commands.literal("list").executes(ctx -> showTrollStatus(ctx.getSource()))))
            .then(Commands.literal("preset")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("set")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(AttractToChatConfig.getPresetNames(), builder))
                        .executes(ctx -> applyPreset(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("custom")
                    .then(Commands.literal("save")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(ctx -> saveCustomPreset(ctx.getSource(),
                                StringArgumentType.getString(ctx, "name"), false))))
                    .then(Commands.literal("update")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(AttractToChatConfig.getCustomPresetNames(), builder))
                            .executes(ctx -> saveCustomPreset(ctx.getSource(),
                                StringArgumentType.getString(ctx, "name"), true))))
                    .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(AttractToChatConfig.getCustomPresetNames(), builder))
                            .executes(ctx -> deleteCustomPreset(ctx.getSource(),
                                StringArgumentType.getString(ctx, "name")))))
                    .then(Commands.literal("rename")
                        .then(Commands.argument("old", StringArgumentType.word())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(AttractToChatConfig.getCustomPresetNames(), builder))
                            .then(Commands.argument("new", StringArgumentType.word())
                                .executes(ctx -> renameCustomPreset(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "old"),
                                    StringArgumentType.getString(ctx, "new"))))))
                    .then(Commands.literal("list").executes(ctx -> listCustomPresets(ctx.getSource()))))
                .then(Commands.literal("undo").executes(ctx -> undoPreset(ctx.getSource())))
                .then(Commands.literal("reset").executes(ctx -> resetPresetFields(ctx.getSource())))
                .then(Commands.literal("status").executes(ctx -> showPresetStatus(ctx.getSource()))))
            .then(Commands.literal("config")
                .requires(src -> src.hasPermission(2))
                .then(buildHearingRangeBranch())
                .then(buildCapsRangeBonusBranch())
                .then(Commands.literal("mobspeed")
                    .then(Commands.literal("base")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.5, 2.0))
                            .executes(ctx -> {
                                double v = DoubleArgumentType.getDouble(ctx, "value");
                                AttractToChatConfig.COMMON.mobSpeedBase.set(v);
                                if (!saveConfig(ctx.getSource(), null)) return 0;
                                feedback(ctx.getSource(), "message.attracttochat.command.mobspeed_base_set", v);
                                return 1;
                            })))
                    .then(Commands.literal("max")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.5, 3.0))
                            .executes(ctx -> {
                                double v = DoubleArgumentType.getDouble(ctx, "value");
                                AttractToChatConfig.COMMON.mobSpeedMax.set(v);
                                if (!saveConfig(ctx.getSource(), null)) return 0;
                                feedback(ctx.getSource(), "message.attracttochat.command.mobspeed_max_set", v);
                                return 1;
                            }))))
                .then(Commands.literal("forgettime")
                    .then(Commands.argument("seconds", IntegerArgumentType.integer(1, 300))
                        .executes(ctx -> {
                            int v = IntegerArgumentType.getInteger(ctx, "seconds");
                            AttractToChatConfig.COMMON.forgetTargetAfterSeconds.set(v);
                            if (!saveConfig(ctx.getSource(), AtcEngine::refreshGoalTiming)) return 0;
                            feedback(ctx.getSource(), "message.attracttochat.command.forgettime_set", v);
                            return 1;
                        })))

                .then(Commands.literal("fatigue")
                    .then(Commands.literal("threshold")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 100000))
                            .executes(ctx -> {
                                int value = IntegerArgumentType.getInteger(ctx, "value");
                                AttractToChatConfig.COMMON.traumaThreshold.set(value);
                                if (!saveConfig(ctx.getSource(), null)) return 0;
                                feedback(ctx.getSource(), "message.attracttochat.command.fatigue_threshold_set", value);
                                return 1;
                            })))
                    .then(Commands.literal("muteduration")
                        .then(Commands.argument("seconds", IntegerArgumentType.integer(1, 3600))
                            .executes(ctx -> {
                                int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
                                AttractToChatConfig.COMMON.muteDurationTicks.set(seconds * 20L);
                                if (!saveConfig(ctx.getSource(), null)) return 0;
                                feedback(ctx.getSource(), "message.attracttochat.command.fatigue_mute_set", seconds);
                                return 1;
                            })))))
            .then(Commands.literal("status").executes(ctx -> {
                CommandSourceStack src = ctx.getSource();
                src.sendSuccess(ServerTranslations.component(src, "message.attracttochat.command.status_header"), false);
                src.sendSuccess(ServerTranslations.component(src, "message.attracttochat.command.status_hearing", AttractToChatConfig.COMMON.hearingRange.get()), false);
                src.sendSuccess(ServerTranslations.component(src, "message.attracttochat.command.status_capsfeature", AttractToChatConfig.COMMON.enableCapsFeature.get()), false);
                src.sendSuccess(ServerTranslations.component(src, "message.attracttochat.command.status_capsbonus", AttractToChatConfig.COMMON.capsRangeBonus.get()), false);
                src.sendSuccess(ServerTranslations.component(src, "message.attracttochat.command.status_fatigue", AttractToChatConfig.COMMON.enableVocalFatigue.get()), false);
                src.sendSuccess(ServerTranslations.component(src, "message.attracttochat.command.status_antispam", AttractToChatConfig.COMMON.enableAntiSpam.get()), false);
                src.sendSuccess(ServerTranslations.component(src, "message.attracttochat.command.status_antispam_detail",
                    AttractToChatConfig.COMMON.scanCooldownTicks.get() / 20.0,
                    AttractToChatConfig.COMMON.antiSpamMaxMessages.get(),
                    AttractToChatConfig.COMMON.antiSpamWindowSeconds.get()), false);
                src.sendSuccess(ServerTranslations.component(src, "message.attracttochat.command.status_mobspeed_base", AttractToChatConfig.COMMON.mobSpeedBase.get()), false);
                src.sendSuccess(ServerTranslations.component(src, "message.attracttochat.command.status_mobspeed_max", AttractToChatConfig.COMMON.mobSpeedMax.get()), false);
                src.sendSuccess(ServerTranslations.component(src, "message.attracttochat.command.status_particles", AttractToChatConfig.COMMON.showParticles.get()), false);
                src.sendSuccess(ServerTranslations.component(src, "message.attracttochat.command.status_footer"), false);
                return 1;
            }))
            .then(Commands.literal("help")
                .executes(ctx -> sendHelp(ctx.getSource(), "overview"))
                .then(Commands.argument("category", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        List<String> categories = new ArrayList<>(Arrays.asList(
                            "overview", "gameplay", "mobs", "admin", "feature"));
                        if (ctx.getSource().getEntity() instanceof ServerPlayer player
                                && Platform.getHelper().hasClientMod(player)) {
                            categories.add("client");
                        }
                        return SharedSuggestionProvider.suggest(categories, builder);
                    })
                    .executes(ctx -> sendHelp(ctx.getSource(),
                        StringArgumentType.getString(ctx, "category")))))
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildCapsFeatureBranch() {
        return Commands.literal("caps")
            .executes(ctx -> {
                feedback(ctx.getSource(), "message.attracttochat.command.caps_status",
                    AttractToChatConfig.COMMON.enableCapsFeature.get());
                return 1;
            })
            .then(Commands.literal("enable").executes(ctx -> {
                if (AttractToChatConfig.COMMON.enableCapsFeature.get()) {
                    feedback(ctx.getSource(), "message.attracttochat.command.caps_already_enabled");
                    return 1;
                }
                AttractToChatConfig.COMMON.enableCapsFeature.set(true);
                int cleared = saveAndApplyRuntime(ctx.getSource(), true, false);
                if (cleared < 0) return 0;
                feedback(ctx.getSource(), "message.attracttochat.command.caps_enabled", cleared);
                return 1;
            }))
            .then(Commands.literal("disable").executes(ctx -> {
                if (!AttractToChatConfig.COMMON.enableCapsFeature.get()) {
                    feedback(ctx.getSource(), "message.attracttochat.command.caps_already_disabled");
                    return 1;
                }
                AttractToChatConfig.COMMON.enableCapsFeature.set(false);
                int cleared = saveAndApplyRuntime(ctx.getSource(), true, false);
                if (cleared < 0) return 0;
                feedback(ctx.getSource(), "message.attracttochat.command.caps_disabled", cleared);
                return 1;
            }))
            .then(Commands.literal("status").executes(ctx -> {
                feedback(ctx.getSource(), "message.attracttochat.command.caps_status",
                    AttractToChatConfig.COMMON.enableCapsFeature.get());
                return 1;
            }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildFatigueBranch() {
        return Commands.literal("fatigue")
            .then(Commands.literal("enable").executes(ctx -> {
                if (AttractToChatConfig.COMMON.enableVocalFatigue.get()) {
                    feedback(ctx.getSource(), "message.attracttochat.command.fatigue_already_enabled");
                    return 1;
                }
                AttractToChatConfig.COMMON.enableVocalFatigue.set(true);
                if (saveAndApplyRuntime(ctx.getSource(), false, false) < 0) return 0;
                feedback(ctx.getSource(), "message.attracttochat.command.fatigue_enabled");
                return 1;
            }))
            .then(Commands.literal("disable").executes(ctx -> {
                if (!AttractToChatConfig.COMMON.enableVocalFatigue.get()) {
                    feedback(ctx.getSource(), "message.attracttochat.command.fatigue_already_disabled");
                    return 1;
                }
                AttractToChatConfig.COMMON.enableVocalFatigue.set(false);
                if (saveAndApplyRuntime(ctx.getSource(), false, false) < 0) return 0;
                AtcEngine.clearVocalFatigueState();
                feedback(ctx.getSource(), "message.attracttochat.command.fatigue_disabled");
                return 1;
            }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildAntispamBranch() {
        return Commands.literal("antispam")
            .then(Commands.literal("enable").executes(ctx -> {
                if (AttractToChatConfig.COMMON.enableAntiSpam.get()) {
                    feedback(ctx.getSource(), "message.attracttochat.command.antispam_already_enabled");
                    return 1;
                }
                AttractToChatConfig.COMMON.enableAntiSpam.set(true);
                if (saveAndApplyRuntime(ctx.getSource(), false, true) < 0) return 0;
                feedback(ctx.getSource(), "message.attracttochat.command.antispam_enabled_detailed",
                    AttractToChatConfig.COMMON.scanCooldownTicks.get(),
                    AttractToChatConfig.COMMON.scanCooldownTicks.get() / 20.0);
                return 1;
            }))
            .then(Commands.literal("disable").executes(ctx -> {
                if (!AttractToChatConfig.COMMON.enableAntiSpam.get()) {
                    feedback(ctx.getSource(), "message.attracttochat.command.antispam_already_disabled");
                    return 1;
                }
                AttractToChatConfig.COMMON.enableAntiSpam.set(false);
                if (saveAndApplyRuntime(ctx.getSource(), false, true) < 0) return 0;
                feedback(ctx.getSource(), "message.attracttochat.command.antispam_disabled");
                return 1;
            }))
            .then(Commands.literal("status").executes(ctx -> {
                feedback(ctx.getSource(), "message.attracttochat.command.antispam_status_full",
                    AttractToChatConfig.COMMON.enableAntiSpam.get(),
                    AttractToChatConfig.COMMON.scanCooldownTicks.get(),
                    AttractToChatConfig.COMMON.scanCooldownTicks.get() / 20.0,
                    AttractToChatConfig.COMMON.antiSpamMaxMessages.get(),
                    AttractToChatConfig.COMMON.antiSpamWindowSeconds.get());
                return 1;
            }))
            .then(Commands.literal("cooldown")
                .then(Commands.argument("seconds", IntegerArgumentType.integer(0, 60))
                    .executes(ctx -> {
                        int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
                        int ticks = Math.max(1, seconds * 20);
                        AttractToChatConfig.COMMON.scanCooldownTicks.set(ticks);
                        if (saveAndApplyRuntime(ctx.getSource(), false, true) < 0) return 0;
                        feedback(ctx.getSource(), "message.attracttochat.command.antispam_cooldown_set",
                            seconds, ticks);
                        return 1;
                    })))
            .then(Commands.literal("window")
                .then(Commands.argument("max_messages", IntegerArgumentType.integer(0, 50))
                    .then(Commands.argument("seconds", IntegerArgumentType.integer(1, 120))
                        .executes(ctx -> {
                            int max = IntegerArgumentType.getInteger(ctx, "max_messages");
                            int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
                            AttractToChatConfig.COMMON.antiSpamMaxMessages.set(max);
                            AttractToChatConfig.COMMON.antiSpamWindowSeconds.set(seconds);
                            if (saveAndApplyRuntime(ctx.getSource(), false, true) < 0) return 0;
                            feedback(ctx.getSource(), "message.attracttochat.command.antispam_window_set",
                                max, seconds);
                            return 1;
                        }))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildHearingRangeBranch() {
        return Commands.literal("hearingrange")
            .executes(ctx -> {
                feedback(ctx.getSource(), "message.attracttochat.command.radius_status",
                        AttractToChatConfig.COMMON.hearingRange.get(),
                        AttractToChatConfig.COMMON.capsRangeBonus.get());
                return 1;
            })
            .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 500.0))
                .executes(ctx -> {
                    double v = DoubleArgumentType.getDouble(ctx, "value");
                    AttractToChatConfig.COMMON.hearingRange.set(v);
                    int cleared = saveAndApplyRuntime(ctx.getSource(), true, false);
                    if (cleared < 0) return 0;
                    feedback(ctx.getSource(), "message.attracttochat.command.radius_set_live", v, cleared);
                    return 1;
                }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildCapsRangeBonusBranch() {
        return Commands.literal("capsrangebonus")
            .executes(ctx -> {
                feedback(ctx.getSource(), "message.attracttochat.command.radius_status",
                        AttractToChatConfig.COMMON.hearingRange.get(),
                        AttractToChatConfig.COMMON.capsRangeBonus.get());
                return 1;
            })
            .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 100.0))
                .executes(ctx -> {
                    double v = DoubleArgumentType.getDouble(ctx, "value");
                    AttractToChatConfig.COMMON.capsRangeBonus.set(v);
                    int cleared = saveAndApplyRuntime(ctx.getSource(), true, false);
                    if (cleared < 0) return 0;
                    feedback(ctx.getSource(), "message.attracttochat.command.capsbonus_set_live", v, cleared);
                    return 1;
                }));
    }

    private static int showTrollStatus(CommandSourceStack src) {
        List<String> players = AttractToChatConfig.COMMON.trollPlayers.get();
        if (players.isEmpty()) {
            feedback(src, "message.attracttochat.command.troll_list_empty");
        } else {
            feedback(src, "message.attracttochat.command.troll_list", String.join(", ", players));
            feedback(src, "message.attracttochat.command.troll_effects",
                AttractToChatConfig.COMMON.trollSpeedMultiplier.get());
        }
        return 1;
    }

    private static Stream<ResourceLocation> attractableEntityIds(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        int registrySize = Registry.ENTITY_TYPE.size();
        List<ResourceLocation> cached = entitySuggestionCache;

        if (entitySuggestionCacheServer != server
            || entitySuggestionCacheRegistrySize != registrySize) {
            synchronized (ENTITY_SUGGESTION_CACHE_LOCK) {
                if (entitySuggestionCacheServer != server
                    || entitySuggestionCacheRegistrySize != registrySize) {
                    entitySuggestionCache = Registry.ENTITY_TYPE.keySet().stream()
                        .filter(id -> isAttractableEntityId(source, id))
                        .sorted(Comparator.comparing(ResourceLocation::toString))
                        .toList();
                    entitySuggestionCacheServer = server;
                    entitySuggestionCacheRegistrySize = registrySize;
                }
                cached = entitySuggestionCache;
            }
        }
        return cached.stream();
    }

    private static Stream<ResourceLocation> entityRemoveSuggestions(CommandSourceStack source) {
        List<String> list = AttractToChatConfig.COMMON.enabledEntities.get();
        boolean exclusionMode = list != null && !list.isEmpty()
            && list.stream().allMatch(value -> value.startsWith("!"));
        if (!exclusionMode) {

            return list.stream()
                .filter(value -> value != null && !value.startsWith("!"))
                .map(ResourceLocation::tryParse)
                .filter(id -> id != null);
        }

        return attractableEntityIds(source);
    }

    private static boolean isAttractableEntityId(CommandSourceStack source, String rawId) {
        if (rawId == null || rawId.isBlank()) return false;
        ResourceLocation id = ResourceLocation.tryParse(rawId.toLowerCase(Locale.ROOT));
        return isAttractableEntityId(source, id);
    }

    private static boolean isAttractableEntityId(CommandSourceStack source, ResourceLocation id) {
        if (!AttractToChatConfig.isConfigurableEntityId(id)) return false;
        EntityType<?> type = Registry.ENTITY_TYPE.get(id);
        if (type == null) return false;

        Entity probe = null;
        try {
            probe = type.create(source.getLevel());
            if (probe != null) return probe instanceof Mob;
        } catch (Throwable error) {

            AttractToChat.LOGGER.debug("Could not probe entity type {} for ATC suggestions", id, error);
        } finally {
            if (probe != null) probe.discard();
        }
        return type.getCategory() != MobCategory.MISC;
    }

    private static int applyPreset(CommandSourceStack src, String name) {
        String preset = name == null ? "" : name.toLowerCase(java.util.Locale.ROOT);
        if (!AttractToChatConfig.applyPresetValues(preset)) {
            feedback(src, "message.attracttochat.command.preset_unknown", name);
            return 0;
        }

        AtcEngine.clearVocalFatigueState();
        int cleared = saveAndApplyRuntime(src, true, true);
        if (cleared < 0) return 0;
        feedback(src, "message.attracttochat.command.preset_set_live", preset, cleared);
        return 1;
    }

    private static int saveCustomPreset(CommandSourceStack src, String name, boolean update) {
        String normalized = name == null ? "" : name.toLowerCase(java.util.Locale.ROOT);
        if (!AttractToChatConfig.isValidCustomPresetName(normalized)) {
            feedback(src, "message.attracttochat.command.preset_custom_invalid", name);
            return 0;
        }
        boolean exists = AttractToChatConfig.hasCustomPreset(normalized);
        if (!update && exists) {
            feedback(src, "message.attracttochat.command.preset_custom_exists", normalized, normalized);
            return 0;
        }
        if (update && !exists) {
            feedback(src, "message.attracttochat.command.preset_custom_missing", normalized);
            return 0;
        }
        if (!AttractToChatConfig.saveCustomPreset(normalized, update)) {
            feedback(src, "message.attracttochat.command.preset_custom_limit");
            return 0;
        }
        if (!saveConfig(src, null)) return 0;
        feedback(src, update
            ? "message.attracttochat.command.preset_custom_updated"
            : "message.attracttochat.command.preset_custom_saved", normalized);
        return 1;
    }

    private static int deleteCustomPreset(CommandSourceStack src, String name) {
        String normalized = name == null ? "" : name.toLowerCase(java.util.Locale.ROOT);
        if (!AttractToChatConfig.deleteCustomPreset(normalized)) {
            feedback(src, "message.attracttochat.command.preset_custom_missing", normalized);
            return 0;
        }
        if (!saveConfig(src, null)) return 0;
        feedback(src, "message.attracttochat.command.preset_custom_deleted", normalized);
        return 1;
    }

    private static int renameCustomPreset(CommandSourceStack src, String oldName, String newName) {
        String oldNormalized = oldName == null ? "" : oldName.toLowerCase(java.util.Locale.ROOT);
        String newNormalized = newName == null ? "" : newName.toLowerCase(java.util.Locale.ROOT);
        if (!AttractToChatConfig.hasCustomPreset(oldNormalized)) {
            feedback(src, "message.attracttochat.command.preset_custom_missing", oldNormalized);
            return 0;
        }
        if (!AttractToChatConfig.isValidCustomPresetName(newNormalized)) {
            feedback(src, "message.attracttochat.command.preset_custom_invalid", newName);
            return 0;
        }
        if (AttractToChatConfig.hasCustomPreset(newNormalized)) {
            feedback(src, "message.attracttochat.command.preset_custom_exists", newNormalized, newNormalized);
            return 0;
        }
        if (!AttractToChatConfig.renameCustomPreset(oldNormalized, newNormalized)) return 0;
        if (!saveConfig(src, null)) return 0;
        feedback(src, "message.attracttochat.command.preset_custom_renamed", oldNormalized, newNormalized);
        return 1;
    }

    private static int listCustomPresets(CommandSourceStack src) {
        List<String> names = AttractToChatConfig.getCustomPresetNames();
        if (names.isEmpty()) {
            feedback(src, "message.attracttochat.command.preset_custom_list_empty");
        } else {
            feedback(src, "message.attracttochat.command.preset_custom_list", String.join(", ", names));
        }
        return 1;
    }

    private static int undoPreset(CommandSourceStack src) {
        AttractToChatConfig.PresetUndoResult result = AttractToChatConfig.undoLastPresetChanges();
        if (!result.isAvailable()) {
            feedback(src, "message.attracttochat.command.preset_undo_none");
            return 0;
        }
        AtcEngine.clearVocalFatigueState();
        int cleared = saveAndApplyRuntime(src, true, true);
        if (cleared < 0) return 0;
        feedback(src, "message.attracttochat.command.preset_undo_live",
            result.getPresetName(), result.getRestoredFields(), result.getPreservedManualFields(), cleared);
        return 1;
    }

    private static int resetPresetFields(CommandSourceStack src) {
        int changed = AttractToChatConfig.resetPresetManagedFieldsToDefaults();
        AtcEngine.clearVocalFatigueState();
        int cleared = saveAndApplyRuntime(src, true, true);
        if (cleared < 0) return 0;
        feedback(src, "message.attracttochat.command.preset_reset_live", changed, cleared);
        return 1;
    }

    private static int showPresetStatus(CommandSourceStack src) {
        String preset = AttractToChatConfig.getUndoablePresetName();
        if (preset == null) {
            feedback(src, "message.attracttochat.command.preset_status_none");
        } else {
            feedback(src, "message.attracttochat.command.preset_status_available", preset);
        }
        return 1;
    }

    private static List<String> ignoreAddSuggestions(CommandSourceStack source) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("@a");
        suggestions.addAll(Arrays.asList(source.getServer().getPlayerNames()));
        return suggestions;
    }

    private static List<String> ensureMutable(List<String> list) {
        return (list instanceof ArrayList) ? list : new ArrayList<>(list);
    }

    private static boolean containsIgnoreCase(List<String> list, String value) {
        return list.stream().anyMatch(entry -> entry.equalsIgnoreCase(value));
    }

    private static boolean removeIgnoreCase(List<String> list, String value) {
        return list.removeIf(entry -> entry.equalsIgnoreCase(value));
    }

    private static int saveAndApplyRuntime(CommandSourceStack src,
            boolean clearSoundTargets, boolean clearAntiSpamTimers) {
        if (!saveConfig(src, null)) return -1;
        int cleared = 0;
        if (clearSoundTargets) {
            cleared = AtcEngine.clearSoundInvestigations();
        }
        if (clearAntiSpamTimers) {
            AtcEngine.clearAntiSpamState();
        }
        return cleared;
    }

    private static boolean saveConfig(CommandSourceStack src, Runnable runtimeUpdate) {
        if (!AttractToChatConfig.save()) {
            feedback(src, "message.attracttochat.command.config_save_failed");
            return false;
        }
        if (runtimeUpdate != null) runtimeUpdate.run();
        return true;
    }

    private static int sendHelp(CommandSourceStack src, String category) {
        if (src.getEntity() instanceof ServerPlayer player) {
            AttractToChat.getInstance().sendHelp(player, category);
            return 1;
        }
        feedback(src, "message.attracttochat.command.help_players_only");
        return 0;
    }

    private static void feedback(CommandSourceStack src, String key, Object... args) {
        src.sendSuccess(ServerTranslations.component(src, "message.attracttochat.prefix")
            .copy()
            .append(ServerTranslations.component(src, key, args)), true);
    }
}
