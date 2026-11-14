/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class ManorSkillDamageRules {
    public static final double STANDARD_MULTIPLIER = 1.5;
    public static final double HEAVY_MULTIPLIER = 2.0;

    private ManorSkillDamageRules() {
    }

    public static float damage(double attack, double enchantment, float baseDamage, double multiplier) {
        double melee = ManorSkillDamageRules.positiveFinite(attack) + ManorSkillDamageRules.positiveFinite(enchantment);
        double ratio = Math.max(1.5, ManorSkillDamageRules.positiveFinite(multiplier));
        return (float)Math.min(3.4028234663852886E38, Math.max(ManorSkillDamageRules.positiveFinite(baseDamage), melee * ratio));
    }

    private static double positiveFinite(double value) {
        return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
    }
}

