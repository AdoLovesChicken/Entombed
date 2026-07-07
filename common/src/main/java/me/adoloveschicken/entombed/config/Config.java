package me.adoloveschicken.entombed.config;

import me.adoloveschicken.entombed.Entombed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Config {
    private static Path configDir;

    public static void load(Path dir) {
        configDir = dir;
        Path json = configDir.resolve("entombed.json");
        Path json5 = configDir.resolve("entombed.json5");
        if (Files.exists(json) && !Files.exists(json5)) {
            try {
                Files.move(json, json5);
            } catch (IOException e) {
                Entombed.LOGGER.warn("Could not migrate old config", e);
            }
        }
        ConfigIO.load(configDir.resolve(json5));
    }

    public static void save() {
        ConfigIO.save(configDir.resolve("entombed.json5"));
    }

}