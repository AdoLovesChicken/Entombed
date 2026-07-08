package me.adoloveschicken.entombed.block;

import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.api.TombIntegration;
import me.adoloveschicken.entombed.api.TombIntegrationRegistry;
import me.adoloveschicken.entombed.config.ConfigData;
import me.adoloveschicken.entombed.integration.galosphere.GalosphereHelper;
import me.adoloveschicken.entombed.integration.soulbound.SoulboundHelper;
import me.adoloveschicken.entombed.migration.GraveMigrator;
import me.adoloveschicken.entombed.storage.GraveIndex;
import me.adoloveschicken.entombed.storage.GraveStorageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class GravestoneBlockEntity extends BlockEntity implements Clearable {

    public static final int INVENTORY_SIZE = 41;
    private UUID ownerUUID;
    private UUID graveID;
    private String ownerName;
    public boolean beingRetrieved;
    public CompoundTag fallbackGraveData = null;

    public GravestoneBlockEntity(BlockPos pos, BlockState blockState) {
        super(CommonModBlockEntities.TOMB_BLOCK_ENTITY, pos, blockState);
    }

    public void storeAll(Player player) {
        ownerUUID = player.getUUID();
        graveID = UUID.randomUUID();

        CompoundTag graveData = new CompoundTag();

        storeItems(player, graveData);
        storeExperience(player, graveData);

        CompoundTag integrationsTag = new CompoundTag();
        for (TombIntegration integration : TombIntegrationRegistry.getIntegrations()) {
            integration.saveData(player, integrationsTag);
        }
        graveData.put("ModExtras", integrationsTag);

        if (!GraveStorageManager.saveGrave(graveID, graveData)) {
            Entombed.LOGGER.warn("Grave storage failed, falling back to block entity NBT");
            fallbackGraveData = graveData;
        }
    }

    public void restoreAll(Player player) {
        beingRetrieved = true;
        CompoundTag loadedGraveData = GraveStorageManager.loadGrave(graveID);
        CompoundTag graveData = loadedGraveData == null
                ? fallbackGraveData
                : loadedGraveData;

        if (graveData == null) {
            Entombed.LOGGER.error("Could not load grave data for id {}, items lost!", graveID);
            removeGraveBlock(player, false);
            return;
        }

        restoreItems(player, graveData);
        restoreExperience(player, graveData);

        CompoundTag integrationsTag = graveData.getCompound("ModExtras");
        for (TombIntegration integration : TombIntegrationRegistry.getIntegrations()) {
            integration.retrieveData(player, integrationsTag);
        }

        setChanged();
        removeGraveBlock(player, true);
        beingRetrieved = false;
    }

    public void storeItems(Player player, CompoundTag graveData) {
        final NonNullList<ItemStack> itemStacks = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

        RegistryAccess registryAccess = player.level().registryAccess();
        for (int i = 0; i < Math.min(player.getInventory().getContainerSize(), itemStacks.size()); i++) {

            ItemStack stack = player.getInventory().getItem(i);

            if (!SoulboundHelper.hasSoulboundEnchantment(stack, registryAccess)
                    && !GalosphereHelper.hasPreservedComponent(stack)) {
                itemStacks.set(i, stack.copy());
            } else {
                player.drop(stack, true);
            }
        }

        ContainerHelper.saveAllItems(graveData, itemStacks, registryAccess);
    }

    public void restoreItems(Player player, CompoundTag graveData) {
        final NonNullList<ItemStack> itemStacks = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        RegistryAccess registryAccess = player.level().registryAccess();

        ContainerHelper.loadAllItems(graveData, itemStacks, registryAccess);

        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack itemStack = itemStacks.get(i);
            if (!itemStack.isEmpty()) restoreItem(player, i, itemStack.copy());
        }
        itemStacks.clear();
    }

    public static void storeExperience(Player player, CompoundTag graveData) {
        graveData.putInt("ExpPoints", decideExperience(player));
    }

    public static int decideExperience(Player player) {
//        ConfigData.DropBehavior experienceDropBehavior = ConfigData.experienceOnDeath;
//        if (experienceDropBehavior == ConfigData.DropBehavior.DEFAULT) {
//            experienceDropBehavior = ConfigData.itemsOnDeath;
//        }
//        return switch (experienceDropBehavior) {
//            case TOMBED -> player.totalExperience;
//            case PARTIAL -> player.getXpNeededForNextLevel();
//            case PERCENT_KEPT -> Math.round(player.totalExperience * ConfigData.experiencePercentKept / 100.0f);
//            default -> 0;
//        };
        if (player.level() instanceof ServerLevel serverLevel) {
            return player.getExperienceReward(serverLevel, null);
        }
        return 0;
    }

    public static void restoreExperience(Player player, CompoundTag graveData) {
        player.giveExperiencePoints(graveData.getInt("ExpPoints"));
    }


    public static void restoreItem(Player player, int slot, ItemStack stack) {
        if (player.getInventory().getItem(slot).isEmpty()) {
            RegistryAccess registryAccess = player.level().registryAccess();

            if (hasVanishingCurse(stack, registryAccess)) return;
            if (hasBindingCurse(stack, registryAccess)) {
                player.drop(stack, false);
                return;
            }
            player.getInventory().setItem(slot, stack);

        } else if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
    }

    public static boolean hasVanishingCurse(ItemStack stack, RegistryAccess registryAccess) {
        return hasEnchantment(stack, registryAccess, Enchantments.VANISHING_CURSE);
    }

    public static boolean hasBindingCurse(ItemStack stack, RegistryAccess registryAccess) {
        return hasEnchantment(stack, registryAccess, Enchantments.BINDING_CURSE);
    }

    public static boolean hasEnchantment(ItemStack stack, RegistryAccess registryAccess, ResourceKey<Enchantment> enchantment) {
        return EnchantmentHelper.getItemEnchantmentLevel(
                registryAccess.lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(enchantment), stack
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

    public void removeGraveBlock(Player player, boolean useEffects) {
        GraveStorageManager.markRetrieved(graveID);
        GraveIndex.removeGrave(player.getUUID(), graveID);
        if (level != null) {
            if (useEffects) playEffects();
            level.removeBlock(getBlockPos(), false);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (!tag.hasUUID("GraveID") && tag.contains("Items")) {
            CompoundTag legacyData = new CompoundTag();
            legacyData.put("Items", tag.getList("Items", 10));
            if (tag.contains("ModExtras")) {
                legacyData.put("ModExtras", tag.getCompound("ModExtras"));
            }
            tag.remove("Items");
            tag.remove("ModExtras");

            GraveMigrator.migrate(legacyData, newGraveID -> {
                this.graveID = newGraveID;
                setChanged();
            });
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
        GraveStorageManager.markRetrieved(graveID);
    }
}
