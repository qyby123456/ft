package net.farmingtales.ftboss.util;

public final class LemonCatWeaponRules {
    private static final double CONE_COSINE = Math.cos(Math.toRadians(55.0D));

    private LemonCatWeaponRules() {}

    public static boolean shouldDrop(int variant, boolean mini, boolean finale, boolean recentlyHit, boolean mobLoot) {
        return variant == 4 && !mini && !finale && recentlyHit && mobLoot;
    }

    public static boolean useHealing(boolean paired, boolean sneaking, boolean spatula) {
        return paired ? sneaking : spatula;
    }

    public static boolean inCone(double offsetX, double offsetY, double offsetZ, double forwardX, double forwardZ, double radius) {
        if (!Double.isFinite(offsetX) || !Double.isFinite(offsetY) || !Double.isFinite(offsetZ)
                || !Double.isFinite(forwardX) || !Double.isFinite(forwardZ) || !Double.isFinite(radius) || radius <= 0.0D) return false;
        double distanceSquared = offsetX * offsetX + offsetZ * offsetZ;
        if (distanceSquared > radius * radius || Math.abs(offsetY) > 2.5D) return false;
        double facingLength = Math.hypot(forwardX, forwardZ);
        if (facingLength < 0.0001D) return false;
        return distanceSquared <= 0.01D
                || (offsetX * forwardX + offsetZ * forwardZ) / Math.sqrt(distanceSquared) / facingLength >= CONE_COSINE - 1.0E-9D;
    }
}
