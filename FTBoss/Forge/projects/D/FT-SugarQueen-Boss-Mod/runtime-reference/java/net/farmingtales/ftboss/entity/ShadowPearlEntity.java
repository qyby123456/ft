/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ThrowableItemProjectile
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.boss.ai.DandanPearlBan;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.item.weapons.ShadowSwordItem;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ShadowPearlEntity
extends ThrowableItemProjectile {
    public static final float DAMAGE = 8.0f;
    public static final float AOE_DAMAGE = 10.0f;
    public static final double AOE_RADIUS = 3.0;
    public static final int BLIND_TICKS = 120;
    public static final int MAX_LIFE = 40;
    private static final int TRAIL_MAX_PER_TICK = 2;
    private int life;
    private Vec3 lastPos;

    public ShadowPearlEntity(EntityType<? extends ShadowPearlEntity> type, Level level) {
        super(type, level);
    }

    public ShadowPearlEntity(Level level, LivingEntity shooter) {
        super((EntityType)FTEntities.SHADOW_PEARL.get(), shooter, level);
    }

    protected Item m_7881_() {
        return Items.f_42584_;
    }

    protected float m_7139_() {
        return 0.0f;
    }

    public void m_8119_() {
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            this.spawnTrail();
        }
        this.lastPos = this.m_20182_();
        if (!this.m_9236_().f_46443_ && ++this.life > 40) {
            this.impact(this.m_20182_(), false);
        }
    }

    private void spawnTrail() {
        if (this.lastPos == null) {
            return;
        }
        double dist = this.lastPos.m_82554_(this.m_20182_());
        int n = (int)Math.min((double)ShadowPearlEntity.TRAIL_MAX_PER_PICK(), Math.max(1.0, Math.ceil(dist / 0.25)));
        for (int i = 0; i < n; ++i) {
            double f = ((double)i + 1.0) / ((double)n + 1.0);
            this.m_9236_().m_7106_((ParticleOptions)ParticleTypes.f_123760_, this.lastPos.f_82479_ + (this.m_20185_() - this.lastPos.f_82479_) * f, this.lastPos.f_82480_ + (this.m_20186_() - this.lastPos.f_82480_) * f, this.lastPos.f_82481_ + (this.m_20189_() - this.lastPos.f_82481_) * f, 0.0, 0.0, 0.0);
        }
    }

    private static int TRAIL_MAX_PER_PICK() {
        return 2;
    }

    protected void m_5790_(EntityHitResult result) {
        Entity owner;
        super.m_5790_(result);
        if (this.m_9236_().f_46443_) {
            return;
        }
        Entity target = result.m_82443_();
        if (target == (owner = this.m_19749_())) {
            return;
        }
        if (owner instanceof Player) {
            Player victim;
            Player shooter = (Player)owner;
            if (target instanceof Player && !shooter.m_7099_(victim = (Player)target)) {
                return;
            }
        }
        target.m_6469_(this.m_9236_().m_269111_().m_269390_((Entity)this, (Entity)(owner == null ? this : owner)), 8.0f);
        this.impact(result.m_82450_(), true);
    }

    protected void m_8060_(BlockHitResult result) {
        super.m_8060_(result);
        if (this.m_9236_().f_46443_) {
            return;
        }
        Vec3 n = Vec3.m_82528_((Vec3i)result.m_82434_().m_122436_());
        this.impact(result.m_82450_().m_82549_(n.m_82490_(0.12)), true);
    }

    protected void m_6532_(HitResult result) {
        super.m_6532_(result);
        if (!this.m_9236_().f_46443_ && result.m_6662_() == HitResult.Type.ENTITY && !this.m_213877_()) {
            this.impact(this.m_20182_(), true);
        }
    }

    private boolean sealed() {
        ServerPlayer owner;
        Entity entity = this.m_19749_();
        return entity instanceof ServerPlayer && DandanPearlBan.blockLanding(owner = (ServerPlayer)entity, "\u88c2\u77b3\u6295\u73e0 \u00b7 \u672b\u5f71\u73cd\u73e0");
    }

    private void impact(Vec3 at, boolean withSound) {
        ServerPlayer sp;
        Level lv = this.m_9236_();
        Entity owner = this.m_19749_();
        if (this.sealed()) {
            this.m_146870_();
            return;
        }
        boolean delegated = false;
        if (owner instanceof ServerPlayer && (sp = (ServerPlayer)owner).m_6084_()) {
            ShadowSwordItem.teleportStrike(sp, at.f_82479_, at.f_82480_, at.f_82481_);
            delegated = true;
        }
        if (!delegated) {
            AABB box = new AABB(at.f_82479_ - 3.0, at.f_82480_ - 3.0, at.f_82481_ - 3.0, at.f_82479_ + 3.0, at.f_82480_ + 3.0, at.f_82481_ + 3.0);
            DamageSource src = lv.m_269111_().m_269390_((Entity)this, (Entity)(owner == null ? this : owner));
            for (LivingEntity e : lv.m_45976_(LivingEntity.class, box)) {
                if (e == owner) continue;
                if (owner instanceof Player) {
                    Player victim;
                    Player shooter = (Player)owner;
                    if (e instanceof Player && !shooter.m_7099_(victim = (Player)e)) continue;
                }
                e.m_6469_(src, 10.0f);
                e.m_7292_(new MobEffectInstance(MobEffects.f_19610_, 120, 0));
            }
        }
        for (int i = 0; i < 10; ++i) {
            double a = this.f_19796_.m_188500_() * Math.PI * 2.0;
            double b = (this.f_19796_.m_188500_() - 0.5) * Math.PI;
            double s = 0.12 + this.f_19796_.m_188500_() * 0.18;
            lv.m_7106_((ParticleOptions)ParticleTypes.f_123765_, at.f_82479_, at.f_82480_, at.f_82481_, Math.cos(a) * Math.cos(b) * s, Math.sin(b) * s * 0.6, Math.sin(a) * Math.cos(b) * s);
        }
        lv.m_7106_((ParticleOptions)ParticleTypes.f_123747_, at.f_82479_, at.f_82480_, at.f_82481_, 0.0, 0.0, 0.0);
        if (withSound) {
            lv.m_6263_(null, at.f_82479_, at.f_82480_, at.f_82481_, SoundEvents.f_144242_, SoundSource.PLAYERS, 0.9f, 0.7f);
        }
        this.m_146870_();
    }
}

