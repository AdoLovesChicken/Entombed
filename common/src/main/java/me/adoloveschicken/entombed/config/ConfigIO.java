package me.adoloveschicken.entombed.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.SyntaxError;
import me.adoloveschicken.entombed.config.ConfigData.DropBehavior;
import me.adoloveschicken.entombed.Entombed;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigIO {
    private static final Jankson JANKSON = Jankson.builder().build();

    public static void save(Path file) {
        JsonObject jsonObject = new JsonObject();
        for (Field field : ConfigData.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                String name = field.getName();
                if (field.getType().equals(boolean.class))
                    jsonObject.put(name, JsonPrimitive.of(field.getBoolean(null)));
                else if (field.getType().equals(short.class))
                    jsonObject.put(name, JsonPrimitive.of((long) field.getShort(null)));
                else if (field.getType().equals(DropBehavior.class))
                    jsonObject.put(name, JsonPrimitive.of(field.get(null).toString()));
            } catch (Exception e) {
                Entombed.LOGGER.error("Could not get field {}", field.getName(), e);
            }
        }
        try {
            Files.writeString(file, jsonObject.toJson(true, true));
        } catch (IOException e) {
            Entombed.LOGGER.error("Could not save config", e);
        }
    }

    public static void load(Path file) {
        if (!Files.exists(file)) {
            save(file);
            return;
        }
        try {
            String json = Files.readString(file);
            JsonObject jsonObject = JANKSON.load(json);
            for (Field field : ConfigData.class.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    String name = field.getName();
                    if (jsonObject.containsKey(name)) {
                        JsonElement elem = jsonObject.get(name);
                        if (field.getType().equals(boolean.class)) {
                            boolean defaultValue = field.getBoolean(null);
                            field.setBoolean(null, ((JsonPrimitive) elem).asBoolean(defaultValue));
                        } else if (field.getType().equals(short.class)) {
                            field.setShort(null, (short) ((JsonPrimitive) elem).asInt(0));
                        } else if (field.getType().equals(DropBehavior.class)) {
                            field.set(null, Enum.valueOf(DropBehavior.class, ((JsonPrimitive) elem).asString()));
                        }
                    }
                } catch (Exception e) {
                    Entombed.LOGGER.error("Could not set field {}", field.getName(), e);
                }
            }
            ConfigData.setItemsOnDeath(ConfigData.itemsOnDeath);
            ConfigData.setLiquidProperties(ConfigData.tombsFloatInLiquid);
        } catch (IOException | SyntaxError e) {
            Entombed.LOGGER.error("Could not load config", e);
        }
    }
}