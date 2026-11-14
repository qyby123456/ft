/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util.foundations;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationProfiles;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;

public final class ManorTridentProfiles {
    private ManorTridentProfiles() {
    }

    public static void addTo(Map<String, FourWeaponFoundationRules.Profile> map) {
        LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<String, Integer>();
        linkedHashMap.put("tridents_n_stuff:slumbering_trident", 1);
        linkedHashMap.put("minecraft:netherite_ingot", 1);
        LinkedHashMap<String, Integer> linkedHashMap2 = new LinkedHashMap<String, Integer>();
        linkedHashMap2.put("minecraft:prismarine_shard", 1);
        linkedHashMap2.put("minecraft:stick", 1);
        Map<String, FourWeaponFoundationRules.Values> map2 = Map.of("tridents_n_stuff:slumbering_trident", new FourWeaponFoundationRules.Values(1.2, 1.4, 0.8, 1.6, 1.2));
        for (String string : List.of("stormseeker", "the_desecrator", "echo_of_the_deep", "stellaris", "sanguine_bond")) {
            String string2 = "tridents_n_stuff:" + string;
            map.put(string2, FourWeaponFoundationProfiles.create(string2, "trident", 13, linkedHashMap, linkedHashMap2, string2 + "_smithing;template-excluded-from-structure", map2));
        }
    }
}

