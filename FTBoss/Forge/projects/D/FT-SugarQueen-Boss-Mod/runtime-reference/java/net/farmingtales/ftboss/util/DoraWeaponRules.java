/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class DoraWeaponRules {
    public static final int MIN_CHARGE_TICKS = 12;
    public static final int FULL_CHARGE_TICKS = 24;
    public static final double GUN_RANGE = 32.0;
    public static final double NOVA_RADIUS = 2.75;
    public static final double THRUST_RANGE = 6.5;
    public static final double SWEEP_RANGE = 4.5;
    public static final float PULSE_GUN_DAMAGE_MULTIPLIER = 6.0f;

    private DoraWeaponRules() {
    }

    public static Mode mode(boolean lance, boolean sneaking) {
        return lance ? (sneaking ? Mode.SWEEP : Mode.THRUST) : (sneaking ? Mode.NOVA : Mode.PULSE);
    }

    public static double charge(int ticks) {
        if (ticks < 12) {
            return 0.0;
        }
        return 0.5 + Math.min(1.0, (double)(ticks - 12) / 12.0) * 0.5;
    }

    public static boolean inThrust(double forward, double sidewaysSquared) {
        return Double.isFinite(forward) && Double.isFinite(sidewaysSquared) && forward >= 0.0 && forward <= 6.5 && sidewaysSquared >= 0.0 && sidewaysSquared <= 0.7224999999999999;
    }

    public static boolean inSweep(double distanceSquared, double facingDot) {
        return Double.isFinite(distanceSquared) && Double.isFinite(facingDot) && distanceSquared >= 0.0 && distanceSquared <= 20.25 && facingDot >= 0.25;
    }

    public static enum Mode {
        PULSE(32.0f, 50, 2),
        NOVA(28.0f, 120, 4),
        THRUST(30.0f, 50, 2),
        SWEEP(24.0f, 90, 3);

        public final float damage;
        public final int cooldownTicks;
        public final int durabilityCost;

        private Mode(float damage, int cooldownTicks, int durabilityCost) {
            this.damage = damage;
            this.cooldownTicks = cooldownTicks;
            this.durabilityCost = durabilityCost;
        }

        public float damage() {
            return this.damage * 3.0f * 2.0f;
        }
    }
}

