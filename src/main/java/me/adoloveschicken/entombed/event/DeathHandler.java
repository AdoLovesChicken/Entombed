package me.adoloveschicken.entombed.event;

import me.adoloveschicken.entombed.block.GravestoneBlock;
import me.adoloveschicken.entombed.block.GravestoneBlockEntity;
import me.adoloveschicken.entombed.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public class DeathHandler {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event){
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                return;
            }
            ServerLevel level = player.serverLevel();
            BlockPos pos = player.blockPosition();
            Direction facing = player.getDirection().getOpposite();
            level.setBlock(pos, ModBlocks.TOMB.get().defaultBlockState().setValue(GravestoneBlock.FACING, facing), 3);
            if (level.getBlockEntity(pos) instanceof GravestoneBlockEntity gravestoneBlockEntity) {
                gravestoneBlockEntity.storeItems(player);
                level.playSound(null, pos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.5f, 0.8f);
                level.sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                return;
            }
            event.setCanceled(true);
        }
    }

    public static void register() {
        NeoForge.EVENT_BUS.register(DeathHandler.class);
    }

}
