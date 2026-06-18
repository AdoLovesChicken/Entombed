package me.adoloveschicken.entombed.integration.trinkets;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import me.adoloveschicken.entombed.api.TombIntegration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;

public class TrinketsHandler implements TombIntegration {

    @Override
    public void saveData(Player player, CompoundTag tag) {
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
        if (component.isEmpty()) return;

        ListTag trinketsList = new ListTag();
        component.get().forEach((slotReference, itemStack) -> {
            if (!itemStack.isEmpty()) {
                CompoundTag itemEntry = new CompoundTag();
                itemEntry.putString("Group", slotReference.inventory().getSlotType().getGroup());
                itemEntry.putString("SlotType", slotReference.inventory().getSlotType().getName());
                itemEntry.putInt("SlotIndex", slotReference.index());
                itemEntry.put("Item", itemStack.save(player.level().registryAccess()));
                trinketsList.add(itemEntry);
                slotReference.inventory().setItem(slotReference.index(), ItemStack.EMPTY);
            }
        });
        tag.put("TrinketItems", trinketsList);
    }

    @Override
    public void retrieveData(Player player, CompoundTag tag) {
        if (!tag.contains("TrinketItems")) return;
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
        if (component.isEmpty()) return;

        ListTag trinketsList = tag.getList("TrinketItems", ListTag.TAG_COMPOUND);
        for (int i = 0; i < trinketsList.size(); i++) {
            CompoundTag itemEntry = trinketsList.getCompound(i);
            String group = itemEntry.getString("Group");
            String slotType = itemEntry.getString("SlotType");
            int slotIndex = itemEntry.getInt("SlotIndex");
            ItemStack item = ItemStack.parseOptional(player.level().registryAccess(), itemEntry.getCompound("Item"));

            Map<String, Map<String, dev.emi.trinkets.api.TrinketInventory>> inventory = component.get().getInventory();
            if (inventory.containsKey(group) && inventory.get(group).containsKey(slotType)) {
                inventory.get(group).get(slotType).setItem(slotIndex, item);
            }
        }
    }

    @Override
    public String integrationId() {
        return "trinkets";
    }
}