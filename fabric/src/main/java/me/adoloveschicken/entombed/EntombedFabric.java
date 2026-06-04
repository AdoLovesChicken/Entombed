package me.adoloveschicken.entombed;

import com.mojang.brigadier.CommandDispatcher;
import me.adoloveschicken.entombed.block.GravestoneBlockEntity;
import me.adoloveschicken.entombed.block.ModBlockEntities;
import me.adoloveschicken.entombed.block.ModBlocks;
import me.adoloveschicken.entombed.command.TombCommand;
import me.adoloveschicken.entombed.event.FabricDeathHandler;
import me.adoloveschicken.entombed.integration.accessory.AccessoriesHandler;
import me.adoloveschicken.entombed.integration.trinkets.TrinketsHandler;
import me.adoloveschicken.entombed.item.ModItems;
import me.adoloveschicken.entombed.storage.GraveIndex;
import me.adoloveschicken.entombed.storage.GraveStorageManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

public class EntombedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        FabricDeathHandler.register();

        GravestoneBlockEntity.setInventorioLoaded(FabricLoader.getInstance().isModLoaded("inventorio"));
        GravestoneBlockEntity.setBackpackedLoaded(FabricLoader.getInstance().isModLoaded("backpacked"));

        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            GravestoneBlockEntity.setGlobalAccessoryHandler(new TrinketsHandler());
        } else if (FabricLoader.getInstance().isModLoaded("accessories")) {
            GravestoneBlockEntity.setGlobalAccessoryHandler(new AccessoriesHandler());
        }

        ServerLifecycleEvents.SERVER_STARTING.register(server ->
        {
            Path root = server.getWorldPath(LevelResource.ROOT);
            GraveStorageManager.initialise(root);
            GraveIndex.initialise(root);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            GraveStorageManager.reset();
            GraveIndex.reset();
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TombCommand.register(dispatcher);
        });
    }
}