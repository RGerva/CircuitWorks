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
import com.rgerva.circuitworks.electrical.component.WireComponent;
import com.rgerva.circuitworks.electrical.network.ElectricalNetworkResult;
import com.rgerva.circuitworks.electrical.world.*;
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

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (oldState.is(this)) {
            return;
        }

        LevelChunk chunk = serverLevel.getChunkAt(pos);
        WireChunkData currentData = chunk.getData(ModAttachments.WIRE_CHUNK_DATA);
        WireChunkData newData = currentData.withWire(pos);

        chunk.setData(ModAttachments.WIRE_CHUNK_DATA, newData);
        ElectricalNetworkManager manager = ElectricalNetworkManager.get(serverLevel);

        manager.registerWire(pos);

        WireNetwork network = manager.getNetworkAt(pos).orElseThrow();
        ElectricalWorldNetwork worldNetwork = manager.getElectricalWorldNetworkAt(pos).orElseThrow();

        WireComponent electricalComponent = manager.getWireComponent(pos).orElseThrow();

        CircuitWorks.LOGGER.info(
                "[Electrical] Wire placed at {} | tracked={} | components={} | wireNetworks={} | worldNetworks={} | wireNetwork=#{}({}) | worldNetwork=#{} wires={} sources={} loads={}",
                pos.toShortString(),
                manager.getWireCount(),
                manager.getWireComponentCount(),
                manager.getNetworkCount(),
                manager.getElectricalWorldNetworkCount(),
                network.id(),
                network.size(),
                worldNetwork.id(),
                worldNetwork.getWireCount(),
                worldNetwork.getSourceCount(),
                worldNetwork.getLoadCount()
        );

        if (worldNetwork.getSourceCount() > 0) {
            WorldCircuitResult circuit = manager.resolveWorldCircuit(worldNetwork);

            if (circuit.electricalResult().isPresent()) {
                ElectricalNetworkResult electrical = circuit.electricalResult().orElseThrow();

                CircuitWorks.LOGGER.info(
                        "[Electrical] Circuit {} | electrical={} | networkWires={} | networkLoads={} | pathComponents={} | V={} V | I={} A | Req={} ohm | faults={}",
                        circuit.status(),
                        electrical.status(),
                        worldNetwork.getWireCount(),
                        worldNetwork.getLoadCount(),
                        circuit.componentPath().size(),
                        String.format("%.2f", electrical.state().voltage()),
                        String.format("%.3f", electrical.state().current()),
                        String.format("%.3f", electrical.equivalentResistance()),
                        electrical.faults().size()
                );
            } else {
                CircuitWorks.LOGGER.info(
                        "[Electrical] Circuit {} | networkWires={} | networkLoads={} | pathComponents={}",
                        circuit.status(),
                        worldNetwork.getWireCount(),
                        worldNetwork.getLoadCount(),
                        circuit.componentPath().size()
                );
            }
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {

        LevelChunk chunk = level.getChunkAt(pos);

        WireChunkData currentData =
                chunk.getData(
                        ModAttachments.WIRE_CHUNK_DATA);

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