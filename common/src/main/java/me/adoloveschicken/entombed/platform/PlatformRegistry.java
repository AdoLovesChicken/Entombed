package me.adoloveschicken.entombed.platform;

public final class PlatformRegistry {
    private static EntombedPlatform INSTANCE;

    private PlatformRegistry() {}

    public static void init(EntombedPlatform platform) {
        if (INSTANCE != null) throw new IllegalStateException("Platform already initialized");
        INSTANCE = platform;
    }

    public static EntombedPlatform get() {
        if (INSTANCE == null) throw new IllegalStateException("Platform not initialized");
        return INSTANCE;
    }
}
