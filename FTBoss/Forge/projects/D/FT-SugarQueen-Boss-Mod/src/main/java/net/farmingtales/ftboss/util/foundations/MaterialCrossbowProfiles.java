package net.farmingtales.ftboss.util.foundations;

import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;

public final class MaterialCrossbowProfiles {
    private MaterialCrossbowProfiles() {}

    public static void addTo(Map<String, Profile> profiles) {
        String[] names = {"wooden", "stone", "iron", "golden", "diamond", "netherite"};
        String[] ingredients = {"minecraft:oak_planks", "minecraft:cobblestone", "minecraft:iron_ingot",
                "minecraft:gold_ingot", "minecraft:diamond", "minecraft:netherite_ingot"};
        int[] capacities = {7, 8, 10, 9, 12, 15};
        for (int index = 0; index < names.length; index++) {
            String id = "ftboss:" + names[index] + "_crossbow";
            Map<String, Integer> recipe = new LinkedHashMap<>();
            recipe.put(ingredients[index], 2);
            recipe.put("minecraft:stick", 2);
            recipe.put("minecraft:string", 2);
            recipe.put("minecraft:tripwire_hook", 1);
            Profile profile = FourWeaponFoundationProfiles.create(id, "crossbow", capacities[index], recipe, Map.of(), id);
            if (profiles.putIfAbsent(id, profile) != null) throw new IllegalStateException("Duplicate material crossbow profile: " + id);
        }
    }
}
