package me.adoloveschicken.entombed.event;

import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.block.GravestoneBlock;
import me.adoloveschicken.entombed.block.GravestoneBlockEntity;
import me.adoloveschicken.entombed.block.CommonModBlocks;
import me.adoloveschicken.entombed.integration.sable.SableAssemblyHelper;
import me.adoloveschicken.entombed.integration.sable.SableGravePositionHandler;
import me.adoloveschicken.entombed.integration.simulated.EndSeaHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameRules;

public class DeathHandler {

    public static void onPlayerDeath(ServerPlayer player, boolean sableLoaded, boolean aeronauticsLoaded) {
        if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;

        ServerLevel level = player.serverLevel();
        BlockPos pos = findSafePlacement(level, getGravePosition(player, sableLoaded), player, sableLoaded);
        Direction facing = sableLoaded && SableGravePositionHandler.isOnSubLevel(player)
                ? SableGravePositionHandler.getLocalFacing(player)
                : player.getDirection().getOpposite();

        level.setBlock(pos, CommonModBlocks.TOMB.defaultBlockState().setValue(GravestoneBlock.FACING, facing), 3);
        if (level.getBlockEntity(pos) instanceof GravestoneBlockEntity gravestoneBlockEntity) {
            gravestoneBlockEntity.storeItems(player);
            level.playSound(null, pos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.5f, 0.8f);
            level.sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
        }

        if (aeronauticsLoaded && EndSeaHandler.hasEndSea(level)) {
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

    public static boolean onPlayerDrops(ServerPlayer player) {
        return !player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
    }

    private static BlockPos getGravePosition(ServerPlayer player, boolean sableLoaded) {
        if (sableLoaded) return SableGravePositionHandler.getPosition(player);
        return player.blockPosition();
    }

    private static BlockPos findSafePlacement(ServerLevel level, BlockPos origin, ServerPlayer player, boolean sableLoaded) {
        if (!sableLoaded) return processSafePlacement(level, origin);
        if (!SableGravePositionHandler.isOnSubLevel(player)) return processSafePlacement(level, origin);
        return origin;
    }

    private static BlockPos processSafePlacement(ServerLevel level, BlockPos origin) {
        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMaxBuildHeight() - 1;

        BlockPos pos = new BlockPos(
                origin.getX(),
                Math.max(minY, Math.min(maxY, origin.getY())),
                origin.getZ()
        );

        while (!level.getBlockState(pos).canBeReplaced() && pos.getY() < maxY) pos = pos.above();

        while (pos.getY() > minY &&
                (level.getBlockState(pos.below()).canBeReplaced() ||
                        !level.getBlockState(pos.below()).getFluidState().isEmpty())) {
            pos = pos.below();
        }

        while (pos.getY() < maxY && !level.getBlockState(pos).getFluidState().isEmpty()) pos = pos.above();

        if (!isReachable(level, pos)) {
            for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                BlockPos candidate = pos;
                for (int i = 0; i < 10; i++) {
                    candidate = candidate.relative(dir);
                    if (level.getBlockState(candidate).canBeReplaced() && isReachable(level, candidate)) {
                        if (!level.getBlockState(candidate.below()).canBeReplaced()) return candidate;
                    }
                }
            }
            Entombed.LOGGER.warn("Could not find reachable placement for tomb at {}, using clamped origin", origin);
            return pos;
        }
        return pos;
    }

    private static boolean isReachable(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).canBeReplaced()) return true;
        }
        return false;
    }
}