package me.adoloveschicken.entombed.block;

import me.adoloveschicken.entombed.integration.accessory.IAccessoryHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class GravestoneBlockEntity extends BlockEntity implements Clearable {

    public static final int INVENTORY_SIZE = 41;
    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private UUID ownerUUID;
    private String ownerName;
    private CompoundTag curiosTag = new CompoundTag();

    private static IAccessoryHandler globalAccessoryHandler = null;

    public static void setGlobalAccessoryHandler(IAccessoryHandler handler) {
        globalAccessoryHandler = handler;
    }


    public GravestoneBlockEntity(BlockPos pos, BlockState blockState) {
        super(CommonModBlockEntities.TOMB_BLOCK_ENTITY, pos, blockState);
    }

    public void storeItems(Player player) {
        if (globalAccessoryHandler != null) globalAccessoryHandler.storeCurios(player, curiosTag);
        for (int i = 0; i < Math.min(player.getInventory().getContainerSize(), itemStacks.size()); i++) {
            itemStacks.set(i, player.getInventory().getItem(i).copy());
        }
        ownerUUID = player.getUUID();
    }

    public void returnItems(Player player) {
        if (globalAccessoryHandler != null) globalAccessoryHandler.returnCurios(player, curiosTag);
        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack itemStack = itemStacks.get(i);
            if (!itemStack.isEmpty()) {
                restoreItem(player, i, itemStack.copy());
            }
        }
        itemStacks.clear();
        setChanged();
        if (level != null) {
            playEffects();
            level.removeBlock(getBlockPos(), false);
        }
    }

    private void restoreItem(Player player, int slot, ItemStack stack) {
        if (player.getInventory().getItem(slot).isEmpty()) {
            player.getInventory().setItem(slot, stack);
        } else if (!player.addItem(stack)) {
                player.drop(stack, false);
        }
    }

    private void playEffects() {
        if (level != null) {
            level.playSound(null, getBlockPos(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
            level.playSound(null, getBlockPos(), SoundEvents.GILDED_BLACKSTONE_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SOUL, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID newOwnerUUID) {
        ownerUUID = newOwnerUUID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String newOwnerName) {
        ownerName = newOwnerName;
    }

    public void setItemInSlot(int slotIndex, ItemStack itemStack) {
        itemStacks.set(slotIndex, itemStack);
    }

    public CompoundTag getCuriosTag() {
        return curiosTag;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
        if (tag.contains("OwnerName")) {
            ownerName = tag.getString("OwnerName");
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
        if (ownerName != null) {
            tag.putString("OwnerName", ownerName);
        }
        tag.put("CurioItems", curiosTag);
        ContainerHelper.saveAllItems(tag, itemStacks, registries);
    }

    @Override
    public void clearContent() {
        itemStacks.clear();
        curiosTag = new CompoundTag();
    }
}
