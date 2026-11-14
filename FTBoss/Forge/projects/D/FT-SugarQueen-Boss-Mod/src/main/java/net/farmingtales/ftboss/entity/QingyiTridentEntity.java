package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.QingyiCombatRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * 【青衣的三叉戟】(2026-09-12 v29 用户定案)。
 *
 * <p>为什么要自定义: 原版 {@code ThrownTrident} 没法拦"命中玩家"这个瞬间, 而用户要求
 * **风暴追寻者命中必定召唤闪电** + **连续命中 5 次给玩家 4 个随机减益** —— 所以做一个子类,
 * 在 {@link #onHitEntity} 里回调施法者({@link EasterBossEntity#onTridentHit})。
 *
 * <p>渲染直接借用原版 {@code ThrownTridentRenderer}(子类实例可以喂给它的 render)。
 */
public class QingyiTridentEntity extends ThrownTrident {

    /** 当前用的是第几件 TnS 独特武器(0 风暴追寻者 / 1 亵渎者 / 2 深渊回响 / 3 恒星) */
    private int weaponIndex = 0;
    /** 2026-09-12 v43 按用户要求: 像故障机器人的充能球一样 —— 定期召唤闪电 + 给本体上增益
     *  2026-09-12 v60 曾把间隔从 3 秒缩到 1.5 秒("充能更快");
     *  2026-09-12 v67 按用户反馈「青衣的闪电打击有点太多了」: **间隔回到 4 秒**, 并加全局冷却 ——
     *  以前是**每把三叉戟各自脉冲**(三叉戟雨一次 16 把), 一秒能劈几十道雷+刷几十条增益日志(实测一局 1345 行)。 */
    /** 2026-09-13 v82 按用户要求「青衣会使用伤害更高、施法频率更高的闪电」:
     *  脉冲间隔 4 秒 -> **3 秒**; 同一主人的闪电全局冷却 6 秒 -> **3.5 秒**。 */
    public static final int ORB_PULSE_TICKS = 3 * 20;
    /** 同一主人(青衣)的**闪电全局冷却**: 场上不管有几把三叉戟, 最多每 3.5 秒劈一道 */
    public static final int STRIKE_CD = 70;
    /** 同一主人的**增益全局冷却**: 每 3 秒最多上一次(避免一秒几十条) */
    public static final int BUFF_CD = 3 * 20;
    /** 主人 UUID -> 上次劈雷 / 上次上增益的游戏刻 */
    private static final java.util.Map<java.util.UUID, Long> LAST_STRIKE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<java.util.UUID, Long> LAST_BUFF = new java.util.concurrent.ConcurrentHashMap<>();
    public static final double ORB_RANGE = 24.0;
    private int pulseTicks = 0;

    public QingyiTridentEntity(EntityType<? extends QingyiTridentEntity> type, Level level) {
        super(type, level);
    }

    /**
     * 生成用构造: 直接走原版 {@code ThrownTrident(Level, LivingEntity, ItemStack)}
     * (它会一并设好主人与携带的物品)。注意这会把实体类型定为原版的 {@code minecraft:trident} ——
     * 这正是我们想要的: **服务端**是子类(命中回调生效), **客户端**用原版三叉戟渲染, 无需新实体类型与渲染器。
     */
    @SuppressWarnings("unchecked")
    public QingyiTridentEntity(Level level, LivingEntity shooter, ItemStack stack, int weaponIndex) {
        super(level, shooter, stack);
        this.weaponIndex = weaponIndex;
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
    }

    public int getWeaponIndex() { return this.weaponIndex; }

    /**
     * 2026-09-12 v43 按用户要求: 三叉戟**像故障机器人的充能球那样**持续生效 ——
     * 每 {@value #ORB_PULSE_TICKS} tick: ①在最靠近的玩家头顶召唤闪电(真实闪电 + 少量魔法伤害);
     * ②给本体(青衣)上一个随机增益。
     */
    @Override
    public void tick() {
        if (!this.level().isClientSide && (this.tickCount >= QingyiCombatRules.TRIDENT_LIFETIME
                || !(this.getOwner() instanceof EasterBossEntity boss) || !boss.isAlive() || boss.isRemoved()
                || boss.isNoAi() || !boss.allowsParallelMelee()
                || net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(boss)
                || this.distanceToSqr(boss) > QingyiCombatRules.TRIDENT_OWNER_RANGE * QingyiCombatRules.TRIDENT_OWNER_RANGE)) {
            this.discard();
            return;
        }
        super.tick();
        if (this.level().isClientSide) return;
        if (++this.pulseTicks < ORB_PULSE_TICKS) return;
        this.pulseTicks = 0;
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel sl)) return;
        if (!(this.getOwner() instanceof LivingEntity owner) || !owner.isAlive()) return;
        if (owner instanceof EasterBossEntity boss && (boss.isNoAi() || boss.isMini()
                || net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(boss))) return;
        long now = sl.getGameTime();
        java.util.UUID id = owner.getUUID();

        /* ① 闪电 —— 同一主人**全局** 6 秒最多一道(以前每把三叉戟各自劈, 16 把就是"雷暴") */
        if (ready(LAST_STRIKE, id, now, STRIKE_CD)) {
            LivingEntity target = net.farmingtales.ftboss.boss.ai.BossAggro.highest(owner);
            double bd = ORB_RANGE * ORB_RANGE;
            if (target != null && target.distanceToSqr(this) > bd) target = null;
            if (target == null) {
                target = net.farmingtales.ftboss.boss.ai.BossTargets.nearest(sl, this, ORB_RANGE);
            }
            if (target != null) {
                try {
                    var bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(sl);
                    if (bolt != null) {
                        bolt.moveTo(target.getX(), target.getY(), target.getZ());
                        bolt.setVisualOnly(false);
                        sl.addFreshEntity(bolt);
                    }
                    /* v82 按用户要求"伤害更高": 5.33 -> **16**(约 3 倍); 数字已含全局修正
                       v95: 再加青衣的「三叉戟层数」加成(每召唤一把全部伤害 +1), 落雷也跟着涨
                       v102: 随青衣攻防强化再 ×1.40 */
                    float boltDmg = 36.0F;
                    if (this.getOwner() instanceof EasterBossEntity qb) {
                        boltDmg = qb.manorSkillDamage(target, boltDmg + qb.qingyiDamageBonus(), 1.5);
                    }
                    target.invulnerableTime = 0;
                    target.hurt(this.damageSources().indirectMagic(this, this.getOwner()), boltDmg);
                    LAST_STRIKE.put(id, now);
                    FTBossMod.LOGGER.info("[FTBoss][Easter] 青衣三叉戟(充能球式) 落雷: 目标={}",
                            target.getName().getString());
                } catch (Throwable t) {
                    FTBossMod.LOGGER.warn("[FTBoss][Easter] 三叉戟召唤闪电失败: {}", t.toString());
                }
            }
        }

        /* ② 给本体随机增益 —— 同一主人每 3 秒最多一次, 且**每个脉冲只记一行日志**(以前每个增益一行) */
        if (owner.isAlive() && ready(LAST_BUFF, id, now, BUFF_CD)) {
            var pool = new net.minecraft.world.effect.MobEffect[]{
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
                    net.minecraft.world.effect.MobEffects.DAMAGE_BOOST,
                    net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
                    net.minecraft.world.effect.MobEffects.REGENERATION,
                    net.minecraft.world.effect.MobEffects.ABSORPTION,
                    net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE,
            };
            var eff = pool[this.random.nextInt(pool.length)];
            owner.addEffect(new net.minecraft.world.effect.MobEffectInstance(eff, 10 * 20, 1, true, false, false));
            LAST_BUFF.put(id, now);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 青衣三叉戟(充能球式)给本体上增益: {}",
                    eff.getDisplayName().getString());
        }
    }

    public static boolean spawnOwned(ServerLevel level, EasterBossEntity owner, QingyiTridentEntity trident) {
        var existing = level.getEntitiesOfClass(QingyiTridentEntity.class,
                owner.getBoundingBox().inflate(QingyiCombatRules.TRIDENT_OWNER_RANGE * 2),
                projectile -> projectile.getOwner() == owner);
        existing.sort(java.util.Comparator.comparingInt((QingyiTridentEntity projectile) -> projectile.tickCount).reversed());
        int removeCount = existing.size() - QingyiCombatRules.MAX_TRIDENTS + 1;
        for (int index = 0; index < removeCount; index++) existing.get(index).discard();
        return level.addFreshEntity(trident);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    /** 冷却判断: 距离上次至少 cd tick 才放行(顺便清理过期条目, 避免 Map 泄漏) */
    private static boolean ready(java.util.Map<java.util.UUID, Long> map,
                                java.util.UUID id, long now, int cd) {
        Long last = map.get(id);
        if (last != null && now - last < cd) return false;
        if (map.size() > 32) map.entrySet().removeIf((en) -> now - en.getValue() > 20L * 600L);
        return true;
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (this.level().isClientSide) return;
        Entity e = hit.getEntity();
        if (!(e instanceof Player p) || !p.isAlive() || p.isCreative() || p.isSpectator()) return;
        /* 2026-09-12 v47 按用户要求: 三叉戟也学"长枪的连续击飞" —— 命中即把玩家挑起来 */
        net.farmingtales.ftboss.util.BossKnockback.push(p, 0.0, 1.05, 0.0);
        p.hurtMarked = true;
        if (!(this.getOwner() instanceof EasterBossEntity boss)) return;
        try {
            boss.onTridentHit(p, this.weaponIndex);
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 三叉戟命中回调异常: {}", t.toString());
        }
    }
}
