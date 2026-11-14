package net.farmingtales.ftboss.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Shared collision guard for non-Manor boss summoning and flight.
 *
 * <p>Boss summon items used to write a position directly and then add the entity. That is
 * unsafe in houses, caves and narrow corridors: a large boss could be born inside a wall and
 * a flying boss could subsequently keep teleporting through the same wall. This helper always
 * validates the entity's complete bounding box before placing it and moves flying entities in
 * short, collision-checked steps.
 */
public final class BossCollision {
    public enum SpawnMode {
        /** Requires a solid collision surface below the entity. */
        GROUND(false, false),
        /** Allows a fluid block to act as the support surface. */
        FLUID(true, false),
        /** Three-dimensional free space; used by hovering/flying bosses. */
        AIR(false, true);

        private final boolean allowFluidSupport;
        private final boolean airborne;

        SpawnMode(boolean allowFluidSupport, boolean airborne) {
            this.allowFluidSupport = allowFluidSupport;
            this.airborne = airborne;
        }
    }

    private static final int HORIZONTAL_RADIUS = 6;
    private static final int AIR_VERTICAL_RADIUS = 8;
    private static final int GROUND_UP = 8;
    private static final int GROUND_DOWN = 16;
    private static final double MOVE_STEP = 0.25D;

    private BossCollision() {}

    /**
     * Find a nearby position whose full entity box is clear of blocks, liquids and the border.
     * Returns {@code null} when the summon should be rejected without consuming its item.
     */
    public static Vec3 findSpawn(ServerLevel level, Entity entity, Vec3 preferred, SpawnMode mode) {
        if (level == null || entity == null || preferred == null
                || !Double.isFinite(preferred.x) || !Double.isFinite(preferred.y)
                || !Double.isFinite(preferred.z)) return null;

        return mode.airborne
                ? findAir(level, entity, preferred, mode)
                : findGround(level, entity, preferred, mode);
    }

    /** Place an entity only after {@link #findSpawn} has validated it. */
    public static boolean place(ServerLevel level, Entity entity, Vec3 preferred, SpawnMode mode,
                                float yaw) {
        Vec3 spawn = findSpawn(level, entity, preferred, mode);
        if (spawn == null) return false;
        entity.moveTo(spawn.x, spawn.y, spawn.z, yaw, 0.0F);
        return true;
    }

    /** Player-facing failure message shared by all non-Manor summon items. */
    public static void tellNoRoom(Player player, String bossName) {
        if (player != null) {
            player.displayClientMessage(Component.literal("§c" + bossName
                    + "无法召唤：附近没有足够的空间，请移到开阔处。"), true);
        }
    }

    /**
     * Move an airborne boss without teleporting through a wall. The requested displacement is
     * subdivided into small swept steps; each step validates the complete bounding box. If one
     * axis is blocked, the other axes are attempted so the boss can slide along a wall.
     */
    public static boolean move(Entity entity, Vec3 displacement) {
        if (entity == null || displacement == null || displacement.lengthSqr() < 1.0E-10) return true;
        if (!(entity.level() instanceof ServerLevel level)) return false;

        int steps = Math.max(1, (int) Math.ceil(displacement.length() / MOVE_STEP));
        Vec3 step = displacement.scale(1.0D / steps);
        boolean moved = false;
        for (int i = 0; i < steps; i++) {
            if (tryStep(level, entity, step)) {
                moved = true;
                continue;
            }
            // Preserve natural sliding instead of allowing a diagonal teleport through a corner.
            boolean axisMoved = false;
            if (Math.abs(step.x) > 1.0E-9 && tryStep(level, entity, new Vec3(step.x, 0, 0))) axisMoved = true;
            if (Math.abs(step.y) > 1.0E-9 && tryStep(level, entity, new Vec3(0, step.y, 0))) axisMoved = true;
            if (Math.abs(step.z) > 1.0E-9 && tryStep(level, entity, new Vec3(0, 0, step.z))) axisMoved = true;
            moved |= axisMoved;
            if (!axisMoved) break;
        }
        if (moved) entity.hasImpulse = true;
        return moved;
    }

