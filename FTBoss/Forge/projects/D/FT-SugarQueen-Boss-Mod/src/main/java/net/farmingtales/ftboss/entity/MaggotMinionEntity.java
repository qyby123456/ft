package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
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
 * 【！?区区？！】—— 2026-09-11 按用户要求新增的召唤小怪。
 *
 * <ul>
 *   <li>外观: 蛆(7 段锥形身体 + 蠕动动画);</li>
 *   <li>每次攻击玩家 **4 点伤害**(ATTACK_DAMAGE = 4.0 = 设计值 8 × 0.5 <b>全局烘焙</b>, v77);</li>
 *   <li>每次命中给玩家叠一层 **【感染】**(可叠加, 上限 {@value #MAX_STACKS} 层, 每层 30 秒);</li>
 *   <li>感染生效期间: **玩家攻击敌人时会受到"层数"点伤害**(见 BossHandler.onPlayerAttackGrubCurse);</li>
 *   <li>寿命 {@value #LIFE}t 后自行消散, 归属发起召唤的 Boss(便于怜悯清场一起带走)。</li>
 * </ul>
 */
public class MaggotMinionEntity extends Monster implements GeoAnimatable {

    /** 存活时间(30 秒) */
    public static final int LIFE = 600;
    /** 感染最大层数 */
    public static final int MAX_STACKS = 10;
    /** 每层持续时间(tick) */
    public static final int STACK_TICKS = 600;

    private static final RawAnimation CRAWL = RawAnimation.begin().thenLoop("animation.maggot.crawl");

    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerId;
    private int life = LIFE;

    public MaggotMinionEntity(EntityType<? extends MaggotMinionEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)      // 设计 8 × 0.5(v77 全局烘焙) = 实际 4
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();      // ★ 必须调 super(LivingEntity 规则)
    }

    public void setOwnerBoss(DefectCoreEntity boss) {
        this.ownerId = boss.getUUID();
    }

    public boolean isOwnedByUuid(UUID id) {
        return this.ownerId != null && this.ownerId.equals(id);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.ownerId != null) tag.putUUID("owner", this.ownerId);
        tag.putInt("life", this.life);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("owner")) this.ownerId = tag.getUUID("owner");
        this.life = tag.contains("life") ? tag.getInt("life") : LIFE;
    }

    /** 命中: 8 点伤害由属性结算, 这里额外叠一层【感染】 */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity le && this.level() instanceof ServerLevel sl) {
            MobEffectInstance cur = le.getEffect(FTEffects.INFECTION.get());
            int stacks = cur == null ? 0 : cur.getAmplifier() + 1;
            int want = Math.min(stacks + 1, MAX_STACKS);
            le.addEffect(new MobEffectInstance(FTEffects.INFECTION.get(), STACK_TICKS, want - 1, false, true, true));
            sl.playSound(null, le.getX(), le.getY(), le.getZ(), SoundEvents.HONEY_BLOCK_SLIDE,
                    SoundSource.HOSTILE, 1.0f, 1.4f);
            Fx.particles(sl, le.getX(), le.getY() + 1.0, le.getZ(), 8, ParticleTypes.ITEM_SLIME, 0.3, 0.05);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide) return false;
        if (!(source.getEntity() instanceof net.minecraft.server.level.ServerPlayer)) return false;  // 只吃玩家伤害
        return super.hurt(source, amount);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (--this.life <= 0) { this.discard(); return; }        // 寿命到, 自己化掉
        if (this.life % 20 == 0 && this.level() instanceof ServerLevel sl) {
            Fx.particles(sl, this.getX(), this.getY() + 0.2, this.getZ(), 1, ParticleTypes.ITEM_SLIME, 0.15, 0.01);
        }
    }

    // ================= GeckoLib =================
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "maggot_ctrl", 4, this::predicate));
    }

    private PlayState predicate(AnimationState<MaggotMinionEntity> state) {
        state.setAnimation(CRAWL);
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
