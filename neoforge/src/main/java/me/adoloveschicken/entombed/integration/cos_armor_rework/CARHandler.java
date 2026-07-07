package me.adoloveschicken.entombed.integration.cos_armor_rework;

import lain.mods.cos.api.CosArmorAPI;
import me.adoloveschicken.entombed.api.TombIntegration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.world.item.ItemStack;

public class CARHandler implements TombIntegration { // COSMETIC ARMOR REWORKED

    @Override
    public void saveData(Player player, CompoundTag tag) {
        CAStacksBase stacks = CosArmorAPI.getCAStacks(player.getUUID());
        if (stacks != null) {
            tag.put("CosmeticArmor", stacks.serializeNBT(player.registryAccess()));
            for (int i = 0; i < stacks.getSlots(); i++) {
                stacks.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void retrieveData(Player player, CompoundTag tag) {
        if (tag.contains("CosmeticArmor")) {
            CAStacksBase stacks = CosArmorAPI.getCAStacks(player.getUUID());
            if (stacks != null) {
                stacks.deserializeNBT(player.registryAccess(), tag.getCompound("CosmeticArmor"));
            }
        }
    }

    @Override
    public String integrationId() {
        return "cosmeticarmorreworked";
    }

}
