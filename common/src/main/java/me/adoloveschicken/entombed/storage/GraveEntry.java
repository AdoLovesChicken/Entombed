package me.adoloveschicken.entombed.storage;

import java.util.UUID;

public class GraveEntry {
    private UUID graveID;
    private String dimension;
    private int x;
    private int y;
    private int z;
    private long timestamp;

    public GraveEntry(UUID graveID, String dimension, int x, int y, int z, long timestamp) {
        this.graveID = graveID;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
    }

    public UUID getGraveID() {
        return graveID;
    }

    public String getDimension() {
        return dimension;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
