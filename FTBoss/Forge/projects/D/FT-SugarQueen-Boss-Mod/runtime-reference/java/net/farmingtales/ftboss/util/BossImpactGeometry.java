/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class BossImpactGeometry {
    public static final int DURATION = 8;
    public static final int HIT_INTERVAL = 3;
    public static final int MAX_ACTIVE = 32;
    public static final int VIEW_DISTANCE = 48;

    private BossImpactGeometry() {
    }

    public static float strength(float damage, float maximumHealth) {
        if (!Float.isFinite(damage) || !Float.isFinite(maximumHealth) || damage <= 0.0f || maximumHealth <= 0.0f) {
            return 0.0f;
        }
        return Math.min(1.0f, Math.max(0.2f, damage / Math.max(24.0f, maximumHealth * 0.035f)));
    }

    public static boolean ready(long now, long previous) {
        return now < previous || now - previous >= 3L;
    }

    public static boolean alive(double age) {
        return Double.isFinite(age) && age >= 0.0 && age < 8.0;
    }

    public static void draw(double age, float strength, boolean shield, int color, int detail, Vertex output) {
        int fragments;
        boolean heavy;
        if (!BossImpactGeometry.alive(age) || !Float.isFinite(strength) || strength <= 0.0f || strength > 1.0f) {
            return;
        }
        double progress = age / 8.0;
        float fade = (float)((1.0 - progress) * (1.0 - progress));
        boolean bl = heavy = strength >= 0.75f;
        if (!shield && age < 3.5) {
            double reach = (0.28 + (double)strength * 0.23) * (1.0 - age / 5.0);
            for (int arm = 0; arm < 2; ++arm) {
                double angle = Math.PI * (0.22 + (double)arm * 0.5);
                double horizontal = Math.cos(angle) * reach;
                double vertical = Math.sin(angle) * reach;
                BossImpactGeometry.strip(-horizontal, -vertical, horizontal, vertical, 0.026 * (1.0 - progress), 0xFFF7DD, fade * 0.86f, output);
            }
        }
        if (detail >= 2 && !shield) {
            return;
        }
        int n = detail == 1 ? 4 : (fragments = heavy ? 10 : 6);
        if (!shield) {
            for (int fragment = 0; fragment < fragments; ++fragment) {
                double angle = (double)fragment * Math.PI * 2.0 / (double)fragments + 0.37;
                double travel = (0.14 + Math.sqrt(progress) * (0.32 + (double)strength * 0.3)) * (fragment % 2 == 0 ? 1.0 : 0.76);
                double length = 0.07 * (1.0 - progress) + 0.012;
                double drop = progress * progress * 0.2;
                BossImpactGeometry.strip(Math.cos(angle) * travel, Math.sin(angle) * travel - drop, Math.cos(angle) * (travel + length), Math.sin(angle) * (travel + length) - drop, 0.012 * (1.0 - progress) + 0.003, color, fade, output);
            }
        }
        if (shield || heavy) {
            int segments = detail >= 2 ? 8 : (detail == 1 ? 12 : 20);
            double radius = 0.16 + progress * (shield ? 0.46 : 0.58);
            double width = 0.018 * (1.0 - progress) + 0.004;
            for (int segment = 0; segment < segments; ++segment) {
                double first = Math.PI * 2 * (double)segment / (double)segments;
                double second = Math.PI * 2 * ((double)segment + 0.78) / (double)segments;
                BossImpactGeometry.strip(Math.cos(first) * radius, Math.sin(first) * radius, Math.cos(second) * radius, Math.sin(second) * radius, width, shield ? 10280447 : color, fade * 0.55f, output);
            }
        }
    }

    private static void strip(double startX, double startY, double endX, double endY, double width, int color, float alpha, Vertex output) {
        double length = Math.hypot(endX - startX, endY - startY);
        if (length < 1.0E-8) {
            return;
        }
        double offsetX = -(endY - startY) / length * width;
        double offsetY = (endX - startX) / length * width;
        output.emit(startX + offsetX, startY + offsetY, color, alpha);
        output.emit(startX - offsetX, startY - offsetY, color, alpha);
        output.emit(endX - offsetX, endY - offsetY, color, alpha);
        output.emit(endX + offsetX, endY + offsetY, color, alpha);
    }

    @FunctionalInterface
    public static interface Vertex {
        public void emit(double var1, double var3, int var5, float var6);
    }
}

