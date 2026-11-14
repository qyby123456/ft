/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.List;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;

public final class WeaponDurabilityRules {
    public static final String ALLOY_ITEM = "kubejs:ne_ingot";
    public static final double BONUS_PER_PART = 0.1;
    public static final double MAX_BONUS = 2.0;

    private WeaponDurabilityRules() {
    }

    public static int count(List<FoodWeaponBuildRules.Material> list) {
        int n = 0;
        for (FoodWeaponBuildRules.Material material : list) {
            if (material != FoodWeaponBuildRules.Material.DIAMOND_ALLOY) continue;
            ++n;
        }
        return n;
    }

    public static double multiplier(int n) {
        return 1.0 + Math.min(2.0, (double)Math.max(0, n) * 0.1);
    }

    public static int maxDurability(int n, int n2) {
        if (n <= 0 || n2 <= 0) {
            return n;
        }
        return (int)Math.min(Integer.MAX_VALUE, Math.round((double)n * WeaponDurabilityRules.multiplier(n2)));
    }
}

