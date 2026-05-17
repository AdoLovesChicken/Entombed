package me.adoloveschicken.entombed.integration.sable;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    public static Boolean isOnSubLevel(ServerPlayer player) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getTrackingOrVehicleSubLevel(player);
        return subLevel != null;
    }

    public static BlockPos getPositionFromWorld(ServerLevel level, BlockPos worldPos) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, Vec3.atCenterOf(worldPos));
        if (subLevel == null) return worldPos;
        Vec3 localPos = subLevel.logicalPose().transformPositionInverse(Vec3.atCenterOf(worldPos));
        return BlockPos.containing(localPos);
    }

    public static Direction getLocalFacing(ServerPlayer player) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getTrackingOrVehicleSubLevel(player);
        if (subLevel == null) return player.getDirection().getOpposite();

        Vec3 worldFacing = Vec3.directionFromRotation(player.getXRot(), player.getYRot());

        Vec3 localFacing = subLevel.logicalPose().transformNormalInverse(worldFacing);

        Direction best = Direction.NORTH;
        double bestDot = Double.NEGATIVE_INFINITY;
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            Vec3 dirVec = Vec3.atLowerCornerOf(dir.getNormal());
            double dot = localFacing.dot(dirVec);
            if (dot > bestDot) {
                bestDot = dot;
                best = dir;
            }
        }

        return best.getOpposite();
    }

}