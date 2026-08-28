/**
 * Generic Class: WorldElectricalNetworkBuilderTest <T>
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
import com.rgerva.circuitworks.electrical.network.ElectricalNetwork;
import com.rgerva.circuitworks.electrical.network.ElectricalNetworkResult;
import com.rgerva.circuitworks.electrical.network.ElectricalNetworkStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorldElectricalNetworkBuilderTest {

    private static final double DELTA =
            1.0E-9;

    @Test
    void parallelWireBranchesShouldBuildAndSolveElectricalNetwork() {
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

        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        1.0,
                        0.1,
                        100.0
                );

        manager.registerSource(
                sourcePos,
                source,
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

        ElectricalNetwork network =
                WorldElectricalNetworkBuilder.build(
                        manager,
                        worldNetwork,
                        sourcePos,
                        positiveWire,
                        negativeWire
                );

        ElectricalNetworkResult result =
                network.solve();

        /*
         * P = 0.01
         *
         * branch A = 0.03
         * branch B = 0.03
         *
         * 0.03 || 0.03 = 0.015
         *
         * N = 0.01
         *
         * Req = 0.035 ohm
         */
        double expectedEquivalentResistance =
                0.035;

        double expectedTotalCurrent =
                1.0 / (
                        expectedEquivalentResistance
                                + 0.1
                );

        assertEquals(
                ElectricalNetworkStatus.ACTIVE,
                result.status()
        );

        assertEquals(
                expectedEquivalentResistance,
                network.getEquivalentResistance(),
                DELTA
        );

        assertEquals(
                expectedTotalCurrent,
                source.getElectricalState().current(),
                DELTA
        );

        /*
         * Antes e depois da divisão:
         * corrente total.
         */
        assertEquals(
                expectedTotalCurrent,
                manager.getWireComponent(
                                positiveWire
                        ).orElseThrow()
                        .getElectricalState()
                        .current(),
                DELTA
        );

        assertEquals(
                expectedTotalCurrent,
                manager.getWireComponent(
                                negativeWire
                        ).orElseThrow()
                        .getElectricalState()
                        .current(),
                DELTA
        );

        /*
         * Branches iguais:
         * metade da corrente em cada um.
         */
        double expectedBranchCurrent =
                expectedTotalCurrent / 2.0;

        assertEquals(
                expectedBranchCurrent,
                manager.getWireComponent(a2)
                        .orElseThrow()
                        .getElectricalState()
                        .current(),
                DELTA
        );

        assertEquals(
                expectedBranchCurrent,
                manager.getWireComponent(b2)
                        .orElseThrow()
                        .getElectricalState()
                        .current(),
                DELTA
        );
    }
}