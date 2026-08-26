/**
 * Record: ThermalState
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

package com.rgerva.circuitworks.electrical.thermal;

public record ThermalState(double temperatureCelsius) {

    public ThermalState {
        if (!Double.isFinite(temperatureCelsius)) {
            throw new IllegalArgumentException("Temperature must be finite.");
        }
    }
}
