package net.farmingtales.ftboss.util;

import java.util.List;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;

public final class WeaponDurabilityRules {
    public static final String ALLOY_ITEM = "kubejs:ne_ingot";
    public static final double BONUS_PER_PART = 0.10;
    public static final double MAX_BONUS = 2.0;

    private WeaponDurabilityRules() {}

    public static int count(List<Material> materials) {
        int count = 0;
        for (Material material : materials) if (material == Material.DIAMOND_ALLOY) count++;
        return count;
    }

    public static double multiplier(int installedParts) {
        return 1.0 + Math.min(MAX_BONUS, Math.max(0, installedParts) * BONUS_PER_PART);
    }

    public static int maxDurability(int original, int installedParts) {
        if (original <= 0 || installedParts <= 0) return original;
        return (int) Math.min(Integer.MAX_VALUE, Math.round(original * multiplier(installedParts)));
    }
}
