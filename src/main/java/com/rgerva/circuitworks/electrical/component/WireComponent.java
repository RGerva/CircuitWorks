/**
 * Generic Class: WireComponent <T>
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

package com.rgerva.circuitworks.electrical.component;

import com.rgerva.circuitworks.electrical.api.ElectricalPort;
import com.rgerva.circuitworks.electrical.api.ElectricalPortType;
import com.rgerva.circuitworks.electrical.api.ElectricalState;
import com.rgerva.circuitworks.electrical.thermal.*;

import java.util.List;
import java.util.Objects;

public class WireComponent implements IResistiveComponent, ICurrentLimitedComponent, IOperationalComponent, IThermalComponent {

    public static final double IDEAL_RESISTANCE = 0.0;
    private final double resistance;
    private final double maxCurrent;

    private final ThermalProperties thermalProperties;
    private ThermalState thermalState;
    public static final double DEFAULT_INITIAL_TEMPERATURE = 20.0;
    public static final ThermalProperties DEFAULT_THERMAL_PROPERTIES = new ThermalProperties(100.0, 0.5);
    public static final ThermalLimits DEFAULT_THERMAL_LIMITS = new ThermalLimits(60.0, 100.0, 180.0);
    private final ThermalLimits thermalLimits;

    private ComponentOperationalStatus operationalStatus = ComponentOperationalStatus.OPERATIONAL;

    private final ElectricalPort terminalA =
            new ElectricalPort(
                    "terminal_a",
                    ElectricalPortType.BIDIRECTIONAL
            );

    private final ElectricalPort terminalB =
            new ElectricalPort(
                    "terminal_b",
                    ElectricalPortType.BIDIRECTIONAL
            );

    private final List<ElectricalPort> ports =
            List.of(terminalA, terminalB);

    private ElectricalState state =
            ElectricalState.ZERO;

    public WireComponent() {
        this(
                IDEAL_RESISTANCE,
                Double.POSITIVE_INFINITY
        );
    }

    public WireComponent(double resistance) {
        this(
                resistance,
                Double.POSITIVE_INFINITY
        );
    }

    public WireComponent(
            double resistance,
            double maxCurrent
    ) {
        this(
                resistance,
                maxCurrent,
                DEFAULT_THERMAL_PROPERTIES,
                DEFAULT_THERMAL_LIMITS,
                DEFAULT_INITIAL_TEMPERATURE
        );
    }

    public WireComponent(
            double resistance,
            double maxCurrent,
            ThermalProperties thermalProperties,
            double initialTemperature
    ) {
        this(
                resistance,
                maxCurrent,
                thermalProperties,
                DEFAULT_THERMAL_LIMITS,
                initialTemperature
        );
    }

    public WireComponent(
            double resistance,
            double maxCurrent,
            ThermalProperties thermalProperties,
            ThermalLimits thermalLimits,
            double initialTemperature
    ) {
        this(
                resistance,
                maxCurrent,
                thermalProperties,
                thermalLimits,
                initialTemperature,
                ComponentOperationalStatus.OPERATIONAL
        );
    }

    public WireComponent(
            double resistance,
            double maxCurrent,
            ThermalProperties thermalProperties,
            ThermalLimits thermalLimits,
            double initialTemperature,
            ComponentOperationalStatus operationalStatus
    ) {
        if (!Double.isFinite(resistance) || resistance < 0.0) {
            throw new IllegalArgumentException("Resistance must be finite and >= 0.");
        }

        if (Double.isNaN(maxCurrent) || maxCurrent <= 0.0) {
            throw new IllegalArgumentException("Max current must be > 0.");
        }

        this.resistance = resistance;
        this.maxCurrent = maxCurrent;
        this.thermalProperties = Objects.requireNonNull(thermalProperties);
        this.thermalLimits = Objects.requireNonNull(thermalLimits);
        this.thermalState = new ThermalState(initialTemperature);
        this.operationalStatus = Objects.requireNonNull(operationalStatus);

        if (!operationalStatus.isOperational()) {
            this.state = ElectricalState.ZERO;
        }
    }

    @Override
    public double getResistance() {
        return resistance;
    }

    @Override
    public List<ElectricalPort> getPorts() {
        return ports;
    }

    @Override
    public ElectricalState getElectricalState() {
        return state;
    }

    @Override
    public void updateElectricalState(
            ElectricalState state
    ) {
        this.state = Objects.requireNonNull(state);
    }

    public ElectricalPort getTerminalA() {
        return terminalA;
    }

    public ElectricalPort getTerminalB() {
        return terminalB;
    }

    @Override
    public double getMaxCurrent() {
        return maxCurrent;
    }


    /* THERMAL */
    @Override
    public ThermalState getThermalState() {
        return thermalState;
    }

    @Override
    public ThermalProperties getThermalProperties() {
        return thermalProperties;
    }

    @Override
    public void updateThermalState(
            double ambientTemperature,
            double deltaSeconds
    ) {
        thermalState =
                ThermalSimulator.step(
                        state,
                        resistance,
                        thermalState,
                        thermalProperties,
                        ambientTemperature,
                        deltaSeconds
                );

        if (getThermalStatus() == ThermalStatus.FAILED) {
            operationalStatus =
                    ComponentOperationalStatus.FAILED;

            state = ElectricalState.ZERO;
        }
    }

    @Override
    public ThermalLimits getThermalLimits() {
        return thermalLimits;
    }

    @Override
    public ThermalStatus getThermalStatus() {
        return thermalLimits.getStatus(
                thermalState.temperatureCelsius()
        );
    }

    /* OPERATIONAL STATUS */
    @Override
    public ComponentOperationalStatus getOperationalStatus() {
        return operationalStatus;
    }
}
