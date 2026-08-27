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

import com.rgerva.circuitworks.electrical.api.ElectricalState;
import com.rgerva.circuitworks.electrical.component.DCVoltageSourceComponent;
import com.rgerva.circuitworks.electrical.component.IElectricalComponent;
import com.rgerva.circuitworks.electrical.component.ResistiveLoadComponent;
import com.rgerva.circuitworks.electrical.component.WireComponent;
import com.rgerva.circuitworks.electrical.network.ElectricalNetwork;
import com.rgerva.circuitworks.electrical.simulation.ElectricalSimulation;
import com.rgerva.circuitworks.electrical.simulation.ElectricalSimulationEvent;
import com.rgerva.circuitworks.electrical.simulation.ElectricalSimulationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

public final class ElectricalNetworkManager {

    private static final double DEFAULT_WIRE_RESISTANCE = 0.01;
    private static final double DEFAULT_WIRE_MAX_CURRENT = 10.0;
    private final Map<BlockPos, WireComponent> wireComponents = new HashMap<>();

    private final Map<BlockPos, WorldSourceNode> sources = new HashMap<>();

    private List<ElectricalWorldNetwork> electricalWorldNetworks = List.of();

    private final Map<BlockPos, WorldLoadNode> loads = new HashMap<>();

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

    ElectricalNetworkManager() {
    }

    public boolean registerWire(BlockPos pos) {
        return registerWire(
                pos,
                WirePersistentState.defaultState()
        );
    }

    public boolean registerWire(
            BlockPos pos,
            WirePersistentState persistentState
    ) {
        Objects.requireNonNull(pos);
        Objects.requireNonNull(persistentState);

        BlockPos immutablePos = pos.immutable();

        boolean added =
                wireTopology.registerWire(immutablePos);

        wireComponents.computeIfAbsent(
                immutablePos,
                ignored -> createWireComponent(persistentState)
        );

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

        if (!removed) {
            return false;
        }

        wireComponents.remove(pos);

        rebuildNetworks();

        return true;
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
        wireComponents.clear();
        sources.clear();
        loads.clear();

        networks = List.of();
        electricalWorldNetworks = List.of();
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

        networks = List.copyOf(rebuilt);

        rebuildElectricalWorldNetworks();
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
            Map<BlockPos, WirePersistentState> states
    ) {
        int added = 0;

        for (Map.Entry<BlockPos, WirePersistentState> entry :
                states.entrySet()) {

            BlockPos pos = entry.getKey().immutable();

            if (wireTopology.registerWire(pos)) {
                added++;
            }

            wireComponents.computeIfAbsent(
                    pos,
                    ignored -> createWireComponent(entry.getValue())
            );
        }

        if (added > 0) {
            rebuildNetworks();
        }

        return added;
    }

    public int registerWires(
            Collection<BlockPos> positions
    ) {
        int added = 0;

        for (BlockPos pos : positions) {
            BlockPos immutablePos =
                    pos.immutable();

            if (wireTopology.registerWire(
                    immutablePos
            )) {
                added++;
            }

            wireComponents.computeIfAbsent(
                    immutablePos,
                    ignored -> createWireComponent()
            );
        }

        if (added > 0) {
            rebuildNetworks();
        }

        return added;
    }

    public Optional<WirePersistentState> getWirePersistentState(
            BlockPos pos
    ) {
        return getWireComponent(pos)
                .map(WirePersistentState::from);
    }

    public Map<BlockPos, WirePersistentState> getWirePersistentStates() {
        Map<BlockPos, WirePersistentState> states = new HashMap<>();

        for (Map.Entry<BlockPos, WireComponent> entry :
                wireComponents.entrySet()) {

            states.put(
                    entry.getKey(),
                    WirePersistentState.from(entry.getValue())
            );
        }

        return Map.copyOf(states);
    }

    public int unregisterWires(Collection<BlockPos> positions) {
        int removed = 0;

        for (BlockPos pos : positions) {
            if (wireTopology.unregisterWire(pos)) {
                wireComponents.remove(pos);

                removed++;
            }
        }

        if (removed > 0) {
            rebuildNetworks();
        }

        return removed;
    }

    private WireComponent createWireComponent() {
        return createWireComponent(WirePersistentState.defaultState());
    }

