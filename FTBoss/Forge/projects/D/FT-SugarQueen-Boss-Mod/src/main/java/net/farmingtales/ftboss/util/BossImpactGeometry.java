package net.farmingtales.ftboss.util;

public final class BossImpactGeometry {
    public static final int DURATION = 8;
    public static final int HIT_INTERVAL = 3;
    public static final int MAX_ACTIVE = 32;
    public static final int VIEW_DISTANCE = 48;

    @FunctionalInterface
    public interface Vertex {
        void emit(double horizontal, double vertical, int color, float alpha);
    }

    private BossImpactGeometry() {}

    public static float strength(float damage, float maximumHealth) {
        if (!Float.isFinite(damage) || !Float.isFinite(maximumHealth) || damage <= 0 || maximumHealth <= 0) return 0;
        return Math.min(1, Math.max(0.2f, damage / Math.max(24, maximumHealth * 0.035f)));
    }

    public static boolean ready(long now, long previous) {
        return now < previous || now - previous >= HIT_INTERVAL;
    }

    public static boolean alive(double age) {
        return Double.isFinite(age) && age >= 0 && age < DURATION;
    }

    public static void draw(double age, float strength, boolean shield, int color, int detail, Vertex output) {
        if (!alive(age) || !Float.isFinite(strength) || strength <= 0 || strength > 1) return;
        double progress = age / DURATION;
        float fade = (float) ((1 - progress) * (1 - progress));
        boolean heavy = strength >= 0.75f;
        if (!shield && age < 3.5) {
            double reach = (0.28 + strength * 0.23) * (1 - age / 5);
            for (int arm = 0; arm < 2; arm++) {
                double angle = Math.PI * (0.22 + arm * 0.5);
                double horizontal = Math.cos(angle) * reach;
                double vertical = Math.sin(angle) * reach;
                strip(-horizontal, -vertical, horizontal, vertical, 0.026 * (1 - progress), 0xfff7dd, fade * 0.86f, output);
            }
        }
        if (detail >= 2 && !shield) return;
        int fragments = detail == 1 ? 4 : heavy ? 10 : 6;
        if (!shield) {
            for (int fragment = 0; fragment < fragments; fragment++) {
                double angle = fragment * Math.PI * 2 / fragments + 0.37;
                double travel = (0.14 + Math.sqrt(progress) * (0.32 + strength * 0.3)) * (fragment % 2 == 0 ? 1 : 0.76);
                double length = 0.07 * (1 - progress) + 0.012;
                double drop = progress * progress * 0.2;
                strip(Math.cos(angle) * travel, Math.sin(angle) * travel - drop,
                        Math.cos(angle) * (travel + length), Math.sin(angle) * (travel + length) - drop,
                        0.012 * (1 - progress) + 0.003, color, fade, output);
            }
        }
        if (shield || heavy) {
            int segments = detail >= 2 ? 8 : detail == 1 ? 12 : 20;
            double radius = 0.16 + progress * (shield ? 0.46 : 0.58);
            double width = 0.018 * (1 - progress) + 0.004;
            for (int segment = 0; segment < segments; segment++) {
                double first = Math.PI * 2 * segment / segments;
                double second = Math.PI * 2 * (segment + 0.78) / segments;
                strip(Math.cos(first) * radius, Math.sin(first) * radius, Math.cos(second) * radius,
                        Math.sin(second) * radius, width, shield ? 0x9cddff : color, fade * 0.55f, output);
            }
        }
    }

    private static void strip(double startX, double startY, double endX, double endY, double width,
                              int color, float alpha, Vertex output) {
        double length = Math.hypot(endX - startX, endY - startY);
        if (length < 1.0e-8) return;
        double offsetX = -(endY - startY) / length * width;
        double offsetY = (endX - startX) / length * width;
        output.emit(startX + offsetX, startY + offsetY, color, alpha);
        output.emit(startX - offsetX, startY - offsetY, color, alpha);
        output.emit(endX - offsetX, endY - offsetY, color, alpha);
        output.emit(endX + offsetX, endY + offsetY, color, alpha);
    }
}
