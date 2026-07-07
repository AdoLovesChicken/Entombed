    package me.adoloveschicken.entombed;
    
    import me.adoloveschicken.entombed.api.TombIntegration;
    import me.adoloveschicken.entombed.api.TombIntegrationRegistry;
    import me.adoloveschicken.entombed.block.ModBlockEntities;
    import me.adoloveschicken.entombed.block.ModBlocks;
    import me.adoloveschicken.entombed.command.TombCommand;
    import me.adoloveschicken.entombed.config.Config;
    import me.adoloveschicken.entombed.config.screen.YaclConfigBuilder;
    import me.adoloveschicken.entombed.event.NeoDeathHandler;
    import me.adoloveschicken.entombed.integration.accessory.AccessoriesHandler;
    import me.adoloveschicken.entombed.integration.backpacked.BackpackedHandler;
    import me.adoloveschicken.entombed.integration.cos_armor_rework.CARHandler;
    import me.adoloveschicken.entombed.integration.curios.CuriosHandler;
    import me.adoloveschicken.entombed.integration.henkelmax.HenkelMaxMigrator;
    import me.adoloveschicken.entombed.integration.inventorio.InventorioHandler;
    import me.adoloveschicken.entombed.integration.satchels.SatchelsHandler;
    import me.adoloveschicken.entombed.item.ModItems;
    import me.adoloveschicken.entombed.migration.GraveMigrator;
    import me.adoloveschicken.entombed.storage.GraveIndex;
    import me.adoloveschicken.entombed.storage.GraveStorageManager;
    import net.minecraft.world.level.storage.LevelResource;
    import net.neoforged.api.distmarker.Dist;
    import net.neoforged.bus.api.IEventBus;
    import net.neoforged.fml.ModContainer;
    import net.neoforged.fml.ModList;
    import net.neoforged.fml.common.Mod;
    import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
    import net.neoforged.fml.loading.FMLEnvironment;
    import net.neoforged.fml.loading.FMLPaths;
    import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
    import net.neoforged.neoforge.common.NeoForge;
    import net.neoforged.neoforge.event.RegisterCommandsEvent;
    import net.neoforged.neoforge.event.server.ServerStartingEvent;
    import net.neoforged.neoforge.event.server.ServerStoppingEvent;
    
    import java.nio.file.Path;
    import java.util.Map;
    import java.util.function.Supplier;

    @Mod(Entombed.MODID)
    public class EntombedNeo {
        public EntombedNeo(IEventBus modEventBus, ModContainer modContainer) {
            ModBlocks.register(modEventBus);
            ModItems.register(modEventBus);
            ModBlockEntities.register(modEventBus);
            NeoDeathHandler.register();
            HenkelMaxMigrator.register();

            Config.load(FMLPaths.CONFIGDIR.get());
            
            if (FMLEnvironment.dist == Dist.CLIENT) {
                modContainer.registerExtensionPoint(
                        IConfigScreenFactory.class,
                        (minecraft, parent) -> YaclConfigBuilder.createScreen(parent)
                );
            }
            
            NeoForge.EVENT_BUS.addListener(ServerStartingEvent.class, event -> {
                Path root = event.getServer().getWorldPath(LevelResource.ROOT).normalize();
                GraveStorageManager.initialise(root);
                GraveIndex.initialise(root);
            });

            NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class, event -> {
                GraveStorageManager.reset();
                GraveIndex.reset();
                GraveMigrator.reset();
            });

            NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> {
                TombCommand.register(event.getDispatcher());
            });

            modEventBus.addListener((FMLCommonSetupEvent event) -> {
                Map<String, Supplier<TombIntegration>> integrations = Map.of(
                        "inventorio", InventorioHandler::new,
                        "backpacked", BackpackedHandler::new,
                        "satchels", SatchelsHandler::new,
                        "curios", CuriosHandler::new,
                        "accessories", AccessoriesHandler::new,
                        "cosmeticarmorreworked", CARHandler::new
                );

                integrations.forEach((modId, handler) -> {
                    if (ModList.get().isLoaded(modId)) TombIntegrationRegistry.register(handler.get());
                });
            });
        }
    }