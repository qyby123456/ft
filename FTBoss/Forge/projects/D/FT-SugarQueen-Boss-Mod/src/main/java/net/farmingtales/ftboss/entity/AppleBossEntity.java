package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ai.SummonLimit;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
 * 【苹果爵士·蜜脆】Sir Honeycrisp(第 8 位 Boss, 2026-09-12)
 *
 * <p><b>常规攻击 = 冲向玩家自爆</b>: 蓄力 -> 冲刺 -> 撞到/冲完引爆(基准 42 点, 按距离衰减, 半径 7)。
 * <p>一阶段「果皮形态」: 冲撞自爆 / 果籽霰弹 / 烂苹果投掷 / 蜡护膜 / 召唤(果虫·果蛾·烂苹果堆·青苹果·金币人) / 分身
 * <p>二阶段「虫蛀形态」(HP ≤ 50%): 蜡盖弹开变腐烂贴图, 追加 酸蚀喷吐 / 汁液喷洒 / 钻地突袭
 *
 * <p>2026-09-12 按用户要求追加: <b>完全免疫击退 / 免疫火焰(且不播火焰动画) / 免疫一切负面效果</b>。
 *
 * <p>注: 本文件在 2026-09-12 因一次脚本事故被覆盖, 已按编译产物的成员清单原样重建。
 */
