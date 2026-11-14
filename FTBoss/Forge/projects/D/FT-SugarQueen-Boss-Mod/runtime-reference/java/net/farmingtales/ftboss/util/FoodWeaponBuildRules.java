/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.farmingtales.ftboss.util.WeaponDurabilityRules;

public final class FoodWeaponBuildRules {
    public static final int REGULAR_MAX_LEVEL = 4;
    public static final int REGULAR_SOCKET_CAPACITY = 10;
    public static final int MAX_BLESSINGS = 6;
    public static final int DAILY_TASKS = 12;
    public static final int DAILY_EVOLUTION_TASKS = 3;
    private static final int[] COSTS = new int[]{100, 250, 450, 700, 1000, 1350, 1750, 2200};

    private FoodWeaponBuildRules() {
    }

    public static int evolutionCost(int n) {
        return COSTS[Math.max(0, Math.min(COSTS.length - 1, n))];
    }

    public static int successPercent(int n, int n2, boolean bl) {
        long l = Math.max(0L, (long)n) * (bl ? 7L : 15L);
        int n3 = switch (Math.max(0, Math.min(3, n2))) {
            case 1 -> 8;
            case 2 -> 15;
            case 3 -> 22;
            default -> 0;
        };
        return (int)Math.max(10L, Math.min(100L, 85L + (long)n3 - l));
    }

    public static boolean succeeds(int n, int n2) {
        if (n < 0 || n >= 100) {
            throw new IllegalArgumentException("roll must be in [0,100)");
        }
        return n < Math.max(0, Math.min(100, n2));
    }

    public static int maxDurability(int n, Build build) {
        if (n <= 0) {
            return n;
        }
        return (int)Math.max(1L, Math.min(100000L, Math.round((double)n * build.durability)));
    }

    public static int preserveDamage(int n, int n2, int n3) {
        if (n2 <= 0 || n3 <= 0) {
            return Math.max(0, n);
        }
        return Math.max(0, Math.min(n3, (int)Math.ceil((double)Math.max(0, n) * (double)n3 / (double)n2)));
    }

    public static Wear wear(int n, double d, double d2) {
        if (n <= 0) {
            return new Wear(n, d2);
        }
        double d3 = Double.isFinite(d2) ? Math.max(0.0, Math.min(0.999999, d2)) : 0.0;
        double d4 = Double.isFinite(d) ? Math.max(0.1, Math.min(3.0, d)) : 1.0;
        double d5 = (double)n * d4 + d3;
        double d6 = Math.floor(d5 + 1.0E-9);
        int n2 = (int)Math.min(2.147483647E9, d6);
        return new Wear(n2, Math.max(0.0, d5 - d6));
    }

    public static Build build(List<Material> list) {
        if (list.size() != Part.values().length) {
            throw new IllegalArgumentException("three parts required");
        }
        double d = 0.0;
        double d2 = 0.0;
        double d3 = 0.0;
        double d4 = 0.0;
        double d5 = 0.0;
        for (Part part : Part.values()) {
            Material material = list.get(part.ordinal());
            d += material.durability * part.durabilityWeight;
            d2 += material.wear * part.durabilityWeight;
            d3 += material.damage * part.damageWeight;
            d4 += material.speed * part.speedWeight;
            d5 += material.reach * part.reachWeight;
        }
        return new Build(WeaponDurabilityRules.multiplier(WeaponDurabilityRules.count(list)), d2, d3, d4, Math.min(0.75, d5));
    }

    public static enum Part {
        EDGE(0.7, 0.5, 0.6, 0.7),
        GRIP(0.15, 0.4, 0.25, 0.2),
        CORE(0.15, 0.1, 0.15, 0.1);

        public final double damageWeight;
        public final double speedWeight;
        public final double durabilityWeight;
        public final double reachWeight;

        private Part(double d, double d2, double d3, double d4) {
            this.damageWeight = d;
            this.speedWeight = d2;
            this.durabilityWeight = d3;
            this.reachWeight = d4;
        }

        public static Part byIndex(int n) {
            return n < 0 || n >= Part.values().length ? null : Part.values()[n];
        }
    }

