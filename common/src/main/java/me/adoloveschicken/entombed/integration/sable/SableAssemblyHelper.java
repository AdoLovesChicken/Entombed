package me.adoloveschicken.entombed.integration.sable;

import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;

// Relieves other classes from having Sable imports
public class SableAssemblyHelper {

    public static void assembleBlocks(ServerLevel level, BlockPos pos) {
        Set<BlockPos> blocks = Set.of(pos);
        BoundingBox3i bounds = new BoundingBox3i(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );

        SubLevelAssemblyHelper.assembleBlocks(level, pos, blocks, bounds);
    }
}