/**
 * Generic Class: ThermalSimulatorTest <T>
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

package com.rgerva.circuitworks.electrical.thermal;

import com.rgerva.circuitworks.electrical.api.ElectricalState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThermalSimulatorTest {

    private static final double DELTA = 1.0E-9;

    @Test
    void electricalCurrentShouldHeatComponent() {
        ElectricalState electrical =
                new ElectricalState(
                        10.0,
                        10.0
                );

        ThermalState thermal =
                new ThermalState(20.0);

        ThermalProperties properties =
                new ThermalProperties(
                        10.0,
                        1.0
                );

        ThermalState result =
                ThermalSimulator.step(
                        electrical,
                        1.0,
                        thermal,
                        properties,
                        20.0,
                        1.0
                );

        /*
         * I = 10 A
         * R = 1 Ω
         *
         * P = I²R
         * P = 100 W
         *
         * At 20 °C with ambient 20 °C:
         * cooling = 0 W
         *
         * Energy after 1 second = 100 J
         *
         * C = 10 J/°C
         *
         * ΔT = 10 °C
         */

        assertEquals(
                30.0,
                result.temperatureCelsius(),
                DELTA
        );
    }

    @Test
    void hotComponentShouldCoolWithoutCurrent() {
        ElectricalState electrical =
                ElectricalState.ZERO;

        ThermalState thermal =
                new ThermalState(100.0);

        ThermalProperties properties =
                new ThermalProperties(
                        10.0,
                        1.0
                );

        ThermalState result =
                ThermalSimulator.step(
                        electrical,
                        1.0,
                        thermal,
                        properties,
                        20.0,
                        1.0
                );

        /*
         * Heating = 0
         *
         * Cooling:
         * 1 × (100 - 20)
         * = 80 W
         *
         * Energy lost in 1 sec:
         * 80 J
         *
         * ΔT = -80 / 10
         * ΔT = -8 °C
         */

        assertEquals(
                92.0,
                result.temperatureCelsius(),
                DELTA
        );
    }

    @Test
    void heatingAndCoolingShouldBeCombined() {
        ElectricalState electrical =
                new ElectricalState(
                        10.0,
                        10.0
                );

        ThermalState thermal =
                new ThermalState(30.0);

        ThermalProperties properties =
                new ThermalProperties(
                        10.0,
                        1.0
                );

        ThermalState result =
                ThermalSimulator.step(
                        electrical,
                        1.0,
                        thermal,
                        properties,
                        20.0,
                        1.0
                );

        /*
         * Heating:
         * 10² × 1 = 100 W
         *
         * Cooling:
         * 1 × (30 - 20) = 10 W
         *
         * Net:
         * 90 W
         *
         * ΔT:
         * 90 / 10 = 9 °C
         */

        assertEquals(
                39.0,
                result.temperatureCelsius(),
                DELTA
        );
    }

    @Test
    void componentAtAmbientTemperatureShouldRemainStable() {
        ThermalState thermal =
                new ThermalState(20.0);

        ThermalProperties properties =
                new ThermalProperties(
                        10.0,
                        1.0
                );

        ThermalState result =
                ThermalSimulator.step(
                        ElectricalState.ZERO,
                        1.0,
                        thermal,
                        properties,
                        20.0,
                        1.0
                );

        assertEquals(
                20.0,
                result.temperatureCelsius(),
                DELTA
        );
    }

    @Test
    void idealWireShouldNotGenerateJouleHeating() {
        ElectricalState electrical =
                new ElectricalState(
                        0.0,
                        120.0
                );

        ThermalState thermal =
                new ThermalState(20.0);

        ThermalProperties properties =
                new ThermalProperties(
                        10.0,
                        1.0
                );

        ThermalState result =
                ThermalSimulator.step(
                        electrical,
                        0.0,
                        thermal,
                        properties,
                        20.0,
                        1.0
                );

        assertEquals(
                20.0,
                result.temperatureCelsius(),
                DELTA
        );
    }
}