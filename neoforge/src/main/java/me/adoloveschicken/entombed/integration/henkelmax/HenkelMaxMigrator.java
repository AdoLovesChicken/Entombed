package me.adoloveschicken.entombed.integration.henkelmax;

import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.block.GravestoneBlock;
import me.adoloveschicken.entombed.block.GravestoneBlockEntity;
import me.adoloveschicken.entombed.block.ModBlocks;
import me.adoloveschicken.entombed.integration.sable.SableGravePositionHandler;
import me.adoloveschicken.entombed.platform.PlatformRegistry;
import me.adoloveschicken.entombed.storage.GraveStorageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

public class HenkelMaxMigrator {
    private static final int ARMOR_SLOT_OFFSET = 36;
    private static final int OFFHAND_SLOT = 40;
    private static boolean hasMigrated = false;

    public static CompoundTag migrate(ListTag items, ServerLevel level) {
        ListTag curiosTagList = new ListTag();
        for (int i = 0; i < items.size(); i++) {
            CompoundTag itemEntry = items.getCompound(i);
            int stackSize = itemEntry.getInt("count");
            String itemID = itemEntry.getString("id");
            CompoundTag slotData = itemEntry.getCompound("components").getCompound("baguettelib:curio_slot_data");
            if (!slotData.isEmpty()) {
                CompoundTag curiosEntry = new CompoundTag();
                String slotType = slotData.getString("slotType");
                int slotIndex = slotData.getInt("slotIndex");
                ItemStack itemStack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemID)), stackSize);
                curiosEntry.putString("SlotType", slotType);
                curiosEntry.putInt("SlotIndex", slotIndex);
                curiosEntry.put("Item", itemStack.save(level.registryAccess()));
                curiosTagList.add(curiosEntry);
            }
        }
        CompoundTag extraTag = new CompoundTag();
        extraTag.put("CurioItems", curiosTagList);
        return extraTag;
    }

    public static void migrate(MinecraftServer server) {
        Path deathsFolder = server.getWorldPath(LevelResource.ROOT).resolve("deaths");
        if (Files.exists(deathsFolder)) {
            try (Stream<Path> stream = Files.walk(deathsFolder)) {
                stream.filter(p -> p.toString().endsWith(".dat"))
                        .forEach(p -> processDeath(p, server));
            } catch (IOException e) {
                Entombed.LOGGER.error("Failed to walk deaths folder", e);
            }
        }
    }

    private static void processDeath(Path datFile, MinecraftServer server) {
        try {
            CompoundTag deathData = NbtIo.read(datFile);
            String ownerName = deathData.getString("PlayerName");
            UUID ownerUUID = NbtUtils.loadUUID(deathData.get("PlayerUuid"));
            double posX = deathData.getDouble("PosX");
            double posY = deathData.getDouble("PosY");
            double posZ = deathData.getDouble("PosZ");
            String dimension = deathData.getString("Dimension");

            ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension)));
            if (level == null) {
                Entombed.LOGGER.warn("Skipping death file {}, dimension {} not found", datFile.getFileName(), dimension);
                return;
            }

            BlockPos gravePos = BlockPos.containing(posX, posY, posZ);
            BlockState blockAtPos = level.getBlockState(gravePos);

            if (!blockAtPos.is(ModBlocks.TOMB.get())) {
                populateGrave(gravePos, level, ownerUUID, ownerName, deathData, datFile);
            }

        } catch (IOException e) {
            Entombed.LOGGER.error("Failed to read death Data file", e);
        }
    }

    private static void populateGrave(BlockPos gravePos, ServerLevel level, UUID ownerUUID, String ownerName, CompoundTag deathData, Path datFile) {
        if (PlatformRegistry.get().isModLoaded("sable")) {
            gravePos = SableGravePositionHandler.getPositionFromWorld(level, gravePos);
        }
        level.setBlock(gravePos, ModBlocks.TOMB.get().defaultBlockState().setValue(GravestoneBlock.FACING, Direction.NORTH), 3);
        if (level.getBlockEntity(gravePos) instanceof GravestoneBlockEntity grave) {
            grave.setOwnerUUID(ownerUUID);
            grave.setOwnerName(ownerName);

            NonNullList<ItemStack> itemStacks = NonNullList.withSize(GravestoneBlockEntity.INVENTORY_SIZE, ItemStack.EMPTY);
            loadInventory(deathData.getList("MainInventory", Tag.TAG_COMPOUND), 0, itemStacks);
            loadInventory(deathData.getList("ArmorInventory", Tag.TAG_COMPOUND), ARMOR_SLOT_OFFSET, itemStacks);
            loadInventory(deathData.getList("OffHandInventory", Tag.TAG_COMPOUND), OFFHAND_SLOT, itemStacks);

            CompoundTag extraInventoriesTag = new CompoundTag();
            if (PlatformRegistry.get().isModLoaded("curios")) {
                extraInventoriesTag = migrate(deathData.getList("Items", Tag.TAG_COMPOUND), level);
            }

            UUID graveID = UUID.randomUUID();
            CompoundTag graveData = new CompoundTag();
            ContainerHelper.saveAllItems(graveData, itemStacks, level.registryAccess());
            graveData.put("ModExtras", extraInventoriesTag);
            GraveStorageManager.saveGrave(graveID, graveData);

            grave.setGraveID(graveID);
            grave.setChanged();
            try {
                Files.move(datFile, datFile.resolveSibling(datFile.getFileName() + ".migrated"));
            } catch (IOException e) {
                Entombed.LOGGER.error("Failed to populate tomb", e);
            }
        }
    }

    private static void loadInventory(ListTag inventory, int slotOffset, NonNullList<ItemStack> itemStacks) {
        for (int i = 0; i < inventory.size(); i++) {
            CompoundTag itemEntry = inventory.getCompound(i);
            int slotIndex = itemEntry.getInt("Slot") + slotOffset;
            int stackSize = itemEntry.getInt("count");
            String itemID = itemEntry.getString("id");
            ItemStack itemStack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemID)), stackSize);
            itemStacks.set(slotIndex, itemStack);
        }
    }

    public static void register() {
        NeoForge.EVENT_BUS.register(HenkelMaxMigrator.class);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!hasMigrated && event.getEntity() instanceof ServerPlayer player) {
            hasMigrated = true;
            migrate(player.server);
        }
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppingEvent event) {
        hasMigrated = false;
    }
}
