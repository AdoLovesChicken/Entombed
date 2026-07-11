package me.adoloveschicken.entombed.event;

import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import me.adoloveschicken.entombed.platform.PlatformRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;

public class FabricDeathHandler {

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof ServerPlayer player) {
                if (DeathHandler.onPlayerDrops(player)) {
                    DeathHandler.onPlayerDeath(player);
                    player.getInventory().clearContent();
                    player.skipDropExperience();
                    if (PlatformRegistry.get().isModLoaded("trinkets")) {
                        TrinketsApi.getTrinketComponent(player).ifPresent(component ->
                                component.getInventory().values().forEach(group ->
                                        group.values().forEach(TrinketInventory::clearContent)
                                )
                        );
                    }
                }
            }
            return true;
        });
    }
}