/**
 * Record: WorldCircuitResult
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

import com.rgerva.circuitworks.electrical.network.ElectricalNetworkResult;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record WorldCircuitResult(
        WorldCircuitStatus status,
        List<BlockPos> componentPath,
        Optional<ElectricalNetworkResult> electricalResult
) {

    public WorldCircuitResult {
        Objects.requireNonNull(status);
        Objects.requireNonNull(componentPath);
        Objects.requireNonNull(electricalResult);

        componentPath = List.copyOf(componentPath);
    }

    public static WorldCircuitResult withoutElectricalResult(
            WorldCircuitStatus status
    ) {
        return new WorldCircuitResult(
                status,
                List.of(),
                Optional.empty()
        );
    }

    public static WorldCircuitResult withElectricalResult(
            WorldCircuitStatus status,
            List<BlockPos> componentPath,
            ElectricalNetworkResult result
    ) {
        return new WorldCircuitResult(
                status,
                componentPath,
                Optional.of(result)
        );
    }
}
