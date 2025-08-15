package com.bielzinrx.attracttochat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

@Mod(AttractToChatMod.MODID)
public class AttractToChatMod {
    public static final String MODID = "attracttochat";

    private static class PendingSound {
        final UUID playerId;
        final double x, y, z;
        final long createdTick;
        PendingSound(UUID id, double x, double y, double z, long tick) {
            this.playerId = id; this.x = x; this.y = y; this.z = z; this.createdTick = tick;
        }
    }

    private final Map<ServerLevel, PendingSound> pendingByLevel = new HashMap<>();
    private final Map<Mob, Long> mobLastHeardTime = new HashMap<>();
    private final Map<Mob, Vec3> mobTargets = new HashMap<>();
    private long lastScanTick = 0L;

    public AttractToChatMod() {
        AttractToChatConfig.register();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent e) {
        ServerPlayer player = e.getPlayer();
        if (player.level() instanceof ServerLevel level) {
            pendingByLevel.put(level, new PendingSound(player.getUUID(), player.getX(), player.getY(), player.getZ(), level.getServer().getTickCount()));
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        ServerLevel anyLevel = e.getServer().overworld();
        long tick = anyLevel.getServer().getTickCount();

        if (tick - lastScanTick < AttractToChatConfig.SERVER.scanCooldownTicks.get()) return;
        lastScanTick = tick;

        double range = AttractToChatConfig.SERVER.hearingRange.get();
        Set<String> allow = AttractToChatConfig.SERVER.enabledEntities.get().stream()
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());

        Map<ServerLevel, PendingSound> snapshot = new HashMap<>(pendingByLevel);
        pendingByLevel.clear();

        for (Map.Entry<ServerLevel, PendingSound> entry : snapshot.entrySet()) {
            ServerLevel level = entry.getKey();
            PendingSound sound = entry.getValue();

            AABB box = new AABB(sound.x - range, sound.y - range, sound.z - range,
                                sound.x + range, sound.y + range, sound.z + range);

            List<Mob> mobs = level.getEntitiesOfClass(Mob.class, box);
            for (Mob mob : mobs) {
                if (!mob.isAlive() || mob.isNoAi()) continue;
                ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
                if (id == null || !allow.contains(id.toString())) continue;

                long now = level.getGameTime();
                mobTargets.put(mob, new Vec3(sound.x, sound.y, sound.z));
                mobLastHeardTime.put(mob, now);
            }
        }

        // Mover mobs para seus alvos, se ainda válido
        Iterator<Map.Entry<Mob, Vec3>> it = mobTargets.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Mob, Vec3> entry = it.next();
            Mob mob = entry.getKey();
            Vec3 target = entry.getValue();

            if (!mob.isAlive()) {
                it.remove();
                mobLastHeardTime.remove(mob);
                continue;
            }

            long heardTime = mobLastHeardTime.getOrDefault(mob, 0L);
            long forgetTicks = AttractToChatConfig.SERVER.forgetTargetAfterSeconds.get() * 20L;
            if (mob.level().getGameTime() - heardTime > forgetTicks) {
                it.remove();
                mobLastHeardTime.remove(mob);
                continue;
            }

            double distSq = mob.distanceToSqr(target.x, target.y, target.z);
            if (distSq > 4.0) { // mais de 2 blocos
                mob.getNavigation().moveTo(target.x, target.y, target.z, 1.1D);
            }
        }
    }
}
