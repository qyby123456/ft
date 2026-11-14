/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.TeaMinionEntity;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public final class MinionBlessing {
    private static final String TAG = "ftboss_summon";
    private static final String K_OWNER = "ftboss_summon_owner";
    private static final String K_CD = "ftboss_bless_cd";
    private static final int PERIOD = 60;
    private static final int MAX_MINIONS = 8;
    private static final int MAX_EFFECTS = 3;
    private static final int DURATION = 140;

    private MinionBlessing() {
    }

    public static void mark(Entity e) {
        MinionBlessing.mark(e, null);
    }

    public static void mark(Entity e, UUID ownerId) {
        e.getPersistentData().m_128379_(TAG, true);
        if (ownerId != null) {
            e.getPersistentData().m_128362_(K_OWNER, ownerId);
        }
    }

    public static UUID ownerId(Entity e) {
        try {
            return e.getPersistentData().m_128403_(K_OWNER) ? e.getPersistentData().m_128342_(K_OWNER) : null;
        }
        catch (Throwable t) {
            return null;
        }
    }

    public static boolean isSummon(Entity e) {
        RatBossEntity r;
        if (e instanceof TeaMinionEntity) {
            return true;
        }
        if (e instanceof RatBossEntity && (r = (RatBossEntity)e).isMinion()) {
            return true;
        }
        return e.getPersistentData().m_128471_(TAG);
    }

    public static void tick(LivingEntity boss, double range) {
        if (boss.m_9236_().m_5776_() || boss.m_21224_()) {
            return;
        }
        CompoundTag tag = boss.getPersistentData();
        int cd = tag.m_128451_(K_CD);
        if (cd > 0) {
            tag.m_128405_(K_CD, cd - 1);
            return;
        }
        tag.m_128405_(K_CD, 60);
        Collection own = boss.m_21220_();
        if (own.isEmpty()) {
            return;
        }
        List nearby = boss.m_9236_().m_45976_(Mob.class, boss.m_20191_().m_82400_(range));
        int n = 0;
        for (Mob m : nearby) {
            if (n >= 8) break;
            if (!m.m_6084_() || m == boss || !MinionBlessing.isSummon((Entity)m)) continue;
            MinionBlessing.share(boss, (LivingEntity)m, own);
            ++n;
        }
    }

    private static void share(LivingEntity boss, LivingEntity minion, Collection<MobEffectInstance> own) {
        Level level;
        int applied = 0;
        for (MobEffectInstance inst : own) {
            RatBossEntity rm;
            if (inst.m_19544_() == null || !inst.m_19544_().m_19486_() || minion instanceof RatBossEntity && (rm = (RatBossEntity)minion).isMinion() && MinionBlessing.isHealing(inst.m_19544_())) continue;
            int amp = Math.min(1, Math.max(0, inst.m_19564_() - 1));
            minion.m_7292_(new MobEffectInstance(inst.m_19544_(), 140, amp, false, false, true));
            if (++applied < 3) continue;
            break;
        }
        if (applied > 0 && (level = boss.m_9236_()) instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            Fx.particles((Level)sl, minion.m_20185_(), minion.m_20186_() + 1.0, minion.m_20189_(), 6, (ParticleOptions)ParticleTypes.f_123748_, 0.3, 0.03);
        }
    }

    private static boolean isHealing(MobEffect e) {
        return e == MobEffects.f_19605_ || e == MobEffects.f_19617_ || e == MobEffects.f_19616_ || e == MobEffects.f_19618_;
    }
}

