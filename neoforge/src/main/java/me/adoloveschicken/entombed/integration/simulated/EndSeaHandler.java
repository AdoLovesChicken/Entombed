package me.adoloveschicken.entombed.integration.simulated;

import dev.simulated_team.simulated.content.end_sea.EndSeaPhysicsData;
import me.adoloveschicken.entombed.integration.sable.SableAssemblyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class EndSeaHandler {
    public static boolean hasEndSea(Level level) {
        return EndSeaPhysicsData.of(level) != null;
    }

    public static void assembleEndSea(ServerLevel level, BlockPos pos) {
        if (!hasEndSea(level)) return;

        BlockPos floorPos = new BlockPos(pos.getX(), level.getMinBuildHeight() + 1, pos.getZ());
        SableAssemblyHelper.assembleBlocks(level, floorPos);
    }
}
