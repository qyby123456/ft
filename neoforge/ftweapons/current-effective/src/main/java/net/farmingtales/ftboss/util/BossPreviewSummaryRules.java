package net.farmingtales.ftboss.util;

import java.util.List;

public final class BossPreviewSummaryRules {
    public static final List<String> ORDINARY_ENTITIES = List.of(
        "ftboss:sugar_queen",
        "ftboss:meat_mountain",
        "ftboss:tea_tree",
        "ftboss:wine_barrel",
        "ftboss:rat_boss",
        "ftboss:defect_core",
        "ftboss:compost_boss",
        "ftboss:apple_boss",
        "ftboss:benger",
        "ftboss:tide_fish_boss"
    );

    private BossPreviewSummaryRules() {
    }

    public static boolean showsKills(int var0) {
        return var0 >= 0 && var0 < ORDINARY_ENTITIES.size();
    }

    public static String entityId(int var0) {
        return showsKills(var0) ? ORDINARY_ENTITIES.get(var0) : "";
    }

    public static int lowBits(int var0) {
        return Math.max(0, var0) & 65535;
    }

    public static int highBits(int var0) {
        return Math.max(0, var0) >>> 16;
    }

    public static int kills(int var0, int var1) {
        return (var1 & 32767) << 16 | var0 & 65535;
    }
}
