package net.farmingtales.ftboss.util;

public final class MeleeEnhancementRules {
    public static final double BONUS_SCALE = 1.3;
    public static final double MAX_CHANCE = 0.85;

    private MeleeEnhancementRules() {
    }

    public static float amount(float var0, boolean var1) {
        return var1 ? var0 * 1.3F : var0;
    }

    public static double amount(double var0, boolean var2) {
        return var2 ? var0 * 1.3 : var0;
    }

    public static float multiplier(float var0, boolean var1) {
        return var1 ? 1.0F + (var0 - 1.0F) * 1.3F : var0;
    }

    public static double chance(double var0, boolean var2) {
        return var2 && var0 > 0.0 && var0 < 1.0 ? Math.max(var0, Math.min(0.85, var0 * 1.3)) : var0;
    }
}
