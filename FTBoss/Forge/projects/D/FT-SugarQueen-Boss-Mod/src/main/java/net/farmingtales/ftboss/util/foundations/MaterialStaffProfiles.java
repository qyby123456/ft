package net.farmingtales.ftboss.util.foundations;

import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;

public final class MaterialStaffProfiles {
    private MaterialStaffProfiles() {}

    public static void addTo(Map<String, Profile> profiles) {
        String[] names = {"wooden", "stone", "iron", "golden", "diamond", "netherite"};
        String[] ingredients = {"minecraft:oak_planks", "minecraft:cobblestone", "minecraft:iron_ingot",
                "minecraft:gold_ingot", "minecraft:diamond", "minecraft:netherite_ingot"};
        int[] capacities = {6, 7, 9, 8, 11, 14};
        for (int index = 0; index < names.length; index++) {
            String id = "ftboss:" + names[index] + "_staff";
            Map<String, Integer> recipe = new LinkedHashMap<>();
            recipe.put(ingredients[index], 2);
            recipe.put("minecraft:stick", 2);
            recipe.put("minecraft:amethyst_shard", 1);
            recipe.put("irons_spellbooks:arcane_essence", 1);
            Profile profile = FourWeaponFoundationProfiles.create(id, "staff", capacities[index], recipe, Map.of(), id);
            if (profiles.putIfAbsent(id, profile) != null) throw new IllegalStateException("Duplicate material staff profile: " + id);
        }
    }
}
