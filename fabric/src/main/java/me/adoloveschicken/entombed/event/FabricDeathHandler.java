package me.adoloveschicken.entombed.event;

import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

public class FabricDeathHandler {

    private static final boolean SABLE_LOADED = FabricLoader.getInstance().isModLoaded("sable");
    private static final boolean AERONAUTICS_LOADED = false;
    private static final boolean TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof ServerPlayer player) {
                if (DeathHandler.onPlayerDrops(player)) {
                    DeathHandler.onPlayerDeath(player, SABLE_LOADED);
                    player.getInventory().clearContent();
                    if (TRINKETS_LOADED) {
                        TrinketsApi.getTrinketComponent(player).ifPresent(component ->
                                component.getInventory().values().forEach(group ->
                                        group.values().forEach(inv -> inv.clearContent())
                                )
                        );
                    }
                }
            }
            return true;
        });
    }
}