package net.farmingtales.ftboss.util;

/** 苹果自爆光效：2026-10-12，用短寿命细光环替代密集爆炸粒子，不参与伤害结算。 */
public final class AppleBlastLightRules {
    public static final int LIFE_TICKS = 10;
    public static final int CORE_TICKS = 2;
    public static final int SEGMENTS = 32;
    public static final int MAX_ACTIVE = 24;
    public static final double VIEW_DISTANCE = 64.0;

    private AppleBlastLightRules() {}

    public static boolean valid(double x, double y, double z, double radius) {
        return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z) && Double.isFinite(radius)
                && Math.abs(x) <= 30_000_000 && Math.abs(z) <= 30_000_000 && Math.abs(y) <= 4096
                && radius >= 3.5 && radius <= 7.0;
    }

    public static float fade(float age) {
        if (!Float.isFinite(age) || age < 0 || age >= LIFE_TICKS) return 0;
        float remaining = 1.0F - age / LIFE_TICKS;
        return remaining * remaining;
    }

    public static double ringRadius(double radius, float age) {
        double progress = Math.max(0, Math.min(1, age / LIFE_TICKS));
        return 0.35 + (radius - 0.35) * (1 - Math.pow(1 - progress, 2));
    }
}
