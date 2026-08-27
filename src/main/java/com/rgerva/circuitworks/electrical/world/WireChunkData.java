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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.*;

public record WireChunkData(
        Map<Long, WirePersistentState> wireStates
) {

    private record WireEntry(
            long position,
            WirePersistentState state
    ) {
        private static final Codec<WireEntry> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        Codec.LONG.fieldOf("position")
                                .forGetter(WireEntry::position),

                        WirePersistentState.CODEC.fieldOf("state")
                                .forGetter(WireEntry::state)
                ).apply(instance, WireEntry::new));
    }

    public static final com.mojang.serialization.MapCodec<WireChunkData> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    WireEntry.CODEC.listOf()
                            .optionalFieldOf("wire_states", List.of())
                            .forGetter(WireChunkData::serializedEntries),

                    /*
                     * Campo antigo.
                     *
                     * Mantemos leitura para não quebrar os
                     * mundos criados até agora.
                     */
                    Codec.LONG.listOf()
                            .optionalFieldOf("wire_positions", List.of())
                            .forGetter(data -> List.of())
            ).apply(instance, WireChunkData::fromSerialized));

    public WireChunkData {
        wireStates = Map.copyOf(wireStates);
    }

    public static WireChunkData empty() {
        return new WireChunkData(Map.of());
    }

    private static WireChunkData fromSerialized(
            List<WireEntry> entries,
            List<Long> legacyPositions
    ) {
        Map<Long, WirePersistentState> states = new HashMap<>();

        /*
         * Saves antigos não possuem temperatura/status.
         */
        for (long position : legacyPositions) {
            states.put(position, WirePersistentState.defaultState());
        }

        /*
         * O formato novo prevalece caso ambos existam.
         */
        for (WireEntry entry : entries) {
            states.put(entry.position(), entry.state());
        }

        return new WireChunkData(states);
    }

    private List<WireEntry> serializedEntries() {
        return wireStates.entrySet()
                .stream()
                .map(entry -> new WireEntry(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();
    }

    public Set<Long> wirePositions() {
        return Set.copyOf(wireStates.keySet());
    }

    public Optional<WirePersistentState> getState(BlockPos pos) {
        return Optional.ofNullable(wireStates.get(pos.asLong()));
    }

    public boolean contains(BlockPos pos) {
        return wireStates.containsKey(pos.asLong());
    }

    public WireChunkData withWire(BlockPos pos) {
        return withWire(pos, WirePersistentState.defaultState());
    }

    public WireChunkData withWire(
            BlockPos pos,
            WirePersistentState state
    ) {
        long packedPos = pos.asLong();

        if (wireStates.containsKey(packedPos)) {
            return this;
        }

        Map<Long, WirePersistentState> updated =
                new HashMap<>(wireStates);

        updated.put(packedPos, state);

        return new WireChunkData(updated);
    }

    public WireChunkData withState(
            BlockPos pos,
            WirePersistentState state
    ) {
        long packedPos = pos.asLong();

        if (!wireStates.containsKey(packedPos)) {
            return this;
        }

        Map<Long, WirePersistentState> updated =
                new HashMap<>(wireStates);

        updated.put(packedPos, state);

        return new WireChunkData(updated);
    }

    public WireChunkData withoutWire(BlockPos pos) {
        long packedPos = pos.asLong();

        if (!wireStates.containsKey(packedPos)) {
            return this;
        }

        Map<Long, WirePersistentState> updated =
                new HashMap<>(wireStates);

        updated.remove(packedPos);

        return new WireChunkData(updated);
    }

    public int size() {
        return wireStates.size();
    }
}
