package net.farmingtales.ftboss.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
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
 * **酒桶王的分身**(2026-09-12 按用户要求"二阶段召唤, 使用和他一样的模型")。
 *
 * <p>与 {@link WineBarrelBossEntity} 的区别: **没有任何 Boss 逻辑** —— 不建 Boss 血条({@code BossBars})、
 * 不占战斗 BGM、不进怜悯清场、不参与阶段/战术; 只有**同一套模型** + 1 滴血 + 不攻击不移动(纯诱饵)。
 */
public class WineBarrelDecoyEntity extends Monster implements GeoAnimatable {

    /* v112 修复: 原写的是 animation.wine_barrel.idle —— wine_barrel_boss.animation.json 里根本没有这个动画,
       该类只注册了这一个控制器, 结果酒桶王的分身永远定格静止。正确的名字是 animation.barrel_boss.idle。 */
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.barrel_boss.idle");
    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache(this);

    public WineBarrelDecoyEntity(EntityType<? extends WineBarrelDecoyEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setNoAi(true);                 // 不攻击、不移动
        this.setPersistenceRequired();      // 不自然消失
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "decoy_ctrl", 8, this::predicate));
    }

    private PlayState predicate(AnimationState<WineBarrelDecoyEntity> state) {
        state.setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geckoCache;
    }

    @Override
    public double getTick(Object o) {
        return this.tickCount;
    }
}
