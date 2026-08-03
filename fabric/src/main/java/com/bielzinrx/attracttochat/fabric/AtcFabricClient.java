package com.bielzinrx.attracttochat.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public final class AtcFabricClient implements ClientModInitializer {
    private static final ResourceLocation CLIENT_PRESENCE =
        new ResourceLocation("attracttochat", "client_presence");

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CLIENT_PRESENCE,
            (client, handler, buffer, responseSender) -> {

            });
    }
}
