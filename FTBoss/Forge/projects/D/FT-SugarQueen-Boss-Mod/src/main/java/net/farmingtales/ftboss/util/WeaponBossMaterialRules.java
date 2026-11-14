package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.BossRelicRules.Kind;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;

/** 真 Boss 构筑原料映射纯规则；每次最终玩家胜利固定 1 件，2026-10-12 新增而不消耗原有遗物。 */
public final class WeaponBossMaterialRules {
    public static final int DROP_COUNT = 1;

    private WeaponBossMaterialRules() {}

    public static Material material(Kind kind) {
        if (kind == null) return Material.ORIGINAL;
        return switch (kind) {
            case FROST -> Material.BOSS_FROST;
            case BUTCHER -> Material.BOSS_BUTCHER;
            case TEA -> Material.BOSS_TEA;
            case WINE -> Material.BOSS_WINE;
            case RAT -> Material.BOSS_CHEESE;
            case DEFECT -> Material.BOSS_CAPACITOR;
            case COMPOST -> Material.BOSS_SPORE;
            case APPLE -> Material.BOSS_APPLE;
            case BENGER -> Material.BOSS_SHEPHERD;
            case TIDE -> Material.BOSS_TIDE;
        };
    }

    public static String itemId(Kind kind) {
        Material material = material(kind);
        return material == Material.ORIGINAL ? "" : material.id().substring("boss_".length()) + "_material";
    }

    public static int count(Kind kind) {
        return kind == null ? 0 : DROP_COUNT;
    }

    public static String componentId(Kind kind) {
        return kind == null ? "" : "ftboss:" + itemId(kind);
    }
}
