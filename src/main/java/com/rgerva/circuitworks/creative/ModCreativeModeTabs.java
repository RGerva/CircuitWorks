/**
 * Generic Class: ModCreativeModeTabs <T>
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

package com.rgerva.circuitworks.creative;

import com.rgerva.circuitworks.CircuitWorks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CircuitWorks.MOD_ID);


    public static final Supplier<CreativeModeTab> CIRCUITWORKS_ITEMS_TAB = CREATIVE_MODE_TABS.register("tab.ezfarm",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(Blocks.AIR))
                    .title(Component.translatable("itemGroup.circuit_works"))
                    .displayItems((itemDisplayParameters, output) -> {

                    }).build());

    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
