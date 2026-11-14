package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.QizaiGeometry.Point;
import net.farmingtales.ftboss.util.QizaiGeometry.Sink;

public final class QizaiAdvancedGeometry {
    public static final int HIGH_BEAM = 0x007EE8;
    private QizaiAdvancedGeometry() {}

    public static double progress(double age, double delay, double duration) {
        if (!Double.isFinite(age + delay + duration) || duration <= 0) return 0;
        return Math.max(0, Math.min(1, (age - delay) / duration));
    }

    public static void nova(double age, boolean released, double radius, double height,
                            int riseTicks, int chargeTicks, int recoveryTicks, boolean reduced, Sink sink) {
        QizaiSkyNovaGeometry.render(age, released, radius, height, riseTicks, chargeTicks, recoveryTicks, reduced, sink);
    }

    public static void sweep(double angle, double progress, boolean firing, double radius,
                             double halfWidth, double lowHeight, double highHeight, boolean reduced, Sink sink) {
        sweep(angle, 0, 2, new double[0], progress, firing, radius, halfWidth, lowHeight, highHeight, reduced, sink);
    }

    public static void sweep(double angle, double elapsed, int round, double[] lengths, double progress,
                             boolean firing, double radius, double halfWidth, double lowHeight, double highHeight,
                             boolean reduced, Sink sink) {
        if (!Double.isFinite(angle + progress + radius + halfWidth + lowHeight + highHeight)
                || radius <= 0 || halfWidth <= 0) return;
        ring(radius, 0.4, 0.06, reduced ? 32 : 64, QizaiGeometry.ROSE, firing ? 0.8F : 0.95F, sink);
        if (!firing) ring(radius * (1 - Math.max(0, Math.min(1, progress))), 0.22,
                0.1, reduced ? 32 : 64, QizaiGeometry.WHITE, 0.9F, sink);
        for (int arm = 0; arm < QizaiSweepRules.arms(round); arm++) {
            double heading = Math.toRadians(QizaiSweepRules.angle(angle, elapsed, round, arm));
            boolean high = QizaiSweepRules.high(round, arm);
            double height = high ? highHeight : lowHeight;
            int color = high ? HIGH_BEAM : QizaiGeometry.WARNING;
            double length = arm < lengths.length && Double.isFinite(lengths[arm]) ? Math.max(0, Math.min(radius, lengths[arm])) : radius;
            if (length < 0.01) continue;
            beam(length, heading, halfWidth, firing ? 0.18 : 0.12, height, reduced ? 4 : 8,
                    color, firing ? 0.42F : 0.18F, sink);
            double sideX = -Math.sin(heading) * Math.max(0, halfWidth - 0.09);
            double sideZ = Math.cos(heading) * Math.max(0, halfWidth - 0.09);
            for (int side : new int[]{-1, 1}) {
                beam(length, heading, 0.09, firing ? 0.065 : 0.045, height, 4,
                        color, firing ? 0.95F : 0.85F, translated(sideX * side, 0, sideZ * side, sink));
            }
            beam(length, heading, firing ? 0.18 : 0.07, firing ? 0.1 : 0.065, height, 4,
                    QizaiGeometry.WHITE, firing ? 1.0F : 0.8F, sink);
            if (length < 4) continue;
            double markerX = Math.cos(heading) * length * 0.65;
            double markerZ = Math.sin(heading) * length * 0.65;
            double tangentX = Math.sin(heading), tangentZ = -Math.cos(heading);
            line(markerX, markerZ, markerX + tangentX * 1.6, markerZ + tangentZ * 1.6,
                    0.08, height, color, 0.8F, sink);
            line(markerX + tangentX * 1.6, markerZ + tangentZ * 1.6,
                    markerX + tangentX * 0.7 + Math.cos(heading) * 0.7,
                    markerZ + tangentZ * 0.7 + Math.sin(heading) * 0.7, 0.08, height, color, 0.8F, sink);
        }
    }

    private static void beam(double length, double heading, double halfWidth, double halfHeight,
                             double height, int segments, int color, float alpha, Sink sink) {
        double forwardX = Math.cos(heading), forwardZ = Math.sin(heading);
        Point[] near = new Point[segments];
        Point[] far = new Point[segments];
        for (int segment = 0; segment < segments; segment++) {
            double angle = segment * Math.PI * 2 / segments;
            double side = Math.cos(angle) * halfWidth;
            near[segment] = new Point(-forwardZ * side, height + Math.sin(angle) * halfHeight, forwardX * side);
            far[segment] = offset(near[segment], forwardX * length, 0, forwardZ * length);
        }
        for (int segment = 0; segment < segments; segment++) {
            int next = (segment + 1) % segments;
            sink.quad(near[segment], near[next], far[next], far[segment], color, alpha);
        }
        for (int segment = 1; segment < segments - 1; segment += 2) {
            sink.quad(near[0], near[segment + 2], near[segment + 1], near[segment], color, alpha);
            sink.quad(far[0], far[segment], far[segment + 1], far[segment + 2], color, alpha);
        }
    }

    private static Sink translated(double offsetX, double offsetY, double offsetZ, Sink sink) {
        return (first, second, third, fourth, color, alpha) -> sink.quad(
                offset(first, offsetX, offsetY, offsetZ), offset(second, offsetX, offsetY, offsetZ),
                offset(third, offsetX, offsetY, offsetZ), offset(fourth, offsetX, offsetY, offsetZ), color, alpha);
    }

    private static Point offset(Point point, double offsetX, double offsetY, double offsetZ) {
        return new Point(point.x() + offsetX, point.y() + offsetY, point.z() + offsetZ);
    }

    private static void ring(double radius, double width, double height, int segments, int color, float alpha, Sink sink) {
        double inner = Math.max(0, radius - width);
        for (int segment = 0; segment < segments; segment++) {
            double first = segment * Math.PI * 2 / segments, second = (segment + 1) * Math.PI * 2 / segments;
            sink.quad(new Point(Math.cos(first) * radius, height, Math.sin(first) * radius),
                    new Point(Math.cos(second) * radius, height, Math.sin(second) * radius),
                    new Point(Math.cos(second) * inner, height, Math.sin(second) * inner),
                    new Point(Math.cos(first) * inner, height, Math.sin(first) * inner), color, alpha);
        }
    }

    private static void line(double fromX, double fromZ, double toX, double toZ, double width,
                             double height, int color, float alpha, Sink sink) {
        double length = Math.max(0.001, Math.hypot(toX - fromX, toZ - fromZ));
        double sideX = -(toZ - fromZ) / length * width, sideZ = (toX - fromX) / length * width;
        sink.quad(new Point(fromX + sideX, height, fromZ + sideZ), new Point(toX + sideX, height, toZ + sideZ),
                new Point(toX - sideX, height, toZ - sideZ), new Point(fromX - sideX, height, fromZ - sideZ), color, alpha);
    }

}
