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
    import me.adoloveschicken.entombed.integration.satchels.SatchelsBootstrap;
    import me.adoloveschicken.entombed.item.ModItems;
    import me.adoloveschicken.entombed.migration.GraveMigrator;
    import me.adoloveschicken.entombed.platform.EntombedPlatform;
    import me.adoloveschicken.entombed.platform.NeoForgePlatform;
    import me.adoloveschicken.entombed.platform.PlatformRegistry;
    import me.adoloveschicken.entombed.storage.GraveIndex;
    import me.adoloveschicken.entombed.storage.GraveStorageManager;
    import net.minecraft.world.level.storage.LevelResource;
    import net.neoforged.api.distmarker.Dist;
    import net.neoforged.bus.api.IEventBus;
    import net.neoforged.fml.ModContainer;
    import net.neoforged.fml.common.Mod;
    import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
    import net.neoforged.fml.loading.FMLEnvironment;
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

            // Initialise config, integrations
            PlatformRegistry.init(new NeoForgePlatform());
            EntombedPlatform platform = PlatformRegistry.get();
            Config.init(platform.getConfigDir());
            
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

            NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event ->
                    TombCommand.register(event.getDispatcher()));

            modEventBus.addListener((FMLCommonSetupEvent event) -> {
                Map<String, Supplier<TombIntegration>> integrations = Map.of(
                        "inventorio", InventorioHandler::new,
                        "backpacked", BackpackedHandler::new,
                        "curios", CuriosHandler::new,
                        "accessories", AccessoriesHandler::new,
                        "cosmeticarmorreworked", CARHandler::new
                );

                integrations.forEach((modId, handler) -> {
                    if (platform.isModLoaded(modId)) TombIntegrationRegistry.register(handler.get());
                });

                // Determines use of Satchels v1 integration; Satchels v2 comes with built-in integration
                if (platform.isModLoaded("satchels") && platform.getModVersion("satchels").startsWith("1"))
                    SatchelsBootstrap.register();

            });
        }
    }