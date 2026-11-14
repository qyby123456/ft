package net.farmingtales.ftboss.util;

public final class MeleeEnhancementRules {
    public static final double BONUS_SCALE = 1.30D;
    public static final double MAX_CHANCE = 0.85D;

    private MeleeEnhancementRules() {}

    public static float amount(float value, boolean melee) {
        return melee ? value * (float) BONUS_SCALE : value;
    }

    public static double amount(double value, boolean melee) {
        return melee ? value * BONUS_SCALE : value;
    }

    public static float multiplier(float value, boolean melee) {
        return melee ? 1.0F + (value - 1.0F) * (float) BONUS_SCALE : value;
    }

    public static double chance(double value, boolean melee) {
        return melee && value > 0.0D && value < 1.0D
                ? Math.max(value, Math.min(MAX_CHANCE, value * BONUS_SCALE)) : value;
    }
}
