package net.farmingtales.ftboss.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
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

/**
 * 【青苹果】(2026-09-12 按用户要求新增): 苹果爵士的召唤物 · **挂 debuff**。
 *
 * <p>未成熟的青苹果, 咬一口又酸又涩: 命中叠 <b>中毒 + 饥饿 + 虚弱</b> 三连(时间都不长, 但会持续消耗玩家)。
 * <p>2026-09-12 改为**专属模型**(geo/green_apple.geo.json, 17 方块/9 骨骼): 会蹦的小青苹果, 不再复用答辩小怪。
 */
public class GreenAppleEntity extends Monster implements GeoAnimatable {

    public static final int LIFE = 600;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.green_apple.hop");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GreenAppleEntity(EntityType<? extends GreenAppleEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 1.5)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.FOLLOW_RANGE, 30.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** 命中挂 debuff(这是它的定位: 不疼, 但很烦) */
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity le) {
            le.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 0, false, true, true));
            le.addEffect(new MobEffectInstance(MobEffects.HUNGER, 160, 1, false, true, true));
            le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0, false, true, true));
        }
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount > LIFE) this.discard();
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "green_apple_ctrl", 4, this::predicate));
    }

    private PlayState predicate(AnimationState<GreenAppleEntity> state) {
        state.setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public double getTick(Object o) { return this.tickCount; }
}
