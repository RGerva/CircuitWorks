/**
 * Generic Class: WorldCircuitResolver <T>
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

import com.rgerva.circuitworks.electrical.api.ElectricalPort;
import com.rgerva.circuitworks.electrical.component.IResistiveComponent;
import com.rgerva.circuitworks.electrical.component.ResistiveLoadComponent;
import com.rgerva.circuitworks.electrical.component.WireComponent;
import com.rgerva.circuitworks.electrical.network.ElectricalConnection;
import com.rgerva.circuitworks.electrical.network.ElectricalNetwork;
import com.rgerva.circuitworks.electrical.network.ElectricalNetworkResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.*;

public final class WorldCircuitResolver {

    private WorldCircuitResolver() {
    }

    public static WorldCircuitResult resolve(
            ElectricalNetworkManager manager,
            ElectricalWorldNetwork worldNetwork
    ) {
        if (worldNetwork.getSourceCount() == 0) {
            return WorldCircuitResult.withoutElectricalResult(
                    WorldCircuitStatus.NO_SOURCE
            );
        }

        if (worldNetwork.getSourceCount() > 1) {
            return WorldCircuitResult.withoutElectricalResult(
                    WorldCircuitStatus.MULTIPLE_SOURCES
            );
        }

        BlockPos sourcePos = worldNetwork.sources()
                .iterator()
                .next();

        WorldSourceNode source = manager.getSourceNode(sourcePos)
                .orElseThrow();

        BlockPos positiveNeighbor = sourcePos.relative(
                source.positiveDirection()
        );

        BlockPos negativeNeighbor = sourcePos.relative(
                source.negativeDirection()
        );

        Set<BlockPos> circuitPositions = new HashSet<>();
        circuitPositions.addAll(worldNetwork.wires());
        circuitPositions.addAll(worldNetwork.loads());

        if (!circuitPositions.contains(positiveNeighbor)
                || !circuitPositions.contains(negativeNeighbor)) {
            return createOpenCircuit(source);
        }

        Set<BlockPos> connected = collectConnectedComponents(
                manager,
                positiveNeighbor,
                circuitPositions
        );

        if (!connected.contains(negativeNeighbor)) {
            return createOpenCircuit(source);
        }

        if (!isSimpleSeriesPath(
                manager,
                connected,
                positiveNeighbor,
                negativeNeighbor
        )) {
            return WorldCircuitResult.withoutElectricalResult(
                    WorldCircuitStatus.UNSUPPORTED_TOPOLOGY
            );
        }

        List<BlockPos> orderedPath = buildOrderedPath(
                manager,
                connected,
                positiveNeighbor,
                negativeNeighbor
        );

        ElectricalNetwork network = buildElectricalNetwork(
                manager,
                sourcePos,
                source,
                orderedPath
        );

        ElectricalNetworkResult electricalResult = network.solve();

        return WorldCircuitResult.withElectricalResult(
                WorldCircuitStatus.SOLVED,
                orderedPath,
                electricalResult
        );
    }

    private static WorldCircuitResult createOpenCircuit(
            WorldSourceNode source
    ) {
        ElectricalNetwork network =
                new ElectricalNetwork(source.component());

        ElectricalNetworkResult result =
                network.solve();

        return WorldCircuitResult.withElectricalResult(
                WorldCircuitStatus.OPEN_CIRCUIT,
                List.of(),
                result
        );
    }

    private static Set<BlockPos> collectConnectedComponents(
            ElectricalNetworkManager manager,
            BlockPos start,
            Set<BlockPos> availablePositions
    ) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();

        visited.add(start);
        pending.add(start);

        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);

                if (!availablePositions.contains(neighbor)) {
                    continue;
                }

                if (!manager.canPhysicallyConnect(
                        current,
                        neighbor,
                        direction
                )) {
                    continue;
                }

                if (visited.add(neighbor)) {
                    pending.addLast(neighbor);
                }
            }
        }

        return Set.copyOf(visited);
    }

    private static boolean isSimpleSeriesPath(
            ElectricalNetworkManager manager,
            Set<BlockPos> positions,
            BlockPos positiveNeighbor,
            BlockPos negativeNeighbor
    ) {
        for (BlockPos pos : positions) {
            int degree = countConnectedNeighbors(
                    manager,
                    pos,
                    positions
            );

            if (pos.equals(positiveNeighbor)
                    || pos.equals(negativeNeighbor)) {
                if (degree != 1) {
                    return false;
                }

                continue;
            }

            if (degree != 2) {
                return false;
            }
        }

        return true;
    }

    private static int countConnectedNeighbors(
            ElectricalNetworkManager manager,
            BlockPos pos,
            Set<BlockPos> positions
    ) {
        int count = 0;

        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);

            if (!positions.contains(neighbor)) {
                continue;
            }

            if (manager.canPhysicallyConnect(
                    pos,
                    neighbor,
                    direction
            )) {
                count++;
            }
        }

        return count;
    }

    private static List<BlockPos> buildOrderedPath(
            ElectricalNetworkManager manager,
            Set<BlockPos> positions,
            BlockPos positiveNeighbor,
            BlockPos negativeNeighbor
    ) {
        List<BlockPos> path = new ArrayList<>();

        BlockPos previous = null;
        BlockPos current = positiveNeighbor;

        while (true) {
            path.add(current);

            if (current.equals(negativeNeighbor)) {
                break;
            }

            BlockPos next = null;

            for (Direction direction : Direction.values()) {
                BlockPos candidate = current.relative(direction);

                if (!positions.contains(candidate)) {
                    continue;
                }

                if (candidate.equals(previous)) {
                    continue;
                }

                if (!manager.canPhysicallyConnect(
                        current,
                        candidate,
                        direction
                )) {
                    continue;
                }

                next = candidate;
                break;
            }

            if (next == null) {
                throw new IllegalStateException(
                        "Series path unexpectedly terminated at " + current
                );
            }

            previous = current;
            current = next;
        }

        return List.copyOf(path);
    }

    static ElectricalNetwork buildElectricalNetwork(
            ElectricalNetworkManager manager,
            ElectricalWorldNetwork worldNetwork,
            List<BlockPos> path
    ) {
        if (worldNetwork.getSourceCount() != 1) {
            throw new IllegalArgumentException(
                    "A solved circuit must contain exactly one source."
            );
        }

        BlockPos sourcePos = worldNetwork.sources()
                .iterator()
                .next();

        WorldSourceNode source = manager.getSourceNode(sourcePos)
                .orElseThrow();

        return buildElectricalNetwork(
                manager,
                sourcePos,
                source,
                path
        );
    }

    private static ElectricalNetwork buildElectricalNetwork(
            ElectricalNetworkManager manager,
            BlockPos sourcePos,
            WorldSourceNode source,
            List<BlockPos> path
    ) {
        ElectricalNetwork network =
                new ElectricalNetwork(source.component());

        List<PathComponent> components = new ArrayList<>();

        for (int i = 0; i < path.size(); i++) {
            BlockPos pos = path.get(i);

            BlockPos previous =
                    i == 0
                            ? sourcePos
                            : path.get(i - 1);

            BlockPos next =
                    i == path.size() - 1
                            ? sourcePos
                            : path.get(i + 1);

            PathComponent pathComponent = resolvePathComponent(
                    manager,
                    pos,
                    previous,
                    next
            );

            components.add(pathComponent);
            network.addComponent(pathComponent.component());
        }

        PathComponent first = components.getFirst();

        network.addConnection(
                new ElectricalConnection(
                        source.component().getPositiveTerminal(),
                        first.entryPort()
                )
        );

        for (int i = 0; i < components.size() - 1; i++) {
            PathComponent current = components.get(i);
            PathComponent next = components.get(i + 1);

            network.addConnection(
                    new ElectricalConnection(
                            current.exitPort(),
                            next.entryPort()
                    )
            );
        }

        PathComponent last = components.getLast();

        network.addConnection(
                new ElectricalConnection(
                        last.exitPort(),
                        source.component().getNegativeTerminal()
                )
        );

        return network;
    }

    private static PathComponent resolvePathComponent(
            ElectricalNetworkManager manager,
            BlockPos pos,
            BlockPos previous,
            BlockPos next
    ) {
        WireComponent wire = manager.getWireComponent(pos)
                .orElse(null);

        if (wire != null) {
            return new PathComponent(
                    wire,
                    wire.getTerminalA(),
                    wire.getTerminalB()
            );
        }

        WorldLoadNode loadNode = manager.getLoadNode(pos)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Missing circuit component at " + pos
                        )
                );

        ResistiveLoadComponent load = loadNode.component();

        Direction entryDirection = directionFromTo(
                pos,
                previous
        );

        Direction exitDirection = directionFromTo(
                pos,
                next
        );

        if (!loadNode.hasTerminal(entryDirection)
                || !loadNode.hasTerminal(exitDirection)) {
            throw new IllegalStateException(
                    "Circuit entered load through a non-terminal face at " + pos
            );
        }

        ElectricalPort entryPort =
                loadNode.isTerminalA(entryDirection)
                        ? load.getTerminalA()
                        : load.getTerminalB();

        ElectricalPort exitPort =
                loadNode.isTerminalA(exitDirection)
                        ? load.getTerminalA()
                        : load.getTerminalB();

        if (entryPort == exitPort) {
            throw new IllegalStateException(
                    "Circuit entered and exited the same load terminal at " + pos
            );
        }

        return new PathComponent(
                load,
                entryPort,
                exitPort
        );
    }

    private static Direction directionFromTo(
            BlockPos from,
            BlockPos to
    ) {
        for (Direction direction : Direction.values()) {
            if (from.relative(direction).equals(to)) {
                return direction;
            }
        }

        throw new IllegalArgumentException(
                "Positions are not adjacent: " + from + " -> " + to
        );
    }

    private record PathComponent(
            IResistiveComponent component,
            ElectricalPort entryPort,
            ElectricalPort exitPort
    ) {
    }
}