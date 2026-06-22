package me.adoloveschicken.entombed;

import me.adoloveschicken.entombed.api.TombIntegration;
import me.adoloveschicken.entombed.api.TombIntegrationRegistry;
import me.adoloveschicken.entombed.block.ModBlockEntities;
import me.adoloveschicken.entombed.block.ModBlocks;
import me.adoloveschicken.entombed.command.TombCommand;
import me.adoloveschicken.entombed.config.Config;
import me.adoloveschicken.entombed.event.FabricDeathHandler;
import me.adoloveschicken.entombed.integration.accessory.AccessoriesHandler;
import me.adoloveschicken.entombed.integration.backpacked.BackpackedHandler;
import me.adoloveschicken.entombed.integration.inventorio.InventorioHandler;
import me.adoloveschicken.entombed.integration.trinkets.TrinketsHandler;
import me.adoloveschicken.entombed.item.ModItems;
import me.adoloveschicken.entombed.migration.GraveMigrator;
import me.adoloveschicken.entombed.storage.GraveIndex;
import me.adoloveschicken.entombed.storage.GraveStorageManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

public class EntombedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        FabricDeathHandler.register();

        Map<String, Supplier<TombIntegration>> integrations = Map.of(
                "inventorio", InventorioHandler::new,
                "backpacked", BackpackedHandler::new,
                "trinkets", TrinketsHandler::new,
                "accessories", AccessoriesHandler::new
        );

        Config.load(FabricLoader.getInstance().getConfigDir());

        integrations.forEach((modId, handler) -> {
            if (FabricLoader.getInstance().isModLoaded(modId)) TombIntegrationRegistry.register(handler.get());
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server ->
        {
            Path root = server.getWorldPath(LevelResource.ROOT).normalize();
            GraveStorageManager.initialise(root);
            GraveIndex.initialise(root);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            GraveStorageManager.reset();
            GraveIndex.reset();
            GraveMigrator.reset();
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TombCommand.register(dispatcher);
        });
    }
}