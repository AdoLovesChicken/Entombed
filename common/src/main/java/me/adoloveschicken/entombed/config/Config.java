package me.adoloveschicken.entombed.config;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;

public class Config {
    public static void load() {
        ConfigIO.load(getConfigDir().resolve("entombed.json"));
    }
    public static void save() {
        ConfigIO.save(getConfigDir().resolve("entombed.json"));
    }

    @ExpectPlatform
    public static Path getConfigDir() {
        throw new AssertionError();
    }

}