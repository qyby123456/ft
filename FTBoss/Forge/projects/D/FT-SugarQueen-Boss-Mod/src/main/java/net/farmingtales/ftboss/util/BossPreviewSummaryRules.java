package net.farmingtales.ftboss.util;

import java.util.List;

/** 石像定位介绍与个人击杀统计的展示、索引及整数同步规则。 */
public final class BossPreviewSummaryRules {
    public static final List<String> ORDINARY_ENTITIES = List.of(
            "ftboss:sugar_queen", "ftboss:meat_mountain", "ftboss:tea_tree", "ftboss:wine_barrel",
            "ftboss:rat_boss", "ftboss:defect_core", "ftboss:compost_boss", "ftboss:apple_boss",
            "ftboss:benger", "ftboss:tide_fish_boss");

    private BossPreviewSummaryRules() {}

    public static boolean showsKills(int profile) {
        return profile >= 0 && profile < ORDINARY_ENTITIES.size();
    }

    public static String entityId(int profile) {
        return showsKills(profile) ? ORDINARY_ENTITIES.get(profile) : "";
    }

    public static int lowBits(int kills) {
        return Math.max(0, kills) & 0xFFFF;
    }

    public static int highBits(int kills) {
        return Math.max(0, kills) >>> 16;
    }

    public static int kills(int lowBits, int highBits) {
        return (highBits & 0x7FFF) << 16 | lowBits & 0xFFFF;
    }
}
