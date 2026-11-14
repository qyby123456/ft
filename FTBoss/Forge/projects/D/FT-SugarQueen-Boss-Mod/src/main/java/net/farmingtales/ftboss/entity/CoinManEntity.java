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
 * 【金币人】(2026-09-12 按用户要求新增): 苹果爵士的召唤物 · **控制减速**。
 *
 * <p>用金币堆起来的小人, 撞上来会把人"黏住": 命中叠 <b>缓慢 III + 挖掘疲劳 II</b>,
 * 并有短促的 <b>定身</b>(缓慢 VI 持续 0.75 秒 ≈ 定在原地) —— 配合 Boss 的冲撞自爆很致命。
 * <p>几何/动画复用姜饼小兵(geo/gingerbread_minion.*), 只换一张**金币**贴图, 所以会走会打。
 */
public class CoinManEntity extends Monster implements GeoAnimatable {

    public static final int LIFE = 800;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.gingerbread_minion.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.gingerbread_minion.walk");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.gingerbread_minion.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.gingerbread_minion.death");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public CoinManEntity(EntityType<? extends CoinManEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 2.5)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** 命中: 控制 + 减速(定位是"抓人", 不是"打伤害") */
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity le) {
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 2, false, true, true));  // 缓慢 III
            le.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 160, 1, false, true, true));      // 挖掘疲劳 II
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15, 6, false, false, true)); // 定身 0.75s
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
        registrar.add(new AnimationController<>(this, "coin_man_ctrl", 3, this::predicate));
    }

    private PlayState predicate(AnimationState<CoinManEntity> state) {
        if (this.isDeadOrDying()) { state.setAnimation(DEATH); return PlayState.CONTINUE; }
        if (this.hurtTime > 0) { state.setAnimation(HURT); return PlayState.CONTINUE; }
        state.setAnimation(state.isMoving() ? WALK : IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public double getTick(Object o) { return this.tickCount; }
}
