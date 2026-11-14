/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util.foundations;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;

public final class FourWeaponFoundationRules {
    public static final int CELLS = 27;
    public static final int MAX_LEVEL = 4;
    public static final String REFERENCE = "native-reference";
    public static final String PLAYER = "player-paid";
    public static final Values ZERO = new Values(0.0, 0.0, 0.0, 0.0, 0.0);
    private static final double EPSILON = 1.0E-9;

    private FourWeaponFoundationRules() {
    }

    private static boolean validId(String string) {
        return string != null && !"minecraft:air".equals(string) && string.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }

    private static void boundedInput(Values values) {
        for (int i = 0; i < 5; ++i) {
            if (!(values.at(i) > 64.0)) continue;
            throw new IllegalArgumentException("Input exceeds 64");
        }
    }

    private static void validateBudget(Map<String, Integer> map) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (FourWeaponFoundationRules.validId(entry.getKey()) && entry.getValue() > 0 && entry.getValue() <= 27) continue;
            throw new IllegalArgumentException("Invalid budget");
        }
    }

    public static void checkLevel(int n) {
        if (n < 0 || n > 4) {
            throw new IllegalArgumentException("Regular foundation level outside 0..4");
        }
    }

    public static boolean unlocked(Profile profile, int n, int n2) {
        FourWeaponFoundationRules.checkLevel(n2);
        return n >= 0 && n < 27 && profile.unlocks().get(n) >= 0 && profile.unlocks().get(n) <= n2;
    }

    public static int capacity(Profile profile, int n) {
        FourWeaponFoundationRules.checkLevel(n);
        int n2 = 0;
        for (int i = 0; i < 27; ++i) {
            if (!FourWeaponFoundationRules.unlocked(profile, i, n)) continue;
            ++n2;
        }
        return n2;
    }

    public static Status evaluate(Profile profile, Map<Integer, Cell> map, int n) {
        return FourWeaponFoundationRules.evaluate(profile, map, n, Map.of());
    }

    public static Status evaluate(Profile profile, Map<Integer, Cell> map, int n, Map<Integer, Values> map2) {
        Object object;
        Object object2;
        Map.Entry<Integer, Values> entry2;
        Objects.requireNonNull(map2);
        for (Map.Entry<Integer, Values> entry2 : map2.entrySet()) {
            object2 = map.get(entry2.getKey());
            if (object2 == null || !((Cell)object2).refundable() || entry2.getValue() == null) {
                throw new IllegalArgumentException("Invalid paid values");
            }
            FourWeaponFoundationRules.boundedInput((Values)entry2.getValue());
        }
        FourWeaponFoundationRules.checkLevel(n);
        FourWeaponFoundationRules.validateCells(map);
        Object object3 = ZERO;
        entry2 = ZERO;
        object2 = new ArrayList();
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        for (int i = 0; i < 27; ++i) {
            object = map.get(i);
            if (object == null) continue;
            if (!FourWeaponFoundationRules.unlocked(profile, i, n)) {
                arrayList.add(i);
                continue;
            }
            Values values = profile.items().getOrDefault(((Cell)object).itemId(), map2.getOrDefault(i, profile.materials().get((Object)((Cell)object).material())));
            if (values == null) {
                object2.add(i);
                continue;
            }
            object3 = ((Values)object3).plus(profile.bases().get(i));
            entry2 = ((Values)((Object)entry2)).plus(values.times(profile.weights().get(i)));
        }
        Values values = ((Values)object3).plus((Values)((Object)entry2));
        object = new ArrayList();
        for (int i = 0; i < 5; ++i) {
            if (!(values.at(i) + 1.0E-9 < profile.minimum().at(i))) continue;
            object.add(profile.axes().get(i));
        }
        return new Status(values, (Values)object3, (Values)((Object)entry2), (List<String>)object, (List<Integer>)object2, arrayList, FourWeaponFoundationRules.capacity(profile, n));
    }

    public static void validateCells(Map<Integer, Cell> map) {
        Objects.requireNonNull(map);
        for (Map.Entry<Integer, Cell> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey() >= 0 && entry.getKey() < 27 && entry.getValue() != null) continue;
            throw new IllegalArgumentException("Invalid cells; preserve original payload");
        }
    }

    public static FoodWeaponBuildRules.Build combat(Profile profile, Map<Integer, Cell> map, int n) {
        FourWeaponFoundationRules.validateCells(map);
        FourWeaponFoundationRules.checkLevel(n);
        EnumMap<FoodWeaponBuildRules.Material, Integer> enumMap = new EnumMap<FoodWeaponBuildRules.Material, Integer>(FoodWeaponBuildRules.Material.class);
        for (Map.Entry<Integer, Cell> entry : map.entrySet()) {
            Cell cell = entry.getValue();
            if (!FourWeaponFoundationRules.unlocked(profile, entry.getKey(), n) || !cell.refundable() || cell.material() == FoodWeaponBuildRules.Material.ORIGINAL || !profile.materials().containsKey((Object)cell.material())) continue;
            enumMap.merge(cell.material(), 1, Integer::sum);
        }
        double d = 1.0;
        double d2 = 1.0;
        double d3 = 1.0;
        double d4 = 1.0;
        double d5 = 0.0;
        for (Map.Entry entry : enumMap.entrySet()) {
            FoodWeaponBuildRules.Material material = (FoodWeaponBuildRules.Material)((Object)entry.getKey());
            double d6 = switch (Math.min(3, (Integer)entry.getValue())) {
                case 1 -> 1.0;
                case 2 -> 1.5;
                default -> 1.75;
            };
            d += (material.durability - 1.0) * 0.12 * d6;
            d2 += (material.wear - 1.0) * 0.12 * d6;
            d3 += (material.damage - 1.0) * 0.08 * d6;
            d4 += (material.speed - 1.0) * 0.08 * d6;
            d5 += material.reach * 0.15 * d6;
        }
        return new FoodWeaponBuildRules.Build(FourWeaponFoundationRules.clamp(d, 0.5, 3.0), FourWeaponFoundationRules.clamp(d2, 0.35, 2.0), FourWeaponFoundationRules.clamp(d3, 0.5, 2.0), FourWeaponFoundationRules.clamp(d4, 0.5, 1.5), FourWeaponFoundationRules.clamp(d5, 0.0, 0.75));
    }

    private static double clamp(double d, double d2, double d3) {
        return Math.max(d2, Math.min(d3, d));
    }

    public record Values(double first, double second, double third, double fourth, double fifth) {
        public Values {
            for (double d6 : new double[]{d, d2, d3, d4, d5}) {
                if (Double.isFinite(d6) && !(d6 < 0.0) && !(d6 > 1000000.0)) continue;
                throw new IllegalArgumentException("Invalid values");
            }
        }

        public double at(int n) {
            return switch (n) {
                case 0 -> this.first;
                case 1 -> this.second;
                case 2 -> this.third;
                case 3 -> this.fourth;
                case 4 -> this.fifth;
                default -> throw new IllegalArgumentException("Axis outside 0..4");
            };
        }

        public Values plus(Values values) {
            return new Values(this.first + values.first, this.second + values.second, this.third + values.third, this.fourth + values.fourth, this.fifth + values.fifth);
        }

        public Values times(Values values) {
            return new Values(this.first * values.first, this.second * values.second, this.third * values.third, this.fourth * values.fourth, this.fifth * values.fifth);
        }

        public Values scale(double d) {
            return this.times(new Values(d, d, d, d, d));
        }
    }

    public record Profile(String id, String family, List<String> axes, Values minimum, List<Values> weights, List<Values> bases, List<Integer> unlocks, Map<FoodWeaponBuildRules.Material, Values> materials, Map<String, Values> items, Map<Integer, Cell> nativeCells, String recipeEvidence, Map<String, Integer> recipeBudget, Map<String, Integer> environmentBudget) {
        public Profile {
            if (!FourWeaponFoundationRules.validId(string) || !List.of("bow", "crossbow", "staff", "axe", "trident").contains(string2)) {
                throw new IllegalArgumentException("Unknown foundation family");
            }
            list = List.copyOf(list);
            list2 = List.copyOf(list2);
            list3 = List.copyOf(list3);
            list4 = List.copyOf(list4);
            map = Map.copyOf(map);
            map2 = Map.copyOf(map2);
            map3 = Map.copyOf(map3);
            map4 = Map.copyOf(map4);
            map5 = Map.copyOf(map5);
            Objects.requireNonNull(values);
            Objects.requireNonNull(string3);
            if (list.size() != 5 || list.stream().distinct().count() != 5L || list.stream().anyMatch(String::isBlank) || list2.size() != 27 || list3.size() != 27 || list4.size() != 27 || map.containsKey((Object)FoodWeaponBuildRules.Material.ORIGINAL)) {
                throw new IllegalArgumentException("Invalid profile dimensions");
            }
            for (int n : list4) {
                if (n >= -1 && n <= 4) continue;
                throw new IllegalArgumentException("Invalid unlock level");
            }
            for (int i = 0; i < 5; ++i) {
                if (!(values.at(i) <= 0.0) && !(values.at(i) > 1024.0)) continue;
                throw new IllegalArgumentException("Invalid minimum");
            }
            for (Values values2 : list2) {
                FourWeaponFoundationRules.boundedInput(values2);
            }
            for (Values values3 : list3) {
                FourWeaponFoundationRules.boundedInput(values3);
            }
            for (Values values4 : map.values()) {
                FourWeaponFoundationRules.boundedInput(values4);
            }
            for (Map.Entry entry : map2.entrySet()) {
                if (!FourWeaponFoundationRules.validId((String)entry.getKey())) {
                    throw new IllegalArgumentException("Invalid item override");
                }
                FourWeaponFoundationRules.boundedInput((Values)entry.getValue());
            }
            FourWeaponFoundationRules.validateBudget(map4);
            FourWeaponFoundationRules.validateBudget(map5);
            HashMap hashMap = new HashMap();
            for (Map.Entry<Integer, Cell> entry : map3.entrySet()) {
                if (entry.getKey() < 0 || entry.getKey() >= 27 || list4.get(entry.getKey()) != 0 || !FourWeaponFoundationRules.REFERENCE.equals(entry.getValue().provenance())) {
                    throw new IllegalArgumentException("Invalid native seed");
                }
                hashMap.merge(entry.getValue().itemId(), 1, Integer::sum);
            }
            for (Map.Entry entry : hashMap.entrySet()) {
                if ((Integer)entry.getValue() <= map4.getOrDefault(entry.getKey(), 0) + map5.getOrDefault(entry.getKey(), 0)) continue;
                throw new IllegalArgumentException("Native reference exceeds finite budget");
            }
        }

        public Profile withOverrides(Values values, List<Values> list, List<Values> list2, List<Integer> list3, Map<FoodWeaponBuildRules.Material, Values> map, Map<String, Values> map2) {
            return new Profile(this.id, this.family, this.axes, values, list, list2, list3, map, map2, this.nativeCells, this.recipeEvidence, this.recipeBudget, this.environmentBudget);
        }
    }

    public record Status(Values total, Values position, Values material, List<String> missing, List<Integer> unsupported, List<Integer> sealed, int openCells) {
        public Status {
            list = List.copyOf(list);
            list2 = List.copyOf(list2);
            list3 = List.copyOf(list3);
        }

        public boolean ready() {
            return this.missing.isEmpty() && this.unsupported.isEmpty() && this.sealed.isEmpty();
        }
    }

    public record Cell(String itemId, FoodWeaponBuildRules.Material material, String provenance, String payload) {
        public Cell {
            if (!FourWeaponFoundationRules.validId(string) || material == null || string3 == null || !FourWeaponFoundationRules.REFERENCE.equals(string2) && !FourWeaponFoundationRules.PLAYER.equals(string2)) {
                throw new IllegalArgumentException("Invalid single item");
            }
        }

        public boolean refundable() {
            return FourWeaponFoundationRules.PLAYER.equals(this.provenance);
        }
    }
}

