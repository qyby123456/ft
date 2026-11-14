package net.farmingtales.ftboss.zheergenfx;

public final class LightClock {
    private LightClock() {
    }

    public static boolean alive(double var0, int var2) {
        return Double.isFinite(var0) && var0 >= 0.0 && var2 > 0 && var0 < (double)var2;
    }

    public static double progress(double var0, int var2) {
        return Math.max(0.0, Math.min(1.0, var0 / (double)Math.max(1, var2)));
    }

    public static int variant(int var0, int var1) {
        return var0 == 3 ? Math.max(0, Math.min(2, var1)) : (var0 == 10 ? Math.max(0, Math.min(5, var1)) : (var0 == 1 ? var1 % 2 : 0));
    }

    public static boolean charge(int var0) {
        return var0 == 0 || var0 == 2 || var0 == 4;
    }

    public static int family(int var0) {
        return var0 <= 1 ? 0 : (var0 <= 3 ? 2 : (var0 <= 5 ? 4 : var0));
    }
}
