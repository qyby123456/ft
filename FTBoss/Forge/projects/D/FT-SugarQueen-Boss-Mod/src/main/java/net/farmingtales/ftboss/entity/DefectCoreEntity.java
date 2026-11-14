package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBars;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
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
 * 故障机器人 Defect Core(2026-09-11 新增, 第 6 个 Boss, Slay the Spire 故障机器人机制)。
 *
 * <p><b>M1(骨架)</b>: 404 血 / 三阶段阈值(70% / 35%) / 悬浮索敌 / 血条 / 限伤接入 / 负面免疫 /
 * 死亡核心超载爆炸(不毁地形)。
 * <p><b>M2~M5 待做</b>: 充能球系统(3→11 槽)、故障回声、故障球封印、阶段技能、glowmask 精修。
 *
 * <p>状态全部放在**实体实例字段**上(不像女王/屠夫用静态大脑), 因此天然支持玩家召唤多只。
 */
public class DefectCoreEntity extends Monster implements GeoAnimatable, net.farmingtales.ftboss.integ.spells.RegularBossCastHost {

    /**
     * 2026-09-11 按用户要求"去除 Boss 燃烧动画避免阻碍观察":
     * 着火时不再往模型上叠火焰图层(火焰伤害照旧), 免得模型/动画被挡住看不清。
     */
    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    /** 阶段阈值 */
    public static final float PHASE2_AT = 0.70F;
    public static final float PHASE3_AT = 0.35F;
    /** 核心超载爆炸威力(不毁地形) */
    public static final float OVERLOAD_POWER = 4.0F;    // 8 → ×0.5(v77)

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.defect_core.idle");
    private static final RawAnimation CAST = RawAnimation.begin().thenPlay("animation.defect_core.cast");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.defect_core.hurt");
    private static final RawAnimation PHASE = RawAnimation.begin().thenPlay("animation.defect_core.phase_shift");
    private static final RawAnimation SUMMON = RawAnimation.begin().thenPlay("animation.defect_core.orb_summon");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.defect_core.death_overload");
    private static final RawAnimation SIGNATURE_CAST = RawAnimation.begin().thenPlay("animation.defect_core.signature_cast");
    private static final RawAnimation SIGNATURE_SELF = RawAnimation.begin().thenPlay("animation.defect_core.signature_self_cast");
    // 五种球的专属攻击动画(2026-09-11)
    private static final RawAnimation CAST_GLASS = RawAnimation.begin().thenPlay("animation.defect_core.cast_glass");
    private static final RawAnimation CAST_LIGHTNING = RawAnimation.begin().thenPlay("animation.defect_core.cast_lightning");
    private static final RawAnimation CAST_FROST = RawAnimation.begin().thenPlay("animation.defect_core.cast_frost");
    private static final RawAnimation CAST_ION = RawAnimation.begin().thenPlay("animation.defect_core.cast_ion");
    private static final RawAnimation CAST_DARK = RawAnimation.begin().thenPlay("animation.defect_core.cast_dark");
    /** 位移时的前倾悬浮(2026-09-11 重做动画时新增) */
    private static final RawAnimation FLOAT_MOVE = RawAnimation.begin().thenLoop("animation.defect_core.float_move");
    /** 【回想形态】开启(高频抖动+核心过载, 2026-09-11 新增) */
    private static final RawAnimation ECHO = RawAnimation.begin().thenPlay("animation.defect_core.echo");
    /** 转向(2026-09-11 按用户要求"转向动作要做"): 左转手写、右转由动画模块镜像生成, 循环播放 */
    private static final RawAnimation TURN_L = RawAnimation.begin().thenLoop("animation.defect_core.turn_l");
    private static final RawAnimation TURN_R = RawAnimation.begin().thenLoop("animation.defect_core.turn_r");
    /** 落地衰弱: 超能光束之后塌在地上喘气。JSON 里只有 `exhausted`(1.6 秒), **没有** landing_weak ——
     *  v112 修复: 原来写 `thenPlay("animation.defect_core.landing_weak")` 播放名不存在,
     *  于是 6 秒的落地衰弱期毫无动作表现。改成循环播 exhausted(1.6s × 约 3.75 次 = 6s)。 */
    private static final RawAnimation LANDING_WEAK = RawAnimation.begin().thenLoop("animation.defect_core.exhausted");
    /** 超能光束(2026-09-11 新增技能): 0.75s 蓄力 + 3s 引导 + 收势 */
    private static final RawAnimation BEAM = RawAnimation.begin().thenPlay("animation.defect_core.beam");
    /** 光束动画总长 = JSON 的 animation_length 14.25s = 285t(v112 修正: 原 245 会提前 2 秒切回 idle) */
    private static final double T_BEAM = 285.0;
    /** 动画时长(tick), **必须与 JSON 里的 animation_length 对齐**(1s = 20t), 否则动画会被提前打断。
     *  v112 修正: phase_shift 的 JSON 长度是 2.6s = **52t**(原 32 会让变形动画被腰斩)。 */
    private static final double T_CAST = 20.0, T_HURT = 10.0, T_PHASE = 52.0, T_SUMMON = 17.0, T_ECHO = 28.0;
    /** 超能光束后的**落地衰弱**时长(2026-09-11 按用户要求: 光束用完会落地并虚弱一小段) */
    public static final int EXHAUST_TICKS = 120;      // 2026-09-11 按用户要求: 落地削弱 6 秒

    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossBar;
    /** 充能球控制器(结构同沙耶的茶芽庇佑) */
    private final net.farmingtales.ftboss.boss.ai.DefectOrbs orbs = new net.farmingtales.ftboss.boss.ai.DefectOrbs(this);

