package me.adoloveschicken.entombed.item;

import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.block.CommonModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModItems {
    public static Item TOMB;

    public static void register() {
        TOMB = Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(Entombed.MODID, "tomb"),
                new BlockItem(CommonModBlocks.TOMB, new Item.Properties())
        );
    }
}