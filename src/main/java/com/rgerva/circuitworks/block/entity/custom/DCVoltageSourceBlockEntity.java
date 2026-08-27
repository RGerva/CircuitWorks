/**
 * Generic Class: DCVoltageSourceBlockEntity <T>
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
import com.rgerva.circuitworks.block.custom.DCVoltageSourceBlock;
import com.rgerva.circuitworks.block.entity.ModBlockEntities;
import com.rgerva.circuitworks.electrical.component.ComponentOperationalStatus;
import com.rgerva.circuitworks.electrical.component.DCVoltageSourceComponent;
import com.rgerva.circuitworks.electrical.world.ElectricalNetworkManager;

import com.rgerva.circuitworks.electrical.world.ElectricalWorldNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Locale;

public class DCVoltageSourceBlockEntity extends BlockEntity {

    public static final double DEFAULT_VOLTAGE = 12.0;
    public static final double DEFAULT_INTERNAL_RESISTANCE = 0.1;
    public static final double DEFAULT_MAX_CURRENT = 10.0;

    private DCVoltageSourceComponent sourceComponent = createDefaultSource();

    public DCVoltageSourceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DC_VOLTAGE_SOURCE.get(), pos, state);
    }

    private static DCVoltageSourceComponent createDefaultSource() {
        return new DCVoltageSourceComponent(DEFAULT_VOLTAGE, DEFAULT_INTERNAL_RESISTANCE, DEFAULT_MAX_CURRENT);
    }

    public DCVoltageSourceComponent getSourceComponent() {
        return sourceComponent;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Direction positiveDirection = getBlockState().getValue(DCVoltageSourceBlock.FACING);

        Direction negativeDirection = positiveDirection.getOpposite();

        ElectricalNetworkManager manager = ElectricalNetworkManager.get(serverLevel);

        manager.registerSource(worldPosition, sourceComponent, positiveDirection, negativeDirection);

        ElectricalWorldNetwork network = manager.getElectricalWorldNetworkAt(worldPosition).orElseThrow();

        CircuitWorks.LOGGER.info(
                "[Electrical] DC source loaded at {} | V={} V | Rinternal={} ohm | Imax={} A | T={} C | status={} | +={} | -={} | sources={} | worldNetwork=#{} | wires={}",
                worldPosition.toShortString(),
                sourceComponent.getVoltage(),
                sourceComponent.getInternalResistance(),
                sourceComponent.getMaxCurrent(),
                String.format(Locale.ROOT, "%.2f", sourceComponent.getThermalState().temperatureCelsius()),
                sourceComponent.getOperationalStatus(),
                positiveDirection,
                negativeDirection,
                manager.getSourceCount(),
                network.id(),
                network.getWireCount()
        );
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel serverLevel) {
            ElectricalNetworkManager manager = ElectricalNetworkManager.get(serverLevel);

            manager.unregisterSource(worldPosition);

            CircuitWorks.LOGGER.info("[Electrical] DC source removed at {} | sources={}",
                    worldPosition.toShortString(), manager.getSourceCount());
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putDouble("voltage", sourceComponent.getVoltage());
        output.putDouble("internal_resistance", sourceComponent.getInternalResistance());
        output.putDouble("max_current", sourceComponent.getMaxCurrent());
        output.putDouble("temperature_celsius", sourceComponent.getThermalState().temperatureCelsius());
        output.putString("operational_status", sourceComponent.getOperationalStatus().name());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        double voltage = input.getDoubleOr("voltage", DEFAULT_VOLTAGE);
        double internalResistance = input.getDoubleOr("internal_resistance", DEFAULT_INTERNAL_RESISTANCE);
        double maxCurrent = input.getDoubleOr("max_current", DEFAULT_MAX_CURRENT);

        double temperature = input.getDoubleOr("temperature_celsius", DCVoltageSourceComponent.DEFAULT_INITIAL_TEMPERATURE);

        ComponentOperationalStatus operationalStatus = readOperationalStatus(input);

        sourceComponent = new DCVoltageSourceComponent(
                voltage,
                internalResistance,
                maxCurrent,
                DCVoltageSourceComponent.DEFAULT_THERMAL_PROPERTIES,
                DCVoltageSourceComponent.DEFAULT_THERMAL_LIMITS,
                temperature,
                operationalStatus
        );
    }

    private static ComponentOperationalStatus readOperationalStatus(ValueInput input) {
        String value = input.getStringOr("operational_status", ComponentOperationalStatus.OPERATIONAL.name());
        try {
            return ComponentOperationalStatus.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return ComponentOperationalStatus.OPERATIONAL;
        }
    }
}