    /** 当前阶段 1/2/3 */
    private int phase = 1;
    /** 悬浮相位(8字形缓慢漂移) */
    private double hoverPhase = 0.0;
    /** 基础攻击节奏(2026-09-11: 保证玩家总能看到"转向 + 攻击动画") */
    private int basicAttackCd = 60;
    private int orbsCastCooldown = 0;
    /** 施法动画计时(放充能球技能时置起) */
    private int castTicks = 0;
    /** 本次要播的攻击动画(按球种区分) */
    private RawAnimation castAnim = CAST;
    /** 本次施法动画的总时长(与 JSON 的 animation_length 对齐, 用于"让动作播完"的判定) */
    private int castAnimTicks = (int) T_CAST;
    /** 抬手(生成充能球)动画计时 */
    private int summonTicks = 0;
    private static final net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.nbt.CompoundTag> DATA_ACTION =
            net.minecraft.network.syncher.SynchedEntityData.defineId(DefectCoreEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.COMPOUND_TAG);
    private final net.farmingtales.ftboss.boss.ai.BossActionState actionState =
            new net.farmingtales.ftboss.boss.ai.BossActionState(this, DATA_ACTION);
    private static final net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.nbt.CompoundTag> DATA_SIGNATURE_CAST =
            net.minecraft.network.syncher.SynchedEntityData.defineId(DefectCoreEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.COMPOUND_TAG);
    private final net.farmingtales.ftboss.boss.ai.BossActionState signatureCast =
            new net.farmingtales.ftboss.boss.ai.BossActionState(this, DATA_SIGNATURE_CAST);
    private String castAction = "cast";
    private String lastVisualAction = "";
    private int lastVisualTicks;
    private net.minecraft.world.entity.LivingEntity pendingMelee;
    private long meleeReleaseAt;
    private boolean pendingBasicMagic;

    private static final net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.nbt.CompoundTag> DATA_ORB_CUE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(DefectCoreEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.COMPOUND_TAG);

    public void showOrbCue(String stage, OrbType type, int orbId, int duration) {
        if (this.level().isClientSide) return;
        net.minecraft.nbt.CompoundTag cue = new net.minecraft.nbt.CompoundTag();
        cue.putString("stage", stage);
        cue.putString("type", type.id);
        cue.putInt("orb", orbId);
        cue.putInt("duration", Math.max(1, duration));
        cue.putLong("start", this.level().getGameTime());
        this.entityData.set(DATA_ORB_CUE, cue);
    }

    public net.minecraft.nbt.CompoundTag orbCue() {
        return this.entityData.get(DATA_ORB_CUE);
    }

    public String orbLinkStage() {
        var cue = orbCue();
        long age = this.level().getGameTime() - cue.getLong("start");
        return age >= 0 && age < cue.getInt("duration") ? cue.getString("stage") : "";
    }

    public net.minecraft.world.phys.Vec3 orbAnchor(boolean palm) {
        double yaw = Math.toRadians(this.getYRot());
        double forward = palm ? 1.25 : 0.34;
        return this.position().add(-Math.sin(yaw) * forward, palm ? 2.05 : 2.1, Math.cos(yaw) * forward);
    }

    public void cancelOrbCastVisual() {
        this.castTicks = 0;
    }

