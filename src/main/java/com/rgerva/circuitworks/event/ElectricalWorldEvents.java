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
import com.rgerva.circuitworks.electrical.world.ElectricalNetworkManager;

import com.rgerva.circuitworks.electrical.world.WireChunkData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.List;

public final class ElectricalWorldEvents {

    private ElectricalWorldEvents() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(
                ElectricalWorldEvents::onChunkLoad
        );

        NeoForge.EVENT_BUS.addListener(
                ElectricalWorldEvents::onChunkUnload
        );

        NeoForge.EVENT_BUS.addListener(
                ElectricalWorldEvents::onLevelUnload
        );
    }

    private static void onChunkLoad(
            ChunkEvent.Load event
    ) {
        if (!(event.getLevel()
                instanceof ServerLevel serverLevel)) {

            return;
        }

        LevelChunk chunk =
                event.getChunk();

        WireChunkData data =
                chunk.getData(
                        ModAttachments.WIRE_CHUNK_DATA
                );

        ElectricalNetworkManager manager =
                ElectricalNetworkManager.get(
                        serverLevel
                );

        List<BlockPos> positions =
                data.wirePositions()
                        .stream()
                        .map(BlockPos::of)
                        .toList();

        int restored =
                manager.registerWires(
                        positions
                );

        if (restored > 0) {
            CircuitWorks.LOGGER.info(
                    "[Electrical] Chunk {} loaded | restored {} wire(s) | tracked={} | networks={}",
                    chunk.getPos(),
                    restored,
                    manager.getWireCount(),
                    manager.getNetworkCount()
            );
        }
    }

    private static void onChunkUnload(
            ChunkEvent.Unload event
    ) {
        if (!(event.getLevel()
                instanceof ServerLevel serverLevel)) {

            return;
        }

        LevelChunk chunk =
                event.getChunk();

        WireChunkData data =
                chunk.getData(
                        ModAttachments.WIRE_CHUNK_DATA
                );

        ElectricalNetworkManager manager =
                ElectricalNetworkManager.get(
                        serverLevel
                );

        List<BlockPos> positions =
                data.wirePositions()
                        .stream()
                        .map(BlockPos::of)
                        .toList();

        int removed =
                manager.unregisterWires(
                        positions
                );

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

    private static void onLevelUnload(
            LevelEvent.Unload event
    ) {
        if (!(event.getLevel()
                instanceof ServerLevel serverLevel)) {

            return;
        }

        ElectricalNetworkManager.unload(
                serverLevel
        );

        CircuitWorks.LOGGER.info(
                "[Electrical] Network manager unloaded for dimension {}",
                serverLevel.dimension().identifier()
        );
    }
}
