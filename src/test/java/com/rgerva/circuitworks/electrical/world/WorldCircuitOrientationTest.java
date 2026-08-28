/**
 * Generic Class: WorldCircuitOrientationTest <T>
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

import com.rgerva.circuitworks.electrical.component.DCVoltageSourceComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldCircuitOrientationTest {

    @Test
    void parallelBranchesShouldOrientWirePortsFromPositiveToNegative() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos sourcePos =
                new BlockPos(0, 64, 0);

        BlockPos positiveWire =
                new BlockPos(1, 64, 0);

        BlockPos negativeWire =
                new BlockPos(-1, 64, 0);

        BlockPos a1 =
                new BlockPos(1, 64, 1);

        BlockPos a2 =
                new BlockPos(0, 64, 1);

        BlockPos a3 =
                new BlockPos(-1, 64, 1);

        BlockPos b1 =
                new BlockPos(1, 65, 0);

        BlockPos b2 =
                new BlockPos(0, 65, 0);

        BlockPos b3 =
                new BlockPos(-1, 65, 0);

        manager.registerSource(
                sourcePos,
                new DCVoltageSourceComponent(
                        1.0,
                        0.1,
                        100.0
                ),
                Direction.EAST,
                Direction.WEST
        );

        manager.registerWire(positiveWire);

        manager.registerWire(a1);
        manager.registerWire(a2);
        manager.registerWire(a3);

        manager.registerWire(b1);
        manager.registerWire(b2);
        manager.registerWire(b3);

        manager.registerWire(negativeWire);

        ElectricalWorldNetwork worldNetwork =
                manager.getElectricalWorldNetworkAt(
                        sourcePos
                ).orElseThrow();

        WorldCircuitGraph graph =
                WorldCircuitGraph.build(
                        manager,
                        worldNetwork
                );

        WorldCircuitOrientation orientation =
                WorldCircuitOrientation.build(
                        graph,
                        sourcePos,
                        positiveWire,
                        negativeWire
                );

        /*
         * SOURCE -> P -> branch
         */
        assertEquals(
                WorldCircuitOrientation.Side.POSITIVE,
                orientation.sideOfNeighbor(
                        positiveWire,
                        sourcePos
                )
        );

        assertEquals(
                WorldCircuitOrientation.Side.NEGATIVE,
                orientation.sideOfNeighbor(
                        positiveWire,
                        a1
                )
        );

        assertEquals(
                WorldCircuitOrientation.Side.NEGATIVE,
                orientation.sideOfNeighbor(
                        positiveWire,
                        b1
                )
        );

        /*
         * Branch A.
         */
        assertEquals(
                WorldCircuitOrientation.Side.POSITIVE,
                orientation.sideOfNeighbor(
                        a2,
                        a1
                )
        );

        assertEquals(
                WorldCircuitOrientation.Side.NEGATIVE,
                orientation.sideOfNeighbor(
                        a2,
                        a3
                )
        );

        /*
         * Branch B.
         */
        assertEquals(
                WorldCircuitOrientation.Side.POSITIVE,
                orientation.sideOfNeighbor(
                        b2,
                        b1
                )
        );

        assertEquals(
                WorldCircuitOrientation.Side.NEGATIVE,
                orientation.sideOfNeighbor(
                        b2,
                        b3
                )
        );

        /*
         * Merge -> N -> SOURCE
         */
        assertEquals(
                WorldCircuitOrientation.Side.POSITIVE,
                orientation.sideOfNeighbor(
                        negativeWire,
                        a3
                )
        );

        assertEquals(
                WorldCircuitOrientation.Side.POSITIVE,
                orientation.sideOfNeighbor(
                        negativeWire,
                        b3
                )
        );

        assertEquals(
                WorldCircuitOrientation.Side.NEGATIVE,
                orientation.sideOfNeighbor(
                        negativeWire,
                        sourcePos
                )
        );
    }
}