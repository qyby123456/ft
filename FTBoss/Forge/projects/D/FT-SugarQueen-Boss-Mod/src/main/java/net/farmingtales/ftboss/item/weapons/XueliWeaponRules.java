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
    /** 技能基础伤害；实际结算与显示走 {@link #sweepDamage()} / {@link #rollDamage(int)}（含庄园技能倍率）。 */
    public static final float SWEEP_DAMAGE = 32.0F;
    public static final float COMBO_DAMAGE = 12.0F;
    public static final double SWEEP_RADIUS = 3.6D;
    public static final double ROLL_LENGTH = 6.0D;
    public static final double ROLL_HALF_WIDTH = 1.05D;

    private XueliWeaponRules() {}

    /** 夜宴煎锅横扫的单体伤害（庄园技能倍率已计入）。 */
    public static float sweepDamage() {
        return 96.0F;
    }

    /** 玫印追加伤害（庄园技能倍率已计入）。 */
    public static float comboDamage() {
        return 36.0F;
    }

    public static boolean inSweep(double forward, double lateral) {
        double distanceSquared = forward * forward + lateral * lateral;
        return forward >= 0.0D && distanceSquared <= SWEEP_RADIUS * SWEEP_RADIUS
                && forward * forward >= distanceSquared * 0.25D;
    }

    public static boolean inRoll(double forward, double lateral, double unobstructedLength) {
        return forward >= 0.0D && forward <= Math.min(ROLL_LENGTH, unobstructedLength)
                && Math.abs(lateral) <= ROLL_HALF_WIDTH;
    }

    public static float rollDamage(int chargeTicks) {
        double fraction = Math.max(0.0D, Math.min(1.0D, (chargeTicks - MIN_CHARGE) / (double) (FULL_CHARGE - MIN_CHARGE)));
        return ManorWeaponBalanceRules.skillDamage((float) (28.0D + 20.0D * fraction));
    }

    public static boolean dropsWeapons(int variant, boolean mini, boolean playerKill, boolean mobLoot) {
        return variant == 10 && !mini && playerKill && mobLoot;
    }
}
