package net.farmingtales.ftboss.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
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

/**
 * 【果蛾】(2026-09-12): 苹果爵士的召唤物 · 无重力飞行。
 * 几何/动画复用苍蝇的资产(geo/fly_minion.*), 只换**奶油色翅膀**贴图; 绕着玩家打转骚扰。
 */
public class AppleMothEntity extends Monster implements GeoAnimatable {

    public static final int LIFE = 600;
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("animation.fly_minion.fly");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final double phase = this.random.nextDouble() * Math.PI * 2.0;
    private int orbitTick = 0;

    public AppleMothEntity(EntityType<? extends AppleMothEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.ATTACK_DAMAGE, 1.5)
                .add(Attributes.MOVEMENT_SPEED, 0.36)
                .add(Attributes.FLYING_SPEED, 0.36)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity le) {
            le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
        }
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (this.tickCount > LIFE) { this.discard(); return; }
        if (this.getTarget() != null && this.getTarget().isAlive()) {
            this.orbitTick++;
            double a = this.phase + this.orbitTick * 0.16;
            double tx = this.getTarget().getX() + Math.cos(a) * 1.5;
            double tz = this.getTarget().getZ() + Math.sin(a) * 1.5;
            double ty = this.getTarget().getEyeY() + 0.7;
            double dx = tx - this.getX(), dy = ty - this.getY(), dz = tz - this.getZ();
            double len = Math.max(0.2, Math.sqrt(dx * dx + dy * dy + dz * dz));
            var dm = this.getDeltaMovement();
            this.setDeltaMovement(dm.x + dx / len * 0.045, dm.y + dy / len * 0.05, dm.z + dz / len * 0.045);
        }
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "moth_ctrl", 2, this::predicate));
    }

    private PlayState predicate(AnimationState<AppleMothEntity> state) {
        state.setAnimation(FLY);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public double getTick(Object o) { return this.tickCount; }
}
