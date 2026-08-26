/**
 * Record: ElectricalSimulationResult
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

import com.rgerva.circuitworks.electrical.network.ElectricalNetworkResult;

import java.util.List;
import java.util.Objects;

public record ElectricalSimulationResult(
        ElectricalNetworkResult initialNetworkResult,
        ElectricalNetworkResult finalNetworkResult,
        List<ElectricalSimulationEvent> events
) {

    public ElectricalSimulationResult {
        Objects.requireNonNull(initialNetworkResult);
        Objects.requireNonNull(finalNetworkResult);

        events = List.copyOf(
                Objects.requireNonNull(events)
        );
    }

    public boolean hasEvents() {
        return !events.isEmpty();
    }

    public boolean hasEvent(
            ElectricalSimulationEventType type
    ) {
        return events.stream()
                .anyMatch(event ->
                        event.type() == type
                );
    }
}
