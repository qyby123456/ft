/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.ManorWeaponRules;

public final class YouziStatueRules {
    public static final int ECHO_COUNT = 3;
    public static final int ECHO_DELAY = 4;
    public static final int ECHO_INTERVAL = 12;
    public static final int ECHO_DURATION = 32;
    public static final int ECHO_COOLDOWN = 160;
    public static final float ECHO_DAMAGE = 16.0f;
    public static final double ECHO_RANGE = 18.0;
    public static final int WARD_DURATION = 120;
    public static final int WARD_COOLDOWN = 320;
    public static final float WARD_REDUCTION = 0.5f;

    private YouziStatueRules() {
    }

    public static int dueEchoes(long age) {
        if (age < 4L || age >= 32L) {
            return 0;
        }
        return Math.min(3, 1 + (int)((age - 4L) / 12L));
    }

    public static float echoDamage() {
        return ManorWeaponRules.skillDamage(16.0f);
    }

    public static float protectedDamage(float damage) {
        return Math.max(0.0f, damage) * 0.5f;
    }

    public static boolean dropsStatue(int variant, boolean mini, boolean giant) {
        return variant == 0 && !mini && !giant;
    }
}

