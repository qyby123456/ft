package net.farmingtales.ftboss.util;

import java.util.List;

/** 食武进化、材料配比与耐久损耗纯规则；等级 4/8、品质 0..3，2026-10-12 新增可失败进化与三部件构成。 */
public final class FoodWeaponBuildRules {
    public static final int REGULAR_MAX_LEVEL = 4;
    public static final int REGULAR_SOCKET_CAPACITY = 10;
    public static final int MAX_BLESSINGS = 6;
    public static final int DAILY_TASKS = 12;
    public static final int DAILY_EVOLUTION_TASKS = 3;
    private static final int[] COSTS = {100, 250, 450, 700, 1000, 1350, 1750, 2200};

    public enum Part {
        EDGE(0.70, 0.50, 0.60, 0.70),
        GRIP(0.15, 0.40, 0.25, 0.20),
        CORE(0.15, 0.10, 0.15, 0.10);

        public final double damageWeight;
        public final double speedWeight;
        public final double durabilityWeight;
        public final double reachWeight;

        Part(double damageWeight, double speedWeight, double durabilityWeight, double reachWeight) {
            this.damageWeight = damageWeight;
            this.speedWeight = speedWeight;
            this.durabilityWeight = durabilityWeight;
            this.reachWeight = reachWeight;
        }

        public static Part byIndex(int index) {
            return index < 0 || index >= values().length ? null : values()[index];
        }
    }

    public enum Material {
        ORIGINAL(1.0, 1.0, 1.0, 1.0, 0.0, 0xBBBBBB),
        WOOD(0.70, 1.15, 0.80, 1.15, 0.10, 0xAF804B),
        STONE(1.00, 1.10, 1.10, 0.88, 0.20, 0x999999),
        IRON(1.35, 0.85, 1.15, 0.98, 0.25, 0xD2DADE),
        COPPER(1.05, 0.90, 1.02, 1.08, 0.20, 0xDB8648),
        GOLD(0.65, 1.25, 0.95, 1.30, 0.05, 0xFFCA48),
        DIAMOND(1.80, 0.70, 1.30, 1.02, 0.30, 0x4DDCD5),
        NETHERITE(2.20, 0.55, 1.42, 0.95, 0.40, 0x766677),
        BONE(0.85, 0.95, 1.05, 1.10, 0.40, 0xE9DFBB),
        OBSIDIAN(1.70, 0.80, 1.38, 0.75, 0.30, 0x493861),
        LEATHER(0.80, 0.65, 0.82, 1.20, 0.15, 0xB87542),
        BLAZE(1.10, 1.05, 1.23, 1.08, 0.30, 0xFFA327),
        EMERALD(1.30, 0.85, 1.10, 1.05, 0.15, 0x51D86E),
        MITHRIL(1.95, 0.65, 1.24, 1.12, 0.35, 0x8CE8EE),
        COAL(0.85, 1.05, 1.00, 1.10, 0.10, 0x56535B),
        LAPIS(1.10, 0.90, 0.94, 1.08, 0.15, 0x426FC0),
        REDSTONE(0.95, 1.05, 0.96, 1.20, 0.15, 0xC84038),
        QUARTZ(1.15, 0.90, 1.15, 1.05, 0.25, 0xEBE1D7),
        AMETHYST(1.20, 0.85, 1.12, 1.04, 0.30, 0xBD8FD4),
        WITHERITE(1.90, 0.68, 1.30, 0.94, 0.35, 0x697981),
        ENDERITE(1.85, 0.61, 1.26, 1.08, 0.40, 0x65B5A3),
        IGNITIUM(2.10, 0.57, 1.40, 0.97, 0.35, 0xEF9245),
        CURSIUM(1.80, 0.65, 1.28, 1.14, 0.30, 0x89DDD3),
        ANCIENT_METAL(1.60, 0.75, 1.16, 1.03, 0.25, 0xA3A27C),
        BLACK_STEEL(1.85, 0.78, 1.32, 0.90, 0.30, 0x596273),
        BOSS_FROST(1.75, 0.72, 1.22, 0.98, 0.30, 0xB8EAFF),
        BOSS_BUTCHER(1.90, 0.78, 1.38, 0.86, 0.35, 0xB93F55),
        BOSS_TEA(1.45, 0.60, 1.04, 1.16, 0.25, 0x72B46B),
        BOSS_WINE(1.25, 0.78, 1.12, 1.18, 0.20, 0x9D67C6),
        BOSS_CHEESE(1.55, 0.70, 1.20, 1.06, 0.25, 0xECBF58),
        BOSS_CAPACITOR(1.85, 0.74, 1.28, 1.04, 0.30, 0x50CEE0),
        BOSS_SPORE(1.60, 0.55, 1.06, 1.05, 0.20, 0xB999E8),
        BOSS_APPLE(1.40, 0.68, 1.14, 1.12, 0.20, 0xF3C454),
        BOSS_SHEPHERD(1.70, 0.66, 1.12, 1.06, 0.35, 0xB9CFAA),
        BOSS_TIDE(1.55, 0.70, 1.18, 1.10, 0.40, 0x5EABD9),
        END_ROD(1.05, 0.80, 0.94, 1.24, 0.35, 0xE7D9F5),
        IRON_BARS(1.45, 0.82, 1.20, 0.88, 0.30, 0xB8C8CC),
        WOOD_FENCE(0.85, 1.00, 0.90, 1.12, 0.25, 0xDBBE94),
        BAMBOO_FENCE(0.75, 1.08, 0.82, 1.27, 0.20, 0xDBD99A),
        NETHER_FENCE(1.40, 0.90, 1.26, 0.87, 0.30, 0xC59DAD),
        STONE_WALL(1.55, 1.00, 1.22, 0.82, 0.20, 0xC7CCC9),
        BRICK_WALL(1.65, 0.90, 1.25, 0.84, 0.20, 0xD9B2A4),
        BLACKSTONE_WALL(1.80, 0.85, 1.32, 0.78, 0.25, 0xAEA8BD),
        DEEPSLATE_WALL(1.95, 0.78, 1.34, 0.75, 0.25, 0xA9CBD3),
        CHAIN(1.25, 0.68, 0.95, 1.16, 0.35, 0xB8C8CC),
        BAMBOO(0.70, 1.10, 0.80, 1.30, 0.20, 0xD5DF9C),
        LIGHTNING_ROD(1.15, 0.78, 1.05, 1.14, 0.30, 0xE7BEA0),
        SLIME(0.80, 0.60, 0.82, 1.18, 0.10, 0xB7DFAD),
        HONEYCOMB(0.90, 0.58, 0.90, 1.08, 0.10, 0xF0D29F),
        PRISMARINE(1.30, 0.80, 1.15, 1.05, 0.25, 0xA5DCD5),
        GLOWSTONE(0.85, 1.15, 1.08, 1.12, 0.15, 0xEFE0AA),
        ECHO_SHARD(1.25, 0.70, 1.16, 0.98, 0.30, 0xA9CBD3),
        DIAMOND_ALLOY(1.0, 1.0, 1.0, 1.0, 0.0, 0x79D5CC);

