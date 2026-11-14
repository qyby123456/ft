package net.farmingtales.ftboss.util;

public final class LemonBalanceRules {
    public static final int BASE_ABSORBED_BOSSES = 3;
    public static final int MAX_ACTIVE_BOSSES = 3;
    public static final int MAX_ACTIVE_SUMMONS = 20;
    public static final float MAX_HEAL_PER_SECOND = 20.0F;

    private LemonBalanceRules() {}

    public static int requiredBosses(int players) {
        return MAX_ACTIVE_BOSSES;
    }

    public static boolean exceedsSummonLimit(int summons, int bosses, boolean boss) {
        return summons >= MAX_ACTIVE_SUMMONS || boss && bosses >= MAX_ACTIVE_BOSSES;
    }

    public static float hitLimit(int absorbed) {
        return 100.0F + 25.0F * Math.max(0, absorbed);
    }

    public static float secondLimit(int absorbed) {
        return 200.0F + 50.0F * Math.max(0, absorbed);
    }
}
