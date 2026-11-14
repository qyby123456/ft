/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.youzi.facing;

public final class FacingMath {
    private FacingMath() {
    }

    public static double wrap(double d) {
        double d2 = d % 360.0;
        if (d2 >= 180.0) {
            d2 -= 360.0;
        }
        if (d2 < -180.0) {
            d2 += 360.0;
        }
        return d2;
    }

    public static float yaw(double d, double d2, float f) {
        if (!Double.isFinite(d) || !Double.isFinite(d2) || Math.hypot(d, d2) < 1.0E-6) {
            return f;
        }
        return (float)FacingMath.wrap(Math.toDegrees(Math.atan2(d2, d)) - 90.0);
    }

    public static float pitch(double d, double d2, double d3, float f) {
        if (!Double.isFinite(d) || !Double.isFinite(d2) || !Double.isFinite(d3) || Math.hypot(Math.hypot(d, d3), d2) < 1.0E-6) {
            return f;
        }
        return (float)(-Math.toDegrees(Math.atan2(d2, Math.hypot(d, d3))));
    }

    public static double modelYaw(float f, float f2, float f3, double d) {
        if (!(Float.isFinite(f) && Float.isFinite(f2) && Float.isFinite(f3))) {
            return d;
        }
        double d2 = Math.max(0.0, Math.min(1.0, (double)f3));
        return FacingMath.wrap(180.0 - ((double)f + FacingMath.wrap(f2 - f) * d2));
    }
}

