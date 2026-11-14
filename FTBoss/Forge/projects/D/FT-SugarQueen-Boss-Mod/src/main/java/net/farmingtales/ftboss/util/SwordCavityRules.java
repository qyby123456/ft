package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;

/** 剑容器完整性纯规则；2026-10-13 不绑定必需物品，允许空腔编辑，实际使用门槛由剑体功能规则独立校验。 */
public final class SwordCavityRules {
    public static final int VERSION = 2;
    public static final int COLUMNS = 9;
    public static final int ROWS = 3;
    public static final int CELL_COUNT = COLUMNS * ROWS;
    public static final int INVENTORY_SIZE = 36;
    public static final int CELL_SIZE = 18;
    public static final int MATERIAL_STACK_LIMIT = 3;
    public static final double FREE_DURABILITY_WEIGHT = 0.12;
    public static final double FREE_DAMAGE_WEIGHT = 0.08;
    public static final double FREE_SPEED_WEIGHT = 0.08;
    public static final double FREE_REACH_WEIGHT = 0.15;
    public static final double MIN_DURABILITY = 0.50;
    public static final double MAX_DURABILITY = 3.00;
    public static final double MIN_WEAR = 0.35;
    public static final double MAX_WEAR = 2.00;
    public static final double MIN_DAMAGE = 0.50;
    public static final double MAX_DAMAGE = 2.00;
    public static final double MIN_SPEED = 0.50;
    public static final double MAX_SPEED = 1.50;
    public static final double MAX_REACH = 0.75;

    public enum State { NATIVE, INSTALLED, MISSING, EMPTY, INVALID }

    public enum Slot {
        BLADE(false, Part.EDGE),
        FRAME(false, null),
        GRIP(false, Part.GRIP),
        BINDING(false, null),
        CORE(false, Part.CORE),
        GUARD(false, null),
        POMMEL(false, null),
        INLAY(false, null),
        FREE_01(false, null), FREE_02(false, null), FREE_03(false, null),
        FREE_04(false, null), FREE_05(false, null), FREE_06(false, null),
        FREE_07(false, null), FREE_08(false, null), FREE_09(false, null),
        FREE_10(false, null), FREE_11(false, null), FREE_12(false, null),
        FREE_13(false, null), FREE_14(false, null), FREE_15(false, null),
        FREE_16(false, null), FREE_17(false, null), FREE_18(false, null),
        FREE_19(false, null);

        public final boolean required;
        public final Part legacyPart;
        public final int column;
        public final int row;
        public final double durabilityWeight;
        public final double damageWeight;
        public final double speedWeight;
        public final double reachWeight;

        Slot(boolean required, Part legacyPart) {
            this.required = required;
            this.legacyPart = legacyPart;
            this.column = ordinal() % COLUMNS;
            this.row = ordinal() / COLUMNS;
            this.durabilityWeight = legacyPart == null ? FREE_DURABILITY_WEIGHT : 0.0;
            this.damageWeight = legacyPart == null ? FREE_DAMAGE_WEIGHT : 0.0;
            this.speedWeight = legacyPart == null ? FREE_SPEED_WEIGHT : 0.0;
            this.reachWeight = legacyPart == null ? FREE_REACH_WEIGHT : 0.0;
        }

        public String id() { return name().toLowerCase(java.util.Locale.ROOT); }
        public static Slot byIndex(int index) { return index >= 0 && index < CELL_COUNT ? values()[index] : null; }
    }

    public record Cell(State state, Material material) {
        public boolean present() { return state == State.NATIVE || state == State.INSTALLED; }
        public boolean installed() { return state == State.INSTALLED && material != null && material != Material.ORIGINAL; }
    }

