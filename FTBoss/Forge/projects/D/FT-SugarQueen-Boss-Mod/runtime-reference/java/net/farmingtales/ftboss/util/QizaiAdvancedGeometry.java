/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.QizaiGeometry;
import net.farmingtales.ftboss.util.QizaiSkyNovaGeometry;
import net.farmingtales.ftboss.util.QizaiSweepRules;

public final class QizaiAdvancedGeometry {
    public static final int HIGH_BEAM = 32488;

    private QizaiAdvancedGeometry() {
    }

    public static double progress(double age, double delay, double duration) {
        if (!Double.isFinite(age + delay + duration) || duration <= 0.0) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, (age - delay) / duration));
    }

    public static void nova(double age, boolean released, double radius, double height, int riseTicks, int chargeTicks, int recoveryTicks, boolean reduced, QizaiGeometry.Sink sink) {
        QizaiSkyNovaGeometry.render(age, released, radius, height, riseTicks, chargeTicks, recoveryTicks, reduced, sink);
    }

    public static void sweep(double angle, double progress, boolean firing, double radius, double halfWidth, double lowHeight, double highHeight, boolean reduced, QizaiGeometry.Sink sink) {
        QizaiAdvancedGeometry.sweep(angle, 0.0, 2, new double[0], progress, firing, radius, halfWidth, lowHeight, highHeight, reduced, sink);
    }

    public static void sweep(double angle, double elapsed, int round, double[] lengths, double progress, boolean firing, double radius, double halfWidth, double lowHeight, double highHeight, boolean reduced, QizaiGeometry.Sink sink) {
        if (!Double.isFinite(angle + progress + radius + halfWidth + lowHeight + highHeight) || radius <= 0.0 || halfWidth <= 0.0) {
            return;
        }
        QizaiAdvancedGeometry.ring(radius, 0.4, 0.06, reduced ? 32 : 64, 10821083, firing ? 0.8f : 0.95f, sink);
        if (!firing) {
            QizaiAdvancedGeometry.ring(radius * (1.0 - Math.max(0.0, Math.min(1.0, progress))), 0.22, 0.1, reduced ? 32 : 64, 16769098, 0.9f, sink);
        }
        for (int arm = 0; arm < QizaiSweepRules.arms(round); ++arm) {
            double length;
            double heading = Math.toRadians(QizaiSweepRules.angle(angle, elapsed, round, arm));
            boolean high = QizaiSweepRules.high(round, arm);
            double height = high ? highHeight : lowHeight;
            int color = high ? 32488 : 15219456;
            double d = length = arm < lengths.length && Double.isFinite(lengths[arm]) ? Math.max(0.0, Math.min(radius, lengths[arm])) : radius;
            if (length < 0.01) continue;
            QizaiAdvancedGeometry.beam(length, heading, halfWidth, firing ? 0.18 : 0.12, height, reduced ? 4 : 8, color, firing ? 0.42f : 0.18f, sink);
            double sideX = -Math.sin(heading) * Math.max(0.0, halfWidth - 0.09);
            double sideZ = Math.cos(heading) * Math.max(0.0, halfWidth - 0.09);
            for (int side : new int[]{-1, 1}) {
                QizaiAdvancedGeometry.beam(length, heading, 0.09, firing ? 0.065 : 0.045, height, 4, color, firing ? 0.95f : 0.85f, QizaiAdvancedGeometry.translated(sideX * (double)side, 0.0, sideZ * (double)side, sink));
            }
            QizaiAdvancedGeometry.beam(length, heading, firing ? 0.18 : 0.07, firing ? 0.1 : 0.065, height, 4, 16769098, firing ? 1.0f : 0.8f, sink);
            if (length < 4.0) continue;
            double markerX = Math.cos(heading) * length * 0.65;
            double markerZ = Math.sin(heading) * length * 0.65;
            double tangentX = Math.sin(heading);
            double tangentZ = -Math.cos(heading);
            QizaiAdvancedGeometry.line(markerX, markerZ, markerX + tangentX * 1.6, markerZ + tangentZ * 1.6, 0.08, height, color, 0.8f, sink);
            QizaiAdvancedGeometry.line(markerX + tangentX * 1.6, markerZ + tangentZ * 1.6, markerX + tangentX * 0.7 + Math.cos(heading) * 0.7, markerZ + tangentZ * 0.7 + Math.sin(heading) * 0.7, 0.08, height, color, 0.8f, sink);
        }
    }

    private static void beam(double length, double heading, double halfWidth, double halfHeight, double height, int segments, int color, float alpha, QizaiGeometry.Sink sink) {
        int segment;
        double forwardX = Math.cos(heading);
        double forwardZ = Math.sin(heading);
        QizaiGeometry.Point[] near = new QizaiGeometry.Point[segments];
        QizaiGeometry.Point[] far = new QizaiGeometry.Point[segments];
        for (segment = 0; segment < segments; ++segment) {
            double angle = (double)segment * Math.PI * 2.0 / (double)segments;
            double side = Math.cos(angle) * halfWidth;
            near[segment] = new QizaiGeometry.Point(-forwardZ * side, height + Math.sin(angle) * halfHeight, forwardX * side);
            far[segment] = QizaiAdvancedGeometry.offset(near[segment], forwardX * length, 0.0, forwardZ * length);
        }
        for (segment = 0; segment < segments; ++segment) {
            int next = (segment + 1) % segments;
            sink.quad(near[segment], near[next], far[next], far[segment], color, alpha);
        }
        for (segment = 1; segment < segments - 1; segment += 2) {
            sink.quad(near[0], near[segment + 2], near[segment + 1], near[segment], color, alpha);
            sink.quad(far[0], far[segment], far[segment + 1], far[segment + 2], color, alpha);
        }
    }

    private static QizaiGeometry.Sink translated(double offsetX, double offsetY, double offsetZ, QizaiGeometry.Sink sink) {
        return (first, second, third, fourth, color, alpha) -> sink.quad(QizaiAdvancedGeometry.offset(first, offsetX, offsetY, offsetZ), QizaiAdvancedGeometry.offset(second, offsetX, offsetY, offsetZ), QizaiAdvancedGeometry.offset(third, offsetX, offsetY, offsetZ), QizaiAdvancedGeometry.offset(fourth, offsetX, offsetY, offsetZ), color, alpha);
    }

    private static QizaiGeometry.Point offset(QizaiGeometry.Point point, double offsetX, double offsetY, double offsetZ) {
        return new QizaiGeometry.Point(point.x() + offsetX, point.y() + offsetY, point.z() + offsetZ);
    }

    private static void ring(double radius, double width, double height, int segments, int color, float alpha, QizaiGeometry.Sink sink) {
        double inner = Math.max(0.0, radius - width);
        for (int segment = 0; segment < segments; ++segment) {
            double first = (double)segment * Math.PI * 2.0 / (double)segments;
            double second = (double)(segment + 1) * Math.PI * 2.0 / (double)segments;
            sink.quad(new QizaiGeometry.Point(Math.cos(first) * radius, height, Math.sin(first) * radius), new QizaiGeometry.Point(Math.cos(second) * radius, height, Math.sin(second) * radius), new QizaiGeometry.Point(Math.cos(second) * inner, height, Math.sin(second) * inner), new QizaiGeometry.Point(Math.cos(first) * inner, height, Math.sin(first) * inner), color, alpha);
        }
    }

    private static void line(double fromX, double fromZ, double toX, double toZ, double width, double height, int color, float alpha, QizaiGeometry.Sink sink) {
        double length = Math.max(0.001, Math.hypot(toX - fromX, toZ - fromZ));
        double sideX = -(toZ - fromZ) / length * width;
        double sideZ = (toX - fromX) / length * width;
        sink.quad(new QizaiGeometry.Point(fromX + sideX, height, fromZ + sideZ), new QizaiGeometry.Point(toX + sideX, height, toZ + sideZ), new QizaiGeometry.Point(toX - sideX, height, toZ - sideZ), new QizaiGeometry.Point(fromX - sideX, height, fromZ - sideZ), color, alpha);
    }
}

