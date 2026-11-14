/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.WeaponDurabilityRules;

public final class SwordCavityRules {
    public static final int INVENTORY_SIZE = 36;
    public static final int CELL_SIZE = 18;
    public static final double MIN_DURABILITY = 0.5;
    public static final double MAX_SPEED = 1.5;
    public static final int CELL_COUNT = 27;
    public static final int VERSION = 2;
    public static final double MAX_WEAR = 2.0;
    public static final double FREE_DAMAGE_WEIGHT = 0.08;
    public static final int MATERIAL_STACK_LIMIT = 3;
    public static final double FREE_REACH_WEIGHT = 0.15;
    public static final double MAX_DAMAGE = 2.0;
    public static final double FREE_DURABILITY_WEIGHT = 0.12;
    public static final double MAX_REACH = 0.75;
    public static final double MIN_WEAR = 0.35;
    public static final int COLUMNS = 9;
    public static final int ROWS = 3;
    public static final double MIN_DAMAGE = 0.5;
    public static final double MIN_SPEED = 0.5;
    public static final double FREE_SPEED_WEIGHT = 0.08;
    public static final double MAX_DURABILITY = 3.0;

    private SwordCavityRules() {
    }

    public static FoodWeaponBuildRules.Build combine(FoodWeaponBuildRules.Build build, List<Cell> list) {
        return SwordCavityRules.combine(build, list, true);
    }

    public static boolean accepts(Slot slot, FoodWeaponBuildRules.Material material) {
        return slot != null && material != null && material != FoodWeaponBuildRules.Material.ORIGINAL;
    }

    public static Slot hit(int n, int n2, double d, double d2) {
        if (!Double.isFinite(d) || !Double.isFinite(d2)) {
            return null;
        }
        for (Slot slot : Slot.values()) {
            long l = (long)n + (long)(slot.column * 18);
            long l2 = (long)n2 + (long)(slot.row * 18);
            if (!(d >= (double)l) || !(d < (double)(l + 18L)) || !(d2 >= (double)l2) || !(d2 < (double)(l2 + 18L))) continue;
            return slot;
        }
        return null;
    }

