/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.farmingtales.ftboss.util.AdditionalSwordProfiles;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.NonManorWeaponComponentRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.farmingtales.ftboss.util.WeaponEnvironmentMaterials;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules;

public final class SwordCompatibilityRules {
    private static final int[] CELL_ORDER = new int[]{2, 3, 13, 14, 15, 16, 17, 6, 20, 7, 21, 8, 22, 9, 23, 10, 24, 4, 5, 11, 12, 18, 19, 25, 26, 0, 1};
    private static final double INITIAL_MARGIN = 0.005;
    private static final SwordNativeMaterialRules.Piece EMPTY = new SwordNativeMaterialRules.Piece("minecraft:air", 0, SwordNativeMaterialRules.Origin.SHARED);
    private static final Map<FoodWeaponBuildRules.Material, SwordFunctionRules.Values> MATERIAL_VALUES = SwordCompatibilityRules.materialValues();
    private static final Map<String, SwordFunctionRules.Values> ITEM_VALUES = SwordCompatibilityRules.itemValues();
    private static final Map<String, Definition> DEFINITIONS = SwordCompatibilityRules.definitionsInternal();
    private static final Map<String, Definition> MECHANICAL = SwordCompatibilityRules.mechanicalDefinitions();
    private static final Map<String, SwordFunctionRules.Profile> PROFILES = SwordCompatibilityRules.profiles();
    private static final Map<String, SwordFunctionRules.Profile> MECHANICAL_PROFILES = SwordCompatibilityRules.mechanicalProfiles();

    private SwordCompatibilityRules() {
    }

    public static Map<String, Definition> definitions() {
        return DEFINITIONS;
    }

    public static SwordFunctionRules.Profile profile(String string, List<SwordNativeMaterialRules.Piece> list) {
        Definition definition = string == null ? null : MECHANICAL.get(string);
        return definition != null && !SwordCompatibilityRules.canonicalBudget(definition, list).isEmpty() ? MECHANICAL_PROFILES.get(string) : SwordCompatibilityRules.profile(string);
    }

    public static List<SwordNativeMaterialRules.Piece> nativeBudget(String string) {
        Definition definition = string == null ? null : DEFINITIONS.get(string);
        return definition == null ? List.of() : definition.budget();
    }

    public static String nativeRecipe(String string) {
        Definition definition = string == null ? null : DEFINITIONS.get(string);
        return definition == null ? "" : definition.recipeId();
    }

    public static SwordFunctionRules.Values materialFunctions(FoodWeaponBuildRules.Material material) {
        return material == null ? SwordFunctionRules.ZERO : MATERIAL_VALUES.getOrDefault((Object)material, SwordFunctionRules.ZERO);
    }

    public static SwordFunctionRules.Values itemFunctions(String string) {
        if (string == null) {
            return null;
        }
        SwordFunctionRules.Values values = ITEM_VALUES.get(string);
        FoodWeaponBuildRules.Material material = NonManorWeaponComponentRules.material(string);
        if (values == null && material != FoodWeaponBuildRules.Material.ORIGINAL) {
            return SwordCompatibilityRules.materialFunctions(material);
        }
        return values != null ? values : WeaponEnvironmentMaterials.functions(string);
    }

    public static FoodWeaponBuildRules.Material material(String string) {
        if (string == null) {
            return FoodWeaponBuildRules.Material.ORIGINAL;
        }
        FoodWeaponBuildRules.Material material = NonManorWeaponComponentRules.material(string);
        if (material != FoodWeaponBuildRules.Material.ORIGINAL) {
            return material;
        }
        FoodWeaponBuildRules.Material material2 = switch (string) {
            case "minecraft:string" -> FoodWeaponBuildRules.Material.LEATHER;
            case "minecraft:flint", "minecraft:blackstone", "minecraft:cobbled_deepslate", "minecraft:nether_brick", "minecraft:end_stone", "minecraft:purpur_block", "minecraft:sandstone", "minecraft:prismarine_shard" -> FoodWeaponBuildRules.Material.STONE;
            case "minecraft:netherite_scrap" -> FoodWeaponBuildRules.Material.NETHERITE;
            case "minecraft:prismarine_crystals", "minecraft:nautilus_shell", "cataclysm:crystallized_coral" -> FoodWeaponBuildRules.Material.QUARTZ;
            case "minecraft:ender_pearl", "cataclysm:void_core" -> FoodWeaponBuildRules.Material.ENDERITE;
            case "minecraft:soul_sand" -> FoodWeaponBuildRules.Material.COAL;
            case "cataclysm:koboleton_bone" -> FoodWeaponBuildRules.Material.BONE;
            case "cataclysm:lacrima" -> FoodWeaponBuildRules.Material.AMETHYST;
            case "cataclysm:essence_of_the_storm" -> FoodWeaponBuildRules.Material.BOSS_CAPACITOR;
            case "cataclysm:sandstorm_in_a_bottle" -> FoodWeaponBuildRules.Material.ANCIENT_METAL;
            case "create:iron_sheet" -> FoodWeaponBuildRules.Material.IRON;
            case "create:large_cogwheel" -> FoodWeaponBuildRules.Material.WOOD;
            default -> SwordCompatibilityRules.plank(string) ? FoodWeaponBuildRules.Material.WOOD : WeaponMaterialCatalogRules.resolve(string, List.of());
        };
        return material2 != FoodWeaponBuildRules.Material.ORIGINAL ? material2 : WeaponEnvironmentMaterials.material(string);
    }

    public static String affinity(String string) {
        if (string == null) {
            return "";
        }
        return switch (string) {
            case "minecraft:blaze_rod", "minecraft:nether_brick", "minecraft:netherite_ingot", "minecraft:netherite_scrap", "cataclysm:ignitium_ingot", "cataclysm:infernal_forge", "cataclysm:bulwark_of_the_flame" -> "nether";
            case "minecraft:prismarine_shard", "minecraft:prismarine_crystals", "minecraft:nautilus_shell", "cataclysm:crystallized_coral", "cataclysm:tidal_claws" -> "abyss";
            case "cataclysm:lacrima", "cataclysm:essence_of_the_storm", "cataclysm:astrape" -> "storm";
            case "cataclysm:cursium_ingot", "cataclysm:black_steel_ingot", "minecraft:soul_sand" -> "curse";
            case "cataclysm:ancient_metal_ingot", "cataclysm:koboleton_bone", "cataclysm:sandstorm_in_a_bottle", "minecraft:sandstone" -> "ancient";
            case "cataclysm:void_core", "minecraft:ender_pearl", "minecraft:end_stone", "minecraft:purpur_block" -> "void";
            default -> "";
        };
    }

