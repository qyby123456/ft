package net.farmingtales.ftboss.util.foundations;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Values;

public final class ManorTridentProfiles {
    private ManorTridentProfiles() {}

    public static void addTo(Map<String, Profile> profiles) {
        Map<String, Integer> recipe = new LinkedHashMap<>();
        recipe.put("tridents_n_stuff:slumbering_trident", 1);
        recipe.put("minecraft:netherite_ingot", 1);
        Map<String, Integer> environment = new LinkedHashMap<>();
        environment.put("minecraft:prismarine_shard", 1);
        environment.put("minecraft:stick", 1);
        Map<String, Values> values = Map.of("tridents_n_stuff:slumbering_trident", new Values(1.2, 1.4, 0.8, 1.6, 1.2));
        for (String name : List.of("stormseeker", "the_desecrator", "echo_of_the_deep", "stellaris", "sanguine_bond")) {
            String id = "tridents_n_stuff:" + name;
            profiles.put(id, FourWeaponFoundationProfiles.create(id, "trident", 13, recipe, environment,
                    id + "_smithing;template-excluded-from-structure", values));
        }
    }
}
