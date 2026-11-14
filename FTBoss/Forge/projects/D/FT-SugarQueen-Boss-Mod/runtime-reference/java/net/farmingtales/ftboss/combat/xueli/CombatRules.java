/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.combat.xueli;

public final class CombatRules {
    public static final String[] IDS = new String[]{"maid_guard", "guard_break", "guard_hit", "pan_charge", "pan_sweep", "roll_wake", "crush_lock", "slam_charge", "slam_impact", "clone_fuse", "clone_burst", "stun_haze"};
    public static final int SNAPSHOT_TICKS = 4;
    public static final int STATE_TTL = 12;
    public static final int MAX_GUARDS = 6;
    public static final int MAX_ACTIVE = 64;

    private CombatRules() {
    }

    public static boolean persistent(int n) {
        return n == 0 || n == 5 || n == 9;
    }

    public static boolean charge(int n) {
        return n == 3 || n == 7;
    }

    public static boolean fixedGround(int n) {
        return n == 7 || n == 8 || n == 10;
    }

    public static double time(int n, int n2, double d, double d2) {
        if (CombatRules.charge(n)) {
            return Math.max(0.0, Math.min(1.0, d2 / (double)n2));
        }
        if (n == 9) {
            return Math.max(0.0, Math.min(5.0, d * 5.0 + d2 / 20.0));
        }
        return Math.max(0.0, d2 / 20.0);
    }

    public static boolean fresh(long l, long l2) {
        return l >= l2;
    }

    public static boolean valid(int n, int n2, double d, double d2, int n3) {
        return n >= 0 && n < IDS.length && n2 > 0 && n2 <= 200 && Double.isFinite(d) && d >= 0.1 && d <= 32.0 && Double.isFinite(d2) && d2 >= 0.0 && d2 <= 1.0 && n3 >= 0 && n3 <= 6 && (n != 6 && n != 11 || n3 == 1);
    }
}

