package me.adoloveschicken.entombed.integration.sable;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class SableGravePositionHandler {

    /*
    If player is on/in a sub-level, return the players coordinates relative to the sub-level,
    otherwise, return their real-world coordinates
     */
    public static BlockPos getPosition(ServerPlayer player) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getTrackingOrVehicleSubLevel(player);
        if (subLevel == null) return player.blockPosition();
        Vec3 localPos = subLevel.logicalPose().transformPositionInverse(player.position());
        return BlockPos.containing(localPos);
    }

    // Checks if a player is on/in a sub-level
    public static boolean isOnSubLevel(ServerPlayer player) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getTrackingOrVehicleSubLevel(player);
        return subLevel != null;
    }

    /*
    If a sub-level is found in a BlockPos, return the appropriate coordinates relative to sub-level,
    otherwise, return the real-world coordinates
     */
    public static BlockPos getPositionFromWorld(ServerLevel level, BlockPos worldPos) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, Vec3.atCenterOf(worldPos));
        if (subLevel == null) return worldPos;
        Vec3 localPos = subLevel.logicalPose().transformPositionInverse(Vec3.atCenterOf(worldPos));
        return BlockPos.containing(localPos);
    }

    // Corrects direction mismatch between sub-levels and the real-world
    public static Direction getLocalFacing(ServerPlayer player) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getTrackingOrVehicleSubLevel(player);
        if (subLevel == null) return player.getDirection().getOpposite();

        // Get the player's world-space facing as a normal vector
        Vec3 worldFacing = Vec3.directionFromRotation(player.getXRot(), player.getYRot());

        // Transform it into the sub-level's local space using the pose's inverse normal transform
        Vec3 localFacing = subLevel.logicalPose().transformNormalInverse(worldFacing);

        // Snap to nearest cardinal direction
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

        return best.getOpposite(); // tomb faces toward where player was looking
    }

}