    public enum Link {
        FROST_EDGE(0.16, -0.12, 0.00, 0.00, 0.00),
        COPPER_CIRCUIT(0.00, 0.00, 0.00, 0.08, 0.20),
        BUTCHER_EDGE(0.00, -0.06, 0.16, 0.00, 0.00),
        TEA_WEAVE(0.00, -0.10, 0.00, 0.12, 0.00),
        WINE_FEAST(0.00, 0.00, 0.10, 0.08, 0.00),
        SPORE_ROOT(0.20, -0.08, 0.00, 0.00, 0.00),
        SHEPHERD_PACT(0.12, 0.00, 0.08, 0.00, 0.00),
        TIDAL_CHANNEL(0.00, -0.12, 0.00, 0.00, 0.16),
        EMBER_FORGE(0.12, 0.00, 0.16, 0.00, 0.00),
        ARCANE_LATTICE(0.00, -0.08, 0.00, 0.10, 0.10),
        ANCIENT_ALLOY(0.16, -0.08, 0.00, 0.00, 0.00),
        GEM_CROWN(0.00, 0.00, 0.08, 0.06, 0.08),
        FORGED_FRAME(0.00, -0.03, 0.00, 0.00, 0.00),
        HONED_INLAY(0.00, 0.00, 0.03, 0.00, 0.00),
        BALANCED_FRAME(0.00, 0.00, 0.00, 0.02, 0.00),
        SUPPLE_BINDING(0.00, -0.02, 0.00, 0.00, 0.00),
        CONDUCTIVE_CORE(0.00, 0.00, 0.00, 0.03, 0.00),
        MINERAL_RESONANCE(0.00, 0.00, 0.02, 0.00, 0.00),
        ROOTED_FRAME(0.00, -0.03, 0.00, 0.00, 0.00),
        PADDED_GUARD(0.00, -0.02, 0.00, 0.00, 0.00),
        LIVING_FOCUS(0.00, 0.00, 0.00, 0.02, 0.00),
        PRISMATIC_CHANNEL(0.00, 0.00, 0.00, 0.00, 0.04),
        FIELD_BINDING(0.00, -0.02, 0.00, 0.00, 0.00),
        KINDLED_GRIP(0.00, 0.00, 0.00, 0.02, 0.00);

        public final double durabilityBonus;
        public final double wearBonus;
        public final double damageBonus;
        public final double speedBonus;
        public final double reachBonus;

        Link(double durabilityBonus, double wearBonus, double damageBonus, double speedBonus, double reachBonus) {
            this.durabilityBonus = durabilityBonus;
            this.wearBonus = wearBonus;
            this.damageBonus = damageBonus;
            this.speedBonus = speedBonus;
            this.reachBonus = reachBonus;
        }
    }

    private SwordCavityRules() {}

    public static Cell nativeCell(Slot slot, Material nativeMaterial) {
        return new Cell(State.EMPTY, Material.ORIGINAL);
    }

    public static boolean usable(List<Cell> cells) {
        if (cells == null || cells.size() != CELL_COUNT) return false;
        for (Slot slot : Slot.values()) {
            Cell cell = cells.get(slot.ordinal());
            if (cell == null || cell.state() == null || cell.state() == State.INVALID || cell.material() == null
                    || cell.state() == State.INSTALLED && !accepts(slot, cell.material())) return false;
        }
        return true;
    }

    public static boolean accepts(Slot slot, Material material) {
        return slot != null && material != null && material != Material.ORIGINAL;
    }

    public static Slot hit(int originX, int originY, double mouseX, double mouseY) {
        if (!Double.isFinite(mouseX) || !Double.isFinite(mouseY)) return null;
        for (Slot slot : Slot.values()) {
            long left = (long) originX + slot.column * CELL_SIZE;
            long top = (long) originY + slot.row * CELL_SIZE;
            if (mouseX >= left && mouseX < left + CELL_SIZE && mouseY >= top && mouseY < top + CELL_SIZE) return slot;
        }
        return null;
    }

