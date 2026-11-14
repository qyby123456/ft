package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ai.ExtraDebuffs;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
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
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;


/**
 * 【benger】—— 边牧犬王（2026-09-14 v127 新 Boss，**第 9 位主 Boss**，不再是庄园彩蛋系）。
 *
 * <p>用户给的参考图是一只水墨风的黑白边牧（黑背 + 白开脸 + 白胸白袜 + 红项圈），要求"根据这张图做一个 Boss"，
 * 名字 <b>benger</b>，掉落武器<b>【打狗棒】</b>。所以造型与招式都围着"牧羊犬 / 流浪犬王"来做：
 * <ul>
 *   <li><b>撕咬</b>（近战）：咬中就挂本模组的【流血】（{@link ExtraDebuffs#bleed}）+ 击退；</li>
 *   <li><b>飞扑</b>：蓄力下蹲后腾空扑向玩家，落点 3.5 格内吃伤害并被掀飞；</li>
 *   <li><b>长嗥</b>：仰头嗥叫 —— 14 格内玩家 缓慢 II + 虚弱 I，自己拿 速度 II + 抗性 I；</li>
 *   <li><b>呼朋引伴</b>：嗥叫召唤 2 只小狗（{@link BengerPupEntity}，场上最多 3 只）；</li>
 *   <li><b>打狗棒横扫</b>：它**嘴里叼着**打狗棒（模型里的 stick 骨骼），近距离横扫 4.2 格、掀飞 + 缓慢；</li>
 *   <li>血量 &lt; 40% 进入<b>狂暴</b>：移速 +、立刻再喊 2 只小狗（只触发一次）。</li>
 * </ul>
 *
 * <p>数值与"食物系 Boss"同级：{@value #MAX_HP} 血，走统一的 {@link net.farmingtales.ftboss.boss.ai.BossDamageGate}
 * （免疫流血/真实伤害；逐 Boss 限伤体系当前关闭）+ {@code BossRage}（狂暴档）+
 * {@code TrapBreaker}（破陷阱）+ {@code BossUnstuck}（脱困）+ 顶部血条（{@link BossBars}）。
 *
 * <p>几何/动画：{@code geo/benger.geo.json} + {@code animations/benger.animation.json} +
 * {@code textures/entity/benger.png}，由 {@code tools/model3d/gen_benger.cjs} 一份规格生成。
 * 动画名前缀 {@code animation.benger.*}，时长与这里的 {@code T_*} 常量一一对应（1s = 20t）。
 */
public class BengerEntity extends Monster implements GeoAnimatable {

    /** 血量（2026-09-14 按用户要求 1000 → **330**） */
    public static final float MAX_HP = 660.0f;

    /* ---- 动作时长：**必须**与 gen_benger.cjs 里的 animation_length 对齐（1s = 20t） ---- */
    public static final int T_BITE = 10;     // bite 0.5s
    public static final int T_POUNCE = 14;   // pounce 0.7s
    public static final int T_ROAR = 20;     // roar 1.0s
    public static final int T_HURT = 8;      // hurt 0.4s
    public static final int T_DEATH = 24;    // death 1.2s

    /* ---- v128 技能组（按用户：合并"飞扑+呼朋引伴"、新增"连续下砸"）----
       ① 狼群扑击 = 先长嗥召狗 → 全体（自己 + 所有小狗）**锁定玩家并同时飞扑**；
       ② 连续下砸 = 腾空 → 朝玩家当前位置砸下 → 连砸 N 次（每次都重新锁定落点）；
       ③ 打狗棒横扫 / ④ 撕咬 / ⑤ 狂暴 保留。 */
    private static final int PACK_CD = 14 * 20;          // 狼群扑击冷却
    private static final int PACK_LEAD = 30;             // 长嗥到集体扑出的延迟（1.5 秒，给玩家反应）
    private static final double PACK_RANGE = 20.0;       // 锁定范围
    private static final float PACK_DAMAGE = 36.0f;   // v129: +50%
    private static final double PACK_RADIUS = 3.5;

    private static final int SLAM_CD = 17 * 20;          // 连续下砸冷却
    private static final int SLAM_COUNT = 3;             // 连砸几次
    private static final int SLAM_GAP = 22;              // 每次之间的间隔
    private static final int SLAM_HANG = 8;              // 腾空滞空（预警）时长
    private static final float SLAM_DAMAGE = 39.0f;   // v129: +50%
    private static final double SLAM_RADIUS = 4.0;

    private static final int ROAR_CD = 16 * 20;
    private static final double ROAR_RADIUS = 14.0;

    /* v130 新增两招（用户要求）：① 撕咬钳制(咬住持续掉血 + 自己回血) ② 冲撞(撞飞 + 眩晕) */
    private static final int CLAMP_CD = 16 * 20;
    private static final double CLAMP_RANGE = 5.5;
    private static final int CLAMP_TICKS = 60;          // 咬住 3 秒
    private static final float CLAMP_DPS = 20.0f;       // 每秒对玩家
    private static final float CLAMP_HEAL = 16.0f;      // 每秒自己回血
    private static final int CHARGE_CD = 19 * 20;
    private static final double CHARGE_MIN = 3.5, CHARGE_MAX = 22.0;
    private static final float CHARGE_DAMAGE = 34.0f;
    private static final int CHARGE_STUN = 40;          // 撞中眩晕 2 秒

    /* v133 用户指定的两个技能：① 大狗叫！（范围全生物眩晕 + 召 2 只小狗）② 野生狗奶（附魔金苹果 + 虚影 + 血王子 IV） */
    private static final int BARK_CD = 20 * 20;
    private static final double BARK_RADIUS = 14.0;
    private static final int BARK_STUN = 50;             // 2.5 秒
    private static final int MILK_CD = 28 * 20;
    private static final int MILK_TICKS = 20 * 20;       // 附魔金苹果/血王子持续
    /* v133 二阶段（半血）：召唤翻倍 / 流血可叠 10 层 / 下砸附加震颤 */
    private int phase = 1;

    private static final int SWEEP_CD = 11 * 20;
    private static final double SWEEP_RANGE = 5.5, SWEEP_RADIUS = 4.2;
    private static final float SWEEP_DAMAGE = 33.0f;   // v129: +50%

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.benger.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.benger.walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.benger.run");
    private static final RawAnimation BITE = RawAnimation.begin().thenPlay("animation.benger.bite");
    private static final RawAnimation POUNCE = RawAnimation.begin().thenPlay("animation.benger.pounce");
    private static final RawAnimation ROAR = RawAnimation.begin().thenPlay("animation.benger.roar");
    private static final RawAnimation SWEEP = RawAnimation.begin().thenPlay("animation.benger.sweep");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.benger.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.benger.death");

