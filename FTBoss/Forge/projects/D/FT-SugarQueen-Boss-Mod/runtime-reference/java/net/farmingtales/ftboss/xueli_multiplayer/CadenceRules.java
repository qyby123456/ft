/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.xueli_multiplayer;

public final class CadenceRules {
    public static final int MAX_PLAYERS = 6;

    private CadenceRules() {
    }

    public static boolean enabled(int n, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, boolean bl7) {
        return n == 10 && !bl && !bl2 && !bl3 && !bl4 && bl5 && !bl6 && !bl7;
    }

    public static boolean participant(boolean bl, boolean bl2, boolean bl3, boolean bl4, double d, double d2) {
        return bl && !bl2 && !bl3 && bl4 && Double.isFinite(d) && d >= 0.0 && d <= d2;
    }

    public static int interval(int n, int n2, int n3) {
        if (n <= 0 || n2 <= 1) {
            return n;
        }
        int n4 = Math.min(6, n2);
        int n5 = (int)(((long)n * 5L + (long)n4 + 3L) / ((long)n4 + 4L));
        return Math.min(n, Math.max(Math.min(n, Math.max(1, n3)), n5));
    }

    public static long deadline(long l, long l2, int n, int n2, int n3) {
        if (n2 <= 1 || n < 0 || l <= l2 || l2 > Long.MAX_VALUE - (long)n) {
            return l;
        }
        long l3 = l2 + (long)n;
        if (l <= l3 || l - l3 < 0L || l - l3 > Integer.MAX_VALUE) {
            return l;
        }
        return l3 + (long)CadenceRules.interval((int)(l - l3), n2, n3);
    }
}

