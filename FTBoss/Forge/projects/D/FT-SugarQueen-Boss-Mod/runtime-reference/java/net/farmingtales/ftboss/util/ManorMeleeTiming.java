/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class ManorMeleeTiming {
    public static final int DEFAULT_INTERVAL = 20;
    public static final int IMPACT_TICKS = 14;

    private ManorMeleeTiming() {
    }

    public static int recoveryTicks(int n, boolean bl) {
        int n2 = bl ? 7 : 14;
        return Math.max(1, Math.min(n, n2));
    }

    public static boolean canRestart(int n, int n2, boolean bl, boolean bl2) {
        return n2 <= 0 && !bl && (n <= 0 || bl2);
    }
}

