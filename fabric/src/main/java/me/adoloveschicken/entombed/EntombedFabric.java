package me.adoloveschicken.entombed;

import me.adoloveschicken.entombed.block.GravestoneBlockEntity;
import me.adoloveschicken.entombed.block.ModBlockEntities;
import me.adoloveschicken.entombed.block.ModBlocks;
import me.adoloveschicken.entombed.event.FabricDeathHandler;
import me.adoloveschicken.entombed.integration.trinkets.TrinketsHandler;
import me.adoloveschicken.entombed.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

public class EntombedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        FabricDeathHandler.register();
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            GravestoneBlockEntity.setGlobalAccessoryHandler(new TrinketsHandler());
        }
    }
}