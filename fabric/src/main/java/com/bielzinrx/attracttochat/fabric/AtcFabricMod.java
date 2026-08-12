package com.bielzinrx.attracttochat.fabric;

import com.bielzinrx.attracttochat.AttractToChat;
import com.bielzinrx.attracttochat.client.ClientPresence;
import com.bielzinrx.attracttochat.command.AtcCommand;
import com.bielzinrx.attracttochat.engine.AtcEngine;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceLocation;

public final class AtcFabricMod implements ModInitializer {
    private static final ResourceLocation CLIENT_PRESENCE =
        new ResourceLocation("attracttochat", "client_presence");

    private static final int CLIENT_PROTOCOL = 1;

    /*
     * ALLOW_CHAT_MESSAGE is also fired after messages originating from /say
     * and /me. This marker keeps commands out of the normal ATC chat path.
     */
    private static final ThreadLocal<PlayerChatMessage> COMMAND_MESSAGE =
        new ThreadLocal<>();

    @Override
    public void onInitialize() {
        AttractToChat.init();

        registerClientPresence();
        registerChatEvents();

        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) ->
                AtcCommand.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            AttractToChat.setServer(server);
            AtcEngine.refreshCaches();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
            AtcEngine.onServerStop());

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            COMMAND_MESSAGE.remove();
            AttractToChat.setServer(null);
        });

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

    private static void registerChatEvents() {
        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register(
            (message, source, params) -> {
                COMMAND_MESSAGE.set(message);
                return true;
            });

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(
            (message, sender, params) -> {
                PlayerChatMessage commandMessage = COMMAND_MESSAGE.get();
                COMMAND_MESSAGE.remove();

                if (commandMessage == message) {
                    return true;
                }

                String content = message.signedContent();

                if (content == null || content.isBlank()) {
                    return true;
                }

                if (AtcEngine.handleChatCancellable(sender, content)) {
                    return false;
                }

                AtcEngine.handleChatAfter(sender, content);
                return true;
            });
    }
}
