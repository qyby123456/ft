package net.farmingtales.ftboss.boss.ai;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.ChargedOrbEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.OrbType;
import net.farmingtales.ftboss.integ.WizardSummon;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.List;

/**
 * 故障机器人的「充能球」控制器(2026-09-11)。
 *
 * <p><b>结构照搬沙耶的 {@link SayaBuds}「茶芽庇佑」</b>: Boss 持有一个控制器实例, 控制器负责
 * 计数存活召唤物 / 定期补充 / 向附近玩家广播 / 在需要时清理。区别是这里的召唤物是**充能球**, 而且:
 *
 * <p><b>激发流程 = 吸收 → Boss 释放对应技能</b>(按用户要求):
 * <ol>
 *   <li>球环绕 Boss 悬浮, 可被玩家打掉(打掉 = 不激发, 这是玩家的反制窗口);</li>
 *   <li>球到期, 或 Boss 的技能冷却好了主动<b>吸收</b>最老的一颗 → 球化作一道光被吸进核心;</li>
 *   <li>Boss 随后<b>释放该球对应的技能</b>(从 Boss 位置结算, 带施法动画/音效/粒子)。</li>
 * </ol>
 *
 * <p>五种球的技能: 玻璃球【AOE】· 电球【单体】· 黑球【蓄力, 存活越久越强】· 冰球【保护】· 离子球【强化】。
 */
public final class DefectOrbs {

    /** 初始球槽(2026-09-11 按用户要求"同时存在的球可以多一点": 3 → 5) */
    public static final int SLOTS_BASE = 5;
    /** 每次阶段突破 +3 槽 */
    public static final int SLOTS_PER_PHASE = 3;
    /** 槽位上限 */
    public static final int SLOTS_MAX = 11;
    /** **每 2 秒一个周期**: 补齐全部球槽, 且"如果原本就有球 -> 激活最老的一颗"(2026-09-11 按用户要求) */
    private static final int CYCLE = 48;   // 2026-09-12: orb cycle 40 -> 48 (slightly slower)
    /** 【回想形态】下周期放慢(施法变慢), 但每次双发 */
    private static final int ECHO_CYCLE = 80;   // 2026-09-12: echo cycle 70 -> 80
    /** 被拆球后短暂不补同类球的惩罚间隔 */
    private static final int PUNISH_CD = 40;

    // ===== 五种球对应的铁魔法法术池(字段名已用 javap 核实存在于 SpellRegistry) =====
    /** 玻璃球【AOE】: 火球/灼烧/烈焰风暴/岩浆弹/星落/火焰弹幕/火焰吐息 + 烈焰打击/毒液飞溅/酸液球/根须/热浪 */
    private static final String[] GLASS_SPELLS = {
            "FIREBALL_SPELL", "SCORCH_SPELL", "BLAZE_STORM_SPELL", "MAGMA_BOMB_SPELL",
            "STARFALL_SPELL", "FLAMING_BARRAGE_SPELL", "FIRE_BREATH_SPELL",
            "FLAMING_STRIKE_SPELL", "POISON_SPLASH_SPELL", "ACID_ORB_SPELL", "ROOT_SPELL", "HEAT_SURGE_SPELL"
    };
    /** 电球【单体】: 雷击/闪电矛/连锁闪电/电击/球状闪电/伏特打击/雷暴 */
    private static final String[] LIGHTNING_SPELLS = {
            "LIGHTNING_BOLT_SPELL", "LIGHTNING_LANCE_SPELL", "CHAIN_LIGHTNING_SPELL", "ELECTROCUTE_SPELL",
            "BALL_LIGHTNING_SPELL", "VOLT_STRIKE_SPELL", "THUNDERSTORM_SPELL"
    };
    /** 冰球【保护】: 冰墙护体/霜波/冰墓/霜咬/寒霜射线/寒冰锥/暴雪/冰刺 + 雪球/霜步 */
    private static final String[] FROST_SPELLS = {
            "ICE_BLOCK_SPELL", "FROSTWAVE_SPELL", "ICE_TOMB_SPELL", "FROSTBITE_SPELL", "RAY_OF_FROST_SPELL",
            "CONE_OF_COLD_SPELL", "BLIZZARD_SPELL", "ICE_SPIKES_SPELL", "SNOWBALL_SPELL", "FROST_STEP_SPELL"
    };
    /** 离子球【强化】: 急速/橡木肤/生命祝福/堡垒/充能/护盾/强效治疗/飞升/闪避/天使之翼 + 治疗/治疗法阵 */
    private static final String[] ION_SPELLS = {
            "HASTE_SPELL", "OAKSKIN_SPELL", "BLESSING_OF_LIFE_SPELL", "FORTIFY_SPELL", "CHARGE_SPELL",
            "SHIELD_SPELL", "GREATER_HEAL_SPELL", "ASCENSION_SPELL", "EVASION_SPELL", "ANGEL_WINGS_SPELL",
            "HEAL_SPELL", "HEALING_CIRCLE_SPELL"
    };
    /** 黑球【蓄力·高等级高稀有度】: 黑洞/凋零之首/心脏骤停/深渊披风/吞噬/亡者复苏/幽匿触须/诡异冲击 + 召唤复仇者/召唤北极熊 */
    private static final String[] DARK_SPELLS = {
            "BLACK_HOLE_SPELL", "WITHER_SKULL_SPELL", "HEARTSTOP_SPELL", "ABYSSAL_SHROUD_SPELL", "DEVOUR_SPELL",
            "RAISE_DEAD_SPELL", "SCULK_TENTACLES_SPELL", "ELDRITCH_BLAST_SPELL",
            "SUMMON_VEX_SPELL", "SUMMON_POLAR_BEAR_SPELL"
    };

    private final java.util.Map<OrbType, List<AbstractSpell>> spellCache = new java.util.EnumMap<>(OrbType.class);

    private static String[] fieldsFor(OrbType t) {
        return switch (t) {
            case GLASS -> GLASS_SPELLS;
            case LIGHTNING -> LIGHTNING_SPELLS;
            case FROST -> FROST_SPELLS;
            case ION -> ION_SPELLS;
            case DARK -> DARK_SPELLS;
        };
    }

    /** 取该球种可用的铁魔法(反射解析一次后缓存; 铁魔法缺失时返回空表 -> 自动回退到内置元素效果) */
    private List<AbstractSpell> spellsFor(OrbType t) {
        return this.spellCache.computeIfAbsent(t, k -> WizardSummon.spellPoolOf(fieldsFor(k)));
    }

    /**
     * 用铁魔法释放该球种的法术(2026-09-11 按用户要求: 球法术走铁魔法)。
     *
     * <p>施法等级: 玻璃/电/冰/离子 = **3 + 阶段**; 黑球 = **5 + 阶段 + 蓄力等级**(存活每 100t 多 1 级, 体现"蓄力"),
     * 且黑球优先挑 **史诗/传说** 稀有度的法术。
     * @return 是否成功释放(没成功就由调用方回退到内置元素效果)
     */
    private boolean castIronSpell(ServerLevel sl, OrbType type, int chargeTicks, ServerPlayer target) {
        List<AbstractSpell> pool = spellsFor(type);
        if (type == OrbType.DARK) return false;      // 黑球已改为撞击攻击, 彻底不走铁魔法
        if (pool.isEmpty()) {
            if (type == OrbType.DARK) {   // 诊断: 用户反馈"黑暗球的铁魔法未使用", 这里把真因写进日志
                FTBossMod.LOGGER.warn("[FTBoss][充能球] 黑球铁魔法池为空(法术名与铁魔法版本对不上?) -> 回退到内置蓄力伤害");
            }
            return false;
        }
        // 2026-09-11 修"充能球完全不放铁魔法"(日志实证: 铁魔法 0 次 / 内置回退 9 次):
        // 故障机器人没有实现 IMagicEntity, 原来的 instanceof 直接 return false => 所有球都走内置回退。
        // 现在: 有 IMagicEntity 就正常施法(带施法动画), 没有就用 castDirect 直放法术本体。
        io.redspace.ironsspellbooks.api.entity.IMagicEntity magic =
                (this.boss instanceof io.redspace.ironsspellbooks.api.entity.IMagicEntity m) ? m : null;

        int phase = this.boss.getPhase();
        int echoBonus = this.echoForm ? 2 : 0;          // 【回想形态】法术等级 +2
        int chargeBonus = Math.min(this.charge / 4, 4); // **充能**抬高施法等级(球给机器人充能的意义之一)
        AbstractSpell spell = null;
        if (type == OrbType.DARK || this.echoForm) {
            // 黑球 & 回想形态: 优先挑高稀有度(回想形态下更进一步优先传说)
            int want = 6 + phase + Math.min(chargeTicks / 60, 6) + echoBonus + chargeBonus;
            int minRarity = this.echoForm ? 4 : 3;      // 4=LEGENDARY, 3=EPIC(黑球从此档起挑)
            List<AbstractSpell> rare = new ArrayList<>();
            for (AbstractSpell s : pool) {
                if (WizardSummon.rarityOf(s, Math.min(want, s.getMaxLevel())) >= minRarity) rare.add(s);
            }
            if (rare.isEmpty() && this.echoForm) {      // 退一档: 至少史诗
                for (AbstractSpell s : pool) {
                    if (WizardSummon.rarityOf(s, Math.min(want, s.getMaxLevel())) >= 3) rare.add(s);
                }
            }
            // 2026-09-11 按用户要求"黑球只出 EPIC/LEGENDARY, 再收紧一次筛选":
            // 稀有度不够就**抬等级重挑**(最多 +8 级), 而不是回退到整池随机。
            if (rare.isEmpty()) {
                for (int extra = 2; extra <= 8 && rare.isEmpty(); extra += 2) {
                    for (AbstractSpell s : pool) {
                        if (WizardSummon.rarityOf(s, Math.min(want + extra, s.getMaxLevel())) >= minRarity) rare.add(s);
                    }
                }
            }
            List<AbstractSpell> pick = rare.isEmpty() ? pool : rare;
            // 黑球特别优待: 一半概率直接点名**【黑洞】**(用户点名的高级法术)
            if (type == OrbType.DARK && this.boss.getRandom().nextFloat() < 0.5f) {
                for (AbstractSpell s : pick) {
                    if ("irons_spellbooks:black_hole".equals(s.getSpellId())) { spell = s; break; }
                }
            }
            if (spell == null) spell = pick.get(this.boss.getRandom().nextInt(pick.size()));
        } else if (type == OrbType.LIGHTNING) {
            // 电球: **优先【雷击】/【雷暴】**(用户点名), 其余电系法术兜底
            List<AbstractSpell> pref = new ArrayList<>();
            for (AbstractSpell s : pool) {
                String id = s.getSpellId() == null ? "" : s.getSpellId();
                if (id.endsWith(":lightning_bolt") || id.endsWith(":thunderstorm")) pref.add(s);
            }
            List<AbstractSpell> pick = pref.isEmpty() ? pool : pref;
            spell = pick.get(this.boss.getRandom().nextInt(pick.size()));
        } else {
            spell = pool.get(this.boss.getRandom().nextInt(pool.size()));
        }
        // 2026-09-11 按用户要求"提高机器人的伤害": 基础等级 3+阶段 -> 4+阶段, 黑球蓄力上限 3 -> 5 级
        // 2026-09-11 按用户要求"加长蓄能时间和法术强度": 黑球等级 +2, 蓄力加成上限 5 → 6
        int lvl = Math.max(spell.getMinLevel(), Math.min(4 + phase + echoBonus + chargeBonus
                + (type == OrbType.DARK ? 2 + Math.min(chargeTicks / 80, 5) : 0), spell.getMaxLevel()));
        // 需要指定目标的法术才绑目标; 自身增益类法术不绑(绑了反而可能被忽略)
        net.minecraft.world.entity.LivingEntity castTarget = switch (type) {
            case FROST, ION -> this.boss;
            default -> target != null ? target : this.boss;
        };
        // 2026-09-11 日志实证修 bug: 出现过"目标=故障机器人"(法术打自己) -> 目标是自己时改打最近玩家
        if (castTarget == null || castTarget == this.boss) castTarget = nearest(sl, 48.0);
        boolean cast = magic != null ? WizardSummon.castAt(magic, castTarget, spell, lvl)
                : WizardSummon.castDirect(this.boss, spell, lvl, castTarget);
        if (!cast) return false;
        FtLog(type, spell.getSpellId(), lvl, chargeTicks);
        // 【回想形态】: 记下这次法术, 稍后**原样再放一次**(等级/目标都相同)
        if (this.echoForm && echoSpell == null) {
            this.echoSpell = spell;
            this.echoLevel = lvl;
            this.echoTarget = castTarget;
            this.echoTicks = ECHO_DELAY;
        }
        return true;
    }