    /** 动作编号（0=没有动作） */
    private static final int ACT_NONE = 0, ACT_BITE = 1, ACT_PACK = 2, ACT_ROAR = 3, ACT_HURT = 4, ACT_DEATH = 5, ACT_SLAM = 6, ACT_LEAP = 7, ACT_SWEEP = 8, ACT_BARK = 9, ACT_MILK = 10;
    /** 小狗上限（狼群扑击一次喊 2 只） */
    public static final int PUP_CAP = 8;   // v130 按用户要求 5 → 8

    private static final EntityDataAccessor<Boolean> DATA_RAGED =
            SynchedEntityData.defineId(BengerEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossBar = new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);

    private int act = ACT_NONE;
    private int actionTicks = 0;
    private static final EntityDataAccessor<CompoundTag> DATA_ACTION =
            SynchedEntityData.defineId(BengerEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private final net.farmingtales.ftboss.boss.ai.BossActionState actionState =
            new net.farmingtales.ftboss.boss.ai.BossActionState(this, DATA_ACTION);
    private int lastSyncedAction = ACT_NONE;
    private int lastSyncedTicks;
    private int packCd = 6 * 20, roarCd = 10 * 20, sweepCd = 6 * 20, slamCd = 12 * 20;
    private int pounceFired = 0;        // 0=还没结算, 1=已结算
    private int roarFired = 0;
    private int sweepFired = 0;
    /* 狼群扑击：长嗥后延迟 PACK_LEAD 才集体扑出 */
    private int packPounceAt = -1;
    private double jumpX, jumpZ;
    /* 连续下砸 */
    private int slamLeft = 0;           // 还剩几次
    private int slamTimer = 0;          // 下一次下砸/腾空的倒计时
    private boolean slamAirborne = false;
    private double slamX, slamY, slamZ;
    private int growlCd = 0;
    /* v132 按用户要求：每 25 秒进入 3 秒"不可选中"的无敌状态 */
    private static final int INTANG_CD = 25 * 20;
    private static final int INTANG_TICKS = 3 * 20;
    private int intangCd = INTANG_CD, intangLeft = 0;
    private int barkCd = 10 * 20, milkCd = 16 * 20;
    /* 咬住 */
    private int clampCd = 14 * 20, clampTicks = 0, clampHealTick = 0;
    private net.minecraft.world.entity.player.Player clampTarget = null;
    private double clampX, clampY, clampZ;
    /* 冲撞 */
    private int chargeCd = 10 * 20, chargeTicks = 0, chargeFired = 0;
    private double chargeX, chargeZ;
    /* 眩晕(被咬/被撞的玩家)：锚点硬定身 */
    private final java.util.Map<java.util.UUID, Integer> stunTicks = new java.util.HashMap<>();
    private final java.util.Map<java.util.UUID, net.minecraft.world.phys.Vec3> stunAnchor = new java.util.HashMap<>();

    public BengerEntity(EntityType<? extends BengerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;                          // 战利品由 BossMercy 统一发
        this.setPersistenceRequired();
    }

    @Override
    public void setSpeed(float speed) {
        super.setSpeed(BossWalkingSpeed.speed(this, speed));
        BossWalkingSpeed.normalizeInput(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HP)
                .add(Attributes.ATTACK_DAMAGE, 30.0)
                .add(Attributes.MOVEMENT_SPEED, BossWalkingSpeed.BASE_SPEED)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, net.farmingtales.ftboss.util.BossKnockback.BOSS_RESISTANCE)   // 2026-09-19: 统一 0.5
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();                  // ★ LivingEntity 系**必须**调 super（见 AGENTS.md）
        this.entityData.define(DATA_RAGED, false);
        this.entityData.define(DATA_ACTION, new CompoundTag());
    }

    /** 狂暴标记（存进 NBT，读档后不重复触发） */
    public boolean isRaged() { return this.entityData.get(DATA_RAGED); }

    private void setRaged(boolean v) { this.entityData.set(DATA_RAGED, v); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 24.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /* ==================== 声音：直接借原版狼的（零新增音频） ==================== */

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.WOLF_GROWL; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.WOLF_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.WOLF_DEATH; }
    @Override protected float getSoundVolume() { return 1.3f; }
    @Override public float getVoicePitch() { return 0.72f; }   // 压低 = "大狗"

    @Override
    public boolean removeWhenFarAway(double dist) { return false; }

    /* ==================== 主循环 ==================== */

    @Override
    public void tick() {
        super.tick();
        /* 动作计时两端都要跑：客户端要拿它选动画 */
        if (this.actionTicks > 0 && --this.actionTicks <= 0) this.act = ACT_NONE;
        if (this.isDeadOrDying()) this.act = ACT_DEATH;

        if (this.level() instanceof ServerLevel sl) {
            net.farmingtales.ftboss.boss.ai.BossDamageGate.tick(this);
            net.farmingtales.ftboss.boss.ai.BossRage.tickBoss(this);
            net.farmingtales.ftboss.boss.ai.TrapBreaker.tick(this);
            net.farmingtales.ftboss.boss.ai.BossUnstuck.tick(this);
            net.farmingtales.ftboss.sound.BossMusic.request(sl, this);
            this.bossBar.setProgress(Math.max(0.0f, this.getHealth() / this.getMaxHealth()));
            this.intangTick(sl);
            this.clampTick(sl);
            this.chargeTick(sl);
            this.stunTick(sl);
            this.skillTick(sl);
            if (this.act != this.lastSyncedAction || this.actionTicks > this.lastSyncedTicks) {
                this.actionState.start(Integer.toString(this.act), this.actionTicks);
            }
            this.lastSyncedAction = this.act;
            this.lastSyncedTicks = this.actionTicks;
            this.syncIntangibleState();
        }
    }

    /** 技能轮转：一次只做一件事（动作优先，AI 的走路/追击让位） */
    private void skillTick(ServerLevel sl) {
        if (this.chargeCd > 0) this.chargeCd--;
        if (this.packCd > 0) this.packCd--;
        if (this.roarCd > 0) this.roarCd--;
        if (this.sweepCd > 0) this.sweepCd--;
        if (this.slamCd > 0) this.slamCd--;
        if (this.growlCd > 0) this.growlCd--;
        if (this.barkCd > 0) this.barkCd--;
        if (this.milkCd > 0) this.milkCd--;

        /* ★ v133 半血进入二阶段（召唤翻倍 / 流血可叠 10 层 / 下砸附加震颤） */
        if (this.phase == 1 && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.phase = 2;
            this.summonPups(sl, 4);
            var spd2 = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (spd2 != null) {
                spd2.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        java.util.UUID.fromString("6f2b1d3c-9a11-4c21-8f30-000000000002"),
                        "ftboss.benger_phase2", 0.05, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
            }
            this.tell(sl, "§4§l【二阶段】§r§cbenger 唤来了整群野狗！");
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WOLF_HOWL, SoundSource.HOSTILE, 2.4f, 0.55f);
            FTBossMod.LOGGER.info("[FTBoss][benger] 进入二阶段（半血）：召唤翻倍 / 流血可叠 10 层 / 下砸附加震颤");
        }

