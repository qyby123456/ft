package net.farmingtales.ftboss.util;

import java.util.List;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;

/** 入门材料羁绊类别；2026-10-25 以两种互斥类别的实装配件提供低强度组合，不放宽高级配方。 */
public final class CommonMaterialBondRules {
    public enum Family {
        METAL("IRON", "COPPER", "GOLD", "NETHERITE", "MITHRIL",
                "WITHERITE", "ENDERITE", "IGNITIUM", "CURSIUM",
                "ANCIENT_METAL", "BLACK_STEEL", "IRON_BARS", "CHAIN",
                "LIGHTNING_ROD", "DIAMOND_ALLOY"),
        STONE("STONE", "OBSIDIAN", "NETHER_FENCE", "STONE_WALL",
                "BRICK_WALL", "BLACKSTONE_WALL", "DEEPSLATE_WALL", "PRISMARINE"),
        CRYSTAL("DIAMOND", "EMERALD", "LAPIS", "QUARTZ",
                "AMETHYST", "ECHO_SHARD"),
        NATURAL("WOOD", "WOOD_FENCE", "BAMBOO_FENCE", "BAMBOO",
                "BOSS_TEA", "BOSS_SPORE", "BOSS_APPLE"),
        FLEXIBLE("BONE", "LEATHER", "SLIME", "HONEYCOMB",
                "BOSS_BUTCHER", "BOSS_WINE", "BOSS_CHEESE", "BOSS_SHEPHERD"),
        ENERGY("BLAZE", "COAL", "REDSTONE", "END_ROD", "GLOWSTONE",
                "BOSS_FROST", "BOSS_CAPACITOR", "BOSS_TIDE");

        private final List<Material> materials;

        Family(String... materialNames) {
            List<String> names = List.of(materialNames);
            this.materials = java.util.Arrays.stream(Material.values()).filter(material -> names.contains(material.name())).toList();
        }

        public List<Material> materials() {
            return materials;
        }
    }

    private CommonMaterialBondRules() {}

    public static List<List<Material>> requirements(String id) {
        return switch (id) {
            case "FORGED_FRAME" -> pair(Family.METAL, Family.STONE);
            case "HONED_INLAY" -> pair(Family.METAL, Family.CRYSTAL);
            case "BALANCED_FRAME" -> pair(Family.METAL, Family.NATURAL);
            case "SUPPLE_BINDING" -> pair(Family.METAL, Family.FLEXIBLE);
            case "CONDUCTIVE_CORE" -> pair(Family.METAL, Family.ENERGY);
            case "MINERAL_RESONANCE" -> pair(Family.STONE, Family.CRYSTAL);
            case "ROOTED_FRAME" -> pair(Family.STONE, Family.NATURAL);
            case "PADDED_GUARD" -> pair(Family.STONE, Family.FLEXIBLE);
            case "LIVING_FOCUS" -> pair(Family.CRYSTAL, Family.NATURAL);
            case "PRISMATIC_CHANNEL" -> pair(Family.CRYSTAL, Family.ENERGY);
            case "FIELD_BINDING" -> pair(Family.NATURAL, Family.FLEXIBLE);
            case "KINDLED_GRIP" -> pair(Family.NATURAL, Family.ENERGY);
            default -> throw new IllegalArgumentException("Unknown common material bond: " + id);
        };
    }

    private static List<List<Material>> pair(Family first, Family second) {
        return List.of(first.materials(), second.materials());
    }
}
