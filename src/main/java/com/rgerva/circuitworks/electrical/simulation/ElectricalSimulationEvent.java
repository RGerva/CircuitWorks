/**
 * Record: ElectricalSimulationEvent
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

package com.rgerva.circuitworks.electrical.simulation;

import com.rgerva.circuitworks.electrical.component.IElectricalComponent;

import java.util.Objects;

public record ElectricalSimulationEvent(
        ElectricalSimulationEventType type,
        IElectricalComponent component,
        double temperatureCelsius
) {

    public ElectricalSimulationEvent {
        Objects.requireNonNull(type);
        Objects.requireNonNull(component);

        if (!Double.isFinite(temperatureCelsius)) {
            throw new IllegalArgumentException(
                    "Event temperature must be finite."
            );
        }
    }
}