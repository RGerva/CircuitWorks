/**
 * Generic Class: ElectricalPortTest <T>
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

import static org.junit.jupiter.api.Assertions.*;

class ElectricalPortTest {

    @Test
    void portShouldStartWithZeroState() {
        ElectricalPort port =
                new ElectricalPort("input", ElectricalPortType.INPUT);

        assertEquals(ElectricalState.ZERO, port.getState());
    }

    @Test
    void outputShouldConnectToInput() {
        ElectricalPort output =
                new ElectricalPort("output", ElectricalPortType.OUTPUT);

        ElectricalPort input =
                new ElectricalPort("input", ElectricalPortType.INPUT);

        assertTrue(output.canConnectTo(input));
        assertTrue(input.canConnectTo(output));
    }

    @Test
    void outputShouldNotConnectToOutput() {
        ElectricalPort first =
                new ElectricalPort("output_1", ElectricalPortType.OUTPUT);

        ElectricalPort second =
                new ElectricalPort("output_2", ElectricalPortType.OUTPUT);

        assertFalse(first.canConnectTo(second));
    }

    @Test
    void inputShouldNotConnectToInput() {
        ElectricalPort first =
                new ElectricalPort("input_1", ElectricalPortType.INPUT);

        ElectricalPort second =
                new ElectricalPort("input_2", ElectricalPortType.INPUT);

        assertFalse(first.canConnectTo(second));
    }

    @Test
    void bidirectionalShouldConnectToInputAndOutput() {
        ElectricalPort bidirectional =
                new ElectricalPort(
                        "terminal",
                        ElectricalPortType.BIDIRECTIONAL
                );

        ElectricalPort input =
                new ElectricalPort("input", ElectricalPortType.INPUT);

        ElectricalPort output =
                new ElectricalPort("output", ElectricalPortType.OUTPUT);

        assertTrue(bidirectional.canConnectTo(input));
        assertTrue(bidirectional.canConnectTo(output));

        assertTrue(input.canConnectTo(bidirectional));
        assertTrue(output.canConnectTo(bidirectional));
    }

    @Test
    void portShouldNotConnectToItself() {
        ElectricalPort port =
                new ElectricalPort("port", ElectricalPortType.BIDIRECTIONAL);

        assertFalse(port.canConnectTo(port));
    }

    @Test
    void portStateShouldBeUpdateable() {
        ElectricalPort port =
                new ElectricalPort("output", ElectricalPortType.OUTPUT);

        ElectricalState state =
                new ElectricalState(12.0, 2.0);

        port.setState(state);

        assertEquals(state, port.getState());
    }
}