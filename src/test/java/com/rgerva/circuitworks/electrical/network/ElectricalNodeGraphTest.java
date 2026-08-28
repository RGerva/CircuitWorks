/**
 * Generic Class: ElectricalNodeGraphTest <T>
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

import com.rgerva.circuitworks.electrical.component.DCVoltageSourceComponent;
import com.rgerva.circuitworks.electrical.component.ResistiveLoadComponent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ElectricalNodeGraphTest {

    @Test
    void parallelLoadsShouldShareTwoElectricalNodes() {
        DCVoltageSourceComponent source =
                new DCVoltageSourceComponent(
                        12.0,
                        0.1,
                        10.0
                );

        ResistiveLoadComponent load1 =
                new ResistiveLoadComponent(10.0);

        ResistiveLoadComponent load2 =
                new ResistiveLoadComponent(20.0);

        List<ElectricalConnection> connections =
                List.of(
                        new ElectricalConnection(
                                source.getPositiveTerminal(),
                                load1.getPorts().get(0)
                        ),
                        new ElectricalConnection(
                                source.getPositiveTerminal(),
                                load2.getPorts().get(0)
                        ),
                        new ElectricalConnection(
                                load1.getPorts().get(1),
                                source.getNegativeTerminal()
                        ),
                        new ElectricalConnection(
                                load2.getPorts().get(1),
                                source.getNegativeTerminal()
                        )
                );

        ElectricalNodeGraph graph =
                ElectricalNodeGraph.build(
                        source,
                        List.of(load1, load2),
                        connections
                );

        /*
         * NODE +
         *
         * source+
         * load1.A
         * load2.A
         */
        assertTrue(
                graph.isSameNode(
                        source.getPositiveTerminal(),
                        load1.getPorts().get(0)
                )
        );

        assertTrue(
                graph.isSameNode(
                        source.getPositiveTerminal(),
                        load2.getPorts().get(0)
                )
        );

        /*
         * NODE -
         *
         * source-
         * load1.B
         * load2.B
         */
        assertTrue(
                graph.isSameNode(
                        source.getNegativeTerminal(),
                        load1.getPorts().get(1)
                )
        );

        assertTrue(
                graph.isSameNode(
                        source.getNegativeTerminal(),
                        load2.getPorts().get(1)
                )
        );

        /*
         * Os dois lados da alimentação
         * continuam sendo nós diferentes.
         */
        assertFalse(
                graph.isSameNode(
                        source.getPositiveTerminal(),
                        source.getNegativeTerminal()
                )
        );
    }
}