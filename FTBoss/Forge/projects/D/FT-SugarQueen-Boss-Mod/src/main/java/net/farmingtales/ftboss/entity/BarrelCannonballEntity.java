package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 酒桶炮弹(2026-09-10 深夜按用户要求新增): 酒桶王偶尔抛射若干颗会爆炸的炮弹。
 *
 * <p><b>模型</b>: 直接复用酒桶王自己的模型(巨桶 geo + 贴图), 渲染时按 0.18 缩放 + 翻滚 —— 一颗滚圆的酒桶飞过来,
 * 不需要新美术资源({@link net.farmingtales.ftboss.client.render.BarrelCannonballRenderer})。
 *
 * <p><b>伤害</b>: 爆炸威力 {@value #EXPLOSION_POWER} = **闪电苦力怕**(原版带电苦力怕的爆炸半径就是 6)。
 * 但用 {@link Explosion.BlockInteraction#NONE} → **只伤实体, 不毁方块**(避免把场地炸平)。
 * 直接命中实体时会额外吃一发直击伤害。
 */
public class BarrelCannonballEntity extends Projectile implements GeoAnimatable {

    /** GeckoLib 要求(渲染器是 GeoEntityRenderer, 泛型上界是 Entity & GeoAnimatable); 不需要动画控制器 */
    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache(this);

    /** 爆炸威力: 与闪电(带电)苦力怕一致 */
    public static final float EXPLOSION_POWER = 3.0F;   // 6 → ×0.5(v77; 爆炸伤害近似线性)
    /** 本发的爆炸威力。默认就是 {@link #EXPLOSION_POWER}；柠檬猫继承来的炮弹会被调高（v159）。 */
    private float explosionPower = EXPLOSION_POWER;
    /** 直击附加伤害 */
    private static final float DIRECT_DAMAGE = 4.0F;    // 8 → ×0.5(v77)
    /** 最长存活(4 秒) */
    private static final int MAX_LIFE = 80;

    private boolean exploded = false;

    public BarrelCannonballEntity(EntityType<? extends BarrelCannonballEntity> type, Level level) {
        super(type, level);
    }

    public BarrelCannonballEntity(Level level, LivingEntity owner) {
        super(FTEntities.BARREL_CANNONBALL.get(), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getY() + 1.6, owner.getZ());
    }

    @Override
    protected void defineSynchedData() {
        // 注意: 这里**不能**调 super —— Projectile 没有实现 defineSynchedData(), 父级是 Entity 的抽象方法,
        // 写了会编译报错 "abstract method ... cannot be accessed directly"。弹体没有额外的同步字段, 留空即可。
    }

    // ---- GeckoLib ----

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        // 炮弹不需要骨骼动画(翻滚在渲染器里做)
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geckoCache;
    }

    @Override
    public double getTick(Object o) {
        return this.tickCount;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.exploded = tag.getBoolean("ftboss_exploded");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("ftboss_exploded", this.exploded);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    /**
     * 本发的爆炸威力（默认 {@link #EXPLOSION_POWER}）。
     *
     * <p>v159：柠檬猫继承来的炮弹要按"继承技能 ×5"放大，而**真身酒桶王射出的炮弹必须保持原样** ——
     * 同一个实体类型两边都在用，所以只能逐发设置。
     */
    public void setExplosionPower(float power) {
        if (Float.isFinite(power) && power > 0.0F) this.explosionPower = power;
    }

    /** 不打自己人(其它炮弹/发射者本体) */
    @Override
    protected boolean canHitEntity(Entity e) {
        if (e instanceof BarrelCannonballEntity) return false;
        Entity owner = this.getOwner();
        if (owner != null && e == owner) return false;
        /* v123: **只有酒桶王自己射出来的**才不打酒桶王 ——
           玩家用【酒桶弩】射出来的那几颗必须能打中酒桶王本体(否则会直接穿过去)。 */
        if (e instanceof WineBarrelBossEntity && owner instanceof WineBarrelBossEntity) return false;
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.exploded) return;

        // 抛物线(重力 0.045/t)
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y - 0.045, motion.z);
        Vec3 next = this.position().add(this.getDeltaMovement());

        if (!this.level().isClientSide) {
            // ① 撞方块
            BlockHitResult blockHit = this.level().clip(new ClipContext(
                    this.position(), next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (blockHit.getType() != HitResult.Type.MISS) {
                this.setPos(blockHit.getLocation().x, blockHit.getLocation().y, blockHit.getLocation().z);
                this.explode();
                return;
            }
            // ② 撞实体
            for (Entity e : this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(0.5))) {
                if (e.isAlive() && this.canHitEntity(e)) {
                    if (e instanceof LivingEntity le) {
                        /* v159: 柠檬猫继承来的炮弹，直击伤害也按"继承技能 ×5"放大（真身酒桶王的不变）。 */
                        float direct = net.farmingtales.ftboss.boss.ai.LemonInheritedAbilityRules.inherited(this)
                                ? net.farmingtales.ftboss.boss.ai.LemonInheritedAbilityRules.damage(DIRECT_DAMAGE)
                                : DIRECT_DAMAGE;
                        le.hurt(this.damageSources().thrown(this, this.getOwner()), direct);
                    }
                    this.explode();
                    return;
                }
            }
        }

        this.setPos(next.x, next.y, next.z);
        if (!this.level().isClientSide && (this.tickCount > MAX_LIFE || this.onGround())) {
            this.explode();
            return;
        }
        // 尾焰
        if (this.level().isClientSide) {
            Fx.particles(this.level(), this.getX(), this.getY(), this.getZ(), 3, ParticleTypes.FLAME, 0.08, 0.01);
        }
    }

    /** 爆炸: 威力=闪电苦力怕, 但不破坏方块 */
    private void explode() {
        if (this.exploded) return;
        this.exploded = true;
        if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.explode(this, this.getX(), this.getY(), this.getZ(),
                    this.explosionPower, Level.ExplosionInteraction.NONE);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 2.0f, 0.9f);
            Fx.particles(sl, this.getX(), this.getY(), this.getZ(), 25, ParticleTypes.EXPLOSION, 1.0, 0.1);
            FTBossMod.LOGGER.info("[FTBoss] 酒桶炮弹爆炸 @({}, {}, {}) 威力={}",
                    String.format("%.1f", this.getX()), String.format("%.1f", this.getY()),
                    String.format("%.1f", this.getZ()), this.explosionPower);
        }
        this.discard();
    }
}
