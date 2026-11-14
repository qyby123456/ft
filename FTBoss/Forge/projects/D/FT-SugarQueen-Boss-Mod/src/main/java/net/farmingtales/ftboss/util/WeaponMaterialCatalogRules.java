package net.farmingtales.ftboss.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;

/** 构筑原料白名单；35 类材料、Boss 独立掉落及原版/灾变锭，2026-10-12 移除多余合金并限定铁魔法秘银。 */
public final class WeaponMaterialCatalogRules {
    public enum Group { BASE, INGOT, BOSS }

    private static final Pattern IDENTIFIER = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");
    private static final Set<String> RETIRED = Set.of("zinc_ingot", "brass_ingot", "electrum_ingot", "pyrium_ingot", "mithril_ingot");
    private static final Map<Material, List<String>> OFFERINGS = Map.ofEntries(
            Map.entry(Material.WOOD, List.of("minecraft:stick")),
            Map.entry(Material.STONE, List.of("minecraft:cobblestone", "minecraft:stone", "minecraft:blackstone")),
            Map.entry(Material.IRON, List.of("minecraft:iron_ingot")),
            Map.entry(Material.COPPER, List.of("minecraft:copper_ingot")),
            Map.entry(Material.GOLD, List.of("minecraft:gold_ingot")),
            Map.entry(Material.DIAMOND, List.of("minecraft:diamond")),
            Map.entry(Material.NETHERITE, List.of("minecraft:netherite_ingot")),
            Map.entry(Material.DIAMOND_ALLOY, List.of(WeaponDurabilityRules.ALLOY_ITEM)),
            Map.entry(Material.BONE, List.of("minecraft:bone")),
            Map.entry(Material.OBSIDIAN, List.of("minecraft:obsidian")),
            Map.entry(Material.LEATHER, List.of("minecraft:leather")),
            Map.entry(Material.BLAZE, List.of("minecraft:blaze_rod")),
            Map.entry(Material.EMERALD, List.of("minecraft:emerald")),
            Map.entry(Material.MITHRIL, List.of("irons_spellbooks:mithril_ingot")),
            Map.entry(Material.COAL, List.of("minecraft:coal")),
            Map.entry(Material.LAPIS, List.of("minecraft:lapis_lazuli")),
            Map.entry(Material.REDSTONE, List.of("minecraft:redstone")),
            Map.entry(Material.QUARTZ, List.of("minecraft:quartz")),
            Map.entry(Material.AMETHYST, List.of("minecraft:amethyst_shard")),
            Map.entry(Material.WITHERITE, List.of("cataclysm:witherite_ingot")),
            Map.entry(Material.ENDERITE, List.of("cataclysm:enderite_ingot")),
            Map.entry(Material.IGNITIUM, List.of("cataclysm:ignitium_ingot")),
            Map.entry(Material.CURSIUM, List.of("cataclysm:cursium_ingot")),
            Map.entry(Material.ANCIENT_METAL, List.of("cataclysm:ancient_metal_ingot")),
            Map.entry(Material.BLACK_STEEL, List.of("cataclysm:black_steel_ingot")),
            Map.entry(Material.BOSS_FROST, List.of("ftboss:frost_material")),
            Map.entry(Material.BOSS_BUTCHER, List.of("ftboss:butcher_material")),
            Map.entry(Material.BOSS_TEA, List.of("ftboss:tea_material")),
            Map.entry(Material.BOSS_WINE, List.of("ftboss:wine_material")),
            Map.entry(Material.BOSS_CHEESE, List.of("ftboss:cheese_material")),
            Map.entry(Material.BOSS_CAPACITOR, List.of("ftboss:capacitor_material")),
            Map.entry(Material.BOSS_SPORE, List.of("ftboss:spore_material")),
            Map.entry(Material.BOSS_APPLE, List.of("ftboss:apple_material")),
            Map.entry(Material.BOSS_SHEPHERD, List.of("ftboss:shepherd_material")),
            Map.entry(Material.BOSS_TIDE, List.of("ftboss:tide_material", "ftboss:tide_pearl")),
            Map.entry(Material.END_ROD, List.of("minecraft:end_rod")),
            Map.entry(Material.IRON_BARS, List.of("minecraft:iron_bars")),
            Map.entry(Material.WOOD_FENCE, List.of("minecraft:oak_fence", "minecraft:spruce_fence", "minecraft:birch_fence",
                    "minecraft:jungle_fence", "minecraft:acacia_fence", "minecraft:dark_oak_fence", "minecraft:mangrove_fence",
                    "minecraft:cherry_fence", "minecraft:crimson_fence", "minecraft:warped_fence")),
            Map.entry(Material.BAMBOO_FENCE, List.of("minecraft:bamboo_fence")),
            Map.entry(Material.NETHER_FENCE, List.of("minecraft:nether_brick_fence")),
            Map.entry(Material.STONE_WALL, List.of("minecraft:cobblestone_wall", "minecraft:mossy_cobblestone_wall",
                    "minecraft:granite_wall", "minecraft:diorite_wall", "minecraft:andesite_wall", "minecraft:sandstone_wall",
                    "minecraft:red_sandstone_wall", "minecraft:stone_brick_wall", "minecraft:mossy_stone_brick_wall")),
            Map.entry(Material.BRICK_WALL, List.of("minecraft:brick_wall", "minecraft:mud_brick_wall", "minecraft:nether_brick_wall",
                    "minecraft:red_nether_brick_wall", "minecraft:end_stone_brick_wall")),
            Map.entry(Material.BLACKSTONE_WALL, List.of("minecraft:blackstone_wall", "minecraft:polished_blackstone_wall",
                    "minecraft:polished_blackstone_brick_wall")),
            Map.entry(Material.DEEPSLATE_WALL, List.of("minecraft:cobbled_deepslate_wall", "minecraft:polished_deepslate_wall",
                    "minecraft:deepslate_brick_wall", "minecraft:deepslate_tile_wall")),
            Map.entry(Material.CHAIN, List.of("minecraft:chain")),
            Map.entry(Material.BAMBOO, List.of("minecraft:bamboo")),
            Map.entry(Material.LIGHTNING_ROD, List.of("minecraft:lightning_rod")),
            Map.entry(Material.SLIME, List.of("minecraft:slime_ball")),
            Map.entry(Material.HONEYCOMB, List.of("minecraft:honeycomb")),
            Map.entry(Material.PRISMARINE, List.of("minecraft:prismarine_shard", "minecraft:prismarine_crystals", "minecraft:prismarine_wall")),
            Map.entry(Material.GLOWSTONE, List.of("minecraft:glowstone_dust")),
            Map.entry(Material.ECHO_SHARD, List.of("minecraft:echo_shard")));
    private static final Map<String, Material> TAGS = Map.ofEntries(
            Map.entry("minecraft:planks", Material.WOOD),
            Map.entry("minecraft:logs", Material.WOOD),
            Map.entry("minecraft:wooden_fences", Material.WOOD_FENCE),
            Map.entry("minecraft:walls", Material.STONE_WALL),
            Map.entry("forge:ingots/iron", Material.IRON),
            Map.entry("forge:ingots/copper", Material.COPPER),
            Map.entry("forge:ingots/gold", Material.GOLD),
            Map.entry("forge:ingots/netherite", Material.NETHERITE),
            Map.entry("forge:gems/diamond", Material.DIAMOND),
            Map.entry("forge:gems/emerald", Material.EMERALD));

