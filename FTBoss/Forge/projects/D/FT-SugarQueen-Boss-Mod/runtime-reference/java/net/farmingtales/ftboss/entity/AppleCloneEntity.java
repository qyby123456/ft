/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.core.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.core.animation.AnimationController
 *  software.bernie.geckolib.core.animation.AnimationState
 *  software.bernie.geckolib.core.animation.RawAnimation
 *  software.bernie.geckolib.core.object.PlayState
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package net.farmingtales.ftboss.entity;

import java.util.EnumSet;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.network.AppleBlastNetwork;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AppleCloneEntity
extends Monster
implements GeoAnimatable {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.apple_boss.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.apple_boss.walk");
    private static final RawAnimation ROLL = RawAnimation.begin().thenPlay("animation.apple_boss.roll_charge");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.apple_boss.death");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private int chargeCd = 15 + (int)(Math.random() * 20.0);
    private int chargeWindup = 0;
    private int dashT = 0;
    private double dashX = 0.0;
    private double dashZ = 0.0;
    private int life = 0;
    private boolean exploded = false;

    public AppleCloneEntity(EntityType<? extends AppleCloneEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 0;
        this.m_21530_();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 20.0).m_22268_(Attributes.f_22281_, 0.0).m_22268_(Attributes.f_22279_, 0.28).m_22268_(Attributes.f_22277_, 40.0);
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(0, (Goal)new FloatGoal((Mob)this));
        this.f_21345_.m_25352_(3, (Goal)new Chase());
        this.f_21346_.m_25352_(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    public void m_8119_() {
        ServerLevel s;
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            return;
        }
        Level level = this.m_9236_();
        ServerLevel sl = level instanceof ServerLevel ? (s = (ServerLevel)level) : null;
        ++this.life;
        if (this.life > 280) {
            this.detonate(sl, false);
            return;
        }
        if (this.chargeWindup > 0) {
            if (--this.chargeWindup == 0) {
                LivingEntity t = this.m_5448_();
                if (t != null) {
                    this.dashX = t.m_20185_();
                    this.dashZ = t.m_20189_();
                }
                this.dashT = 30;
                if (sl != null) {
                    sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12363_, SoundSource.HOSTILE, 1.0f, 1.4f);
                }
            }
            return;
        }
        if (this.dashT > 0) {
            --this.dashT;
            double dx = this.dashX - this.m_20185_();
            double dz = this.dashZ - this.m_20189_();
            double dl = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
            this.m_6034_(this.m_20185_() + dx / dl * 0.95, this.m_20186_(), this.m_20189_() + dz / dl * 0.95);
            if (sl != null) {
                Fx.particles((Level)sl, this.m_20185_(), this.m_20186_() + 0.3, this.m_20189_(), 1, (ParticleOptions)ParticleTypes.f_123810_, 0.7, 0.05);
            }
            for (Player p : this.m_9236_().m_6907_()) {
                if (!p.m_6084_() || !(p.m_20280_((Entity)this) < 6.25)) continue;
                this.detonate(sl, false);
                return;
            }
            if (this.dashT <= 0 || dl < 1.2) {
                this.detonate(sl, false);
            }
            return;
        }
        if (this.m_5448_() != null && this.m_5448_().m_6084_() && --this.chargeCd <= 0) {
            this.chargeCd = 60 + (int)(Math.random() * 40.0);
            this.chargeWindup = 8;
        }
    }

    private void detonate(ServerLevel sl, boolean onDeath) {
        if (this.exploded) {
            return;
        }
        this.exploded = true;
        double R = onDeath ? 3.5 : 5.0;
        float base = onDeath ? 14.0f : 28.0f;
        double x = this.m_20185_();
        double y = this.m_20186_() + 0.7;
        double z = this.m_20189_();
        if (sl != null) {
            AppleBlastNetwork.show(sl, x, y, z, R);
            sl.m_6263_(null, x, y, z, SoundEvents.f_11913_, SoundSource.HOSTILE, 1.6f, 0.75f);
            for (Player p : sl.m_6907_()) {
                double d;
                if (!p.m_6084_() || (d = Math.sqrt(p.m_20275_(x, y, z))) > R) continue;
                p.m_6469_(this.m_269291_().m_269036_((Entity)this, (Entity)this), (float)((double)base * 0.5 * Math.max(0.25, 1.0 - d / R)));
                BossKnockback.knockback((LivingEntity)p, 1.3, x - p.m_20185_(), z - p.m_20189_());
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] clone detonated (death={})", (Object)onDeath);
        }
        this.m_146870_();
    }

    public void m_6667_(DamageSource source) {
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            this.detonate(sl, true);
        }
        super.m_6667_(source);
    }

    public boolean m_6785_(double d) {
        return false;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "apple_clone_ctrl", 4, this::predicate)});
    }

    private PlayState predicate(AnimationState<AppleCloneEntity> state) {
        if (this.m_21224_()) {
            state.setAnimation(DEATH);
            return PlayState.CONTINUE;
        }
        if (this.dashT > 0 || this.chargeWindup > 0) {
            state.setAnimation(ROLL);
            return PlayState.CONTINUE;
        }
        state.setAnimation(state.isMoving() ? WALK : IDLE);
        return PlayState.CONTINUE;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public double getTick(Object o) {
        return this.f_19797_;
    }

    private class Chase
    extends Goal {
        Chase() {
            this.m_7021_(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean m_8036_() {
            LivingEntity t = AppleCloneEntity.this.m_5448_();
            return t != null && t.m_6084_() && AppleCloneEntity.this.dashT <= 0 && AppleCloneEntity.this.chargeWindup <= 0;
        }

        public boolean m_8045_() {
            return this.m_8036_();
        }

        public void m_8037_() {
            LivingEntity t = AppleCloneEntity.this.m_5448_();
            if (t == null) {
                return;
            }
            AppleCloneEntity.this.m_21563_().m_24960_((Entity)t, 30.0f, 30.0f);
            if (AppleCloneEntity.this.m_20280_((Entity)t) > 4.0) {
                AppleCloneEntity.this.m_21573_().m_5624_((Entity)t, 1.25);
            } else {
                AppleCloneEntity.this.m_21573_().m_26573_();
            }
        }
    }
}

