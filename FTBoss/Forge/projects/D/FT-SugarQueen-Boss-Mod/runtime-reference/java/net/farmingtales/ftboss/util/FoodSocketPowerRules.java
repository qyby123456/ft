/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class FoodSocketPowerRules {
    public static final int FLIGHT_DURATION_TICKS = 60;
    public static final int FLIGHT_COOLDOWN_TICKS = 600;
    public static final int PACK_DURATION_TICKS = 600;
    public static final int PACK_COOLDOWN_TICKS = 400;
    public static final int PACK_SIZE = 2;

    private FoodSocketPowerRules() {
    }

    public static boolean flight(int n, int n2) {
        return n >= 2 && n2 >= 1;
    }

    public static boolean pack(int n, int n2) {
        return n >= 2 && n2 >= 1;
    }

    public static int setBonus(int n, int n2) {
        return n >= 3 && n2 >= 1 ? 1 : 0;
    }

    public static boolean ready(long l, long l2, int n) {
        return l2 <= l || l2 > l && l2 - l > (long)n;
    }

    public static boolean active(long l, long l2, int n) {
        return l2 > l && l2 - l <= (long)n;
    }
}