    public boolean isActionLocked() {
        return this.exhaustedTicks > 0 || this.beamAnimTicks > 0 || this.phaseAnimTicks > 0
                || this.echoAnimTicks > 0 || this.castTicks > 0 || this.summonTicks > 0;
    }
    /** 胸口核心当前颜色 = 最近一颗球的种类(2026-09-11 按用户要求) —— **必须同步给客户端**, 否则贴图永远按默认渲染 */
    private static final net.minecraft.network.syncher.EntityDataAccessor<String> DATA_CORE_TINT =
            net.minecraft.network.syncher.SynchedEntityData.defineId(DefectCoreEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.STRING);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ACTION, new net.minecraft.nbt.CompoundTag());
        this.entityData.define(DATA_SIGNATURE_CAST, new net.minecraft.nbt.CompoundTag());
        this.entityData.define(DATA_ORB_CUE, new net.minecraft.nbt.CompoundTag());
        this.entityData.define(DATA_CORE_TINT, net.farmingtales.ftboss.entity.OrbType.GLASS.id);
    }
    @Override
    public void startSignatureCast(net.farmingtales.ftboss.util.BossSpellRules.Kind kind, int castTicks) {
        this.signatureCast.start(net.farmingtales.ftboss.integ.spells.RegularBossCastHost.action(kind),
                net.farmingtales.ftboss.integ.spells.RegularBossCastHost.visualTicks(castTicks));
    }

    /** 阶段突破动画计时 */
    private int phaseAnimTicks = 0;
    /** 【回想形态】开启动画计时 */
    private int echoAnimTicks = 0;
    /** 超能光束引导动画计时 */
    private int beamAnimTicks = 0;
    /** 超能光束蓄力中(2026-09-11): 期间**站立不动**(tickHover 直接返回), 但仍会转身面向玩家 */
    private boolean beamCharging = false;

    /** 由 DefectOrbs 在蓄力开始/结束时调用 */
    public void setBeamCharging(boolean charging) {
        this.beamCharging = charging;
    }

    /** 2026-09-12 按用户要求: 机器人**免疫自己充能球的伤害**(电球范围不再误伤到自己/召唤物) */
    @Override
    public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source) {
        if (source.getDirectEntity() instanceof ChargedOrbEntity) return true;
        if (source.getEntity() instanceof net.minecraft.world.entity.LivingEntity le
                && net.farmingtales.ftboss.boss.ai.BossAggro.isBossSide(le)) return true;
        return super.isInvulnerableTo(source);
    }

    /** 落地衰弱计时(>0 = 已落地且虚弱: 不移动、不出手、受到 1.5× 伤害) */
    private int exhaustedTicks = 0;
    /** 虚弱期要回复的**总血量**(落地削弱固定 40 点; 传比例时按最大生命折算) */
    private float exhaustHealTotal = 0f;
    /** 转向动画状态(转身时循环播, 停下来回 idle) */
    private int turnTicks = 0;
    private boolean turningLeft = false;
    private float lastYaw = 0f;

    public DefectCoreEntity(EntityType<? extends DefectCoreEntity> type, Level level) {
        super(type, level);
        this.xpReward = 120;
        this.setNoGravity(true);                 // 悬浮核心
        this.setPersistenceRequired();
        this.bossBar = new ServerBossEvent(Component.translatable("boss.ftboss.defect_core"),
                BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
        BossBars.track(this, this.bossBar);
    }

    /** 兜底: 无论玩家跑多远都**不自然消失**(只有被打死或"连死怜悯"才会离场) */
    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    /**
     * 2026-09-11 修"血条又卡在天上": 之前只在 {@code die()} 里注销血条, 但**连死怜悯清场走的是 discard()**,
     * 不走 die() → 客户端收不到 REMOVE 包, 血条就永远挂着。这里补上所有移除路径(含区块卸载/指令清除)。
     */
    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide) {
            BossBars.untrack(this, this.bossBar);
            // 2026-09-11: 被 discard(怜悯清场)/区块卸载/指令清除这些**不走 die()** 的路径也要放掉 BGM,
            // 否则客户端那条音乐流没人叫停 -> "死后音乐有概率不停"。
            if (this.level() instanceof ServerLevel rsl) net.farmingtales.ftboss.sound.BossMusic.release(rsl, this);
        }
        super.remove(reason);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 808.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.ATTACK_DAMAGE, 9.0)      // 2026-09-11 按用户要求提高伤害(原 12)
                .add(Attributes.ATTACK_SPEED, 1.0)
                .add(Attributes.MOVEMENT_SPEED, BossWalkingSpeed.BASE_SPEED)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, net.farmingtales.ftboss.util.BossKnockback.BOSS_RESISTANCE);   // 2026-09-19: 允许被击退
    }

    @Override
    protected void registerGoals() {
        // 悬浮移动由 tick 逐帧控制, 这里只留索敌与朝向
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    public int getPhase() {
        return this.phase;
    }

    /** 充能球控制器(技能全在里面) */
    public net.farmingtales.ftboss.boss.ai.DefectOrbs orbs() {
        return this.orbs;
    }

    /** 放技能时置起施法动画(由 DefectOrbs 调用); 按球种选专属攻击动画, **并按该动画的实际时长置计时** */
    public void playCastAnim(net.farmingtales.ftboss.entity.OrbType type) {
        if (this.castTicks > 0 && this.castAction.equals("cast_" + type.id)) return;
        this.castAction = "cast_" + type.id;
        this.castAnim = switch (type) {
            case GLASS -> CAST_GLASS;
            case LIGHTNING -> CAST_LIGHTNING;
            case FROST -> CAST_FROST;
            case ION -> CAST_ION;
            case DARK -> CAST_DARK;
        };
        // 各球种动画长度不同(玻璃 1.15s / 黑球 1.3s / 其余 1.0s), 计时跟着走才不会中途被切回 idle
        this.castAnimTicks = switch (type) {
            case GLASS -> 23;
            case DARK -> 26;
            default -> (int) T_CAST;
        };
        this.castTicks = this.castAnimTicks;
        this.orbsCastCooldown = 50;      // 告诉基础攻击: 刚放过球技能, 别抢动画
    }

    /** 【回想形态】开启时播一次(由 DefectOrbs 调用) */
    public void playEchoAnim() {
        this.echoAnimTicks = (int) T_ECHO;
        showOrbCue("echo", OrbType.ION, -1, (int) T_ECHO);
    }

    /** 超能光束引导动画(由 DefectOrbs 在开始蓄力时调用) */
    public void playBeamAnim() {
        this.beamAnimTicks = (int) T_BEAM;
    }

    /**
     * 进入**落地衰弱**(2026-09-11 按用户要求"使用超能光束技能后会落地并进入一小段削弱阶段")。
     * 期间: 强制降落到地面、不再漂移、不出手、且受到的伤害 ×1.5 —— 这是玩家的反击窗口。
     */
    public void enterExhausted(int ticks) {
        enterExhausted(ticks, 0f);      // 默认: 固定回 40 点(见下)
    }

    /**
     * 带**回血比例**的落弱版本(【电流相生】用: 虚弱 5 秒内回复 10% 最大生命)。
     * 进入时还会**剥掉全部防御增益** —— 用户要求"虚弱时期内无法防御"。
     */
    public void enterExhausted(int ticks, float regenFraction) {
        this.exhaustedTicks = Math.max(this.exhaustedTicks, ticks);
        showOrbCue("", coreTint(), -1, 1);
        // 2026-09-11 按用户要求: 落地削弱期间**只能回复 40 血**(比例传参时按最大生命折算)
        this.exhaustHealTotal = regenFraction > 0f ? this.getMaxHealth() * regenFraction : 80f;   // 2026-09-11 回复提高: 40 -> 80
        for (var fx : new net.minecraft.world.effect.MobEffect[]{
                net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
                net.minecraft.world.effect.MobEffects.ABSORPTION,
                net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE,
                net.minecraft.world.effect.MobEffects.HEALTH_BOOST}) {
            this.removeEffect(fx);
        }
    }

    /** 是否处于落地衰弱(DefectOrbs 用它停掉补球 / 技能 / 光束) */
    public boolean isExhausted() {
        return this.exhaustedTicks > 0;
    }

    /** 阶段突破时置起专属动画(由 DefectOrbs.unlockPhase 调用) */
    public void playPhaseAnim() {
        this.phaseAnimTicks = (int) T_PHASE;
        showOrbCue("phase", coreTint(), -1, (int) T_PHASE);
    }

    /** 抬手动画: 生成充能球时播(2026-09-11 按用户要求) */
    public void playSummonAnim() {
        this.summonTicks = (int) T_SUMMON;
    }

    /** 胸口核心颜色 = 当前球种(贴图会随之切换; 走同步数据, 客户端才看得到) */
    public net.farmingtales.ftboss.entity.OrbType coreTint() {
        return net.farmingtales.ftboss.entity.OrbType.byId(this.entityData.get(DATA_CORE_TINT));
    }

    public void setCoreTint(net.farmingtales.ftboss.entity.OrbType type) {
        this.entityData.set(DATA_CORE_TINT, type.id);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("core_tint", this.coreTint().id);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("core_tint")) {
            this.setCoreTint(net.farmingtales.ftboss.entity.OrbType.byId(tag.getString("core_tint")));
        }
    }

    // ================= 受击 =================

    @Override
    public boolean hurt(DamageSource source, float amount) {
        float gated = net.farmingtales.ftboss.boss.ai.BossDamageGate.gate(this, source, amount);
        if (gated <= 0F) return false;
        // 2026-09-11 按用户要求: **去掉虚弱期对玩家的增伤**(不再 ×1.5), 虚弱只锁行动不送伤害
        boolean hit = super.hurt(source, gated);
        if (hit) net.farmingtales.ftboss.boss.ai.BossDamageGate.onHit(this);
        return hit;
    }

    // ================= tick =================

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (!this.isAlive()) { this.pendingMelee = null; return; }
        net.farmingtales.ftboss.boss.ai.BossDamageGate.tick(this);
        net.farmingtales.ftboss.boss.ai.TrapBreaker.tick(this);
        // 狂暴阶段(2026-09-11 补: 旧版漏了这一句 —— 导致"狂暴后进入【回想形态】"永远触发不了,
        // 因为 DefectOrbs 读的 BossRage.tierOf() 一直是 0)。故障机器人的狂暴增益见 BossRage: 纯进攻, 不给抗性/再生。
        net.farmingtales.ftboss.boss.ai.BossRage.tickBoss(this);
        // 负面效果免疫(与其他 Boss 同口径: 每 20t 清一次)
        if (this.tickCount % 20 == 0) {
            for (var inst : new java.util.ArrayList<>(this.getActiveEffects())) {
                if (!inst.getEffect().isBeneficial()) this.removeEffect(inst.getEffect());
            }
        }
        tickBossBar();
        tickPhases();
        tickFaceAndTarget();     // 主动索敌 + 明确转身(2026-09-11 修"不会转向")
        tickTurnAnim();          // 转身动作(2026-09-11 按用户要求新增)
        tickHover();
        tickThrusterFx();        // 脚下推进喷流(2026-09-11 按用户要求"改成机器人会飞")
        tickBasicAttack();       // 常驻基础攻击, 保证有攻击动画
        // 充能球: 补球 / 吸收 / 放对应技能(结构同沙耶茶芽庇佑)
        if (this.castTicks > 0) this.castTicks--;
        if (this.phaseAnimTicks > 0) this.phaseAnimTicks--;
        if (this.summonTicks > 0) this.summonTicks--;
        if (this.echoAnimTicks > 0) this.echoAnimTicks--;
        if (this.beamAnimTicks > 0) this.beamAnimTicks--;
        if (this.exhaustedTicks > 0) {
            this.exhaustedTicks--;
            // 2026-09-12 按用户要求: 虚弱期**强制落地** —— 每 tick 下压到地表并贴地(最多 0.9 格/tick)
            if (this.level() instanceof ServerLevel gsl) {
                net.minecraft.core.BlockPos gp = gsl.getHeightmapPos(
                        net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.blockPosition());
                double groundY = gp.getY();
                double cy = this.getY();
                this.setDeltaMovement(0, 0, 0);
                this.fallDistance = 0f;
                if (cy > groundY + 0.02) {
                    net.farmingtales.ftboss.util.BossCollision.move(this,
                            new Vec3(0, Math.max(groundY, cy - 0.9) - this.getY(), 0));
                } else if (cy < groundY - 0.05) {
                    net.farmingtales.ftboss.util.BossCollision.move(this,
                            new Vec3(0, groundY - this.getY(), 0));
                }
            }
            // 【电流相生】的虚弱期: 每 5t 回复总量 = 最大生命 × regenFraction(默认 10% / 5 秒)
            // 每 5t 回一次, 24 次合计 = exhaustHealTotal(落地削弱 = 40 点)
            if (this.tickCount % 5 == 0) {
                this.heal(this.exhaustHealTotal / (EXHAUST_TICKS / 5f));
            }
            if (this.exhaustedTicks == 0 && this.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_POWER_SELECT,
                        SoundSource.HOSTILE, 1.6f, 1.2f);
                this.playPhaseAnim();      // 重新升空的姿态
                /* v182 按用户要求「优化 boss 文本, 非必要减少文字输出」:
                   这是**纯战报** —— 动作本身有音效(BEACON_POWER_SELECT)+升空动画,
                   玩家看得见, 不必再用文字复述一遍。降级为日志(排查时仍在)。 */
                net.farmingtales.ftboss.boss.BossText.flavor("故障机器人",
                        "重新升起 —— 衰弱结束");
                FTBossMod.LOGGER.info("[FTBoss] 故障机器人落地衰弱结束, 重新升空");
            }
        }
        if (this.orbsCastCooldown > 0) this.orbsCastCooldown--;
        // ★ 战斗BGM(2026-09-11 修"没听见 BGM"): BossMusic 必须由 Boss 每 tick 主动 request,
        //   故障机器人之前一次都没调过 -> 自然永远不播。
        net.farmingtales.ftboss.sound.BossMusic.request((ServerLevel) this.level(), this);
        this.orbs.tick();
        syncAction();
    }

    private void syncAction() {
        String visualAction = "";
        int remaining = 0;
        if (this.exhaustedTicks > 0) { visualAction = "exhausted"; remaining = this.exhaustedTicks; }
        else if (this.beamAnimTicks > 0) { visualAction = "beam"; remaining = this.beamAnimTicks; }
        else if (this.phaseAnimTicks > 0) { visualAction = "phase_shift"; remaining = this.phaseAnimTicks; }
        else if (this.echoAnimTicks > 0) { visualAction = "echo"; remaining = this.echoAnimTicks; }
        else if (this.castTicks > 0) { visualAction = this.castAction; remaining = this.castTicks; }
        else if (this.summonTicks > 0) { visualAction = "orb_summon"; remaining = this.summonTicks; }
        if (!visualAction.equals(this.lastVisualAction) || remaining > this.lastVisualTicks) {
            this.actionState.start(visualAction, remaining);
        }
        this.lastVisualAction = visualAction;
        this.lastVisualTicks = remaining;
    }

    /**
     * 主动索敌 + 转身(2026-09-11 按用户反馈"boss没有转向"新增)。
     * <p>原来只靠 {@code LookAtPlayerGoal} + {@code LookControl}, 但本体是**每 tick 传送**的悬浮 Boss,
     * 身体朝向不会被 AI 的转身逻辑带着走 → 现在直接显式写 yRot / yBodyRot / yHeadRot(与屠夫同款写法)。
     */
    private void tickFaceAndTarget() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        var t = this.getTarget();
        if (t == null || !t.isAlive()) {
            ServerPlayer best = null;
            double bd = 48 * 48;
            for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(48))) {
                if (!p.isAlive() || p.isCreative() || p.isSpectator()) continue;
                double d = this.distanceToSqr(p);
                if (d < bd) { bd = d; best = p; }
            }
            if (best != null) this.setTarget(best);
            t = best;
        }
        if (t == null) return;
        double dx = t.getX() - this.getX();
        double dz = t.getZ() - this.getZ();
        if (dx * dx + dz * dz > 0.001) {
            float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            this.yRotO = this.getYRot();
            this.setYRot(yaw);
            this.yBodyRotO = this.yBodyRot;
            this.yBodyRot = yaw;
            this.setYHeadRot(yaw);
        }
        double dy = t.getEyeY() - this.getEyeY();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        this.xRotO = this.getXRot();
        this.setXRot((float) -Math.toDegrees(Math.atan2(dy, Math.max(horiz, 0.01))));
    }

    /**
     * 转身动作(2026-09-11 按用户要求"移动动作和转向动作要做")。
     * 用**每 tick 的 yaw 变化量**判断本体是否在转身: 转就循环播 turn_l / turn_r(身体先倒向内侧 +
     * 躯干与头领先转过去 + 双臂反向配重 + 披风反甩), 停下来自然过渡回 idle。
     */
    private void tickTurnAnim() {
        float dYaw = net.minecraft.util.Mth.wrapDegrees(this.getYRot() - this.lastYaw);
        this.lastYaw = this.getYRot();
        if (Math.abs(dYaw) > 1.2f) {
            this.turnTicks = 6;                 // 转身中持续保持(每 tick 刷新)
            this.turningLeft = dYaw < 0f;       // yaw 减小 = 向左转
        } else if (this.turnTicks > 0) {
            this.turnTicks--;
        }
    }

    /** 常驻基础攻击: 球技能冷却期间也会定期出手(远程奥术弹 / 贴身挥击), 且一定带攻击动画 */
    private void tickBasicAttack() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        if (this.pendingMelee != null) {
            if (this.exhaustedTicks > 0 || this.beamCharging || this.phaseAnimTicks > 0) this.pendingMelee = null;
            else if (sl.getGameTime() >= this.meleeReleaseAt) {
                var victim = this.pendingMelee;
                this.pendingMelee = null;
                sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        this.pendingBasicMagic ? SoundEvents.EVOKER_CAST_SPELL : SoundEvents.IRON_GOLEM_ATTACK,
                        SoundSource.HOSTILE, 1.2f, 1.1f);
                double reach = this.pendingBasicMagic ? 26 : 3.4;
                if (victim.isAlive() && victim.level() == sl && this.distanceToSqr(victim) < reach * reach
                        && this.hasLineOfSight(victim) && net.farmingtales.ftboss.boss.ai.BossTargets.valid(victim)) {
                    if (this.pendingBasicMagic) victim.hurt(sl.damageSources().indirectMagic(this, this), 4.5f + this.phase * 1.5f);
                    else this.doHurtTarget(victim);
                }
            }
            return;
        }
        if (isActionLocked()) return;
        if (this.exhaustedTicks > 0) return;      // 落地衰弱期间不出手
        if (this.beamCharging) return;            // 光束 10 秒蓄力期间也不出手
        var t = this.getTarget();
        if (t == null || !t.isAlive()) return;
        if (this.orbsCastCooldown > 0 || this.phaseAnimTicks > 0) return;
        if (--this.basicAttackCd > 0) return;
        this.basicAttackCd = this.phase >= 3 ? 36 : (this.phase == 2 ? 48 : 60);   // 提高出手频率(原 40/55/70)
        double dist = this.distanceTo(t);
        // 贴脸 -> 机械手挥击; 否则 -> **放一个低环铁魔法**(2026-09-11 按用户要求"技能用铁魔法", 并去掉机器人自身粒子)
        if (dist < 3.4) {
            this.playCastAnim(net.farmingtales.ftboss.entity.OrbType.ION);
            this.pendingMelee = t;
            this.pendingBasicMagic = false;
            this.meleeReleaseAt = sl.getGameTime() + 6;
        } else if (dist < 26.0) {
            if (!this.orbs.castBasicSpell(t)) {
                // 没装铁魔法时的回退: 直接一发魔法伤害
                float dmg = 4.5f + this.phase * 1.5f;      // ×0.5(v77)
                this.playCastAnim(net.farmingtales.ftboss.entity.OrbType.LIGHTNING);
                this.pendingMelee = t;
                this.pendingBasicMagic = true;
                this.meleeReleaseAt = sl.getGameTime() + 9;
                FTBossMod.LOGGER.info("[FTBoss][故障机器人] 基础攻击(回退) -> {} ({} 伤害, 阶段 {})",
                        t.getName().getString(), String.format("%.1f", dmg), this.phase);
            }
        }
    }

    /** 血条: 只显示给 48 格内的玩家, 名称随阶段变化 */
    private void tickBossBar() {
        if (!this.isAlive()) {
            BossBars.untrack(this, this.bossBar);
            return;
        }
        this.bossBar.setProgress(Math.max(0F, Math.min(1F, this.getHealth() / this.getMaxHealth())));
        Component want = Component.translatable("boss.ftboss.defect_core")
                .append(Component.literal(" §7[阶段 " + roman(this.phase) + "]"));
        if (!want.getString().equals(this.bossBar.getName().getString())) this.bossBar.setName(want);
        // ① **先清理血条里已经失效的玩家**(死亡重生换了实例/换维度/跑远) —— 不清理的话客户端收到不了 REMOVE 包, 血条会一直挂着
        for (ServerPlayer p : new java.util.ArrayList<>(this.bossBar.getPlayers())) {
            boolean keep = p.isAlive() && !p.isRemoved() && p.level() == this.level()
                    && p.distanceToSqr(this) <= 48 * 48;
            if (!keep) this.bossBar.removePlayer(p);
        }
        // ② 再把 48 格内活着的玩家加进来
        boolean visible = false;
        for (ServerPlayer p : ((ServerLevel) this.level()).getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(48))) {
            if (p.isAlive()) {
                this.bossBar.addPlayer(p);
                visible = true;
            }
        }
        if (this.bossBar.isVisible() != visible) this.bossBar.setVisible(visible);
    }

    private static String roman(int n) {
        return switch (n) {
            case 2 -> "II";
            case 3 -> "III";
            default -> "I";
        };
    }

    /** 阶段切换: 70% / 35%（M2 起在这里解锁球槽与技能）
     *  <p>v112 修复: **阶段只升不降**。以前用 {@code want == phase} 判定, 而回血(落地衰弱每 5t 回一次,
     *  电流相生结束还回 10%)能把血量比例推回阈值之上 → 阶段回退, 还会反向播报"进入阶段 I/II"。
     *  {@code unlockPhase} 本身是单调的(球槽不会缩), 所以这里只是把"回退"这件事堵掉。 */
    private void tickPhases() {
        float ratio = this.getHealth() / this.getMaxHealth();
        int want = ratio < PHASE3_AT ? 3 : (ratio < PHASE2_AT ? 2 : 1);
        if (want <= this.phase) return;                 // v112: 只前进
        this.phase = want;
        this.orbs.unlockPhase(want);      // 阶段突破 -> 球槽 +2
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 2.0f, want == 3 ? 0.6f : 0.9f);
            for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(48))) {
                p.displayClientMessage(Component.literal("§5故障机器人 §7进入 §d阶段 " + roman(want) + "§7！"), true);
            }
        }
        FTBossMod.LOGGER.info("[FTBoss] 故障机器人进入阶段 {} (血量 {}/{})", this.phase,
                (int) this.getHealth(), (int) this.getMaxHealth());
    }

    /** 悬浮: 悬停在目标上方 2.4 格并 8 字漂移 */
    private void tickHover() {
        if (!(this.level() instanceof ServerLevel)) return;
        // 落地衰弱: 不再 8 字漂移, 而是**贴着地面**喘气(玩家要求的"落地"阶段)
        if (this.exhaustedTicks > 0) {
            net.minecraft.core.BlockPos p = this.level().getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.blockPosition());
            double ty = p.getY() + 0.05;
            double dy = ty - this.getY();
            if (Math.abs(dy) > 0.05) {
                net.farmingtales.ftboss.util.BossCollision.move(this,
                        new Vec3(0, Math.max(-0.4, Math.min(0.4, dy)), 0));
            }
            return;
        }
        if (this.beamCharging) return;      // 蓄力 2 秒: 原地不动(朝向由 tickFaceAndTarget 继续更新)
        var target = this.getTarget();
        if (target == null || !target.isAlive()) return;
        this.hoverPhase += 0.03;
        double radius = 3.5;
        double hx = target.getX() + Math.cos(this.hoverPhase) * radius;
        double hz = target.getZ() + Math.sin(this.hoverPhase * 2.0) * radius;
        double hy = target.getY() + 1.3 + Math.sin(this.hoverPhase * 1.5) * 0.2;   // 2026-09-12: hover 2.4 -> 1.3 (do not fly too high)
        double dx = hx - this.getX(), dy = hy - this.getY(), dz = hz - this.getZ();
        double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dl > 0.6) {
            double step = Math.min(BossWalkingSpeed.BLOCKS_PER_TICK, dl);
            net.farmingtales.ftboss.util.BossCollision.move(this,
                    new Vec3(dx / dl * step, dy / dl * step, dz / dl * step));
        }
        this.getLookControl().setLookAt(target, 30f, 30f);
    }

    /**
     * 飞行推进特效(2026-09-11 按用户要求"改成机器人会飞")。
     * 本体是无重力悬浮 + 传送移动, 动画上已经把双腿向后收起, 这里再补**脚下两个喷口的持续喷流**:
     * 悬停时每 2t 一点 + 每 10t 一圈下洗气流, 位移时每 t 更密的拖尾 —— 一眼就能看出是"在飞"而不是"站在空中"。
     */
    private void tickThrusterFx() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        double mdx = this.getX() - this.xo, mdz = this.getZ() - this.zo;
        boolean moving = Math.abs(mdx) + Math.abs(mdz) > 0.055;
        if (this.tickCount % (moving ? 3 : 5) == 0) {      // 2026-09-11 按用户要求: 飞行粒子大幅减量
            float yaw = (float) Math.toRadians(this.getYRot());
            // 右向量(模型放大 1.2× 后脚位约 ±0.45 格)
            double rx = Math.cos(yaw), rz = Math.sin(yaw);
            double y = this.getY() + 0.12;
            for (int s = -1; s <= 1; s += 2) {
                net.farmingtales.ftboss.util.Fx.particles(sl,
                        this.getX() + rx * 0.45 * s, y, this.getZ() + rz * 0.45 * s,
                        1, net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, 0.06, 0.02);
            }
            net.farmingtales.ftboss.util.Fx.particles(sl, this.getX(), y - 0.1, this.getZ(),
                    moving ? 1 : 0, net.minecraft.core.particles.ParticleTypes.END_ROD, 0.12, 0.01);
        }
        if (this.tickCount % 20 == 0) {     // 悬停下洗气流(2026-09-11 减量: 20t/8 点)
            net.farmingtales.ftboss.util.Fx.ring(sl, this.getX(), this.getY() + 0.08, this.getZ(),
                    moving ? 1.1 : 0.8, 8, net.minecraft.core.particles.ParticleTypes.CLOUD, 0.01);
        }
    }

    // ================= 死亡: 核心超载 =================

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.orbs.reset();               // 球随核心一起消散
        /* 2026-09-12 v58 按用户反馈「召唤物死不干净」: 蛆小怪随主消散(它们本来有寿命, 这里兜底) */
        net.farmingtales.ftboss.boss.ai.SummonSweep.clear(this,
                net.farmingtales.ftboss.boss.ai.SummonSweep.RANGE,
                net.farmingtales.ftboss.entity.MaggotMinionEntity.class);
        if (this.level() instanceof ServerLevel bsl) net.farmingtales.ftboss.sound.BossMusic.release(bsl, this);   // 释放 BGM(否则音乐不会停)
        if (!(this.level() instanceof ServerLevel sl)) return;
        BossBars.untrack(this, this.bossBar);
        // 核心超载爆炸: 只伤实体、不毁地形(本工程铁律)
        sl.explode(this, this.getX(), this.getY() + 0.8, this.getZ(), OVERLOAD_POWER, Level.ExplosionInteraction.NONE);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 3.0f, 0.6f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 2.0f, 0.5f);
        FTBossMod.LOGGER.info("[FTBoss] 故障机器人超载爆炸 @({}, {}, {}) 威力={}", String.format("%.1f", this.getX()),
                String.format("%.1f", this.getY()), String.format("%.1f", this.getZ()), OVERLOAD_POWER);
    }

    // ================= GeckoLib =================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        // 过渡帧 8t(0.4s): 施法结束 → idle / idle ↔ float_move 之间**交叉淡入**, 动作衔接不生硬
        // (2026-09-11 按用户要求"施法动画要流畅有衔接"); 数值可用 AnimationController#setTransitionLength 调
        registrar.add(new AnimationController<>(this, "defect_ctrl", 8, this::predicate));
    }

    private PlayState predicate(AnimationState<DefectCoreEntity> state) {
        this.actionState.prepare(state);
        this.signatureCast.prepare(state);
        state.getController().setAnimationSpeed(1.0);
        if (this.isDeadOrDying()) {
            state.setAnimation(DEATH);
            return PlayState.CONTINUE;
        }
        // 超能光束优先级最高: 引导 5 秒里任何受击/施法都不打断它(用户点名的新技能要看得完整)
        if ("beam".equals(this.actionState.name())) {
            double age = this.actionState.elapsed(0);
            double animationAge = age <= 160 ? age * 10.5 / 8.0 : 210 + (age - 160) * 2.5 / 3.0;
            state.getController().setAnimationSpeed(age > 0 ? animationAge / age : 10.5 / 8.0);
            state.setAnimation(BEAM);
            return PlayState.CONTINUE;
        }
        // 落地衰弱: 塌在地上喘气(2026-09-11 按用户要求新增)
        if ("exhausted".equals(this.actionState.name())) {
            state.setAnimation(LANDING_WEAK);
            return PlayState.CONTINUE;
        }
        if (this.signatureCast.active()) {
            state.setAnimation("signature_self_cast".equals(this.signatureCast.name()) ? SIGNATURE_SELF : SIGNATURE_CAST);
            return PlayState.CONTINUE;
        }
        // ★ 2026-09-11 按用户要求"施法要有动作、要流畅有衔接": **动作优先级高于受击**。
        //   旧版把 hurt 放在最前面, 而 hurtTime 在被打后 10t 内一直 > 0 —— 玩家连续输出时
        //   施法动画几乎每帧都被受击动画挤掉, 表现就是"施法看不出动作"。现在: 施法/召唤/阶段/回想一旦开始就播完;
        //   受击只在"没有动作在播"时插空(短、不打断)。切回 idle/float 由控制器的过渡帧(8t)做交叉淡入。
        if (this.actionState.name().startsWith("cast")) {
            state.setAnimation(switch (this.actionState.name()) {
                case "cast_glass" -> CAST_GLASS;
                case "cast_lightning" -> CAST_LIGHTNING;
                case "cast_frost" -> CAST_FROST;
                case "cast_ion" -> CAST_ION;
                case "cast_dark" -> CAST_DARK;
                default -> CAST;
            });
            return PlayState.CONTINUE;
        }
        if ("phase_shift".equals(this.actionState.name())) {
            state.setAnimation(PHASE);
            return PlayState.CONTINUE;
        }
        if ("orb_summon".equals(this.actionState.name())) {
            state.setAnimation(SUMMON);
            return PlayState.CONTINUE;
        }
        if ("echo".equals(this.actionState.name())) {
            state.setAnimation(ECHO);
            return PlayState.CONTINUE;
        }
        // 转身优先于受击: 用户点名要"转向动作", 被连续输出时也要看得见(受击只在站定时插空)
        float turn = net.minecraft.util.Mth.wrapDegrees(this.getYRot() - this.yRotO);
        if (Math.abs(turn) > 1.2f) {
            state.setAnimation(turn > 0 ? TURN_L : TURN_R);
            return PlayState.CONTINUE;
        }
        if (this.hurtTime > 0 || playing(state, HURT, T_HURT)) {
            state.setAnimation(HURT);
            return PlayState.CONTINUE;
        }
        // 悬浮漂移时前倾 + 披风拖尾, 站着不动时呼吸 idle(2026-09-11 重做动画时新增)
        state.setAnimation(isDrifting() ? FLOAT_MOVE : IDLE);
        return PlayState.CONTINUE;
    }

    /** 这一 tick 是否在水平漂移(用原版的上一帧坐标 xo/zo 判断, 不用额外字段) */
    private boolean isDrifting() {
        return Math.abs(this.getX() - this.xo) + Math.abs(this.getZ() - this.zo) > 0.055;
    }

    private static boolean playing(AnimationState<DefectCoreEntity> state, RawAnimation anim, double ticks) {
        return state.isCurrentAnimation(anim) && state.getAnimationTick() < ticks;
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
