package net.farmingtales.ftboss.youzi.facing;

public final class FacingMath {
    private FacingMath() {
    }

    public static double wrap(double var0) {
        double var2 = var0 % 360.0;
        if (var2 >= 180.0) {
            var2 -= 360.0;
        }

        if (var2 < -180.0) {
            var2 += 360.0;
        }

        return var2;
    }

    public static float yaw(double var0, double var2, float var4) {
        return Double.isFinite(var0) && Double.isFinite(var2) && !(Math.hypot(var0, var2) < 1.0E-6)
            ? (float)wrap(Math.toDegrees(Math.atan2(var2, var0)) - 90.0)
            : var4;
    }

    public static float pitch(double var0, double var2, double var4, float var6) {
        return Double.isFinite(var0) && Double.isFinite(var2) && Double.isFinite(var4) && !(Math.hypot(Math.hypot(var0, var4), var2) < 1.0E-6)
            ? (float)(-Math.toDegrees(Math.atan2(var2, Math.hypot(var0, var4))))
            : var6;
    }

    public static double modelYaw(float var0, float var1, float var2, double var3) {
        if (Float.isFinite(var0) && Float.isFinite(var1) && Float.isFinite(var2)) {
            double var5 = Math.max(0.0, Math.min(1.0, (double)var2));
            return wrap(180.0 - ((double)var0 + wrap((double)(var1 - var0)) * var5));
        } else {
            return var3;
        }
    }
}
