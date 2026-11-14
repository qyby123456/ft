package net.farmingtales.ftboss.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * 【折耳根的紫色残影】(2026-09-24 按用户要求)。
 *
 * <p>用户原话：「独立做一个折耳根人形紫色残影的模型来达成效果，虚影本身无碰撞体积和攻击性」。
 *
 * <p>所以这是一个**纯视觉实体**，除了"活够 tick 就消散"之外什么都不做：
 *
 * <ul>
 *   <li><b>无碰撞体积</b>：{@code noPhysics = true} + 无重力 + 不可推 + 不可被碰撞
 *       （{@link #isPushable()} / {@link #canBeCollidedWith()} / {@link #push(Entity)} 全部关掉），
 *       玩家和生物可以直接穿过去，不会被它挡住；</li>
 *   <li><b>不可被选中/伤害</b>：{@link #isPickable()} / {@link #isAttackable()} 关掉 +
 *       {@link #hurt} 恒 false + {@link #isInvulnerableTo} 恒真 —— 它连靶子都不是；</li>
 *   <li><b>无攻击性</b>：不注册任何 AI goal（{@link #registerGoals()} 空实现）、
 *       {@link #canAttack(LivingEntity)} 与 {@link #doHurtTarget(Entity)} 恒 false，
 *       也没有目标选择器 —— 它不会追人、不会攻击、不会反击。</li>
 * </ul>
 *
 * <p>技能的"复现"**不在这个实体里**：残影只是本体留下的影子，
 * 到点时由**折耳根本体**（{@code EasterBossEntity.firePhantomEcho}）结算那一次命中效果 ——
 * 伤害归属仍是折耳根，所以它照样吃 {@code BossOutgoingDamageNerfs} 的折耳根倍率，
 * 不会因为"换了个实体打的"而突然变疼。见 {@code EasterBossEntity} 的虚影一节。
 */
public class ShadowPhantomEntity extends PathfinderMob {
    /** 默认寿命（本体生成时按 PHANTOM_DELAY + PHANTOM_LINGER 覆盖）。 */
    public static final int DEFAULT_LIFE = 30;
    /** 残影的碰撞箱尺寸用不到（无碰撞），但实体类型注册需要一个尺寸；给个瘦小的值。 */
    public static final float WIDTH = 0.6F;
    public static final float HEIGHT = 1.8F;

    private int life = DEFAULT_LIFE;

    public ShadowPhantomEntity(EntityType<? extends ShadowPhantomEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;          // 无碰撞体积：自己不与方块/实体结算碰撞
        this.setNoGravity(true);        // 残影停在原地，不掉下去
        this.setSilent(true);
        this.setInvulnerable(true);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        /* ★ 2026-09-24：`ATTACK_DAMAGE` 虽然**永远用不到**（残影 canAttack/doHurtTarget 恒 false），
           但本模组的启动自检会对每个注册生物要求它 —— 缺了会打一条 ERROR：
           「属性表缺 ATTACK_DAMAGE —— 它一近战就会崩服」。所以照规矩补 0。 */
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    /** 寿命（tick）。由本体在生成时设置。 */
    public void setLife(int ticks) { this.life = Math.max(1, ticks); }

    public int getLife() { return this.life; }

    /* ==================== 无攻击性 ==================== */

    /** 不注册任何 AI：不追人、不攻击、不躲避、不环顾 —— 一个纯站桩的影子。 */
    @Override
    protected void registerGoals() { /* 故意留空 */ }

    @Override
    public boolean canAttack(LivingEntity target) { return false; }

    @Override
    public boolean doHurtTarget(Entity target) { return false; }

    /* ==================== 无碰撞体积 / 不会移动 ==================== */

    @Override
    public boolean isPushable() { return false; }

    @Override
    public boolean canBeCollidedWith() { return false; }

    @Override
    public void push(Entity entity) { /* 推不动别人，也推不动它 */ }

    @Override
    protected void doPush(Entity entity) { /* 同上 */ }

    /** 击退一律无效：残影**不会移动**（连爆炸/技能推力都不吃）。 */
    @Override
    public void knockback(double strength, double x, double z) { /* 不移动 */ }

    @Override
    public void push(double x, double y, double z) { /* 不移动 */ }

    @Override
    public void move(net.minecraft.world.entity.MoverType type, net.minecraft.world.phys.Vec3 movement) {
        /* 除了生成时的落位（moveTo 直接写坐标），任何"移动"都不生效：残影钉在留下的位置。 */
        if (this.tickCount == 0) super.move(type, movement);
    }

    /* ==================== 不可被选中 / 不可被伤害 ==================== */

    /** 打不到它：射线检测/攻击判定直接穿过去。 */
    @Override
    public boolean isPickable() { return false; }

    @Override
    public boolean isAttackable() { return false; }

    @Override
    public boolean isInvulnerableTo(DamageSource source) { return true; }

    @Override
    public boolean hurt(DamageSource source, float amount) { return false; }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) { /* 什么都不掉 */ }

    /* ==================== 寿命 ==================== */

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (--this.life <= 0) this.discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PhantomLife", this.life);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("PhantomLife")) this.life = Math.max(1, tag.getInt("PhantomLife"));
    }
}
