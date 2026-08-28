/**
 * Generic Class: ElectricalNodeGraph <T>
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
import com.rgerva.circuitworks.electrical.component.IElectricalSource;
import com.rgerva.circuitworks.electrical.component.IResistiveComponent;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ElectricalNodeGraph {

    private final Map<ElectricalPort, ElectricalPort> parent = new IdentityHashMap<>();

    private ElectricalNodeGraph() {
    }

    static ElectricalNodeGraph build(
            IElectricalSource source,
            List<IResistiveComponent> components,
            List<ElectricalConnection> connections
    ) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(components);
        Objects.requireNonNull(connections);

        ElectricalNodeGraph graph =
                new ElectricalNodeGraph();

        for (ElectricalPort port : source.getPorts()) {
            graph.add(port);
        }

        for (IResistiveComponent component : components) {
            for (ElectricalPort port : component.getPorts()) {
                graph.add(port);
            }
        }

        for (ElectricalConnection connection : connections) {
            graph.union(
                    connection.getFirst(),
                    connection.getSecond()
            );
        }

        return graph;
    }

    boolean isSameNode(
            ElectricalPort first,
            ElectricalPort second
    ) {
        return find(first) == find(second);
    }

    ElectricalPort getNode(
            ElectricalPort port
    ) {
        return find(port);
    }

    private void add(
            ElectricalPort port
    ) {
        parent.putIfAbsent(
                Objects.requireNonNull(port),
                port
        );
    }

    private ElectricalPort find(
            ElectricalPort port
    ) {
        ElectricalPort current =
                parent.get(port);

        if (current == null) {
            throw new IllegalArgumentException(
                    "Unknown electrical port."
            );
        }

        if (current != port) {
            current = find(current);

            parent.put(
                    port,
                    current
            );
        }

        return current;
    }

    private void union(
            ElectricalPort first,
            ElectricalPort second
    ) {
        ElectricalPort firstRoot =
                find(first);

        ElectricalPort secondRoot =
                find(second);

        if (firstRoot == secondRoot) {
            return;
        }

        parent.put(
                secondRoot,
                firstRoot
        );
    }
}