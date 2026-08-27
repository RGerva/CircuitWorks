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

import com.rgerva.circuitworks.electrical.component.ComponentOperationalStatus;
import com.rgerva.circuitworks.electrical.component.DCVoltageSourceComponent;
import com.rgerva.circuitworks.electrical.component.ResistiveLoadComponent;
import com.rgerva.circuitworks.electrical.component.WireComponent;

import com.rgerva.circuitworks.electrical.network.ElectricalNetworkResult;
import com.rgerva.circuitworks.electrical.network.ElectricalNetworkStatus;
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
    void branchedWirePathShouldBeUnsupportedForNow() {
        ElectricalNetworkManager manager =
                new ElectricalNetworkManager();

        BlockPos sourcePos = new BlockPos(0, 64, 0);

        manager.registerSource(
                sourcePos,
                new DCVoltageSourceComponent(12.0, 0.1, 100.0),
                Direction.EAST,
                Direction.WEST
        );

        manager.registerWire(new BlockPos(1, 64, 0));
        manager.registerWire(new BlockPos(1, 64, 1));
        manager.registerWire(new BlockPos(0, 64, 1));
        manager.registerWire(new BlockPos(-1, 64, 1));
        manager.registerWire(new BlockPos(-1, 64, 0));

        /*
         * Branch adicional.
         */
        manager.registerWire(new BlockPos(0, 65, 1));

        ElectricalWorldNetwork worldNetwork =
                manager.getElectricalWorldNetworkAt(sourcePos)
                        .orElseThrow();

        WorldCircuitResult result =
                manager.resolveWorldCircuit(worldNetwork);

        assertEquals(
                WorldCircuitStatus.UNSUPPORTED_TOPOLOGY,
                result.status()
        );

        assertTrue(
                result.electricalResult().isEmpty()
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
}