    public static List<Link> links(List<Cell> cells) {
        if (!usable(cells)) return List.of();
        EnumSet<Material> materials = EnumSet.noneOf(Material.class);
        for (Cell cell : cells) if (cell.installed()) materials.add(cell.material());
        List<Link> result = new ArrayList<>();
        for (Link link : Link.values()) {
            boolean matches = true;
            for (List<Material> alternatives : linkRequirements(link)) {
                boolean present = false;
                for (Material material : alternatives) if (materials.contains(material)) present = true;
                if (!present) {
                    matches = false;
                    break;
                }
            }
            if (matches) result.add(link);
        }
        return List.copyOf(result);
    }

    private static boolean hasAny(EnumSet<Material> materials, Material first, Material second) {
        return materials.contains(first) || materials.contains(second);
    }

    public static List<List<Material>> linkRequirements(Link link) {
        return switch (link.name()) {
            case "FROST_EDGE" -> List.of(List.of(Material.BOSS_FROST), List.of(Material.DIAMOND));
            case "COPPER_CIRCUIT" -> List.of(List.of(Material.BOSS_CAPACITOR), List.of(Material.COPPER, Material.REDSTONE));
            case "BUTCHER_EDGE" -> List.of(List.of(Material.BOSS_BUTCHER), List.of(Material.BLACK_STEEL, Material.WITHERITE));
            case "TEA_WEAVE" -> List.of(List.of(Material.BOSS_TEA), List.of(Material.WOOD, Material.LEATHER));
            case "WINE_FEAST" -> List.of(List.of(Material.BOSS_WINE), List.of(Material.BOSS_APPLE, Material.BOSS_CHEESE));
            case "SPORE_ROOT" -> List.of(List.of(Material.BOSS_SPORE), List.of(Material.WOOD, Material.BONE));
            case "SHEPHERD_PACT" -> List.of(List.of(Material.BOSS_SHEPHERD), List.of(Material.BONE, Material.BOSS_CHEESE));
            case "TIDAL_CHANNEL" -> List.of(List.of(Material.BOSS_TIDE), List.of(Material.QUARTZ, Material.ENDERITE));
            case "EMBER_FORGE" -> List.of(List.of(Material.IGNITIUM, Material.BLAZE), List.of(Material.OBSIDIAN, Material.NETHERITE));
            case "ARCANE_LATTICE" -> List.of(List.of(Material.MITHRIL, Material.AMETHYST), List.of(Material.LAPIS, Material.ENDERITE));
            case "ANCIENT_ALLOY" -> List.of(List.of(Material.ANCIENT_METAL), List.of(Material.IRON, Material.BLACK_STEEL));
            case "GEM_CROWN" -> List.of(List.of(Material.GOLD), List.of(Material.EMERALD, Material.DIAMOND));
            default -> CommonMaterialBondRules.requirements(link.name());
        };
    }

    public static List<Link> participatingLinks(Material material) {
        if (material == null || material == Material.ORIGINAL) return List.of();
        List<Link> result = new ArrayList<>();
        for (Link link : Link.values()) {
            if (linkRequirements(link).stream().anyMatch(group -> group.contains(material))) result.add(link);
        }
        return List.copyOf(result);
    }

    public static Material installed(List<Cell> cells, Slot slot) {
        if (cells == null || cells.size() != CELL_COUNT || slot == null) return Material.ORIGINAL;
        Cell cell = cells.get(slot.ordinal());
        return cell != null && cell.installed() ? cell.material() : Material.ORIGINAL;
    }

    public static Slot lossSlot(List<Cell> cells) {
        if (!usable(cells)) return null;
        for (int index = CELL_COUNT - 1; index >= 4; index--) {
            Cell cell = cells.get(index);
            if (cell != null && cell.installed()) return Slot.byIndex(index);
        }
        for (int index = 3; index >= 0; index--) {
            Cell cell = cells.get(index);
            if (cell != null && cell.installed()) return Slot.byIndex(index);
        }
        for (int index = CELL_COUNT - 1; index >= 0; index--) {
            Cell cell = cells.get(index);
            if (cell != null && cell.present()) return Slot.byIndex(index);
        }
        return null;
    }

