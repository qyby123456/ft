package net.farmingtales.ftboss.entity;

import net.minecraft.world.entity.EntityType;
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
 * 【苍蝇】(2026-09-12, 炫彩答辩超人的召唤物): 无重力飞行的小飞虫, 贴身骚扰玩家。
 * 血量很低但飞得快、会绕圈, 咬到玩家只**刷新**「屎？！！」的时长(不加层, 见 {@code BossHandler})。
 * 30 秒后自行消散。
 */
public class FlyMinionEntity extends Monster implements GeoAnimatable {

    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("animation.fly_minion.fly");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /** 每只苍蝇的绕圈相位, 让一群苍蝇不会叠在同一个点上 */
    private final double orbitPhase = this.random.nextDouble() * Math.PI * 2.0;
    private int orbitTick = 0;

    public FlyMinionEntity(EntityType<? extends FlyMinionEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.ATTACK_DAMAGE, 1.5)
                .add(Attributes.MOVEMENT_SPEED, 0.40)
                .add(Attributes.FLYING_SPEED, 0.40)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.35, true));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (this.tickCount > 600) {
            this.discard();       // 30s 后自行消散
            return;
        }
        /* 绕圈骚扰: 目标贴近时加一点切向速度, 看起来像苍蝇在玩家身边打转而不是直线撞上来 */
        if (this.getTarget() != null && this.getTarget().isAlive()) {
            this.orbitTick++;
            double a = this.orbitPhase + this.orbitTick * 0.15;
            double r = 1.6;
            double tx = this.getTarget().getX() + Math.cos(a) * r;
            double tz = this.getTarget().getZ() + Math.sin(a) * r;
            double ty = this.getTarget().getEyeY() + 0.6;
            double dx = tx - this.getX(), dy = ty - this.getY(), dz = tz - this.getZ();
            double len = Math.max(0.2, Math.sqrt(dx * dx + dy * dy + dz * dz));
            net.minecraft.world.phys.Vec3 dm = this.getDeltaMovement();
            this.setDeltaMovement(dm.x + dx / len * 0.045, dm.y + dy / len * 0.05, dm.z + dz / len * 0.045);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "fly_ctrl", 2, this::predicate));
    }

    private PlayState predicate(AnimationState<FlyMinionEntity> state) {
        state.setAnimation(FLY);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object o) {
        return this.tickCount;
    }
}
