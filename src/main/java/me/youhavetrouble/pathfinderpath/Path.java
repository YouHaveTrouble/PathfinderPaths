package me.youhavetrouble.pathfinderpath;

import com.destroystokyo.paper.entity.Pathfinder;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Mob;

import java.util.ArrayList;
import java.util.List;

public class Path {

    List<Location> path = new ArrayList<>();
    Location startLocation, endLocation;
    int pointLimit;

    /**
     * @param startLocation Location to start pathfinding from
     * @param endLocation   Location that pathfinder is trying to reach
     * @param stepLimit     Limit on how many maximum steps pathfinder will take
     */
    public Path(Location startLocation, Location endLocation, int stepLimit) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.pointLimit = stepLimit;
    }

    /**
     * Updates path Location list for getPath(). Calling this from any other thread than primary will result in ASYNC_ERROR result.
     *
     * @return Result of calculating path
     */
    public PathCalculationResult recalculatePath() {

        if (!Bukkit.isPrimaryThread()) {
            return PathCalculationResult.ASYNC_ERROR;
        }

        // Clear all current points
        path.clear();

        if (startLocation.getWorld() != endLocation.getWorld())
            return PathCalculationResult.DIFFERENT_WORLD;

        Mob mob = spawnMob(startLocation, pointLimit);

        if (mob == null)
            return PathCalculationResult.FAILED_TO_SPAWN_MOB;

        Pathfinder.PathResult pathResult = mob.getPathfinder().findPath(endLocation);

        if (pathResult == null)
            return PathCalculationResult.PATH_NULL;

        for (Location point : pathResult.getPoints()) {
            // add the offset, so location points to the center of a block
            point.add(0.5, 0.25, 0.5);
            path.add(point);
        }
        mob.remove();
        return PathCalculationResult.SUCCESS;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public double getDistance() {
        return startLocation.distance(endLocation);
    }

    public double getDistanceSquared() {
        return startLocation.distanceSquared(endLocation);
    }

    public List<Location> getPath() {
        return path;
    }

    private Mob spawnMob(Location loc, int range) {
        // Create an NMS entity without adding it to the world to prevent clients from rendering it
        WorldServer nmsWorld = ((CraftWorld) loc.getWorld()).getHandle();
        EntityInsentient nmsEntity = EntityTypes.EVOKER.createCreature(nmsWorld, null, null, null, BlockPosition.ZERO, EnumMobSpawn.TRIGGERED, false, false);

        // If failed to spawn mob, return null
        if (nmsEntity == null)
            return null;

        nmsEntity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

        // Convert nms entity to bukkit entity
        Mob mob = (Mob) nmsEntity.getBukkitEntity();

        // This is for setting pathfinder range
        AttributeInstance followRange = mob.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
        if (followRange != null) {
            followRange.setBaseValue(range);
        }
        return mob;
    }
}