    public static List<Link> links(List<Cell> list) {
        if (!SwordCavityRules.usable(list)) {
            return List.of();
        }
        EnumSet<FoodWeaponBuildRules.Material> enumSet = EnumSet.noneOf(FoodWeaponBuildRules.Material.class);
        for (Cell cell : list) {
            if (!cell.installed()) continue;
            enumSet.add(cell.material());
        }
        ArrayList arrayList = new ArrayList();
        if (enumSet.contains((Object)FoodWeaponBuildRules.Material.BOSS_FROST) && enumSet.contains((Object)FoodWeaponBuildRules.Material.DIAMOND)) {
            arrayList.add(Link.FROST_EDGE);
        }
        if (enumSet.contains((Object)FoodWeaponBuildRules.Material.BOSS_CAPACITOR) && SwordCavityRules.hasAny(enumSet, FoodWeaponBuildRules.Material.COPPER, FoodWeaponBuildRules.Material.REDSTONE)) {
            arrayList.add(Link.COPPER_CIRCUIT);
        }
        if (enumSet.contains((Object)FoodWeaponBuildRules.Material.BOSS_BUTCHER) && SwordCavityRules.hasAny(enumSet, FoodWeaponBuildRules.Material.BLACK_STEEL, FoodWeaponBuildRules.Material.WITHERITE)) {
            arrayList.add(Link.BUTCHER_EDGE);
        }
        if (enumSet.contains((Object)FoodWeaponBuildRules.Material.BOSS_TEA) && SwordCavityRules.hasAny(enumSet, FoodWeaponBuildRules.Material.WOOD, FoodWeaponBuildRules.Material.LEATHER)) {
            arrayList.add(Link.TEA_WEAVE);
        }
        if (enumSet.contains((Object)FoodWeaponBuildRules.Material.BOSS_WINE) && SwordCavityRules.hasAny(enumSet, FoodWeaponBuildRules.Material.BOSS_APPLE, FoodWeaponBuildRules.Material.BOSS_CHEESE)) {
            arrayList.add(Link.WINE_FEAST);
        }
        if (enumSet.contains((Object)FoodWeaponBuildRules.Material.BOSS_SPORE) && SwordCavityRules.hasAny(enumSet, FoodWeaponBuildRules.Material.WOOD, FoodWeaponBuildRules.Material.BONE)) {
            arrayList.add(Link.SPORE_ROOT);
        }
        if (enumSet.contains((Object)FoodWeaponBuildRules.Material.BOSS_SHEPHERD) && SwordCavityRules.hasAny(enumSet, FoodWeaponBuildRules.Material.BONE, FoodWeaponBuildRules.Material.BOSS_CHEESE)) {
            arrayList.add(Link.SHEPHERD_PACT);
        }
        if (enumSet.contains((Object)FoodWeaponBuildRules.Material.BOSS_TIDE) && SwordCavityRules.hasAny(enumSet, FoodWeaponBuildRules.Material.QUARTZ, FoodWeaponBuildRules.Material.ENDERITE)) {
            arrayList.add(Link.TIDAL_CHANNEL);
        }
        if (SwordCavityRules.hasAny(enumSet, FoodWeaponBuildRules.Material.IGNITIUM, FoodWeaponBuildRules.Material.BLAZE) && SwordCavityRules.hasAny(enumSet, FoodWeaponBuildRules.Material.OBSIDIAN, FoodWeaponBuildRules.Material.NETHERITE)) {
            arrayList.add(Link.EMBER_FORGE);
        }
        if (SwordCavityRules.hasAny(enumSet, FoodWeaponBuildRules.Material.MITHRIL, FoodWeaponBuildRules.Material.AMETHYST) && SwordCavityRules.hasAny(enumSet, FoodWeaponBuildRules.Material.LAPIS, FoodWeaponBuildRules.Material.ENDERITE)) {
            arrayList.add(Link.ARCANE_LATTICE);
        }
        if (enumSet.contains((Object)FoodWeaponBuildRules.Material.ANCIENT_METAL) && SwordCavityRules.hasAny(enumSet, FoodWeaponBuildRules.Material.IRON, FoodWeaponBuildRules.Material.BLACK_STEEL)) {
            arrayList.add(Link.ANCIENT_ALLOY);
        }
        if (enumSet.contains((Object)FoodWeaponBuildRules.Material.GOLD) && SwordCavityRules.hasAny(enumSet, FoodWeaponBuildRules.Material.EMERALD, FoodWeaponBuildRules.Material.DIAMOND)) {
            arrayList.add(Link.GEM_CROWN);
        }
        return List.copyOf(arrayList);
    }

    private static boolean hasAny(EnumSet<FoodWeaponBuildRules.Material> enumSet, FoodWeaponBuildRules.Material material, FoodWeaponBuildRules.Material material2) {
        return enumSet.contains((Object)material) || enumSet.contains((Object)material2);
    }

    public static FoodWeaponBuildRules.Material installed(List<Cell> list, Slot slot) {
        if (list == null || list.size() != 27 || slot == null) {
            return FoodWeaponBuildRules.Material.ORIGINAL;
        }
        Cell cell = list.get(slot.ordinal());
        return cell != null && cell.installed() ? cell.material() : FoodWeaponBuildRules.Material.ORIGINAL;
    }

    public static FoodWeaponBuildRules.Build combine(FoodWeaponBuildRules.Build build, List<Cell> list, boolean bl) {
        return SwordCavityRules.applyMaterials(build, list, bl, false);
    }

    public static FoodWeaponBuildRules.Build gridBuild(List<Cell> list, boolean bl) {
        return SwordCavityRules.applyMaterials(new FoodWeaponBuildRules.Build(1.0, 1.0, 1.0, 1.0, 0.0), list, bl, true);
    }

    private static double bounded(double d, double d2, double d3, double d4) {
        return Double.isFinite(d) ? Math.max(d2, Math.min(d3, d)) : d4;
    }

