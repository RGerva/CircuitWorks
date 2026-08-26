/**
 * Enum: ElectricalNetworkStatus
 * Represents predefined constants for a specific purpose.
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

public enum ElectricalNetworkStatus {

    UNRESOLVED,
    NO_SOURCE,
    SOURCE_FAILED,
    INACTIVE,
    OPEN_CIRCUIT,
    ACTIVE,
    OVERCURRENT,
    SHORT_CIRCUIT;

    public boolean isFault() {
        return this == SOURCE_FAILED || this == OVERCURRENT || this == SHORT_CIRCUIT;
    }
}
