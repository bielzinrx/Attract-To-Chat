package com.bielzinrx.attracttochat.fabric;

import com.bielzinrx.attracttochat.engine.AtcEngine;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public final class AtcFabricChat {

    private AtcFabricChat() {}

    public static boolean onChatPacket(ServerGamePacketListener listener, String message) {
        if (!(listener instanceof ServerGamePacketListenerImpl handler)) return false;
        ServerPlayer player = handler.player;
        if (message == null || message.isBlank()) return false;
        if (AtcEngine.handleChatCancellable(player, message)) return true;
        player.server.execute(() -> AtcEngine.handleChatAfter(player, message));
        return false;
    }
}
