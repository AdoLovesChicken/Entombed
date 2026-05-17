package me.adoloveschicken.entombed.event;

import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.block.GravestoneBlock;
import me.adoloveschicken.entombed.block.GravestoneBlockEntity;
import me.adoloveschicken.entombed.block.ModBlocks;
import me.adoloveschicken.entombed.integration.sable.SableAssemblyHelper;
import me.adoloveschicken.entombed.integration.sable.SableGravePositionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public class DeathHandler {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event){
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                return;
            }

            ServerLevel level = player.serverLevel();
            BlockPos pos = findSafePlacement(level, getGravePosition(player), player);
            Direction facing = ModList.get().isLoaded("sable") && SableGravePositionHandler.isOnSubLevel(player)
                    ? SableGravePositionHandler.getLocalFacing(player)
                    : player.getDirection().getOpposite();

            level.setBlock(pos, ModBlocks.TOMB.get().defaultBlockState().setValue(GravestoneBlock.FACING, facing), 3);
            if (level.getBlockEntity(pos) instanceof GravestoneBlockEntity gravestoneBlockEntity) {
                gravestoneBlockEntity.storeItems(player);
                level.playSound(null, pos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.5f, 0.8f);
                level.sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
            }

            // Tombs join the end sea in Sable!
            if (level.dimension() == ServerLevel.END && ModList.get().isLoaded("aeronautics")) {
                while (pos.getY() > 1 &&
                        (level.getBlockState(pos.below()).canBeReplaced() ||
                                !level.getBlockState(pos.below()).getFluidState().isEmpty())) {
                    pos = pos.below();
                }
                if (pos.getY() <= 1 ) {
                    SableAssemblyHelper.assembleBlocks(level, pos);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                return;
            }
            event.setCanceled(true);
        }
    }

    private static BlockPos getGravePosition(ServerPlayer player) {
        if (ModList.get().isLoaded("sable")) {
            return SableGravePositionHandler.getPosition(player);
        }
        return player.blockPosition();
    }

    public static void register() {
        NeoForge.EVENT_BUS.register(DeathHandler.class);
    }

    private static BlockPos findSafePlacement(ServerLevel level, BlockPos origin, ServerPlayer player) {
        BlockPos pos = origin;
        if (!ModList.get().isLoaded("sable")) {
            pos = processSafePlaceMent(level, origin);
        } else if (!SableGravePositionHandler.isOnSubLevel(player)) {
            pos = processSafePlaceMent(level, origin);
        }
        return pos;
    }

    private static BlockPos processSafePlaceMent(ServerLevel level, BlockPos origin){
        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMaxBuildHeight() - 1;

        BlockPos pos = origin;

        // Clamp to world bounds
        pos = new BlockPos(
                origin.getX(),
                Math.max(minY, Math.min(maxY, origin.getY())),
                origin.getZ()
        );

        // Scan up if pos is solid
        while (!level.getBlockState(pos).canBeReplaced() && pos.getY() < maxY) {
            pos = pos.above();
        }

        // Scan down if floating (pos.below is replaceable or fluid)
        while (pos.getY() > minY &&
                (level.getBlockState(pos.below()).canBeReplaced() ||
                        !level.getBlockState(pos.below()).getFluidState().isEmpty())) {
            pos = pos.below();
        }

        // Scan up if pos is in lava or fire
        while (pos.getY() < maxY && (!level.getBlockState(pos).getFluidState().isEmpty())) {
            pos = pos.above();
        }

        // Reachability check: at least one neighbour must be replaceable
        if (!isReachable(level, pos)) {
            for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                BlockPos candidate = pos;
                for (int i = 0; i < 10; i++) {
                    candidate = candidate.relative(dir);
                    if (level.getBlockState(candidate).canBeReplaced() && isReachable(level, candidate)) {
                        if (!level.getBlockState(candidate.below()).canBeReplaced()) {
                            return candidate;
                        }
                    }
                }
            }
            // fallback if nothing found
            Entombed.LOGGER.warn("Could not find reachable placement for tomb at {}, using clamped origin", origin);
            return pos;
        }
        return pos;
    }

    private static boolean isReachable(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).canBeReplaced()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWithinHeightLimit(ServerLevel level, BlockPos pos) {
        return pos.getY() >= level.getMinBuildHeight() && pos.getY() <= level.getMaxBuildHeight();
    }
}
