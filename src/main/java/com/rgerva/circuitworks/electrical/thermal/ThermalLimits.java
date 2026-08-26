/**
 * Record: ThermalLimits
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

public record ThermalLimits(
        double hotTemperature,
        double maxTemperature,
        double failureTemperature
) {

    public ThermalLimits {
        if (!Double.isFinite(hotTemperature)
                || !Double.isFinite(maxTemperature)
                || !Double.isFinite(failureTemperature)) {

            throw new IllegalArgumentException(
                    "Thermal limits must be finite."
            );
        }

        if (hotTemperature >= maxTemperature) {
            throw new IllegalArgumentException(
                    "Hot temperature must be lower than maximum temperature."
            );
        }

        if (maxTemperature >= failureTemperature) {
            throw new IllegalArgumentException(
                    "Maximum temperature must be lower than failure temperature."
            );
        }
    }

    public ThermalStatus getStatus(double temperature) {
        if (temperature >= failureTemperature) {
            return ThermalStatus.FAILED;
        }

        if (temperature >= maxTemperature) {
            return ThermalStatus.OVERHEATED;
        }

        if (temperature >= hotTemperature) {
            return ThermalStatus.HOT;
        }

        return ThermalStatus.NORMAL;
    }
}
