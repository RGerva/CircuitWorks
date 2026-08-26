/**
 * Generic Class: WireTopologyTest <T>
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

package com.rgerva.circuitworks.electrical.world;

import net.minecraft.core.BlockPos;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WireTopologyTest {

    @Test
    void shouldRegisterWire() {
        WireTopology topology =
                new WireTopology();

        BlockPos pos =
                new BlockPos(
                        0,
                        64,
                        0
                );

        assertTrue(
                topology.registerWire(pos)
        );

        assertTrue(
                topology.containsWire(pos)
        );

        assertEquals(
                1,
                topology.getWireCount()
        );
    }

    @Test
    void adjacentWiresShouldBeConnected() {
        WireTopology topology =
                new WireTopology();

        BlockPos first =
                new BlockPos(
                        0,
                        64,
                        0
                );

        BlockPos second =
                new BlockPos(
                        1,
                        64,
                        0
                );

        topology.registerWire(first);
        topology.registerWire(second);

        Set<BlockPos> connected =
                topology.getConnectedWires(first);

        assertEquals(
                2,
                connected.size()
        );

        assertTrue(
                connected.contains(first)
        );

        assertTrue(
                connected.contains(second)
        );
    }

    @Test
    void diagonalWiresShouldNotBeConnected() {
        WireTopology topology =
                new WireTopology();

        BlockPos first =
                new BlockPos(
                        0,
                        64,
                        0
                );

        BlockPos diagonal =
                new BlockPos(
                        1,
                        65,
                        0
                );

        topology.registerWire(first);
        topology.registerWire(diagonal);

        Set<BlockPos> connected =
                topology.getConnectedWires(first);

        assertEquals(
                1,
                connected.size()
        );

        assertTrue(
                connected.contains(first)
        );

        assertFalse(
                connected.contains(diagonal)
        );
    }

    @Test
    void wireChainShouldFormSingleConnectedNetwork() {
        WireTopology topology =
                new WireTopology();

        BlockPos first =
                new BlockPos(0, 64, 0);

        BlockPos second =
                new BlockPos(1, 64, 0);

        BlockPos third =
                new BlockPos(2, 64, 0);

        BlockPos fourth =
                new BlockPos(3, 64, 0);

        topology.registerWire(first);
        topology.registerWire(second);
        topology.registerWire(third);
        topology.registerWire(fourth);

        Set<BlockPos> connected =
                topology.getConnectedWires(first);

        assertEquals(
                4,
                connected.size()
        );
    }

    @Test
    void removingMiddleWireShouldSplitNetwork() {
        WireTopology topology =
                new WireTopology();

        BlockPos first =
                new BlockPos(0, 64, 0);

        BlockPos middle =
                new BlockPos(1, 64, 0);

        BlockPos last =
                new BlockPos(2, 64, 0);

        topology.registerWire(first);
        topology.registerWire(middle);
        topology.registerWire(last);

        assertEquals(
                3,
                topology
                        .getConnectedWires(first)
                        .size()
        );

        topology.unregisterWire(middle);

        Set<BlockPos> firstNetwork =
                topology.getConnectedWires(first);

        Set<BlockPos> secondNetwork =
                topology.getConnectedWires(last);

        assertEquals(
                1,
                firstNetwork.size()
        );

        assertEquals(
                1,
                secondNetwork.size()
        );

        assertFalse(
                firstNetwork.contains(last)
        );
    }

    @Test
    void separatedGroupsShouldCreateTwoNetworks() {
        WireTopology topology =
                new WireTopology();

        topology.registerWire(
                new BlockPos(0, 64, 0)
        );

        topology.registerWire(
                new BlockPos(1, 64, 0)
        );

        topology.registerWire(
                new BlockPos(10, 64, 0)
        );

        assertEquals(
                2,
                topology.getNetworks().size()
        );
    }

    @Test
    void removingMiddleWireShouldCreateTwoNetworks() {
        WireTopology topology =
                new WireTopology();

        BlockPos first =
                new BlockPos(0, 64, 0);

        BlockPos middle =
                new BlockPos(1, 64, 0);

        BlockPos last =
                new BlockPos(2, 64, 0);

        topology.registerWire(first);
        topology.registerWire(middle);
        topology.registerWire(last);

        assertEquals(
                1,
                topology.getNetworks().size()
        );

        topology.unregisterWire(middle);

        assertEquals(
                2,
                topology.getNetworks().size()
        );
    }
}