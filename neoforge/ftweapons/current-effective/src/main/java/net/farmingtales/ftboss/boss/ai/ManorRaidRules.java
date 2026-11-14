package net.farmingtales.ftboss.boss.ai;

public final class ManorRaidRules {
    public static final double IMPOSSIBLE_HEALTH_MULTIPLIER = 4.0;
    public static final double PREVIOUS_LEADER_THREAT = 100.0;
    public static final int OTHER_DAMAGE = 0;
    public static final int PHYSICAL_DAMAGE = 1;
    public static final int MAGIC_DAMAGE = 2;
    public static final float ADAPTIVE_REDUCTION = 0.05F;

    private ManorRaidRules() {
    }

    public static double acceptedDamage(float before, float requestedAfter) {
        return Float.isFinite(before) && Float.isFinite(requestedAfter) && !(before <= 0.0F)
            ? Math.max(0.0, (double)before - Math.max(0.0, (double)requestedAfter))
            : 0.0;
    }

    public static double maximumHealth(double normalScaledHealth, boolean impossibleMainBoss) {
        return impossibleMainBoss && Double.isFinite(normalScaledHealth) && !(normalScaledHealth <= 0.0)
            ? Math.min(4.4942328371557893E307, normalScaledHealth) * 4.0
            : normalScaledHealth;
    }

    public static int dominantDamage(double physical, double magic) {
        if (Double.isFinite(physical) && Double.isFinite(magic) && !(physical < 0.0) && !(magic < 0.0) && physical != magic) {
            return physical > magic ? 1 : 2;
        } else {
            return 0;
        }
    }

    public static float adaptiveDamage(float damage, int dominant, int incoming, boolean supported, boolean bypass) {
        return supported && !bypass && dominant != 0 && dominant == incoming && Float.isFinite(damage) && !(damage <= 0.0F) ? damage * 0.95F : damage;
    }
}