    /** 回想形态的"第二发": 与第一发完全相同的法术 */
    private void fireEcho() {
        AbstractSpell spell = this.echoSpell;
        net.minecraft.world.entity.LivingEntity target = this.echoTarget;
        int lvl = this.echoLevel;
        this.echoSpell = null;
        if (spell == null || !(this.boss.level() instanceof ServerLevel sl)) return;
        if (!(this.boss instanceof io.redspace.ironsspellbooks.api.entity.IMagicEntity magic)) return;
        if (!WizardSummon.castAt(magic, target, spell, lvl)) return;
        sl.playSound(null, this.boss.getX(), this.boss.getY(), this.boss.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.4f, 0.6f);
        this.boss.playCastAnim(OrbType.DARK);   // 复读时也甩一次攻击动作
            broadcast("§5【回想】§7 同一法术再次施放: §d" + spell.getSpellId() + " §7lv" + lvl);
        FTBossMod.LOGGER.info("[FTBoss][回想形态] 复读法术 {} lv{} -> {}", spell.getSpellId(), lvl,
                target == null ? "-" : target.getName().getString());
    }

    // ===== 基础攻击也走铁魔法(2026-09-11 按用户要求"技能用铁魔法") =====
    /** 基础攻击法术池(低环法术, 冷却期间穿插) */
    private static final String[] BASIC_SPELLS = {
            "MAGIC_MISSILE_SPELL", "FIREBOLT_SPELL", "ICICLE_SPELL", "FROSTBITE_SPELL", "MAGIC_ARROW_SPELL",
            "POISON_ARROW_SPELL", "SNOWBALL_SPELL"
    };
    private List<AbstractSpell> basicPool;

    /**
     * 基础攻击: 随机放一个低环铁魔法(魔法飞弹/火焰弹/冰锥…), 从 Boss 位置射出。
     * @return 是否成功放出(没装铁魔法时返回 false, 由调用方回退到内置伤害)
     */
    public boolean castBasicSpell(net.minecraft.world.entity.LivingEntity target) {
        if (!(this.boss instanceof io.redspace.ironsspellbooks.api.entity.IMagicEntity magic)) return false;
        if (this.basicPool == null) this.basicPool = WizardSummon.spellPoolOf(BASIC_SPELLS);
        if (this.basicPool.isEmpty()) return false;
        AbstractSpell s = this.basicPool.get(this.boss.getRandom().nextInt(this.basicPool.size()));
        int lvl = Math.max(s.getMinLevel(), Math.min(3 + this.boss.getPhase() + (this.echoForm ? 2 : 0), s.getMaxLevel()));
        // 按法术属性挑一个攻击动作(冰/霜 -> 冰球, 火 -> 玻璃球, 其余 -> 电球)
        String sid = s.getSpellId() == null ? "" : s.getSpellId();
        OrbType anim = (sid.contains("ice") || sid.contains("frost")) ? OrbType.FROST
                : (sid.contains("fire") || sid.contains("flame")) ? OrbType.GLASS : OrbType.LIGHTNING;
        this.boss.playCastAnim(anim);
        this.pendingBasicSpell = s;
        this.pendingBasicLevel = lvl;
        this.pendingBasicTarget = target;
        this.pendingBasicTicks = releaseDelay(anim);
        FTBossMod.LOGGER.info("[FTBoss][故障机器人] 基础攻击法术 {} lv{} -> {}", s.getSpellId(), lvl, target.getName().getString());
        return true;
    }

    private void FtLog(OrbType type, String spellId, int lvl, int chargeTicks) {
        FTBossMod.LOGGER.info("[FTBoss][充能球] {} 激发 -> 铁魔法 {} lv{}{} (蓄力 {}t, 阶段 {})",
                type.id, spellId, lvl, this.echoForm ? " [回想形态]" : "", chargeTicks, this.boss.getPhase());
    }


    private final DefectCoreEntity boss;

    private int orbSlots = SLOTS_BASE;
    private int cycleCd = CYCLE;
    private int punishCd = 0;
    private int passiveCd = 20;
    /** 待生效的施法(动画推到"推出帧"再结算, 见 {@link #absorbAndCast}) */
    private OrbType pendingType = null;
    private int pendingTicks = 0;
    private int pendingCharge = 0;
    private record QueuedCast(OrbType type, int charge, java.util.UUID orbId) { }
    private final java.util.ArrayDeque<QueuedCast> castBacklog = new java.util.ArrayDeque<>();
    private java.util.UUID pendingOrbId;
    private AbstractSpell pendingBasicSpell;
    private int pendingBasicLevel;
    private int pendingBasicTicks;
    private net.minecraft.world.entity.LivingEntity pendingBasicTarget;

    // ===== 超能光束(2026-09-11 按用户要求新增技能) =====
    /** 冷却(出场先缓冲一段, 免得一见面就放) */
    private int beamCd = 240;
    /** >0 = 蓄力中(剩余 tick) */
    private int beamCharge = 0;
    /** >0 = 引导中(剩余 tick) */
    private int beamFire = 0;
    /** 伤害脉冲计时(每 {@value #BEAM_PULSE}t 结算一次 => 见 {@link #BEAM_DMG} 的口径) */
    private int beamPulse = BEAM_PULSE;
    /** 锁定后的光束方向(单位向量) —— **锁定后不再追踪, 玩家可以侧移躲开** */
    private double beamDx = 0, beamDy = 0, beamDz = -1;
    /** 蓄力时间(0.75s, 与 beam 动画的蓄力段对齐) */
    private static final int BEAM_CHARGE = 160;     // 2026-09-11: 蓄力 10 秒 **-20% = 8 秒**, 期间无法防御/移动/攻击
    /** 引导时间(3 秒) */
    private static final int BEAM_DURATION = 60;
    /** 伤害脉冲间隔(0.5s) */
    private static final int BEAM_PULSE = 10;
    /**
     * 每脉冲伤害。**口径统一写在这里(2026-09-14 v110 修正过期注释)**:
     * <ul>
     *   <li>设计值 **22 / 脉冲**, 10t 一次 → 设计 44/秒、3 秒 132;</li>
     *   <li>但 {@code beamDamage()} 把它**均分四份再各自 ×全局 0.5**(v77 烘焙) ——
     *       {@code bQuarter = BossScaling.dmg(boss, BEAM_DMG) / 8}, 连打四次(物理/魔法/虚空/真实),
     *       所以玩家**实际每脉冲吃 11 点**(22 × 0.5), 实伤 **22/秒**、3 秒约 **66**。</li>
     * </ul>
     * 之前这里和上面 {@code beamPulse} 的注释还写着"15/脉冲、30/秒", 是 v77 之前的旧数, 已删。
     */
    private static final float BEAM_DMG = 22f;    // 设计值 22(实伤 11, 见上)
    /** 光束长度 / 判定半径 */
    private static final double BEAM_LEN = 40.0, BEAM_RADIUS = 3.5;   // 2026-09-11 按用户要求: 光束更长更粗

