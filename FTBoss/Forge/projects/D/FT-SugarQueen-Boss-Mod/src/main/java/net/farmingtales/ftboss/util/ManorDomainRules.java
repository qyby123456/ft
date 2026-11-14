package net.farmingtales.ftboss.util;

import java.util.Locale;

public final class ManorDomainRules {
    public static final double RADIUS = ManorTeleportRules.STRAY_FROM_BOSS;
    public static final int REFRESH_TICKS = 20;

    private ManorDomainRules() {}

    public static boolean supports(int variant) {
        return variant >= 0 && variant <= 11 && variant != 9;
    }

    public static boolean contains(double offsetX, double offsetY, double offsetZ) {
        return Double.isFinite(offsetX) && Double.isFinite(offsetY) && Double.isFinite(offsetZ)
                && offsetX * offsetX + offsetZ * offsetZ <= RADIUS * RADIUS;
    }

    public static double outgoing(int variant, boolean ranged, double distance, double healthRatio, boolean behind, int cadence) {
        return switch (variant) {
            case 1, 5 -> ranged ? 1.25 : 1.0;
            case 2 -> ranged && distance >= 6.0 ? 1.30 : 1.0;
            case 3 -> !ranged && healthRatio < 0.6 ? 1.35 : 1.0;
            case 6 -> ranged ? 1.30 : 1.0;
            case 7 -> !ranged && behind ? 1.30 : 1.0;
            case 11 -> ranged && cadence % 3 == 0 ? 1.45 : 1.0;
            default -> 1.0;
        };
    }

    public static boolean rangedHit(int variant, boolean projectile, String damageType, String damageClass, double distance) {
        String normalized = damageType.toLowerCase(Locale.ROOT);
        return projectile || normalized.contains("magic") || normalized.contains("spell")
                || damageClass.startsWith("io.redspace.ironsspellbooks.damage.") || variant == 2 && distance >= 6.0;
    }

    public static double armor(int variant) {
        return variant == 0 ? 6.0 : variant == 8 ? 4.0 : variant == 10 ? 5.0 : 0.0;
    }

    public static double speed(int variant) {
        return variant == 7 ? 0.18 : 0.0;
    }

    public static double knockbackResistance(int variant) {
        return variant == 6 ? 0.45 : variant == 10 ? 0.30 : 0.0;
    }

    public static double attack(int variant) {
        return variant == 4 ? 0.18 : 0.0;
    }
}
