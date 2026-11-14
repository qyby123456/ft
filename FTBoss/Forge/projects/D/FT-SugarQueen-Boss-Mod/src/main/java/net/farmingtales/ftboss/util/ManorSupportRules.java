package net.farmingtales.ftboss.util;

import java.util.Arrays;
import java.util.List;

public final class ManorSupportRules {
    public static final double BASE_HEALTH = 8000;
    public static final float SUPPORT_HEALTH = 2500;
    public static final int INTERVAL = 200;
    private static final int[] VARIANTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11};

    private ManorSupportRules() {}

    public static int variants() { return VARIANTS.length; }

    public static List<ManorSpellRules.Kind> pool(int index) {
        int variant = VARIANTS[Math.floorMod(index, VARIANTS.length)];
        return Arrays.stream(ManorSpellRules.Kind.values()).filter(kind -> kind.variant == variant).toList();
    }

    public static double scaledBase(double current, double previous, double desired) {
        return Double.isFinite(current) && Double.isFinite(previous) && previous > 0
                ? current * desired / previous : desired;
    }

    public static float health(float current, float maximum, float nextMaximum) {
        if (current <= 0) return 0;
        return Math.min(nextMaximum, Math.max(0.01F, maximum > 0 ? current / maximum * nextMaximum : current));
    }
}
