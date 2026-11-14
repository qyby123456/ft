/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.control.FlyingMoveControl
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.ai.navigation.FlyingPathNavigation
 *  net.minecraft.world.entity.ai.navigation.PathNavigation
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
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

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FlyMinionEntity
extends Monster
implements GeoAnimatable {
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("animation.fly_minion.fly");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private final double orbitPhase = this.f_19796_.m_188500_() * Math.PI * 2.0;
    private int orbitTick = 0;

    public FlyMinionEntity(EntityType<? extends FlyMinionEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 0;
        this.m_21530_();
        this.m_20242_(true);
        this.f_21342_ = new FlyingMoveControl((Mob)this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 6.0).m_22268_(Attributes.f_22281_, 1.5).m_22268_(Attributes.f_22279_, 0.4).m_22268_(Attributes.f_22280_, 0.4).m_22268_(Attributes.f_22277_, 32.0);
    }

    protected PathNavigation m_6037_(Level level) {
        return new FlyingPathNavigation((Mob)this, level);
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(2, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.35, true));
        this.f_21346_.m_25352_(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    public void m_8119_() {
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            return;
        }
        if (this.f_19797_ > 600) {
            this.m_146870_();
            return;
        }
        if (this.m_5448_() != null && this.m_5448_().m_6084_()) {
            ++this.orbitTick;
            double a = this.orbitPhase + (double)this.orbitTick * 0.15;
            double r = 1.6;
            double tx = this.m_5448_().m_20185_() + Math.cos(a) * r;
            double tz = this.m_5448_().m_20189_() + Math.sin(a) * r;
            double ty = this.m_5448_().m_20188_() + 0.6;
            double dx = tx - this.m_20185_();
            double dy = ty - this.m_20186_();
            double dz = tz - this.m_20189_();
            double len = Math.max(0.2, Math.sqrt(dx * dx + dy * dy + dz * dz));
            Vec3 dm = this.m_20184_();
            this.m_20334_(dm.f_82479_ + dx / len * 0.045, dm.f_82480_ + dy / len * 0.05, dm.f_82481_ + dz / len * 0.045);
        }
    }

    public boolean m_6785_(double distance) {
        return false;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "fly_ctrl", 2, this::predicate)});
    }

    private PlayState predicate(AnimationState<FlyMinionEntity> state) {
        state.setAnimation(FLY);
        return PlayState.CONTINUE;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public double getTick(Object o) {
        return this.f_19797_;
    }
}

