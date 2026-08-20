package com.bielzinrx.attracttochat.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class AtcFabricClient implements ClientModInitializer {
    private static final ResourceLocation CLIENT_PRESENCE =
        new ResourceLocation("attracttochat", "client_presence");

    private static final int CLIENT_PROTOCOL = 1;

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (!ClientPlayNetworking.canSend(CLIENT_PRESENCE)) {
                return;
            }

            FriendlyByteBuf buffer = PacketByteBufs.create();
            buffer.writeVarInt(CLIENT_PROTOCOL);

            ClientPlayNetworking.send(CLIENT_PRESENCE, buffer);
        });
    }
}
