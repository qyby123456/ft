/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;

public final class SwordFunctionRules {
    private static final double EPSILON = 1.0E-9;
    private static final double MAX_INPUT = 64.0;
    private static final double MAX_VALUE = 1000000.0;
    private static final double MAX_MINIMUM = 1024.0;
    public static final int MAX_EVOLUTION = 4;
    public static final Values ZERO = new Values(0.0, 0.0, 0.0, 0.0, 0.0);
    private static final Profile IRON = SwordFunctionRules.ironProfile();

    private SwordFunctionRules() {
    }

    private static void validateInput(Values values) {
        for (Function function : Function.values()) {
            if (!(values.value(function) > 64.0)) continue;
            throw new IllegalArgumentException("Function input exceeds limit");
        }
    }

    private static boolean validItemId(String string) {
        return string != null && !string.equals("minecraft:air") && string.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }

    public static Profile iron() {
        return IRON;
    }

    public static boolean unlocked(Profile profile, int n, int n2) {
        return n >= 0 && n < 27 && (profile == null || profile.unlockLevels().get(n) >= 0 && profile.unlockLevels().get(n) <= Math.max(0, Math.min(4, n2)));
    }

    public static int capacity(Profile profile) {
        return profile == null ? 27 : profile.capacity();
    }

    public static int maximumCells(Profile profile) {
        return SwordFunctionRules.capacity(profile);
    }

    public static int unlockedCells(Profile profile, int n) {
        int n2 = 0;
        for (int i = 0; i < 27; ++i) {
            if (!SwordFunctionRules.unlocked(profile, i, n)) continue;
            ++n2;
        }
        return n2;
    }

    public static Status evaluate(Profile profile, List<SwordCavityRules.Cell> list, int n) {
        boolean bl = SwordCavityRules.usable(list);
        double[] dArray = new double[Function.values().length];
        for (int i = 0; i < 27; ++i) {
            if (!SwordFunctionRules.unlocked(profile, i, n) || !bl || profile == null || !list.get(i).present()) continue;
            Values values = profile.materials().getOrDefault((Object)list.get(i).material(), ZERO);
            SwordFunctionRules.addContribution(profile, i, values, dArray);
        }
        return SwordFunctionRules.status(profile, bl, dArray, n);
    }

    public static Status evaluateItems(Profile profile, List<Input> list, int n) {
        boolean bl;
        boolean bl2 = bl = list != null && list.size() == 27;
        if (bl) {
            for (Input input : list) {
                if (input != null && (!input.present() || input.material() != null && SwordFunctionRules.validItemId(input.itemId()))) continue;
                bl = false;
                break;
            }
        }
        Object object = new double[Function.values().length];
        if (bl && profile != null) {
            for (int i = 0; i < 27; ++i) {
                Input input = list.get(i);
                if (!input.present() || !SwordFunctionRules.unlocked(profile, i, n)) continue;
                Values values = profile.itemValues().get(input.itemId());
                if (values == null) {
                    values = profile.materials().getOrDefault((Object)input.material(), ZERO);
                }
                SwordFunctionRules.addContribution(profile, i, values, (double[])object);
            }
        }
        return SwordFunctionRules.status(profile, bl, (double[])object, n);
    }

    private static void addContribution(Profile profile, int n, Values values, double[] dArray) {
        Values values2 = profile.cellWeights().get(n);
        Values values3 = profile.cellBaseValues().get(n);
        for (Function function : Function.values()) {
            int n2 = function.ordinal();
            dArray[n2] = dArray[n2] + (values3.value(function) + values.value(function) * values2.value(function));
        }
    }

    private static Status status(Profile profile, boolean bl, double[] dArray, int n) {
        Values values = new Values(dArray[0], dArray[1], dArray[2], dArray[3], dArray[4]);
        Values values2 = profile == null ? ZERO : profile.minimum();
        ArrayList<Function> arrayList = new ArrayList<Function>();
        for (Function function : Function.values()) {
            if (bl && !(values.value(function) + 1.0E-9 < values2.value(function))) continue;
            arrayList.add(function);
        }
        return new Status(bl, values, values2, arrayList, SwordFunctionRules.unlockedCells(profile, n));
    }

