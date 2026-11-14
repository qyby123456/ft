package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordFunctionRules.Function;
import net.farmingtales.ftboss.util.SwordFunctionRules.Input;
import net.farmingtales.ftboss.util.SwordFunctionRules.Profile;
import net.farmingtales.ftboss.util.SwordFunctionRules.Values;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Origin;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;

/** 近战构筑纯规则；2026-10-20 补齐铁魔法等模组剑的实际配方、环境参考与进化锁格，保留原版/灾变及铁剑契约。 */
public final class SwordCompatibilityRules {
    private static final java.util.regex.Pattern ITEM_IDENTIFIER = java.util.regex.Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");
    private static final int[] CELL_ORDER = {2, 3, 13, 14, 15, 16, 17, 6, 20, 7, 21, 8, 22, 9, 23, 10, 24, 4, 5, 11, 12, 18, 19, 25, 26, 0, 1};
    private static final double INITIAL_MARGIN = 0.005;
    private static final Piece EMPTY = new Piece("minecraft:air", 0, Origin.SHARED);
    private static final Map<Material, Values> MATERIAL_VALUES = materialValues();
    private static final Map<String, Values> ITEM_VALUES = itemValues();
    private static final Map<String, Definition> DEFINITIONS = definitionsInternal();
    private static final Map<String, Definition> MECHANICAL = mechanicalDefinitions();
    private static final Map<String, Profile> PROFILES = profiles();
    private static final Map<String, Profile> MECHANICAL_PROFILES = mechanicalProfiles();

    public enum Form { SWORD, DAGGER, SPEAR, AXE, SCYTHE, GAUNTLET, HAMMER, SAW }

    public record Definition(String weaponId, Form form, String recipeId, int initialCells, List<Piece> budget) {
        public Definition {
            budget = List.copyOf(budget);
            if (!validId(weaponId) || form == null || recipeId == null || !recipeId.isEmpty() && !validId(recipeId)
                    || initialCells < 5 || initialCells > 19 || budget.size() < 4 || budget.size() > initialCells) {
                throw new IllegalArgumentException("Invalid weapon compatibility definition");
            }
            for (Piece piece : budget) if (!validPiece(piece) || piece.count() != 1 || piece.origin() == Origin.SHARED) {
                throw new IllegalArgumentException("Compatibility budget requires single pieces");
            }
        }

        public int maximumCells() { return initialCells + SwordFunctionRules.MAX_EVOLUTION * 2; }
        public boolean crafted() { return !recipeId.isEmpty(); }
    }

    private SwordCompatibilityRules() {}

    public static Map<String, Definition> definitions() { return DEFINITIONS; }
    public static boolean attachmentsExcluded(String identifier) {
        return identifier != null && identifier.startsWith("armamentarium:");
    }
    public static boolean supports(String identifier) {
        return identifier != null && !attachmentsExcluded(identifier) && DEFINITIONS.containsKey(identifier);
    }
    public static Profile profile(String identifier) {
        return identifier == null || attachmentsExcluded(identifier) ? null : PROFILES.get(identifier);
    }
    public static Profile profile(String identifier, List<Piece> source) {
        Definition alternate = identifier == null ? null : MECHANICAL.get(identifier);
        return alternate != null && !canonicalBudget(alternate, source).isEmpty() ? MECHANICAL_PROFILES.get(identifier) : profile(identifier);
    }
    public static List<Piece> nativeBudget(String identifier) {
        Definition definition = identifier == null ? null : DEFINITIONS.get(identifier);
        return definition == null ? List.of() : definition.budget();
    }
    public static String nativeRecipe(String identifier) {
        Definition definition = identifier == null ? null : DEFINITIONS.get(identifier);
        return definition == null ? "" : definition.recipeId();
    }

    public static List<Piece> upgradeReferenceBudget(String identifier, List<Piece> source) {
        if (NonManorWeaponComponentRules.kind(identifier) != null && supports(identifier)) return nativeBudget(identifier);
        if (identifier == null || !(AdditionalSwordProfiles.supports(identifier) || identifier.startsWith("irons_spellbooks:") || identifier.startsWith("legendary_monsters:")
                || identifier.startsWith("armamentarium:") || identifier.startsWith("rajswordmod:")) || !supports(identifier)) return source;
        List<Piece> canonical = canonicalBudget(identifier, source);
        if (!canonical.isEmpty()) return canonical;
        if (source != null) {
            List<Piece> ingredients = new ArrayList<>();
            for (Piece piece : source) if (piece.origin() == Origin.RECIPE) ingredients.add(piece);
            if (!ingredients.isEmpty()) {
                canonical = canonicalBudget(identifier, ingredients);
                return canonical.isEmpty() ? source : canonical;
            }
        }
        return nativeBudget(identifier);
    }

    public static Values materialFunctions(Material material) { return material == null ? SwordFunctionRules.ZERO : MATERIAL_VALUES.getOrDefault(material, SwordFunctionRules.ZERO); }
    public static Values itemFunctions(String identifier) {
        if (identifier == null) return null;
        Values values = ITEM_VALUES.get(identifier);
        Material component = NonManorWeaponComponentRules.material(identifier);
        if (values == null && component != Material.ORIGINAL) return materialFunctions(component);
        return values != null ? values : WeaponEnvironmentMaterials.functions(identifier);
    }

