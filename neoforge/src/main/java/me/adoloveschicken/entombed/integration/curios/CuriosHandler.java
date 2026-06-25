package me.adoloveschicken.entombed.integration.curios;

import me.adoloveschicken.entombed.api.TombIntegration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Optional;

public class CuriosHandler implements TombIntegration {

    @Override
    public void saveData(Player player, CompoundTag tag) {
        if (ModList.get().isLoaded("curios")) {
            Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(player);
            if (curiosInventory.isPresent()) {
                ICuriosItemHandler handler = curiosInventory.get();
                Map<String, ICurioStacksHandler> curiosMap = handler.getCurios();
                ListTag curiosTagList = new ListTag();
                for (Map.Entry<String, ICurioStacksHandler> entry : curiosMap.entrySet()) {
                    String slotType = entry.getKey();
                    ICurioStacksHandler stacksHandler = entry.getValue();
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
                }
                tag.put("CurioItems", curiosTagList);
            }
        }
    }

    @Override
    public void retrieveData(Player player, CompoundTag tag) {
        if (ModList.get().isLoaded("curios")) {
            Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(player);
            if (curiosInventory.isPresent() && tag.contains("CurioItems")) {
                ICuriosItemHandler handler = curiosInventory.get();
                ListTag curiosTagList = tag.getList("CurioItems", ListTag.TAG_COMPOUND);
                for (int i = 0; i < curiosTagList.size(); i++) {
                    CompoundTag itemEntry = curiosTagList.getCompound(i);
                    String slotType = itemEntry.getString("SlotType");
                    int slotIndex = itemEntry.getInt("SlotIndex");
                    ItemStack item = ItemStack.parseOptional(player.level().registryAccess(), itemEntry.getCompound("Item"));

                    ICurioStacksHandler stacksHandler = handler.getCurios().get(slotType);
                    if (stacksHandler != null) {
                        if (stacksHandler.getStacks().getStackInSlot(slotIndex).isEmpty()) stacksHandler.getStacks().setStackInSlot(slotIndex, item);
                        else if (!player.getInventory().add(item)) player.drop(item, false);
                    }
                }
            }
        }
    }

    @Override
    public String integrationId() {
        return "curios";
    }
}
