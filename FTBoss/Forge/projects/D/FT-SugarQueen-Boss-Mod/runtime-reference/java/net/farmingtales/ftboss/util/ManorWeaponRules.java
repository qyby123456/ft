/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class ManorWeaponRules {
    public static final int CLEAVER_IMPACT_TICKS = 9;
    public static final int CLEAVER_COOLDOWN_TICKS = 80;
    public static final float CLEAVER_DAMAGE = 32.0f;
    public static final float CLEAVER_RAGE_DAMAGE = 80.0f;
    public static final double CLEAVER_RADIUS = 3.0;
    public static final double CLEAVER_RAGE_RADIUS = 4.0;
    public static final float SKILL_DAMAGE_MULTIPLIER = 1.5f;
    public static final float TRIDENT_THROW_MULTIPLIER = 3.0f;

    private ManorWeaponRules() {
    }

    public static double attackBonus(double originalBonus) {
        return originalBonus + 1.0;
    }

    public static float skillDamage(float base) {
        return base * 1.5f;
    }

    public static float skillBonusDamage(float base) {
        return ManorWeaponRules.skillDamage(base);
    }

    public static float skillHeal(float base) {
        return ManorWeaponRules.skillDamage(base);
    }

    public static float cleaverDamage(int stacks) {
        return stacks >= 5 ? 80.0f : 32.0f;
    }

    public static double cleaverRadius(int stacks) {
        return stacks >= 5 ? 4.0 : 3.0;
    }

    public static boolean insideSlam(double offsetX, double offsetY, double offsetZ, int stacks) {
        double radius = ManorWeaponRules.cleaverRadius(stacks);
        return Double.isFinite(offsetX) && Double.isFinite(offsetY) && Double.isFinite(offsetZ) && Math.abs(offsetY) <= 2.5 && offsetX * offsetX + offsetZ * offsetZ <= radius * radius;
    }
}