    public static Material material(String identifier) {
        if (identifier == null) return Material.ORIGINAL;
        Material component = NonManorWeaponComponentRules.material(identifier);
        if (component != Material.ORIGINAL) return component;
        Material material = switch (identifier) {
            case "minecraft:string" -> Material.LEATHER;
            case "minecraft:flint", "minecraft:blackstone", "minecraft:cobbled_deepslate", "minecraft:nether_brick", "minecraft:end_stone", "minecraft:purpur_block", "minecraft:sandstone",
                    "minecraft:prismarine_shard" -> Material.STONE;
            case "minecraft:netherite_scrap" -> Material.NETHERITE;
            case "minecraft:prismarine_crystals", "minecraft:nautilus_shell", "cataclysm:crystallized_coral" -> Material.QUARTZ;
            case "minecraft:ender_pearl", "cataclysm:void_core" -> Material.ENDERITE;
            case "minecraft:soul_sand" -> Material.COAL;
            case "cataclysm:koboleton_bone" -> Material.BONE;
            case "cataclysm:lacrima" -> Material.AMETHYST;
            case "cataclysm:essence_of_the_storm" -> Material.BOSS_CAPACITOR;
            case "cataclysm:sandstorm_in_a_bottle" -> Material.ANCIENT_METAL;
            case "create:iron_sheet" -> Material.IRON;
            case "create:large_cogwheel" -> Material.WOOD;
            default -> plank(identifier) ? Material.WOOD
                    : WeaponMaterialCatalogRules.resolve(identifier, List.of());
        };
        return material != Material.ORIGINAL ? material : WeaponEnvironmentMaterials.material(identifier);
    }

    public static String affinity(String identifier) {
        if (identifier == null) return "";
        return switch (identifier) {
            case "minecraft:blaze_rod", "minecraft:nether_brick", "minecraft:netherite_ingot", "minecraft:netherite_scrap",
                    "cataclysm:ignitium_ingot", "cataclysm:infernal_forge", "cataclysm:bulwark_of_the_flame" -> "nether";
            case "minecraft:prismarine_shard", "minecraft:prismarine_crystals", "minecraft:nautilus_shell",
                    "cataclysm:crystallized_coral", "cataclysm:tidal_claws" -> "abyss";
            case "cataclysm:lacrima", "cataclysm:essence_of_the_storm", "cataclysm:astrape" -> "storm";
            case "cataclysm:cursium_ingot", "cataclysm:black_steel_ingot", "minecraft:soul_sand" -> "curse";
            case "cataclysm:ancient_metal_ingot", "cataclysm:koboleton_bone", "cataclysm:sandstorm_in_a_bottle", "minecraft:sandstone" -> "ancient";
            case "cataclysm:void_core", "minecraft:ender_pearl", "minecraft:end_stone", "minecraft:purpur_block" -> "void";
            default -> "";
        };
    }

    public static List<Piece> expandIngredient(String identifier) {
        if (!validId(identifier)) return List.of();
        if ("minecraft:netherite_sword".equals(identifier)) return recipe("minecraft:diamond", 2, "minecraft:stick", 1, "minecraft:netherite_ingot", 1);
        if ("minecraft:netherite_axe".equals(identifier)) return recipe("minecraft:diamond", 3, "minecraft:stick", 2, "minecraft:netherite_ingot", 1);
        if (identifier.equals("minecraft:iron_block")) return List.of(new Piece("minecraft:iron_ingot", 9, Origin.RECIPE));
        Definition definition = DEFINITIONS.get(identifier);
        if (definition == null || !definition.crafted()) return List.of(new Piece(identifier, 1, Origin.RECIPE));
        return definition.budget().stream().filter(piece -> piece.origin() == Origin.RECIPE).toList();
    }

    public static List<Piece> canonicalBudget(String identifier, List<Piece> source) {
        Definition definition = identifier == null ? null : DEFINITIONS.get(identifier);
        List<Piece> canonical = canonicalBudget(definition, source);
        if (!canonical.isEmpty()) return canonical;
        return canonicalBudget(identifier == null ? null : MECHANICAL.get(identifier), source);
    }

    private static List<Piece> canonicalBudget(Definition definition, List<Piece> source) {
        if (definition == null || source == null || source.isEmpty() || source.size() > SwordCavityRules.CELL_COUNT) return List.of();
        for (Piece piece : source) if (!validPiece(piece)) return List.of();
        Map<String, Integer> allowedTheme = new LinkedHashMap<>();
        for (Piece piece : definition.budget()) if (piece.origin() == Origin.THEME) allowedTheme.merge(piece.itemId(), piece.count(), Integer::sum);
        Map<String, Integer> usedTheme = new LinkedHashMap<>();
        for (Piece piece : source) if (piece.origin() == Origin.THEME) {
            int permitted = allowedTheme.getOrDefault(piece.itemId(), 0);
            if (piece.count() > permitted - usedTheme.getOrDefault(piece.itemId(), 0)) return List.of();
            usedTheme.merge(piece.itemId(), piece.count(), Integer::sum);
        }
        Map<String, Long> expected;
        Map<String, Long> actual;
        try {
            expected = recipeCost(definition.budget());
            actual = recipeCost(source);
        } catch (IllegalArgumentException | ArithmeticException exception) {
            return List.of();
        }
        if (!expected.equals(actual)) return List.of();
        if (!definition.crafted() && source.stream().anyMatch(piece -> piece.origin() == Origin.RECIPE)) return List.of();
        if (!definition.crafted() && !allowedTheme.equals(usedTheme)) return List.of();
        List<Piece> canonical = new ArrayList<>(definition.budget());
        for (String reference : List.of("minecraft:oak_planks", "minecraft:cobblestone")) {
            List<String> actualItems = new ArrayList<>();
            for (Piece piece : source) if (piece.origin() == Origin.RECIPE && (reference.equals("minecraft:oak_planks") ? plank(piece.itemId())
                    : List.of("minecraft:cobblestone", "minecraft:blackstone", "minecraft:cobbled_deepslate").contains(piece.itemId()))) {
                if (piece.count() > canonical.size()) return List.of();
                for (int count = 0; count < piece.count(); count++) actualItems.add(piece.itemId());
            }
            int next = 0;
            for (int index = 0; index < canonical.size() && next < actualItems.size(); index++) if (canonical.get(index).origin() == Origin.RECIPE
                    && canonical.get(index).itemId().equals(reference)) canonical.set(index, new Piece(actualItems.get(next++), 1, Origin.RECIPE));
        }
        return List.copyOf(canonical);
    }

