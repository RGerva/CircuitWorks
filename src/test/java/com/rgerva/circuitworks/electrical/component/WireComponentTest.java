/**
 * Generic Class: WireComponentTest <T>
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

class WireComponentTest {

    private static final double DELTA = 1.0E-9;

    @Test
    void defaultWireShouldHaveZeroResistance() {
        WireComponent wire =
                new WireComponent();

        assertEquals(
                0.0,
                wire.getResistance(),
                DELTA
        );
    }

    @Test
    void wireShouldAllowCustomResistance() {
        WireComponent wire =
                new WireComponent(0.25);

        assertEquals(
                0.25,
                wire.getResistance(),
                DELTA
        );
    }

    @Test
    void shouldHaveTwoBidirectionalPorts() {
        WireComponent wire =
                new WireComponent();

        assertEquals(
                2,
                wire.getPorts().size()
        );

        assertEquals(
                ElectricalPortType.BIDIRECTIONAL,
                wire.getTerminalA().getType()
        );

        assertEquals(
                ElectricalPortType.BIDIRECTIONAL,
                wire.getTerminalB().getType()
        );
    }

    @Test
    void shouldStartWithZeroElectricalState() {
        WireComponent wire =
                new WireComponent();

        assertEquals(
                ElectricalState.ZERO,
                wire.getElectricalState()
        );
    }

    @Test
    void shouldUpdateElectricalState() {
        WireComponent wire =
                new WireComponent();

        ElectricalState state =
                new ElectricalState(0.0, 2.0);

        wire.updateElectricalState(state);

        assertEquals(
                state,
                wire.getElectricalState()
        );
    }

    @Test
    void negativeResistanceShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WireComponent(-1.0)
        );
    }

    @Test
    void infiniteResistanceShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WireComponent(
                        Double.POSITIVE_INFINITY
                )
        );
    }

    @Test
    void nanResistanceShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WireComponent(
                        Double.NaN
                )
        );
    }

    @Test
    void defaultWireShouldHaveUnlimitedCurrent() {
        WireComponent wire =
                new WireComponent();

        assertEquals(
                Double.POSITIVE_INFINITY,
                wire.getMaxCurrent()
        );
    }

    @Test
    void shouldStoreMaximumCurrent() {
        WireComponent wire =
                new WireComponent(
                        0.01,
                        5.0
                );

        assertEquals(
                5.0,
                wire.getMaxCurrent(),
                DELTA
        );
    }

    @Test
    void zeroMaximumCurrentShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WireComponent(
                        0.01,
                        0.0
                )
        );
    }

    @Test
    void negativeMaximumCurrentShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WireComponent(
                        0.01,
                        -5.0
                )
        );
    }

    @Test
    void nanMaximumCurrentShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WireComponent(
                        0.01,
                        Double.NaN
                )
        );
    }

    @Test
    void shouldStartAtDefaultTemperature() {
        WireComponent wire =
                new WireComponent();

        assertEquals(
                20.0,
                wire.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );
    }

    @Test
    void shouldStoreThermalProperties() {
        ThermalProperties properties =
                new ThermalProperties(
                        50.0,
                        2.0
                );

        WireComponent wire =
                new WireComponent(
                        0.1,
                        5.0,
                        properties,
                        25.0
                );

        assertSame(
                properties,
                wire.getThermalProperties()
        );

        assertEquals(
                25.0,
                wire.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );
    }

    @Test
    void resistiveWireShouldHeatUnderCurrent() {
        ThermalProperties properties =
                new ThermalProperties(
                        10.0,
                        1.0
                );

        WireComponent wire =
                new WireComponent(
                        1.0,
                        50.0,
                        properties,
                        20.0
                );

        wire.updateElectricalState(
                new ElectricalState(
                        10.0,
                        10.0
                )
        );

        wire.updateThermalState(
                20.0,
                1.0
        );

        assertEquals(
                30.0,
                wire.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );
    }

    @Test
    void hotWireShouldCoolWhenCurrentStops() {
        ThermalProperties properties =
                new ThermalProperties(
                        10.0,
                        1.0
                );

        WireComponent wire =
                new WireComponent(
                        1.0,
                        50.0,
                        properties,
                        100.0
                );

        wire.updateElectricalState(
                ElectricalState.ZERO
        );

        wire.updateThermalState(
                20.0,
                1.0
        );

        assertEquals(
                92.0,
                wire.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );
    }

    @Test
    void idealWireShouldNotHeatFromCurrent() {
        ThermalProperties properties =
                new ThermalProperties(
                        10.0,
                        1.0
                );

        WireComponent wire =
                new WireComponent(
                        0.0,
                        200.0,
                        properties,
                        20.0
                );

        wire.updateElectricalState(
                new ElectricalState(
                        0.0,
                        120.0
                )
        );

        wire.updateThermalState(
                20.0,
                1.0
        );

        assertEquals(
                20.0,
                wire.getThermalState()
                        .temperatureCelsius(),
                DELTA
        );
    }

    @Test
    void wireAtRoomTemperatureShouldBeNormal() {
        WireComponent wire =
                new WireComponent();

        assertEquals(
                ThermalStatus.NORMAL,
                wire.getThermalStatus()
        );
    }

    @Test
    void wireShouldBecomeHotAfterHeating() {
        WireComponent wire =
                new WireComponent(
                        1.0,
                        50.0,
                        new ThermalProperties(
                                10.0,
                                0.0
                        ),
                        new ThermalLimits(
                                30.0,
                                100.0,
                                180.0
                        ),
                        20.0
                );

        wire.updateElectricalState(
                new ElectricalState(
                        10.0,
                        10.0
                )
        );

        wire.updateThermalState(
                20.0,
                1.0
        );

        /*
         * I²R = 100 W
         * heat capacity = 10
         *
         * +10 °C
         *
         * 20 → 30 °C
         */

        assertEquals(
                ThermalStatus.HOT,
                wire.getThermalStatus()
        );
    }

    @Test
    void wireShouldBecomeOverheated() {
        WireComponent wire =
                new WireComponent(
                        1.0,
                        50.0,
                        new ThermalProperties(
                                10.0,
                                0.0
                        ),
                        new ThermalLimits(
                                30.0,
                                40.0,
                                100.0
                        ),
                        35.0
                );

        wire.updateElectricalState(
                new ElectricalState(
                        10.0,
                        10.0
                )
        );

        wire.updateThermalState(
                20.0,
                1.0
        );

        // 35 → 45 °C

        assertEquals(
                ThermalStatus.OVERHEATED,
                wire.getThermalStatus()
        );
    }

    @Test
    void wireShouldFailAboveFailureTemperature() {
        WireComponent wire =
                new WireComponent(
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

        wire.updateElectricalState(
                new ElectricalState(
                        10.0,
                        10.0
                )
        );

        wire.updateThermalState(
                20.0,
                1.0
        );

        assertEquals(
                ThermalStatus.FAILED,
                wire.getThermalStatus()
        );

        assertTrue(
                wire.getThermalStatus().isFault()
        );
    }

    @Test
    void wireShouldStartOperational() {
        WireComponent wire =
                new WireComponent();

        assertEquals(
                ComponentOperationalStatus.OPERATIONAL,
                wire.getOperationalStatus()
        );

        assertTrue(
                wire.isOperational()
        );
    }

    @Test
    void wireShouldFailWhenFailureTemperatureIsReached() {
        WireComponent wire =
                new WireComponent(
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

        wire.updateElectricalState(
                new ElectricalState(
                        10.0,
                        10.0
                )
        );

        wire.updateThermalState(
                20.0,
                1.0
        );

        assertEquals(
                ThermalStatus.FAILED,
                wire.getThermalStatus()
        );

        assertEquals(
                ComponentOperationalStatus.FAILED,
                wire.getOperationalStatus()
        );

        assertFalse(
                wire.isOperational()
        );

        assertEquals(
                ElectricalState.ZERO,
                wire.getElectricalState()
        );
    }

    @Test
    void failedWireShouldNotRecoverAfterCooling() {
        WireComponent wire =
                new WireComponent(
                        1.0,
                        50.0,
                        new ThermalProperties(
                                10.0,
                                10.0
                        ),
                        new ThermalLimits(
                                30.0,
                                40.0,
                                50.0
                        ),
                        60.0
                );

        // Força avaliação térmica
        wire.updateThermalState(
                20.0,
                0.0
        );

        assertEquals(
                ComponentOperationalStatus.FAILED,
                wire.getOperationalStatus()
        );

        // Sem corrente, começa a esfriar
        wire.updateElectricalState(
                ElectricalState.ZERO
        );

        for (int i = 0; i < 20; i++) {
            wire.updateThermalState(
                    20.0,
                    0.1
            );
        }

        assertTrue(
                wire.getThermalState()
                        .temperatureCelsius()
                        < 50.0
        );

        // Mesmo frio, continua queimado
        assertEquals(
                ComponentOperationalStatus.FAILED,
                wire.getOperationalStatus()
        );

        assertFalse(
                wire.isOperational()
        );
    }

}
