/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.BossRelicRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;

public final class WeaponBossMaterialRules {
    public static final int DROP_COUNT = 1;

    private WeaponBossMaterialRules() {
    }

    public static FoodWeaponBuildRules.Material material(BossRelicRules.Kind kind) {
        if (kind == null) {
            return FoodWeaponBuildRules.Material.ORIGINAL;
        }
        return switch (kind) {
            default -> throw new IncompatibleClassChangeError();
            case BossRelicRules.Kind.FROST -> FoodWeaponBuildRules.Material.BOSS_FROST;
            case BossRelicRules.Kind.BUTCHER -> FoodWeaponBuildRules.Material.BOSS_BUTCHER;
            case BossRelicRules.Kind.TEA -> FoodWeaponBuildRules.Material.BOSS_TEA;
            case BossRelicRules.Kind.WINE -> FoodWeaponBuildRules.Material.BOSS_WINE;
            case BossRelicRules.Kind.RAT -> FoodWeaponBuildRules.Material.BOSS_CHEESE;
            case BossRelicRules.Kind.DEFECT -> FoodWeaponBuildRules.Material.BOSS_CAPACITOR;
            case BossRelicRules.Kind.COMPOST -> FoodWeaponBuildRules.Material.BOSS_SPORE;
            case BossRelicRules.Kind.APPLE -> FoodWeaponBuildRules.Material.BOSS_APPLE;
            case BossRelicRules.Kind.BENGER -> FoodWeaponBuildRules.Material.BOSS_SHEPHERD;
            case BossRelicRules.Kind.TIDE -> FoodWeaponBuildRules.Material.BOSS_TIDE;
        };
    }

    public static String itemId(BossRelicRules.Kind kind) {
        FoodWeaponBuildRules.Material material = WeaponBossMaterialRules.material(kind);
        return material == FoodWeaponBuildRules.Material.ORIGINAL ? "" : material.id().substring("boss_".length()) + "_material";
    }

    public static int count(BossRelicRules.Kind kind) {
        return kind == null ? 0 : 1;
    }

    public static String componentId(BossRelicRules.Kind kind) {
        return kind == null ? "" : "ftboss:" + WeaponBossMaterialRules.itemId(kind);
    }
}

