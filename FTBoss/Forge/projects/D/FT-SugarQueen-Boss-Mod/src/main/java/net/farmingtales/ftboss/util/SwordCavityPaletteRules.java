package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.SwordFunctionRules.Function;
import net.farmingtales.ftboss.util.SwordFunctionRules.Profile;
import net.farmingtales.ftboss.util.SwordFunctionRules.Values;

/** 剑格浅色提示；2026-10-13 按实际五维占用基值混色，不依赖显示坐标或更改功能计算。 */
public final class SwordCavityPaletteRules {
    public static final int NEUTRAL = 0xFF2B3941;

    private SwordCavityPaletteRules() {}

    public static int functionColor(Function function) {
        return switch (function) {
            case HANDLE -> 0xFFF0CEAE;
            case GUARD -> 0xFFF0E3B2;
            case CORE -> 0xFFD9C7ED;
            case STABILITY -> 0xFFC4E2C0;
            case BLADE -> 0xFFBEDFEF;
        };
    }

    public static int cellColor(Profile profile, int index) {
        if (profile == null || index < 0 || index >= SwordCavityRules.CELL_COUNT) return NEUTRAL;
        Values base = profile.cellBaseValues().get(index);
        double total = 0;
        double red = 0;
        double green = 0;
        double blue = 0;
        for (Function function : Function.values()) {
            double value = base.value(function);
            int color = functionColor(function);
            total += value;
            red += ((color >>> 16) & 255) * value;
            green += ((color >>> 8) & 255) * value;
            blue += (color & 255) * value;
        }
        if (total == 0) return NEUTRAL;
        return 0xFF000000 | ((int) Math.round(red / total) << 16) | ((int) Math.round(green / total) << 8) | (int) Math.round(blue / total);
    }
}
