/**
 * Generic Class: WireBlock <T>
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

package com.rgerva.circuitworks.block.custom;

import com.rgerva.circuitworks.CircuitWorks;
import com.rgerva.circuitworks.attachment.ModAttachments;
import com.rgerva.circuitworks.electrical.world.ElectricalNetworkManager;
import com.rgerva.circuitworks.electrical.world.WireChunkData;
import com.rgerva.circuitworks.electrical.world.WireNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public class WireBlock extends Block {
    public WireBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (!(level
                instanceof ServerLevel serverLevel)) {

            return;
        }

        if (oldState.is(this)) {
            return;
        }

        LevelChunk chunk =
                serverLevel.getChunkAt(pos);

        WireChunkData currentData =
                chunk.getData(
                        ModAttachments.WIRE_CHUNK_DATA
                );

        WireChunkData newData =
                currentData.withWire(pos);

        chunk.setData(
                ModAttachments.WIRE_CHUNK_DATA,
                newData
        );

        ElectricalNetworkManager manager =
                ElectricalNetworkManager.get(
                        serverLevel
                );

        manager.registerWire(pos);

        WireNetwork network =
                manager.getNetworkAt(pos)
                        .orElseThrow();

        CircuitWorks.LOGGER.info(
                "[Electrical] Wire placed at {} | tracked={} | networks={} | network=#{} | network wires={}",
                pos.toShortString(),
                manager.getWireCount(),
                manager.getNetworkCount(),
                network.id(),
                network.size()
        );
    }

    @Override
    protected void affectNeighborsAfterRemoval(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            boolean movedByPiston
    ) {
        LevelChunk chunk =
                level.getChunkAt(pos);

        WireChunkData currentData =
                chunk.getData(
                        ModAttachments.WIRE_CHUNK_DATA
                );

        WireChunkData newData =
                currentData.withoutWire(pos);

        chunk.setData(
                ModAttachments.WIRE_CHUNK_DATA,
                newData
        );

        ElectricalNetworkManager manager =
                ElectricalNetworkManager.get(level);

        manager.unregisterWire(pos);

        CircuitWorks.LOGGER.info(
                "[Electrical] Wire removed at {} | chunk={} | chunk wires={} | tracked={} | networks={}",
                pos.toShortString(),
                chunk.getPos(),
                newData.size(),
                manager.getWireCount(),
                manager.getNetworkCount()
        );

        super.affectNeighborsAfterRemoval(
                state,
                level,
                pos,
                movedByPiston
        );
    }
}