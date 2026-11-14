package net.farmingtales.ftboss.util;

/** Client-safe geometry for the moving Manor recall boundary around the current boss. */
public final class ManorRecallBoundaryGeometry {
    public static final double RADIUS = ManorTeleportRules.STRAY_FROM_BOSS;
    public static final int MAX_QUADS = 256;
    public static final int DANGER = 0xFF355D;
    public static final int WARNING = 0xFFD166;
    public static final int HOT = 0xFFF1CF;

    public record Point(double x, double y, double z) {}

    @FunctionalInterface
    public interface Sink {
        void quad(Point first, Point second, Point third, Point fourth, int color, float alpha);
    }

    private ManorRecallBoundaryGeometry() {}

    /** Strengthens the warning only in the final six blocks before recall. */
    public static float urgency(double distance, double radius) {
        if (!Double.isFinite(distance) || !Double.isFinite(radius) || distance < 0 || radius <= 0) return 0;
        double progress = clamp((distance - Math.max(0, radius - 6.0)) / Math.min(6.0, radius));
        return (float) (progress * progress * (3.0 - 2.0 * progress));
    }

    public static int draw(double age, double radius, boolean reduced, float urgency, Sink output) {
        if (output == null || !Double.isFinite(age) || age < 0 || !Double.isFinite(radius)
                || radius < 2 || radius > 128 || !Float.isFinite(urgency) || urgency < 0 || urgency > 1) return 0;
        Mesh mesh = new Mesh(output);
        int segments = reduced ? 48 : 72;
        int markers = reduced ? 8 : 12;
        double rotation = age * 0.008;
        float pulse = (float) (0.86 + 0.14 * Math.sin(age * 0.22));
        float dangerAlpha = (0.28F + urgency * 0.34F) * pulse;
        float warningAlpha = (0.22F + urgency * 0.28F) * pulse;

        for (int segment = 0; segment < segments; segment++) {
            if (segment % 6 == 5) continue; // Rotating gaps keep the ring readable instead of becoming a flat disc.
            double start = rotation + segment * Math.PI * 2.0 / segments;
            double end = rotation + (segment + 0.78) * Math.PI * 2.0 / segments;
            mesh.ring(start, end, radius, 0.22, 0.08, DANGER, dangerAlpha);
            mesh.wall(start, end, radius, 0.06, 0.72, DANGER, dangerAlpha * 0.32F);
            if ((segment & 1) == 0) {
                mesh.ring(start, end, radius - 0.38, 0.065, 0.105, WARNING, warningAlpha);
            }
        }

        for (int marker = 0; marker < markers; marker++) {
            double angle = rotation * 0.35 + marker * Math.PI * 2.0 / markers;
            double wave = 0.5 + 0.5 * Math.sin(age * 0.18 + marker * 0.9);
            double height = 1.0 + wave * 1.25;
            mesh.pillar(angle, radius, height, 0.18, false, DANGER, dangerAlpha * 0.78F);
            mesh.pillar(angle, radius, height * 0.78, 0.10, true, HOT, warningAlpha * 0.72F);

            Point tip = polar(radius - 1.65, angle, 0.115);
            Point left = polar(radius - 0.72, angle - 0.055, 0.115);
            Point right = polar(radius - 0.72, angle + 0.055, 0.115);
            mesh.strip(left, tip, 0.075, WARNING, warningAlpha);
            mesh.strip(right, tip, 0.075, WARNING, warningAlpha);
        }
        return mesh.count;
    }

    private static Point polar(double radius, double angle, double y) {
        return new Point(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private static final class Mesh {
        private final Sink output;
        private int count;

        private Mesh(Sink output) { this.output = output; }

        private void face(Point first, Point second, Point third, Point fourth, int color, float alpha) {
            if (++count > MAX_QUADS) throw new IllegalStateException("Manor recall boundary over budget");
            output.quad(first, second, third, fourth, color, Math.max(0, Math.min(1, alpha)));
        }

        private void ring(double start, double end, double radius, double halfWidth, double y,
                          int color, float alpha) {
            face(polar(radius + halfWidth, start, y), polar(radius + halfWidth, end, y),
                    polar(radius - halfWidth, end, y), polar(radius - halfWidth, start, y), color, alpha);
        }

        private void wall(double start, double end, double radius, double bottom, double top,
                          int color, float alpha) {
            Point first = polar(radius, start, bottom);
            Point second = polar(radius, end, bottom);
            face(first, second, new Point(second.x, top, second.z), new Point(first.x, top, first.z), color, alpha);
        }

        private void pillar(double angle, double radius, double height, double halfWidth, boolean radial,
                            int color, float alpha) {
            double centerX = Math.cos(angle) * radius, centerZ = Math.sin(angle) * radius;
            double sideX = (radial ? Math.cos(angle) : -Math.sin(angle)) * halfWidth;
            double sideZ = (radial ? Math.sin(angle) : Math.cos(angle)) * halfWidth;
            double bottom = 0.12;
            face(new Point(centerX - sideX, bottom, centerZ - sideZ),
                    new Point(centerX + sideX, bottom, centerZ + sideZ),
                    new Point(centerX + sideX, height, centerZ + sideZ),
                    new Point(centerX - sideX, height, centerZ - sideZ), color, alpha);
        }

        private void strip(Point start, Point end, double halfWidth, int color, float alpha) {
            double dx = end.x - start.x, dz = end.z - start.z;
            double length = Math.hypot(dx, dz);
            if (length < 1.0E-6) return;
            double sideX = -dz / length * halfWidth, sideZ = dx / length * halfWidth;
            face(new Point(start.x + sideX, start.y, start.z + sideZ),
                    new Point(end.x + sideX, end.y, end.z + sideZ),
                    new Point(end.x - sideX, end.y, end.z - sideZ),
                    new Point(start.x - sideX, start.y, start.z - sideZ), color, alpha);
        }
    }
}
