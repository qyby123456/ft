/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class ManorBossCombatRules {
    public static final int SPELLS_PER_CAST = 2;
    public static final double DORA_SPEAR_REACH = 6.0;
    public static final int DORA_SPEAR_HITS = 3;
    public static final int DORA_SPEAR_INTERVAL = 4;
    public static final int DORA_SPEAR_COOLDOWN = 30;
    public static final double GONGDI_SPELL_INTERVAL_MULTIPLIER = 1.8;
    public static final int GONGDI_SKILL_COOLDOWN = 120;
    public static final int ZICAI_MELEE_INTERVAL = 20;
    public static final int ZHEERGEN_SKILL_COOLDOWN = 70;

    public static int meleeInterval(int variant) {
        return variant == 8 ? 20 : 0;
    }

    private ManorBossCombatRules() {
    }

    public static double radiusScale(int variant) {
        if (variant == 1) {
            return 1.5;
        }
        if (variant == 11) {
            return 2.0;
        }
        return variant >= 2 && variant <= 8 || variant == 10 ? 2.0 : 1.0;
    }

    public static double spellRadius(int variant, double radius) {
        return radius * ManorBossCombatRules.radiusScale(variant) + (variant == 6 ? 1.0 : 0.0);
    }

    public static boolean prefersMelee(int variant) {
        return variant == 0 || variant == 1 || variant == 3 || variant == 4 || variant == 7 || variant == 8 || variant == 10;
    }

    public static int skillCooldown(int variant, int original) {
        if (variant == 7) {
            return 70;
        }
        return ManorBossCombatRules.prefersMelee(variant) ? original * 2 : original;
    }

    public static int spellInterval(int variant) {
        return ManorBossCombatRules.prefersMelee(variant) ? 320 : 80;
    }

    public static int spellsPerCast(int variant) {
        return ManorBossCombatRules.prefersMelee(variant) ? 1 : 2;
    }

    public static double spellIntervalMultiplier(int variant) {
        return variant == 3 ? 1.8 : 1.0;
    }

    public static int gongdiSkillCooldown() {
        return 120;
    }
}

