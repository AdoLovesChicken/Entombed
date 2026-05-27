package me.adoloveschicken.entombed.mixin;

import me.adoloveschicken.entombed.block.GravestoneBlock;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Inject(method = "finalizeExplosion", at = @At("HEAD"))
    private void removeProtectedBlocks(CallbackInfo ci) {
        Explosion self = (Explosion)(Object)this;
        ExplosionAccessor accessor = (ExplosionAccessor) self;
        self.getToBlow().removeIf(pos ->
                accessor.getLevel().getBlockState(pos).getBlock() instanceof GravestoneBlock
        );
    }
}