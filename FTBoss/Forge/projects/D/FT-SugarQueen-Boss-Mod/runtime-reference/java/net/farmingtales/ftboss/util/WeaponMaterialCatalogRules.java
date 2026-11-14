/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;

public final class WeaponMaterialCatalogRules {
    private static final Pattern IDENTIFIER = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");
    private static final Set<String> RETIRED = Set.of("zinc_ingot", "brass_ingot", "electrum_ingot", "pyrium_ingot", "mithril_ingot");
    private static final Map<FoodWeaponBuildRules.Material, List<String>> OFFERINGS = Map.ofEntries(Map.entry(FoodWeaponBuildRules.Material.WOOD, List.of("minecraft:stick")), Map.entry(FoodWeaponBuildRules.Material.STONE, List.of("minecraft:cobblestone", "minecraft:stone", "minecraft:blackstone")), Map.entry(FoodWeaponBuildRules.Material.IRON, List.of("minecraft:iron_ingot")), Map.entry(FoodWeaponBuildRules.Material.COPPER, List.of("minecraft:copper_ingot")), Map.entry(FoodWeaponBuildRules.Material.GOLD, List.of("minecraft:gold_ingot")), Map.entry(FoodWeaponBuildRules.Material.DIAMOND, List.of("minecraft:diamond")), Map.entry(FoodWeaponBuildRules.Material.NETHERITE, List.of("minecraft:netherite_ingot")), Map.entry(FoodWeaponBuildRules.Material.BONE, List.of("minecraft:bone")), Map.entry(FoodWeaponBuildRules.Material.OBSIDIAN, List.of("minecraft:obsidian")), Map.entry(FoodWeaponBuildRules.Material.LEATHER, List.of("minecraft:leather")), Map.entry(FoodWeaponBuildRules.Material.BLAZE, List.of("minecraft:blaze_rod")), Map.entry(FoodWeaponBuildRules.Material.EMERALD, List.of("minecraft:emerald")), Map.entry(FoodWeaponBuildRules.Material.MITHRIL, List.of("irons_spellbooks:mithril_ingot")), Map.entry(FoodWeaponBuildRules.Material.COAL, List.of("minecraft:coal")), Map.entry(FoodWeaponBuildRules.Material.LAPIS, List.of("minecraft:lapis_lazuli")), Map.entry(FoodWeaponBuildRules.Material.REDSTONE, List.of("minecraft:redstone")), Map.entry(FoodWeaponBuildRules.Material.QUARTZ, List.of("minecraft:quartz")), Map.entry(FoodWeaponBuildRules.Material.AMETHYST, List.of("minecraft:amethyst_shard")), Map.entry(FoodWeaponBuildRules.Material.WITHERITE, List.of("cataclysm:witherite_ingot")), Map.entry(FoodWeaponBuildRules.Material.ENDERITE, List.of("cataclysm:enderite_ingot")), Map.entry(FoodWeaponBuildRules.Material.IGNITIUM, List.of("cataclysm:ignitium_ingot")), Map.entry(FoodWeaponBuildRules.Material.CURSIUM, List.of("cataclysm:cursium_ingot")), Map.entry(FoodWeaponBuildRules.Material.ANCIENT_METAL, List.of("cataclysm:ancient_metal_ingot")), Map.entry(FoodWeaponBuildRules.Material.BLACK_STEEL, List.of("cataclysm:black_steel_ingot")), Map.entry(FoodWeaponBuildRules.Material.BOSS_FROST, List.of("ftboss:frost_material")), Map.entry(FoodWeaponBuildRules.Material.BOSS_BUTCHER, List.of("ftboss:butcher_material")), Map.entry(FoodWeaponBuildRules.Material.BOSS_TEA, List.of("ftboss:tea_material")), Map.entry(FoodWeaponBuildRules.Material.BOSS_WINE, List.of("ftboss:wine_material")), Map.entry(FoodWeaponBuildRules.Material.BOSS_CHEESE, List.of("ftboss:cheese_material")), Map.entry(FoodWeaponBuildRules.Material.BOSS_CAPACITOR, List.of("ftboss:capacitor_material")), Map.entry(FoodWeaponBuildRules.Material.BOSS_SPORE, List.of("ftboss:spore_material")), Map.entry(FoodWeaponBuildRules.Material.BOSS_APPLE, List.of("ftboss:apple_material")), Map.entry(FoodWeaponBuildRules.Material.BOSS_SHEPHERD, List.of("ftboss:shepherd_material")), Map.entry(FoodWeaponBuildRules.Material.BOSS_TIDE, List.of("ftboss:tide_material", "ftboss:tide_pearl")));
    private static final Map<String, FoodWeaponBuildRules.Material> TAGS = Map.ofEntries(Map.entry("minecraft:planks", FoodWeaponBuildRules.Material.WOOD), Map.entry("minecraft:logs", FoodWeaponBuildRules.Material.WOOD), Map.entry("forge:ingots/iron", FoodWeaponBuildRules.Material.IRON), Map.entry("forge:ingots/copper", FoodWeaponBuildRules.Material.COPPER), Map.entry("forge:ingots/gold", FoodWeaponBuildRules.Material.GOLD), Map.entry("forge:ingots/netherite", FoodWeaponBuildRules.Material.NETHERITE), Map.entry("forge:gems/diamond", FoodWeaponBuildRules.Material.DIAMOND), Map.entry("forge:gems/emerald", FoodWeaponBuildRules.Material.EMERALD));

