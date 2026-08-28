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
import com.rgerva.circuitworks.electrical.thermal.ThermalStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void resistiveLoadShouldHeatFromCurrent() {
        ResistiveLoadComponent load =
                new ResistiveLoadComponent(10.0);

        double initialTemperature =
                load.getThermalState()
                        .temperatureCelsius();

        load.updateElectricalState(
                new ElectricalState(
                        10.0,
                        1.0
                )
        );

        load.updateThermalState(
                20.0,
                1.0
        );

        assertTrue(
                load.getThermalState()
                        .temperatureCelsius()
                        > initialTemperature
        );
    }

    @Test
    void restoredHotLoadShouldKeepThermalState() {
        ResistiveLoadComponent load =
                new ResistiveLoadComponent(
                        10.0,
                        ResistiveLoadComponent.DEFAULT_THERMAL_PROPERTIES,
                        ResistiveLoadComponent.DEFAULT_THERMAL_LIMITS,
                        75.0,
                        ComponentOperationalStatus.OPERATIONAL
                );

        assertEquals(
                75.0,
                load.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );

        assertEquals(
                ThermalStatus.HOT,
                load.getThermalStatus()
        );

        assertTrue(load.isOperational());
    }

    @Test
    void restoredFailedLoadShouldRemainFailed() {
        ResistiveLoadComponent load =
                new ResistiveLoadComponent(
                        10.0,
                        ResistiveLoadComponent.DEFAULT_THERMAL_PROPERTIES,
                        ResistiveLoadComponent.DEFAULT_THERMAL_LIMITS,
                        150.0,
                        ComponentOperationalStatus.FAILED
                );

        assertFalse(load.isOperational());

        assertEquals(
                ComponentOperationalStatus.FAILED,
                load.getOperationalStatus()
        );

        assertEquals(
                0.0,
                load.getElectricalState().current(),
                DELTA
        );
    }

    @Test
    void resistanceCanBeChangedAtRuntime() {
        ResistiveLoadComponent load =
                new ResistiveLoadComponent(10.0);

        load.setResistance(20.0);

        assertEquals(
                20.0,
                load.getResistance(),
                DELTA
        );
    }

    @Test
    void resistanceCannotBeZero() {
        ResistiveLoadComponent load =
                new ResistiveLoadComponent(10.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> load.setResistance(0.0)
        );
    }

    @Test
    void resistanceCannotBeNegative() {
        ResistiveLoadComponent load =
                new ResistiveLoadComponent(10.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> load.setResistance(-1.0)
        );
    }

    @Test
    void resistanceCannotBeNaN() {
        ResistiveLoadComponent load =
                new ResistiveLoadComponent(10.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> load.setResistance(Double.NaN)
        );
    }
}