    public static Cell nativeCell(Slot slot, FoodWeaponBuildRules.Material material) {
        return new Cell(State.EMPTY, FoodWeaponBuildRules.Material.ORIGINAL);
    }

    public static boolean usable(List<Cell> list) {
        if (list == null || list.size() != 27) {
            return false;
        }
        for (Slot slot : Slot.values()) {
            Cell cell = list.get(slot.ordinal());
            if (cell != null && cell.state() != null && cell.state() != State.INVALID && cell.material() != null && (cell.state() != State.INSTALLED || SwordCavityRules.accepts(slot, cell.material()))) continue;
            return false;
        }
        return true;
    }

    public static Slot lossSlot(List<Cell> list) {
        Cell cell;
        int n;
        if (!SwordCavityRules.usable(list)) {
            return null;
        }
        for (n = 26; n >= 4; --n) {
            cell = list.get(n);
            if (cell == null || !cell.installed()) continue;
            return Slot.byIndex(n);
        }
        for (n = 3; n >= 0; --n) {
            cell = list.get(n);
            if (cell == null || !cell.installed()) continue;
            return Slot.byIndex(n);
        }
        for (n = 26; n >= 0; --n) {
            cell = list.get(n);
            if (cell == null || !cell.present()) continue;
            return Slot.byIndex(n);
        }
        return null;
    }

    private static FoodWeaponBuildRules.Build applyMaterials(FoodWeaponBuildRules.Build build, List<Cell> list, boolean bl, boolean bl2) {
        FoodWeaponBuildRules.Build build2 = build == null ? new FoodWeaponBuildRules.Build(1.0, 1.0, 1.0, 1.0, 0.0) : build;
        double d = SwordCavityRules.bounded(build2.durability(), 0.5, 3.0, 1.0);
        int n = 0;
        if (list != null && list.size() == Slot.values().length) {
            for (Slot slot : Slot.values()) {
                Cell cell = list.get(slot.ordinal());
                if (cell == null || !bl2 && slot.legacyPart != null || !cell.installed() || cell.material() != FoodWeaponBuildRules.Material.DIAMOND_ALLOY) continue;
                ++n;
            }
        }
        d = Math.min(3.0, Math.max(1.0, d) + (WeaponDurabilityRules.multiplier(n) - 1.0));
        double d2 = SwordCavityRules.bounded(build2.wear(), 0.35, 2.0, 1.0);
        double d3 = SwordCavityRules.bounded(build2.damage(), 0.5, 2.0, 1.0);
        double d4 = SwordCavityRules.bounded(build2.speed(), 0.5, 1.5, 1.0);
        double d5 = SwordCavityRules.bounded(build2.reach(), 0.0, 0.75, 0.0);
        if (!SwordCavityRules.usable(list)) {
            return new FoodWeaponBuildRules.Build(d, d2, 0.0, 0.0, 0.0);
        }
        int[] nArray = new int[FoodWeaponBuildRules.Material.values().length];
        for (Slot enum_ : Slot.values()) {
            Cell cell = list.get(enum_.ordinal());
            if (!bl2 && enum_.legacyPart != null || !cell.installed()) continue;
            int n2 = cell.material().ordinal();
            nArray[n2] = nArray[n2] + 1;
        }
        for (Enum enum_ : FoodWeaponBuildRules.Material.values()) {
            int n3 = Math.min(3, nArray[enum_.ordinal()]);
            double d6 = n3 == 0 ? 0.0 : (n3 == 1 ? 1.0 : (n3 == 2 ? 1.5 : 1.75));
            d2 += (((FoodWeaponBuildRules.Material)enum_).wear - 1.0) * 0.12 * d6;
            d3 += (((FoodWeaponBuildRules.Material)enum_).damage - 1.0) * 0.08 * d6;
            d4 += (((FoodWeaponBuildRules.Material)enum_).speed - 1.0) * 0.08 * d6;
            d5 += ((FoodWeaponBuildRules.Material)enum_).reach * 0.15 * d6;
        }
        for (Link link : bl ? SwordCavityRules.links(list) : List.of()) {
            d2 += link.wearBonus;
            d3 += link.damageBonus;
            d4 += link.speedBonus;
            d5 += link.reachBonus;
        }
        return new FoodWeaponBuildRules.Build(SwordCavityRules.bounded(d, 0.5, 3.0, 1.0), SwordCavityRules.bounded(d2, 0.35, 2.0, 1.0), SwordCavityRules.bounded(d3, 0.5, 2.0, 1.0), SwordCavityRules.bounded(d4, 0.5, 1.5, 1.0), SwordCavityRules.bounded(d5, 0.0, 0.75, 0.0));
    }

