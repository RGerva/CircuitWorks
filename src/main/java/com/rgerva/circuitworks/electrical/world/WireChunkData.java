/**
 * Record: WireChunkData
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
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record WireChunkData(
        Set<Long> wirePositions
) {

    public static final MapCodec<WireChunkData> CODEC =
            Codec.LONG
                    .listOf()
                    .fieldOf("wire_positions")
                    .xmap(
                            values ->
                                    new WireChunkData(
                                            new HashSet<>(values)
                                    ),
                            data ->
                                    List.copyOf(
                                            data.wirePositions()
                                    )
                    );

    public WireChunkData {
        wirePositions =
                Set.copyOf(wirePositions);
    }

    public static WireChunkData empty() {
        return new WireChunkData(
                Set.of()
        );
    }

    public boolean contains(
            BlockPos pos
    ) {
        return wirePositions.contains(
                pos.asLong()
        );
    }

    public WireChunkData withWire(
            BlockPos pos
    ) {
        Set<Long> positions =
                new HashSet<>(
                        wirePositions
                );

        positions.add(
                pos.asLong()
        );

        return new WireChunkData(
                positions
        );
    }

    public WireChunkData withoutWire(
            BlockPos pos
    ) {
        Set<Long> positions =
                new HashSet<>(
                        wirePositions
                );

        positions.remove(
                pos.asLong()
        );

        return new WireChunkData(
                positions
        );
    }

    public int size() {
        return wirePositions.size();
    }
}
