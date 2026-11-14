package net.farmingtales.ftboss.util;

import java.util.Arrays;
import java.util.List;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;

public final class ManorSupportRules {
    public static final double BASE_HEALTH = 1500.0;
    public static final float SUPPORT_HEALTH = 500.0F;
    public static final int INTERVAL = 200;
    private static final int[] VARIANTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11};

    private ManorSupportRules() {
    }

    public static int variants() {
        return VARIANTS.length;
    }

    public static List<Kind> pool(int var0) {
        int var1 = VARIANTS[Math.floorMod(var0, VARIANTS.length)];
        return Arrays.stream(Kind.values()).filter(var1x -> var1x.variant == var1).toList();
    }

    public static double scaledBase(double var0, double var2, double var4) {
        return Double.isFinite(var0) && Double.isFinite(var2) && var2 > 0.0 ? var0 * var4 / var2 : var4;
    }

    public static float health(float var0, float var1, float var2) {
        return var0 <= 0.0F ? 0.0F : Math.min(var2, Math.max(0.01F, var1 > 0.0F ? var0 / var1 * var2 : var0));
    }
}
