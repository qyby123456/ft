package net.farmingtales.ftboss.util;

import java.util.Set;

public final class ManorWeaponBalanceRules {
    public static final float DAMAGE_SCALE = 2.0F;
    public static final double BLACK_CLEAVER_BASE_DAMAGE = 36.0;
    public static final double SANGUINE_BASE_DAMAGE = 18.0;
    private static final Set<String> WEAPONS = Set.of(
            "ftboss:dandan_scythe", "ftboss:dora_nova_launcher", "ftboss:dora_pulse_gun", "ftboss:dora_lance",
            "ftboss:lemoncat_skillet", "ftboss:lemoncat_spatula", "tridents_n_stuff:stormseeker",
            "tridents_n_stuff:the_desecrator", "tridents_n_stuff:echo_of_the_deep", "tridents_n_stuff:stellaris",
            "ftboss:wenxuanlan_flywheel", "ftboss:shadow_sword", "ftboss:puffer_bat", "ftboss:maque_blessing_feather",
            "ftboss:xueli_skillet", "ftboss:xueli_rolling_pin", "ftboss:qizai_prayer_staff", "ftboss:qingyi_confluence_trident");

    private ManorWeaponBalanceRules() {}

    public static boolean doubled(String identifier) {
        return WEAPONS.contains(identifier);
    }

    public static boolean attributeTarget(boolean manorWeapon, String identifier) {
        return manorWeapon || doubled(identifier) || identifier.equals("tridents_n_stuff:sanguine_bond");
    }

    public static double attackBonus(double originalBonus, String identifier) {
        if (identifier.equals("ftboss:black_cleaver")) return BLACK_CLEAVER_BASE_DAMAGE - originalBonus - 1.0;
        if (identifier.equals("tridents_n_stuff:sanguine_bond")) {
            return SANGUINE_BASE_DAMAGE - originalBonus - 1.0;
        }
        if (!doubled(identifier)) return ManorWeaponRules.attackBonus(originalBonus);
        if (identifier.equals("ftboss:qingyi_confluence_trident")) return originalBonus + 1.0;
        double previousTotal = 1.0 + originalBonus + ManorWeaponRules.attackBonus(originalBonus);
        return previousTotal * DAMAGE_SCALE - originalBonus - 1.0;
    }

    public static double emptyHandBonus(String identifier) {
        return attackBonus(0.0, identifier);
    }

    public static float skillDamage(float base) {
        return ManorWeaponRules.skillDamage(base) * DAMAGE_SCALE;
    }

    public static float skillBonusDamage(float base) {
        return ManorWeaponRules.skillBonusDamage(base) * DAMAGE_SCALE;
    }
}
