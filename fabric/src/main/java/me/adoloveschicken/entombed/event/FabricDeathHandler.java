package me.adoloveschicken.entombed.event;

import dev.emi.trinkets.api.TrinketsApi;
import me.adoloveschicken.entombed.platform.PlatformRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

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