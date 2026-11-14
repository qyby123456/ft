/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class FlywheelCombatRules {
    public static final int HIT_CD_NEAR = 2;
    public static final int HIT_CD_FAR = 6;
    public static final double NEAR_DIST = 1.0;
    public static final double FAR_DIST = 6.0;
    public static final double TARGET_RANGE = 24.0;
    public static final int OPENING_COUNT = 10;

    private FlywheelCombatRules() {
    }

    public static int openingProgress(int granted, int existing) {
        return Math.max(0, Math.min(10, Math.max(granted, existing)));
    }

    public static int hitCooldown(double ownerDistance, boolean maxed) {
        double distance = Double.isFinite(ownerDistance) ? ownerDistance : 6.0;
        double fraction = (Math.max(1.0, Math.min(6.0, distance)) - 1.0) / 5.0;
        int cooldown = (int)Math.round(2.0 + 4.0 * fraction);
        return Math.max(2, cooldown - (maxed ? 1 : 0));
    }

    public static boolean shouldRelaunch(int orbitTicks, double ownerDistance, boolean maxed, boolean ownerReady) {
        return ownerReady && Double.isFinite(ownerDistance) && ownerDistance >= 0.0 && ownerDistance <= 24.0 && orbitTicks >= FlywheelCombatRules.hitCooldown(ownerDistance, maxed) * 2;
    }

    public static boolean hitReady(long now, Long lastHit, int cooldown) {
        return lastHit == null || now < lastHit || now - lastHit >= (long)cooldown;
    }
}