    private static Vec3 findAir(ServerLevel level, Entity entity, Vec3 preferred, SpawnMode mode) {
        Vec3 best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        int baseX = (int) Math.floor(preferred.x);
        int baseY = (int) Math.floor(preferred.y);
        int baseZ = (int) Math.floor(preferred.z);
        for (int radius = 0; radius <= HORIZONTAL_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    double x = preferred.x + dx;
                    double z = preferred.z + dz;
                    for (int dy = -AIR_VERTICAL_RADIUS; dy <= AIR_VERTICAL_RADIUS; dy++) {
                        double y = preferred.y + dy;
                        if (!validPosition(level, entity, x, y, z, false)) continue;
                        double distance = distanceSqr(x, y, z, preferred);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            best = new Vec3(x, y, z);
                        }
                    }
                }
            }
            // Once this shell produced a candidate, a farther shell cannot be a better answer.
            if (best != null) break;
        }
        return best;
    }

    private static Vec3 findGround(ServerLevel level, Entity entity, Vec3 preferred, SpawnMode mode) {
        Vec3 best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        int baseX = (int) Math.floor(preferred.x);
        int baseY = (int) Math.floor(preferred.y);
        int baseZ = (int) Math.floor(preferred.z);
        for (int radius = 0; radius <= HORIZONTAL_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    int blockX = baseX + dx;
                    int blockZ = baseZ + dz;
                    double x = preferred.x + dx;
                    double z = preferred.z + dz;
                    int high = Math.min(level.getMaxBuildHeight() - 1, baseY + GROUND_UP);
                    int low = Math.max(level.getMinBuildHeight(), baseY - GROUND_DOWN);
                    for (int blockY = high; blockY >= low; blockY--) {
                        BlockPos floor = new BlockPos(blockX, blockY, blockZ);
                        if (!level.hasChunkAt(floor)) continue;
                        BlockState state = level.getBlockState(floor);
                        var shape = state.getCollisionShape(level, floor);
                        double localX = x - blockX;
                        double localZ = z - blockZ;
                        double feetY;
                        boolean supported = false;
                        for (AABB surface : shape.toAabbs()) {
                            if (localX >= surface.minX && localX <= surface.maxX
                                    && localZ >= surface.minZ && localZ <= surface.maxZ) {
                                feetY = blockY + surface.maxY;
                                supported = true;
                                if (validPosition(level, entity, x, feetY, z, mode.allowFluidSupport)) {
                                    double distance = distanceSqr(x, feetY, z, preferred);
                                    if (distance < bestDistance) {
                                        bestDistance = distance;
                                        best = new Vec3(x, feetY, z);
                                    }
                                }
                            }
                        }
                        if (!supported && mode.allowFluidSupport && !state.getFluidState().isEmpty()
                                && validPosition(level, entity, x, blockY, z, true)) {
                            double distance = distanceSqr(x, blockY, z, preferred);
                            if (distance < bestDistance) {
                                bestDistance = distance;
                                best = new Vec3(x, blockY, z);
                            }
                        }
                    }
                }
            }
            if (best != null) break;
        }
        return best;
    }

    private static boolean validPosition(ServerLevel level, Entity entity, double x, double y, double z,
                                         boolean allowLiquid) {
        if (y < level.getMinBuildHeight() || y + entity.getBbHeight() > level.getMaxBuildHeight()) return false;
        AABB box = entity.getBoundingBox().move(x - entity.getX(), y - entity.getY(), z - entity.getZ());
        return level.getWorldBorder().isWithinBounds(box)
                && level.hasChunkAt(BlockPos.containing(x, y, z))
                && level.noCollision(entity, box)
                && (allowLiquid || !level.containsAnyLiquid(box));
    }

    private static boolean tryStep(ServerLevel level, Entity entity, Vec3 step) {
        double x = entity.getX() + step.x;
        double y = entity.getY() + step.y;
        double z = entity.getZ() + step.z;
        if (!validPosition(level, entity, x, y, z, true)) return false;
        entity.setPos(x, y, z);
        return true;
    }

    private static double distanceSqr(double x, double y, double z, Vec3 target) {
        double dx = x - target.x;
        double dy = y - target.y;
        double dz = z - target.z;
        return dx * dx + dy * dy + dz * dz;
    }
}