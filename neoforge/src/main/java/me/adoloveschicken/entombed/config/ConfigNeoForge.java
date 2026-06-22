package me.adoloveschicken.entombed.config;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ConfigNeoForge {
    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