    private WeaponMaterialCatalogRules() {}

    public static List<String> offerings(Material material) {
        return material == null ? List.of() : OFFERINGS.getOrDefault(material, List.of());
    }

    public static Group group(Material material) {
        if (material == null) return Group.BASE;
        if (boss(material)) return Group.BOSS;
        return switch (material) {
            case IRON, COPPER, GOLD, NETHERITE, DIAMOND_ALLOY, MITHRIL, WITHERITE, ENDERITE, IGNITIUM, CURSIUM, ANCIENT_METAL, BLACK_STEEL -> Group.INGOT;
            default -> Group.BASE;
        };
    }

    public static boolean boss(Material material) {
        return material != null && material.id().startsWith("boss_");
    }

    public static Material resolve(String itemId, Collection<String> itemTags) {
        if (itemId == null || !IDENTIFIER.matcher(itemId).matches()) return Material.ORIGINAL;
        for (Material material : Material.values()) {
            if (offerings(material).contains(itemId)) return material;
        }
        String name = itemId.substring(itemId.indexOf(':') + 1);
        if (RETIRED.contains(name) || name.startsWith("raw_") || name.endsWith("_nugget")
                || name.endsWith("_ore") || name.endsWith("_block")) return Material.ORIGINAL;
        if (itemId.startsWith("ftboss:") || itemTags == null) return Material.ORIGINAL;
        Material result = Material.ORIGINAL;
        for (String tag : itemTags) {
            if (tag == null) continue;
            Material candidate = TAGS.get(tag);
            if (candidate == null) continue;
            if (result != Material.ORIGINAL && result != candidate) return Material.ORIGINAL;
            result = candidate;
        }
        return result;
    }
}
