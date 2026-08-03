package com.bielzinrx.attracttochat.fabric;

import com.bielzinrx.attracttochat.AttractToChat;
import com.bielzinrx.attracttochat.command.AtcCommand;
import com.bielzinrx.attracttochat.engine.AtcEngine;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class AtcFabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        AttractToChat.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> AtcCommand.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            AttractToChat.setServer(server);
            AtcEngine.refreshCaches();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            AttractToChat.setServer(null);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> AtcEngine.onServerTick());
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            AtcEngine.onPlayerDisconnect(handler.player.getUUID()));

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> AtcEngine.onServerStop());
    }
}
