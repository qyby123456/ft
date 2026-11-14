/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class ZicaiCombatRules {
    private ZicaiCombatRules() {
    }

    public static int interval(int n, int n2) {
        return n2 == 8 ? Math.max(1, (int)Math.round((double)n / 1.5)) : n;
    }

    public static double reachSquared(double d, int n) {
        if (n != 8) {
            return d;
        }
        double d2 = Math.sqrt(d) + 1.0;
        return d2 * d2;
    }

    public static String stripFlavor(int n) {
        return n == 8 ? "\u6cb3\u8c5a\u4fb5\u8680" : "\u672b\u5f71\u6270\u52a8";
    }
}

