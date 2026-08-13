package com.bielzinrx.attracttochat.engine;

import java.util.function.IntPredicate;

final class GroundTargetResolver {
    static final int NO_TARGET = Integer.MIN_VALUE;

    private GroundTargetResolver() {}

    static int findFeetY(int originY, int minBuildHeight, int maxSearchDepth,
            IntPredicate isStandableFloor) {
        if (isStandableFloor == null || originY < minBuildHeight || maxSearchDepth < 0) {
            return NO_TARGET;
        }

        int lowestFloorY = Math.max(minBuildHeight, originY - maxSearchDepth);
        for (int floorY = originY; floorY >= lowestFloorY; floorY--) {
            if (isStandableFloor.test(floorY)) {
                return floorY + 1;
            }
        }
        return NO_TARGET;
    }
}
