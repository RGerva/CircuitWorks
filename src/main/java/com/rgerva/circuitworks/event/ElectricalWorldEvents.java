/**
 * Generic Class: ElectricalWorldEvents <T>
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

package com.rgerva.circuitworks.event;

import com.rgerva.circuitworks.CircuitWorks;
import com.rgerva.circuitworks.attachment.ModAttachments;
import com.rgerva.circuitworks.block.entity.custom.DCVoltageSourceBlockEntity;
import com.rgerva.circuitworks.block.entity.custom.ResistiveLoadBlockEntity;
import com.rgerva.circuitworks.electrical.component.DCVoltageSourceComponent;
import com.rgerva.circuitworks.electrical.component.ResistiveLoadComponent;
import com.rgerva.circuitworks.electrical.component.WireComponent;
import com.rgerva.circuitworks.electrical.simulation.ElectricalSimulationEvent;
import com.rgerva.circuitworks.electrical.simulation.ElectricalSimulationEventType;
import com.rgerva.circuitworks.electrical.world.ElectricalNetworkManager;
import com.rgerva.circuitworks.electrical.world.WireChunkData;
import com.rgerva.circuitworks.electrical.world.WirePersistentState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;

@EventBusSubscriber(modid = CircuitWorks.MOD_ID)
public final class ElectricalWorldEvents {

    private static final Map<ServerLevel, Set<LevelChunk>> PENDING_CHUNK_LOADS = new WeakHashMap<>();

    private ElectricalWorldEvents() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ElectricalWorldEvents::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(ElectricalWorldEvents::onChunkUnload);
        NeoForge.EVENT_BUS.addListener(ElectricalWorldEvents::onLevelUnload);
    }


    private static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        PENDING_CHUNK_LOADS
                .computeIfAbsent(serverLevel, ignored -> new HashSet<>())
                .add(event.getChunk());
    }

    private static void onChunkUnload(
            ChunkEvent.Unload event
    ) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        LevelChunk chunk = event.getChunk();

        ElectricalNetworkManager manager =
                ElectricalNetworkManager.get(serverLevel);

        WireChunkData data =
                chunk.getData(
                        ModAttachments.WIRE_CHUNK_DATA
                );

        List<BlockPos> positions =
                data.wirePositions()
                        .stream()
                        .map(BlockPos::of)
                        .toList();

        int removed =
                manager.unregisterWires(positions);

        if (removed > 0) {
            CircuitWorks.LOGGER.info(
                    "[Electrical] Chunk {} unloaded | removed {} runtime wire(s) | tracked={} | networks={}",
                    chunk.getPos(),
                    removed,
                    manager.getWireCount(),
                    manager.getNetworkCount()
            );
        }
    }

    private static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        PENDING_CHUNK_LOADS.remove(serverLevel);
        ElectricalNetworkManager.unload(serverLevel);
        PENDING_CHUNK_LOADS.remove(serverLevel);

        CircuitWorks.LOGGER.info(
                "[Electrical] Network manager unloaded for dimension {}", serverLevel.dimension().identifier());
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        processPendingChunkLoads(serverLevel);

        ElectricalNetworkManager manager =
                ElectricalNetworkManager.get(serverLevel);

        List<ElectricalSimulationEvent> events =
                manager.tickSimulation(20.0, 0.05);

        if (serverLevel.getGameTime() % 20 == 0) {
            markElectricalBlockEntitiesChanged(
                    serverLevel,
                    manager
            );

            syncWirePersistentStates(
                    serverLevel,
                    manager
            );

        }

        for (ElectricalSimulationEvent simulationEvent : events) {
            handleSimulationEvent(
                    serverLevel,
                    manager,
                    simulationEvent
            );
        }
    }

    private static void logLoadThermalState(
            ElectricalNetworkManager manager
    ) {
        for (BlockPos pos : manager.getLoadPositions()) {
            manager.getLoad(pos).ifPresent(load ->
                    CircuitWorks.LOGGER.info(
                            "[Electrical][Load Thermal] pos={} | T={} C | V={} V | I={} A",
                            pos.toShortString(),
                            String.format(
                                    Locale.ROOT,
                                    "%.2f",
                                    load.getThermalState()
                                            .temperatureCelsius()
                            ),
                            String.format(
                                    Locale.ROOT,
                                    "%.3f",
                                    load.getElectricalState()
                                            .voltage()
                            ),
                            String.format(
                                    Locale.ROOT,
                                    "%.3f",
                                    load.getElectricalState()
                                            .current()
                            )
                    )
            );
        }
    }

    private static void processPendingChunkLoads(
            ServerLevel level
    ) {
        Set<LevelChunk> pending =
                PENDING_CHUNK_LOADS.remove(level);

        if (pending == null || pending.isEmpty()) {
            return;
        }

        for (LevelChunk chunk : pending) {
            restoreElectricalChunk(level, chunk);
        }
    }

    private static void restoreElectricalChunk(
            ServerLevel level,
            LevelChunk chunk
    ) {
        ElectricalNetworkManager manager =
                ElectricalNetworkManager.get(level);

        WireChunkData data =
                chunk.getData(
                        ModAttachments.WIRE_CHUNK_DATA
                );

        Map<BlockPos, WirePersistentState> restoredStates =
                new HashMap<>();

        for (Map.Entry<Long, WirePersistentState> entry :
                data.wireStates().entrySet()) {

            restoredStates.put(
                    BlockPos.of(entry.getKey()),
                    entry.getValue()
            );
        }

        int restoredWires =
                manager.registerWires(restoredStates);


        if (restoredWires == 0) {
            return;
        }

        double hottestWire =
                restoredStates.values()
                        .stream()
                        .mapToDouble(
                                WirePersistentState::temperatureCelsius
                        )
                        .max()
                        .orElse(
                                WireComponent.DEFAULT_INITIAL_TEMPERATURE
                        );

        if (restoredWires > 0) {
            CircuitWorks.LOGGER.info(
                    "[Electrical] Chunk {} loaded | restored {} wire(s) | tracked={} | networks={} | hottestWire={} C",
                    chunk.getPos(),
                    restoredWires,
                    manager.getWireCount(),
                    manager.getNetworkCount(),
                    String.format(
                            Locale.ROOT,
                            "%.2f",
                            hottestWire
                    )
            );
        }
    }

    private static void markElectricalBlockEntitiesChanged(ServerLevel level, ElectricalNetworkManager manager) {
        for (BlockPos pos : manager.getSourcePositions()) {
            if (level.getBlockEntity(pos) instanceof DCVoltageSourceBlockEntity blockEntity) {
                blockEntity.setChanged();
            }
        }

        for (BlockPos pos : manager.getLoadPositions()) {
            if (level.getBlockEntity(pos)
                    instanceof ResistiveLoadBlockEntity blockEntity) {

                blockEntity.setChanged();
            }
        }
    }

    private static void syncWirePersistentStates(ServerLevel level, ElectricalNetworkManager manager) {
        Map<LevelChunk, WireChunkData> updates = new HashMap<>();

        for (Map.Entry<BlockPos, WirePersistentState> entry : manager.getWirePersistentStates().entrySet()) {
            BlockPos pos = entry.getKey();
            LevelChunk chunk = level.getChunkAt(pos);

            WireChunkData data = updates.computeIfAbsent(chunk,
                    current -> current.getData(ModAttachments.WIRE_CHUNK_DATA));

            updates.put(chunk, data.withState(pos, entry.getValue()));
        }

        for (Map.Entry<LevelChunk, WireChunkData> entry : updates.entrySet()) {
            LevelChunk chunk = entry.getKey();
            WireChunkData updatedData = entry.getValue();

            WireChunkData currentData = chunk.getData(ModAttachments.WIRE_CHUNK_DATA);

            if (!updatedData.equals(currentData)) {
                chunk.setData(ModAttachments.WIRE_CHUNK_DATA, updatedData);
            }
        }
    }

    private static void markSourceBlockEntitiesChanged(ServerLevel level, ElectricalNetworkManager manager) {
        for (BlockPos pos : manager.getSourcePositions()) {
            if (level.getBlockEntity(pos)
                    instanceof DCVoltageSourceBlockEntity sourceBlockEntity) {

                sourceBlockEntity.setChanged();
            }
        }
    }

    private static void handleSimulationEvent(ServerLevel level, ElectricalNetworkManager manager, ElectricalSimulationEvent event) {
        BlockPos pos = manager.findComponentPosition(event.component()).orElse(null);

        if (pos != null) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof DCVoltageSourceBlockEntity || blockEntity instanceof ResistiveLoadBlockEntity) {
                blockEntity.setChanged();
            }
        }

        CircuitWorks.LOGGER.info(
                "[Electrical] Thermal event {} at {} | component={} | T={} C",
                event.type(),
                pos != null ? pos.toShortString() : "unknown",
                event.component().getClass().getSimpleName(),
                String.format(Locale.ROOT, "%.2f", event.temperatureCelsius())
        );

        if (event.type() != ElectricalSimulationEventType.COMPONENT_FAILED) {
            return;
        }

        if (pos == null) {
            CircuitWorks.LOGGER.warn(
                    "[Electrical] Failed component has no world position: {}",
                    event.component().getClass().getSimpleName()
            );
            return;
        }

        handleComponentFailure(level, pos, event);
    }

    private static void handleComponentFailure(ServerLevel level, BlockPos pos, ElectricalSimulationEvent event) {
        if (event.component() instanceof WireComponent) {
            handleWireFailure(level, pos);
            return;
        }

        if (event.component() instanceof DCVoltageSourceComponent) {
            handleSourceFailure(pos);
        }

        if (event.component() instanceof ResistiveLoadComponent) {
            handleLoadFailure(pos);
        }
    }

    private static void handleWireFailure(ServerLevel level, BlockPos pos) {
        CircuitWorks.LOGGER.warn("[Electrical] Wire FAILED at {} | removing block", pos.toShortString());

        boolean removed = level.destroyBlock(pos, false);

        if (!removed) {
            CircuitWorks.LOGGER.warn("[Electrical] Failed wire could not be removed at {}", pos.toShortString());
        }
    }

    private static void handleSourceFailure(BlockPos pos) {
        CircuitWorks.LOGGER.warn("[Electrical] DC source FAILED at {} | source disabled", pos.toShortString());
    }

    private static void handleLoadFailure(BlockPos pos) {
        CircuitWorks.LOGGER.warn("[Electrical] Resistive load FAILED at {} | load disabled", pos.toShortString());
    }
}
