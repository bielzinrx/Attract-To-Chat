package com.bielzinrx.attracttochat.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bielzinrx.attracttochat.config.AttractToChatConfig;
import com.bielzinrx.attracttochat.fatigue.FatigueTracker;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AtcEngineBehaviorTest {
    @AfterEach
    void resetState() {
        AttractToChatConfig.COMMON.enableCapsFeature.set(true);
        AttractToChatConfig.COMMON.enableAntiSpam.set(false);
        AttractToChatConfig.COMMON.scanCooldownTicks.set(40);
        AttractToChatConfig.COMMON.antiSpamMaxMessages.set(3);
        AttractToChatConfig.COMMON.antiSpamWindowSeconds.set(8);
        AttractToChatConfig.COMMON.ignoredPlayers.set(new ArrayList<>());
        AttractToChatConfig.COMMON.trollPlayers.set(new ArrayList<>());
        AtcEngine.refreshPlayerRules();
        AtcEngine.clearAntiSpamState();
        FatigueTracker.clearAll();
    }

    @Test
    void capsToggleChangesUppercaseScoringWhileExclamationsRemainActive() {
        AttractToChatConfig.COMMON.enableCapsFeature.set(true);
        MessageScore caps = new MessageScore("HELLO", UUID.randomUUID());
        assertEquals(5, caps.caps);
        assertEquals("shout", caps.factor);

        AttractToChatConfig.COMMON.enableCapsFeature.set(false);
        MessageScore disabledCaps = new MessageScore("HELLO", UUID.randomUUID());
        assertEquals(0, disabledCaps.caps);
        assertEquals("normal", disabledCaps.factor);

        MessageScore exclamation = new MessageScore("hello!!", UUID.randomUUID());
        assertEquals(2, exclamation.excl);
        assertEquals("shout", exclamation.factor);
    }

    @Test
    void antiSpamEnforcesCooldownAndCanBeDisabled() {
        UUID playerId = UUID.randomUUID();
        AttractToChatConfig.COMMON.enableAntiSpam.set(true);
        AttractToChatConfig.COMMON.scanCooldownTicks.set(40);
        AtcEngine.recordAcceptedScan(playerId);
        assertEquals(2L, AtcEngine.getAntiSpamWaitSeconds(playerId));

        AttractToChatConfig.COMMON.enableAntiSpam.set(false);
        assertEquals(0L, AtcEngine.getAntiSpamWaitSeconds(playerId));
    }

    @Test
    void ignoreAndTrollRulesAreCaseInsensitiveAndGlobalIgnoreWins() {
        AttractToChatConfig.COMMON.ignoredPlayers.set(new ArrayList<>(List.of("Alice")));
        AttractToChatConfig.COMMON.trollPlayers.set(new ArrayList<>(List.of("Bob")));
        AtcEngine.refreshPlayerRules();

        assertTrue(AtcEngine.isIgnoredPlayerName("alice"));
        assertFalse(AtcEngine.isIgnoredPlayerName("bob"));
        assertTrue(AtcEngine.isTrollPlayerName("BOB"));
        assertFalse(AtcEngine.isTrollPlayerName("alice"));

        AttractToChatConfig.COMMON.ignoredPlayers.set(new ArrayList<>(List.of("@a")));
        AtcEngine.refreshPlayerRules();
        assertTrue(AtcEngine.isIgnoredPlayerName("anyone"));
    }

    @Test
    void vocalFatigueAccumulatesAndCanBeReduced() {
        UUID playerId = UUID.randomUUID();
        FatigueTracker.addFatigue(playerId, 200);
        assertTrue(FatigueTracker.getFatigueTicks(playerId) > 150);
        FatigueTracker.reduceFatigue(playerId, 200);
        assertEquals(0, FatigueTracker.getFatigueTicks(playerId));
    }
}
