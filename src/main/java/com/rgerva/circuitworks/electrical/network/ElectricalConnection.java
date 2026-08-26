/**
 * Generic Class: ElectricalConnection <T>
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

package com.rgerva.circuitworks.electrical.network;

import com.rgerva.circuitworks.electrical.api.ElectricalPort;

import java.util.Objects;

public class ElectricalConnection {
    private final ElectricalPort first;
    private final ElectricalPort second;

    public ElectricalConnection(
            ElectricalPort first,
            ElectricalPort second
    ) {
        this.first = Objects.requireNonNull(first);
        this.second = Objects.requireNonNull(second);

        if (!first.canConnectTo(second)) {
            throw new IllegalArgumentException(
                    "Electrical ports cannot be connected."
            );
        }
    }

    public ElectricalPort getFirst() {
        return first;
    }

    public ElectricalPort getSecond() {
        return second;
    }

    public boolean contains(ElectricalPort port) {
        return first == port || second == port;
    }

    public ElectricalPort getOther(ElectricalPort port) {
        if (first == port) {
            return second;
        }

        if (second == port) {
            return first;
        }

        throw new IllegalArgumentException(
                "The provided port is not part of this connection."
        );
    }
}