        public final double durability;
        public final double wear;
        public final double damage;
        public final double speed;
        public final double reach;
        public final int color;

        Material(double durability, double wear, double damage, double speed, double reach, int color) {
            this.durability = durability;
            this.wear = wear;
            this.damage = damage;
            this.speed = speed;
            this.reach = reach;
            this.color = color;
        }

        public String id() { return name().toLowerCase(java.util.Locale.ROOT); }

        public static Material byId(String id) {
            for (Material material : values()) if (material.id().equals(id)) return material;
            return ORIGINAL;
        }
    }

    public record Build(double durability, double wear, double damage, double speed, double reach) {}
    public record Wear(int damage, double carry) {}

    private FoodWeaponBuildRules() {}

    public static int evolutionCost(int level) {
        return COSTS[Math.max(0, Math.min(COSTS.length - 1, level))];
    }

    public static int successPercent(int level, int quality, boolean blankBlade) {
        return switch (Math.max(0, level)) {
            case 0 -> 90;
            case 1 -> 80;
            case 2 -> 65;
            case 3 -> 50;
            case 4 -> 35;
            default -> 20;
        };
    }

    public static boolean succeeds(int roll, int chance) {
        if (roll < 0 || roll >= 100) throw new IllegalArgumentException("roll must be in [0,100)");
        return roll < Math.max(0, Math.min(100, chance));
    }

    public static Build build(List<Material> materials) {
        if (materials.size() != Part.values().length) throw new IllegalArgumentException("three parts required");
        double durability = 0.0;
        double wear = 0.0;
        double damage = 0.0;
        double speed = 0.0;
        double reach = 0.0;
        for (Part part : Part.values()) {
            Material material = materials.get(part.ordinal());
            durability += material.durability * part.durabilityWeight;
            wear += material.wear * part.durabilityWeight;
            damage += material.damage * part.damageWeight;
            speed += material.speed * part.speedWeight;
            reach += material.reach * part.reachWeight;
        }
        return new Build(WeaponDurabilityRules.multiplier(WeaponDurabilityRules.count(materials)), wear, damage, speed, Math.min(0.75, reach));
    }

    public static int maxDurability(int original, Build build) {
        if (original <= 0) return original;
        return (int) Math.max(1L, Math.min(100000L, Math.round(original * build.durability)));
    }

    public static int preserveDamage(int damage, int oldMax, int newMax) {
        if (oldMax <= 0 || newMax <= 0) return Math.max(0, damage);
        return Math.max(0, Math.min(newMax, (int) Math.ceil((double) Math.max(0, damage) * newMax / oldMax)));
    }

    public static Wear wear(int requested, double multiplier, double carry) {
        if (requested <= 0) return new Wear(requested, carry);
        double safeCarry = Double.isFinite(carry) ? Math.max(0.0, Math.min(0.999999, carry)) : 0.0;
        double safeMultiplier = Double.isFinite(multiplier) ? Math.max(0.1, Math.min(3.0, multiplier)) : 1.0;
        double total = requested * safeMultiplier + safeCarry;
        double settled = Math.floor(total + 1.0E-9);
        int damage = (int) Math.min(Integer.MAX_VALUE, settled);
        return new Wear(damage, Math.max(0.0, total - settled));
    }
}
