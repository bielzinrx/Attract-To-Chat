package com.bielzinrx.attracttochat;

import com.bielzinrx.attracttochat.config.AttractToChatConfig;
import com.bielzinrx.attracttochat.engine.AtcEngine;
import com.bielzinrx.attracttochat.i18n.ServerTranslations;
import com.bielzinrx.attracttochat.platform.Platform;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public final class AttractToChat {
    public static final String MOD_ID = "attracttochat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static AttractToChat instance;
    private static net.minecraft.server.MinecraftServer serverInstance;

    public static AttractToChat getInstance() {
        return instance;
    }

    public static net.minecraft.server.MinecraftServer getServer() {
        return serverInstance;
    }

    public static void setServer(net.minecraft.server.MinecraftServer server) {
        serverInstance = server;
    }


    public static void init() {
        instance = new AttractToChat();
        LOGGER.info("[AttractToChat] Initializing Common...");

        AttractToChatConfig.load();
        AtcEngine.refreshCaches();

    }

    public void sendHelp(ServerPlayer player) {
        sendHelp(player, "overview");
    }

    public void sendHelp(ServerPlayer player, String category) {
        player.sendSystemMessage(ServerTranslations.component(player, "message.attracttochat.command.help.header"));
        String safeCategory = category == null ? "overview" : category.toLowerCase(Locale.ROOT);
        switch (safeCategory) {
            case "overview" -> {
                sendHelpLine(player, "message.attracttochat.command.help.overview.gameplay");
                sendHelpLine(player, "message.attracttochat.command.help.overview.feature");
                sendHelpLine(player, "message.attracttochat.command.help.overview.mobs");
                if (Platform.getHelper().hasClientMod(player)) {
                    sendHelpLine(player, "message.attracttochat.command.help.overview.client");
                }
                sendHelpLine(player, "message.attracttochat.command.help.overview.admin");
            }
            case "gameplay" -> {
                sendHelpLine(player, "message.attracttochat.command.help.radius");
                sendHelpLine(player, "message.attracttochat.command.help.capsrangebonus");
                sendHelpLine(player, "message.attracttochat.command.help.feature");
                sendHelpLine(player, "message.attracttochat.command.help.preset");
                sendHelpLine(player, "message.attracttochat.command.help.preset_custom");
            }
            case "feature" -> {
                sendHelpLine(player, "message.attracttochat.command.help.feature");
                sendHelpLine(player, "message.attracttochat.command.help.caps");
                sendHelpLine(player, "message.attracttochat.command.help.fatigue");
                sendHelpLine(player, "message.attracttochat.command.help.antispam");
                sendHelpLine(player, "message.attracttochat.command.help.cooldown");
            }
            case "mobs" -> {
                sendHelpLine(player, "message.attracttochat.command.help.entity");
                sendHelpLine(player, "message.attracttochat.command.help.ignore");
                sendHelpLine(player, "message.attracttochat.command.help.trollmode");
            }
            case "client" -> {
                if (Platform.getHelper().hasClientMod(player)) {
                    sendHelpLine(player, "message.attracttochat.command.help.client");
                    sendHelpLine(player, "message.attracttochat.command.help.client_note");
                } else {
                    player.sendSystemMessage(ServerTranslations.component(
                        player, "message.attracttochat.command.help.unknown_category", safeCategory));
                }
            }
            case "admin" -> {
                sendHelpLine(player, "message.attracttochat.command.help.status");
                sendHelpLine(player, "message.attracttochat.command.help.debug");
                sendHelpLine(player, "message.attracttochat.command.help.config");
                sendHelpLine(player, "message.attracttochat.command.help.mobspeed");
                sendHelpLine(player, "message.attracttochat.command.help.forgettime");
            }
            case "config" -> {
                sendHelpLine(player, "message.attracttochat.command.help.config");
                sendHelpLine(player, "message.attracttochat.command.help.config_list");
                sendHelpLine(player, "message.attracttochat.command.help.config_info");
            }
            default -> player.sendSystemMessage(ServerTranslations.component(
                player, "message.attracttochat.command.help.unknown_category", safeCategory));
        }
    }

    private static void sendHelpLine(ServerPlayer player, String key) {
        player.sendSystemMessage(ServerTranslations.component(player, key));
    }
}