    public static Map<String, Long> recipeCost(List<Piece> source) {
        Map<String, Long> result = new java.util.TreeMap<>();
        if (source == null) return Map.of();
        for (Piece piece : source) {
            if (!validPiece(piece)) throw new IllegalArgumentException("Invalid recipe piece");
            if (piece.origin() != Origin.RECIPE) continue;
            addCost(result, piece.itemId(), piece.count(), 0);
        }
        return Map.copyOf(result);
    }

    private static void addCost(Map<String, Long> result, String identifier, long count, int depth) {
        if (depth > 4 || count < 1 || count > Integer.MAX_VALUE) throw new IllegalArgumentException("Invalid recipe expansion");
        if (plank(identifier)) identifier = "minecraft:oak_planks";
        if (identifier.equals("minecraft:blackstone") || identifier.equals("minecraft:cobbled_deepslate")) identifier = "minecraft:cobblestone";
        if (identifier.equals("minecraft:netherite_ingot")) {
            addCost(result, "minecraft:netherite_scrap", count * 4, depth + 1);
            addCost(result, "minecraft:gold_nugget", count * 36, depth + 1);
        } else if (identifier.equals("minecraft:iron_ingot") || identifier.equals("minecraft:gold_ingot")) {
            String nugget = identifier.equals("minecraft:iron_ingot") ? "minecraft:iron_nugget" : "minecraft:gold_nugget";
            result.merge(nugget, count * 9, Math::addExact);
        } else if (identifier.equals("minecraft:iron_block")) {
            addCost(result, "minecraft:iron_ingot", count * 9, depth + 1);
        } else {
            Definition nested = DEFINITIONS.get(identifier);
            if (nested != null && nested.crafted()) {
                for (Piece ingredient : nested.budget()) if (ingredient.origin() == Origin.RECIPE) addCost(result, ingredient.itemId(), count, depth + 1);
            } else result.merge(identifier, count, Math::addExact);
        }
    }

    public static List<Piece> nativeLayout(String identifier, List<Piece> source) {
        if ("minecraft:iron_sword".equals(identifier)) return SwordNativeMaterialRules.ironPrototypeLayout(source);
        List<Piece> canonical = canonicalBudget(identifier, source);
        return canonical.isEmpty() ? SwordNativeMaterialRules.layout(source) : layout(canonical);
    }

    private static List<Piece> layout(List<Piece> pieces) {
        List<Piece> result = new ArrayList<>(Collections.nCopies(SwordCavityRules.CELL_COUNT, EMPTY));
        List<Piece> remaining = new ArrayList<>(pieces);
        Piece grip = take(remaining, "minecraft:stick");
        if (grip == null) grip = take(remaining, "minecraft:blaze_rod");
        if (grip == null) grip = remaining.remove(0);
        result.set(CELL_ORDER[0], grip);
        Piece binding = take(remaining, "minecraft:string");
        if (binding == null) binding = take(remaining, "minecraft:leather");
        if (binding == null) binding = remaining.remove(0);
        result.set(CELL_ORDER[1], binding);
        for (int index = 0; index < remaining.size(); index++) result.set(CELL_ORDER[index + 2], remaining.get(index));
        return List.copyOf(result);
    }

    private static Piece take(List<Piece> pieces, String identifier) {
        for (int index = 0; index < pieces.size(); index++) if (pieces.get(index).itemId().equals(identifier)) return pieces.remove(index);
        return null;
    }

    private static boolean plank(String identifier) {
        return identifier != null && switch (identifier) {
            case "minecraft:oak_planks", "minecraft:spruce_planks", "minecraft:birch_planks", "minecraft:jungle_planks",
                    "minecraft:acacia_planks", "minecraft:dark_oak_planks", "minecraft:mangrove_planks", "minecraft:cherry_planks",
                    "minecraft:bamboo_planks", "minecraft:crimson_planks", "minecraft:warped_planks", "cataclysm:chorus_planks" -> true;
            default -> false;
        };
    }

    private static boolean validPiece(Piece piece) {
        return piece != null && validId(piece.itemId()) && piece.origin() != null
                && (piece.origin() == Origin.SHARED ? piece.count() == 0 : piece.count() > 0);
    }

    private static boolean validId(String identifier) { return identifier != null && ITEM_IDENTIFIER.matcher(identifier).matches(); }

