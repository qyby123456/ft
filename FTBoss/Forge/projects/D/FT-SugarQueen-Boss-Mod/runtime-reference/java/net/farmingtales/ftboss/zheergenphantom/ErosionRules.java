/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.zheergenphantom;

public final class ErosionRules {
    private ErosionRules() {
    }

    public static float damage(int n) {
        return 4 * (Math.max(0, Math.min(4, n)) + 1);
    }

    public static float unscaledDamage(float f) {
        return Float.isFinite(f) ? Math.max(4.0f, Math.min(20.0f, f)) : 4.0f;
    }

    public static float strength(int n) {
        return switch (Math.max(0, Math.min(5, n))) {
            case 1 -> 0.45f;
            case 2 -> 0.62f;
            case 3 -> 0.78f;
            case 4 -> 0.91f;
            case 5 -> 1.0f;
            default -> 0.0f;
        };
    }

    public static float step(float f, float f2) {
        float f3 = Float.isFinite(f) ? Math.max(0.0f, Math.min(1.0f, f)) : 0.0f;
        float f4 = Float.isFinite(f2) ? Math.max(0.0f, Math.min(1.0f, f2)) : 0.0f;
        return f3 + Math.max(-0.1f, Math.min(0.1f, f4 - f3));
    }

    private static double breathing(double d) {
        return Double.isFinite(d) ? 0.5 + 0.5 * Math.sin(d % 100.0 * Math.PI * 2.0 / 100.0) : 0.0;
    }

    public static float frameAlpha(double d) {
        return (float)(0.985 + 0.01 * ErosionRules.breathing(d));
    }

    public static float sigilAlpha(double d) {
        return (float)(0.12 + 0.04 * ErosionRules.breathing(d));
    }

    public static int tint(float f) {
        return Math.round(242.0f * (Float.isFinite(f) ? Math.max(0.0f, Math.min(1.0f, f)) : 0.0f)) << 24 | 0x10103;
    }
}