    private WeaponMaterialCatalogRules() {
    }

    public static List<String> offerings(FoodWeaponBuildRules.Material material) {
        return material == null ? List.of() : OFFERINGS.getOrDefault((Object)material, List.of());
    }

    public static Group group(FoodWeaponBuildRules.Material material) {
        if (material == FoodWeaponBuildRules.Material.DIAMOND_ALLOY) {
            return Group.INGOT;
        }
        if (material == null) {
            return Group.BASE;
        }
        if (WeaponMaterialCatalogRules.boss(material)) {
            return Group.BOSS;
        }
        return switch (material) {
            case FoodWeaponBuildRules.Material.IRON, FoodWeaponBuildRules.Material.COPPER, FoodWeaponBuildRules.Material.GOLD, FoodWeaponBuildRules.Material.NETHERITE, FoodWeaponBuildRules.Material.MITHRIL, FoodWeaponBuildRules.Material.WITHERITE, FoodWeaponBuildRules.Material.ENDERITE, FoodWeaponBuildRules.Material.IGNITIUM, FoodWeaponBuildRules.Material.CURSIUM, FoodWeaponBuildRules.Material.ANCIENT_METAL, FoodWeaponBuildRules.Material.BLACK_STEEL -> Group.INGOT;
            default -> Group.BASE;
        };
    }

    public static boolean boss(FoodWeaponBuildRules.Material material) {
        return material != null && material.id().startsWith("boss_");
    }

    public static FoodWeaponBuildRules.Material resolve(String string, Collection<String> collection) {
        if (string == null || !IDENTIFIER.matcher(string).matches()) {
            return FoodWeaponBuildRules.Material.ORIGINAL;
        }
        for (FoodWeaponBuildRules.Material object : FoodWeaponBuildRules.Material.values()) {
            if (!WeaponMaterialCatalogRules.offerings(object).contains(string)) continue;
            return object;
        }
        String string2 = string.substring(string.indexOf(58) + 1);
        if (RETIRED.contains(string2) || string2.startsWith("raw_") || string2.endsWith("_nugget") || string2.endsWith("_ore") || string2.endsWith("_block")) {
            return FoodWeaponBuildRules.Material.ORIGINAL;
        }
        if (string.startsWith("ftboss:") || collection == null) {
            return FoodWeaponBuildRules.Material.ORIGINAL;
        }
        FoodWeaponBuildRules.Material material = FoodWeaponBuildRules.Material.ORIGINAL;
        for (String string3 : collection) {
            FoodWeaponBuildRules.Material material2;
            if (string3 == null || (material2 = TAGS.get(string3)) == null) continue;
            if (material != FoodWeaponBuildRules.Material.ORIGINAL && material != material2) {
                return FoodWeaponBuildRules.Material.ORIGINAL;
            }
            material = material2;
        }
        return material;
    }

    static {
        LinkedHashMap<FoodWeaponBuildRules.Material, List<String>> linkedHashMap = new LinkedHashMap<FoodWeaponBuildRules.Material, List<String>>(OFFERINGS);
        linkedHashMap.put(FoodWeaponBuildRules.Material.DIAMOND_ALLOY, List.of("kubejs:ne_ingot"));
        OFFERINGS = Map.copyOf(linkedHashMap);
    }

    public static enum Group {
        BASE,
        INGOT,
        BOSS;

    }
}

