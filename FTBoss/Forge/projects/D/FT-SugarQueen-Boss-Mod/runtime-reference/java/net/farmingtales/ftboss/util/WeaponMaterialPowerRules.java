/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.FoodWeaponBuildRules;

public final class WeaponMaterialPowerRules {
    public static final int SYNERGY_FOOD_COUNT = 2;
    public static final int FOOD_COUNT_LIMIT = 30;
    public static final int PET_RANGE = 8;
    public static final int REPEL_RANGE = 3;

    private WeaponMaterialPowerRules() {
    }

    public static String foodFamily(FoodWeaponBuildRules.Material material) {
        if (material == null) {
            return "";
        }
        return switch (material) {
            case FoodWeaponBuildRules.Material.BLACK_STEEL, FoodWeaponBuildRules.Material.BOSS_CHEESE -> "CHEESE";
            case FoodWeaponBuildRules.Material.AMETHYST, FoodWeaponBuildRules.Material.CURSIUM, FoodWeaponBuildRules.Material.BOSS_WINE -> "WINE";
            case FoodWeaponBuildRules.Material.MITHRIL, FoodWeaponBuildRules.Material.LAPIS, FoodWeaponBuildRules.Material.BOSS_TEA -> "TEA";
            case FoodWeaponBuildRules.Material.COAL, FoodWeaponBuildRules.Material.WITHERITE, FoodWeaponBuildRules.Material.IGNITIUM, FoodWeaponBuildRules.Material.BOSS_BUTCHER, FoodWeaponBuildRules.Material.BOSS_SHEPHERD -> "MEAT";
            case FoodWeaponBuildRules.Material.QUARTZ, FoodWeaponBuildRules.Material.BOSS_FROST -> "SWEET";
            case FoodWeaponBuildRules.Material.REDSTONE, FoodWeaponBuildRules.Material.ANCIENT_METAL, FoodWeaponBuildRules.Material.BOSS_CAPACITOR, FoodWeaponBuildRules.Material.BOSS_SPORE -> "VEGETABLE";
            case FoodWeaponBuildRules.Material.BOSS_APPLE -> "APPLE";
            case FoodWeaponBuildRules.Material.ENDERITE, FoodWeaponBuildRules.Material.BOSS_TIDE -> "FISH";
            default -> "";
        };
    }

    public static boolean synergy(int n) {
        return n >= 2;
    }

    public static int foodCount(int n) {
        return Math.max(0, Math.min(30, n));
    }

    public static int baseCooldown(FoodWeaponBuildRules.Material material) {
        if (material == null) {
            return 0;
        }
        return switch (material) {
            case FoodWeaponBuildRules.Material.BOSS_FROST, FoodWeaponBuildRules.Material.REDSTONE -> 120;
            case FoodWeaponBuildRules.Material.CURSIUM, FoodWeaponBuildRules.Material.LAPIS, FoodWeaponBuildRules.Material.IGNITIUM, FoodWeaponBuildRules.Material.BOSS_BUTCHER, FoodWeaponBuildRules.Material.BOSS_CAPACITOR, FoodWeaponBuildRules.Material.BOSS_SPORE -> 160;
            case FoodWeaponBuildRules.Material.WITHERITE, FoodWeaponBuildRules.Material.ANCIENT_METAL -> 180;
            case FoodWeaponBuildRules.Material.BOSS_TEA -> 240;
            case FoodWeaponBuildRules.Material.AMETHYST, FoodWeaponBuildRules.Material.BOSS_WINE, FoodWeaponBuildRules.Material.BOSS_SHEPHERD, FoodWeaponBuildRules.Material.BOSS_TIDE -> 200;
            case FoodWeaponBuildRules.Material.BOSS_APPLE -> 400;
            default -> 0;
        };
    }

    public static int cooldown(FoodWeaponBuildRules.Material material, int n) {
        int n2 = WeaponMaterialPowerRules.baseCooldown(material);
        return WeaponMaterialPowerRules.synergy(n) ? n2 * 4 / 5 : n2;
    }

    public static int duration(int n, int n2) {
        int n3 = Math.max(0, Math.min(12000, n));
        return WeaponMaterialPowerRules.synergy(n2) ? n3 + n3 / 2 : n3;
    }

    public static boolean ready(long l, boolean bl, long l2, int n) {
        if (l < 0L || n <= 0) {
            return false;
        }
        if (!bl) {
            return true;
        }
        return l2 >= 0L && l >= l2 && l - l2 >= (long)n;
    }

    public static double repelStrength(int n) {
        return WeaponMaterialPowerRules.synergy(n) ? 0.8 : 0.6;
    }
}

