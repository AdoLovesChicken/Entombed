package me.adoloveschicken.entombed.platform;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Path;
import java.util.Optional;

public class FabricPlatform implements EntombedPlatform {
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public String getModVersion(String modId) {
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modId);
        return modContainer.map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse(null);
    }
}
