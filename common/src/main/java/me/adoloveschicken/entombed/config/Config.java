package me.adoloveschicken.entombed.config;

import java.nio.file.Path;

public class Config {
    private static Path configDir;

    public static void load(Path dir) {
        configDir = dir;
        ConfigIO.load(configDir.resolve("entombed.json"));
    }

    public static void save() {
        ConfigIO.save(configDir.resolve("entombed.json"));
    }

}