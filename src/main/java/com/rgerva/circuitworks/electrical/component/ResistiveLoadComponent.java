/**
 * Generic Class: ResistiveLoadComponent <T>
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

import com.rgerva.circuitworks.electrical.api.ElectricalConstants;
import com.rgerva.circuitworks.electrical.api.ElectricalPort;
import com.rgerva.circuitworks.electrical.api.ElectricalPortType;
import com.rgerva.circuitworks.electrical.api.ElectricalState;
import com.rgerva.circuitworks.electrical.thermal.*;

import java.util.List;
import java.util.Objects;

public class ResistiveLoadComponent implements IResistiveComponent, IOperationalComponent, IThermalComponent {

    private double resistance;

    private final ElectricalPort terminalA =
            new ElectricalPort("terminal_a", ElectricalPortType.BIDIRECTIONAL);

    private final ElectricalPort terminalB =
            new ElectricalPort("terminal_b", ElectricalPortType.BIDIRECTIONAL);

    private final List<ElectricalPort> ports = List.of(terminalA, terminalB);

    private ElectricalState state = ElectricalState.ZERO;

    public static final double DEFAULT_INITIAL_TEMPERATURE = 20.0;

    public static final ThermalProperties DEFAULT_THERMAL_PROPERTIES =
            new ThermalProperties(200.0, 1.0);

    public static final ThermalLimits DEFAULT_THERMAL_LIMITS =
            new ThermalLimits(60.0, 100.0, 180.0);

    private final ThermalProperties thermalProperties;
    private final ThermalLimits thermalLimits;

    private ThermalState thermalState;
    private ComponentOperationalStatus operationalStatus;

    public ResistiveLoadComponent(double resistance) {
        this(resistance, DEFAULT_THERMAL_PROPERTIES, DEFAULT_THERMAL_LIMITS,
                DEFAULT_INITIAL_TEMPERATURE, ComponentOperationalStatus.OPERATIONAL);
    }

    public ResistiveLoadComponent(double resistance, ThermalProperties thermalProperties, ThermalLimits thermalLimits,
                                  double initialTemperature, ComponentOperationalStatus operationalStatus) {

        if (!Double.isFinite(resistance) || resistance <= ElectricalConstants.EPSILON) {
            throw new IllegalArgumentException("Resistance must be finite and > EPSILON.");
        }

        this.resistance = validateResistance(resistance);
        this.thermalProperties = Objects.requireNonNull(thermalProperties);
        this.thermalLimits = Objects.requireNonNull(thermalLimits);
        this.thermalState = new ThermalState(initialTemperature);
        this.operationalStatus = Objects.requireNonNull(operationalStatus);

        if (!operationalStatus.isOperational()) {
            this.state = ElectricalState.ZERO;
        }
    }

    private static double validateResistance(double resistance) {
        if (!Double.isFinite(resistance) || resistance <= 0.0) {
            throw new IllegalArgumentException("Resistance must be finite and greater than zero.");
        }
        return resistance;
    }

    public void setResistance(double resistance) {
        this.resistance = validateResistance(resistance);
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
    public void updateElectricalState(ElectricalState state) {
        this.state = Objects.requireNonNull(state);
    }

    public ElectricalPort getTerminalA() {
        return terminalA;
    }

    public ElectricalPort getTerminalB() {
        return terminalB;
    }

    /* THERMAL */

    @Override
    public ComponentOperationalStatus getOperationalStatus() {
        return operationalStatus;
    }

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
        return thermalLimits.getStatus(thermalState.temperatureCelsius());
    }

    @Override
    public void updateThermalState(double ambientTemperature, double deltaSeconds) {
        thermalState = ThermalSimulator.step(state, resistance, thermalState,
                thermalProperties, ambientTemperature, deltaSeconds);

        if (getThermalStatus() == ThermalStatus.FAILED) {
            operationalStatus = ComponentOperationalStatus.FAILED;
            state = ElectricalState.ZERO;
        }
    }
}