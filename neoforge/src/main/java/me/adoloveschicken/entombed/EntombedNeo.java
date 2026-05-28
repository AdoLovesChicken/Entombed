package me.adoloveschicken.entombed;

import me.adoloveschicken.entombed.block.GravestoneBlockEntity;
import me.adoloveschicken.entombed.block.ModBlockEntities;
import me.adoloveschicken.entombed.block.ModBlocks;
import me.adoloveschicken.entombed.event.NeoDeathHandler;
import me.adoloveschicken.entombed.integration.accessory.AccessoriesHandler;
import me.adoloveschicken.entombed.integration.curios.CuriosHandler;
import me.adoloveschicken.entombed.integration.henkelmax.HenkelMaxMigrator;
import me.adoloveschicken.entombed.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Entombed.MODID)
public class EntombedNeo {
    public EntombedNeo(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        NeoDeathHandler.register();
        HenkelMaxMigrator.register();

        modEventBus.addListener((FMLCommonSetupEvent event) -> {
            GravestoneBlockEntity.setInventorioLoaded(ModList.get().isLoaded("inventorio"));

            if (ModList.get().isLoaded("curios")) {
                GravestoneBlockEntity.setGlobalAccessoryHandler(new CuriosHandler());
            } else if (ModList.get().isLoaded("accessories")) {
                GravestoneBlockEntity.setGlobalAccessoryHandler(new AccessoriesHandler());
            }
        });
    }
}