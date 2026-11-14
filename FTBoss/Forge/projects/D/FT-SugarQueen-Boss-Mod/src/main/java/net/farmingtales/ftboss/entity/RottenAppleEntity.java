package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
 * 【烂苹果堆】(2026-09-12): 苹果爵士的召唤物 · 缓慢逼近, **死亡时炸开**。
 * 几何/动画复用大便小怪的资产(geo/poop_minion.*), 只换**腐烂红褐**贴图。
 * 炸开时半径内玩家吃伤害 + 中毒, 并喷一团腐气。
 */
public class RottenAppleEntity extends Monster implements GeoAnimatable {

    public static final int LIFE = 900;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.poop_minion.idle");
    private static final float BLAST_RADIUS = 1.1F;     // 2.2 → ×0.5(v77)
    private static final double HURT_RANGE = 3.4;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean blasted = false;

    public RottenAppleEntity(EntityType<? extends RottenAppleEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.FOLLOW_RANGE, 30.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (this.tickCount > LIFE) { this.discard(); return; }
        /* 靠近玩家时自己引爆(腐气) */
        if (this.getTarget() != null && this.distanceToSqr(this.getTarget()) < 6.25) this.blast();
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.blast();
    }

    private void blast() {
        if (this.level().isClientSide || this.blasted) return;
        this.blasted = true;
        double x = this.getX(), y = this.getY() + 0.3, z = this.getZ();
        this.level().explode(this, x, y, z, BLAST_RADIUS, Level.ExplosionInteraction.NONE);
        this.level().playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.9F, 1.3F);
        Fx.particles(this.level(), x, y, z, 22, ParticleTypes.ITEM_SLIME, 1.1, 0.12);
        Fx.particles(this.level(), x, y, z, 12, ParticleTypes.LARGE_SMOKE, 0.9, 0.06);
        Fx.particles(this.level(), x, y, z, 10, ParticleTypes.SNEEZE, 0.8, 0.05);
        for (Player p : this.level().players()) {
            if (!p.isAlive() || p.distanceToSqr(x, y, z) > HURT_RANGE * HURT_RANGE) continue;
            p.hurt(this.damageSources().mobAttack(this), 3.0F);   // 6 → ×0.5(v77)
            p.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 0, false, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 140, 0, false, true, true));
        }
        if (!this.isDeadOrDying()) this.discard();   // 自爆时才移除; 死亡流程里交给原版处理
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "rotten_ctrl", 6, this::predicate));
    }

    private PlayState predicate(AnimationState<RottenAppleEntity> state) {
        state.setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public double getTick(Object o) { return this.tickCount; }
}
