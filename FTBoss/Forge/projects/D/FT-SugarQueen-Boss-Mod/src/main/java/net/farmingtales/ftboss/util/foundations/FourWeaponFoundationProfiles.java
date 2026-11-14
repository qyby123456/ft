package net.farmingtales.ftboss.util.foundations;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.NonManorWeaponComponentRules;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Cell;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Values;

/** 原版弓弩、六斧、三叉戟及实际 Iron 六杖样例；2026-10-14 配方与有限环境参考零退款。 */
public final class FourWeaponFoundationProfiles {
    private static final int[] BOW_LAYOUT = {4, 12, 21, 5, 14, 23, 3, 11, 20, 6, 15, 24, 2, 10, 19, 7, 16, 25, 1, 9, 18, 8, 17, 26, 0, 13, 22};
    private static final int[] CROSSBOW_LAYOUT = {3, 4, 5, 13, 14, 22, 12, 2, 6, 11, 15, 21, 23, 1, 7, 10, 16, 20, 24, 0, 8, 9, 17, 18, 25, 26, 19};
    private static final int[] STAFF_LAYOUT = {4, 13, 22, 3, 5, 12, 14, 21, 23, 2, 6, 11, 15, 20, 24, 1, 7, 10, 16, 19, 25, 0, 8, 9, 17, 18, 26};
    private static final int[] AXE_LAYOUT = {3, 4, 12, 13, 22, 5, 14, 21, 23, 2, 11, 20, 6, 15, 24, 1, 10, 19, 7, 16, 25, 0, 9, 18, 8, 17, 26};
    private static final int[] TRIDENT_LAYOUT = {3, 13, 4, 5, 12, 14, 22, 2, 6, 11, 15, 21, 23, 1, 7, 10, 16, 20, 24, 0, 8, 9, 17, 18, 25, 26, 19};

    private FourWeaponFoundationProfiles() {}

