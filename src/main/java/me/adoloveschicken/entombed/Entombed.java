package me.adoloveschicken.entombed;

import com.mojang.logging.LogUtils;
import me.adoloveschicken.entombed.block.ModBlockEntities;
import me.adoloveschicken.entombed.block.ModBlocks;
import me.adoloveschicken.entombed.event.DeathHandler;
import me.adoloveschicken.entombed.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Entombed.MODID)
public class Entombed {
    public static final String MODID = "entombed";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Entombed(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        DeathHandler.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }


}
