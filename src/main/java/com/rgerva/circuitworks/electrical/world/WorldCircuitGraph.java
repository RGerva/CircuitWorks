/**
 * Generic Class: WorldCircuitGraph <T>
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class WorldCircuitGraph {

    private final Set<BlockPos> positions;
    private final Map<BlockPos, Set<BlockPos>> adjacency;

    private final Set<WorldCircuitEdge> edges;

    private WorldCircuitGraph(
            Set<BlockPos> positions,
            Map<BlockPos, Set<BlockPos>> adjacency
    ) {
        this.positions =
                Set.copyOf(positions);

        Map<BlockPos, Set<BlockPos>> immutable =
                new HashMap<>();

        for (Map.Entry<BlockPos, Set<BlockPos>> entry
                : adjacency.entrySet()) {

            immutable.put(
                    entry.getKey(),
                    Set.copyOf(entry.getValue())
            );
        }

        this.adjacency =
                Map.copyOf(immutable);

        Set<WorldCircuitEdge> builtEdges =
                new HashSet<>();

        for (Map.Entry<BlockPos, Set<BlockPos>> entry
                : adjacency.entrySet()) {

            for (BlockPos neighbor :
                    entry.getValue()) {

                builtEdges.add(
                        new WorldCircuitEdge(
                                entry.getKey(),
                                neighbor
                        )
                );
            }
        }

        this.edges =
                Set.copyOf(builtEdges);
    }

    Set<WorldCircuitEdge> edges() {
        return edges;
    }

    Set<WorldCircuitEdge> edgesOf(
            BlockPos position
    ) {
        Set<WorldCircuitEdge> result =
                new HashSet<>();

        for (WorldCircuitEdge edge : edges) {
            if (edge.contains(position)) {
                result.add(edge);
            }
        }

        return Set.copyOf(result);
    }

    static WorldCircuitGraph build(
            ElectricalNetworkManager manager,
            ElectricalWorldNetwork worldNetwork
    ) {
        Set<BlockPos> positions =
                new HashSet<>();

        positions.addAll(
                worldNetwork.wires()
        );

        positions.addAll(
                worldNetwork.sources()
        );

        positions.addAll(
                worldNetwork.loads()
        );

        Map<BlockPos, Set<BlockPos>> adjacency =
                new HashMap<>();

        for (BlockPos position : positions) {
            adjacency.put(
                    position,
                    new HashSet<>()
            );
        }

        for (BlockPos current : positions) {
            for (Direction direction :
                    Direction.values()) {

                BlockPos neighbor =
                        current.relative(direction);

                if (!positions.contains(neighbor)) {
                    continue;
                }

                if (!manager.canPhysicallyConnect(
                        current,
                        neighbor,
                        direction
                )) {
                    continue;
                }

                adjacency.get(current)
                        .add(neighbor);

                adjacency.get(neighbor)
                        .add(current);
            }
        }

        return new WorldCircuitGraph(
                positions,
                adjacency
        );
    }

    Set<BlockPos> positions() {
        return positions;
    }

    Set<BlockPos> neighbors(
            BlockPos position
    ) {
        return adjacency.getOrDefault(
                position,
                Set.of()
        );
    }

    int degree(
            BlockPos position
    ) {
        return neighbors(position).size();
    }

    boolean contains(
            BlockPos position
    ) {
        return positions.contains(position);
    }
}
