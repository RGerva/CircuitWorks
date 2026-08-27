/**
 * Generic Class: DCVoltageSourceComponent <T>
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

public class DCVoltageSourceComponent implements IElectricalSource, IOperationalComponent, IThermalComponent {

    private final double voltage;
    private final double internalResistance;
    private final double maxCurrent;

    public static final double DEFAULT_INITIAL_TEMPERATURE = 20.0;

    public static final ThermalProperties DEFAULT_THERMAL_PROPERTIES = new ThermalProperties(500.0, 1.0);
    public static final ThermalLimits DEFAULT_THERMAL_LIMITS = new ThermalLimits(60.0, 100.0, 180.0);
    private final ThermalProperties thermalProperties;
    private final ThermalLimits thermalLimits;
    private ThermalState thermalState;
    private ComponentOperationalStatus operationalStatus = ComponentOperationalStatus.OPERATIONAL;

    private final ElectricalPort positiveTerminal = new ElectricalPort("positive", ElectricalPortType.OUTPUT);
    private final ElectricalPort negativeTerminal = new ElectricalPort("negative", ElectricalPortType.INPUT);
    private final List<ElectricalPort> ports = List.of(positiveTerminal, negativeTerminal);
    private ElectricalState state = ElectricalState.ZERO;

    public DCVoltageSourceComponent(
            double voltage
    ) {
        this(
                voltage,
                0.0,
                Double.POSITIVE_INFINITY
        );
    }

    public DCVoltageSourceComponent(
            double voltage,
            double internalResistance
    ) {
        this(
                voltage,
                internalResistance,
                Double.POSITIVE_INFINITY
        );
    }

    public DCVoltageSourceComponent(
            double voltage,
            double internalResistance,
            double maxCurrent
    ) {
        this(
                voltage,
                internalResistance,
                maxCurrent,
                DEFAULT_THERMAL_PROPERTIES,
                DEFAULT_THERMAL_LIMITS,
                DEFAULT_INITIAL_TEMPERATURE
        );
    }

    public DCVoltageSourceComponent(
            double voltage,
            double internalResistance,
            double maxCurrent,
            ThermalProperties thermalProperties,
            ThermalLimits thermalLimits,
            double initialTemperature
    ) {
        this(
                voltage,
                internalResistance,
                maxCurrent,
                thermalProperties,
                thermalLimits,
                initialTemperature,
                ComponentOperationalStatus.OPERATIONAL
        );
    }

    public DCVoltageSourceComponent(
            double voltage,
            double internalResistance,
            double maxCurrent,
            ThermalProperties thermalProperties,
            ThermalLimits thermalLimits,
            double initialTemperature,
            ComponentOperationalStatus operationalStatus
    ) {
        if (!Double.isFinite(voltage) || voltage < 0.0) {
            throw new IllegalArgumentException(
                    "Voltage must be finite and >= 0."
            );
        }

        if (!Double.isFinite(internalResistance)
                || internalResistance < 0.0) {
            throw new IllegalArgumentException(
                    "Internal resistance must be finite and >= 0."
            );
        }

        if (Double.isNaN(maxCurrent) || maxCurrent <= 0.0) {
            throw new IllegalArgumentException(
                    "Max current must be > 0."
            );
        }

        this.voltage = voltage;
        this.internalResistance = internalResistance;
        this.maxCurrent = maxCurrent;
        this.thermalProperties =
                Objects.requireNonNull(thermalProperties);
        this.thermalLimits =
                Objects.requireNonNull(thermalLimits);
        this.thermalState =
                new ThermalState(initialTemperature);
        this.operationalStatus =
                Objects.requireNonNull(operationalStatus);

        if (!operationalStatus.isOperational()) {
            this.state = ElectricalState.ZERO;
        }
    }

    @Override
    public double getVoltage() {
        return voltage;
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
    public void updateElectricalState(ElectricalState state) {
        this.state = Objects.requireNonNull(state);
    }

    @Override
    public ElectricalPort getPositiveTerminal() {
        return positiveTerminal;
    }

    @Override
    public ElectricalPort getNegativeTerminal() {
        return negativeTerminal;
    }

    @Override
    public double getInternalResistance() {
        return internalResistance;
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
    public ThermalLimits getThermalLimits() {
        return thermalLimits;
    }

    @Override
    public ThermalStatus getThermalStatus() {
        return thermalLimits.getStatus(
                thermalState.temperatureCelsius()
        );
    }

    @Override
    public ComponentOperationalStatus getOperationalStatus() {
        return operationalStatus;
    }

    @Override
    public void updateThermalState(
            double ambientTemperature,
            double deltaSeconds
    ) {
        thermalState =
                ThermalSimulator.step(
                        state,
                        internalResistance,
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
}
