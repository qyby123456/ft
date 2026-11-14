/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class QizaiGeometry {
    public static final int ROSE = 10821083;
    public static final int WHITE = 16769098;
    public static final int WARNING = 15219456;

    private QizaiGeometry() {
    }

    public static void sigil(double radius, double progress, boolean burst, boolean reduced, Sink sink) {
        if (!Double.isFinite(radius) || radius <= 0.0 || !Double.isFinite(progress)) {
            return;
        }
        progress = Math.max(0.0, Math.min(1.0, progress));
        float alpha = burst ? (float)(1.0 - progress) : 0.96f;
        int segments = reduced ? 32 : 48;
        QizaiGeometry.ring(radius, Math.min(0.3, radius * 0.05), 0.0, segments, burst ? 16769098 : 15219456, alpha, sink);
        QizaiGeometry.ring(radius * 0.9, Math.min(0.16, radius * 0.035), progress * 0.3, segments, 10821083, alpha * 0.95f, sink);
        QizaiGeometry.ring(radius * (burst ? 0.4 + progress * 0.8 : 1.0 - progress), Math.min(0.2, radius * 0.04), 0.0, segments, 16769098, alpha, sink);
        for (int point = 0; point < 5; ++point) {
            double angle = (double)point * Math.PI * 2.0 / 5.0 - 1.5707963267948966;
            double next = angle + 2.5132741228718345;
            QizaiGeometry.line(Math.cos(angle) * radius * 0.72, Math.sin(angle) * radius * 0.72, Math.cos(next) * radius * 0.72, Math.sin(next) * radius * 0.72, 0.045, 10821083, alpha * 0.75f, sink);
            double marker = radius - 0.22;
            QizaiGeometry.line(Math.cos(angle) * marker, Math.sin(angle) * marker, Math.cos(angle) * radius, Math.sin(angle) * radius, 0.12, 16769098, alpha, sink);
        }
    }

    public static void cross(double progress, boolean burst, Sink sink) {
        if (!Double.isFinite(progress)) {
            return;
        }
        progress = Math.max(0.0, Math.min(1.0, progress));
        float alpha = burst ? (float)(1.0 - progress) : 0.85f;
        double length = 12.0;
        double width = 1.4;
        QizaiGeometry.line(-length, 0.0, length, 0.0, width, 10821083, alpha * (burst ? 0.65f : 0.12f), sink);
        QizaiGeometry.line(0.0, -length, 0.0, length, width, 10821083, alpha * (burst ? 0.65f : 0.12f), sink);
        for (int sign : new int[]{-1, 1}) {
            QizaiGeometry.line(-length, (double)sign * width, length, (double)sign * width, 0.1, 15219456, alpha, sink);
            QizaiGeometry.line((double)sign * width, -length, (double)sign * width, length, 0.1, 15219456, alpha, sink);
        }
        QizaiGeometry.line(-length, 0.0, length, 0.0, burst ? 0.18 : 0.07, 16769098, alpha, sink);
        QizaiGeometry.line(0.0, -length, 0.0, length, burst ? 0.18 : 0.07, 16769098, alpha, sink);
        QizaiGeometry.sigil(0.6, progress, burst, true, sink);
    }

    private static void ring(double radius, double width, double angle, int segments, int color, float alpha, Sink sink) {
        for (int segment = 0; segment < segments; ++segment) {
            double start = angle + (double)segment * Math.PI * 2.0 / (double)segments;
            double end = angle + (double)(segment + 1) * Math.PI * 2.0 / (double)segments;
            double inner = Math.max(0.0, radius - width);
            sink.quad(new Point(Math.cos(start) * radius, 0.0, Math.sin(start) * radius), new Point(Math.cos(end) * radius, 0.0, Math.sin(end) * radius), new Point(Math.cos(end) * inner, 0.0, Math.sin(end) * inner), new Point(Math.cos(start) * inner, 0.0, Math.sin(start) * inner), color, alpha);
        }
    }

    private static void line(double fromX, double fromZ, double toX, double toZ, double width, int color, float alpha, Sink sink) {
        double length = Math.max(0.001, Math.hypot(toX - fromX, toZ - fromZ));
        double sideX = -(toZ - fromZ) / length * width;
        double sideZ = (toX - fromX) / length * width;
        sink.quad(new Point(fromX + sideX, 0.005, fromZ + sideZ), new Point(toX + sideX, 0.005, toZ + sideZ), new Point(toX - sideX, 0.005, toZ - sideZ), new Point(fromX - sideX, 0.005, fromZ - sideZ), color, alpha);
    }

    public static void crystal(double radius, double height, double rotation, float alpha, Sink sink) {
        Point top = new Point(0.0, height, 0.0);
        Point bottom = new Point(0.0, -height, 0.0);
        for (int face = 0; face < 4; ++face) {
            double angle = rotation + (double)face * Math.PI / 2.0;
            Point first = new Point(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
            Point second = new Point(Math.cos(angle + 1.5707963267948966) * radius, 0.0, Math.sin(angle + 1.5707963267948966) * radius);
            sink.quad(top, first, second, top, face % 2 == 0 ? 16769098 : 10821083, alpha);
            sink.quad(bottom, second, first, bottom, face % 2 == 0 ? 10821083 : 15219456, alpha);
        }
    }

    @FunctionalInterface
    public static interface Sink {
        public void quad(Point var1, Point var2, Point var3, Point var4, int var5, float var6);
    }

    public record Point(double x, double y, double z) {
    }
}

