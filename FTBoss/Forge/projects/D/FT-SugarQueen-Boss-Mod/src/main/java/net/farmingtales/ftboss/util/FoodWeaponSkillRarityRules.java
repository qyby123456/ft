package net.farmingtales.ftboss.util;

/** 进化祭品品质与技能稀有度纯规则；倍率保留三位小数，2026-10-13 新增服务端单次抽样边界。 */
public final class FoodWeaponSkillRarityRules {
    public enum Rarity {
        COMMON, UNCOMMON, RARE, EPIC;

        public String id() { return name().toLowerCase(java.util.Locale.ROOT); }
    }

    public record Range(double minimum, double maximum) {}
    public record Roll(int quality, Rarity rarity, double multiplier) {}
    private static final int[][] WEIGHTS = {{55, 30, 12, 3}, {45, 32, 18, 5}, {35, 32, 25, 8}, {20, 30, 35, 15}};

    private FoodWeaponSkillRarityRules() {}

    public static int quality(int value) { return Math.max(0, Math.min(3, value)); }

    public static Range range(int value) {
        return switch (quality(value)) {
            case 0 -> new Range(0.85, 1.10);
            case 1 -> new Range(1.00, 1.30);
            case 2 -> new Range(1.15, 1.55);
            default -> new Range(1.30, 1.85);
        };
    }

    public static Rarity rarity(int value, int percentile) {
        int remaining = Math.max(0, Math.min(99, percentile));
        int[] weights = WEIGHTS[quality(value)];
        for (Rarity rarity : Rarity.values()) {
            if (remaining < weights[rarity.ordinal()]) return rarity;
            remaining -= weights[rarity.ordinal()];
        }
        return Rarity.EPIC;
    }

    public static Range rarityRange(int value, Rarity rarity) {
        Range full = range(value);
        int rank = rarity == null ? 0 : rarity.ordinal();
        double width = (full.maximum - full.minimum) / Rarity.values().length;
        return new Range(full.minimum + width * rank, full.minimum + width * (rank + 1));
    }

    public static Roll roll(int value, int percentile, double sample) {
        int quality = quality(value);
        Rarity rarity = rarity(quality, percentile);
        Range selected = rarityRange(quality, rarity);
        double fraction = Double.isFinite(sample) ? Math.max(0, Math.min(1, sample)) : 0;
        double raw = selected.minimum + (selected.maximum - selected.minimum) * fraction;
        long minimum = (long) Math.ceil(selected.minimum * 1000.0 - 1.0E-9);
        long maximum = (long) Math.floor(selected.maximum * 1000.0 + 1.0E-9);
        double multiplier = Math.max(minimum, Math.min(maximum, Math.round(raw * 1000.0))) / 1000.0;
        return new Roll(quality, rarity, multiplier);
    }

    public static double validate(int quality, Rarity rarity, double multiplier) {
        if (quality < 0 || quality > 3 || rarity == null || !Double.isFinite(multiplier)) return 1.0;
        Range range = rarityRange(quality, rarity);
        return multiplier < range.minimum - 1.0E-9 || multiplier > range.maximum + 1.0E-9 ? 1.0 : multiplier;
    }
}
