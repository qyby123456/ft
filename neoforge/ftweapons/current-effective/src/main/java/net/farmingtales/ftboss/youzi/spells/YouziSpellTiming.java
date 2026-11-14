package net.farmingtales.ftboss.youzi.spells;

public final class YouziSpellTiming {
    public static boolean eventFresh(long var0, long var2, String var4) {
        long var5 = var0 - var2;
        return var5 >= -2L && var5 <= (long)(var4.equals("BEAM") ? 4 : (var4.equals("CHARGE") ? 8 : 100));
    }

    public static boolean ordered(long var0, String var2, long var3, String var5) {
        return var0 != var3 ? var0 > var3 : order(var2) > order(var5);
    }

    private static int order(String var0) {
        return var0.equals("CHARGE") ? 0 : (var0.equals("RELEASE") ? 1 : (var0.equals("PULSE") ? 2 : 3));
    }

    public static boolean expired(String var0, String var1, long var2, long var4, long var6, boolean var8, boolean var9, double var10) {
        if (var8 && var2 >= var4) {
            if (var1.equals("CHARGE")) {
                return var2 - var6 > 8L;
            }

            if (!var0.equals("effigy_ward")) {
                return var2 - var4 >= (long)(var0.equals("effigy_volley") ? 40 : 60) || var10 > 48.0;
            }

            if (var2 - var4 <= 370L) {
                return var2 - var4 > 4L && !var9;
            }
        }

        return true;
    }

    public static YouziSpellTiming.Sample sample(String var0, boolean var1, double var2, double var4, double var6, double var8) {
        if (var1) {
            return new YouziSpellTiming.Sample("charge", Math.max(0.0, Math.min(1.0, var4)) * var6, 1.0);
        } else {
            double var10 = var0.equals("effigy_volley") ? 1.0417 : 0.0;
            String var12 = var2 + var10 < var8 ? "release" : "sustain";
            double var13 = var12.equals("release") ? var2 + var10 : var2 - var8 + var10;
            double var15 = ((double)(var0.equals("effigy_volley") ? 40 : 60) - var2 * 20.0) / 8.0;
            return new YouziSpellTiming.Sample(var12, Math.max(0.0, var13), var0.equals("effigy_ward") ? 1.0 : YouziVisibility.smooth(var15));
        }
    }

    public static record Sample(String channel, double seconds, double opacity) {
    }
}
