/**
 * Record: ElectricalFault
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

package com.rgerva.circuitworks.electrical.network;

import com.rgerva.circuitworks.electrical.component.IElectricalComponent;

import java.util.Objects;

public record ElectricalFault(
        ElectricalFaultType type,
        IElectricalComponent component,
        double measuredValue,
        double limit
) {

    public ElectricalFault {
        Objects.requireNonNull(type);
        Objects.requireNonNull(component);

        if (!Double.isFinite(measuredValue)
                || measuredValue < 0.0) {

            throw new IllegalArgumentException(
                    "Measured value must be finite and greater than or equal to zero."
            );
        }

        if (Double.isNaN(limit)
                || limit <= 0.0) {

            throw new IllegalArgumentException(
                    "Limit must be greater than zero."
            );
        }
    }

    public double ratio() {
        if (Double.isInfinite(limit)) {
            return 0.0;
        }

        return measuredValue / limit;
    }

    public ElectricalFaultSeverity severity() {
        double ratio = ratio();

        if (ratio <= 1.0) {
            return ElectricalFaultSeverity.NONE;
        }

        if (ratio <= 1.25) {
            return ElectricalFaultSeverity.LOW;
        }

        if (ratio <= 2.0) {
            return ElectricalFaultSeverity.MODERATE;
        }

        if (ratio <= 5.0) {
            return ElectricalFaultSeverity.HIGH;
        }

        return ElectricalFaultSeverity.CRITICAL;
    }
}