    private static Map<String, Profile> profiles() {
        Map<String, Profile> result = new LinkedHashMap<>();
        for (Definition definition : DEFINITIONS.values()) result.put(definition.weaponId(), createProfile(definition));
        return Map.copyOf(result);
    }

    private static Map<String, Profile> mechanicalProfiles() {
        Map<String, Profile> result = new LinkedHashMap<>();
        for (Definition definition : MECHANICAL.values()) result.put(definition.weaponId(), createProfile(definition));
        return Map.copyOf(result);
    }

    private static Profile createProfile(Definition definition) {
        if (definition.weaponId().equals("minecraft:iron_sword")) return SwordFunctionRules.iron();
        List<Integer> unlock = new ArrayList<>(Collections.nCopies(SwordCavityRules.CELL_COUNT, -1));
        for (int order = 0; order < definition.maximumCells(); order++) {
            unlock.set(CELL_ORDER[order], order < definition.initialCells() ? 0 : (order - definition.initialCells()) / 2 + 1);
        }
        List<Values> weights = new ArrayList<>();
        for (Values base : SwordFunctionRules.iron().cellWeights()) weights.add(new Values(Math.max(0.15, base.handle()),
                Math.max(0.15, base.guard()), Math.max(0.15, base.core()), Math.max(0.15, base.stability()), Math.max(0.15, base.blade())));
        Map<String, Values> items = ITEM_VALUES;
        if (NonManorWeaponComponentRules.kind(definition.weaponId()) != null) {
            Map<String, Values> components = new LinkedHashMap<>(ITEM_VALUES);
            for (Piece component : definition.budget()) {
                components.put(component.itemId(), materialFunctions(NonManorWeaponComponentRules.material(component.itemId())));
            }
            items = Map.copyOf(components);
        }
        Profile seed = new Profile(SwordFunctionRules.ZERO, weights, unlock, MATERIAL_VALUES,
                SwordFunctionRules.iron().cellBaseValues(), items);
        List<Input> inputs = new ArrayList<>();
        for (Piece piece : layout(definition.budget())) inputs.add(new Input(piece.itemId(), material(piece.itemId()), piece.count() > 0));
        Values total = SwordFunctionRules.evaluateItems(seed, inputs, 0).total();
        double fraction = 1.0 - INITIAL_MARGIN;
        Values minimum = new Values(total.handle() * fraction, total.guard() * fraction, total.core() * fraction,
                total.stability() * fraction, total.blade() * fraction);
        for (Function function : Function.values()) if (minimum.value(function) <= 0) throw new IllegalStateException("Missing native weapon function");
        return AdditionalSwordProfiles.enrich(new Profile(minimum, weights, unlock, MATERIAL_VALUES, seed.cellBaseValues(), items), definition);
    }

    private static Map<Material, Values> materialValues() {
        Map<Material, Values> result = new EnumMap<>(Material.class);
        result.putAll(SwordFunctionRules.iron().materials());
        result.put(Material.DIAMOND, new Values(0.5, 1.8, 1.8, 1.6, 2));
        result.put(Material.NETHERITE, new Values(0.65, 2.5, 2.6, 2.5, 2.5));
        result.put(Material.IGNITIUM, new Values(0.6, 2.6, 3.4, 2.7, 3.2));
        result.put(Material.CURSIUM, new Values(0.7, 2.8, 3.3, 2.8, 3.3));
        result.put(Material.WITHERITE, new Values(0.6, 2.2, 2.3, 2.3, 2.6));
        result.put(Material.ANCIENT_METAL, new Values(0.6, 1.6, 1.7, 1.9, 1.8));
        result.put(Material.BLACK_STEEL, new Values(0.55, 1.8, 1.2, 1.8, 1.6));
        result.put(Material.ENDERITE, new Values(0.6, 2.5, 3, 2.2, 2.4));
        result.put(Material.BLAZE, new Values(1, 0.8, 1.8, 0.8, 0.8));
        return Map.copyOf(result);
    }

