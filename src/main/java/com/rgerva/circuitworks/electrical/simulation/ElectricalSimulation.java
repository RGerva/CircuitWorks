/**
 * Generic Class: ElectricalSimulation <T>
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

package com.rgerva.circuitworks.electrical.simulation;

import com.rgerva.circuitworks.electrical.component.IElectricalComponent;
import com.rgerva.circuitworks.electrical.component.IElectricalSource;
import com.rgerva.circuitworks.electrical.component.IOperationalComponent;
import com.rgerva.circuitworks.electrical.component.IResistiveComponent;
import com.rgerva.circuitworks.electrical.network.ElectricalNetwork;
import com.rgerva.circuitworks.electrical.network.ElectricalNetworkResult;
import com.rgerva.circuitworks.electrical.thermal.IThermalComponent;
import com.rgerva.circuitworks.electrical.thermal.ThermalStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ElectricalSimulation {

    private final ElectricalNetwork network;

    public ElectricalSimulation(
            ElectricalNetwork network
    ) {
        this.network =
                Objects.requireNonNull(network);
    }

    public ElectricalSimulationResult step(
            double ambientTemperature,
            double deltaSeconds
    ) {
        validateStep(
                ambientTemperature,
                deltaSeconds
        );

        ElectricalNetworkResult initialResult =
                network.solve();

        List<ElectricalSimulationEvent> events =
                updateThermalStates(
                        ambientTemperature,
                        deltaSeconds
                );

        boolean operationalStateChanged =
                events.stream()
                        .anyMatch(event ->
                                event.type()
                                        == ElectricalSimulationEventType.COMPONENT_FAILED
                        );

        ElectricalNetworkResult finalResult =
                operationalStateChanged
                        ? network.solve()
                        : initialResult;

        return new ElectricalSimulationResult(
                initialResult,
                finalResult,
                events
        );
    }

    private List<ElectricalSimulationEvent> updateThermalStates(
            double ambientTemperature,
            double deltaSeconds
    ) {
        List<ElectricalSimulationEvent> events =
                new ArrayList<>();

        IElectricalSource source =
                network.getSource();

        if (source != null) {
            updateThermalComponent(
                    source,
                    ambientTemperature,
                    deltaSeconds,
                    events
            );
        }

        for (IResistiveComponent component
                : network.getComponents()) {

            updateThermalComponent(
                    component,
                    ambientTemperature,
                    deltaSeconds,
                    events
            );
        }

        return events;
    }

    private void updateThermalComponent(
            IElectricalComponent component,
            double ambientTemperature,
            double deltaSeconds,
            List<ElectricalSimulationEvent> events
    ) {
        if (!(component
                instanceof IThermalComponent thermalComponent)) {

            return;
        }

        ThermalStatus previousThermalStatus =
                thermalComponent.getThermalStatus();

        boolean wasOperational =
                isOperational(component);

        thermalComponent.updateThermalState(
                ambientTemperature,
                deltaSeconds
        );

        ThermalStatus currentThermalStatus =
                thermalComponent.getThermalStatus();

        boolean isOperational =
                isOperational(component);

        /*
         * Eventos térmicos reversíveis só são
         * relevantes enquanto o componente ainda
         * está operacional.
         */
        if (wasOperational
                && previousThermalStatus
                != currentThermalStatus) {

            switch (currentThermalStatus) {

                case NORMAL -> events.add(
                        createThermalEvent(
                                ElectricalSimulationEventType.COMPONENT_NORMALIZED,
                                component,
                                thermalComponent
                        )
                );

                case HOT -> {
                    /*
                     * Se veio de OVERHEATED,
                     * está esfriando.
                     *
                     * Caso contrário, veio de NORMAL
                     * e está aquecendo.
                     */
                    ElectricalSimulationEventType type =
                            previousThermalStatus
                                    == ThermalStatus.OVERHEATED
                                    ? ElectricalSimulationEventType.COMPONENT_COOLED
                                    : ElectricalSimulationEventType.COMPONENT_HOT;

                    events.add(
                            createThermalEvent(
                                    type,
                                    component,
                                    thermalComponent
                            )
                    );
                }

                case OVERHEATED -> events.add(
                        createThermalEvent(
                                ElectricalSimulationEventType.COMPONENT_OVERHEATED,
                                component,
                                thermalComponent
                        )
                );

                case FAILED -> {
                    /*
                     * A falha é tratada abaixo como
                     * mudança operacional.
                     */
                }
            }
        }

        /*
         * FAILED é permanente e representa uma
         * mudança operacional.
         */
        if (wasOperational
                && !isOperational) {

            events.add(
                    createThermalEvent(
                            ElectricalSimulationEventType.COMPONENT_FAILED,
                            component,
                            thermalComponent
                    )
            );
        }
    }

    private ElectricalSimulationEvent createThermalEvent(
            ElectricalSimulationEventType type,
            IElectricalComponent component,
            IThermalComponent thermalComponent
    ) {
        return new ElectricalSimulationEvent(
                type,
                component,
                thermalComponent
                        .getThermalState()
                        .temperatureCelsius()
        );
    }

    private boolean isOperational(
            IElectricalComponent component
    ) {
        if (component
                instanceof IOperationalComponent operational) {

            return operational.isOperational();
        }

        /*
         * Componentes que não implementam
         * IOperationalComponent são considerados
         * permanentemente operacionais.
         */
        return true;
    }

    private void validateStep(
            double ambientTemperature,
            double deltaSeconds
    ) {
        if (!Double.isFinite(ambientTemperature)) {
            throw new IllegalArgumentException(
                    "Ambient temperature must be finite."
            );
        }

        if (!Double.isFinite(deltaSeconds)
                || deltaSeconds < 0.0) {

            throw new IllegalArgumentException(
                    "Delta time must be finite and greater than or equal to zero."
            );
        }
    }

    public ElectricalNetwork getNetwork() {
        return network;
    }
}
