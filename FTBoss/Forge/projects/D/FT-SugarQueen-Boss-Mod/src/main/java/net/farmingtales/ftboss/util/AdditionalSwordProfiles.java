package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.util.SwordCompatibilityRules.Definition;
import net.farmingtales.ftboss.util.SwordCompatibilityRules.Form;
import net.farmingtales.ftboss.util.SwordFunctionRules.Input;
import net.farmingtales.ftboss.util.SwordFunctionRules.Profile;
import net.farmingtales.ftboss.util.SwordFunctionRules.Values;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Origin;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;

public final class AdditionalSwordProfiles {
    private static final Map<String, Definition> ADDITIONS = definitions();

    private AdditionalSwordProfiles() {}

    public static void addTo(Map<String, Definition> definitions) {
        ADDITIONS.forEach(definitions::putIfAbsent);
    }

    public static boolean supports(String identifier) {
        return identifier != null && ADDITIONS.containsKey(identifier);
    }

    private static Map<String, Definition> definitions() {
        Map<String, Definition> definitions = new LinkedHashMap<>();
        add(definitions, "bakery:bread_knife", Form.DAGGER, "bakery:bread_knife", 9, List.of("minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:oak_planks"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "farmingtales:kd", Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "ftboss:tide_trident", Form.SPEAR, "ftboss:tide_trident_recast", 11, List.of("ftboss:tide_pearl", "ftboss:tide_pearl", "ftboss:tide_pearl", "minecraft:trident", "minecraft:prismarine_crystals"), List.of("minecraft:prismarine_shard", "minecraft:nautilus_shell", "minecraft:stick", "minecraft:string"));
        add(definitions, "irons_spellbooks:twilight_gale", Form.SWORD, "irons_spellbooks:twilight_gale", 11, List.of("minecraft:quartz", "irons_spellbooks:mithril_ingot", "irons_spellbooks:weapon_parts", "minecraft:quartz", "irons_spellbooks:lightning_bottle"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "kaleidoscope_cookery:diamond_kitchen_knife", Form.DAGGER, "kaleidoscope_cookery:diamond_kitchen_knife", 10, List.of("minecraft:diamond", "minecraft:diamond", "minecraft:diamond", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "kaleidoscope_cookery:gold_kitchen_knife", Form.DAGGER, "kaleidoscope_cookery:gold_kitchen_knife", 10, List.of("minecraft:gold_ingot", "minecraft:gold_ingot", "minecraft:gold_ingot", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "kaleidoscope_cookery:iron_kitchen_knife", Form.DAGGER, "kaleidoscope_cookery:iron_kitchen_knife", 10, List.of("minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "kaleidoscope_cookery:netherite_kitchen_knife", Form.DAGGER, "minecraft:netherite_kitchen_knife_smithing", 9, List.of("kaleidoscope_cookery:diamond_kitchen_knife", "minecraft:netherite_ingot"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "kubejs:copper_sword", Form.SWORD, "zicaiot:tool/copper_sword", 9, List.of("minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "kubejs:ne_sword", Form.SWORD, "zicaiot:duanzao/ne_sword_dz", 9, List.of("minecraft:netherite_sword", "kubejs:ne_ingot"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "kubejs:obsidian_sword", Form.SWORD, "zicaiot:tool/obsidian_sword", 9, List.of("kubejs:obsidian_ingot", "kubejs:obsidian_ingot", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "kubejs:zsj_sword", Form.SWORD, "zicaiot:tool/zsj_sword", 9, List.of("kubejs:zsj_ingot", "kubejs:zsj_ingot", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "legendary_monsters:sharp_sai", Form.DAGGER, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "more_orn_plants:baihua_sword", Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "more_orn_plants:camphor_wooden_sword", Form.SWORD, "more_orn_plants:camphor_wooden_sword", 9, List.of("more_orn_plants:camphor_log", "more_orn_plants:camphor_log", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "more_orn_plants:chinese_parasol_wooden_sword", Form.SWORD, "more_orn_plants:chinese_parasol_wooden_sword", 9, List.of("more_orn_plants:chinese_parasol_log", "more_orn_plants:chinese_parasol_log", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "more_orn_plants:peach_wooden_sword", Form.SWORD, "more_orn_plants:peach_wooden_sword", 9, List.of("more_orn_plants:peach_log", "more_orn_plants:peach_log", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "more_orn_plants:suyu_sword", Form.SWORD, "more_orn_plants:suyu_sword", 9, List.of("more_orn_plants:suyu_bead", "more_orn_plants:suyu_bead", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "more_orn_plants:ziying_sword", Form.SWORD, "more_orn_plants:ziying_sword", 9, List.of("more_orn_plants:ziying_bead", "more_orn_plants:ziying_bead", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "more_orn_plants:ziyu_yuanyang_sword", Form.SWORD, "more_orn_plants:ziyu_yuanyang_sword", 9, List.of("more_orn_plants:ziyu_yuanyang_bead", "more_orn_plants:ziyu_yuanyang_bead", "minecraft:stick"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "rajswordmod:anti_armor_sword", Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "rajswordmod:blood_sword", Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "rajswordmod:deflect_sword", Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "rajswordmod:gaia_sword", Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "rajswordmod:holy_sword", Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "rajswordmod:knockback_sword", Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "rajswordmod:shadow_clone_sword", Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "farmers_spell:gospel", Form.SWORD, "farmers_spell:gospel", 9, List.of("farmersdelight:iron_knife", "irons_spellbooks:divine_pearl", "irons_spellbooks:divine_pearl"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "farmers_spell:hell_knife", Form.DAGGER, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "farmers_spell:grow_knife", Form.DAGGER, "farmers_spell:grow_knife", 11, List.of("minecraft:amethyst_shard", "minecraft:amethyst_shard", "minecraft:amethyst_shard", "farmersdelight:flint_knife", "irons_spellbooks:arcane_essence"), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "farmers_spell:twilight_blade", Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "farmers_spell:boreal_knife", Form.DAGGER, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "farmers_spell:echoing_knife", Form.DAGGER, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "more_orn_plants:caiyun_sword", Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        add(definitions, "more_orn_plants:zhuiyue_sword", Form.SWORD, "", 9, List.of(), List.of("minecraft:stick", "minecraft:string", "minecraft:leather", "minecraft:flint"));
        return Map.copyOf(definitions);
    }

    private static void add(Map<String, Definition> definitions, String id, Form form, String recipe, int capacity,
                            List<String> ingredients, List<String> environment) {
        List<Piece> pieces = new ArrayList<>();
        for (String item : ingredients) pieces.add(new Piece(item, 1, Origin.RECIPE));
        for (String item : environment) pieces.add(new Piece(item, 1, Origin.THEME));
        definitions.put(id, new Definition(id, form, recipe, capacity, pieces));
    }

    public static Profile enrich(Profile original, Definition definition) {
        if (!definition.equals(ADDITIONS.get(definition.weaponId()))) return original;
        Map<String, Values> items = new LinkedHashMap<>(original.itemValues());
        for (Piece piece : definition.budget()) {
            if (items.containsKey(piece.itemId())) continue;
            Values known = SwordCompatibilityRules.itemFunctions(piece.itemId());
            if (known == null) known = SwordCompatibilityRules.materialFunctions(SwordCompatibilityRules.material(piece.itemId()));
            if (known == null || known.equals(SwordFunctionRules.ZERO)) known = new Values(0.7, 0.8, 1.0, 0.9, 1.0);
            items.put(piece.itemId(), known);
        }
        Profile seed = new Profile(SwordFunctionRules.ZERO, original.cellWeights(), original.unlockLevels(),
                original.materials(), original.cellBaseValues(), items);
        List<Input> inputs = new ArrayList<>();
        for (Piece piece : SwordCompatibilityRules.nativeLayout(definition.weaponId(), definition.budget())) {
            inputs.add(new Input(piece.itemId(), SwordCompatibilityRules.material(piece.itemId()), piece.count() > 0));
        }
        Values total = SwordFunctionRules.evaluateItems(seed, inputs, 0).total();
        Values minimum = new Values(total.handle() * 0.995, total.guard() * 0.995, total.core() * 0.995,
                total.stability() * 0.995, total.blade() * 0.995);
        return new Profile(minimum, seed.cellWeights(), seed.unlockLevels(), seed.materials(), seed.cellBaseValues(), items);
    }
}
