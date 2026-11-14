package net.farmingtales.ftboss.util;

public final class DoraCloneRules {
    public static final float HEALTH = 1000.0F;
    public static final int BASE_COOLDOWN = 180 * 20;
    public static final int PER_EXTRA_PLAYER = 30 * 20;
    public static final int MIN_COOLDOWN = 30 * 20;

    private DoraCloneRules() {}

    public static int cooldownTicks(int players) {
        long extra = Math.max(0L, (long) players - 2L);
        return (int) Math.max(MIN_COOLDOWN, BASE_COOLDOWN - extra * PER_EXTRA_PLAYER);
    }

    public static boolean canSummon(int players, boolean active, long now, long deathTick) {
        return players >= 2 && !active && (deathTick < 0
                || now >= deathTick && now - deathTick >= cooldownTicks(players));
    }
}
