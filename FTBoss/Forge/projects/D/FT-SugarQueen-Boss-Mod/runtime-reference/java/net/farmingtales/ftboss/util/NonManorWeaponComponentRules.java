/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.util.BossRelicRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.farmingtales.ftboss.util.WeaponBossMaterialRules;

public final class NonManorWeaponComponentRules {
    public static final int INITIAL_MELEE_CELLS = 13;
    private static final Map<String, BossRelicRules.Kind> WEAPONS = NonManorWeaponComponentRules.weapons();
    private static final Map<BossRelicRules.Kind, String> FOODS = Map.of(BossRelicRules.Kind.FROST, "minecraft:sugar", BossRelicRules.Kind.BUTCHER, "minecraft:cooked_beef", BossRelicRules.Kind.TEA, "herbalbrews:green_tea", BossRelicRules.Kind.WINE, "vinery:clark_wine", BossRelicRules.Kind.RAT, "brewinandchewin:flaxen_cheese_wheel", BossRelicRules.Kind.DEFECT, "minecraft:cocoa_beans", BossRelicRules.Kind.COMPOST, "minecraft:carrot", BossRelicRules.Kind.APPLE, "minecraft:apple", BossRelicRules.Kind.BENGER, "minecraft:cooked_beef", BossRelicRules.Kind.TIDE, "minecraft:cooked_cod");

    private NonManorWeaponComponentRules() {
    }

    public static Map<String, BossRelicRules.Kind> weaponsById() {
        return WEAPONS;
    }

    public static BossRelicRules.Kind kind(String string) {
        return string == null ? null : WEAPONS.get(string);
    }

    public static String foodId(BossRelicRules.Kind kind) {
        return kind == null ? "" : FOODS.get((Object)kind);
    }

    public static List<SwordNativeMaterialRules.Piece> budget(BossRelicRules.Kind kind) {
        if (kind == null) {
            return List.of();
        }
        return List.of(NonManorWeaponComponentRules.piece("ftboss:" + kind.item), NonManorWeaponComponentRules.piece(WeaponBossMaterialRules.componentId(kind)), NonManorWeaponComponentRules.piece("kubejs:ne_ingot"), NonManorWeaponComponentRules.piece("minecraft:netherite_ingot"), NonManorWeaponComponentRules.piece("minecraft:netherite_ingot"), NonManorWeaponComponentRules.piece(NonManorWeaponComponentRules.foodId(kind)));
    }

    public static List<SwordNativeMaterialRules.Piece> budget(String string) {
        return NonManorWeaponComponentRules.budget(NonManorWeaponComponentRules.kind(string));
    }

    public static Map<String, Integer> componentCounts(String string) {
        LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<String, Integer>();
        for (SwordNativeMaterialRules.Piece piece : NonManorWeaponComponentRules.budget(string)) {
            linkedHashMap.merge(piece.itemId(), piece.count(), Integer::sum);
        }
        return Collections.unmodifiableMap(linkedHashMap);
    }

    public static FoodWeaponBuildRules.Material material(String string) {
        if (string == null) {
            return FoodWeaponBuildRules.Material.ORIGINAL;
        }
        if ("kubejs:ne_ingot".equals(string)) {
            return FoodWeaponBuildRules.Material.DIAMOND_ALLOY;
        }
        if ("minecraft:netherite_ingot".equals(string)) {
            return FoodWeaponBuildRules.Material.NETHERITE;
        }
        for (BossRelicRules.Kind kind : BossRelicRules.Kind.values()) {
            if (("ftboss:" + kind.item).equals(string) || WeaponBossMaterialRules.componentId(kind).equals(string)) {
                return WeaponBossMaterialRules.material(kind);
            }
            if (!NonManorWeaponComponentRules.foodId(kind).equals(string)) continue;
            return FoodWeaponBuildRules.Material.WOOD;
        }
        return FoodWeaponBuildRules.Material.ORIGINAL;
    }

    private static SwordNativeMaterialRules.Piece piece(String string) {
        return new SwordNativeMaterialRules.Piece(string, 1, SwordNativeMaterialRules.Origin.THEME);
    }

    private static Map<String, BossRelicRules.Kind> weapons() {
        LinkedHashMap<String, BossRelicRules.Kind> linkedHashMap = new LinkedHashMap<String, BossRelicRules.Kind>();
        linkedHashMap.put("ftboss:candy_cane", BossRelicRules.Kind.FROST);
        linkedHashMap.put("ftboss:frost_queen_staff", BossRelicRules.Kind.FROST);
        linkedHashMap.put("ftboss:turkey_legs", BossRelicRules.Kind.BUTCHER);
        linkedHashMap.put("ftboss:blooming_thorns", BossRelicRules.Kind.TEA);
        linkedHashMap.put("ftboss:wine_bottle_mace", BossRelicRules.Kind.WINE);
        linkedHashMap.put("ftboss:barrel_crossbow", BossRelicRules.Kind.WINE);
        linkedHashMap.put("ftboss:cheese_wheel_mace", BossRelicRules.Kind.RAT);
        linkedHashMap.put("ftboss:defect_claw", BossRelicRules.Kind.DEFECT);
        linkedHashMap.put("ftboss:compost_crown", BossRelicRules.Kind.COMPOST);
        linkedHashMap.put("ftboss:apple_stem", BossRelicRules.Kind.APPLE);
        linkedHashMap.put("ftboss:tide_trident", BossRelicRules.Kind.TIDE);
        linkedHashMap.put("ftboss:dog_beating_stick", BossRelicRules.Kind.BENGER);
        return Collections.unmodifiableMap(linkedHashMap);
    }
}