    private static Map<String, Values> itemValues() {
        Map<String, Values> result = new LinkedHashMap<>(SwordFunctionRules.iron().itemValues());
        for (Material material : Material.values()) for (String identifier : WeaponMaterialCatalogRules.offerings(material)) result.put(identifier, materialFunctions(material));
        for (String wood : List.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "bamboo", "crimson", "warped")) {
            result.put("minecraft:" + wood + "_planks", materialFunctions(Material.WOOD));
        }
        result.put("cataclysm:chorus_planks", materialFunctions(Material.WOOD));
        result.put("minecraft:netherite_scrap", new Values(0.12, 0.65, 0.65, 0.65, 0.65));
        result.put("minecraft:blackstone", materialFunctions(Material.STONE));
        result.put("minecraft:cobbled_deepslate", materialFunctions(Material.STONE));
        result.put("create:iron_sheet", new Values(0.15, 0.95, 0.7, 0.7, 0.85));
        result.put("create:large_cogwheel", new Values(0.8, 0.9, 0.5, 1.1, 0.2));
        result.put("minecraft:nether_brick", new Values(0.1, 1, 0.65, 1.2, 0.6));
        result.put("minecraft:end_stone", new Values(0.1, 1.1, 0.3, 1.5, 0.55));
        result.put("minecraft:purpur_block", new Values(0.1, 1.2, 0.6, 1.2, 0.6));
        result.put("minecraft:sandstone", new Values(0.1, 0.7, 0.25, 1, 0.45));
        result.put("minecraft:prismarine_shard", new Values(0.15, 0.75, 0.65, 0.85, 0.8));
        result.put("minecraft:prismarine_crystals", new Values(0.1, 0.6, 1.5, 0.7, 0.85));
        result.put("minecraft:nautilus_shell", new Values(0.3, 1, 1.3, 1.2, 0.7));
        result.put("minecraft:ender_pearl", new Values(0.15, 0.8, 1.6, 0.6, 0.2));
        result.put("minecraft:soul_sand", new Values(0.05, 0.5, 0.8, 0.7, 0.05));
        result.put("cataclysm:koboleton_bone", new Values(0.9, 1.1, 1, 0.9, 0.7));
        result.put("cataclysm:crystallized_coral", new Values(0.2, 1.4, 1.2, 1.5, 1.6));
        result.put("cataclysm:lacrima", new Values(0.6, 1.5, 2.2, 1.3, 1.7));
        result.put("cataclysm:essence_of_the_storm", new Values(0.3, 1.5, 3, 1.4, 1.5));
        result.put("cataclysm:void_core", new Values(0.15, 1.2, 4, 0.9, 0.15));
        result.put("cataclysm:sandstorm_in_a_bottle", new Values(0.15, 0.8, 3.5, 1, 0.1));
        result.put("cataclysm:gauntlet_of_guard", new Values(1.2, 3.5, 4, 3, 2.8));
        result.put("cataclysm:bulwark_of_the_flame", new Values(0.6, 3.4, 2.4, 3.5, 0.3));
        result.put("cataclysm:infernal_forge", new Values(1.4, 3, 4, 3, 3.2));
        result.put("minecraft:chain", new Values(0.5, 1.2, 0.2, 1.2, 0.4));
        for (String identifier : List.of("irons_spellbooks:weapon_parts", "irons_spellbooks:arcane_ingot",
                "irons_spellbooks:mithril_ingot", "armamentarium:greatalloy_ingot")) {
            result.put(identifier, new Values(0.6, 1.6, 1.5, 1.6, 1.7));
        }
        for (String identifier : List.of("irons_spellbooks:arcane_essence", "irons_spellbooks:ice_venom_vial", "legendary_monsters:chorus_crystal",
                "legendary_monsters:infected_chorus_fruit", "legendary_monsters:void_gem")) {
            result.put(identifier, new Values(0.2, 0.6, 1.5, 0.8, 0.7));
        }
        for (String identifier : List.of("irons_spellbooks:pyrium_ingot", "legendary_monsters:enderitium_ingot")) {
            result.put(identifier, new Values(0.6, 2.2, 2.6, 2.2, 2.5));
        }
        result.put("minecraft:ice", new Values(0.1, 0.6, 1, 0.8, 0.2));
        for (String identifier : List.of("legendary_monsters:knights_sword_blade_half", "legendary_monsters:metal_debris",
                "legendary_monsters:withered_bone", "legendary_monsters:withered_horn", "armamentarium:greatalloy_nugget")) {
            result.put(identifier, new Values(0.5, 1, 0.5, 1, 1));
        }
        return Map.copyOf(result);
    }

    private static Map<String, Definition> definitionsInternal() {
        Map<String, Definition> result = new LinkedHashMap<>();
        add(result, "minecraft:wooden_sword", Form.SWORD, "minecraft:wooden_sword", 7, recipe("minecraft:oak_planks", 2, "minecraft:stick", 1), "minecraft:string", "minecraft:flint");
        add(result, "minecraft:stone_sword", Form.SWORD, "minecraft:stone_sword", 8, recipe("minecraft:cobblestone", 2, "minecraft:stick", 1), "minecraft:string", "minecraft:flint");
        result.put("minecraft:iron_sword", new Definition("minecraft:iron_sword", Form.SWORD, "minecraft:iron_sword", 9, SwordNativeMaterialRules.ironPrototype()));
        add(result, "minecraft:golden_sword", Form.SWORD, "minecraft:golden_sword", 9, recipe("minecraft:gold_ingot", 2, "minecraft:stick", 1), "minecraft:string", "minecraft:cobblestone", "minecraft:flint");
        add(result, "minecraft:diamond_sword", Form.SWORD, "minecraft:diamond_sword", 11, recipe("minecraft:diamond", 2, "minecraft:stick", 1), "minecraft:string", "minecraft:cobblestone", "minecraft:flint");
        add(result, "minecraft:netherite_sword", Form.SWORD, "minecraft:netherite_sword_smithing", 15,
                recipe("minecraft:diamond", 2, "minecraft:stick", 1, "minecraft:netherite_scrap", 4, "minecraft:gold_ingot", 4), "minecraft:string", "minecraft:obsidian", "minecraft:blaze_rod");
        add(result, "minecraft:wooden_axe", Form.AXE, "minecraft:wooden_axe", 8, recipe("minecraft:oak_planks", 3, "minecraft:stick", 2), "minecraft:string", "minecraft:flint");
        add(result, "minecraft:stone_axe", Form.AXE, "minecraft:stone_axe", 9, recipe("minecraft:cobblestone", 3, "minecraft:stick", 2), "minecraft:string", "minecraft:flint");
        add(result, "minecraft:iron_axe", Form.AXE, "minecraft:iron_axe", 10, recipe("minecraft:iron_ingot", 3, "minecraft:stick", 2), "minecraft:string", "minecraft:flint");
        add(result, "minecraft:golden_axe", Form.AXE, "minecraft:golden_axe", 10, recipe("minecraft:gold_ingot", 3, "minecraft:stick", 2), "minecraft:string", "minecraft:flint");
        add(result, "minecraft:diamond_axe", Form.AXE, "minecraft:diamond_axe", 12, recipe("minecraft:diamond", 3, "minecraft:stick", 2), "minecraft:string", "minecraft:flint");
        add(result, "minecraft:netherite_axe", Form.AXE, "minecraft:netherite_axe_smithing", 17,
                recipe("minecraft:diamond", 3, "minecraft:stick", 2, "minecraft:netherite_scrap", 4, "minecraft:gold_ingot", 4), "minecraft:string", "minecraft:obsidian", "minecraft:blaze_rod");
        add(result, "minecraft:trident", Form.SPEAR, "", 13, List.of(), "minecraft:prismarine_shard", "minecraft:prismarine_shard",
                "minecraft:prismarine_crystals", "minecraft:nautilus_shell", "minecraft:stick", "minecraft:stick", "minecraft:string");
        add(result, "cataclysm:black_steel_sword", Form.SWORD, "cataclysm:black_steel_sword", 11, recipe("cataclysm:black_steel_ingot", 2, "minecraft:stick", 1), "minecraft:string", "minecraft:cobblestone", "minecraft:flint");
        add(result, "cataclysm:black_steel_axe", Form.AXE, "cataclysm:black_steel_axe", 12, recipe("cataclysm:black_steel_ingot", 3, "minecraft:stick", 2), "minecraft:string", "minecraft:flint");
        add(result, "cataclysm:athame", Form.DAGGER, "", 9, List.of(), "minecraft:iron_ingot", "minecraft:bone", "minecraft:stick", "minecraft:string", "minecraft:prismarine_shard", "minecraft:flint");
        add(result, "cataclysm:khopesh", Form.SWORD, "", 11, List.of(), "cataclysm:ancient_metal_ingot", "cataclysm:ancient_metal_ingot", "cataclysm:koboleton_bone", "minecraft:stick", "minecraft:string", "minecraft:sandstone");
        add(result, "cataclysm:coral_spear", Form.SPEAR, "", 11, List.of(), "cataclysm:crystallized_coral", "cataclysm:crystallized_coral", "minecraft:stick", "minecraft:stick", "minecraft:string", "minecraft:prismarine_shard");
        add(result, "cataclysm:coral_bardiche", Form.AXE, "", 13, List.of(), "cataclysm:crystallized_coral", "cataclysm:crystallized_coral", "cataclysm:crystallized_coral", "minecraft:stick", "minecraft:stick", "minecraft:string", "minecraft:prismarine_crystals");
        add(result, "cataclysm:ancient_spear", Form.SPEAR, "cataclysm:ancient_spear", 13, recipe("cataclysm:ancient_metal_ingot", 4), "cataclysm:koboleton_bone", "minecraft:string", "minecraft:sandstone");
        add(result, "cataclysm:the_annihilator", Form.AXE, "cataclysm:the_annihilator", 15, recipe("cataclysm:cursium_ingot", 2, "cataclysm:black_steel_ingot", 1), "minecraft:string", "minecraft:bone", "minecraft:soul_sand");
        add(result, "cataclysm:soul_render", Form.SCYTHE, "cataclysm:soul_render", 15, recipe("cataclysm:cursium_ingot", 3, "cataclysm:black_steel_ingot", 2), "minecraft:string", "minecraft:bone", "minecraft:soul_sand");
        add(result, "cataclysm:the_incinerator", Form.SWORD, "cataclysm:the_incinerator", 19,
                recipe("minecraft:diamond", 2, "minecraft:stick", 1, "minecraft:netherite_ingot", 1, "minecraft:blaze_rod", 4, "cataclysm:ignitium_ingot", 2), "minecraft:string", "minecraft:obsidian", "minecraft:nether_brick");
        add(result, "cataclysm:the_immolator", Form.SWORD, "cataclysm:weapon_infusion/the_immolator", 17,
                recipe("cataclysm:cursium_ingot", 2, "cataclysm:black_steel_ingot", 1, "cataclysm:ignitium_ingot", 1), "minecraft:string", "minecraft:bone", "minecraft:nether_brick");
        add(result, "cataclysm:astrape", Form.SPEAR, "cataclysm:astrape", 15, recipe("cataclysm:lacrima", 3, "cataclysm:essence_of_the_storm", 2), "minecraft:string", "minecraft:quartz");
        add(result, "cataclysm:ceraunus", Form.AXE, "cataclysm:ceraunus", 17, recipe("cataclysm:lacrima", 3, "cataclysm:essence_of_the_storm", 2), "minecraft:string", "minecraft:quartz", "minecraft:iron_ingot");
        add(result, "cataclysm:meat_shredder", Form.SAW, "cataclysm:meat_shredder", 17,
                recipe("minecraft:iron_ingot", 9, "minecraft:redstone", 2, "cataclysm:witherite_ingot", 2), "minecraft:string", "minecraft:leather");
        add(result, "cataclysm:gauntlet_of_guard", Form.GAUNTLET, "", 15, List.of(), "minecraft:obsidian", "minecraft:obsidian", "minecraft:ender_pearl", "minecraft:end_stone", "minecraft:leather", "minecraft:string");
        add(result, "cataclysm:gauntlet_of_bulwark", Form.GAUNTLET, "cataclysm:weapon_infusion/gauntlet_of_bulwark", 17,
                recipe("cataclysm:gauntlet_of_guard", 1, "cataclysm:bulwark_of_the_flame", 1), "minecraft:obsidian", "minecraft:blaze_rod", "minecraft:leather", "minecraft:string");
        add(result, "cataclysm:gauntlet_of_maelstrom", Form.GAUNTLET, "cataclysm:weapon_infusion/gauntlet_of_maelstrom", 17,
                recipe("cataclysm:gauntlet_of_guard", 1, "cataclysm:sandstorm_in_a_bottle", 1), "minecraft:end_stone", "minecraft:sandstone", "minecraft:leather", "minecraft:string");
        add(result, "cataclysm:infernal_forge", Form.HAMMER, "", 15, List.of(), "minecraft:netherite_scrap", "minecraft:netherite_scrap", "minecraft:blaze_rod", "minecraft:obsidian", "minecraft:nether_brick", "minecraft:string");
        add(result, "cataclysm:void_forge", Form.HAMMER, "cataclysm:weapon_infusion/void_forge", 17,
                recipe("cataclysm:infernal_forge", 1, "cataclysm:void_core", 1), "minecraft:obsidian", "minecraft:end_stone", "minecraft:ender_pearl", "minecraft:string");
        add(result, "cataclysm:brontes", Form.HAMMER, "cataclysm:weapon_infusion/brontes", 17,
                recipe("cataclysm:infernal_forge", 1, "cataclysm:lacrima", 3, "cataclysm:essence_of_the_storm", 2), "minecraft:quartz", "minecraft:string");
        add(result, "cataclysm:tidal_claws", Form.GAUNTLET, "", 15, List.of(), "minecraft:prismarine_crystals", "minecraft:prismarine_crystals", "minecraft:prismarine_shard", "minecraft:nautilus_shell", "minecraft:leather", "minecraft:string");
        add(result, "irons_spellbooks:amethyst_rapier", Form.SWORD, "irons_spellbooks:amethyst_rapier", 11,
                recipe("minecraft:amethyst_shard", 2, "minecraft:chain", 2, "irons_spellbooks:weapon_parts", 1),
                "irons_spellbooks:arcane_essence", "minecraft:string", "minecraft:quartz");
        add(result, "irons_spellbooks:spellbreaker", Form.SWORD, "irons_spellbooks:spellbreaker", 13,
                recipe("irons_spellbooks:mithril_ingot", 2, "minecraft:diamond", 2, "irons_spellbooks:weapon_parts", 1),
                "irons_spellbooks:arcane_essence", "minecraft:string", "minecraft:quartz");
        add(result, "irons_spellbooks:magehunter", Form.SWORD, "", 13, List.of(),
                "minecraft:iron_ingot", "minecraft:iron_ingot", "irons_spellbooks:arcane_essence", "minecraft:quartz", "minecraft:leather", "minecraft:string");
        add(result, "irons_spellbooks:claymore", Form.SWORD, "", 13, List.of(),
                "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:stick", "minecraft:leather", "minecraft:string");
        add(result, "irons_spellbooks:keeper_flamberge", Form.SWORD, "", 13, List.of(),
                "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:bone", "minecraft:coal", "minecraft:leather", "minecraft:string");
        add(result, "irons_spellbooks:legionnaire_flamberge", Form.SWORD, "irons_spellbooks:legionnaire_flamberge_repair", 17,
                recipe("irons_spellbooks:keeper_flamberge", 1, "irons_spellbooks:pyrium_ingot", 1),
                "minecraft:blaze_rod", "minecraft:obsidian", "minecraft:leather", "minecraft:string");
        add(result, "irons_spellbooks:misery", Form.SWORD, "", 15, List.of(),
                "minecraft:iron_ingot", "minecraft:bone", "minecraft:soul_sand", "minecraft:obsidian", "minecraft:leather", "minecraft:string");
        add(result, "irons_spellbooks:decrepit_scythe", Form.SCYTHE, "", 13, List.of(),
                "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:bone", "minecraft:stick", "minecraft:leather", "minecraft:string");
        add(result, "irons_spellbooks:hellrazor", Form.SCYTHE, "irons_spellbooks:decrepit_scythe_repair", 17,
                recipe("irons_spellbooks:decrepit_scythe", 1, "irons_spellbooks:pyrium_ingot", 1),
                "minecraft:blaze_rod", "minecraft:obsidian", "minecraft:leather", "minecraft:string");
        add(result, "irons_spellbooks:boreal_blade", Form.SWORD, "irons_spellbooks:boreal_blade", 15,
                recipe("irons_spellbooks:ice_venom_vial", 2, "irons_spellbooks:mithril_ingot", 1, "irons_spellbooks:weapon_parts", 1, "minecraft:ice", 1),
                "minecraft:prismarine_crystals", "minecraft:leather", "minecraft:string");
        add(result, "legendary_monsters:knights_sword", Form.SWORD, "legendary_monsters:knights_sword", 11,
                recipe("legendary_monsters:knights_sword_blade_half", 2, "minecraft:stick", 1), "minecraft:string", "minecraft:cobblestone", "minecraft:flint");
        add(result, "legendary_monsters:shattered_greatsword", Form.SWORD, "legendary_monsters:shattered_greatsword", 13,
                recipe("legendary_monsters:metal_debris", 2, "minecraft:iron_nugget", 2, "minecraft:stick", 1), "minecraft:string", "minecraft:leather", "minecraft:flint");
        add(result, "legendary_monsters:chorus_blade", Form.SWORD, "legendary_monsters:chorus_blade", 15,
                recipe("legendary_monsters:chorus_crystal", 1, "legendary_monsters:infected_chorus_fruit", 2, "minecraft:iron_sword", 1),
                "minecraft:string", "minecraft:end_stone", "minecraft:ender_pearl");
        add(result, "legendary_monsters:void_sword", Form.SWORD, "legendary_monsters:void_sword", 15,
                recipe("legendary_monsters:void_gem", 2, "legendary_monsters:withered_bone", 1), "minecraft:string", "minecraft:end_stone", "minecraft:obsidian");
        add(result, "legendary_monsters:enderitium_sword", Form.SWORD, "legendary_monsters:enderitium_sword", 15,
                recipe("minecraft:diamond_sword", 1, "legendary_monsters:enderitium_ingot", 1), "minecraft:string", "minecraft:end_stone", "minecraft:ender_pearl");
        add(result, "legendary_monsters:soul_great_sword", Form.SWORD, "", 15, List.of(),
                "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:obsidian", "minecraft:soul_sand", "minecraft:leather", "minecraft:string");
        add(result, "armamentarium:greatalloy_sword", Form.SWORD, "armamentarium:greatalloy_sword", 13,
                recipe("armamentarium:greatalloy_nugget", 1, "armamentarium:greatalloy_ingot", 1, "minecraft:stick", 1),
                "minecraft:string", "minecraft:cobblestone", "minecraft:flint");
        add(result, "armamentarium:severance_shotel", Form.SWORD, "armamentarium:severance_shotel_recipe", 13,
                recipe("minecraft:iron_nugget", 1, "minecraft:iron_ingot", 2, "armamentarium:greatalloy_nugget", 1, "minecraft:flint", 1),
                "minecraft:string", "minecraft:leather", "minecraft:quartz");
        add(result, "armamentarium:severance_scythe", Form.SCYTHE, "armamentarium:severance_scythe", 17,
                recipe("minecraft:quartz", 1, "minecraft:iron_ingot", 1, "minecraft:ghast_tear", 1, "minecraft:phantom_membrane", 1,
                        "armamentarium:severance_shotel", 1, "armamentarium:greatalloy_ingot", 1), "minecraft:string", "minecraft:leather", "minecraft:bone");
        add(result, "rajswordmod:sentinel_sword", Form.SWORD, "", 15, List.of(),
                "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:quartz", "minecraft:obsidian", "minecraft:leather", "minecraft:string");
        for (String identifier : NonManorWeaponComponentRules.weaponsById().keySet()) {
            if (identifier.equals("ftboss:frost_queen_staff") || identifier.equals("ftboss:barrel_crossbow")) continue;
            Form form = identifier.equals("ftboss:tide_trident") ? Form.SPEAR : Form.SWORD;
            result.put(identifier, new Definition(identifier, form, "", NonManorWeaponComponentRules.INITIAL_MELEE_CELLS,
                    NonManorWeaponComponentRules.budget(identifier)));
        }
        AdditionalSwordProfiles.addTo(result);
        return Map.copyOf(result);
    }

    private static Map<String, Definition> mechanicalDefinitions() {
        Map<String, Definition> result = new LinkedHashMap<>();
        add(result, "cataclysm:the_incinerator", Form.SWORD, "cataclysm:the_incinerator", 19,
                recipe("minecraft:diamond", 2, "minecraft:stick", 1, "minecraft:netherite_ingot", 1, "minecraft:blaze_rod", 8, "cataclysm:ignitium_ingot", 3),
                "minecraft:string", "minecraft:obsidian", "minecraft:nether_brick");
        add(result, "cataclysm:meat_shredder", Form.SAW, "cataclysm:meat_shredder", 17,
                recipe("create:iron_sheet", 8, "minecraft:redstone", 4, "cataclysm:witherite_ingot", 2, "create:large_cogwheel", 1),
                "minecraft:string", "minecraft:leather");
        return Map.copyOf(result);
    }

    private static void add(Map<String, Definition> definitions, String identifier, Form form, String recipeId,
                            int initialCells, List<Piece> ingredients, String... environment) {
        if (environment.length > 7) throw new IllegalArgumentException("Environment reference budget exceeds limit");
        List<Piece> pieces = new ArrayList<>(ingredients);
        for (String item : environment) pieces.add(new Piece(item, 1, Origin.THEME));
        if (definitions.putIfAbsent(identifier, new Definition(identifier, form, recipeId, initialCells, pieces)) != null) {
            throw new IllegalArgumentException("Duplicate compatibility weapon");
        }
    }

    private static List<Piece> recipe(Object... pairs) {
        if (pairs.length % 2 != 0) throw new IllegalArgumentException("Recipe requires identifier/count pairs");
        List<Piece> result = new ArrayList<>();
        for (int index = 0; index < pairs.length; index += 2) {
            String identifier = (String) pairs[index];
            int count = (Integer) pairs[index + 1];
            if (count < 1 || count > SwordCavityRules.CELL_COUNT) throw new IllegalArgumentException("Invalid recipe count");
            for (int piece = 0; piece < count; piece++) result.add(new Piece(identifier, 1, Origin.RECIPE));
        }
        return List.copyOf(result);
    }
}
