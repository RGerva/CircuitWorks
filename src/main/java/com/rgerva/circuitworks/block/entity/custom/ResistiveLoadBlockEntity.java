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
import com.rgerva.circuitworks.electrical.component.ComponentOperationalStatus;
import com.rgerva.circuitworks.electrical.component.ResistiveLoadComponent;
import com.rgerva.circuitworks.electrical.world.ElectricalNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Locale;

public class ResistiveLoadBlockEntity extends BlockEntity {

    public static final double DEFAULT_RESISTANCE = 10.0;

    private ResistiveLoadComponent loadComponent = createDefaultLoad();

    private boolean electricalRegistered;

    public ResistiveLoadBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESISTIVE_LOAD.get(), pos, state);
    }

    private static ResistiveLoadComponent createDefaultLoad() {
        return new ResistiveLoadComponent(DEFAULT_RESISTANCE);
    }

    public ResistiveLoadComponent getLoadComponent() {
        return loadComponent;
    }

    public double getResistance() {
        return loadComponent.getResistance();
    }

    public void setResistance(double resistance) {
        loadComponent.setResistance(resistance);
        setChanged();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        ensureElectricalRegistration();
    }

    @Override
    public void setRemoved() {
        unregisterElectrical();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        unregisterElectrical();
        super.onChunkUnloaded();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putDouble("resistance", loadComponent.getResistance());
        output.putDouble("temperature_celsius", loadComponent.getThermalState().temperatureCelsius());
        output.putString("operational_status", loadComponent.getOperationalStatus().name());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        double resistance = input.getDoubleOr("resistance", DEFAULT_RESISTANCE);
        double temperature = input.getDoubleOr("temperature_celsius", ResistiveLoadComponent.DEFAULT_INITIAL_TEMPERATURE);

        ComponentOperationalStatus operationalStatus = readOperationalStatus(input);

        loadComponent = new ResistiveLoadComponent(resistance, ResistiveLoadComponent.DEFAULT_THERMAL_PROPERTIES,
                ResistiveLoadComponent.DEFAULT_THERMAL_LIMITS, temperature, operationalStatus);
    }

    private static ComponentOperationalStatus readOperationalStatus(ValueInput input) {
        String value = input.getStringOr("operational_status", ComponentOperationalStatus.OPERATIONAL.name());

        try {
            return ComponentOperationalStatus.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return ComponentOperationalStatus.OPERATIONAL;
        }
    }

    public void ensureElectricalRegistration() {
        if (electricalRegistered) {
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (isRemoved()) {
            return;
        }

        Direction terminalA =
                getBlockState().getValue(
                        ResistiveLoadBlock.FACING
                );

        Direction terminalB =
                terminalA.getOpposite();

        ElectricalNetworkManager manager =
                ElectricalNetworkManager.get(serverLevel);

        manager.registerLoad(
                worldPosition,
                loadComponent,
                terminalA,
                terminalB
        );

        electricalRegistered = true;

        CircuitWorks.LOGGER.info(
                "[Electrical] Resistive load registered at {} | R={} ohm | T={} C | status={} | A={} | B={}",
                worldPosition.toShortString(),
                loadComponent.getResistance(),
                String.format(
                        Locale.ROOT,
                        "%.2f",
                        loadComponent.getThermalState()
                                .temperatureCelsius()
                ),
                loadComponent.getOperationalStatus(),
                terminalA,
                terminalB
        );
    }

    private void unregisterElectrical() {
        if (!electricalRegistered) {
            return;
        }

        if (level instanceof ServerLevel serverLevel) {
            ElectricalNetworkManager.get(serverLevel)
                    .unregisterLoad(worldPosition);
        }

        electricalRegistered = false;
    }
}
