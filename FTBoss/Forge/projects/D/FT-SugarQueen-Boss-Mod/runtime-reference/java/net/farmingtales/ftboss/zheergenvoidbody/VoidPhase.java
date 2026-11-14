/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.zheergenvoidbody;

public record VoidPhase(boolean active, String track, double progress, double weight) {
    public static final String KEY = "zheergenVoidBody";

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    private static double ease(double d) {
        double d2 = VoidPhase.clamp(d);
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static VoidPhase sample(String string, double d, int n, double d2) {
        if (!Double.isFinite(d) || d < 0.0 || n < 1 || n > 200 || string.equals("cancel") && !Double.isFinite(d2)) {
            return new VoidPhase(false, "charge", 0.0, 0.0);
        }
        return switch (string) {
            case "charge" -> new VoidPhase(d <= (double)(n + 3), "charge", VoidPhase.clamp(d / (double)n), 1.0);
            case "release" -> new VoidPhase(d < (double)n, "release", VoidPhase.clamp(d / (double)n), 1.0 - VoidPhase.ease((d / (double)n - 0.72) / 0.28));
            case "cancel" -> new VoidPhase(d < (double)n, "charge", VoidPhase.clamp(d2), 1.0 - VoidPhase.ease(d / (double)n));
            default -> new VoidPhase(false, "charge", 0.0, 0.0);
        };
    }
}

