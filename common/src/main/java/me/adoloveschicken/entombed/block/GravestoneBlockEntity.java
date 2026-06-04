package me.adoloveschicken.entombed.block;

import me.adoloveschicken.entombed.Entombed;import me.adoloveschicken.entombed.integration.IAccessoryHandler;
import me.adoloveschicken.entombed.integration.backpacked.BackpackedHandler;import me.adoloveschicken.entombed.integration.inventorio.InventorioHandler;
import me.adoloveschicken.entombed.integration.soulbound.SoulboundHelper;
import me.adoloveschicken.entombed.storage.GraveIndex;
import me.adoloveschicken.entombed.storage.GraveStorageManager;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class GravestoneBlockEntity extends BlockEntity implements Clearable {

    public static final int INVENTORY_SIZE = 41;
    private UUID ownerUUID;
    private UUID graveID;
    private String ownerName;
    private CompoundTag fallbackGraveData = null;

    private static IAccessoryHandler globalAccessoryHandler = null;
    private static boolean inventorioLoaded = false;
    private static boolean backpackedLoaded = false;

    public static void setGlobalAccessoryHandler(IAccessoryHandler handler) { globalAccessoryHandler = handler; }
    public static void setInventorioLoaded(boolean isInventorioLoaded) { inventorioLoaded = isInventorioLoaded; }
    public static void setBackpackedLoaded(boolean isBackpackedLoaded) { backpackedLoaded = isBackpackedLoaded; }


    public GravestoneBlockEntity(BlockPos pos, BlockState blockState) {
        super(CommonModBlockEntities.TOMB_BLOCK_ENTITY, pos, blockState);
    }

    public void storeItems(Player player) {
        final NonNullList<ItemStack> itemStacks = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        CompoundTag extraInventoriesTag = new CompoundTag();

        RegistryAccess registryAccess = player.level().registryAccess();
        for (int i = 0; i < Math.min(player.getInventory().getContainerSize(), itemStacks.size()); i++) {

            ItemStack stack = player.getInventory().getItem(i);

            if (!SoulboundHelper.hasSoulboundEnchantment(stack, registryAccess)) {
                itemStacks.set(i, stack.copy());
            } else {
                player.drop(stack, true);
            }
        }
        if (globalAccessoryHandler != null) globalAccessoryHandler.storeCurios(player, extraInventoriesTag);
        if (inventorioLoaded) InventorioHandler.storeInventorio(player, extraInventoriesTag);
        if (backpackedLoaded) BackpackedHandler.storeBackpack(player, extraInventoriesTag);
        ownerUUID = player.getUUID();
        graveID = UUID.randomUUID();

        CompoundTag graveData = new CompoundTag();
        ContainerHelper.saveAllItems(graveData, itemStacks, registryAccess);
        graveData.put("ModExtras", extraInventoriesTag);
        if (!GraveStorageManager.saveGrave(graveID, graveData)) {
            Entombed.LOGGER.warn("Grave storage failed, falling back to block entity NBT");
            fallbackGraveData = graveData;
        }
    }

    public void returnItems(Player player) {
        final NonNullList<ItemStack> itemStacks = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        RegistryAccess registryAccess = player.level().registryAccess();

        CompoundTag graveData = GraveStorageManager.loadGrave(graveID);
        if (graveData == null) {
            graveData = fallbackGraveData;
        }

        if (graveData == null) {
            Entombed.LOGGER.error("Could not load grave data for id {}, items lost!", graveID);
            GraveStorageManager.deleteGrave(graveID);
            GraveIndex.removeGrave(player.getUUID(), graveID);
            if (level != null) level.removeBlock(getBlockPos(), false);
            return;
        }

        ContainerHelper.loadAllItems(graveData, itemStacks, registryAccess);
        CompoundTag extraInventoriesTag = graveData.getCompound("ModExtras");

        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack itemStack = itemStacks.get(i);
            if (!itemStack.isEmpty()) {
                restoreItem(player, i, itemStack.copy());
            }
        }
        if (globalAccessoryHandler != null) globalAccessoryHandler.returnCurios(player, extraInventoriesTag);
        if (inventorioLoaded) InventorioHandler.returnInventorio(player, extraInventoriesTag);
        if (backpackedLoaded) BackpackedHandler.returnBackpack(player, extraInventoriesTag);

        GraveStorageManager.deleteGrave(graveID);
        GraveIndex.removeGrave(player.getUUID(), graveID);
        itemStacks.clear();
        setChanged();
        if (level != null) {
            playEffects();
            level.removeBlock(getBlockPos(), false);
        }
    }

    public static void restoreItem(Player player, int slot, ItemStack stack) {
        if (player.getInventory().getItem(slot).isEmpty()) {
            if (hasVanishingCurse(stack, player.level().registryAccess())) {
                return;
            }

            if (hasBindingCurse(stack, player.level().registryAccess())) {
                player.drop(stack, false);
                return;
            }

            player.getInventory().setItem(slot, stack);

        } else if (!player.addItem(stack)) {
                player.drop(stack, false);
        }
    }

    public static boolean hasVanishingCurse(ItemStack stack, RegistryAccess registryAccess) {
        return EnchantmentHelper.getItemEnchantmentLevel(
                registryAccess.lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.VANISHING_CURSE), stack
        ) > 0;
    }

    public static boolean hasBindingCurse(ItemStack stack, RegistryAccess registryAccess) {
        return EnchantmentHelper.getItemEnchantmentLevel(
                registryAccess.lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.BINDING_CURSE), stack
        ) > 0;
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

    public UUID getGraveID() {
        return graveID;
    }

    public void setGraveID(UUID graveID) {
        this.graveID = graveID;
    }

    public static IAccessoryHandler getGlobalAccessoryHandler() {
        return globalAccessoryHandler;
    }

    public static boolean isInventorioLoaded() {
        return inventorioLoaded;
    }

    public static boolean isBackpackedLoaded() { return backpackedLoaded; }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (!tag.hasUUID("GraveID") && tag.contains("Items")) {
            if (GraveStorageManager.isInitialised()) {
                graveID = UUID.randomUUID();

                CompoundTag legacyData = new CompoundTag();
                legacyData.put("Items", tag.getList("Items", 10));

                if (tag.contains("ModExtras")) {
                    legacyData.put("ModExtras", tag.getCompound("ModExtras"));
                }

                GraveStorageManager.saveGrave(graveID, legacyData);

                tag.remove("Items");
                tag.remove("ModExtras");
                setChanged();
            } else {
                Entombed.LOGGER.warn("Legacy grave found at {} but storage not ready - skipping migration", getBlockPos());
            }
        }

        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
        if (tag.contains("OwnerName")) {
            ownerName = tag.getString("OwnerName");
        }
        if (tag.hasUUID("GraveID")) {
            graveID = tag.getUUID("GraveID");
        }
        if (tag.contains("FallbackData")) {
            fallbackGraveData = tag.getCompound("FallbackData");
        }
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
        if (graveID != null) {
            tag.putUUID("GraveID", graveID);
        }
        if (fallbackGraveData != null) {
            tag.put("FallbackData", fallbackGraveData);
        }
    }

    @Override
    public void clearContent() {
        GraveStorageManager.deleteGrave(graveID);
    }
}
