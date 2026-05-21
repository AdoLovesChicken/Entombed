package me.adoloveschicken.entombed.integration.accessory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface IAccessoryHandler {
    void storeCurios(Player player, CompoundTag tag);
    void returnCurios(Player player, CompoundTag tag);
}
