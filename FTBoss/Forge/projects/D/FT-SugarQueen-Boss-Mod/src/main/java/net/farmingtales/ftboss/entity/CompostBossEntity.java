package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.sound.BossMusic;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.item.ItemStack;
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
 *  7  Boss: ****(Compost Superman, 2026-09-12 ).
 *
 * <p>:
 * <ul>
 *   <li>/, ****; (),  Create ****;</li>
 *   <li> 340( 300~400 ), ****( _p2 );</li>
 *   <li>: (BossScaling )/ Boss (BossBars)/  BGM(BossMusic)/ (BossMercy)</li>
 * </ul>
 * (////),  phase / 
 */
public class CompostBossEntity extends Monster implements GeoAnimatable, net.farmingtales.ftboss.integ.spells.RegularBossCastHost {
    private static final EntityDataAccessor<CompoundTag> DATA_ACTION =
            SynchedEntityData.defineId(CompostBossEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private final net.farmingtales.ftboss.boss.ai.BossActionState actionState =
            new net.farmingtales.ftboss.boss.ai.BossActionState(this, DATA_ACTION);
    private static final EntityDataAccessor<CompoundTag> DATA_SIGNATURE_CAST =
            SynchedEntityData.defineId(CompostBossEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private final net.farmingtales.ftboss.boss.ai.BossActionState signatureCast =
            new net.farmingtales.ftboss.boss.ai.BossActionState(this, DATA_SIGNATURE_CAST);

    /**  */
    private static final float P2_AT = 0.70f, P3_AT = 0.35f;
    /** () */

    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(CompostBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_EMPOWERED =
            SynchedEntityData.defineId(CompostBossEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.compost_superman.idle");
    private static final RawAnimation FLOAT_MOVE = RawAnimation.begin().thenLoop("animation.compost_superman.float_move");
    private static final RawAnimation TURN_L = RawAnimation.begin().thenLoop("animation.compost_superman.turn_l");
    private static final RawAnimation TURN_R = RawAnimation.begin().thenLoop("animation.compost_superman.turn_r");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.compost_superman.walk");
    private static final RawAnimation FIRE = RawAnimation.begin().thenPlay("animation.compost_superman.cannon_fire");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlay("animation.compost_superman.gas_charge");
    private static final RawAnimation SHIFT = RawAnimation.begin().thenPlay("animation.compost_superman.phase_shift");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.compost_superman.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.compost_superman.death");
    private static final RawAnimation TRANSFORM = RawAnimation.begin().thenPlay("animation.compost_superman.transform");
    private static final RawAnimation SIGNATURE_CAST = RawAnimation.begin().thenPlay("animation.compost_superman.signature_cast");
    private static final RawAnimation SIGNATURE_SELF = RawAnimation.begin().thenPlay("animation.compost_superman.signature_self_cast");
    private static final double T_FIRE = 32.0, T_CHARGE = 52.0, T_SHIFT = 44.0, T_TRANSFORM = 56.0, T_HURT = 9.0;

    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossBar;

    /** 连射: 剩余发数 / 间隔计时 */
    private int burstLeft = 0;
    private int burstGap = 0;
    private int burstCd = 120;
    /** lobbed poop volley, scheduled so it leaves the barrel after the cannon is raised */
    private int lobLeft = 0;
    private int lobGap = 0;
    /** skill timers + gas explosion fuse */
    private int gasCd = 200;
    private int lobCd = 160;
    private int gasFuse = -1;
    /** flight attacks (phase 2+): barrage + dive */
    private int barrageCd = 90;
    private int barrageWindup = -1;
    private int curseWindup = -1;
    private int diveCd = 200;
    private int diveTicks = -1;
    /** hovering laser ray (Hovering Inferno style) */
    private int laserCd = 120;
    private int laserTicks = -1;
    private int diveWindup = -1;
    /** 空中移动攻击: 环绕方向 / 投弹计时 */
    private int orbitDir = 1;
    private int bombCd = 20;
    private int bombWindup = -1;
    /** 光之女王式冲刺 */
    private int dashTicks = 0;
    private int dashCd = 20;
    /** 召唤大便小怪 */
    private int minionCd = 200;
    /** 召唤苍蝇群(2026-09-12) */
    private int flyCd = 180;
    /** 召唤蛆(2026-09-12, 复用故障机器人的蛆小怪) */
    private int maggotCd = 140;
    /** 召唤棕黑史莱姆(2026-09-12) */
    private int slimeCd = 160;
    /** 召唤羊屎蛋(2026-09-12) */
    private int dungCd = 120;
    /** 「屎？！！」概率施放(2026-09-12 晚: 取代原来的"命中即叠层") */
    private int curseCd = 200;
    private double dashX = 0, dashZ = 0;
    /** attack speed stacking: +1 per hit on a player, reset after 3s without a hit */
    private int hitStreak = 0;
    private int lastHitTick = -1000;
    private double actionTicks = 0.0;
    private String actionName = "";

    public CompostBossEntity(EntityType<? extends CompostBossEntity> type, Level level) {
        super(type, level);
        this.xpReward = 120;
        this.setPersistenceRequired();
        this.bossBar = new ServerBossEvent(this.getDisplayName(),
                BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
        BossBars.track(this, this.bossBar);
        if (!level.isClientSide) this.ensureWeapon();
    }

    @Override
    public void setSpeed(float speed) {
        super.setSpeed(BossWalkingSpeed.speed(this, speed));
        BossWalkingSpeed.normalizeInput(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 680.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, BossWalkingSpeed.BASE_SPEED)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PHASE, 1);
        this.entityData.define(DATA_ACTION, new CompoundTag());
        this.entityData.define(DATA_SIGNATURE_CAST, new CompoundTag());
        this.entityData.define(DATA_EMPOWERED, false);
    }

    public int getPhase() {
        return this.entityData.get(DATA_PHASE);
    }

    /** 血条(2026-09-12 补: 原来缺这个访问器 -> BossHandler 的"区块卸载清血条"够不到它, 血条会卡在屏幕上) */
    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    public boolean isEmpowered() {
        return this.entityData.get(DATA_EMPOWERED);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("phase", this.getPhase());
        tag.putBoolean("empowered", this.isEmpowered());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("phase")) this.entityData.set(DATA_PHASE, tag.getInt("phase"));
        if (tag.contains("empowered")) this.entityData.set(DATA_EMPOWERED, tag.getBoolean("empowered"));
        if (!this.level().isClientSide) this.ensureWeapon();
    }

    @Override
    public void startSignatureCast(net.farmingtales.ftboss.util.BossSpellRules.Kind kind, int castTicks) {
        this.signatureCast.start(net.farmingtales.ftboss.integ.spells.RegularBossCastHost.action(kind),
                net.farmingtales.ftboss.integ.spells.RegularBossCastHost.visualTicks(castTicks));
    }

    /** () */
    public void playAction(String name, double ticks) {
        this.actionState.start(name, (int) ticks);
        this.actionName = name;
        this.actionTicks = ticks;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hit = super.hurt(source, amount);
        if (hit && this.actionTicks <= 0 && !this.isDeadOrDying()) {
            this.playAction("hurt", T_HURT);
        }
        return hit;
    }

    private void ensureWeapon() {
        ItemStack weapon = this.getMainHandItem();
        if (!weapon.is(FTBossItems.COMPOST_CROWN.get())) {
            weapon = new ItemStack(FTBossItems.COMPOST_CROWN.get());
            this.setItemInHand(InteractionHand.MAIN_HAND, weapon);
        }
        if (!weapon.getOrCreateTag().contains("AttributeModifiers", Tag.TAG_LIST)) {
            weapon.getOrCreateTag().put("AttributeModifiers", new ListTag());
        }
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (!this.isAlive()) { cancelPendingSkills(); return; }
        if (this.getTarget() == null || !this.getTarget().isAlive()
                || !net.farmingtales.ftboss.boss.ai.BossTargets.valid(this.getTarget())) cancelPendingSkills();
        if (this.actionTicks > 0) this.actionTicks -= 1.0;
        this.ensureWeapon();
        /* () */
        float ratio = this.getMaxHealth() > 0 ? this.getHealth() / this.getMaxHealth() : 1f;
        int want = ratio <= P3_AT ? 3 : (ratio <= P2_AT ? 2 : 1);
        if (want > this.getPhase()) {
            cancelPendingSkills();
            this.entityData.set(DATA_PHASE, want);
            if (want >= 2 && this.level() instanceof ServerLevel p2sl) {
                BossMusic.request(p2sl, this);
            }
            this.entityData.set(DATA_EMPOWERED, want >= 2);
            /* phase 2 gets the dedicated transform reveal; later phase changes keep phase_shift */
            if (want == 2) this.playAction("transform", T_TRANSFORM);
            else this.playAction("phase_shift", T_SHIFT);
            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.4f, 0.7f);
                net.farmingtales.ftboss.util.Fx.particles(sl, this.getX(), this.getY() + 1.0, this.getZ(), 24, ParticleTypes.HAPPY_VILLAGER, 0.8, 0.05);
            }
            FTBossMod.LOGGER.info("[FTBoss]  {}", want);
        }
        /*  + BGM */
        /* hover flight in phase 2 (2026-09-12): 二阶段直接飞起来 */
        boolean fly = this.getPhase() >= 2;
        if (this.isNoGravity() != fly) this.setNoGravity(fly);
        if (fly && this.diveTicks <= 0 && this.level() instanceof ServerLevel fsl) {
            /* 2026-09-12: 飞行时**头/身都要转向玩家**(原来悬停期间完全不转) */
            if (this.getTarget() != null) {
                net.minecraft.world.entity.LivingEntity look = this.getTarget();
                this.getLookControl().setLookAt(look, 30.0f, 30.0f);
                float wantYaw = (float) (Math.toDegrees(Math.atan2(look.getZ() - this.getZ(), look.getX() - this.getX()))) - 90.0f;
                this.setYRot(wantYaw);
                this.yBodyRot = wantYaw;
                this.yHeadRot = wantYaw;
            }
            net.minecraft.core.BlockPos gp = fsl.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.blockPosition());
            /* 2026-09-12 按用户要求: 空中改为**移动攻击**(光之女王式) —— 绕玩家环绕/穿插, 高度保持在地面上方 4 格 */
            double wantY = gp.getY() + 5.0 + Math.sin(this.tickCount * 0.08) * 0.3;   // 2026-09-12: 高度 +1 格
            double cy = this.getY();
            this.setDeltaMovement(0, 0, 0);
            net.farmingtales.ftboss.util.BossCollision.move(this,
                    new Vec3(0, Math.max(-0.3, Math.min(0.3, wantY - cy)), 0));
            if (this.getTarget() != null) {
                LivingEntity mt = this.getTarget();
                this.orbitDir = (this.tickCount / 40) % 2 == 0 ? 1 : -1;      // 每 40t 换向 -> 来回穿插
                double ang = this.tickCount * 0.045 * this.orbitDir;
                double radius = 6.5 + Math.sin(this.tickCount * 0.02) * 2.0;   // 半径 4.5~8.5 波动
                double tx = mt.getX() + Math.cos(ang) * radius;
                double tz = mt.getZ() + Math.sin(ang) * radius;
                double dx = tx - this.getX(), dz = tz - this.getZ();
                double dl = Math.max(0.001, Math.sqrt(dx * dx + dz * dz));
                /* 2026-09-12 按用户要求: 学习**光之女王**的移动 —— 悬停定位后高速直线冲刺 */
                if (this.dashTicks <= 0) {
                    this.dashCd--;
                    if (this.dashCd <= 0) {
                        this.dashCd = 26 + this.random.nextInt(14);
                        this.dashTicks = 14;
                        this.dashX = -Math.sin(Math.toRadians(this.getYRot()));
                        this.dashZ = Math.cos(Math.toRadians(this.getYRot()));
                    }
                }
                double step;
                if (this.dashTicks > 0) {
                    this.dashTicks--;
                    step = 0.95;                       // 冲刺速度
                    net.farmingtales.ftboss.util.BossCollision.move(this,
                            new Vec3(this.dashX * step, 0, this.dashZ * step));
                } else {
                    step = Math.min(0.55, dl);         // 基础移动也更快(0.38 -> 0.55)
                }
                net.farmingtales.ftboss.util.BossCollision.move(this,
                        new Vec3(dx / dl * step, 0, dz / dl * step));
                /* 移动中**向下投掷答辩炸弹**(每 24t) */
                if (--this.bombCd <= 0 && canStartSkill() && this.gasCd > 0 && this.lobCd > 0
                        && this.burstCd > 0 && this.barrageCd > 0 && this.diveCd > 0 && this.curseCd > 0) {
                    this.bombCd = 30;   // 2026-09-12 按用户要求"减少炸弹": 12 -> 30t
                    this.playAction("cannon_fire", T_FIRE);   // 2026-09-12: 投弹前抬手
                    this.bombWindup = 17;
                }
                if (this.bombWindup > 0 && --this.bombWindup == 0) this.dropBomb(mt, fsl);
            }
        }

        /* ===== 飞行攻击(阶段 2+): 空中弹幕 + 俯冲撞击 ===== */
        if (fly && this.getTarget() != null && this.getTarget().isAlive()) {
            LivingEntity ft = this.getTarget();
            /* ② 空中弹幕: 3 发扇形(向下压角) */
            if (this.barrageCd > 0) this.barrageCd--;
            if (canStartSkill() && this.barrageCd <= 0) {
                this.barrageCd = 170 + this.random.nextInt(60);   // 减少弹幕
                this.playAction("cannon_fire", T_FIRE);
                this.barrageWindup = 17;
            }
            if (this.barrageWindup > 0 && --this.barrageWindup == 0 && this.level() instanceof ServerLevel bsl2) {
                for (int i = -2; i <= 2; i++) {   // 2026-09-12: 3 -> 5 发(更宽的扇形)
                    RainbowPoopEntity poop = new RainbowPoopEntity(bsl2, this);
                    poop.setRainbow(true);
                    poop.setDamage(9.0f + this.getPhase() * 2.5f);
                    double yaw = Math.toRadians(this.getYRot());
                    double fwx = -Math.sin(yaw), fwz = Math.cos(yaw);
                    double sx = this.getX() + fwx * 1.1, sy = this.getY() + 1.2, sz = this.getZ() + fwz * 1.1;
                    poop.setPos(sx, sy, sz);
                    double dx = ft.getX() - sx + i * 1.9, dy = ft.getEyeY() - sy, dz = ft.getZ() - sz + i * 1.9;
                    double len = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
                    poop.shoot(dx / len, Math.min(-0.15, dy / len), dz / len, 1.3f, 0.8f);   // 向下压角
                    bsl2.addFreshEntity(poop);
                    this.muzzleFx(bsl2, sx, sy, sz, fwx, fwz);   // 每发都喷一口炮口烟
                }
                bsl2.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.HOSTILE, 1.2f, 1.1f);
            }
            /* ②b 悬停激光射线(炼狱游者 shield_lasers 风格): 锁定玩家, 每 2t 伤害一次, 持续 20t */
            /* 2026-09-12 修 bug: 原来误加了 barrageCd > 0 的限制 -> 激光永远不触发 */
            if (false && this.laserTicks < 0 && this.diveTicks <= 0 && this.laserCd > 0 && --this.laserCd <= 0) {   // 2026-09-12 按用户要求: 去除激光效果
                this.laserCd = 150 + this.random.nextInt(60);
                this.laserTicks = 20;
                this.playAction("cannon_fire", T_FIRE);
                if (this.level() instanceof ServerLevel lsl) {
                    lsl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0f, 1.4f);
                }
            }
            if (this.laserTicks > 0) {
                this.laserTicks--;
                if (this.level() instanceof ServerLevel lsl) {
                    /* 从本体朝玩家画一条射线: 每 0.6 格一个光点, 命中半径 1.3 */
                    double lx = ft.getX() - this.getX(), ly = ft.getEyeY() - (this.getY() + 1.4), lz = ft.getZ() - this.getZ();
                    double len = Math.max(0.1, Math.sqrt(lx * lx + ly * ly + lz * lz));
                    for (double d = 1.0; d <= Math.min(24.0, len + 1.0); d += 0.6) {
                        net.farmingtales.ftboss.util.Fx.particles(lsl,
                                this.getX() + lx / len * d, this.getY() + 1.4 + ly / len * d, this.getZ() + lz / len * d,
                                1, ParticleTypes.END_ROD, 0.03, 0.0);
                    }
                    if (this.laserTicks % 2 == 0) {
                        for (Player lp : lsl.players()) {
                            if (!lp.isAlive()) continue;
                            /* 点到直线距离判定 */
                            double px = lp.getX() - this.getX(), py = lp.getEyeY() - (this.getY() + 1.4), pz = lp.getZ() - this.getZ();
                            double t = (px * lx + py * ly + pz * lz) / (len * len);
                            if (t < 0.0 || t > 1.0) continue;
                            double cx = px - lx * t, cy = py - ly * t, cz = pz - lz * t;
                            if (cx * cx + cy * cy + cz * cz <= 1.7) {
                                lp.invulnerableTime = 0;
                                lp.hurt(this.damageSources().mobAttack(this), 4.5f + this.getPhase() * 1.0f);   // ×0.5(v77)
                            }
                        }
                    }
                }
            }

            /* ③ 俯冲撞击 */
            if (this.diveCd > 0) this.diveCd--;
            if (canStartSkill() && this.diveCd <= 0) {
                this.diveCd = 180 + this.random.nextInt(60);
                this.diveWindup = 14;                       // 2026-09-12: 俯冲前先悬停蓄势(可见的抬手)
                this.playAction("phase_shift", T_SHIFT);
            }
            if (this.diveWindup > 0 && --this.diveWindup == 0) this.diveTicks = 40;
            if (this.diveTicks > 0) {
                this.diveTicks--;
                double dx = ft.getX() - this.getX(), dy = ft.getEyeY() - this.getY(), dz = ft.getZ() - this.getZ();
                double len = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
                this.setDeltaMovement(dx / len * 0.55, dy / len * 0.55, dz / len * 0.55);
                for (Player dp : this.level().players()) {
                    if (net.farmingtales.ftboss.boss.ai.BossTargets.valid(dp) && this.distanceToSqr(dp) < 3.24) {
                        dp.hurt(this.damageSources().mobAttack(this), 9.0f + this.getPhase() * 2.0f);   // ×0.5(v77)
                        net.farmingtales.ftboss.util.BossKnockback.knockback(dp, 1.2, this.getX() - dp.getX(), this.getZ() - dp.getZ());
                        dp.setSecondsOnFire(3);
                        this.diveTicks = 0;      // 撞到就结束这次俯冲
                    }
                }
                if (this.diveTicks == 0 && this.level() instanceof ServerLevel dsl) {
                    net.farmingtales.ftboss.util.Fx.particles(dsl, this.getX(), this.getY(), this.getZ(), 24, ParticleTypes.ITEM_SLIME, 1.6, 0.1);
                    dsl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.2f, 0.8f);
                }
            }
        }

        /* 2026-09-12: **不免疫 debuff**, 但每有一个负面效果就获得等额强化(力量/速度/抗性) */
        if (this.tickCount % 40 == 0) {
            int debuffs = 0;
            for (net.minecraft.world.effect.MobEffectInstance inst : this.getActiveEffects()) {
                if (!inst.getEffect().isBeneficial()) debuffs++;
            }
            if (debuffs > 0) {
                int amp = Math.min(4, debuffs - 1);
                this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 90, amp, false, false, false));
                this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 90, Math.min(3, amp), false, false, false));
                this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 90, Math.min(3, amp), false, false, false));
                FTBossMod.LOGGER.info("[FTBoss][CompostMan] absorbed {} debuffs -> buff amp {}", debuffs, amp);
            }
        }

        /* 2026-09-12 按用户要求: **完全移除铁魔法技能**(原黑洞/魔法飞弹已删), 腾出的时间交给召唤物 */

        /* 2026-09-12: 二阶段召唤**可移动的大便小怪**
           (2026-09-12 晚按用户要求: 同种场上满 10 只就不再召唤, 改为给它们全体上 buff, 见 SummonLimit) */
        if (this.getPhase() >= 2 && this.minionCd > 0 && --this.minionCd <= 0 && this.level() instanceof ServerLevel msl) {
            this.minionCd = 240;
            if (!net.farmingtales.ftboss.boss.ai.SummonLimit.atCap(this, PoopMinionEntity.class)) {
                int n = Math.min(3 + this.random.nextInt(2),
                        net.farmingtales.ftboss.boss.ai.SummonLimit.room(this, PoopMinionEntity.class));
                for (int i = 0; i < n; i++) {
                    PoopMinionEntity m = FTEntities.POOP_MINION.get().create(msl);
                    if (m == null) continue;
                    m.moveTo(this.getX() + (this.random.nextDouble() - 0.5) * 3, this.getY(), this.getZ() + (this.random.nextDouble() - 0.5) * 3, 0f, 0f);
                    if (this.getTarget() != null) m.setTarget(this.getTarget());
                    msl.addFreshEntity(m);
                }
                FTBossMod.LOGGER.info("[FTBoss][CompostMan] summoned {} poop minions", n);
            }
        }

        /* 2026-09-12 按用户要求: 召唤**苍蝇群** —— 无重力小飞虫, 绕着玩家打转,
           咬到只刷新「屎？！！」的时长(不加层, 见 BossHandler), 30s 后自行消散 */
        if (this.getPhase() >= 2 && this.flyCd > 0 && --this.flyCd <= 0 && this.level() instanceof ServerLevel fsl) {
            this.flyCd = 200 + this.random.nextInt(80);
            if (!net.farmingtales.ftboss.boss.ai.SummonLimit.atCap(this, FlyMinionEntity.class)) {
                int n = Math.min(4 + this.random.nextInt(3),
                        net.farmingtales.ftboss.boss.ai.SummonLimit.room(this, FlyMinionEntity.class));
                for (int i = 0; i < n; i++) {
                    FlyMinionEntity f = FTEntities.FLY_MINION.get().create(fsl);
                    if (f == null) continue;
                    f.moveTo(this.getX() + (this.random.nextDouble() - 0.5) * 2.5,
                            this.getY() + 1.6 + this.random.nextDouble() * 1.2,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 2.5,
                            this.random.nextFloat() * 360.0f, 0.0f);
                    if (this.getTarget() != null) f.setTarget(this.getTarget());
                    fsl.addFreshEntity(f);
                }
                fsl.playSound(null, this.getX(), this.getY() + 1.5, this.getZ(),
                        net.minecraft.sounds.SoundEvents.BEE_LOOP_AGGRESSIVE,
                        net.minecraft.sounds.SoundSource.HOSTILE, 1.1f, 1.6f);
                FTBossMod.LOGGER.info("[FTBoss][CompostMan] summoned {} flies", n);
            }
        }

        /* 2026-09-12 按用户要求: 召唤**蛆**(直接复用故障机器人的蛆小怪) —— 从肥料堆里爬向玩家 */
        /* v112 修复(同下面几处): 递减不能写在 && 链里 —— 到点那一 tick 若目标为空(玩家阵亡时
           TargetGoal.stop() 会 setTarget(null)), 字段会被永久钉在 0, 该技能整场失效。
           先无条件递减, 再单独判"到点 + 条件满足"。 */
        if (this.maggotCd > 0) this.maggotCd--;
        if (this.maggotCd <= 0 && this.level() instanceof ServerLevel gsl
                && this.getTarget() != null && this.getTarget().isAlive()) {
            this.maggotCd = 170 + this.random.nextInt(80);
            if (!net.farmingtales.ftboss.boss.ai.SummonLimit.atCap(this, MaggotMinionEntity.class)) {
                int n = Math.min(3 + this.random.nextInt(3),
                        net.farmingtales.ftboss.boss.ai.SummonLimit.room(this, MaggotMinionEntity.class));
                for (int i = 0; i < n; i++) {
                    MaggotMinionEntity g = FTEntities.MAGGOT_MINION.get().create(gsl);
                    if (g == null) continue;
                    g.moveTo(this.getX() + (this.random.nextDouble() - 0.5) * 3.5, this.getY(),
                            this.getZ() + (this.random.nextDouble() - 0.5) * 3.5, 0.0f, 0.0f);
                    g.setTarget(this.getTarget());
                    net.farmingtales.ftboss.boss.ai.MinionBlessing.mark(g, this.getUUID());   // v112: 认主人(和故障机器人的蛆区分开)
                    gsl.addFreshEntity(g);
                }
                FTBossMod.LOGGER.info("[FTBoss][CompostMan] summoned {} maggots", n);
            }
        }

        /* 2026-09-12 按用户要求: 召唤**棕黑史莱姆**(继承原版史莱姆: 会弹跳、挤压、死亡分裂成小的) */
        if (this.slimeCd > 0) this.slimeCd--;                       // v112: 递减移出 && 链(避免闩锁)
        if (this.slimeCd <= 0 && this.level() instanceof ServerLevel ssl
                && this.getTarget() != null && this.getTarget().isAlive()) {
            this.slimeCd = 220 + this.random.nextInt(90);
            if (!net.farmingtales.ftboss.boss.ai.SummonLimit.atCap(this, CompostSlimeEntity.class)) {
                int n = Math.min(3 + this.random.nextInt(2),
                        net.farmingtales.ftboss.boss.ai.SummonLimit.room(this, CompostSlimeEntity.class));
                for (int i = 0; i < n; i++) {
                    CompostSlimeEntity s = FTEntities.COMPOST_SLIME.get().create(ssl);
                    if (s == null) continue;
                    s.setSize(2 + this.random.nextInt(3), true);     // 2~4 号(原版 1 号小史莱姆不造成伤害)
                    s.moveTo(this.getX() + (this.random.nextDouble() - 0.5) * 3.5, this.getY(),
                            this.getZ() + (this.random.nextDouble() - 0.5) * 3.5, 0.0f, 0.0f);
                    s.setTarget(this.getTarget());
                    ssl.addFreshEntity(s);
                }
                FTBossMod.LOGGER.info("[FTBoss][CompostMan] summoned {} compost slimes", n);
            }
        }

        /* 2026-09-12 按用户要求新增: 召唤**羊屎蛋**(岩浆怪套皮, 死亡后炸开) */
        if (this.dungCd > 0) this.dungCd--;                         // v112: 递减移出 && 链(避免闩锁)
        if (this.dungCd <= 0 && this.level() instanceof ServerLevel dsl
                && this.getTarget() != null && this.getTarget().isAlive()) {
            this.dungCd = 200 + this.random.nextInt(80);
            if (!net.farmingtales.ftboss.boss.ai.SummonLimit.atCap(this, SheepDungEntity.class)) {
                int n = Math.min(3 + this.random.nextInt(3),
                        net.farmingtales.ftboss.boss.ai.SummonLimit.room(this, SheepDungEntity.class));
                for (int i = 0; i < n; i++) {
                    SheepDungEntity d = FTEntities.SHEEP_DUNG.get().create(dsl);
                    if (d == null) continue;
                    d.setSize(1 + this.random.nextInt(2), true);   // 1~2 号小颗粒(大颗粒自己会分裂)
                    d.moveTo(this.getX() + (this.random.nextDouble() - 0.5) * 3.5, this.getY() + 0.4,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 3.5, 0.0f, 0.0f);
                    d.setTarget(this.getTarget());
                    dsl.addFreshEntity(d);
                }
                dsl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        net.minecraft.sounds.SoundEvents.SLIME_BLOCK_PLACE,
                        net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.3f);
                FTBossMod.LOGGER.info("[FTBoss][CompostMan] summoned {} sheep dung", n);
            }
        }

        /* 2026-09-12 晚按用户要求: 「屎？！！」改成**概率施放一次** ——
           每 10~16 秒尝试一次, 命中概率 35%, 命中后固定 I 级 / 8 秒, 不叠层;
           (被命中、被召唤物咬中都不再触发, 施放频率比原来低得多) */
        if (this.curseCd > 0) this.curseCd--;
        if (this.curseCd <= 0 && canStartSkill() && this.level() instanceof ServerLevel csl) {
            this.curseCd = 200 + this.random.nextInt(120);
            if (this.getTarget() != null && this.getTarget().isAlive()
                    && this.getTarget() instanceof net.minecraft.server.level.ServerPlayer sp
                    && this.distanceToSqr(sp) <= 32.0 * 32.0
                    && this.random.nextFloat() < 0.35f) {
                this.playAction("cannon_fire", T_FIRE);   // 抬炮"施法"的起手动作
                this.curseWindup = 17;
                csl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.2f, 0.6f);
            }
        }

        if (this.curseWindup > 0 && --this.curseWindup == 0
                && this.getTarget() instanceof net.minecraft.server.level.ServerPlayer victim
                && net.farmingtales.ftboss.boss.ai.BossTargets.valid(victim)
                && this.hasLineOfSight(victim) && this.distanceToSqr(victim) <= 32 * 32) {
            net.farmingtales.ftboss.boss.ai.PoopCurse.castOnce(victim, "答辩超人");
        }

        // attack speed decay: no hit on a player for 3s -> reset
        if (this.tickCount - this.lastHitTick > 60) this.hitStreak = 0;

        // SKILL: lobbed poop (3 shots, upward arc), delayed to match the 16t cannon raise
        if (this.lobCd > 0) this.lobCd--;                           // v112: 递减移出 && 链(避免闩锁)
        if (this.lobCd <= 0 && canStartSkill()) {
            this.lobCd = 140 + this.random.nextInt(80);
            this.playAction("cannon_fire", T_FIRE);
            this.lobLeft = 3;
            this.lobGap = 17;
        }
        if (this.lobLeft > 0 && --this.lobGap <= 0) {
            this.lobGap = 4;
            this.fireLob(3 - this.lobLeft);
            this.lobLeft--;
        }
        // SKILL: gas charge -> explosion (8 blocks)
        if (this.gasCd > 0) this.gasCd--;                           // v112: 递减移出 && 链(避免闩锁)
        if (this.gasCd <= 0 && canStartSkill()) {
            this.gasCd = 200 + this.random.nextInt(100);
            this.playAction("gas_charge", 52.0);
            this.gasFuse = 41;
        }
        if (this.gasFuse > 1 && this.tickCount % 4 == 0) {
            net.farmingtales.ftboss.util.Fx.ring(this.level(), this.getX(), this.getY() + 0.1,
                    this.getZ(), 8, 24, ParticleTypes.COMPOSTER, 0.0);
        }
        if (this.gasFuse > 0 && --this.gasFuse == 0) this.gasExplode();
        /* fire burst: 4-5 normal poops */
        if (this.burstCd > 0) this.burstCd--;
        if (this.burstLeft > 0) {
            if (--this.burstGap <= 0) {
                this.burstGap = Math.max(1, 3 - this.hitStreak / 4);
                this.burstLeft--;
                this.firePoop();
            }
        } else if (this.burstCd <= 0 && canStartSkill()) {
            /* 攻速叠层: 每命中一次玩家就更快(max 12 层), 3 秒未命中回到基础速度 */
            this.burstCd = Math.max(16, 55 - this.hitStreak * 4) + this.random.nextInt(12);
            this.burstLeft = 4 + this.random.nextInt(2);
            this.burstGap = 16;   // 开火前 12t 抬炮(可见的抬手动作)
            this.playAction("cannon_fire", T_FIRE);
        }
        if (this.bossBar != null) {
            this.bossBar.setProgress(Math.max(0f, Math.min(1f, ratio)));
            this.bossBar.setVisible(this.isAlive());
            for (Player p : this.level().players()) {
                if (p instanceof net.minecraft.server.level.ServerPlayer sp) {
                    if (this.distanceToSqr(sp) <= 48.0 * 48.0) this.bossBar.addPlayer(sp);
                    else this.bossBar.removePlayer(sp);
                }
            }
        }
        if (this.level() instanceof ServerLevel bsl) BossMusic.request(bsl, this);
        /* (: ; ) */
    }

    private boolean canStartSkill() {
        return this.actionTicks <= 0 && this.diveTicks <= 0 && this.diveWindup <= 0
                && this.burstLeft <= 0 && this.lobLeft <= 0 && this.getTarget() != null
                && this.getTarget().isAlive() && net.farmingtales.ftboss.boss.ai.BossTargets.valid(this.getTarget());
    }

    private void cancelPendingSkills() {
        this.burstLeft = 0;
        this.lobLeft = 0;
        this.gasFuse = -1;
        this.bombWindup = -1;
        this.barrageWindup = -1;
        this.curseWindup = -1;
        this.diveWindup = -1;
        this.diveTicks = -1;
    }

    /** 发射一发答辩(二阶段彩色) */
    /** called by the projectile when it hits a player */
    public void noteHitOnPlayer() {
        this.hitStreak = Math.min(12, this.hitStreak + 1);
        this.lastHitTick = this.tickCount;
    }

    /** 0..12 */
    public int getHitStreak() {
        return this.hitStreak;
    }

    /** 向下投掷答辩炸弹(移动攻击用): 竖直下压落向玩家当前位置, 轻微散布 */
    private void dropBomb(LivingEntity target, ServerLevel sl) {
        RainbowPoopEntity bomb = new RainbowPoopEntity(sl, this);
        bomb.setRainbow(this.getPhase() >= 2);
        bomb.setDamage(10.0f + this.getPhase() * 3.0f);   // 2026-09-12: 炸弹增伤
        bomb.setPos(this.getX(), this.getY() - 0.4, this.getZ());
        double dx = (target.getX() + (this.random.nextDouble() - 0.5) * 2.0) - this.getX();
        double dz = (target.getZ() + (this.random.nextDouble() - 0.5) * 2.0) - this.getZ();
        double dl = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
        /* 2026-09-12 参考酒桶王的抛射: 水平引导 + 明显上抛 -> 抛物线砸向玩家(迫击炮手感) */
        bomb.shoot(dx / dl * 0.95, 1.05, dz / dl * 0.95, 1.15f, 0.45f);
        sl.addFreshEntity(bomb);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), net.minecraft.sounds.SoundEvents.SNOWBALL_THROW,
                net.minecraft.sounds.SoundSource.HOSTILE, 0.8f, 0.7f);
    }

    /** lobbed poop: upward arc toward the target */
    private void fireLob(int index) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        LivingEntity t = this.getTarget();
        if (t == null) return;
        RainbowPoopEntity poop = new RainbowPoopEntity(sl, this);
        poop.setRainbow(this.getPhase() >= 2);
        poop.setDamage(7.0f + this.getPhase() * 2.5f);
        double yaw = Math.toRadians(this.getYRot());
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);
        double sx = this.getX() + fx * 1.2, sy = this.getY() + 2.1, sz = this.getZ() + fz * 1.2;
        poop.setPos(sx, sy, sz);
        double dx = t.getX() - sx + (index - 1) * 1.2, dy = t.getEyeY() - sy, dz = t.getZ() - sz + (index - 1) * 1.2;
        double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
        poop.shoot(dx / len, 0.55 + dy / Math.max(4.0, len * 2.0), dz / len, 1.05f, 1.2f);
        sl.addFreshEntity(poop);
        sl.playSound(null, sx, sy, sz, SoundEvents.SNOWBALL_THROW, SoundSource.HOSTILE, 1.0f, 1.2f);
        this.muzzleFx(sl, sx, sy, sz, fx, fz);
    }

    /** gas charge explosion: 8 blocks, damage + ignite + brown particles */
    private void gasExplode() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        double x = this.getX(), y = this.getY() + 0.6, z = this.getZ();
        sl.explode(this, x, y, z, 1.6f, net.minecraft.world.level.Level.ExplosionInteraction.NONE);   // 3.2 → ×0.5(v77)
        sl.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.8f, 0.6f);
        net.farmingtales.ftboss.util.Fx.particles(sl, x, y, z, 60, ParticleTypes.ITEM_SLIME, 2.6, 0.15);
        net.farmingtales.ftboss.util.Fx.particles(sl, x, y, z, 20, ParticleTypes.LARGE_SMOKE, 2.0, 0.08);
        for (Player p : sl.players()) {
            if (p.distanceToSqr(this) <= 64.0 && p.isAlive()) {
                p.hurt(this.damageSources().mobAttack(this), 8.0f + this.getPhase() * 1.5f);   // ×0.5(v77)
                p.setSecondsOnFire(4);
            }
        }
        FTBossMod.LOGGER.info("[FTBoss][CompostMan] gas explosion at {}, {}, {}", (int) x, (int) y, (int) z);
    }

    private void firePoop() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        LivingEntity t = this.getTarget();
        RainbowPoopEntity poop = new RainbowPoopEntity(sl, this);
        poop.setRainbow(this.getPhase() >= 2);
        poop.setDamage(6.0f + this.getPhase() * 2.0f);
        /* 2026-09-12: 从**手持土豆加农炮的炮口**发射(右手 + 前方), 而不是身体中心(否则像口吐) */
        double yaw = Math.toRadians(this.getYRot());
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);      // 前方
        double rx = Math.cos(yaw), rz = Math.sin(yaw);       // 右侧
        double sx = this.getX() + rx * 1.15 + fx * 1.10;
        double sy = this.getY() + 1.85 + (this.actionTicks > 0 ? 0.35 : 0.0);   // 抬炮时炮口更高
        double sz = this.getZ() + rz * 1.15 + fz * 1.10;
        poop.setPos(sx, sy, sz);
        if (t != null) {
            double dx = t.getX() - sx, dy = t.getEyeY() - sy, dz = t.getZ() - sz;
            double len = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
            /* 预判: 按目标当前速度提前量, 并把散布 3.0 -> 0.6(命中率大幅提高) */
            double lead = Math.min(1.0, len / 24.0);
            dx += t.getDeltaMovement().x * lead * 12.0;
            dy += t.getDeltaMovement().y * lead * 6.0;
            dz += t.getDeltaMovement().z * lead * 12.0;
            double len2 = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
            poop.shoot(dx / len2, dy / len2 + 0.10, dz / len2, 1.35f, 0.6f);
        } else {
            poop.shoot(0, 0, 0, 0, 0);
        }
        sl.addFreshEntity(poop);
        sl.playSound(null, sx, sy, sz, SoundEvents.SNOWBALL_THROW, SoundSource.HOSTILE, 1.0f, 1.4f);
        this.muzzleFx(sl, sx, sy, sz, fx, fz);
    }

    /**
     * 炮口特效(2026-09-12 按用户要求: 发射散射炮弹时加一点炮口粒子):
     * 炮口一小团白烟 + 往前溅出去的答辩碎块 + 一缕上飘的篝火烟。
     * 只发粒子不改判定, 每发炮弹调一次即可。
     */
    private void muzzleFx(ServerLevel sl, double x, double y, double z, double fx, double fz) {
        net.farmingtales.ftboss.util.Fx.particles(sl, x, y, z, 4, ParticleTypes.CLOUD, 0.10, 0.03);
        net.farmingtales.ftboss.util.Fx.particles(sl, x + fx * 0.45, y + 0.05, z + fz * 0.45, 6,
                ParticleTypes.ITEM_SLIME, 0.16, 0.08);
        net.farmingtales.ftboss.util.Fx.particles(sl, x + fx * 0.25, y + 0.12, z + fz * 0.25, 2,
                ParticleTypes.CAMPFIRE_COSY_SMOKE, 0.06, 0.012);
    }

    /** 2026-09-12 按用户要求: 本体**不显示火焰燃烧动画**(伤害照旧) */
    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }


    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel sl) BossMusic.release(sl, this);
        /* 2026-09-12 v58 按用户反馈「召唤物死不干净」: 屎团/苍蝇 随主消散(它们本来有寿命, 这里兜底)
           ★ v112 修复两件事:
             ① 补上**自己召的**棕黑史莱姆与羊屎蛋(两种都会死亡分裂, 以前大怪死了小怪还在);
             ② 蛆**不能按类型清** —— 故障机器人也用同一个 MaggotMinionEntity, 按类型会把它的蛆一起清掉。
                改成"认主人"清(clearOwned), 所以下面这些召唤物在生成时都打了带主人 UUID 的标记。 */
        net.farmingtales.ftboss.boss.ai.SummonSweep.clear(this,
                net.farmingtales.ftboss.boss.ai.SummonSweep.RANGE,
                PoopMinionEntity.class, FlyMinionEntity.class,
                net.farmingtales.ftboss.entity.CompostSlimeEntity.class,
                net.farmingtales.ftboss.entity.SheepDungEntity.class);
        net.farmingtales.ftboss.boss.ai.SummonSweep.clearOwned(this,
                net.farmingtales.ftboss.boss.ai.SummonSweep.RANGE);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide) BossBars.untrack(this, this.bossBar);
        super.remove(reason);
    }

    /* ---------------- GeckoLib ---------------- */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "compost_ctrl", 4, this::predicate));
    }

    private PlayState predicate(AnimationState<CompostBossEntity> state) {
        this.actionState.prepare(state);
        this.signatureCast.prepare(state);
        if (this.isDeadOrDying()) { state.setAnimation(DEATH); return PlayState.CONTINUE; }
        if (this.signatureCast.active()) {
            state.setAnimation("signature_self_cast".equals(this.signatureCast.name()) ? SIGNATURE_SELF : SIGNATURE_CAST);
            return PlayState.CONTINUE;
        }
        if (this.actionState.active()) {
            switch (this.actionState.name()) {
                case "cannon_fire" -> { state.setAnimation(FIRE); return PlayState.CONTINUE; }
                case "gas_charge" -> { state.setAnimation(CHARGE); return PlayState.CONTINUE; }
                case "phase_shift" -> { state.setAnimation(SHIFT); return PlayState.CONTINUE; }
                case "transform" -> { state.setAnimation(TRANSFORM); return PlayState.CONTINUE; }
                default -> { state.setAnimation(HURT); return PlayState.CONTINUE; }
            }
        }
        /* 2026-09-12: 二阶段飞行用 float_move; 快速转向用 turn_l/turn_r(照机器人做法) */
        boolean flying = this.getPhase() >= 2;
        float dYaw = net.minecraft.util.Mth.wrapDegrees(this.getYRot() - this.yRotO);
        if (Math.abs(dYaw) > 1.2f) { state.setAnimation(dYaw > 0 ? TURN_L : TURN_R); return PlayState.CONTINUE; }
        state.setAnimation(flying ? FLOAT_MOVE : (state.isMoving() ? WALK : IDLE));
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
