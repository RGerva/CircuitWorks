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

import java.util.List;
import java.util.Objects;

public class ResistiveLoadComponent implements IResistiveComponent {

    private final double resistance;

    private final ElectricalPort terminalA =
            new ElectricalPort("terminal_a", ElectricalPortType.BIDIRECTIONAL);

    private final ElectricalPort terminalB =
            new ElectricalPort("terminal_b", ElectricalPortType.BIDIRECTIONAL);

    private final List<ElectricalPort> ports =
            List.of(terminalA, terminalB);

    private ElectricalState state = ElectricalState.ZERO;

    public ResistiveLoadComponent(double resistance) {
        if (!Double.isFinite(resistance) || resistance <= ElectricalConstants.EPSILON) {
            throw new IllegalArgumentException("Resistance must be a finite value greater than zero.");
        }

        this.resistance = resistance;
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
}