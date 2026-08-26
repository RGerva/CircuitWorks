/**
 * Generic Class: WireChunkDataTest <T>
 * A generic structure that works with type parameters.
 *
 * <p>Created by: superuser
 * <p>On: 2026/ago.
 *
 * <p>GitHub: https://github.com/RGerva
 *
 * <p>Copyright (c) 2026 @RGerva.
 *
 * <p>All Rights Reserved.
 */

package com.rgerva.circuitworks.electrical.world;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WireChunkDataTest {

    @Test
    void shouldAddWirePosition() {
        WireChunkData data =
                WireChunkData.empty();

        BlockPos pos =
                new BlockPos(
                        10,
                        64,
                        20
                );

        WireChunkData updated =
                data.withWire(pos);

        assertFalse(
                data.contains(pos)
        );

        assertTrue(
                updated.contains(pos)
        );

        assertEquals(
                1,
                updated.size()
        );
    }

    @Test
    void shouldRemoveWirePosition() {
        BlockPos pos =
                new BlockPos(
                        10,
                        64,
                        20
                );

        WireChunkData data =
                WireChunkData
                        .empty()
                        .withWire(pos);

        WireChunkData updated =
                data.withoutWire(pos);

        assertFalse(
                updated.contains(pos)
        );

        assertEquals(
                0,
                updated.size()
        );
    }
}
