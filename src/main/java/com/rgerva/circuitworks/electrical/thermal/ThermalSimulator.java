/**
 * Generic Class: ThermalSimulator <T>
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

public final class ThermalSimulator {

    private ThermalSimulator() {
    }

    public static ThermalState step(
            ElectricalState electricalState,
            double resistance,
            ThermalState thermalState,
            ThermalProperties properties,
            double ambientTemperature,
            double deltaSeconds
    ) {
        if (!Double.isFinite(resistance)
                || resistance < 0.0) {

            throw new IllegalArgumentException(
                    "Resistance must be finite and greater than or equal to zero."
            );
        }

        if (!Double.isFinite(ambientTemperature)) {
            throw new IllegalArgumentException(
                    "Ambient temperature must be finite."
            );
        }

        if (!Double.isFinite(deltaSeconds)
                || deltaSeconds < 0.0) {

            throw new IllegalArgumentException(
                    "Delta time must be finite and greater than or equal to zero."
            );
        }

        double current =
                electricalState.current();

        /*
         * Joule heating:
         *
         * P = I²R
         */
        double heatingPower =
                current
                        * current
                        * resistance;

        /*
         * Simplified heat loss:
         *
         * Pcool = k(T - Tambient)
         */
        double coolingPower =
                properties.coolingCoefficient()
                        * (
                        thermalState.temperatureCelsius()
                                - ambientTemperature
                );

        /*
         * Net thermal power.
         */
        double netPower =
                heatingPower - coolingPower;

        /*
         * Energy accumulated during the interval:
         *
         * E = P × t
         */
        double energy =
                netPower * deltaSeconds;

        /*
         * Temperature variation:
         *
         * ΔT = E / C
         */
        double temperatureChange =
                energy / properties.heatCapacity();

        return new ThermalState(
                thermalState.temperatureCelsius()
                        + temperatureChange
        );
    }
}