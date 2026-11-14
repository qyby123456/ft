package net.farmingtales.ftboss.util;

import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;

/** 弓与法杖的部件权重、蓄力余量和施法成本纯规则；2026-10-13 保持近战权重及原部件存档不变。 */
public final class FoodWeaponFamilyRules {
    public static final int STAFF_BASE_DURABILITY = 1024;
    private static final double[][] BOW_WEIGHTS = {
            {0.60, 0.25, 0.65, 0.70}, {0.15, 0.15, 0.20, 0.10}, {0.25, 0.60, 0.15, 0.20}};
    private static final double[][] STAFF_WEIGHTS = {
            {0.20, 0.20, 0.25, 0.45}, {0.10, 0.60, 0.55, 0.10}, {0.70, 0.20, 0.20, 0.45}};

    public enum Kind { MELEE, BOW, STAFF }
    public record Charge(int remaining, double carry) {}

    private FoodWeaponFamilyRules() {}

    public static Build build(Kind kind, List<Material> materials) {
        if (kind == null || kind == Kind.MELEE) return FoodWeaponBuildRules.build(materials);
        if (materials.size() != Part.values().length) throw new IllegalArgumentException("three parts required");
        double[][] weights = kind == Kind.BOW ? BOW_WEIGHTS : STAFF_WEIGHTS;
        double durability = 0.0;
        double wear = 0.0;
        double damage = 0.0;
        double speed = 0.0;
        double reach = 0.0;
        for (Part part : Part.values()) {
            Material material = materials.get(part.ordinal());
            double[] weight = weights[part.ordinal()];
            durability += material.durability * weight[2];
            wear += material.wear * weight[2];
            damage += material.damage * weight[0];
            speed += material.speed * weight[1];
            reach += material.reach * weight[3];
        }
        return new Build(WeaponDurabilityRules.multiplier(WeaponDurabilityRules.count(materials)), wear, damage, speed, Math.min(0.75, reach));
    }

    public static double damageMultiplier(Build build) {
        return bounded(build.damage(), 0.50, 2.0, 1.0);
    }

    public static double speedMultiplier(Build build) {
        return bounded(build.speed(), 0.50, 1.50, 1.0);
    }

    public static double rangeMultiplier(Build build) {
        return 1.0 + bounded(build.reach(), 0.0, 0.75, 0.0) * 0.60;
    }

    public static double manaMultiplier(Kind kind, Build build) {
        return kind == Kind.STAFF ? bounded(1.0 - (damageMultiplier(build) - 1.0) * 0.35, 0.75, 1.20, 1.0) : 1.0;
    }

    public static int chargeTicks(int original, double speed) {
        if (original <= 0) return Math.max(0, original);
        long ticks = BigDecimal.valueOf(original).divide(BigDecimal.valueOf(bounded(speed, 0.50, 1.50, 1.0)),
                0, RoundingMode.CEILING).longValue();
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, ticks));
    }

    public static Charge chargeStep(int remaining, double speed, double carry) {
        double safeCarry = bounded(carry, 0.0, Math.nextDown(1.0), 0.0);
        BigDecimal total = BigDecimal.valueOf(bounded(speed, 0.50, 1.50, 1.0)).subtract(BigDecimal.ONE)
                .add(BigDecimal.valueOf(safeCarry));
        int delta = total.setScale(0, RoundingMode.FLOOR).intValue();
        int next = (int) Math.max(0L, Math.min(Integer.MAX_VALUE, (long) remaining - delta));
        return new Charge(next, Math.max(0.0, Math.min(Math.nextDown(1.0),
                total.subtract(BigDecimal.valueOf(delta)).doubleValue())));
    }

    private static double bounded(double value, double minimum, double maximum, double fallback) {
        return Double.isFinite(value) ? Math.max(minimum, Math.min(maximum, value)) : fallback;
    }
}
