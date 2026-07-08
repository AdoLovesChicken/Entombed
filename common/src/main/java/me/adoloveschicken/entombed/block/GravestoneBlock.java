package me.adoloveschicken.entombed.block;

import com.mojang.serialization.MapCodec;
import me.adoloveschicken.entombed.config.ConfigData;
import me.adoloveschicken.entombed.storage.GraveIndex;
import me.adoloveschicken.entombed.storage.GraveStorageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class GravestoneBlock extends BaseEntityBlock {

    private static final VoxelShape NORTH_SOUTH_SHAPE = Block.box(1, 0, 5, 15, 14, 11);
    private static final VoxelShape EAST_WEST_SHAPE = Block.box(5, 0, 1, 11, 14, 15);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public GravestoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(GravestoneBlock::new);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        useWithoutItem(state, level, pos, player, hitResult);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof GravestoneBlockEntity grave) {
            if (grave.getOwnerUUID() == null || grave.getGraveID() == null) {
                level.removeBlock(pos, false);
                return InteractionResult.CONSUME;
            }
            if (player.getUUID().equals(grave.getOwnerUUID())) {
                grave.restoreAll(player);
                return InteractionResult.CONSUME;
            } else if (!ConfigData.requireOpForRetrieve || player instanceof ServerPlayer serverPlayer && serverPlayer.hasPermissions(2)) {
                grave.restoreAll(player);
                player.displayClientMessage(Component.translatable("entombed.stolen_tomb")
                        .withColor(0xFF746C), true);
                return InteractionResult.CONSUME;
            } else {
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0.3, 0.3, 0.3, 0.05);
                }
                level.playSound(null, player.blockPosition(), SoundEvents.ANCIENT_DEBRIS_STEP, SoundSource.BLOCKS, 1.0f, 1.0f);
                player.displayClientMessage(Component.translatable("entombed.owner_mismatch")
                        .withColor(0xFF746C), true);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.CONSUME;
    }

    // Returns items if grave is destroyed directly by player
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof GravestoneBlockEntity grave) {
            if (grave.getOwnerUUID() != null && grave.getGraveID() != null) {
                if (ConfigData.tombsCanBeBrokenDirectly) {
                    grave.restoreAll(player);
                    return super.playerWillDestroy(level, pos, state, player);
                }
                return state;
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public float getExplosionResistance() {
        return ConfigData.tombsCanBeBrokenIndirectly ? 10F : Float.MAX_VALUE;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GravestoneBlockEntity grave && !grave.beingRetrieved) {
                if (!ConfigData.tombsCanBeBrokenIndirectly) {
                    level.setBlock(pos, state, 3);
                    return;
                }
                // Drop items
                CompoundTag graveData = GraveStorageManager.loadGrave(grave.getGraveID());
                if (graveData == null) graveData = grave.fallbackGraveData;
                if (graveData != null && level instanceof ServerLevel) {
                    NonNullList<ItemStack> itemStacks = NonNullList.withSize(GravestoneBlockEntity.INVENTORY_SIZE, ItemStack.EMPTY);
                    ContainerHelper.loadAllItems(graveData, itemStacks, level.registryAccess());
                    for (ItemStack stack : itemStacks) {
                        if (!stack.isEmpty()) {
                            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                        }
                    }
                    GraveStorageManager.markRetrieved(grave.getGraveID());
                    GraveIndex.removeGrave(grave.getOwnerUUID(), grave.getGraveID());
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {}

    @Override
    protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer) {}

    // Corrects block directions
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? NORTH_SOUTH_SHAPE : EAST_WEST_SHAPE;
    }

    // Corrects block hitbox
    @Override
    protected boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return false;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GravestoneBlockEntity(pos, state);
    }

    // Provides custom block hitbox
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!ConfigData.tombsHaveCollision) return Shapes.empty();
        return getShape(state, level, pos, context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }


}
