package com.bielzinrx.attracttochat.forge;

import com.bielzinrx.attracttochat.AttractToChat;
import com.bielzinrx.attracttochat.command.AtcCommand;
import com.bielzinrx.attracttochat.engine.AtcEngine;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AttractToChat.MOD_ID)
public final class AtcForgeMod {
    public AtcForgeMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLeave);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
        MinecraftForge.EVENT_BUS.addListener(
            EventPriority.LOWEST, true, ServerChatEvent.Submitted.class, this::onServerChat);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        AttractToChat.init();
    }

    private void registerCommands(RegisterCommandsEvent event) {
        AtcCommand.register(event.getDispatcher());
    }

    private void onServerStarting(net.minecraftforge.event.server.ServerStartingEvent event) {
        AttractToChat.setServer(event.getServer());
        AtcEngine.refreshCaches();
    }

    private void onServerChat(ServerChatEvent.Submitted event) {
        if (event.isCanceled()) return;

        ServerPlayer player = event.getPlayer();
        String message = event.getRawText();

        if (AtcEngine.isVocallyMuted(player.getUUID())) {
            event.setCanceled(true);
            player.server.execute(() -> AtcEngine.handleChatCancellable(player, message));
            return;
        }

        player.server.execute(() -> AtcEngine.handleChatAfter(player, message));
    }

    private void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            AtcEngine.onServerTick();
        }
    }

    private void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        AtcEngine.onPlayerDisconnect(event.getEntity().getUUID());
    }

    private void onServerStopping(ServerStoppingEvent event) {
        AttractToChat.setServer(null);
        AtcEngine.onServerStop();
    }
}
