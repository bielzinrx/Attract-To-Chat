package com.bielzinrx.attracttochat.client;

import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientPresence {
    private static final Set<UUID> PRESENT = ConcurrentHashMap.newKeySet();

    private ClientPresence() {}

    public static boolean isPresent(ServerPlayer player) {
        return player != null && PRESENT.contains(player.getUUID());
    }

    public static void markPresent(ServerPlayer player) {
        if (player == null || !PRESENT.add(player.getUUID())) return;
        if (player.getServer() != null) {
            player.getServer().getCommands().sendCommands(player);
        }
    }

    public static void forget(UUID playerId) {
        if (playerId != null) PRESENT.remove(playerId);
    }

    public static void clear() {
        PRESENT.clear();
    }
}
