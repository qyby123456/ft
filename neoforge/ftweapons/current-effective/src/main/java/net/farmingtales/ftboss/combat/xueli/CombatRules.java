package net.farmingtales.ftboss.combat.xueli;

public final class CombatRules {
    public static final String[] IDS = new String[]{
        "maid_guard",
        "guard_break",
        "guard_hit",
        "pan_charge",
        "pan_sweep",
        "roll_wake",
        "crush_lock",
        "slam_charge",
        "slam_impact",
        "clone_fuse",
        "clone_burst",
        "stun_haze"
    };
    public static final int SNAPSHOT_TICKS = 4;
    public static final int STATE_TTL = 12;
    public static final int MAX_GUARDS = 6;
    public static final int MAX_ACTIVE = 64;

    private CombatRules() {
    }

    public static boolean persistent(int var0) {
        return var0 == 0 || var0 == 5 || var0 == 9;
    }

    public static boolean charge(int var0) {
        return var0 == 3 || var0 == 7;
    }

    public static boolean fixedGround(int var0) {
        return var0 == 7 || var0 == 8 || var0 == 10;
    }

    public static double time(int var0, int var1, double var2, double var4) {
        if (charge(var0)) {
            return Math.max(0.0, Math.min(1.0, var4 / (double)var1));
        } else {
            return var0 == 9 ? Math.max(0.0, Math.min(5.0, var2 * 5.0 + var4 / 20.0)) : Math.max(0.0, var4 / 20.0);
        }
    }

    public static boolean fresh(long var0, long var2) {
        return var0 >= var2;
    }

    public static boolean valid(int var0, int var1, double var2, double var4, int var6) {
        return var0 >= 0
            && var0 < IDS.length
            && var1 > 0
            && var1 <= 200
            && Double.isFinite(var2)
            && var2 >= 0.1
            && var2 <= 32.0
            && Double.isFinite(var4)
            && var4 >= 0.0
            && var4 <= 1.0
            && var6 >= 0
            && var6 <= 6
            && (var0 != 6 && var0 != 11 || var6 == 1);
    }
}
