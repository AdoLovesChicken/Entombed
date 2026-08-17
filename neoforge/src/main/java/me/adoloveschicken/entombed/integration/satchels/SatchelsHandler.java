package me.adoloveschicken.entombed.integration.satchels;

import me.adoloveschicken.entombed.api.TombIntegration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.vercte.satchels.ModAttachmentTypes;
import net.vercte.satchels.content.satchel.SatchelData;

public class SatchelsHandler implements TombIntegration {

    @Override
    public void saveData(Player player, CompoundTag tag) {
        // Save satchel inventory contents
        SatchelData satchelData = SatchelData.get(player);
        if (!satchelData.getSatchelInventory().isEmpty()) {
            CompoundTag inventoryTag = satchelData.getSatchelInventory().serializeNBT(player.registryAccess());
            tag.put("SatchelInventory", inventoryTag);
        }

        // Save satchel item for when Curios isn't installed
        ItemStack satchel = player.getData(ModAttachmentTypes.SATCHEL_SLOT).getStackInSlot(0);
        if (!satchel.isEmpty()) {
            tag.put("SatchelItem", satchel.save(player.registryAccess()));
        }
    }

    @Override
    public void retrieveData(Player player, CompoundTag tag) {
        // Restore satchel item for when Curios isn't installed
        if (tag.contains("SatchelItem")) {
            ItemStack satchel = ItemStack.parse(player.registryAccess(), tag.getCompound("SatchelItem")).orElse(ItemStack.EMPTY);
            if (!satchel.isEmpty()) {
                player.getData(ModAttachmentTypes.SATCHEL_SLOT).setStackInSlot(0, satchel);
                player.syncData(ModAttachmentTypes.SATCHEL_SLOT);
            }
        }

        // Restore satchel inventory contents
        if (tag.contains("SatchelInventory")) {
            SatchelData satchelData = SatchelData.get(player);
            satchelData.getSatchelInventory().deserializeNBT(player.registryAccess(), tag.getCompound("SatchelInventory"));
            if (satchelData.isActive()) {
                satchelData.setActive(false, false);
            }
            satchelData.sendData();
            player.inventoryMenu.slotsChanged(player.getInventory());
        }
    }

    @Override
    public String integrationId() {
        return "satchels_v1";
    }
}