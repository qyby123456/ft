/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 */
package net.farmingtales.ftboss.boss.ai;

import java.lang.reflect.Method;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

public final class BossUnstuck {
    private static final int CHECK = 80;
    private static final double MIN_MOVE = 2.0;
    private static final double MIN_TARGET_DIST = 4.0;
    private static final String K_T = "ftboss_unstuck_t";
    private static final String K_X = "ftboss_unstuck_x";
    private static final String K_Z = "ftboss_unstuck_z";
    private static Class<?> MAGIC_IFACE;
    private static Method IS_CASTING;
    private static boolean magicLookedUp;

    private BossUnstuck() {
    }

    public static void tick(LivingEntity boss) {
        LivingEntity target;
        if (boss.m_9236_().m_5776_() || boss.m_21224_()) {
            return;
        }
        Level level = boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        CompoundTag tag = boss.getPersistentData();
        int t = tag.m_128451_(K_T);
        if (t <= 0) {
            tag.m_128405_(K_T, 80);
            tag.m_128347_(K_X, boss.m_20185_());
            tag.m_128347_(K_Z, boss.m_20189_());
            return;
        }
        tag.m_128405_(K_T, t - 1);
        if (t > 1) {
            return;
        }
        double moved = Math.sqrt(Math.pow(boss.m_20185_() - tag.m_128459_(K_X), 2.0) + Math.pow(boss.m_20189_() - tag.m_128459_(K_Z), 2.0));
        tag.m_128347_(K_X, boss.m_20185_());
        tag.m_128347_(K_Z, boss.m_20189_());
        if (moved >= 2.0) {
            return;
        }
        if (boss instanceof Mob) {
            Mob m = (Mob)boss;
            v0 = m.m_5448_();
        } else {
            v0 = target = null;
        }
        if (target == null || (double)boss.m_20270_((Entity)target) <= 4.0) {
            return;
        }
        if (BossUnstuck.castingByReflection(boss)) {
            return;
        }
        for (int i = 0; i < 10; ++i) {
            double a = boss.m_217043_().m_188500_() * Math.PI * 2.0;
            double r = 2.0 + boss.m_217043_().m_188500_() * 3.5;
            double x = target.m_20185_() + Math.cos(a) * r;
            double z = target.m_20189_() + Math.sin(a) * r;
            int y = sl.m_6924_(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)Math.floor(x), (int)Math.floor(z));
            if (!boss.m_20984_(x, (double)y + 0.1, z, false)) continue;
            Fx.particles((Level)sl, x, (double)y + 1.0, z, 24, (ParticleOptions)ParticleTypes.f_123789_, 0.4, 0.04);
            Fx.particles((Level)sl, boss.m_20185_(), boss.m_20186_() + 1.0, boss.m_20189_(), 16, (ParticleOptions)ParticleTypes.f_123760_, 0.3, 0.05);
            sl.m_6263_(null, x, (double)y, z, SoundEvents.f_11852_, SoundSource.HOSTILE, 1.0f, 0.9f);
            FTBossMod.LOGGER.info("[FTBoss] \u9632\u5361\u77ac\u79fb: {} -> ({}, {}, {}) ({}t \u5185\u53ea\u632a\u4e86 {} \u683c)", new Object[]{boss.m_7755_().getString(), String.format("%.1f", x), y, String.format("%.1f", z), 80, String.format("%.1f", moved)});
            return;
        }
        boss.m_6021_(target.m_20185_(), target.m_20186_() + 0.5, target.m_20189_());
        FTBossMod.LOGGER.info("[FTBoss] \u9632\u5361\u77ac\u79fb(\u515c\u5e95): {} -> \u76ee\u6807\u811a\u8fb9", (Object)boss.m_7755_().getString());
    }

    private static boolean castingByReflection(LivingEntity boss) {
        try {
            Boolean b;
            if (!magicLookedUp) {
                magicLookedUp = true;
                MAGIC_IFACE = Class.forName("io.redspace.ironsspellbooks.api.entity.IMagicEntity");
                IS_CASTING = MAGIC_IFACE.getMethod("isCasting", new Class[0]);
            }
            if (MAGIC_IFACE == null || IS_CASTING == null || !MAGIC_IFACE.isInstance(boss)) {
                return false;
            }
            Object r = IS_CASTING.invoke(boss, new Object[0]);
            return r instanceof Boolean && (b = (Boolean)r) != false;
        }
        catch (Throwable t) {
            MAGIC_IFACE = null;
            IS_CASTING = null;
            return false;
        }
    }

    static {
        magicLookedUp = false;
    }
}

