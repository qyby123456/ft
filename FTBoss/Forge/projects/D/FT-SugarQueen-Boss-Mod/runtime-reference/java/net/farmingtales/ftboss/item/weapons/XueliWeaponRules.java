/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.item.weapons;

import net.farmingtales.ftboss.util.ManorWeaponBalanceRules;

public final class XueliWeaponRules {
    public static final int SKILLET_COOLDOWN = 120;
    public static final int GUARD_COOLDOWN = 100;
    public static final int GUARD_DURATION = 20;
    public static final int PIN_COOLDOWN = 160;
    public static final int MIN_CHARGE = 8;
    public static final int FULL_CHARGE = 24;
    public static final int MARK_DURATION = 80;
    public static final int SLOW_DURATION = 60;
    public static final int MAX_TARGETS = 12;
    public static final float SWEEP_DAMAGE = 32.0f;
    public static final float COMBO_DAMAGE = 12.0f;
    public static final double SWEEP_RADIUS = 3.6;
    public static final double ROLL_LENGTH = 6.0;
    public static final double ROLL_HALF_WIDTH = 1.05;

    private XueliWeaponRules() {
    }

    public static float sweepDamage() {
        return ManorWeaponBalanceRules.skillDamage(32.0f);
    }

    public static float comboDamage() {
        return ManorWeaponBalanceRules.skillBonusDamage(12.0f);
    }

    public static boolean inSweep(double forward, double lateral) {
        double distanceSquared = forward * forward + lateral * lateral;
        return forward >= 0.0 && distanceSquared <= 12.96 && forward * forward >= distanceSquared * 0.25;
    }

    public static boolean inRoll(double forward, double lateral, double unobstructedLength) {
        return forward >= 0.0 && forward <= Math.min(6.0, unobstructedLength) && Math.abs(lateral) <= 1.05;
    }

    public static float rollDamage(int chargeTicks) {
        double fraction = Math.max(0.0, Math.min(1.0, (double)(chargeTicks - 8) / 16.0));
        return ManorWeaponBalanceRules.skillDamage((float)(28.0 + 20.0 * fraction));
    }

    public static boolean dropsWeapons(int variant, boolean mini, boolean playerKill, boolean mobLoot) {
        return variant == 10 && !mini && playerKill && mobLoot;
    }
}

