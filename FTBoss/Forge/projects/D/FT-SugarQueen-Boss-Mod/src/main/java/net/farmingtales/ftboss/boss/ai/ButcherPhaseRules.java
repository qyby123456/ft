package net.farmingtales.ftboss.boss.ai;

public final class ButcherPhaseRules {
    public static final double BASE_HEALTH = 760;
    public static final int REST = 1;
    public static final int ASSAULT = 2;
    public static final int RECOVERY = 3;
    public static final int RECOVERY_DURATION = 20 * 120;
    public static final float REGENERATION_RATIO = 0.05F;
    public static final float DODGE_CHANCE = 0.10F;
    public static final int DODGE_COOLDOWN = 20;

    private ButcherPhaseRules() {}

    public static int normalize(int phase) {
        return phase >= REST && phase <= RECOVERY ? phase : REST;
    }

    public static int afterDefeat(int phase) {
        return normalize(phase) == RECOVERY ? 0 : normalize(phase) + 1;
    }

    public static double healthMultiplier(int phase) {
        return normalize(phase) == RECOVERY ? 0.5 : 1.0;
    }

    public static int interval(int phase) {
        return normalize(phase) == ASSAULT ? 10 : normalize(phase) == RECOVERY ? 14 : 20;
    }

    public static float damageMultiplier(int phase) {
        return normalize(phase) == ASSAULT ? 2.0F : normalize(phase) == RECOVERY ? 2.0F / 3.0F : 1.0F / 3.0F;
    }

    public static boolean expired(int phase, int elapsedTicks) {
        return phase == RECOVERY && elapsedTicks >= RECOVERY_DURATION;
    }

    public static float scaledHealth(float ratio, float maximumHealth) {
        return Math.max(1.0F, Math.min(maximumHealth, ratio * maximumHealth));
    }

    public static float regeneration(int phase, int elapsedTicks, float maximumHealth, float missingHealth) {
        if (phase != RECOVERY || elapsedTicks <= 0 || elapsedTicks % 20 != 0 || expired(phase, elapsedTicks)) return 0;
        return Math.max(0, Math.min(missingHealth, maximumHealth * REGENERATION_RATIO));
    }

    public static boolean dodge(int phase, int cooldown, float roll) {
        return phase == RECOVERY && cooldown <= 0 && roll >= 0 && roll < DODGE_CHANCE;
    }
}
