package net.farmingtales.ftboss.util.foundations;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Values;

public final class AdditionalFoundationProfiles {
    private AdditionalFoundationProfiles() {}

    public static void addTo(Map<String, Profile> profiles) {
        add(profiles, "ftboss:qingyi_confluence_trident", "trident", 11, "ftboss:qingyi_confluence_trident", List.of("tridents_n_stuff:stormseeker", "tridents_n_stuff:the_desecrator", "tridents_n_stuff:echo_of_the_deep", "tridents_n_stuff:stellaris", "minecraft:nether_star"), List.of("minecraft:prismarine_shard", "minecraft:nautilus_shell", "minecraft:stick", "minecraft:string"));
        add(profiles, "kubejs:copper_axe", "axe", 11, "zicaiot:tool/copper_axe", List.of("minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:stick", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(profiles, "kubejs:ne_axe", "axe", 9, "zicaiot:duanzao/ne_axe_dz", List.of("minecraft:netherite_axe", "kubejs:ne_ingot"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(profiles, "kubejs:obsidian_axe", "axe", 11, "zicaiot:tool/obsidian_axe", List.of("kubejs:obsidian_ingot", "kubejs:obsidian_ingot", "kubejs:obsidian_ingot", "minecraft:stick", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(profiles, "kubejs:zsj_axe", "axe", 11, "zicaiot:tool/zsj_axe", List.of("kubejs:zsj_ingot", "kubejs:zsj_ingot", "kubejs:zsj_ingot", "minecraft:stick", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(profiles, "legendary_monsters:axe_of_lightning", "axe", 12, "legendary_monsters:axe_of_lightning", List.of("minecraft:iron_ingot", "legendary_monsters:air_rune", "minecraft:iron_ingot", "minecraft:stick", "minecraft:iron_ingot", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(profiles, "legendary_monsters:enderitium_axe", "axe", 9, "legendary_monsters:enderitium_axe", List.of("minecraft:diamond_axe", "legendary_monsters:enderitium_ingot"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(profiles, "legendary_monsters:fiery_jaw", "axe", 15, "legendary_monsters:fiery_jaw", List.of("minecraft:blackstone", "minecraft:blackstone", "minecraft:blackstone", "legendary_monsters:lava_eaters_skin", "legendary_monsters:lava_eaters_skin", "legendary_monsters:lava_eaters_skin", "minecraft:iron_ingot", "minecraft:magma_block", "minecraft:iron_ingot"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(profiles, "legendary_monsters:golden_halbert", "axe", 12, "legendary_monsters:golden_halbert", List.of("minecraft:gold_block", "minecraft:gold_ingot", "minecraft:gold_ingot", "legendary_monsters:long_stick_half", "minecraft:gold_ingot", "legendary_monsters:long_stick_half"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(profiles, "legendary_monsters:sharp_battle_axe", "axe", 9, "NONE:installed-no-output-recipe;explicit-themed-reference", List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(profiles, "more_orn_plants:suyu_axe", "axe", 11, "more_orn_plants:suyu_axe", List.of("more_orn_plants:suyu_bead", "more_orn_plants:suyu_bead", "more_orn_plants:suyu_bead", "minecraft:stick", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(profiles, "more_orn_plants:ziying_axe", "axe", 11, "more_orn_plants:ziying_axe", List.of("more_orn_plants:ziying_bead", "more_orn_plants:ziying_bead", "more_orn_plants:ziying_bead", "minecraft:stick", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(profiles, "more_orn_plants:ziyu_yuanyang_axe", "axe", 11, "more_orn_plants:ziyu_yuanyang_axe", List.of("more_orn_plants:ziyu_yuanyang_bead", "more_orn_plants:ziyu_yuanyang_bead", "more_orn_plants:ziyu_yuanyang_bead", "minecraft:stick", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(profiles, "tridents_n_stuff:slumbering_trident", "trident", 9, "NONE:installed-no-output-recipe;explicit-themed-reference", List.of(), List.of("minecraft:prismarine_shard", "minecraft:nautilus_shell", "minecraft:stick", "minecraft:string"));
        add(profiles, "farmers_spell:cherry_spoon", "staff", 13, "farmers_spell:cherry_spoon", List.of("minecraft:amethyst_shard", "minecraft:cherry_log", "farmersdelight:rope", "minecraft:cherry_log", "minecraft:amethyst_shard", "minecraft:cherry_log", "farmersdelight:rope"), List.of("minecraft:amethyst_shard", "minecraft:quartz", "minecraft:stick", "minecraft:string"));
        add(profiles, "farmers_spell:iris_fork", "staff", 13, "farmers_spell:iris_fork", List.of("farmers_spell:glutton_rune", "irons_spellbooks:mithril_ingot", "irons_spellbooks:arcane_ingot", "irons_spellbooks:mithril_ingot", "farmers_spell:glutton_rune", "irons_spellbooks:mithril_ingot", "irons_spellbooks:arcane_ingot"), List.of("minecraft:amethyst_shard", "minecraft:quartz", "minecraft:stick", "minecraft:string"));
    }

    private static void add(Map<String, Profile> profiles, String id, String family, int capacity, String evidence,
                            List<String> ingredients, List<String> environment) {
        if (profiles.containsKey(id)) return;
        Map<String, Values> values = new LinkedHashMap<>();
        for (List<String> components : List.of(ingredients, environment)) {
            for (String item : components) {
                Material material = SwordCompatibilityRules.material(item);
                if (material == Material.ORIGINAL) material = Material.WOOD;
                values.put(item, FourWeaponFoundationProfiles.materialValues(family, material));
            }
        }
        profiles.put(id, FourWeaponFoundationProfiles.create(id, family, capacity, counts(ingredients), counts(environment), evidence, values));
    }

    private static Map<String, Integer> counts(List<String> items) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String item : items) result.merge(item, 1, Integer::sum);
        return result;
    }
}
