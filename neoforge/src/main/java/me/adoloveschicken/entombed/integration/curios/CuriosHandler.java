package me.adoloveschicken.entombed.integration.curios;

import me.adoloveschicken.entombed.api.TombIntegration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CuriosHandler implements TombIntegration {

    @Override
    public void saveData(Player player, CompoundTag tag) {
        Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(player);
        if (curiosInventory.isPresent()) {
            ICuriosItemHandler handler = curiosInventory.get();
            Map<String, ICurioStacksHandler> curiosMap = handler.getCurios();

            ListTag curiosTagList = new ListTag();
            ListTag cosmeticTagList = new ListTag();

            for (Map.Entry<String, ICurioStacksHandler> entry : curiosMap.entrySet()) {
                String slotType = entry.getKey();
                ICurioStacksHandler stacksHandler = entry.getValue();

                // Functional
                IItemHandler stacks = stacksHandler.getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack item = stacks.getStackInSlot(i);
                    if (!item.isEmpty()) {
                        CompoundTag itemEntry = new CompoundTag();
                        itemEntry.putString("SlotType", slotType);
                        itemEntry.putInt("SlotIndex", i);
                        itemEntry.put("Item", item.save(player.level().registryAccess()));
                        curiosTagList.add(itemEntry);
                        stacksHandler.getStacks().setStackInSlot(i, ItemStack.EMPTY);
                    }
                }

                // Cosmetics
                if (stacksHandler.hasCosmetic()) {
                    IItemHandler cosmeticStacks = stacksHandler.getCosmeticStacks();
                    for (int i = 0; i < cosmeticStacks.getSlots(); i++) {
                        ItemStack item = cosmeticStacks.getStackInSlot(i);
                        if (!item.isEmpty()) {
                            CompoundTag itemEntry = new CompoundTag();
                            itemEntry.putString("SlotType", slotType);
                            itemEntry.putInt("SlotIndex", i);
                            itemEntry.put("Item", item.save(player.level().registryAccess()));
                            cosmeticTagList.add(itemEntry);
                            stacksHandler.getCosmeticStacks().setStackInSlot(i, ItemStack.EMPTY);
                        }
                    }
                }
            }

            tag.put("CurioItems", curiosTagList);
            if (!cosmeticTagList.isEmpty()) {
                tag.put("CurioCosmeticItems", cosmeticTagList);
            }
        }
    }

    @Override
    public void retrieveData(Player player, CompoundTag tag) {
        Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(player);
        if (curiosInventory.isPresent()) {
            ICuriosItemHandler handler = curiosInventory.get();
            if (tag.contains("CurioItems")) {
                restoreCurioItems(player, handler, tag.getList("CurioItems", ListTag.TAG_COMPOUND), false);
            }
            if (tag.contains("CurioCosmeticItems")) {
                restoreCurioItems(player, handler, tag.getList("CurioCosmeticItems", ListTag.TAG_COMPOUND), true);
            }
        }
    }

    private static void restoreCurioItems(Player player, ICuriosItemHandler handler, ListTag tagList, boolean cosmetic) {
        List<ItemStack> failedItems = new ArrayList<>();

        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemEntry = tagList.getCompound(i);
            String slotType = itemEntry.getString("SlotType");
            int slotIndex = itemEntry.getInt("SlotIndex");
            ItemStack item = ItemStack.parseOptional(player.level().registryAccess(), itemEntry.getCompound("Item"));
            ICurioStacksHandler stacksHandler = handler.getCurios().get(slotType);

            if (stacksHandler != null) {
                IItemHandler stacks = cosmetic && stacksHandler.hasCosmetic() ? stacksHandler.getCosmeticStacks() : stacksHandler.getStacks();
                if (slotIndex >= 0 && slotIndex < stacks.getSlots() && stacks.getStackInSlot(slotIndex).isEmpty()) {
                    if (stacks instanceof IItemHandlerModifiable modifiable) {
                        modifiable.setStackInSlot(slotIndex, item);
                    }
                    continue;
                }
            }
            failedItems.add(item);
        }

        for (ItemStack item : new ArrayList<>(failedItems)) {
            for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                IItemHandler stacks = cosmetic && stacksHandler.hasCosmetic() ? stacksHandler.getCosmeticStacks() : stacksHandler.getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    if (stacks.getStackInSlot(i).isEmpty()) {
                        if (stacks instanceof IItemHandlerModifiable modifiable) {
                            modifiable.setStackInSlot(i, item);
                        }
                        failedItems.remove(item);
                        break;
                    }
                }
                if (!failedItems.contains(item)) break;
            }
        }

        for (ItemStack item : failedItems) {
            if (!player.getInventory().add(item)) {
                player.drop(item, false);
            }
        }
    }

    @Override
    public String integrationId() {
        return "curios";
    }
}
