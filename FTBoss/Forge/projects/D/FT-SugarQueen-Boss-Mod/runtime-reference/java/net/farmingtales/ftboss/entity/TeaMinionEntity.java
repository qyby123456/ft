/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
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

import java.util.UUID;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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

public class TeaMinionEntity
extends Monster
implements GeoAnimatable {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.camellia.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.camellia.walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.camellia.attack");
    private static final RawAnimation POLLEN = RawAnimation.begin().thenPlay("animation.camellia.pollen_burst");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.camellia.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.camellia.death");
    private static final double T_ATTACK = 11.0;
    private static final double T_POLLEN = 20.0;
    private static final double T_HURT = 8.0;
    private static final int POLLEN_PERIOD = 140;
    private static final String TAG_GUARD = "ftboss_guard_bud";
    private static final UUID GUARD_HP_ID = UUID.fromString("8f2b1c44-9a13-4a55-9c6e-71c3a0f4d512");
    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public TeaMinionEntity(EntityType<? extends TeaMinionEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 4;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 20.0).m_22268_(Attributes.f_22281_, 1.5).m_22268_(Attributes.f_22283_, 1.5).m_22268_(Attributes.f_22279_, 0.28).m_22268_(Attributes.f_22277_, 24.0).m_22268_(Attributes.f_22278_, 0.3);
    }

    public boolean isGuardBud() {
        return this.getPersistentData().m_128471_(TAG_GUARD);
    }

    public void makeGuardBud() {
        this.getPersistentData().m_128379_(TAG_GUARD, true);
        AttributeInstance hp = this.m_21051_(Attributes.f_22276_);
        if (hp != null) {
            hp.m_22120_(GUARD_HP_ID);
            hp.m_22118_(new AttributeModifier(GUARD_HP_ID, "ftboss_guard_bud_hp", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            this.m_21153_(this.m_21233_());
        }
        this.m_21530_();
        this.m_20340_(true);
        this.f_21364_ = 12;
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(1, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.2, true));
        this.f_21345_.m_25352_(2, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 8.0f));
        this.f_21345_.m_25352_(3, (Goal)new RandomStrollGoal((PathfinderMob)this, 0.8));
        this.f_21346_.m_25352_(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "camellia_ctrl", 4, this::predicate)});
    }

    private PlayState predicate(AnimationState<TeaMinionEntity> state) {
        if (this.m_21224_()) {
            state.setAnimation(DEATH);
            return PlayState.CONTINUE;
        }
        if (TeaMinionEntity.playing(state, HURT, 8.0)) {
            return PlayState.CONTINUE;
        }
        if (TeaMinionEntity.playing(state, ATTACK, 11.0)) {
            return PlayState.CONTINUE;
        }
        if (TeaMinionEntity.playing(state, POLLEN, 20.0)) {
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
        if (!state.isMoving() && Math.floorMod(this.f_19797_ + this.m_19879_() * 13, 140) == 0) {
            state.setAnimation(POLLEN);
            return PlayState.CONTINUE;
        }
        state.setAnimation(state.isMoving() ? WALK : IDLE);
        return PlayState.CONTINUE;
    }

    private static boolean playing(AnimationState<TeaMinionEntity> state, RawAnimation anim, double ticks) {
        return state.isCurrentAnimation(anim) && state.getAnimationTick() < ticks;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geckoCache;
    }

    public double getTick(Object o) {
        return this.f_19797_;
    }
}

