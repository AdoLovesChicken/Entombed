package me.adoloveschicken.entombed.storage;

import me.adoloveschicken.entombed.Entombed;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class GraveStorageManager {
    private static Path graveDir = null;

    public static void initialise(Path worldDir) {
        graveDir = worldDir.resolve("entombed").resolve("tombs");
        try {
            Files.createDirectories(graveDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create grave directory", e);
        }
    }

    public static void saveGrave(UUID graveID, CompoundTag inventoryNbt) {
        if (graveDir == null) return;

        Path graveFile = graveDir.resolve(graveID.toString() + ".dat");
        try {
            NbtIo.writeCompressed(inventoryNbt, graveFile);
        } catch (IOException e) {
            Entombed.LOGGER.error("Failed to save grave {} - player items may be lost!", graveID, e);
        }
    }

    public static CompoundTag loadGrave(UUID graveID) {
        if (graveDir == null) {
            Entombed.LOGGER.error("loadGrave called but graveDir is null!");
            return null;
        }
        Path graveFile = graveDir.resolve(graveID.toString() + ".dat");
        if (!Files.exists(graveFile)) {
            Entombed.LOGGER.error("Grave file missing for UUID {}", graveID);
            return null;
        }
        try {
            return NbtIo.readCompressed(graveFile, NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            Entombed.LOGGER.error("Failed to read grave file for UUID {}", graveID, e);
            return null;
        }
    }

    public static void deleteGrave(UUID graveID) {
        if (graveDir == null) return;

        Path graveFile = graveDir.resolve(graveID.toString() + ".dat");
        try {
            Files.deleteIfExists(graveFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