    private static Profile ironProfile() {
        ArrayList<Values> arrayList = new ArrayList<Values>();
        ArrayList<Values> arrayList2 = new ArrayList<Values>();
        ArrayList<Integer> arrayList3 = new ArrayList<Integer>();
        for (int i = 0; i < 27; ++i) {
            int n = SwordNativeMaterialRules.visualColumn(i);
            int n2 = SwordNativeMaterialRules.visualRow(i);
            Values values = n == 0 ? new Values(1.0, 0.0, 0.0, 0.35, 0.0) : (n == 1 ? new Values(0.25, 0.25, 0.0, 1.0, 0.0) : (n == 2 ? new Values(0.0, 1.0, 0.0, 0.4, 0.0) : (n2 == 1 && n == 3 ? new Values(0.0, 0.15, 1.0, 0.3, 0.15) : (n2 == 1 && n == 4 ? new Values(0.0, 0.1, 0.25, 1.0, 0.55) : (n2 == 1 && n == 5 ? new Values(0.0, 0.05, 0.1, 0.25, 1.0) : new Values(0.0, 0.05, 0.0, 0.2, 1.0))))));
            arrayList.add(values);
            arrayList2.add(n == 0 ? new Values(0.08, 0.0, 0.0, 0.03, 0.0) : (n == 1 ? new Values(0.02, 0.02, 0.0, 0.08, 0.0) : (n == 2 ? new Values(0.0, 0.08, 0.0, 0.03, 0.0) : (n2 == 1 && n == 3 ? new Values(0.0, 0.01, 0.08, 0.03, 0.02) : (n2 == 1 && n == 4 ? new Values(0.0, 0.01, 0.02, 0.08, 0.04) : (n2 == 1 && n == 5 ? new Values(0.0, 0.01, 0.01, 0.02, 0.08) : new Values(0.0, 0.01, 0.0, 0.02, 0.06)))))));
            arrayList3.add(switch (i) {
                case 2, 3, 6, 13, 14, 15, 16, 17, 20 -> 0;
                case 7, 21 -> 1;
                case 8, 22 -> 2;
                case 9, 23 -> 3;
                case 10, 24 -> 4;
                default -> -1;
            });
        }
        EnumMap<FoodWeaponBuildRules.Material, Values> enumMap = new EnumMap<FoodWeaponBuildRules.Material, Values>(FoodWeaponBuildRules.Material.class);
        for (FoodWeaponBuildRules.Material material : FoodWeaponBuildRules.Material.values()) {
            if (material == FoodWeaponBuildRules.Material.ORIGINAL) continue;
            enumMap.put(material, new Values(0.25, material.durability / 1.35, material.damage / 1.15, material.durability / 1.35, material.damage / 1.15));
        }
        enumMap.put(FoodWeaponBuildRules.Material.WOOD, new Values(1.0, 0.25, 0.15, 0.35, 0.15));
        enumMap.put(FoodWeaponBuildRules.Material.IRON, new Values(0.25, 1.0, 1.0, 1.0, 1.0));
        enumMap.put(FoodWeaponBuildRules.Material.STONE, new Values(0.15, 0.85, 0.55, 1.1, 0.75));
        enumMap.put(FoodWeaponBuildRules.Material.BONE, new Values(1.0, 0.7, 0.4, 0.6, 0.55));
        enumMap.put(FoodWeaponBuildRules.Material.LEATHER, new Values(0.95, 0.5, 0.2, 0.7, 0.05));
        enumMap.put(FoodWeaponBuildRules.Material.COPPER, new Values(0.4, 0.7, 1.1, 0.8, 0.65));
        enumMap.put(FoodWeaponBuildRules.Material.GOLD, new Values(0.3, 0.35, 0.9, 0.4, 0.55));
        enumMap.put(FoodWeaponBuildRules.Material.COAL, new Values(0.05, 0.15, 0.9, 0.25, 0.1));
        enumMap.put(FoodWeaponBuildRules.Material.LAPIS, new Values(0.1, 0.2, 1.4, 0.5, 0.3));
        enumMap.put(FoodWeaponBuildRules.Material.REDSTONE, new Values(0.1, 0.1, 1.6, 0.25, 0.1));
        enumMap.put(FoodWeaponBuildRules.Material.QUARTZ, new Values(0.1, 0.6, 1.0, 0.7, 1.1));
        enumMap.put(FoodWeaponBuildRules.Material.AMETHYST, new Values(0.1, 0.55, 1.3, 0.7, 0.95));
        Map<String, Values> map = Map.of("minecraft:stick", (Values)enumMap.get((Object)FoodWeaponBuildRules.Material.WOOD), "minecraft:string", new Values(0.65, 0.35, 0.15, 0.85, 0.05), "minecraft:cobblestone", (Values)enumMap.get((Object)FoodWeaponBuildRules.Material.STONE), "minecraft:iron_ingot", (Values)enumMap.get((Object)FoodWeaponBuildRules.Material.IRON), "minecraft:flint", new Values(0.1, 0.45, 0.25, 0.55, 1.3));
        return new Profile(new Values(1.25, 1.33, 1.38, 3.1, 2.13), arrayList, arrayList3, enumMap, arrayList2, map);
    }

