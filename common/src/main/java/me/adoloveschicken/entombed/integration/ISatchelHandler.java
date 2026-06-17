package me.adoloveschicken.entombed.integration;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface ISatchelHandler {
    void storeSatchel(Player player, CompoundTag tag);
    void returnSatchel(Player player, CompoundTag tag);
}
