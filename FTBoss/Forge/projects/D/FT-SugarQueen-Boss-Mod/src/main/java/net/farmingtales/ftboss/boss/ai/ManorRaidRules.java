package net.farmingtales.ftboss.boss.ai;

/** 庄园多人贡献与终结生命纯规则：实际扣血计分，下一轮奖励100初始仇恨，终结生命×10。 */
public final class ManorRaidRules {
    public static final double IMPOSSIBLE_HEALTH_MULTIPLIER = net.farmingtales.ftboss.boss.ManorDifficulty.IMPOSSIBLE_ATTRIBUTE_MULTIPLIER;
    public static final double PREVIOUS_LEADER_THREAT = 100.0D;
    public static final int OTHER_DAMAGE = 0;
    public static final int PHYSICAL_DAMAGE = 1;
    public static final int MAGIC_DAMAGE = 2;
    public static final float ADAPTIVE_REDUCTION = 0.10F;

    private ManorRaidRules() {}

    public static double acceptedDamage(float before, float requestedAfter) {
        if (!Float.isFinite(before) || !Float.isFinite(requestedAfter) || before <= 0.0F) return 0.0D;
        return Math.max(0.0D, before - Math.max(0.0D, requestedAfter));
    }

    public static double maximumHealth(double normalScaledHealth, boolean impossibleMainBoss) {
        if (!impossibleMainBoss || !Double.isFinite(normalScaledHealth) || normalScaledHealth <= 0.0D) {
            return normalScaledHealth;
        }
        return Math.min(Double.MAX_VALUE / IMPOSSIBLE_HEALTH_MULTIPLIER, normalScaledHealth)
                * IMPOSSIBLE_HEALTH_MULTIPLIER;
    }

    public static int dominantDamage(double physical, double magic) {
        if (!Double.isFinite(physical) || !Double.isFinite(magic) || physical < 0 || magic < 0 || physical == magic) return OTHER_DAMAGE;
        return physical > magic ? PHYSICAL_DAMAGE : MAGIC_DAMAGE;
    }

    public static float adaptiveDamage(float damage, int dominant, int incoming, boolean supported, boolean bypass) {
        if (!supported || bypass || dominant == OTHER_DAMAGE || dominant != incoming || !Float.isFinite(damage) || damage <= 0) return damage;
        return damage * (1.0F - ADAPTIVE_REDUCTION);
    }
}
