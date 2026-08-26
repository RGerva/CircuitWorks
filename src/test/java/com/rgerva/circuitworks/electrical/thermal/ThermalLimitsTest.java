/**
 * Generic Class: ThermalLimitsTest <T>
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ThermalLimitsTest {

    private final ThermalLimits limits =
            new ThermalLimits(
                    60.0,
                    100.0,
                    180.0
            );

    @Test
    void temperatureBelowHotLimitShouldBeNormal() {
        assertEquals(
                ThermalStatus.NORMAL,
                limits.getStatus(59.0)
        );
    }

    @Test
    void hotTemperatureShouldBeHot() {
        assertEquals(
                ThermalStatus.HOT,
                limits.getStatus(60.0)
        );
    }

    @Test
    void maxTemperatureShouldBeOverheated() {
        assertEquals(
                ThermalStatus.OVERHEATED,
                limits.getStatus(100.0)
        );
    }

    @Test
    void failureTemperatureShouldBeFailed() {
        assertEquals(
                ThermalStatus.FAILED,
                limits.getStatus(180.0)
        );
    }

    @Test
    void invalidLimitOrderShouldNotBeAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ThermalLimits(
                        100.0,
                        80.0,
                        180.0
                )
        );
    }
}