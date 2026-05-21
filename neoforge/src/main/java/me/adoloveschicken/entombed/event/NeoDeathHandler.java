package me.adoloveschicken.entombed.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public class NeoDeathHandler {

    private static final boolean SABLE_LOADED = ModList.get().isLoaded("sable");
    private static final boolean AERONAUTICS_LOADED = ModList.get().isLoaded("aeronautics");

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DeathHandler.onPlayerDeath(player, SABLE_LOADED, AERONAUTICS_LOADED);
        }
    }

    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (DeathHandler.onPlayerDrops(player)) {
                event.setCanceled(true);
            }
        }
    }

    public static void register() {
        NeoForge.EVENT_BUS.register(NeoDeathHandler.class);
    }
}