package me.adoloveschicken.entombed.integration.backpacked;

import com.mrcrayfish.backpacked.BackpackHelper;
import io.wispforest.accessories.api.AccessoriesContainer;
import me.adoloveschicken.entombed.api.TombIntegration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import static com.mrcrayfish.backpacked.BackpackHelper.setBackpackStack;

public class BackpackedHandler implements TombIntegration {

    @Override
    public void saveData(Player player, CompoundTag tag) {
        ListTag backpackList = new ListTag();
        int maxBackpacks = BackpackHelper.getBackpackUnlockableSlots(player).getUnlockCount();

        for (int i = 0; i < maxBackpacks; i++) {
            ItemStack stack = BackpackHelper.getBackpackStack(player, i);
            if (!stack.isEmpty()) {
                CompoundTag backpackTag = new CompoundTag();
                backpackTag.putInt("Index", i);
                backpackTag.put("Stack", stack.save(player.registryAccess()));
                backpackList.add(backpackTag);
                setBackpackStack(player, ItemStack.EMPTY, i);
            }
        }

        if (!backpackList.isEmpty()) {
            tag.put("BackpackedItems", backpackList);
        }
    }

    @Override
    public void retrieveData(Player player, CompoundTag tag) {
        if (tag.contains("BackpackedItems")) {
            ListTag backpackList = tag.getList("BackpackedItems", 10);
            for (int i = 0; i < backpackList.size(); i++) {
                CompoundTag backpackTag = backpackList.getCompound(i);
                int index = backpackTag.getInt("Index");
                ItemStack stack = ItemStack.parse(player.registryAccess(), backpackTag.getCompound("Stack")).orElse(ItemStack.EMPTY);
                if (!stack.isEmpty()) {
                    if (!setBackpackStack(player, stack, index)) {
                        if (!player.getInventory().add(stack)) player.drop(stack, false);
                    }
                }
            }
        }
    }

    @Override
    public String integrationId() {
        return "backpacked";
    }
}