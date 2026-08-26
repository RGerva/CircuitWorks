/**
 * Generic Class: DCVoltageSourceComponentTest <T>
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
import com.rgerva.circuitworks.electrical.thermal.ThermalLimits;
import com.rgerva.circuitworks.electrical.thermal.ThermalProperties;
import com.rgerva.circuitworks.electrical.thermal.ThermalStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DCVoltageSourceComponentTest {

    private static final double DELTA = 1.0E-9;

    @Test
    void shouldStoreVoltage() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        assertEquals(
                12.0,
                source.getVoltage(),
                DELTA
        );
    }

    @Test
    void shouldHavePositiveAndNegativeTerminals() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        assertEquals(
                2,
                source.getPorts().size()
        );

        assertEquals(
                ElectricalPortType.OUTPUT,
                source.getPositiveTerminal().getType()
        );

        assertEquals(
                ElectricalPortType.INPUT,
                source.getNegativeTerminal().getType()
        );
    }

    @Test
    void shouldStartWithZeroState() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        assertEquals(
                ElectricalState.ZERO,
                source.getElectricalState()
        );
    }

    @Test
    void negativeVoltageShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DCVoltageSourceComponent(-12.0)
        );
    }

    @Test
    void infiniteVoltageShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DCVoltageSourceComponent(
                        Double.POSITIVE_INFINITY
                )
        );
    }

    @Test
    void nanVoltageShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DCVoltageSourceComponent(
                        Double.NaN
                )
        );
    }

    @Test
    void idealSourceShouldHaveZeroInternalResistance() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        assertEquals(
                0.0,
                source.getInternalResistance(),
                DELTA
        );
    }

    @Test
    void shouldStoreInternalResistance() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.1
                );

        assertEquals(
                0.1,
                source.getInternalResistance(),
                DELTA
        );
    }

    @Test
    void negativeInternalResistanceShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DCVoltageSourceComponent(
                        12.0,
                        -0.1
                )
        );
    }

    @Test
    void infiniteInternalResistanceShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DCVoltageSourceComponent(
                        12.0,
                        Double.POSITIVE_INFINITY
                )
        );
    }

    @Test
    void nanInternalResistanceShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DCVoltageSourceComponent(
                        12.0,
                        Double.NaN
                )
        );
    }

    @Test
    void defaultSourceShouldHaveUnlimitedCurrent() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(12.0);

        assertEquals(
                Double.POSITIVE_INFINITY,
                source.getMaxCurrent()
        );
    }

    @Test
    void shouldStoreMaximumCurrent() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        5.0
                );

        assertEquals(
                5.0,
                source.getMaxCurrent(),
                DELTA
        );
    }

    @Test
    void zeroMaximumCurrentShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        0.0
                )
        );
    }

    @Test
    void negativeMaximumCurrentShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        -5.0
                )
        );
    }

    @Test
    void nanMaximumCurrentShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        Double.NaN
                )
        );
    }

    @Test
    void sourceShouldHeatFromInternalResistance() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        1.0,
                        50.0,
                        new ThermalProperties(
                                10.0,
                                0.0
                        ),
                        new ThermalLimits(
                                60.0,
                                100.0,
                                180.0
                        ),
                        20.0
                );

        source.updateElectricalState(
                new ElectricalState(
                        12.0,
                        10.0
                )
        );

        source.updateThermalState(
                20.0,
                1.0
        );

        /*
         * P = I²Rinternal
         *
         * P = 10² × 1
         * P = 100 W
         *
         * C = 10 J/°C
         *
         * ΔT = 10 °C
         */

        assertEquals(
                30.0,
                source.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );
    }

    @Test
    void idealSourceShouldNotGenerateInternalHeat() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.0,
                        50.0,
                        new ThermalProperties(
                                10.0,
                                0.0
                        ),
                        new ThermalLimits(
                                60.0,
                                100.0,
                                180.0
                        ),
                        20.0
                );

        source.updateElectricalState(
                new ElectricalState(
                        12.0,
                        100.0
                )
        );

        source.updateThermalState(
                20.0,
                1.0
        );

        assertEquals(
                20.0,
                source.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );
    }

    @Test
    void sourceShouldFailWhenFailureTemperatureIsReached() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        1.0,
                        50.0,
                        new ThermalProperties(
                                10.0,
                                0.0
                        ),
                        new ThermalLimits(
                                30.0,
                                40.0,
                                50.0
                        ),
                        45.0
                );

        source.updateElectricalState(
                new ElectricalState(
                        12.0,
                        10.0
                )
        );

        source.updateThermalState(
                20.0,
                1.0
        );

        assertEquals(
                55.0,
                source.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );

        assertEquals(
                ThermalStatus.FAILED,
                source.getThermalStatus()
        );

        assertEquals(
                ComponentOperationalStatus.FAILED,
                source.getOperationalStatus()
        );

        assertFalse(
                source.isOperational()
        );

        assertEquals(
                ElectricalState.ZERO,
                source.getElectricalState()
        );
    }
}
