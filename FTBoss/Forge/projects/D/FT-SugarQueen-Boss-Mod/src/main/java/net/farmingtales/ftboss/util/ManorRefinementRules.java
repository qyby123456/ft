package net.farmingtales.ftboss.util;

/** 庄园六项调整的纯数值规则，便于离线验证边界。 */
public final class ManorRefinementRules {
    public static final int TELEPORT_SEAL_TICKS = 200;
    public static final int PREDICTION_TICKS = 10;
    public static final double MAX_LEAD = 6.0;
    public static final float DORA_DAMAGE_MULTIPLIER = 1.25F;
    public static final double GONGDI_RANGE_MULTIPLIER = 1.5;

    private ManorRefinementRules() {}

    public static double leadScale(double velocityX, double velocityZ) {
        double speed = Math.hypot(velocityX, velocityZ);
        return !Double.isFinite(speed) || speed < 0.001 || speed > 1.5 ? 0
                : Math.min(PREDICTION_TICKS, MAX_LEAD / speed);
    }

    public static int armorSteps(int tridents, int cap) {
        return Math.min(Math.max(0, tridents) / 5 * 2, Math.max(0, cap));
    }

    public static boolean sealed(long now, long expires) {
        return expires > now && expires - now <= TELEPORT_SEAL_TICKS;
    }

    public static boolean minorSpell(int spell) {
        return spell >= 1 && spell <= 6 && spell != 4;
    }
}
