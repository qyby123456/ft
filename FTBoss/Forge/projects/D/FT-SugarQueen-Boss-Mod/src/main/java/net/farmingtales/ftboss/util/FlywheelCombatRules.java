package net.farmingtales.ftboss.util;

public final class FlywheelCombatRules {
    public static final int HIT_CD_NEAR = 2;
    public static final int HIT_CD_FAR = 6;
    public static final double NEAR_DIST = 1.0;
    public static final double FAR_DIST = 6.0;
    public static final double TARGET_RANGE = 24.0;
    public static final int OPENING_COUNT = 10;

    private FlywheelCombatRules() {}

    public static boolean canAttack(boolean orbiting, int runTicks, int homingTicks) {
        return !orbiting && runTicks >= 0 && runTicks <= homingTicks;
    }

    public static int openingProgress(int granted, int existing) {
        return Math.max(0, Math.min(OPENING_COUNT, Math.max(granted, existing)));
    }

    public static int hitCooldown(double ownerDistance, boolean maxed) {
        double distance = Double.isFinite(ownerDistance) ? ownerDistance : FAR_DIST;
        double fraction = (Math.max(NEAR_DIST, Math.min(FAR_DIST, distance)) - NEAR_DIST)
                / (FAR_DIST - NEAR_DIST);
        int cooldown = (int) Math.round(HIT_CD_NEAR + (HIT_CD_FAR - HIT_CD_NEAR) * fraction);
        return Math.max(HIT_CD_NEAR, cooldown - (maxed ? 1 : 0));
    }

    public static boolean shouldRelaunch(int orbitTicks, double ownerDistance, boolean maxed, boolean ownerReady) {
        return ownerReady && Double.isFinite(ownerDistance) && ownerDistance >= 0.0 && ownerDistance <= TARGET_RANGE
                && orbitTicks >= hitCooldown(ownerDistance, maxed) * 2;
    }

    public static boolean hitReady(long now, Long lastHit, int cooldown) {
        return lastHit == null || now < lastHit || now - lastHit >= cooldown;
    }
}
