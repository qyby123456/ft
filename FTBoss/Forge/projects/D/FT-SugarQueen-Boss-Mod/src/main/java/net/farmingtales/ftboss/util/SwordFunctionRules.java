package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;

/** 剑体五项功能纯规则；2026-10-13 铁剑九格起步十七格封顶，占用解锁格的少量基值叠加物品/材料乘位置权重，缺功能不等于耐久损坏。 */
public final class SwordFunctionRules {
    private static final Pattern ITEM_IDENTIFIER = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    private static final double EPSILON = 0.000000001;
    private static final double MAX_INPUT = 64.0;
    private static final double MAX_VALUE = 1000000.0;
    private static final double MAX_MINIMUM = 1024.0;
    public static final int MAX_EVOLUTION = 4;
    public static final Values ZERO = new Values(0, 0, 0, 0, 0);
    private static final Profile IRON = ironProfile();

    public enum Function { HANDLE, GUARD, CORE, STABILITY, BLADE }

    public record Values(double handle, double guard, double core, double stability, double blade) {
        public Values {
            for (double value : new double[]{handle, guard, core, stability, blade}) {
                if (!Double.isFinite(value) || value < 0 || value > MAX_VALUE) throw new IllegalArgumentException("Invalid function value");
            }
        }

        public double value(Function function) {
            return switch (Objects.requireNonNull(function)) {
                case HANDLE -> handle;
                case GUARD -> guard;
                case CORE -> core;
                case STABILITY -> stability;
                case BLADE -> blade;
            };
        }
    }

    public record Input(String itemId, Material material, boolean present) {}

    public record Profile(Values minimum, List<Values> cellWeights, List<Integer> unlockLevels, Map<Material, Values> materials,
                          List<Values> cellBaseValues, Map<String, Values> itemValues) {
        public Profile {
            Objects.requireNonNull(minimum);
            cellWeights = List.copyOf(cellWeights);
            unlockLevels = List.copyOf(unlockLevels);
            materials = Map.copyOf(materials);
            cellBaseValues = List.copyOf(cellBaseValues);
            itemValues = Map.copyOf(itemValues);
            if (cellWeights.size() != SwordCavityRules.CELL_COUNT || unlockLevels.size() != SwordCavityRules.CELL_COUNT
                    || cellBaseValues.size() != SwordCavityRules.CELL_COUNT || materials.isEmpty() && itemValues.isEmpty()
                    || materials.containsKey(Material.ORIGINAL)) throw new IllegalArgumentException("Invalid function profile");
            for (Function function : Function.values()) if (minimum.value(function) > MAX_MINIMUM) throw new IllegalArgumentException("Minimum exceeds limit");
            for (Values weights : cellWeights) validateInput(weights);
            for (Values values : cellBaseValues) validateInput(values);
            for (Values values : materials.values()) validateInput(values);
            for (Map.Entry<String, Values> entry : itemValues.entrySet()) {
                if (!validItemId(entry.getKey())) throw new IllegalArgumentException("Invalid item identifier");
                validateInput(entry.getValue());
            }
            for (int level : unlockLevels) if (level < -1 || level > MAX_EVOLUTION) throw new IllegalArgumentException("Unlock level must be -1..4");
        }

        public Profile(Values minimum, List<Values> cellWeights, List<Integer> unlockLevels, Map<Material, Values> materials) {
            this(minimum, cellWeights, unlockLevels, materials, java.util.Collections.nCopies(SwordCavityRules.CELL_COUNT, ZERO), Map.of());
        }

        public int capacity() { return (int) unlockLevels.stream().filter(level -> level >= 0).count(); }

        public Profile withItemValues(Map<String, Values> overrides) {
            Map<String, Values> merged = new java.util.HashMap<>(itemValues);
            merged.putAll(Objects.requireNonNull(overrides));
            return new Profile(minimum, cellWeights, unlockLevels, materials, cellBaseValues, merged);
        }
    }

    public record Status(boolean valid, Values total, Values minimum, List<Function> missing, int unlockedCells) {
        public Status { missing = List.copyOf(missing); }
        public boolean ready() { return valid && missing.isEmpty(); }
        public boolean sufficient(Function function) { return valid && total.value(function) + EPSILON >= minimum.value(function); }
    }

    private SwordFunctionRules() {}

    private static void validateInput(Values values) {
        for (Function function : Function.values()) if (values.value(function) > MAX_INPUT) throw new IllegalArgumentException("Function input exceeds limit");
    }

    private static boolean validItemId(String identifier) {
        return identifier != null && !identifier.equals("minecraft:air") && ITEM_IDENTIFIER.matcher(identifier).matches();
    }

