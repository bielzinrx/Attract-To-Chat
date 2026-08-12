package com.bielzinrx.attracttochat.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class DebugCoordinateFormatTest {
    @Test
    void formatsCoordinatesWithoutMappedClassNames() {
        String formatted = AtcEngine.formatCoordinates(new BlockPos(-208, 75, 196));

        assertEquals("(-208, 75, 196)", formatted);
        assertFalse(formatted.contains("class_"));
    }
}
