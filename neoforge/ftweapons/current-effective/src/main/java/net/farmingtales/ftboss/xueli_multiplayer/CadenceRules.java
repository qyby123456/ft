package net.farmingtales.ftboss.xueli_multiplayer;

public final class CadenceRules {
    public static final int MAX_PLAYERS = 6;

    private CadenceRules() {
    }

    public static boolean enabled(int var0, boolean var1, boolean var2, boolean var3, boolean var4, boolean var5, boolean var6, boolean var7) {
        return var0 == 10 && !var1 && !var2 && !var3 && !var4 && var5 && !var6 && !var7;
    }

    public static boolean participant(boolean var0, boolean var1, boolean var2, boolean var3, double var4, double var6) {
        return var0 && !var1 && !var2 && var3 && Double.isFinite(var4) && var4 >= 0.0 && var4 <= var6;
    }

    public static int interval(int var0, int var1, int var2) {
        if (var0 > 0 && var1 > 1) {
            int var3 = Math.min(6, var1);
            int var4 = (int)(((long)var0 * 5L + (long)var3 + 3L) / ((long)var3 + 4L));
            return Math.min(var0, Math.max(Math.min(var0, Math.max(1, var2)), var4));
        } else {
            return var0;
        }
    }

    public static long deadline(long var0, long var2, int var4, int var5, int var6) {
        if (var5 > 1 && var4 >= 0 && var0 > var2 && var2 <= Long.MAX_VALUE - (long)var4) {
            long var7 = var2 + (long)var4;
            return var0 > var7 && var0 - var7 >= 0L && var0 - var7 <= 2147483647L ? var7 + (long)interval((int)(var0 - var7), var5, var6) : var0;
        } else {
            return var0;
        }
    }
}
