/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Locale;

public final class FoodWeaponSkillRarityRules {
    private static final int[][] WEIGHTS = new int[][]{{55, 30, 12, 3}, {45, 32, 18, 5}, {35, 32, 25, 8}, {20, 30, 35, 15}};

    private FoodWeaponSkillRarityRules() {
    }

    public static int quality(int n) {
        return Math.max(0, Math.min(3, n));
    }

    public static Range range(int n) {
        return switch (FoodWeaponSkillRarityRules.quality(n)) {
            case 0 -> new Range(0.85, 1.1);
            case 1 -> new Range(1.0, 1.3);
            case 2 -> new Range(1.15, 1.55);
            default -> new Range(1.3, 1.85);
        };
    }

    public static Rarity rarity(int n, int n2) {
        int n3 = Math.max(0, Math.min(99, n2));
        int[] nArray = WEIGHTS[FoodWeaponSkillRarityRules.quality(n)];
        for (Rarity rarity : Rarity.values()) {
            if (n3 < nArray[rarity.ordinal()]) {
                return rarity;
            }
            n3 -= nArray[rarity.ordinal()];
        }
        return Rarity.EPIC;
    }

    public static Range rarityRange(int n, Rarity rarity) {
        Range range = FoodWeaponSkillRarityRules.range(n);
        int n2 = rarity == null ? 0 : rarity.ordinal();
        double d = (range.maximum - range.minimum) / (double)Rarity.values().length;
        return new Range(range.minimum + d * (double)n2, range.minimum + d * (double)(n2 + 1));
    }

    public static Roll roll(int n, int n2, double d) {
        int n3 = FoodWeaponSkillRarityRules.quality(n);
        Rarity rarity = FoodWeaponSkillRarityRules.rarity(n3, n2);
        Range range = FoodWeaponSkillRarityRules.rarityRange(n3, rarity);
        double d2 = Double.isFinite(d) ? Math.max(0.0, Math.min(1.0, d)) : 0.0;
        double d3 = range.minimum + (range.maximum - range.minimum) * d2;
        long l = (long)Math.ceil(range.minimum * 1000.0 - 1.0E-9);
        long l2 = (long)Math.floor(range.maximum * 1000.0 + 1.0E-9);
        double d4 = (double)Math.max(l, Math.min(l2, Math.round(d3 * 1000.0))) / 1000.0;
        return new Roll(n3, rarity, d4);
    }

    public static double validate(int n, Rarity rarity, double d) {
        if (n < 0 || n > 3 || rarity == null || !Double.isFinite(d)) {
            return 1.0;
        }
        Range range = FoodWeaponSkillRarityRules.rarityRange(n, rarity);
        return d < range.minimum - 1.0E-9 || d > range.maximum + 1.0E-9 ? 1.0 : d;
    }

    public record Range(double minimum, double maximum) {
    }

    public static enum Rarity {
        COMMON,
        UNCOMMON,
        RARE,
        EPIC;


        public String id() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public record Roll(int quality, Rarity rarity, double multiplier) {
    }
}

