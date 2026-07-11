package me.adoloveschicken.entombed.integration.accessory;

import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import me.adoloveschicken.entombed.api.TombIntegration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class AccessoriesHandler implements TombIntegration {

    @Override
    public void saveData(Player player, CompoundTag tag) {
        AccessoriesCapability capability = AccessoriesCapability.get(player);
        if (capability == null) return;

        ListTag accessoriesList = new ListTag();

        for (Map.Entry<String, AccessoriesContainer> entry : capability.getContainers().entrySet()) {
            String slotName = entry.getKey();
            AccessoriesContainer container = entry.getValue();

            for (int i = 0; i < container.getSize(); i++) {
                ItemStack item = container.getAccessories().getItem(i);
                if (!item.isEmpty()) {
                    CompoundTag itemEntry = new CompoundTag();
                    itemEntry.putString("SlotName", slotName);
                    itemEntry.putInt("SlotIndex", i);
                    itemEntry.put("Item", item.save(player.level().registryAccess()));
                    accessoriesList.add(itemEntry);
                    container.getAccessories().setItem(i, ItemStack.EMPTY);
                }
            }
        }

        tag.put("AccessoryItems", accessoriesList);
    }

    @Override
    public void retrieveData(Player player, CompoundTag tag) {
        if (!tag.contains("AccessoryItems")) return;

        AccessoriesCapability capability = AccessoriesCapability.get(player);
        if (capability == null) return;

        ListTag accessoriesList = tag.getList("AccessoryItems", ListTag.TAG_COMPOUND);

        for (int i = 0; i < accessoriesList.size(); i++) {
            CompoundTag itemEntry = accessoriesList.getCompound(i);
            String slotName = itemEntry.getString("SlotName");
            int slotIndex = itemEntry.getInt("SlotIndex");
            ItemStack item = ItemStack.parseOptional(
                    player.level().registryAccess(),
                    itemEntry.getCompound("Item")
            );

            AccessoriesContainer container = capability.getContainers().get(slotName);
            if (container != null && slotIndex < container.getSize()) {
                if (container.getAccessories().getItem(slotIndex).isEmpty()) container.getAccessories().setItem(slotIndex, item);
                else if (!player.getInventory().add(item)) player.drop(item, false);
            }
        }
    }

    @Override
    public String integrationId() {
        return "";
    }
}