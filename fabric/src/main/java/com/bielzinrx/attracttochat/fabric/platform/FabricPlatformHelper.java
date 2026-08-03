package com.bielzinrx.attracttochat.fabric.platform;

import com.bielzinrx.attracttochat.mixin.MobAccessorMixin;
import com.bielzinrx.attracttochat.platform.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;

import java.nio.file.Path;

public final class FabricPlatformHelper implements IPlatformHelper {
    private static final ResourceLocation CLIENT_PRESENCE =
        new ResourceLocation("attracttochat", "client_presence");

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean hasClientMod(ServerPlayer player) {
        return player != null && ServerPlayNetworking.canSend(player, CLIENT_PRESENCE);
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public GoalSelector getGoalSelector(Mob mob) {
        return ((MobAccessorMixin) mob).atc_getGoalSelector();
    }

    @Override
    public String getEntityTypeId(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
    }
}
