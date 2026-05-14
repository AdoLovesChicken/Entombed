package me.adoloveschicken.entombed.integration.henkelmax;

import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.block.GravestoneBlock;
import me.adoloveschicken.entombed.block.GravestoneBlockEntity;
import me.adoloveschicken.entombed.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

public class HenkelMaxMigrator {
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

            ListTag mainInventory = deathData.getList("MainInventory", Tag.TAG_COMPOUND);
            ListTag offHandInventory = deathData.getList("OffHandInventory", Tag.TAG_COMPOUND);
            ListTag armorInventory = deathData.getList("ArmorInventory", Tag.TAG_COMPOUND);

            ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension)));
            BlockPos gravePos = BlockPos.containing(posX, posY, posZ);
            BlockState blockAtPos = level.getBlockState(gravePos);

            if (!blockAtPos.is(ModBlocks.TOMB.get())) {
                level.setBlock(gravePos, ModBlocks.TOMB.get().defaultBlockState().setValue(GravestoneBlock.FACING, Direction.NORTH), 3);
                if (level.getBlockEntity(gravePos) instanceof GravestoneBlockEntity grave) {
                    grave.setOwnerUUID(ownerUUID);
                    grave.setOwnerName(ownerName);
                    loadInventory(mainInventory, 0, grave, level);
                    loadInventory(armorInventory, 36, grave, level);
                    loadInventory(offHandInventory, 40, grave, level);
                    if (ModList.get().isLoaded("curios")) {
                        HenkelMaxCuriosMigrator.migrate(deathData.getList("Items", Tag.TAG_COMPOUND), grave, level);
                    }

                    grave.setChanged();
                    Files.move(datFile, datFile.resolveSibling(datFile.getFileName() + ".migrated"));
                }
            }

        } catch (IOException e) {
            Entombed.LOGGER.error("Failed to read death Data file", e);
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
    public static void onServerStart (ServerStartingEvent event) {
        migrate(event.getServer());
    }
}
