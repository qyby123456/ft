package net.farmingtales.ftboss.util;

public final class ManorArmorBalanceRules {
    private ManorArmorBalanceRules() {}

    public static int armor(int variant, boolean mini, boolean giant, int originalArmor) {
        if (mini && variant == 0) return YouziMiniRules.ARMOR;
        if (mini || giant || variant < 0 || variant > 11 || variant == 9) return originalArmor;
        if (variant == 6) return 55;
        return Math.max(0, originalArmor) / 2;
    }
}