    public static List<List<FoodWeaponBuildRules.Material>> linkRequirements(Link link) {
        return switch (link.name()) {
            case "FROST_EDGE" -> List.of(List.of(FoodWeaponBuildRules.Material.BOSS_FROST), List.of(FoodWeaponBuildRules.Material.DIAMOND));
            case "COPPER_CIRCUIT" -> List.of(List.of(FoodWeaponBuildRules.Material.BOSS_CAPACITOR), List.of(FoodWeaponBuildRules.Material.COPPER, FoodWeaponBuildRules.Material.REDSTONE));
            case "BUTCHER_EDGE" -> List.of(List.of(FoodWeaponBuildRules.Material.BOSS_BUTCHER), List.of(FoodWeaponBuildRules.Material.BLACK_STEEL, FoodWeaponBuildRules.Material.WITHERITE));
            case "TEA_WEAVE" -> List.of(List.of(FoodWeaponBuildRules.Material.BOSS_TEA), List.of(FoodWeaponBuildRules.Material.WOOD, FoodWeaponBuildRules.Material.LEATHER));
            case "WINE_FEAST" -> List.of(List.of(FoodWeaponBuildRules.Material.BOSS_WINE), List.of(FoodWeaponBuildRules.Material.BOSS_APPLE, FoodWeaponBuildRules.Material.BOSS_CHEESE));
            case "SPORE_ROOT" -> List.of(List.of(FoodWeaponBuildRules.Material.BOSS_SPORE), List.of(FoodWeaponBuildRules.Material.WOOD, FoodWeaponBuildRules.Material.BONE));
            case "SHEPHERD_PACT" -> List.of(List.of(FoodWeaponBuildRules.Material.BOSS_SHEPHERD), List.of(FoodWeaponBuildRules.Material.BONE, FoodWeaponBuildRules.Material.BOSS_CHEESE));
            case "TIDAL_CHANNEL" -> List.of(List.of(FoodWeaponBuildRules.Material.BOSS_TIDE), List.of(FoodWeaponBuildRules.Material.QUARTZ, FoodWeaponBuildRules.Material.ENDERITE));
            case "EMBER_FORGE" -> List.of(List.of(FoodWeaponBuildRules.Material.IGNITIUM, FoodWeaponBuildRules.Material.BLAZE), List.of(FoodWeaponBuildRules.Material.OBSIDIAN, FoodWeaponBuildRules.Material.NETHERITE));
            case "ARCANE_LATTICE" -> List.of(List.of(FoodWeaponBuildRules.Material.MITHRIL, FoodWeaponBuildRules.Material.AMETHYST), List.of(FoodWeaponBuildRules.Material.LAPIS, FoodWeaponBuildRules.Material.ENDERITE));
            case "ANCIENT_ALLOY" -> List.of(List.of(FoodWeaponBuildRules.Material.ANCIENT_METAL), List.of(FoodWeaponBuildRules.Material.IRON, FoodWeaponBuildRules.Material.BLACK_STEEL));
            case "GEM_CROWN" -> List.of(List.of(FoodWeaponBuildRules.Material.GOLD), List.of(FoodWeaponBuildRules.Material.EMERALD, FoodWeaponBuildRules.Material.DIAMOND));
            default -> throw new IllegalArgumentException("Unknown material link: " + link);
        };
    }

    public static List<Link> participatingLinks(FoodWeaponBuildRules.Material material) {
        if (material == null || material == FoodWeaponBuildRules.Material.ORIGINAL) {
            return List.of();
        }
        ArrayList<Link> arrayList = new ArrayList<Link>();
        for (Link link : Link.values()) {
            if (!SwordCavityRules.linkRequirements(link).stream().anyMatch(list -> list.contains((Object)material))) continue;
            arrayList.add(link);
        }
        return List.copyOf(arrayList);
    }

