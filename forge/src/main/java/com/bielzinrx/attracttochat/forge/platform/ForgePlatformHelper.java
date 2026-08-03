package com.bielzinrx.attracttochat.forge.platform;

import com.bielzinrx.attracttochat.mixin.MobAccessorMixin;
import com.bielzinrx.attracttochat.platform.IPlatformHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Path;

public final class ForgePlatformHelper implements IPlatformHelper {
    private static final SimpleChannel CLIENT_PRESENCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("attracttochat", "client_presence"),
        () -> "1", version -> true, version -> true);

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean hasClientMod(ServerPlayer player) {
        return player != null && CLIENT_PRESENCE.isRemotePresent(player.connection.connection);
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
}