    // ===== 【电流相生】Electric Current Intergrowth(2026-09-11 按用户要求新增, **二阶段**起可用) =====
    /* v110 注释修正: 代码判定一直是 {@code getPhase() >= 2}(见 tickCurrentIntergrowth 第一行,
       那里 2026-09-11 就注明"二阶段即可用"), 但本法里另外 5 处注释/日志还写着"三阶段", 已统一改回。 */
    /** 累计"用掉过的电球"数量 —— 这就是【电流相生】能召唤的电球数 */
    private int lightningUsed = 0;
    /** 技能冷却(二阶段起每 30 秒一次) */
    private int currentCd = 400;
    /** 技能施放次数(2026-09-11: 每 3 次有 5% 概率自我虚弱) */
    private int skillCount = 0;
    /** 上次"机能紊乱"的时间(内置 30s 冷却, 防抖动) */
    private long lastMisfire = -10000L;
    /** 【集中碎片】(2026-09-12): 只用一次, 之后全部球伤害 ×1.5 */
    private boolean fragmentsUsed = false;
    private float orbDamageMul = 1f;
    /** 蛆小怪每次召唤的基数(集中碎片后提升) */
    private int maggotBase = 2;
    /** 【！?区区？！】蛆小怪的召唤冷却(2026-09-11) */
    private int maggotCd = 200;
    /** 单次最多召唤多少颗(防止玩家刷出几十颗导致卡顿) */
    private static final int CURRENT_MAX_ORBS = 12;
    /** 技能后的虚弱时长(5 秒)与回血比例(10%) */
    private static final int CURRENT_EXHAUST = 100;
    private static final float CURRENT_REGEN = 0.10f;
    /** 核心充能(由玻璃球/电球提供, 攒满触发闪电打击, 并抬高施法等级) */
    private int charge = 0;
    /** 一次闪电打击需要的充能 */
    private static final int CHARGE_PER_STRIKE = 6;
    /** 充能上限 */
    private static final int CHARGE_MAX = 60;

