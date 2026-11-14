/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.qingyi.multiplayer;

public final class MultiplayerRules {
    public static final double BONUS = 0.5;

    private MultiplayerRules() {
    }

    public static boolean active(int n) {
        return n >= 2;
    }

    public static int interval(int n, int n2) {
        if (n <= 0) {
            return n;
        }
        return Math.max(1, (int)(((long)n * 2L + (long)Math.floorMod(n2, 3)) / 3L));
    }

    public static int remainder(int n, int n2) {
        return Math.floorMod((long)n * 2L + (long)Math.floorMod(n2, 3), 3);
    }
}

