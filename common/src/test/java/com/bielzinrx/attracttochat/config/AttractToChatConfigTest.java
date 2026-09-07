package com.bielzinrx.attracttochat.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttractToChatConfigTest {
    private double hearingRange;
    private double capsRangeBonus;
    private boolean vocalFatigue;
    private boolean antiSpam;
    private int scanCooldown;
    private boolean showParticles;

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void captureConfig() {
        hearingRange = AttractToChatConfig.COMMON.hearingRange.get();
        capsRangeBonus = AttractToChatConfig.COMMON.capsRangeBonus.get();
        vocalFatigue = AttractToChatConfig.COMMON.enableVocalFatigue.get();
        antiSpam = AttractToChatConfig.COMMON.enableAntiSpam.get();
        scanCooldown = AttractToChatConfig.COMMON.scanCooldownTicks.get();
        showParticles = AttractToChatConfig.COMMON.showParticles.get();
    }

    @AfterEach
    void restoreConfig() {
        AttractToChatConfig.COMMON.hearingRange.set(hearingRange);
        AttractToChatConfig.COMMON.capsRangeBonus.set(capsRangeBonus);
        AttractToChatConfig.COMMON.enableVocalFatigue.set(vocalFatigue);
        AttractToChatConfig.COMMON.enableAntiSpam.set(antiSpam);
        AttractToChatConfig.COMMON.scanCooldownTicks.set(scanCooldown);
        AttractToChatConfig.COMMON.showParticles.set(showParticles);
        AttractToChatConfig.undoLastPresetChanges();
    }

    @Test
    void keepsReleaseSchemaAtFifteen() throws ReflectiveOperationException {
        Field field = AttractToChatConfig.class.getDeclaredField("CONFIG_VERSION");
        field.setAccessible(true);
        assertEquals(16, field.getInt(null));
    }

    @Test
    void appliesAndUndoesBuiltInPresetWithoutChangingUnmanagedValues() {
        AttractToChatConfig.COMMON.hearingRange.set(41.0);
        AttractToChatConfig.COMMON.capsRangeBonus.set(9.0);
        AttractToChatConfig.COMMON.enableVocalFatigue.set(true);
        AttractToChatConfig.COMMON.enableAntiSpam.set(true);
        AttractToChatConfig.COMMON.scanCooldownTicks.set(75);
        AttractToChatConfig.COMMON.showParticles.set(false);

        assertTrue(AttractToChatConfig.applyPresetValues("safe"));
        assertEquals(24.0, AttractToChatConfig.COMMON.hearingRange.get());
        assertEquals(4.0, AttractToChatConfig.COMMON.capsRangeBonus.get());
        assertFalse(AttractToChatConfig.COMMON.enableVocalFatigue.get());
        assertFalse(AttractToChatConfig.COMMON.enableAntiSpam.get());
        assertEquals(20, AttractToChatConfig.COMMON.scanCooldownTicks.get());
        assertFalse(AttractToChatConfig.COMMON.showParticles.get());

        AttractToChatConfig.PresetUndoResult undo = AttractToChatConfig.undoLastPresetChanges();
        assertTrue(undo.isAvailable());
        assertEquals("safe", undo.getPresetName());
        assertEquals(41.0, AttractToChatConfig.COMMON.hearingRange.get());
        assertEquals(9.0, AttractToChatConfig.COMMON.capsRangeBonus.get());
        assertTrue(AttractToChatConfig.COMMON.enableVocalFatigue.get());
        assertTrue(AttractToChatConfig.COMMON.enableAntiSpam.get());
        assertEquals(75, AttractToChatConfig.COMMON.scanCooldownTicks.get());
        assertFalse(AttractToChatConfig.COMMON.showParticles.get());
    }
}
