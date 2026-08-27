/**
 * Record: WirePersistentState
 * Immutable data structure for simplified object representation.
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

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rgerva.circuitworks.electrical.component.ComponentOperationalStatus;
import com.rgerva.circuitworks.electrical.component.WireComponent;

public record WirePersistentState(
        double temperatureCelsius,
        ComponentOperationalStatus operationalStatus
) {

    private static final Codec<ComponentOperationalStatus> OPERATIONAL_STATUS_CODEC =
            Codec.STRING.comapFlatMap(
                    value -> {
                        try {
                            return DataResult.success(ComponentOperationalStatus.valueOf(value));
                        } catch (IllegalArgumentException ignored) {
                            return DataResult.error(() -> "Unknown operational status: " + value);
                        }
                    },
                    ComponentOperationalStatus::name
            );

    public static final Codec<WirePersistentState> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.DOUBLE.optionalFieldOf(
                            "temperature_celsius",
                            WireComponent.DEFAULT_INITIAL_TEMPERATURE
                    ).forGetter(WirePersistentState::temperatureCelsius),

                    OPERATIONAL_STATUS_CODEC.optionalFieldOf(
                            "operational_status",
                            ComponentOperationalStatus.OPERATIONAL
                    ).forGetter(WirePersistentState::operationalStatus)
            ).apply(instance, WirePersistentState::new));

    public static WirePersistentState defaultState() {
        return new WirePersistentState(
                WireComponent.DEFAULT_INITIAL_TEMPERATURE,
                ComponentOperationalStatus.OPERATIONAL
        );
    }

    public static WirePersistentState from(WireComponent component) {
        return new WirePersistentState(
                component.getThermalState().temperatureCelsius(),
                component.getOperationalStatus()
        );
    }
}