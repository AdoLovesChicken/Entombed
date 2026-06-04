package me.adoloveschicken.entombed.integration.backpacked;

import com.mrcrayfish.backpacked.BackpackHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BackpackedHandler {

    public static void storeBackpack(Player player, CompoundTag extraTag) {
        ListTag backpackList = new ListTag();
        int maxBackpacks = BackpackHelper.getBackpackUnlockableSlots(player).getUnlockCount();

        for (int i = 0; i < maxBackpacks; i++) {
            ItemStack stack = BackpackHelper.getBackpackStack(player, i);
            if (!stack.isEmpty()) {
                CompoundTag backpackTag = new CompoundTag();
                backpackTag.putInt("Index", i);
                backpackTag.put("Stack", stack.save(player.registryAccess()));
                backpackList.add(backpackTag);
            }
        }

        if (!backpackList.isEmpty()) {
            extraTag.put("BackpackedItems", backpackList);
        }
    }

    public static void returnBackpack(Player player, CompoundTag extraTag) {
        if (extraTag.contains("BackpackedItems")) {
            ListTag backpackList = extraTag.getList("BackpackedItems", 10);
            for (int i = 0; i < backpackList.size(); i++) {
                CompoundTag backpackTag = backpackList.getCompound(i);
                int index = backpackTag.getInt("Index");
                ItemStack stack = ItemStack.parse(player.registryAccess(), backpackTag.getCompound("Stack")).orElse(ItemStack.EMPTY);
                if (!stack.isEmpty()) {
                    BackpackHelper.setBackpackStack(player, stack, index);
                }
            }
        }
    }
}