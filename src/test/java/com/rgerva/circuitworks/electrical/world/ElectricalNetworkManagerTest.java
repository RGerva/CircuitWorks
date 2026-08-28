/**
 * Generic Class: ElectricalNetworkManagerTest <T>
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

import com.rgerva.circuitworks.electrical.api.ElectricalState;
import com.rgerva.circuitworks.electrical.component.ComponentOperationalStatus;
import com.rgerva.circuitworks.electrical.component.DCVoltageSourceComponent;
import com.rgerva.circuitworks.electrical.component.ResistiveLoadComponent;
import com.rgerva.circuitworks.electrical.component.WireComponent;

import com.rgerva.circuitworks.electrical.network.ElectricalConnection;
import com.rgerva.circuitworks.electrical.network.ElectricalNetwork;
import com.rgerva.circuitworks.electrical.network.ElectricalNetworkResult;
import com.rgerva.circuitworks.electrical.network.ElectricalNetworkStatus;
import com.rgerva.circuitworks.electrical.thermal.ThermalProperties;
import com.rgerva.circuitworks.electrical.thermal.ThermalStatus;
import net.minecraft.core.BlockPos;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElectricalNetworkManagerTest {

    @Test
    void registeringWireShouldCreateElectricalComponent() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos pos =
                new BlockPos(
                        0,
                        64,
                        0
                );

        manager.registerWire(pos);

        assertEquals(
                1,
                manager.getWireCount()
        );

        assertEquals(
                1,
                manager.getWireComponentCount()
        );

        assertTrue(
                manager.getWireComponent(pos)
                        .isPresent()
        );
    }

    @Test
    void registeringSameWireTwiceShouldPreserveComponent() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos pos =
                new BlockPos(
                        0,
                        64,
                        0
                );

        manager.registerWire(pos);

        WireComponent first =
                manager.getWireComponent(pos)
                        .orElseThrow();

        manager.registerWire(pos);

        WireComponent second =
                manager.getWireComponent(pos)
                        .orElseThrow();

        assertSame(
                first,
                second
        );

        assertEquals(
                1,
                manager.getWireCount()
        );

        assertEquals(
                1,
                manager.getWireComponentCount()
        );
    }

    @Test
    void mergingNetworksShouldPreserveExistingWireComponents() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos first =
                new BlockPos(
                        0,
                        64,
                        0
                );

        BlockPos bridge =
                new BlockPos(
                        1,
                        64,
                        0
                );

        BlockPos last =
                new BlockPos(
                        2,
                        64,
                        0
                );

        /*
         * Inicialmente temos duas redes.
         */
        manager.registerWire(first);
        manager.registerWire(last);

        assertEquals(
                2,
                manager.getNetworkCount()
        );

        WireComponent firstComponent =
                manager.getWireComponent(first)
                        .orElseThrow();

        WireComponent lastComponent =
                manager.getWireComponent(last)
                        .orElseThrow();

        /*
         * Adiciona o bridge.
         *
         * As redes passam a ser uma só.
         */
        manager.registerWire(bridge);

        assertEquals(
                1,
                manager.getNetworkCount()
        );

        /*
         * Mas os componentes antigos continuam
         * sendo exatamente os mesmos objetos.
         */
        assertSame(
                firstComponent,
                manager.getWireComponent(first)
                        .orElseThrow()
        );

        assertSame(
                lastComponent,
                manager.getWireComponent(last)
                        .orElseThrow()
        );

        assertEquals(
                3,
                manager.getWireComponentCount()
        );
    }

    @Test
    void removingWireShouldRemoveElectricalComponent() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos pos =
                new BlockPos(
                        0,
                        64,
                        0
                );

        manager.registerWire(pos);

        assertTrue(
                manager.getWireComponent(pos)
                        .isPresent()
        );

        manager.unregisterWire(pos);

        assertFalse(
                manager.getWireComponent(pos)
                        .isPresent()
        );

        assertEquals(
                0,
                manager.getWireComponentCount()
        );
    }

    @Test
    void shouldRegisterVoltageSource() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos pos =
                new BlockPos(
                        0,
                        64,
                        0
                );

        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        10.0
                );

        manager.registerSource(
                pos,
                source,
                Direction.EAST,
                Direction.WEST
        );
        assertEquals(
                1,
                manager.getSourceCount()
        );

        assertSame(
                source,
                manager.getSource(pos)
                        .orElseThrow()
        );
    }

    @Test
    void shouldUnregisterVoltageSource() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos pos =
                new BlockPos(
                        0,
                        64,
                        0
                );

        manager.registerSource(
                pos,
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        10.0
                ),
                Direction.EAST,
                Direction.WEST
        );

        assertTrue(
                manager.unregisterSource(pos)
        );

        assertEquals(
                0,
                manager.getSourceCount()
        );

        assertTrue(
                manager.getSource(pos)
                        .isEmpty()
        );
    }

    @Test
    void sourceAloneShouldCreateElectricalWorldNetwork() {
        ElectricalNetworkManager manager = new ElectricalNetworkManager();

        BlockPos sourcePos = new BlockPos(0, 64, 0);

        manager.registerSource(
                sourcePos,
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        10.0
                ),
                Direction.EAST,
                Direction.WEST
        );

        assertEquals(
                1,
                manager.getElectricalWorldNetworkCount()
        );

        ElectricalWorldNetwork network =
                manager.getElectricalWorldNetworkAt(sourcePos)
                        .orElseThrow();

        assertEquals(0, network.getWireCount());
        assertEquals(1, network.getSourceCount());
        assertEquals(1, network.size());
    }

    @Test
    void adjacentSourceAndWireShouldShareElectricalWorldNetwork() {
        ElectricalNetworkManager manager = new ElectricalNetworkManager();

        BlockPos sourcePos = new BlockPos(0, 64, 0);
        BlockPos wirePos = new BlockPos(1, 64, 0);

        manager.registerSource(
                sourcePos,
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        10.0
                ),
                Direction.EAST,
                Direction.WEST
        );

        manager.registerWire(wirePos);

        assertEquals(
                1,
                manager.getElectricalWorldNetworkCount()
        );

        ElectricalWorldNetwork network =
                manager.getElectricalWorldNetworkAt(sourcePos)
                        .orElseThrow();

        assertTrue(network.contains(sourcePos));
        assertTrue(network.contains(wirePos));

        assertEquals(1, network.getSourceCount());
        assertEquals(1, network.getWireCount());
        assertEquals(2, network.size());
    }

    @Test
    void distantSourceAndWireShouldCreateSeparateElectricalWorldNetworks() {
        ElectricalNetworkManager manager = new ElectricalNetworkManager();

        BlockPos sourcePos = new BlockPos(0, 64, 0);
        BlockPos wirePos = new BlockPos(10, 64, 0);

        manager.registerSource(
                sourcePos,
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        10.0
                ),
                Direction.EAST,
                Direction.WEST
        );

        manager.registerWire(wirePos);

        assertEquals(
                2,
                manager.getElectricalWorldNetworkCount()
        );
    }

    @Test
    void sourceShouldPhysicallyJoinTwoWireNetworks() {
        ElectricalNetworkManager manager = new ElectricalNetworkManager();

        BlockPos leftWire = new BlockPos(-1, 64, 0);
        BlockPos sourcePos = new BlockPos(0, 64, 0);
        BlockPos rightWire = new BlockPos(1, 64, 0);

        manager.registerWire(leftWire);
        manager.registerWire(rightWire);

        assertEquals(
                2,
                manager.getNetworkCount()
        );

        manager.registerSource(
                sourcePos,
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        10.0
                ),
                Direction.EAST,
                Direction.WEST
        );

        /*
         * Continuam sendo duas redes exclusivamente
         * de fios.
         */
        assertEquals(
                2,
                manager.getNetworkCount()
        );

        /*
         * Mas fisicamente todos fazem parte de
         * uma única rede elétrica do mundo.
         */
        assertEquals(
                1,
                manager.getElectricalWorldNetworkCount()
        );

        ElectricalWorldNetwork network =
                manager.getElectricalWorldNetworkAt(sourcePos)
                        .orElseThrow();

        assertEquals(2, network.getWireCount());
        assertEquals(1, network.getSourceCount());
        assertEquals(3, network.size());
    }

    @Test
    void wireOnPositiveTerminalShouldConnectToSource() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos sourcePos =
                new BlockPos(0, 64, 0);

        BlockPos wirePos =
                sourcePos.east();

        manager.registerSource(
                sourcePos,
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        10.0
                ),
                Direction.EAST,
                Direction.WEST
        );

        manager.registerWire(wirePos);

        assertEquals(
                1,
                manager.getElectricalWorldNetworkCount()
        );

        ElectricalWorldNetwork network =
                manager.getElectricalWorldNetworkAt(sourcePos)
                        .orElseThrow();

        assertEquals(1, network.getSourceCount());
        assertEquals(1, network.getWireCount());
    }

    @Test
    void wireOnNegativeTerminalShouldConnectToSource() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos sourcePos =
                new BlockPos(0, 64, 0);

        BlockPos wirePos =
                sourcePos.west();

        manager.registerSource(
                sourcePos,
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        10.0
                ),
                Direction.EAST,
                Direction.WEST
        );

        manager.registerWire(wirePos);

        assertEquals(
                1,
                manager.getElectricalWorldNetworkCount()
        );
    }

    @Test
    void wireOnSourceSideShouldNotConnect() {
        ElectricalNetworkManager manager = new ElectricalNetworkManager();

        BlockPos sourcePos = new BlockPos(0, 64, 0);

        BlockPos sideWire = sourcePos.north();

        manager.registerSource(sourcePos,
                new DCVoltageSourceComponent(12.0, 0.1, 10.0),
                Direction.EAST, Direction.WEST);

        manager.registerWire(sideWire);

        assertEquals(2, manager.getElectricalWorldNetworkCount());
    }

    @Test
    void wiresOnBothSourceTerminalsShouldShareWorldNetwork() {
        ElectricalNetworkManager manager = new ElectricalNetworkManager();

        BlockPos sourcePos = new BlockPos(0, 64, 0);

        BlockPos negativeWire = sourcePos.west();

        BlockPos positiveWire = sourcePos.east();

        manager.registerWire(negativeWire);
        manager.registerWire(positiveWire);

        assertEquals(2, manager.getNetworkCount());

        manager.registerSource(sourcePos,
                new DCVoltageSourceComponent(12.0, 0.1, 10.0),
                Direction.EAST, Direction.WEST);

        /*
         * Os wires ainda não encostam diretamente,
         * então continuam sendo 2 WireNetworks.
         */
        assertEquals(2, manager.getNetworkCount());

        /*
         * Mas pertencem à mesma rede elétrica
         * física através da source.
         */
        assertEquals(1, manager.getElectricalWorldNetworkCount());

        ElectricalWorldNetwork network = manager.getElectricalWorldNetworkAt(sourcePos).orElseThrow();

        assertEquals(2, network.getWireCount());
        assertEquals(1, network.getSourceCount());
    }

    @Test
    void sourceWithOnlyOneTerminalConnectedShouldBeOpenCircuit() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos sourcePos = new BlockPos(0, 64, 0);
        BlockPos positiveWire = sourcePos.east();

        manager.registerSource(
                sourcePos,
                new DCVoltageSourceComponent(12.0, 0.1, 10.0),
                Direction.EAST,
                Direction.WEST
        );

        manager.registerWire(positiveWire);

        ElectricalWorldNetwork worldNetwork =
                manager.getElectricalWorldNetworkAt(sourcePos)
                        .orElseThrow();

        WorldCircuitResult result =
                manager.resolveWorldCircuit(worldNetwork);

        assertEquals(
                WorldCircuitStatus.OPEN_CIRCUIT,
                result.status()
        );

        assertEquals(
                ElectricalNetworkStatus.OPEN_CIRCUIT,
                result.electricalResult()
                        .orElseThrow()
                        .status()
        );
    }

    private static final double DELTA = 1.0E-9;

    @Test
    void closedWirePathShouldBeConvertedToElectricalNetwork() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos sourcePos = new BlockPos(0, 64, 0);

        manager.registerSource(
                sourcePos,
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        100.0
                ),
                Direction.EAST,
                Direction.WEST
        );

        manager.registerWire(new BlockPos(1, 64, 0));
        manager.registerWire(new BlockPos(1, 64, 1));
        manager.registerWire(new BlockPos(0, 64, 1));
        manager.registerWire(new BlockPos(-1, 64, 1));
        manager.registerWire(new BlockPos(-1, 64, 0));

        ElectricalWorldNetwork worldNetwork =
                manager.getElectricalWorldNetworkAt(sourcePos)
                        .orElseThrow();

        WorldCircuitResult result =
                manager.resolveWorldCircuit(worldNetwork);

        assertEquals(
                WorldCircuitStatus.SOLVED,
                result.status()
        );

        assertEquals(
                5,
                result.componentPath().size()
        );

        ElectricalNetworkResult electrical =
                result.electricalResult()
                        .orElseThrow();

        /*
         * Cada wire:
         * 0.01 ohm
         *
         * 5 wires:
         * Req externo = 0.05 ohm
         */
        assertEquals(
                0.05,
                electrical.equivalentResistance(),
                DELTA
        );

        /*
         * Total:
         *
         * 0.05 external
         * +
         * 0.10 source internal
         *
         * = 0.15 ohm
         *
         * I = 12 / 0.15
         * I = 80 A
         */
        assertEquals(
                80.0,
                electrical.state().current(),
                DELTA
        );
    }

    @Test
    void branchedWirePathShouldBeSolved() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos sourcePos =
                new BlockPos(0, 64, 0);

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

        /*
         *                    ┌── A1 ─ A2 ─ A3 ──┐
         * SOURCE + ── P ─────┤                  ├──── N ── SOURCE -
         *                    └── B1 ─ B2 ─ B3 ──┘
         */

        BlockPos positiveWire =
                new BlockPos(1, 64, 0);

        BlockPos negativeWire =
                new BlockPos(-1, 64, 0);

        /*
         * Branch A.
         */
        BlockPos a1 =
                new BlockPos(1, 64, 1);

        BlockPos a2 =
                new BlockPos(0, 64, 1);

        BlockPos a3 =
                new BlockPos(-1, 64, 1);

        /*
         * Branch B.
         */
        BlockPos b1 =
                new BlockPos(1, 65, 0);

        BlockPos b2 =
                new BlockPos(0, 65, 0);

        BlockPos b3 =
                new BlockPos(-1, 65, 0);

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

        WorldCircuitResult result =
                manager.resolveWorldCircuit(
                        worldNetwork
                );

        assertEquals(
                1,
                worldNetwork.getSourceCount()
        );

        assertEquals(
                8,
                worldNetwork.getWireCount()
        );

        assertEquals(
                WorldCircuitStatus.SOLVED,
                result.status()
        );

        assertTrue(
                result.electricalResult().isPresent()
        );
    }

    @Test
    void registeringLoadShouldCreateWorldLoadNode() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos pos = new BlockPos(0, 64, 0);

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(10.0);

        manager.registerLoad(
                pos,
                load,
                Direction.EAST,
                Direction.WEST
        );

        assertEquals(1, manager.getLoadCount());

        assertSame(load, manager.getLoad(pos).orElseThrow());

        WorldLoadNode node = manager.getLoadNode(pos).orElseThrow();

        assertEquals(Direction.EAST, node.terminalADirection());
        assertEquals(Direction.WEST, node.terminalBDirection());

        assertTrue(node.hasTerminal(Direction.EAST));
        assertTrue(node.hasTerminal(Direction.WEST));
        assertFalse(node.hasTerminal(Direction.NORTH));
    }

    @Test
    void removingLoadShouldRemoveWorldLoadNode() {
        ElectricalNetworkManager manager = new ElectricalNetworkManager();

        BlockPos pos = new BlockPos(0, 64, 0);

        manager.registerLoad(pos, new ResistiveLoadComponent(10.0),
                Direction.EAST, Direction.WEST);

        assertTrue(manager.unregisterLoad(pos));

        assertEquals(0, manager.getLoadCount());
        assertTrue(manager.getLoad(pos).isEmpty());
        assertTrue(manager.getLoadNode(pos).isEmpty());
    }

    @Test
    void wireOnLoadTerminalShouldShareWorldNetwork() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos loadPos = new BlockPos(0, 64, 0);
        BlockPos wirePos = loadPos.east();

        manager.registerLoad(
                loadPos,
                new ResistiveLoadComponent(10.0),
                Direction.EAST,
                Direction.WEST
        );

        manager.registerWire(wirePos);

        assertEquals(
                1,
                manager.getElectricalWorldNetworkCount()
        );

        ElectricalWorldNetwork network =
                manager.getElectricalWorldNetworkAt(loadPos)
                        .orElseThrow();

        assertEquals(1, network.getLoadCount());
        assertEquals(1, network.getWireCount());
        assertEquals(0, network.getSourceCount());
    }

    @Test
    void wireOnLoadSideShouldNotConnect() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos loadPos = new BlockPos(0, 64, 0);
        BlockPos sideWire = loadPos.north();

        manager.registerLoad(
                loadPos,
                new ResistiveLoadComponent(10.0),
                Direction.EAST,
                Direction.WEST
        );

        manager.registerWire(sideWire);

        assertEquals(
                2,
                manager.getElectricalWorldNetworkCount()
        );
    }

    @Test
    void sourceWireAndLoadShouldShareWorldNetwork() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos sourcePos = new BlockPos(0, 64, 0);
        BlockPos wirePos = new BlockPos(0, 64, 1);
        BlockPos loadPos = new BlockPos(0, 64, 2);

        manager.registerSource(
                sourcePos,
                new DCVoltageSourceComponent(12.0, 0.1, 10.0),
                Direction.SOUTH,
                Direction.NORTH
        );

        manager.registerWire(wirePos);

        manager.registerLoad(
                loadPos,
                new ResistiveLoadComponent(10.0),
                Direction.SOUTH,
                Direction.NORTH
        );

        assertEquals(
                1,
                manager.getElectricalWorldNetworkCount()
        );

        ElectricalWorldNetwork network =
                manager.getElectricalWorldNetworkAt(sourcePos)
                        .orElseThrow();

        assertEquals(1, network.getSourceCount());
        assertEquals(1, network.getWireCount());
        assertEquals(1, network.getLoadCount());
        assertEquals(3, network.size());
    }

    @Test
    void closedCircuitWithResistiveLoadShouldBeActive() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos sourcePos = new BlockPos(0, 64, 0);
        BlockPos loadPos = new BlockPos(2, 64, 0);

        manager.registerSource(
                sourcePos,
                new DCVoltageSourceComponent(12.0, 0.1, 10.0),
                Direction.SOUTH,
                Direction.NORTH
        );

        manager.registerLoad(
                loadPos,
                new ResistiveLoadComponent(10.0),
                Direction.SOUTH,
                Direction.NORTH
        );

        manager.registerWire(new BlockPos(0, 64, 1));
        manager.registerWire(new BlockPos(1, 64, 1));
        manager.registerWire(new BlockPos(2, 64, 1));

        manager.registerWire(new BlockPos(0, 64, -1));
        manager.registerWire(new BlockPos(1, 64, -1));
        manager.registerWire(new BlockPos(2, 64, -1));

        ElectricalWorldNetwork worldNetwork =
                manager.getElectricalWorldNetworkAt(sourcePos)
                        .orElseThrow();

        assertEquals(1, worldNetwork.getSourceCount());
        assertEquals(1, worldNetwork.getLoadCount());
        assertEquals(6, worldNetwork.getWireCount());

        WorldCircuitResult result =
                manager.resolveWorldCircuit(worldNetwork);

        assertEquals(
                WorldCircuitStatus.SOLVED,
                result.status()
        );

        assertEquals(
                7,
                result.componentPath().size()
        );

        ElectricalNetworkResult electrical =
                result.electricalResult()
                        .orElseThrow();

        assertEquals(
                ElectricalNetworkStatus.ACTIVE,
                electrical.status()
        );

        assertEquals(
                10.06,
                electrical.equivalentResistance(),
                DELTA
        );

        assertEquals(
                12.0 / 10.16,
                electrical.state().current(),
                DELTA
        );

        assertTrue(
                electrical.faults().isEmpty()
        );
    }

    @Test
    void tickingClosedCircuitShouldUpdateElectricalAndThermalState() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos sourcePos = new BlockPos(0, 64, 0);
        BlockPos loadPos = new BlockPos(2, 64, 0);

        manager.registerSource(
                sourcePos,
                new DCVoltageSourceComponent(12.0, 0.1, 10.0),
                Direction.SOUTH,
                Direction.NORTH
        );

        manager.registerLoad(
                loadPos,
                new ResistiveLoadComponent(10.0),
                Direction.SOUTH,
                Direction.NORTH
        );

        manager.registerWire(new BlockPos(0, 64, 1));
        manager.registerWire(new BlockPos(1, 64, 1));
        manager.registerWire(new BlockPos(2, 64, 1));

        manager.registerWire(new BlockPos(0, 64, -1));
        manager.registerWire(new BlockPos(1, 64, -1));
        manager.registerWire(new BlockPos(2, 64, -1));

        WireComponent wire = manager.getWireComponent(
                new BlockPos(0, 64, 1)
        ).orElseThrow();

        double initialTemperature =
                wire.getThermalState().temperatureCelsius();

        manager.tickSimulation(
                20.0,
                0.05
        );

        assertEquals(
                12.0 / 10.16,
                wire.getElectricalState().current(),
                DELTA
        );

        assertTrue(
                wire.getThermalState().temperatureCelsius()
                        > initialTemperature
        );
    }

    @Test
    void openingCircuitShouldClearPreviousCurrent() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos sourcePos = new BlockPos(0, 64, 0);
        BlockPos loadPos = new BlockPos(2, 64, 0);

        BlockPos wireToRemove =
                new BlockPos(1, 64, 1);

        manager.registerSource(
                sourcePos,
                new DCVoltageSourceComponent(12.0, 0.1, 10.0),
                Direction.SOUTH,
                Direction.NORTH
        );

        manager.registerLoad(
                loadPos,
                new ResistiveLoadComponent(10.0),
                Direction.SOUTH,
                Direction.NORTH
        );

        manager.registerWire(new BlockPos(0, 64, 1));
        manager.registerWire(wireToRemove);
        manager.registerWire(new BlockPos(2, 64, 1));

        manager.registerWire(new BlockPos(0, 64, -1));
        manager.registerWire(new BlockPos(1, 64, -1));
        manager.registerWire(new BlockPos(2, 64, -1));

        WireComponent remainingWire =
                manager.getWireComponent(
                        new BlockPos(0, 64, 1)
                ).orElseThrow();

        manager.tickSimulation(20.0, 0.05);

        assertTrue(
                remainingWire.getElectricalState().current() > 0.0
        );

        manager.unregisterWire(wireToRemove);

        manager.tickSimulation(20.0, 0.05);

        assertEquals(
                0.0,
                remainingWire.getElectricalState().current(),
                DELTA
        );
    }

    @Test
    void failedSourceShouldStopCircuitCurrent() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos sourcePos = new BlockPos(0, 64, 0);

        manager.registerSource(
                sourcePos,
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        100.0
                ),
                Direction.EAST,
                Direction.WEST
        );

        manager.registerWire(new BlockPos(1, 64, 0));
        manager.registerWire(new BlockPos(1, 64, 1));
        manager.registerWire(new BlockPos(0, 64, 1));
        manager.registerWire(new BlockPos(-1, 64, 1));
        manager.registerWire(new BlockPos(-1, 64, 0));

        DCVoltageSourceComponent source =
                manager.getSource(sourcePos)
                        .orElseThrow();

        int safetyCounter = 0;

        while (source.isOperational()
                && safetyCounter < 10000) {

            manager.tickSimulation(
                    20.0,
                    0.05
            );

            safetyCounter++;
        }

        assertFalse(source.isOperational());

        manager.tickSimulation(
                20.0,
                0.05
        );

        assertEquals(
                0.0,
                source.getElectricalState().current(),
                DELTA
        );
    }

    @Test
    void wireShouldFailBeforeLowResistanceSource() {
        ElectricalNetworkManager manager = new ElectricalNetworkManager();

        BlockPos sourcePos = new BlockPos(0, 64, 0);

        manager.registerSource(
                sourcePos,
                new DCVoltageSourceComponent(12.0, 0.001, 1000.0),
                Direction.EAST,
                Direction.WEST
        );

        manager.registerWire(new BlockPos(1, 64, 0));
        manager.registerWire(new BlockPos(1, 64, 1));
        manager.registerWire(new BlockPos(0, 64, 1));
        manager.registerWire(new BlockPos(-1, 64, 1));
        manager.registerWire(new BlockPos(-1, 64, 0));

        WireComponent wire = manager.getWireComponent(
                new BlockPos(1, 64, 0)
        ).orElseThrow();

        DCVoltageSourceComponent source = manager.getSource(sourcePos)
                .orElseThrow();

        int ticks = 0;

        while (wire.isOperational() && ticks < 4000) {
            manager.tickSimulation(20.0, 0.05);
            ticks++;
        }

        assertFalse(wire.isOperational());
        assertTrue(source.isOperational());

        assertEquals(
                ComponentOperationalStatus.FAILED,
                wire.getOperationalStatus()
        );

        assertEquals(
                ComponentOperationalStatus.OPERATIONAL,
                source.getOperationalStatus()
        );
    }

    @Test
    void registeringPersistedHotWireShouldRestoreTemperature() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos pos = new BlockPos(0, 64, 0);

        manager.registerWire(
                pos,
                new WirePersistentState(
                        85.0,
                        ComponentOperationalStatus.OPERATIONAL
                )
        );

        WireComponent wire =
                manager.getWireComponent(pos)
                        .orElseThrow();

        assertEquals(
                85.0,
                wire.getThermalState().temperatureCelsius(),
                DELTA
        );

        assertEquals(
                ThermalStatus.HOT,
                wire.getThermalStatus()
        );

        assertTrue(wire.isOperational());
    }

    @Test
    void registeringPersistedFailedWireShouldRemainFailed() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos pos = new BlockPos(0, 64, 0);

        manager.registerWire(
                pos,
                new WirePersistentState(
                        150.0,
                        ComponentOperationalStatus.FAILED
                )
        );

        WireComponent wire =
                manager.getWireComponent(pos)
                        .orElseThrow();

        assertFalse(wire.isOperational());

        assertEquals(
                ComponentOperationalStatus.FAILED,
                wire.getOperationalStatus()
        );

        assertEquals(
                150.0,
                wire.getThermalState().temperatureCelsius(),
                DELTA
        );

        assertEquals(
                0.0,
                wire.getElectricalState().current(),
                DELTA
        );
    }

    @Test
    void disconnectedResistiveLoadShouldCoolTowardAmbientTemperature() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(
                        10.0,
                        ResistiveLoadComponent.DEFAULT_THERMAL_PROPERTIES,
                        ResistiveLoadComponent.DEFAULT_THERMAL_LIMITS,
                        80.0,
                        ComponentOperationalStatus.OPERATIONAL
                );

        manager.registerLoad(
                BlockPos.ZERO,
                load,
                Direction.NORTH,
                Direction.SOUTH
        );

        double before =
                load.getThermalState()
                        .temperatureCelsius();

        manager.tickSimulation(
                20.0,
                1.0
        );

        double after =
                load.getThermalState()
                        .temperatureCelsius();

        assertTrue(after < before);
        assertTrue(after > 20.0);
        assertEquals(
                ElectricalState.ZERO,
                load.getElectricalState()
        );
    }

    @Test
    void twoResistiveLoadsInSeriesShouldShareSameCurrent() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        10.0
                );

        ResistiveLoadComponent load1 =
                new ResistiveLoadComponent(10.0);

        ResistiveLoadComponent load2 =
                new ResistiveLoadComponent(20.0);

        BlockPos sourcePos = new BlockPos(0, 0, 0);
        BlockPos load1Pos = new BlockPos(1, 0, 0);
        BlockPos load2Pos = new BlockPos(2, 0, 0);

        manager.registerSource(
                sourcePos,
                source,
                Direction.EAST,
                Direction.WEST
        );

        manager.registerLoad(
                load1Pos,
                load1,
                Direction.WEST,
                Direction.EAST
        );

        manager.registerLoad(
                load2Pos,
                load2,
                Direction.WEST,
                Direction.EAST
        );

        /*
         * SOURCE + -> LOAD1 -> LOAD2
         *                       |
         *                       W
         *                       |
         * SOURCE - <- W <- W <- W
         */
        manager.registerWire(new BlockPos(3, 0, 0));
        manager.registerWire(new BlockPos(3, 0, 1));
        manager.registerWire(new BlockPos(2, 0, 1));
        manager.registerWire(new BlockPos(1, 0, 1));
        manager.registerWire(new BlockPos(0, 0, 1));
        manager.registerWire(new BlockPos(-1, 0, 1));
        manager.registerWire(new BlockPos(-1, 0, 0));

        manager.tickSimulation(
                20.0,
                0.05
        );

        double wireResistance =
                7 * 0.01;

        double externalResistance =
                10.0
                        + 20.0
                        + wireResistance;

        double totalResistance =
                externalResistance
                        + 0.1;

        double expectedCurrent =
                12.0 / totalResistance;

        assertEquals(
                expectedCurrent,
                source.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                expectedCurrent,
                load1.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                expectedCurrent,
                load2.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                expectedCurrent * 10.0,
                load1.getElectricalState().voltage(),
                DELTA
        );

        assertEquals(
                expectedCurrent * 20.0,
                load2.getElectricalState().voltage(),
                DELTA
        );
    }

    @Test
    void changingLoadResistanceAtRuntimeShouldRecalculateCircuit() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        10.0
                );

        ResistiveLoadComponent load1 =
                new ResistiveLoadComponent(10.0);

        ResistiveLoadComponent load2 =
                new ResistiveLoadComponent(10.0);

        manager.registerSource(
                new BlockPos(0, 0, 0),
                source,
                Direction.EAST,
                Direction.WEST
        );

        manager.registerLoad(
                new BlockPos(1, 0, 0),
                load1,
                Direction.WEST,
                Direction.EAST
        );

        manager.registerLoad(
                new BlockPos(2, 0, 0),
                load2,
                Direction.WEST,
                Direction.EAST
        );

        manager.registerWire(new BlockPos(3, 0, 0));
        manager.registerWire(new BlockPos(3, 0, 1));
        manager.registerWire(new BlockPos(2, 0, 1));
        manager.registerWire(new BlockPos(1, 0, 1));
        manager.registerWire(new BlockPos(0, 0, 1));
        manager.registerWire(new BlockPos(-1, 0, 1));
        manager.registerWire(new BlockPos(-1, 0, 0));

        /*
         * Estado inicial:
         *
         * 10 + 10 + 0.07 + 0.10
         * = 20.17 ohm
         */
        manager.tickSimulation(
                20.0,
                0.05
        );

        double initialCurrent =
                12.0 / 20.17;

        assertEquals(
                initialCurrent,
                source.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                initialCurrent,
                load1.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                initialCurrent,
                load2.getElectricalState().current(),
                DELTA
        );

        /*
         * Alteração em runtime.
         *
         * Não removemos o load.
         * Não registramos novamente.
         * Não rebuildamos a topology.
         */
        load2.setResistance(20.0);

        manager.tickSimulation(
                20.0,
                0.05
        );

        /*
         * Novo estado:
         *
         * 10 + 20 + 0.07 + 0.10
         * = 30.17 ohm
         */
        double updatedCurrent =
                12.0 / 30.17;

        assertEquals(
                updatedCurrent,
                source.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                updatedCurrent,
                load1.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                updatedCurrent,
                load2.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                updatedCurrent * 10.0,
                load1.getElectricalState().voltage(),
                DELTA
        );

        assertEquals(
                updatedCurrent * 20.0,
                load2.getElectricalState().voltage(),
                DELTA
        );
    }

    @Test
    void twoParallelLoadsShouldSplitCurrent() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        10.0
                );

        ResistiveLoadComponent load1 =
                new ResistiveLoadComponent(10.0);

        ResistiveLoadComponent load2 =
                new ResistiveLoadComponent(20.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(load1);
        network.addComponent(load2);

        /*
         *          ┌── 10 ohm ──┐
         * SOURCE + ┤             ├ SOURCE -
         *          └── 20 ohm ──┘
         */
        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        load1.getPorts().get(0)
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        load2.getPorts().get(0)
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load1.getPorts().get(1),
                        source.getNegativeTerminal()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load2.getPorts().get(1),
                        source.getNegativeTerminal()
                )
        );

        network.solve();

        double expectedEquivalentResistance =
                1.0 / (
                        1.0 / 10.0
                                + 1.0 / 20.0
                );

        double expectedTotalResistance =
                expectedEquivalentResistance
                        + 0.1;

        double expectedTotalCurrent =
                12.0 / expectedTotalResistance;

        double expectedLoadVoltage =
                expectedTotalCurrent
                        * expectedEquivalentResistance;

        double expectedLoad1Current =
                expectedLoadVoltage / 10.0;

        double expectedLoad2Current =
                expectedLoadVoltage / 20.0;

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

        assertEquals(
                expectedLoadVoltage,
                load1.getElectricalState().voltage(),
                DELTA
        );

        assertEquals(
                expectedLoad1Current,
                load1.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                expectedLoadVoltage,
                load2.getElectricalState().voltage(),
                DELTA
        );

        assertEquals(
                expectedLoad2Current,
                load2.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                expectedTotalCurrent,
                load1.getElectricalState().current()
                        + load2.getElectricalState().current(),
                DELTA
        );
    }

    @Test
    void branchedWireNetworkShouldBeSimulatedDuringTick() {
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

        manager.tickSimulation(
                20.0,
                0.05
        );

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
         * Req = 0.035
         */
        double equivalentResistance =
                0.035;

        double expectedTotalCurrent =
                1.0 / (
                        equivalentResistance
                                + 0.1
                );

        double expectedBranchCurrent =
                expectedTotalCurrent / 2.0;

        assertEquals(
                expectedTotalCurrent,
                source.getElectricalState().current(),
                DELTA
        );

        /*
         * Antes da divisão.
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

        /*
         * Depois da junção.
         */
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
         * Cada branch recebe metade.
         */
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

    @Test
    void seriesLoadBeforeParallelLoadsShouldBeSolvedInWorld() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos sourcePos =
                new BlockPos(0, 64, 0);

        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        100.0
                );

        manager.registerSource(
                sourcePos,
                source,
                Direction.EAST,
                Direction.WEST
        );

        /*
         * SOURCE+ -> 5Ω -> P
         *
         *                 +-- 10Ω -- 5 wires --+
         *                 |                    |
         *                 +-- 20Ω -- 5 wires --+
         *
         *                                      N -> SOURCE-
         */

        /*
         * Resistor em série.
         */
        BlockPos seriesLoadPos =
                new BlockPos(1, 64, 0);

        ResistiveLoadComponent seriesLoad =
                new ResistiveLoadComponent(5.0);

        manager.registerLoad(
                seriesLoadPos,
                seriesLoad,
                Direction.WEST,
                Direction.EAST
        );

        BlockPos positiveJunction =
                new BlockPos(2, 64, 0);

        BlockPos negativeJunction =
                new BlockPos(-1, 64, 0);

        manager.registerWire(positiveJunction);
        manager.registerWire(negativeJunction);

        /*
         * Branch A = 10Ω + 0.05Ω de wires.
         */
        BlockPos loadAPos =
                new BlockPos(2, 64, 1);

        ResistiveLoadComponent loadA =
                new ResistiveLoadComponent(10.0);

        manager.registerLoad(
                loadAPos,
                loadA,
                Direction.NORTH,
                Direction.SOUTH
        );

        manager.registerWire(new BlockPos(2, 64, 2));
        manager.registerWire(new BlockPos(1, 64, 2));
        manager.registerWire(new BlockPos(0, 64, 2));
        manager.registerWire(new BlockPos(-1, 64, 2));
        manager.registerWire(new BlockPos(-1, 64, 1));

        /*
         * Branch B = 20Ω + 0.05Ω de wires.
         */
        BlockPos loadBPos =
                new BlockPos(2, 64, -1);

        ResistiveLoadComponent loadB =
                new ResistiveLoadComponent(20.0);

        manager.registerLoad(
                loadBPos,
                loadB,
                Direction.SOUTH,
                Direction.NORTH
        );

        manager.registerWire(new BlockPos(2, 64, -2));
        manager.registerWire(new BlockPos(1, 64, -2));
        manager.registerWire(new BlockPos(0, 64, -2));
        manager.registerWire(new BlockPos(-1, 64, -2));
        manager.registerWire(new BlockPos(-1, 64, -1));

        ElectricalWorldNetwork worldNetwork =
                manager.getElectricalWorldNetworkAt(
                        sourcePos
                ).orElseThrow();

        WorldCircuitResult result =
                manager.resolveWorldCircuit(
                        worldNetwork
                );

        assertEquals(
                WorldCircuitStatus.SOLVED,
                result.status()
        );

        ElectricalNetworkResult electricalResult =
                result.electricalResult()
                        .orElseThrow();

        double branchAResistance =
                10.05;

        double branchBResistance =
                20.05;

        double parallelResistance =
                1.0 / (
                        (1.0 / branchAResistance)
                                + (1.0 / branchBResistance)
                );

        /*
         * 5Ω series load
         * + P wire 0.01Ω
         * + paralelo
         * + N wire 0.01Ω
         */
        double expectedEquivalentResistance =
                5.02
                        + parallelResistance;

        assertEquals(
                expectedEquivalentResistance,
                electricalResult.equivalentResistance(),
                DELTA
        );
    }

    @Test
    void seriesLoadBeforeParallelLoadsShouldSplitCurrentDuringTick() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos sourcePos =
                new BlockPos(0, 64, 0);

        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        100.0
                );

        manager.registerSource(
                sourcePos,
                source,
                Direction.EAST,
                Direction.WEST
        );

        BlockPos seriesLoadPos =
                new BlockPos(1, 64, 0);

        ResistiveLoadComponent seriesLoad =
                new ResistiveLoadComponent(5.0);

        manager.registerLoad(
                seriesLoadPos,
                seriesLoad,
                Direction.WEST,
                Direction.EAST
        );

        BlockPos positiveJunction =
                new BlockPos(2, 64, 0);

        BlockPos negativeJunction =
                new BlockPos(-1, 64, 0);

        manager.registerWire(positiveJunction);
        manager.registerWire(negativeJunction);

        /*
         * Branch A = 10Ω + 0.05Ω
         */
        BlockPos loadAPos =
                new BlockPos(2, 64, 1);

        ResistiveLoadComponent loadA =
                new ResistiveLoadComponent(
                        10.0,
                        new ThermalProperties(
                                1000.0,
                                100.0
                        ),
                        ResistiveLoadComponent.DEFAULT_THERMAL_LIMITS,
                        20.0,
                        ComponentOperationalStatus.OPERATIONAL
                );

        manager.registerLoad(
                loadAPos,
                loadA,
                Direction.NORTH,
                Direction.SOUTH
        );

        manager.registerWire(new BlockPos(2, 64, 2));
        manager.registerWire(new BlockPos(1, 64, 2));
        manager.registerWire(new BlockPos(0, 64, 2));
        manager.registerWire(new BlockPos(-1, 64, 2));
        manager.registerWire(new BlockPos(-1, 64, 1));

        /*
         * Branch B = 20Ω + 0.05Ω
         */
        BlockPos loadBPos =
                new BlockPos(2, 64, -1);

        ResistiveLoadComponent loadB =
                new ResistiveLoadComponent(20.0);

        manager.registerLoad(
                loadBPos,
                loadB,
                Direction.SOUTH,
                Direction.NORTH
        );

        manager.registerWire(new BlockPos(2, 64, -2));
        manager.registerWire(new BlockPos(1, 64, -2));
        manager.registerWire(new BlockPos(0, 64, -2));
        manager.registerWire(new BlockPos(-1, 64, -2));
        manager.registerWire(new BlockPos(-1, 64, -1));

        manager.tickSimulation(
                20.0,
                0.05
        );

        double branchAResistance =
                10.05;

        double branchBResistance =
                20.05;

        double parallelResistance =
                1.0 / (
                        (1.0 / branchAResistance)
                                + (1.0 / branchBResistance)
                );

        double equivalentResistance =
                5.02
                        + parallelResistance;

        double expectedTotalCurrent =
                12.0 / (
                        equivalentResistance
                                + 0.1
                );

        double parallelVoltage =
                expectedTotalCurrent
                        * parallelResistance;

        double expectedBranchACurrent =
                parallelVoltage
                        / branchAResistance;

        double expectedBranchBCurrent =
                parallelVoltage
                        / branchBResistance;

        double expectedLoadAPower =
                expectedBranchACurrent
                        * expectedBranchACurrent
                        * 10.0;

        double expectedLoadATemperature =
                20.0
                        + (
                        expectedLoadAPower
                                * 0.05
                                / 1000.0
                );

        assertEquals(
                expectedLoadATemperature,
                loadA.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );

        /*
         * Corrente total passa pela fonte
         * e pelo resistor em série.
         */
        assertEquals(
                expectedTotalCurrent,
                source.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                expectedTotalCurrent,
                seriesLoad.getElectricalState().current(),
                DELTA
        );

        /*
         * Antes da divisão.
         */
        assertEquals(
                expectedTotalCurrent,
                manager.getWireComponent(
                                positiveJunction
                        ).orElseThrow()
                        .getElectricalState()
                        .current(),
                DELTA
        );

        /*
         * Branches possuem correntes diferentes.
         */
        assertEquals(
                expectedBranchACurrent,
                loadA.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                expectedBranchBCurrent,
                loadB.getElectricalState().current(),
                DELTA
        );

        /*
         * Depois da junção volta a ser a corrente total.
         */
        assertEquals(
                expectedTotalCurrent,
                manager.getWireComponent(
                                negativeJunction
                        ).orElseThrow()
                        .getElectricalState()
                        .current(),
                DELTA
        );

        /*
         * KCL.
         */
        assertEquals(
                expectedTotalCurrent,
                expectedBranchACurrent
                        + expectedBranchBCurrent,
                DELTA
        );
    }

    @Test
    void removingWireFromParallelBranchShouldRecalculateCircuit() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos sourcePos =
                new BlockPos(0, 64, 0);

        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        100.0
                );

        manager.registerSource(
                sourcePos,
                source,
                Direction.EAST,
                Direction.WEST
        );

        ResistiveLoadComponent seriesLoad =
                new ResistiveLoadComponent(5.0);

        manager.registerLoad(
                new BlockPos(1, 64, 0),
                seriesLoad,
                Direction.WEST,
                Direction.EAST
        );

        BlockPos positiveJunction =
                new BlockPos(2, 64, 0);

        BlockPos negativeJunction =
                new BlockPos(-1, 64, 0);

        manager.registerWire(positiveJunction);
        manager.registerWire(negativeJunction);

        /*
         * Branch A.
         */
        ResistiveLoadComponent loadA =
                new ResistiveLoadComponent(10.0);

        manager.registerLoad(
                new BlockPos(2, 64, 1),
                loadA,
                Direction.NORTH,
                Direction.SOUTH
        );

        manager.registerWire(new BlockPos(2, 64, 2));
        manager.registerWire(new BlockPos(1, 64, 2));

        BlockPos brokenWire =
                new BlockPos(0, 64, 2);

        manager.registerWire(brokenWire);

        manager.registerWire(new BlockPos(-1, 64, 2));
        manager.registerWire(new BlockPos(-1, 64, 1));

        /*
         * Branch B.
         */
        ResistiveLoadComponent loadB =
                new ResistiveLoadComponent(20.0);

        manager.registerLoad(
                new BlockPos(2, 64, -1),
                loadB,
                Direction.SOUTH,
                Direction.NORTH
        );

        manager.registerWire(new BlockPos(2, 64, -2));
        manager.registerWire(new BlockPos(1, 64, -2));
        manager.registerWire(new BlockPos(0, 64, -2));
        manager.registerWire(new BlockPos(-1, 64, -2));
        manager.registerWire(new BlockPos(-1, 64, -1));

        /*
         * Primeiro tick: os dois branches estão ativos.
         */
        manager.tickSimulation(
                20.0,
                0.05
        );

        assertTrue(
                loadA.getElectricalState().current() > 0.0
        );

        assertTrue(
                loadB.getElectricalState().current() > 0.0
        );

        /*
         * Quebra o branch A.
         */
        assertTrue(
                manager.unregisterWire(
                        brokenWire
                )
        );

        manager.tickSimulation(
                20.0,
                0.05
        );

        /*
         * Agora só existe o caminho:
         *
         * 5Ω
         * + P 0.01Ω
         * + branch B (20Ω + 0.05Ω)
         * + N 0.01Ω
         */
        double expectedEquivalentResistance =
                5.0
                        + 0.01
                        + 20.05
                        + 0.01;

        double expectedCurrent =
                12.0 / (
                        expectedEquivalentResistance
                                + 0.1
                );

        assertEquals(
                0.0,
                loadA.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                expectedCurrent,
                loadB.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                expectedCurrent,
                seriesLoad.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                expectedCurrent,
                source.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                expectedCurrent,
                manager.getWireComponent(
                                positiveJunction
                        ).orElseThrow()
                        .getElectricalState()
                        .current(),
                DELTA
        );

        assertEquals(
                expectedCurrent,
                manager.getWireComponent(
                                negativeJunction
                        ).orElseThrow()
                        .getElectricalState()
                        .current(),
                DELTA
        );
    }
}