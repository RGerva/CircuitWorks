/**
 * Generic Class: ElectricalSimulationTest <T>
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

package com.rgerva.circuitworks.electrical.simulation;

import com.rgerva.circuitworks.electrical.component.ComponentOperationalStatus;
import com.rgerva.circuitworks.electrical.component.DCVoltageSourceComponent;
import com.rgerva.circuitworks.electrical.component.ResistiveLoadComponent;
import com.rgerva.circuitworks.electrical.component.WireComponent;
import com.rgerva.circuitworks.electrical.network.ElectricalConnection;
import com.rgerva.circuitworks.electrical.network.ElectricalNetwork;
import com.rgerva.circuitworks.electrical.network.ElectricalNetworkStatus;
import com.rgerva.circuitworks.electrical.thermal.ThermalLimits;
import com.rgerva.circuitworks.electrical.thermal.ThermalProperties;
import com.rgerva.circuitworks.electrical.thermal.ThermalStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElectricalSimulationTest {

    private static final double DELTA = 1.0E-9;
    private static final double STEP_SECONDS = 0.05;

    @Test
    void simulationStepShouldSolveNetworkAndUpdateTemperature() {
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
                new ResistiveLoadComponent(
                        5.0
                );

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

        ElectricalSimulation simulation =
                new ElectricalSimulation(network);

        ElectricalSimulationResult result =
                simulation.step(
                        20.0,
                        1.0
                );

        /*
         * Rtotal = 1 + 5 = 6 Ω
         *
         * I = 12 / 6
         * I = 2 A
         */
        assertEquals(
                ElectricalNetworkStatus.ACTIVE,
                result.initialNetworkResult()
                        .status()
        );

        assertEquals(
                ElectricalNetworkStatus.ACTIVE,
                result.finalNetworkResult()
                        .status()
        );

        assertTrue(
                result.events().isEmpty()
        );

        /*
         * Wire:
         *
         * P = I²R
         * P = 2² × 1
         * P = 4 W
         *
         * C = 10 J/°C
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
    void simulationShouldAutomaticallyResolveAgainWhenWireFails() {
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

        ElectricalSimulation simulation =
                new ElectricalSimulation(network);

        /*
         * Initial:
         *
         * I = 12 / 6
         * I = 2 A
         *
         * Wire:
         *
         * P = 2² × 1
         * P = 4 W
         *
         * 8 seconds:
         *
         * E = 32 J
         *
         * C = 1 J/°C
         *
         * ΔT = 32 °C
         *
         * 20 → 52 °C
         *
         * Failure = 50 °C
         */
        ElectricalSimulationResult result =
                simulation.step(
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
                ComponentOperationalStatus.FAILED,
                wire.getOperationalStatus()
        );

        /*
         * ElectricalSimulation deve detectar
         * a falha e chamar solve() novamente.
         */
        assertEquals(
                ElectricalNetworkStatus.ACTIVE,
                result.initialNetworkResult()
                        .status()
        );

        assertEquals(
                ElectricalNetworkStatus.OPEN_CIRCUIT,
                result.finalNetworkResult()
                        .status()
        );

        assertTrue(
                result.hasEvent(
                        ElectricalSimulationEventType.COMPONENT_FAILED
                )
        );

        assertEquals(
                0.0,
                load.getElectricalState()
                        .current(),
                DELTA
        );
    }

    @Test
    void simulationShouldAutomaticallyResolveAgainWhenSourceFails() {
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

        ElectricalSimulation simulation =
                new ElectricalSimulation(network);

        /*
         * Short:
         *
         * I = 12 / 1
         * I = 12 A
         *
         * Source:
         *
         * P = 12² × 1
         * P = 144 W
         *
         * 3 seconds:
         *
         * E = 432 J
         *
         * C = 10
         *
         * ΔT = 43.2 °C
         *
         * 20 → 63.2 °C
         */
        ElectricalSimulationResult result =
                simulation.step(
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
                ElectricalNetworkStatus.SHORT_CIRCUIT,
                result.initialNetworkResult()
                        .status()
        );

        assertEquals(
                ElectricalNetworkStatus.SOURCE_FAILED,
                result.finalNetworkResult()
                        .status()
        );

        assertTrue(
                result.hasEvent(
                        ElectricalSimulationEventType.COMPONENT_FAILED
                )
        );

        assertEquals(
                0.0,
                wire.getElectricalState()
                        .current(),
                DELTA
        );
    }

    @Test
    void disconnectedWireShouldStillCool() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0
                );

        WireComponent wire =
                new WireComponent(
                        1.0,
                        50.0,
                        new ThermalProperties(
                                10.0,
                                1.0
                        ),
                        new ThermalLimits(
                                150.0,
                                200.0,
                                300.0
                        ),
                        100.0
                );

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire);

        ElectricalSimulation simulation =
                new ElectricalSimulation(network);

        ElectricalSimulationResult result =
                simulation.step(
                        20.0,
                        1.0
                );

        assertEquals(
                ElectricalNetworkStatus.OPEN_CIRCUIT,
                result.finalNetworkResult().status()
        );

        /*
         * No electrical heating.
         *
         * Cooling:
         *
         * 1 × (100 - 20)
         * = 80 W
         *
         * ΔT = -80 / 10
         * = -8 °C
         *
         * 100 → 92 °C
         */
        assertEquals(
                92.0,
                wire.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );
    }

    @Test
    void simulationShouldReportWireFailureEvent() {
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

        ElectricalSimulation simulation =
                new ElectricalSimulation(network);

        ElectricalSimulationResult result =
                simulation.step(
                        20.0,
                        8.0
                );

        /*
         * Antes da atualização térmica:
         *
         * circuito funcionando normalmente.
         */
        assertEquals(
                ElectricalNetworkStatus.ACTIVE,
                result.initialNetworkResult()
                        .status()
        );

        /*
         * Wire atingiu 52 °C e falhou.
         */
        assertEquals(
                52.0,
                wire.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );

        assertEquals(
                ComponentOperationalStatus.FAILED,
                wire.getOperationalStatus()
        );

        /*
         * Depois da falha:
         *
         * circuito aberto.
         */
        assertEquals(
                ElectricalNetworkStatus.OPEN_CIRCUIT,
                result.finalNetworkResult()
                        .status()
        );

        /*
         * Um único evento deve ter sido gerado.
         */
        assertEquals(
                1,
                result.events().size()
        );

        ElectricalSimulationEvent event =
                result.events().getFirst();

        assertEquals(
                ElectricalSimulationEventType.COMPONENT_FAILED,
                event.type()
        );

        assertSame(
                wire,
                event.component()
        );

        assertEquals(
                52.0,
                event.temperatureCelsius(),
                DELTA
        );

        assertTrue(
                result.hasEvent(
                        ElectricalSimulationEventType.COMPONENT_FAILED
                )
        );
    }

    @Test
    void simulationShouldPreserveShortCircuitBeforeSourceFailure() {
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

        ElectricalSimulation simulation =
                new ElectricalSimulation(network);

        ElectricalSimulationResult result =
                simulation.step(
                        20.0,
                        3.0
                );

        /*
         * No início do intervalo existia curto.
         */
        assertEquals(
                ElectricalNetworkStatus.SHORT_CIRCUIT,
                result.initialNetworkResult()
                        .status()
        );

        assertEquals(
                12.0,
                result.initialNetworkResult()
                        .state()
                        .current(),
                DELTA
        );

        /*
         * Durante o intervalo a fonte aqueceu
         * e falhou.
         */
        assertEquals(
                63.2,
                source.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );

        /*
         * No final do intervalo:
         *
         * a fonte já está destruída.
         */
        assertEquals(
                ElectricalNetworkStatus.SOURCE_FAILED,
                result.finalNetworkResult()
                        .status()
        );

        assertEquals(
                1,
                result.events().size()
        );

        ElectricalSimulationEvent event =
                result.events().getFirst();

        assertSame(
                source,
                event.component()
        );

        assertEquals(
                ElectricalSimulationEventType.COMPONENT_FAILED,
                event.type()
        );
    }

    @Test
    void simulationShouldReportWhenWireBecomesHot() {
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
                                22.0,
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

        ElectricalSimulation simulation =
                new ElectricalSimulation(network);

        /*
         * I = 12 / 6 = 2 A
         *
         * Pwire = 2² × 1
         *       = 4 W
         *
         * C = 1
         *
         * Em 1 segundo:
         *
         * 20 °C -> 24 °C
         *
         * hotTemperature = 22 °C
         */
        ElectricalSimulationResult result =
                simulation.step(
                        20.0,
                        1.0
                );

        assertEquals(
                24.0,
                wire.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );

        assertEquals(
                ThermalStatus.HOT,
                wire.getThermalStatus()
        );

        assertEquals(
                ComponentOperationalStatus.OPERATIONAL,
                wire.getOperationalStatus()
        );

        assertEquals(
                ElectricalNetworkStatus.ACTIVE,
                result.initialNetworkResult()
                        .status()
        );

        assertEquals(
                ElectricalNetworkStatus.ACTIVE,
                result.finalNetworkResult()
                        .status()
        );

        assertEquals(
                1,
                result.events().size()
        );

        ElectricalSimulationEvent event =
                result.events().getFirst();

        assertEquals(
                ElectricalSimulationEventType.COMPONENT_HOT,
                event.type()
        );

        assertSame(
                wire,
                event.component()
        );

        assertEquals(
                24.0,
                event.temperatureCelsius(),
                DELTA
        );
    }

    @Test
    void simulationShouldReportWhenWireBecomesOverheated() {
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
                                22.0,
                                26.0,
                                50.0
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

        ElectricalSimulation simulation =
                new ElectricalSimulation(network);

        /*
         * First second:
         *
         * 20 -> 24 °C
         * NORMAL -> HOT
         */
        ElectricalSimulationResult first =
                simulation.step(
                        20.0,
                        1.0
                );

        assertTrue(
                first.hasEvent(
                        ElectricalSimulationEventType.COMPONENT_HOT
                )
        );

        /*
         * Second second:
         *
         * 24 -> 28 °C
         * HOT -> OVERHEATED
         */
        ElectricalSimulationResult second =
                simulation.step(
                        20.0,
                        1.0
                );

        assertEquals(
                28.0,
                wire.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );

        assertEquals(
                ThermalStatus.OVERHEATED,
                wire.getThermalStatus()
        );

        assertEquals(
                ComponentOperationalStatus.OPERATIONAL,
                wire.getOperationalStatus()
        );

        assertTrue(
                second.hasEvent(
                        ElectricalSimulationEventType.COMPONENT_OVERHEATED
                )
        );

        /*
         * OVERHEATED ainda conduz.
         */
        assertEquals(
                ElectricalNetworkStatus.ACTIVE,
                second.finalNetworkResult()
                        .status()
        );
    }

    @Test
    void hotEventShouldOnlyBeGeneratedWhenStatusChanges() {
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
                                10.0,
                                0.0
                        ),
                        new ThermalLimits(
                                20.1,
                                100.0,
                                200.0
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

        ElectricalSimulation simulation =
                new ElectricalSimulation(network);

        ElectricalSimulationResult first =
                simulation.step(
                        20.0,
                        1.0
                );

        assertTrue(
                first.hasEvent(
                        ElectricalSimulationEventType.COMPONENT_HOT
                )
        );

        /*
         * Continua HOT no próximo step.
         *
         * Não deve gerar COMPONENT_HOT novamente.
         */
        ElectricalSimulationResult second =
                simulation.step(
                        20.0,
                        1.0
                );

        assertFalse(
                second.hasEvent(
                        ElectricalSimulationEventType.COMPONENT_HOT
                )
        );
    }

    @Test
    void overheatedWireShouldCoolBackToHot() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0
                );

        WireComponent wire =
                new WireComponent(
                        1.0,
                        50.0,
                        new ThermalProperties(
                                10.0,
                                1.0
                        ),
                        new ThermalLimits(
                                22.0,
                                26.0,
                                50.0
                        ),
                        30.0
                );

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire);

        ElectricalSimulation simulation =
                new ElectricalSimulation(network);

        assertEquals(
                ThermalStatus.OVERHEATED,
                wire.getThermalStatus()
        );

        /*
         * Wire desconectado:
         *
         * I = 0 A
         * heating = 0 W
         *
         * T = 30 °C
         * Tambient = 20 °C
         *
         * Cooling:
         * P = 1 × (30 - 20)
         * P = 10 W
         *
         * Durante 6 s:
         * E = 60 J
         *
         * C = 10 J/°C
         *
         * ΔT = -6 °C
         *
         * 30 -> 24 °C
         *
         * 24 °C = HOT
         */
        ElectricalSimulationResult result =
                simulation.step(
                        20.0,
                        6.0
                );

        assertEquals(
                24.0,
                wire.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );

        assertEquals(
                ThermalStatus.HOT,
                wire.getThermalStatus()
        );

        assertEquals(
                ComponentOperationalStatus.OPERATIONAL,
                wire.getOperationalStatus()
        );

        assertTrue(
                result.hasEvent(
                        ElectricalSimulationEventType.COMPONENT_COOLED
                )
        );

        assertFalse(
                result.hasEvent(
                        ElectricalSimulationEventType.COMPONENT_FAILED
                )
        );
    }

    @Test
    void hotWireShouldCoolBackToNormal() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0
                );

        WireComponent wire =
                new WireComponent(
                        1.0,
                        50.0,
                        new ThermalProperties(
                                10.0,
                                1.0
                        ),
                        new ThermalLimits(
                                22.0,
                                26.0,
                                50.0
                        ),
                        24.0
                );

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire);

        ElectricalSimulation simulation =
                new ElectricalSimulation(network);

        assertEquals(
                ThermalStatus.HOT,
                wire.getThermalStatus()
        );

        /*
         * T = 24
         * ambient = 20
         *
         * Cooling = 4 W
         *
         * 6 seconds:
         * 24 J
         *
         * C = 10
         *
         * ΔT = -2.4 °C
         *
         * 24 -> 21.6 °C
         */
        ElectricalSimulationResult result =
                simulation.step(
                        20.0,
                        6.0
                );

        assertEquals(
                21.6,
                wire.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );

        assertEquals(
                ThermalStatus.NORMAL,
                wire.getThermalStatus()
        );

        assertEquals(
                ComponentOperationalStatus.OPERATIONAL,
                wire.getOperationalStatus()
        );

        assertTrue(
                result.hasEvent(
                        ElectricalSimulationEventType.COMPONENT_NORMALIZED
                )
        );
    }

    @Test
    void failedWireShouldNotRecoverWhenItCools() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0
                );

        WireComponent wire =
                new WireComponent(
                        1.0,
                        50.0,
                        new ThermalProperties(
                                10.0,
                                1.0
                        ),
                        new ThermalLimits(
                                30.0,
                                40.0,
                                50.0
                        ),
                        60.0
                );

        /*
         * Avalia o estado térmico sem avançar tempo.
         *
         * 60 °C >= failureTemperature.
         */
        wire.updateThermalState(
                20.0,
                0.0
        );

        assertEquals(
                ComponentOperationalStatus.FAILED,
                wire.getOperationalStatus()
        );

        ElectricalNetwork network =
                new ElectricalNetwork(source);

        network.addComponent(wire);

        ElectricalSimulation simulation =
                new ElectricalSimulation(network);

        /*
         * Sem corrente.
         *
         * Cooling:
         *
         * 1 × (60 - 20) = 40 W
         *
         * 10 seconds:
         * 400 J
         *
         * C = 10
         *
         * ΔT = -40 °C
         *
         * 60 -> 20 °C
         */
        ElectricalSimulationResult result =
                simulation.step(
                        20.0,
                        10.0
                );

        assertEquals(
                20.0,
                wire.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );

        /*
         * Termicamente está frio novamente...
         */
        assertEquals(
                ThermalStatus.NORMAL,
                wire.getThermalStatus()
        );

        /*
         * ...mas continua fisicamente destruído.
         */
        assertEquals(
                ComponentOperationalStatus.FAILED,
                wire.getOperationalStatus()
        );

        assertFalse(
                wire.isOperational()
        );

        /*
         * E não reportamos uma falsa recuperação.
         */
        assertFalse(
                result.hasEvent(
                        ElectricalSimulationEventType.COMPONENT_NORMALIZED
                )
        );

        assertFalse(
                result.hasEvent(
                        ElectricalSimulationEventType.COMPONENT_COOLED
                )
        );
    }

    @Test
    void twentySmallStepsShouldAccumulateOneSecondOfHeating() {
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
                                10.0,
                                0.0
                        ),
                        new ThermalLimits(
                                100.0,
                                200.0,
                                300.0
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

        ElectricalSimulation simulation =
                new ElectricalSimulation(network);

        /*
         * I = 12 / 6 = 2 A
         *
         * Pwire = I²R
         *       = 2² × 1
         *       = 4 W
         *
         * Cada step:
         *
         * E = 4 × 0.05
         *   = 0.2 J
         *
         * C = 10 J/°C
         *
         * ΔT por step = 0.02 °C
         *
         * 20 steps:
         * ΔT = 0.4 °C
         *
         * 20 → 20.4 °C
         */
        for (int i = 0; i < 20; i++) {
            ElectricalSimulationResult result =
                    simulation.step(
                            20.0,
                            STEP_SECONDS
                    );

            assertEquals(
                    ElectricalNetworkStatus.ACTIVE,
                    result.finalNetworkResult()
                            .status()
            );
        }

        assertEquals(
                20.4,
                wire.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );

        assertEquals(
                2.0,
                wire.getElectricalState()
                        .current(),
                DELTA
        );
    }

    @Test
    void wireShouldApproachThermalEquilibriumOverManySteps() {
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
                                10.0,
                                1.0
                        ),
                        new ThermalLimits(
                                100.0,
                                200.0,
                                300.0
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

        ElectricalSimulation simulation =
                new ElectricalSimulation(network);

        /*
         * 2000 steps
         *
         * 2000 × 0.05 = 100 seconds
         */
        for (int i = 0; i < 2000; i++) {
            simulation.step(
                    20.0,
                    STEP_SECONDS
            );

            /*
             * Com esse step pequeno, a temperatura
             * deve se aproximar de 24 °C sem ultrapassar
             * significativamente o equilíbrio.
             */
            assertTrue(
                    wire.getThermalState()
                            .temperatureCelsius()
                            <= 24.0 + DELTA
            );
        }

        assertEquals(
                24.0,
                wire.getThermalState()
                        .temperatureCelsius(),
                0.001
        );

        assertEquals(
                ThermalStatus.NORMAL,
                wire.getThermalStatus()
        );

        assertTrue(
                wire.isOperational()
        );
    }

    @Test
    void wireShouldFailAfterRepeatedSmallSteps() {
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
                                21.5,
                                25.5,
                                29.5
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

        ElectricalSimulation simulation =
                new ElectricalSimulation(network);

        int failureStep = -1;

        for (int step = 1; step <= 60; step++) {
            ElectricalSimulationResult result =
                    simulation.step(
                            20.0,
                            STEP_SECONDS
                    );

            if (result.hasEvent(
                    ElectricalSimulationEventType.COMPONENT_FAILED
            )) {
                failureStep = step;
                break;
            }
        }

        assertEquals(
                48,
                failureStep
        );

        assertEquals(
                ComponentOperationalStatus.FAILED,
                wire.getOperationalStatus()
        );

        assertFalse(
                wire.isOperational()
        );

        assertEquals(
                ElectricalNetworkStatus.OPEN_CIRCUIT,
                network.getResult()
                        .status()
        );
    }

    @Test
    void thermalTransitionEventsShouldOnlyOccurOnce() {
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
                                21.5,
                                25.5,
                                29.5
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

        ElectricalSimulation simulation =
                new ElectricalSimulation(network);

        int hotEvents = 0;
        int overheatedEvents = 0;
        int failedEvents = 0;

        /*
         * Continua alguns ticks depois da falha
         * propositalmente.
         */
        for (int i = 0; i < 60; i++) {
            ElectricalSimulationResult result =
                    simulation.step(
                            20.0,
                            STEP_SECONDS
                    );

            if (result.hasEvent(
                    ElectricalSimulationEventType.COMPONENT_HOT
            )) {
                hotEvents++;
            }

            if (result.hasEvent(
                    ElectricalSimulationEventType.COMPONENT_OVERHEATED
            )) {
                overheatedEvents++;
            }

            if (result.hasEvent(
                    ElectricalSimulationEventType.COMPONENT_FAILED
            )) {
                failedEvents++;
            }
        }

        assertEquals(
                1,
                hotEvents
        );

        assertEquals(
                1,
                overheatedEvents
        );

        assertEquals(
                1,
                failedEvents
        );

        assertEquals(
                ComponentOperationalStatus.FAILED,
                wire.getOperationalStatus()
        );
    }
}