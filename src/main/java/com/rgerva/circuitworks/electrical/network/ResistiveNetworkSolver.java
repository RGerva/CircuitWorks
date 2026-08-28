/**
 * Generic Class: ResistiveNetworkSolver <T>
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
import com.rgerva.circuitworks.electrical.component.IElectricalSource;
import com.rgerva.circuitworks.electrical.component.IOperationalComponent;
import com.rgerva.circuitworks.electrical.component.IResistiveComponent;

import java.util.*;

public final class ResistiveNetworkSolver {

    private ResistiveNetworkSolver() {
    }

    static Solution solve(
            IElectricalSource source,
            List<IResistiveComponent> components,
            List<ElectricalConnection> connections
    ) {
        if (source == null) {
            return Solution.UNSUPPORTED;
        }

        ElectricalNodeGraph graph =
                ElectricalNodeGraph.build(
                        source,
                        components,
                        connections
                );

        ElectricalPort positiveNode =
                graph.getNode(
                        source.getPositiveTerminal()
                );

        ElectricalPort negativeNode =
                graph.getNode(
                        source.getNegativeTerminal()
                );

        if (positiveNode == negativeNode) {
            return Solution.UNSUPPORTED;
        }

        List<Edge> edges =
                new ArrayList<>();

        for (IResistiveComponent component : components) {
            List<ElectricalPort> ports =
                    component.getPorts();

            if (ports.size() != 2) {
                return Solution.UNSUPPORTED;
            }

            /*
             * Um componente FAILED representa
             * circuito aberto.
             *
             * Portanto ele continua existindo na
             * topologia, mas não cria uma aresta
             * condutiva no solver.
             */
            if (component instanceof IOperationalComponent operational
                    && !operational.isOperational()) {
                continue;
            }

            double resistance =
                    component.getResistance();

            if (!Double.isFinite(resistance)
                    || resistance
                    <= ElectricalConstants.EPSILON) {

                return Solution.UNSUPPORTED;
            }

            ElectricalPort firstNode =
                    graph.getNode(
                            ports.get(0)
                    );

            ElectricalPort secondNode =
                    graph.getNode(
                            ports.get(1)
                    );

            edges.add(
                    new Edge(
                            component,
                            firstNode,
                            secondNode
                    )
            );
        }

        Set<ElectricalPort> reachable =
                collectReachableNodes(
                        positiveNode,
                        edges
                );

        if (!reachable.contains(negativeNode)) {
            return Solution.OPEN;
        }

        List<ElectricalPort> internalNodes =
                reachable.stream()
                        .filter(node ->
                                node != positiveNode
                                        && node != negativeNode
                        )
                        .toList();

        Map<ElectricalPort, Integer> nodeIndexes =
                new IdentityHashMap<>();

        for (int i = 0;
             i < internalNodes.size();
             i++) {

            nodeIndexes.put(
                    internalNodes.get(i),
                    i
            );
        }

        double[][] matrix =
                new double[
                        internalNodes.size()
                        ][
                        internalNodes.size()
                        ];

        double[] rhs =
                new double[
                        internalNodes.size()
                        ];

        /*
         * Aplicamos 1 V entre os terminais.
         *
         * Assim:
         *
         * Req = 1 / Itotal
         *
         * e os estados encontrados ficam normalizados
         * para depois escalarmos pela tensão real
         * disponível nos terminais da fonte.
         */
        for (Edge edge : edges) {
            if (!reachable.contains(edge.firstNode())
                    || !reachable.contains(edge.secondNode())) {
                continue;
            }

            stampNode(
                    edge.firstNode(),
                    edge.secondNode(),
                    edge.conductance(),
                    positiveNode,
                    negativeNode,
                    nodeIndexes,
                    matrix,
                    rhs
            );

            stampNode(
                    edge.secondNode(),
                    edge.firstNode(),
                    edge.conductance(),
                    positiveNode,
                    negativeNode,
                    nodeIndexes,
                    matrix,
                    rhs
            );
        }

        double[] solvedInternalVoltages =
                solveLinearSystem(
                        matrix,
                        rhs
                );

        Map<ElectricalPort, Double> nodeVoltages =
                new IdentityHashMap<>();

        nodeVoltages.put(
                positiveNode,
                1.0
        );

        nodeVoltages.put(
                negativeNode,
                0.0
        );

        for (int i = 0;
             i < internalNodes.size();
             i++) {

            nodeVoltages.put(
                    internalNodes.get(i),
                    solvedInternalVoltages[i]
            );
        }

        Map<IResistiveComponent, ElectricalState>
                normalizedStates =
                new IdentityHashMap<>();

        for (IResistiveComponent component : components) {
            normalizedStates.put(
                    component,
                    ElectricalState.ZERO
            );
        }

        for (Edge edge : edges) {
            if (!reachable.contains(edge.firstNode())
                    || !reachable.contains(edge.secondNode())) {
                continue;
            }

            double firstVoltage =
                    nodeVoltages.get(
                            edge.firstNode()
                    );

            double secondVoltage =
                    nodeVoltages.get(
                            edge.secondNode()
                    );

            double voltageDrop =
                    Math.abs(
                            firstVoltage
                                    - secondVoltage
                    );

            double current =
                    voltageDrop
                            / edge.component()
                            .getResistance();

            normalizedStates.put(
                    edge.component(),
                    new ElectricalState(
                            voltageDrop,
                            current
                    )
            );
        }

        double sourceCurrent =
                calculateSourceCurrent(
                        positiveNode,
                        reachable,
                        nodeVoltages,
                        edges
                );

        if (sourceCurrent
                <= ElectricalConstants.EPSILON) {
            return Solution.OPEN;
        }

        double equivalentResistance =
                1.0 / sourceCurrent;

        return new Solution(
                true,
                true,
                equivalentResistance,
                Collections.unmodifiableMap(
                        normalizedStates
                )
        );
    }

    private static void stampNode(
            ElectricalPort node,
            ElectricalPort otherNode,
            double conductance,
            ElectricalPort positiveNode,
            ElectricalPort negativeNode,
            Map<ElectricalPort, Integer> nodeIndexes,
            double[][] matrix,
            double[] rhs
    ) {
        Integer row =
                nodeIndexes.get(node);

        /*
         * NODE+ e NODE- têm tensão conhecida,
         * portanto não possuem equação própria.
         */
        if (row == null) {
            return;
        }

        matrix[row][row] +=
                conductance;

        Integer otherColumn =
                nodeIndexes.get(otherNode);

        if (otherColumn != null) {
            matrix[row][otherColumn] -=
                    conductance;

            return;
        }

        double knownVoltage;

        if (otherNode == positiveNode) {
            knownVoltage = 1.0;
        } else if (otherNode == negativeNode) {
            knownVoltage = 0.0;
        } else {
            throw new IllegalStateException(
                    "Unknown boundary electrical node."
            );
        }

        rhs[row] +=
                conductance
                        * knownVoltage;
    }

    private static Set<ElectricalPort>
    collectReachableNodes(
            ElectricalPort start,
            List<Edge> edges
    ) {
        Set<ElectricalPort> visited =
                Collections.newSetFromMap(
                        new IdentityHashMap<>()
                );

        ArrayDeque<ElectricalPort> pending =
                new ArrayDeque<>();

        visited.add(start);
        pending.add(start);

        while (!pending.isEmpty()) {
            ElectricalPort current =
                    pending.removeFirst();

            for (Edge edge : edges) {
                ElectricalPort next = null;

                if (edge.firstNode() == current) {
                    next = edge.secondNode();
                } else if (edge.secondNode() == current) {
                    next = edge.firstNode();
                }

                if (next != null
                        && visited.add(next)) {

                    pending.addLast(next);
                }
            }
        }

        return visited;
    }

    private static double calculateSourceCurrent(
            ElectricalPort positiveNode,
            Set<ElectricalPort> reachable,
            Map<ElectricalPort, Double> voltages,
            List<Edge> edges
    ) {
        double totalCurrent = 0.0;

        for (Edge edge : edges) {
            if (!reachable.contains(edge.firstNode())
                    || !reachable.contains(edge.secondNode())) {
                continue;
            }

            ElectricalPort otherNode;

            if (edge.firstNode()
                    == positiveNode) {

                otherNode =
                        edge.secondNode();

            } else if (edge.secondNode()
                    == positiveNode) {

                otherNode =
                        edge.firstNode();

            } else {
                continue;
            }

            /*
             * Resistor ligado entre o mesmo nó
             * não possui queda de tensão.
             */
            if (otherNode == positiveNode) {
                continue;
            }

            double otherVoltage =
                    voltages.get(otherNode);

            totalCurrent +=
                    (1.0 - otherVoltage)
                            / edge.component()
                            .getResistance();
        }

        return totalCurrent;
    }

    private static double[] solveLinearSystem(
            double[][] matrix,
            double[] rhs
    ) {
        int size =
                rhs.length;

        if (size == 0) {
            return new double[0];
        }

        double[][] a =
                new double[size][size];

        double[] b =
                rhs.clone();

        for (int row = 0;
             row < size;
             row++) {

            a[row] =
                    matrix[row].clone();
        }

        /*
         * Gaussian elimination
         * com partial pivoting.
         */
        for (int pivot = 0;
             pivot < size;
             pivot++) {

            int bestRow =
                    pivot;

            for (int row = pivot + 1;
                 row < size;
                 row++) {

                if (Math.abs(a[row][pivot])
                        > Math.abs(
                        a[bestRow][pivot]
                )) {

                    bestRow = row;
                }
            }

            if (Math.abs(
                    a[bestRow][pivot]
            ) <= ElectricalConstants.EPSILON) {

                throw new IllegalStateException(
                        "Singular resistive network."
                );
            }

            if (bestRow != pivot) {
                double[] temporaryRow =
                        a[pivot];

                a[pivot] =
                        a[bestRow];

                a[bestRow] =
                        temporaryRow;

                double temporaryValue =
                        b[pivot];

                b[pivot] =
                        b[bestRow];

                b[bestRow] =
                        temporaryValue;
            }

            for (int row = pivot + 1;
                 row < size;
                 row++) {

                double factor =
                        a[row][pivot]
                                / a[pivot][pivot];

                for (int column = pivot;
                     column < size;
                     column++) {

                    a[row][column] -=
                            factor
                                    * a[pivot][column];
                }

                b[row] -=
                        factor
                                * b[pivot];
            }
        }

        double[] solution =
                new double[size];

        for (int row = size - 1;
             row >= 0;
             row--) {

            double value =
                    b[row];

            for (int column = row + 1;
                 column < size;
                 column++) {

                value -=
                        a[row][column]
                                * solution[column];
            }

            solution[row] =
                    value
                            / a[row][row];
        }

        return solution;
    }

    private record Edge(
            IResistiveComponent component,
            ElectricalPort firstNode,
            ElectricalPort secondNode
    ) {
        private double conductance() {
            return 1.0
                    / component.getResistance();
        }
    }

    record Solution(
            boolean supported,
            boolean closed,
            double equivalentResistance,
            Map<IResistiveComponent, ElectricalState>
            normalizedStates
    ) {
        private static final Solution UNSUPPORTED =
                new Solution(
                        false,
                        false,
                        Double.POSITIVE_INFINITY,
                        Map.of()
                );

        private static final Solution OPEN =
                new Solution(
                        true,
                        false,
                        Double.POSITIVE_INFINITY,
                        Map.of()
                );
    }
}
