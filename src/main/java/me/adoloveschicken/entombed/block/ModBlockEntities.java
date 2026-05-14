package me.adoloveschicken.entombed.block;

import me.adoloveschicken.entombed.Entombed;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Entombed.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GravestoneBlockEntity>> TOMB_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "tomb_block_entity",
            () -> BlockEntityType.Builder.of(
                    GravestoneBlockEntity::new,
                    ModBlocks.TOMB.get()
            ).build(null)
    );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
