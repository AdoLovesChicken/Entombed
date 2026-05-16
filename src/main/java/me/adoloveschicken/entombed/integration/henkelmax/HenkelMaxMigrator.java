package me.adoloveschicken.entombed.integration.henkelmax;

import dev.ryanhcode.sable.companion.SableCompanion;
import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.block.GravestoneBlock;
import me.adoloveschicken.entombed.block.GravestoneBlockEntity;
import me.adoloveschicken.entombed.block.ModBlocks;
import me.adoloveschicken.entombed.integration.sable.SableGravePositionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
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
        if (ModList.get().isLoaded("sable")) {
            gravePos = SableGravePositionHandler.getPositionFromWorld(level, gravePos);
        }
        level.setBlock(gravePos, ModBlocks.TOMB.get().defaultBlockState().setValue(GravestoneBlock.FACING, Direction.NORTH), 3);
        if (level.getBlockEntity(gravePos) instanceof GravestoneBlockEntity grave) {
            grave.setOwnerUUID(ownerUUID);
            grave.setOwnerName(ownerName);
            loadInventory(deathData.getList("MainInventory", Tag.TAG_COMPOUND), 0, grave, level);
            loadInventory(deathData.getList("ArmorInventory", Tag.TAG_COMPOUND), ARMOR_SLOT_OFFSET, grave, level);
            loadInventory(deathData.getList("OffHandInventory", Tag.TAG_COMPOUND), OFFHAND_SLOT, grave, level);
            if (ModList.get().isLoaded("curios")) {
                HenkelMaxCuriosMigrator.migrate(deathData.getList("Items", Tag.TAG_COMPOUND), grave, level);
            }

            grave.setChanged();
            try {
                Files.move(datFile, datFile.resolveSibling(datFile.getFileName() + ".migrated"));
            } catch (IOException e) {
                Entombed.LOGGER.error("Failed to populate tomb", e);
            }
        }
    }

    private static void loadInventory(ListTag inventory, int slotOffset, GravestoneBlockEntity grave, ServerLevel level) {
        for (int i = 0; i < inventory.size(); i++) {
            CompoundTag itemEntry = inventory.getCompound(i);
            int slotIndex = itemEntry.getInt("Slot") + slotOffset;
            int stackSize = itemEntry.getInt("count");
            String itemID = itemEntry.getString("id");
            ItemStack itemStack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemID)), stackSize);
            grave.setItemInSlot(slotIndex, itemStack);
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
