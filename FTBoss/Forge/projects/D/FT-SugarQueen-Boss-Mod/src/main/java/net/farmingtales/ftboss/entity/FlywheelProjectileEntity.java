package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.util.FlywheelCombatRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * **飞轮回旋镖**(2026-09-12 v22 起, v24 改成"回旋后环绕")。
 *
 * <p>文玄蓝(彩蛋 Boss #6)扔出的 Create 飞轮, 三段状态机:
 * <ol>
 *   <li><b>追踪</b>(前 {@value #HOMING_TICKS} tick): 无重力, 每 tick 把速度朝**最近的玩家**修正;</li>
 *   <li><b>返航</b>: 追回主人身上;</li>
 *   <li><b>环绕</b>(2026-09-12 v24 按用户要求"会越打越多并环绕在身边"):
 *       贴近主人后**不再消失**, 改成绕着主人转圈(半径/角速度/高度各自随机 → 看起来像一圈飞轮护体),
 *       返航和环绕时不造成接触伤害, 必须回到主人身边才能再次出击; 主人死了/消失才消散。总数量上限由本体控制在 {@code MAX_FLYWHEELS}=15。</li>
 * </ol>
 *
 * <p>防秒杀: 同一个玩家每 {@value #HIT_COOLDOWN} tick 最多被飞轮打一次(15 个飞轮也不会瞬间 180 伤害)。
 * 外观复用 {@code create:flywheel} 物品 + 原版 {@code ThrownItemRenderer}(零新增模型/贴图)。
 */
public class FlywheelProjectileEntity extends ThrowableItemProjectile {

    /** 命中伤害(2026-09-12 v41: 一个飞轮 **16 点**) */
    public static final float DAMAGE = 10.67f;      // 16 → ×2/3(已含全局修正, v77)
    /** 2026-09-12 v43 按用户要求: 飞轮**无视玩家 50% 护甲** —— 一半走物理(吃护甲), 一半走魔法(穿甲) */
    public static final float ARMOR_PIERCE_RATIO = 0.5f;
    /** 2026-09-12 v43 按用户要求: 玩家血量超过这个值 -> **额外 25% 伤害** */
    public static final float HEALTH_BONUS_THRESHOLD = 100.0f;
    public static final float HEALTH_BONUS_MULT = 1.25f;
    /* 2026-09-12 v63 按用户要求: 飞轮额外附带的三种"一点"伤害(每次命中各结算一下, 都要先清无敌帧) */
    /** 额外魔法伤害 */
    public static final float EXTRA_MAGIC = 1.33f;  // 2 → ×2/3(v77; 这一下带攻击者, 会吃全局修正)
    /** 额外虚空伤害(虚空伤害类型自带穿甲) */
    public static final float EXTRA_VOID = 1.0f;
    /** 额外火焰伤害(会顺手点着 2 秒) */
    public static final float EXTRA_FIRE = 1.0f;
    /** 追踪玩家的时长 */
    public static final int HOMING_TICKS = 40;
    /** 追踪范围(格) —— 2026-09-12 v41 按用户要求: 文玄蓝远程距离提高到 **24 格** */
    public static final double HOMING_RANGE = FlywheelCombatRules.TARGET_RANGE;
    /** 环绕阶段兜底寿命(3 分钟, 防止主人不见后泄漏) */
    public static final int ORBIT_MAX_LIFE = 20 * 60 * 3;
    /**
     * 同一玩家被飞轮命中的最小间隔(tick)。
     * 2026-09-12 v47 按用户要求: **离玩家越近攻击越快** ——
     * 贴着玩家(≤1 格) => {@value #HIT_CD_NEAR} tick(约每秒 10 次),
     * 6 格开外     => {@value #HIT_CD_FAR} tick(约每秒 3.3 次);
     * 叠满 15 个飞轮时再 −1 tick(最低仍为 2)。
     */
    public static final int HIT_CD_NEAR = FlywheelCombatRules.HIT_CD_NEAR;
    public static final int HIT_CD_FAR = FlywheelCombatRules.HIT_CD_FAR;
    public static final double NEAR_DIST = FlywheelCombatRules.NEAR_DIST;
    public static final double FAR_DIST = FlywheelCombatRules.FAR_DIST;
    /** (仅用于日志文案) 常态 / 叠满时的参考间隔 */
    public static final int HIT_COOLDOWN = 5;
    public static final int HIT_COOLDOWN_MAXED = 2;
    /** 本体同时最多维持多少个飞轮(用户定案 15) */
    public static final int MAX_FLYWHEELS = 15;

    /** 玩家 UUID -> 上次被飞轮命中的游戏刻(防秒杀) */
    private static final Map<Level, Map<UUID, Long>> LAST_HIT = new WeakHashMap<>();

    /** 环绕参数(进入环绕时随机, 让一圈飞轮错落有致) */
    private double orbitAngle = 0.0;
    private double orbitRadius = 1.9;
    private double orbitSpeed = 0.06;
    private double orbitHeight = 1.0;
    private boolean orbiting = false;
    /** 状态机自己的计时(不占用 tickCount, 因为齐射时要重置) */
    private int runTicks = 0;
    private int orbitTicks = 0;
    private static final double RETURN_SPEED = 0.62 / (1.0 - 0.55);

    public FlywheelProjectileEntity(EntityType<? extends FlywheelProjectileEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public FlywheelProjectileEntity(Level level, LivingEntity shooter) {
        super(FTEntities.FLYWHEEL.get(), shooter, level);
        this.setNoGravity(true);
        this.setItem(new ItemStack(this.getDefaultItem()));
        this.orbitAngle = this.random.nextDouble() * Math.PI * 2.0;
        this.orbitRadius = 1.7 + this.random.nextDouble() * 1.1;
        this.orbitSpeed = 0.045 + this.random.nextDouble() * 0.045;
        this.orbitHeight = 0.6 + this.random.nextDouble() * 1.0;
    }

    /** 2026-09-12 v35 按用户要求: 用**专属模型+贴图**的 ftboss:wenxuanlan_flywheel; 取不到再退回 create:flywheel */
    @Override
    protected Item getDefaultItem() {
        try {
            Item own = ForgeRegistries.ITEMS.getValue(new ResourceLocation("ftboss", "wenxuanlan_flywheel"));
            if (own != null && own != Items.AIR) return own;
            Item flywheel = ForgeRegistries.ITEMS.getValue(new ResourceLocation("create", "flywheel"));
            if (flywheel != null && flywheel != Items.AIR) return flywheel;
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 取飞轮物品失败: {}", t.toString());
        }
        return Items.IRON_INGOT;
    }

    /** 已经贴上主人并开始转圈了 */
    public boolean isOrbiting() { return this.orbiting; }

    public void beginOrbit(double angle) {
        Entity owner = this.getOwner();
        if (owner == null) return;
        this.orbiting = true;
        this.orbitAngle = angle;
        this.orbitTicks = 0;
        this.runTicks = 0;
        this.setPos(owner.getX() + Math.cos(angle) * this.orbitRadius,
                owner.getY() + this.orbitHeight, owner.getZ() + Math.sin(angle) * this.orbitRadius);
        this.setDeltaMovement(Vec3.ZERO);
    }

    /**
     * 2026-09-12 v41 按用户要求: **齐射** —— 本体等一会儿会让所有环绕中的飞轮同时再扑一次。
     * respawn 一次状态机(重新飞向最近玩家), HOMING_TICKS 之后自然回到环绕。
     */
    public void launchAgain() {
        if (!this.orbiting) return;
        this.orbiting = false;
        this.runTicks = 0;
        this.orbitTicks = 0;
    }

    private boolean isAttackReady() {
        return FlywheelCombatRules.canAttack(this.orbiting, this.runTicks, HOMING_TICKS);
    }

    private void startReturning() {
        this.runTicks = Math.max(this.runTicks, HOMING_TICKS + 1);
        this.orbitTicks = 0;
        Entity owner = this.getOwner();
        if (owner == null || !owner.isAlive() || owner.level() != this.level()) return;
        Vec3 direction = owner.position().add(0.0, owner.getBbHeight() * 0.55, 0.0).subtract(this.position());
        this.setDeltaMovement(direction.normalize().scale(RETURN_SPEED));
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            this.runTicks++;
            this.steer();
            if (this.isRemoved()) return;
        }
        super.tick();
    }

    /** 追踪 -> 返航 -> 环绕 */
    private void steer() {
        Entity owner = this.getOwner();
        /* 主人没了就消散 */
        if (owner == null || !owner.isAlive() || owner.level() != this.level()) {
            if (owner != null || this.runTicks > 40) this.poof();
            return;
        }
        if (owner instanceof EasterBossEntity skyBoss && skyBoss.isXuanlanSkyCasting()) {
            this.runTicks--;
            this.orbiting = true;
            this.setInvisible(true);
            this.setPos(owner.getX(), owner.getY() + this.orbitHeight, owner.getZ());
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }
        this.setInvisible(false);
        if (this.orbiting) {
            if (this.runTicks > ORBIT_MAX_LIFE) { this.poof(); return; }
            this.orbitTicks++;
            Player target = this.nearestPlayer();
            boolean maxed = owner instanceof EasterBossEntity boss && boss.isFlywheelMaxed();
            boolean ownerReady = !(owner instanceof EasterBossEntity boss) || !boss.isNoAi() && !boss.xuanlanBeamBusy();
            if (target != null && FlywheelCombatRules.shouldRelaunch(
                    this.orbitTicks, owner.distanceTo(target), maxed, ownerReady)) {
                this.launchAgain();
            } else {
                this.orbitStep(owner);
                return;
            }
        }
        if (this.runTicks > ORBIT_MAX_LIFE) { this.poof(); return; }

        Vec3 aim = null;
        if (this.runTicks <= HOMING_TICKS) {
            LivingEntity t = this.nearestPlayer();
            if (t != null) aim = t.position().add(0.0, t.getBbHeight() * 0.55, 0.0);
        } else if (owner != null) {
            if (this.distanceToSqr(owner) < 4.0) {          // 贴上主人 -> 进入环绕(不再消失)
                this.orbiting = true;
                this.orbitTicks = 0;
                this.orbitAngle = Math.atan2(this.getZ() - owner.getZ(), this.getX() - owner.getX());
                this.orbitStep(owner);
                return;
            }
            aim = owner.position().add(0.0, owner.getBbHeight() * 0.55, 0.0);
        }
        if (aim == null) return;
        Vec3 dir = aim.subtract(this.position());
        if (dir.lengthSqr() < 1.0E-4) return;
        dir = dir.normalize();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.55).add(dir.scale(0.62)));
        this.setYRot((float) Math.toDegrees(Math.atan2(dir.x, dir.z)));
        this.yRotO = this.getYRot();
    }

    /** 绕主人转圈: 用速度追一个在圆环上移动的目标点(交给 super.tick 处理碰撞/命中) */
    private void orbitStep(Entity owner) {
        if (owner == null) return;
        this.orbitAngle += this.orbitSpeed;
        double tx = owner.getX() + Math.cos(this.orbitAngle) * this.orbitRadius;
        double tz = owner.getZ() + Math.sin(this.orbitAngle) * this.orbitRadius;
        double ty = owner.getY() + this.orbitHeight;
        Vec3 to = new Vec3(tx - this.getX(), ty - this.getY(), tz - this.getZ());
        this.setDeltaMovement(to.scale(0.45));
        this.setYRot((float) Math.toDegrees(Math.atan2(to.x, to.z)));
        this.yRotO = this.getYRot();
    }

    private Player nearestPlayer() {
        Entity owner = this.getOwner();
        if (owner == null || !owner.isAlive() || owner.level() != this.level()) return null;
        if (owner instanceof EasterBossEntity boss && boss.getTarget() instanceof Player target
                && BossTargets.valid(target) && target.level() == this.level()
                && target.distanceToSqr(owner) <= HOMING_RANGE * HOMING_RANGE) {
            return target;
        }
        Player nearest = null;
        double nearestDistanceSquared = HOMING_RANGE * HOMING_RANGE;
        for (Player player : this.level().players()) {
            if (!BossTargets.valid(player)) continue;
            double distanceSquared = player.distanceToSqr(owner);
            if (distanceSquared <= nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearest = player;
            }
        }
        return nearest;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!this.isAttackReady()) return false;
        if (this.getOwner() instanceof EasterBossEntity boss && boss.isXuanlanSkyCasting()) return false;
        return entity != this.getOwner() && entity instanceof Player player
                && BossTargets.valid(player) && super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (!this.isAttackReady()) return;
        if (this.getOwner() instanceof EasterBossEntity boss && boss.isXuanlanSkyCasting()) return;
        super.onHitEntity(hit);
        if (this.level().isClientSide) return;
        Entity e = hit.getEntity();
        if (e == this.getOwner()) return;
        /* 2026-09-12 v48 按用户反馈"柚子分身能召唤分身 / 一个 Boss 没死另一个就进场":
           飞轮以前打**所有生物**(连自己这边的 Boss 也打) —— 于是柚子被友军飞轮每 tick 打中,
           "自我叠影"计数狂涨刷出一堆分身, 日志被刷了 10007 行。现在**只打玩家**。 */
        if (!(e instanceof Player le)) return;
        /* v114 按用户反馈: 创造/旁观玩家不打(打也打不动, 还会白白消耗掉这颗飞轮) —— 直接忽略, 继续飞 */
        if (!net.farmingtales.ftboss.boss.ai.BossTargets.valid(le)) return;
        this.startReturning();
        /* 防秒杀: 同一玩家每 HIT_COOLDOWN tick 只吃一次 */
        if (this.level() instanceof ServerLevel sl) {
            long now = sl.getGameTime();
            Entity owner = this.getOwner();
            if (owner == null || !owner.isAlive() || owner.level() != sl) return;
            boolean maxed = owner instanceof EasterBossEntity boss && boss.isFlywheelMaxed();
            int cooldown = FlywheelCombatRules.hitCooldown(owner.distanceTo(le), maxed);
            if (owner instanceof EasterBossEntity boss) cooldown = boss.getChallengeDifficulty().acceleratedTicks(cooldown);
            Map<UUID, Long> levelHits = LAST_HIT.computeIfAbsent(sl, unused -> new HashMap<>());
            Long lastHit = levelHits.get(le.getUUID());
            if (!FlywheelCombatRules.hitReady(now, lastHit, cooldown)) return;
            levelHits.put(le.getUUID(), now);
            if (levelHits.size() > 64) levelHits.entrySet().removeIf(entry -> now - entry.getValue() > 400L);
        }
        /* 2026-09-12 v43 按用户要求:
           ① **无视玩家 50% 护甲** —— 伤害对半分: 物理那半吃护甲, 魔法那半直接穿甲;
           ② 玩家**血量 > 100** 时总额外 +25%。 */
        float base = DAMAGE;
        boolean bonus = le.getHealth() > HEALTH_BONUS_THRESHOLD;
        if (bonus) base *= HEALTH_BONUS_MULT;
        /* 2026-09-13 v71 按用户要求: 主人(文玄蓝)每次"光束连招"后飞轮攻击力 +10%, 这里取用 */
        if (this.getOwner() instanceof EasterBossEntity fwOwner) {
            double m = fwOwner.flywheelDamageMul();
            if (m > 1.0) base *= (float) m;
        }
        float armored = base * (1.0f - ARMOR_PIERCE_RATIO);
        float pierce = base * ARMOR_PIERCE_RATIO;
        le.invulnerableTime = 0;
        le.hurt(this.damageSources().thrown(this, this.getOwner()), armored);
        le.invulnerableTime = 0;
        le.hurt(this.damageSources().indirectMagic(this, this.getOwner()), pierce);
        /* 2026-09-12 v63 按用户要求: 飞轮再附带**一点魔法 / 一点虚空 / 一点火焰**伤害
           (都先清无敌帧, 保证三下都实打实; 虚空伤害自带穿甲, 火焰伤害另附 2 秒点燃做视觉反馈) */
        le.invulnerableTime = 0;
        le.hurt(this.damageSources().indirectMagic(this, this.getOwner()), EXTRA_MAGIC);
        le.invulnerableTime = 0;
        net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage.hurtSkill(
                le, this.getOwner(), this.damageSources().fellOutOfWorld(), EXTRA_VOID);
        le.invulnerableTime = 0;
        net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage.hurtSkill(
                le, this.getOwner(), this.damageSources().onFire(), EXTRA_FIRE);
        if (EXTRA_FIRE > 0.0f) le.setSecondsOnFire(2);
        /* 2026-09-12 v41: 只上缓慢 + 虚弱, **不造成任何击退** */
        le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5 * 20, 1));
        le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 5 * 20, 1));
    }

    /** 撞到东西: **一律不消耗**, 只继续飞/继续绕(2026-09-12 v41 起不再放粒子) */
    @Override
    protected void onHit(HitResult result) {
        if (this.level().isClientSide) return;
        if (result.getType() == HitResult.Type.ENTITY) this.onHitEntity((EntityHitResult) result);
    }

    /** 消散(2026-09-12 v41 起不再放粒子) */
    private void poof() {
        this.discard();
    }
}
