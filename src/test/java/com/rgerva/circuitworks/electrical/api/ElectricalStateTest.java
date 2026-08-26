/**
 * Generic Class: ElectricalStateTest <T>
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

package com.rgerva.circuitworks.electrical.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ElectricalStateTest {
    private static final double DELTA = 1.0E-9;

    @Test
    void shouldCalculatePower() {
        ElectricalState state = new ElectricalState(12.0, 2.0);

        assertEquals(24.0, state.power(), DELTA);
    }

    @Test
    void zeroStateShouldHaveNoPower() {
        assertEquals(0.0, ElectricalState.ZERO.voltage(), DELTA);
        assertEquals(0.0, ElectricalState.ZERO.current(), DELTA);
        assertEquals(0.0, ElectricalState.ZERO.power(), DELTA);
    }
}