public class AppleBossEntity extends Monster implements GeoAnimatable, net.farmingtales.ftboss.integ.spells.RegularBossCastHost {
    private static final EntityDataAccessor<CompoundTag> DATA_ACTION =
            SynchedEntityData.defineId(AppleBossEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private final net.farmingtales.ftboss.boss.ai.BossActionState actionState =
            new net.farmingtales.ftboss.boss.ai.BossActionState(this, DATA_ACTION);
    private static final EntityDataAccessor<CompoundTag> DATA_SIGNATURE_CAST =
            SynchedEntityData.defineId(AppleBossEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private final net.farmingtales.ftboss.boss.ai.BossActionState signatureCast =
            new net.farmingtales.ftboss.boss.ai.BossActionState(this, DATA_SIGNATURE_CAST);

    /* ---------------- 动画 ---------------- */
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.apple_boss.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.apple_boss.walk");
    private static final RawAnimation TURN_L = RawAnimation.begin().thenLoop("animation.apple_boss.turn_l");
    private static final RawAnimation TURN_R = RawAnimation.begin().thenLoop("animation.apple_boss.turn_r");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.apple_boss.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.apple_boss.death");
    private static final RawAnimation ROLL = RawAnimation.begin().thenPlay("animation.apple_boss.roll_charge");
    private static final RawAnimation SEED = RawAnimation.begin().thenPlay("animation.apple_boss.seed_shot");
    private static final RawAnimation THROW = RawAnimation.begin().thenPlay("animation.apple_boss.throw_rotten");
    private static final RawAnimation WAX = RawAnimation.begin().thenPlay("animation.apple_boss.wax_shield");
    private static final RawAnimation SUMMON = RawAnimation.begin().thenPlay("animation.apple_boss.summon");
    private static final RawAnimation TRANSFORM = RawAnimation.begin().thenPlay("animation.apple_boss.transform");
    private static final RawAnimation ACID = RawAnimation.begin().thenPlay("animation.apple_boss.acid_spit");
    /** 近战挥击(保留备用; 常规攻击已改为冲撞自爆) */
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.apple_boss.attack");
    /** 二阶段常驻姿势(蜡盖缩没 + 虫子探出), 由第二个控制器叠加 */
    private static final RawAnimation LID_OPEN = RawAnimation.begin().thenLoop("animation.apple_boss.lid_open");
    /** 头顶三苹果・充能抛射(2026-09-12 按用户要求): 蓄能鼓胀 -> 三颗一起射出 -> 重新长出 */
    private static final RawAnimation ORBIT = RawAnimation.begin().thenPlay("animation.apple_boss.orbit_shot");
    private static final RawAnimation SIGNATURE_CAST = RawAnimation.begin().thenPlay("animation.apple_boss.signature_cast");
    private static final RawAnimation SIGNATURE_SELF = RawAnimation.begin().thenPlay("animation.apple_boss.signature_self_cast");

    /* ---------------- 时长(tick, 与动画长度一一对应) ---------------- */
    private static final double T_ROLL = 32.0, T_SEED = 32.0, T_THROW = 28.0, T_WAX = 40.0,
            T_SUMMON = 44.0, T_TRANSFORM = 56.0, T_ACID = 36.0, T_HURT = 9.0, T_ATTACK = 18.0,
            T_ORBIT = 32.0;

    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(AppleBossEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossBar;

    private double actionTicks = 0.0;
    private String actionName = "";
    /** 技能爆发帧计数(每 tick +1, 与动画关键帧对齐) */
    private int fxAt = -1;
    private int seedCd = 90, acidCd = 200, waxCd = 320, summonCd = 160;
    /* v112: 删掉 dashTicks / dashX / dashZ —— 这三个字段全文件只有声明, 一段 dash 状态机从没写完;
       苹果爵士的冲撞实际走下面的 chargeState/chargeT 状态机。 */
    /* 常规攻击(冲撞自爆)状态机: 0=空闲 1=蓄力 2=冲刺 */
    private int chargeState = 0;
    private int chargeT = 0;
    private double chargeX = 0, chargeZ = 0;
    private int chargeCd = 10;
    /* 追加技能冷却: 烂苹果投掷 / 汁液喷洒 / 钻地突袭 / 分身 / 头顶三苹果 */
    private int throwCd = 140, juiceCd = 240, burrowCd = 320, burrowT = 0;
    private int cloneCd = 120;
    private int orbitCd = 160;
    /* 多段爆炸(2026-09-12 按用户要求: 爆炸变成多段): 首爆后再来 2 段, 每 8 tick 一段 */
    private int blastWaves = 0;
    private int blastT = 0;
    private double blastX = 0, blastY = 0, blastZ = 0;
    /* 2026-09-12 追加: 天光光束(3 秒后 30 格随机魔法+闪电轰炸) / 巨型火球砸地 */
    private int skyCd = 220, skyT = 0;
    private double skyX = 0, skyZ = 0;
    /** 天光轰炸波状态(2026-09-12 按用户要求: 闪电连续施放 5 次, 每波 5 处天火 + 4 道雷) */
    private int skyWave = 0, skyWaveT = 0;
    /** 巨型果蛆召唤冷却 */
    private int grubCd = 200;
    private int metCd = 300, metT = 0;
    private double metX = 0, metY = 0, metZ = 0;
    /** 天光光束的施法动作时长(复用 summon 动作) */
    private static final double T_SKY = 44.0;
    /** 巨型火球: 1.5 秒(30t) 坠落预警 */
    private static final int METEOR_TELEGRAPH = 30;
    /** 2026-09-12 按用户要求: 金苹果改为 **15 秒**一次(一/二阶段都是) */
    private int appleCd = 300;
    /** 2026-09-12 按用户要求: 二阶段额外每 **45 秒**一次附魔金苹果 */
    private int enchantCd = 900;
    /** 2026-09-12 按用户要求: 检测到特定头衔的玩家时, 爵士会"蜕变成下一位 Boss"(目前为占位实现) */
    private boolean ascended = false;
    /** 触发头衔关键词(甜苹果庄园 / 血腥契约) */
    /**
     * 头衔必须精确是【甜苹果庄园】。2026-09-12 从整合包里查实其定义:
     * {@code kubejs/data/farmingtales/titles/sweet_apple_manor.json -> id = farmingtales:sweet_apple_manor}
     * (同目录另有 21 个头衔, 如 legendary_conqueror / cataclysm_conqueror), 所以这里**只按 id 精确匹配**,
     * 不再用宽泛关键词, 免得别的头衔误触发。
     */
    private static final String[] ASCEND_TITLES = {"farmingtales:sweet_apple_manor", "sweet_apple_manor"};
    private boolean transformed = false;

    public AppleBossEntity(EntityType<? extends AppleBossEntity> type, Level level) {
        super(type, level);
        this.xpReward = 140;
        this.bossBar = new ServerBossEvent(this.getDisplayName(),
                BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
        BossBars.track(this, this.bossBar);
    }

    @Override
    public void setSpeed(float speed) {
        super.setSpeed(BossWalkingSpeed.speed(this, speed));
        BossWalkingSpeed.normalizeInput(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 640.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.ATTACK_DAMAGE, 3.5)
                .add(Attributes.MOVEMENT_SPEED, BossWalkingSpeed.BASE_SPEED)
                .add(Attributes.KNOCKBACK_RESISTANCE, net.farmingtales.ftboss.util.BossKnockback.BOSS_RESISTANCE)   // 2026-09-19 按用户要求: 允许 Boss 受到击退(0.5 = 吃一半)
                .add(Attributes.FOLLOW_RANGE, 42.0);
    }

    @Override
    protected void registerGoals() {
        /* 常规攻击不是近战挥击, 而是 tick 里的"冲撞自爆"状态机;
           但 Boss 仍然需要**走位与转身** —— NearestAttackableTargetGoal 只负责选目标,
           所以这里补 FloatGoal + PursueGoal(只走位不出手)。 */
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new PursueGoal(1.5));   // 2026-09-12: 移速大幅增加, 走位同步加快
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** 追击目标: 走向目标 + 始终看向目标(转身)。冲撞/钻地期间让位。 */
    private class PursueGoal extends Goal {
        private final double speed;
        private final net.farmingtales.ftboss.boss.ai.BossApproach approach =
                new net.farmingtales.ftboss.boss.ai.BossApproach();

        PursueGoal(double speed) {
            this.speed = speed;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity t = AppleBossEntity.this.getTarget();
            return t != null && t.isAlive()
                    && AppleBossEntity.this.chargeState == 0 && AppleBossEntity.this.burrowT == 0;
        }

        @Override
        public boolean canContinueToUse() { return this.canUse(); }

        @Override
        public void stop() { this.approach.reset(AppleBossEntity.this); }

        @Override
        public void tick() {
            LivingEntity t = AppleBossEntity.this.getTarget();
            if (t == null) return;
            AppleBossEntity.this.getLookControl().setLookAt(t, 30.0F, 30.0F);
            this.approach.tick(AppleBossEntity.this, t, this.speed, 2.2, 2.5, 0, 0);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PHASE, 1);
        this.entityData.define(DATA_ACTION, new CompoundTag());
        this.entityData.define(DATA_SIGNATURE_CAST, new CompoundTag());
    }

    public int getPhase() { return this.entityData.get(DATA_PHASE); }

    /** 2026-09-12 按用户要求: 着火时不播火焰动画 */
    @Override
    public boolean displayFireAnimation() { return false; }

    /** 2026-09-12 按用户要求: 火焰免疫(火/岩浆伤害直接免掉) */
    @Override
    public boolean fireImmune() { return true; }

    /** 血条(供 BossHandler 在区块卸载/离场时清理) */
    public ServerBossEvent getBossBar() { return this.bossBar; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("phase", this.getPhase());
        tag.putBoolean("transformed", this.transformed);
        tag.putDouble("bossbar_progress", this.bossBar.getProgress());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("phase")) this.entityData.set(DATA_PHASE, tag.getInt("phase"));
        this.transformed = tag.getBoolean("transformed");
    }

    @Override
    public void startSignatureCast(net.farmingtales.ftboss.util.BossSpellRules.Kind kind, int castTicks) {
        this.signatureCast.start(net.farmingtales.ftboss.integ.spells.RegularBossCastHost.action(kind),
                net.farmingtales.ftboss.integ.spells.RegularBossCastHost.visualTicks(castTicks));
    }

    /** 播放一次性动作动画 */
    public void playAction(String name, double ticks) {
        this.summonVariant = 0;
        this.actionState.start(name, (int) ticks);
        this.actionName = name;
        this.actionTicks = ticks;
        this.fxAt = -1;
    }

    private int summonVariant;

    /** 近战命中(常规攻击已改为冲撞自爆, 这里是保留路径) */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity le) {
            le.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0));
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
            if (this.actionTicks <= 0 && !this.isDeadOrDying()) this.playAction("attack", T_ATTACK);
        }
        return hit;
    }

    /** 自身受击: 播一次受击后摇(没有别的动作在播时) */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean ok = super.hurt(source, amount);
        if (ok && this.actionTicks <= 0 && !this.isDeadOrDying()) {
            this.playAction("hurt", T_HURT);
        }
        return ok;
    }

    /* ============================ 主 tick ============================ */
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        if (this.actionTicks > 0) this.actionTicks -= 1.0;
        /* 2026-09-12 按用户要求"免疫 debuff": 每秒清一次自身负面效果
           (BossHandler 的 DENY 是第一道保险, 这里兜底; 只清负面, 蜡护膜等正面 buff 保留) */
        if (this.tickCount % 20 == 0) {
            for (MobEffectInstance inst : new java.util.ArrayList<>(this.getActiveEffects())) {
                if (!inst.getEffect().isBeneficial()) this.removeEffect(inst.getEffect());
            }
        }
        /* BGM: 每 tick 向调度器登记 */
        if (this.level() instanceof ServerLevel bsl) net.farmingtales.ftboss.sound.BossMusic.request(bsl, this);

        final LivingEntity target = this.getTarget();
        final ServerLevel sl = this.level() instanceof ServerLevel s ? s : null;

        this.bossBar.setProgress(Math.max(0f, Math.min(1f, this.getHealth() / this.getMaxHealth())));
        /* v112 修复: 只有它一条血条**从来没 addPlayer** —— 血条对象建了、进度也在更新,
           但没有任何玩家能看到(其它 7 只 Boss 都有这一圈 48 格内的增删)。 */
        boolean wantBar = false;
        for (ServerPlayer bp : this.level().getEntitiesOfClass(ServerPlayer.class,
                this.getBoundingBox().inflate(48.0))) {
            if (bp.isAlive() && this.distanceToSqr(bp) < 48.0 * 48.0) {
                this.bossBar.addPlayer(bp);
                wantBar = true;
            } else {
                this.bossBar.removePlayer(bp);
            }
        }
        this.bossBar.setVisible(this.isAlive() && wantBar);

        /* 2026-09-12 按用户要求: **每 15 秒吃一次金苹果**(再生 II 5s + 吸收 I 2min) */
        if (--this.appleCd <= 0) {
            this.appleCd = 300;
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, false, true));
            this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0, false, false, true));
            this.heal(4.0f);
            if (sl != null) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 1.4f, 0.8f);
                Fx.particles(sl, this.getX(), this.getY() + 1.6, this.getZ(), 24,
                        ParticleTypes.HAPPY_VILLAGER, 1.3, 0.08);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] golden apple (regen II + absorption I)");
        }
        /* 2026-09-12 按用户要求: **二阶段**额外每 45 秒吃一次附魔金苹果 */
        if (this.getPhase() >= 2 && --this.enchantCd <= 0) {
            this.enchantCd = 900;
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1, false, false, true));      // 再生 II 30s
            this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3, false, false, true));      // 吸收 IV 2min
            this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0, false, false, true));  // 防火 5min
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 0, false, false, true));// 抗性 I 5min
            this.heal(40.0f);
            if (sl != null) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.HOSTILE, 1.4f, 0.7f);
                Fx.particles(sl, this.getX(), this.getY() + 1.8, this.getZ(), 40,
                        ParticleTypes.TOTEM_OF_UNDYING, 1.6, 0.10);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] ENCHANTED golden apple (regen II / absorption IV / fire res / resistance)");
        }

        /* 2026-09-12 按用户要求: **头衔检测** —— 附近玩家戴着「甜苹果庄园 … 武器：血腥契约」时,
           爵士会蜕变成"下一位 Boss"(当前用占位 Boss 测试, 见 ascendToNextBoss)。 */
        if (!this.ascended && this.tickCount % 20 == 0 && sl != null) {
            for (Player p : sl.players()) {
                if (!p.isAlive() || p.distanceToSqr(this) > 48.0 * 48.0) continue;
                boolean byTitle = net.farmingtales.ftboss.boss.BossHandler.hasTitle(p, ASCEND_TITLES);
                String contractHit = net.farmingtales.ftboss.boss.BossHandler.matchedContract(p);
                boolean byContract = contractHit != null;
                /* 2026-09-12 按用户要求: **头衔与血腥契约"两个都是必要条件"** —— 缺一不可。 */
                if (byTitle && byContract) {
                    FTBossMod.LOGGER.info("[FTBoss][Apple] 触发蜕变: 头衔+血腥契约 均满足 (title={} contract={}); 命中物品={}",
                            byTitle, byContract, contractHit);
                    this.ascendToNextBoss(sl, p);
                    return;
                }
                /* 调试: 每 5 秒打一次"我看到的东西", 用来排查为什么没触发 */
                if (this.tickCount % 100 == 0) {
                    FTBossMod.LOGGER.info("[FTBoss][title-debug] {}",
                            net.farmingtales.ftboss.boss.BossHandler.debugLine(p));
                }
            }
        }
        /* 二阶段: 血量过半 -> 变身(蜡盖弹开) */
                /* 二阶段: 血量过半 -> 变身(蜡盖弹开) */
        if (!this.transformed && this.getHealth() <= this.getMaxHealth() * 0.5f && this.actionTicks <= 0) {
            this.transformed = true;
            /* ★ v112 修复: 阶段要和 transformed **同一刻**落盘。原来 phase=2 是在动画第 24 tick 的
               transformFx 里才写的, 而这 24 tick 内如果区块卸载/存档, NBT 里就是
               transformed=true + phase=1 -> 以后既不会再触发变身、也永远停在阶段一。 */
            this.entityData.set(DATA_PHASE, 2);
            this.playAction("transform", T_TRANSFORM);
            if (sl != null) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_SPAWN,
                        SoundSource.HOSTILE, 1.3f, 0.7f);
                Fx.particles(sl, this.getX(), this.getY() + 1.4, this.getZ(), 40, ParticleTypes.ITEM_SLIME, 1.8, 0.12);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] phase -> 2 (transform)");
        }
        /* 变身动画播到爆点(1.2s=24t): 落地冲击 + 转阶段 + 换曲(唯一推进 fxAt 的地方) */
        if (this.actionTicks > 0 && "transform".equals(this.actionName)) {
            this.transformFx(sl);
        }
        /* ===== 天光光束倒计时: 3 秒后 30 格随机魔法+闪电轰炸 ===== */
        if (this.skyT > 0) {
            this.skyT--;
            if (sl != null) {
                /* 头顶一道通天的光柱(预警, 玩家有 3 秒跑位) */
                for (int i = 0; i < 6; i++) {
                    sl.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 2.0 + i * 3.0, this.getZ(),
                            1, 0.15, 0.0, 0.15, 0.0);
                }
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 4.0, this.getZ(),
                        4, 0.4, 2.0, 0.4, 0.05);
            }
            if (this.skyT == 0) { this.skyWave = 10; this.skyWaveT = 0; }   // 2026-09-12 按用户要求: 波数 5 -> 10
            return;
        }
        /* ===== 天光轰炸的 5 连波(2026-09-12 按用户要求: 闪电连续施放 5 次) ===== */
        if (this.skyWave > 0) {
            if (--this.skyWaveT <= 0) {
                this.skyWaveT = 10;                             // 波间隔 0.5 秒(10 波共 5 秒, 每波都看得出来)
                this.skyWave--;
                this.skyBarrage(sl);
            }
            return;
        }
        /* ===== 巨型火球坠落(1.5 秒预警 -> 60 点 AoE) ===== */
        if (this.metT > 0) {
            this.metT--;
            if (sl != null) {
                /* 火球从空中一路砸下来: 用粒子画出坠落轨迹 */
                double k = 1.0 - this.metT / (double) METEOR_TELEGRAPH;
                double fy = this.metY + (1.0 - k) * 28.0;
                for (int i = 0; i < 5; i++) {
                    sl.sendParticles(ParticleTypes.FLAME, this.metX, fy + i * 0.5, this.metZ,
                            1, 0.35, 0.2, 0.35, 0.02);
                }
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, this.metX, fy, this.metZ, 3, 0.5, 0.5, 0.5, 0.02);
            }
            if (this.metT == 0) this.meteorImpact(sl);
            return;
        }
        /* ===== 多段爆炸的后续段(2026-09-12) ===== */
        if (this.blastWaves > 0) {
            if (--this.blastT <= 0) {
                this.blastT = 8;
                int stage = 3 - this.blastWaves + 1;
                this.blastWaves--;
                this.explosionStage(sl, stage);
            }
            return;
        }
        /* ===== 冲撞状态机(常规攻击) ===== */
        if (this.chargeState >= 1) {
            this.tickCharge(sl);
            return;
        }
        /* ===== 钻地突袭状态机(二阶段) ===== */
        if (this.burrowT > 0) {
            this.tickBurrow(sl, target);
            return;
        }
        /* 动作动画播放期间: 只结算"爆发帧" */
        if (this.actionTicks > 0) {
            this.tickAction(sl, target);
            return;
        }
        if (target == null || !target.isAlive()) return;

        /* ① 常规攻击(冷却最短): 冲向玩家自爆 */
        if (--this.chargeCd <= 0) {
            this.startCharge(sl, target);
            return;
        }
        /* ② 烂苹果投掷(中距离 7~26 格) */
        if (--this.throwCd <= 0 && this.distanceToSqr(target) > 49.0 && this.distanceToSqr(target) < 26.0 * 26.0) {
            this.throwCd = this.getPhase() >= 2 ? 150 + this.random.nextInt(70) : 230 + this.random.nextInt(90);
            this.playAction("throw_rotten", T_THROW);
            this.fxAt = 0;
            return;
        }
        /* ③ 果籽霰弹 */
        if (--this.seedCd <= 0 && this.distanceToSqr(target) < 20.0 * 20.0) {
            this.seedCd = this.getPhase() >= 2 ? 130 + this.random.nextInt(60) : 200 + this.random.nextInt(90);
            this.playAction("seed_shot", T_SEED);
            this.fxAt = 0;
            return;
        }
        /* ④ 二阶段: 汁液喷洒(黏稠果胶, 大幅减速) */
        if (this.getPhase() >= 2 && --this.juiceCd <= 0 && this.distanceToSqr(target) < 14.0 * 14.0) {
            this.juiceCd = 220 + this.random.nextInt(90);
            this.playAction("juice_spray", T_ACID);
            this.fxAt = 0;
            return;
        }
        /* ⑤ 酸蚀喷吐 */
        if (--this.acidCd <= 0 && this.distanceToSqr(target) < 12.0 * 12.0) {
            this.acidCd = this.getPhase() >= 2 ? 200 + this.random.nextInt(90) : 300 + this.random.nextInt(120);
            this.playAction("acid_spit", T_ACID);
            this.fxAt = 0;
            return;
        }
        /* ⑥ 二阶段: 钻地突袭 */
        if (this.getPhase() >= 2 && --this.burrowCd <= 0 && this.distanceToSqr(target) < 30.0 * 30.0) {
            this.burrowCd = 400 + this.random.nextInt(160);
            this.burrowT = 26;
            this.setInvisible(true);
            this.setInvulnerable(true);
            this.getNavigation().stop();
            if (sl != null) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.WARDEN_DIG, SoundSource.HOSTILE, 1.4f, 1.2f);
                Fx.particles(sl, this.getX(), this.getY() + 0.5, this.getZ(), 40,
                        ParticleTypes.LARGE_SMOKE, 1.4, 0.10);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] burrow dive start");
            return;
        }
        /* ⑦ 分身召唤(会冲撞自爆的复制体) —— 2026-09-12 按用户要求: 次数与数量都变多 */
        if (--this.cloneCd <= 0) {
            this.cloneCd = this.getPhase() >= 2 ? 140 + this.random.nextInt(80) : 200 + this.random.nextInt(100);
            this.playAction("summon", T_SUMMON);
            this.fxAt = 0;
            this.summonVariant = 1;
            return;
        }
        /* ⑦.2 巨型果蛆(2026-09-12 按用户要求): 同时只允许 1 条 */
        if (--this.grubCd <= 0) {
            this.grubCd = this.getPhase() >= 2 ? 400 + this.random.nextInt(160) : 600 + this.random.nextInt(240);
            this.playAction("summon", T_SUMMON);
            this.fxAt = 0;
            this.summonVariant = 2;
            return;
        }
        /* ⑦.5 头顶三苹果充能抛射(2026-09-12) */
        if (--this.orbitCd <= 0) {
            this.orbitCd = this.getPhase() >= 2 ? 150 + this.random.nextInt(70) : 240 + this.random.nextInt(90);
            this.playAction("orbit_shot", T_ORBIT);
            this.fxAt = 0;
            return;
        }
        /* ⑧ 天光光束(2026-09-12): 向天发射光束, 3 秒后 30 格内随机魔法+闪电轰炸 */
        if (--this.skyCd <= 0) {
            this.skyCd = this.getPhase() >= 2 ? 380 + this.random.nextInt(140) : 560 + this.random.nextInt(200);
            this.playAction("sky_beam", T_SKY);
            this.fxAt = 0;
            this.skyT = 60;                                  // 3 秒(60t) 预警
            this.skyX = target.getX();
            this.skyZ = target.getZ();
            if (sl != null) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.6f, 1.4f);
                sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.CONDUIT_ACTIVATE, SoundSource.HOSTILE, 1.4f, 0.9f);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] sky beam charging at {}, {}", (int) this.skyX, (int) this.skyZ);
            return;
        }
        /* ⑨ 巨型火球砸地(2026-09-12): 60 点 AoE */
        if (--this.metCd <= 0) {
            this.metCd = this.getPhase() >= 2 ? 320 + this.random.nextInt(140) : 460 + this.random.nextInt(200);
            this.playAction("meteor", T_SUMMON);
            this.fxAt = 0;
            this.metT = METEOR_TELEGRAPH;
            this.metX = target.getX();
            this.metY = target.getY();
            this.metZ = target.getZ();
            if (sl != null) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 1.8f, 0.6f);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] meteor incoming at {}, {}", (int) this.metX, (int) this.metZ);
            return;
        }
        /* ⑧ 蜡护膜 */
        if (--this.waxCd <= 0) {
            this.waxCd = 460 + this.random.nextInt(160);
            this.playAction("wax_shield", T_WAX);
            this.fxAt = 0;
            return;
        }
        /* ⑨ 召唤果虫/果蛾/烂苹果堆/青苹果/金币人 */
        if (--this.summonCd <= 0) {
            this.summonCd = this.getPhase() >= 2 ? 180 + this.random.nextInt(80) : 260 + this.random.nextInt(100);
            this.playAction("summon", T_SUMMON);
            this.fxAt = 0;
            return;
        }
    }

    /* ==================== 常规攻击: 冲撞自爆 ==================== */

    private void startCharge(ServerLevel sl, LivingEntity target) {
        this.chargeState = 1;
        this.chargeT = 10;
        this.chargeX = target.getX();
        this.chargeZ = target.getZ();
        this.playAction("roll_charge", T_ROLL);
        if (sl != null) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.3f, 1.15f);
            Fx.particles(sl, this.getX(), this.getY() + 0.6, this.getZ(), 4, ParticleTypes.CRIT, 1.2, 0.10);
        }
    }

    private void tickCharge(ServerLevel sl) {
        /* 蓄力阶段: 倒计时结束才切到冲刺 */
        if (this.chargeState == 1) {
            if (--this.chargeT > 0) return;
            this.chargeState = 2;
            this.chargeT = 40;                     // 2026-09-12 按用户要求: 冲刺距离增加(26 -> 40 tick)
            LivingEntity t = this.getTarget();
            if (t != null) { this.chargeX = t.getX(); this.chargeZ = t.getZ(); }
            return;
        }
        /* 冲刺阶段(2026-09-12 修"爆炸缺少冲刺"): **每 tick 都必须前进**。
           原来把 `if (--chargeT > 0) return;` 放在最前面, 冲刺期间每 tick 都提前返回,
           只在倒计时归零那一 tick 走一步 -> 表现出来就是"原地爆炸, 没有冲刺"。 */
        if (this.chargeState == 2) {
            double dx = this.chargeX - this.getX(), dz = this.chargeZ - this.getZ();
            double dl = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
            this.setPos(this.getX() + dx / dl * 1.2, this.getY(), this.getZ() + dz / dl * 1.2);   // 每 tick 1.2 格
            if (sl != null) Fx.particles(sl, this.getX(), this.getY() + 0.3, this.getZ(), 1,
                    ParticleTypes.END_ROD, 0.9, 0.05);
            boolean contact = false;
            for (Player p : this.level().players()) {
                if (p.isAlive() && p.distanceToSqr(this) < 6.25) { contact = true; break; }
            }
            if (contact || --this.chargeT <= 0 || dl < 1.2) this.chargeExplode(sl);
        }
    }

    /**
     * 冲撞到点的自爆(2026-09-12 按用户要求改成**多段爆炸**):
     * 第 1 段 45 点 -> 8 tick 后第 2 段 52 点 -> 再 8 tick 第 3 段 **60** 点(半径逐段放大)。
     * 每段都按距离衰减(最低 25%)并击退; 只出粒子+音效, 不做真实爆炸(不毁掉落物/不殉爆)。
     */
    private void chargeExplode(ServerLevel sl) {
        this.chargeState = 0;
        this.chargeCd = 22 + this.random.nextInt(16);           // 1.1~1.9 秒一次
        this.blastX = this.getX();
        this.blastY = this.getY() + 0.8;
        this.blastZ = this.getZ();
        this.blastWaves = 2;        // 后面还有 2 段
        this.blastT = 8;
        this.explosionStage(sl, 1);
    }

    /** 单段爆炸: stage 1/2/3 对应 45 / 52 / 60 点与半径 6 / 6.5 / 7 */
    private void explosionStage(ServerLevel sl, int stage) {
        if (sl == null) return;
        final double R = stage >= 3 ? 7.0 : (stage == 2 ? 6.5 : 6.0);
        final float base = stage >= 3 ? 60.0f : (stage == 2 ? 52.0f : 45.0f);   // 2026-09-12 按用户要求: 提高到 60
        double x = this.blastX, y = this.blastY, z = this.blastZ;
        net.farmingtales.ftboss.network.AppleBlastNetwork.show(sl, x, y, z, R);
        sl.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE,
                1.6f + stage * 0.25f, 0.75f - stage * 0.06f);
        for (Player p : sl.players()) {
            if (!p.isAlive()) continue;
            double d = Math.sqrt(p.distanceToSqr(x, y, z));
            if (d > R) continue;
            /* v77: base 是"设计值", 这里直接乘 0.5 得到玩家实际吃到的(原来由全局钩子做) */
            p.hurt(this.damageSources().explosion(this, this), (float) (base * 0.5 * Math.max(0.25, 1.0 - d / R)));
            net.farmingtales.ftboss.util.BossKnockback.knockback(p, 0.9 + stage * 0.4, x - p.getX(), z - p.getZ());
        }
        FTBossMod.LOGGER.info("[FTBoss][Apple] explosion stage {} ({} dmg, r={}) at {}, {}, {}",
                stage, (int) base, R, (int) x, (int) y, (int) z);
    }

    /* ==================== 分身召唤 ==================== */

    /** 放出 2~3 个苹果分身(受同类 10 只上限约束, 满了则改为给已有分身强化) */
    private void summonClones(ServerLevel sl) {
        if (sl == null) return;
        int room = SummonLimit.room(this, AppleCloneEntity.class);
        if (room <= 0) { SummonLimit.atCap(this, AppleCloneEntity.class); return; }
        int n = Math.min(5 + this.random.nextInt(3) + (this.getPhase() >= 2 ? 2 : 0), room);   // 分身更多
        for (int i = 0; i < n; i++) {
            AppleCloneEntity c = FTEntities.APPLE_CLONE.get().create(sl);
            if (c == null) continue;
            /* 2026-09-12 按用户要求: 分身**均匀分散**(原来纯随机, 会挤成一堆) */
            double a = (Math.PI * 2 / Math.max(1, n)) * i + this.random.nextDouble() * 0.5;
            double rr = 4.5 + this.random.nextDouble() * 1.5;
            c.moveTo(this.getX() + Math.cos(a) * rr, this.getY(), this.getZ() + Math.sin(a) * rr,
                    this.random.nextFloat() * 360f, 0f);
            c.setTarget(this.getTarget());
            sl.addFreshEntity(c);
            Fx.particles(sl, c.getX(), c.getY() + 1.0, c.getZ(), 18, ParticleTypes.CRIT, 1.1, 0.10);
        }
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.HOSTILE, 1.4f, 0.8f);
        FTBossMod.LOGGER.info("[FTBoss][Apple] summoned {} clones", n);
    }

    /* ==================== 头衔共鸣 -> 蜕变为下一位 Boss(占位) ==================== */

    /**
     * 【头衔共鸣】(2026-09-12 按用户要求): 附近玩家佩戴「甜苹果庄园 … 武器：血腥契约」头衔时,
     * 爵士不再打了 —— 它蜕变成下一位 Boss。
     *
     * TODO(新 Boss 做好后改这里): 当前是占位实现 —— 用「炫彩答辩超人」顶替,
     * 只做"特效 + 播报 + 生成占位 Boss + 自己退场"。换新 Boss 时只改下面那一行实体类型。
     */
    private void ascendToNextBoss(ServerLevel sl, Player trigger) {
        this.ascended = true;
        FTBossMod.LOGGER.info("[FTBoss][Apple] 头衔共鸣 by {} -> 蜕变(占位: 炫彩答辩超人)",
                trigger.getName().getString());
        if (sl == null) return;
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0f, 0.5f);
        Fx.particles(sl, this.getX(), this.getY() + 1.5, this.getZ(), 120, ParticleTypes.TOTEM_OF_UNDYING, 2.6, 0.16);
        Fx.particles(sl, this.getX(), this.getY() + 1.5, this.getZ(), 80, ParticleTypes.SOUL_FIRE_FLAME, 2.0, 0.12);
        for (Player p : sl.players()) {
            if (p.distanceToSqr(this) > 64.0 * 64.0) continue;
            p.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "§6【头衔共鸣】§7检测到 §e甜苹果庄园§7 的持有者 —— 爵士蜕变为 §c下一位 Boss§7！"), false);
        }
        /* 2026-09-12 按用户要求: 隐藏 Boss 入场演出 —— 出现在"离玩家 15 格、远离玩家"的位置,
           **无敌**状态下做粒子 + 模型表演, 逐句播报台词, 并在台词播完时把血条填满。
           台词由 HiddenBossIntro 自己播(实测更可靠)。
           2026-09-13 v78 按用户要求: 演出**之前**先弹"难度极高, 毕业了再来"的警告 + [挑战]/[放弃] 按钮,
           玩家点【挑战】才真的开始(并同时发放金苹果 + 400 点伤害吸收)。 */
        /* 2026-09-14 v113 按用户要求「接受/拒绝只能由触发彩蛋玩家选择」:
           把 trigger(头衔共鸣的那位)一并传进去 —— 只有他拿到 [挑战]/[放弃] 按钮。 */
        net.farmingtales.ftboss.boss.HiddenBossChallenge.ask(sl, trigger,
                trigger.position(), this.position(),
                "§6哦哦，此乃吾辈之戟也。",
                "§6则汝当是勇士也？",
                "§6剿灭邪龙之事可当是真？",
                "§e若如此，则吾用以狩猎邪龙之剑技",
                "§e汝当身以受之，加以习得即可…");
        /* 爵士退场(不是"被击败", 所以不触发石像/武器掉落) */
        /* ★ v112 修复: 退场前先把**自己的召唤物**清干净 —— 原来只有 die() 里做这件事,
           而蜕变走的是 discard(), 于是蜕变后 7 类召唤物(含**没有寿命**的巨型果蛆)继续在场上打玩家,
           甚至会穿过后面的 HiddenBossChallenge 对话。 */
        net.farmingtales.ftboss.boss.ai.SummonSweep.clear(this,
                net.farmingtales.ftboss.boss.ai.SummonSweep.RANGE,
                AppleWormEntity.class, AppleMothEntity.class, RottenAppleEntity.class,
                GreenAppleEntity.class, AppleCloneEntity.class, CoinManEntity.class,
                GiantGrubEntity.class);
        this.discard();
    }

