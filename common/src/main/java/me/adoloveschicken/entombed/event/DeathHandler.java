package me.adoloveschicken.entombed.event;

import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.block.CommonModBlocks;
import me.adoloveschicken.entombed.block.GravestoneBlock;
import me.adoloveschicken.entombed.block.GravestoneBlockEntity;
import me.adoloveschicken.entombed.config.ConfigData;
import me.adoloveschicken.entombed.integration.sable.SableGravePositionHandler;
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
import net.minecraft.world.level.Level;import net.minecraft.world.level.block.state.BlockState;import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class DeathHandler {
    private static int minY;
    private static int maxY;

    public static BlockPos onPlayerDeath(ServerPlayer player, boolean sableLoaded) {
        if (keepInvEnabled(player)) return null;

        ServerLevel level = player.serverLevel();
        minY = level.getMinBuildHeight();
        maxY = level.getMaxBuildHeight() - 1;

        boolean onSubLevel = sableLoaded && SableGravePositionHandler.isOnSubLevel(player);

        BlockPos pos = onSubLevel
                ? SableGravePositionHandler.getSafePositionOnSubLevel(level, SableGravePositionHandler.getPosition(player))
                : getSafePlacement(level, player.blockPosition());

        Direction deathFacing = onSubLevel
                ? SableGravePositionHandler.getLocalFacing(player)
                : player.getDirection().getOpposite();

        level.setBlock(pos, CommonModBlocks.TOMB.defaultBlockState().setValue(GravestoneBlock.FACING, deathFacing), 3);
        if (level.getBlockEntity(pos) instanceof GravestoneBlockEntity gravestoneBlockEntity) {
            gravestoneBlockEntity.storeAll(player);
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
        return pos;
    }

    private static BlockPos getSafePlacement(ServerLevel level, BlockPos origin) {
        BlockPos pos = new BlockPos(
                origin.getX(),
                Math.clamp(origin.getY(), minY, maxY),
                origin.getZ()
        );

        if (isAirAboveSolid(pos, level)) {
            return pos;
        }

        if (level.dimension() != Level.END){
            if (isReplaceable(level.getBlockState(pos)) && isReplaceable(level.getBlockState(pos.below()))) {
                BlockPos downResult = findAirAboveSolid(pos, level, Direction.DOWN);
                if (downResult != null) {
                    pos = downResult;
                } else {
                    pos = findAirAboveSolid(pos, level, Direction.UP);
                }
            } else {
                pos = findAirAboveSolid(pos, level, Direction.UP);
            }
        } else {
            BlockPos downResult = findAirAboveSolid(pos, level, Direction.DOWN);
            pos = Objects.requireNonNullElseGet(downResult, () -> new BlockPos(origin.getX(), minY, origin.getZ()));
        }

        if (pos == null || !isAirAboveSolid(pos, level)) {
            pos = new BlockPos(origin.getX(), minY, origin.getZ());
            Entombed.LOGGER.warn("Could not find suitable placement for tomb at {}, using y={}", origin, minY);
        }

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

    private static boolean isReplaceable(BlockState state) {
        return state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    private static boolean isAirAboveSolid(BlockPos pos, ServerLevel level) {
        return isReplaceable(level.getBlockState(pos)) && !isReplaceable(level.getBlockState(pos.below()));
    }

    private static BlockPos findAirAboveSolid(BlockPos pos, ServerLevel level, Direction direction) {
        while (pos.getY() >= minY && pos.getY() <= maxY) {
            if (isAirAboveSolid(pos, level)) return pos;
            pos = pos.relative(direction);
        }
        return null;
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