    /**
     * 球的**被动能力**(2026-09-11 按用户要求改造: 球给机器人**充能**, 触发**闪电打击**; 去掉"力量")。
     * <ul>
     *   <li>玻璃球 / 电球 → 给核心**充能**(电球加倍); 充能攒满 -> 对最近玩家降下**闪电打击**;</li>
     *   <li>充能还会**抬高施法等级**(见 {@code castIronSpell}: +充能/4, 上限 +3);</li>
     *   <li>冰球 → 吸收 I/II(**无抗性**, 见下方 2026-09-11 二次削减);</li>
     *   <li>离子球 → 一批**进攻/功能**增益(速度/急迫/跳跃/幸运/饱食/海豚/水下呼吸/夜视…), <b>不再给吸收/生命提升/抗性</b>;</li>
     *   <li>黑球 → 无被动, 它的价值是蓄力等级与高稀有度法术;</li>
     *   <li><b>阶段越高全部球效果越强</b>: 充能获得量、被动等级、闪电伤害、球的存在时间全部按阶段放大。</li>
     * </ul>
     */
    private void tickPassiveBuffs(List<ChargedOrbEntity> mine) {
        if (!mine.isEmpty()) this.boss.showOrbCue("passive", OrbType.ION, -1, 8);
        int glass = 0, lightning = 0, frost = 0, ion = 0;
        for (ChargedOrbEntity o : mine) {
            switch (o.getOrbType()) {
                case GLASS -> glass++;
                case LIGHTNING -> lightning++;
                case FROST -> frost++;
                case ION -> ion++;
                case DARK -> { /* 黑球: 无被动, 只贡献蓄力 */ }
            }
        }
        int phase = this.boss.getPhase();

        // ① 充能: 玻璃 +1 / 电球 +2, 再乘阶段
        int gain = (glass + lightning * 2) * phase;
        if (gain > 0) {
            this.charge = Math.min(CHARGE_MAX, this.charge + gain);
        }
        // ② 充能满了 -> 闪电打击
        if (this.charge >= CHARGE_PER_STRIKE && this.boss.level() instanceof ServerLevel sl) {
            while (this.charge >= CHARGE_PER_STRIKE) {
                this.charge -= CHARGE_PER_STRIKE;
                ServerPlayer t = nearest(sl, 32.0);
                if (t == null) break;
                strikeLightning(sl, t, phase);
            }
        }
        // ③ 冰球: 保护 —— 2026-09-11 二次按用户要求"减少防御buff": **去掉抗性**, 吸收只到 II 且短(60t)
        if (frost > 0) {
            this.boss.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 60,
                    Math.min((frost - 1) / 2 + (phase - 1) / 2, 1), false, false, true));      // 最多吸收 II
            this.boss.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 90, 0, false, false, true)); // 抗火(不挡玩家伤害)
        }
        // ④ 离子球: **更多进攻向增益**, 去掉生命恢复/吸收/生命提升/抗性(全是"防御buff", 用户要求削)
        if (ion > 0) {
            int amp = Math.min(ion - 1 + (phase - 1), 3);
            for (var fx : new net.minecraft.world.effect.MobEffect[]{
                    MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED, MobEffects.JUMP,
                    MobEffects.LUCK, MobEffects.SATURATION, MobEffects.FIRE_RESISTANCE,
                    MobEffects.NIGHT_VISION, MobEffects.WATER_BREATHING,
                    MobEffects.DOLPHINS_GRACE, MobEffects.SLOW_FALLING, MobEffects.CONDUIT_POWER}) {
                this.boss.addEffect(new MobEffectInstance(fx, 90, amp, false, false, true));
            }
        }
        if ((glass + lightning + frost + ion) > 0 && !this.passiveLogged) {
            this.passiveLogged = true;
            FTBossMod.LOGGER.info("[FTBoss][充能球] 球被动生效: 玻璃{} 电{} 冰{} 离子{} | 阶段{} 充能{}/{} | 每次闪电伤害 {}",
                    glass, lightning, frost, ion, phase, this.charge, CHARGE_MAX, 12 + phase * 6);
        }
    }

    /** 闪电打击: 目标脚下劈一道**纯视觉**闪电 + 雷电伤害(阶段越高越痛) */
    private void strikeLightning(ServerLevel sl, ServerPlayer t, int phase) {
        try {
            net.minecraft.world.entity.LightningBolt bolt =
                    net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(sl);
            if (bolt != null) {
                bolt.moveTo(t.getX(), t.getY(), t.getZ());
                bolt.setVisualOnly(true);          // 只做特效, 不点火/不引雷
                sl.addFreshEntity(bolt);
            }
        } catch (Throwable ignored) {
        }
        // 2026-09-11 按用户要求"电球引发闪电时更主动靠近玩家": 闪电落下的同时, 场上的电球立刻扑向玩家
        for (ChargedOrbEntity o : collectOrbs(sl)) {
            if (o.getOrbType() == OrbType.LIGHTNING) o.beginAssault();
        }
        float dmg = net.farmingtales.ftboss.boss.ai.BossScaling.dmg(this.boss, 12f + phase * 6f);      // 2026-09-11 按用户要求提高伤害(原 6+阶段×3)
        t.invulnerableTime = 0;
        // 2026-09-12 按用户要求: 充能打击时也摆手势(手部动画用得更频繁)
        this.boss.playCastAnim(OrbType.LIGHTNING);
        t.hurt(sl.damageSources().mobAttack(this.boss), (dmg) * 0.25f);   // 物理(技能半 × 全局 0.5 = 0.25, v77)
        t.invulnerableTime = 0;
        t.hurt(sl.damageSources().indirectMagic(this.boss, this.boss), (dmg) * 0.25f);   // 魔法(技能半 × 全局 0.5, v77)
        sl.playSound(null, t.getX(), t.getY(), t.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.2f, 1.2f);
        broadcast("§e【充能·闪电打击】§7 充能已满 -> " + String.format("%.1f", dmg) + " 点雷电伤害");
        FTBossMod.LOGGER.info("[FTBoss][充能球] 闪电打击 -> {} ({} 伤害, 阶段 {})",
                t.getName().getString(), String.format("%.1f", dmg), phase);
    }

    /** 当前充能值(供日志/调试) */
    public int charge() {
        return this.charge;
    }

    private boolean passiveLogged = false;
    /** 已突破的阶段数(用于 +2 槽) */
    private int unlockedPhases = 1;

    // ===== 【回想形态】(2026-09-11 按用户要求) =====
    /** 复读延迟(tick) */
    private static final int ECHO_DELAY = 16;
    /** 回想形态是否开启(狂暴 或 阶段 3) */
    private boolean echoForm = false;
    private AbstractSpell echoSpell;
    private net.minecraft.world.entity.LivingEntity echoTarget;
    private int echoLevel;
    private int echoTicks = 0;

    public boolean isEchoForm() {
        return this.echoForm;
    }

    public DefectOrbs(DefectCoreEntity boss) {
        this.boss = boss;
    }

    public int slots() {
        return this.orbSlots;
    }

    /** 阶段突破时调用: 球槽 +2(上限 {@value #SLOTS_MAX}) */
    public void unlockPhase(int phase) {
        if (phase <= this.unlockedPhases) return;
        this.unlockedPhases = phase;
        int before = this.orbSlots;
        this.orbSlots = Math.min(SLOTS_MAX, SLOTS_BASE + SLOTS_PER_PHASE * (phase - 1));
        this.boss.playPhaseAnim();      // 阶段突破动画(旋转升空)
        if (this.orbSlots != before) {
            FTBossMod.LOGGER.info("[FTBoss][充能球] 阶段 {} 解锁: 球槽 {} -> {}", phase, before, this.orbSlots);
            if (this.boss.level() instanceof ServerLevel sl) {
                broadcast("§5故障机器人§7 球槽扩展至 §d" + this.orbSlots + "§7 个");
            }
        }
    }

    /** 玩家拆掉一颗球: 短暂停补(给反制留出收益) */
    public void onOrbDestroyed(OrbType type) {
        this.punishCd = PUNISH_CD;
    }

    public void onOrbDestroyed(ChargedOrbEntity orb) {
        onOrbDestroyed(orb.getOrbType());
        this.castBacklog.removeIf(queued -> orb.getUUID().equals(queued.orbId()));
        if (orb.getUUID().equals(this.pendingOrbId)) {
            this.pendingType = null;
            this.pendingOrbId = null;
            this.pendingTicks = 0;
            this.boss.cancelOrbCastVisual();
            this.boss.showOrbCue("interrupt", orb.getOrbType(), orb.getId(), 8);
        }
    }

    /** 球到期: Boss 吸收它并准备放技能 */
    public void onOrbMatured(ChargedOrbEntity orb) {
        requestOrbCast(orb);
    }

    private void requestOrbCast(ChargedOrbEntity orb) {
        if (!orb.isOrbAvailable() || !orb.isOwnedBy(this.boss) || orb.isReservedForCast() || orb.isSpentProjectile()) return;
        if (this.castBacklog.size() >= SLOTS_MAX) return;
        orb.reserveForCast();
        if (this.pendingType != null || this.boss.isActionLocked()) {
            this.castBacklog.addLast(new QueuedCast(orb.getOrbType(), orb.getChargeTicks(), orb.getUUID()));
        } else {
            beginOrbCast(orb.getOrbType(), orb.getChargeTicks(), orb);
        }
    }

    private ChargedOrbEntity findOrb(java.util.UUID orbId) {
        if (orbId == null || !(this.boss.level() instanceof ServerLevel server)) return null;
        return server.getEntity(orbId) instanceof ChargedOrbEntity orb
                && orb.isOrbAvailable() && orb.isOwnedBy(this.boss) ? orb : null;
    }

    // ================= 每 tick =================

    /**
     * 每 tick 调用。**每 {@value #CYCLE}t(2 秒)一个周期**:
     * <ol>
     *   <li><b>补齐全部球槽</b> —— 空槽一次性补满;</li>
     *   <li><b>如果周期开始时场上已有球 → 激活最老的一颗</b>(吸收 + 放对应法术/技能)。</li>
     * </ol>
     * 补球时 Boss 播"抬手"动画, 核心按新球的种类变色。
     */
    public void tick() {
        if (this.boss.isDeadOrDying()) return;
        if (!(this.boss.level() instanceof ServerLevel sl)) return;
        if (this.pendingOrbId != null && findOrb(this.pendingOrbId) == null) {
            this.pendingType = null;
            this.pendingOrbId = null;
            this.boss.cancelOrbCastVisual();
        }
        if (this.pendingBasicSpell != null && --this.pendingBasicTicks <= 0) {
            AbstractSpell spell = this.pendingBasicSpell;
            var target = this.pendingBasicTarget;
            this.pendingBasicSpell = null;
            this.pendingBasicTarget = null;
            if (this.boss instanceof io.redspace.ironsspellbooks.api.entity.IMagicEntity magic
                    && !this.boss.isExhausted() && target != null && target.isAlive() && target.level() == sl
                    && BossTargets.valid(target) && this.boss.distanceToSqr(target) <= 26 * 26 && this.boss.hasLineOfSight(target)) {
                WizardSummon.castAt(magic, target, spell, this.pendingBasicLevel);
            }
        }
        // ★ 排队的施法到点就结算(即使这一刻没有目标也要走完, 免得卡住后续节奏)
        if (this.pendingType != null && --this.pendingTicks <= 0) {
            OrbType t = this.pendingType;
            int c = this.pendingCharge;
            ChargedOrbEntity selected = findOrb(this.pendingOrbId);
            this.pendingType = null;
            this.pendingOrbId = null;
            this.boss.showOrbCue("release", t, selected == null ? -1 : selected.getId(), 12);
            if (t == OrbType.LIGHTNING) this.lightningUsed++;
            if (selected != null && t == OrbType.DARK) {
                selected.releaseDark(c, this.orbDamageMul, nearest(sl, 28.0));
                sl.playSound(null, selected.getX(), selected.getY(), selected.getZ(), SoundEvents.WITHER_SHOOT,
                        net.minecraft.sounds.SoundSource.HOSTILE, 1.6f, 0.8f);
            } else {
                if (selected != null) selected.absorbed();
                releaseCast(t, c);
            }
            countSkillAndMaybeExhaust();
        }
        if (this.pendingType == null && !this.boss.isActionLocked() && !this.castBacklog.isEmpty()) {
            QueuedCast next = this.castBacklog.removeFirst();
            ChargedOrbEntity selected = findOrb(next.orbId());
            if (next.orbId() == null || selected != null) beginOrbCast(next.type(), next.charge(), selected);
        }
        if (this.boss.getTarget() == null) return;         // 没目标不折腾
        // ★ 超能光束: 蓄力/引导期间专心打光束(这期间不补球、也不放球技能)
        if ((this.beamCharge > 0 || this.beamFire > 0 || !this.boss.isActionLocked()) && tickBeam(sl)) return;
        // ★ 落地衰弱: 停掉补球与技能(用户要求"落地并进入一小段削弱阶段")
        if (this.boss.isExhausted()) return;
        if (this.boss.isActionLocked()) return;
        // ★【电流相生】: 二阶段专属(phase >= 2), 按已用电球数召唤等量电球扑击, 之后自己进入虚弱期
        if (tickCurrentIntergrowth(sl)) return;
        // ★【集中碎片】(2026-09-12 按用户要求): **整场只放一次** —— 之后全部球效果提升, 伤害 +50%, 召唤蛆更多
        if (!this.fragmentsUsed && this.boss.getPhase() >= 2) {
            this.fragmentsUsed = true;
            this.orbDamageMul = 1.5f;                 // 伤害 +50%
            this.maggotBase = 5;                      // 召唤的蛆增加
            this.currentCd = Math.min(this.currentCd, 200);
            this.boss.playCastAnim(OrbType.ION);
            this.boss.showOrbCue("echo", OrbType.ION, -1, 20);
            sl.playSound(null, this.boss.getX(), this.boss.getY(), this.boss.getZ(),
                    SoundEvents.BEACON_ACTIVATE, net.minecraft.sounds.SoundSource.HOSTILE, 2.0f, 0.7f);
            broadcast("§d【集中碎片】§7 核心把碎片全部吞下 —— §c全部球伤害 +50%§7, 召唤的蛆也更多了!");
            FTBossMod.LOGGER.info("[FTBoss][集中碎片] 已使用(仅一次): 球伤害 ×1.5, 蛆基数 {}", this.maggotBase);
            return;
        }
        // ★ 召唤【！?区区？！】蛆小怪(2026-09-11 按用户要求): 二阶段起每 12 秒吐 2~3 只
        if (this.boss.getPhase() >= 2 && --this.maggotCd <= 0) {
            this.maggotCd = 240;
            int n = this.maggotBase + this.boss.getRandom().nextInt(2);   // 2026-09-12: 基数随【集中碎片】提升
            for (int i = 0; i < n; i++) {
                net.farmingtales.ftboss.entity.MaggotMinionEntity mag =
                        net.farmingtales.ftboss.entity.FTEntities.MAGGOT_MINION.get().create(sl);
                if (mag == null) break;
                double a = this.boss.getRandom().nextDouble() * Math.PI * 2;
                mag.setOwnerBoss(this.boss);
                mag.moveTo(this.boss.getX() + Math.cos(a) * 2.0, this.boss.getY() + 0.2,
                        this.boss.getZ() + Math.sin(a) * 2.0, this.boss.getRandom().nextFloat() * 360f, 0f);
                sl.addFreshEntity(mag);
            }
            broadcast("§6【！?区区？！】§7 核心吐出了 " + n + " 只蛆 —— 被咬会叠【感染】(攻击敌人时反噬)");
            FTBossMod.LOGGER.info("[FTBoss][区区] 召唤蛆小怪 x{}", n);
        }

        if (this.punishCd > 0) this.punishCd--;
        // 球的被动(按存活颗数叠加)每 20t 刷新一次, 保证不断档
        if (--this.passiveCd <= 0) {
            this.passiveCd = 20;
            tickPassiveBuffs(collectOrbs(sl));
        }

        // ===== 【回想形态】判定: 狂暴(BossRage ≥ I) 或 二阶段 =====
        boolean wantEcho = net.farmingtales.ftboss.boss.ai.BossRage.tierOf(this.boss) >= 1 || this.boss.getPhase() >= 2;   // 2026-09-11 按用户要求: 二阶段起就进回响形态
        if (wantEcho != this.echoForm) {
            this.echoForm = wantEcho;
            if (wantEcho) {
                sl.playSound(null, this.boss.getX(), this.boss.getY(), this.boss.getZ(),
                        SoundEvents.WARDEN_SONIC_BOOM, net.minecraft.sounds.SoundSource.HOSTILE, 1.2f, 0.7f);
                this.boss.playEchoAnim();          // 2026-09-11 重做动画: 专属的"回想形态开启"演出(高频抖动+核心过载)
                broadcast("§5故障机器人 §d进入【回想形态】§7 —— 施法变慢, 但每次双发, 法术等级与稀有度提升");
                FTBossMod.LOGGER.info("[FTBoss] 故障机器人进入【回想形态】(狂暴/二阶段): 周期 40t -> {}t, 法术双发+等级+2", ECHO_CYCLE);
            } else {
                broadcast("§7故障机器人 §7退出了【回想形态】");
                FTBossMod.LOGGER.info("[FTBoss] 故障机器人退出【回想形态】");
            }
        }
        if (this.boss.isActionLocked()) return;
        // 复读(第二发)
        if (this.echoTicks > 0 && --this.echoTicks <= 0) fireEcho();

        int cycleLen = this.echoForm ? ECHO_CYCLE : CYCLE;
        if (--this.cycleCd > 0) return;
        this.cycleCd = cycleLen;

        List<ChargedOrbEntity> mine = collectOrbs(sl);
        boolean hadOrbs = !mine.isEmpty();

        // ① 补齐全部空槽
        /* v110 修复: 把 PUNISH_CD 的判定补上 —— {@link #onOrbDestroyed} 里写着
           "玩家拆掉一颗球: 短暂停补(给反制留出收益)", 但这里从来没有判定过它,
           拆掉的球下一个周期(2 秒)就补回来了, 反制等于零收益。
           只挡"补球", **不挡下面 ② 的激活** —— 否则拆一颗球能让 Boss 整个哑火 2 秒, 太狠。 */
        if (this.punishCd <= 0) {
            int want = Math.min(this.orbSlots, SLOTS_MAX);
            int add = Math.max(0, want - mine.size());
            for (int i = 0; i < add; i++) summonOrb(sl, i);    // i 用来错开一点生成角度
        }

        // ② 原本就有球 -> 激活最老的一颗
        if (hadOrbs) {
            // 2026-09-11 修"黑球几乎不释放铁魔法": 以前按 chargeTicks 取"最老"的球,
            // 而黑球存活时间更久(+240t) => 永远排不到最老 => 几乎轮不到它激发。
            // 现在改按**成熟度比例**(已存活 / 总存活)挑选, 谁最接近成熟就先激发, 黑球因此能正常轮转。
            ChargedOrbEntity oldest = null;
            double best = -1.0;
            for (ChargedOrbEntity o : mine) {
                if (o.isReservedForCast() || o.isSpentProjectile()) continue;
                double ratio = (double) o.getChargeTicks() / o.getLifeTicks();
                if (ratio > best) { best = ratio; oldest = o; }
            }
            if (oldest != null) requestOrbCast(oldest);
        }
    }

    // ================= 召唤 =================

    private void summonOrb(ServerLevel sl, int index) {
        ChargedOrbEntity orb = FTEntities.CHARGED_ORB.get().create(sl);
        if (orb == null) return;
        OrbType type = rollType();
        orb.setOrbType(type);
        orb.setOwnerBoss(this.boss);
        boolean[] occupied = new boolean[Math.max(1, this.orbSlots)];
        for (ChargedOrbEntity existing : collectOrbs(sl)) {
            int slot = existing.formationSlot();
            if (slot >= 0 && slot < occupied.length) occupied[slot] = true;
        }
        int slot = 0;
        while (slot < occupied.length - 1 && occupied[slot]) slot++;
        orb.setFormationSlot(slot, this.orbSlots);
        // 错开一点初始角度, 一次补满多颗时不会叠在一起
        double a = (Math.PI * 2 / Math.max(1, this.orbSlots)) * index;
        orb.setPos(this.boss.getX() + Math.cos(a) * 1.4, this.boss.getY() + 1.6, this.boss.getZ() + Math.sin(a) * 1.4);
        // 阶段越高, 球存在时间越长(= 全部球效果提升的一环)
        // 2026-09-11 按用户要求"黑暗球加长蓄能时间": **黑球存活更久**(多蓄 12 秒) => 蓄力等级更高、法术更狠
        orb.setLifeTicks(ChargedOrbEntity.LIFE + (this.boss.getPhase() - 1) * 80
                + (type == OrbType.DARK ? 240 : 0));    // 2026-09-11 蓄力时间 -20%: 300 -> 240t
        sl.addFreshEntity(orb);
        // 抬手动画 + 核心按新球变色(2026-09-11 按用户要求)
        this.boss.playSummonAnim();
        this.boss.showOrbCue("summon", type, orb.getId(), 17);
        this.boss.setCoreTint(type);
        sl.playSound(null, this.boss.getX(), this.boss.getY(), this.boss.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, net.minecraft.sounds.SoundSource.HOSTILE, 1.2f, 1.3f);
        FTBossMod.LOGGER.info("[FTBoss][充能球] 生成 {} (槽位 {}/{})", type.id, collectOrbs(sl).size(), this.orbSlots);
    }

    /** 兼容旧调用 */
    private void summonOrb(ServerLevel sl) {
        summonOrb(sl, 0);
    }

    /**
     * 掷球: **五种球从第一阶段起都可能出现**(2026-09-11 修 —— 旧版按阶段把黑球/离子权重设成 0,
     * 前期血量阶段永远抽不到, 表现就是"黑球离子球一直不生成")。
     * 权重随阶段倾斜, 但任何阶段每种球的权重都 &gt; 0。
     */
    private static final int[][] ORB_WEIGHTS = {
            // 玻璃, 电, 冰, 离子, 黑
            {25, 25, 20, 18, 12},   // P1
            {20, 22, 20, 20, 18},   // P2
            {15, 20, 18, 22, 25}    // P3
    };

    private OrbType rollType() {
        int idx = Math.max(0, Math.min(2, this.boss.getPhase() - 1));
        int[] w = ORB_WEIGHTS[idx];
        int total = 0;
        for (int x : w) total += x;
        int r = this.boss.getRandom().nextInt(total);
        for (int i = 0; i < w.length; i++) {
            r -= w[i];
            if (r < 0) return OrbType.values()[i];
        }
        return OrbType.GLASS;
    }

    // ================= 吸收 + 放技能 =================

    /**
     * 吸收一颗球并释放对应技能(2026-09-11 按用户要求: 参考沙耶吸收球 → 再用对应技能)。
     * 全过程从 **Boss 位置**结算, 并触发 Boss 的施法动画。
     *
     * <p><b>2026-09-11 按用户要求"施法要有动作、要流畅有衔接"</b>: 不再"动画一起手就结算技能", 而是
     * <b>把技能真正生效推迟到该动画的"推出/爆发"关键帧</b>({@link #releaseDelay}) —— 起手是抬手蓄力,
     * 提前结算会出现"手还没推出去伤害已经打完"的脱节感。推迟期间 Boss 那边照常播施法动画。
     */
    public void absorbAndCast(OrbType type, int chargeTicks) {
        if (!this.boss.isAlive() || this.boss.level().isClientSide) return;
        if (this.pendingType != null || this.boss.isActionLocked()) {
            if (this.castBacklog.size() < SLOTS_MAX) this.castBacklog.addLast(new QueuedCast(type, chargeTicks, null));
            return;
        }
        beginOrbCast(type, chargeTicks, null);
    }

    private void beginOrbCast(OrbType type, int chargeTicks, ChargedOrbEntity selected) {
        if (!(this.boss.level() instanceof ServerLevel sl)) return;
        this.boss.playCastAnim(type);                     // 该球种专属攻击动画(起手就是抬手蓄力)
        this.boss.setCoreTint(type);                      // 核心切换成该球的颜色
        this.pendingType = type;                          // ★ 排队: 到"推出帧"再结算
        this.pendingCharge = chargeTicks;
        this.pendingTicks = releaseDelay(type);
        this.pendingOrbId = selected == null ? null : selected.getUUID();
        if (selected != null) selected.focusForCast(this.pendingTicks);
        this.boss.showOrbCue("focus", type, selected == null ? -1 : selected.getId(), this.pendingTicks);
        sl.playSound(null, this.boss.getX(), this.boss.getY() + 1.6, this.boss.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 0.75f);
    }

    /** 该球种动画的"推出/爆发"时刻(tick) —— 必须与 `defect_core_anims.cjs` 的关键帧对齐 */
    private static int releaseDelay(OrbType type) {
        return net.farmingtales.ftboss.util.OrbLinkMath.releaseTicks(type.id);
    }

    /** 真正结算技能(由 {@link #absorbAndCast} 排队, 到点后在 tick 里调用) */
    /**
     * 黑球专用: **被激活时发射出去撞击**(2026-09-11 按用户要求, 取消其铁魔法)。
     * 蓄力越久伤害越高 —— **每多蓄 1 秒 +2 点伤害**(chargeTicks/20 × 2)。
     */
    private void launchDarkOrb(ServerLevel sl, int chargeTicks, ServerPlayer target) {
        double bx = this.boss.getX(), by = this.boss.getY() + 1.6, bz = this.boss.getZ();
        float bonus = 2f * (chargeTicks / 20f);                     // 每秒 +2
        ChargedOrbEntity orb = FTEntities.CHARGED_ORB.get().create(sl);
        if (orb == null) return;
        double ax = target != null ? target.getX() - bx : -Math.sin(Math.toRadians(this.boss.getYRot()));
        double az = target != null ? target.getZ() - bz : Math.cos(Math.toRadians(this.boss.getYRot()));
        double al = Math.max(0.001, Math.sqrt(ax * ax + az * az));
        orb.setOrbType(OrbType.DARK);
        orb.setOwnerBoss(this.boss);
        orb.setSlamBonus(bonus);
        orb.setDamageMul(this.orbDamageMul);      // 集中碎片: 球伤害 ×1.5
        orb.setBlackHoleChance(true);        // 2026-09-11: 满充能撞击有 5% 放黑洞
        orb.setLifeTicks(200);
        orb.setPos(bx + ax / al * 1.6, by, bz + az / al * 1.6);
        orb.releaseDark(chargeTicks, this.orbDamageMul, target);
        sl.addFreshEntity(orb);
        this.boss.playCastAnim(OrbType.DARK);      // 2026-09-12: 发射时摆手势
        sl.playSound(null, bx, by, bz, SoundEvents.WITHER_SHOOT, net.minecraft.sounds.SoundSource.HOSTILE, 1.6f, 0.8f);
        broadcast("§5【黑球·发射】§7 蓄力 " + (chargeTicks / 20) + " 秒 -> 撞击伤害 §c+" + (int) bonus);
        FTBossMod.LOGGER.info("[FTBoss][黑球] 发射撞击: 蓄力 {}t, 额外伤害 +{}", chargeTicks, (int) bonus);
    }

    private void releaseCast(OrbType type, int chargeTicks) {
        if (!(this.boss.level() instanceof ServerLevel sl)) return;
        double bx = this.boss.getX(), by = this.boss.getY() + 1.6, bz = this.boss.getZ();
        ServerPlayer target = nearest(sl, 28.0);
        // 推出瞬间的爆发特效(与动画的推出帧同步, 这是"有衔接"的关键观感)
        Fx.particles(sl, bx, by, bz, 20, ParticleTypes.END_ROD, 0.45, 0.16);
        Fx.ring(sl, bx, this.boss.getY() + 0.4, bz, 2.2, 20, ParticleTypes.CRIT, 0.05);
        // 2026-09-11 按用户要求: **黑球不再使用铁魔法**, 改为发射出去撞击
        if (type == OrbType.DARK) {
            launchDarkOrb(sl, chargeTicks, target);
            return;
        }
        // ① 先放铁魔法(有铁魔法就用真法术; 没有则回退到下面的内置元素效果)
        boolean spellCast = castIronSpell(sl, type, chargeTicks, target);

        switch (type) {
            case GLASS -> {              // 【AOE】
                if (!spellCast) {
                    int hits = 0;
                    for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, this.boss.getBoundingBox().inflate(8.0))) {
                        if (!p.isAlive()) continue;
                        p.invulnerableTime = 0;
        p.hurt(sl.damageSources().mobAttack(this.boss), (net.farmingtales.ftboss.boss.ai.BossScaling.dmg(this.boss, 20f * this.orbDamageMul)) * 0.25f);   // 物理(v77)
        p.invulnerableTime = 0;
        p.hurt(sl.damageSources().indirectMagic(this.boss, this.boss), (net.farmingtales.ftboss.boss.ai.BossScaling.dmg(this.boss, 20f * this.orbDamageMul)) * 0.25f);   // 魔法(v77)
                        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, true, true));
                        hits++;
                    }
                    FTBossMod.LOGGER.info("[FTBoss][充能球] 玻璃球(内置AOE回退), 命中 {} 人", hits);
                }
                sl.playSound(null, bx, by, bz, SoundEvents.GLASS_BREAK, net.minecraft.sounds.SoundSource.HOSTILE, 2.4f, 0.6f);
                broadcast("§b【玻璃球·范围法术】§7 " + (spellCast ? "铁魔法已发动" : "范围碎裂"));
            }
            case LIGHTNING -> {          // 【单体】
                if (target == null) {
                    broadcast("§e【电球·雷击】§7 找不到目标, 空放");
                    break;
                }
                if (!spellCast) {
                    target.invulnerableTime = 0;
        target.hurt(sl.damageSources().mobAttack(this.boss), (net.farmingtales.ftboss.boss.ai.BossScaling.dmg(this.boss, 30f * this.orbDamageMul)) * 0.25f);   // 物理(v77)
        target.invulnerableTime = 0;
        target.hurt(sl.damageSources().indirectMagic(this.boss, this.boss), (net.farmingtales.ftboss.boss.ai.BossScaling.dmg(this.boss, 30f * this.orbDamageMul)) * 0.25f);   // 魔法(v77)
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 2, false, true, true));
                    FTBossMod.LOGGER.info("[FTBoss][充能球] 电球(内置雷击回退) -> {}", target.getName().getString());
                }
                if (target != null) {
                    sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT,
                            net.minecraft.sounds.SoundSource.HOSTILE, 1.4f, 1.3f);
                }
                broadcast("§e【电球·单体雷击】§7 锁定 " + target.getName().getString()
                        + (spellCast ? " (铁魔法)" : ""));
            }
            case DARK -> {               // 【蓄力】高等级高稀有度
                if (!spellCast) {
                    ServerPlayer t = target;
                    float dmg = net.farmingtales.ftboss.boss.ai.BossScaling.dmg(this.boss, (16f + Math.min(chargeTicks / 14f, 30f)) * this.orbDamageMul);
                    if (t != null) {
                        t.invulnerableTime = 0;
        t.hurt(sl.damageSources().mobAttack(this.boss), (dmg) * 0.25f);   // 物理(技能半 × 全局 0.5 = 0.25, v77)
        t.invulnerableTime = 0;
        t.hurt(sl.damageSources().indirectMagic(this.boss, this.boss), (dmg) * 0.25f);   // 魔法(技能半 × 全局 0.5, v77)
                        /* 2026-09-12 v37 按用户要求: 对玩家的减益不允许黑暗 -> 改成反胃 */
                        FTBossMod.LOGGER.info("[FTBoss][充能球] 黑球(内置蓄力回退) {}t -> {} 伤害 {}",
                                chargeTicks, t.getName().getString(), String.format("%.1f", dmg));
                    }
                }
                if (target != null) {
                    sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SCULK_SHRIEKER_SHRIEK,
                            net.minecraft.sounds.SoundSource.HOSTILE, 1.2f, 1.1f);
                }
                broadcast("§5【黑球·蓄力爆发】§7 蓄力 " + (chargeTicks / 20) + "s → 高稀有度法术"
                        + (spellCast ? " (铁魔法)" : ""));
            }
            case FROST -> {              // 【保护】法术 + 保底薄霜甲(2026-09-11 二次削减: 吸收III+抗性I -> 只留吸收I, 去掉抗性)
                this.boss.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 0, false, false, true));
                sl.playSound(null, bx, by, bz, SoundEvents.GLASS_PLACE, net.minecraft.sounds.SoundSource.HOSTILE, 1.6f, 0.8f);
                broadcast("§f【冰球·保护】§7 霜甲(吸收I)" + (spellCast ? " + 铁魔法冰系" : ""));
            }
            case ION -> {                // 【强化】法术 + 保底强化(去掉"力量": 改成**直接充能**, 充能既抬法术等级又攒闪电打击)
                this.charge = Math.min(CHARGE_MAX, this.charge + 12);
                this.boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 260, 1, false, false, true));
                sl.playSound(null, bx, by, bz, SoundEvents.BEACON_POWER_SELECT, net.minecraft.sounds.SoundSource.HOSTILE, 1.8f, 1.5f);
                broadcast("§d【离子球·强化】§7 充能 +12(法术等级+闪电打击) + 速度II" + (spellCast ? " + 铁魔法增益" : ""));
            }
        }
    }

    /** Boss 死亡/清理: 移除所有球 */
    public void reset() {
        if (!(this.boss.level() instanceof ServerLevel sl)) return;
        for (ChargedOrbEntity orb : sl.getEntitiesOfClass(ChargedOrbEntity.class, this.boss.getBoundingBox().inflate(64))) {
            if (orb.isOwnedBy(this.boss)) orb.discard();
        }
        this.castBacklog.clear();
        this.pendingType = null;
        this.pendingOrbId = null;
        this.pendingBasicSpell = null;
        this.echoSpell = null;
        this.boss.showOrbCue("", OrbType.GLASS, -1, 1);
        this.orbSlots = SLOTS_BASE;
        this.unlockedPhases = 1;
    }

    // ================= 内部 =================

    /** 收集"我的"球(按存活时间从老到新) */
    public List<ChargedOrbEntity> collectOrbs(ServerLevel sl) {
        List<ChargedOrbEntity> out = new ArrayList<>();
        for (ChargedOrbEntity o : sl.getEntitiesOfClass(ChargedOrbEntity.class, this.boss.getBoundingBox().inflate(48))) {
            if (o.isOrbAvailable() && o.isOwnedBy(this.boss)) out.add(o);
        }
        out.sort((a, b) -> Integer.compare(b.getChargeTicks(), a.getChargeTicks()));
        return out;
    }

    // ================= 超能光束(2026-09-11 新增技能) =================

    /**
     * 超能光束状态机(用户要求: "对核心前方直线范围生物造成每秒 30 点伤害, 持续三秒")。
     * 流程: 冷却到 → **锁定方向**(锁定后不再追踪, 侧移可躲) → 0.75s 蓄力 → 3s 引导
     * (每 0.5s 结算一次, 设计 22 / 实伤 11 —— 口径见 {@link #BEAM_DMG}) → 收势。
     * @return true = 正在蓄力或引导(调用方就此 return, 让 Boss"专心引导")
     */
    private boolean tickBeam(ServerLevel sl) {
        double x = this.boss.getX(), y = this.boss.getY() + 1.7, z = this.boss.getZ();
        if (this.beamFire > 0) {
            // 2026-09-11 按用户要求"必定锁定": 引导期间**每 tick 重新对准玩家**(横向绕背也甩不掉)
            ServerPlayer lockOn = nearest(sl, BEAM_LEN);
            if (lockOn != null && lockOn.isAlive()) {
                double lx = lockOn.getX() - x;
                double ly = lockOn.getY() + lockOn.getBbHeight() * 0.5 - y;
                double lz = lockOn.getZ() - z;
                double ll = Math.max(0.001, Math.sqrt(lx * lx + ly * ly + lz * lz));
                this.beamDx = lx / ll; this.beamDy = ly / ll; this.beamDz = lz / ll;
            }
            beamFx(sl);
            if (--this.beamPulse <= 0) {
                this.beamPulse = BEAM_PULSE;
                beamDamage(sl);
            }
            if (--this.beamFire <= 0) {
                sl.playSound(null, x, y, z, SoundEvents.BEACON_DEACTIVATE,
                        net.minecraft.sounds.SoundSource.HOSTILE, 1.6f, 0.6f);
                broadcast("§b【超能光束】§7 引导结束");
                countSkillAndMaybeExhaust();      // 光束也算一次技能
                FTBossMod.LOGGER.info("[FTBoss][超能光束] 引导结束(共 {} 次脉冲, 每次 {} 伤害)",
                        BEAM_DURATION / BEAM_PULSE, String.format("%.1f", BEAM_DMG));
                this.beamCd = beamCooldown();
                this.boss.setBeamCharging(false);   // 兜底: 引导结束一定解除冻结
                // 2026-09-11 按用户要求: 光束放完**落地并虚弱一小段**(移动/出手全停, 且受 1.5× 伤害)
                this.boss.enterExhausted(net.farmingtales.ftboss.entity.DefectCoreEntity.EXHAUST_TICKS);
                broadcast("§c【过载衰弱】§7 核心落地, 期间受到 §c1.5×§7 伤害 —— 反击时机!");
                FTBossMod.LOGGER.info("[FTBoss][超能光束] 进入落地衰弱 {}t",
                        net.farmingtales.ftboss.entity.DefectCoreEntity.EXHAUST_TICKS);
            }
            return true;
        }
        if (this.beamCharge > 0) {
            Fx.particles(sl, x + this.beamDx * 0.6, y + this.beamDy * 0.6, z + this.beamDz * 0.6,
                    3, ParticleTypes.REVERSE_PORTAL, 0.45, 0.03);
            // ================= 2026-09-11 按用户要求: 红色蓄力预警 =================
            double prog = 1.0 - (double) this.beamCharge / BEAM_CHARGE;   // 0 -> 1
            var warnRed = new net.minecraft.core.particles.DustParticleOptions(
                    new org.joml.Vector3f(1.0f, 0.05f, 0.05f), 1.35f);
            Fx.ring(sl, x, this.boss.getY() + 0.2, z, 4.6 - 3.4 * prog, 8, warnRed, 0.0);   // 收缩红环
            for (double d = 1.0; d <= BEAM_LEN * (0.3 + 0.7 * prog); d += 3.0) {            // 红色预警线
                Fx.particles(sl, x + this.beamDx * d, y + this.beamDy * d, z + this.beamDz * d, 1, warnRed, 0.06, 0.0);
            }
            Fx.particles(sl, x, y, z, 3, warnRed, 0.6, 0.02);                               // 核心红光
            if (this.beamCharge % 8 == 0) {                                                 // 警示音(音调递增)
                sl.playSound(null, x, y, z, SoundEvents.BEACON_POWER_SELECT,
                        net.minecraft.sounds.SoundSource.HOSTILE, 1.1f, 0.7f + (float) prog * 0.7f);
            }   // 能量向核心收束
            if (--this.beamCharge <= 0) beginBeamFire(sl);
            return true;
        }
        if (this.beamCd > 0 && --this.beamCd <= 0) {
            ServerPlayer t = nearest(sl, 24.0);
            if (t != null && this.pendingType == null) {
                // 2026-09-11 按用户要求改回"**必定锁定玩家**": 起手直接对准目标(玩家躯干中心)
                double ty2 = t.getY() + t.getBbHeight() * 0.5;
                double dx2 = t.getX() - x, dy2 = ty2 - y, dz2 = t.getZ() - z;
                double len = Math.max(0.001, Math.sqrt(dx2 * dx2 + dy2 * dy2 + dz2 * dz2));
                this.beamDx = dx2 / len; this.beamDy = dy2 / len; this.beamDz = dz2 / len;
                this.beamCharge = BEAM_CHARGE;
                this.beamPulse = BEAM_PULSE;
                this.boss.playBeamAnim();
                this.boss.showOrbCue("beam_charge", OrbType.ION, -1, BEAM_CHARGE);
                this.boss.setBeamCharging(true);           // 蓄力期间**站立不动**
                // 10 秒蓄力期间**无法防御**: 剥掉全部防御增益(和落地削弱同一套)
                for (var fx : new net.minecraft.world.effect.MobEffect[]{
                        net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
                        net.minecraft.world.effect.MobEffects.ABSORPTION,
                        net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE,
                        net.minecraft.world.effect.MobEffects.HEALTH_BOOST}) {
                    this.boss.removeEffect(fx);
                }
                // 2026-09-11 按用户要求: 蓄力时给射程内的玩家**加速 I**, 让他们有机会跑开或找掩体
                for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, this.boss.getBoundingBox().inflate(BEAM_LEN))) {
                    if (!p.isAlive()) continue;
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BEAM_CHARGE + 30, 0, false, true, true));
                    p.displayClientMessage(Component.literal("§b【超能光束】§7核心正在蓄力 —— §a你获得加速 I§7, 快离开射线!"), true);
                }
                sl.playSound(null, x, y, z, SoundEvents.EVOKER_PREPARE_ATTACK,
                        net.minecraft.sounds.SoundSource.HOSTILE, 1.5f, 0.7f);
                broadcast("§b【超能光束】§7 核心开始蓄能 —— §c离开射线!");
                FTBossMod.LOGGER.info("[FTBoss][超能光束] 蓄力开始, 锁定方向 ({}, {}, {})",
                        String.format("%.2f", this.beamDx), String.format("%.2f", this.beamDy),
                        String.format("%.2f", this.beamDz));
                return true;
            }
            this.beamCd = 40;      // 没目标: 过 2 秒再看
        }
        return false;
    }

    /** 光束冷却: 阶段越高越频繁, 回想形态再 ×0.7 */
    private int beamCooldown() {
        int phase = this.boss.getPhase();
        int base = phase >= 3 ? 200 : (phase == 2 ? 280 : 360);
        return this.echoForm ? (int) (base * 0.7) : base;
    }

    private void beginBeamFire(ServerLevel sl) {
        double x = this.boss.getX(), y = this.boss.getY() + 1.7, z = this.boss.getZ();
        this.beamFire = BEAM_DURATION;
        this.boss.showOrbCue("beam_fire", OrbType.ION, -1, BEAM_DURATION);
        this.beamPulse = BEAM_PULSE;
        this.boss.setBeamCharging(false);   // 开火, 解除站立不动
        sl.playSound(null, x, y, z, SoundEvents.BEACON_ACTIVATE,
                net.minecraft.sounds.SoundSource.HOSTILE, 2.0f, 1.6f);
        broadcast("§b【超能光束】§7 射线将持续 3 秒 —— §c尽快离开射线!");
        beamDamage(sl);
    }

    /** 对射线附近的**生物**每 0.5s 结算 15 点(合计 30/秒); 到射线垂直距离在半径内的才算命中 */
    private void beamDamage(ServerLevel sl) {
        net.minecraft.world.phys.Vec3 from = new net.minecraft.world.phys.Vec3(
                this.boss.getX(), this.boss.getY() + 1.7, this.boss.getZ());
        net.minecraft.world.phys.Vec3 dir = new net.minecraft.world.phys.Vec3(this.beamDx, this.beamDy, this.beamDz).normalize();
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(from, from.add(dir.scale(BEAM_LEN)))
                .inflate(BEAM_RADIUS + 1.5);
        int hits = 0;
        for (net.minecraft.world.entity.LivingEntity le :
                sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, box)) {
            if (le == this.boss || !le.isAlive() || le instanceof DefectCoreEntity) continue;
            net.minecraft.world.phys.Vec3 rel = le.position().add(0, le.getBbHeight() * 0.5, 0).subtract(from);
            double along = rel.dot(dir);
            if (along < -0.5 || along > BEAM_LEN) continue;
            double perp = rel.subtract(dir.scale(along)).length();
            if (perp > BEAM_RADIUS + le.getBbWidth() * 0.5) continue;
            // 2026-09-11 按用户要求: 总伤害**均分四份** —— 物理 / 魔法 / 虚空 / 真实伤害 各 1/4
            // 2026-09-12 v69: 物理/魔法挂上攻击者, 由 BossDamageTuning 全局减半;
            //                 虚空/真实这两种类型无法携带攻击者, 这里**手动**乘同一个倍率, 保证四份都减半。
            float bTotal = net.farmingtales.ftboss.boss.ai.BossScaling.dmg(this.boss, BEAM_DMG);
            float bQuarter = bTotal / 8f;      // 四份 × 全局 0.5(v77)
            le.invulnerableTime = 0;
            le.hurt(sl.damageSources().mobAttack(this.boss), bQuarter);      // ① 物理
            le.invulnerableTime = 0;
            le.hurt(sl.damageSources().indirectMagic(this.boss, this.boss), bQuarter);  // ② 魔法
            le.invulnerableTime = 0;
            le.hurt(sl.damageSources().fellOutOfWorld(), bQuarter);           // ③ 虚空
            le.invulnerableTime = 0;
            le.hurt(beamDamageSource(sl), bQuarter);                          // ④ 真实伤害(armamentarium)
            net.minecraft.world.phys.Vec3 push = dir.scale(0.32);
            net.farmingtales.ftboss.util.BossKnockback.push(le, push.x, 0.06, push.z);
            hits++;
        }
        sl.playSound(null, from.x, from.y, from.z, SoundEvents.WARDEN_SONIC_CHARGE,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.3f, 1.5f);
        if (hits > 0) {
            FTBossMod.LOGGER.info("[FTBoss][超能光束] 命中 {} 个生物, 每个 {} 伤害",
                    hits, String.format("%.1f", BEAM_DMG));
        }
    }

    /** 光束的伤害来源: **armamentarium 的真实伤害**(无视护甲/抗性); 取不到就退回魔法伤害 */
    private static net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> TRUE_DMG_KEY = null;
    private static boolean TRUE_DMG_TRIED = false;

    private net.minecraft.world.damagesource.DamageSource beamDamageSource(ServerLevel sl) {
        if (!TRUE_DMG_TRIED) {
            TRUE_DMG_TRIED = true;
            var reg = sl.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE);
            // 2026-09-11 按用户要求: 用 armamentarium **bypasses_armor(真实伤害)** 名单里的类型
            // (electrocution 是"电击", 最贴合这道能量束; 其余为同名单里的备选)
            for (String p : new String[]{"electrocution", "assassination", "hemorrhage", "frenzied_burn", "venom", "cacophony", "doom"}) {
                var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                        new net.minecraft.resources.ResourceLocation("armamentarium", p));
                if (reg.containsKey(key)) {
                    TRUE_DMG_KEY = key;
                    FTBossMod.LOGGER.info("[FTBoss][超能光束] 伤害类型 = 真实伤害 armamentarium:{}", p);
                    break;
                }
            }
            if (TRUE_DMG_KEY == null) {
                FTBossMod.LOGGER.warn("[FTBoss][超能光束] 未找到 armamentarium 真实伤害类型 -> 退回魔法伤害");
            }
        }
        if (TRUE_DMG_KEY == null) return sl.damageSources().magic();
        // 1.20.1 没有合适的 source(key,e,e) 重载, 直接用 DamageSource(Holder<DamageType>, 直接来源, 造成者)
        var holder = sl.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                .getHolderOrThrow(TRUE_DMG_KEY);
        return new net.minecraft.world.damagesource.DamageSource(holder, this.boss, this.boss);
    }

    /** 每用 3 次技能, 有 35% 概率自己进入落地虚弱(不动/不攻击/不防御) */
    private void countSkillAndMaybeExhaust() {
        this.skillCount++;
        if (this.skillCount % 3 != 0) return;
        // 2026-09-11 性能/合理性修正(日志实证本会话触发 90 次):
        // ① 已经处于虚弱就别再触发(否则"虚弱->恢复->立刻又虚弱"来回抖动)
        if (this.boss.isExhausted()) return;
        // ② 内置 30 秒冷却, 避免技能密集时连续触发
        long now = this.boss.level().getGameTime();
        if (now - this.lastMisfire < 600L) return;
        if (true) return;   // 2026-09-11 按用户要求: **移除"施放技能导致机器人眩晕"**(反复抖动+费性能, 彻底停用)
        this.lastMisfire = now;
        this.boss.enterExhausted(net.farmingtales.ftboss.entity.DefectCoreEntity.EXHAUST_TICKS);
        broadcast("§c【机能紊乱】§7 机器人过载, 自己陷入了落地虚弱!");
        FTBossMod.LOGGER.info("[FTBoss][机能紊乱] 第 {} 次技能后自我虚弱", this.skillCount);
    }

    /** 光束视觉: 从核心沿锁定方向画一条亮线(END_ROD 芯 + ELECTRIC_SPARK 外层 + 枪口火花) */
    private void beamFx(ServerLevel sl) {
        double x = this.boss.getX(), y = this.boss.getY() + 1.7, z = this.boss.getZ();
        Fx.particles(sl, x + this.beamDx * 0.9, y + this.beamDy * 0.9, z + this.beamDz * 0.9,
                3, ParticleTypes.END_ROD, 0.14, 0.03);
        for (double d = 2.0; d <= BEAM_LEN; d += 2.5) {      // 2026-09-11: 粒子稀疏化(范围靠判定半径, 不靠粒子堆)
            double px = x + this.beamDx * d, py = y + this.beamDy * d, pz = z + this.beamDz * d;
            Fx.particles(sl, px, py, pz, 1, ParticleTypes.ELECTRIC_SPARK, 0.18, 0.01);
        }
        Fx.particles(sl, x, y, z, 1, ParticleTypes.SOUL_FIRE_FLAME, 0.2, 0.01);
    }

    // ================= 【电流相生】(2026-09-11 新增技能) =================

    /**
     * 【电流相生】(用户原文: "在三阶段使用，机器人会根据之前使用的电球总数召唤一样数量的电球攻击，
     * 施放后机器人进入虚弱期，虚弱时不会攻击玩家，但是会在虚弱时期 5 秒内回复 10% 血量，无法移动，无法防御")。
     *
     * <p>实现(v110 注释订正): **二阶段起**({@code getPhase() >= 2}, 用户后来把"三阶段"改成了二阶段)
     * + 冷却到 + 已有 ≥2 颗电球记录 → 对玩家落 min(已用电球数, 12) 道**纯视觉闪电**(2026-09-11 起
     * 不再召唤电球, 而是"按用掉的数量落等量雷", 落点 ±3 格内吃全额伤害),
     * 每颗一出场就 {@link ChargedOrbEntity#beginAssault()} 直接扑向玩家(不等环绕);
     * 随后 Boss 进入 {@value #CURRENT_EXHAUST}t 虚弱: 不移动(已落地)、不出手、剥掉防御增益、受伤 ×1.5,
     * 但**每 5t 回复最大生命的 1/50**(合计 10%)。
     * @return true = 本 tick 由技能接管
     */
    private boolean tickCurrentIntergrowth(ServerLevel sl) {
        if (this.boss.getPhase() < 2) return false;   // 2026-09-11 按用户要求: 二阶段即可用
        if (this.currentCd > 0 && --this.currentCd > 0) return false;
        if (this.lightningUsed < 2) { this.currentCd = 100; return false; }   // 电球还没攒够, 过 5 秒再看
        int n = Math.min(this.lightningUsed, CURRENT_MAX_ORBS);
        double bx = this.boss.getX(), by = this.boss.getY() + 1.8, bz = this.boss.getZ();
        // 2026-09-11 按用户要求: 改成**按已施放的电球数量, 对玩家落下等量闪电**(不再召唤电球)
        // 2026-09-12: 落雷前摆手势
        this.boss.playCastAnim(OrbType.LIGHTNING);
        java.util.List<ServerPlayer> victims = sl.getEntitiesOfClass(ServerPlayer.class, this.boss.getBoundingBox().inflate(40.0));
        for (int i = 0; i < n; i++) {
            double ox = bx + (this.boss.getRandom().nextDouble() - 0.5) * 6.0;
            double oz = bz + (this.boss.getRandom().nextDouble() - 0.5) * 6.0;
            net.minecraft.world.entity.LightningBolt bolt =
                    net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(sl);
            if (bolt != null) {
                bolt.moveTo(ox, this.boss.getY(), oz);
                bolt.setVisualOnly(true);
                sl.addFreshEntity(bolt);
            }
            for (ServerPlayer p : victims) {
                if (!p.isAlive() || p.distanceToSqr(ox, this.boss.getY(), oz) > 12.0) continue;
                p.invulnerableTime = 0;
                p.hurt(sl.damageSources().lightningBolt(),
                        net.farmingtales.ftboss.boss.ai.BossScaling.dmg(this.boss, 12f + this.boss.getPhase() * 6f));
            }
            sl.playSound(null, ox, this.boss.getY(), oz, SoundEvents.LIGHTNING_BOLT_THUNDER,
                    net.minecraft.sounds.SoundSource.HOSTILE, 1.2f, 1.0f);
        }
        this.boss.playCastAnim(OrbType.LIGHTNING);
        sl.playSound(null, bx, by, bz, SoundEvents.LIGHTNING_BOLT_THUNDER,
                net.minecraft.sounds.SoundSource.HOSTILE, 2.2f, 0.9f);
        broadcast("§e【电流相生】§7 核心把用掉的 §e" + n + "§7 颗电球全部召回! 随之进入 §c5 秒虚弱§7(回血 10%)");
        FTBossMod.LOGGER.info("[FTBoss][电流相生] 二阶段施放: 已用电球 {} 颗 -> 落雷 {} 道, 随后虚弱 {}t(回血 {}%)",
                this.lightningUsed, n, CURRENT_EXHAUST, (int) (CURRENT_REGEN * 100));
        // 施放代价: 落地 + 5 秒虚弱(不攻击/不移动/无法防御, 但回 10% 血)
        this.boss.enterExhausted(CURRENT_EXHAUST, CURRENT_REGEN);
        this.currentCd = 600;      // 30 秒冷却
        return true;
    }

    private ServerPlayer nearest(ServerLevel sl, double radius) {
        net.minecraft.world.entity.LivingEntity priority = BossAggro.highest(this.boss);
        if (priority != null) {
            return priority instanceof ServerPlayer player && this.boss.distanceToSqr(player) <= radius * radius
                    ? player : null;
        }
        ServerPlayer best = null;
        double bd = radius * radius;
        for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, this.boss.getBoundingBox().inflate(radius))) {
            /* v114 按用户反馈: 创造/旁观玩家不当索敌目标(光束以前会锁上一个打不动的无敌玩家) */
            if (!net.farmingtales.ftboss.boss.ai.BossTargets.valid(p)) continue;
            double d = this.boss.distanceToSqr(p);
            if (p.isAlive() && d < bd) { bd = d; best = p; }
        }
        return best;
    }

    private void broadcast(String msg) {
        // 2026-09-12 按用户要求: **只提示光束与虚弱(过载衰弱)**, 球的产生/使用一律不打扰玩家
        if (msg == null) return;
        if (!(msg.contains("光束") || msg.contains("虚弱") || msg.contains("过载") || msg.contains("衰弱"))) return;
        if (!(this.boss.level() instanceof ServerLevel sl)) return;
        for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, this.boss.getBoundingBox().inflate(48))) {
            p.displayClientMessage(Component.literal(msg), true);
        }
    }
}
