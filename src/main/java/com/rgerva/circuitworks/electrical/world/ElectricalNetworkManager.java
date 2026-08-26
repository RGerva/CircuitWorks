/**
 * Generic Class: ElectricalNetworkManager <T>
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
import net.minecraft.server.level.ServerLevel;

import java.util.*;

public final class ElectricalNetworkManager {

    private static final Map<ServerLevel, ElectricalNetworkManager>
            INSTANCES =
            new WeakHashMap<>();

    public static ElectricalNetworkManager get(
            ServerLevel level
    ) {
        Objects.requireNonNull(level);

        return INSTANCES.computeIfAbsent(
                level,
                ignored ->
                        new ElectricalNetworkManager()
        );
    }

    private final WireTopology wireTopology =
            new WireTopology();

    private List<WireNetwork> networks =
            List.of();

    private ElectricalNetworkManager() {
    }

    public boolean registerWire(
            BlockPos pos
    ) {
        boolean added =
                wireTopology.registerWire(pos);

        if (added) {
            rebuildNetworks();
        }

        return added;
    }

    public boolean unregisterWire(
            BlockPos pos
    ) {
        boolean removed =
                wireTopology.unregisterWire(pos);

        if (removed) {
            rebuildNetworks();
        }

        return removed;
    }

    public boolean containsWire(
            BlockPos pos
    ) {
        return wireTopology.containsWire(pos);
    }

    public int getWireCount() {
        return wireTopology.getWireCount();
    }

    public Set<BlockPos> getConnectedWires(
            BlockPos pos
    ) {
        return wireTopology.getConnectedWires(pos);
    }

    public static void unload(
            ServerLevel level
    ) {
        ElectricalNetworkManager manager =
                INSTANCES.remove(level);

        if (manager != null) {
            manager.clear();
        }
    }

    public void clear() {
        wireTopology.clear();
    }

    private void rebuildNetworks() {
        List<Set<BlockPos>> groups =
                new ArrayList<>(
                        wireTopology.getNetworks()
                );

        /*
         * Só precisamos de uma ordem determinística
         * para gerar IDs previsíveis durante debug.
         */
        Comparator<BlockPos> positionComparator =
                Comparator.naturalOrder();

        groups.sort(
                Comparator.comparing(
                        group ->
                                group.stream()
                                        .min(positionComparator)
                                        .orElseThrow(),
                        positionComparator
                )
        );

        List<WireNetwork> rebuilt =
                new ArrayList<>();

        int id = 1;

        for (Set<BlockPos> group : groups) {
            rebuilt.add(
                    new WireNetwork(
                            id++,
                            group
                    )
            );
        }

        networks =
                List.copyOf(rebuilt);
    }

    public List<WireNetwork> getNetworks() {
        return networks;
    }

    public int getNetworkCount() {
        return networks.size();
    }

    public Optional<WireNetwork> getNetworkAt(
            BlockPos pos
    ) {
        return networks.stream()
                .filter(network ->
                        network.contains(pos)
                )
                .findFirst();
    }

    public int registerWires(
            Collection<BlockPos> positions
    ) {
        int added = 0;

        for (BlockPos pos : positions) {
            if (wireTopology.registerWire(pos)) {
                added++;
            }
        }

        if (added > 0) {
            rebuildNetworks();
        }

        return added;
    }

    public int unregisterWires(
            Collection<BlockPos> positions
    ) {
        int removed = 0;

        for (BlockPos pos : positions) {
            if (wireTopology.unregisterWire(pos)) {
                removed++;
            }
        }

        if (removed > 0) {
            rebuildNetworks();
        }

        return removed;
    }
}