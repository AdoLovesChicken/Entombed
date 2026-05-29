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
        if (EndSeaHandler.hasEndSea(level)) {
            while (pos.getY() > 1 &&
                    (level.getBlockState(pos.below()).canBeReplaced() ||
                            !level.getBlockState(pos.below()).getFluidState().isEmpty())) {
                pos = pos.below();
            }
            if (pos.getY() <= 1) {
                SableAssemblyHelper.assembleBlocks(level, pos);
            }
        }

    }
}