    public static Map<String, Profile> examples() {
        Map<String, Profile> profiles = new LinkedHashMap<>();
        put(profiles, create("minecraft:bow", "bow", 6,
                budget("minecraft:stick", 3, "minecraft:string", 3), Map.of(), "minecraft:bow"));
        put(profiles, create("minecraft:crossbow", "crossbow", 7,
                budget("minecraft:stick", 3, "minecraft:string", 2, "minecraft:iron_ingot", 1, "minecraft:tripwire_hook", 1),
                Map.of(), "minecraft:crossbow"));
        put(profiles, create("irons_spellbooks:graybeard_staff", "staff", 3,
                budget("minecraft:wooden_shovel", 1, "minecraft:iron_ingot", 1, "irons_spellbooks:arcane_essence", 1),
                Map.of(), "irons_spellbooks:graybeard_staff"));
        put(profiles, create("irons_spellbooks:artificer_cane", "staff", 6,
                budget("minecraft:crimson_planks", 3, "minecraft:gold_ingot", 2, "minecraft:amethyst_shard", 1),
                Map.of(), "irons_spellbooks:artificer_cane"));
        put(profiles, create("irons_spellbooks:ice_staff", "staff", 2,
                budget("irons_spellbooks:frosted_helve", 1, "irons_spellbooks:permafrost_shard", 1), Map.of(), "irons_spellbooks:ice_staff"));
        put(profiles, create("irons_spellbooks:pyrium_staff", "staff", 4,
                budget("irons_spellbooks:pyrium_ingot", 2, "irons_spellbooks:magic_cloth", 1, "minecraft:netherite_ingot", 1),
                Map.of(), "irons_spellbooks:pyrium_staff"));
        put(profiles, create("irons_spellbooks:lightning_rod", "staff", 2, Map.of(),
                budget("minecraft:copper_ingot", 1, "minecraft:amethyst_shard", 1), "NONE:audited-jar-no-recipe;environment-only"));
        put(profiles, create("irons_spellbooks:blood_staff", "staff", 2, Map.of(),
                budget("minecraft:bone", 1, "minecraft:redstone", 1), "NONE:audited-jar-no-recipe;environment-only"));
        String[] axeIds = {"wooden", "stone", "iron", "golden", "diamond", "netherite"};
        String[] ingredients = {"minecraft:oak_planks", "minecraft:cobblestone", "minecraft:iron_ingot", "minecraft:gold_ingot",
                "minecraft:diamond", "minecraft:diamond"};
        int[] capacities = {8, 9, 10, 10, 12, 17};
        for (int index = 0; index < axeIds.length; index++) {
            Map<String, Integer> recipe = new LinkedHashMap<>(budget(ingredients[index], 3, "minecraft:stick", 2));
            if (index == 5) recipe.put("minecraft:netherite_ingot", 1);
            String id = "minecraft:" + axeIds[index] + "_axe";
            put(profiles, create(id, "axe", capacities[index], recipe, Map.of(), index == 5
                    ? "minecraft:diamond_axe+minecraft:netherite_axe_smithing;template-excluded-from-structure" : id));
        }
        put(profiles, create("minecraft:trident", "trident", 13, Map.of(),
                budget("minecraft:prismarine_shard", 1, "minecraft:stick", 1), "NONE:vanilla-no-recipe;environment-only"));
        put(profiles, upgradedTrident("tridents_n_stuff:frenzied_trident",
                budget("minecraft:trident", 1, "minecraft:gold_ingot", 4, "minecraft:redstone_block", 4),
                Map.of("minecraft:redstone_block", new Values(0.9, 1.1, 0.65, 1.7, 1.7))));
        put(profiles, upgradedTrident("tridents_n_stuff:farshot_trident",
                budget("minecraft:trident", 1, "minecraft:gold_ingot", 4, "minecraft:phantom_membrane", 2, "minecraft:feather", 2),
                Map.of("minecraft:phantom_membrane", new Values(0.6, 0.6, 1.6, 1.4, 1.3),
                        "minecraft:feather", new Values(0.4, 0.4, 1.7, 1.2, 0.8))));
        put(profiles, upgradedTrident("tridents_n_stuff:ancient_trident",
                budget("minecraft:trident", 1, "minecraft:gold_ingot", 4, "minecraft:netherite_scrap", 4),
                Map.of("minecraft:netherite_scrap", materialValues("trident", Material.NETHERITE))));
        put(profiles, upgradedTrident("tridents_n_stuff:holy_trident",
                budget("minecraft:trident", 1, "minecraft:gold_ingot", 4, "minecraft:golden_apple", 4),
                Map.of("minecraft:golden_apple", new Values(0.8, 0.9, 0.7, 1.1, 1.6))));
        put(profiles, upgradedTrident("tridents_n_stuff:oceanic_trident",
                budget("minecraft:trident", 1, "minecraft:gold_ingot", 4, "minecraft:nautilus_shell", 2, "minecraft:kelp", 2),
                Map.of("minecraft:nautilus_shell", new Values(1.1, 1.2, 0.8, 1.8, 1.6),
                        "minecraft:kelp", new Values(0.5, 0.4, 1.0, 1.6, 0.8))));
        put(profiles, create("tide:starlight_bow", "bow", 8,
                budget("minecraft:bow", 1, "tide:shooting_starfish", 1), Map.of(), "tide:starlight_bow",
                Map.of("minecraft:bow", new Values(1.1, 1.6, 1.2, 1.0, 1.1),
                        "tide:shooting_starfish", new Values(0.8, 1.0, 1.7, 1.2, 0.9))));
        put(profiles, create("irons_spellbooks:autoloader_crossbow", "crossbow", 9, Map.of(),
                budget("minecraft:iron_ingot", 1, "minecraft:redstone", 1, "minecraft:stick", 1),
                "NONE:installed-3.16.3-no-output-recipe;ItemRegistry-registered-source", Map.of()));
        put(profiles, bossProfile("ftboss:barrel_crossbow", "crossbow", 14));
        put(profiles, create("armamentarium:ender_maiden_shortbow", "bow", 10, Map.of(),
                budget("minecraft:ender_pearl", 1, "minecraft:stick", 1, "minecraft:string", 1),
                "NONE:installed-no-output-recipe;farmingtales:advancements/culinary_master-reward",
                Map.of("minecraft:ender_pearl", new Values(0.9, 1.0, 1.7, 1.2, 0.7))));
        put(profiles, create("cataclysm:cursed_bow", "bow", 12,
                budget("cataclysm:cursium_ingot", 2, "cataclysm:black_steel_ingot", 1, "minecraft:string", 3),
                Map.of(), "cataclysm:cursed_bow", Map.of()));
        put(profiles, create("ftboss:qizai_prayer_staff", "staff", 14, Map.of(),
                budget("minecraft:amethyst_shard", 1, "minecraft:gold_ingot", 1, "minecraft:stick", 1),
                "NONE:5C5B-no-output-recipe;FTCreativeTabs-registered-source", Map.of()));
        put(profiles, bossProfile("ftboss:frost_queen_staff", "staff", 14));
        put(profiles, create("armamentarium:mariana_scepter", "staff", 12, Map.of(),
                budget("minecraft:prismarine_shard", 1, "minecraft:nautilus_shell", 1, "minecraft:stick", 1),
                "NONE:installed-no-output-recipe;farmingtales:advancements/weapons/mariana_scepter-reward",
                Map.of("minecraft:nautilus_shell", new Values(1.5, 1.3, 1.1, 1.4, 0.8))));
        put(profiles, create("irons_spellbooks:staff_of_the_nines", "staff", 8, Map.of(),
                budget("minecraft:oak_planks", 1, "minecraft:iron_ingot", 1),
                "NONE:installed-3.16.3-no-output-recipe;ItemRegistry-registered-source", Map.of()));
        put(profiles, create("legendary_monsters:wand_of_clouds", "staff", 9,
                budget("legendary_monsters:air_rune", 1, "legendary_monsters:cloud_rod", 1, "minecraft:iron_ingot", 1),
                Map.of(), "legendary_monsters:wand_of_clouds",
                Map.of("legendary_monsters:air_rune", new Values(1.7, 1.5, 0.7, 1.4, 0.6),
                        "legendary_monsters:cloud_rod", new Values(0.9, 1.1, 1.2, 1.4, 1.6))));
        put(profiles, create("cataclysm:wrath_of_the_desert", "bow", 14,
                budget("cataclysm:cursed_bow", 1, "cataclysm:sandstorm_in_a_bottle", 1), Map.of(),
                "cataclysm:weapon_infusion/wrath_of_the_desert",
                Map.of("cataclysm:cursed_bow", new Values(1.4, 1.7, 1.3, 1.0, 1.6),
                        "cataclysm:sandstorm_in_a_bottle", new Values(0.9, 0.7, 1.6, 1.4, 1.1))));
        put(profiles, create("cataclysm:black_steel_axe", "axe", 12,
                budget("cataclysm:black_steel_ingot", 3, "minecraft:stick", 2), Map.of(),
                "cataclysm:black_steel_axe", Map.of()));
        MaterialCrossbowProfiles.addTo(profiles);
        MaterialBowProfiles.addTo(profiles);
        MaterialStaffProfiles.addTo(profiles);
        ManorTridentProfiles.addTo(profiles);
        AdditionalFoundationProfiles.addTo(profiles);
        return java.util.Collections.unmodifiableMap(profiles);
    }

