/**
 * Record: WorldLoadNode
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

import com.rgerva.circuitworks.electrical.component.ResistiveLoadComponent;
import net.minecraft.core.Direction;

import java.util.Objects;

public record WorldLoadNode(ResistiveLoadComponent component, Direction terminalADirection,
                            Direction terminalBDirection) {

    public WorldLoadNode {
        Objects.requireNonNull(component);
        Objects.requireNonNull(terminalADirection);
        Objects.requireNonNull(terminalBDirection);

        if (terminalBDirection != terminalADirection.getOpposite()) {
            throw new IllegalArgumentException(
                    "Load terminals must be on opposite faces."
            );
        }
    }

    public boolean hasTerminal(Direction direction) {
        return direction == terminalADirection
                || direction == terminalBDirection;
    }

    public boolean isTerminalA(Direction direction) {
        return direction == terminalADirection;
    }

    public boolean isTerminalB(Direction direction) {
        return direction == terminalBDirection;
    }
}