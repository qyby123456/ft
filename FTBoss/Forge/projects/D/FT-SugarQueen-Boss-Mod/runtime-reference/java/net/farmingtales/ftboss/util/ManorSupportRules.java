/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Arrays;
import java.util.List;
import net.farmingtales.ftboss.util.ManorSpellRules;

public final class ManorSupportRules {
    public static final double BASE_HEALTH = 1500.0;
    public static final float SUPPORT_HEALTH = 500.0f;
    public static final int INTERVAL = 200;
    private static final int[] VARIANTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11};

    private ManorSupportRules() {
    }

    public static int variants() {
        return VARIANTS.length;
    }

    public static List<ManorSpellRules.Kind> pool(int n) {
        int n2 = VARIANTS[Math.floorMod(n, VARIANTS.length)];
        return Arrays.stream(ManorSpellRules.Kind.values()).filter(kind -> kind.variant == n2).toList();
    }

    public static double scaledBase(double d, double d2, double d3) {
        return Double.isFinite(d) && Double.isFinite(d2) && d2 > 0.0 ? d * d3 / d2 : d3;
    }

    public static float health(float f, float f2, float f3) {
        if (f <= 0.0f) {
            return 0.0f;
        }
        return Math.min(f3, Math.max(0.01f, f2 > 0.0f ? f / f2 * f3 : f));
    }
}

