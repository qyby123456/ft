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
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public final class SayaBuds {
    private static final int BUD_COUNT = 5;
    private static final double SCAN = 64.0;
    private static final double PLAYER_RANGE = 48.0;
    private static final int BUFF_CD = 40;
    private static final int REMIND_CD = 100;
    private static final int BLOCK_NOTIFY_CD = 40;
    private final Mob boss;
    private final Predicate<Entity> owned;
    private boolean shield = false;
    private int buffCd = 40;
    private int remindCd = 20;
    private int blockCd = 0;

    public SayaBuds(TeaTreeBossEntity boss) {
        this((Mob)boss, summon -> SummonSweep.belongsTo((Entity)boss, summon));
    }

    public SayaBuds(Mob boss, Predicate<Entity> owned) {
        this.boss = boss;
        this.owned = owned;
    }

    public boolean shielded() {
        return this.shield;
    }

    public int aliveBuds() {
        return this.collectBuds().size();
    }

    public void trigger() {
        this.shield = true;
        this.buffCd = 30;
        this.remindCd = 20;
        this.blockCd = 0;
        Level level = this.boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        int spawned = 0;
        for (int i = 0; i < 5; ++i) {
            TeaMinionEntity bud = (TeaMinionEntity)((EntityType)FTEntities.TEA_MINION.get()).m_20615_((Level)sl);
            if (bud == null) continue;
            double angle = 1.2566370614359172 * (double)i + this.boss.m_217043_().m_188500_() * 0.4;
            double r = 4.5 + this.boss.m_217043_().m_188500_() * 2.5;
            bud.m_6034_(this.boss.m_20185_() + Math.cos(angle) * r, this.boss.m_20186_(), this.boss.m_20189_() + Math.sin(angle) * r);
            bud.makeGuardBud();
            bud.m_6593_((Component)Component.m_237115_((String)"entity.ftboss.guard_bud"));
            if (this.boss.m_5448_() != null) {
                bud.m_6710_(this.boss.m_5448_());
            }
            ManorFightMembers.inherit((Entity)this.boss, (Entity)bud);
            sl.m_7967_((Entity)bud);
            Fx.particles((Level)sl, bud.m_20185_(), bud.m_20186_() + 0.7, bud.m_20189_(), 18, (ParticleOptions)ParticleTypes.f_175833_, 0.35, 0.03);
            Fx.particles((Level)sl, bud.m_20185_(), bud.m_20186_() + 0.7, bud.m_20189_(), 10, (ParticleOptions)ParticleTypes.f_123748_, 0.3, 0.05);
            ++spawned;
        }
        Fx.sound((Level)sl, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_11739_, 1.5f, 0.7f);
        Fx.sound((Level)sl, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_11767_, 1.0f, 1.1f);
        Fx.ring((Level)sl, this.boss.m_20185_(), this.boss.m_20186_() + 0.2, this.boss.m_20189_(), 5.5, 40, (ParticleOptions)ParticleTypes.f_123748_, 0.02);
        this.broadcast("msg.ftboss.saya_shield", spawned);
        FTBossMod.LOGGER.info("[FTBoss] \u6c99\u8036 \u4e8c\u9636\u6bb5: \u8336\u82bd\u5e87\u4f51\u542f\u52a8({} \u682a), \u6e05\u7a7a\u524d\u5b8c\u5168\u514d\u4f24", (Object)spawned);
    }

    public void tick() {
        if (this.boss.m_21224_()) {
            return;
        }
        if (!this.shield) {
            if (this.boss.m_9236_() instanceof ServerLevel && this.aliveBuds() > 0) {
                this.shield = true;
                FTBossMod.LOGGER.info("[FTBoss] \u6c99\u8036 \u8336\u82bd\u5e87\u4f51\u91cd\u65b0\u63a5\u7ba1({} \u682a)", (Object)this.aliveBuds());
            }
            return;
        }
        int alive = this.aliveBuds();
        if (alive <= 0) {
            this.shield = false;
            Level level = this.boss.m_9236_();
            if (level instanceof ServerLevel) {
                ServerLevel sl = (ServerLevel)level;
                Fx.sound((Level)sl, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_11738_, 1.5f, 0.8f);
                Fx.sound((Level)sl, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_11983_, 1.4f, 0.7f);
                Fx.particles((Level)sl, this.boss.m_20185_(), this.boss.m_20186_() + 1.4, this.boss.m_20189_(), 45, (ParticleOptions)ParticleTypes.f_123797_, 0.9, 0.12);
                this.broadcast("msg.ftboss.saya_shield_break", new Object[0]);
            }
            FTBossMod.LOGGER.info("[FTBoss] \u6c99\u8036 \u5b88\u62a4\u8336\u82bd\u5c3d\u6bc1, \u5e87\u4f51\u7834\u9664");
            return;
        }
        if (this.boss.f_19797_ % 5 == 0) {
            Fx.dust(this.boss.m_9236_(), this.boss.m_20185_(), this.boss.m_20186_() + 1.1, this.boss.m_20189_(), 4, 0.2f, 0.75f, 0.45f, 1.0, 0.01);
        }
        if (--this.buffCd <= 0) {
            this.buffCd = 40;
            this.channelBuffs();
        }
        if (this.blockCd > 0) {
            --this.blockCd;
        }
        if (--this.remindCd <= 0) {
            this.remindCd = 100;
            this.remind(alive);
        }
    }

    public void onBlocked(DamageSource source) {
        if (this.blockCd > 0) {
            return;
        }
        this.blockCd = 40;
        Entity entity = source.m_7639_();
        if (!(entity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer p = (ServerPlayer)entity;
        p.m_5661_((Component)Component.m_237110_((String)"msg.ftboss.saya_shield_hit", (Object[])new Object[]{this.aliveBuds()}), true);
        Fx.sound(this.boss.m_9236_(), this.boss.m_20185_(), this.boss.m_20186_() + 1.0, this.boss.m_20189_(), SoundEvents.f_12346_, 1.0f, 1.3f);
    }

    public void reset() {
        this.shield = false;
        if (this.boss.m_9236_() instanceof ServerLevel) {
            for (TeaMinionEntity bud : this.collectBuds()) {
                bud.m_146870_();
            }
        }
    }

    private void channelBuffs() {
        Level level = this.boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        this.grant(new MobEffectInstance(MobEffects.f_19605_, 140, 1, false, false, true));
        this.grant(new MobEffectInstance(MobEffects.f_19606_, 140, 1, false, false, true));
        for (TeaMinionEntity bud : this.collectBuds()) {
            Fx.particles((Level)sl, bud.m_20185_(), bud.m_20186_() + 0.7, bud.m_20189_(), 8, (ParticleOptions)ParticleTypes.f_175833_, 0.25, 0.02);
            Fx.particles((Level)sl, bud.m_20185_(), bud.m_20186_() + 1.0, bud.m_20189_(), 5, (ParticleOptions)ParticleTypes.f_123748_, 0.25, 0.04);
        }
        Fx.sound((Level)sl, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_144243_, 1.0f, 1.3f);
    }

    private void grant(MobEffectInstance effect) {
        Mob mob = this.boss;
        if (mob instanceof EasterBossEntity) {
            EasterBossEntity lemon = (EasterBossEntity)mob;
            lemon.lemonSacrifice().grant(effect);
        } else {
            this.boss.m_7292_(effect);
        }
    }

    private void remind(int alive) {
        Level level = this.boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        for (ServerPlayer p : sl.m_45976_(ServerPlayer.class, this.boss.m_20191_().m_82400_(48.0))) {
            p.m_5661_((Component)Component.m_237110_((String)"msg.ftboss.saya_shield_remind", (Object[])new Object[]{alive}), true);
        }
    }

    private void broadcast(String key, Object ... args) {
        Level level = this.boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        for (ServerPlayer p : sl.m_45976_(ServerPlayer.class, this.boss.m_20191_().m_82400_(48.0))) {
            p.m_5661_((Component)Component.m_237110_((String)key, (Object[])args), false);
        }
    }

    private List<TeaMinionEntity> collectBuds() {
        ArrayList<TeaMinionEntity> out = new ArrayList<TeaMinionEntity>();
        if (this.boss.m_9236_().m_5776_()) {
            return out;
        }
        for (TeaMinionEntity m : this.boss.m_9236_().m_45976_(TeaMinionEntity.class, this.boss.m_20191_().m_82400_(64.0))) {
            if (!m.m_6084_() || !m.isGuardBud() || !this.owned.test((Entity)m)) continue;
            out.add(m);
        }
        return out;
    }
}

