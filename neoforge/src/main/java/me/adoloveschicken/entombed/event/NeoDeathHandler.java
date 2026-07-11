package me.adoloveschicken.entombed.event;

import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.config.ConfigData;
import me.adoloveschicken.entombed.integration.sable.SableAssemblyHelper;
import me.adoloveschicken.entombed.integration.simulated.EndSeaHandler;
import me.adoloveschicken.entombed.platform.PlatformRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

public class NeoDeathHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BlockPos gravePos = DeathHandler.onPlayerDeath(player);
            player.skipDropExperience();
            assembleTombInEndSea(player, gravePos);
        }
    }

    private static void assembleTombInEndSea(ServerPlayer player, BlockPos gravePos) {
        try {
            boolean aeroLoaded = PlatformRegistry.get().isModLoaded("aeronautics");
            if (aeroLoaded && ConfigData.tombsCanBecomeSublevel && !ConfigData.allTombsAreSublevel
                    && gravePos != null && EndSeaHandler.hasEndSea(player.serverLevel())) {
                ServerLevel level = player.serverLevel();
                BlockPos adjustedPos = gravePos;

                // Decide if end sea start y or if world limit is higher
                int suitableY = Math.max(level.getMinBuildHeight(), (int) Math.ceil(EndSeaHandler.getStartY(level)));

                while (adjustedPos.getY() > suitableY &&
                        (level.getBlockState(adjustedPos.below()).canBeReplaced() ||
                                !level.getBlockState(adjustedPos.below()).getFluidState().isEmpty())) {
                    adjustedPos = adjustedPos.below();
                }
                if (adjustedPos.getY() <= suitableY) {
                    SableAssemblyHelper.assembleBlocks(level, adjustedPos);
                }
            }
        } catch (Exception e) {
            Entombed.LOGGER.error("Error in death handler", e);
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
        if (event.getEntity() instanceof ServerPlayer) {
            event.setCanceled(true);
        }
    }

    public static void register() {
        NeoForge.EVENT_BUS.register(NeoDeathHandler.class);
    }
}