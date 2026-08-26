/**
 * Generic Class: ElectricalNetworkTest <T>
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

package com.rgerva.circuitworks.electrical.network;

import com.rgerva.circuitworks.electrical.api.ElectricalState;
import com.rgerva.circuitworks.electrical.component.ComponentOperationalStatus;
import com.rgerva.circuitworks.electrical.component.DCVoltageSourceComponent;
import com.rgerva.circuitworks.electrical.component.ResistiveLoadComponent;
import com.rgerva.circuitworks.electrical.component.WireComponent;
import com.rgerva.circuitworks.electrical.thermal.ThermalLimits;
import com.rgerva.circuitworks.electrical.thermal.ThermalProperties;
import com.rgerva.circuitworks.electrical.thermal.ThermalStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElectricalNetworkTest {

    private static final double DELTA = 1.0E-9;

    @Test
    void emptyNetworkShouldBehaveAsOpenCircuit() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        ElectricalNetworkResult result = network.solve();
        ElectricalState state = result.state();

        assertEquals(12.0, state.voltage(), DELTA);
        assertEquals(0.0, state.current(), DELTA);
        assertEquals(0.0, state.power(), DELTA);

        assertTrue(
                Double.isInfinite(
                        network.getEquivalentResistance()
                )
        );
    }

    @Test
    void singleSixOhmLoadShouldProduceTwoAmpsAtTwelveVolts() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(6.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(load);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result = network.solve();
        ElectricalState state = result.state();

        assertEquals(
                6.0,
                network.getEquivalentResistance(),
                DELTA
        );

        assertEquals(12.0, state.voltage(), DELTA);
        assertEquals(2.0, state.current(), DELTA);
        assertEquals(24.0, state.power(), DELTA);

        assertEquals(
                12.0,
                load.getElectricalState().voltage(),
                DELTA
        );

        assertEquals(
                2.0,
                load.getElectricalState().current(),
                DELTA
        );
    }

    @Test
    void seriesResistanceShouldBeSummed() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        ResistiveLoadComponent first =
                new ResistiveLoadComponent(4.0);

        ResistiveLoadComponent second =
                new ResistiveLoadComponent(8.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(first);
        network.addComponent(second);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        first.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        first.getTerminalB(),
                        second.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        second.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        assertEquals(
                12.0,
                network.getEquivalentResistance(),
                DELTA
        );

        ElectricalNetworkResult result = network.solve();
        ElectricalState state = result.state();

        assertEquals(
                1.0,
                state.current(),
                DELTA
        );
    }

    @Test
    void seriesComponentsShouldReceiveCorrectVoltageDrop() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        ResistiveLoadComponent first =
                new ResistiveLoadComponent(4.0);

        ResistiveLoadComponent second =
                new ResistiveLoadComponent(8.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(first);
        network.addComponent(second);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        first.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        first.getTerminalB(),
                        second.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        second.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        network.solve();

        assertEquals(
                4.0,
                first.getElectricalState().voltage(),
                DELTA
        );

        assertEquals(
                8.0,
                second.getElectricalState().voltage(),
                DELTA
        );

        assertEquals(
                1.0,
                first.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                1.0,
                second.getElectricalState().current(),
                DELTA
        );
    }

    @Test
    void zeroVoltageShouldResetComponentStates() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(6.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(load);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        network.solve();

        assertEquals(
                2.0,
                load.getElectricalState().current(),
                DELTA
        );

        // Troca da fonte
        DCVoltageSourceComponent zeroVoltageSource =
                new DCVoltageSourceComponent(0.0);

        network.setSource(zeroVoltageSource);

        // setSource remove as conexões da fonte antiga,
        // então conectamos a nova fonte.
        network.addConnection(
                new ElectricalConnection(
                        zeroVoltageSource.getPositiveTerminal(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        zeroVoltageSource.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result = network.solve();
        ElectricalState state = result.state();

        assertEquals(
                ElectricalState.ZERO,
                state
        );

        assertEquals(
                ElectricalState.ZERO,
                load.getElectricalState()
        );

        assertEquals(
                ElectricalState.ZERO,
                zeroVoltageSource.getElectricalState()
        );

        // A fonte removida também deve ter sido zerada.
        assertEquals(
                ElectricalState.ZERO,
                source.getElectricalState()
        );
    }

    @Test
    void removingSeriesComponentShouldOpenCircuit() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        ResistiveLoadComponent first =
                new ResistiveLoadComponent(4.0);

        ResistiveLoadComponent second =
                new ResistiveLoadComponent(8.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(first);
        network.addComponent(second);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        first.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        first.getTerminalB(),
                        second.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        second.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        assertEquals(
                12.0,
                network.getEquivalentResistance(),
                DELTA
        );

        network.removeComponent(second);

        assertTrue(
                Double.isInfinite(
                        network.getEquivalentResistance()
                )
        );

        ElectricalNetworkResult result = network.solve();
        ElectricalState state = result.state();

        assertEquals(
                0.0,
                state.current(),
                DELTA
        );

        assertEquals(
                ElectricalState.ZERO,
                second.getElectricalState()
        );
    }

    @Test
    void networkWithoutSourceShouldHaveZeroState() {

        ElectricalNetwork network =
                new ElectricalNetwork();

        network.addComponent(
                new ResistiveLoadComponent(6.0)
        );

        ElectricalNetworkResult result = network.solve();
        ElectricalState state = result.state();

        assertEquals(
                ElectricalState.ZERO,
                state
        );
    }

    @Test
    void sourceWithoutLoadShouldBehaveAsOpenCircuit() {

        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        ElectricalNetworkResult result = network.solve();
        ElectricalState state = result.state();

        assertEquals(
                12.0,
                state.voltage(),
                DELTA
        );

        assertEquals(
                0.0,
                state.current(),
                DELTA
        );

        assertEquals(
                0.0,
                state.power(),
                DELTA
        );

        assertEquals(
                state,
                source.getElectricalState()
        );
    }

    @Test
    void disconnectedLoadShouldNotConsumeCurrent() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(6.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(load);

        ElectricalNetworkResult result = network.solve();
        ElectricalState state = result.state();

        assertEquals(12.0, state.voltage(), DELTA);
        assertEquals(0.0, state.current(), DELTA);

        assertEquals(
                ElectricalState.ZERO,
                load.getElectricalState()
        );

        assertTrue(
                Double.isInfinite(
                        network.getEquivalentResistance()
                )
        );
    }

    @Test
    void partiallyConnectedLoadShouldBehaveAsOpenCircuit() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(6.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(load);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        load.getTerminalA()
                )
        );

        ElectricalNetworkResult result = network.solve();
        ElectricalState state = result.state();

        assertEquals(12.0, state.voltage(), DELTA);
        assertEquals(0.0, state.current(), DELTA);

        assertEquals(
                ElectricalState.ZERO,
                load.getElectricalState()
        );
    }

    @Test
    void closedSingleLoadCircuitShouldSolve() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(6.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(load);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result = network.solve();
        ElectricalState state = result.state();

        assertEquals(6.0,
                network.getEquivalentResistance(),
                DELTA);

        assertEquals(12.0, state.voltage(), DELTA);
        assertEquals(2.0, state.current(), DELTA);
        assertEquals(24.0, state.power(), DELTA);

        assertEquals(
                12.0,
                load.getElectricalState().voltage(),
                DELTA
        );

        assertEquals(
                2.0,
                load.getElectricalState().current(),
                DELTA
        );
    }

    @Test
    void twoConnectedSeriesLoadsShouldSolve() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        ResistiveLoadComponent first =
                new ResistiveLoadComponent(4.0);

        ResistiveLoadComponent second =
                new ResistiveLoadComponent(8.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(first);
        network.addComponent(second);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        first.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        first.getTerminalB(),
                        second.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        second.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result = network.solve();
        ElectricalState state = result.state();

        assertEquals(
                12.0,
                network.getEquivalentResistance(),
                DELTA
        );

        assertEquals(
                1.0,
                state.current(),
                DELTA
        );

        assertEquals(
                4.0,
                first.getElectricalState().voltage(),
                DELTA
        );

        assertEquals(
                8.0,
                second.getElectricalState().voltage(),
                DELTA
        );
    }

    @Test
    void directConnectionBetweenSourceTerminalsShouldBeShortCircuit() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result =
                network.solve();

        assertEquals(
                ElectricalNetworkStatus.SHORT_CIRCUIT,
                result.status()
        );

        assertTrue(result.isFault());

        assertEquals(
                0.0,
                result.equivalentResistance(),
                DELTA
        );

        assertEquals(
                12.0,
                result.state().voltage(),
                DELTA
        );

        assertEquals(
                0.0,
                result.state().current(),
                DELTA
        );
    }

    @Test
    void idealWireBetweenSourceAndLoadShouldNotChangeCircuit() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        WireComponent wire =
                new WireComponent();

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(6.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire);
        network.addComponent(load);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        wire.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        wire.getTerminalB(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result = network.solve();
        ElectricalState state = result.state();

        assertEquals(
                6.0,
                network.getEquivalentResistance(),
                DELTA
        );

        assertEquals(
                2.0,
                state.current(),
                DELTA
        );

        assertEquals(
                0.0,
                wire.getElectricalState().voltage(),
                DELTA
        );

        assertEquals(
                2.0,
                wire.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                12.0,
                load.getElectricalState().voltage(),
                DELTA
        );
    }

    @Test
    void multipleIdealWiresShouldCarryCurrent() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        WireComponent wire1 =
                new WireComponent();

        WireComponent wire2 =
                new WireComponent();

        WireComponent wire3 =
                new WireComponent();

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(6.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire1);
        network.addComponent(wire2);
        network.addComponent(wire3);
        network.addComponent(load);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        wire1.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        wire1.getTerminalB(),
                        wire2.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        wire2.getTerminalB(),
                        wire3.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        wire3.getTerminalB(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result = network.solve();
        ElectricalState state = result.state();

        assertEquals(
                6.0,
                network.getEquivalentResistance(),
                DELTA
        );

        assertEquals(
                2.0,
                state.current(),
                DELTA
        );

        assertEquals(
                2.0,
                wire1.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                2.0,
                wire2.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                2.0,
                wire3.getElectricalState().current(),
                DELTA
        );
    }

    @Test
    void resistiveWireShouldProduceVoltageDrop() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        WireComponent wire =
                new WireComponent(1.0);

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(5.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire);
        network.addComponent(load);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        wire.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        wire.getTerminalB(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result = network.solve();
        ElectricalState state = result.state();

        assertEquals(
                6.0,
                network.getEquivalentResistance(),
                DELTA
        );

        assertEquals(
                2.0,
                state.current(),
                DELTA
        );

        assertEquals(
                2.0,
                wire.getElectricalState().voltage(),
                DELTA
        );

        assertEquals(
                10.0,
                load.getElectricalState().voltage(),
                DELTA
        );
    }

    @Test
    void idealWireDirectlyAcrossSourceShouldCauseShortCircuit() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        WireComponent wire =
                new WireComponent();

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        wire.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        wire.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result =
                network.solve();

        assertEquals(
                ElectricalNetworkStatus.SHORT_CIRCUIT,
                result.status()
        );

        assertTrue(result.isFault());

        assertEquals(
                0.0,
                result.equivalentResistance(),
                DELTA
        );

        assertEquals(
                12.0,
                result.state().voltage(),
                DELTA
        );

        assertEquals(
                0.0,
                result.state().current(),
                DELTA
        );

        assertEquals(
                ElectricalState.ZERO,
                wire.getElectricalState()
        );
    }

    @Test
    void idealWireAcrossSourceShouldReportShortCircuit() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        WireComponent wire =
                new WireComponent();

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        wire.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        wire.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result =
                network.solve();

        assertEquals(
                ElectricalNetworkStatus.SHORT_CIRCUIT,
                result.status()
        );

        assertTrue(result.isFault());

        assertEquals(
                0.0,
                result.equivalentResistance(),
                DELTA
        );

        assertEquals(
                12.0,
                result.state().voltage(),
                DELTA
        );
    }

    @Test
    void networkWithoutSourceShouldReportNoSource() {
        ElectricalNetwork network =
                new ElectricalNetwork();

        ElectricalNetworkResult result =
                network.solve();

        assertEquals(
                ElectricalNetworkStatus.NO_SOURCE,
                result.status()
        );

        assertEquals(
                ElectricalState.ZERO,
                result.state()
        );
    }

    @Test
    void zeroVoltSourceWithClosedCircuitShouldBeInactive() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(0.0);

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(6.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(load);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result =
                network.solve();

        assertEquals(
                ElectricalNetworkStatus.INACTIVE,
                result.status()
        );

        assertEquals(
                ElectricalState.ZERO,
                result.state()
        );

        assertEquals(
                6.0,
                result.equivalentResistance(),
                DELTA
        );
    }

    @Test
    void realSourceShortCircuitShouldCalculateCurrent() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.1
                );

        WireComponent wire =
                new WireComponent();

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        wire.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        wire.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result =
                network.solve();

        assertEquals(
                ElectricalNetworkStatus.SHORT_CIRCUIT,
                result.status()
        );

        assertEquals(
                120.0,
                result.state().current(),
                DELTA
        );

        assertEquals(
                120.0,
                wire.getElectricalState().current(),
                DELTA
        );

        assertEquals(
                0.0,
                wire.getElectricalState().voltage(),
                DELTA
        );
    }

    @Test
    void sourceInternalResistanceShouldReduceLoadVoltage() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        1.0
                );

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(5.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(load);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result =
                network.solve();

        // Rtotal = 1 + 5 = 6 ohms
        // I = 12 / 6 = 2 A
        assertEquals(
                2.0,
                result.state().current(),
                DELTA
        );

        // Load:
        // V = I * R
        // V = 2 * 5 = 10 V
        assertEquals(
                10.0,
                load.getElectricalState().voltage(),
                DELTA
        );
    }

    @Test
    void currentAboveSourceLimitShouldReportOvercurrent() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.0,
                        5.0
                );

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(1.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(load);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result =
                network.solve();

        assertEquals(
                ElectricalNetworkStatus.OVERCURRENT,
                result.status()
        );

        assertTrue(result.isFault());

        assertEquals(
                12.0,
                result.state().current(),
                DELTA
        );

        assertEquals(
                12.0,
                load.getElectricalState().current(),
                DELTA
        );
    }

    @Test
    void currentAtSourceLimitShouldRemainActive() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.0,
                        2.0
                );

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(6.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(load);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result =
                network.solve();

        assertEquals(
                ElectricalNetworkStatus.ACTIVE,
                result.status()
        );

        assertFalse(result.isFault());

        assertEquals(
                2.0,
                result.state().current(),
                DELTA
        );
    }

    @Test
    void internalResistanceCanKeepCurrentBelowLimit() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        1.0,
                        3.0
                );

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(5.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(load);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result =
                network.solve();

        // Rtotal = 1 + 5 = 6 Ω
        // I = 12 / 6 = 2 A

        assertEquals(
                ElectricalNetworkStatus.ACTIVE,
                result.status()
        );

        assertEquals(
                2.0,
                result.state().current(),
                DELTA
        );
    }

    @Test
    void shortCircuitShouldTakePrecedenceOverOvercurrent() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        5.0
                );

        WireComponent wire =
                new WireComponent();

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        wire.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        wire.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result =
                network.solve();

        assertEquals(
                ElectricalNetworkStatus.SHORT_CIRCUIT,
                result.status()
        );

        assertEquals(
                120.0,
                result.state().current(),
                DELTA
        );

        assertTrue(result.isFault());
    }


    @Test
    void wireAboveCurrentLimitShouldReportOvercurrent() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.0,
                        50.0
                );

        WireComponent wire =
                new WireComponent(
                        0.0,
                        5.0
                );

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(1.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire);
        network.addComponent(load);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        wire.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        wire.getTerminalB(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result =
                network.solve();

        // I = 12 / 1
        // I = 12 A

        assertEquals(
                12.0,
                result.state().current(),
                DELTA
        );

        assertEquals(
                ElectricalNetworkStatus.OVERCURRENT,
                result.status()
        );

        assertTrue(
                result.hasFault(
                        ElectricalFaultType.OVERCURRENT
                )
        );

        assertEquals(
                1,
                result.faults().size()
        );

        ElectricalFault fault =
                result.faults().getFirst();

        assertSame(
                wire,
                fault.component()
        );

        assertEquals(
                12.0,
                fault.measuredValue(),
                DELTA
        );

        assertEquals(
                5.0,
                fault.limit(),
                DELTA
        );
    }

    @Test
    void wireAtMaximumCurrentShouldRemainActive() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.0,
                        50.0
                );

        WireComponent wire =
                new WireComponent(
                        0.0,
                        2.0
                );

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(6.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire);
        network.addComponent(load);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        wire.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        wire.getTerminalB(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result =
                network.solve();

        assertEquals(
                2.0,
                result.state().current(),
                DELTA
        );

        assertEquals(
                ElectricalNetworkStatus.ACTIVE,
                result.status()
        );

        assertTrue(
                result.faults().isEmpty()
        );
    }

    @Test
    void shortCircuitShouldIdentifyOverloadedComponents() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        50.0
                );

        WireComponent wire =
                new WireComponent(
                        0.0,
                        5.0
                );

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        wire.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        wire.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result =
                network.solve();

        assertEquals(
                ElectricalNetworkStatus.SHORT_CIRCUIT,
                result.status()
        );

        // Ishort = 12 / 0.1
        assertEquals(
                120.0,
                result.state().current(),
                DELTA
        );

        /*
         * Source max = 50 A
         * Wire max   = 5 A
         *
         * Both are overloaded.
         */
        assertEquals(
                2,
                result.faults().size()
        );
    }

    @Test
    void overloadedWireShouldExposeFaultSeverity() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.0,
                        50.0
                );

        WireComponent wire =
                new WireComponent(
                        0.0,
                        5.0
                );

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(1.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire);
        network.addComponent(load);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        wire.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        wire.getTerminalB(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result =
                network.solve();

        ElectricalFault fault =
                result.faults().getFirst();

        assertSame(
                wire,
                fault.component()
        );

        assertEquals(
                2.4,
                fault.ratio(),
                DELTA
        );

        assertEquals(
                ElectricalFaultSeverity.HIGH,
                fault.severity()
        );
    }

    @Test
    void networkCurrentShouldCauseWireHeating() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.0,
                        50.0
                );

        WireComponent wire =
                new WireComponent(
                        1.0,
                        20.0,
                        new ThermalProperties(
                                10.0,
                                1.0
                        ),
                        20.0
                );

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(5.0);

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire);
        network.addComponent(load);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        wire.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        wire.getTerminalB(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        ElectricalNetworkResult result =
                network.solve();

        // Rtotal = 1 + 5 = 6 Ω
        // I = 12 / 6 = 2 A

        assertEquals(
                2.0,
                result.state().current(),
                DELTA
        );

        wire.updateThermalState(
                20.0,
                1.0
        );

        /*
         * P = I²R
         *
         * P = 2² × 1
         * P = 4 W
         *
         * C = 10
         *
         * ΔT = 0.4 °C
         */

        assertEquals(
                20.4,
                wire.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );
    }

    @Test
    void failedWireShouldOpenCircuit() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.0,
                        50.0
                );

        WireComponent wire =
                new WireComponent(
                        1.0,
                        50.0,
                        new ThermalProperties(
                                1.0,
                                0.0
                        ),
                        new ThermalLimits(
                                30.0,
                                40.0,
                                50.0
                        ),
                        20.0
                );

        ResistiveLoadComponent load =
                new ResistiveLoadComponent(
                        5.0
                );

        ElectricalNetwork network =
                new ElectricalNetwork(
                        source
                );

        network.addComponent(
                wire
        );

        network.addComponent(
                load
        );

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        wire.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        wire.getTerminalB(),
                        load.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        load.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        /*
         * Primeiro solve:
         *
         * Rtotal = Rwire + Rload
         * Rtotal = 1 + 5
         * Rtotal = 6 Ω
         *
         * I = V / R
         * I = 12 / 6
         * I = 2 A
         */
        ElectricalNetworkResult firstResult =
                network.solve();

        assertEquals(
                ElectricalNetworkStatus.ACTIVE,
                firstResult.status()
        );

        assertEquals(
                2.0,
                firstResult.state().current(),
                DELTA
        );

        assertEquals(
                ComponentOperationalStatus.OPERATIONAL,
                wire.getOperationalStatus()
        );

        assertTrue(
                wire.isOperational()
        );

        /*
         * Aquecimento do wire:
         *
         * P = I²R
         * P = 2² × 1
         * P = 4 W
         *
         * Heat capacity = 1 J/°C
         *
         * Em 8 segundos:
         *
         * E = P × t
         * E = 4 × 8
         * E = 32 J
         *
         * ΔT = E / C
         * ΔT = 32 / 1
         * ΔT = 32 °C
         *
         * Temperatura inicial = 20 °C
         *
         * Temperatura final:
         * 20 + 32 = 52 °C
         *
         * Failure temperature = 50 °C
         *
         * Portanto:
         * wire -> FAILED
         */
        wire.updateThermalState(
                20.0,
                8.0
        );

        assertEquals(
                52.0,
                wire.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );

        assertEquals(
                ThermalStatus.FAILED,
                wire.getThermalStatus()
        );

        assertEquals(
                ComponentOperationalStatus.FAILED,
                wire.getOperationalStatus()
        );

        assertFalse(
                wire.isOperational()
        );

        /*
         * Ao falhar, o WireComponent deve zerar
         * seu estado elétrico.
         */
        assertEquals(
                ElectricalState.ZERO,
                wire.getElectricalState()
        );

        /*
         * Resolve novamente a mesma topologia.
         *
         * Como o wire está FAILED,
         * resolveSeriesPath() deve considerar
         * esse caminho como aberto.
         */
        ElectricalNetworkResult secondResult =
                network.solve();

        assertEquals(
                ElectricalNetworkStatus.OPEN_CIRCUIT,
                secondResult.status()
        );

        assertEquals(
                0.0,
                secondResult.state().current(),
                DELTA
        );

        assertTrue(
                Double.isInfinite(
                        secondResult.equivalentResistance()
                )
        );

        /*
         * Como o circuito está aberto,
         * o load também não pode mais
         * possuir corrente.
         */
        assertEquals(
                ElectricalState.ZERO,
                load.getElectricalState()
        );

        /*
         * E o wire continua permanentemente FAILED.
         */
        assertEquals(
                ComponentOperationalStatus.FAILED,
                wire.getOperationalStatus()
        );

        assertFalse(
                wire.isOperational()
        );
    }

    @Test
    void shortCircuitShouldHeatAndFailSource() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        1.0,
                        100.0,
                        new ThermalProperties(
                                10.0,
                                0.0
                        ),
                        new ThermalLimits(
                                30.0,
                                40.0,
                                50.0
                        ),
                        20.0
                );

        WireComponent wire =
                new WireComponent();

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire);

        network.addConnection(
                new ElectricalConnection(
                        source.getPositiveTerminal(),
                        wire.getTerminalA()
                )
        );

        network.addConnection(
                new ElectricalConnection(
                        wire.getTerminalB(),
                        source.getNegativeTerminal()
                )
        );

        /*
         * Short:
         *
         * V = 12 V
         * Rinternal = 1 Ω
         *
         * Ishort = 12 A
         */
        ElectricalNetworkResult firstResult =
                network.solve();

        assertEquals(
                ElectricalNetworkStatus.SHORT_CIRCUIT,
                firstResult.status()
        );

        assertEquals(
                12.0,
                firstResult.state().current(),
                DELTA
        );

        /*
         * Internal heating:
         *
         * P = I²R
         * P = 12² × 1
         * P = 144 W
         *
         * C = 10 J/°C
         *
         * After 3 seconds:
         *
         * E = 432 J
         * ΔT = 43.2 °C
         *
         * 20 + 43.2 = 63.2 °C
         */
        source.updateThermalState(
                20.0,
                3.0
        );

        assertEquals(
                63.2,
                source.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );

        assertEquals(
                ComponentOperationalStatus.FAILED,
                source.getOperationalStatus()
        );

        /*
         * Network is solved again.
         *
         * The failed source can no longer
         * energize the circuit.
         */
        ElectricalNetworkResult secondResult =
                network.solve();

        assertEquals(
                ElectricalNetworkStatus.SOURCE_FAILED,
                secondResult.status()
        );

        assertEquals(
                ElectricalState.ZERO,
                secondResult.state()
        );

        assertEquals(
                ElectricalState.ZERO,
                wire.getElectricalState()
        );
    }
}
