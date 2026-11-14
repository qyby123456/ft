package net.farmingtales.ftboss.util;

/** 朵拉玩家武器数值与几何规则；2026-09-17，独立于 Bren/TnS，蓄力、距离和冷却均由服务端执行。 */
public final class DoraWeaponRules {
    public static final int MIN_CHARGE_TICKS = 12;
    public static final int FULL_CHARGE_TICKS = 24;
    public static final double GUN_RANGE = 32;
    public static final double NOVA_RADIUS = 2.75;
    public static final double THRUST_RANGE = 6.5;
    public static final double SWEEP_RANGE = 4.5;

    /**
     * 地脉脉冲枪（dora_pulse_gun）的伤害倍率 —— <b>2026-09-18 用户口径：「×3」</b>。
     *
     * <p>与 {@link ManorWeaponRules#SKILL_DAMAGE_MULTIPLIER}（1.5）是<b>替换</b>关系不是叠乘：
     * 这里给的是相对最初版本的总倍率。原来那轮 ×1.5 已被 ×3 覆盖。
     *
     * <p>鸣脉冲锋枪（dora_nova_launcher）的倍率不在这里 —— 它是连发武器，
     * 数值归 {@link DoraSmgRules#DAMAGE_MULTIPLIER} 管。
     */
    public static final float PULSE_GUN_DAMAGE_MULTIPLIER = 6.0F;

    public enum Mode {
        PULSE(32, 50, 2), NOVA(28, 120, 4), THRUST(30, 50, 2), SWEEP(24, 90, 3);

        /** 技能基础伤害（最初版本的原始值）；实际结算与显示都走 {@link #damage()}。 */
        public final float damage;
        public final int cooldownTicks;
        public final int durabilityCost;

        Mode(float damage, int cooldownTicks, int durabilityCost) {
            this.damage = damage;
            this.cooldownTicks = cooldownTicks;
            this.durabilityCost = durabilityCost;
        }

        /** 本次技能的实际基础伤害（脉冲枪倍率已计入，再乘蓄力系数）。 */
        public float damage() {
            return damage * PULSE_GUN_DAMAGE_MULTIPLIER;
        }
    }

    private DoraWeaponRules() {}

    public static Mode mode(boolean lance, boolean sneaking) {
        return lance ? (sneaking ? Mode.SWEEP : Mode.THRUST) : (sneaking ? Mode.NOVA : Mode.PULSE);
    }

    public static double charge(int ticks) {
        if (ticks < MIN_CHARGE_TICKS) return 0;
        return 0.5 + Math.min(1, (ticks - MIN_CHARGE_TICKS) / (double) (FULL_CHARGE_TICKS - MIN_CHARGE_TICKS)) * 0.5;
    }

    public static boolean inThrust(double forward, double sidewaysSquared) {
        return Double.isFinite(forward) && Double.isFinite(sidewaysSquared)
                && forward >= 0 && forward <= THRUST_RANGE && sidewaysSquared >= 0 && sidewaysSquared <= 0.85 * 0.85;
    }

    public static boolean inSweep(double distanceSquared, double facingDot) {
        return Double.isFinite(distanceSquared) && Double.isFinite(facingDot)
                && distanceSquared >= 0 && distanceSquared <= SWEEP_RANGE * SWEEP_RANGE && facingDot >= 0.25;
    }
}
