/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util.foundations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.NonManorWeaponComponentRules;
import net.farmingtales.ftboss.util.foundations.AdditionalFoundationProfiles;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.farmingtales.ftboss.util.foundations.ManorTridentProfiles;
import net.farmingtales.ftboss.util.foundations.MaterialBowProfiles;
import net.farmingtales.ftboss.util.foundations.MaterialCrossbowProfiles;

public final class FourWeaponFoundationProfiles {
    private static final int[] BOW_LAYOUT = new int[]{4, 12, 21, 5, 14, 23, 3, 11, 20, 6, 15, 24, 2, 10, 19, 7, 16, 25, 1, 9, 18, 8, 17, 26, 0, 13, 22};
    private static final int[] CROSSBOW_LAYOUT = new int[]{3, 4, 5, 13, 14, 22, 12, 2, 6, 11, 15, 21, 23, 1, 7, 10, 16, 20, 24, 0, 8, 9, 17, 18, 25, 26, 19};
    private static final int[] STAFF_LAYOUT = new int[]{4, 13, 22, 3, 5, 12, 14, 21, 23, 2, 6, 11, 15, 20, 24, 1, 7, 10, 16, 19, 25, 0, 8, 9, 17, 18, 26};
    private static final int[] AXE_LAYOUT = new int[]{3, 4, 12, 13, 22, 5, 14, 21, 23, 2, 11, 20, 6, 15, 24, 1, 10, 19, 7, 16, 25, 0, 9, 18, 8, 17, 26};
    private static final int[] TRIDENT_LAYOUT = new int[]{3, 13, 4, 5, 12, 14, 22, 2, 6, 11, 15, 21, 23, 1, 7, 10, 16, 20, 24, 0, 8, 9, 17, 18, 25, 26, 19};

    private FourWeaponFoundationProfiles() {
    }