    private WireComponent createWireComponent(WirePersistentState persistentState) {
        return new WireComponent(
                DEFAULT_WIRE_RESISTANCE,
                DEFAULT_WIRE_MAX_CURRENT,
                WireComponent.DEFAULT_THERMAL_PROPERTIES,
                WireComponent.DEFAULT_THERMAL_LIMITS,
                persistentState.temperatureCelsius(),
                persistentState.operationalStatus()
        );
    }

    public Optional<WireComponent> getWireComponent(BlockPos pos) {
        return Optional.ofNullable(wireComponents.get(pos));
    }

    public int getWireComponentCount() {
        return wireComponents.size();
    }

    public Map<BlockPos, WireComponent> getWireComponents(
            WireNetwork network
    ) {
        Map<BlockPos, WireComponent> components =
                new HashMap<>();

        for (BlockPos pos : network.wires()) {
            WireComponent component =
                    wireComponents.get(pos);

            if (component == null) {
                throw new IllegalStateException(
                        "Wire network contains a position without a WireComponent: "
                                + pos
                );
            }

            components.put(
                    pos,
                    component
            );
        }

        return Map.copyOf(components);
    }

    /* DC SOURCE */

    public Set<BlockPos> getSourcePositions() {
        return Set.copyOf(sources.keySet());
    }

    public void registerSource(
            BlockPos pos,
            DCVoltageSourceComponent source,
            Direction positiveDirection,
            Direction negativeDirection
    ) {
        Objects.requireNonNull(pos);
        Objects.requireNonNull(source);
        Objects.requireNonNull(positiveDirection);
        Objects.requireNonNull(negativeDirection);

        sources.put(
                pos.immutable(),
                new WorldSourceNode(
                        source,
                        positiveDirection,
                        negativeDirection
                )
        );

        rebuildElectricalWorldNetworks();
    }

    public boolean unregisterSource(BlockPos pos) {
        boolean removed = sources.remove(pos) != null;

        if (removed) {
            rebuildElectricalWorldNetworks();
        }

        return removed;
    }

    public Optional<DCVoltageSourceComponent> getSource(BlockPos pos) {
        return Optional.ofNullable(sources.get(pos))
                .map(WorldSourceNode::component);
    }

    public Optional<WorldSourceNode> getSourceNode(BlockPos pos) {
        return Optional.ofNullable(sources.get(pos));
    }

    public int getSourceCount() {
        return sources.size();
    }

    public List<ElectricalWorldNetwork> getElectricalWorldNetworks() {
        return electricalWorldNetworks;
    }

    public int getElectricalWorldNetworkCount() {
        return electricalWorldNetworks.size();
    }

    public Optional<ElectricalWorldNetwork> getElectricalWorldNetworkAt(
            BlockPos pos
    ) {
        return electricalWorldNetworks.stream()
                .filter(network -> network.contains(pos))
                .findFirst();
    }

    boolean canPhysicallyConnect(
            BlockPos current,
            BlockPos neighbor,
            Direction direction
    ) {
        boolean currentIsWire =
                wireTopology.containsWire(current);

        boolean neighborIsWire =
                wireTopology.containsWire(neighbor);

        WorldSourceNode currentSource =
                sources.get(current);

        WorldSourceNode neighborSource =
                sources.get(neighbor);

        WorldLoadNode currentLoad =
                loads.get(current);

        WorldLoadNode neighborLoad =
                loads.get(neighbor);

        /*
         * Wire <-> Wire
         */
        if (currentIsWire && neighborIsWire) {
            return true;
        }

        /*
         * Source -> Wire
         */
        if (currentSource != null && neighborIsWire) {
            return currentSource.hasTerminal(direction);
        }

        /*
         * Wire -> Source
         */
        if (currentIsWire && neighborSource != null) {
            return neighborSource.hasTerminal(
                    direction.getOpposite()
            );
        }

        /*
         * Load -> Wire
         */
        if (currentLoad != null && neighborIsWire) {
            return currentLoad.hasTerminal(direction);
        }

        /*
         * Wire -> Load
         */
        if (currentIsWire && neighborLoad != null) {
            return neighborLoad.hasTerminal(
                    direction.getOpposite()
            );
        }

        /*
         * Source <-> Source
         */
        if (currentSource != null && neighborSource != null) {
            return currentSource.hasTerminal(direction)
                    && neighborSource.hasTerminal(
                    direction.getOpposite()
            );
        }

        /*
         * Load <-> Load
         */
        if (currentLoad != null && neighborLoad != null) {
            return currentLoad.hasTerminal(direction)
                    && neighborLoad.hasTerminal(
                    direction.getOpposite()
            );
        }

        /*
         * Source -> Load
         */
        if (currentSource != null && neighborLoad != null) {
            return currentSource.hasTerminal(direction)
                    && neighborLoad.hasTerminal(
                    direction.getOpposite()
            );
        }

        /*
         * Load -> Source
         */
        if (currentLoad != null && neighborSource != null) {
            return currentLoad.hasTerminal(direction)
                    && neighborSource.hasTerminal(
                    direction.getOpposite()
            );
        }

        return false;
    }