    public static final class Material
    extends Enum<Material> {
        public static final /* enum */ Material ORIGINAL = new Material(1.0, 1.0, 1.0, 1.0, 0.0, 0xBBBBBB);
        public static final /* enum */ Material WOOD = new Material(0.7, 1.15, 0.8, 1.15, 0.1, 11501643);
        public static final /* enum */ Material STONE = new Material(1.0, 1.1, 1.1, 0.88, 0.2, 0x999999);
        public static final /* enum */ Material IRON = new Material(1.35, 0.85, 1.15, 0.98, 0.25, 13818590);
        public static final /* enum */ Material COPPER = new Material(1.05, 0.9, 1.02, 1.08, 0.2, 14386760);
        public static final /* enum */ Material GOLD = new Material(0.65, 1.25, 0.95, 1.3, 0.05, 16763464);
        public static final /* enum */ Material DIAMOND = new Material(1.8, 0.7, 1.3, 1.02, 0.3, 5102805);
        public static final /* enum */ Material NETHERITE = new Material(2.2, 0.55, 1.42, 0.95, 0.4, 0x766677);
        public static final /* enum */ Material BONE = new Material(0.85, 0.95, 1.05, 1.1, 0.4, 15327163);
        public static final /* enum */ Material OBSIDIAN = new Material(1.7, 0.8, 1.38, 0.75, 0.3, 4798561);
        public static final /* enum */ Material LEATHER = new Material(0.8, 0.65, 0.82, 1.2, 0.15, 12088642);
        public static final /* enum */ Material BLAZE = new Material(1.1, 1.05, 1.23, 1.08, 0.3, 16753447);
        public static final /* enum */ Material EMERALD = new Material(1.3, 0.85, 1.1, 1.05, 0.15, 5363822);
        public static final /* enum */ Material MITHRIL = new Material(1.95, 0.65, 1.24, 1.12, 0.35, 0x8CE8EE);
        public static final /* enum */ Material COAL = new Material(0.85, 1.05, 1.0, 1.1, 0.1, 5657435);
        public static final /* enum */ Material LAPIS = new Material(1.1, 0.9, 0.94, 1.08, 0.15, 4353984);
        public static final /* enum */ Material REDSTONE = new Material(0.95, 1.05, 0.96, 1.2, 0.15, 13123640);
        public static final /* enum */ Material QUARTZ = new Material(1.15, 0.9, 1.15, 1.05, 0.25, 15458775);
        public static final /* enum */ Material AMETHYST = new Material(1.2, 0.85, 1.12, 1.04, 0.3, 12423124);
        public static final /* enum */ Material WITHERITE = new Material(1.9, 0.68, 1.3, 0.94, 0.35, 6912385);
        public static final /* enum */ Material ENDERITE = new Material(1.85, 0.61, 1.26, 1.08, 0.4, 6665635);
        public static final /* enum */ Material IGNITIUM = new Material(2.1, 0.57, 1.4, 0.97, 0.35, 15700549);
        public static final /* enum */ Material CURSIUM = new Material(1.8, 0.65, 1.28, 1.14, 0.3, 9035219);
        public static final /* enum */ Material ANCIENT_METAL = new Material(1.6, 0.75, 1.16, 1.03, 0.25, 10723964);
        public static final /* enum */ Material BLACK_STEEL = new Material(1.85, 0.78, 1.32, 0.9, 0.3, 5857907);
        public static final /* enum */ Material BOSS_FROST = new Material(1.75, 0.72, 1.22, 0.98, 0.3, 12118783);
        public static final /* enum */ Material BOSS_BUTCHER = new Material(1.9, 0.78, 1.38, 0.86, 0.35, 12140373);
        public static final /* enum */ Material BOSS_TEA = new Material(1.45, 0.6, 1.04, 1.16, 0.25, 7517291);
        public static final /* enum */ Material BOSS_WINE = new Material(1.25, 0.78, 1.12, 1.18, 0.2, 10315718);
        public static final /* enum */ Material BOSS_CHEESE = new Material(1.55, 0.7, 1.2, 1.06, 0.25, 15515480);
        public static final /* enum */ Material BOSS_CAPACITOR = new Material(1.85, 0.74, 1.28, 1.04, 0.3, 5295840);
        public static final /* enum */ Material BOSS_SPORE = new Material(1.6, 0.55, 1.06, 1.05, 0.2, 12163560);
        public static final /* enum */ Material BOSS_APPLE = new Material(1.4, 0.68, 1.14, 1.12, 0.2, 15975508);
        public static final /* enum */ Material BOSS_SHEPHERD = new Material(1.7, 0.66, 1.12, 1.06, 0.35, 12177322);
        public static final /* enum */ Material BOSS_TIDE = new Material(1.55, 0.7, 1.18, 1.1, 0.4, 6204377);
        public final double durability;
        public final double wear;
        public final double damage;
        public final double speed;
        public final double reach;
        public final int color;
        private static final /* synthetic */ Material[] $VALUES;
        public static final /* enum */ Material DIAMOND_ALLOY;

        public static Material[] values() {
            return (Material[])$VALUES.clone();
        }

        public static Material valueOf(String string) {
            return Enum.valueOf(Material.class, string);
        }

        private Material(double d, double d2, double d3, double d4, double d5, int n2) {
            this.durability = d;
            this.wear = d2;
            this.damage = d3;
            this.speed = d4;
            this.reach = d5;
            this.color = n2;
        }

        public String id() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public static Material byId(String string) {
            for (Material material : Material.values()) {
                if (!material.id().equals(string)) continue;
                return material;
            }
            return ORIGINAL;
        }

        private static /* synthetic */ Material[] $values() {
            return new Material[]{ORIGINAL, WOOD, STONE, IRON, COPPER, GOLD, DIAMOND, NETHERITE, BONE, OBSIDIAN, LEATHER, BLAZE, EMERALD, MITHRIL, COAL, LAPIS, REDSTONE, QUARTZ, AMETHYST, WITHERITE, ENDERITE, IGNITIUM, CURSIUM, ANCIENT_METAL, BLACK_STEEL, BOSS_FROST, BOSS_BUTCHER, BOSS_TEA, BOSS_WINE, BOSS_CHEESE, BOSS_CAPACITOR, BOSS_SPORE, BOSS_APPLE, BOSS_SHEPHERD, BOSS_TIDE};
        }

        static {
            $VALUES = Material.$values();
            DIAMOND_ALLOY = new Material(1.0, 1.0, 1.0, 1.0, 0.0, 7984588);
            Material[] materialArray = Arrays.copyOf($VALUES, 36);
            materialArray[35] = DIAMOND_ALLOY;
            $VALUES = materialArray;
        }
    }

    public record Build(double durability, double wear, double damage, double speed, double reach) {
    }

    public record Wear(int damage, double carry) {
    }
}

