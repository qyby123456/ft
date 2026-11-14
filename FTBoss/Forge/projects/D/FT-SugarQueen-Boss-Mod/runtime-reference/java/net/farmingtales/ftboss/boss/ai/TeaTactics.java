/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.List;
import java.util.function.Predicate;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.SummonSweep;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.TeaMinionEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public final class TeaTactics {
    private static final int MINION_RANGE = 48;
    private static final int ABSORB_RANGE = 24;
    private static final int ABSORB_MAX_PER_CAST = 3;
    private static final int ABSORB_STACK_CAP = 4;
    private final Mob e;
    private final Predicate<Entity> owned;
    private int minionCd = 150;
    private int absorbCd = 320;
    private int absorbStacks = 0;

    public TeaTactics(TeaTreeBossEntity entity) {
        this((Mob)entity, summon -> SummonSweep.belongsTo((Entity)entity, summon));
    }

    public TeaTactics(Mob entity, Predicate<Entity> owned) {
        this.e = entity;
        this.owned = owned;
    }

    public int stacks() {
        return this.absorbStacks;
    }

    private void grant(MobEffectInstance effect) {
        Mob mob = this.e;
        if (mob instanceof EasterBossEntity) {
            EasterBossEntity lemon = (EasterBossEntity)mob;
            lemon.lemonSacrifice().grant(effect);
        } else {
            this.e.m_7292_(effect);
        }
    }

    public void tickMinionSummon() {
        if (this.e.m_21223_() <= 0.0f) {
            return;
        }
        if (--this.minionCd > 0) {
            return;
        }
        boolean lowHp = this.e.m_21223_() < this.e.m_21233_() * 0.5f;
        this.minionCd = lowHp ? 90 : 150;
        int alive = 0;
        for (TeaMinionEntity t : this.e.m_9236_().m_45976_(TeaMinionEntity.class, this.e.m_20191_().m_82400_(48.0))) {
            if (!t.m_6084_() || !this.owned.test((Entity)t) || t.isGuardBud()) continue;
            ++alive;
            t.m_21153_(t.m_21233_());
            t.m_6710_(this.e.m_5448_());
            t.m_21530_();
        }
        int cap = lowHp ? 5 : 3;
        for (int i = alive; i < cap && i < alive + 3; ++i) {
            TeaMinionEntity baby = (TeaMinionEntity)((EntityType)FTEntities.TEA_MINION.get()).m_20615_(this.e.m_9236_());
            if (baby == null) continue;
            double a = this.e.m_217043_().m_188500_() * Math.PI * 2.0;
            double r = 2.5 + this.e.m_217043_().m_188500_() * 4.0;
            baby.m_6034_(this.e.m_20185_() + Math.cos(a) * r, this.e.m_20186_(), this.e.m_20189_() + Math.sin(a) * r);
            baby.m_6593_((Component)Component.m_237115_((String)"entity.ftboss.tea_minion"));
            baby.m_20340_(true);
            baby.m_21530_();
            baby.m_21153_(baby.m_21233_());
            if (this.e.m_5448_() != null) {
                baby.m_6710_(this.e.m_5448_());
            }
            ManorFightMembers.inherit((Entity)this.e, (Entity)baby);
            this.e.m_9236_().m_7967_((Entity)baby);
        }
    }

    public void absorb() {
        TeaTreeBossEntity tea;
        Mob mob = this.e;
        if (mob instanceof TeaTreeBossEntity && (tea = (TeaTreeBossEntity)mob).isTeaMinion() || this.e.m_21224_()) {
            return;
        }
        if (--this.absorbCd > 0) {
            return;
        }
        this.absorbCd = 240;
        if (this.e.m_21223_() >= this.e.m_21233_() - 0.5f) {
            return;
        }
        List minions = this.e.m_9236_().m_45976_(TeaMinionEntity.class, this.e.m_20191_().m_82400_(24.0));
        if (minions.isEmpty()) {
            return;
        }
        int n = 0;
        for (TeaMinionEntity m : minions) {
            if (n >= 3) break;
            if (!m.m_6084_() || !this.owned.test((Entity)m) || m.isGuardBud()) continue;
            ++n;
            Fx.particles(this.e.m_9236_(), m.m_20185_(), m.m_20186_() + 0.7, m.m_20189_(), 14, (ParticleOptions)ParticleTypes.f_123748_, 0.3, 0.05);
            Fx.particles(this.e.m_9236_(), m.m_20185_(), m.m_20186_() + 0.7, m.m_20189_(), 8, (ParticleOptions)ParticleTypes.f_175833_, 0.25, 0.02);
            Fx.sound(this.e.m_9236_(), m.m_20185_(), m.m_20186_(), m.m_20189_(), SoundEvents.f_11968_, 1.0f, 0.7f);
            m.m_146870_();
        }
        float heal = this.e.m_21233_() * 0.075f * (float)n;
        this.e.m_5634_(heal);
        if (n == 0) {
            return;
        }
        int st = this.absorbStacks = Math.min(4, this.absorbStacks + n);
        this.grant(new MobEffectInstance(MobEffects.f_19600_, 400, Math.min(1, st - 1), false, false, true));
        this.grant(new MobEffectInstance(MobEffects.f_19606_, 400, st >= 3 ? 1 : 0, false, false, true));
        this.grant(new MobEffectInstance(MobEffects.f_19617_, 400, Math.min(1, st / 2), false, false, true));
        Level level = this.e.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            Fx.particles((Level)sl, this.e.m_20185_(), this.e.m_20186_() + 1.2, this.e.m_20189_(), 30, (ParticleOptions)ParticleTypes.f_123748_, 0.6, 0.08);
            Fx.particles((Level)sl, this.e.m_20185_(), this.e.m_20186_() + 1.4, this.e.m_20189_(), 20, (ParticleOptions)ParticleTypes.f_175833_, 0.5, 0.02);
            Fx.sound((Level)sl, this.e.m_20185_(), this.e.m_20186_(), this.e.m_20189_(), SoundEvents.f_11739_, 1.0f, 0.8f);
            sl.m_6263_(null, this.e.m_20185_(), this.e.m_20186_(), this.e.m_20189_(), SoundEvents.f_12275_, SoundSource.HOSTILE, 0.5f, 1.4f);
            for (ServerPlayer p : sl.m_45976_(ServerPlayer.class, this.e.m_20191_().m_82400_(32.0))) {
                p.m_5661_((Component)Component.m_237110_((String)"msg.ftboss.saya_absorb", (Object[])new Object[]{n, st}), false);
            }
        }
        FTBossMod.LOGGER.info("[FTBoss] \u6c99\u8036 \u5438\u53d6 {} \u682a\u8336\u82bd\u5c0f\u5996: \u56de\u8840 {} \u5f3a\u5316\u5c42\u6570 {}", new Object[]{n, (int)heal, st});
    }
}

