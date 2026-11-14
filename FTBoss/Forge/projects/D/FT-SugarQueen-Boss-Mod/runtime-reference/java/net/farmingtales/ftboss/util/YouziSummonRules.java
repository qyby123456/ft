/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class YouziSummonRules {
    public static final int BASE_CAP = 6;
    public static final int BASE_COUNT = 2;
    public static final double PLAYER_RANGE = 64.0;

    private YouziSummonRules() {
    }

    public static boolean countsPlayer(boolean bl, boolean bl2, boolean bl3, boolean bl4, double d) {
        return bl && !bl2 && !bl3 && !bl4 && Double.isFinite(d) && d >= 0.0 && d <= 4096.0;
    }

    public static int cap(int n) {
        long l = Math.max(0L, (long)n - 1L);
        return (int)Math.min(Integer.MAX_VALUE, 6L + l * 2L);
    }

    public static int count(int n) {
        long l = Math.max(0L, (long)n - 1L);
        return (int)Math.min(Integer.MAX_VALUE, 2L + l);
    }

    public static int remainingCount(int n, int n2) {
        return Math.min(YouziSummonRules.count(n), Math.max(0, YouziSummonRules.cap(n) - Math.max(0, n2)));
    }
}

