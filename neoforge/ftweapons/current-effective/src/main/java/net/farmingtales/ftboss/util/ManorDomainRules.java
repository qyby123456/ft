package net.farmingtales.ftboss.util;

import java.util.Locale;

public final class ManorDomainRules {
    public static final double RADIUS = 76.0;
    public static final int REFRESH_TICKS = 20;

    private ManorDomainRules() {
    }

    public static boolean supports(int var0) {
        return var0 >= 0 && var0 <= 11 && var0 != 9;
    }

    public static boolean contains(double var0, double var2, double var4) {
        return Double.isFinite(var0) && Double.isFinite(var2) && Double.isFinite(var4) && var0 * var0 + var4 * var4 <= 5776.0;
    }

    public static double outgoing(int var0, boolean var1, double var2, double var4, boolean var6, int var7) {
        return switch (var0) {
            case 1, 5 -> var1 ? 1.18 : 1.0;
            case 2 -> var1 && var2 >= 6.0 ? 1.22 : 1.0;
            case 3 -> !var1 && var4 < 0.6 ? 1.25 : 1.0;
            default -> 1.0;
            case 6 -> var1 ? 1.2 : 1.0;
            case 7 -> !var1 && var6 ? 1.2 : 1.0;
            case 11 -> var1 && var7 % 3 == 0 ? 1.35 : 1.0;
        };
    }

    public static boolean rangedHit(int var0, boolean var1, String var2, String var3, double var4) {
        String var6 = var2.toLowerCase(Locale.ROOT);
        return var1 || var6.contains("magic") || var6.contains("spell") || var3.startsWith("io.redspace.ironsspellbooks.damage.") || var0 == 2 && var4 >= 6.0;
    }

    public static double armor(int var0) {
        return var0 == 0 ? 4.0 : (var0 == 8 ? 2.0 : (var0 == 10 ? 3.0 : 0.0));
    }

    public static double speed(int var0) {
        return var0 == 7 ? 0.12 : 0.0;
    }

    public static double knockbackResistance(int var0) {
        return var0 == 6 ? 0.35 : (var0 == 10 ? 0.2 : 0.0);
    }

    public static double attack(int var0) {
        return var0 == 4 ? 0.12 : 0.0;
    }
}