    private void rebuildElectricalWorldNetworks() {
        Set<BlockPos> allPositions = new HashSet<>(wireTopology.getWires());

        allPositions.addAll(sources.keySet());
        allPositions.addAll(loads.keySet());

        Set<BlockPos> remaining = new HashSet<>(allPositions);
        List<Set<BlockPos>> groups = new ArrayList<>();

        while (!remaining.isEmpty()) {
            BlockPos start = remaining.iterator().next();

            Set<BlockPos> connected = collectConnectedElectricalPositions(
                    start,
                    allPositions
            );

            groups.add(connected);
            remaining.removeAll(connected);
        }

        Comparator<BlockPos> positionComparator = Comparator.naturalOrder();

        groups.sort(
                Comparator.comparing(
                        group -> group.stream()
                                .min(positionComparator)
                                .orElseThrow(),
                        positionComparator
                )
        );

        List<ElectricalWorldNetwork> rebuilt = new ArrayList<>();

        int id = 1;

        for (Set<BlockPos> group : groups) {
            Set<BlockPos> wires = new HashSet<>();
            Set<BlockPos> networkSources = new HashSet<>();
            Set<BlockPos> networkLoads = new HashSet<>();

            for (BlockPos pos : group) {
                if (wireTopology.containsWire(pos)) {
                    wires.add(pos);
                }

                if (sources.containsKey(pos)) {
                    networkSources.add(pos);
                }

                if (loads.containsKey(pos)) {
                    networkLoads.add(pos);
                }
            }

            rebuilt.add(
                    new ElectricalWorldNetwork(
                            id++,
                            wires,
                            networkSources,
                            networkLoads
                    )
            );
        }

        electricalWorldNetworks = List.copyOf(rebuilt);
    }

    private Set<BlockPos> collectConnectedElectricalPositions(
            BlockPos start,
            Set<BlockPos> allPositions
    ) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();

