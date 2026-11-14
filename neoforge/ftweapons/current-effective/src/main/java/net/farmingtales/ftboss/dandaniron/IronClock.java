package net.farmingtales.ftboss.dandaniron;

public final class IronClock {
    private IronClock() {
    }

    public static double progress(double var0, long var2, int var4) {
        return Math.max(0.0, Math.min(1.0, (var0 - (double)var2) / (double)Math.max(1, var4)));
    }

    public static double clock(boolean var0, double var1, long var3, int var5) {
        double var6 = progress(var1, var3, var5);
        return var0 ? 15.0 * var6 : 15.0 + 24.166 * var6;
    }

    public static double opacity(boolean var0, double var1, long var3, int var5, long var6) {
        if (var1 < (double)var3 || var1 - (double)var6 > 8.0 && var0 || var1 >= (double)(var3 + (long)var5)) {
            return 0.0;
        } else {
            return var0 ? 0.72 + 0.28 * smooth(progress(var1, var3, var5)) : 1.0 - smooth((progress(var1, var3, var5) - 0.7) / 0.3);
        }
    }

    public static double smooth(double var0) {
        var0 = Math.max(0.0, Math.min(1.0, var0));
        return var0 * var0 * (3.0 - 2.0 * var0);
    }

    public static int displayTicks(String var0, String var1, int var2) {
        return Math.max(1, var1.equals("BEAM") ? Math.min(var2, 8) : (var0.equals("candle_reap") ? var2 / 2 : var2));
    }

    public static double personOpacity(boolean var0, boolean var1) {
        return !var0 && var1 ? 0.14 : 1.0;
    }
}
