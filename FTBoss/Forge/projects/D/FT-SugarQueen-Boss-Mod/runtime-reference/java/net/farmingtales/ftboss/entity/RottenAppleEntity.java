/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
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

import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
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

public class RottenAppleEntity
extends Monster
implements GeoAnimatable {
    public static final int LIFE = 900;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.poop_minion.idle");
    private static final float BLAST_RADIUS = 1.1f;
    private static final double HURT_RANGE = 3.4;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private boolean blasted = false;

    public RottenAppleEntity(EntityType<? extends RottenAppleEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 0;
        this.m_21530_();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 18.0).m_22268_(Attributes.f_22281_, 2.0).m_22268_(Attributes.f_22279_, 0.26).m_22268_(Attributes.f_22277_, 30.0);
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(2, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.0, false));
        this.f_21346_.m_25352_(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    public void m_8119_() {
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            return;
        }
        if (this.f_19797_ > 900) {
            this.m_146870_();
            return;
        }
        if (this.m_5448_() != null && this.m_20280_((Entity)this.m_5448_()) < 6.25) {
            this.blast();
        }
    }

    public void m_6667_(DamageSource source) {
        super.m_6667_(source);
        this.blast();
    }

    private void blast() {
        if (this.m_9236_().f_46443_ || this.blasted) {
            return;
        }
        this.blasted = true;
        double x = this.m_20185_();
        double y = this.m_20186_() + 0.3;
        double z = this.m_20189_();
        this.m_9236_().m_254849_((Entity)this, x, y, z, 1.1f, Level.ExplosionInteraction.NONE);
        this.m_9236_().m_6263_(null, x, y, z, SoundEvents.f_11913_, SoundSource.HOSTILE, 0.9f, 1.3f);
        Fx.particles(this.m_9236_(), x, y, z, 22, (ParticleOptions)ParticleTypes.f_123753_, 1.1, 0.12);
        Fx.particles(this.m_9236_(), x, y, z, 12, (ParticleOptions)ParticleTypes.f_123755_, 0.9, 0.06);
        Fx.particles(this.m_9236_(), x, y, z, 10, (ParticleOptions)ParticleTypes.f_123763_, 0.8, 0.05);
        for (Player p : this.m_9236_().m_6907_()) {
            if (!p.m_6084_() || p.m_20275_(x, y, z) > 11.559999999999999) continue;
            p.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 3.0f);
            p.m_7292_(new MobEffectInstance(MobEffects.f_19614_, 120, 0, false, true, true));
            p.m_7292_(new MobEffectInstance(MobEffects.f_19612_, 140, 0, false, true, true));
        }
        if (!this.m_21224_()) {
            this.m_146870_();
        }
    }

    public boolean m_6785_(double d) {
        return false;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "rotten_ctrl", 6, this::predicate)});
    }

    private PlayState predicate(AnimationState<RottenAppleEntity> state) {
        state.setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public double getTick(Object o) {
        return this.f_19797_;
    }
}

