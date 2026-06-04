    package me.adoloveschicken.entombed;

    import com.mojang.brigadier.CommandDispatcher;
    import me.adoloveschicken.entombed.block.GravestoneBlockEntity;
    import me.adoloveschicken.entombed.block.ModBlockEntities;
    import me.adoloveschicken.entombed.block.ModBlocks;
    import me.adoloveschicken.entombed.command.TombCommand;
    import me.adoloveschicken.entombed.event.NeoDeathHandler;
    import me.adoloveschicken.entombed.integration.accessory.AccessoriesHandler;
    import me.adoloveschicken.entombed.integration.curios.CuriosHandler;
    import me.adoloveschicken.entombed.integration.henkelmax.HenkelMaxMigrator;
    import me.adoloveschicken.entombed.item.ModItems;
    import me.adoloveschicken.entombed.storage.GraveIndex;
    import me.adoloveschicken.entombed.storage.GraveStorageManager;
    import net.minecraft.world.level.storage.LevelResource;
    import net.neoforged.bus.api.IEventBus;
    import net.neoforged.fml.ModContainer;
    import net.neoforged.fml.ModList;
    import net.neoforged.fml.common.Mod;
    import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
    import net.neoforged.neoforge.common.NeoForge;
    import net.neoforged.neoforge.event.RegisterCommandsEvent;
    import net.neoforged.neoforge.event.server.ServerStartingEvent;

    import java.nio.file.Path;

    @Mod(Entombed.MODID)
    public class EntombedNeo {
        public EntombedNeo(IEventBus modEventBus, ModContainer modContainer) {
            ModBlocks.register(modEventBus);
            ModItems.register(modEventBus);
            ModBlockEntities.register(modEventBus);
            NeoDeathHandler.register();
            HenkelMaxMigrator.register();

            NeoForge.EVENT_BUS.addListener(ServerStartingEvent.class, event -> {
                Path root = event.getServer().getWorldPath(LevelResource.ROOT);
                GraveStorageManager.initialise(root);
                GraveIndex.initialise(root);
            });

            NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> {
                TombCommand.register(event.getDispatcher());
            });

            modEventBus.addListener((FMLCommonSetupEvent event) -> {
                GravestoneBlockEntity.setInventorioLoaded(ModList.get().isLoaded("inventorio"));
                GravestoneBlockEntity.setBackpackedLoaded(ModList.get().isLoaded("backpacked"));

                if (ModList.get().isLoaded("curios")) {
                    GravestoneBlockEntity.setGlobalAccessoryHandler(new CuriosHandler());
                } else if (ModList.get().isLoaded("accessories")) {
                    GravestoneBlockEntity.setGlobalAccessoryHandler(new AccessoriesHandler());
                }
            });
        }
    }