        /* 狂暴：血量 < 40% 触发一次 */
        if (!this.isRaged() && this.getHealth() < this.getMaxHealth() * 0.4f) {
            this.setRaged(true);
            var spd = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (spd != null) {
                spd.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        java.util.UUID.fromString("6f2b1d3c-9a11-4c21-8f30-000000000001"),
                        "ftboss.benger_rage", 0.07, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
            }
            this.summonPups(sl, this.phase == 2 ? 6 : 3);
            sl.playSound(null, this.getX(), this.getY() + 1.0, this.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.6f, 1.5f);
            this.playSoundLocal(sl, SoundEvents.WOLF_HOWL, 1.6f, 0.6f);
            FTBossMod.LOGGER.info("[FTBoss][benger] 狂暴（血量 {}/{}）：移速 +0.07，再喊 2 只小狗",
                    (int) this.getHealth(), (int) this.getMaxHealth());
        }

        /* 狼群扑击: 到点 → 自己先扑(小狗在各自的 tick 里同时扑) */
        if (this.packPounceAt > 0 && this.tickCount >= this.packPounceAt) {
            this.selfPackLeap(sl);
            this.act = ACT_LEAP; this.actionTicks = T_POUNCE; this.pounceFired = 0;
        }

        /* 进行中的动作：到点结算 */
        if (this.chargeTicks > 0 || this.clampTicks > 0 || (this.packPounceAt > 0 && this.act == ACT_NONE)) return;
        if (this.act != ACT_NONE) {
            this.actionResolve(sl);
            return;
        }
        /* 连续下砸的腾空/砸落节奏（动作之外也在跑，保证连砸不断档） */
        if (this.slamLeft > 0) {
            this.slamTick(sl);
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;
        double dist = this.distanceTo(target);
        boolean see = this.getSensing().hasLineOfSight(target);

        if (this.chargeCd <= 0 && dist >= CHARGE_MIN && dist <= CHARGE_MAX && see) {
            this.startCharge(sl, target);
            return;
        }

        /* ⓪ 大狗叫！（近距离就喊：全场眩晕 + 召 2 只小狗） */
        if (this.barkCd <= 0 && dist <= BARK_RADIUS) {
            this.act = ACT_BARK; this.actionTicks = T_ROAR; this.roarFired = 0;
            this.barkCd = BARK_CD;
            FTBossMod.LOGGER.info("[FTBoss][benger] 大狗叫！起手（{} 格内全部生物眩晕 {} 秒 + 召 2 只小狗）", (int) BARK_RADIUS, BARK_STUN / 20);
            return;
        }
        /* ⓪ 野生狗奶：血量掉到 70% 以下就喝（或冷却到了常喝） */
        if (this.milkCd <= 0 && (this.getHealth() < this.getMaxHealth() * 0.7f || this.getRandom().nextFloat() < 0.25f)) {
            this.act = ACT_MILK; this.actionTicks = T_ROAR; this.roarFired = 0;
            this.milkCd = MILK_CD;
            return;
        }
        /* ① 狼群扑击（原"飞扑+呼朋引伴"合并）：先长嗥召狗, 再全体锁定玩家同时飞扑 */
        if (this.packCd <= 0 && dist <= PACK_RANGE && see) {
            this.startPackPounce(sl, target);
            return;
        }
        /* ② 连续下砸：中距离腾空连砸 */
        if (this.slamCd <= 0 && dist <= 18.0 && see) {
            this.startSlam(sl, target);
            return;
        }
        /* ★ v130 撕咬钳制：咬住玩家 → 持续掉血 + 自己回血（用户要求） */
        if (this.clampCd <= 0 && dist <= CLAMP_RANGE && target instanceof net.minecraft.world.entity.player.Player pl) {
            this.startClamp(sl, pl);
            return;
        }
        /* ③ 打狗棒横扫：贴近了抡一下 */
        if (this.sweepCd <= 0 && dist <= SWEEP_RANGE) {
            this.act = ACT_SWEEP; this.actionTicks = T_BITE; this.sweepFired = 0;
            this.playSoundLocal(sl, SoundEvents.PLAYER_ATTACK_SWEEP, 1.4f, 0.7f);
            this.sweepCd = SWEEP_CD;
            FTBossMod.LOGGER.info("[FTBoss][benger] 打狗棒横扫（目标距离 {} 格）", String.format("%.1f", dist));
            return;
        }
        /* ④ 单独长嗥（不需要喊狗时的减益/自增益） */
        if (this.roarCd <= 0 && dist <= ROAR_RADIUS) {
            this.act = ACT_ROAR; this.actionTicks = T_ROAR; this.roarFired = 0;
            this.roarCd = ROAR_CD;
            return;
        }
        /* ⑤ 没事时偶尔低吼（氛围） */
        if (this.growlCd <= 0 && this.getRandom().nextFloat() < 0.02f) {
            this.growlCd = 8 * 20;
            this.playSoundLocal(sl, SoundEvents.WOLF_GROWL, 1.2f, 0.7f);
        }
    }

    /** 动作的关键帧结算（伤害/减益都卡在动画的爆发帧上，见 gen_benger.cjs 的键位） */
    private void actionResolve(ServerLevel sl) {
        switch (this.act) {
            case ACT_PACK -> {
                /* 起手长嗥：召狗 + 让全体锁定玩家 */
                if (this.pounceFired == 0 && this.actionTicks <= T_ROAR - 8) {
                    this.pounceFired = 1;
                    this.summonPups(sl, this.phase == 2 ? 6 : 3);
                    sl.playSound(null, this.getX(), this.getY() + 1.0, this.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.6f, 1.4f);
                    this.lockPack(sl);
                    this.playSoundLocal(sl, SoundEvents.WOLF_HOWL, 1.8f, 0.62f);
                    FTBossMod.LOGGER.info("[FTBoss][benger] 狼群扑击：长嗥召狗 + 锁定玩家（{} tick 后全体扑出）", PACK_LEAD);
                }
            }
            case ACT_BARK -> {
                if (this.roarFired == 0 && this.actionTicks <= T_ROAR - 8) {
                    this.roarFired = 1;
                    this.barkAoe(sl);
                }
            }
            case ACT_MILK -> {
                if (this.roarFired == 0 && this.actionTicks <= T_ROAR - 8) {
                    this.roarFired = 1;
                    this.drinkMilk(sl);
                }
            }
            case ACT_LEAP -> {
                if (this.pounceFired == 0 && this.actionTicks <= T_POUNCE - 6) {
                    this.pounceFired = 1;
                    this.packImpact(sl);
                }
            }
            case ACT_BITE, ACT_SWEEP -> {
                if (this.sweepFired == 0 && this.actionTicks <= T_BITE - 6) {
                    this.sweepFired = 1;
                    this.sweepDamage(sl);
                }
            }
            case ACT_ROAR -> {
                if (this.roarFired == 0 && this.actionTicks <= T_ROAR - 8) {
                    this.roarFired = 1;
                    this.roarEffect(sl);
                }
            }
            default -> { }
        }
    }

