package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.effect.FTEffects;
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
 * 【巨型果蛆】(2026-09-12 按用户要求新增): 苹果爵士召来的**极大果蛆**。
 *
 * <p>模型复用蛆小怪(geo/maggot_minion.geo.json)但在渲染器里**放大 3.5 倍**, 贴图用苹果果肉色 ——
 * 所以它看起来就是一条巨大的果肉蛆。
 *
 * <p>咬中玩家叠三重负面(全是"恶心"向的):
 * <ul>
 *   <li><b>反胃</b>(CONFUSION) —— 视角乱晃;</li>
 *   <li><b>饥饿</b>(HUNGER) —— 吃不了东西;</li>
 *   <li><b>【感染】</b>(本模组自定义) —— 攻击敌人时被反噬, 可叠层。</li>
 * </ul>
 * 另外每 2 秒对 6 格内玩家施加一次饥饿(靠近它就难受)。
 */
public class GiantGrubEntity extends Monster implements GeoAnimatable {

    /** 这类蛆同时只允许存在 1 条, 由召唤处判定 */
    public static final int MAX_ALIVE = 1;
    private static final int INFECTION_STACK_TICKS = 600;
    private static final RawAnimation CRAWL = RawAnimation.begin().thenLoop("animation.maggot.crawl");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GiantGrubEntity(EntityType<? extends GiantGrubEntity> type, Level level) {
        super(type, level);
        this.xpReward = 30;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 180.0)
                .add(Attributes.ATTACK_DAMAGE, 4.5)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** 咬中: 反胃 + 饥饿 + 叠 2 层【感染】 */
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity le) {
            le.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 220, 0, false, true, true));
            le.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 1, false, true, true));
            MobEffectInstance cur = le.getEffect(FTEffects.INFECTION.get());
            int have = cur == null ? 0 : cur.getAmplifier() + 1;
            int want = Math.min(5, have + 2);
            le.addEffect(new MobEffectInstance(FTEffects.INFECTION.get(), INFECTION_STACK_TICKS,
                    want - 1, false, true, true));
            FTBossMod.LOGGER.info("[FTBoss][Apple] giant grub bit {} -> nausea/hunger/infection x{}",
                    le.getName().getString(), want);
        }
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        /* 每 2 秒: 6 格内玩家挂饥饿(靠近它就难受) */
        if (this.tickCount % 40 == 0) {
            for (Player p : this.level().players()) {
                if (p.isAlive() && p.distanceToSqr(this) <= 36.0) {
                    p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0, false, true, true));
                }
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "giant_grub_ctrl", 5, this::predicate));
    }

    private PlayState predicate(AnimationState<GiantGrubEntity> state) {
        state.setAnimation(CRAWL);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public double getTick(Object o) { return this.tickCount; }
}
