package net.farmingtales.ftboss.util;

public final class ManorSkillDamageRules {
    public static final double STANDARD_MULTIPLIER = 1.50;
    public static final double HEAVY_MULTIPLIER = 2.00;

    private ManorSkillDamageRules() {}

    public static float damage(double attack, double enchantment, float baseDamage, double multiplier) {
        double melee = positiveFinite(attack) + positiveFinite(enchantment);
        double ratio = Math.max(STANDARD_MULTIPLIER, positiveFinite(multiplier));
        return (float) Math.min(Float.MAX_VALUE, Math.max(positiveFinite(baseDamage), melee * ratio));
    }

    private static double positiveFinite(double value) {
        return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
    }
}
