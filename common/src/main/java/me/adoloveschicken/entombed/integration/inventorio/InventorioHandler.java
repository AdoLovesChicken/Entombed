package me.adoloveschicken.entombed.integration.inventorio;

import de.rubixdev.inventorio.player.PlayerInventoryAddon;import de.rubixdev.inventorio.player.inventory.PlayerInventoryExtension;
import me.adoloveschicken.entombed.Entombed;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;import java.util.List;

public class InventorioHandler {

    public static void storeInventorio(Player player, CompoundTag tag) {
        PlayerInventoryAddon addon = PlayerInventoryAddon.Companion.getInventoryAddon(player);
        if (addon == null) {
            Entombed.LOGGER.info("Inventorio addon not available, skipping");
            return;
        }
        saveList(addon.utilityBelt, tag, "InventorioUtilityBelt", player);
        saveList(addon.toolBelt,    tag, "InventorioToolBelt",    player);
        saveList(addon.deepPockets, tag, "InventorioDeepPockets", player);
    }

    public static void returnInventorio(Player player, CompoundTag tag) {
        PlayerInventoryAddon addon = PlayerInventoryAddon.Companion.getInventoryAddon(player);
        if (addon == null) {
            Entombed.LOGGER.info("Inventorio addon not available, skipping");
            return;
        }
        loadList(addon.utilityBelt, tag, "InventorioUtilityBelt", player);
        loadList(addon.toolBelt,    tag, "InventorioToolBelt",    player);
        loadList(addon.deepPockets, tag, "InventorioDeepPockets", player);
    }

    private static void saveList(List<ItemStack> list, CompoundTag tag, String key, Player player) {
        ListTag nbtList = new ListTag();
        for (int i = 0; i < list.size(); i++) {
            ItemStack stack = list.get(i);
            if (!stack.isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putInt("SlotIndex", i);
                entry.put("Item", stack.save(player.level().registryAccess()));
                nbtList.add(entry);
            }
        }
        tag.put(key, nbtList);
    }

    private static void loadList(List<ItemStack> list, CompoundTag tag, String key, Player player) {
        if (!tag.contains(key)) return;
        ListTag nbtList = tag.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < nbtList.size(); i++) {
            CompoundTag entry = nbtList.getCompound(i);
            int slotIndex = entry.getInt("SlotIndex");
            ItemStack stack = ItemStack.parseOptional(player.level().registryAccess(),
                    entry.getCompound("Item"));
            if (slotIndex < list.size()) {
                list.set(slotIndex, stack);
            }
        }
    }

      /*                */
     // UNUSED METHODS //
    /*                */

    public static void saveContainer(Container container, CompoundTag tag, String key, Player player) {
        ListTag list = new ListTag();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putInt("SlotIndex", i);
                entry.put("Item", stack.save(player.level().registryAccess()));
                list.add(entry);
            }
        }
        tag.put(key, list);
    }

    public static void loadContainer(Container container, CompoundTag tag, String key, Player player) {
        if (tag.contains(key)) {
            ListTag list = tag.getList(key, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                int slotIndex = entry.getInt("SlotIndex");
                ItemStack stack = ItemStack.parseOptional(player.level().registryAccess(),
                        entry.getCompound("Item"));
                if (slotIndex < container.getContainerSize()) {
                    container.setItem(slotIndex, stack);
                }
            }
        }
    }
}