    public static List<SwordNativeMaterialRules.Piece> expandIngredient(String string) {
        if (!SwordCompatibilityRules.validId(string)) {
            return List.of();
        }
        if ("minecraft:netherite_sword".equals(string)) {
            return SwordCompatibilityRules.recipe("minecraft:diamond", 2, "minecraft:stick", 1, "minecraft:netherite_ingot", 1);
        }
        if ("minecraft:netherite_axe".equals(string)) {
            return SwordCompatibilityRules.recipe("minecraft:diamond", 3, "minecraft:stick", 2, "minecraft:netherite_ingot", 1);
        }
        if (string.equals("minecraft:iron_block")) {
            return List.of(new SwordNativeMaterialRules.Piece("minecraft:iron_ingot", 9, SwordNativeMaterialRules.Origin.RECIPE));
        }
        Definition definition = DEFINITIONS.get(string);
        if (definition == null || !definition.crafted()) {
            return List.of(new SwordNativeMaterialRules.Piece(string, 1, SwordNativeMaterialRules.Origin.RECIPE));
        }
        return definition.budget().stream().filter(piece -> piece.origin() == SwordNativeMaterialRules.Origin.RECIPE).toList();
    }

    public static List<SwordNativeMaterialRules.Piece> canonicalBudget(String string, List<SwordNativeMaterialRules.Piece> list) {
        Definition definition = string == null ? null : DEFINITIONS.get(string);
        List<SwordNativeMaterialRules.Piece> list2 = SwordCompatibilityRules.canonicalBudget(definition, list);
        if (!list2.isEmpty()) {
            return list2;
        }
        return SwordCompatibilityRules.canonicalBudget(string == null ? null : MECHANICAL.get(string), list);
    }

