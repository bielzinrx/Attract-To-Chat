package com.bielzinrx.attracttochat.platform;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public interface IPlatformHelper {

    boolean isModLoaded(String modId);
    boolean hasClientMod(ServerPlayer player);
    Path getConfigDir();

    GoalSelector getGoalSelector(Mob mob);
    String getEntityTypeId(EntityType<?> type);
}
