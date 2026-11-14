/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.ManorWeaponRules;

public final class MaqueFeatherRules {
    public static final int COOLDOWN_TICKS = 400;
    public static final int PASSIVE_INTERVAL_TICKS = 80;
    public static final int BLESSING_TICKS = 120;
    public static final int WIND_TICKS = 120;
    public static final float PASSIVE_HEAL = 2.0f;
    public static final float BURST_HEAL = 16.0f;
    public static final double RADIUS = 6.0;
    public static final int MAX_TARGETS = 16;

    private MaqueFeatherRules() {
    }

    public static float passiveHeal() {
        return ManorWeaponRules.skillHeal(2.0f);
    }

    public static float burstHeal() {
        return ManorWeaponRules.skillHeal(16.0f);
    }

    public static boolean shouldDrop(int variant, boolean mini, boolean giant, boolean recentlyHit) {
        return variant == 9 && !mini && !giant && recentlyHit;
    }

    public static int remainingCooldown(long now, long until) {
        if (until <= now) {
            return 0;
        }
        return (int)Math.min(400L, until - now);
    }

    public static boolean continuousHold(long now, long previous) {
        return previous == now - 1L;
    }

    public static boolean canSupport(boolean self, boolean alive, boolean spectator, boolean alliedPlayer, boolean ownedPet, double distanceSquared, boolean visible) {
        return alive && !spectator && (self || (alliedPlayer || ownedPet) && distanceSquared <= 36.0 && visible);
    }
}