    private static List<SwordNativeMaterialRules.Piece> canonicalBudget(Definition definition, List<SwordNativeMaterialRules.Piece> list) {
        Map<String, Long> map;
        Map<String, Long> map2;
        if (definition == null || list == null || list.isEmpty() || list.size() > 27) {
            return List.of();
        }
        for (SwordNativeMaterialRules.Piece object32 : list) {
            if (SwordCompatibilityRules.validPiece(object32)) continue;
            return List.of();
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (SwordNativeMaterialRules.Piece piece2 : definition.budget()) {
            if (piece2.origin() != SwordNativeMaterialRules.Origin.THEME) continue;
            linkedHashMap.merge(piece2.itemId(), piece2.count(), Integer::sum);
        }
        LinkedHashMap<String, Integer> linkedHashMap2 = new LinkedHashMap<String, Integer>();
        for (SwordNativeMaterialRules.Piece piece3 : list) {
            if (piece3.origin() != SwordNativeMaterialRules.Origin.THEME) continue;
            int n = linkedHashMap.getOrDefault(piece3.itemId(), 0);
            if (piece3.count() > n - linkedHashMap2.getOrDefault(piece3.itemId(), 0)) {
                return List.of();
            }
            linkedHashMap2.merge(piece3.itemId(), piece3.count(), Integer::sum);
        }
        try {
            map2 = SwordCompatibilityRules.recipeCost(definition.budget());
            map = SwordCompatibilityRules.recipeCost(list);
        }
        catch (ArithmeticException | IllegalArgumentException runtimeException) {
            return List.of();
        }
        if (!map2.equals(map)) {
            return List.of();
        }
        if (!definition.crafted() && list.stream().anyMatch(piece -> piece.origin() == SwordNativeMaterialRules.Origin.RECIPE)) {
            return List.of();
        }
        if (!definition.crafted() && !linkedHashMap.equals(linkedHashMap2)) {
            return List.of();
        }
        ArrayList<SwordNativeMaterialRules.Piece> arrayList = new ArrayList<SwordNativeMaterialRules.Piece>(definition.budget());
        for (String string : List.of("minecraft:oak_planks", "minecraft:cobblestone")) {
            ArrayList<String> arrayList2 = new ArrayList<String>();
            for (SwordNativeMaterialRules.Piece piece4 : list) {
                if (piece4.origin() != SwordNativeMaterialRules.Origin.RECIPE || !(string.equals("minecraft:oak_planks") ? SwordCompatibilityRules.plank(piece4.itemId()) : List.of("minecraft:cobblestone", "minecraft:blackstone", "minecraft:cobbled_deepslate").contains(piece4.itemId()))) continue;
                if (piece4.count() > arrayList.size()) {
                    return List.of();
                }
                for (int i = 0; i < piece4.count(); ++i) {
                    arrayList2.add(piece4.itemId());
                }
            }
            int n = 0;
            for (int i = 0; i < arrayList.size() && n < arrayList2.size(); ++i) {
                if (((SwordNativeMaterialRules.Piece)arrayList.get(i)).origin() != SwordNativeMaterialRules.Origin.RECIPE || !((SwordNativeMaterialRules.Piece)arrayList.get(i)).itemId().equals(string)) continue;
                arrayList.set(i, new SwordNativeMaterialRules.Piece((String)arrayList2.get(n++), 1, SwordNativeMaterialRules.Origin.RECIPE));
            }
        }
        return List.copyOf(arrayList);
    }

    public static Map<String, Long> recipeCost(List<SwordNativeMaterialRules.Piece> list) {
        TreeMap<String, Long> treeMap = new TreeMap<String, Long>();
        if (list == null) {
            return Map.of();
        }
        for (SwordNativeMaterialRules.Piece piece : list) {
            if (!SwordCompatibilityRules.validPiece(piece)) {
                throw new IllegalArgumentException("Invalid recipe piece");
            }
            if (piece.origin() != SwordNativeMaterialRules.Origin.RECIPE) continue;
            SwordCompatibilityRules.addCost(treeMap, piece.itemId(), piece.count(), 0);
        }
        return Map.copyOf(treeMap);
    }

    private static void addCost(Map<String, Long> map, String string, long l, int n) {
        if (n > 4 || l < 1L || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid recipe expansion");
        }
        if (SwordCompatibilityRules.plank(string)) {
            string = "minecraft:oak_planks";
        }
        if (string.equals("minecraft:blackstone") || string.equals("minecraft:cobbled_deepslate")) {
            string = "minecraft:cobblestone";
        }
        if (string.equals("minecraft:netherite_ingot")) {
            SwordCompatibilityRules.addCost(map, "minecraft:netherite_scrap", l * 4L, n + 1);
            SwordCompatibilityRules.addCost(map, "minecraft:gold_nugget", l * 36L, n + 1);
        } else if (string.equals("minecraft:iron_ingot") || string.equals("minecraft:gold_ingot")) {
            String string2 = string.equals("minecraft:iron_ingot") ? "minecraft:iron_nugget" : "minecraft:gold_nugget";
            map.merge(string2, l * 9L, Math::addExact);
        } else if (string.equals("minecraft:iron_block")) {
            SwordCompatibilityRules.addCost(map, "minecraft:iron_ingot", l * 9L, n + 1);
        } else {
            Definition definition = DEFINITIONS.get(string);
            if (definition != null && definition.crafted()) {
                for (SwordNativeMaterialRules.Piece piece : definition.budget()) {
                    if (piece.origin() != SwordNativeMaterialRules.Origin.RECIPE) continue;
                    SwordCompatibilityRules.addCost(map, piece.itemId(), l, n + 1);
                }
            } else {
                map.merge(string, l, Math::addExact);
            }
        }
    }

    public static List<SwordNativeMaterialRules.Piece> nativeLayout(String string, List<SwordNativeMaterialRules.Piece> list) {
        if ("minecraft:iron_sword".equals(string)) {
            return SwordNativeMaterialRules.ironPrototypeLayout(list);
        }
        List<SwordNativeMaterialRules.Piece> list2 = SwordCompatibilityRules.canonicalBudget(string, list);
        return list2.isEmpty() ? SwordNativeMaterialRules.layout(list) : SwordCompatibilityRules.layout(list2);
    }

    private static List<SwordNativeMaterialRules.Piece> layout(List<SwordNativeMaterialRules.Piece> list) {
        ArrayList<SwordNativeMaterialRules.Piece> arrayList = new ArrayList<SwordNativeMaterialRules.Piece>(Collections.nCopies(27, EMPTY));
        ArrayList<SwordNativeMaterialRules.Piece> arrayList2 = new ArrayList<SwordNativeMaterialRules.Piece>(list);
        SwordNativeMaterialRules.Piece piece = SwordCompatibilityRules.take(arrayList2, "minecraft:stick");
        if (piece == null) {
            piece = SwordCompatibilityRules.take(arrayList2, "minecraft:blaze_rod");
        }
        if (piece == null) {
            piece = (SwordNativeMaterialRules.Piece)arrayList2.remove(0);
        }
        arrayList.set(CELL_ORDER[0], piece);
        SwordNativeMaterialRules.Piece piece2 = SwordCompatibilityRules.take(arrayList2, "minecraft:string");
        if (piece2 == null) {
            piece2 = SwordCompatibilityRules.take(arrayList2, "minecraft:leather");
        }
        if (piece2 == null) {
            piece2 = (SwordNativeMaterialRules.Piece)arrayList2.remove(0);
        }
        arrayList.set(CELL_ORDER[1], piece2);
        for (int i = 0; i < arrayList2.size(); ++i) {
            arrayList.set(CELL_ORDER[i + 2], (SwordNativeMaterialRules.Piece)arrayList2.get(i));
        }
        return List.copyOf(arrayList);
    }

    private static SwordNativeMaterialRules.Piece take(List<SwordNativeMaterialRules.Piece> list, String string) {
        for (int i = 0; i < list.size(); ++i) {
            if (!list.get(i).itemId().equals(string)) continue;
            return list.remove(i);
        }
        return null;
    }

    private static boolean plank(String string) {
        boolean bl;
        block18: {
            block17: {
                if (string == null) break block17;
                switch (string) {
                    case "minecraft:oak_planks": 
                    case "minecraft:spruce_planks": 
                    case "minecraft:birch_planks": 
                    case "minecraft:jungle_planks": 
                    case "minecraft:acacia_planks": 
                    case "minecraft:dark_oak_planks": 
                    case "minecraft:mangrove_planks": 
                    case "minecraft:cherry_planks": 
                    case "minecraft:bamboo_planks": 
                    case "minecraft:crimson_planks": 
                    case "minecraft:warped_planks": 
                    case "cataclysm:chorus_planks": {
                        break;
                    }
                    default: {
                        break block17;
                    }
                }
                bl = true;
                break block18;
            }
            bl = false;
        }
        return bl;
    }

    private static boolean validPiece(SwordNativeMaterialRules.Piece piece) {
        return piece != null && SwordCompatibilityRules.validId(piece.itemId()) && piece.origin() != null && (piece.origin() == SwordNativeMaterialRules.Origin.SHARED ? piece.count() == 0 : piece.count() > 0);
    }

    private static boolean validId(String string) {
        return string != null && string.matches("[a-z0-9_.-]+:[a-z0-9/._-]+");
    }

    private static Map<String, SwordFunctionRules.Profile> profiles() {
        LinkedHashMap<String, SwordFunctionRules.Profile> linkedHashMap = new LinkedHashMap<String, SwordFunctionRules.Profile>();
        for (Definition definition : DEFINITIONS.values()) {
            linkedHashMap.put(definition.weaponId(), SwordCompatibilityRules.createProfile(definition));
        }
        return Map.copyOf(linkedHashMap);
    }

    private static Map<String, SwordFunctionRules.Profile> mechanicalProfiles() {
        LinkedHashMap<String, SwordFunctionRules.Profile> linkedHashMap = new LinkedHashMap<String, SwordFunctionRules.Profile>();
        for (Definition definition : MECHANICAL.values()) {
            linkedHashMap.put(definition.weaponId(), SwordCompatibilityRules.createProfile(definition));
        }
        return Map.copyOf(linkedHashMap);
    }

    private static SwordFunctionRules.Profile createProfile(Definition definition) {
        if (definition.weaponId().equals("minecraft:iron_sword")) {
            return AdditionalSwordProfiles.enrich(SwordFunctionRules.iron(), definition);
        }
        ArrayList<Integer> arrayList = new ArrayList<Integer>(Collections.nCopies(27, -1));
        for (int i = 0; i < definition.maximumCells(); ++i) {
            arrayList.set(CELL_ORDER[i], i < definition.initialCells() ? 0 : (i - definition.initialCells()) / 2 + 1);
        }
        ArrayList<SwordFunctionRules.Values> arrayList2 = new ArrayList<SwordFunctionRules.Values>();
        for (SwordFunctionRules.Values object22 : SwordFunctionRules.iron().cellWeights()) {
            arrayList2.add(new SwordFunctionRules.Values(Math.max(0.15, object22.handle()), Math.max(0.15, object22.guard()), Math.max(0.15, object22.core()), Math.max(0.15, object22.stability()), Math.max(0.15, object22.blade())));
        }
        Map<String, SwordFunctionRules.Values> map = ITEM_VALUES;
        if (NonManorWeaponComponentRules.kind(definition.weaponId()) != null) {
            LinkedHashMap<String, SwordFunctionRules.Values> linkedHashMap = new LinkedHashMap<String, SwordFunctionRules.Values>(ITEM_VALUES);
            for (SwordNativeMaterialRules.Piece piece : definition.budget()) {
                linkedHashMap.put(piece.itemId(), SwordCompatibilityRules.materialFunctions(NonManorWeaponComponentRules.material(piece.itemId())));
            }
            map = Map.copyOf(linkedHashMap);
        }
        SwordFunctionRules.Profile profile = new SwordFunctionRules.Profile(SwordFunctionRules.ZERO, arrayList2, arrayList, MATERIAL_VALUES, SwordFunctionRules.iron().cellBaseValues(), map);
        ArrayList arrayList3 = new ArrayList();
        for (SwordNativeMaterialRules.Piece piece : SwordCompatibilityRules.layout(definition.budget())) {
            arrayList3.add(new SwordFunctionRules.Input(piece.itemId(), SwordCompatibilityRules.material(piece.itemId()), piece.count() > 0));
        }
        SwordFunctionRules.Values values = SwordFunctionRules.evaluateItems(profile, arrayList3, 0).total();
        double d = 0.995;
        SwordFunctionRules.Values values2 = new SwordFunctionRules.Values(values.handle() * d, values.guard() * d, values.core() * d, values.stability() * d, values.blade() * d);
        for (SwordFunctionRules.Function function : SwordFunctionRules.Function.values()) {
            if (!(values2.value(function) <= 0.0)) continue;
            throw new IllegalStateException("Missing native weapon function");
        }
        return AdditionalSwordProfiles.enrich(new SwordFunctionRules.Profile(values2, arrayList2, arrayList, MATERIAL_VALUES, profile.cellBaseValues(), map), definition);
    }

    private static Map<FoodWeaponBuildRules.Material, SwordFunctionRules.Values> materialValues() {
        EnumMap<FoodWeaponBuildRules.Material, SwordFunctionRules.Values> enumMap = new EnumMap<FoodWeaponBuildRules.Material, SwordFunctionRules.Values>(FoodWeaponBuildRules.Material.class);
        enumMap.putAll(SwordFunctionRules.iron().materials());
        enumMap.put(FoodWeaponBuildRules.Material.DIAMOND, new SwordFunctionRules.Values(0.5, 1.8, 1.8, 1.6, 2.0));
        enumMap.put(FoodWeaponBuildRules.Material.NETHERITE, new SwordFunctionRules.Values(0.65, 2.5, 2.6, 2.5, 2.5));
        enumMap.put(FoodWeaponBuildRules.Material.IGNITIUM, new SwordFunctionRules.Values(0.6, 2.6, 3.4, 2.7, 3.2));
        enumMap.put(FoodWeaponBuildRules.Material.CURSIUM, new SwordFunctionRules.Values(0.7, 2.8, 3.3, 2.8, 3.3));
        enumMap.put(FoodWeaponBuildRules.Material.WITHERITE, new SwordFunctionRules.Values(0.6, 2.2, 2.3, 2.3, 2.6));
        enumMap.put(FoodWeaponBuildRules.Material.ANCIENT_METAL, new SwordFunctionRules.Values(0.6, 1.6, 1.7, 1.9, 1.8));
        enumMap.put(FoodWeaponBuildRules.Material.BLACK_STEEL, new SwordFunctionRules.Values(0.55, 1.8, 1.2, 1.8, 1.6));
        enumMap.put(FoodWeaponBuildRules.Material.ENDERITE, new SwordFunctionRules.Values(0.6, 2.5, 3.0, 2.2, 2.4));
        enumMap.put(FoodWeaponBuildRules.Material.BLAZE, new SwordFunctionRules.Values(1.0, 0.8, 1.8, 0.8, 0.8));
        return Map.copyOf(enumMap);
    }

    private static Map<String, Definition> mechanicalDefinitions() {
        LinkedHashMap<String, Definition> linkedHashMap = new LinkedHashMap<String, Definition>();
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:the_incinerator", Form.SWORD, "cataclysm:the_incinerator", 19, SwordCompatibilityRules.recipe("minecraft:diamond", 2, "minecraft:stick", 1, "minecraft:netherite_ingot", 1, "minecraft:blaze_rod", 8, "cataclysm:ignitium_ingot", 3), "minecraft:string", "minecraft:obsidian", "minecraft:nether_brick");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:meat_shredder", Form.SAW, "cataclysm:meat_shredder", 17, SwordCompatibilityRules.recipe("create:iron_sheet", 8, "minecraft:redstone", 4, "cataclysm:witherite_ingot", 2, "create:large_cogwheel", 1), "minecraft:string", "minecraft:leather");
        return Map.copyOf(linkedHashMap);
    }

