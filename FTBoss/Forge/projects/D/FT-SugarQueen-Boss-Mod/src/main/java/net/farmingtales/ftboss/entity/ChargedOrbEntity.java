package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
 * 充能球(2026-09-11 新增): 故障机器人"召唤小怪 = 召唤充能球"的载体。
 *
 * <p>行为照沙耶的守护茶芽(吸收球)同构: 悬浮在 Boss 周围、可被玩家拆掉;
 * <b>到期(或被 Boss 主动吸收)时不自己结算, 而是把球交给 Boss 的
 * {@code DefectOrbs} 控制器 → Boss 吸收后释放该球对应的技能</b>。
 *
 * <ul>
 *   <li>存活到 {@value #LIFE}t → 交给 Boss 吸收并放技能;</li>
 *   <li>被玩家打掉 → 直接消散、**不激发**(玩家的反制窗口, 并让 Boss 短暂停补球);</li>
 *   <li>Boss 死亡 → 一起消散。</li>
 * </ul>
 *
 * <h3>运动方式(2026-09-11 按用户要求"除环绕外增加其他运动方式, 参考光女的撞击")</h3>
 * 每颗球是一个独立的状态机, 会自己换姿势, 不再只是"绕圈":
 * <ol>
 *   <li>{@link Move#ORBIT} <b>环绕</b> —— 每颗球有自己的半径(2.3~3.5)与上下摆幅, 不会叠成一圈;</li>
 *   <li>{@link Move#SPIRAL} <b>螺旋收束</b> —— 半径周期性外扩/内收 + 上下波浪, 像呼吸;</li>
 *   <li>{@link Move#WINDUP} <b>后撤蓄力</b> —— 往目标的反方向退开并抬升(撞击的<b>预警</b>窗口);</li>
 *   <li>{@link Move#DASH} <b>撞击</b> —— 沿直线高速冲过玩家(参考光女的冲撞): 命中 = 伤害 + 击退,
 *       撞到方块则弹回; 玻璃球撞击附带小范围溅射;</li>
 *   <li>{@link Move#RETURN} <b>回旋归位</b> —— 撞完飞回 Boss 身边, 回到环绕。</li>
 * </ol>
 * 进攻型球(玻璃/电/黑)撞击倾向高(50%), 防守型球(冰/离子)基本只环绕(16%);
 * <b>【回想形态】下撞击会"双撞"</b>(与法术双发同款待遇)。
 */
public class ChargedOrbEntity extends Monster implements GeoAnimatable {

    /** 存活时间(20 秒) */
    public static final int LIFE = 400;

    private static final RawAnimation IDLE_A = RawAnimation.begin().thenLoop("animation.orb.idle");
    private static final RawAnimation BURST_A = RawAnimation.begin().thenPlay("animation.orb.burst");
    private static final RawAnimation SUMMON_A = RawAnimation.begin().thenPlay("animation.orb.summon");
    private static final RawAnimation FOCUS_A = RawAnimation.begin().thenLoop("animation.orb.focus");
    private static final RawAnimation DASH_A = RawAnimation.begin().thenLoop("animation.orb.dash");
    private static final RawAnimation RETURN_A = RawAnimation.begin().thenLoop("animation.orb.return");
    private static final RawAnimation BEAM_A = RawAnimation.begin().thenLoop("animation.orb.beam");
    private static final RawAnimation DRAIN_A = RawAnimation.begin().thenPlay("animation.orb.drain");
    private static final int BIRTH_TICKS = 10;
    private static final int FADE_TICKS = 6;

    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache(this);

    /**
     * 球种**必须用同步数据**: 2026-09-11 修 —— 旧版只把 orbType 存在服务端字段/ NBT,
     * 客户端实体是凭生成包新建的, 拿不到这个值 → 所有球都按默认(玻璃球青蓝)渲染,
     * 表现就是"召唤的球没有颜色区分"。
     */
    private static final net.minecraft.network.syncher.EntityDataAccessor<String> DATA_TYPE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(ChargedOrbEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.STRING);

    private UUID ownerId;
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DATA_OWNER =
            net.minecraft.network.syncher.SynchedEntityData.defineId(ChargedOrbEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DATA_CHARGE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(ChargedOrbEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.nbt.CompoundTag> DATA_VISUAL =
            net.minecraft.network.syncher.SynchedEntityData.defineId(ChargedOrbEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.COMPOUND_TAG);
    private boolean reservedForCast;
    private boolean spentProjectile;
    private int focusDuration;
    private int focusTicks;
    private int fadeTicks;
    private Vec3 transferStart = Vec3.ZERO;
    private int formationIndex;
    private int formationCount = 5;
    private int renderedVisualSequence = -1;
    private boolean maturedRequested;

    public boolean isOrbAvailable() {
        return !this.gone && this.isAlive();
    }

    public boolean isReservedForCast() {
        return this.reservedForCast;
    }

    public boolean isSpentProjectile() {
        return this.spentProjectile;
    }

    public void setFormationSlot(int index, int count) {
        this.formationIndex = index;
        this.formationCount = Math.max(1, count);
        this.orbitPhase = index * Math.PI * 2.0 / this.formationCount;
        this.spin = 1;
    }

    public int formationSlot() {
        return this.formationIndex;
    }

    public void reserveForCast() {
        this.reservedForCast = true;
        this.maturedRequested = true;
    }

    public void focusForCast(int duration) {
        reserveForCast();
        this.focusDuration = duration;
        this.focusTicks = 0;
        this.transferStart = this.position();
        setVisualState("focus", duration);
    }

    public void releaseDark(int chargeTicks, float damageMultiplier, LivingEntity target) {
        this.reservedForCast = false;
        this.spentProjectile = true;
        this.maturedRequested = true;
        this.focusDuration = 0;
        this.charge = chargeTicks;
        this.entityData.set(DATA_CHARGE, chargeTicks);
        setSlamBonus(2.0f * (chargeTicks / 20.0f));
        setDamageMul(damageMultiplier);
        setBlackHoleChance(true);
        setLifeTicks(200);
        beginDash(target);
        setVisualState("dash", 28);
    }

    public String visualState() {
        return this.entityData.get(DATA_VISUAL).getString("state");
    }

    public float chargeFraction() {
        return Math.min(1.0f, this.entityData.get(DATA_CHARGE) / 640.0f);
    }

    private void setVisualState(String state, int duration) {
        if (this.level().isClientSide || state.equals(visualState())) return;
        var previous = this.entityData.get(DATA_VISUAL);
        var next = new net.minecraft.nbt.CompoundTag();
        next.putString("state", state);
        next.putLong("start", this.level().getGameTime());
        next.putInt("duration", duration);
        next.putInt("sequence", previous.getInt("sequence") + 1);
        this.entityData.set(DATA_VISUAL, next);
    }
    private double orbitPhase = 0.0;
    private int life = LIFE;
    /** 存活时间(= 黑球蓄力进度, 也用于排序"最老的球") */
    private int charge = 0;
    private boolean gone = false;

    // ================= 运动状态机(2026-09-11) =================

    /** 运动模式 */
    public enum Move {
        /** 环绕 */
        ORBIT,
        /** 螺旋收束 */
        SPIRAL,
        /** 后撤蓄力(撞击预警) */
        WINDUP,
        /** 撞击(直线冲撞) */
        DASH,
        /** 回旋归位 */
        RETURN
    }

    /** 撞击冲刺速度(格/tick) —— 远快于玩家走速, 靠"看到后撤就闪开"来躲 */
    private static final double DASH_SPEED = 1.35;      // 2026-09-11 提高电球命中率: 1.15 -> 1.35
    /** 撞击伤害 = 基数 + 阶段 × 每阶段 */
    private static final float SLAM_DMG_BASE = 12f;
    private static final float SLAM_DMG_PER_PHASE = 6f;
    /** 撞完之后这颗球多久不再撞 */
    private static final int SLAM_CD = 100;

    private Move move = Move.ORBIT;
    /** 当前模式剩余 tick */
    private int moveTicks = 60;
    /** 撞击冷却(<=0 才允许再次进入蓄力) */
    private int slamCd = 40;
    /** 本次撞击后还要补撞几次(回想形态 = 1, 即"双撞") */
    private int dashRepeat = 0;
    /** 每颗球自己的环绕半径/上下摆幅/旋向 */
    private double orbitR = 2.9;
    private double bobA = 0.35;
    private int spin = 1;
    /** 冲刺方向(单位向量)与还能飞几 tick */
    private double dashX, dashY, dashZ;
    private int dashLeft = 0;
    /** 这一趟冲刺是否已经命中(避免一帧多次结算) */
    private boolean dashHit = false;
    /** 锁定的撞击目标实体 id(-1 = 跟随 Boss 的目标) */
    private int dashTargetId = -1;

    public ChargedOrbEntity(EntityType<? extends ChargedOrbEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setNoGravity(true);
        this.setPersistenceRequired();
        this.setSilent(true);
    }

    /**
     * 充能球的属性表。
     *
     * <p><b>v156 补 ATTACK_DAMAGE</b>：{@code createMobAttributes()} **不含** ATTACK_DAMAGE，
     * 而 {@code FTEntities} 的启动自检（{@code attr()}）会在日志里 ERROR 点名
     * 「ftboss:charged_orb 的属性表缺 ATTACK_DAMAGE —— 它一近战就会崩服」。
     * 球本身没有近战目标（{@code registerGoals()} 为空，运动全由 tick 状态机控制），
     * 所以这 1.0 只是把属性表补齐、消除崩服隐患与启动报错，不影响实际伤害 ——
     * 球的伤害走 {@code ChargedOrbEntity} 自己按 Boss 攻击力的百分比结算（技能半 × 全局系数）。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void registerGoals() {
        // 运动完全由 tick 里的状态机控制
    }

    public OrbType getOrbType() {
        return OrbType.byId(this.entityData.get(DATA_TYPE));
    }

    public void setOrbType(OrbType t) {
        this.entityData.set(DATA_TYPE, t.id);
    }

    public void setOwnerBoss(DefectCoreEntity boss) {
        this.ownerId = boss.getUUID();
        this.entityData.set(DATA_OWNER, boss.getId());
        this.orbitPhase = this.random.nextDouble() * Math.PI * 2;
        this.orbitR = 2.3 + this.random.nextDouble() * 1.2;      // 每颗球半径不同 -> 不会叠成一圈
        this.bobA = 0.2 + this.random.nextDouble() * 0.5;
        this.spin = this.random.nextBoolean() ? 1 : -1;
        this.slamCd = 20 + this.random.nextInt(60);              // 出场别立刻撞
        this.moveTicks = 20 + this.random.nextInt(60);
    }

    /** 存活 tick(黑球蓄力值 / 排序依据) */
    public int getChargeTicks() {
        return this.charge;
    }

    /** 由 Boss 按阶段延长存活时间(阶段越高球越持久 = 全部球效果提升的一环) */
    public void setLifeTicks(int ticks) {
        this.life = ticks;
    }

    /** 【黑球】额外撞击伤害(2026-09-11): 蓄力越久越高, 每秒 +2 */
    private float slamBonus = 0f;

    /** 设置本颗球的额外撞击伤害(由 DefectOrbs 的 launchDarkOrb 调用) */
    public void setSlamBonus(float bonus) {
        this.slamBonus = Math.max(0f, bonus);
    }

    /** 本颗球的总存活时间(黑球更长) —— 供控制器按"成熟度比例"挑选要激活的球 */
    public int getLifeTicks() {
        return this.life <= 0 ? 1 : this.life;
    }

    public boolean isOwnedBy(DefectCoreEntity boss) {
        return this.ownerId != null && this.ownerId.equals(boss.getUUID());
    }

    /** 按 UUID 判断归属(连死怜悯清理某一只故障机器人的球时用) */
    public boolean isOwnedByUuid(UUID owner) {
        return this.ownerId != null && this.ownerId.equals(owner);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();      // ★ 必须调 super: 否则原版(生命值/旗帜/名条…)同步数据全没注册 -> getHealth() 直接 NPE 崩服
        this.entityData.define(DATA_TYPE, OrbType.GLASS.id);
        this.entityData.define(DATA_OWNER, -1);
        this.entityData.define(DATA_CHARGE, 0);
        this.entityData.define(DATA_VISUAL, new net.minecraft.nbt.CompoundTag());
    }

    // ================= 受击 / 消散 =================

    /**
     * 被玩家(或任何东西)打掉: 不激发(反制窗口)。
     *
     * <p>2026-09-11 起: **任何伤害来源都能打爆它**(玩家近战/弓箭/法术/AoE/其它 mod 的伤害都要算),
     * 环境伤害同样算 —— 因为球会高速飞进地形, 被墙里窒息/岩浆"白掉"也算玩家的收益。
     * <p>v112: 注释以前写的是"只有玩家造成的伤害能拆球, 环境伤害一律免疫"(与实现相反),
     * 顺手把那个空的 {@code if (source.getEntity() instanceof ServerPlayer) { }} 删了(死代码)。
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || this.gone) return false;
        DefectCoreEntity boss = ownerBoss();
        if (boss != null) boss.orbs().onOrbDestroyed(this);
        this.vanish(false, false);
        return true;
    }

    @Override
    public void die(DamageSource source) {
        this.vanish(false, false);
    }

    /** Boss 主动吸收(由控制器调用): 消散, 技能由 Boss 那边结算 */
    public void absorbed() {
        this.vanish(true, false);
    }

    /**
     * 【电流相生】用: 让这颗球**立刻进入撞击流程**(不等环绕计时) ——
     * 召唤出来就扑向玩家, 而不是先绕两圈。
     */
    /** 球伤害倍率(2026-09-12 【集中碎片】: ×1.5) */
    private float damageMul = 1f;

    public void setDamageMul(float m) {
        this.damageMul = m <= 0f ? 1f : m;
    }

    /** 【黑球】满充能撞击时是否有 5% 概率放黑洞(2026-09-11) */
    private boolean blackHole = false;

    public void setBlackHoleChance(boolean v) {
        this.blackHole = v;
    }

    @Override
    public boolean isPickable() {
        return !this.gone;
    }

    public void beginAssault() {
        if (this.gone || this.reservedForCast) return;
        this.move = Move.WINDUP;
        this.moveTicks = 10;
        this.slamCd = 0;
        this.dashRepeat = 0;
    }

    private void vanish(boolean absorbed, boolean matured) {
        if (this.gone) return;
        this.gone = true;
        this.reservedForCast = false;
        this.fadeTicks = FADE_TICKS;
        setVisualState(absorbed ? "drain" : "burst", FADE_TICKS);
        if (this.level() instanceof ServerLevel sl) {
            Fx.particles(sl, this.getX(), this.getY(), this.getZ(), 16,
                    absorbed ? ParticleTypes.REVERSE_PORTAL : particlesFor(), 0.35, 0.06);
            if (absorbed) {
                // 一道光被吸向核心
                Entity boss = ownerBoss();
                if (boss != null) {
                    for (int i = 0; i < 6; i++) {
                        double t = i / 6.0;
                        Fx.particles(sl, this.getX() + (boss.getX() - this.getX()) * t,
                                this.getY() + (boss.getY() + 1.6 - this.getY()) * t,
                                this.getZ() + (boss.getZ() - this.getZ()) * t, 3, ParticleTypes.END_ROD, 0.05, 0.01);
                    }
                }
            }
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_BLOCK_BREAK,
                    SoundSource.HOSTILE, 1.0f, absorbed ? 0.7f : 1.4f);
        }
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("orb_type", getOrbType().id);
        if (this.ownerId != null) tag.putUUID("orb_owner", this.ownerId);
        tag.putInt("orb_charge", this.charge);
        tag.putInt("orb_life", this.gone ? 0 : this.life);
        tag.putInt("orb_slot", this.formationIndex);
        tag.putInt("orb_slots", this.formationCount);
        tag.putBoolean("orb_spent", this.spentProjectile || this.gone);
        tag.putFloat("orb_damage_mul", this.damageMul);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setOrbType(OrbType.byId(tag.getString("orb_type")));
        if (tag.hasUUID("orb_owner")) this.ownerId = tag.getUUID("orb_owner");
        this.charge = Math.max(0, tag.getInt("orb_charge"));
        this.entityData.set(DATA_CHARGE, this.charge);
        this.life = tag.contains("orb_life") ? tag.getInt("orb_life") : LIFE;
        setFormationSlot(tag.getInt("orb_slot"), Math.max(1, tag.getInt("orb_slots")));
        this.spentProjectile = tag.getBoolean("orb_spent");
        this.maturedRequested = this.spentProjectile;
        if (tag.contains("orb_damage_mul")) setDamageMul(tag.getFloat("orb_damage_mul"));
        if (this.spentProjectile) {
            setSlamBonus(2.0f * (this.charge / 20.0f));
            setBlackHoleChance(true);
        }
    }

    private SimpleParticleType particlesFor() {
        return switch (getOrbType()) {
            case GLASS -> ParticleTypes.END_ROD;
            case LIGHTNING -> ParticleTypes.ELECTRIC_SPARK;
            case DARK -> ParticleTypes.SCULK_SOUL;
            case FROST -> ParticleTypes.SNOWFLAKE;
            case ION -> ParticleTypes.WARPED_SPORE;
        };
    }

    public DefectCoreEntity ownerBoss() {
        Entity owner = this.level().isClientSide ? this.level().getEntity(this.entityData.get(DATA_OWNER))
                : this.ownerId != null && this.level() instanceof ServerLevel server ? server.getEntity(this.ownerId) : null;
        return owner instanceof DefectCoreEntity boss ? boss : null;
    }

    // ================= tick =================

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        DefectCoreEntity boss = ownerBoss();
        if (this.ownerId != null && (boss == null || !boss.isAlive())) {
            if (boss != null || this.tickCount > 40) this.discard();
            return;
        }
        if (boss != null && this.entityData.get(DATA_OWNER) != boss.getId()) this.entityData.set(DATA_OWNER, boss.getId());
        if (this.gone) {
            if ("drain".equals(visualState()) && boss != null) {
                Vec3 destination = boss.orbAnchor(false);
                this.setPos(this.position().lerp(destination, 0.45));
            }
            if (--this.fadeTicks <= 0) this.discard();
            return;
        }
        if (this.reservedForCast) {
            if (this.focusDuration > 0 && boss != null) {
                Vec3 destination = boss.orbAnchor(true);
                double progress = net.farmingtales.ftboss.util.OrbLinkMath.progress(++this.focusTicks, this.focusDuration);
                this.setPos(net.farmingtales.ftboss.util.OrbLinkMath.arc(this.transferStart.x, destination.x, progress, 0.0),
                        net.farmingtales.ftboss.util.OrbLinkMath.arc(this.transferStart.y, destination.y, progress, 0.4),
                        net.farmingtales.ftboss.util.OrbLinkMath.arc(this.transferStart.z, destination.z, progress, 0.0));
            } else if (boss != null) {
                if (boss.orbLinkStage().startsWith("beam")) {
                    tickMovement(boss);
                    setVisualState("beam", 0);
                } else {
                    anchorOrbit(boss, 1.0, 0.0);
                    setVisualState("idle", 0);
                }
            }
            return;
        }
        this.charge++;
        this.life--;
        if (this.tickCount % 10 == 0) this.entityData.set(DATA_CHARGE, this.charge);
        if (boss != null && boss.isAlive()) {
            if (this.tickCount <= BIRTH_TICKS && !this.spentProjectile) {
                Vec3 start = boss.orbAnchor(true);
                double angle = this.formationIndex * Math.PI * 2.0 / this.formationCount;
                Vec3 end = boss.position().add(Math.cos(angle) * this.orbitR, 2.2, Math.sin(angle) * this.orbitR);
                double progress = net.farmingtales.ftboss.util.OrbLinkMath.progress(this.tickCount, BIRTH_TICKS);
                this.setPos(net.farmingtales.ftboss.util.OrbLinkMath.arc(start.x, end.x, progress, 0),
                        net.farmingtales.ftboss.util.OrbLinkMath.arc(start.y, end.y, progress, 0.6),
                        net.farmingtales.ftboss.util.OrbLinkMath.arc(start.z, end.z, progress, 0));
                setVisualState("summon", BIRTH_TICKS);
                return;
            }
            tickMovement(boss);
            if (this.gone) return;
            String state = boss.orbLinkStage().startsWith("beam") && !this.spentProjectile && this.move != Move.DASH ? "beam" : switch (this.move) {
                case WINDUP -> "focus";
                case DASH -> "dash";
                case RETURN -> "return";
                default -> boss.orbLinkStage().startsWith("beam") ? "beam" : "idle";
            };
            setVisualState(state, 0);
        } else if (this.ownerId != null && boss == null) {
            this.discard();      // Boss 没了 -> 球一起消失
            return;
        }

        // 到期: 交给 Boss 吸收(由 Boss 放技能)
        if (this.life <= 0) {
            if (this.spentProjectile) {
                this.vanish(false, true);
            } else if (boss != null && !this.maturedRequested) {
                boss.orbs().onOrbMatured(this);
            } else if (boss == null) {
                this.vanish(false, true);
            }
            return;
        }
        // 轨迹粒子(撞击时更密, 见 tickDash)
        if (this.move != Move.DASH && this.tickCount % 4 == 0 && this.level() instanceof ServerLevel sl2) {
            Fx.particles(sl2, this.getX(), this.getY(), this.getZ(), 2, particlesFor(), 0.15, 0.01);
        }
    }

    // ================= 运动状态机 =================

    private void tickMovement(DefectCoreEntity boss) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        if (boss.orbLinkStage().startsWith("beam") && !this.spentProjectile && this.move != Move.DASH) {
            double angle = this.formationIndex * Math.PI * 2.0 / this.formationCount + boss.tickCount * 0.018;
            double radius = "beam_fire".equals(boss.orbLinkStage()) ? 1.9 : 2.7;
            flyTo(boss.getX() + Math.cos(angle) * radius, boss.getY() + 2.65 + Math.sin(angle * 2) * 0.25,
                    boss.getZ() + Math.sin(angle) * radius, 0.45);
            return;
        }
        if (this.slamCd > 0) this.slamCd--;
        LivingEntity target = currentTarget();

        switch (this.move) {
            case ORBIT -> {
                anchorOrbit(boss, 1.0, 0.0);
                if (--this.moveTicks <= 0) pickNextMode(target);
            }
            case SPIRAL -> {
                anchorOrbit(boss, 1.35, 0.8);
                if (--this.moveTicks <= 0) pickNextMode(target);
            }
            case WINDUP -> {
                pullBack(boss, target);
                if (--this.moveTicks <= 0) beginDash(target);
            }
            case DASH -> tickDash(sl, target);
            case RETURN -> {
                boolean home = flyTo(boss.getX(), boss.getY() + 1.6, boss.getZ(), 0.55);
                if (home || --this.moveTicks <= 0) {
                    this.move = Move.ORBIT;
                    this.moveTicks = 40 + this.random.nextInt(60);
                }
            }
        }
    }

    /** 环绕/螺旋: 半径、摆幅、旋向都是每颗球自己的 */
    private void anchorOrbit(DefectCoreEntity boss, double speedMul, double pulse) {
        this.orbitPhase += 0.045 * speedMul * this.spin;
        double r = this.orbitR * (pulse > 0.0
                ? 1.0 - pulse * (0.5 + 0.5 * Math.sin(this.orbitPhase * 1.7))   // 收束到 20%~100%
                : 1.0);
        double hx = boss.getX() + Math.cos(this.orbitPhase) * r;
        double hz = boss.getZ() + Math.sin(this.orbitPhase) * r;
        double hy = boss.getY() + 2.2 + Math.sin(this.orbitPhase * 2.0) * this.bobA;
        flyTo(hx, hy, hz, 0.38);
    }

    /** 后撤蓄力: 退到 Boss 背离目标的一侧并抬升 —— 这就是撞击的预警动作 */
    private void pullBack(DefectCoreEntity boss, LivingEntity target) {
        double ax = target != null ? target.getX() - boss.getX() : 0.0;
        double az = target != null ? target.getZ() - boss.getZ() : 1.0;
        double len = Math.max(0.001, Math.sqrt(ax * ax + az * az));
        double px = boss.getX() - ax / len * 3.2;
        double pz = boss.getZ() - az / len * 3.2;
        flyTo(px, boss.getY() + 2.3, pz, 0.85);
    }

    /** 选择下一个模式: 进攻型球爱撞, 防守型球基本只环绕 */
    private void pickNextMode(LivingEntity target) {
        boolean canSlam = target != null && this.slamCd <= 0 && this.distanceTo(target) < 26.0;
        if (canSlam && this.random.nextFloat() < slamChance()) {
            this.move = Move.WINDUP;
            this.moveTicks = 12;
            DefectCoreEntity boss = ownerBoss();
            this.dashRepeat = boss != null && boss.orbs().isEchoForm() ? 1 : 0;   // 回想形态: 双撞
            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH,
                        SoundSource.HOSTILE, 0.9f, 1.6f);
                Fx.particles(sl, this.getX(), this.getY(), this.getZ(), 10, particlesFor(), 0.3, 0.05);
            }
        } else {
            this.move = this.random.nextFloat() < 0.5f ? Move.ORBIT : Move.SPIRAL;
            this.moveTicks = 60 + this.random.nextInt(80);
        }
    }

    /** 各球种的撞击倾向: 玻璃(AOE)/电(单体)/黑(蓄力) 是进攻球; 冰/离子是辅助球, 老实待着 */
    private float slamChance() {
        return switch (getOrbType()) {
            case GLASS, LIGHTNING, DARK -> 0.5f;
            case FROST, ION -> 0.16f;
        };
    }

    /** 起撞: 朝目标**带一点预判**的方向定死一条直线, 中途不再转向(所以能被侧移躲开) */
    private void beginDash(LivingEntity target) {
        if (target == null) {
            toOrbit();
            return;
        }
        this.dashTargetId = target.getId();
        double tx = target.getX() + target.getDeltaMovement().x * 6.0;
        double ty = target.getY() + 1.0 + target.getDeltaMovement().y * 6.0;
        double tz = target.getZ() + target.getDeltaMovement().z * 6.0;
        double dx = tx - this.getX(), dy = ty - this.getY(), dz = tz - this.getZ();
        double len = Math.max(0.001, Math.sqrt(dx * dx + dy * dy + dz * dz));
        this.dashX = dx / len;
        this.dashY = dy / len;
        this.dashZ = dz / len;
        this.dashLeft = (int) Math.min(28.0, len + 6.0);   // 冲过目标后再飞一段
        this.dashHit = false;
        this.move = Move.DASH;
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PHANTOM_SWOOP,
                    SoundSource.HOSTILE, 1.3f, 1.2f);
        }
    }

    private void tickDash(ServerLevel sl, LivingEntity target) {
        // 2026-09-11 按用户要求"机器人的电球命中率要高一点": **电球在冲刺途中会追踪目标**
        // (每 tick 朝目标修正 35% 方向; 其它球种仍是直线, 保留"侧移可躲"的手感)
        if (getOrbType() == OrbType.LIGHTNING && target != null && target.isAlive()) {
            double tx = target.getX() - this.getX();
            double ty = target.getY() + target.getBbHeight() * 0.5 - this.getY();
            double tz = target.getZ() - this.getZ();
            double tl = Math.max(0.001, Math.sqrt(tx * tx + ty * ty + tz * tz));
            double k = 0.5;      // 2026-09-11 电球追踪更黏(0.35 -> 0.5)
            double mx = this.dashX + (tx / tl - this.dashX) * k;
            double my = this.dashY + (ty / tl - this.dashY) * k;
            double mz = this.dashZ + (tz / tl - this.dashZ) * k;
            double ml = Math.max(0.001, Math.sqrt(mx * mx + my * my + mz * mz));
            this.dashX = mx / ml; this.dashY = my / ml; this.dashZ = mz / ml;
        }
        double nx = this.getX() + this.dashX * DASH_SPEED;
        double ny = this.getY() + this.dashY * DASH_SPEED;
        double nz = this.getZ() + this.dashZ * DASH_SPEED;
        // 撞墙 -> 弹回, 不穿墙
        HitResult hit = this.level().clip(new ClipContext(this.position(), new Vec3(nx, ny, nz),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.MISS) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHIELD_BLOCK,
                    SoundSource.HOSTILE, 1.0f, 0.7f);
            Fx.particles(sl, this.getX(), this.getY(), this.getZ(), 12, particlesFor(), 0.3, 0.08);
            toOrbit();
            return;
        }
        this.teleportTo(nx, ny, nz);
        // 冲刺尾迹
        Fx.particles(sl, nx, ny, nz, 6, particlesFor(), 0.12, 0.02);
        Fx.particles(sl, nx, ny, nz, 2, ParticleTypes.END_ROD, 0.05, 0.01);
        // 命中判定
        if (!this.dashHit && target != null && target.isAlive()
                && target.getBoundingBox().inflate(getOrbType() == OrbType.LIGHTNING ? 1.6 : 1.0).contains(this.position())) {
            slamHit(sl, target);
        }
        if (--this.dashLeft <= 0) {
            if (this.dashRepeat > 0) {
                this.dashRepeat--;
                this.move = Move.WINDUP;      // 回想形态的第二次撞击: 短暂再蓄力
                this.moveTicks = 8;
            } else {
                toOrbit();
            }
        }
    }

    /** 撞击命中: 魔法伤害(无视护甲) + 击退 + 元素特效; 玻璃球额外溅射 */
    private void slamHit(ServerLevel sl, LivingEntity target) {
        this.dashHit = true;
        DefectCoreEntity boss = ownerBoss();
        int phase = boss != null ? boss.getPhase() : 1;
        // 2026-09-11 按用户要求: **满充能黑球撞击玩家时, 5% 概率放【黑洞】**
        if (getOrbType() == OrbType.DARK && this.blackHole && boss != null
                && boss.getRandom().nextFloat() < 0.05f) {
            net.farmingtales.ftboss.integ.WizardSummon.castByName(sl, boss, target, "irons_spellbooks:black_hole", 4);
        }
        float dmg = net.farmingtales.ftboss.boss.ai.BossScaling.dmg(boss, (SLAM_DMG_BASE + phase * SLAM_DMG_PER_PHASE + this.slamBonus) * this.damageMul);
        // 2026-09-12 按用户要求: 球的伤害 = **物理 / 魔法 各一半**; 每次都要清无敌帧, 否则第二段会被吃掉。
        // (v112: 删掉这里重复的第一句 invulnerableTime = 0 —— 下面每段之前各有一句, 那句是死语句)
        target.invulnerableTime = 0;
        target.hurt(sl.damageSources().mobAttack(boss), dmg * 0.25f);      // ① 物理(技能半 × 全局 0.5, v77)
        target.invulnerableTime = 0;
        target.hurt(sl.damageSources().indirectMagic(boss, boss), dmg * 0.25f);              // ② 魔法(v77)
        net.farmingtales.ftboss.util.BossKnockback.knockback(target, 1.5, this.dashX, this.dashZ);   // 沿冲撞方向击飞
        // 玻璃球 = AOE 定位: 撞击也带一圈溅射
        if (getOrbType() == OrbType.GLASS) {
            for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, target.getBoundingBox().inflate(4.0))) {
                if (p == target || !p.isAlive()) continue;
                p.invulnerableTime = 0;
            p.hurt(sl.damageSources().mobAttack(boss), dmg * 0.15f);       // ① 物理(溅射, v77)
            p.invulnerableTime = 0;
            p.hurt(sl.damageSources().indirectMagic(boss, boss), dmg * 0.15f);               // ② 魔法(溅射, v77)
                net.farmingtales.ftboss.util.BossKnockback.knockback(p, 0.9, this.dashX, this.dashZ);
            }
            Fx.ring(sl, target.getX(), target.getY() + 0.4, target.getZ(), 3.4, 24, ParticleTypes.CRIT, 0.04);
        }
        Fx.particles(sl, target.getX(), target.getEyeY(), target.getZ(), 22, particlesFor(), 0.45, 0.12);
        SoundEvent sfx = switch (getOrbType()) {
            case GLASS -> SoundEvents.GLASS_BREAK;
            case LIGHTNING -> SoundEvents.LIGHTNING_BOLT_IMPACT;
            case DARK -> SoundEvents.SCULK_SHRIEKER_SHRIEK;
            case FROST -> SoundEvents.GLASS_BREAK;
            case ION -> SoundEvents.AMETHYST_BLOCK_CHIME;
        };
        sl.playSound(null, target.getX(), target.getEyeY(), target.getZ(), sfx, SoundSource.HOSTILE, 1.2f, 1.0f);
        sl.playSound(null, target.getX(), target.getEyeY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT,
                SoundSource.HOSTILE, 1.0f, 0.8f);
        if (target instanceof ServerPlayer sp) {
            // 2026-09-12: 撞击提示已按要求静音
        }
        FTBossMod.LOGGER.info("[FTBoss][充能球] 撞击 -> {} ({} 伤害, 阶段 {})",
                target.getName().getString(), String.format("%.1f", dmg), phase);
    }

    /** 回到环绕 */
    private void toOrbit() {
        this.move = Move.RETURN;
        this.moveTicks = 40 + this.random.nextInt(60);
        this.slamCd = SLAM_CD;
        this.dashTargetId = -1;
        this.dashHit = false;
        this.dashRepeat = 0;
    }

    /** 往一个点飞(限速), 到位返回 true */
    private boolean flyTo(double x, double y, double z, double step) {
        double dx = x - this.getX(), dy = y - this.getY(), dz = z - this.getZ();
        double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dl <= 0.35) return true;
        double s = Math.min(step, dl);
        this.teleportTo(this.getX() + dx / dl * s, this.getY() + dy / dl * s, this.getZ() + dz / dl * s);
        return false;
    }

    /** 撞击目标: 优先用锁定 id, 否则跟随 Boss 的目标 */
    private LivingEntity currentTarget() {
        if (this.dashTargetId >= 0) {
            Entity e = this.level().getEntity(this.dashTargetId);
            if (e instanceof LivingEntity le && le.isAlive()) return le;
        }
        DefectCoreEntity boss = ownerBoss();
        LivingEntity t = boss != null ? boss.getTarget() : null;
        return t != null && t.isAlive() ? t : null;
    }

    // ================= GeckoLib =================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "orb_ctrl", 2, this::predicate));
    }

    private PlayState predicate(AnimationState<ChargedOrbEntity> state) {
        int sequence = this.entityData.get(DATA_VISUAL).getInt("sequence");
        if (sequence != this.renderedVisualSequence) {
            this.renderedVisualSequence = sequence;
            state.getController().forceAnimationReset();
        }
        state.getController().setTransitionLength(0);
        state.setAnimation(switch (visualState()) {
            case "summon" -> SUMMON_A;
            case "focus" -> FOCUS_A;
            case "dash" -> DASH_A;
            case "return" -> RETURN_A;
            case "beam" -> BEAM_A;
            case "drain" -> DRAIN_A;
            case "burst" -> BURST_A;
            default -> IDLE_A;
        });
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

    /** 调试用 */
    public String describe() {
        return getOrbType().id + "(" + this.charge + "t/" + this.move + ")";
    }
}
