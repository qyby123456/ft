package net.farmingtales.ftboss.util;

public final class ShadowDarknessRules {
    public static final int TEXTURE_WIDTH = 1024;
    public static final int TEXTURE_HEIGHT = 576;
    public static final int FADE_TICKS = 8;
    public static final int PERIOD_TICKS = 100;
    public static final int ANIMATION_PERIOD_TICKS = 400;

    private ShadowDarknessRules() {}

    public static boolean active(boolean shadowBlind, boolean shadowErosion) {
        return shadowBlind || shadowErosion;
    }

    public static float step(float amount, boolean active) {
        float current = Float.isFinite(amount) ? Math.max(0, Math.min(1, amount)) : 0;
        return Math.max(0, Math.min(1, current + (active ? 1F : -1F) / FADE_TICKS));
    }

    public static float interpolate(float previous, float current, float partialTick) {
        if (!Float.isFinite(previous) || !Float.isFinite(current) || !Float.isFinite(partialTick)) return 0;
        return Math.max(0, Math.min(1, previous + (current - previous) * Math.max(0, Math.min(1, partialTick))));
    }

    public static float frameAlpha(double ticks) {
        return (float) (0.9 + 0.035 * breathing(ticks));
    }

    public static float sigilAlpha(double ticks) {
        return (float) (0.42 + 0.16 * breathing(ticks));
    }

    public static int tint(float amount) {
        if (!Float.isFinite(amount)) return 0;
        return Math.round(52 * Math.max(0, Math.min(1, amount))) << 24 | 0x090411;
    }

    private static double breathing(double ticks) {
        return Double.isFinite(ticks) ? 0.5 + 0.5 * Math.sin(ticks % PERIOD_TICKS * Math.PI * 2 / PERIOD_TICKS) : 0;
    }
}
