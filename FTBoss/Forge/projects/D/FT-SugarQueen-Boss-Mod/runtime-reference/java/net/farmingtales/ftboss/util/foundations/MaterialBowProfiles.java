/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util.foundations;

import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationProfiles;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;

public final class MaterialBowProfiles {
    private MaterialBowProfiles() {
    }

    public static void addTo(Map<String, FourWeaponFoundationRules.Profile> map) {
        String[] stringArray = new String[]{"wooden", "stone", "iron", "golden", "diamond", "netherite"};
        String[] stringArray2 = new String[]{"minecraft:oak_planks", "minecraft:cobblestone", "minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:diamond", "minecraft:netherite_ingot"};
        int[] nArray = new int[]{6, 7, 9, 8, 11, 14};
        for (int i = 0; i < stringArray.length; ++i) {
            String string = "ftboss:" + stringArray[i] + "_bow";
            LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<String, Integer>();
            linkedHashMap.put(stringArray2[i], 3);
            linkedHashMap.put("minecraft:string", 3);
            FourWeaponFoundationRules.Profile profile = FourWeaponFoundationProfiles.create(string, "bow", nArray[i], linkedHashMap, Map.of(), string);
            if (map.putIfAbsent(string, profile) == null) continue;
            throw new IllegalStateException("Duplicate material bow profile: " + string);
        }
    }
}

