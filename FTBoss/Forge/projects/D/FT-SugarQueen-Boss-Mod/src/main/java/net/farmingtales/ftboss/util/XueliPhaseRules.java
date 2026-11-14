package net.farmingtales.ftboss.util;

/** 雪黎四阶段的血量边界、间隔与命中回复规则（2026-10-26）。 */
public final class XueliPhaseRules {
    public static final double LOW_SKILL_CHANCE = 0.5;

    private XueliPhaseRules() {}

    public static int phase(float health, float maximumHealth) {
        if (!Float.isFinite(health) || !Float.isFinite(maximumHealth) || maximumHealth <= 0.0f) return 1;
        double fraction = (double) health / maximumHealth;
        if (fraction <= 0.25) return 4;
        if (fraction <= 0.50) return 3;
        if (fraction <= 0.75) return 2;
        return 1;
    }

    public static int maximumSkillInterval(int phase) {
        return switch (phase) {
            case 2 -> 60;
            case 3 -> 40;
            case 4 -> 20;
            default -> 100;
        };
    }

    public static int maximumAttackInterval(int phase) {
        return switch (phase) {
            case 2 -> 50;
            case 3 -> 30;
            case 4 -> 20;
            default -> 80;
        };
    }

    private static int bonusPercent(int phase) {
        return phase == 4 ? 30 : phase == 3 ? 20 : 0;
    }

    public static double speedBonus(int phase) {
        return bonusPercent(phase) / 100.0;
    }

    public static int cooldown(int ticks, int phase) {
        if (ticks <= 0) return 0;
        return (int) (((long) ticks * (100 - bonusPercent(phase)) + 99) / 100);
    }

    public static int skillInterval(int original, int phase) {
        return cooldown(Math.min(Math.max(0, original), maximumSkillInterval(phase)), phase);
    }

    public static int attackInterval(int phase) {
        int divisor = 100 + bonusPercent(phase);
        return (maximumAttackInterval(phase) * 100 + divisor - 1) / divisor;
    }

    public static boolean shouldCast(int phase, float roll) {
        return phase != 1 || roll < LOW_SKILL_CHANCE;
    }

    public static float healFraction(int phase) {
        return phase == 4 ? 0.04f : 0.01f;
    }

    public static float healing(float health, float maximumHealth) {
        if (!Float.isFinite(health) || !Float.isFinite(maximumHealth) || health <= 0.0f || maximumHealth <= health) return 0.0f;
        return Math.min(maximumHealth - health, maximumHealth * healFraction(phase(health, maximumHealth)));
    }
}