    /* ==================== v128: 狼群扑击（飞扑 + 呼朋引伴合并） ==================== */

    /** 第一步：长嗥（播 roar 动作）→ 到点召狗并锁定全体 */
    private void startPackPounce(ServerLevel sl, LivingEntity target) {
        this.act = ACT_ROAR;                 // 用"长嗥"当起手动作
        this.actionTicks = T_ROAR;
        this.pounceFired = 0;
        this.packCd = PACK_CD;
        this.packPounceAt = this.tickCount + PACK_LEAD;
        this.playSoundLocal(sl, SoundEvents.WOLF_GROWL, 1.5f, 0.85f);
    }

    /** 全体（自己 + 场上小狗）锁定同一个目标，PACK_LEAD 之后一起扑 */
    private void lockPack(ServerLevel sl) {
        var target = this.getTarget() instanceof net.minecraft.world.entity.player.Player p ? p : null;
        if (target == null) return;
        int n = 0;
        this.lockSelf(target);
        if (target instanceof net.minecraft.server.level.ServerPlayer sp) {
            net.farmingtales.ftboss.boss.ai.DogPack.launchAll(sl, sp, PACK_LEAD);   // v136: 真狗也一起扑
        }
        for (BengerPupEntity pup : sl.getEntitiesOfClass(BengerPupEntity.class, this.getBoundingBox().inflate(48.0))) {
            if (!pup.isAlive()) continue;
            pup.packPounce(target, PACK_LEAD, PACK_RADIUS, PACK_DAMAGE);
            n++;
        }
        FTBossMod.LOGGER.info("[FTBoss][benger] 狼群锁定：benger + {} 只小狗 同时扑向 {}",
                n, target.getName().getString());
    }

    private void lockSelf(net.minecraft.world.entity.player.Player target) {
        Vec3 d = new Vec3(target.getX() - this.getX(), 0.0, target.getZ() - this.getZ());
        if (d.lengthSqr() < 1.0E-4) d = new Vec3(0, 0, 1);
        d = d.normalize();
        this.jumpX = d.x; this.jumpZ = d.z;
    }

