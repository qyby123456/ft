/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class GourmetBladeStreamMotion {
    public static final int LIFE_TICKS = 8;
    public static final float FIRST_PERSON_ROLL_DEGREES = 32.0f;
    public static final float WORLD_LOCAL_YAW_DEGREES = 180.0f;

    private GourmetBladeStreamMotion() {
    }

    public static float opacity(float f, float f2) {
        if (!Float.isFinite(f) || !Float.isFinite(f2) || f <= 0.0f || f >= 8.0f) {
            return 0.0f;
        }
        if (f2 < 0.0f || f2 > 1.0f) {
            return 0.0f;
        }
        float f3 = 1.0f - f2;
        float f4 = GourmetBladeStreamMotion.clamp((f - 0.15f) / 3.25f);
        float f5 = GourmetBladeStreamMotion.clamp((f - 2.0f) / 6.0f);
        float f6 = GourmetBladeStreamMotion.clamp(f / 0.75f);
        float f7 = GourmetBladeStreamMotion.clamp((8.0f - f) / 2.5f);
        return f6 * f7 * GourmetBladeStreamMotion.clamp((f4 - f3) / 0.12f) * GourmetBladeStreamMotion.clamp((f3 - f5) / 0.22f);
    }

    private static float clamp(float f) {
        return Math.max(0.0f, Math.min(1.0f, f));
    }
}