    public static Map<String, FourWeaponFoundationRules.Profile> examples() {
        LinkedHashMap<String, FourWeaponFoundationRules.Profile> linkedHashMap = new LinkedHashMap<String, FourWeaponFoundationRules.Profile>();
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("minecraft:bow", "bow", 6, FourWeaponFoundationProfiles.budget("minecraft:stick", 3, "minecraft:string", 3), Map.of(), "minecraft:bow"));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("minecraft:crossbow", "crossbow", 7, FourWeaponFoundationProfiles.budget("minecraft:stick", 3, "minecraft:string", 2, "minecraft:iron_ingot", 1, "minecraft:tripwire_hook", 1), Map.of(), "minecraft:crossbow"));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("irons_spellbooks:graybeard_staff", "staff", 3, FourWeaponFoundationProfiles.budget("minecraft:wooden_shovel", 1, "minecraft:iron_ingot", 1, "irons_spellbooks:arcane_essence", 1), Map.of(), "irons_spellbooks:graybeard_staff"));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("irons_spellbooks:artificer_cane", "staff", 6, FourWeaponFoundationProfiles.budget("minecraft:crimson_planks", 3, "minecraft:gold_ingot", 2, "minecraft:amethyst_shard", 1), Map.of(), "irons_spellbooks:artificer_cane"));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("irons_spellbooks:ice_staff", "staff", 2, FourWeaponFoundationProfiles.budget("irons_spellbooks:frosted_helve", 1, "irons_spellbooks:permafrost_shard", 1), Map.of(), "irons_spellbooks:ice_staff"));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("irons_spellbooks:pyrium_staff", "staff", 4, FourWeaponFoundationProfiles.budget("irons_spellbooks:pyrium_ingot", 2, "irons_spellbooks:magic_cloth", 1, "minecraft:netherite_ingot", 1), Map.of(), "irons_spellbooks:pyrium_staff"));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("irons_spellbooks:lightning_rod", "staff", 2, Map.of(), FourWeaponFoundationProfiles.budget("minecraft:copper_ingot", 1, "minecraft:amethyst_shard", 1), "NONE:audited-jar-no-recipe;environment-only"));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("irons_spellbooks:blood_staff", "staff", 2, Map.of(), FourWeaponFoundationProfiles.budget("minecraft:bone", 1, "minecraft:redstone", 1), "NONE:audited-jar-no-recipe;environment-only"));
        String[] stringArray = new String[]{"wooden", "stone", "iron", "golden", "diamond", "netherite"};
        String[] stringArray2 = new String[]{"minecraft:oak_planks", "minecraft:cobblestone", "minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:diamond", "minecraft:diamond"};
        int[] nArray = new int[]{8, 9, 10, 10, 12, 17};
        for (int i = 0; i < stringArray.length; ++i) {
            LinkedHashMap<String, Integer> linkedHashMap2 = new LinkedHashMap<String, Integer>(FourWeaponFoundationProfiles.budget(stringArray2[i], 3, "minecraft:stick", 2));
            if (i == 5) {
                linkedHashMap2.put("minecraft:netherite_ingot", 1);
            }
            String string = "minecraft:" + stringArray[i] + "_axe";
            FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create(string, "axe", nArray[i], linkedHashMap2, Map.of(), (String)(i == 5 ? "minecraft:diamond_axe+minecraft:netherite_axe_smithing;template-excluded-from-structure" : string)));
        }
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("minecraft:trident", "trident", 13, Map.of(), FourWeaponFoundationProfiles.budget("minecraft:prismarine_shard", 1, "minecraft:stick", 1), "NONE:vanilla-no-recipe;environment-only"));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.upgradedTrident("tridents_n_stuff:frenzied_trident", FourWeaponFoundationProfiles.budget("minecraft:trident", 1, "minecraft:gold_ingot", 4, "minecraft:redstone_block", 4), Map.of("minecraft:redstone_block", new FourWeaponFoundationRules.Values(0.9, 1.1, 0.65, 1.7, 1.7))));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.upgradedTrident("tridents_n_stuff:farshot_trident", FourWeaponFoundationProfiles.budget("minecraft:trident", 1, "minecraft:gold_ingot", 4, "minecraft:phantom_membrane", 2, "minecraft:feather", 2), Map.of("minecraft:phantom_membrane", new FourWeaponFoundationRules.Values(0.6, 0.6, 1.6, 1.4, 1.3), "minecraft:feather", new FourWeaponFoundationRules.Values(0.4, 0.4, 1.7, 1.2, 0.8))));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.upgradedTrident("tridents_n_stuff:ancient_trident", FourWeaponFoundationProfiles.budget("minecraft:trident", 1, "minecraft:gold_ingot", 4, "minecraft:netherite_scrap", 4), Map.of("minecraft:netherite_scrap", FourWeaponFoundationProfiles.materialValues("trident", FoodWeaponBuildRules.Material.NETHERITE))));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.upgradedTrident("tridents_n_stuff:holy_trident", FourWeaponFoundationProfiles.budget("minecraft:trident", 1, "minecraft:gold_ingot", 4, "minecraft:golden_apple", 4), Map.of("minecraft:golden_apple", new FourWeaponFoundationRules.Values(0.8, 0.9, 0.7, 1.1, 1.6))));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.upgradedTrident("tridents_n_stuff:oceanic_trident", FourWeaponFoundationProfiles.budget("minecraft:trident", 1, "minecraft:gold_ingot", 4, "minecraft:nautilus_shell", 2, "minecraft:kelp", 2), Map.of("minecraft:nautilus_shell", new FourWeaponFoundationRules.Values(1.1, 1.2, 0.8, 1.8, 1.6), "minecraft:kelp", new FourWeaponFoundationRules.Values(0.5, 0.4, 1.0, 1.6, 0.8))));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("tide:starlight_bow", "bow", 8, FourWeaponFoundationProfiles.budget("minecraft:bow", 1, "tide:shooting_starfish", 1), Map.of(), "tide:starlight_bow", Map.of("minecraft:bow", new FourWeaponFoundationRules.Values(1.1, 1.6, 1.2, 1.0, 1.1), "tide:shooting_starfish", new FourWeaponFoundationRules.Values(0.8, 1.0, 1.7, 1.2, 0.9))));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("irons_spellbooks:autoloader_crossbow", "crossbow", 9, Map.of(), FourWeaponFoundationProfiles.budget("minecraft:iron_ingot", 1, "minecraft:redstone", 1, "minecraft:stick", 1), "NONE:installed-3.16.3-no-output-recipe;ItemRegistry-registered-source", Map.of()));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.bossProfile("ftboss:barrel_crossbow", "crossbow", 14));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("armamentarium:ender_maiden_shortbow", "bow", 10, Map.of(), FourWeaponFoundationProfiles.budget("minecraft:ender_pearl", 1, "minecraft:stick", 1, "minecraft:string", 1), "NONE:installed-no-output-recipe;farmingtales:advancements/culinary_master-reward", Map.of("minecraft:ender_pearl", new FourWeaponFoundationRules.Values(0.9, 1.0, 1.7, 1.2, 0.7))));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("cataclysm:cursed_bow", "bow", 12, FourWeaponFoundationProfiles.budget("cataclysm:cursium_ingot", 2, "cataclysm:black_steel_ingot", 1, "minecraft:string", 3), Map.of(), "cataclysm:cursed_bow", Map.of()));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("ftboss:qizai_prayer_staff", "staff", 14, Map.of(), FourWeaponFoundationProfiles.budget("minecraft:amethyst_shard", 1, "minecraft:gold_ingot", 1, "minecraft:stick", 1), "NONE:5C5B-no-output-recipe;FTCreativeTabs-registered-source", Map.of()));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.bossProfile("ftboss:frost_queen_staff", "staff", 14));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("armamentarium:mariana_scepter", "staff", 12, Map.of(), FourWeaponFoundationProfiles.budget("minecraft:prismarine_shard", 1, "minecraft:nautilus_shell", 1, "minecraft:stick", 1), "NONE:installed-no-output-recipe;farmingtales:advancements/weapons/mariana_scepter-reward", Map.of("minecraft:nautilus_shell", new FourWeaponFoundationRules.Values(1.5, 1.3, 1.1, 1.4, 0.8))));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("irons_spellbooks:staff_of_the_nines", "staff", 8, Map.of(), FourWeaponFoundationProfiles.budget("minecraft:oak_planks", 1, "minecraft:iron_ingot", 1), "NONE:installed-3.16.3-no-output-recipe;ItemRegistry-registered-source", Map.of()));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("legendary_monsters:wand_of_clouds", "staff", 9, FourWeaponFoundationProfiles.budget("legendary_monsters:air_rune", 1, "legendary_monsters:cloud_rod", 1, "minecraft:iron_ingot", 1), Map.of(), "legendary_monsters:wand_of_clouds", Map.of("legendary_monsters:air_rune", new FourWeaponFoundationRules.Values(1.7, 1.5, 0.7, 1.4, 0.6), "legendary_monsters:cloud_rod", new FourWeaponFoundationRules.Values(0.9, 1.1, 1.2, 1.4, 1.6))));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("cataclysm:wrath_of_the_desert", "bow", 14, FourWeaponFoundationProfiles.budget("cataclysm:cursed_bow", 1, "cataclysm:sandstorm_in_a_bottle", 1), Map.of(), "cataclysm:weapon_infusion/wrath_of_the_desert", Map.of("cataclysm:cursed_bow", new FourWeaponFoundationRules.Values(1.4, 1.7, 1.3, 1.0, 1.6), "cataclysm:sandstorm_in_a_bottle", new FourWeaponFoundationRules.Values(0.9, 0.7, 1.6, 1.4, 1.1))));
        FourWeaponFoundationProfiles.put(linkedHashMap, FourWeaponFoundationProfiles.create("cataclysm:black_steel_axe", "axe", 12, FourWeaponFoundationProfiles.budget("cataclysm:black_steel_ingot", 3, "minecraft:stick", 2), Map.of(), "cataclysm:black_steel_axe", Map.of()));
        MaterialCrossbowProfiles.addTo(linkedHashMap);
        LinkedHashMap<String, FourWeaponFoundationRules.Profile> linkedHashMap3 = linkedHashMap;
        MaterialBowProfiles.addTo(linkedHashMap3);
        ManorTridentProfiles.addTo(linkedHashMap3);
        AdditionalFoundationProfiles.addTo(linkedHashMap3);
        return Collections.unmodifiableMap(linkedHashMap3);
    }

    private static void put(Map<String, FourWeaponFoundationRules.Profile> map, FourWeaponFoundationRules.Profile profile) {
        map.put(profile.id(), profile);
    }

    private static FourWeaponFoundationRules.Profile upgradedTrident(String string, Map<String, Integer> map, Map<String, FourWeaponFoundationRules.Values> map2) {
        HashMap<String, FourWeaponFoundationRules.Values> hashMap = new HashMap<String, FourWeaponFoundationRules.Values>(map2);
        hashMap.put("minecraft:trident", new FourWeaponFoundationRules.Values(1.2, 1.4, 0.8, 1.6, 1.2));
        return FourWeaponFoundationProfiles.create(string, "trident", 15, map, FourWeaponFoundationProfiles.budget("minecraft:prismarine_shard", 1, "minecraft:stick", 1), string, hashMap);
    }

    private static Map<String, Integer> budget(Object ... objectArray) {
        LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < objectArray.length; i += 2) {
            linkedHashMap.put((String)objectArray[i], (Integer)objectArray[i + 1]);
        }
        return Collections.unmodifiableMap(linkedHashMap);
    }

    public static List<String> axes(String string) {
        return switch (string) {
            case "bow" -> List.of("limb_flex", "string_tension", "arrow_guidance", "draw_recovery", "frame_bracing");
            case "crossbow" -> List.of("prod_flex", "string_tension", "bolt_guidance", "trigger_release", "stock_bracing");
            case "staff" -> List.of("spell_conduction", "focus_coherence", "mana_insulation", "channel_stability", "shaft_support");
            case "axe" -> List.of("haft_leverage", "head_binding", "impact_transfer", "swing_balance", "edge_integrity");
            case "trident" -> List.of("shaft_thrust", "tine_integrity", "launch_balance", "water_tracking", "return_binding");
            default -> throw new IllegalArgumentException("Unknown family");
        };
    }

    public static FourWeaponFoundationRules.Values materialValues(String string, FoodWeaponBuildRules.Material material) {
        String string2;
        FourWeaponFoundationRules.Values values = switch (string2 = material.name()) {
            case "WOOD", "WOOD_FENCE", "BAMBOO", "BAMBOO_FENCE" -> new FourWeaponFoundationRules.Values(0.8, 1.25, 0.25, 0.45, 0.55);
            case "LEATHER", "CHAIN", "SLIME", "HONEYCOMB" -> new FourWeaponFoundationRules.Values(0.55, 1.45, 0.35, 0.6, 0.35);
            case "IRON", "IRON_BARS", "COPPER", "GOLD", "LIGHTNING_ROD", "ANCIENT_METAL" -> new FourWeaponFoundationRules.Values(1.2, 0.55, 1.3, 1.25, 1.05);
            case "STONE", "STONE_WALL", "BRICK_WALL", "BLACKSTONE_WALL", "DEEPSLATE_WALL", "OBSIDIAN", "NETHER_FENCE" -> new FourWeaponFoundationRules.Values(1.35, 0.3, 0.35, 1.45, 1.1);
            case "DIAMOND", "NETHERITE", "IGNITIUM", "CURSIUM", "BLACK_STEEL", "WITHERITE" -> new FourWeaponFoundationRules.Values(1.65, 0.45, 1.0, 1.4, 1.65);
            case "AMETHYST", "QUARTZ", "LAPIS", "REDSTONE", "EMERALD", "GLOWSTONE", "ECHO_SHARD", "END_ROD", "ENDERITE", "MITHRIL" -> new FourWeaponFoundationRules.Values(0.9, 0.65, 1.7, 0.7, 1.1);
            case "PRISMARINE", "BOSS_TIDE" -> new FourWeaponFoundationRules.Values(1.1, 0.7, 1.3, 0.9, 1.0);
            case "ORIGINAL" -> throw new IllegalArgumentException("ORIGINAL has no universal foundation contribution");
            default -> new FourWeaponFoundationRules.Values(1.0, 0.8, 1.0, 0.9, 1.0);
        };
        return switch (string) {
            case "bow" -> new FourWeaponFoundationRules.Values(values.second(), values.second(), values.first(), (values.first() + values.second()) * 0.5, values.first());
            case "crossbow" -> new FourWeaponFoundationRules.Values((values.first() + values.second()) * 0.5, values.second(), values.first(), values.third(), values.first());
            case "staff" -> new FourWeaponFoundationRules.Values(values.third(), (values.third() + values.fifth()) * 0.5, values.first(), (values.first() + values.third()) * 0.5, values.first());
            case "axe" -> new FourWeaponFoundationRules.Values(values.first(), values.second(), values.fourth(), (values.first() + values.second()) * 0.5, values.fifth());
            case "trident" -> new FourWeaponFoundationRules.Values(values.first(), values.fifth(), values.second(), (values.second() + values.third()) * 0.5, values.third());
            default -> throw new IllegalArgumentException("Unknown family");
        };
    }

    public static FourWeaponFoundationRules.Profile create(String string, String string2, int n, Map<String, Integer> map, Map<String, Integer> map2, String string3) {
        return FourWeaponFoundationProfiles.create(string, string2, n, map, map2, string3, Map.of());
    }

    public static FourWeaponFoundationRules.Profile create(String string, String string2, int n, Map<String, Integer> map, Map<String, Integer> map2, String string3, Map<String, FourWeaponFoundationRules.Values> map3) {
        int[] nArray = switch (string2) {
            case "bow" -> BOW_LAYOUT;
            case "crossbow" -> CROSSBOW_LAYOUT;
            case "staff" -> STAFF_LAYOUT;
            case "axe" -> AXE_LAYOUT;
            case "trident" -> TRIDENT_LAYOUT;
            default -> throw new IllegalArgumentException("Unknown family");
        };
        if (n < 1 || n + 8 > 27) {
            throw new IllegalArgumentException("Invalid capacity");
        }
        Object object = new ArrayList<FourWeaponFoundationRules.Values>(Collections.nCopies(27, FourWeaponFoundationRules.ZERO));
        ArrayList<FourWeaponFoundationRules.Values> arrayList = new ArrayList<FourWeaponFoundationRules.Values>(Collections.nCopies(27, FourWeaponFoundationRules.ZERO));
        ArrayList<Integer> arrayList2 = new ArrayList<Integer>(Collections.nCopies(27, -1));
        for (int i = 0; i < n + 8; ++i) {
            int n2 = nArray[i];
            int n3 = i % 5;
            double[] dArray = new double[]{0.25, 0.25, 0.25, 0.25, 0.25};
            dArray[n3] = 1.0;
            object.set(n2, new FourWeaponFoundationRules.Values(dArray[0], dArray[1], dArray[2], dArray[3], dArray[4]));
            arrayList.set(n2, new FourWeaponFoundationRules.Values(0.005, 0.005, 0.005, 0.005, 0.005));
            arrayList2.set(n2, i < n ? 0 : 1 + (i - n) / 2);
        }
        EnumMap<FoodWeaponBuildRules.Material, FourWeaponFoundationRules.Values> enumMap = new EnumMap<FoodWeaponBuildRules.Material, FourWeaponFoundationRules.Values>(FoodWeaponBuildRules.Material.class);
        for (Object object2 : FoodWeaponBuildRules.Material.values()) {
            if (object2 == FoodWeaponBuildRules.Material.ORIGINAL) continue;
            enumMap.put((FoodWeaponBuildRules.Material)((Object)object2), FourWeaponFoundationProfiles.materialValues(string2, object2));
        }
        HashMap hashMap = new HashMap();
        hashMap.put("minecraft:string", new FourWeaponFoundationRules.Values(0.4, 1.6, 0.35, 1.2, 0.5));
        hashMap.put("minecraft:tripwire_hook", new FourWeaponFoundationRules.Values(0.7, 0.9, 0.8, 1.8, 1.1));
        hashMap.put("minecraft:prismarine_shard", new FourWeaponFoundationRules.Values(1.1, 1.3, 0.8, 1.8, 1.0));
        hashMap.put("minecraft:wooden_shovel", new FourWeaponFoundationRules.Values(0.6, 0.5, 1.0, 0.7, 1.4));
        hashMap.put("irons_spellbooks:arcane_essence", new FourWeaponFoundationRules.Values(1.8, 1.8, 0.6, 1.3, 0.35));
        hashMap.put("irons_spellbooks:frosted_helve", new FourWeaponFoundationRules.Values(0.8, 0.7, 1.2, 1.1, 1.5));
        hashMap.put("irons_spellbooks:permafrost_shard", new FourWeaponFoundationRules.Values(1.5, 1.6, 0.7, 1.2, 0.8));
        hashMap.put("irons_spellbooks:pyrium_ingot", new FourWeaponFoundationRules.Values(1.6, 1.3, 1.1, 1.4, 1.2));
        hashMap.put("irons_spellbooks:magic_cloth", new FourWeaponFoundationRules.Values(0.5, 0.7, 1.6, 1.4, 0.65));
        hashMap.putAll(map3);
        LinkedHashMap<Integer, FourWeaponFoundationRules.Cell> linkedHashMap = new LinkedHashMap<Integer, FourWeaponFoundationRules.Cell>();
        ArrayList<String> arrayList3 = new ArrayList<String>();
        for (Map iterator : List.of(map, map2)) {
            for (Map.Entry entry : iterator.entrySet()) {
                for (int i = 0; i < (Integer)entry.getValue(); ++i) {
                    arrayList3.add((String)entry.getKey());
                }
            }
        }
        if (arrayList3.size() > n) {
            throw new IllegalArgumentException("Too many native components");
        }
        for (int i = 0; i < arrayList3.size(); ++i) {
            String string4 = (String)arrayList3.get(i);
            linkedHashMap.put(nArray[i], new FourWeaponFoundationRules.Cell(string4, FourWeaponFoundationProfiles.nativeMaterial(string4), "native-reference", ""));
        }
        FourWeaponFoundationRules.Values values = FourWeaponFoundationRules.ZERO;
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            FourWeaponFoundationRules.Values values2 = hashMap.getOrDefault(((FourWeaponFoundationRules.Cell)entry.getValue()).itemId(), (FourWeaponFoundationRules.Values)enumMap.get((Object)((FourWeaponFoundationRules.Cell)entry.getValue()).material()));
            if (values2 == null) {
                throw new IllegalArgumentException("Native contribution unknown: " + ((FourWeaponFoundationRules.Cell)entry.getValue()).itemId());
            }
            values = values.plus((FourWeaponFoundationRules.Values)arrayList.get((Integer)entry.getKey())).plus(values2.times((FourWeaponFoundationRules.Values)object.get((Integer)entry.getKey())));
        }
        return new FourWeaponFoundationRules.Profile(string, string2, FourWeaponFoundationProfiles.axes(string2), values.scale(0.995), (List<FourWeaponFoundationRules.Values>)object, arrayList, arrayList2, enumMap, hashMap, linkedHashMap, string3, map, map2);
    }

    private static FoodWeaponBuildRules.Material nativeMaterial(String string) {
        return switch (string) {
            case "minecraft:stick", "minecraft:oak_planks", "minecraft:crimson_planks", "minecraft:wooden_shovel" -> FoodWeaponBuildRules.Material.WOOD;
            case "minecraft:cobblestone" -> FoodWeaponBuildRules.Material.STONE;
            case "minecraft:iron_ingot", "minecraft:tripwire_hook" -> FoodWeaponBuildRules.Material.IRON;
            case "minecraft:copper_ingot" -> FoodWeaponBuildRules.Material.COPPER;
            case "minecraft:gold_ingot" -> FoodWeaponBuildRules.Material.GOLD;
            case "minecraft:diamond" -> FoodWeaponBuildRules.Material.DIAMOND;
            case "minecraft:netherite_ingot" -> FoodWeaponBuildRules.Material.NETHERITE;
            case "minecraft:amethyst_shard" -> FoodWeaponBuildRules.Material.AMETHYST;
            case "minecraft:bone" -> FoodWeaponBuildRules.Material.BONE;
            case "cataclysm:black_steel_ingot" -> FoodWeaponBuildRules.Material.BLACK_STEEL;
            case "cataclysm:cursium_ingot" -> FoodWeaponBuildRules.Material.CURSIUM;
            case "minecraft:redstone" -> FoodWeaponBuildRules.Material.REDSTONE;
            default -> FoodWeaponBuildRules.Material.ORIGINAL;
        };
    }

    private static FourWeaponFoundationRules.Profile bossProfile(String string, String string2, int n) {
        Map<String, Integer> map = NonManorWeaponComponentRules.componentCounts(string);
        LinkedHashMap<String, FourWeaponFoundationRules.Values> linkedHashMap = new LinkedHashMap<String, FourWeaponFoundationRules.Values>();
        for (String object2 : map.keySet()) {
            linkedHashMap.put(object2, FourWeaponFoundationProfiles.materialValues(string2, NonManorWeaponComponentRules.material(object2)));
        }
        FourWeaponFoundationRules.Profile profile = FourWeaponFoundationProfiles.create(string, string2, n, Map.of(), map, "NONE:non-manor-boss-relic-component-alloys-food;native-reference-not-refundable", linkedHashMap);
        LinkedHashMap<Integer, FourWeaponFoundationRules.Cell> linkedHashMap2 = new LinkedHashMap<Integer, FourWeaponFoundationRules.Cell>();
        for (Map.Entry<Integer, FourWeaponFoundationRules.Cell> entry : profile.nativeCells().entrySet()) {
            FourWeaponFoundationRules.Cell cell = entry.getValue();
            linkedHashMap2.put(entry.getKey(), new FourWeaponFoundationRules.Cell(cell.itemId(), NonManorWeaponComponentRules.material(cell.itemId()), "native-reference", ""));
        }
        return new FourWeaponFoundationRules.Profile(profile.id(), profile.family(), profile.axes(), profile.minimum(), profile.weights(), profile.bases(), profile.unlocks(), profile.materials(), profile.items(), linkedHashMap2, profile.recipeEvidence(), profile.recipeBudget(), profile.environmentBudget());
    }

    public static FourWeaponFoundationRules.Profile previousBossProfile(String string) {
        return switch (string) {
            case "ftboss:barrel_crossbow" -> FourWeaponFoundationProfiles.create(string, "crossbow", 14, Map.of(), FourWeaponFoundationProfiles.budget("minecraft:oak_planks", 1, "minecraft:iron_ingot", 1, "minecraft:string", 1), "NONE:5C5B-no-output-recipe;BossMercy-barrel-drop", Map.of());
            case "ftboss:frost_queen_staff" -> FourWeaponFoundationProfiles.create(string, "staff", 14, Map.of(), FourWeaponFoundationProfiles.budget("minecraft:ice", 1, "minecraft:amethyst_shard", 1, "minecraft:stick", 1), "NONE:5C5B-no-output-recipe;BossRelicDrops-frost-drop", Map.of("minecraft:ice", new FourWeaponFoundationRules.Values(1.4, 1.3, 0.8, 1.0, 0.7)));
            default -> null;
        };
    }
}

