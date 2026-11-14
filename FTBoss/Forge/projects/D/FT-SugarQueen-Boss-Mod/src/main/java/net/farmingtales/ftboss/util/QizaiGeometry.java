package net.farmingtales.ftboss.util;

public final class QizaiGeometry {
    public static final int ROSE = 0xA51DDB;
    public static final int WHITE = 0xFFE04A;
    public static final int WARNING = 0xE83B00;
    public record Point(double x, double y, double z) {}
    @FunctionalInterface public interface Sink {
        void quad(Point first, Point second, Point third, Point fourth, int color, float alpha);
    }

    private QizaiGeometry() {}

    public static void sigil(double radius, double progress, boolean burst, boolean reduced, Sink sink) {
        if (!Double.isFinite(radius) || radius <= 0 || !Double.isFinite(progress)) return;
        progress = Math.max(0, Math.min(1, progress));
        float alpha = burst ? (float) (1 - progress) : 0.96F;
        int segments = reduced ? 32 : 48;
        ring(radius, Math.min(0.3, radius * 0.05), 0, segments, burst ? WHITE : WARNING, alpha, sink);
        ring(radius * 0.9, Math.min(0.16, radius * 0.035), progress * 0.3, segments, ROSE, alpha * 0.95F, sink);
        ring(radius * (burst ? 0.4 + progress * 0.8 : 1 - progress), Math.min(0.2, radius * 0.04), 0, segments, WHITE, alpha, sink);
        for (int point = 0; point < 5; point++) {
            double angle = point * Math.PI * 2 / 5 - Math.PI / 2;
            double next = angle + Math.PI * 4 / 5;
            line(Math.cos(angle) * radius * 0.72, Math.sin(angle) * radius * 0.72,
                    Math.cos(next) * radius * 0.72, Math.sin(next) * radius * 0.72, 0.045, ROSE, alpha * 0.75F, sink);
            double marker = radius - 0.22;
            line(Math.cos(angle) * marker, Math.sin(angle) * marker,
                    Math.cos(angle) * radius, Math.sin(angle) * radius, 0.12, WHITE, alpha, sink);
        }
    }

    public static void cross(double progress, boolean burst, Sink sink) {
        if (!Double.isFinite(progress)) return;
        progress = Math.max(0, Math.min(1, progress));
        float alpha = burst ? (float) (1 - progress) : 0.85F;
        double length = QizaiRules.CROSS_LENGTH;
        double width = QizaiRules.CROSS_WIDTH;
        line(-length, 0, length, 0, width, ROSE, alpha * (burst ? 0.65F : 0.12F), sink);
        line(0, -length, 0, length, width, ROSE, alpha * (burst ? 0.65F : 0.12F), sink);
        for (int sign : new int[]{-1, 1}) {
            line(-length, sign * width, length, sign * width, 0.1, WARNING, alpha, sink);
            line(sign * width, -length, sign * width, length, 0.1, WARNING, alpha, sink);
        }
        line(-length, 0, length, 0, burst ? 0.18 : 0.07, WHITE, alpha, sink);
        line(0, -length, 0, length, burst ? 0.18 : 0.07, WHITE, alpha, sink);
        sigil(0.6, progress, burst, true, sink);
    }

    private static void ring(double radius, double width, double angle, int segments, int color, float alpha, Sink sink) {
        for (int segment = 0; segment < segments; segment++) {
            double start = angle + segment * Math.PI * 2 / segments;
            double end = angle + (segment + 1) * Math.PI * 2 / segments;
            double inner = Math.max(0, radius - width);
            sink.quad(new Point(Math.cos(start) * radius, 0, Math.sin(start) * radius),
                    new Point(Math.cos(end) * radius, 0, Math.sin(end) * radius),
                    new Point(Math.cos(end) * inner, 0, Math.sin(end) * inner),
                    new Point(Math.cos(start) * inner, 0, Math.sin(start) * inner), color, alpha);
        }
    }

    private static void line(double fromX, double fromZ, double toX, double toZ, double width, int color, float alpha, Sink sink) {
        double length = Math.max(0.001, Math.hypot(toX - fromX, toZ - fromZ));
        double sideX = -(toZ - fromZ) / length * width;
        double sideZ = (toX - fromX) / length * width;
        sink.quad(new Point(fromX + sideX, 0.005, fromZ + sideZ), new Point(toX + sideX, 0.005, toZ + sideZ),
                new Point(toX - sideX, 0.005, toZ - sideZ), new Point(fromX - sideX, 0.005, fromZ - sideZ), color, alpha);
    }

    public static void crystal(double radius, double height, double rotation, float alpha, Sink sink) {
        Point top = new Point(0, height, 0);
        Point bottom = new Point(0, -height, 0);
        for (int face = 0; face < 4; face++) {
            double angle = rotation + face * Math.PI / 2;
            Point first = new Point(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            Point second = new Point(Math.cos(angle + Math.PI / 2) * radius, 0, Math.sin(angle + Math.PI / 2) * radius);
            sink.quad(top, first, second, top, face % 2 == 0 ? WHITE : ROSE, alpha);
            sink.quad(bottom, second, first, bottom, face % 2 == 0 ? ROSE : WARNING, alpha);
        }
    }
}
