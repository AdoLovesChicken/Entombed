package me.adoloveschicken.entombed.platform;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class NeoForgePlatform implements EntombedPlatform {
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public String getModVersion(String modId) {
        ModContainer container = ModList.get().getModContainerById(modId).orElse(null);
        return container != null
                ? container.getModInfo().getVersion().toString()
                : null;
    }
}
