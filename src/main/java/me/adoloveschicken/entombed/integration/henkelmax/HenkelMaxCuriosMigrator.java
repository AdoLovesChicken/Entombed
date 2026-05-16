package me.adoloveschicken.entombed.integration.henkelmax;

import me.adoloveschicken.entombed.block.GravestoneBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public class HenkelMaxCuriosMigrator {
    public static void migrate(ListTag items, GravestoneBlockEntity grave, ServerLevel level) {
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
        grave.getCuriosTag().put("CurioItems", curiosTagList);
    }
}
