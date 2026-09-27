package com.bielzinrx.attracttochat.engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AtcEngineDedupeTest {

    @AfterEach
    void resetDedupeState() {
        AtcEngine.onServerStop();
    }

    @Test
    void identicalMessageSameBlockAttractsOnce() {
        UUID player = UUID.randomUUID();
        String key = AtcEngine.attractionDedupeKey("minecraft:overworld",
            new BlockPos(-226, -52, 209), new MessageScore("hello world", player));
        assertTrue(AtcEngine.acceptAttraction(key));
        assertFalse(AtcEngine.acceptAttraction(key));
    }

    @Test
    void differentMessagesSamePlayerAndBlockBothAttract() {
        UUID player = UUID.randomUUID();
        String first = AtcEngine.attractionDedupeKey("minecraft:overworld",
            new BlockPos(0, 64, 0), new MessageScore("hi", player));
        String second = AtcEngine.attractionDedupeKey("minecraft:overworld",
            new BlockPos(0, 64, 0), new MessageScore("hey", player));
        assertNotEquals(first, second);
        assertTrue(AtcEngine.acceptAttraction(first));
        assertTrue(AtcEngine.acceptAttraction(second));
    }

    @Test
    void sameMessageAtDifferentBlocksOrPlayersBothAttract() {
        UUID player = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        String here = AtcEngine.attractionDedupeKey("minecraft:overworld",
            new BlockPos(0, 64, 0), new MessageScore("same text", player));
        String elsewhere = AtcEngine.attractionDedupeKey("minecraft:overworld",
            new BlockPos(5, 64, 5), new MessageScore("same text", player));
        String otherPlayer = AtcEngine.attractionDedupeKey("minecraft:overworld",
            new BlockPos(0, 64, 0), new MessageScore("same text", other));
        assertTrue(AtcEngine.acceptAttraction(here));
        assertTrue(AtcEngine.acceptAttraction(elsewhere));
        assertTrue(AtcEngine.acceptAttraction(otherPlayer));
    }

    @Test
    void serverStopClearsDedupeSoReloadDoesNotBlockAttractions() {
        UUID player = UUID.randomUUID();
        String key = AtcEngine.attractionDedupeKey("minecraft:overworld",
            new BlockPos(10, 70, 10), new MessageScore("reload test", player));
        assertTrue(AtcEngine.acceptAttraction(key));
        AtcEngine.onServerStop();

        assertTrue(AtcEngine.acceptAttraction(key));
    }

    @Test
    void sameMessageAttractsAgainAfterWindowExpires() throws Exception {
        UUID player = UUID.randomUUID();
        String key = AtcEngine.attractionDedupeKey("minecraft:overworld",
            new BlockPos(3, 3, 3), new MessageScore("patience", player));
        assertTrue(AtcEngine.acceptAttraction(key));
        assertFalse(AtcEngine.acceptAttraction(key));

        java.lang.reflect.Field ticks = AtcEngine.class.getDeclaredField("serverTicks");
        ticks.setAccessible(true);
        ticks.setLong(null, 25L);
        assertTrue(AtcEngine.acceptAttraction(key));
    }
}
