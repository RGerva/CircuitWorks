/**
 * Generic Class: ResistiveLoadBlockEntity <T>
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

package com.rgerva.circuitworks.block.entity.custom;

import com.rgerva.circuitworks.CircuitWorks;
import com.rgerva.circuitworks.block.custom.ResistiveLoadBlock;
import com.rgerva.circuitworks.block.entity.ModBlockEntities;
import com.rgerva.circuitworks.electrical.component.ResistiveLoadComponent;
import com.rgerva.circuitworks.electrical.world.ElectricalNetworkManager;
import com.rgerva.circuitworks.electrical.world.ElectricalWorldNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ResistiveLoadBlockEntity extends BlockEntity {

    public static final double DEFAULT_RESISTANCE = 10.0;

    private ResistiveLoadComponent loadComponent =
            createDefaultLoad();

    public ResistiveLoadBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESISTIVE_LOAD.get(), pos, state);
    }

    private static ResistiveLoadComponent createDefaultLoad() {
        return new ResistiveLoadComponent(DEFAULT_RESISTANCE);
    }

    public ResistiveLoadComponent getLoadComponent() {
        return loadComponent;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Direction terminalADirection =
                getBlockState().getValue(ResistiveLoadBlock.FACING);

        Direction terminalBDirection =
                terminalADirection.getOpposite();

        ElectricalNetworkManager manager =
                ElectricalNetworkManager.get(serverLevel);

        manager.registerLoad(
                worldPosition,
                loadComponent,
                terminalADirection,
                terminalBDirection
        );

        ElectricalWorldNetwork network =
                manager.getElectricalWorldNetworkAt(worldPosition)
                        .orElseThrow();

        CircuitWorks.LOGGER.info(
                "[Electrical] Resistive load loaded at {} | R={} ohm | A={} | B={} | loads={} | worldNetwork=#{} | wires={} | sources={} | networkLoads={}",
                worldPosition.toShortString(),
                loadComponent.getResistance(),
                terminalADirection,
                terminalBDirection,
                manager.getLoadCount(),
                network.id(),
                network.getWireCount(),
                network.getSourceCount(),
                network.getLoadCount()
        );
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel serverLevel) {
            ElectricalNetworkManager manager =
                    ElectricalNetworkManager.get(serverLevel);

            manager.unregisterLoad(worldPosition);

            CircuitWorks.LOGGER.info(
                    "[Electrical] Resistive load removed at {} | loads={}",
                    worldPosition.toShortString(),
                    manager.getLoadCount()
            );
        }

        super.setRemoved();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putDouble(
                "resistance",
                loadComponent.getResistance()
        );
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        double resistance = input.getDoubleOr(
                "resistance",
                DEFAULT_RESISTANCE
        );

        loadComponent = new ResistiveLoadComponent(resistance);
    }
}
