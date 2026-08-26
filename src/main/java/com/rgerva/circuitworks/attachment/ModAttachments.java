/**
 * Generic Class: ModAttachments <T>
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

package com.rgerva.circuitworks.attachment;

import com.rgerva.circuitworks.CircuitWorks;

import com.rgerva.circuitworks.electrical.world.WireChunkData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, CircuitWorks.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WireChunkData>> WIRE_CHUNK_DATA =
            ATTACHMENT_TYPES.register("wire_chunk_data",
                    () -> AttachmentType
                            .builder(WireChunkData::empty)
                            .serialize(WireChunkData.CODEC)
                            .build()
            );

    private ModAttachments() {
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
