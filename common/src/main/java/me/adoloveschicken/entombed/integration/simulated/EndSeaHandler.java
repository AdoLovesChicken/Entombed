package me.adoloveschicken.entombed.integration.simulated;

import dev.simulated_team.simulated.content.end_sea.EndSeaPhysicsData;
import net.minecraft.world.level.Level;

public class EndSeaHandler {
    public static boolean hasEndSea(Level level) {
        return EndSeaPhysicsData.of(level) != null;
    }
}