    public static enum Function {
        HANDLE,
        GUARD,
        CORE,
        STABILITY,
        BLADE;

    }

    public record Values(double handle, double guard, double core, double stability, double blade) {
        public Values {
            for (double d6 : new double[]{d, d2, d3, d4, d5}) {
                if (Double.isFinite(d6) && !(d6 < 0.0) && !(d6 > 1000000.0)) continue;
                throw new IllegalArgumentException("Invalid function value");
            }
        }

        public double value(Function function) {
            return switch (Objects.requireNonNull(function)) {
                default -> throw new IncompatibleClassChangeError();
                case Function.HANDLE -> this.handle;
                case Function.GUARD -> this.guard;
                case Function.CORE -> this.core;
                case Function.STABILITY -> this.stability;
                case Function.BLADE -> this.blade;
            };
        }
    }

    public record Profile(Values minimum, List<Values> cellWeights, List<Integer> unlockLevels, Map<FoodWeaponBuildRules.Material, Values> materials, List<Values> cellBaseValues, Map<String, Values> itemValues) {
        public Profile {
            Objects.requireNonNull(values);
            list = List.copyOf(list);
            list2 = List.copyOf(list2);
            map = Map.copyOf(map);
            list3 = List.copyOf(list3);
            map2 = Map.copyOf(map2);
            if (list.size() != 27 || list2.size() != 27 || list3.size() != 27 || map.isEmpty() && map2.isEmpty() || map.containsKey((Object)FoodWeaponBuildRules.Material.ORIGINAL)) {
                throw new IllegalArgumentException("Invalid function profile");
            }
            for (Function function : Function.values()) {
                if (!(values.value(function) > 1024.0)) continue;
                throw new IllegalArgumentException("Minimum exceeds limit");
            }
            for (Values values2 : list) {
                SwordFunctionRules.validateInput(values2);
            }
            for (Values values3 : list3) {
                SwordFunctionRules.validateInput(values3);
            }
            for (Values values4 : map.values()) {
                SwordFunctionRules.validateInput(values4);
            }
            for (Map.Entry entry : map2.entrySet()) {
                if (!SwordFunctionRules.validItemId((String)entry.getKey())) {
                    throw new IllegalArgumentException("Invalid item identifier");
                }
                SwordFunctionRules.validateInput((Values)entry.getValue());
            }
            Iterator<Integer> iterator = list2.iterator();
            while (iterator.hasNext()) {
                int n = (Integer)iterator.next();
                if (n >= -1 && n <= 4) continue;
                throw new IllegalArgumentException("Unlock level must be -1..4");
            }
        }

        public Profile(Values values, List<Values> list, List<Integer> list2, Map<FoodWeaponBuildRules.Material, Values> map) {
            this(values, list, list2, map, Collections.nCopies(27, ZERO), Map.of());
        }

        public int capacity() {
            return (int)this.unlockLevels.stream().filter(n -> n >= 0).count();
        }

        public Profile withItemValues(Map<String, Values> map) {
            HashMap<String, Values> hashMap = new HashMap<String, Values>(this.itemValues);
            hashMap.putAll(Objects.requireNonNull(map));
            return new Profile(this.minimum, this.cellWeights, this.unlockLevels, this.materials, this.cellBaseValues, hashMap);
        }
    }

    public record Status(boolean valid, Values total, Values minimum, List<Function> missing, int unlockedCells) {
        public Status {
            list = List.copyOf(list);
        }

        public boolean ready() {
            return this.valid && this.missing.isEmpty();
        }

        public boolean sufficient(Function function) {
            return this.valid && this.total.value(function) + 1.0E-9 >= this.minimum.value(function);
        }
    }

    public record Input(String itemId, FoodWeaponBuildRules.Material material, boolean present) {
    }
}

