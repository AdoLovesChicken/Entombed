package me.adoloveschicken.entombed.migration;

import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.storage.GraveStorageManager;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class GraveMigrator {

    /* ---NOTE--- */
    // If you are looking for the code for HenkelMax migration
    // (and the curios addon), please check the neoforge package
    // under entombed/integration/henkelmax
    //
    // If you are looking for the code for the migration from
    // glitched tombs with Prism Launcher, check common package
    // under entombed/storage/GraveStorageManager
    /* --------- */


    /* Migration for pre-2.0 tombs to work with new GraveEntry */
    private record PendingMigration(CompoundTag legacyData, Consumer<UUID> onComplete) {}

    private static final List<PendingMigration> pendingMigrations = new ArrayList<>();

    public static void migrate(CompoundTag legacyData, Consumer<UUID> onComplete) {
        if (GraveStorageManager.isInitialised()) {
            runMigration(legacyData, onComplete);
        } else {
            Entombed.LOGGER.warn("Storage not ready, deferring legacy grave migration");
            pendingMigrations.add(new PendingMigration(legacyData, onComplete));
        }
    }

    public static void drainQueue() {
        if (pendingMigrations.isEmpty()) return;
        Entombed.LOGGER.info("Draining {} pending migrations", pendingMigrations.size());
        for (PendingMigration pendingMigration : pendingMigrations) {
            runMigration(pendingMigration.legacyData, pendingMigration.onComplete);
        }
        pendingMigrations.clear();
    }

    private static void runMigration(CompoundTag legacyData, Consumer<UUID> onComplete) {
        UUID graveID = UUID.randomUUID();
        if (GraveStorageManager.saveGrave(graveID, legacyData)) {
            Entombed.LOGGER.info("Migrated legacy grave to storage with ID {}", graveID);
            onComplete.accept(graveID);
        } else {
            Entombed.LOGGER.warn("Failed to store legacy grave to storage, data may be lost");
        }
    }

    public static void reset() {
        pendingMigrations.clear();
    }
}
