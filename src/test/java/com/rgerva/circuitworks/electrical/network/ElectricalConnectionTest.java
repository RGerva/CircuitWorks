/**
 * Generic Class: ElectricalConnectionTest <T>
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

import com.rgerva.circuitworks.electrical.api.ElectricalPort;
import com.rgerva.circuitworks.electrical.api.ElectricalPortType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElectricalConnectionTest {

    @Test
    void shouldConnectCompatiblePorts() {
        ElectricalPort output =
                new ElectricalPort(
                        "output",
                        ElectricalPortType.OUTPUT
                );

        ElectricalPort input =
                new ElectricalPort(
                        "input",
                        ElectricalPortType.INPUT
                );

        ElectricalConnection connection =
                new ElectricalConnection(output, input);

        assertSame(output, connection.getFirst());
        assertSame(input, connection.getSecond());
    }

    @Test
    void shouldConnectBidirectionalPorts() {
        ElectricalPort first =
                new ElectricalPort(
                        "terminal_a",
                        ElectricalPortType.BIDIRECTIONAL
                );

        ElectricalPort second =
                new ElectricalPort(
                        "terminal_b",
                        ElectricalPortType.BIDIRECTIONAL
                );

        assertDoesNotThrow(
                () -> new ElectricalConnection(
                        first,
                        second
                )
        );
    }

    @Test
    void shouldRejectTwoOutputs() {
        ElectricalPort first =
                new ElectricalPort(
                        "output_1",
                        ElectricalPortType.OUTPUT
                );

        ElectricalPort second =
                new ElectricalPort(
                        "output_2",
                        ElectricalPortType.OUTPUT
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ElectricalConnection(
                        first,
                        second
                )
        );
    }

    @Test
    void shouldRejectTwoInputs() {
        ElectricalPort first =
                new ElectricalPort(
                        "input_1",
                        ElectricalPortType.INPUT
                );

        ElectricalPort second =
                new ElectricalPort(
                        "input_2",
                        ElectricalPortType.INPUT
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ElectricalConnection(
                        first,
                        second
                )
        );
    }

    @Test
    void shouldRejectSelfConnection() {
        ElectricalPort port =
                new ElectricalPort(
                        "terminal",
                        ElectricalPortType.BIDIRECTIONAL
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ElectricalConnection(
                        port,
                        port
                )
        );
    }

    @Test
    void containsShouldIdentifyConnectedPorts() {
        ElectricalPort first =
                new ElectricalPort(
                        "first",
                        ElectricalPortType.OUTPUT
                );

        ElectricalPort second =
                new ElectricalPort(
                        "second",
                        ElectricalPortType.INPUT
                );

        ElectricalPort unrelated =
                new ElectricalPort(
                        "other",
                        ElectricalPortType.INPUT
                );

        ElectricalConnection connection =
                new ElectricalConnection(
                        first,
                        second
                );

        assertTrue(connection.contains(first));
        assertTrue(connection.contains(second));

        assertFalse(
                connection.contains(unrelated)
        );
    }

    @Test
    void getOtherShouldReturnOppositePort() {
        ElectricalPort first =
                new ElectricalPort(
                        "first",
                        ElectricalPortType.OUTPUT
                );

        ElectricalPort second =
                new ElectricalPort(
                        "second",
                        ElectricalPortType.INPUT
                );

        ElectricalConnection connection =
                new ElectricalConnection(
                        first,
                        second
                );

        assertSame(
                second,
                connection.getOther(first)
        );

        assertSame(
                first,
                connection.getOther(second)
        );
    }

    @Test
    void getOtherShouldRejectUnrelatedPort() {
        ElectricalPort first =
                new ElectricalPort(
                        "first",
                        ElectricalPortType.OUTPUT
                );

        ElectricalPort second =
                new ElectricalPort(
                        "second",
                        ElectricalPortType.INPUT
                );

        ElectricalPort unrelated =
                new ElectricalPort(
                        "other",
                        ElectricalPortType.INPUT
                );

        ElectricalConnection connection =
                new ElectricalConnection(
                        first,
                        second
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> connection.getOther(unrelated)
        );
    }
}