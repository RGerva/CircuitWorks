/**
 * Record: ThermalProperties
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

public record ThermalProperties(double heatCapacity, double coolingCoefficient) {

    public ThermalProperties {
        if (!Double.isFinite(heatCapacity) || heatCapacity <= 0.0) {
            throw new IllegalArgumentException("Heat capacity must be finite and greater than zero.");
        }

        if (!Double.isFinite(coolingCoefficient) || coolingCoefficient < 0.0) {
            throw new IllegalArgumentException("Cooling coefficient must be finite and greater than or equal to zero.");
        }
    }
}
