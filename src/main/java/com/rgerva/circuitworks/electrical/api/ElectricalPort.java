/**
 * Generic Class: ElectricalPort <T>
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

package com.rgerva.circuitworks.electrical.api;

import java.util.Objects;

public class ElectricalPort {
    private final String id;
    private final ElectricalPortType type;

    private ElectricalState state = ElectricalState.ZERO;

    public ElectricalPort(String id, ElectricalPortType type) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
    }

    public boolean canConnectTo(ElectricalPort other) {
        if (other == null || other == this) {
            return false;
        }

        if (type == ElectricalPortType.BIDIRECTIONAL
                || other.type == ElectricalPortType.BIDIRECTIONAL) {
            return true;
        }

        return type != other.type;
    }

    public String getId() {
        return id;
    }

    public ElectricalPortType getType() {
        return type;
    }

    public ElectricalState getState() {
        return state;
    }

    public void setState(ElectricalState state) {
        this.state = Objects.requireNonNull(state);
    }
}
