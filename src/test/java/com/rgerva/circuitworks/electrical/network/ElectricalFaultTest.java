/**
 * Generic Class: ElectricalFaultTest <T>
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

package com.rgerva.circuitworks.electrical.network;

import com.rgerva.circuitworks.electrical.component.WireComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElectricalFaultTest {

    private static final double DELTA = 1.0E-9;

    @Test
    void shouldCalculateFaultRatio() {
        WireComponent wire =
                new WireComponent(0.0, 5.0);

        ElectricalFault fault =
                new ElectricalFault(
                        ElectricalFaultType.OVERCURRENT,
                        wire,
                        10.0,
                        5.0
                );

        assertEquals(
                2.0,
                fault.ratio(),
                DELTA
        );
    }

    @Test
    void slightOvercurrentShouldBeLowSeverity() {
        WireComponent wire =
                new WireComponent(0.0, 10.0);

        ElectricalFault fault =
                new ElectricalFault(
                        ElectricalFaultType.OVERCURRENT,
                        wire,
                        11.0,
                        10.0
                );

        assertEquals(
                ElectricalFaultSeverity.LOW,
                fault.severity()
        );
    }

    @Test
    void moderateOvercurrentShouldBeModerateSeverity() {
        WireComponent wire =
                new WireComponent(0.0, 10.0);

        ElectricalFault fault =
                new ElectricalFault(
                        ElectricalFaultType.OVERCURRENT,
                        wire,
                        15.0,
                        10.0
                );

        assertEquals(
                ElectricalFaultSeverity.MODERATE,
                fault.severity()
        );
    }

    @Test
    void strongOvercurrentShouldBeHighSeverity() {
        WireComponent wire =
                new WireComponent(0.0, 5.0);

        ElectricalFault fault =
                new ElectricalFault(
                        ElectricalFaultType.OVERCURRENT,
                        wire,
                        12.0,
                        5.0
                );

        assertEquals(
                ElectricalFaultSeverity.HIGH,
                fault.severity()
        );
    }

    @Test
    void extremeOvercurrentShouldBeCritical() {
        WireComponent wire =
                new WireComponent(0.0, 5.0);

        ElectricalFault fault =
                new ElectricalFault(
                        ElectricalFaultType.OVERCURRENT,
                        wire,
                        120.0,
                        5.0
                );

        assertEquals(
                24.0,
                fault.ratio(),
                DELTA
        );

        assertEquals(
                ElectricalFaultSeverity.CRITICAL,
                fault.severity()
        );
    }
}