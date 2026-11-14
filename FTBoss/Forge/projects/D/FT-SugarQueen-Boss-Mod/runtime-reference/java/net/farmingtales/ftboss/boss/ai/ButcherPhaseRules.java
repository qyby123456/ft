/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.boss.ai;

public final class ButcherPhaseRules {
    public static final double BASE_HEALTH = 380.0;
    public static final int REST = 1;
    public static final int ASSAULT = 2;
    public static final int RECOVERY = 3;
    public static final int RECOVERY_DURATION = 1200;
    public static final float REGENERATION_RATIO = 0.05f;
    public static final float DODGE_CHANCE = 0.1f;
    public static final int DODGE_COOLDOWN = 20;

    private ButcherPhaseRules() {
    }

    public static int normalize(int n) {
        return n >= 1 && n <= 3 ? n : 1;
    }

    public static int afterDefeat(int n) {
        return ButcherPhaseRules.normalize(n) == 3 ? 0 : ButcherPhaseRules.normalize(n) + 1;
    }

    public static double healthMultiplier(int n) {
        return ButcherPhaseRules.normalize(n) == 1 ? 1.0 : 0.5;
    }

    public static int interval(int n) {
        return ButcherPhaseRules.normalize(n) == 2 ? 10 : (ButcherPhaseRules.normalize(n) == 3 ? 14 : 20);
    }

    public static float damageMultiplier(int n) {
        return ButcherPhaseRules.normalize(n) == 2 ? 2.0f : (ButcherPhaseRules.normalize(n) == 3 ? 0.6666667f : 0.33333334f);
    }

    public static boolean expired(int n, int n2) {
        return n == 3 && n2 >= 1200;
    }

    public static float regeneration(int n, int n2, float f, float f2) {
        if (n != 3 || n2 <= 0 || n2 % 20 != 0 || ButcherPhaseRules.expired(n, n2)) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(f2, f * 0.05f));
    }

    public static boolean dodge(int n, int n2, float f) {
        return n == 3 && n2 <= 0 && f >= 0.0f && f < 0.1f;
    }

    public static float scaledHealth(float f, float f2) {
        return Math.max(1.0f, Math.min(f2, f * f2));
    }
}

