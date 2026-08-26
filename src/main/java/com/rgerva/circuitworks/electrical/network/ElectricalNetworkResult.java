/**
 * Record: ElectricalNetworkResult
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

import com.rgerva.circuitworks.electrical.api.ElectricalState;

import java.util.List;

public record ElectricalNetworkResult(ElectricalNetworkStatus status, ElectricalState state,
                                      double equivalentResistance, List<ElectricalFault> faults) {

    public ElectricalNetworkResult {
        faults = List.copyOf(faults);
    }

    public ElectricalNetworkResult(
            ElectricalNetworkStatus status,
            ElectricalState state,
            double equivalentResistance
    ) {
        this(
                status,
                state,
                equivalentResistance,
                List.of()
        );
    }

    public boolean isFault() {
        return status.isFault();
    }

    public boolean isActive() {
        return status
                == ElectricalNetworkStatus.ACTIVE;
    }

    public boolean hasFault(
            ElectricalFaultType type
    ) {
        return faults.stream()
                .anyMatch(fault ->
                        fault.type() == type
                );
    }
}
