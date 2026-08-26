/**
 * Generic Class: WireTopology <T>
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
import net.minecraft.core.Direction;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class WireTopology {

    private final Set<BlockPos> wires =
            new HashSet<>();

    public boolean registerWire(
            BlockPos pos
    ) {
        return wires.add(
                pos.immutable()
        );
    }

    public boolean unregisterWire(
            BlockPos pos
    ) {
        return wires.remove(pos);
    }

    public boolean containsWire(
            BlockPos pos
    ) {
        return wires.contains(pos);
    }

    public int getWireCount() {
        return wires.size();
    }

    public Set<BlockPos> getWires() {
        return Set.copyOf(wires);
    }

    public Set<BlockPos> getConnectedWires(
            BlockPos start
    ) {
        if (!wires.contains(start)) {
            return Set.of();
        }

        Set<BlockPos> visited =
                new HashSet<>();

        ArrayDeque<BlockPos> pending =
                new ArrayDeque<>();

        visited.add(start);
        pending.add(start);

        while (!pending.isEmpty()) {
            BlockPos current =
                    pending.removeFirst();

            for (Direction direction
                    : Direction.values()) {

                BlockPos neighbor =
                        current.relative(direction);

                if (!wires.contains(neighbor)) {
                    continue;
                }

                if (visited.add(neighbor)) {
                    pending.addLast(neighbor);
                }
            }
        }

        return Set.copyOf(visited);
    }

    public void clear() {
        wires.clear();
    }

    public Set<Set<BlockPos>> getNetworks() {
        Set<Set<BlockPos>> networks =
                new HashSet<>();

        Set<BlockPos> remaining =
                new HashSet<>(wires);

        while (!remaining.isEmpty()) {
            BlockPos start =
                    remaining.iterator().next();

            Set<BlockPos> connected =
                    getConnectedWires(start);

            networks.add(connected);

            remaining.removeAll(connected);
        }

        return Set.copyOf(networks);
    }
}
