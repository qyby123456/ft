package net.farmingtales.ftboss.util;

public final class ManorWeaponRules {
    public static final int CLEAVER_IMPACT_TICKS = 9;
    public static final int CLEAVER_COOLDOWN_TICKS = 80;
    public static final float CLEAVER_DAMAGE = 32.0F;
    public static final float CLEAVER_RAGE_DAMAGE = 80.0F;
    public static final double CLEAVER_RADIUS = 3.0;
    public static final double CLEAVER_RAGE_RADIUS = 4.0;

    /**
     * 庄园武器「主动技能」伤害与直接治疗的统一倍率（2026-09-18）。
     *
     * <p>用户要求：<b>只加强技能，主手基础攻击不动</b>，目录内 16 件一致处理，黑切除外。
     * 因此这里只作用于"右键 / 蓄力 / J 技能结算出来的那一份伤害与治疗"，
     * <b>不</b>参与 {@link #attackBonus(double)} 的主手面板，也<b>不</b>作用于持续时间、状态等级、
     * 冷却、范围与耐久。
     *
     * <p>0.5 的加量都落在这个常量上：想再调幅度只改这一个数字，
     * 所有 {@link #skillDamage(float)} / {@link #skillHeal(float)} 的调用点自动跟随。
     */
    public static final float SKILL_DAMAGE_MULTIPLIER = 1.5F;

    /**
     * 青衣四把 TnS 三叉戟「投出去」那一下的伤害倍率。
     *
     * <p>它们的外部实现不在本模组里，改不了基础值，所以 2026-09-17 的庄园强化是在
     * {@code ManorWeaponEnhancements.tridentDamage} 里把投掷命中乘了一次 <b>2.0</b>。
     * 2026-09-18 的"技能 +50%"同样只加在投掷段上（近战那一下属于主手基础攻击，保持 2.0 不动），
     * 因此这里是 {@code 2.0 × }{@link #SKILL_DAMAGE_MULTIPLIER}。
     */
    public static final float TRIDENT_THROW_MULTIPLIER = 3.0F;

    private ManorWeaponRules() {}

    public static double attackBonus(double originalBonus) {
        return originalBonus + 1.0;
    }

    /** 庄园武器主动技能伤害：基础值 × {@link #SKILL_DAMAGE_MULTIPLIER}。 */
    public static float skillDamage(float base) {
        return base * SKILL_DAMAGE_MULTIPLIER;
    }

    /** 庄园武器主动技能的固定追加伤害（月印、烛痕、玫印这类触发式加值）。 */
    public static float skillBonusDamage(float base) {
        return skillDamage(base);
    }

    /** 庄园武器主动技能的直接治疗：基础值 × {@link #SKILL_DAMAGE_MULTIPLIER}。 */
    public static float skillHeal(float base) {
        return skillDamage(base);
    }

    public static float cleaverDamage(int stacks) {
        return stacks >= 5 ? CLEAVER_RAGE_DAMAGE : CLEAVER_DAMAGE;
    }

    public static double cleaverRadius(int stacks) {
        return stacks >= 5 ? CLEAVER_RAGE_RADIUS : CLEAVER_RADIUS;
    }

    public static boolean insideSlam(double offsetX, double offsetY, double offsetZ, int stacks) {
        double radius = cleaverRadius(stacks);
        return Double.isFinite(offsetX) && Double.isFinite(offsetY) && Double.isFinite(offsetZ)
                && Math.abs(offsetY) <= 2.5 && offsetX * offsetX + offsetZ * offsetZ <= radius * radius;
    }
}
