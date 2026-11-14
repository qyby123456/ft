/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.LemonSacrifice;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public final class SummonSweep {
    public static final double RANGE = 80.0;

    private SummonSweep() {
    }

    public static boolean belongsTo(Entity owner, Entity summon) {
        UUID actual = ManorFightMembers.ownerId(summon);
        if (LemonSacrifice.isInheritedSummon(summon) && !owner.m_20148_().equals(actual)) {
            return false;
        }
        UUID root = ManorFightMembers.ownerId(owner);
        if (root == null) {
            root = owner.m_20148_();
        }
        if (actual == null) {
            actual = MinionBlessing.ownerId(summon);
        }
        return actual == null || actual.equals(root) || actual.equals(owner.m_20148_());
    }

    public static int clear(LivingEntity owner, double range, Class<?> ... types) {
        if (owner == null || owner.m_9236_().f_46443_) {
            return 0;
        }
        Level level = owner.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return 0;
        }
        ServerLevel sl = (ServerLevel)level;
        int n = 0;
        for (Entity e : sl.m_45976_(Entity.class, owner.m_20191_().m_82400_(range))) {
            if (e == owner || e.m_213877_() || !SummonSweep.belongsTo((Entity)owner, e)) continue;
            boolean match = false;
            for (Class<?> t : types) {
                if (!t.isInstance(e)) continue;
                match = true;
                break;
            }
            if (!match) continue;
            SummonSweep.burst(sl, e);
            e.m_146870_();
            ++n;
        }
        SummonSweep.report(owner, n, "\u53ec\u5524\u7269");
        return n;
    }

    public static int clearMarked(LivingEntity owner, double range) {
        if (owner == null || owner.m_9236_().f_46443_) {
            return 0;
        }
        Level level = owner.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return 0;
        }
        ServerLevel sl = (ServerLevel)level;
        int n = 0;
        for (Entity e : sl.m_45976_(Entity.class, owner.m_20191_().m_82400_(range))) {
            UUID oid;
            if (e == owner || e.m_213877_() || !MinionBlessing.isSummon(e) || (oid = MinionBlessing.ownerId(e)) == null || !oid.equals(owner.m_20148_())) continue;
            SummonSweep.burst(sl, e);
            e.m_146870_();
            ++n;
        }
        SummonSweep.report(owner, n, "\u6807\u8bb0\u53ec\u5524\u7269");
        return n;
    }

    public static int clearOwned(LivingEntity owner, double range) {
        if (owner == null || owner.m_9236_().f_46443_) {
            return 0;
        }
        Level level = owner.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return 0;
        }
        ServerLevel sl = (ServerLevel)level;
        int n = 0;
        for (Entity e : sl.m_45976_(Entity.class, owner.m_20191_().m_82400_(range))) {
            UUID oid;
            if (e == owner || e.m_213877_() || (oid = MinionBlessing.ownerId(e)) == null || !oid.equals(owner.m_20148_())) continue;
            SummonSweep.burst(sl, e);
            e.m_146870_();
            ++n;
        }
        SummonSweep.report(owner, n, "\u81ea\u6709\u53ec\u5524\u7269");
        return n;
    }

    private static void burst(ServerLevel sl, Entity e) {
        sl.m_8767_((ParticleOptions)ParticleTypes.f_123796_, e.m_20185_(), e.m_20186_() + 0.6, e.m_20189_(), 14, 0.3, 0.4, 0.3, 0.05);
        sl.m_8767_((ParticleOptions)ParticleTypes.f_123762_, e.m_20185_(), e.m_20186_() + 0.4, e.m_20189_(), 8, 0.25, 0.3, 0.25, 0.03);
        sl.m_6263_(null, e.m_20185_(), e.m_20186_(), e.m_20189_(), SoundEvents.f_11937_, SoundSource.HOSTILE, 0.8f, 1.5f);
    }

    private static void report(LivingEntity owner, int n, String kind) {
        if (n <= 0) {
            return;
        }
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey((Object)owner.m_6095_());
        FTBossMod.LOGGER.info("[FTBoss] {} \u6b7b\u4ea1/\u9000\u573a -> \u6e05\u6389 {} \u4e2a{}(\u968f\u4e3b\u6d88\u6563)", new Object[]{key == null ? "Boss" : key, n, kind});
    }
}

