package com.bielzinrx.attracttochat.fabric;

import com.bielzinrx.attracttochat.AttractToChat;
import com.bielzinrx.attracttochat.client.ClientPresence;
import com.bielzinrx.attracttochat.command.AtcCommand;
import com.bielzinrx.attracttochat.engine.AtcEngine;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public final class AtcFabricMod implements ModInitializer {
    private static final ResourceLocation CLIENT_PRESENCE =
        new ResourceLocation("attracttochat", "client_presence");

    private static final int CLIENT_PROTOCOL = 1;

    @Override
    public void onInitialize() {
        AttractToChat.init();

        registerClientPresence();

        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) ->
                AtcCommand.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            AttractToChat.setServer(server);
            AtcEngine.refreshCaches();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
            AtcEngine.onServerStop());

        ServerLifecycleEvents.SERVER_STOPPED.register(server ->
            AttractToChat.setServer(null));

        ServerTickEvents.END_SERVER_TICK.register(server ->
            AtcEngine.onServerTick());

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            AtcEngine.onPlayerDisconnect(handler.player.getUUID()));
    }

    private static void registerClientPresence() {
        boolean registered = ServerPlayNetworking.registerGlobalReceiver(
            CLIENT_PRESENCE,
            (server, player, handler, buffer, responseSender) -> {
                final int protocol;

                try {
                    if (!buffer.isReadable()) {
                        AttractToChat.LOGGER.warn(
                            "Rejected empty ATC Fabric client-presence packet from {}.",
                            player.getGameProfile().getName());
                        return;
                    }

                    protocol = buffer.readVarInt();
                } catch (RuntimeException exception) {
                    AttractToChat.LOGGER.warn(
                        "Rejected malformed ATC Fabric client-presence packet from {}: {}",
                        player.getGameProfile().getName(),
                        exception.toString());
                    return;
                }

                if (protocol != CLIENT_PROTOCOL) {
                    AttractToChat.LOGGER.warn(
                        "Rejected incompatible ATC Fabric client protocol {} from {}. Expected {}.",
                        protocol,
                        player.getGameProfile().getName(),
                        CLIENT_PROTOCOL);
                    return;
                }

                server.execute(() -> ClientPresence.markPresent(player));
            });

        if (!registered) {
            throw new IllegalStateException(
                "The Fabric receiver attracttochat:client_presence was already registered.");
        }
    }
}
