/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class MaqueFlightRules {
    public static final int COOLDOWN_TICKS = 160;
    public static final int INITIAL_DELAY = 40;
    public static final int CANCEL_RETRY = 40;
    public static final int HEAL_BAN_TICKS = 100;
    public static final int MAX_ACTIVE = 64;
    public static final double OWNER_RANGE = 32.0;
    public static final double TARGET_RANGE = 24.0;

    private MaqueFlightRules() {
    }

    public static boolean canStart(long l, long l2, int n, boolean bl) {
        return l >= l2 && n < 64 && !bl;
    }

    public static int castState(long l, int n, boolean bl, boolean bl2) {
        if (!bl || !bl2 || n < 1 || n > 200 || l < 0L || l > (long)n + 20L) {
            return -1;
        }
        return l < (long)n ? 0 : 1;
    }

    public static boolean refreshBan(int n, boolean bl) {
        return !bl && n < 100;
    }
}

