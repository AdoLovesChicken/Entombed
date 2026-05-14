package me.adoloveschicken.entombed.item;

import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Entombed.MODID);

    public static  final DeferredItem<Item> TOMB = ITEMS.register("tomb",
            () -> new BlockItem(ModBlocks.TOMB.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
