package net.farmingtales.ftboss.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ManorSafeTeleport {
    private ManorSafeTeleport() {}

    public static Vec3 nearby(ServerLevel level, ServerPlayer player, double radius) {
        if (radius <= 0 || radius > 3 || !Double.isFinite(radius)) return null;
        Vec3 origin = player.position();
        BlockPos center = player.blockPosition();
        int reach = (int) Math.ceil(radius);
        java.util.List<Vec3> candidates = new java.util.ArrayList<>();
        for (int offsetX = -reach; offsetX <= reach; offsetX++) {
            for (int offsetZ = -reach; offsetZ <= reach; offsetZ++) {
                int columnX = center.getX() + offsetX;
                int columnZ = center.getZ() + offsetZ;
                BlockPos column = new BlockPos(columnX, center.getY(), columnZ);
                if (!level.hasChunkAt(column) || !level.getWorldBorder().isWithinBounds(column)) continue;
                for (int offsetY = -reach - 1; offsetY <= reach; offsetY++) {
                    Vec3 spot = onFloor(level, player, columnX, center.getY() + offsetY, columnZ);
                    if (spot == null) continue;
                    double distance = spot.distanceToSqr(origin);
                    if (distance >= 0.25 && distance <= radius * radius) candidates.add(spot);
                }
            }
        }
        return candidates.isEmpty() ? null : candidates.get(player.getRandom().nextInt(candidates.size()));
    }

    public static Vec3 find(ServerLevel level, Entity entity, Vec3 preferred, int radius) {
        BlockPos center = BlockPos.containing(preferred);
        for (int ring = 0; ring <= radius; ring++) {
            for (int offsetX = -ring; offsetX <= ring; offsetX++) {
                for (int offsetZ = -ring; offsetZ <= ring; offsetZ++) {
                    if (Math.max(Math.abs(offsetX), Math.abs(offsetZ)) != ring) continue;
                    int columnX = center.getX() + offsetX;
                    int columnZ = center.getZ() + offsetZ;
                    BlockPos column = new BlockPos(columnX, level.getMinBuildHeight(), columnZ);
                    if (!level.hasChunkAt(column) || !level.getWorldBorder().isWithinBounds(column)) continue;
                    int reference = Math.max(level.getMinBuildHeight(), Math.min(level.getMaxBuildHeight() - 2, center.getY()));
                    for (int distance = 0; distance <= 32; distance++) {
                        Vec3 spot = onFloor(level, entity, columnX, reference - distance - 1, columnZ);
                        if (spot != null) return spot;
                        if (distance <= 16) {
                            spot = onFloor(level, entity, columnX, reference + distance, columnZ);
                            if (spot != null) return spot;
                        }
                    }
                    int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, columnX, columnZ);
                    Vec3 spot = onFloor(level, entity, columnX, surface - 1, columnZ);
                    if (spot != null) return spot;
                }
            }
        }
        return null;
    }

    private static Vec3 onFloor(ServerLevel level, Entity entity, int columnX, int floorY, int columnZ) {
        if (floorY < level.getMinBuildHeight() || floorY >= level.getMaxBuildHeight() - 2) return null;
        BlockPos floor = new BlockPos(columnX, floorY, columnZ);
        var state = level.getBlockState(floor);
        if (!state.getFluidState().isEmpty() || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.CACTUS)
                || state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE) || state.is(Blocks.POWDER_SNOW)) return null;
        for (AABB surface : state.getCollisionShape(level, floor).toAabbs()) {
            if (surface.minX > 0.5 || surface.maxX < 0.5 || surface.minZ > 0.5 || surface.maxZ < 0.5) continue;
            Vec3 spot = new Vec3(columnX + 0.5, floorY + surface.maxY, columnZ + 0.5);
            AABB body = entity.getBoundingBox().move(spot.subtract(entity.position()));
            if (body.maxY >= level.getMaxBuildHeight() || !level.getWorldBorder().isWithinBounds(body)
                    || !level.hasChunksAt(BlockPos.containing(body.minX, body.minY, body.minZ),
                            BlockPos.containing(body.maxX, body.maxY, body.maxZ))
                    || !level.noCollision(entity, body) || level.containsAnyLiquid(body)) continue;
            boolean hazard = false;
            for (BlockPos occupied : BlockPos.betweenClosed(BlockPos.containing(body.minX, body.minY, body.minZ),
                    BlockPos.containing(body.maxX, body.maxY, body.maxZ))) {
                var block = level.getBlockState(occupied);
                if (block.is(Blocks.FIRE) || block.is(Blocks.SOUL_FIRE) || block.is(Blocks.SWEET_BERRY_BUSH)
                        || block.is(Blocks.WITHER_ROSE) || block.is(Blocks.POWDER_SNOW)) { hazard = true; break; }
            }
            if (!hazard) return spot;
        }
        return null;
    }

    public static void move(ServerLevel level, Entity entity, Vec3 spot) {
        entity.stopRiding();
        if (entity instanceof Mob mob) mob.getNavigation().stop();
        if (entity instanceof ServerPlayer player) {
            player.teleportTo(level, spot.x, spot.y, spot.z, player.getYRot(), player.getXRot());
        } else {
            entity.teleportTo(spot.x, spot.y, spot.z);
        }
        entity.setDeltaMovement(Vec3.ZERO);
        entity.fallDistance = 0.0f;
        entity.hasImpulse = true;
    }
}
