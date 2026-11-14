package net.farmingtales.ftboss.item;

import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class AdditionalWeaponUiEligibility {
    private static final Set<String> WEAPONS = Set.of(
            "bakery:bread_knife",
            "farmingtales:kd",
            "ftboss:qingyi_confluence_trident",
            "ftboss:tide_trident",
            "irons_spellbooks:twilight_gale",
            "kaleidoscope_cookery:diamond_kitchen_knife",
            "kaleidoscope_cookery:gold_kitchen_knife",
            "kaleidoscope_cookery:iron_kitchen_knife",
            "kaleidoscope_cookery:netherite_kitchen_knife",
            "kubejs:copper_axe",
            "kubejs:copper_sword",
            "kubejs:ne_axe",
            "kubejs:ne_sword",
            "kubejs:obsidian_axe",
            "kubejs:obsidian_sword",
            "kubejs:zsj_axe",
            "kubejs:zsj_sword",
            "legendary_monsters:axe_of_lightning",
            "legendary_monsters:enderitium_axe",
            "legendary_monsters:fiery_jaw",
            "legendary_monsters:golden_halbert",
            "legendary_monsters:sharp_battle_axe",
            "legendary_monsters:sharp_sai",
            "more_orn_plants:baihua_sword",
            "more_orn_plants:camphor_wooden_sword",
            "more_orn_plants:chinese_parasol_wooden_sword",
            "more_orn_plants:peach_wooden_sword",
            "more_orn_plants:suyu_axe",
            "more_orn_plants:suyu_sword",
            "more_orn_plants:ziying_axe",
            "more_orn_plants:ziying_sword",
            "more_orn_plants:ziyu_yuanyang_axe",
            "more_orn_plants:ziyu_yuanyang_sword",
            "rajswordmod:anti_armor_sword",
            "rajswordmod:blood_sword",
            "rajswordmod:deflect_sword",
            "rajswordmod:gaia_sword",
            "rajswordmod:holy_sword",
            "rajswordmod:knockback_sword",
            "rajswordmod:shadow_clone_sword",
            "tridents_n_stuff:slumbering_trident",
            "farmers_spell:gospel",
            "farmers_spell:hell_knife",
            "farmers_spell:cherry_spoon",
            "farmers_spell:iris_fork",
            "farmers_spell:grow_knife",
            "farmers_spell:twilight_blade",
            "farmers_spell:boreal_knife",
            "farmers_spell:echoing_knife",
            "more_orn_plants:caiyun_sword",
            "more_orn_plants:zhuiyue_sword"
    );

    private AdditionalWeaponUiEligibility() {}

    public static boolean applies(ItemStack weapon) {
        if (weapon.isEmpty() || weapon.getCount() != 1) return false;
        var identifier = ForgeRegistries.ITEMS.getKey(weapon.getItem());
        return identifier != null && WEAPONS.contains(identifier.toString());
    }

    public static boolean keepLegacyUi(ItemStack weapon) {
        if (!applies(weapon) || weapon.getTagElement("FTBossSwordStructure") != null
                || weapon.getTagElement("FTBossFourWeaponFoundation") != null) return false;
        return weapon.getTagElement("FTBossWeaponMaterials") != null;
    }
}
