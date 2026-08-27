/**
 * Generic Class: ModBlockEntities <T>
 * A generic structure that works with type parameters.
 *
 * <p>Created by: superuser
 * <p>On: 2026/ago.
 *
 * <p>GitHub: https://github.com/RGerva
 *
 * <p>Copyright (c) 2026 @RGerva. All Rights Reserved.
 *
 * <p>Licensed under the MIT License.
 */

package com.rgerva.circuitworks.block.entity;

import com.rgerva.circuitworks.CircuitWorks;
import com.rgerva.circuitworks.block.ModBlocks;
import com.rgerva.circuitworks.block.entity.custom.DCVoltageSourceBlockEntity;
import com.rgerva.circuitworks.block.entity.custom.ResistiveLoadBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CircuitWorks.MOD_ID);

    public static final Supplier<BlockEntityType<DCVoltageSourceBlockEntity>> DC_VOLTAGE_SOURCE =
            BLOCK_ENTITIES.register("dc_voltage_source",
                    () -> new BlockEntityType<>(DCVoltageSourceBlockEntity::new, false,
                            ModBlocks.DC_VOLTAGE_SOURCE.get()));

    public static final Supplier<BlockEntityType<ResistiveLoadBlockEntity>> RESISTIVE_LOAD =
            BLOCK_ENTITIES.register("resistive_load",
                    () -> new BlockEntityType<>(ResistiveLoadBlockEntity::new, false,
                            ModBlocks.RESISTIVE_LOAD.get()));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
