package me.adoloveschicken.entombed.event;

import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.integration.sable.SableAssemblyHelper;
import me.adoloveschicken.entombed.integration.simulated.EndSeaHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

public class NeoDeathHandler {

    private static final boolean SABLE_LOADED = ModList.get().isLoaded("sable");
    private static final boolean AERONAUTICS_LOADED = ModList.get().isLoaded("aeronautics");

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            try {
                BlockPos gravePos = DeathHandler.onPlayerDeath(player, SABLE_LOADED);
                player.skipDropExperience();

                if (AERONAUTICS_LOADED && gravePos != null && EndSeaHandler.hasEndSea(player.serverLevel())) {
                    ServerLevel level = player.serverLevel();
                    BlockPos adjustedPos = gravePos;
                    while (adjustedPos.getY() > 1 &&
                            (level.getBlockState(adjustedPos.below()).canBeReplaced() ||
                                    !level.getBlockState(adjustedPos.below()).getFluidState().isEmpty())) {
                        adjustedPos = adjustedPos.below();
                    }
                    if (adjustedPos.getY() <= 1) {
                        SableAssemblyHelper.assembleBlocks(level, adjustedPos);
                    }
                }
            } catch (Exception e) {
                Entombed.LOGGER.error("Error in death handler", e);
            }
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

    @SubscribeEvent
    public static void onExperienceDrops(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            event.setCanceled(true);
        }
    }

    public static void register() {
        NeoForge.EVENT_BUS.register(NeoDeathHandler.class);
    }
}