/* ==================== 巨型果蛆(2026-09-12 按用户要求) ==================== */

    /** 召来一条极大的果蛆(咬中挂 反胃/饥饿/感染; 同时只允许 1 条) */
    private void summonGiantGrub(ServerLevel sl) {
        if (sl == null) return;
        var box = this.getBoundingBox().inflate(64.0);
        for (GiantGrubEntity g : sl.getEntitiesOfClass(GiantGrubEntity.class, box)) {
            if (g.isAlive()) {                       // 已经有了就不再召
                FTBossMod.LOGGER.info("[FTBoss][Apple] giant grub already alive, skip");
                return;
            }
        }
        GiantGrubEntity g = FTEntities.GIANT_GRUB.get().create(sl);
        if (g == null) return;
        double a = this.random.nextDouble() * Math.PI * 2;
        g.moveTo(this.getX() + Math.cos(a) * 4.0, this.getY(), this.getZ() + Math.sin(a) * 4.0,
                this.random.nextFloat() * 360f, 0f);
        g.setTarget(this.getTarget());
        sl.addFreshEntity(g);
        sl.playSound(null, g.getX(), g.getY(), g.getZ(),
                SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.6f, 0.5f);
        Fx.particles(sl, g.getX(), g.getY() + 1.0, g.getZ(), 40, ParticleTypes.ITEM_SLIME, 2.0, 0.12);
        FTBossMod.LOGGER.info("[FTBoss][Apple] GIANT GRUB summoned at {}, {}", (int) g.getX(), (int) g.getZ());
    }

    /* ==================== 天光光束轰炸 / 巨型火球(2026-09-12) ==================== */


    /** 苹果阵营(本体 / 分身 / 召唤物): 友军免伤判定。
     *  <p>v112 修复: 补上 {@link GiantGrubEntity} —— 巨型果蛆是本体的召唤物, 以前漏在这个名单外,
     *  于是自家"110 道真雷"和分身自爆会打自己的果蛆。 */
    public static boolean isAppleSide(Entity e) {
        return e instanceof AppleBossEntity || e instanceof AppleCloneEntity
                || e instanceof GiantGrubEntity
                || e instanceof AppleWormEntity || e instanceof AppleMothEntity
                || e instanceof RottenAppleEntity || e instanceof GreenAppleEntity
                || e instanceof CoinManEntity;
    }

    /**
     * 天光光束落地(2026-09-12 按用户要求): 以 3 秒前锁定的位置为中心, **半径 15 格内每波 13 处随机轰炸**(共 10 波)。
     * 每处随机是 火 / 冰 / 雷 / 凋零 四种元素; 雷属性落**真实闪电**(附近有友军时不落)。
     * 伤害只对玩家结算(半径 3.5, 基准 14 带衰减) —— 自身与召唤物完全不受影响。
     */
    private void skyBarrage(ServerLevel sl) {
        if (sl == null) return;
        sl.playSound(null, this.skyX, this.getY(), this.skyZ,
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 2.4f, 1.0f);
        /* 单波: 5 处天火 + 4 道雷(5 波 -> 25 处天火 / 20 道雷, 满足"天火至少 5、闪电 15 以上") */
        final int blasts = 16;                    // 每波 16 处: 5 天火 + 11 道雷(2026-09-12 加密)
        for (int i = 0; i < blasts; i++) {
            /* 2026-09-12 按用户要求: **更倾向于命中玩家** —— 60% 落点压在目标 6 格内, 其余在 15 格内散布 */
            LivingEntity tgt = this.getTarget();
            double a = this.random.nextDouble() * Math.PI * 2;
            double d, cx, cz;
            if (tgt != null && this.random.nextDouble() < 0.6) {
                d = Math.sqrt(this.random.nextDouble()) * 6.0;
                cx = tgt.getX();
                cz = tgt.getZ();
            } else {
                d = Math.sqrt(this.random.nextDouble()) * 15.0;
                cx = this.skyX;
                cz = this.skyZ;
            }
            double bx = cx + Math.cos(a) * d, bz = cz + Math.sin(a) * d, by = this.getY();
            if (i == 5 && tgt != null) { bx = tgt.getX(); bz = tgt.getZ(); }   // 每波第一道雷必劈玩家
            int kind = i < 5 ? 0 : 2;             // 前 5 处=天火(火), 后 8 处=雷
            switch (kind) {
                case 0 -> {
                    Fx.particles(sl, bx, by + 0.5, bz, 26, ParticleTypes.FLAME, 1.3, 0.14);
                    Fx.particles(sl, bx, by + 0.5, bz, 12, ParticleTypes.LAVA, 1.0, 0.05);
                }
                case 1 -> {
                    Fx.particles(sl, bx, by + 0.5, bz, 26, ParticleTypes.SNOWFLAKE, 1.3, 0.10);
                    Fx.particles(sl, bx, by + 0.5, bz, 14, ParticleTypes.ITEM_SNOWBALL, 1.0, 0.12);
                }
                case 2 -> {
                    Fx.particles(sl, bx, by + 0.5, bz, 30, ParticleTypes.ELECTRIC_SPARK, 1.4, 0.22);
                    /* 2026-09-12 修"闪电未体现": 原来附近有友军就跳过, 而半径压到 15 格后落点几乎全在
                       自家召唤物旁边 -> 雷几乎全被跳过。友伤已由 LivingHurtEvent 免疫, 所以这里**照落不误**。 */
                    var bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(sl);
                    if (bolt != null) {
                        bolt.moveTo(bx, by, bz);
                        sl.addFreshEntity(bolt);
                    }
                }
                default -> {
                    Fx.particles(sl, bx, by + 0.5, bz, 24, ParticleTypes.SOUL_FIRE_FLAME, 1.3, 0.10);
                    Fx.particles(sl, bx, by + 0.5, bz, 12, ParticleTypes.SQUID_INK, 1.0, 0.06);
                }
            }
            sl.playSound(null, bx, by, bz, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE,
                    0.9f, 1.2f + this.random.nextFloat() * 0.3f);
            for (Player p : sl.players()) {
                if (!p.isAlive()) continue;
                double pd = Math.sqrt(p.distanceToSqr(bx, by, bz));
                if (pd > 3.5) continue;
                p.hurt(this.damageSources().mobAttack(this),
                        (float) (7.0 * Math.max(0.35, 1.0 - pd / 3.5)));   // 14 → ×0.5(v77)
                switch (kind) {
                    case 0 -> p.setSecondsOnFire(4);
                    case 1 -> p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, true, true));
                    case 2 -> { }
                    default -> p.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0, false, true, true));
                }
                net.farmingtales.ftboss.util.BossKnockback.push(p, 0, 0.4, 0);
            }
        }
        FTBossMod.LOGGER.info("[FTBoss][Apple] sky barrage wave: {} blasts within r=15 around {}, {}",
                blasts, (int) this.skyX, (int) this.skyZ);
    }

    /** 巨型火球落地(2026-09-12 按用户要求): 半径 8 格, **基准 60 点 AoE**(边缘衰减到 40%) + 点燃 + 顶飞 */
    private void meteorImpact(ServerLevel sl) {
        if (sl == null) return;
        double x = this.metX, y = this.metY + 0.4, z = this.metZ;
        sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y + 1.4, z, 1, 0.0, 0.0, 0.0, 0.0);
        sl.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 2.4f, 0.5f);
        sl.playSound(null, x, y, z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 1.6f, 0.8f);
        Fx.particles(sl, x, y, z, 90, ParticleTypes.FLAME, 3.6, 0.20);
        Fx.particles(sl, x, y, z, 40, ParticleTypes.LAVA, 2.6, 0.10);
        Fx.particles(sl, x, y, z, 60, ParticleTypes.LARGE_SMOKE, 3.2, 0.12);
        final double R = 8.0;
        for (Player p : sl.players()) {
            if (!p.isAlive()) continue;
            double d = Math.sqrt(p.distanceToSqr(x, y, z));
            if (d > R) continue;
            p.hurt(this.damageSources().mobAttack(this), (float) (30.0 * Math.max(0.4, 1.0 - d / R)));   // 60 → ×0.5(v77)
            p.setSecondsOnFire(6);
            net.farmingtales.ftboss.util.BossKnockback.push(p, 0, 1.0, 0);
        }
        FTBossMod.LOGGER.info("[FTBoss][Apple] meteor impact (60 AoE, r=8) at {}, {}, {}",
                (int) x, (int) y, (int) z);
    }

    /* ==================== 烂苹果投掷 / 汁液喷洒 ==================== */

    /** 烂苹果投掷: 复用 RainbowPoopEntity 当投射物(外观用苹果), 带预判提前量 */
    private void throwRotten(ServerLevel sl) {
        if (sl == null) return;
        LivingEntity t = this.getTarget();
        net.farmingtales.ftboss.entity.RainbowPoopEntity proj =
                new net.farmingtales.ftboss.entity.RainbowPoopEntity(sl, this);
        proj.setRainbow(false);
        // 2026-09-18：ftboss:apple_core 已彻底删除，投射物外观改用原版苹果
        proj.setItem(new ItemStack(net.minecraft.world.item.Items.APPLE));
        proj.setDamage(12.0f + this.getPhase() * 3.0f);
        double yaw = Math.toRadians(this.getYRot());
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);
        double sx = this.getX() + fx * 1.4, sy = this.getY() + 1.6, sz = this.getZ() + fz * 1.4;
        proj.setPos(sx, sy, sz);
        if (t != null) {
            double dx = t.getX() - sx, dy = t.getEyeY() - sy, dz = t.getZ() - sz;
            double len = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
            double lead = Math.min(1.0, len / 24.0);
            dx += t.getDeltaMovement().x * lead * 12.0;
            dy += t.getDeltaMovement().y * lead * 6.0;
            dz += t.getDeltaMovement().z * lead * 12.0;
            double l2 = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
            proj.shoot(dx / l2, dy / l2 + 0.10, dz / l2, 1.25f, 0.8f);
        }
        sl.addFreshEntity(proj);
        sl.playSound(null, sx, sy, sz, SoundEvents.SNOWBALL_THROW, SoundSource.HOSTILE, 1.0f, 0.9f);
        Fx.particles(sl, sx, sy, sz, 10, ParticleTypes.ITEM_SLIME, 0.5, 0.06);
        FTBossMod.LOGGER.info("[FTBoss][Apple] throw rotten apple");
    }

    /**
     * 头顶三苹果・充能抛射(2026-09-12 按用户要求): 把头上那三颗小苹果当**抛射炸弹**打出去。
     * 三颗呈扇形、各带不同抛高(像充能球充好后一起放出去), 命中造成 14+3×阶段 伤害。
     * 动画里三颗会先鼓胀充能、射出后消失、再重新长出(orbit_shot)。
     */
    private void throwOrbitApples(ServerLevel sl) {
        if (sl == null) return;
        LivingEntity t = this.getTarget();
        final int count = 3;
        for (int i = 0; i < count; i++) {
            net.farmingtales.ftboss.entity.RainbowPoopEntity proj =
                    new net.farmingtales.ftboss.entity.RainbowPoopEntity(sl, this);
            proj.setRainbow(false);
            // 2026-09-18：ftboss:apple_core 已彻底删除，投射物外观改用原版苹果
            proj.setItem(new ItemStack(net.minecraft.world.item.Items.APPLE));
            proj.setDamage(14.0f + this.getPhase() * 3.0f);
            double spread = Math.toRadians((i - 1) * 12.0);
            double yaw = Math.toRadians(this.getYRot()) + spread;
            double sx = this.getX() - Math.sin(yaw) * 1.2, sy = this.getY() + 2.6, sz = this.getZ() + Math.cos(yaw) * 1.2;
            proj.setPos(sx, sy, sz);
            if (t != null) {
                double dx = t.getX() - sx, dy = t.getEyeY() - sy, dz = t.getZ() - sz;
                double len = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
                double lead = Math.min(1.0, len / 20.0);
                dx += t.getDeltaMovement().x * lead * 10.0;
                dz += t.getDeltaMovement().z * lead * 10.0;
                double l2 = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
                /* 抛物线: 给一个较高的初始仰角, 落点再靠重力 */
                proj.shoot(dx / l2, dy / l2 + 0.42, dz / l2, 1.15f + i * 0.06f, 0.4f);
            }
            sl.addFreshEntity(proj);
            Fx.particles(sl, sx, sy, sz, 8, ParticleTypes.CRIT, 0.4, 0.08);
        }
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.HOSTILE, 1.2f, 1.5f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 0.9f, 1.4f);
        FTBossMod.LOGGER.info("[FTBoss][Apple] orbit apples launched x{}", count);
    }

    /** 汁液喷洒(二阶段): 前方扇形粘稠果胶 —— 大幅减速 + 少量伤害 */
    private void juiceSpray(ServerLevel sl) {
        if (sl == null) return;
        double yaw = Math.toRadians(this.getYRot());
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);
        for (Player p : sl.players()) {
            if (!p.isAlive()) continue;
            double dx = p.getX() - this.getX(), dz = p.getZ() - this.getZ();
            double dl = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
            if (dl > 8.0) continue;
            double dot = (dx / dl) * fx + (dz / dl) * fz;
            if (Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, dot)))) > 55.0) continue;
            p.hurt(this.damageSources().mobAttack(this), 3.0f);            // 6 → ×0.5(v77)
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 2, false, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 120, 1, false, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, true, true));
        }
        for (int i = 0; i < 40; i++) {
            double a = yaw + (this.random.nextDouble() - 0.5) * Math.toRadians(110);
            double d = this.random.nextDouble() * 8.0;
            Fx.particles(sl, this.getX() - Math.sin(a) * d, this.getY() + 1.2 + this.random.nextDouble() * 0.8,
                    this.getZ() + Math.cos(a) * d, 1, ParticleTypes.ITEM_SLIME, 0.35, 0.05);
        }
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LLAMA_SPIT, SoundSource.HOSTILE, 1.3f, 0.6f);
        FTBossMod.LOGGER.info("[FTBoss][Apple] juice spray");
    }

    /* ==================== 钻地突袭(二阶段) ==================== */

    private void tickBurrow(ServerLevel sl, LivingEntity target) {
        this.burrowT--;
        if (sl != null) Fx.particles(sl, this.getX(), this.getY() + 0.2, this.getZ(), 4,
                ParticleTypes.SMOKE, 0.7, 0.03);
        if (this.burrowT > 0) return;
        LivingEntity t = target != null ? target : this.getTarget();
        if (t != null) this.setPos(t.getX(), t.getY(), t.getZ());
        this.setInvisible(false);
        this.setInvulnerable(false);
        this.playAction("summon", T_SUMMON);
        this.fxAt = 0;
        if (sl != null) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WARDEN_EMERGE, SoundSource.HOSTILE, 1.5f, 1.1f);
            /* 2026-09-12 按用户要求: 纯视觉爆炸(不再调用 sl.explode(), 它会摧毁掉落物 + 打死旁边分身) */
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 0.4, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            Fx.particles(sl, this.getX(), this.getY() + 0.5, this.getZ(), 50, ParticleTypes.LARGE_SMOKE, 2.0, 0.12);
            for (Player p : sl.players()) {
                if (!p.isAlive() || p.distanceToSqr(this) > 30.0) continue;
                p.hurt(this.damageSources().mobAttack(this), 10.0f + this.getPhase() * 1.5f);   // 20+3p → ×0.5(v77)
                net.farmingtales.ftboss.util.BossKnockback.push(p, 0, 0.85, 0);
            }
        }
        FTBossMod.LOGGER.info("[FTBoss][Apple] burrow dive emerge");
    }

    /* ==================== 变身 ==================== */

    /** 变身爆发帧: 到 1.2s(24t) 时转阶段 + 落地冲击 + 换二阶段 BGM */
    private void transformFx(ServerLevel sl) {
        if (this.fxAt < 0) this.fxAt = 0;
        if (this.fxAt == 24) {
            this.entityData.set(DATA_PHASE, 2);
            var spd = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (spd != null) spd.setBaseValue(BossWalkingSpeed.BASE_SPEED);
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1, false, false, false));
            /* 2026-09-12 按用户要求: 切换阶段获得一次**附魔金苹果**效果 */
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1, false, false, true));      // 再生 II 30s
            this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3, false, false, true));      // 吸收 IV 2min
            this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0, false, false, true));  // 防火 5min
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 0, false, false, true));// 抗性 I 5min
            this.heal(40.0f);
            this.appleCd = 300;      // 切换阶段后金苹果计时重置为 15 秒
            this.enchantCd = 900;    // 附魔金苹果计时重置为 45 秒
            /* 2026-09-12 按用户要求: 切换阶段**必定**放一次天光光束 + 巨型火球 */
            LivingEntity tt = this.getTarget();
            double tx = tt != null ? tt.getX() : this.getX();
            double ty = tt != null ? tt.getY() : this.getY();
            double tz = tt != null ? tt.getZ() : this.getZ();
            this.skyT = 60;
            this.skyX = tx;
            this.skyZ = tz;
            this.metT = METEOR_TELEGRAPH;
            this.metX = tx;
            this.metY = ty;
            this.metZ = tz;
            this.playAction("sky_beam", T_SKY);
            FTBossMod.LOGGER.info("[FTBoss][Apple] transform: enchanted golden apple + sky beam + meteor locked");
            if (sl != null) {
                /* 纯视觉爆炸(2026-09-12) */
                sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 0.4, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                Fx.particles(sl, this.getX(), this.getY() + 0.6, this.getZ(), 50,
                        ParticleTypes.SNEEZE, 2.4, 0.14);
                net.farmingtales.ftboss.sound.BossMusic.request(sl, this);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] phase -> 2 (BGM -> p2)");
        }
        this.fxAt++;
    }

    /** 动作动画播到爆发帧时结算效果(和动画关键帧对齐) */
    private void tickAction(ServerLevel sl, LivingEntity target) {
        if (this.fxAt < 0) return;
        if ("transform".equals(this.actionName)) return;   // 变身由 transformFx 单独推进, 避免重复计数
        this.fxAt++;
        switch (this.actionName) {
            case "seed_shot" -> {
                if (this.fxAt == 16) this.coneAttack(sl, 7.0, 70.0, 9.0f + this.getPhase() * 2.0f, false);
            }
            case "acid_spit" -> {
                if (this.fxAt == 15) this.coneAttack(sl, 6.5, 60.0, 12.0f + this.getPhase() * 3.0f, true);
            }
            case "throw_rotten" -> {
                if (this.fxAt == 12) this.throwRotten(sl);
            }
            case "juice_spray" -> {
                if (this.fxAt == 15) this.juiceSpray(sl);
            }
            case "orbit_shot" -> {
                if (this.fxAt == 21) this.throwOrbitApples(sl);   // 动画 1.05s(21t) 三苹果射出
            }
            case "wax_shield" -> {
                if (this.fxAt == 20) {
                    this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 140, 2, false, false, false));
                    this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1, false, false, false));
                    this.heal(20.0f);
                }
            }
            case "summon" -> {
                if (this.fxAt == 19) {
                    if (this.summonVariant == 1) this.summonClones(sl);
                    else if (this.summonVariant == 2) this.summonGiantGrub(sl);
                    else this.summonSwarm(sl);
                    this.summonVariant = 0;
                }
            }
            default -> { }
        }
    }

    /** 扇形攻击: 前方 range 格、半角 halfDeg 内的玩家吃伤害(+可选酸蚀) */
    private void coneAttack(ServerLevel sl, double range, double halfDeg, float dmg, boolean acid) {
        if (sl == null) return;
        double yaw = Math.toRadians(this.getYRot());
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);
        for (Player p : sl.players()) {
            if (!p.isAlive()) continue;
            double dx = p.getX() - this.getX(), dz = p.getZ() - this.getZ();
            double dl = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
            if (dl > range) continue;
            double dot = (dx / dl) * fx + (dz / dl) * fz;
            if (Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, dot)))) > halfDeg) continue;
            p.hurt(this.damageSources().mobAttack(this), dmg * 0.5f);      // ×0.5(v77)
            net.farmingtales.ftboss.util.BossKnockback.knockback(p, 0.7, this.getX() - p.getX(), this.getZ() - p.getZ());
            if (acid) {
                p.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 0, false, true, true));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, true, true));
            }
        }
        for (int i = 0; i < 26; i++) {
            double t = (this.random.nextDouble() - 0.5) * Math.toRadians(halfDeg * 2);
            double a = yaw + t;
            double d = this.random.nextDouble() * range;
            Fx.particles(sl, this.getX() - Math.sin(a) * d, this.getY() + 1.4 + this.random.nextDouble() * 0.6,
                    this.getZ() + Math.cos(a) * d, 1,
                    acid ? ParticleTypes.SNEEZE : ParticleTypes.CRIT, 0.25, 0.05);
        }
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                acid ? SoundEvents.LLAMA_SPIT : SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE, 1.2f, acid ? 0.7f : 1.3f);
    }

    /** 召唤五选一: 果虫 / 果蛾 / 烂苹果堆 / 青苹果 / 金币人; 同类满 10 只时改为全体强化 */
    private void summonSwarm(ServerLevel sl) {
        if (sl == null) return;
        int pick = this.random.nextInt(5);
        int want = 3 + this.random.nextInt(3) + (this.getPhase() >= 2 ? 2 : 0);   // 2026-09-12 召唤数量增加
        if (pick == 0) {
            var room = SummonLimit.room(this, AppleWormEntity.class);
            if (room <= 0) { SummonLimit.atCap(this, AppleWormEntity.class); return; }
            int n = Math.min(want, room);
            for (int i = 0; i < n; i++) {
                AppleWormEntity m = FTEntities.APPLE_WORM.get().create(sl);
                if (m == null) continue;
                m.moveTo(this.getX() + (this.random.nextDouble() - 0.5) * 3.5, this.getY(),
                        this.getZ() + (this.random.nextDouble() - 0.5) * 3.5, 0f, 0f);
                m.setTarget(this.getTarget());
                sl.addFreshEntity(m);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] summoned {} worms", n);
        } else if (pick == 1) {
            var room = SummonLimit.room(this, AppleMothEntity.class);
            if (room <= 0) { SummonLimit.atCap(this, AppleMothEntity.class); return; }
            int n = Math.min(want + 2, room);   // 蛾更多
            for (int i = 0; i < n; i++) {
                AppleMothEntity m = FTEntities.APPLE_MOTH.get().create(sl);
                if (m == null) continue;
                m.moveTo(this.getX() + (this.random.nextDouble() - 0.5) * 3.0,
                        this.getY() + 1.8 + this.random.nextDouble(),
                        this.getZ() + (this.random.nextDouble() - 0.5) * 3.0,
                        this.random.nextFloat() * 360f, 0f);
                m.setTarget(this.getTarget());
                sl.addFreshEntity(m);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] summoned {} moths", n);
        } else if (pick == 2) {
            var room = SummonLimit.room(this, RottenAppleEntity.class);
            if (room <= 0) { SummonLimit.atCap(this, RottenAppleEntity.class); return; }
            int n = Math.min(want, room);
            for (int i = 0; i < n; i++) {
                RottenAppleEntity m = FTEntities.ROTTEN_APPLE.get().create(sl);
                if (m == null) continue;
                m.moveTo(this.getX() + (this.random.nextDouble() - 0.5) * 3.5, this.getY(),
                        this.getZ() + (this.random.nextDouble() - 0.5) * 3.5, 0f, 0f);
                m.setTarget(this.getTarget());
                sl.addFreshEntity(m);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] summoned {} rotten apples", n);
        } else if (pick == 3) {
            var room = SummonLimit.room(this, GreenAppleEntity.class);
            if (room <= 0) { SummonLimit.atCap(this, GreenAppleEntity.class); return; }
            int n = Math.min(want + 3, room);   // 青苹果更多
            for (int i = 0; i < n; i++) {
                GreenAppleEntity m = FTEntities.GREEN_APPLE.get().create(sl);
                if (m == null) continue;
                m.moveTo(this.getX() + (this.random.nextDouble() - 0.5) * 3.5, this.getY(),
                        this.getZ() + (this.random.nextDouble() - 0.5) * 3.5, 0f, 0f);
                m.setTarget(this.getTarget());
                sl.addFreshEntity(m);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] summoned {} green apples", n);
        } else {
            var room = SummonLimit.room(this, CoinManEntity.class);
            if (room <= 0) { SummonLimit.atCap(this, CoinManEntity.class); return; }
            int n = Math.min(3 + this.random.nextInt(3) + (this.getPhase() >= 2 ? 2 : 0), room);   // 金币人更多
            for (int i = 0; i < n; i++) {
                CoinManEntity m = FTEntities.COIN_MAN.get().create(sl);
                if (m == null) continue;
                m.moveTo(this.getX() + (this.random.nextDouble() - 0.5) * 3.5, this.getY(),
                        this.getZ() + (this.random.nextDouble() - 0.5) * 3.5, 0f, 0f);
                m.setTarget(this.getTarget());
                sl.addFreshEntity(m);
            }
            FTBossMod.LOGGER.info("[FTBoss][Apple] summoned {} coin men", n);
        }
    }

    /* ============================ 生命周期 ============================ */
    @Override
    public void die(DamageSource source) {
        super.die(source);
        BossBars.untrack(this, this.bossBar);
        this.bossBar.removeAllPlayers();
        /* 2026-09-12 v58 按用户反馈「召唤物死不干净」: 果虫/果蛾/烂苹果/青苹果/分身/金币人/巨型果蛆 随主消散
           (它们各自有 600~900t 寿命, 这里保证主人一死就干净) */
        net.farmingtales.ftboss.boss.ai.SummonSweep.clear(this,
                net.farmingtales.ftboss.boss.ai.SummonSweep.RANGE,
                AppleWormEntity.class, AppleMothEntity.class, RottenAppleEntity.class,
                GreenAppleEntity.class, AppleCloneEntity.class, CoinManEntity.class,
                GiantGrubEntity.class);
        if (this.level() instanceof ServerLevel msl) net.farmingtales.ftboss.sound.BossMusic.release(msl, this);
        if (this.level() instanceof ServerLevel) {
            /* 凋落物: 金苹果 x1~2(武器与石像走 BossMercy)。
               2026-09-18 按用户要求【删除苹果核掉落】—— 连同物品注册与创造栏一并移除,
               所以这里不再补任何替代材料。 */
            this.spawnAtLocation(new ItemStack(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE,
                    1 + this.random.nextInt(2)));
            FTBossMod.LOGGER.info("[FTBoss][Apple] defeated (drops: golden apples; apple core removed)");
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        BossBars.untrack(this, this.bossBar);
        super.remove(reason);
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    /* ============================ GeckoLib ============================ */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "apple_ctrl", 4, this::predicate));
        /* 第二个控制器: 二阶段常驻"开盖 + 虫子探出"姿势(基础动画里没有 lid/worm 关键帧, 不会打架) */
        registrar.add(new AnimationController<>(this, "apple_p2_ctrl", 0, this::phaseTwoPredicate));
    }

    private PlayState phaseTwoPredicate(AnimationState<AppleBossEntity> state) {
        if (this.getPhase() >= 2 && !this.isDeadOrDying()) {
            state.setAnimation(LID_OPEN);
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private PlayState predicate(AnimationState<AppleBossEntity> state) {
        this.actionState.prepare(state);
        this.signatureCast.prepare(state);
        if (this.isDeadOrDying()) { state.setAnimation(DEATH); return PlayState.CONTINUE; }
        if (this.signatureCast.active()) {
            state.setAnimation("signature_self_cast".equals(this.signatureCast.name()) ? SIGNATURE_SELF : SIGNATURE_CAST);
            return PlayState.CONTINUE;
        }
        if (this.actionState.active()) {
            switch (this.actionState.name()) {
                case "attack" -> { state.setAnimation(ATTACK); return PlayState.CONTINUE; }
                case "roll_charge" -> { state.setAnimation(ROLL); return PlayState.CONTINUE; }
                case "seed_shot" -> { state.setAnimation(SEED); return PlayState.CONTINUE; }
                case "throw_rotten" -> { state.setAnimation(THROW); return PlayState.CONTINUE; }
                case "wax_shield" -> { state.setAnimation(WAX); return PlayState.CONTINUE; }
                case "summon" -> { state.setAnimation(SUMMON); return PlayState.CONTINUE; }
                case "transform" -> { state.setAnimation(TRANSFORM); return PlayState.CONTINUE; }
                case "acid_spit" -> { state.setAnimation(ACID); return PlayState.CONTINUE; }
                case "juice_spray" -> { state.setAnimation(ACID); return PlayState.CONTINUE; }
                case "orbit_shot" -> { state.setAnimation(ORBIT); return PlayState.CONTINUE; }
                case "sky_beam" -> { state.setAnimation(SUMMON); return PlayState.CONTINUE; }
                case "meteor" -> { state.setAnimation(TRANSFORM); return PlayState.CONTINUE; }
                default -> { state.setAnimation(HURT); return PlayState.CONTINUE; }
            }
        }
        float dYaw = net.minecraft.util.Mth.wrapDegrees(this.getYRot() - this.yRotO);
        if (Math.abs(dYaw) > 1.2f) { state.setAnimation(dYaw > 0 ? TURN_L : TURN_R); return PlayState.CONTINUE; }
        state.setAnimation(state.isMoving() ? WALK : IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public double getTick(Object o) { return this.tickCount; }
}
