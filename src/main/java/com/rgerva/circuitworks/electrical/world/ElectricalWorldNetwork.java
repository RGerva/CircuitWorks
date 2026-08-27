/**
 * Record: ElectricalWorldNetwork
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

public record ElectricalWorldNetwork(int id, Set<BlockPos> wires, Set<BlockPos> sources, Set<BlockPos> loads) {

    public ElectricalWorldNetwork {
        wires = Set.copyOf(wires);
        sources = Set.copyOf(sources);
        loads = Set.copyOf(loads);
    }

    public int getWireCount() {
        return wires.size();
    }

    public int getSourceCount() {
        return sources.size();
    }

    public int getLoadCount() {
        return loads.size();
    }

    public int size() {
        return wires.size() + sources.size() + loads.size();
    }

    public boolean contains(BlockPos pos) {
        return wires.contains(pos)
                || sources.contains(pos)
                || loads.contains(pos);
    }
}
