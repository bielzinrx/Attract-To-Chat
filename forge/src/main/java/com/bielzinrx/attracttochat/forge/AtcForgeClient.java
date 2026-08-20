package com.bielzinrx.attracttochat.forge;

import com.bielzinrx.attracttochat.AttractToChat;
import com.bielzinrx.attracttochat.forge.platform.ForgePlatformHelper;
import net.minecraft.network.Connection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
    modid = AttractToChat.MOD_ID,
    value = Dist.CLIENT,
    bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AtcForgeClient {
    private static Connection pendingConnection;
    private static int retryTicks;

    private AtcForgeClient() {}

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        pendingConnection = event.getConnection();
        retryTicks = 200;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || pendingConnection == null) {
            return;
        }

        if (ForgePlatformHelper.isClientPresenceChannelAvailable(pendingConnection)) {
            ForgePlatformHelper.sendClientPresence();
            pendingConnection = null;
            retryTicks = 0;
            return;
        }

        retryTicks--;
        if (retryTicks <= 0) {
            pendingConnection = null;
        }
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        pendingConnection = null;
        retryTicks = 0;
    }
}
