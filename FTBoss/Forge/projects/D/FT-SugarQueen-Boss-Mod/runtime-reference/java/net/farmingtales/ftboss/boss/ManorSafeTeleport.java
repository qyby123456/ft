/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ManorSafeTeleport {
    private ManorSafeTeleport() {
    }

    public static Vec3 nearby(ServerLevel level, ServerPlayer player, double radius) {
        if (radius <= 0.0 || radius > 3.0 || !Double.isFinite(radius)) {
            return null;
        }
        Vec3 origin = player.m_20182_();
        BlockPos center = player.m_20183_();
        int reach = (int)Math.ceil(radius);
        ArrayList<Vec3> candidates = new ArrayList<Vec3>();
        for (int offsetX = -reach; offsetX <= reach; ++offsetX) {
            for (int offsetZ = -reach; offsetZ <= reach; ++offsetZ) {
                int columnX = center.m_123341_() + offsetX;
                int columnZ = center.m_123343_() + offsetZ;
                BlockPos column = new BlockPos(columnX, center.m_123342_(), columnZ);
                if (!level.m_46805_(column) || !level.m_6857_().m_61937_(column)) continue;
                for (int offsetY = -reach - 1; offsetY <= reach; ++offsetY) {
                    double distance;
                    Vec3 spot = ManorSafeTeleport.onFloor(level, (Entity)player, columnX, center.m_123342_() + offsetY, columnZ);
                    if (spot == null || !((distance = spot.m_82557_(origin)) >= 0.25) || !(distance <= radius * radius)) continue;
                    candidates.add(spot);
                }
            }
        }
        return candidates.isEmpty() ? null : (Vec3)candidates.get(player.m_217043_().m_188503_(candidates.size()));
    }

    public static Vec3 find(ServerLevel level, Entity entity, Vec3 preferred, int radius) {
        BlockPos center = BlockPos.m_274446_((Position)preferred);
        for (int ring = 0; ring <= radius; ++ring) {
            for (int offsetX = -ring; offsetX <= ring; ++offsetX) {
                for (int offsetZ = -ring; offsetZ <= ring; ++offsetZ) {
                    Vec3 spot;
                    if (Math.max(Math.abs(offsetX), Math.abs(offsetZ)) != ring) continue;
                    int columnX = center.m_123341_() + offsetX;
                    int columnZ = center.m_123343_() + offsetZ;
                    BlockPos column = new BlockPos(columnX, level.m_141937_(), columnZ);
                    if (!level.m_46805_(column) || !level.m_6857_().m_61937_(column)) continue;
                    int reference = Math.max(level.m_141937_(), Math.min(level.m_151558_() - 2, center.m_123342_()));
                    for (int distance = 0; distance <= 32; ++distance) {
                        spot = ManorSafeTeleport.onFloor(level, entity, columnX, reference - distance - 1, columnZ);
                        if (spot != null) {
                            return spot;
                        }
                        if (distance > 16 || (spot = ManorSafeTeleport.onFloor(level, entity, columnX, reference + distance, columnZ)) == null) continue;
                        return spot;
                    }
                    int surface = level.m_6924_(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, columnX, columnZ);
                    spot = ManorSafeTeleport.onFloor(level, entity, columnX, surface - 1, columnZ);
                    if (spot == null) continue;
                    return spot;
                }
            }
        }
        return null;
    }

    private static Vec3 onFloor(ServerLevel level, Entity entity, int columnX, int floorY, int columnZ) {
        if (floorY < level.m_141937_() || floorY >= level.m_151558_() - 2) {
            return null;
        }
        BlockPos floor = new BlockPos(columnX, floorY, columnZ);
        BlockState state = level.m_8055_(floor);
        if (!state.m_60819_().m_76178_() || state.m_60713_(Blocks.f_50450_) || state.m_60713_(Blocks.f_50128_) || state.m_60713_(Blocks.f_50683_) || state.m_60713_(Blocks.f_50684_) || state.m_60713_(Blocks.f_152499_)) {
            return null;
        }
        for (AABB surface : state.m_60812_((BlockGetter)level, floor).m_83299_()) {
            if (surface.f_82288_ > 0.5 || surface.f_82291_ < 0.5 || surface.f_82290_ > 0.5 || surface.f_82293_ < 0.5) continue;
            Vec3 spot = new Vec3((double)columnX + 0.5, (double)floorY + surface.f_82292_, (double)columnZ + 0.5);
            AABB body = entity.m_20191_().m_82383_(spot.m_82546_(entity.m_20182_()));
            if (body.f_82292_ >= (double)level.m_151558_() || !level.m_6857_().m_61935_(body) || !level.m_46832_(BlockPos.m_274561_((double)body.f_82288_, (double)body.f_82289_, (double)body.f_82290_), BlockPos.m_274561_((double)body.f_82291_, (double)body.f_82292_, (double)body.f_82293_)) || !level.m_45756_(entity, body) || level.m_46855_(body)) continue;
            boolean hazard = false;
            for (BlockPos occupied : BlockPos.m_121940_((BlockPos)BlockPos.m_274561_((double)body.f_82288_, (double)body.f_82289_, (double)body.f_82290_), (BlockPos)BlockPos.m_274561_((double)body.f_82291_, (double)body.f_82292_, (double)body.f_82293_))) {
                BlockState block = level.m_8055_(occupied);
                if (!block.m_60713_(Blocks.f_50083_) && !block.m_60713_(Blocks.f_50084_) && !block.m_60713_(Blocks.f_50685_) && !block.m_60713_(Blocks.f_50070_) && !block.m_60713_(Blocks.f_152499_)) continue;
                hazard = true;
                break;
            }
            if (hazard) continue;
            return spot;
        }
        return null;
    }

    public static void move(ServerLevel level, Entity entity, Vec3 spot) {
        entity.m_8127_();
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            mob.m_21573_().m_26573_();
        }
        if (entity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)entity;
            player.m_8999_(level, spot.f_82479_, spot.f_82480_, spot.f_82481_, player.m_146908_(), player.m_146909_());
        } else {
            entity.m_6021_(spot.f_82479_, spot.f_82480_, spot.f_82481_);
        }
        entity.m_20256_(Vec3.f_82478_);
        entity.f_19789_ = 0.0f;
        entity.f_19812_ = true;
    }
}

