/**
 * Generic Class: ElectricalNetwork <T>
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

import com.rgerva.circuitworks.electrical.api.ElectricalConstants;
import com.rgerva.circuitworks.electrical.api.ElectricalPort;
import com.rgerva.circuitworks.electrical.api.ElectricalState;
import com.rgerva.circuitworks.electrical.component.ICurrentLimitedComponent;
import com.rgerva.circuitworks.electrical.component.IElectricalSource;
import com.rgerva.circuitworks.electrical.component.IOperationalComponent;
import com.rgerva.circuitworks.electrical.component.IResistiveComponent;

import java.util.*;

public class ElectricalNetwork {

    private IElectricalSource source;

    private final List<IResistiveComponent> components =
            new ArrayList<>();

    private final List<ElectricalConnection> connections =
            new ArrayList<>();

    private ElectricalNetworkResult result =
            new ElectricalNetworkResult(
                    ElectricalNetworkStatus.UNRESOLVED, ElectricalState.ZERO, Double.POSITIVE_INFINITY);

    public ElectricalNetwork() {
    }

    public ElectricalNetwork(IElectricalSource source) {
        this.source = Objects.requireNonNull(source);
    }

    public ElectricalNetworkResult solve() {
        resetComponentStates();

        if (source == null) {
            result = new ElectricalNetworkResult(
                    ElectricalNetworkStatus.NO_SOURCE,
                    ElectricalState.ZERO,
                    Double.POSITIVE_INFINITY
            );

            return result;
        }

        if (source instanceof IOperationalComponent operational
                && !operational.isOperational()) {

            source.updateElectricalState(
                    ElectricalState.ZERO
            );

            result = new ElectricalNetworkResult(
                    ElectricalNetworkStatus.SOURCE_FAILED,
                    ElectricalState.ZERO,
                    Double.POSITIVE_INFINITY
            );

            return result;
        }

        double sourceVoltage = source.getVoltage();

        SeriesPath path = resolveSeriesPath();

        // Circuito aberto
        if (!path.closed()) {
            ElectricalState state =
                    new ElectricalState(
                            sourceVoltage,
                            0.0
                    );

            source.updateElectricalState(state);

            result = new ElectricalNetworkResult(
                    ElectricalNetworkStatus.OPEN_CIRCUIT,
                    state,
                    Double.POSITIVE_INFINITY
            );

            return result;
        }

        double equivalentResistance =
                path.components()
                        .stream()
                        .mapToDouble(
                                IResistiveComponent::getResistance
                        )
                        .sum();

        /*
         * Circuito fechado com resistência praticamente zero.
         *
         * Não calculamos a corrente ainda porque isso dependerá
         * da resistência interna / limite de corrente da fonte.
         */
        if (equivalentResistance
                <= ElectricalConstants.EPSILON) {

            double internalResistance =
                    source.getInternalResistance();

            /*
             * Ideal source.
             *
             * The short-circuit current is theoretically
             * infinite, so we keep it unknown for now.
             */
            if (internalResistance
                    <= ElectricalConstants.EPSILON) {

                ElectricalState state =
                        new ElectricalState(
                                sourceVoltage,
                                0.0
                        );

                source.updateElectricalState(state);

                result = new ElectricalNetworkResult(
                        ElectricalNetworkStatus.SHORT_CIRCUIT,
                        state,
                        0.0
                );

                return result;
            }

            /*
             * Real source.
             *
             * Short-circuit current is limited by the
             * source internal resistance.
             *
             * Ishort = V / Rinternal
             */
            double shortCircuitCurrent =
                    sourceVoltage / internalResistance;

            ElectricalState state =
                    new ElectricalState(
                            sourceVoltage,
                            shortCircuitCurrent
                    );

            source.updateElectricalState(state);

            updateComponentStates(
                    path.components(),
                    shortCircuitCurrent
            );

            List<ElectricalFault> faults =
                    detectCurrentFaults(
                            path.components(),
                            shortCircuitCurrent
                    );

            result = new ElectricalNetworkResult(
                    ElectricalNetworkStatus.SHORT_CIRCUIT,
                    state,
                    0.0,
                    faults
            );

            return result;
        }

        if (!Double.isFinite(equivalentResistance)) {
            throw new IllegalStateException(
                    "Invalid equivalent resistance."
            );
        }

        // Circuito válido, mas fonte desligada.
        if (Math.abs(sourceVoltage)
                <= ElectricalConstants.EPSILON) {

            source.updateElectricalState(
                    ElectricalState.ZERO
            );

            result = new ElectricalNetworkResult(
                    ElectricalNetworkStatus.INACTIVE,
                    ElectricalState.ZERO,
                    equivalentResistance
            );

            return result;
        }

        double totalResistance =
                equivalentResistance
                        + source.getInternalResistance();

        double current =
                sourceVoltage / totalResistance;

        ElectricalState state =
                new ElectricalState(
                        sourceVoltage,
                        current
                );

        source.updateElectricalState(state);

        updateComponentStates(
                path.components(),
                current
        );

        List<ElectricalFault> faults =
                detectCurrentFaults(
                        path.components(),
                        current
                );

        ElectricalNetworkStatus status =
                faults.isEmpty()
                        ? ElectricalNetworkStatus.ACTIVE
                        : ElectricalNetworkStatus.OVERCURRENT;

        result = new ElectricalNetworkResult(
                status,
                state,
                equivalentResistance,
                faults
        );

        return result;
    }

    /**
     * Follows a single series path from the positive terminal
     * of the source until the negative terminal.
     * Parallel branches are intentionally not supported yet.
     */
    private SeriesPath resolveSeriesPath() {
        if (source == null) {
            return SeriesPath.OPEN;
        }

        List<IResistiveComponent> path =
                new ArrayList<>();

        Set<IResistiveComponent> visited =
                java.util.Collections.newSetFromMap(
                        new IdentityHashMap<>()
                );

        ElectricalPort current =
                source.getPositiveTerminal();

        while (true) {
            ElectricalConnection connection =
                    getSingleConnection(current);

            // Caminho terminou antes de voltar à fonte
            if (connection == null) {
                return SeriesPath.OPEN;
            }

            ElectricalPort next =
                    connection.getOther(current);

            // Circuito fechou no negativo da fonte
            if (next == source.getNegativeTerminal()) {

                // Também garante que não existe branch
                // no terminal negativo.
                getSingleConnection(next);

                return new SeriesPath(
                        true,
                        List.copyOf(path)
                );
            }

            IResistiveComponent component =
                    findComponentByPort(next);

            if (component == null) {
                throw new IllegalStateException(
                        "Connection leads to an unknown electrical component."
                );
            }

            if (component instanceof IOperationalComponent operational
                    && !operational.isOperational()) {

                return SeriesPath.OPEN;
            }

            if (!visited.add(component)) {
                throw new IllegalStateException(
                        "Electrical loop detected in series-only network."
                );
            }

            List<ElectricalPort> ports =
                    component.getPorts();

            if (ports.size() != 2) {
                throw new IllegalStateException(
                        "Series resistive components must have exactly two ports."
                );
            }

            ElectricalPort exitPort;

            if (ports.get(0) == next) {
                exitPort = ports.get(1);
            } else if (ports.get(1) == next) {
                exitPort = ports.get(0);
            } else {
                throw new IllegalStateException(
                        "Component does not contain the expected port."
                );
            }

            path.add(component);
            current = exitPort;
        }
    }

    private ElectricalConnection getSingleConnection(
            ElectricalPort port
    ) {
        ElectricalConnection found = null;

        for (ElectricalConnection connection : connections) {
            if (!connection.contains(port)) {
                continue;
            }

            if (found != null) {
                throw new IllegalStateException(
                        "Parallel or branching connections are not supported yet."
                );
            }

            found = connection;
        }

        return found;
    }

    private IResistiveComponent findComponentByPort(
            ElectricalPort port
    ) {
        for (IResistiveComponent component : components) {
            for (ElectricalPort componentPort
                    : component.getPorts()) {

                if (componentPort == port) {
                    return component;
                }
            }
        }

        return null;
    }

    private boolean isKnownPort(ElectricalPort port) {
        if (source != null) {
            for (ElectricalPort sourcePort : source.getPorts()) {
                if (sourcePort == port) {
                    return true;
                }
            }
        }

        return findComponentByPort(port) != null;
    }

    private void updateComponentStates(
            List<IResistiveComponent> activeComponents,
            double current
    ) {
        for (IResistiveComponent component
                : activeComponents) {

            double voltageDrop =
                    current * component.getResistance();

            component.updateElectricalState(
                    new ElectricalState(
                            voltageDrop,
                            current
                    )
            );
        }
    }

    private void resetComponentStates() {
        for (IResistiveComponent component : components) {
            component.updateElectricalState(
                    ElectricalState.ZERO
            );
        }
    }

    private void invalidate() {
        result = new ElectricalNetworkResult(
                ElectricalNetworkStatus.UNRESOLVED,
                ElectricalState.ZERO,
                Double.POSITIVE_INFINITY
        );

        if (source != null) {
            source.updateElectricalState(
                    ElectricalState.ZERO
            );
        }

        resetComponentStates();
    }

    public void addComponent(
            IResistiveComponent component
    ) {
        components.add(
                Objects.requireNonNull(component)
        );

        invalidate();
    }

    public boolean removeComponent(
            IResistiveComponent component
    ) {
        boolean removed =
                components.remove(component);

        if (!removed) {
            return false;
        }

        connections.removeIf(connection ->
                component.getPorts()
                        .stream()
                        .anyMatch(connection::contains)
        );

        component.updateElectricalState(
                ElectricalState.ZERO
        );

        invalidate();

        return true;
    }

    public void addConnection(
            ElectricalConnection connection
    ) {
        Objects.requireNonNull(connection);

        if (!isKnownPort(connection.getFirst())
                || !isKnownPort(connection.getSecond())) {

            throw new IllegalArgumentException(
                    "Both ports must belong to this electrical network."
            );
        }

        boolean duplicate =
                connections.stream()
                        .anyMatch(existing ->
                                (existing.getFirst()
                                        == connection.getFirst()
                                        && existing.getSecond()
                                        == connection.getSecond())
                                        ||
                                        (existing.getFirst()
                                                == connection.getSecond()
                                                && existing.getSecond()
                                                == connection.getFirst())
                        );

        if (duplicate) {
            throw new IllegalArgumentException(
                    "Electrical connection already exists."
            );
        }

        connections.add(connection);
        invalidate();
    }

    public boolean removeConnection(
            ElectricalConnection connection
    ) {
        boolean removed =
                connections.remove(connection);

        if (removed) {
            invalidate();
        }

        return removed;
    }

    public double getEquivalentResistance() {
        SeriesPath path = resolveSeriesPath();

        if (!path.closed()) {
            return Double.POSITIVE_INFINITY;
        }

        return path.components()
                .stream()
                .mapToDouble(
                        IResistiveComponent::getResistance
                )
                .sum();
    }

    public void setSource(IElectricalSource source) {
        Objects.requireNonNull(source);

        if (this.source != null
                && this.source != source) {

            IElectricalSource oldSource =
                    this.source;

            connections.removeIf(connection ->
                    oldSource.getPorts()
                            .stream()
                            .anyMatch(connection::contains)
            );

            oldSource.updateElectricalState(
                    ElectricalState.ZERO
            );
        }

        this.source = source;

        invalidate();
    }

    public void clearSource() {
        if (source == null) {
            return;
        }

        IElectricalSource oldSource = source;

        connections.removeIf(connection ->
                oldSource.getPorts()
                        .stream()
                        .anyMatch(connection::contains)
        );

        source.updateElectricalState(
                ElectricalState.ZERO
        );

        source = null;

        invalidate();
    }

    public IElectricalSource getSource() {
        return source;
    }

    public List<IResistiveComponent> getComponents() {
        return List.copyOf(components);
    }

    public List<ElectricalConnection> getConnections() {
        return List.copyOf(connections);
    }

    private record SeriesPath(
            boolean closed,
            List<IResistiveComponent> components
    ) {
        private static final SeriesPath OPEN =
                new SeriesPath(false, List.of());
    }

    public ElectricalNetworkResult getResult() {
        return result;
    }

    private List<ElectricalFault> detectCurrentFaults(
            List<IResistiveComponent> activeComponents,
            double current
    ) {
        List<ElectricalFault> faults = new ArrayList<>();

        // Verifica o limite de corrente da fonte
        if (source != null) {
            checkCurrentLimit(
                    source,
                    current,
                    faults
            );
        }

        // Verifica o limite de corrente dos componentes
        // que participam do caminho elétrico ativo.
        for (IResistiveComponent component : activeComponents) {
            if (component instanceof ICurrentLimitedComponent limited) {
                checkCurrentLimit(
                        limited,
                        current,
                        faults
                );
            }
        }

        return faults;
    }

    private void checkCurrentLimit(
            ICurrentLimitedComponent component,
            double current,
            List<ElectricalFault> faults
    ) {
        if (current
                > component.getMaxCurrent()
                + ElectricalConstants.EPSILON) {

            faults.add(
                    new ElectricalFault(
                            ElectricalFaultType.OVERCURRENT,
                            component,
                            current,
                            component.getMaxCurrent()
                    )
            );
        }
    }
}