    public static enum Slot {
        BLADE(false, FoodWeaponBuildRules.Part.EDGE),
        FRAME(false, null),
        GRIP(false, FoodWeaponBuildRules.Part.GRIP),
        BINDING(false, null),
        CORE(false, FoodWeaponBuildRules.Part.CORE),
        GUARD(false, null),
        POMMEL(false, null),
        INLAY(false, null),
        FREE_01(false, null),
        FREE_02(false, null),
        FREE_03(false, null),
        FREE_04(false, null),
        FREE_05(false, null),
        FREE_06(false, null),
        FREE_07(false, null),
        FREE_08(false, null),
        FREE_09(false, null),
        FREE_10(false, null),
        FREE_11(false, null),
        FREE_12(false, null),
        FREE_13(false, null),
        FREE_14(false, null),
        FREE_15(false, null),
        FREE_16(false, null),
        FREE_17(false, null),
        FREE_18(false, null),
        FREE_19(false, null);

        public final boolean required;
        public final FoodWeaponBuildRules.Part legacyPart;
        public final int column;
        public final int row;
        public final double durabilityWeight;
        public final double damageWeight;
        public final double speedWeight;
        public final double reachWeight;

        public String id() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        private Slot(boolean bl, FoodWeaponBuildRules.Part part) {
            this.required = bl;
            this.legacyPart = part;
            this.column = this.ordinal() % 9;
            this.row = this.ordinal() / 9;
            this.durabilityWeight = part == null ? 0.12 : 0.0;
            this.damageWeight = part == null ? 0.08 : 0.0;
            this.speedWeight = part == null ? 0.08 : 0.0;
            this.reachWeight = part == null ? 0.15 : 0.0;
        }

        public static Slot byIndex(int n) {
            return n >= 0 && n < 27 ? Slot.values()[n] : null;
        }
    }

    public record Cell(State state, FoodWeaponBuildRules.Material material) {
        public boolean present() {
            return this.state == State.NATIVE || this.state == State.INSTALLED;
        }

        public boolean installed() {
            return this.state == State.INSTALLED && this.material != null && this.material != FoodWeaponBuildRules.Material.ORIGINAL;
        }
    }

    public static enum State {
        NATIVE,
        INSTALLED,
        MISSING,
        EMPTY,
        INVALID;

    }

    public static enum Link {
        FROST_EDGE(0.08, -0.06, 0.0, 0.0, 0.0),
        COPPER_CIRCUIT(0.0, 0.0, 0.0, 0.04, 0.1),
        BUTCHER_EDGE(0.0, -0.03, 0.08, 0.0, 0.0),
        TEA_WEAVE(0.0, -0.05, 0.0, 0.06, 0.0),
        WINE_FEAST(0.0, 0.0, 0.05, 0.04, 0.0),
        SPORE_ROOT(0.1, -0.04, 0.0, 0.0, 0.0),
        SHEPHERD_PACT(0.06, 0.0, 0.04, 0.0, 0.0),
        TIDAL_CHANNEL(0.0, -0.06, 0.0, 0.0, 0.08),
        EMBER_FORGE(0.06, 0.0, 0.08, 0.0, 0.0),
        ARCANE_LATTICE(0.0, -0.04, 0.0, 0.05, 0.05),
        ANCIENT_ALLOY(0.08, -0.04, 0.0, 0.0, 0.0),
        GEM_CROWN(0.0, 0.0, 0.04, 0.03, 0.04);

        public final double damageBonus;
        public final double wearBonus;
        public final double reachBonus;
        public final double durabilityBonus;
        public final double speedBonus;

        private Link(double d, double d2, double d3, double d4, double d5) {
            this.durabilityBonus = d;
            this.wearBonus = d2;
            this.damageBonus = d3;
            this.speedBonus = d4;
            this.reachBonus = d5;
        }
    }
}

