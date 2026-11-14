/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.WeaponDurabilityRules;

public final class FoodWeaponFamilyRules {
    public static final int STAFF_BASE_DURABILITY = 1024;
    private static final double[][] BOW_WEIGHTS = new double[][]{{0.6, 0.25, 0.65, 0.7}, {0.15, 0.15, 0.2, 0.1}, {0.25, 0.6, 0.15, 0.2}};
    private static final double[][] STAFF_WEIGHTS = new double[][]{{0.2, 0.2, 0.25, 0.45}, {0.1, 0.6, 0.55, 0.1}, {0.7, 0.2, 0.2, 0.45}};

    private FoodWeaponFamilyRules() {
    }

    public static double damageMultiplier(FoodWeaponBuildRules.Build build) {
        return FoodWeaponFamilyRules.bounded(build.damage(), 0.5, 2.0, 1.0);
    }

    public static double speedMultiplier(FoodWeaponBuildRules.Build build) {
        return FoodWeaponFamilyRules.bounded(build.speed(), 0.5, 1.5, 1.0);
    }

    public static double rangeMultiplier(FoodWeaponBuildRules.Build build) {
        return 1.0 + FoodWeaponFamilyRules.bounded(build.reach(), 0.0, 0.75, 0.0) * 0.6;
    }

    public static double manaMultiplier(Kind kind, FoodWeaponBuildRules.Build build) {
        return kind == Kind.STAFF ? FoodWeaponFamilyRules.bounded(1.0 - (FoodWeaponFamilyRules.damageMultiplier(build) - 1.0) * 0.35, 0.75, 1.2, 1.0) : 1.0;
    }

    public static int chargeTicks(int n, double d) {
        if (n <= 0) {
            return Math.max(0, n);
        }
        long l = BigDecimal.valueOf(n).divide(BigDecimal.valueOf(FoodWeaponFamilyRules.bounded(d, 0.5, 1.5, 1.0)), 0, RoundingMode.CEILING).longValue();
        return (int)Math.max(1L, Math.min(Integer.MAX_VALUE, l));
    }

    public static Charge chargeStep(int n, double d, double d2) {
        double d3 = FoodWeaponFamilyRules.bounded(d2, 0.0, Math.nextDown(1.0), 0.0);
        BigDecimal bigDecimal = BigDecimal.valueOf(FoodWeaponFamilyRules.bounded(d, 0.5, 1.5, 1.0)).subtract(BigDecimal.ONE).add(BigDecimal.valueOf(d3));
        int n2 = bigDecimal.setScale(0, RoundingMode.FLOOR).intValue();
        int n3 = (int)Math.max(0L, Math.min(Integer.MAX_VALUE, (long)n - (long)n2));
        return new Charge(n3, Math.max(0.0, Math.min(Math.nextDown(1.0), bigDecimal.subtract(BigDecimal.valueOf(n2)).doubleValue())));
    }

    private static double bounded(double d, double d2, double d3, double d4) {
        return Double.isFinite(d) ? Math.max(d2, Math.min(d3, d)) : d4;
    }

    public static FoodWeaponBuildRules.Build build(Kind kind, List<FoodWeaponBuildRules.Material> list) {
        if (kind == null || kind == Kind.MELEE) {
            return FoodWeaponBuildRules.build(list);
        }
        if (list.size() != FoodWeaponBuildRules.Part.values().length) {
            throw new IllegalArgumentException("three parts required");
        }
        double[][] dArray = kind == Kind.BOW ? BOW_WEIGHTS : STAFF_WEIGHTS;
        double d = 0.0;
        double d2 = 0.0;
        double d3 = 0.0;
        double d4 = 0.0;
        double d5 = 0.0;
        for (FoodWeaponBuildRules.Part part : FoodWeaponBuildRules.Part.values()) {
            FoodWeaponBuildRules.Material material = list.get(part.ordinal());
            double[] dArray2 = dArray[part.ordinal()];
            d += material.durability * dArray2[2];
            d2 += material.wear * dArray2[2];
            d3 += material.damage * dArray2[0];
            d4 += material.speed * dArray2[1];
            d5 += material.reach * dArray2[3];
        }
        return new FoodWeaponBuildRules.Build(WeaponDurabilityRules.multiplier(WeaponDurabilityRules.count(list)), d2, d3, d4, Math.min(0.75, d5));
    }

    public static enum Kind {
        MELEE,
        BOW,
        STAFF;

    }

    public record Charge(int remaining, double carry) {
    }
}

