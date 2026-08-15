package me.adoloveschicken.entombed.config;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.config.ConfigData.DropBehavior;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Config {
    private static Path configDir;

    private static Path getTomlPath() { return configDir.resolve("entombed.toml"); }
    private static Path getJsonPath() { return configDir.resolve("entombed.json"); }

    public static void init(Path dir) {
        configDir = dir;
        final Path JSON = getJsonPath();
        final Path TOML = getTomlPath();

        // Migrate json -> toml
        if (Files.exists(JSON) && !Files.exists(TOML)) {
            try {
                Files.move(JSON, TOML);
            } catch (IOException e) {
                Entombed.LOGGER.warn("Could not migrate old config", e);
            }
        }
        if (!Files.exists(TOML)) { save(); return; }
        load();
    }

    public static void save() {
        final Path TOML = getTomlPath();
        Map<String, Object> map = new LinkedHashMap<>();
        for (Field field : ConfigData.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                String name = field.getName();
                Class<?> type = field.getType();
                Object value = field.get(null);
                if (type == boolean.class) map.put(name, value);
                else if (type == short.class) map.put(name, value);
                else if (type == DropBehavior.class) map.put(name, value.toString());
                else if (List.class.isAssignableFrom(type)) map.put(name, value);
            } catch (Exception e) {
                Entombed.LOGGER.error("Could not get field {}", field.getName(), e);
            }
        }
        try {
            TomlWriter writer = new TomlWriter();
            Files.writeString(TOML, writer.write(map));
        } catch (IOException e) {
            Entombed.LOGGER.error("Could not save config", e);
        }
    }

    public static void load() {
        final Path TOML = getTomlPath();
        try {
            Toml file = new Toml().read(TOML.toFile());
            for (Field field : ConfigData.class.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    String name = field.getName();
                    if (file.contains(name)) {
                        Class<?> type = field.getType();
                        if (type == boolean.class)
                            field.setBoolean(null, file.getBoolean(name));
                        else if (type == short.class)
                            field.setShort(null, file.getLong(name).shortValue());
                        else if (type == DropBehavior.class)
                            field.set(null, Enum.valueOf(DropBehavior.class, file.getString(name)));
                        else if (List.class.isAssignableFrom(type)) {
                            List<Object> rawList = file.getList(name);
                            if (rawList != null) {
                                field.set(null, new ArrayList<>(rawList));
                            }
                        }
                            field.set(null, file.getList(name));
                    }
                } catch (Exception e) {
                    Entombed.LOGGER.error("Could not set field {}", field.getName(), e);
                }
            }
            ConfigData.setItemsOnDeath(ConfigData.itemsOnDeath);
            ConfigData.setLiquidProperties(ConfigData.tombsFloatInLiquid);
        } catch (Exception e) {
            Entombed.LOGGER.error("Could not load config, resetting", e);
            save();
        }
    }
}