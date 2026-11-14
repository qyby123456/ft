/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
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

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.effect.FTEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
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

public class GiantGrubEntity
extends Monster
implements GeoAnimatable {
    public static final int MAX_ALIVE = 1;
    private static final int INFECTION_STACK_TICKS = 600;
    private static final RawAnimation CRAWL = RawAnimation.begin().thenLoop("animation.maggot.crawl");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public GiantGrubEntity(EntityType<? extends GiantGrubEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 30;
        this.m_21530_();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 180.0).m_22268_(Attributes.f_22281_, 4.5).m_22268_(Attributes.f_22279_, 0.26).m_22268_(Attributes.f_22284_, 6.0).m_22268_(Attributes.f_22278_, 0.8).m_22268_(Attributes.f_22277_, 40.0);
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(0, (Goal)new FloatGoal((Mob)this));
        this.f_21345_.m_25352_(2, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.1, false));
        this.f_21346_.m_25352_(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    public boolean m_7327_(Entity target) {
        boolean hit = super.m_7327_(target);
        if (hit && target instanceof LivingEntity) {
            LivingEntity le = (LivingEntity)target;
            le.m_7292_(new MobEffectInstance(MobEffects.f_19604_, 220, 0, false, true, true));
            le.m_7292_(new MobEffectInstance(MobEffects.f_19612_, 300, 1, false, true, true));
            MobEffectInstance cur = le.m_21124_((MobEffect)FTEffects.INFECTION.get());
            int have = cur == null ? 0 : cur.m_19564_() + 1;
            int want = Math.min(5, have + 2);
            le.m_7292_(new MobEffectInstance((MobEffect)FTEffects.INFECTION.get(), 600, want - 1, false, true, true));
            FTBossMod.LOGGER.info("[FTBoss][Apple] giant grub bit {} -> nausea/hunger/infection x{}", (Object)le.m_7755_().getString(), (Object)want);
        }
        return hit;
    }

    public void m_8119_() {
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            return;
        }
        if (this.f_19797_ % 40 == 0) {
            for (Player p : this.m_9236_().m_6907_()) {
                if (!p.m_6084_() || !(p.m_20280_((Entity)this) <= 36.0)) continue;
                p.m_7292_(new MobEffectInstance(MobEffects.f_19612_, 100, 0, false, true, true));
            }
        }
    }

    public boolean m_6785_(double d) {
        return false;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "giant_grub_ctrl", 5, this::predicate)});
    }

    private PlayState predicate(AnimationState<GiantGrubEntity> state) {
        state.setAnimation(CRAWL);
        return PlayState.CONTINUE;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public double getTick(Object o) {
        return this.f_19797_;
    }
}

