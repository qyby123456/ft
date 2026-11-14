package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

/**
 * 【苹果爵士·分身】(2026-09-12 按用户要求): 参照酒桶王的分身思路, 但这个分身**会冲撞自爆**。
 *
 * <p>与 {@link WineBarrelDecoyEntity}(纯站桩诱饵)的区别: 分身有 AI —— 追人 -> 加速冲撞 -> **贴到就炸**
 * (半径 5 格, 基准 28 点并随距离衰减)。被打死也会炸, 但威力只有一半(半径 3.5 / 基准 14),
 * 所以"远程点掉"比"贴脸放它炸"划算。
 *
 * <p>不占 Boss 血条、不参与 BGM/阶段/战利品: 它只是个会跑的炸弹。寿命 14 秒, 到点自爆。
 */
public class AppleCloneEntity extends Monster implements GeoAnimatable {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.apple_boss.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.apple_boss.walk");
    private static final RawAnimation ROLL = RawAnimation.begin().thenPlay("animation.apple_boss.roll_charge");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.apple_boss.death");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int chargeCd = 15 + (int) (Math.random() * 20);
    private int chargeWindup = 0;      // >0 蓄力中
    private int dashT = 0;             // >0 冲刺中
    private double dashX = 0, dashZ = 0;
    private int life = 0;
    private boolean exploded = false;

    public AppleCloneEntity(EntityType<? extends AppleCloneEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new Chase());
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** 只走位不出手: 攻击手段就是最后那一下自爆 */
    private class Chase extends Goal {
        Chase() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }
        @Override public boolean canUse() {
            LivingEntity t = AppleCloneEntity.this.getTarget();
            return t != null && t.isAlive() && AppleCloneEntity.this.dashT <= 0 && AppleCloneEntity.this.chargeWindup <= 0;
        }
        @Override public boolean canContinueToUse() { return this.canUse(); }
        @Override public void tick() {
            LivingEntity t = AppleCloneEntity.this.getTarget();
            if (t == null) return;
            AppleCloneEntity.this.getLookControl().setLookAt(t, 30.0F, 30.0F);
            if (AppleCloneEntity.this.distanceToSqr(t) > 4.0) AppleCloneEntity.this.getNavigation().moveTo(t, 1.25);
            else AppleCloneEntity.this.getNavigation().stop();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        final ServerLevel sl = this.level() instanceof ServerLevel s ? s : null;
        this.life++;

        /* 寿命到点自爆(免得分身留在场上当装饰) */
        if (this.life > 280) { this.detonate(sl, false); return; }

        if (this.chargeWindup > 0) {
            if (--this.chargeWindup == 0) {
                LivingEntity t = this.getTarget();
                if (t != null) { this.dashX = t.getX(); this.dashZ = t.getZ(); }
                this.dashT = 30;
                if (sl != null) sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.0f, 1.4f);
            }
            return;
        }
        if (this.dashT > 0) {
            this.dashT--;
            double dx = this.dashX - this.getX(), dz = this.dashZ - this.getZ();
            double dl = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
            this.setPos(this.getX() + dx / dl * 0.95, this.getY(), this.getZ() + dz / dl * 0.95);
            if (sl != null) Fx.particles(sl, this.getX(), this.getY() + 0.3, this.getZ(), 1, ParticleTypes.END_ROD, 0.7, 0.05);
            for (Player p : this.level().players()) {
                if (p.isAlive() && p.distanceToSqr(this) < 6.25) { this.detonate(sl, false); return; }
            }
            if (this.dashT <= 0 || dl < 1.2) this.detonate(sl, false);
            return;
        }
        if (this.getTarget() != null && this.getTarget().isAlive() && --this.chargeCd <= 0) {
            this.chargeCd = 60 + (int) (Math.random() * 40);
            this.chargeWindup = 8;
        }
    }

    /** 自爆: onDeath=true 时是"被打死"的弱化版 */
    private void detonate(ServerLevel sl, boolean onDeath) {
        if (this.exploded) return;
        this.exploded = true;
        double R = onDeath ? 3.5 : 5.0;
        float base = onDeath ? 14.0f : 28.0f;
        double x = this.getX(), y = this.getY() + 0.7, z = this.getZ();
        if (sl != null) {
                        /* 2026-09-12 按用户要求: 改为**纯视觉爆炸** —— 不再调用 sl.explode(),
               因为它会摧毁掉在地上的凋落物, 还会打死旁边的分身(殉爆);
               伤害与击退本来就是我们自己逐玩家结算的。 */
            net.farmingtales.ftboss.network.AppleBlastNetwork.show(sl, x, y, z, R);
            sl.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.6f, 0.75f);
            for (Player p : sl.players()) {
                if (!p.isAlive()) continue;
                double d = Math.sqrt(p.distanceToSqr(x, y, z));
                if (d > R) continue;
                p.hurt(this.damageSources().explosion(this, this), (float) (base * 0.5 * Math.max(0.25, 1.0 - d / R)));   // ×0.5(v77)
                net.farmingtales.ftboss.util.BossKnockback.knockback(p, 1.3, x - p.getX(), z - p.getZ());
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] clone detonated (death={})", onDeath);
        }
        this.discard();
    }

    @Override
    public void die(DamageSource source) {
        if (this.level() instanceof ServerLevel sl) this.detonate(sl, true);
        super.die(source);
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "apple_clone_ctrl", 4, this::predicate));
    }

    private PlayState predicate(AnimationState<AppleCloneEntity> state) {
        if (this.isDeadOrDying()) { state.setAnimation(DEATH); return PlayState.CONTINUE; }
        if (this.dashT > 0 || this.chargeWindup > 0) { state.setAnimation(ROLL); return PlayState.CONTINUE; }
        state.setAnimation(state.isMoving() ? WALK : IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public double getTick(Object o) { return this.tickCount; }
}