    public static Profile iron() { return IRON; }

    public static boolean unlocked(Profile profile, int index, int evolution) {
        return index >= 0 && index < SwordCavityRules.CELL_COUNT && (profile == null
                || profile.unlockLevels().get(index) >= 0
                && profile.unlockLevels().get(index) <= Math.max(0, Math.min(MAX_EVOLUTION, evolution)));
    }

    public static int capacity(Profile profile) { return profile == null ? SwordCavityRules.CELL_COUNT : profile.capacity(); }

    public static int maximumCells(Profile profile) { return capacity(profile); }

    public static int unlockedCells(Profile profile, int evolution) {
        int count = 0;
        for (int index = 0; index < SwordCavityRules.CELL_COUNT; index++) if (unlocked(profile, index, evolution)) count++;
        return count;
    }

    public static Status evaluate(Profile profile, List<Cell> cells, int evolution) {
        boolean valid = SwordCavityRules.usable(cells);
        double[] totals = new double[Function.values().length];
        for (int index = 0; index < SwordCavityRules.CELL_COUNT; index++) {
            if (!unlocked(profile, index, evolution)) continue;
            if (!valid || profile == null || !cells.get(index).present()) continue;
            Values material = profile.materials().getOrDefault(cells.get(index).material(), ZERO);
            addContribution(profile, index, material, totals);
        }
        return status(profile, valid, totals, evolution);
    }

    public static Status evaluateItems(Profile profile, List<Input> items, int evolution) {
        boolean valid = items != null && items.size() == SwordCavityRules.CELL_COUNT;
        if (valid) for (Input input : items) {
            if (input == null || input.present() && (input.material() == null || !validItemId(input.itemId()))) {
                valid = false;
                break;
            }
        }
        double[] totals = new double[Function.values().length];
        if (valid && profile != null) for (int index = 0; index < SwordCavityRules.CELL_COUNT; index++) {
            Input input = items.get(index);
            if (!input.present() || !unlocked(profile, index, evolution)) continue;
            Values material = profile.itemValues().get(input.itemId());
            if (material == null) material = profile.materials().getOrDefault(input.material(), ZERO);
            addContribution(profile, index, material, totals);
        }
        return status(profile, valid, totals, evolution);
    }

    private static void addContribution(Profile profile, int index, Values material, double[] totals) {
        Values weights = profile.cellWeights().get(index);
        Values base = profile.cellBaseValues().get(index);
        for (Function function : Function.values()) totals[function.ordinal()] += base.value(function) + material.value(function) * weights.value(function);
    }

    private static Status status(Profile profile, boolean valid, double[] totals, int evolution) {
        Values total = new Values(totals[0], totals[1], totals[2], totals[3], totals[4]);
        Values minimum = profile == null ? ZERO : profile.minimum();
        List<Function> missing = new ArrayList<>();
        for (Function function : Function.values()) if (!valid || total.value(function) + EPSILON < minimum.value(function)) missing.add(function);
        return new Status(valid, total, minimum, missing, unlockedCells(profile, evolution));
    }

