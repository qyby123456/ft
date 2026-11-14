/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.RandomStrollGoal
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

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
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

public class SmallBarrelEntity
extends Monster
implements GeoAnimatable {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.barrel_minion.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.barrel_minion.walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.barrel_minion.attack");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.barrel_minion.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.barrel_minion.death");
    private static final double T_ATTACK = 10.0;
    private static final double T_HURT = 8.0;
    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private int fuse = -1;
    private static final double TRIGGER_RANGE = 5.0;
    private static final int FUSE = 30;
    private static final float BLAST = 1.2f;

    public SmallBarrelEntity(EntityType<? extends SmallBarrelEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 30.0).m_22268_(Attributes.f_22281_, 3.0).m_22268_(Attributes.f_22283_, 1.0).m_22268_(Attributes.f_22279_, 0.28).m_22268_(Attributes.f_22277_, 28.0).m_22268_(Attributes.f_22278_, 0.5);
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(1, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.1, true));
        this.f_21345_.m_25352_(2, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 8.0f));
        this.f_21345_.m_25352_(3, (Goal)new RandomStrollGoal((PathfinderMob)this, 0.8));
        this.f_21346_.m_25352_(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    public void m_8119_() {
        double dist;
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            return;
        }
        LivingEntity t = this.m_5448_();
        double d = dist = t == null || !t.m_6084_() ? Double.MAX_VALUE : (double)this.m_20270_((Entity)t);
        if (this.fuse < 0) {
            if (dist <= 5.0) {
                this.fuse = 30;
                this.m_6674_(InteractionHand.MAIN_HAND);
                this.m_9236_().m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12512_, SoundSource.HOSTILE, 1.2f, 1.3f);
            }
        } else {
            if (dist > 9.0) {
                this.fuse = -1;
                return;
            }
            if (this.fuse % 2 == 0) {
                Fx.particles(this.m_9236_(), this.m_20185_(), this.m_20186_() + 0.8, this.m_20189_(), 2, (ParticleOptions)ParticleTypes.f_123762_, 0.2, 0.02);
            }
            if (--this.fuse <= 0) {
                this.detonate();
            }
        }
    }

    private void detonate() {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            this.m_146870_();
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        sl.m_254849_((Entity)this, this.m_20185_(), this.m_20186_() + 0.4, this.m_20189_(), 1.2f, Level.ExplosionInteraction.NONE);
        sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11913_, SoundSource.HOSTILE, 1.8f, 1.1f);
        FTBossMod.LOGGER.info("[FTBoss][\u5c0f\u9152\u6876] \u81ea\u7206 @ {}, {}, {} (\u5a01\u529b {}, \u4e0d\u6bc1\u5730\u5f62)", new Object[]{String.format("%.1f", this.m_20185_()), String.format("%.1f", this.m_20186_()), String.format("%.1f", this.m_20189_()), Float.valueOf(1.2f)});
        this.m_146870_();
    }

    public void m_6667_(DamageSource source) {
        super.m_6667_(source);
        if (!this.m_9236_().f_46443_) {
            Fx.particles(this.m_9236_(), this.m_20185_(), this.m_20186_() + 0.5, this.m_20189_(), 16, (ParticleOptions)ParticleTypes.f_123769_, 0.5, 0.05);
        }
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "barrel_ctrl", 4, this::predicate)});
    }

    private PlayState predicate(AnimationState<SmallBarrelEntity> state) {
        if (this.m_21224_()) {
            state.setAnimation(DEATH);
            return PlayState.CONTINUE;
        }
        if (SmallBarrelEntity.playing(state, HURT, 8.0)) {
            return PlayState.CONTINUE;
        }
        if (SmallBarrelEntity.playing(state, ATTACK, 10.0)) {
            return PlayState.CONTINUE;
        }
        if (this.f_20916_ > 0) {
            state.setAnimation(HURT);
            return PlayState.CONTINUE;
        }
        if (this.f_20911_) {
            state.setAnimation(ATTACK);
            return PlayState.CONTINUE;
        }
        state.setAnimation(state.isMoving() ? WALK : IDLE);
        return PlayState.CONTINUE;
    }

    private static boolean playing(AnimationState<SmallBarrelEntity> state, RawAnimation anim, double ticks) {
        return state.isCurrentAnimation(anim) && state.getAnimationTick() < ticks;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geckoCache;
    }

    public double getTick(Object o) {
        return this.f_19797_;
    }
}