        visited.add(start);
        pending.add(start);

        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);

                if (!allPositions.contains(neighbor)) {
                    continue;
                }

                if (!canPhysicallyConnect(
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

    /* CIRCUIT */

    public WorldCircuitResult resolveWorldCircuit(ElectricalWorldNetwork worldNetwork) {
        return WorldCircuitResolver.resolve(this, worldNetwork);
    }

    public Optional<WorldCircuitResult> resolveWorldCircuitAt(BlockPos pos) {
        return getElectricalWorldNetworkAt(pos).map(this::resolveWorldCircuit);
    }

    /* LOADS */

    public void registerLoad(
            BlockPos pos,
            ResistiveLoadComponent load,
            Direction terminalADirection,
            Direction terminalBDirection
    ) {
        Objects.requireNonNull(pos);
        Objects.requireNonNull(load);
        Objects.requireNonNull(terminalADirection);
        Objects.requireNonNull(terminalBDirection);

        loads.put(
                pos.immutable(),
                new WorldLoadNode(
                        load,
                        terminalADirection,
                        terminalBDirection
                )
        );

        rebuildElectricalWorldNetworks();
    }

    public boolean unregisterLoad(BlockPos pos) {
        boolean removed = loads.remove(pos) != null;
        if (removed) {
            rebuildElectricalWorldNetworks();
        }
        return removed;
    }

    public Optional<ResistiveLoadComponent> getLoad(BlockPos pos) {
        return Optional.ofNullable(loads.get(pos))
                .map(WorldLoadNode::component);
    }

    public Optional<WorldLoadNode> getLoadNode(BlockPos pos) {
        return Optional.ofNullable(loads.get(pos));
    }

    public int getLoadCount() {
        return loads.size();
    }

    /* TICK */
    public List<ElectricalSimulationEvent> tickSimulation(
            double ambientTemperature,
            double deltaSeconds
    ) {
        List<ElectricalSimulationEvent> events = new ArrayList<>();

        Set<IElectricalComponent> simulatedComponents =
                Collections.newSetFromMap(new IdentityHashMap<>());

        /*
         * Primeiro zeramos tudo.
         *
         * Isso evita um componente manter corrente antiga
         * depois que o jogador abre o circuito.
         */
        for (WireComponent wire : wireComponents.values()) {
            wire.updateElectricalState(ElectricalState.ZERO);
        }

        for (WorldSourceNode source : sources.values()) {
            source.component().updateElectricalState(ElectricalState.ZERO);
        }

        for (WorldLoadNode load : loads.values()) {
            load.component().updateElectricalState(ElectricalState.ZERO);
        }

        for (ElectricalWorldNetwork worldNetwork : electricalWorldNetworks) {
            if (worldNetwork.getSourceCount() != 1) {
                continue;
            }

            WorldCircuitResult circuit = resolveWorldCircuit(worldNetwork);

            if (circuit.status() != WorldCircuitStatus.SOLVED) {
                continue;
            }

            ElectricalNetwork electricalNetwork =
                    WorldCircuitResolver.buildElectricalNetwork(
                            this,
                            worldNetwork,
                            circuit.componentPath()
                    );

            ElectricalSimulation simulation =
                    new ElectricalSimulation(electricalNetwork);

            ElectricalSimulationResult simulationResult =
                    simulation.step(
                            ambientTemperature,
                            deltaSeconds
                    );

            events.addAll(simulationResult.events());

            markSimulatedComponents(
                    worldNetwork,
                    circuit.componentPath(),
                    simulatedComponents
            );
        }

        /*
         * Componentes que não pertencem a um circuito ativo
         * continuam esfriando naturalmente.
         */
        for (WireComponent wire : wireComponents.values()) {
            if (!simulatedComponents.contains(wire)) {
                wire.updateThermalState(
                        ambientTemperature,
                        deltaSeconds
                );
            }
        }

        for (WorldSourceNode source : sources.values()) {
            if (!simulatedComponents.contains(source.component())) {
                source.component().updateThermalState(
                        ambientTemperature,
                        deltaSeconds
                );
            }
        }

        return List.copyOf(events);
    }


    private void markSimulatedComponents(
            ElectricalWorldNetwork worldNetwork,
            List<BlockPos> path,
            Set<IElectricalComponent> simulatedComponents
    ) {
        BlockPos sourcePos = worldNetwork.sources()
                .iterator()
                .next();

        WorldSourceNode source = sources.get(sourcePos);

        if (source != null) {
            simulatedComponents.add(source.component());
        }

        for (BlockPos pos : path) {
            WireComponent wire = wireComponents.get(pos);

            if (wire != null) {
                simulatedComponents.add(wire);
                continue;
            }

            WorldLoadNode load = loads.get(pos);

            if (load != null) {
                simulatedComponents.add(load.component());
            }
        }
    }

    public Optional<BlockPos> findComponentPosition(
            IElectricalComponent component
    ) {
        for (Map.Entry<BlockPos, WireComponent> entry :
                wireComponents.entrySet()) {

            if (entry.getValue() == component) {
                return Optional.of(entry.getKey());
            }
        }

        for (Map.Entry<BlockPos, WorldSourceNode> entry :
                sources.entrySet()) {

            if (entry.getValue().component() == component) {
                return Optional.of(entry.getKey());
            }
        }

        for (Map.Entry<BlockPos, WorldLoadNode> entry :
                loads.entrySet()) {

            if (entry.getValue().component() == component) {
                return Optional.of(entry.getKey());
            }
        }

        return Optional.empty();
    }
}