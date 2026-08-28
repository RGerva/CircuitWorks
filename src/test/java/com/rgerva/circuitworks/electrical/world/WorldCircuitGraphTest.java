/**
 * Generic Class: WorldCircuitGraphTest <T>
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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorldCircuitGraphTest {

    @Test
    void parallelWireBranchesShouldBeRepresentedAsGraph() {
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

        assertEquals(
                9,
                graph.positions().size()
        );

        /*
         * SOURCE possui seus dois terminais.
         */
        assertEquals(
                2,
                graph.degree(sourcePos)
        );

        /*
         * Aqui ocorre a divisão:
         *
         * source -> P
         *           |\
         *           A B
         */
        assertEquals(
                3,
                graph.degree(positiveWire)
        );

        assertTrue(
                graph.neighbors(positiveWire)
                        .contains(sourcePos)
        );

        assertTrue(
                graph.neighbors(positiveWire)
                        .contains(a1)
        );

        assertTrue(
                graph.neighbors(positiveWire)
                        .contains(b1)
        );

        /*
         * Aqui ocorre a junção novamente.
         */
        assertEquals(
                3,
                graph.degree(negativeWire)
        );

        assertTrue(
                graph.neighbors(negativeWire)
                        .contains(sourcePos)
        );

        assertTrue(
                graph.neighbors(negativeWire)
                        .contains(a3)
        );

        assertTrue(
                graph.neighbors(negativeWire)
                        .contains(b3)
        );

        /*
         * Interior dos branches.
         */
        assertEquals(
                2,
                graph.degree(a2)
        );

        assertEquals(
                2,
                graph.degree(b2)
        );

        /*
         * Conexões físicas:
         *
         * source-P
         * source-N
         *
         * P-A1
         * A1-A2
         * A2-A3
         * A3-N
         *
         * P-B1
         * B1-B2
         * B2-B3
         * B3-N
         *
         * total = 10
         */
        assertEquals(
                10,
                graph.edges().size()
        );

        /*
         * O wire positivo participa de:
         *
         * source-P
         * P-A1
         * P-B1
         */
        assertEquals(
                3,
                graph.edgesOf(
                        positiveWire
                ).size()
        );

        /*
         * O wire negativo participa de:
         *
         * source-N
         * A3-N
         * B3-N
         */
        assertEquals(
                3,
                graph.edgesOf(
                        negativeWire
                ).size()
        );

        assertTrue(
                graph.edges().contains(
                        new WorldCircuitEdge(
                                positiveWire,
                                a1
                        )
                )
        );

        assertTrue(
                graph.edges().contains(
                        new WorldCircuitEdge(
                                positiveWire,
                                b1
                        )
                )
        );

        assertTrue(
                graph.edges().contains(
                        new WorldCircuitEdge(
                                a3,
                                negativeWire
                        )
                )
        );

        assertTrue(
                graph.edges().contains(
                        new WorldCircuitEdge(
                                b3,
                                negativeWire
                        )
                )
        );
    }
}
