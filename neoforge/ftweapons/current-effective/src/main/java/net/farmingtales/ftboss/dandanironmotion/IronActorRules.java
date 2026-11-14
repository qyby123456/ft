package net.farmingtales.ftboss.dandanironmotion;

public final class IronActorRules {
    private IronActorRules() {
    }

    public static boolean autonomous(String var0) {
        return var0.equals("PULSE") || var0.equals("BEAM");
    }

    public static boolean stale(String var0, long var1, long var3, String var5, long var6, long var8) {
        return var1 > var6 || var1 == var6 && (var3 > var8 || var0.equals("RELEASE") && var5.equals("CHARGE"));
    }

    public static boolean expires(boolean var0, long var1, long var3, boolean var5, long var6, int var8, long var9) {
        return var0 ? var1 - var3 >= 7L : var1 >= var6 + (long)var8 || var5 && var1 - var9 > 8L;
    }

    public static boolean nativeHasPriority(long var0, int var2, long var3, double var5) {
        return var0 >= var3 && var2 > 0 && var5 >= (double)var0 && var5 < (double)(var0 + (long)var2);
    }
}
