/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.SwordFunctionRules;

public final class SwordCavityPaletteRules {
    public static final int NEUTRAL = -13944511;

    private SwordCavityPaletteRules() {
    }

    public static int functionColor(SwordFunctionRules.Function function) {
        return switch (function) {
            default -> throw new IncompatibleClassChangeError();
            case SwordFunctionRules.Function.HANDLE -> -995666;
            case SwordFunctionRules.Function.GUARD -> -990286;
            case SwordFunctionRules.Function.CORE -> -2504723;
            case SwordFunctionRules.Function.STABILITY -> -3874112;
            case SwordFunctionRules.Function.BLADE -> -4268049;
        };
    }

    public static int cellColor(SwordFunctionRules.Profile profile, int n) {
        if (profile == null || n < 0 || n >= 27) {
            return -13944511;
        }
        SwordFunctionRules.Values values = profile.cellBaseValues().get(n);
        double d = 0.0;
        double d2 = 0.0;
        double d3 = 0.0;
        double d4 = 0.0;
        for (SwordFunctionRules.Function function : SwordFunctionRules.Function.values()) {
            double d5 = values.value(function);
            int n2 = SwordCavityPaletteRules.functionColor(function);
            d += d5;
            d2 += (double)(n2 >>> 16 & 0xFF) * d5;
            d3 += (double)(n2 >>> 8 & 0xFF) * d5;
            d4 += (double)(n2 & 0xFF) * d5;
        }
        if (d == 0.0) {
            return -13944511;
        }
        return 0xFF000000 | (int)Math.round(d2 / d) << 16 | (int)Math.round(d3 / d) << 8 | (int)Math.round(d4 / d);
    }
}

