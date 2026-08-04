package com.bielzinrx.attracttochat.forge.platform;

import com.bielzinrx.attracttochat.client.ClientPresence;
import com.bielzinrx.attracttochat.mixin.MobAccessorMixin;
import com.bielzinrx.attracttochat.platform.IPlatformHelper;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Path;
import java.util.function.Supplier;

public final class ForgePlatformHelper implements IPlatformHelper {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CLIENT_PRESENCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("attracttochat", "client_presence"),
        () -> PROTOCOL_VERSION,
        NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION),
        NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION));

    static {
        CLIENT_PRESENCE.registerMessage(0, ClientPresencePacket.class,
            (message, buffer) -> {},
            buffer -> new ClientPresencePacket(),
            ForgePlatformHelper::handleClientPresence);
    }

    private static void handleClientPresence(ClientPresencePacket message,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) ClientPresence.markPresent(sender);
        });
        context.setPacketHandled(true);
    }

    public static boolean isClientPresenceChannelAvailable(Connection connection) {
        return connection != null && CLIENT_PRESENCE.isRemotePresent(connection);
    }

    public static void sendClientPresence() {
        CLIENT_PRESENCE.sendToServer(new ClientPresencePacket());
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean hasClientMod(ServerPlayer player) {
        return ClientPresence.isPresent(player);
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public GoalSelector getGoalSelector(Mob mob) {
        return ((MobAccessorMixin) mob).atc_getGoalSelector();
    }

    @Override
    public String getEntityTypeId(EntityType<?> type) {
        return String.valueOf(ForgeRegistries.ENTITY_TYPES.getKey(type));
    }

    private static final class ClientPresencePacket {}
}
