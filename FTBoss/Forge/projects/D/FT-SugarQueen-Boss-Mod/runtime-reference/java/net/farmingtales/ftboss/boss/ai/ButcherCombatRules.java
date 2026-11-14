/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.boss.ai;

public final class ButcherCombatRules {
    public static final int MELEE_IMPACT = 1;
    public static final int MELEE_DURATION = 4;
    public static final int NORMAL_INTERVAL = 6;
    public static final int ENRAGED_INTERVAL = 4;
    public static final int REPATH_INTERVAL = 3;
    public static final double TARGET_RANGE = 48.0;
    public static final int FIRST_SUMMON_DELAY = 160;
    public static final int SUMMON_INTERVAL = 360;
    public static final int ENRAGED_SUMMON_INTERVAL = 240;
    public static final int MAX_MINIONS = 6;
    public static final double BASE_SPEED = 0.1;
    public static final double MAX_SPEED = 0.42;
    public static final float LIFESTEAL_RATIO = 0.15f;

    private ButcherCombatRules() {
    }

    public static int interval(boolean enraged) {
        return enraged ? 4 : 6;
    }

    public static double chaseSpeed(boolean enraged) {
        return enraged ? 1.15 : 1.05;
    }

    public static boolean withinReach(double distanceSquared, double verticalGap, double ownerWidth, double targetWidth) {
        double reach = ownerWidth * 2.0;
        return distanceSquared <= reach * reach + targetWidth && verticalGap < 2.0;
    }

    public static boolean empowered(int successfulHits) {
        return successfulHits % 2 == 1;
    }

    public static float damage(float attackDamage, int successfulHits) {
        return attackDamage > 0.0f ? Math.max(24.0f, attackDamage * 2.0f) * (ButcherCombatRules.empowered(successfulHits) ? 1.25f : 1.0f) : 0.0f;
    }

    public static double minionHealth(int kind) {
        return kind == 2 ? 160.0 : (kind == 1 ? 120.0 : 80.0);
    }

    public static double minionDamage(int kind) {
        return kind == 2 ? 12.0 : (kind == 1 ? 14.0 : 8.0);
    }

    public static double minionArmor(int kind) {
        return kind == 2 ? 16.0 : (kind == 1 ? 12.0 : 8.0);
    }

    public static double minionSpeed(int kind) {
        return kind == 2 ? 0.38 : (kind == 1 ? 0.32 : 0.36);
    }

    public static int minionInterval(int kind) {
        return kind == 2 ? 10 : 12;
    }

    public static int summonCount(int requested, int current) {
        return Math.max(0, Math.min(requested, 6 - current));
    }

    public static float lifesteal(float dealt, float missingHealth) {
        return dealt > 0.0f ? Math.max(0.0f, Math.min(missingHealth, dealt * 0.15f)) : 0.0f;
    }
}

