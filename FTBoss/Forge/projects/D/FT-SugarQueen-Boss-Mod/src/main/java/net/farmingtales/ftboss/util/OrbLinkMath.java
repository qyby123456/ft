package net.farmingtales.ftboss.util;

/** 充能球联动的确定性曲线与时序；2026-09-17，客户端和服务端共用。 */
public final class OrbLinkMath {
    private OrbLinkMath() {}

    public static double progress(double elapsed, double duration) {
        return Math.max(0.0, Math.min(1.0, elapsed / Math.max(1.0, duration)));
    }

    public static double smooth(double progress) {
        double clamped = Math.max(0.0, Math.min(1.0, progress));
        return clamped * clamped * (3.0 - 2.0 * clamped);
    }

    public static double arc(double start, double end, double progress, double lift) {
        double clamped = Math.max(0.0, Math.min(1.0, progress));
        return start + (end - start) * smooth(clamped) + Math.sin(clamped * Math.PI) * lift;
    }

    public static int releaseTicks(String type) {
        return switch (type) {
            case "glass" -> 11;
            case "lightning" -> 9;
            case "frost", "ion" -> 6;
            case "dark" -> 16;
            default -> throw new IllegalArgumentException("Unknown orb type: " + type);
        };
    }
}
