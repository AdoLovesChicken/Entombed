package me.adoloveschicken.entombed.storage;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GraveIndex {
    private static Map<UUID, List<GraveEntry>> playerGraves = new HashMap<>();
    private static Path indexPath;

    public static void initialise(Path worldDir) {
        indexPath = worldDir.resolve("entombed").resolve("grave_index.json");
        try {
            Files.createDirectories(indexPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Could not create grave index directory", e);
        }
    }

    public static void save() {
        if (indexPath == null) return;
        try {
            Gson gson = new Gson();
            String json = gson.toJson(playerGraves);
            Files.writeString(indexPath, json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save grave index", e);
        }
    }

    public static void load() {
        try {
            if (Files.exists(indexPath)) {
                String json = Files.readString(indexPath);
                Type type = new TypeToken<Map<UUID, List<GraveEntry>>>(){}.getType();
                playerGraves = new Gson().fromJson(json, type);
            } else {
                playerGraves = new HashMap<>();
            }
        } catch (IOException e) {
            playerGraves = new HashMap<>();
        }
    }

    public static void addGrave(UUID playerUUID, GraveEntry entry) {
        List<GraveEntry> playerList = playerGraves.computeIfAbsent(playerUUID, k -> new ArrayList<>());
        playerList.add(entry);
        save();
    }

    public static void removeGrave(UUID playerUUID, UUID graveID) {
        List<GraveEntry> playerList = playerGraves.get(playerUUID);
        if (playerList != null) {
            playerList.removeIf(entry -> entry.getGraveID().equals(graveID));
            if (playerList.isEmpty()) {
                playerGraves.remove(playerUUID);
            }
            save();
        }
    }

    public static List<GraveEntry> getGraves(UUID playerUUID) {
        List<GraveEntry> graveEntryList = playerGraves.get(playerUUID);
        if (graveEntryList == null) return new ArrayList<>();
        return graveEntryList;
    }

    public static GraveEntry getLastGrave(UUID playerUUID) {
        List<GraveEntry> graveEntryList = playerGraves.get(playerUUID);
        if (graveEntryList == null || graveEntryList.isEmpty()) return null;
        int lastGraveIndex = 0;
        for (int i = 0; i < graveEntryList.size(); i++) {
            if (graveEntryList.get(i).getTimestamp() > graveEntryList.get(lastGraveIndex).getTimestamp()) {
                lastGraveIndex = i;
            }
        }
        return graveEntryList.get(lastGraveIndex);
    }


}
