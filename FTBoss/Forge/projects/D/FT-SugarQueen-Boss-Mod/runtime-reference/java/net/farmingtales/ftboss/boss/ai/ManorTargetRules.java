/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.boss.ai;

public final class ManorTargetRules {
    public static final double LOW_AIRBORNE_HEIGHT = 2.0;

    private ManorTargetRules() {
    }

    public static boolean hybrid(int n) {
        return n == 0 || n == 1 || n == 4 || n == 5 || n == 7;
    }

    public static boolean preferRanged(boolean bl, boolean bl2, int n, boolean bl3, boolean bl4, double d, double d2, double d3) {
        if (!bl || bl2 || !ManorTargetRules.hybrid(n) || bl3 || bl4 || !Double.isFinite(d) || d < 0.0 || !Double.isFinite(d2) || !Double.isFinite(d3) || d3 < 0.0) {
            return bl;
        }
        return d > d3 && Math.abs(d2) > 2.0;
    }
}

