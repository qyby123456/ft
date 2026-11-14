/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class ShadowDarknessRules {
    public static final int TEXTURE_WIDTH = 1024;
    public static final int TEXTURE_HEIGHT = 576;
    public static final int FADE_TICKS = 8;
    public static final int PERIOD_TICKS = 100;
    public static final int ANIMATION_PERIOD_TICKS = 400;

    private ShadowDarknessRules() {
    }

    public static boolean active(boolean bl, boolean bl2) {
        return bl || bl2;
    }

    public static float step(float f, boolean bl) {
        float f2 = Float.isFinite(f) ? Math.max(0.0f, Math.min(1.0f, f)) : 0.0f;
        return Math.max(0.0f, Math.min(1.0f, f2 + (bl ? 1.0f : -1.0f) / 8.0f));
    }

    public static float interpolate(float f, float f2, float f3) {
        if (!(Float.isFinite(f) && Float.isFinite(f2) && Float.isFinite(f3))) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, f + (f2 - f) * Math.max(0.0f, Math.min(1.0f, f3))));
    }

    public static float frameAlpha(double d) {
        return (float)(0.9 + 0.035 * ShadowDarknessRules.breathing(d));
    }

    public static float sigilAlpha(double d) {
        return (float)(0.42 + 0.16 * ShadowDarknessRules.breathing(d));
    }

    public static int tint(float f) {
        if (!Float.isFinite(f)) {
            return 0;
        }
        return Math.round(52.0f * Math.max(0.0f, Math.min(1.0f, f))) << 24 | 0x90411;
    }

    private static double breathing(double d) {
        return Double.isFinite(d) ? 0.5 + 0.5 * Math.sin(d % 100.0 * Math.PI * 2.0 / 100.0) : 0.0;
    }
}

