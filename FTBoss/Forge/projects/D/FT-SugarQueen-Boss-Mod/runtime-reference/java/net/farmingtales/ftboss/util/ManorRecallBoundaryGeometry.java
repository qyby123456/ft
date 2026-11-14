/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class ManorRecallBoundaryGeometry {
    public static final double RADIUS = 76.0;
    public static final int MAX_QUADS = 256;
    public static final int DANGER = 16725341;
    public static final int WARNING = 16765286;
    public static final int HOT = 0xFFF1CF;

    private ManorRecallBoundaryGeometry() {
    }

    public static float urgency(double distance, double radius) {
        if (!Double.isFinite(distance) || !Double.isFinite(radius) || distance < 0.0 || radius <= 0.0) {
            return 0.0f;
        }
        double progress = ManorRecallBoundaryGeometry.clamp((distance - Math.max(0.0, radius - 6.0)) / Math.min(6.0, radius));
        return (float)(progress * progress * (3.0 - 2.0 * progress));
    }

    public static int draw(double age, double radius, boolean reduced, float urgency, Sink output) {
        if (output == null || !Double.isFinite(age) || age < 0.0 || !Double.isFinite(radius) || radius < 2.0 || radius > 128.0 || !Float.isFinite(urgency) || urgency < 0.0f || urgency > 1.0f) {
            return 0;
        }
        Mesh mesh = new Mesh(output);
        int segments = reduced ? 48 : 72;
        int markers = reduced ? 8 : 12;
        double rotation = age * 0.008;
        float pulse = (float)(0.86 + 0.14 * Math.sin(age * 0.22));
        float dangerAlpha = (0.28f + urgency * 0.34f) * pulse;
        float warningAlpha = (0.22f + urgency * 0.28f) * pulse;
        for (int segment = 0; segment < segments; ++segment) {
            if (segment % 6 == 5) continue;
            double start = rotation + (double)segment * Math.PI * 2.0 / (double)segments;
            double end = rotation + ((double)segment + 0.78) * Math.PI * 2.0 / (double)segments;
            mesh.ring(start, end, radius, 0.22, 0.08, 16725341, dangerAlpha);
            mesh.wall(start, end, radius, 0.06, 0.72, 16725341, dangerAlpha * 0.32f);
            if ((segment & 1) != 0) continue;
            mesh.ring(start, end, radius - 0.38, 0.065, 0.105, 16765286, warningAlpha);
        }
        for (int marker = 0; marker < markers; ++marker) {
            double angle = rotation * 0.35 + (double)marker * Math.PI * 2.0 / (double)markers;
            double wave = 0.5 + 0.5 * Math.sin(age * 0.18 + (double)marker * 0.9);
            double height = 1.0 + wave * 1.25;
            mesh.pillar(angle, radius, height, 0.18, false, 16725341, dangerAlpha * 0.78f);
            mesh.pillar(angle, radius, height * 0.78, 0.1, true, 0xFFF1CF, warningAlpha * 0.72f);
            Point tip = ManorRecallBoundaryGeometry.polar(radius - 1.65, angle, 0.115);
            Point left = ManorRecallBoundaryGeometry.polar(radius - 0.72, angle - 0.055, 0.115);
            Point right = ManorRecallBoundaryGeometry.polar(radius - 0.72, angle + 0.055, 0.115);
            mesh.strip(left, tip, 0.075, 16765286, warningAlpha);
            mesh.strip(right, tip, 0.075, 16765286, warningAlpha);
        }
        return mesh.count;
    }

    private static Point polar(double radius, double angle, double y) {
        return new Point(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static final class Mesh {
        private final Sink output;
        private int count;

        private Mesh(Sink output) {
            this.output = output;
        }

        private void face(Point first, Point second, Point third, Point fourth, int color, float alpha) {
            if (++this.count > 256) {
                throw new IllegalStateException("Manor recall boundary over budget");
            }
            this.output.quad(first, second, third, fourth, color, Math.max(0.0f, Math.min(1.0f, alpha)));
        }

        private void ring(double start, double end, double radius, double halfWidth, double y, int color, float alpha) {
            this.face(ManorRecallBoundaryGeometry.polar(radius + halfWidth, start, y), ManorRecallBoundaryGeometry.polar(radius + halfWidth, end, y), ManorRecallBoundaryGeometry.polar(radius - halfWidth, end, y), ManorRecallBoundaryGeometry.polar(radius - halfWidth, start, y), color, alpha);
        }

        private void wall(double start, double end, double radius, double bottom, double top, int color, float alpha) {
            Point first = ManorRecallBoundaryGeometry.polar(radius, start, bottom);
            Point second = ManorRecallBoundaryGeometry.polar(radius, end, bottom);
            this.face(first, second, new Point(second.x, top, second.z), new Point(first.x, top, first.z), color, alpha);
        }

        private void pillar(double angle, double radius, double height, double halfWidth, boolean radial, int color, float alpha) {
            double centerX = Math.cos(angle) * radius;
            double centerZ = Math.sin(angle) * radius;
            double sideX = (radial ? Math.cos(angle) : -Math.sin(angle)) * halfWidth;
            double sideZ = (radial ? Math.sin(angle) : Math.cos(angle)) * halfWidth;
            double bottom = 0.12;
            this.face(new Point(centerX - sideX, bottom, centerZ - sideZ), new Point(centerX + sideX, bottom, centerZ + sideZ), new Point(centerX + sideX, height, centerZ + sideZ), new Point(centerX - sideX, height, centerZ - sideZ), color, alpha);
        }

        private void strip(Point start, Point end, double halfWidth, int color, float alpha) {
            double dx = end.x - start.x;
            double dz = end.z - start.z;
            double length = Math.hypot(dx, dz);
            if (length < 1.0E-6) {
                return;
            }
            double sideX = -dz / length * halfWidth;
            double sideZ = dx / length * halfWidth;
            this.face(new Point(start.x + sideX, start.y, start.z + sideZ), new Point(end.x + sideX, end.y, end.z + sideZ), new Point(end.x - sideX, end.y, end.z - sideZ), new Point(start.x - sideX, start.y, start.z - sideZ), color, alpha);
        }
    }

    @FunctionalInterface
    public static interface Sink {
        public void quad(Point var1, Point var2, Point var3, Point var4, int var5, float var6);
    }

    public record Point(double x, double y, double z) {
    }
}

