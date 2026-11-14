package net.farmingtales.ftboss.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
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

import java.util.UUID;

/**
 * 【benger 的小狗】—— 犬王"呼朋引伴"喊来的跟班（2026-09-14 v127）。
 *
 * <p>与 {@link BengerEntity} **共用同一套 geo / 动画 / 贴图**，渲染时缩到 0.55 倍（见 {@code FTClientSetup}），
 * 碰撞箱 0.9 x 1.6。会咬人（挂【流血】）、血量 {@value #MAX_HP}，活 {@value #LIFE} tick 后自己退场
 * （免得打完之后一地小狗跟着玩家跑）。不掉落、不给经验。
 */
public class BengerPupEntity extends Monster implements GeoAnimatable {

    public static final float MAX_HP = 30.0f;
    public static final int LIFE = 45 * 20;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.benger.idle");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.benger.run");
    private static final RawAnimation BITE = RawAnimation.begin().thenPlay("animation.benger.bite");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.benger.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.benger.death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int act = 0, actionTicks = 0;
    private UUID owner;
    /* v128 狼群扑击：被 Boss 锁定后, 到点与全体一起扑向玩家 */
    private int packAt = -1;
    private int packImpactIn = -1;
    private double packX, packZ;
    private double packRadius = 3.5;
    private float packDamage = 24.0f;

    /** Boss 的"狼群扑击"：锁定同一个玩家，delay 之后一起起飞 */
    public void packPounce(net.minecraft.world.entity.player.Player target, int delay, double radius, float damage) {
        if (target == null) return;
        this.packAt = this.tickCount + delay;
        this.packImpactIn = -1;
        this.packRadius = radius;
        this.packDamage = damage;
        net.minecraft.world.phys.Vec3 d = new net.minecraft.world.phys.Vec3(
                target.getX() - this.getX(), 0.0, target.getZ() - this.getZ());
        if (d.lengthSqr() < 1.0E-4) d = new net.minecraft.world.phys.Vec3(0, 0, 1);
        d = d.normalize();
        this.packX = d.x; this.packZ = d.z;
        this.setTarget(target);
    }

    public BengerPupEntity(EntityType<? extends BengerPupEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HP)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.50)
                .add(Attributes.FOLLOW_RANGE, 52.0);
    }

    public void setOwner(UUID id) { this.owner = id; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.6, true));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0f));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.WOLF_PANT; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.WOLF_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.WOLF_DEATH; }
    @Override protected float getSoundVolume() { return 0.7f; }
    @Override public float getVoicePitch() { return 1.35f; }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    @Override
    public void tick() {
        super.tick();
        if (this.actionTicks > 0 && --this.actionTicks <= 0) this.act = 0;
        if (this.isDeadOrDying()) this.act = 4;
        if (!this.level().isClientSide && this.tickCount > LIFE) this.discard();
        /* v128 狼群扑击：到点起飞 → 8 tick 后落地结算 */
        if (!this.level().isClientSide) {
            if (this.packAt > 0 && this.tickCount >= this.packAt) {
                this.packAt = -1;
                this.packImpactIn = 8;
                this.setDeltaMovement(this.packX * 1.0, 0.5, this.packZ * 1.0);
                this.hasImpulse = true;
                this.act = 2; this.actionTicks = 14;          // 播飞扑动作
            }
            if (this.packImpactIn > 0 && --this.packImpactIn <= 0) {
                if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) this.packImpact(sl);
            }
        }
    }

    /** 落地：小范围 AOE（和 Boss 一起扑，伤害低一些但会被围殴） */
    private void packImpact(net.minecraft.server.level.ServerLevel sl) {
        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,
                this.getX(), this.getY() + 0.2, this.getZ(), 10, 0.8, 0.15, 0.8, 0.04);
        int hits = 0;
        for (net.minecraft.world.entity.player.Player p : sl.players()) {
            if (!p.isAlive() || p.isSpectator()) continue;
            if (p.distanceToSqr(this) > this.packRadius * this.packRadius) continue;
            p.invulnerableTime = 0;
            p.hurt(this.damageSources().mobAttack(this), this.packDamage * 0.5f);
            net.farmingtales.ftboss.boss.ai.ExtraDebuffs.bleed(p, 4 * 20, 0);
            hits++;
        }
        if (hits > 0) {
            net.farmingtales.ftboss.FTBossMod.LOGGER.info("[FTBoss][benger] 小狗扑击命中 {} 人", hits);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        /* 只让玩家能打（和悬浮/传送型实体同一条理由：避免环境伤害把它们白掉） */
        if (!(source.getEntity() instanceof Player) && source.getEntity() != null) return false;
        boolean hit = super.hurt(source, amount);
        if (hit && !this.level().isClientSide && this.act == 0 && this.getRandom().nextFloat() < 0.7f) {
            this.act = 3; this.actionTicks = 8;
        }
        return hit;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity le) {
            net.farmingtales.ftboss.boss.ai.ExtraDebuffs.bleed(le, 5 * 20, 0);           // 流血
            if (le instanceof net.minecraft.world.entity.player.Player pl1) net.farmingtales.ftboss.boss.ai.BleedLink.link(pl1, this);
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0));
            /* v130 按用户要求：护甲削弱 + 肌腱断裂（armamentarium 的两个减益, 缺 mod 自动跳过） */
            debuff(le, "vulnerable", 200, 0);
            debuff(le, "clipped", 200, 0);
            if (!this.level().isClientSide) { this.act = 2; this.actionTicks = 10; }
        }
        return hit;
    }

    /** 挂一个 armamentarium 减益（先摘旧实例再加, 出事只记日志 —— 见 AGENTS.md 那条"扫注册表挂效果"的规矩） */
    private static void debuff(net.minecraft.world.entity.LivingEntity target, String path, int ticks, int amp) {
        try {
            var eff = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS
                    .getValue(new net.minecraft.resources.ResourceLocation("armamentarium", path));
            if (eff == null) return;
            if (target.hasEffect(eff)) target.removeEffect(eff);
            target.addEffect(new MobEffectInstance(eff, ticks, amp, false, false, true));
        } catch (Throwable t) {
            net.farmingtales.ftboss.FTBossMod.LOGGER.warn("[FTBoss][benger] 小狗减益 {} 施加失败: {}", path, t.toString());
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.owner != null) tag.putUUID("bengerOwner", this.owner);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("bengerOwner")) this.owner = tag.getUUID("bengerOwner");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "pup_ctrl", 3, this::predicate));
    }

    private PlayState predicate(AnimationState<BengerPupEntity> state) {
        if (this.act == 4 || this.isDeadOrDying()) { state.setAnimation(DEATH); return PlayState.CONTINUE; }
        if (this.act == 2) { state.setAnimation(BITE); return PlayState.CONTINUE; }
        if (this.act == 3) { state.setAnimation(HURT); return PlayState.CONTINUE; }
        state.setAnimation(state.isMoving() ? RUN : IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.geoCache; }

    @Override
    public double getTick(Object o) { return this.tickCount; }
}
