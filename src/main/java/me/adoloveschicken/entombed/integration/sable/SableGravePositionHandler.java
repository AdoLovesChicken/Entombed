package me.adoloveschicken.entombed.integration.sable;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class SableGravePositionHandler {

    public static BlockPos getPosition(ServerPlayer player) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getTrackingOrVehicleSubLevel(player);
        if (subLevel == null) return player.blockPosition();
        Vec3 localPos = subLevel.logicalPose().transformPositionInverse(player.position());
        return BlockPos.containing(localPos);
    }

    public static BlockPos getPositionFromWorld(ServerLevel level, BlockPos worldPos) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, Vec3.atCenterOf(worldPos));
        if (subLevel == null) return worldPos;
        Vec3 localPos = subLevel.logicalPose().transformPositionInverse(Vec3.atCenterOf(worldPos));
        return BlockPos.containing(localPos);
    }

}