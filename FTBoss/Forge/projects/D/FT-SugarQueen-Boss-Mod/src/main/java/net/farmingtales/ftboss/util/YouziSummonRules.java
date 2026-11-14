package net.farmingtales.ftboss.util;

public final class YouziSummonRules {
    public static final int BASE_CAP = 6;
    public static final int BASE_COUNT = 2;
    public static final double PLAYER_RANGE = 64.0;

    private YouziSummonRules() {}

    public static boolean countsPlayer(boolean alive, boolean creative, boolean spectator, boolean removed, double distanceSquared) {
        return alive && !creative && !spectator && !removed && Double.isFinite(distanceSquared)
                && distanceSquared >= 0 && distanceSquared <= PLAYER_RANGE * PLAYER_RANGE;
    }

    public static int cap(int players) {
        long additional = Math.max(0L, (long) players - 1);
        return (int) Math.min(Integer.MAX_VALUE, BASE_CAP + additional * 2);
    }

    public static int count(int players) {
        long additional = Math.max(0L, (long) players - 1);
        return (int) Math.min(Integer.MAX_VALUE, BASE_COUNT + additional);
    }

    public static int remainingCount(int players, int alive) {
        return Math.min(count(players), Math.max(0, cap(players) - Math.max(0, alive)));
    }
}
