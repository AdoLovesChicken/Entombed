package me.adoloveschicken.entombed.integration.simulated;

import dev.simulated_team.simulated.content.end_sea.EndSeaPhysics;
import dev.simulated_team.simulated.content.end_sea.EndSeaPhysicsData;
import me.adoloveschicken.entombed.integration.sable.SableAssemblyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class EndSeaHandler {
    public static boolean hasEndSea(Level level) {
        return EndSeaPhysicsData.of(level) != null;
    }

    public static double getStartY(Level level) {
        if (hasEndSea(level)) {
            EndSeaPhysics endSeaPhysics = EndSeaPhysicsData.of(level);
            if (endSeaPhysics != null) { return endSeaPhysics.startY(); }
        }
        return 0; // Ultimately should be checked by hasEndSea...
    }
}
