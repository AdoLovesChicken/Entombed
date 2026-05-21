package me.adoloveschicken.entombed.block;

import me.adoloveschicken.entombed.Entombed;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static void register() {
        CommonModBlockEntities.TOMB_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entombed.MODID, "tomb_block_entity"),
                BlockEntityType.Builder.of(GravestoneBlockEntity::new, CommonModBlocks.TOMB).build(null)
        );
    }
}