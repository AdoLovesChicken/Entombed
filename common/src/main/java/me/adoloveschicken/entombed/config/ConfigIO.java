package me.adoloveschicken.entombed.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.adoloveschicken.entombed.Entombed;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigIO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save(Path file) {
        JsonObject jsonObject = new JsonObject();
        for (Field field : ConfigData.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                if (field.getType().equals(boolean.class)) // Booleans
                    jsonObject.addProperty(field.getName(), field.getBoolean(null));
                else if (field.getType().equals(short.class)) // Shorts (percents)
                    jsonObject.addProperty(field.getName(), field.getShort(null));
                else if (field.getType().equals(ConfigData.DropBehavior.class)) // DropBehaviors
                    jsonObject.addProperty(field.getName(), field.get(null).toString());
                else Entombed.LOGGER.warn("{} is not of known type", field.getName());
            } catch (Exception e) {
                Entombed.LOGGER.error("Could not get field {} of {}", field.getName(), ConfigData.class.getName(), e);
            }
        }
        try {
            Files.writeString(file, GSON.toJson(jsonObject));
        } catch (IOException e) {
            Entombed.LOGGER.error("Could not save config file {}", file, e);
        }
    }

    public static void load(Path file) {
        if (!Files.exists(file)) {
            Entombed.LOGGER.info("Config file {} does not exist", file);
            save(file);
            return;
        }
        try {
            String json = Files.readString(file);
            JsonObject jsonObject = GSON.fromJson(json, JsonObject.class);
            for (Field field : ConfigData.class.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    JsonElement jsonElement = jsonObject.get(field.getName());
                    if (jsonObject.has(field.getName())) {
                        if (field.getType().equals(boolean.class))
                            field.setBoolean(null, jsonElement.getAsBoolean());
                        else if (field.getType().equals(short.class))
                            field.setShort(null, jsonElement.getAsShort());
                        else if (field.getType().equals(ConfigData.DropBehavior.class))
                            field.set(null, Enum.valueOf(ConfigData.DropBehavior.class, jsonElement.getAsString()));
                        else Entombed.LOGGER.warn("{} is not of known type", field.getName());
                    }
                } catch (Exception e) {
                    Entombed.LOGGER.error("Could not set field {} of {}", field.getName(), ConfigData.class.getName(), e);
                }
            }
        } catch (IOException e) {
            Entombed.LOGGER.error("Could not load config file {}", file, e);
        }

    }
}