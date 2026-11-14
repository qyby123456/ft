/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.Fx;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CleaverSlamFx {
    public static final int LIFE_TICKS = 16;
    public static final double RING_RADIUS = 2.6;
    private static final int GROUND_SEARCH_DOWN = 3;
    private static final double COLUMN_CLEARANCE = 2.5;

    private CleaverSlamFx() {
    }

    public static void play(Level level, double x, double impactY, double z) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        BlockPos base = BlockPos.m_274561_((double)x, (double)impactY, (double)z);
        if (!sl.m_46749_(base)) {
            return;
        }
        double groundY = CleaverSlamFx.findGroundY(sl, base);
        if (Double.isNaN(groundY)) {
            sl.m_8767_((ParticleOptions)ParticleTypes.f_123747_, x, impactY, z, 1, 0.0, 0.0, 0.0, 0.0);
            Fx.particles((Level)sl, x, impactY, z, 10, (ParticleOptions)ParticleTypes.f_123765_, 0.35, 0.08);
            Fx.sound((Level)sl, x, impactY, z, SoundEvents.f_11668_, 0.7f, 0.7f);
            return;
        }
        boolean roomy = CleaverSlamFx.hasClearance(sl, x, groundY, z);
        double r = roomy ? 2.6 : 1.56;
        double top = groundY + 0.06;
        sl.m_8767_((ParticleOptions)ParticleTypes.f_123747_, x, top + 0.4, z, 1, 0.0, 0.0, 0.0, 0.0);
        WeaponFx.burst(sl, new Vec3(x, top, z), r, WeaponFxRules.Theme.BLOOD);
        Fx.dust((Level)sl, x, top + 0.2, z, 26, 0.32f, 0.12f, 0.42f, r * 0.9, 0.18);
        Fx.particles((Level)sl, x, top + 0.3, z, 12, (ParticleOptions)ParticleTypes.f_123797_, 0.7, 0.14);
        if (roomy) {
            for (int i = 0; i < 10; ++i) {
                double yy = top + 0.25 + (double)i * 0.22;
                sl.m_8767_((ParticleOptions)ParticleTypes.f_123789_, x, yy, z, 2, 0.1, 0.02, 0.1, 0.0);
            }
        }
        Fx.sound((Level)sl, x, groundY, z, SoundEvents.f_11668_, 1.0f, 0.62f);
        Fx.sound((Level)sl, x, groundY, z, SoundEvents.f_12201_, 0.6f, 1.25f);
    }

    private static double findGroundY(ServerLevel sl, BlockPos base) {
        for (int dy = 0; dy <= 3; ++dy) {
            BlockPos p = base.m_6625_(dy);
            if (!sl.m_46749_(p)) {
                return Double.NaN;
            }
            VoxelShape shape = sl.m_8055_(p).m_60812_((BlockGetter)sl, p);
            if (shape.m_83281_()) continue;
            AABB top = shape.m_83215_().m_82338_(p);
            double y = top.f_82292_;
            if (!sl.m_46859_(p.m_7494_()) || !sl.m_46859_(p.m_6630_(2))) continue;
            return y;
        }
        return Double.NaN;
    }

    private static boolean hasClearance(ServerLevel sl, double x, double groundY, double z) {
        BlockPos p = BlockPos.m_274561_((double)x, (double)(groundY + 0.1), (double)z);
        for (int i = 1; i <= (int)Math.ceil(2.5); ++i) {
            if (sl.m_46859_(p.m_6630_(i))) continue;
            return false;
        }
        return true;
    }
}