    private static void put(Map<String, Profile> profiles, Profile profile) { profiles.put(profile.id(), profile); }

    private static Profile upgradedTrident(String id, Map<String, Integer> recipe, Map<String, Values> itemValues) {
        Map<String, Values> items = new HashMap<>(itemValues);
        items.put("minecraft:trident", new Values(1.2, 1.4, 0.8, 1.6, 1.2));
        return create(id, "trident", 15, recipe, budget("minecraft:prismarine_shard", 1, "minecraft:stick", 1), id, items);
    }

    private static Map<String, Integer> budget(Object... entries) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) result.put((String) entries[index], (Integer) entries[index + 1]);
        return java.util.Collections.unmodifiableMap(result);
    }

    public static List<String> axes(String family) {
        return switch (family) {
            case "bow" -> List.of("limb_flex", "string_tension", "arrow_guidance", "draw_recovery", "frame_bracing");
            case "crossbow" -> List.of("prod_flex", "string_tension", "bolt_guidance", "trigger_release", "stock_bracing");
            case "staff" -> List.of("spell_conduction", "focus_coherence", "mana_insulation", "channel_stability", "shaft_support");
            case "axe" -> List.of("haft_leverage", "head_binding", "impact_transfer", "swing_balance", "edge_integrity");
            case "trident" -> List.of("shaft_thrust", "tine_integrity", "launch_balance", "water_tracking", "return_binding");
            default -> throw new IllegalArgumentException("Unknown family");
        };
    }

    public static Values materialValues(String family, Material material) {
        String name = material.name();
        Values physical = switch (name) {
            case "WOOD", "WOOD_FENCE", "BAMBOO", "BAMBOO_FENCE" -> new Values(0.8, 1.25, 0.25, 0.45, 0.55);
            case "LEATHER", "CHAIN", "SLIME", "HONEYCOMB" -> new Values(0.55, 1.45, 0.35, 0.6, 0.35);
            case "IRON", "IRON_BARS", "COPPER", "GOLD", "LIGHTNING_ROD", "ANCIENT_METAL" -> new Values(1.2, 0.55, 1.3, 1.25, 1.05);
            case "STONE", "STONE_WALL", "BRICK_WALL", "BLACKSTONE_WALL", "DEEPSLATE_WALL", "OBSIDIAN", "NETHER_FENCE" ->
                    new Values(1.35, 0.3, 0.35, 1.45, 1.1);
            case "DIAMOND", "NETHERITE", "IGNITIUM", "CURSIUM", "BLACK_STEEL", "WITHERITE" -> new Values(1.65, 0.45, 1.0, 1.4, 1.65);
            case "AMETHYST", "QUARTZ", "LAPIS", "REDSTONE", "EMERALD", "GLOWSTONE", "ECHO_SHARD", "END_ROD", "ENDERITE", "MITHRIL" ->
                    new Values(0.9, 0.65, 1.7, 0.7, 1.1);
            case "PRISMARINE", "BOSS_TIDE" -> new Values(1.1, 0.7, 1.3, 0.9, 1.0);
            case "ORIGINAL" -> throw new IllegalArgumentException("ORIGINAL has no universal foundation contribution");
            default -> new Values(1.0, 0.8, 1.0, 0.9, 1.0);
        };
        return switch (family) {
            case "bow" -> new Values(physical.second(), physical.second(), physical.first(),
                    (physical.first() + physical.second()) * 0.5, physical.first());
            case "crossbow" -> new Values((physical.first() + physical.second()) * 0.5, physical.second(),
                    physical.first(), physical.third(), physical.first());
            case "staff" -> new Values(physical.third(), (physical.third() + physical.fifth()) * 0.5,
                    physical.first(), (physical.first() + physical.third()) * 0.5, physical.first());
            case "axe" -> new Values(physical.first(), physical.second(), physical.fourth(),
                    (physical.first() + physical.second()) * 0.5, physical.fifth());
            case "trident" -> new Values(physical.first(), physical.fifth(), physical.second(),
                    (physical.second() + physical.third()) * 0.5, physical.third());
            default -> throw new IllegalArgumentException("Unknown family");
        };
    }

    public static Profile create(String id, String family, int initialCapacity, Map<String, Integer> recipe,
                                 Map<String, Integer> environment, String evidence) {
        return create(id, family, initialCapacity, recipe, environment, evidence, Map.of());
    }

    public static Profile create(String id, String family, int initialCapacity, Map<String, Integer> recipe,
                                 Map<String, Integer> environment, String evidence, Map<String, Values> itemValues) {
        int[] layout = switch (family) {
            case "bow" -> BOW_LAYOUT;
            case "crossbow" -> CROSSBOW_LAYOUT;
            case "staff" -> STAFF_LAYOUT;
            case "axe" -> AXE_LAYOUT;
            case "trident" -> TRIDENT_LAYOUT;
            default -> throw new IllegalArgumentException("Unknown family");
        };
        if (initialCapacity < 1 || initialCapacity + 8 > FourWeaponFoundationRules.CELLS) throw new IllegalArgumentException("Invalid capacity");
        List<Values> weights = new ArrayList<>(java.util.Collections.nCopies(27, FourWeaponFoundationRules.ZERO));
        List<Values> bases = new ArrayList<>(java.util.Collections.nCopies(27, FourWeaponFoundationRules.ZERO));
        List<Integer> unlocks = new ArrayList<>(java.util.Collections.nCopies(27, -1));
        for (int order = 0; order < initialCapacity + 8; order++) {
            int cell = layout[order];
            int priority = order % 5;
            double[] factors = {0.25, 0.25, 0.25, 0.25, 0.25};
            factors[priority] = 1;
            weights.set(cell, new Values(factors[0], factors[1], factors[2], factors[3], factors[4]));
            bases.set(cell, new Values(0.005, 0.005, 0.005, 0.005, 0.005));
            unlocks.set(cell, order < initialCapacity ? 0 : 1 + (order - initialCapacity) / 2);
        }
        Map<Material, Values> materials = new EnumMap<>(Material.class);
        for (Material material : Material.values()) if (material != Material.ORIGINAL) materials.put(material, materialValues(family, material));
        Map<String, Values> items = new HashMap<>();
        items.put("minecraft:string", new Values(0.4, 1.6, 0.35, 1.2, 0.5));
        items.put("minecraft:tripwire_hook", new Values(0.7, 0.9, 0.8, 1.8, 1.1));
        items.put("minecraft:prismarine_shard", new Values(1.1, 1.3, 0.8, 1.8, 1.0));
        items.put("minecraft:wooden_shovel", new Values(0.6, 0.5, 1.0, 0.7, 1.4));
        items.put("irons_spellbooks:arcane_essence", new Values(1.8, 1.8, 0.6, 1.3, 0.35));
        items.put("irons_spellbooks:frosted_helve", new Values(0.8, 0.7, 1.2, 1.1, 1.5));
        items.put("irons_spellbooks:permafrost_shard", new Values(1.5, 1.6, 0.7, 1.2, 0.8));
        items.put("irons_spellbooks:pyrium_ingot", new Values(1.6, 1.3, 1.1, 1.4, 1.2));
        items.put("irons_spellbooks:magic_cloth", new Values(0.5, 0.7, 1.6, 1.4, 0.65));
        items.putAll(itemValues);
        Map<Integer, Cell> seeds = new LinkedHashMap<>();
        List<String> components = new ArrayList<>();
        for (Map<String, Integer> budget : List.of(recipe, environment)) {
            for (Map.Entry<String, Integer> entry : budget.entrySet()) {
                for (int count = 0; count < entry.getValue(); count++) components.add(entry.getKey());
            }
        }
        if (components.size() > initialCapacity) throw new IllegalArgumentException("Too many native components");
        for (int index = 0; index < components.size(); index++) {
            String item = components.get(index);
            seeds.put(layout[index], new Cell(item, nativeMaterial(item), FourWeaponFoundationRules.REFERENCE, ""));
        }
        Values total = FourWeaponFoundationRules.ZERO;
        for (Map.Entry<Integer, Cell> entry : seeds.entrySet()) {
            Values value = items.getOrDefault(entry.getValue().itemId(), materials.get(entry.getValue().material()));
            if (value == null) throw new IllegalArgumentException("Native contribution unknown: " + entry.getValue().itemId());
            total = total.plus(bases.get(entry.getKey())).plus(value.times(weights.get(entry.getKey())));
        }
        return new Profile(id, family, axes(family), total.scale(0.995), weights, bases, unlocks, materials, items, seeds,
                evidence, recipe, environment);
    }

    private static Profile bossProfile(String id, String family, int initialCapacity) {
        Map<String, Integer> components = NonManorWeaponComponentRules.componentCounts(id);
        Map<String, Values> values = new LinkedHashMap<>();
        for (String item : components.keySet()) {
            values.put(item, materialValues(family, NonManorWeaponComponentRules.material(item)));
        }
        Profile profile = create(id, family, initialCapacity, Map.of(), components,
                "NONE:non-manor-boss-relic-component-alloys-food;native-reference-not-refundable", values);
        Map<Integer, Cell> nativeCells = new LinkedHashMap<>();
        for (Map.Entry<Integer, Cell> entry : profile.nativeCells().entrySet()) {
            Cell cell = entry.getValue();
            nativeCells.put(entry.getKey(), new Cell(cell.itemId(), NonManorWeaponComponentRules.material(cell.itemId()),
                    FourWeaponFoundationRules.REFERENCE, ""));
        }
        return new Profile(profile.id(), profile.family(), profile.axes(), profile.minimum(), profile.weights(),
                profile.bases(), profile.unlocks(), profile.materials(), profile.items(), nativeCells,
                profile.recipeEvidence(), profile.recipeBudget(), profile.environmentBudget());
    }

    public static Profile previousBossProfile(String id) {
        return switch (id) {
            case "ftboss:barrel_crossbow" -> create(id, "crossbow", 14, Map.of(),
                    budget("minecraft:oak_planks", 1, "minecraft:iron_ingot", 1, "minecraft:string", 1),
                    "NONE:5C5B-no-output-recipe;BossMercy-barrel-drop", Map.of());
            case "ftboss:frost_queen_staff" -> create(id, "staff", 14, Map.of(),
                    budget("minecraft:ice", 1, "minecraft:amethyst_shard", 1, "minecraft:stick", 1),
                    "NONE:5C5B-no-output-recipe;BossRelicDrops-frost-drop",
                    Map.of("minecraft:ice", new Values(1.4, 1.3, 0.8, 1.0, 0.7)));
            default -> null;
        };
    }

    private static Material nativeMaterial(String id) {
        return switch (id) {
            case "minecraft:stick", "minecraft:oak_planks", "minecraft:crimson_planks", "minecraft:wooden_shovel" -> Material.WOOD;
            case "minecraft:cobblestone" -> Material.STONE;
            case "minecraft:iron_ingot", "minecraft:tripwire_hook" -> Material.IRON;
            case "minecraft:copper_ingot" -> Material.COPPER;
            case "minecraft:gold_ingot" -> Material.GOLD;
            case "minecraft:diamond" -> Material.DIAMOND;
            case "minecraft:netherite_ingot" -> Material.NETHERITE;
            case "minecraft:amethyst_shard" -> Material.AMETHYST;
            case "minecraft:bone" -> Material.BONE;
            case "cataclysm:black_steel_ingot" -> Material.BLACK_STEEL;
            case "cataclysm:cursium_ingot" -> Material.CURSIUM;
            case "minecraft:redstone" -> Material.REDSTONE;
            default -> Material.ORIGINAL;
        };
    }
}
