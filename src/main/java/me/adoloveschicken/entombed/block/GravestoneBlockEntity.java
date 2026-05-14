package me.adoloveschicken.entombed.block;

import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.integration.curios.CuriosHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class GravestoneBlockEntity extends BlockEntity {

    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(41, ItemStack.EMPTY);
    private UUID ownerUUID;
    private CompoundTag curiosTag = new CompoundTag();

    private static final CuriosHandler CURIOS_HANDLER = new CuriosHandler();


    public GravestoneBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.TOMB_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void storeItems(Player player){
        CURIOS_HANDLER.storeCurios(player, curiosTag);
        for (int i = 0; i < Math.min(player.getInventory().getContainerSize(), itemStacks.size()); i++) {
            itemStacks.set(i, player.getInventory().getItem(i).copy());
        }
        ownerUUID = player.getUUID();
    }

    public void returnItems(Player player) {
        BlockPos pos = getBlockPos();
        CURIOS_HANDLER.returnCurios(player, curiosTag);
        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack itemStack = itemStacks.get(i);
            if (!itemStack.isEmpty()) {
                if (player.getInventory().getItem(i).isEmpty()) {
                    player.getInventory().setItem(i, itemStack.copy());
                } else {
                    if (!player.addItem(itemStack.copy())) {
                        player.drop(itemStack.copy(), false);
                    }
                }
            }
        }
        itemStacks.clear();
        setChanged();
        if (level != null) {
            level.playSound(null, getBlockPos(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
            level.playSound(null, getBlockPos(), SoundEvents.GILDED_BLACKSTONE_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
            }
            level.removeBlock(getBlockPos(), false);
        }
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
        if (tag.contains("CurioItems")) {
            curiosTag = tag.getCompound("CurioItems");
        }
        ContainerHelper.loadAllItems(tag, itemStacks, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        tag.put("CurioItems", curiosTag);
        ContainerHelper.saveAllItems(tag, itemStacks, registries);
    }



}
