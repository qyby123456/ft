package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.entity.EasterBossEntity;

public final class ManorBossCombatRules {
    public static final int SPELLS_PER_CAST = 2;
    public static final double DORA_SPEAR_REACH = 6.0;
    public static final int DORA_SPEAR_HITS = 3;
    public static final int DORA_SPEAR_INTERVAL = 4;
    public static final int DORA_SPEAR_COOLDOWN = 30;

    /** ★ v178 功底专用: 铁魔法施法间隔倍数(1.8 倍 = 施法密度降到约 55%)。 */
    public static final double GONGDI_SPELL_INTERVAL_MULTIPLIER = 1.8;
    /** ★ v178 功底专用: 三招轮转的出手冷却(tick)。 */
    public static final int GONGDI_SKILL_COOLDOWN = 120;

    /**
     * ★ 2026-09-24 按用户要求「减少紫菜的攻速」：紫菜(variant 8)的近战出手间隔(tick)。
     *
     * <p>庄园各变体的近战节奏本来都走原版 {@code MeleeAttackGoal.getAttackInterval()}
     * ＝ 20 tick(1 秒 1 下)。这里只给紫菜一个人拉长到 <b>30 tick(1.5 秒 1 下)</b>，
     * 用来配合"一次攻击只给 2 个 debuff"的新口径 —— 出手次数少了，减益压力才是真的降下来。
     * 其他变体(含青衣那条并行近战分支)不受影响。
     */
    public static final int ZICAI_MELEE_INTERVAL = 30;

    /** 这个变体的近战出手间隔覆盖值(0 = 用原版默认节奏)。见 {@link #ZICAI_MELEE_INTERVAL}。 */
    public static int meleeInterval(int variant) {
        return variant == EasterBossEntity.ZICAI_VARIANT ? ZICAI_MELEE_INTERVAL : 0;
    }

    private ManorBossCombatRules() {}

    public static double radiusScale(int variant) {
        if (variant == 1) return 1.5;
        /* ★ v188 用户要求「祁崽法术半径翻倍」: variant 11 从 1.0 -> 2.0 */
        if (variant == EasterBossEntity.QIZAI_VARIANT) return 2.0;
        return variant >= 2 && variant <= 8 || variant == 10 ? 2.0 : 1.0;
    }

    public static double spellRadius(int variant, double radius) {
        return radius * radiusScale(variant) + (variant == 6 ? 1.0 : 0.0);
    }

    public static boolean prefersMelee(int variant) {
        return variant == 0 || variant == 1 || variant == 3 || variant == 4 || variant == 7 || variant == 8 || variant == 10;
    }

    /**
     * ★ 2026-09-24 按用户要求「折耳根会更多使用暗影剑进行传送」：折耳根的技能冷却**单独给值**。
     *
     * <p>改前：调度处先给折耳根 {@code cd = 80 + rand(40)}，再被下面那条"近战位 ×2"的
     * {@link #skillCooldown} 拍到 **160~239 tick（8~12 秒）**一招 —— 加上这一招原本
     * 还只对召唤者生效（见 {@code EasterBossEntity.canShadowStrike}），实战里几乎看不到他闪。
     *
     * <p>改后：折耳根**不受 ×2 规则影响**，固定 {@value #ZHEERGEN_SKILL_COOLDOWN} tick
     * （{@value #ZHEERGEN_SKILL_COOLDOWN} / 20 秒）一次「暗影突袭」。
     * 终结难度的 {@code acceleratedTicks} 仍会把前摇减半（那是难度自己的规则，没动）。
     *
     * <p>要再调只改这一个常量。
     */
    public static final int ZHEERGEN_SKILL_COOLDOWN = 70;

    public static int skillCooldown(int variant, int original) {
        if (variant == EasterBossEntity.ZHEERGEN_VARIANT) return ZHEERGEN_SKILL_COOLDOWN;
        return prefersMelee(variant) ? original * 2 : original;
    }

    /**
     * 两次"主动施法"之间的间隔(tick)。
     *
     * <p>★ v176 按用户要求「近战 boss 减少远程法术使用」:
     * 近战位由 **160 → 320**(翻倍 = 施法密度减半), 远程位(朵拉 2 / 文玄蓝 6)保持 80 不变 ——
     * 它们是货真价实的施法者, 不该跟着降。
     *
     * <p>注意这**只改施法节奏**, 不改法术本身(伤害/范围/冷却都没动),
     * 也不影响 {@code skillCooldown}(那是技能) —— 用户要的是"少放远程法术", 不是"削弱法术"。
     */
    public static int spellInterval(int variant) { return prefersMelee(variant) ? 320 : 80; }

    /**
     * 单次施法最多同时放几个法术({@code SPELLS_PER_CAST} 是远程位的上限)。
     *
     * <p>★ v176: 近战位**每次只放一个**(2 → 1), 避免"一次读条甩两套法术"把远程压力堆起来;
     * 远程位维持 2。配合 {@link #spellInterval} 的翻倍, 近战位的远程法术存在感约降到原先的 1/4。
     */
    public static int spellsPerCast(int variant) { return prefersMelee(variant) ? 1 : SPELLS_PER_CAST; }

    /**
     * ★ v178 按用户要求「功底减少铁魔法施法, 多用技能」——<b>只作用于功底(variant 3)</b>的施法间隔倍数。
     *
     * <p>功底的三招轮转(E 无情铁手 / W 致残打击 / R 诺克萨斯断头台)和铁魔法共用同一套节奏:
     * 技能侧 {@code skillCd = spellInterval(3) / SPELLS_PER_CAST}, 法术侧
     * {@code READY = now + duration + spellInterval(3)} —— 两边都挂在「施法间隔」上。
     * 所以「少放法术」必须从施法间隔切, 否则法术会继续按 16 秒一轮挤进来。
     *
     * <p>1.8 倍 ⇒ 功底约每 29 秒才放一轮铁魔法(每轮仍 1 个法术), 其余时间交给三招轮转。
     * 其它近战位(柚子/蛋蛋/柠檬猫/折耳根/紫菜/雪黎)与远程位(朵拉/文玄蓝)不受影响。
     *
     * @return 施法间隔倍数(1.0 = 与 {@link #spellInterval} 完全一致)
     */
    public static double spellIntervalMultiplier(int variant) {
        return variant == EasterBossEntity.GONGDI_VARIANT ? GONGDI_SPELL_INTERVAL_MULTIPLIER : 1.0;
    }

    /**
     * 功底(variant 3)的物理技能冷却(tick)。
     *
     * <p>★ v178: 功底不再回退「大杀四方」, 三招轮转改由本值驱动 ——
     * 120 tick(6 秒)出手一次, 比原来跟着施法间隔走的 160 tick(8 秒)更快;
     * 单招自身冷却(致残打击 100 / 无情铁手 160 / 断头台 360)仍然生效, 所以不会刷屏。
     */
    public static int gongdiSkillCooldown() {
        return GONGDI_SKILL_COOLDOWN;
    }
}