    private static void add(Map<String, Definition> map, String string, Form form, String string2, int n, List<SwordNativeMaterialRules.Piece> list, String ... stringArray) {
        if (stringArray.length > 7) {
            throw new IllegalArgumentException("Environment reference budget exceeds limit");
        }
        ArrayList<SwordNativeMaterialRules.Piece> arrayList = new ArrayList<SwordNativeMaterialRules.Piece>(list);
        for (String string3 : stringArray) {
            arrayList.add(new SwordNativeMaterialRules.Piece(string3, 1, SwordNativeMaterialRules.Origin.THEME));
        }
        if (map.putIfAbsent(string, new Definition(string, form, string2, n, arrayList)) != null) {
            throw new IllegalArgumentException("Duplicate compatibility weapon");
        }
    }

    private static List<SwordNativeMaterialRules.Piece> recipe(Object ... objectArray) {
        if (objectArray.length % 2 != 0) {
            throw new IllegalArgumentException("Recipe requires identifier/count pairs");
        }
        ArrayList<SwordNativeMaterialRules.Piece> arrayList = new ArrayList<SwordNativeMaterialRules.Piece>();
        for (int i = 0; i < objectArray.length; i += 2) {
            String string = (String)objectArray[i];
            int n = (Integer)objectArray[i + 1];
            if (n < 1 || n > 27) {
                throw new IllegalArgumentException("Invalid recipe count");
            }
            for (int j = 0; j < n; ++j) {
                arrayList.add(new SwordNativeMaterialRules.Piece(string, 1, SwordNativeMaterialRules.Origin.RECIPE));
            }
        }
        return List.copyOf(arrayList);
    }

