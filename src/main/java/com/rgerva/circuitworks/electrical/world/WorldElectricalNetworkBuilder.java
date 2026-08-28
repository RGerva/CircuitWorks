/**
 * Generic Class: WorldElectricalNetworkBuilder <T>
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
import com.rgerva.circuitworks.electrical.component.DCVoltageSourceComponent;
import com.rgerva.circuitworks.electrical.component.IResistiveComponent;
import com.rgerva.circuitworks.electrical.component.ResistiveLoadComponent;
import com.rgerva.circuitworks.electrical.component.WireComponent;
import com.rgerva.circuitworks.electrical.network.ElectricalConnection;
import com.rgerva.circuitworks.electrical.network.ElectricalNetwork;
import net.minecraft.core.BlockPos;

import java.util.*;

public final class WorldElectricalNetworkBuilder {

    private WorldElectricalNetworkBuilder() {
    }

    static ElectricalNetwork build(
            ElectricalNetworkManager manager,
            ElectricalWorldNetwork worldNetwork,
            BlockPos sourcePos,
            BlockPos positiveAnchor,
            BlockPos negativeAnchor
    ) {
        Objects.requireNonNull(manager);
        Objects.requireNonNull(worldNetwork);
        Objects.requireNonNull(sourcePos);
        Objects.requireNonNull(positiveAnchor);
        Objects.requireNonNull(negativeAnchor);

        if (worldNetwork.getSourceCount() != 1) {
            throw new IllegalArgumentException(
                    "Graph builder requires exactly one source."
            );
        }

        DCVoltageSourceComponent source =
                manager.getSource(sourcePos)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "World source component was not found."
                                )
                        );

        WorldCircuitGraph fullGraph =
                WorldCircuitGraph.build(
                        manager,
                        worldNetwork
                );

        Set<BlockPos> activePositions =
                removeDanglingPositions(
                        fullGraph,
                        sourcePos,
                        positiveAnchor,
                        negativeAnchor
                );

        Set<BlockPos> activeWires =
                new HashSet<>();

        for (BlockPos wirePos :
                worldNetwork.wires()) {

            if (activePositions.contains(wirePos)) {
                activeWires.add(wirePos);
            }
        }

        Set<BlockPos> activeLoads =
                new HashSet<>();

        for (BlockPos loadPos :
                worldNetwork.loads()) {

            if (activePositions.contains(loadPos)) {
                activeLoads.add(loadPos);
            }
        }

        ElectricalWorldNetwork activeWorldNetwork =
                new ElectricalWorldNetwork(
                        worldNetwork.id(),
                        activeWires,
                        Set.of(sourcePos),
                        activeLoads
                );

        WorldCircuitGraph graph =
                WorldCircuitGraph.build(
                        manager,
                        activeWorldNetwork
                );

        WorldCircuitOrientation orientation =
                WorldCircuitOrientation.build(
                        graph,
                        sourcePos,
                        positiveAnchor,
                        negativeAnchor
                );

        ElectricalNetwork electricalNetwork =
                new ElectricalNetwork(source);

        Map<BlockPos, IResistiveComponent> components =
                new HashMap<>();

        /*
         * Wires.
         */
        for (BlockPos wirePos :
                activeWorldNetwork.wires()) {

            WireComponent wire =
                    manager.getWireComponent(wirePos)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Missing WireComponent at "
                                                    + wirePos
                                    )
                            );

            components.put(
                    wirePos,
                    wire
            );

            electricalNetwork.addComponent(
                    wire
            );
        }

        /*
         * Loads.
         */
        for (BlockPos loadPos :
                activeWorldNetwork.loads()) {

            ResistiveLoadComponent load =
                    manager.getLoad(loadPos)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Missing ResistiveLoadComponent at "
                                                    + loadPos
                                    )
                            );

            components.put(
                    loadPos,
                    load
            );

            electricalNetwork.addComponent(
                    load
            );
        }

        /*
         * Conexões físicas.
         */
        for (WorldCircuitEdge edge :
                graph.edges()) {

            /*
             * Source <-> component.
             */
            if (edge.contains(sourcePos)) {
                BlockPos componentPos =
                        edge.other(sourcePos);

                IResistiveComponent component =
                        components.get(componentPos);

                if (component == null) {
                    throw new IllegalStateException(
                            "Source is connected to an unknown resistive component."
                    );
                }

                ElectricalPort sourcePort;

                if (componentPos.equals(
                        positiveAnchor
                )) {
                    sourcePort =
                            source.getPositiveTerminal();
                } else if (componentPos.equals(
                        negativeAnchor
                )) {
                    sourcePort =
                            source.getNegativeTerminal();
                } else {
                    throw new IllegalStateException(
                            "Source is connected to an unknown terminal anchor."
                    );
                }

                ElectricalPort componentPort =
                        portForNeighbor(
                                manager,
                                orientation,
                                componentPos,
                                sourcePos,
                                component
                        );

                electricalNetwork.addConnection(
                        new ElectricalConnection(
                                sourcePort,
                                componentPort
                        )
                );

                continue;
            }

            /*
             * Component <-> component.
             */
            IResistiveComponent firstComponent =
                    components.get(
                            edge.first()
                    );

            IResistiveComponent secondComponent =
                    components.get(
                            edge.second()
                    );

            if (firstComponent == null
                    || secondComponent == null) {
                throw new IllegalStateException(
                        "Graph contains an unknown resistive component."
                );
            }

            ElectricalPort firstPort =
                    portForNeighbor(
                            manager,
                            orientation,
                            edge.first(),
                            edge.second(),
                            firstComponent
                    );

            ElectricalPort secondPort =
                    portForNeighbor(
                            manager,
                            orientation,
                            edge.second(),
                            edge.first(),
                            secondComponent
                    );

            electricalNetwork.addConnection(
                    new ElectricalConnection(
                            firstPort,
                            secondPort
                    )
            );
        }

        return electricalNetwork;
    }

    private static ElectricalPort portForNeighbor(
            ElectricalNetworkManager manager,
            WorldCircuitOrientation orientation,
            BlockPos componentPos,
            BlockPos neighbor,
            IResistiveComponent component
    ) {
        if (component instanceof WireComponent wire) {
            return wirePortForNeighbor(
                    orientation,
                    componentPos,
                    neighbor,
                    wire
            );
        }

        if (component
                instanceof ResistiveLoadComponent load) {

            return loadPortForNeighbor(
                    manager,
                    componentPos,
                    neighbor,
                    load
            );
        }

        throw new IllegalStateException(
                "Unsupported world resistive component: "
                        + component.getClass()
                        .getSimpleName()
        );
    }

    private static ElectricalPort wirePortForNeighbor(
            WorldCircuitOrientation orientation,
            BlockPos wirePos,
            BlockPos neighbor,
            WireComponent wire
    ) {
        if (wire.getPorts().size() != 2) {
            throw new IllegalStateException(
                    "World wire must have exactly two electrical ports."
            );
        }

        WorldCircuitOrientation.Side side =
                orientation.sideOfNeighbor(
                        wirePos,
                        neighbor
                );

        return switch (side) {
            case POSITIVE -> wire.getPorts().get(0);

            case NEGATIVE -> wire.getPorts().get(1);
        };
    }

    private static ElectricalPort loadPortForNeighbor(
            ElectricalNetworkManager manager,
            BlockPos loadPos,
            BlockPos neighbor,
            ResistiveLoadComponent load
    ) {
        if (load.getPorts().size() != 2) {
            throw new IllegalStateException(
                    "World resistive load must have exactly two electrical ports."
            );
        }

        WorldLoadNode node =
                manager.getLoadNode(loadPos)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "World load node was not found."
                                )
                        );

        BlockPos terminalANeighbor =
                loadPos.relative(
                        node.terminalADirection()
                );

        if (neighbor.equals(
                terminalANeighbor
        )) {
            return load.getPorts().get(0);
        }

        BlockPos terminalBNeighbor =
                loadPos.relative(
                        node.terminalBDirection()
                );

        if (neighbor.equals(
                terminalBNeighbor
        )) {
            return load.getPorts().get(1);
        }

        throw new IllegalStateException(
                "Neighbor is not connected to a physical load terminal."
        );
    }

    private static Set<BlockPos> removeDanglingPositions(
            WorldCircuitGraph graph,
            BlockPos sourcePos,
            BlockPos positiveAnchor,
            BlockPos negativeAnchor
    ) {
        Set<BlockPos> active =
                new HashSet<>(
                        graph.positions()
                );

        /*
         * A source não participa da poda.
         * Queremos analisar o caminho externo entre + e -.
         */
        active.remove(sourcePos);

        ArrayDeque<BlockPos> pending =
                new ArrayDeque<>();

        for (BlockPos pos : active) {
            if (pos.equals(positiveAnchor)
                    || pos.equals(negativeAnchor)) {
                continue;
            }

            if (activeDegree(
                    graph,
                    pos,
                    active
            ) <= 1) {
                pending.add(pos);
            }
        }

        while (!pending.isEmpty()) {
            BlockPos pos =
                    pending.removeFirst();

            if (!active.remove(pos)) {
                continue;
            }

            for (BlockPos neighbor :
                    graph.neighbors(pos)) {

                if (!active.contains(neighbor)) {
                    continue;
                }

                if (neighbor.equals(positiveAnchor)
                        || neighbor.equals(negativeAnchor)) {
                    continue;
                }

                if (activeDegree(
                        graph,
                        neighbor,
                        active
                ) <= 1) {
                    pending.addLast(neighbor);
                }
            }
        }

        /*
         * Recolocamos a source para o graph builder
         * reconstruir as conexões dos terminais.
         */
        active.add(sourcePos);

        return Set.copyOf(active);
    }

    private static int activeDegree(
            WorldCircuitGraph graph,
            BlockPos pos,
            Set<BlockPos> active
    ) {
        int degree = 0;

        for (BlockPos neighbor :
                graph.neighbors(pos)) {

            if (active.contains(neighbor)) {
                degree++;
            }
        }

        return degree;
    }
}