    public static Build combine(Build legacy, List<Cell> cells) {
        return combine(legacy, cells, true);
    }

    public static Build combine(Build legacy, List<Cell> cells, boolean linksEnabled) {
        return applyMaterials(legacy, cells, linksEnabled, false);
    }

    public static Build gridBuild(List<Cell> cells, boolean linksEnabled) {
        return applyMaterials(new Build(1.0, 1.0, 1.0, 1.0, 0.0), cells, linksEnabled, true);
    }

    private static Build applyMaterials(Build legacy, List<Cell> cells, boolean linksEnabled, boolean allSlots) {
        Build base = legacy == null ? new Build(1.0, 1.0, 1.0, 1.0, 0.0) : legacy;
        double durability = bounded(base.durability(), MIN_DURABILITY, MAX_DURABILITY, 1.0);
        int alloyParts = 0;
        if (cells != null && cells.size() == Slot.values().length) for (Slot slot : Slot.values()) {
            Cell cell = cells.get(slot.ordinal());
            if (cell != null && (allSlots || slot.legacyPart == null) && cell.installed() && cell.material() == Material.DIAMOND_ALLOY) alloyParts++;
        }
        durability = Math.min(MAX_DURABILITY, Math.max(1.0, durability) + (WeaponDurabilityRules.multiplier(alloyParts) - 1.0));
        double wear = bounded(base.wear(), MIN_WEAR, MAX_WEAR, 1.0);
        double damage = bounded(base.damage(), MIN_DAMAGE, MAX_DAMAGE, 1.0);
        double speed = bounded(base.speed(), MIN_SPEED, MAX_SPEED, 1.0);
        double reach = bounded(base.reach(), 0.0, MAX_REACH, 0.0);
        if (!usable(cells)) return new Build(durability, wear, 0.0, 0.0, 0.0);
        int[] counts = new int[Material.values().length];
        for (Slot slot : Slot.values()) {
            Cell cell = cells.get(slot.ordinal());
            if ((allSlots || slot.legacyPart == null) && cell.installed()) counts[cell.material().ordinal()]++;
        }
        for (Material material : Material.values()) {
            int count = Math.min(MATERIAL_STACK_LIMIT, counts[material.ordinal()]);
            double strength = count == 0 ? 0.0 : count == 1 ? 1.0 : count == 2 ? 1.5 : 1.75;
            wear += wearBonus(material.wear - 1.0) * FREE_DURABILITY_WEIGHT * strength;
            damage += materialBonus(material.damage - 1.0) * FREE_DAMAGE_WEIGHT * strength;
            speed += materialBonus(material.speed - 1.0) * FREE_SPEED_WEIGHT * strength;
            reach += materialBonus(material.reach) * FREE_REACH_WEIGHT * strength;
        }
        for (Link link : linksEnabled ? links(cells) : List.<Link>of()) {
            wear += link.wearBonus;
            damage += link.damageBonus;
            speed += link.speedBonus;
            reach += link.reachBonus;
        }
        return new Build(bounded(durability, MIN_DURABILITY, MAX_DURABILITY, 1.0),
                bounded(wear, MIN_WEAR, MAX_WEAR, 1.0), bounded(damage, MIN_DAMAGE, MAX_DAMAGE, 1.0),
                bounded(speed, MIN_SPEED, MAX_SPEED, 1.0), bounded(reach, 0.0, MAX_REACH, 0.0));
    }

    public static double materialBonus(double delta) {
        return delta > 0.0 ? delta * 2.0 : delta;
    }

    public static double wearBonus(double delta) {
        return delta < 0.0 ? delta * 2.0 : delta;
    }

    private static double bounded(double value, double minimum, double maximum, double fallback) {
        return Double.isFinite(value) ? Math.max(minimum, Math.min(maximum, value)) : fallback;
    }
}
