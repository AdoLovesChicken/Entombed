package me.adoloveschicken.entombed.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface TombIntegration {
    /*
     * Called when a player dies, stores items into a tomb
     * @param player The Player who died
     * @param tag The CompoundTag to save data into
     */
    void saveData(Player player, CompoundTag tag);

    /*
     * Called when a tomb is retrieved, returns items to a player
     * @param player The player to return items to
     * @param tag The CompoundTag containing the saved data
     */
    void retrieveData(Player player, CompoundTag tag);

    /*
     * A unique identifier to be stored into the tomb's data
     * It is recommended to use your mod's ModID
     */
    String integrationId();
}
