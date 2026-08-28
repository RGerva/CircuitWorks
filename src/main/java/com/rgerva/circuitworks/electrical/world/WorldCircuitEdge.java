/**
 * Generic Class: WorldCircuitEdge <T>
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

import java.util.Objects;

record WorldCircuitEdge(
        BlockPos first,
        BlockPos second
) {

    WorldCircuitEdge {
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);

        if (first.equals(second)) {
            throw new IllegalArgumentException(
                    "World circuit edge cannot connect a position to itself."
            );
        }

        /*
         * Ordem canônica.
         *
         * Assim:
         *
         * A -> B
         *
         * e
         *
         * B -> A
         *
         * representam a mesma aresta.
         */
        if (first.compareTo(second) > 0) {
            BlockPos temporary =
                    first;

            first =
                    second;

            second =
                    temporary;
        }
    }

    boolean contains(
            BlockPos position
    ) {
        return first.equals(position)
                || second.equals(position);
    }

    BlockPos other(
            BlockPos position
    ) {
        if (first.equals(position)) {
            return second;
        }

        if (second.equals(position)) {
            return first;
        }

        throw new IllegalArgumentException(
                "Position does not belong to this world circuit edge."
        );
    }
}