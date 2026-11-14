/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package net.farmingtales.ftboss.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BossCollision {
    private static final int HORIZONTAL_RADIUS = 6;
    private static final int AIR_VERTICAL_RADIUS = 8;
    private static final int GROUND_UP = 8;
    private static final int GROUND_DOWN = 16;
    private static final double MOVE_STEP = 0.25;

    private BossCollision() {
    }

    public static Vec3 findSpawn(ServerLevel level, Entity entity, Vec3 preferred, SpawnMode mode) {
        if (!(level != null && entity != null && preferred != null && Double.isFinite(preferred.f_82479_) && Double.isFinite(preferred.f_82480_) && Double.isFinite(preferred.f_82481_))) {
            return null;
        }
        return mode.airborne ? BossCollision.findAir(level, entity, preferred, mode) : BossCollision.findGround(level, entity, preferred, mode);
    }

    public static boolean place(ServerLevel level, Entity entity, Vec3 preferred, SpawnMode mode, float yaw) {
        Vec3 spawn = BossCollision.findSpawn(level, entity, preferred, mode);
        if (spawn == null) {
            return false;
        }
        entity.m_7678_(spawn.f_82479_, spawn.f_82480_, spawn.f_82481_, yaw, 0.0f);
        return true;
    }

    public static void tellNoRoom(Player player, String bossName) {
        if (player != null) {
            player.m_5661_((Component)Component.m_237113_((String)("\u00a7c" + bossName + "\u65e0\u6cd5\u53ec\u5524\uff1a\u9644\u8fd1\u6ca1\u6709\u8db3\u591f\u7684\u7a7a\u95f4\uff0c\u8bf7\u79fb\u5230\u5f00\u9614\u5904\u3002")), true);
        }
    }

    public static boolean move(Entity entity, Vec3 displacement) {
        if (entity == null || displacement == null || displacement.m_82556_() < 1.0E-10) {
            return true;
        }
        Level level = entity.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel)level;
        int steps = Math.max(1, (int)Math.ceil(displacement.m_82553_() / 0.25));
        Vec3 step = displacement.m_82490_(1.0 / (double)steps);
        boolean moved = false;
        for (int i = 0; i < steps; ++i) {
            if (BossCollision.tryStep(level2, entity, step)) {
                moved = true;
                continue;
            }
            boolean axisMoved = false;
            if (Math.abs(step.f_82479_) > 1.0E-9 && BossCollision.tryStep(level2, entity, new Vec3(step.f_82479_, 0.0, 0.0))) {
                axisMoved = true;
            }
            if (Math.abs(step.f_82480_) > 1.0E-9 && BossCollision.tryStep(level2, entity, new Vec3(0.0, step.f_82480_, 0.0))) {
                axisMoved = true;
            }
            if (Math.abs(step.f_82481_) > 1.0E-9 && BossCollision.tryStep(level2, entity, new Vec3(0.0, 0.0, step.f_82481_))) {
                axisMoved = true;
            }
            moved |= axisMoved;
            if (!axisMoved) break;
        }
        if (moved) {
            entity.f_19812_ = true;
        }
        return moved;
    }

    private static Vec3 findAir(ServerLevel level, Entity entity, Vec3 preferred, SpawnMode mode) {
        Vec3 best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        int baseX = (int)Math.floor(preferred.f_82479_);
        int baseY = (int)Math.floor(preferred.f_82480_);
        int baseZ = (int)Math.floor(preferred.f_82481_);
        for (int radius = 0; radius <= 6; ++radius) {
            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    double x = preferred.f_82479_ + (double)dx;
                    double z = preferred.f_82481_ + (double)dz;
                    for (int dy = -8; dy <= 8; ++dy) {
                        double distance;
                        double y = preferred.f_82480_ + (double)dy;
                        if (!BossCollision.validPosition(level, entity, x, y, z, false) || !((distance = BossCollision.distanceSqr(x, y, z, preferred)) < bestDistance)) continue;
                        bestDistance = distance;
                        best = new Vec3(x, y, z);
                    }
                }
            }
            if (best != null) break;
        }
        return best;
    }

    private static Vec3 findGround(ServerLevel level, Entity entity, Vec3 preferred, SpawnMode mode) {
        Vec3 best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        int baseX = (int)Math.floor(preferred.f_82479_);
        int baseY = (int)Math.floor(preferred.f_82480_);
        int baseZ = (int)Math.floor(preferred.f_82481_);
        for (int radius = 0; radius <= 6; ++radius) {
            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    int blockX = baseX + dx;
                    int blockZ = baseZ + dz;
                    double x = preferred.f_82479_ + (double)dx;
                    double z = preferred.f_82481_ + (double)dz;
                    int high = Math.min(level.m_151558_() - 1, baseY + 8);
                    int low = Math.max(level.m_141937_(), baseY - 16);
                    for (int blockY = high; blockY >= low; --blockY) {
                        double distance;
                        BlockPos floor = new BlockPos(blockX, blockY, blockZ);
                        if (!level.m_46805_(floor)) continue;
                        BlockState state = level.m_8055_(floor);
                        VoxelShape shape = state.m_60812_((BlockGetter)level, floor);
                        double localX = x - (double)blockX;
                        double localZ = z - (double)blockZ;
                        boolean supported = false;
                        for (AABB surface : shape.m_83299_()) {
                            double distance2;
                            if (!(localX >= surface.f_82288_) || !(localX <= surface.f_82291_) || !(localZ >= surface.f_82290_) || !(localZ <= surface.f_82293_)) continue;
                            double feetY = (double)blockY + surface.f_82292_;
                            supported = true;
                            if (!BossCollision.validPosition(level, entity, x, feetY, z, mode.allowFluidSupport) || !((distance2 = BossCollision.distanceSqr(x, feetY, z, preferred)) < bestDistance)) continue;
                            bestDistance = distance2;
                            best = new Vec3(x, feetY, z);
                        }
                        if (supported || !mode.allowFluidSupport || state.m_60819_().m_76178_() || !BossCollision.validPosition(level, entity, x, blockY, z, true) || !((distance = BossCollision.distanceSqr(x, blockY, z, preferred)) < bestDistance)) continue;
                        bestDistance = distance;
                        best = new Vec3(x, (double)blockY, z);
                    }
                }
            }
            if (best != null) break;
        }
        return best;
    }

    private static boolean validPosition(ServerLevel level, Entity entity, double x, double y, double z, boolean allowLiquid) {
        if (y < (double)level.m_141937_() || y + (double)entity.m_20206_() > (double)level.m_151558_()) {
            return false;
        }
        AABB box = entity.m_20191_().m_82386_(x - entity.m_20185_(), y - entity.m_20186_(), z - entity.m_20189_());
        return level.m_6857_().m_61935_(box) && level.m_46805_(BlockPos.m_274561_((double)x, (double)y, (double)z)) && level.m_45756_(entity, box) && (allowLiquid || !level.m_46855_(box));
    }

    private static boolean tryStep(ServerLevel level, Entity entity, Vec3 step) {
        double z;
        double y;
        double x = entity.m_20185_() + step.f_82479_;
        if (!BossCollision.validPosition(level, entity, x, y = entity.m_20186_() + step.f_82480_, z = entity.m_20189_() + step.f_82481_, true)) {
            return false;
        }
        entity.m_6034_(x, y, z);
        return true;
    }

    private static double distanceSqr(double x, double y, double z, Vec3 target) {
        double dx = x - target.f_82479_;
        double dy = y - target.f_82480_;
        double dz = z - target.f_82481_;
        return dx * dx + dy * dy + dz * dz;
    }

    public static enum SpawnMode {
        GROUND(false, false),
        FLUID(true, false),
        AIR(false, true);

        private final boolean allowFluidSupport;
        private final boolean airborne;

        private SpawnMode(boolean allowFluidSupport, boolean airborne) {
            this.allowFluidSupport = allowFluidSupport;
            this.airborne = airborne;
        }
    }
}

