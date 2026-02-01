package com.bielzinrx.attracttochat;

import com.bielzinrx.attracttochat.config.AttractToChatConfig;
import com.bielzinrx.attracttochat.effect.VocalFatigueEffect;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Attract to Chat - Mobs react to player chat messages
 * 
 * @author bielzinrx
 * @version 2.0.0
 */
@Mod(AttractToChatMod.MOD_ID)
public class AttractToChatMod {
    public static final String MOD_ID = "attracttochat";
    private final Random random = new Random();
    
    // Armazena dados dos mobs com Goals
    private static final Map<UUID, MobGoalData> MOB_GOAL_DATA = new ConcurrentHashMap<>();
    
    // Estatísticas de sessão
    private static final Map<UUID, PlayerStats> PLAYER_STATS = new ConcurrentHashMap<>();
    
    // Cooldown por jogador (para scanCooldownTicks)
    private static final Map<UUID, Long> PLAYER_COOLDOWNS = new ConcurrentHashMap<>();
    
    // Debug mode runtime
    private static boolean debugModeRuntime = false;
    
    // Contador de ticks para limpeza periódica
    private int cleanupTickCounter = 0;
    private static final int CLEANUP_INTERVAL = 200; // A cada 10 segundos

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MOD_ID);
    public static final RegistryObject<MobEffect> VOCAL_FATIGUE = EFFECTS.register("vocal_fatigue",
            () -> new VocalFatigueEffect(MobEffectCategory.HARMFUL, 0x8B0000));

    public AttractToChatMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AttractToChatConfig.SPEC);
        EFFECTS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    // ==================== ESTRUTURAS DE DADOS ====================
    
    private static class MobGoalData {
        final Mob mob;
        final MoveToSoundGoal goal;
        final WrappedGoal wrappedGoal;

        MobGoalData(Mob mob, MoveToSoundGoal goal, WrappedGoal wrappedGoal) {
            this.mob = mob;
            this.goal = goal;
            this.wrappedGoal = wrappedGoal;
        }
    }
    
    private static class PlayerStats {
        int totalMessages = 0;
        int totalMobsAttracted = 0;
        int totalFatigueApplied = 0;
        int totalUppercaseLetters = 0;
        long lastMessageTime = 0;
        
        void recordMessage(int mobsAttracted, int uppercase) {
            totalMessages++;
            totalMobsAttracted += mobsAttracted;
            totalUppercaseLetters += uppercase;
            lastMessageTime = System.currentTimeMillis();
        }
        
        void recordFatigue() {
            totalFatigueApplied++;
        }
    }

    // ==================== EVENTOS DE TICK ====================
    
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        cleanupTickCounter++;
        if (cleanupTickCounter >= CLEANUP_INTERVAL) {
            cleanupTickCounter = 0;
            cleanupDeadMobs();
        }
    }

    // ==================== REGISTRO DE COMANDOS ====================
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        
        // Comando: /attractdebug [on|off]
        dispatcher.register(
            Commands.literal("attractdebug")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    debugModeRuntime = !debugModeRuntime;
                    sendFormattedMessage(context.getSource(), 
                        debugModeRuntime ? Component.translatable("message.attracttochat.debug_on") : Component.translatable("message.attracttochat.debug_off"), true);
                    return 1;
                })
                .then(Commands.argument("mode", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        builder.suggest("on");
                        builder.suggest("off");
                        return builder.buildFuture();
                    })
                    .executes(context -> {
                        String mode = StringArgumentType.getString(context, "mode");
                        debugModeRuntime = mode.equalsIgnoreCase("on");
                        sendFormattedMessage(context.getSource(), 
                            debugModeRuntime ? Component.translatable("message.attracttochat.debug_on") : Component.translatable("message.attracttochat.debug_off"), true);
                        return 1;
                    })
                )
        );
        
        // Comando: /attractlist
        dispatcher.register(
            Commands.literal("attractlist")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    List<? extends String> enabled = AttractToChatConfig.COMMON.enabledEntities.get();
                    sendFormattedMessage(context.getSource(), 
                        Component.translatable("message.attracttochat.entity_list").append(String.valueOf(enabled.size())), false);
                    
                    for (String entityId : enabled) {
                        context.getSource().sendSuccess(
                            () -> Component.literal("  §8• §7" + entityId), false);
                    }
                    
                    cleanupDeadMobs();
                    int activeGoals = MOB_GOAL_DATA.size();
                    sendFormattedMessage(context.getSource(), 
                        Component.translatable("message.attracttochat.active_goals").append(String.valueOf(activeGoals)), false);
                    return 1;
                })
        );
        
        // Comando: /attractreload
        dispatcher.register(
            Commands.literal("attractreload")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    int[] counts = reloadAllMobGoals(context.getSource().getServer());
                    sendFormattedMessage(context.getSource(), Component.translatable("message.attracttochat.goals_reloaded"), true);
                    context.getSource().sendSuccess(
                        () -> Component.translatable("message.attracttochat.removed").append(String.valueOf(counts[0]))
                            .append(Component.literal(" §8| "))
                            .append(Component.translatable("message.attracttochat.added").append(String.valueOf(counts[1]))), 
                        false);
                    return 1;
                })
        );
        
        // Comando: /attractstats [player]
        dispatcher.register(
            Commands.literal("attractstats")
                .executes(context -> {
                    if (context.getSource().getEntity() instanceof ServerPlayer player) {
                        showPlayerStats(context.getSource(), player);
                    }
                    return 1;
                })
                .then(Commands.argument("player", StringArgumentType.word())
                    .requires(source -> source.hasPermission(2))
                    .executes(context -> {
                        String playerName = StringArgumentType.getString(context, "player");
                        ServerPlayer target = context.getSource().getServer()
                            .getPlayerList().getPlayerByName(playerName);
                        if (target != null) {
                            showPlayerStats(context.getSource(), target);
                        } else {
                            sendFormattedMessage(context.getSource(), Component.translatable("message.attracttochat.player_not_found"), false);
                        }
                        return 1;
                    })
                )
        );
        
        // Comando: /attracthelp
        dispatcher.register(
            Commands.literal("attracthelp")
                .executes(context -> {
                    sendHelpMessage(context.getSource());
                    return 1;
                })
        );
    }
    
    private void sendFormattedMessage(CommandSourceStack source, Component message, boolean broadcast) {
        source.sendSuccess(() -> Component.literal("§6[§eAttract§6] §e").append(message), broadcast);
    }
    
    private void sendFormattedMessage(CommandSourceStack source, String message, boolean broadcast) {
        source.sendSuccess(() -> Component.literal("§6[§eAttract§6] §e" + message), broadcast);
    }
    
    private void showPlayerStats(CommandSourceStack source, ServerPlayer player) {
        PlayerStats stats = PLAYER_STATS.getOrDefault(player.getUUID(), new PlayerStats());
        
        source.sendSuccess(() -> Component.translatable("message.attracttochat.stats_title"), false);
        source.sendSuccess(() -> Component.translatable("message.attracttochat.stats_player").append(player.getName().getString()), false);
        source.sendSuccess(() -> Component.translatable("message.attracttochat.stats_messages").append(String.valueOf(stats.totalMessages)), false);
        source.sendSuccess(() -> Component.translatable("message.attracttochat.stats_mobs").append(String.valueOf(stats.totalMobsAttracted)), false);
        source.sendSuccess(() -> Component.translatable("message.attracttochat.stats_uppercase").append(String.valueOf(stats.totalUppercaseLetters)), false);
        source.sendSuccess(() -> Component.translatable("message.attracttochat.stats_fatigue").append(String.valueOf(stats.totalFatigueApplied)), false);
        source.sendSuccess(() -> Component.literal("§6══════════════════════"), false);
    }
    
    private void sendHelpMessage(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("message.attracttochat.help_title"), false);
        source.sendSuccess(() -> Component.translatable("message.attracttochat.help_subtitle"), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.translatable("message.attracttochat.help_caps"), false);
        source.sendSuccess(() -> Component.translatable("message.attracttochat.help_shouting"), false);
        source.sendSuccess(() -> Component.translatable("message.attracttochat.help_cure"), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.translatable("message.attracttochat.help_commands"), false);
        source.sendSuccess(() -> Component.translatable("message.attracttochat.help_stats"), false);
        source.sendSuccess(() -> Component.translatable("message.attracttochat.help_list"), false);
        source.sendSuccess(() -> Component.translatable("message.attracttochat.help_reload"), false);
        source.sendSuccess(() -> Component.translatable("message.attracttochat.help_debug"), false);
        source.sendSuccess(() -> Component.literal("§6═══════════════════════════"), false);
    }

    // ==================== RELOAD DE MOBS ====================
    
    private int[] reloadAllMobGoals(MinecraftServer server) {
        final int[] removed = {0};
        final int[] added = {0};
        
        List<? extends String> enabledEntities = AttractToChatConfig.COMMON.enabledEntities.get();
        Set<String> enabledSet = new HashSet<>(enabledEntities);
        
        if (debugModeRuntime) {
            System.out.println("[AttractToChat] Reloading... Enabled entities: " + enabledEntities);
        }
        
        // 1. Remove mobs que não estão mais na lista
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, MobGoalData> entry : MOB_GOAL_DATA.entrySet()) {
            MobGoalData data = entry.getValue();
            
            if (data.mob.isRemoved() || !data.mob.isAlive()) {
                toRemove.add(entry.getKey());
                removed[0]++;
                continue;
            }
            
            String entityId = ForgeRegistries.ENTITY_TYPES.getKey(data.mob.getType()).toString();
            
            if (!enabledSet.contains(entityId)) {
                data.mob.goalSelector.removeGoal(data.goal);
                toRemove.add(entry.getKey());
                removed[0]++;
            }
        }
        
        for (UUID uuid : toRemove) {
            MOB_GOAL_DATA.remove(uuid);
        }
        
        // 2. Adiciona Goals para novos mobs
        for (ServerLevel level : server.getAllLevels()) {
            level.getAllEntities().forEach(entity -> {
                if (!(entity instanceof Mob mob)) return;
                if (MOB_GOAL_DATA.containsKey(mob.getUUID())) return;
                
                String entityId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()).toString();
                
                if (enabledSet.contains(entityId)) {
                    addGoalToMob(mob);
                    added[0]++;
                }
            });
        }
        
        return new int[]{removed[0], added[0]};
    }

    // ==================== EVENTOS DE MOB ====================
    
    @SubscribeEvent
    public void onMobSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;

        String entityId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()).toString();
        List<? extends String> enabled = AttractToChatConfig.COMMON.enabledEntities.get();

        if (enabled.contains(entityId)) {
            addGoalToMob(mob);
        }
    }

    private void addGoalToMob(Mob mob) {
        if (MOB_GOAL_DATA.containsKey(mob.getUUID())) return;
        
        // Usa forgetTargetAfterSeconds da config para o timeout base
        int forgetSeconds = AttractToChatConfig.COMMON.forgetTargetAfterSeconds.get();
        MoveToSoundGoal goal = new MoveToSoundGoal(mob, forgetSeconds * 20);
        mob.goalSelector.addGoal(1, goal);
        
        WrappedGoal wrappedGoal = mob.goalSelector.getAvailableGoals().stream()
            .filter(wg -> wg.getGoal() == goal)
            .findFirst()
            .orElse(null);
        
        if (wrappedGoal != null) {
            MOB_GOAL_DATA.put(mob.getUUID(), new MobGoalData(mob, goal, wrappedGoal));
        }
    }

    private void cleanupDeadMobs() {
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, MobGoalData> entry : MOB_GOAL_DATA.entrySet()) {
            if (entry.getValue().mob.isRemoved() || !entry.getValue().mob.isAlive()) {
                toRemove.add(entry.getKey());
            }
        }
        for (UUID uuid : toRemove) {
            MOB_GOAL_DATA.remove(uuid);
        }
        
        if (debugModeRuntime && !toRemove.isEmpty()) {
            System.out.println("[AttractToChat] Cleaned up " + toRemove.size() + " dead mobs");
        }
    }

    // ==================== EVENTO DE CHAT ====================
    
    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getRawText();

        // Verifica fadiga vocal
        if (checkAndPunishFatigue(player)) {
            event.setCanceled(true);
            return;
        }
        
        // Verifica cooldown (usa scanCooldownTicks da config)
        int cooldownTicks = AttractToChatConfig.COMMON.scanCooldownTicks.get();
        long currentTime = System.currentTimeMillis();
        long cooldownMs = (cooldownTicks * 50L); // 50ms por tick
        
        Long lastTime = PLAYER_COOLDOWNS.get(player.getUUID());
        if (lastTime != null && (currentTime - lastTime) < cooldownMs) {
            // Em cooldown, não atrai mobs mas não cancela a mensagem
            if (debugModeRuntime) {
                System.out.println("[AttractToChat] Player " + player.getName().getString() + " em cooldown");
            }
            return;
        }
        PLAYER_COOLDOWNS.put(player.getUUID(), currentTime);

        int upperCaseCount = countUpperCaseLetters(message);

        // Processa chance de fadiga
        if (upperCaseCount > 3) {
            processVocalFatigueChance(player, upperCaseCount);
        }

        double finalRange = calculateRangeWithCaps(upperCaseCount);
        int attracted = attractMobs(player, finalRange, upperCaseCount);
        
        // Registra estatísticas
        PlayerStats stats = PLAYER_STATS.computeIfAbsent(player.getUUID(), k -> new PlayerStats());
        stats.recordMessage(attracted, upperCaseCount);
        
        // Feedback visual se atraiu mobs
        if (attracted > 0 && AttractToChatConfig.COMMON.showAttractionFeedback.get()) {
            sendActionBar(player, Component.translatable("message.attracttochat.mobs_attracted", attracted));
        }
    }

    private int countUpperCaseLetters(String message) {
        int count = 0;
        for (char c : message.toCharArray()) {
            if (Character.isUpperCase(c) && Character.isLetter(c)) {
                count++;
            }
        }
        return count;
    }

    private void processVocalFatigueChance(ServerPlayer player, int upperCaseCount) {
        double multiplier = AttractToChatConfig.COMMON.fatigueChanceMultiplier.get();
        double chancePercent = (upperCaseCount * multiplier) / 100.0;

        if (random.nextDouble() < chancePercent) {
            if (random.nextBoolean()) {
                int durationSeconds = AttractToChatConfig.COMMON.fatigueDurationBase.get();
                applyVocalFatigue(player, durationSeconds * 20);
            } else {
                sendActionBar(player, Component.translatable("message.attracttochat.throat_warning"));
                playWarningSound(player, 0.5f);
            }
        } else if (upperCaseCount > 15) {
            sendActionBar(player, Component.translatable("message.attracttochat.throat_dry"));
            playWarningSound(player, 0.3f);
        } else if (upperCaseCount > 10) {
            sendActionBar(player, Component.translatable("message.attracttochat.throat_hurts"));
        }
    }

    private double calculateRangeWithCaps(int upperCaseCount) {
        double baseRange = AttractToChatConfig.COMMON.hearingRange.get();
        double bonusPerCap = AttractToChatConfig.COMMON.capsRangeBonus.get();
        return baseRange + (upperCaseCount * bonusPerCap);
    }

    private int attractMobs(ServerPlayer player, double range, int upperCaseCount) {
        BlockPos targetPos = player.blockPosition();
        cleanupDeadMobs();

        List<Mob> attractedMobs = player.level().getEntitiesOfClass(
            Mob.class, 
            player.getBoundingBox().inflate(range)
        );

        int attracted = 0;
        ServerLevel level = (ServerLevel) player.level();
        
        for (Mob mob : attractedMobs) {
            MobGoalData data = MOB_GOAL_DATA.get(mob.getUUID());
            if (data != null) {
                data.goal.setTarget(targetPos, upperCaseCount);
                attracted++;
                
                // Partículas visuais (se habilitado)
                if (AttractToChatConfig.COMMON.showAttractionParticles.get()) {
                    spawnAttractionParticles(mob, level);
                }
            }
        }

        if (debugModeRuntime) {
            System.out.println("[AttractToChat] Attracted " + attracted + " mobs in range " + range);
        }
        
        return attracted;
    }
    
    private void spawnAttractionParticles(Mob mob, ServerLevel level) {
        Vec3 pos = mob.position();
        // Partículas de nota musical acima do mob
        level.sendParticles(
            ParticleTypes.NOTE,
            pos.x, pos.y + mob.getBbHeight() + 0.5, pos.z,
            3, 0.2, 0.2, 0.2, 0.5
        );
        // Partículas de exclamação/alerta
        level.sendParticles(
            ParticleTypes.ANGRY_VILLAGER,
            pos.x, pos.y + mob.getBbHeight() + 0.2, pos.z,
            1, 0.1, 0.1, 0.1, 0
        );
    }
    
    private void sendActionBar(ServerPlayer player, Component message) {
        player.connection.send(new ClientboundSetActionBarTextPacket(message));
    }
    
    private void playWarningSound(ServerPlayer player, float volume) {
        player.level().playSound(
            null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.NOTE_BLOCK_BASS.get(),
            SoundSource.PLAYERS,
            volume, 0.5f
        );
    }

    // ==================== BLOQUEIO DE COMANDOS ====================
    
    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        if (event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player) {
            String command = event.getParseResults().getReader().getString();
            List<String> vocalCommands = Arrays.asList("tell", "w", "msg", "say", "me", "shout");
            
            boolean isVocal = vocalCommands.stream()
                .anyMatch(cmd -> command.startsWith(cmd) || command.startsWith("/" + cmd));

            if (isVocal && checkAndPunishFatigue(player)) {
                event.setCanceled(true);
            }
        }
    }

    // ==================== CURA COM LÍQUIDOS ====================
    
    @SubscribeEvent
    public void onDrink(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.hasEffect(VOCAL_FATIGUE.get())) return;

        ItemStack stack = event.getItem();
        MobEffectInstance currentEffect = player.getEffect(VOCAL_FATIGUE.get());
        if (currentEffect == null) return;
        
        int currentDuration = currentEffect.getDuration();
        int reductionSeconds = 0;
        boolean isBadLiquid = false;

        int honeyRelief = AttractToChatConfig.COMMON.honeyRelief.get();
        int waterRelief = AttractToChatConfig.COMMON.waterRelief.get();
        int stewRelief = AttractToChatConfig.COMMON.stewRelief.get();
        int poisonPenalty = AttractToChatConfig.COMMON.poisonWorsen.get();

        if (stack.is(Items.HONEY_BOTTLE)) {
            reductionSeconds = honeyRelief;
            sendActionBar(player, Component.translatable("message.attracttochat.honey_relief"));
            playHealSound(player);
        } 
        else if (stack.is(Items.MILK_BUCKET)) {
            player.removeEffect(VOCAL_FATIGUE.get());
            sendActionBar(player, Component.translatable("message.attracttochat.milk_relief"));
            playHealSound(player);
            return;
        }
        else if (stack.is(Items.MUSHROOM_STEW) || stack.is(Items.BEETROOT_SOUP) || stack.is(Items.RABBIT_STEW)) {
            reductionSeconds = stewRelief;
            sendActionBar(player, Component.translatable("message.attracttochat.stew_relief"));
            playHealSound(player);
        }
        else if (stack.is(Items.POTION)) {
            if (PotionUtils.getPotion(stack) == Potions.WATER) {
                reductionSeconds = waterRelief;
                sendActionBar(player, Component.translatable("message.attracttochat.water_relief"));
                playHealSound(player);
            } 
            else if (PotionUtils.getPotion(stack) == Potions.HARMING || 
                     PotionUtils.getPotion(stack) == Potions.POISON ||
                     PotionUtils.getPotion(stack) == Potions.LONG_POISON ||
                     PotionUtils.getPotion(stack) == Potions.STRONG_POISON) {
                isBadLiquid = true;
            }
        }

        player.removeEffect(VOCAL_FATIGUE.get());
        
        if (isBadLiquid) {
            sendActionBar(player, Component.translatable("message.attracttochat.bad_liquid"));
            player.addEffect(new MobEffectInstance(VOCAL_FATIGUE.get(), currentDuration + (poisonPenalty * 20), 0));
            playDamageSound(player);
        } else if (reductionSeconds > 0) {
            int newDuration = currentDuration - (reductionSeconds * 20);
            if (newDuration > 0) {
                player.addEffect(new MobEffectInstance(VOCAL_FATIGUE.get(), newDuration, 0));
            }
        }
    }
    
    private void playHealSound(ServerPlayer player) {
        player.level().playSound(
            null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_LEVELUP,
            SoundSource.PLAYERS,
            0.3f, 1.5f
        );
    }
    
    private void playDamageSound(ServerPlayer player) {
        player.level().playSound(
            null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_HURT,
            SoundSource.PLAYERS,
            0.5f, 0.8f
        );
    }

    // ==================== MÉTODOS AUXILIARES ====================
    
    private boolean checkAndPunishFatigue(ServerPlayer player) {
        if (!player.hasEffect(VOCAL_FATIGUE.get())) return false;
        
        MobEffectInstance current = player.getEffect(VOCAL_FATIGUE.get());
        if (current == null) return false;
        
        if (current.getDuration() > 12000) {
            sendActionBar(player, Component.translatable("message.attracttochat.voice_lost"));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 1));
            playDamageSound(player);
            return true;
        }

        sendActionBar(player, Component.translatable("message.attracttochat.try_speak_fail"));
        int penalty = AttractToChatConfig.COMMON.fatiguePenalty.get() * 20;
        player.addEffect(new MobEffectInstance(VOCAL_FATIGUE.get(), current.getDuration() + penalty, 0));
        return true;
    }

    private void applyVocalFatigue(ServerPlayer player, int durationTicks) {
        player.addEffect(new MobEffectInstance(VOCAL_FATIGUE.get(), durationTicks, 0));
        sendActionBar(player, Component.translatable("message.attracttochat.fatigue_start"));
        playDamageSound(player);
        
        // Registra estatística
        PlayerStats stats = PLAYER_STATS.computeIfAbsent(player.getUUID(), k -> new PlayerStats());
        stats.recordFatigue();
    }

    private static boolean isDebugMode() {
        return debugModeRuntime || AttractToChatConfig.COMMON.debugMode.get();
    }

    // ==================== GOAL INTERNO ====================
    
    public static class MoveToSoundGoal extends Goal {
        private final Mob mob;
        private BlockPos targetPos;
        private int timeout;
        private int maxTimeout;
        private final int baseTimeout;
        
        private static final int TICKS_PER_UPPERCASE = 10; // +0.5s por maiúscula
        private static final int MAX_TIMEOUT = 600; // 30 segundos máximo

        public MoveToSoundGoal(Mob mob, int baseTimeoutTicks) {
            this.mob = mob;
            this.baseTimeout = baseTimeoutTicks;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public void setTarget(BlockPos pos, int upperCaseCount) {
            this.targetPos = pos;
            this.maxTimeout = Math.min(
                baseTimeout + (upperCaseCount * TICKS_PER_UPPERCASE),
                MAX_TIMEOUT
            );
            this.timeout = this.maxTimeout;
            
            if (isDebugMode()) {
                System.out.println("[AttractToChat] Goal activated | Persistence: " + 
                    (this.maxTimeout / 20) + "s | Uppercase: " + upperCaseCount);
            }
        }

        @Override
        public boolean canUse() {
            return this.targetPos != null && this.timeout > 0;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.targetPos == null || this.timeout <= 0) return false;
            double distSq = this.mob.blockPosition().distSqr(this.targetPos);
            return distSq > 4.0;
        }

        @Override
        public void start() {
            if (this.targetPos != null && this.mob.getNavigation() != null) {
                Path path = this.mob.getNavigation().createPath(this.targetPos, 0);
                if (path != null) {
                    double speedMultiplier = 1.0 + (this.maxTimeout - baseTimeout) / 1000.0;
                    speedMultiplier = Math.min(speedMultiplier, 1.5);
                    
                    this.mob.getNavigation().moveTo(path, speedMultiplier);
                    
                    if (isDebugMode()) {
                        System.out.println("[AttractToChat] Path found | Speed: " + 
                            String.format("%.2f", speedMultiplier));
                    }
                }
            }
        }

        @Override
        public void tick() {
            this.timeout--;
            
            if (this.timeout <= 0) {
                if (isDebugMode()) {
                    System.out.println("[AttractToChat] Goal timeout");
                }
                this.stop();
            }
        }

        @Override
        public void stop() {
            this.targetPos = null;
            this.timeout = 0;
            if (this.mob.getNavigation() != null) {
                this.mob.getNavigation().stop();
            }
        }
        
        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }
}
