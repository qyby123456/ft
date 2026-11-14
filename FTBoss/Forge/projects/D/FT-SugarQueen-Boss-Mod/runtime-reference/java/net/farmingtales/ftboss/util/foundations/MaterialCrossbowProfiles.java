/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util.foundations;

import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationProfiles;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;

public final class MaterialCrossbowProfiles {
    private MaterialCrossbowProfiles() {
    }

    public static void addTo(Map<String, FourWeaponFoundationRules.Profile> map) {
        String[] stringArray = new String[]{"wooden", "stone", "iron", "golden", "diamond", "netherite"};
        String[] stringArray2 = new String[]{"minecraft:oak_planks", "minecraft:cobblestone", "minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:diamond", "minecraft:netherite_ingot"};
        int[] nArray = new int[]{7, 8, 10, 9, 12, 15};
        for (int i = 0; i < stringArray.length; ++i) {
            String string = "ftboss:" + stringArray[i] + "_crossbow";
            LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<String, Integer>();
            linkedHashMap.put(stringArray2[i], 2);
            linkedHashMap.put("minecraft:stick", 2);
            linkedHashMap.put("minecraft:string", 2);
            linkedHashMap.put("minecraft:tripwire_hook", 1);
            FourWeaponFoundationRules.Profile profile = FourWeaponFoundationProfiles.create(string, "crossbow", nArray[i], linkedHashMap, Map.of(), string);
            if (map.putIfAbsent(string, profile) == null) continue;
            throw new IllegalStateException("Duplicate material crossbow profile: " + string);
        }
    }
}