    public static List<SwordNativeMaterialRules.Piece> upgradeReferenceBudget(String string, List<SwordNativeMaterialRules.Piece> list) {
        if (NonManorWeaponComponentRules.kind(string) != null && SwordCompatibilityRules.supports(string)) {
            return SwordCompatibilityRules.nativeBudget(string);
        }
        if (!((AdditionalSwordProfiles.supports(string) || string != null && (string.startsWith("irons_spellbooks:") || string.startsWith("legendary_monsters:") || string.startsWith("armamentarium:") || string.startsWith("rajswordmod:"))) && SwordCompatibilityRules.supports(string))) {
            return list;
        }
        List<SwordNativeMaterialRules.Piece> list2 = SwordCompatibilityRules.canonicalBudget(string, list);
        if (!list2.isEmpty()) {
            return list2;
        }
        if (list != null) {
            ArrayList<SwordNativeMaterialRules.Piece> arrayList = new ArrayList<SwordNativeMaterialRules.Piece>();
            for (SwordNativeMaterialRules.Piece piece : list) {
                if (piece.origin() != SwordNativeMaterialRules.Origin.RECIPE) continue;
                arrayList.add(piece);
            }
            if (!arrayList.isEmpty()) {
                list2 = SwordCompatibilityRules.canonicalBudget(string, arrayList);
                return list2.isEmpty() ? list : list2;
            }
        }
        return SwordCompatibilityRules.nativeBudget(string);
    }

