package me.adoloveschicken.entombed.integration.satchels;

import me.adoloveschicken.entombed.integration.ISatchelHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.vercte.satchels.ModAttachmentTypes;
import net.vercte.satchels.content.satchel.SatchelData;

public class SatchelsHandler implements ISatchelHandler {

    @Override
    public void storeSatchel(Player player, CompoundTag extraTag) {
        ItemStack satchel = player.getData(ModAttachmentTypes.SATCHEL_SLOT).getStackInSlot(0);
        if (!satchel.isEmpty()) {
            extraTag.put("SatchelItem", satchel.save(player.registryAccess()));
            SatchelData satchelData = SatchelData.get(player);
            CompoundTag inventoryTag = satchelData.getSatchelInventory().serializeNBT(player.registryAccess());
            extraTag.put("SatchelInventory", inventoryTag);
        }
    }

    @Override
    public void returnSatchel(Player player, CompoundTag extraTag) {
        if (extraTag.contains("SatchelItem")) {
            ItemStack satchel = ItemStack.parse(player.registryAccess(), extraTag.getCompound("SatchelItem")).orElse(ItemStack.EMPTY);
            if (!satchel.isEmpty()) {
                player.getData(ModAttachmentTypes.SATCHEL_SLOT).setStackInSlot(0, satchel);
                if (extraTag.contains("SatchelInventory")) {
                    SatchelData satchelData = SatchelData.get(player);
                    satchelData.getSatchelInventory().deserializeNBT(player.registryAccess(), extraTag.getCompound("SatchelInventory"));
                    if (satchelData.isActive()) {
                        satchelData.setActive(false, false);
                    }
                    player.syncData(ModAttachmentTypes.SATCHEL_SLOT);
                    satchelData.sendData();
                    player.inventoryMenu.slotsChanged(player.getInventory());
                }
            }
        }
    }
}