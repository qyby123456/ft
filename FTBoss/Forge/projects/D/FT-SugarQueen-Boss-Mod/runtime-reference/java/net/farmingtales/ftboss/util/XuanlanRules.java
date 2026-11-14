/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class XuanlanRules {
    public static final int CHARGE_TICKS = 20;
    public static final int MIN_CHARGE_TICKS = 4;
    public static final int MARK_TICKS = 120;
    public static final int ENERGY_TICKS = 160;
    public static final int OUTBOUND_TICKS = 10;
    public static final int MAX_FLIGHT_TICKS = 60;
    public static final int MAX_ACTIVE_BLADES = 2;
    public static final double RANGE = 32.0;
    public static final float WENXUANLAN_DAMAGE_MULTIPLIER = 4.0f;

    private XuanlanRules() {
    }

    public static float weaponDamage(float base) {
        return base * 2.0f * 2.0f;
    }

    public static boolean shouldDrop(int variant, boolean mini, boolean giant, boolean recentlyHit) {
        return variant == 6 && !mini && !giant && recentlyHit;
    }

    public static float shotDamage(int chargeTicks, int energy) {
        return XuanlanRules.weaponDamage(16.0f + 20.0f * (float)Math.min(20, Math.max(0, chargeTicks)) / 20.0f + 4.0f * (float)Math.min(3, Math.max(0, energy)));
    }

    public static float bladeDamage(boolean returning, boolean marked) {
        return XuanlanRules.weaponDamage((returning ? 12.0f : 16.0f) + (marked ? 16.0f : 0.0f));
    }

    public static boolean active(long now, long until, int maximumDuration) {
        return until > now && until - now <= (long)maximumDuration;
    }
}

