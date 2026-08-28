/**
 * Generic Class: WorldCircuitOrientation <T>
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

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class WorldCircuitOrientation {

    enum Side {
        POSITIVE,
        NEGATIVE
    }

    private final WorldCircuitGraph graph;

    private final BlockPos sourcePos;
    private final BlockPos positiveAnchor;
    private final BlockPos negativeAnchor;

    private final Map<BlockPos, Integer>
            positiveDistances;

    private final Map<BlockPos, Integer>
            negativeDistances;

    private WorldCircuitOrientation(
            WorldCircuitGraph graph,
            BlockPos sourcePos,
            BlockPos positiveAnchor,
            BlockPos negativeAnchor
    ) {
        this.graph =
                Objects.requireNonNull(graph);

        this.sourcePos =
                Objects.requireNonNull(sourcePos);

        this.positiveAnchor =
                Objects.requireNonNull(positiveAnchor);

        this.negativeAnchor =
                Objects.requireNonNull(negativeAnchor);

        if (!graph.contains(positiveAnchor)
                || !graph.contains(negativeAnchor)) {

            throw new IllegalArgumentException(
                    "Both source terminal anchors must belong to the world circuit graph."
            );
        }

        /*
         * A posição da fonte é removida da busca.
         *
         * Caso contrário o grafo físico permitiria:
         *
         * positive -> SOURCE -> negative
         *
         * como se a própria fonte fosse um fio.
         */
        this.positiveDistances =
                calculateDistances(
                        graph,
                        positiveAnchor,
                        sourcePos
                );

        this.negativeDistances =
                calculateDistances(
                        graph,
                        negativeAnchor,
                        sourcePos
                );
    }

    static WorldCircuitOrientation build(
            WorldCircuitGraph graph,
            BlockPos sourcePos,
            BlockPos positiveAnchor,
            BlockPos negativeAnchor
    ) {
        return new WorldCircuitOrientation(
                graph,
                sourcePos,
                positiveAnchor,
                negativeAnchor
        );
    }

    Side sideOfNeighbor(
            BlockPos wire,
            BlockPos neighbor
    ) {
        if (!graph.neighbors(wire)
                .contains(neighbor)) {

            throw new IllegalArgumentException(
                    "Positions are not physically connected."
            );
        }

        /*
         * Conexão direta com a fonte.
         */
        if (neighbor.equals(sourcePos)) {
            if (wire.equals(positiveAnchor)) {
                return Side.POSITIVE;
            }

            if (wire.equals(negativeAnchor)) {
                return Side.NEGATIVE;
            }

            throw new IllegalStateException(
                    "Wire is connected to an unknown source terminal."
            );
        }

        long wireScore =
                score(wire);

        long neighborScore =
                score(neighbor);

        /*
         * Quanto menor o score,
         * mais próximo eletricamente do positivo.
         */
        if (neighborScore < wireScore) {
            return Side.POSITIVE;
        }

        if (neighborScore > wireScore) {
            return Side.NEGATIVE;
        }

        /*
         * Mesmo nível elétrico.
         *
         * Por enquanto não escolhemos arbitrariamente.
         * Esse caso representa uma conexão transversal
         * que precisará de tratamento próprio depois.
         */
        throw new IllegalStateException(
                "Cannot orient an equipotential world circuit edge."
        );
    }

    private long score(
            BlockPos position
    ) {
        Integer positive =
                positiveDistances.get(position);

        Integer negative =
                negativeDistances.get(position);

        if (positive == null
                || negative == null) {

            throw new IllegalStateException(
                    "Position is not reachable from both source terminals: "
                            + position
            );
        }

        return (long) positive
                - negative;
    }

    private static Map<BlockPos, Integer>
    calculateDistances(
            WorldCircuitGraph graph,
            BlockPos start,
            BlockPos blocked
    ) {
        Map<BlockPos, Integer> distances =
                new HashMap<>();

        ArrayDeque<BlockPos> pending =
                new ArrayDeque<>();

        distances.put(
                start,
                0
        );

        pending.add(start);

        while (!pending.isEmpty()) {
            BlockPos current =
                    pending.removeFirst();

            int currentDistance =
                    distances.get(current);

            for (BlockPos neighbor :
                    graph.neighbors(current)) {

                if (neighbor.equals(blocked)) {
                    continue;
                }

                if (distances.containsKey(neighbor)) {
                    continue;
                }

                distances.put(
                        neighbor,
                        currentDistance + 1
                );

                pending.addLast(neighbor);
            }
        }

        return Map.copyOf(distances);
    }
}