package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;

/** 锭与 Boss 材质触发纯规则；冷却单位 tick、同系食物 2 个联动，2026-10-12 新增玩家共享冷却。 */
public final class WeaponMaterialPowerRules {
    public static final int SYNERGY_FOOD_COUNT = 2;
    public static final int FOOD_COUNT_LIMIT = 30;
    public static final int PET_RANGE = 8;
    public static final int REPEL_RANGE = 3;

    private WeaponMaterialPowerRules() {}

    public static String foodFamily(Material material) {
        if (material == null) return "";
        return switch (material) {
            case BLACK_STEEL, BOSS_CHEESE -> "CHEESE";
            case AMETHYST, CURSIUM, BOSS_WINE -> "WINE";
            case MITHRIL, LAPIS, BOSS_TEA -> "TEA";
            case COAL, WITHERITE, IGNITIUM, BOSS_BUTCHER, BOSS_SHEPHERD -> "MEAT";
            case QUARTZ, BOSS_FROST -> "SWEET";
            case REDSTONE, ANCIENT_METAL, BOSS_CAPACITOR, BOSS_SPORE -> "VEGETABLE";
            case BOSS_APPLE -> "APPLE";
            case ENDERITE, BOSS_TIDE -> "FISH";
            default -> "";
        };
    }

    public static boolean synergy(int foodCount) {
        return foodCount >= SYNERGY_FOOD_COUNT;
    }

    public static int foodCount(int count) {
        return Math.max(0, Math.min(FOOD_COUNT_LIMIT, count));
    }

    public static int baseCooldown(Material material) {
        if (material == null) return 0;
        return switch (material) {
            case REDSTONE, BOSS_FROST -> 120;
            case LAPIS, IGNITIUM, CURSIUM, BOSS_BUTCHER, BOSS_CAPACITOR, BOSS_SPORE -> 160;
            case WITHERITE, ANCIENT_METAL -> 180;
            case BOSS_TEA -> 240;
            case AMETHYST, BOSS_WINE, BOSS_SHEPHERD, BOSS_TIDE -> 200;
            case BOSS_APPLE -> 400;
            default -> 0;
        };
    }

    public static int cooldown(Material material, int foodCount) {
        int base = baseCooldown(material);
        return synergy(foodCount) ? base * 4 / 5 : base;
    }

    public static int duration(int baseTicks, int foodCount) {
        int safeBase = Math.max(0, Math.min(12000, baseTicks));
        return synergy(foodCount) ? safeBase + safeBase / 2 : safeBase;
    }

    public static boolean ready(long now, boolean hasStamp, long stamp, int cooldownTicks) {
        if (now < 0 || cooldownTicks <= 0) return false;
        if (!hasStamp) return true;
        return stamp >= 0 && now >= stamp && now - stamp >= cooldownTicks;
    }

    public static double repelStrength(int foodCount) {
        return synergy(foodCount) ? 0.80 : 0.60;
    }
}
