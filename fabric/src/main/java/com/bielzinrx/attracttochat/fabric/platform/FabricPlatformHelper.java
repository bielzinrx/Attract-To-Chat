package com.bielzinrx.attracttochat.fabric.platform;

import com.bielzinrx.attracttochat.client.ClientPresence;
import com.bielzinrx.attracttochat.mixin.MobAccessorMixin;
import com.bielzinrx.attracttochat.platform.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;

import java.nio.file.Path;

public final class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean hasClientMod(ServerPlayer player) {
        return ClientPresence.isPresent(player);
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
        return Registry.ENTITY_TYPE.getKey(type).toString();
    }
}
