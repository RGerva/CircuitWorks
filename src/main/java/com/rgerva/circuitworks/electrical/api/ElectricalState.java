/**
 * Record: ElectricalState
 * Immutable data structure for simplified object representation.
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

public record ElectricalState(double voltage, double current) {

    public static final ElectricalState ZERO = new ElectricalState(0.0, 0.0);

    public double power() {
        return voltage * current;
    }
}
