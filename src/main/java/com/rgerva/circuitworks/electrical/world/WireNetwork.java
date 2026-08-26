/**
 * Record: WireNetwork
 * Immutable data structure for simplified object representation.
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

import java.util.Set;

public record WireNetwork(
        int id,
        Set<BlockPos> wires
) {

    public WireNetwork {
        wires = Set.copyOf(wires);
    }

    public int size() {
        return wires.size();
    }

    public boolean contains(
            BlockPos pos
    ) {
        return wires.contains(pos);
    }
}