    /** Boss 自己的那一扑（和小狗同一帧起飞） */
    private void selfPackLeap(ServerLevel sl) {
        if (this.packPounceAt < 0) return;
        this.packPounceAt = -1;
        this.setDeltaMovement(this.jumpX * 1.05, 0.55, this.jumpZ * 1.05);
        this.hasImpulse = true;
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WOLF_GROWL, SoundSource.HOSTILE, 1.4f, 0.9f);
    }

    private void packImpact(ServerLevel sl) {
        sl.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.3, this.getZ(), 20, 1.4, 0.2, 1.4, 0.05);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0f, 1.5f);
        int hits = 0;
        for (Player p : sl.players()) {
            if (!p.isAlive() || p.isSpectator()) continue;
            if (p.distanceToSqr(this) > PACK_RADIUS * PACK_RADIUS) continue;
            p.invulnerableTime = 0;
            p.hurt(this.damageSources().mobAttack(this), PACK_DAMAGE);
            this.applyBleedStack(p, 8 * 20);
            net.farmingtales.ftboss.util.BossKnockback.knockback(p, 1.0, this.jumpX, this.jumpZ);
            hits++;
        }
        FTBossMod.LOGGER.info("[FTBoss][benger] 集体扑击落点命中 {} 人（{} 伤害 + 流血）", hits, (int) PACK_DAMAGE);
    }

    /* ==================== v128: 连续下砸 ==================== */

    private void startSlam(ServerLevel sl, LivingEntity target) {
        if (target == null) return;
        this.slamLeft = SLAM_COUNT;
        this.slamTimer = SLAM_HANG;
        this.slamAirborne = false;
        this.slamX = target.getX(); this.slamY = target.getY(); this.slamZ = target.getZ();
        this.slamCd = SLAM_CD;
        this.playSoundLocal(sl, SoundEvents.WOLF_GROWL, 1.6f, 1.1f);
        FTBossMod.LOGGER.info("[FTBoss][benger] 连续下砸起手：连砸 {} 次，首次落点 ({}, {}, {})",
                SLAM_COUNT, String.format("%.1f", this.slamX), String.format("%.1f", this.slamY), String.format("%.1f", this.slamZ));
    }

    /** 每 tick 推进：腾空(预警) → 砸下 → 间隔 → 再腾空 …… */
    private void slamTick(ServerLevel sl) {
        if (--this.slamTimer > 0) {
            /* 腾空期间把玩家当前位置当落点（会追着人砸），并画地面预警圈 */
            if (this.slamAirborne) {
                LivingEntity t = this.getTarget();
                if (t != null) { this.slamX = t.getX(); this.slamY = t.getY(); this.slamZ = t.getZ(); }
                double r = SLAM_RADIUS;
                for (int a = 0; a < 24; a++) {
                    double ang = a / 24.0 * Math.PI * 2.0 + this.tickCount * 0.05;
                    sl.sendParticles(ParticleTypes.CRIT, this.slamX + Math.cos(ang) * r, this.slamY + 0.15,
                            this.slamZ + Math.sin(ang) * r, 1, 0.0, 0.0, 0.0, 0.0);
                }
                sl.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 3, 0.2, 0.1, 0.2, 0.01);
            }
            return;
        }
        if (!this.slamAirborne) {
            /* 起跳：传送到落点上方 → 静止滞空（本模组 Boss 的常规做法: 直接 teleportTo） */
            this.slamAirborne = true;
            this.slamTimer = SLAM_HANG;
            this.teleportTo(this.slamX, this.slamY + 9.0, this.slamZ);
            this.setDeltaMovement(0, 0, 0);
            this.fallDistance = 0.0f;
            this.playSoundLocal(sl, SoundEvents.WOLF_HOWL, 1.4f, 1.25f);
        } else {
            /* 砸落 */
            this.slamAirborne = false;
            this.teleportTo(this.slamX, this.slamY, this.slamZ);
            this.setDeltaMovement(0, 0, 0);
            this.fallDistance = 0.0f;
            this.slamImpact(sl);
            if (--this.slamLeft > 0) {
                this.slamTimer = SLAM_GAP;
            } else {
                this.slamTimer = 0;
            }
        }
    }

    private void slamImpact(ServerLevel sl) {
        sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.slamX, this.slamY + 0.3, this.slamZ, 1, 0.0, 0.0, 0.0, 0.0);
        net.farmingtales.ftboss.util.Fx.particles(sl, this.slamX, this.slamY + 0.3, this.slamZ, 40, ParticleTypes.CLOUD, (float) SLAM_RADIUS, 0.15);
        sl.playSound(null, this.slamX, this.slamY, this.slamZ, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.6f, 0.6f);
        sl.playSound(null, this.slamX, this.slamY, this.slamZ, SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.3f, 0.5f);
        int hits = 0;
        for (Player p : sl.players()) {
            if (!p.isAlive() || p.isSpectator()) continue;
            if (p.distanceToSqr(this.slamX, this.slamY, this.slamZ) > SLAM_RADIUS * SLAM_RADIUS) continue;
            p.invulnerableTime = 0;
            p.hurt(this.damageSources().mobAttack(this), SLAM_DAMAGE);
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 4 * 20, 1));
            this.addTremor(p);   // v133 下砸附带震颤（满 10 层眩晕 1 秒）
            net.farmingtales.ftboss.util.BossKnockback.push(p, 0.0, 0.55, 0.0);
            hits++;
        }
        FTBossMod.LOGGER.info("[FTBoss][benger] 下砸第 {} 击落点命中 {} 人（{} 伤害）",
                SLAM_COUNT - this.slamLeft + 1, hits, (int) SLAM_DAMAGE);
    }

    private void sweepDamage(ServerLevel sl) {
        int hits = 0;
        for (Player p : sl.players()) {
            if (!p.isAlive() || p.isSpectator()) continue;
            if (p.distanceToSqr(this) > SWEEP_RADIUS * SWEEP_RADIUS) continue;
            p.invulnerableTime = 0;
            p.hurt(this.damageSources().mobAttack(this), SWEEP_DAMAGE);
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5 * 20, 0));
            net.farmingtales.ftboss.util.BossKnockback.knockback(p, 1.3, p.getX() - this.getX(), p.getZ() - this.getZ());
            hits++;
        }
        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 1.0, this.getZ(), 6, 1.6, 0.4, 1.6, 0.02);
        if (hits > 0) FTBossMod.LOGGER.info("[FTBoss][benger] 打狗棒横扫命中 {} 人（{} 伤害 + 击飞）", hits, (int) SWEEP_DAMAGE);
    }

    private void roarEffect(ServerLevel sl) {
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WOLF_HOWL, SoundSource.HOSTILE, 2.2f, 0.62f);
        sl.sendParticles(ParticleTypes.NOTE, this.getX(), this.getY() + 2.2, this.getZ(), 24, 0.9, 0.6, 0.9, 0.2);
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 8 * 20, 1));
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 8 * 20, 0));
        int hits = 0;
        for (Player p : sl.players()) {
            if (!p.isAlive() || p.isSpectator()) continue;
            if (p.distanceToSqr(this) > ROAR_RADIUS * ROAR_RADIUS) continue;
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6 * 20, 1));
            p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 6 * 20, 0));
            net.farmingtales.ftboss.util.BossKnockback.knockback(p, 0.7, p.getX() - this.getX(), p.getZ() - this.getZ());
            hits++;
        }
        FTBossMod.LOGGER.info("[FTBoss][benger] 长嗥：影响 {} 名玩家", hits);
    }

    private void summonPups(ServerLevel sl, int n) {
        /* v136 按用户要求：不再召自定义小狗, 改召**整合包里的真狗**(原版狼 / 夸克柴犬 / 猎狐犬…, 见 DogPack) */
        int made = net.farmingtales.ftboss.boss.ai.DogPack.summon(sl, this, this.getX(), this.getY(), this.getZ(), n);
        if (made > 0) {
            sl.playSound(null, this.getX(), this.getY() + 1.0, this.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.5f, 1.5f);
            FTBossMod.LOGGER.info("[FTBoss][benger] 唤来 {} 只整合包里的狗（场上共 {} 只）", made,
                    net.farmingtales.ftboss.boss.ai.DogPack.count(sl));
        }
        if (true) return;
        for (int i = 0; i < n; i++) {
            BengerPupEntity pup = FTEntities.BENGER_PUP.get().create(sl);
            if (pup == null) break;
            double ang = this.getRandom().nextDouble() * Math.PI * 2.0;
            double x = this.getX() + Math.cos(ang) * 2.6, z = this.getZ() + Math.sin(ang) * 2.6;
            pup.moveTo(x, this.getY(), z, this.getRandom().nextFloat() * 360.0f, 0.0f);
            pup.setTarget(this.getTarget());
            pup.setOwner(this.getUUID());
            sl.addFreshEntity(pup);
            sl.sendParticles(ParticleTypes.POOF, x, this.getY() + 0.6, z, 16, 0.3, 0.4, 0.3, 0.05);
            sl.playSound(null, x, this.getY() + 0.8, z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.3f, 1.6f);
        }
    }

    private void playSoundLocal(ServerLevel sl, SoundEvent ev, float vol, float pitch) {
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), ev, SoundSource.HOSTILE, vol, pitch);
    }

    /** 给玩家看的"生效了"提示（actionbar，和庄园那些 Boss 一致） */
    private void tell(ServerLevel sl, String msg) {
        for (ServerPlayer p : sl.players()) {
            if (p.distanceToSqr(this) < 40.0 * 40.0) {
                p.displayClientMessage(net.minecraft.network.chat.Component.literal(msg), true);
            }
        }
    }

    /* ==================== v133: 二阶段 / 流血叠层 / 下砸震颤 ==================== */

    /** 咬中叠加流血层数：一阶段最多 5 层、二阶段最多 10 层（层数 = amp+1） */
    private void applyBleedStack(LivingEntity le, int ticks) {
        int maxAmp = (this.phase == 2) ? 9 : 4;
        var own = net.farmingtales.ftboss.effect.FTEffects.BLEED.get();
        int cur = (own != null && le.hasEffect(own)) ? le.getEffect(own).getAmplifier() : -1;
        int amp = Math.min(maxAmp, cur + 1);
        ExtraDebuffs.bleed(le, ticks, amp);
        if (le instanceof Player pl) net.farmingtales.ftboss.boss.ai.BleedLink.link(pl, this);
    }

    /** 下砸给玩家叠一层【震颤】；满 10 层由 TremorEffect 自己结算眩晕并清零 */
    private void addTremor(Player p) {
        var eff = net.farmingtales.ftboss.effect.FTEffects.TREMOR.get();
        if (eff == null) return;
        int cur = p.hasEffect(eff) ? p.getEffect(eff).getAmplifier() : -1;
        int amp = Math.min(9, cur + 1);
        p.addEffect(new MobEffectInstance(eff, net.farmingtales.ftboss.effect.TremorEffect.TICKS, amp, false, true, true));
        FTBossMod.LOGGER.info("[FTBoss][benger] 下砸震颤 -> {} 叠到 {} 层", p.getName().getString(), amp + 1);
    }

    /* ==================== v133: 大狗叫！ / 野生狗奶 ==================== */

    /** 大狗叫！：{} 格内**全部生物**眩晕 + 召 2 只小狗 */
    private void barkAoe(ServerLevel sl) {
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.2f, 0.8f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WOLF_GROWL, SoundSource.HOSTILE, 2.0f, 0.65f);
        sl.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 1.6, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        net.farmingtales.ftboss.util.Fx.particles(sl, this.getX(), this.getY() + 1.2, this.getZ(), 60, ParticleTypes.NOTE, (float) BARK_RADIUS, 0.6);
        int n = 0;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(BARK_RADIUS))) {
            if (le == this || le instanceof BengerPupEntity || !le.isAlive()) continue;
            if (le instanceof Player p) {
                this.stunPlayer(p, BARK_STUN);
                p.hurt(this.damageSources().mobAttack(this), 8.0f);
            } else {
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BARK_STUN, 9));
                le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, BARK_STUN, 2));
                le.setDeltaMovement(0, le.getDeltaMovement().y, 0);
            }
            n++;
        }
        this.summonPups(sl, this.phase == 2 ? 4 : 2);
        sl.playSound(null, this.getX(), this.getY() + 1.0, this.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.6f, 1.4f);
        FTBossMod.LOGGER.info("[FTBoss][benger] 大狗叫！命中 {} 个生物（眩晕 {} 秒）+ 召 2 只小狗", n, BARK_STUN / 20);
    }

    /** 野生狗奶：附魔金苹果三件套 + 血王子 IV（armamentarium:damon_effect）+ 当场进入虚影 */
    private void drinkMilk(ServerLevel sl) {
        this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, MILK_TICKS, 3));       // 吸收 IV（附魔金苹果）
        this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30 * 20, 1));        // 生命恢复 II
        this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, MILK_TICKS * 3, 0)); // 抗火（附魔金苹果 5 分钟）
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, MILK_TICKS, 1)); // 抗性 II（喝奶时的护体）
        /* 血王子 IV = armamentarium:damon_effect（整合包里没有"血王子"这个名字的套装效果, 沿用 v80 起就在用的那一只） */
        try {
            var damon = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS
                    .getValue(new net.minecraft.resources.ResourceLocation("armamentarium", "damon_effect"));
            if (damon != null) {
                if (this.hasEffect(damon)) this.removeEffect(damon);
                this.addEffect(new MobEffectInstance(damon, MILK_TICKS, 3));               // amplifier 3 = IV 级
            } else {
                FTBossMod.LOGGER.warn("[FTBoss][benger] 没找到 armamentarium:damon_effect, 血王子 IV 跳过");
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][benger] 血王子效果施加失败: {}", t.toString());
        }
        /* 当场进入虚影（复用 v132 的那套：无敌 + 解除索敌） */
        this.intangLeft = Math.max(this.intangLeft, INTANG_TICKS);
        this.setInvulnerable(true);
        for (var m : sl.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, this.getBoundingBox().inflate(64.0))) {
            if (m != this && m.getTarget() == this) m.setTarget(null);
        }
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_DRINK, SoundSource.HOSTILE, 1.6f, 0.8f);
        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.4, this.getZ(), 30, 1.0, 1.0, 1.0, 0.2);
        sl.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 1.2, this.getZ(), 30, 1.2, 1.4, 1.2, 0.15);
        FTBossMod.LOGGER.info("[FTBoss][benger] 野生狗奶：附魔金苹果（吸收 IV/再生 II/抗火）+ 血王子 IV + 虚影 {} 秒", INTANG_TICKS / 20);
    }

    /* ==================== v132: 每 25 秒 3 秒"不可选中"的无敌 ==================== */

    /** 期间：免疫一切非穿透伤害 + 让所有把它当目标的生物失去目标 + 一圈虚影粒子 */
    private void intangTick(ServerLevel sl) {
        if (this.intangLeft > 0) {
            this.intangLeft--;
            this.setInvulnerable(true);
            this.intangibleParticles(sl);
            if (this.intangLeft % 10 == 0 && this.intangLeft > 0) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 1.6f);
            }
            if (this.intangLeft == 0) {
                this.setInvulnerable(false);
                sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 1.0, this.getZ(), 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 24, 1.0, 0.8, 1.0, 0.04);
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WOLF_GROWL, SoundSource.HOSTILE, 1.4f, 0.7f);
                FTBossMod.LOGGER.info("[FTBoss][benger] 虚影结束（重新可以被选中）");
            }
            return;
        }
        if (--this.intangCd <= 0) {
            this.intangCd = INTANG_CD;
            this.intangLeft = INTANG_TICKS;
            this.setInvulnerable(true);
            this.setTarget(null);
            /* 让所有正在打它的生物也失去目标（"无法选中"） */
            int n = 0;
            for (var m : sl.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, this.getBoundingBox().inflate(64.0))) {
                if (m != this && m.getTarget() == this) { m.setTarget(null); n++; }
            }
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.4f, 1.4f);
            sl.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 1.2, this.getZ(), 40, 1.2, 1.4, 1.2, 0.15);
            FTBossMod.LOGGER.info("[FTBoss][benger] 进入虚影状态：{} 秒不可选中/无敌（解除 {} 个索敌）", INTANG_TICKS / 20, n);
        }
    }

    /** 渲染器据此把模型画成半透明（虚影状态） */
    public boolean isIntangible() {
        return this.isAlive() && (this.level().isClientSide
                ? this.entityData.get(DATA_ACTION).getBoolean("bengerIntangible") : this.intangLeft > 0);
    }

    private void syncIntangibleState() {
        if (this.level().isClientSide) return;
        boolean intangible = this.intangLeft > 0 && this.isAlive();
        CompoundTag previous = this.entityData.get(DATA_ACTION);
        if (previous.getBoolean("bengerIntangible") == intangible) return;
        CompoundTag next = previous.copy();
        next.putBoolean("bengerIntangible", intangible);
        this.entityData.set(DATA_ACTION, next);
    }

    private void intangibleParticles(ServerLevel sl) {
        if (this.intangLeft <= 0) return;
        for (int point = 0; point < 12; point++) {
            double angle = point * Math.PI / 6.0 + this.tickCount * 0.18;
            double radius = 1.35 + 0.12 * Math.sin(this.tickCount * 0.3);
            double height = 0.3 + point % 3 * 0.65;
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX() + Math.cos(angle) * radius,
                    this.getY() + height, this.getZ() + Math.sin(angle) * radius, 1, 0, 0, 0, 0.005);
        }
        sl.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 6, 0.8, 0.8, 0.8, 0.03);
    }

    /** 虚影期间免疫一切**非穿透**伤害（/kill、虚空这类 BYPASSES_INVULNERABILITY 照常生效） */
    @Override
    public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source) {
        return super.isInvulnerableTo(source) || this.intangLeft > 0;
    }

    /* ==================== v130: 撕咬钳制（咬住持续伤害 + 自身回复） ==================== */

    private void startClamp(ServerLevel sl, net.minecraft.world.entity.player.Player p) {
        this.clampTarget = p;
        this.clampTicks = CLAMP_TICKS;
        this.clampHealTick = 0;
        this.clampCd = CLAMP_CD;
        this.clampX = p.getX(); this.clampY = p.getY(); this.clampZ = p.getZ();
        this.act = ACT_BITE; this.actionTicks = T_BITE; this.sweepFired = 1;
        this.setDeltaMovement((p.getX() - this.getX()) * 0.12, 0.12, (p.getZ() - this.getZ()) * 0.12);   // 前冲一口(刺击感)
        this.hasImpulse = true;
        this.playSoundLocal(sl, SoundEvents.WOLF_GROWL, 1.6f, 0.75f);
        FTBossMod.LOGGER.info("[FTBoss][benger] 撕咬钳制：咬住 {}（{} 秒，每秒 {} 伤害 + 自身回 {}）",
                p.getName().getString(), CLAMP_TICKS / 20, (int) CLAMP_DPS, (int) CLAMP_HEAL);
    }

    /** 每 tick：把玩家按在嘴边（客户端权威 → 硬拉回锚点）、每秒结算一次伤害与自愈 */
    private void clampTick(ServerLevel sl) {
        if (this.clampTicks <= 0 || this.clampTarget == null) return;
        var p = this.clampTarget;
        if (!p.isAlive() || p.isSpectator() || this.distanceTo(p) > 6.0) { this.clampTicks = 0; this.clampTarget = null; return; }
        if (p instanceof net.minecraft.server.level.ServerPlayer sp) { sp.connection.teleport(this.clampX, this.clampY, this.clampZ, p.getYRot(), p.getXRot()); }
        p.hurtMarked = true;
        p.fallDistance = 0.0f;
        sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR, p.getX(), p.getY() + 1.0, p.getZ(), 3, 0.3, 0.3, 0.3, 0.02);
        if (--this.clampTicks % 20 == 0 || this.clampTicks <= 0) {
            p.invulnerableTime = 0;
            p.hurt(this.damageSources().mobAttack(this), CLAMP_DPS);
            this.applyBleedStack(p, 6 * 20);
            if (this.getHealth() < this.getMaxHealth()) {
                this.heal(CLAMP_HEAL);
            }
            sl.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 1.2f, 0.8f);
        }
        if (this.clampTicks <= 0) {
            FTBossMod.LOGGER.info("[FTBoss][benger] 撕咬钳制结束（{} 剩余血量 {}）",
                    p.getName().getString(), (int) p.getHealth());
            this.clampTarget = null;
        }
    }

    /* ==================== v130: 冲撞（伤害 + 眩晕） ==================== */

    private void startCharge(ServerLevel sl, LivingEntity target) {
        net.minecraft.world.phys.Vec3 d = new net.minecraft.world.phys.Vec3(target.getX() - this.getX(), 0, target.getZ() - this.getZ());
        if (d.lengthSqr() < 1.0E-4) d = new net.minecraft.world.phys.Vec3(0, 0, 1);
        d = d.normalize();
        this.chargeX = d.x; this.chargeZ = d.z;
        this.chargeTicks = 24;
        this.chargeFired = 0;
        this.chargeCd = CHARGE_CD;
        this.act = ACT_LEAP; this.actionTicks = 24; this.pounceFired = 1;
        this.getNavigation().stop();
        this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        float yaw = (float) Math.toDegrees(Math.atan2(-this.chargeX, this.chargeZ));
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
        this.playSoundLocal(sl, SoundEvents.RAVAGER_ROAR, 1.4f, 1.2f);
        FTBossMod.LOGGER.info("[FTBoss][benger] 追猎突击起手：8 tick 方向预警 + 16 tick 冲刺（{} 伤害 + 眩晕 {} 秒）", (int) CHARGE_DAMAGE, CHARGE_STUN / 20);
    }

    private void chargeTick(ServerLevel sl) {
        if (this.chargeTicks <= 0) return;
        if (!this.isAlive()) {
            this.finishCharge();
            return;
        }
        this.getNavigation().stop();
        float yaw = (float) Math.toDegrees(Math.atan2(-this.chargeX, this.chargeZ));
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
        if (this.chargeTicks > 16) {
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
            if (this.chargeTicks % 2 == 0) {
                var warning = new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(1.0f, 0.55f, 0.08f), 1.5f);
                for (int point = 1; point <= 12; point++) {
                    sl.sendParticles(warning, this.getX() + this.chargeX * point,
                            this.getY() + 0.2, this.getZ() + this.chargeZ * point, 1, 0.08, 0.02, 0.08, 0);
                }
                sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.0, this.getZ(), 8, 0.6, 0.5, 0.6, 0.02);
            }
            this.chargeTicks--;
            return;
        }
        if (this.chargeTicks == 16) {
            this.playSoundLocal(sl, SoundEvents.PLAYER_ATTACK_SWEEP, 2.0f, 0.6f);
            sl.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.6, this.getZ(), 24, 0.7, 0.4, 0.7, 0.12);
            FTBossMod.LOGGER.info("[FTBoss][benger] 追猎突击冲刺生效");
        }
        var movement = new net.minecraft.world.phys.Vec3(this.chargeX * 1.6, 0, this.chargeZ * 1.6);
        if (sl.getBlockCollisions(this, this.getBoundingBox().expandTowards(movement)).iterator().hasNext()) {
            sl.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.5, this.getZ(), 16, 0.6, 0.3, 0.6, 0.06);
            FTBossMod.LOGGER.info("[FTBoss][benger] 追猎突击遇障碍停止");
            this.finishCharge();
            return;
        }
        this.setDeltaMovement(movement.x, this.getDeltaMovement().y, movement.z);
        this.hasImpulse = true;
        sl.sendParticles(ParticleTypes.CLOUD, this.getX() - this.chargeX, this.getY() + 0.3, this.getZ() - this.chargeZ, 6, 0.45, 0.15, 0.45, 0.04);
        sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.0, this.getZ(), 4, 0.4, 0.4, 0.4, 0.03);
        var sweptBox = this.getBoundingBox().expandTowards(movement).inflate(0.65, 0.3, 0.65);
        if (this.chargeFired == 0) {
            for (Player p : sl.players()) {
                if (!p.isAlive() || p.isSpectator() || p.isCreative()) continue;
                if (!sweptBox.intersects(p.getBoundingBox()) || !this.getSensing().hasLineOfSight(p)) continue;
                p.invulnerableTime = 0;
                if (!p.hurt(this.damageSources().mobAttack(this), CHARGE_DAMAGE)) continue;
                net.farmingtales.ftboss.util.BossKnockback.push(p, this.chargeX * 1.4, 0.45, this.chargeZ * 1.4);
                this.stunPlayer(p, CHARGE_STUN);
                this.chargeFired = 1;
                sl.sendParticles(ParticleTypes.FLASH, p.getX(), p.getY() + 0.9, p.getZ(), 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.CRIT, p.getX(), p.getY() + 0.9, p.getZ(), 24, 0.6, 0.6, 0.6, 0.15);
                sl.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.4f, 0.6f);
                FTBossMod.LOGGER.info("[FTBoss][benger] 冲撞命中 {}（{} 伤害 + 眩晕 {} 秒）",
                        p.getName().getString(), (int) CHARGE_DAMAGE, CHARGE_STUN / 20);
                break;
            }
        }
        if (--this.chargeTicks <= 0) this.finishCharge();
    }

    private void finishCharge() {
        this.chargeTicks = 0;
        this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        if (this.act == ACT_LEAP) {
            this.act = ACT_NONE;
            this.actionTicks = 0;
        }
    }

    /* ==================== v130: 眩晕（锚点硬定身, 与雪黎那套同一做法） ==================== */

    public void stunPlayer(Player p, int ticks) {
        this.stunTicks.put(p.getUUID(), ticks);
        this.stunAnchor.put(p.getUUID(), p.position());
    }

    private void stunTick(ServerLevel sl) {
        if (this.stunTicks.isEmpty()) return;
        var it = this.stunTicks.entrySet().iterator();
        while (it.hasNext()) {
            var en = it.next();
            net.minecraft.server.level.ServerPlayer p = sl.getServer().getPlayerList().getPlayer(en.getKey());
            int left = en.getValue() - 1;
            if (p == null || !p.isAlive() || left <= 0) { it.remove(); this.stunAnchor.remove(en.getKey()); continue; }
            en.setValue(left);
            var a = this.stunAnchor.get(en.getKey());
            if (a != null) {
                p.connection.teleport(a.x, a.y, a.z, p.getYRot(), p.getXRot());
                p.hurtMarked = true;
            }
            if (left % 5 == 0) {
                sl.sendParticles(ParticleTypes.CRIT, p.getX(), p.getY() + 1.9, p.getZ(), 4, 0.25, 0.2, 0.25, 0.02);
            }
        }
    }

    /* ==================== 受击 / 近战 ==================== */

    @Override
    public boolean hurt(DamageSource source, float amount) {
        amount = net.farmingtales.ftboss.boss.ai.BossDamageGate.gate(this, source, amount);
        if (amount <= 0.0f) return false;
        boolean hit = super.hurt(source, amount);
        if (hit) {
            net.farmingtales.ftboss.boss.ai.BossDamageGate.onHit(this);
            this.bossBar.setVisible(true);
            if (!this.level().isClientSide && this.act == ACT_NONE && this.getRandom().nextFloat() < 0.6f) {
                this.act = ACT_HURT; this.actionTicks = T_HURT;
            }
        }
        return hit;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity le) {
            this.applyBleedStack(le, 8 * 20);
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3 * 20, 0));
            net.farmingtales.ftboss.util.BossKnockback.knockback(le, 0.8, le.getX() - this.getX(), le.getZ() - this.getZ());
            if (!this.level().isClientSide && this.act == ACT_NONE) {
                /* v133: 近战咬中有 35% 直接转成【撕咬钳制】(参考焰魔刺击: 扑上去咬住不松口) ——
                   原来只有 skillTick 里"距离 ≤ 4 格"才触发, 实战里常被别的动作挤掉, 玩家看不到 */
                if (le instanceof net.minecraft.world.entity.player.Player pl && this.clampCd <= 0
                        && this.level() instanceof ServerLevel slc && this.getRandom().nextFloat() < 0.35f) {
                    this.startClamp(slc, pl);
                } else {
                    this.act = ACT_BITE; this.actionTicks = T_BITE; this.sweepFired = 1;
                }
            }
        }
        return hit;
    }

    /* ==================== 血条 / 死亡 ==================== */

    @Override
    public void startSeenByPlayer(ServerPlayer p) {
        super.startSeenByPlayer(p);
        this.bossBar.addPlayer(p);
        BossBars.track(this, this.bossBar);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer p) {
        super.stopSeenByPlayer(p);
        this.bossBar.removePlayer(p);
    }

    public ServerBossEvent getBossBar() { return this.bossBar; }

    @Override
    public void die(DamageSource source) {
        if (this.level() instanceof ServerLevel sl) {
            net.farmingtales.ftboss.sound.BossMusic.release(sl, this);
            BossBars.untrack(this, this.bossBar);
            FTBossMod.LOGGER.info("[FTBoss][benger] 被击败（{} 的伤害）", source.getMsgId());
        }
        this.act = ACT_DEATH;
        super.die(source);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level() instanceof ServerLevel sl) {
            BossBars.untrack(this, this.bossBar);
            net.farmingtales.ftboss.sound.BossMusic.release(sl, this);
        }
        super.remove(reason);
    }

    /* ==================== NBT ==================== */

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("bengerRaged", this.isRaged());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("bengerRaged")) this.setRaged(tag.getBoolean("bengerRaged"));
    }

    /* ==================== GeckoLib ==================== */

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "benger_ctrl", 4, this::predicate));
    }

    /** 动画优先级：死亡 > 飞扑 > 长嗥 > 咬/横扫 > 受击 > 跑 > 走 > 待机（动作优先，受击让位） */
    private PlayState predicate(AnimationState<BengerEntity> state) {
        this.actionState.prepare(state);
        if (this.isDeadOrDying()) { state.setAnimation(DEATH); return PlayState.CONTINUE; }
        int visualAction = this.actionState.active() ? Integer.parseInt(this.actionState.name()) : ACT_NONE;
        switch (visualAction) {
            case ACT_LEAP -> { state.setAnimation(POUNCE); return PlayState.CONTINUE; }
            case ACT_SWEEP -> { state.setAnimation(SWEEP); return PlayState.CONTINUE; }
            case ACT_PACK, ACT_ROAR, ACT_BARK -> { state.setAnimation(ROAR); return PlayState.CONTINUE; }
            case ACT_MILK -> { state.setAnimation(IDLE); return PlayState.CONTINUE; }
            case ACT_BITE -> { state.setAnimation(BITE); return PlayState.CONTINUE; }
            case ACT_HURT -> { state.setAnimation(HURT); return PlayState.CONTINUE; }
            default -> { }
        }
        if (state.isMoving()) {
            state.setAnimation(this.getDeltaMovement().horizontalDistanceSqr() > 0.02 ? RUN : WALK);
            return PlayState.CONTINUE;
        }
        state.setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.geoCache; }

    @Override
    public double getTick(Object o) { return this.tickCount; }
}
