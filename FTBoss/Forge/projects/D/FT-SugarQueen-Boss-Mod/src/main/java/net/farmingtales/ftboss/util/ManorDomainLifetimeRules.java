package net.farmingtales.ftboss.util;

public final class ManorDomainLifetimeRules {
    public static final int DURATION_TICKS = 5 * 60 * 20;
    public static final float RECOVERY_FRACTION = 0.20f;

    private ManorDomainLifetimeRules() {}

    public static boolean expired(long started, long now) {
        return now >= started && now - started >= DURATION_TICKS;
    }

    public static float recoveredHealth(float health, float maximum) {
        if (!Float.isFinite(health) || !Float.isFinite(maximum) || health <= 0.0f || maximum <= 0.0f) return health;
        return Math.min(maximum, health + maximum * RECOVERY_FRACTION);
    }
}
