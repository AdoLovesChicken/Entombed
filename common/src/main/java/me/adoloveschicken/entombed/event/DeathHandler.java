package me.adoloveschicken.entombed.event;

import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.block.CommonModBlocks;
import me.adoloveschicken.entombed.block.GravestoneBlock;
import me.adoloveschicken.entombed.block.GravestoneBlockEntity;
import me.adoloveschicken.entombed.integration.sable.SableGravePositionHandler;
import me.adoloveschicken.entombed.integration.simulated.EndSeaHandler;
import me.adoloveschicken.entombed.storage.GraveEntry;
import me.adoloveschicken.entombed.storage.GraveIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameRules;

public class DeathHandler {
    private static int minY;
    private static int maxY;

    public static void onPlayerDeath(ServerPlayer player, boolean sableLoaded, boolean aeronauticsLoaded) {
        if (keepInvEnabled(player)) return;

        ServerLevel level = player.serverLevel();
        minY = level.getMinBuildHeight();
        maxY = level.getMaxBuildHeight() - 1;

        BlockPos deathPos = sableLoaded
                ? SableGravePositionHandler.getPosition(player)
                : player.blockPosition();
        Direction deathFacing = sableLoaded && SableGravePositionHandler.isOnSubLevel(player)
                ? SableGravePositionHandler.getLocalFacing(player)
                : player.getDirection().getOpposite();

        BlockPos pos = sableLoaded
                ? deathPos
                : getSafePlacement(level, deathPos);

        level.setBlock(pos, CommonModBlocks.TOMB.defaultBlockState().setValue(GravestoneBlock.FACING, deathFacing), 3);
        if (level.getBlockEntity(pos) instanceof GravestoneBlockEntity gravestoneBlockEntity) {
            gravestoneBlockEntity.storeItems(player);
            GraveEntry entry = new GraveEntry(
                    gravestoneBlockEntity.getGraveID(),
                    level.dimension().location().toString(),
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    System.currentTimeMillis()
            );
            GraveIndex.addGrave(player.getUUID(), entry);
            level.playSound(null, pos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.5f, 0.8f);
            level.sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
        }

        if (aeronauticsLoaded) EndSeaHandler.assembleEndSea(level, pos);
    }

    private static BlockPos getSafePlacement(ServerLevel level, BlockPos origin) {
        BlockPos pos = new BlockPos(
                origin.getX(),
                Math.max(minY, Math.min(maxY, origin.getY())), // Clamp to world confines
                origin.getZ()
        );

        pos = getNearestAir(pos, level, Direction.UP);
        pos = getNearestAir(pos, level, Direction.DOWN);

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

    private static BlockPos getNearestAir(BlockPos pos, ServerLevel level, Direction direction) {
        while (!level.getBlockState(pos).isAir()
                && pos.getY() < maxY
                && pos.getY() > minY) {
            pos = pos.relative(direction);
        }
        return pos;
    }

    private static boolean isReachable(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).canBeReplaced()) return true;
        }
        return false;
    }

    public static boolean onPlayerDrops(ServerPlayer player) {
        return !keepInvEnabled(player);
    }

    private static boolean keepInvEnabled(ServerPlayer player) {
        return player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
    }


}