    private static Profile ironProfile() {
        List<Values> weights = new ArrayList<>();
        List<Values> baseValues = new ArrayList<>();
        List<Integer> unlockLevels = new ArrayList<>();
        for (int index = 0; index < SwordCavityRules.CELL_COUNT; index++) {
            int column = SwordNativeMaterialRules.visualColumn(index);
            int row = SwordNativeMaterialRules.visualRow(index);
            Values weight = column == 0 ? new Values(1, 0, 0, 0.35, 0)
                    : column == 1 ? new Values(0.25, 0.25, 0, 1, 0)
                    : column == 2 ? new Values(0, 1, 0, 0.4, 0)
                    : row == 1 && column == 3 ? new Values(0, 0.15, 1, 0.3, 0.15)
                    : row == 1 && column == 4 ? new Values(0, 0.1, 0.25, 1, 0.55)
                    : row == 1 && column == 5 ? new Values(0, 0.05, 0.1, 0.25, 1)
                    : new Values(0, 0.05, 0, 0.2, 1);
            weights.add(weight);
            baseValues.add(column == 0 ? new Values(0.08, 0, 0, 0.03, 0)
                    : column == 1 ? new Values(0.02, 0.02, 0, 0.08, 0)
                    : column == 2 ? new Values(0, 0.08, 0, 0.03, 0)
                    : row == 1 && column == 3 ? new Values(0, 0.01, 0.08, 0.03, 0.02)
                    : row == 1 && column == 4 ? new Values(0, 0.01, 0.02, 0.08, 0.04)
                    : row == 1 && column == 5 ? new Values(0, 0.01, 0.01, 0.02, 0.08)
                    : new Values(0, 0.01, 0, 0.02, 0.06));
            unlockLevels.add(switch (index) {
                case 2, 3, 13, 14, 15, 16, 17, 6, 20 -> 0;
                case 7, 21 -> 1;
                case 8, 22 -> 2;
                case 9, 23 -> 3;
                case 10, 24 -> 4;
                default -> -1;
            });
        }
        Map<Material, Values> materials = new EnumMap<>(Material.class);
        for (Material material : Material.values()) if (material != Material.ORIGINAL) {
            materials.put(material, new Values(0.25, material.durability / 1.35, material.damage / 1.15,
                    material.durability / 1.35, material.damage / 1.15));
        }
        materials.put(Material.WOOD, new Values(1, 0.25, 0.15, 0.35, 0.15));
        materials.put(Material.IRON, new Values(0.25, 1, 1, 1, 1));
        materials.put(Material.STONE, new Values(0.15, 0.85, 0.55, 1.1, 0.75));
        materials.put(Material.BONE, new Values(1, 0.7, 0.4, 0.6, 0.55));
        materials.put(Material.LEATHER, new Values(0.95, 0.5, 0.2, 0.7, 0.05));
        materials.put(Material.COPPER, new Values(0.4, 0.7, 1.1, 0.8, 0.65));
        materials.put(Material.GOLD, new Values(0.3, 0.35, 0.9, 0.4, 0.55));
        materials.put(Material.COAL, new Values(0.05, 0.15, 0.9, 0.25, 0.1));
        materials.put(Material.LAPIS, new Values(0.1, 0.2, 1.4, 0.5, 0.3));
        materials.put(Material.REDSTONE, new Values(0.1, 0.1, 1.6, 0.25, 0.1));
        materials.put(Material.QUARTZ, new Values(0.1, 0.6, 1, 0.7, 1.1));
        materials.put(Material.AMETHYST, new Values(0.1, 0.55, 1.3, 0.7, 0.95));
        materials.put(Material.END_ROD, new Values(1.35, 0.25, 1.15, 0.55, 0.10));
        materials.put(Material.IRON_BARS, new Values(1.30, 1.10, 0.45, 1.25, 0.45));
        materials.put(Material.WOOD_FENCE, new Values(1.25, 0.55, 0.20, 0.85, 0.10));
        materials.put(Material.BAMBOO_FENCE, new Values(1.30, 0.35, 0.15, 0.65, 0.08));
        materials.put(Material.NETHER_FENCE, new Values(1.15, 1.00, 0.65, 1.20, 0.55));
        materials.put(Material.STONE_WALL, new Values(1.10, 1.15, 0.40, 1.50, 0.35));
        materials.put(Material.BRICK_WALL, new Values(1.15, 1.25, 0.50, 1.45, 0.45));
        materials.put(Material.BLACKSTONE_WALL, new Values(1.20, 1.35, 0.65, 1.60, 0.60));
        materials.put(Material.DEEPSLATE_WALL, new Values(1.25, 1.45, 0.55, 1.75, 0.55));
        materials.put(Material.CHAIN, new Values(1.25, 0.70, 0.25, 1.30, 0.10));
        materials.put(Material.BAMBOO, new Values(1.25, 0.20, 0.10, 0.45, 0.08));
        materials.put(Material.LIGHTNING_ROD, new Values(1.25, 0.55, 1.30, 0.90, 0.25));
        materials.put(Material.SLIME, new Values(0.40, 0.30, 0.15, 1.40, 0.03));
        materials.put(Material.HONEYCOMB, new Values(0.45, 0.35, 0.20, 1.50, 0.05));
        materials.put(Material.PRISMARINE, new Values(0.20, 0.65, 1.25, 0.90, 0.85));
        materials.put(Material.GLOWSTONE, new Values(0.10, 0.15, 1.60, 0.30, 0.15));
        materials.put(Material.ECHO_SHARD, new Values(0.15, 0.35, 1.75, 0.80, 0.75));
        Map<String, Values> items = Map.of("minecraft:stick", materials.get(Material.WOOD),
                "minecraft:string", new Values(0.65, 0.35, 0.15, 0.85, 0.05),
                "minecraft:cobblestone", materials.get(Material.STONE), "minecraft:iron_ingot", materials.get(Material.IRON),
                "minecraft:flint", new Values(0.1, 0.45, 0.25, 0.55, 1.3));
        return new Profile(new Values(1.25, 1.33, 1.38, 3.10, 2.13), weights, unlockLevels, materials, baseValues, items);
    }
}
