/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class WeaponMaterialBonusRules {
    public static final Bonus ZERO = new Bonus(0.0, 0.0, 0.0, 0.0, 0.0);
    private static final double[] MANA = new double[]{0.0, 5.0, 10.0, 20.0, 30.0, 40.0};
    private static final Map<String, Integer> MAGIC = Map.ofEntries(Map.entry("irons_spellbooks:common_ink", 1), Map.entry("irons_spellbooks:uncommon_ink", 2), Map.entry("irons_spellbooks:rare_ink", 3), Map.entry("irons_spellbooks:epic_ink", 4), Map.entry("irons_spellbooks:legendary_ink", 5), Map.entry("irons_spellbooks:lightning_bottle", 1), Map.entry("irons_spellbooks:frozen_bone", 1), Map.entry("irons_spellbooks:blood_vial", 1), Map.entry("irons_spellbooks:divine_pearl", 3), Map.entry("irons_spellbooks:hogskin", 1), Map.entry("irons_spellbooks:dragonskin", 1), Map.entry("irons_spellbooks:bloody_vellum", 1), Map.entry("irons_spellbooks:cinder_essence", 3), Map.entry("irons_spellbooks:timeless_slurry", 1), Map.entry("irons_spellbooks:mithril_scrap", 1), Map.entry("irons_spellbooks:raw_mithril", 1), Map.entry("irons_spellbooks:mithril_weave", 1), Map.entry("irons_spellbooks:divine_soulshard", 4), Map.entry("irons_spellbooks:icy_fang", 1), Map.entry("irons_spellbooks:energized_core", 3), Map.entry("irons_spellbooks:dead_king_phylactery_shard", 3), Map.entry("irons_spellbooks:ancient_knowledge_fragment", 1), Map.entry("irons_spellbooks:arcane_essence", 1), Map.entry("irons_spellbooks:arcane_ingot", 3), Map.entry("irons_spellbooks:weapon_parts", 1), Map.entry("irons_spellbooks:ice_venom_vial", 1), Map.entry("irons_spellbooks:pyrium_ingot", 1), Map.entry("irons_spellbooks:frosted_helve", 1), Map.entry("irons_spellbooks:permafrost_shard", 1), Map.entry("irons_spellbooks:magic_cloth", 1), Map.entry("irons_spellbooks:mithril_ingot", 1));
    private static final Map<String, Integer> COMBAT = Map.ofEntries(Map.entry("cataclysm:witherite_ingot", 4), Map.entry("cataclysm:enderite_ingot", 4), Map.entry("cataclysm:ignitium_ingot", 5), Map.entry("cataclysm:cursium_ingot", 5), Map.entry("cataclysm:ancient_metal_ingot", 3), Map.entry("cataclysm:black_steel_ingot", 2), Map.entry("cataclysm:ancient_metal_nugget", 1), Map.entry("cataclysm:black_steel_nugget", 1), Map.entry("cataclysm:lacrima", 3), Map.entry("cataclysm:essence_of_the_storm", 5), Map.entry("cataclysm:koboleton_bone", 2), Map.entry("cataclysm:void_core", 4), Map.entry("cataclysm:crystallized_coral", 3), Map.entry("cataclysm:void_shard", 2), Map.entry("cataclysm:void_jaw", 4), Map.entry("cataclysm:monstrous_horn", 4), Map.entry("cataclysm:lava_power_cell", 5), Map.entry("cataclysm:burning_ashes", 4), Map.entry("cataclysm:dying_ember", 3), Map.entry("cataclysm:crystallized_coral_fragments", 1), Map.entry("cataclysm:coral_chunk", 2), Map.entry("cataclysm:amethyst_crab_shell", 2), Map.entry("cataclysm:lionfish_spike", 1), Map.entry("cataclysm:urchin_spike", 1), Map.entry("legendary_monsters:chorus_crystal", 3), Map.entry("legendary_monsters:infected_chorus_fruit", 2), Map.entry("legendary_monsters:void_gem", 4), Map.entry("legendary_monsters:enderitium_ingot", 4), Map.entry("legendary_monsters:knights_sword_blade_half", 2), Map.entry("legendary_monsters:metal_debris", 1), Map.entry("legendary_monsters:withered_bone", 3), Map.entry("legendary_monsters:withered_horn", 4), Map.entry("legendary_monsters:air_rune", 4), Map.entry("legendary_monsters:cloud_rod", 3));
    private static final Set<String> FRAGMENTS = Set.of("irons_spellbooks:mithril_scrap", "irons_spellbooks:raw_mithril", "cataclysm:ancient_metal_nugget", "cataclysm:black_steel_nugget", "cataclysm:crystallized_coral_fragments");

    private WeaponMaterialBonusRules() {
    }

    public static boolean relevant(String string) {
        return string != null && (MAGIC.containsKey(string) || COMBAT.containsKey(string));
    }

    public static Bonus material(String string, String string2) {
        int n;
        if (!WeaponMaterialBonusRules.relevant(string)) {
            return ZERO;
        }
        if (string2 == null) {
            n = 1;
        } else {
            switch (string2) {
                case "UNCOMMON": {
                    n = 2;
                    break;
                }
                case "RARE": {
                    n = 3;
                    break;
                }
                case "EPIC": {
                    n = 4;
                    break;
                }
                case "CINDEROUS": {
                    n = 5;
                    break;
                }
                default: {
                    n = string2.contains("CINDEROUS") ? 5 : 1;
                }
            }
        }
        int n2 = n;
        boolean bl = MAGIC.containsKey(string);
        int n3 = Math.max(n2, bl ? MAGIC.get(string) : COMBAT.get(string));
        if (FRAGMENTS.contains(string)) {
            n3 = 1;
        }
        return bl ? new Bonus((double)n3 * 0.01, MANA[n3], 0.0, 0.0, 0.0) : new Bonus(0.0, 0.0, (double)n3 * 0.5, (double)n3 * 0.25, n3);
    }

    public static Bonus combine(List<Input> list) {
        if (list == null) {
            return ZERO;
        }
        HashSet<String> hashSet = new HashSet<String>();
        Bonus bonus = ZERO;
        for (int i = 0; i < Math.min(27, list.size()); ++i) {
            Input input = list.get(i);
            if (input == null || !WeaponMaterialBonusRules.relevant(input.identifier()) || !hashSet.add(input.identifier())) continue;
            bonus = bonus.plus(WeaponMaterialBonusRules.material(input.identifier(), input.rarity()));
        }
        return bonus;
    }

    public record Bonus(double spellPower, double maxMana, double attackDamage, double armor, double maxHealth) {
        public Bonus plus(Bonus bonus) {
            return new Bonus(Math.min(0.25, this.spellPower + bonus.spellPower), Math.min(200.0, this.maxMana + bonus.maxMana), Math.min(8.0, this.attackDamage + bonus.attackDamage), Math.min(4.0, this.armor + bonus.armor), Math.min(12.0, this.maxHealth + bonus.maxHealth));
        }
    }

    public record Input(String identifier, String rarity) {
    }
}

