/**
 * Record: WorldSourceNode
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

package com.rgerva.circuitworks.electrical.world;

import com.rgerva.circuitworks.electrical.component.DCVoltageSourceComponent;
import net.minecraft.core.Direction;

import java.util.Objects;

public record WorldSourceNode(DCVoltageSourceComponent component, Direction positiveDirection,
                              Direction negativeDirection) {

    public WorldSourceNode {
        Objects.requireNonNull(component);
        Objects.requireNonNull(positiveDirection);
        Objects.requireNonNull(negativeDirection);

        if (negativeDirection != positiveDirection.getOpposite()) {
            throw new IllegalArgumentException(
                    "Source terminals must be on opposite faces."
            );
        }
    }

    public boolean hasTerminal(Direction direction) {
        return direction == positiveDirection
                || direction == negativeDirection;
    }

    public boolean isPositiveTerminal(Direction direction) {
        return direction == positiveDirection;
    }

    public boolean isNegativeTerminal(Direction direction) {
        return direction == negativeDirection;
    }
}
