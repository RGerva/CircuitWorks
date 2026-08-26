/**
 * Generic Class: ResistiveLoadComponentTest <T>
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

package com.rgerva.circuitworks.electrical.component;

import com.rgerva.circuitworks.electrical.api.ElectricalPortType;
import com.rgerva.circuitworks.electrical.api.ElectricalState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResistiveLoadComponentTest {

    private static final double DELTA = 1.0E-9;

    @Test
    void shouldStoreResistance() {
        ResistiveLoadComponent load =
                new ResistiveLoadComponent(6.0);

        assertEquals(6.0, load.getResistance(), DELTA);
    }

    @Test
    void shouldHaveTwoBidirectionalPorts() {
        ResistiveLoadComponent load =
                new ResistiveLoadComponent(6.0);

        assertEquals(2, load.getPorts().size());

        assertEquals(
                ElectricalPortType.BIDIRECTIONAL,
                load.getPorts().get(0).getType()
        );

        assertEquals(
                ElectricalPortType.BIDIRECTIONAL,
                load.getPorts().get(1).getType()
        );
    }

    @Test
    void shouldStartWithZeroElectricalState() {
        ResistiveLoadComponent load =
                new ResistiveLoadComponent(6.0);

        assertEquals(
                ElectricalState.ZERO,
                load.getElectricalState()
        );
    }

    @Test
    void shouldUpdateElectricalState() {
        ResistiveLoadComponent load =
                new ResistiveLoadComponent(6.0);

        ElectricalState state =
                new ElectricalState(12.0, 2.0);

        load.updateElectricalState(state);

        assertEquals(state, load.getElectricalState());
        assertEquals(24.0, load.getElectricalState().power(), DELTA);
    }

    @Test
    void zeroResistanceShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ResistiveLoadComponent(0.0)
        );
    }

    @Test
    void negativeResistanceShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ResistiveLoadComponent(-5.0)
        );
    }

    @Test
    void infiniteResistanceShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ResistiveLoadComponent(
                        Double.POSITIVE_INFINITY
                )
        );
    }

    @Test
    void nanResistanceShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ResistiveLoadComponent(Double.NaN)
        );
    }
}
