package net.farmingtales.ftboss.util;

public final class YouziStatueRules {
    public static final int ECHO_COUNT = 3;
    public static final int ECHO_DELAY = 4;
    public static final int ECHO_INTERVAL = 12;
    public static final int ECHO_DURATION = 32;
    public static final int ECHO_COOLDOWN = 160;
    /** 技能基础伤害；实际结算走 {@link #echoDamage()}（含庄园技能倍率）。 */
    public static final float ECHO_DAMAGE = 16.0F;
    public static final double ECHO_RANGE = 18.0;
    public static final int WARD_DURATION = 120;
    public static final int WARD_COOLDOWN = 320;
    public static final float WARD_REDUCTION = 0.50F;

    private YouziStatueRules() {}

    public static int dueEchoes(long age) {
        if (age < ECHO_DELAY || age >= ECHO_DURATION) return 0;
        return Math.min(ECHO_COUNT, 1 + (int) ((age - ECHO_DELAY) / ECHO_INTERVAL));
    }

    /** 每次回响的实际伤害（庄园技能倍率已计入）。 */
    public static float echoDamage() {
        return 24.0F;
    }

    public static float protectedDamage(float damage) {
        return Math.max(0.0F, damage) * (1.0F - WARD_REDUCTION);
    }

    public static boolean dropsStatue(int variant, boolean mini, boolean giant) {
        return variant == 0 && !mini && !giant;
    }
}
