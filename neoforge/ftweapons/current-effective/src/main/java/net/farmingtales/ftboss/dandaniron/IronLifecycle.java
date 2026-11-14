package net.farmingtales.ftboss.dandaniron;

public final class IronLifecycle {
    private IronLifecycle() {
    }

    public static boolean stale(String var0, long var1, long var3, String var5, long var6, long var8) {
        return var0.equals(var5) && var3 > var8 || !var0.equals("CHARGE") && !var5.equals("CHARGE") && var1 > var6;
    }

    public static boolean replaces(String var0, long var1, String var3, long var4) {
        if (var0.equals(var3)) {
            return true;
        } else {
            return var3.equals("RELEASE") ? !var0.equals("BEAM") && var1 <= var4 : var3.equals("PULSE") && var0.equals("RELEASE") && var1 <= var4;
        }
    }

    public static boolean cancels(String var0, long var1, long var3) {
        return var0.equals("CHARGE") && var1 <= var3;
    }
}
