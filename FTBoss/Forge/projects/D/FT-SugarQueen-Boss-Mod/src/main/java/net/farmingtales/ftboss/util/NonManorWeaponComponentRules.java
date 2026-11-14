package net.farmingtales.ftboss.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.util.BossRelicRules.Kind;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Origin;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;

/** 非庄园十王武器的原生配件规则；遗物、专属配件、一镶钻合金、二下界合金与对应食材共用单件账本。 */
public final class NonManorWeaponComponentRules {
    public static final int INITIAL_MELEE_CELLS = 13;
    private static final Map<String, Kind> WEAPONS = weapons();
    private static final Map<Kind, String> FOODS = Map.of(
            Kind.FROST, "minecraft:sugar", Kind.BUTCHER, "minecraft:cooked_beef",
            Kind.TEA, "herbalbrews:green_tea", Kind.WINE, "vinery:clark_wine",
            Kind.RAT, "brewinandchewin:flaxen_cheese_wheel", Kind.DEFECT, "minecraft:cocoa_beans",
            Kind.COMPOST, "minecraft:carrot", Kind.APPLE, "minecraft:apple",
            Kind.BENGER, "minecraft:cooked_beef", Kind.TIDE, "minecraft:cooked_cod");

    private NonManorWeaponComponentRules() {}

    public static Map<String, Kind> weaponsById() { return WEAPONS; }

    public static Kind kind(String weaponId) { return weaponId == null ? null : WEAPONS.get(weaponId); }

    public static String foodId(Kind kind) { return kind == null ? "" : FOODS.get(kind); }

    public static List<Piece> budget(Kind kind) {
        if (kind == null) return List.of();
        return List.of(piece("ftboss:" + kind.item), piece(WeaponBossMaterialRules.componentId(kind)),
                piece(WeaponDurabilityRules.ALLOY_ITEM), piece("minecraft:netherite_ingot"),
                piece("minecraft:netherite_ingot"), piece(foodId(kind)));
    }

    public static List<Piece> budget(String weaponId) { return budget(kind(weaponId)); }

    public static Map<String, Integer> componentCounts(String weaponId) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Piece piece : budget(weaponId)) result.merge(piece.itemId(), piece.count(), Integer::sum);
        return Collections.unmodifiableMap(result);
    }

    public static Material material(String itemId) {
        if (itemId == null) return Material.ORIGINAL;
        if (WeaponDurabilityRules.ALLOY_ITEM.equals(itemId)) return Material.DIAMOND_ALLOY;
        if ("minecraft:netherite_ingot".equals(itemId)) return Material.NETHERITE;
        for (Kind kind : Kind.values()) {
            if (("ftboss:" + kind.item).equals(itemId) || WeaponBossMaterialRules.componentId(kind).equals(itemId)) {
                return WeaponBossMaterialRules.material(kind);
            }
            if (foodId(kind).equals(itemId)) return Material.WOOD;
        }
        return Material.ORIGINAL;
    }

    private static Piece piece(String itemId) { return new Piece(itemId, 1, Origin.THEME); }

    private static Map<String, Kind> weapons() {
        Map<String, Kind> result = new LinkedHashMap<>();
        result.put("ftboss:candy_cane", Kind.FROST);
        result.put("ftboss:frost_queen_staff", Kind.FROST);
        result.put("ftboss:turkey_legs", Kind.BUTCHER);
        result.put("ftboss:blooming_thorns", Kind.TEA);
        result.put("ftboss:wine_bottle_mace", Kind.WINE);
        result.put("ftboss:barrel_crossbow", Kind.WINE);
        result.put("ftboss:cheese_wheel_mace", Kind.RAT);
        result.put("ftboss:defect_claw", Kind.DEFECT);
        result.put("ftboss:compost_crown", Kind.COMPOST);
        result.put("ftboss:apple_stem", Kind.APPLE);
        result.put("ftboss:tide_trident", Kind.TIDE);
        result.put("ftboss:dog_beating_stick", Kind.BENGER);
        return Collections.unmodifiableMap(result);
    }
}
