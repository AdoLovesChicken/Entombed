package me.adoloveschicken.entombed.platform;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FabricPlatform implements EntombedPlatform {
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
