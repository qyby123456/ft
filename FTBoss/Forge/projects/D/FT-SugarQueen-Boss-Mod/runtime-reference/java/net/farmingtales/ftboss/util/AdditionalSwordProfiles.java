/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;

public final class AdditionalSwordProfiles {
    private static final Map<String, SwordCompatibilityRules.Definition> ADDITIONS = AdditionalSwordProfiles.definitions();

    private AdditionalSwordProfiles() {
    }

    public static void addTo(Map<String, SwordCompatibilityRules.Definition> map) {
        ADDITIONS.forEach(map::putIfAbsent);
    }

    public static boolean supports(String string) {
        return string != null && ADDITIONS.containsKey(string);
    }

    private static Map<String, SwordCompatibilityRules.Definition> definitions() {
        LinkedHashMap<String, SwordCompatibilityRules.Definition> linkedHashMap = new LinkedHashMap<String, SwordCompatibilityRules.Definition>();
        AdditionalSwordProfiles.add(linkedHashMap, "bakery:bread_knife", SwordCompatibilityRules.Form.DAGGER, "bakery:bread_knife", 9, List.of("minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:oak_planks"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "farmingtales:kd", SwordCompatibilityRules.Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "ftboss:tide_trident", SwordCompatibilityRules.Form.SPEAR, "ftboss:tide_trident_recast", 11, List.of("ftboss:tide_pearl", "ftboss:tide_pearl", "ftboss:tide_pearl", "minecraft:trident", "minecraft:prismarine_crystals"), List.of("minecraft:prismarine_shard", "minecraft:nautilus_shell", "minecraft:stick", "minecraft:string"));
        AdditionalSwordProfiles.add(linkedHashMap, "irons_spellbooks:twilight_gale", SwordCompatibilityRules.Form.SWORD, "irons_spellbooks:twilight_gale", 11, List.of("minecraft:quartz", "irons_spellbooks:mithril_ingot", "irons_spellbooks:weapon_parts", "minecraft:quartz", "irons_spellbooks:lightning_bottle"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "kaleidoscope_cookery:diamond_kitchen_knife", SwordCompatibilityRules.Form.DAGGER, "kaleidoscope_cookery:diamond_kitchen_knife", 10, List.of("minecraft:diamond", "minecraft:diamond", "minecraft:diamond", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "kaleidoscope_cookery:gold_kitchen_knife", SwordCompatibilityRules.Form.DAGGER, "kaleidoscope_cookery:gold_kitchen_knife", 10, List.of("minecraft:gold_ingot", "minecraft:gold_ingot", "minecraft:gold_ingot", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "kaleidoscope_cookery:iron_kitchen_knife", SwordCompatibilityRules.Form.DAGGER, "kaleidoscope_cookery:iron_kitchen_knife", 10, List.of("minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "kaleidoscope_cookery:netherite_kitchen_knife", SwordCompatibilityRules.Form.DAGGER, "minecraft:netherite_kitchen_knife_smithing", 9, List.of("kaleidoscope_cookery:diamond_kitchen_knife", "minecraft:netherite_ingot"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "kubejs:copper_sword", SwordCompatibilityRules.Form.SWORD, "zicaiot:tool/copper_sword", 9, List.of("minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "kubejs:ne_sword", SwordCompatibilityRules.Form.SWORD, "zicaiot:duanzao/ne_sword_dz", 9, List.of("minecraft:netherite_sword", "kubejs:ne_ingot"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "kubejs:obsidian_sword", SwordCompatibilityRules.Form.SWORD, "zicaiot:tool/obsidian_sword", 9, List.of("kubejs:obsidian_ingot", "kubejs:obsidian_ingot", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "kubejs:zsj_sword", SwordCompatibilityRules.Form.SWORD, "zicaiot:tool/zsj_sword", 9, List.of("kubejs:zsj_ingot", "kubejs:zsj_ingot", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "legendary_monsters:sharp_sai", SwordCompatibilityRules.Form.DAGGER, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "more_orn_plants:baihua_sword", SwordCompatibilityRules.Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "more_orn_plants:camphor_wooden_sword", SwordCompatibilityRules.Form.SWORD, "more_orn_plants:camphor_wooden_sword", 9, List.of("more_orn_plants:camphor_log", "more_orn_plants:camphor_log", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "more_orn_plants:chinese_parasol_wooden_sword", SwordCompatibilityRules.Form.SWORD, "more_orn_plants:chinese_parasol_wooden_sword", 9, List.of("more_orn_plants:chinese_parasol_log", "more_orn_plants:chinese_parasol_log", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "more_orn_plants:peach_wooden_sword", SwordCompatibilityRules.Form.SWORD, "more_orn_plants:peach_wooden_sword", 9, List.of("more_orn_plants:peach_log", "more_orn_plants:peach_log", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "more_orn_plants:suyu_sword", SwordCompatibilityRules.Form.SWORD, "more_orn_plants:suyu_sword", 9, List.of("more_orn_plants:suyu_bead", "more_orn_plants:suyu_bead", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "more_orn_plants:ziying_sword", SwordCompatibilityRules.Form.SWORD, "more_orn_plants:ziying_sword", 9, List.of("more_orn_plants:ziying_bead", "more_orn_plants:ziying_bead", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "more_orn_plants:ziyu_yuanyang_sword", SwordCompatibilityRules.Form.SWORD, "more_orn_plants:ziyu_yuanyang_sword", 9, List.of("more_orn_plants:ziyu_yuanyang_bead", "more_orn_plants:ziyu_yuanyang_bead", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "rajswordmod:anti_armor_sword", SwordCompatibilityRules.Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "rajswordmod:blood_sword", SwordCompatibilityRules.Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "rajswordmod:deflect_sword", SwordCompatibilityRules.Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "rajswordmod:gaia_sword", SwordCompatibilityRules.Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "rajswordmod:holy_sword", SwordCompatibilityRules.Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "rajswordmod:knockback_sword", SwordCompatibilityRules.Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "rajswordmod:shadow_clone_sword", SwordCompatibilityRules.Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "farmers_spell:gospel", SwordCompatibilityRules.Form.SWORD, "farmers_spell:gospel", 9, List.of("farmersdelight:iron_knife", "irons_spellbooks:divine_pearl", "irons_spellbooks:divine_pearl"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "farmers_spell:hell_knife", SwordCompatibilityRules.Form.DAGGER, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "farmers_spell:grow_knife", SwordCompatibilityRules.Form.DAGGER, "farmers_spell:grow_knife", 11, List.of("minecraft:amethyst_shard", "minecraft:amethyst_shard", "minecraft:amethyst_shard", "farmersdelight:flint_knife", "irons_spellbooks:arcane_essence"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "farmers_spell:twilight_blade", SwordCompatibilityRules.Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "farmers_spell:boreal_knife", SwordCompatibilityRules.Form.DAGGER, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "farmers_spell:echoing_knife", SwordCompatibilityRules.Form.DAGGER, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "more_orn_plants:caiyun_sword", SwordCompatibilityRules.Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        AdditionalSwordProfiles.add(linkedHashMap, "more_orn_plants:zhuiyue_sword", SwordCompatibilityRules.Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        return Map.copyOf(linkedHashMap);
    }

    private static void add(Map<String, SwordCompatibilityRules.Definition> map, String string, SwordCompatibilityRules.Form form, String string2, int n, List<String> list, List<String> list2) {
        ArrayList<SwordNativeMaterialRules.Piece> arrayList = new ArrayList<SwordNativeMaterialRules.Piece>();
        for (String string3 : list) {
            arrayList.add(new SwordNativeMaterialRules.Piece(string3, 1, SwordNativeMaterialRules.Origin.RECIPE));
        }
        for (String string3 : list2) {
            arrayList.add(new SwordNativeMaterialRules.Piece(string3, 1, SwordNativeMaterialRules.Origin.THEME));
        }
        map.put(string, new SwordCompatibilityRules.Definition(string, form, string2, n, arrayList));
    }

    public static SwordFunctionRules.Profile enrich(SwordFunctionRules.Profile profile, SwordCompatibilityRules.Definition definition) {
        Record record2;
        Object object;
        if (!definition.equals(ADDITIONS.get(definition.weaponId()))) {
            return profile;
        }
        LinkedHashMap<String, SwordFunctionRules.Values> linkedHashMap = new LinkedHashMap<String, SwordFunctionRules.Values>(profile.itemValues());
        for (SwordNativeMaterialRules.Piece object22 : definition.budget()) {
            if (linkedHashMap.containsKey(object22.itemId())) continue;
            object = SwordCompatibilityRules.itemFunctions(object22.itemId());
            if (object == null) {
                object = SwordCompatibilityRules.materialFunctions(SwordCompatibilityRules.material(object22.itemId()));
            }
            if (object == null || ((SwordFunctionRules.Values)object).equals(SwordFunctionRules.ZERO)) {
                object = new SwordFunctionRules.Values(0.7, 0.8, 1.0, 0.9, 1.0);
            }
            linkedHashMap.put(object22.itemId(), (SwordFunctionRules.Values)object);
        }
        SwordFunctionRules.Profile profile2 = new SwordFunctionRules.Profile(SwordFunctionRules.ZERO, profile.cellWeights(), profile.unlockLevels(), profile.materials(), profile.cellBaseValues(), linkedHashMap);
        ArrayList<SwordFunctionRules.Input> arrayList = new ArrayList<SwordFunctionRules.Input>();
        for (Record record2 : SwordCompatibilityRules.nativeLayout(definition.weaponId(), definition.budget())) {
            arrayList.add(new SwordFunctionRules.Input(record2.itemId(), SwordCompatibilityRules.material(record2.itemId()), record2.count() > 0));
        }
        object = SwordFunctionRules.evaluateItems(profile2, arrayList, 0).total();
        record2 = new SwordFunctionRules.Values(((SwordFunctionRules.Values)object).handle() * 0.995, ((SwordFunctionRules.Values)object).guard() * 0.995, ((SwordFunctionRules.Values)object).core() * 0.995, ((SwordFunctionRules.Values)object).stability() * 0.995, ((SwordFunctionRules.Values)object).blade() * 0.995);
        return new SwordFunctionRules.Profile((SwordFunctionRules.Values)record2, profile2.cellWeights(), profile2.unlockLevels(), profile2.materials(), profile2.cellBaseValues(), linkedHashMap);
    }
}

