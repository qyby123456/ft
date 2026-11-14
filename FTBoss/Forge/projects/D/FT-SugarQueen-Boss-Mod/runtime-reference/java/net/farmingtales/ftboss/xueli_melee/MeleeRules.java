/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.xueli_melee;

public final class MeleeRules {
    public static final double MELEE_REACH_SQUARED = 9.0;
    public static final double LOW_AIRBORNE_HEIGHT = 2.0;

    private MeleeRules() {
    }

    public static boolean preferRanged(boolean bl, boolean bl2, int n, boolean bl3, boolean bl4, double d, double d2) {
        if (!bl || bl2 || n != 10 || bl3 || bl4 || !Double.isFinite(d) || d < 0.0 || !Double.isFinite(d2)) {
            return bl;
        }
        return d > 9.0 && d2 > 2.0;
    }
}

