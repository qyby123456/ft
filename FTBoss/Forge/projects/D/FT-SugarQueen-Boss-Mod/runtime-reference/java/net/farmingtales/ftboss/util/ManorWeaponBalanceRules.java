/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Set;
import net.farmingtales.ftboss.util.ManorWeaponRules;

public final class ManorWeaponBalanceRules {
    public static final float DAMAGE_SCALE = 2.0f;
    private static final Set<String> WEAPONS = Set.of("ftboss:dandan_scythe", "ftboss:dora_nova_launcher", "ftboss:dora_pulse_gun", "ftboss:dora_lance", "ftboss:lemoncat_skillet", "ftboss:lemoncat_spatula", "tridents_n_stuff:stormseeker", "tridents_n_stuff:the_desecrator", "tridents_n_stuff:echo_of_the_deep", "tridents_n_stuff:stellaris", "ftboss:wenxuanlan_flywheel", "ftboss:shadow_sword", "ftboss:puffer_bat", "ftboss:maque_blessing_feather", "ftboss:xueli_skillet", "ftboss:xueli_rolling_pin", "ftboss:qizai_prayer_staff", "ftboss:qingyi_confluence_trident");

    private ManorWeaponBalanceRules() {
    }

    public static boolean doubled(String string) {
        return WEAPONS.contains(string);
    }

    public static boolean attributeTarget(boolean bl, String string) {
        return bl || ManorWeaponBalanceRules.doubled(string);
    }

    public static double attackBonus(double d, String string) {
        if (!ManorWeaponBalanceRules.doubled(string)) {
            return ManorWeaponRules.attackBonus(d);
        }
        if (string.equals("ftboss:qingyi_confluence_trident")) {
            return d + 1.0;
        }
        double d2 = 1.0 + d + ManorWeaponRules.attackBonus(d);
        return d2 * 2.0 - d - 1.0;
    }

    public static double emptyHandBonus(String string) {
        return ManorWeaponBalanceRules.attackBonus(0.0, string);
    }

    public static float skillDamage(float f) {
        return ManorWeaponRules.skillDamage(f) * 2.0f;
    }

    public static float skillBonusDamage(float f) {
        return ManorWeaponRules.skillBonusDamage(f) * 2.0f;
    }
}

