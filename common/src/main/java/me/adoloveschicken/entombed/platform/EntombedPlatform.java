package me.adoloveschicken.entombed.platform;

import java.nio.file.Path;

public interface EntombedPlatform {
    Path getConfigDir();
    boolean isModLoaded(String modId);
    String getModVersion(String modId);
}
