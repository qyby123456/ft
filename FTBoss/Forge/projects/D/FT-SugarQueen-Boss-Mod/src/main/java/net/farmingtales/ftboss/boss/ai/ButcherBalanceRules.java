package net.farmingtales.ftboss.boss.ai;

public final class ButcherBalanceRules {
    public static final float MELEE_DAMAGE = 45.0F;
    public static final int OPENING_MINIONS = 3;
    public static final double ASSAULT_SPEED_BONUS = 0.5;

    private ButcherBalanceRules() {}

    public static boolean enterAssault(int phase, float health, float maximumHealth) {
        return phase == ButcherPhaseRules.REST && health > 0 && maximumHealth > 0 && health <= maximumHealth * 0.5F;
    }

    public static float lifesteal(int phase, float dealt, float missingHealth) {
        return phase == ButcherPhaseRules.ASSAULT ? ButcherCombatRules.lifesteal(dealt, missingHealth) : 0;
    }
}
