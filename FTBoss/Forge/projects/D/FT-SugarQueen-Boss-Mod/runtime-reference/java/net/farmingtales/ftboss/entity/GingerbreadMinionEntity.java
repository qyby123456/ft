/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
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

import net.farmingtales.ftboss.integ.WizardSummon;
import net.minecraft.server.level.ServerLevel;
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

public class GingerbreadMinionEntity
extends Monster
implements GeoAnimatable {
    private int spellCd = 160;
    private static final String[] LOW_SPELLS = new String[]{"irons_spellbooks:fireball", "irons_spellbooks:ice_block", "irons_spellbooks:lightning_bolt", "irons_spellbooks:magic_missile"};
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.gingerbread_minion.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.gingerbread_minion.walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.gingerbread_minion.attack");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.gingerbread_minion.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.gingerbread_minion.death");
    private static final double T_ATTACK = 10.0;
    private static final double T_HURT = 8.0;
    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public void m_8119_() {
        Player p;
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            return;
        }
        if (--this.spellCd > 0) {
            return;
        }
        this.spellCd = 160 + this.m_217043_().m_188503_(120);
        LivingEntity livingEntity = this.m_5448_();
        if (!(livingEntity instanceof Player) || !(p = (Player)livingEntity).m_6084_()) {
            return;
        }
        if (this.m_20280_((Entity)p) > 576.0) {
            return;
        }
        String id = LOW_SPELLS[this.m_217043_().m_188503_(LOW_SPELLS.length)];
        WizardSummon.castByName((ServerLevel)this.m_9236_(), (LivingEntity)this, (LivingEntity)p, id, 1);
    }

    public GingerbreadMinionEntity(EntityType<? extends GingerbreadMinionEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 36.0).m_22268_(Attributes.f_22281_, 3.0).m_22268_(Attributes.f_22283_, 1.2).m_22268_(Attributes.f_22279_, 0.34).m_22268_(Attributes.f_22277_, 28.0).m_22268_(Attributes.f_22278_, 0.4);
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(1, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.15, true));
        this.f_21345_.m_25352_(2, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 8.0f));
        this.f_21345_.m_25352_(3, (Goal)new RandomStrollGoal((PathfinderMob)this, 0.8));
        this.f_21346_.m_25352_(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "gingerbread_ctrl", 4, this::predicate)});
    }

    private PlayState predicate(AnimationState<GingerbreadMinionEntity> state) {
        if (this.m_21224_()) {
            state.setAnimation(DEATH);
            return PlayState.CONTINUE;
        }
        if (GingerbreadMinionEntity.playing(state, HURT, 8.0)) {
            return PlayState.CONTINUE;
        }
        if (GingerbreadMinionEntity.playing(state, ATTACK, 10.0)) {
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

    private static boolean playing(AnimationState<GingerbreadMinionEntity> state, RawAnimation anim, double ticks) {
        return state.isCurrentAnimation(anim) && state.getAnimationTick() < ticks;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geckoCache;
    }

    public double getTick(Object o) {
        return this.f_19797_;
    }
}

