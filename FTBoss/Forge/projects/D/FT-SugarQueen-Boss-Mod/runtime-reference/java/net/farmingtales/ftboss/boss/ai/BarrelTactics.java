/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class BarrelTactics {
    private final Mob e;
    private final float damageScale;

    public BarrelTactics(Mob entity) {
        this(entity, 1.0f);
    }

    public BarrelTactics(Mob entity, float damageScale) {
        this.e = entity;
        this.damageScale = damageScale > 0.0f ? damageScale : 1.0f;
    }

    public float damageScale() {
        return this.damageScale;
    }

    private ServerLevel sl() {
        ServerLevel s;
        Level level = this.e.m_9236_();
        return level instanceof ServerLevel ? (s = (ServerLevel)level) : null;
    }

    private void snd(SoundEvent ev, float vol, float pitch) {
        Fx.sound(this.e.m_9236_(), this.e.m_20185_(), this.e.m_20186_() + 0.6, this.e.m_20189_(), ev, vol, pitch);
    }

    private void part(double x, double y, double z, int n, SimpleParticleType t, double spread, double speed) {
        Fx.particles(this.e.m_9236_(), x, y, z, n, (ParticleOptions)t, spread, speed);
    }

    private void fx(double x, double y, double z, int n, float r, float g, float b, double spread, double speed) {
        Fx.dust(this.e.m_9236_(), x, y, z, n, r, g, b, spread, speed);
    }

    public LivingEntity nearestPlayer(double range) {
        return BossTargets.combatTarget(this.e, range);
    }

    public void mist() {
        double x = this.e.m_20185_() + (this.e.m_217043_().m_188500_() - 0.5) * 3.0;
        double y = this.e.m_20186_() + this.e.m_217043_().m_188500_() * 2.4;
        double z = this.e.m_20189_() + (this.e.m_217043_().m_188500_() - 0.5) * 3.0;
        this.part(x, y, z, 2, ParticleTypes.f_175833_, 0.4, 0.01);
        this.fx(x, y, z, 1, 0.72f, 0.1f, 0.14f, 0.35, 0.01);
    }

    public void rollTrail() {
        double x = this.e.m_20185_() + (this.e.m_217043_().m_188500_() - 0.5) * 1.6;
        double z = this.e.m_20189_() + (this.e.m_217043_().m_188500_() - 0.5) * 1.6;
        this.fx(x, this.e.m_20186_() + 0.15, z, 2, 0.62f, 0.1f, 0.16f, 0.25, 0.005);
        this.part(x, this.e.m_20186_() + 0.1, z, 1, ParticleTypes.f_123797_, 0.3, 0.02);
    }

    public void slam() {
        ServerLevel s = this.sl();
        if (s == null) {
            return;
        }
        double x = this.e.m_20185_();
        double y = this.e.m_20186_() + 0.3;
        double z = this.e.m_20189_();
        this.snd(SoundEvents.f_12630_, 1.7f, 0.5f);
        this.snd(SoundEvents.f_11913_, 0.5f, 1.6f);
        Fx.ring(this.e.m_9236_(), x, y, z, 6.5, 36, (ParticleOptions)ParticleTypes.f_123796_, 0.02);
        this.part(x, y, z, 20, ParticleTypes.f_123797_, 1.3, 0.28);
        this.part(x, y, z, 12, ParticleTypes.f_123759_, 1.1, 0.06);
        this.fx(x, y, z, 14, 0.85f, 0.62f, 0.18f, 0.9, 0.15);
        for (ServerPlayer p : this.e.m_9236_().m_45976_(ServerPlayer.class, this.e.m_20191_().m_82400_(6.5))) {
            if (!BossTargets.valid((Player)p) || this.e.m_20280_((Entity)p) > 42.25 || !p.m_6469_(this.e.m_269291_().m_269333_((LivingEntity)this.e), 4.75f * this.damageScale)) continue;
            BossKnockback.knockback((LivingEntity)p, 1.5, x - p.m_20185_(), z - p.m_20189_());
            this.part(p.m_20185_(), p.m_20186_() + 1.9, p.m_20189_(), 8, ParticleTypes.f_123771_, 0.35, 0.06);
        }
    }

    public void corkSpray() {
        ServerLevel s = this.sl();
        if (s == null) {
            return;
        }
        this.snd(SoundEvents.f_11769_, 1.5f, 0.8f);
        this.snd(SoundEvents.f_11968_, 1.0f, 0.7f);
        Vec3 dir = new Vec3(-Math.sin(Math.toRadians(this.e.m_146908_())), 0.0, Math.cos(Math.toRadians(this.e.m_146908_())));
        double x = this.e.m_20185_() + dir.f_82479_ * 1.4;
        double y = this.e.m_20186_() + 1.7;
        double z = this.e.m_20189_() + dir.f_82481_ * 1.4;
        for (int i = 0; i < 26; ++i) {
            double sp = 1.2 + this.e.m_217043_().m_188500_() * 2.4;
            s.m_8767_((ParticleOptions)new DustParticleOptions(new Vector3f(0.78f, 0.1f, 0.16f), 1.1f), x, y, z, 1, dir.f_82479_ * sp + (this.e.m_217043_().m_188500_() - 0.5) * 0.5, 0.25 + this.e.m_217043_().m_188500_() * 0.6, dir.f_82481_ * sp + (this.e.m_217043_().m_188500_() - 0.5) * 0.5, 0.12);
        }
        this.fx(x, y, z, 10, 0.88f, 0.66f, 0.2f, 0.6, 0.12);
        for (ServerPlayer p : this.e.m_9236_().m_45976_(ServerPlayer.class, this.e.m_20191_().m_82400_(11.0))) {
            Vec3 to;
            if (!BossTargets.valid((Player)p) || !this.e.m_142582_((Entity)p) || Math.abs(p.m_20186_() - this.e.m_20186_()) > 3.0 || (to = new Vec3(p.m_20185_() - this.e.m_20185_(), 0.0, p.m_20189_() - this.e.m_20189_())).m_82553_() > 11.0 || to.m_82556_() < 0.001 || dir.m_82526_(to.m_82541_()) < 0.35 || !p.m_6469_(this.e.m_269291_().m_269333_((LivingEntity)this.e), 3.0f * this.damageScale)) continue;
            p.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 100, 0, false, false, true));
            this.part(p.m_20185_(), p.m_20186_() + 1.9, p.m_20189_(), 8, ParticleTypes.f_123771_, 0.35, 0.06);
        }
    }

    public void warnSlam() {
        Fx.ring(this.e.m_9236_(), this.e.m_20185_(), this.e.m_20186_() + 0.12, this.e.m_20189_(), 6.5, 24, (ParticleOptions)ParticleTypes.f_123797_, 0.0);
    }

    public void warnCharge(double forwardX, double forwardZ) {
        for (int step = 2; step <= 10; step += 2) {
            this.part(this.e.m_20185_() + forwardX * (double)step, this.e.m_20186_() + 0.12, this.e.m_20189_() + forwardZ * (double)step, 1, ParticleTypes.f_123797_, 0.04, 0.0);
        }
    }

    public void sprayTrail() {
        double yaw = Math.toRadians(this.e.m_146908_());
        for (int step = 2; step <= 10; step += 2) {
            this.fx(this.e.m_20185_() - Math.sin(yaw) * (double)step, this.e.m_20186_() + 1.2, this.e.m_20189_() + Math.cos(yaw) * (double)step, 2, 0.78f, 0.1f, 0.16f, (double)step * 0.12, 0.015);
        }
    }

    public void hitPlayers(double radius, float dmg, double kb) {
        for (ServerPlayer p : this.e.m_9236_().m_45976_(ServerPlayer.class, this.e.m_20191_().m_82400_(radius))) {
            if (!p.m_6084_() || !p.m_6469_(this.e.m_269291_().m_269333_((LivingEntity)this.e), dmg * 0.5f * this.damageScale)) continue;
            BossKnockback.knockback((LivingEntity)p, kb, this.e.m_20185_() - p.m_20185_(), this.e.m_20189_() - p.m_20189_());
        }
    }
}