    private static Map<String, SwordFunctionRules.Values> itemValues() {
        LinkedHashMap<String, SwordFunctionRules.Values> linkedHashMap = new LinkedHashMap<String, SwordFunctionRules.Values>(SwordFunctionRules.iron().itemValues());
        for (FoodWeaponBuildRules.Material material : FoodWeaponBuildRules.Material.values()) {
            for (String string : WeaponMaterialCatalogRules.offerings(material)) {
                linkedHashMap.put(string, SwordCompatibilityRules.materialFunctions(material));
            }
        }
        for (String string : List.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "bamboo", "crimson", "warped")) {
            linkedHashMap.put("minecraft:" + string + "_planks", SwordCompatibilityRules.materialFunctions(FoodWeaponBuildRules.Material.WOOD));
        }
        linkedHashMap.put("cataclysm:chorus_planks", SwordCompatibilityRules.materialFunctions(FoodWeaponBuildRules.Material.WOOD));
        linkedHashMap.put("minecraft:netherite_scrap", new SwordFunctionRules.Values(0.12, 0.65, 0.65, 0.65, 0.65));
        linkedHashMap.put("minecraft:blackstone", SwordCompatibilityRules.materialFunctions(FoodWeaponBuildRules.Material.STONE));
        linkedHashMap.put("minecraft:cobbled_deepslate", SwordCompatibilityRules.materialFunctions(FoodWeaponBuildRules.Material.STONE));
        linkedHashMap.put("create:iron_sheet", new SwordFunctionRules.Values(0.15, 0.95, 0.7, 0.7, 0.85));
        linkedHashMap.put("create:large_cogwheel", new SwordFunctionRules.Values(0.8, 0.9, 0.5, 1.1, 0.2));
        linkedHashMap.put("minecraft:nether_brick", new SwordFunctionRules.Values(0.1, 1.0, 0.65, 1.2, 0.6));
        linkedHashMap.put("minecraft:end_stone", new SwordFunctionRules.Values(0.1, 1.1, 0.3, 1.5, 0.55));
        linkedHashMap.put("minecraft:purpur_block", new SwordFunctionRules.Values(0.1, 1.2, 0.6, 1.2, 0.6));
        linkedHashMap.put("minecraft:sandstone", new SwordFunctionRules.Values(0.1, 0.7, 0.25, 1.0, 0.45));
        linkedHashMap.put("minecraft:prismarine_shard", new SwordFunctionRules.Values(0.15, 0.75, 0.65, 0.85, 0.8));
        linkedHashMap.put("minecraft:prismarine_crystals", new SwordFunctionRules.Values(0.1, 0.6, 1.5, 0.7, 0.85));
        linkedHashMap.put("minecraft:nautilus_shell", new SwordFunctionRules.Values(0.3, 1.0, 1.3, 1.2, 0.7));
        linkedHashMap.put("minecraft:ender_pearl", new SwordFunctionRules.Values(0.15, 0.8, 1.6, 0.6, 0.2));
        linkedHashMap.put("minecraft:soul_sand", new SwordFunctionRules.Values(0.05, 0.5, 0.8, 0.7, 0.05));
        linkedHashMap.put("cataclysm:koboleton_bone", new SwordFunctionRules.Values(0.9, 1.1, 1.0, 0.9, 0.7));
        linkedHashMap.put("cataclysm:crystallized_coral", new SwordFunctionRules.Values(0.2, 1.4, 1.2, 1.5, 1.6));
        linkedHashMap.put("cataclysm:lacrima", new SwordFunctionRules.Values(0.6, 1.5, 2.2, 1.3, 1.7));
        linkedHashMap.put("cataclysm:essence_of_the_storm", new SwordFunctionRules.Values(0.3, 1.5, 3.0, 1.4, 1.5));
        linkedHashMap.put("cataclysm:void_core", new SwordFunctionRules.Values(0.15, 1.2, 4.0, 0.9, 0.15));
        linkedHashMap.put("cataclysm:sandstorm_in_a_bottle", new SwordFunctionRules.Values(0.15, 0.8, 3.5, 1.0, 0.1));
        linkedHashMap.put("cataclysm:gauntlet_of_guard", new SwordFunctionRules.Values(1.2, 3.5, 4.0, 3.0, 2.8));
        linkedHashMap.put("cataclysm:bulwark_of_the_flame", new SwordFunctionRules.Values(0.6, 3.4, 2.4, 3.5, 0.3));
        linkedHashMap.put("cataclysm:infernal_forge", new SwordFunctionRules.Values(1.4, 3.0, 4.0, 3.0, 3.2));
        linkedHashMap.put("minecraft:chain", new SwordFunctionRules.Values(0.5, 1.2, 0.2, 1.2, 0.4));
        for (String string : List.of("irons_spellbooks:weapon_parts", "irons_spellbooks:arcane_ingot", "irons_spellbooks:mithril_ingot", "armamentarium:greatalloy_ingot")) {
            linkedHashMap.put(string, new SwordFunctionRules.Values(0.6, 1.6, 1.5, 1.6, 1.7));
        }
        for (String string : List.of("irons_spellbooks:arcane_essence", "irons_spellbooks:ice_venom_vial", "legendary_monsters:chorus_crystal", "legendary_monsters:infected_chorus_fruit", "legendary_monsters:void_gem")) {
            linkedHashMap.put(string, new SwordFunctionRules.Values(0.2, 0.6, 1.5, 0.8, 0.7));
        }
        for (String string : List.of("irons_spellbooks:pyrium_ingot", "legendary_monsters:enderitium_ingot")) {
            linkedHashMap.put(string, new SwordFunctionRules.Values(0.6, 2.2, 2.6, 2.2, 2.5));
        }
        linkedHashMap.put("minecraft:ice", new SwordFunctionRules.Values(0.1, 0.6, 1.0, 0.8, 0.2));
        for (String string : List.of("legendary_monsters:knights_sword_blade_half", "legendary_monsters:metal_debris", "legendary_monsters:withered_bone", "legendary_monsters:withered_horn", "armamentarium:greatalloy_nugget")) {
            linkedHashMap.put(string, new SwordFunctionRules.Values(0.5, 1.0, 0.5, 1.0, 1.0));
        }
        return Map.copyOf(linkedHashMap);
    }

    private static Map<String, Definition> definitionsInternal() {
        LinkedHashMap<String, Definition> linkedHashMap = new LinkedHashMap<String, Definition>();
        SwordCompatibilityRules.add(linkedHashMap, "minecraft:wooden_sword", Form.SWORD, "minecraft:wooden_sword", 7, SwordCompatibilityRules.recipe("minecraft:oak_planks", 2, "minecraft:stick", 1), "minecraft:string", "minecraft:flint");
        SwordCompatibilityRules.add(linkedHashMap, "minecraft:stone_sword", Form.SWORD, "minecraft:stone_sword", 8, SwordCompatibilityRules.recipe("minecraft:cobblestone", 2, "minecraft:stick", 1), "minecraft:string", "minecraft:flint");
        linkedHashMap.put("minecraft:iron_sword", new Definition("minecraft:iron_sword", Form.SWORD, "minecraft:iron_sword", 9, SwordNativeMaterialRules.ironPrototype()));
        SwordCompatibilityRules.add(linkedHashMap, "minecraft:golden_sword", Form.SWORD, "minecraft:golden_sword", 9, SwordCompatibilityRules.recipe("minecraft:gold_ingot", 2, "minecraft:stick", 1), "minecraft:string", "minecraft:cobblestone", "minecraft:flint");
        SwordCompatibilityRules.add(linkedHashMap, "minecraft:diamond_sword", Form.SWORD, "minecraft:diamond_sword", 11, SwordCompatibilityRules.recipe("minecraft:diamond", 2, "minecraft:stick", 1), "minecraft:string", "minecraft:cobblestone", "minecraft:flint");
        SwordCompatibilityRules.add(linkedHashMap, "minecraft:netherite_sword", Form.SWORD, "minecraft:netherite_sword_smithing", 15, SwordCompatibilityRules.recipe("minecraft:diamond", 2, "minecraft:stick", 1, "minecraft:netherite_scrap", 4, "minecraft:gold_ingot", 4), "minecraft:string", "minecraft:obsidian", "minecraft:blaze_rod");
        SwordCompatibilityRules.add(linkedHashMap, "minecraft:wooden_axe", Form.AXE, "minecraft:wooden_axe", 8, SwordCompatibilityRules.recipe("minecraft:oak_planks", 3, "minecraft:stick", 2), "minecraft:string", "minecraft:flint");
        SwordCompatibilityRules.add(linkedHashMap, "minecraft:stone_axe", Form.AXE, "minecraft:stone_axe", 9, SwordCompatibilityRules.recipe("minecraft:cobblestone", 3, "minecraft:stick", 2), "minecraft:string", "minecraft:flint");
        SwordCompatibilityRules.add(linkedHashMap, "minecraft:iron_axe", Form.AXE, "minecraft:iron_axe", 10, SwordCompatibilityRules.recipe("minecraft:iron_ingot", 3, "minecraft:stick", 2), "minecraft:string", "minecraft:flint");
        SwordCompatibilityRules.add(linkedHashMap, "minecraft:golden_axe", Form.AXE, "minecraft:golden_axe", 10, SwordCompatibilityRules.recipe("minecraft:gold_ingot", 3, "minecraft:stick", 2), "minecraft:string", "minecraft:flint");
        SwordCompatibilityRules.add(linkedHashMap, "minecraft:diamond_axe", Form.AXE, "minecraft:diamond_axe", 12, SwordCompatibilityRules.recipe("minecraft:diamond", 3, "minecraft:stick", 2), "minecraft:string", "minecraft:flint");
        SwordCompatibilityRules.add(linkedHashMap, "minecraft:netherite_axe", Form.AXE, "minecraft:netherite_axe_smithing", 17, SwordCompatibilityRules.recipe("minecraft:diamond", 3, "minecraft:stick", 2, "minecraft:netherite_scrap", 4, "minecraft:gold_ingot", 4), "minecraft:string", "minecraft:obsidian", "minecraft:blaze_rod");
        SwordCompatibilityRules.add(linkedHashMap, "minecraft:trident", Form.SPEAR, "", 13, List.of(), "minecraft:prismarine_shard", "minecraft:prismarine_shard", "minecraft:prismarine_crystals", "minecraft:nautilus_shell", "minecraft:stick", "minecraft:stick", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:black_steel_sword", Form.SWORD, "cataclysm:black_steel_sword", 11, SwordCompatibilityRules.recipe("cataclysm:black_steel_ingot", 2, "minecraft:stick", 1), "minecraft:string", "minecraft:cobblestone", "minecraft:flint");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:black_steel_axe", Form.AXE, "cataclysm:black_steel_axe", 12, SwordCompatibilityRules.recipe("cataclysm:black_steel_ingot", 3, "minecraft:stick", 2), "minecraft:string", "minecraft:flint");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:athame", Form.DAGGER, "", 9, List.of(), "minecraft:iron_ingot", "minecraft:bone", "minecraft:stick", "minecraft:string", "minecraft:prismarine_shard", "minecraft:flint");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:khopesh", Form.SWORD, "", 11, List.of(), "cataclysm:ancient_metal_ingot", "cataclysm:ancient_metal_ingot", "cataclysm:koboleton_bone", "minecraft:stick", "minecraft:string", "minecraft:sandstone");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:coral_spear", Form.SPEAR, "", 11, List.of(), "cataclysm:crystallized_coral", "cataclysm:crystallized_coral", "minecraft:stick", "minecraft:stick", "minecraft:string", "minecraft:prismarine_shard");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:coral_bardiche", Form.AXE, "", 13, List.of(), "cataclysm:crystallized_coral", "cataclysm:crystallized_coral", "cataclysm:crystallized_coral", "minecraft:stick", "minecraft:stick", "minecraft:string", "minecraft:prismarine_crystals");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:ancient_spear", Form.SPEAR, "cataclysm:ancient_spear", 13, SwordCompatibilityRules.recipe("cataclysm:ancient_metal_ingot", 4), "cataclysm:koboleton_bone", "minecraft:string", "minecraft:sandstone");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:the_annihilator", Form.AXE, "cataclysm:the_annihilator", 15, SwordCompatibilityRules.recipe("cataclysm:cursium_ingot", 2, "cataclysm:black_steel_ingot", 1), "minecraft:string", "minecraft:bone", "minecraft:soul_sand");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:soul_render", Form.SCYTHE, "cataclysm:soul_render", 15, SwordCompatibilityRules.recipe("cataclysm:cursium_ingot", 3, "cataclysm:black_steel_ingot", 2), "minecraft:string", "minecraft:bone", "minecraft:soul_sand");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:the_incinerator", Form.SWORD, "cataclysm:the_incinerator", 19, SwordCompatibilityRules.recipe("minecraft:diamond", 2, "minecraft:stick", 1, "minecraft:netherite_ingot", 1, "minecraft:blaze_rod", 4, "cataclysm:ignitium_ingot", 2), "minecraft:string", "minecraft:obsidian", "minecraft:nether_brick");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:the_immolator", Form.SWORD, "cataclysm:weapon_infusion/the_immolator", 17, SwordCompatibilityRules.recipe("cataclysm:cursium_ingot", 2, "cataclysm:black_steel_ingot", 1, "cataclysm:ignitium_ingot", 1), "minecraft:string", "minecraft:bone", "minecraft:nether_brick");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:astrape", Form.SPEAR, "cataclysm:astrape", 15, SwordCompatibilityRules.recipe("cataclysm:lacrima", 3, "cataclysm:essence_of_the_storm", 2), "minecraft:string", "minecraft:quartz");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:ceraunus", Form.AXE, "cataclysm:ceraunus", 17, SwordCompatibilityRules.recipe("cataclysm:lacrima", 3, "cataclysm:essence_of_the_storm", 2), "minecraft:string", "minecraft:quartz", "minecraft:iron_ingot");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:meat_shredder", Form.SAW, "cataclysm:meat_shredder", 17, SwordCompatibilityRules.recipe("minecraft:iron_ingot", 9, "minecraft:redstone", 2, "cataclysm:witherite_ingot", 2), "minecraft:string", "minecraft:leather");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:gauntlet_of_guard", Form.GAUNTLET, "", 15, List.of(), "minecraft:obsidian", "minecraft:obsidian", "minecraft:ender_pearl", "minecraft:end_stone", "minecraft:leather", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:gauntlet_of_bulwark", Form.GAUNTLET, "cataclysm:weapon_infusion/gauntlet_of_bulwark", 17, SwordCompatibilityRules.recipe("cataclysm:gauntlet_of_guard", 1, "cataclysm:bulwark_of_the_flame", 1), "minecraft:obsidian", "minecraft:blaze_rod", "minecraft:leather", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:gauntlet_of_maelstrom", Form.GAUNTLET, "cataclysm:weapon_infusion/gauntlet_of_maelstrom", 17, SwordCompatibilityRules.recipe("cataclysm:gauntlet_of_guard", 1, "cataclysm:sandstorm_in_a_bottle", 1), "minecraft:end_stone", "minecraft:sandstone", "minecraft:leather", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:infernal_forge", Form.HAMMER, "", 15, List.of(), "minecraft:netherite_scrap", "minecraft:netherite_scrap", "minecraft:blaze_rod", "minecraft:obsidian", "minecraft:nether_brick", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:void_forge", Form.HAMMER, "cataclysm:weapon_infusion/void_forge", 17, SwordCompatibilityRules.recipe("cataclysm:infernal_forge", 1, "cataclysm:void_core", 1), "minecraft:obsidian", "minecraft:end_stone", "minecraft:ender_pearl", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:brontes", Form.HAMMER, "cataclysm:weapon_infusion/brontes", 17, SwordCompatibilityRules.recipe("cataclysm:infernal_forge", 1, "cataclysm:lacrima", 3, "cataclysm:essence_of_the_storm", 2), "minecraft:quartz", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "cataclysm:tidal_claws", Form.GAUNTLET, "", 15, List.of(), "minecraft:prismarine_crystals", "minecraft:prismarine_crystals", "minecraft:prismarine_shard", "minecraft:nautilus_shell", "minecraft:leather", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "irons_spellbooks:amethyst_rapier", Form.SWORD, "irons_spellbooks:amethyst_rapier", 11, SwordCompatibilityRules.recipe("minecraft:amethyst_shard", 2, "minecraft:chain", 2, "irons_spellbooks:weapon_parts", 1), "irons_spellbooks:arcane_essence", "minecraft:string", "minecraft:quartz");
        SwordCompatibilityRules.add(linkedHashMap, "irons_spellbooks:spellbreaker", Form.SWORD, "irons_spellbooks:spellbreaker", 13, SwordCompatibilityRules.recipe("irons_spellbooks:mithril_ingot", 2, "minecraft:diamond", 2, "irons_spellbooks:weapon_parts", 1), "irons_spellbooks:arcane_essence", "minecraft:string", "minecraft:quartz");
        SwordCompatibilityRules.add(linkedHashMap, "irons_spellbooks:magehunter", Form.SWORD, "", 13, List.of(), "minecraft:iron_ingot", "minecraft:iron_ingot", "irons_spellbooks:arcane_essence", "minecraft:quartz", "minecraft:leather", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "irons_spellbooks:claymore", Form.SWORD, "", 13, List.of(), "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:stick", "minecraft:leather", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "irons_spellbooks:keeper_flamberge", Form.SWORD, "", 13, List.of(), "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:bone", "minecraft:coal", "minecraft:leather", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "irons_spellbooks:legionnaire_flamberge", Form.SWORD, "irons_spellbooks:legionnaire_flamberge_repair", 17, SwordCompatibilityRules.recipe("irons_spellbooks:keeper_flamberge", 1, "irons_spellbooks:pyrium_ingot", 1), "minecraft:blaze_rod", "minecraft:obsidian", "minecraft:leather", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "irons_spellbooks:misery", Form.SWORD, "", 15, List.of(), "minecraft:iron_ingot", "minecraft:bone", "minecraft:soul_sand", "minecraft:obsidian", "minecraft:leather", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "irons_spellbooks:decrepit_scythe", Form.SCYTHE, "", 13, List.of(), "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:bone", "minecraft:stick", "minecraft:leather", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "irons_spellbooks:hellrazor", Form.SCYTHE, "irons_spellbooks:decrepit_scythe_repair", 17, SwordCompatibilityRules.recipe("irons_spellbooks:decrepit_scythe", 1, "irons_spellbooks:pyrium_ingot", 1), "minecraft:blaze_rod", "minecraft:obsidian", "minecraft:leather", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "irons_spellbooks:boreal_blade", Form.SWORD, "irons_spellbooks:boreal_blade", 15, SwordCompatibilityRules.recipe("irons_spellbooks:ice_venom_vial", 2, "irons_spellbooks:mithril_ingot", 1, "irons_spellbooks:weapon_parts", 1, "minecraft:ice", 1), "minecraft:prismarine_crystals", "minecraft:leather", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "legendary_monsters:knights_sword", Form.SWORD, "legendary_monsters:knights_sword", 11, SwordCompatibilityRules.recipe("legendary_monsters:knights_sword_blade_half", 2, "minecraft:stick", 1), "minecraft:string", "minecraft:cobblestone", "minecraft:flint");
        SwordCompatibilityRules.add(linkedHashMap, "legendary_monsters:shattered_greatsword", Form.SWORD, "legendary_monsters:shattered_greatsword", 13, SwordCompatibilityRules.recipe("legendary_monsters:metal_debris", 2, "minecraft:iron_nugget", 2, "minecraft:stick", 1), "minecraft:string", "minecraft:leather", "minecraft:flint");
        SwordCompatibilityRules.add(linkedHashMap, "legendary_monsters:chorus_blade", Form.SWORD, "legendary_monsters:chorus_blade", 15, SwordCompatibilityRules.recipe("legendary_monsters:chorus_crystal", 1, "legendary_monsters:infected_chorus_fruit", 2, "minecraft:iron_sword", 1), "minecraft:string", "minecraft:end_stone", "minecraft:ender_pearl");
        SwordCompatibilityRules.add(linkedHashMap, "legendary_monsters:void_sword", Form.SWORD, "legendary_monsters:void_sword", 15, SwordCompatibilityRules.recipe("legendary_monsters:void_gem", 2, "legendary_monsters:withered_bone", 1), "minecraft:string", "minecraft:end_stone", "minecraft:obsidian");
        SwordCompatibilityRules.add(linkedHashMap, "legendary_monsters:enderitium_sword", Form.SWORD, "legendary_monsters:enderitium_sword", 15, SwordCompatibilityRules.recipe("minecraft:diamond_sword", 1, "legendary_monsters:enderitium_ingot", 1), "minecraft:string", "minecraft:end_stone", "minecraft:ender_pearl");
        SwordCompatibilityRules.add(linkedHashMap, "legendary_monsters:soul_great_sword", Form.SWORD, "", 15, List.of(), "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:obsidian", "minecraft:soul_sand", "minecraft:leather", "minecraft:string");
        SwordCompatibilityRules.add(linkedHashMap, "armamentarium:greatalloy_sword", Form.SWORD, "armamentarium:greatalloy_sword", 13, SwordCompatibilityRules.recipe("armamentarium:greatalloy_nugget", 1, "armamentarium:greatalloy_ingot", 1, "minecraft:stick", 1), "minecraft:string", "minecraft:cobblestone", "minecraft:flint");
        SwordCompatibilityRules.add(linkedHashMap, "armamentarium:severance_shotel", Form.SWORD, "armamentarium:severance_shotel_recipe", 13, SwordCompatibilityRules.recipe("minecraft:iron_nugget", 1, "minecraft:iron_ingot", 2, "armamentarium:greatalloy_nugget", 1, "minecraft:flint", 1), "minecraft:string", "minecraft:leather", "minecraft:quartz");
        SwordCompatibilityRules.add(linkedHashMap, "armamentarium:severance_scythe", Form.SCYTHE, "armamentarium:severance_scythe", 17, SwordCompatibilityRules.recipe("minecraft:quartz", 1, "minecraft:iron_ingot", 1, "minecraft:ghast_tear", 1, "minecraft:phantom_membrane", 1, "armamentarium:severance_shotel", 1, "armamentarium:greatalloy_ingot", 1), "minecraft:string", "minecraft:leather", "minecraft:bone");
        SwordCompatibilityRules.add(linkedHashMap, "rajswordmod:sentinel_sword", Form.SWORD, "", 15, List.of(), "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:quartz", "minecraft:obsidian", "minecraft:leather", "minecraft:string");
        LinkedHashMap<String, Definition> linkedHashMap2 = linkedHashMap;
        AdditionalSwordProfiles.addTo(linkedHashMap2);
        linkedHashMap2.putAll(SwordCompatibilityRules.nonManorDefinitions());
        return Map.copyOf(linkedHashMap2);
    }

    public static boolean attachmentsExcluded(String string) {
        return string != null && string.startsWith("armamentarium:");
    }

    public static boolean supports(String string) {
        return string != null && !SwordCompatibilityRules.attachmentsExcluded(string) && DEFINITIONS.containsKey(string);
    }

    public static SwordFunctionRules.Profile profile(String string) {
        return string == null || SwordCompatibilityRules.attachmentsExcluded(string) ? null : PROFILES.get(string);
    }

    private static Map<String, Definition> nonManorDefinitions() {
        LinkedHashMap<String, Definition> linkedHashMap = new LinkedHashMap<String, Definition>();
        for (String string : NonManorWeaponComponentRules.weaponsById().keySet()) {
            if (string.equals("ftboss:frost_queen_staff") || string.equals("ftboss:barrel_crossbow")) continue;
            Form form = string.equals("ftboss:tide_trident") ? Form.SPEAR : Form.SWORD;
            linkedHashMap.put(string, new Definition(string, form, "", 13, NonManorWeaponComponentRules.budget(string)));
        }
        return Map.copyOf(linkedHashMap);
    }

    public record Definition(String weaponId, Form form, String recipeId, int initialCells, List<SwordNativeMaterialRules.Piece> budget) {
        public Definition {
            list = List.copyOf(list);
            if (!SwordCompatibilityRules.validId(string) || form == null || string2 == null || !string2.isEmpty() && !SwordCompatibilityRules.validId(string2) || n < 5 || n > 19 || list.size() < 4 || list.size() > n) {
                throw new IllegalArgumentException("Invalid weapon compatibility definition");
            }
            for (SwordNativeMaterialRules.Piece piece : list) {
                if (SwordCompatibilityRules.validPiece(piece) && piece.count() == 1 && piece.origin() != SwordNativeMaterialRules.Origin.SHARED) continue;
                throw new IllegalArgumentException("Compatibility budget requires single pieces");
            }
        }

        public int maximumCells() {
            return this.initialCells + 8;
        }

        public boolean crafted() {
            return !this.recipeId.isEmpty();
        }
    }

    public static enum Form {
        SWORD,
        DAGGER,
        SPEAR,
        AXE,
        SCYTHE,
        GAUNTLET,
        HAMMER,
        SAW;

    }
}

