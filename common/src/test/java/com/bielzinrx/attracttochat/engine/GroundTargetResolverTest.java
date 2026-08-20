package com.bielzinrx.attracttochat.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GroundTargetResolverTest {
    @Test
    void resolvesFloorBelowThreeAirBlocks() {
        int feetY = GroundTargetResolver.findFeetY(52, -64, 30, floorY -> floorY == 48);
        assertEquals(49, feetY);
    }

    @Test
    void resolvesConfirmedFourAirRegression() {
        int feetY = GroundTargetResolver.findFeetY(52, -64, 30, floorY -> floorY == 47);
        assertEquals(48, feetY);
    }

    @Test
    void rejectsFloorOutsideLegacyDepthFour() {
        int feetY = GroundTargetResolver.findFeetY(52, -64, 4, floorY -> floorY == 47);
        assertEquals(GroundTargetResolver.NO_TARGET, feetY);
    }

    @Test
    void selectsNearestStandableFloor() {
        int feetY = GroundTargetResolver.findFeetY(80, -64, 40,
            floorY -> floorY == 72 || floorY == 60);
        assertEquals(73, feetY);
    }

    @Test
    void neverSearchesBelowBuildHeight() {
        int feetY = GroundTargetResolver.findFeetY(-60, -64, 30, floorY -> floorY == -65);
        assertEquals(GroundTargetResolver.NO_TARGET, feetY);
    }
}
