package me.adoloveschicken.entombed.config;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ConfigFabric {
    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
