/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.util.QizaiGeometry;

public final class QizaiSkyNovaModel {
    public static final int SHELL = 4534112;
    public static final int ARMOR = 10308982;
    public static final int EDGE = 15910284;
    public static final int CORE = 16773314;
    public static final int EMBER = 15692698;
    public static final double TIP = -3.4;
    private static final QizaiGeometry.Point[] PETAL = new QizaiGeometry.Point[]{new QizaiGeometry.Point(1.15, -1.25, 0.0), new QizaiGeometry.Point(3.8, 2.1, 0.0), new QizaiGeometry.Point(3.2, 3.3, 0.0), new QizaiGeometry.Point(1.5, 0.65, 0.0)};

    private QizaiSkyNovaModel() {
    }

    public static void render(double time, double charge, double rupture, boolean reduced, float alpha, QizaiGeometry.Sink sink) {
        if (!Double.isFinite(time + charge + rupture) || !Float.isFinite(alpha) || alpha <= 0.0f) {
            return;
        }
        charge = QizaiSkyNovaModel.clamp(charge);
        rupture = QizaiSkyNovaModel.clamp(rupture);
        alpha = Math.min(1.0f, alpha);
        double closing = QizaiSkyNovaModel.smooth((charge - 0.84) / 0.16);
        double opening = QizaiSkyNovaModel.smooth(charge / 0.6) * (1.0 - closing);
        double spin = time * 0.012;
        QizaiGeometry.Sink root = QizaiSkyNovaModel.transform(0.0, 0.0, 0.0, spin, 0.0, 0.0, 1.0, sink);
        QizaiSkyNovaModel.core(alpha, root);
        for (int petal = 0; petal < 6; ++petal) {
            double heading = (double)petal * Math.PI / 3.0;
            double spread = opening * 0.55 + rupture * 4.0;
            QizaiGeometry.Sink wing = QizaiSkyNovaModel.transform(Math.cos(heading) * spread, rupture * 2.0, Math.sin(heading) * spread, -heading, 0.0, -opening * 0.24 - rupture * 0.8, 1.0, root);
            QizaiSkyNovaModel.plate(alpha, wing);
        }
        int segments = reduced ? 12 : 24;
        double orbit = 4.35 + opening * 0.65 + rupture * 2.0;
        QizaiSkyNovaModel.hoop(orbit, 0.1, 0.1, segments, alpha * 0.9f, QizaiSkyNovaModel.transform(0.0, 0.45, 0.0, -spin * 1.8, 0.52, 0.0, 1.0, sink));
        QizaiSkyNovaModel.hoop(orbit * 0.88, 0.075, 0.08, segments, alpha * 0.75f, QizaiSkyNovaModel.transform(0.0, 0.85, 0.0, spin * 1.5, -0.68, 0.0, 1.0, sink));
        for (int jewel = 0; jewel < 6; ++jewel) {
            double heading = (double)jewel * Math.PI / 3.0 - spin * 1.8;
            double orbitY = 0.45 + Math.sin(heading) * orbit * Math.sin(0.52);
            QizaiSkyNovaModel.fragment(0.17, 0.4, alpha, QizaiSkyNovaModel.transform(Math.cos(heading) * orbit, orbitY, Math.sin(heading) * orbit * Math.cos(0.52), heading, 0.0, 0.0, 1.0, sink));
        }
    }

    private static void core(float alpha, QizaiGeometry.Sink sink) {
        double[] heights = new double[]{-3.4, -1.15, 0.35, 1.65, 2.65};
        double[] widths = new double[]{0.0, 0.75, 1.18, 0.58, 0.0};
        int[] colors = new int[]{15692698, 16773314, 16773314, 15910284};
        for (int band = 0; band < heights.length - 1; ++band) {
            for (int facet = 0; facet < 8; ++facet) {
                double first = (double)facet * Math.PI / 4.0;
                double second = (double)(facet + 1) * Math.PI / 4.0;
                QizaiSkyNovaModel.face(QizaiSkyNovaModel.radial(widths[band], heights[band], first), QizaiSkyNovaModel.radial(widths[band], heights[band], second), QizaiSkyNovaModel.radial(widths[band + 1], heights[band + 1], second), QizaiSkyNovaModel.radial(widths[band + 1], heights[band + 1], first), colors[band], alpha, sink);
            }
        }
    }

    private static void plate(float alpha, QizaiGeometry.Sink sink) {
        int corner;
        QizaiGeometry.Point[] front = new QizaiGeometry.Point[4];
        QizaiGeometry.Point[] back = new QizaiGeometry.Point[4];
        QizaiGeometry.Point[] inset = new QizaiGeometry.Point[4];
        double centerX = 2.4125;
        double centerY = 1.2;
        for (corner = 0; corner < 4; ++corner) {
            QizaiGeometry.Point point = PETAL[corner];
            front[corner] = new QizaiGeometry.Point(point.x(), point.y(), -0.23);
            back[corner] = new QizaiGeometry.Point(point.x(), point.y(), 0.23);
            inset[corner] = new QizaiGeometry.Point(centerX + (point.x() - centerX) * 0.81, centerY + (point.y() - centerY) * 0.81, -0.32);
        }
        QizaiSkyNovaModel.face(back[3], back[2], back[1], back[0], 4534112, alpha, sink);
        QizaiSkyNovaModel.face(inset[0], inset[1], inset[2], inset[3], 10308982, alpha, sink);
        for (corner = 0; corner < 4; ++corner) {
            int next = (corner + 1) % 4;
            QizaiSkyNovaModel.face(front[corner], back[corner], back[next], front[next], 4534112, alpha, sink);
            QizaiSkyNovaModel.face(front[corner], front[next], inset[next], inset[corner], 15910284, alpha, sink);
        }
        QizaiSkyNovaModel.face(new QizaiGeometry.Point(1.62, 0.23, -0.325), new QizaiGeometry.Point(1.74, 0.25, -0.325), new QizaiGeometry.Point(3.12, 2.61, -0.325), new QizaiGeometry.Point(2.94, 2.43, -0.325), 16773314, alpha, sink);
    }

    private static void hoop(double radius, double width, double thickness, int segments, float alpha, QizaiGeometry.Sink sink) {
        for (int segment = 0; segment < segments; ++segment) {
            double first = (double)segment * Math.PI * 2.0 / (double)segments;
            double second = ((double)segment + 0.84) * Math.PI * 2.0 / (double)segments;
            QizaiGeometry.Point outerFirst = QizaiSkyNovaModel.radial(radius, thickness, first);
            QizaiGeometry.Point outerSecond = QizaiSkyNovaModel.radial(radius, thickness, second);
            QizaiGeometry.Point innerFirst = QizaiSkyNovaModel.radial(radius - width, thickness, first);
            QizaiGeometry.Point innerSecond = QizaiSkyNovaModel.radial(radius - width, thickness, second);
            QizaiSkyNovaModel.face(outerFirst, outerSecond, innerSecond, innerFirst, 15910284, alpha, sink);
            QizaiSkyNovaModel.face(QizaiSkyNovaModel.radial(radius, -thickness, second), QizaiSkyNovaModel.radial(radius, -thickness, first), QizaiSkyNovaModel.radial(radius - width, -thickness, first), QizaiSkyNovaModel.radial(radius - width, -thickness, second), 4534112, alpha, sink);
            QizaiSkyNovaModel.face(outerSecond, outerFirst, QizaiSkyNovaModel.radial(radius, -thickness, first), QizaiSkyNovaModel.radial(radius, -thickness, second), 10308982, alpha, sink);
            QizaiSkyNovaModel.face(innerFirst, innerSecond, QizaiSkyNovaModel.radial(radius - width, -thickness, second), QizaiSkyNovaModel.radial(radius - width, -thickness, first), 15910284, alpha, sink);
            QizaiSkyNovaModel.face(outerFirst, innerFirst, QizaiSkyNovaModel.radial(radius - width, -thickness, first), QizaiSkyNovaModel.radial(radius, -thickness, first), 15910284, alpha, sink);
            QizaiSkyNovaModel.face(innerSecond, outerSecond, QizaiSkyNovaModel.radial(radius, -thickness, second), QizaiSkyNovaModel.radial(radius - width, -thickness, second), 15910284, alpha, sink);
        }
    }

    public static void fragment(double width, double length, float alpha, QizaiGeometry.Sink sink) {
        for (int facet = 0; facet < 4; ++facet) {
            QizaiGeometry.Point first = QizaiSkyNovaModel.radial(width, 0.0, (double)facet * Math.PI / 2.0);
            QizaiGeometry.Point second = QizaiSkyNovaModel.radial(width, 0.0, (double)(facet + 1) * Math.PI / 2.0);
            QizaiSkyNovaModel.face(first, second, new QizaiGeometry.Point(0.0, length, 0.0), new QizaiGeometry.Point(0.0, length, 0.0), 15910284, alpha, sink);
            QizaiSkyNovaModel.face(second, first, new QizaiGeometry.Point(0.0, -length, 0.0), new QizaiGeometry.Point(0.0, -length, 0.0), 15692698, alpha, sink);
        }
    }

    public static QizaiGeometry.Sink transform(double offsetX, double offsetY, double offsetZ, double yaw, double pitch, double roll, double scale, QizaiGeometry.Sink sink) {
        double yawCos = Math.cos(yaw);
        double yawSin = Math.sin(yaw);
        double pitchCos = Math.cos(pitch);
        double pitchSin = Math.sin(pitch);
        double rollCos = Math.cos(roll);
        double rollSin = Math.sin(roll);
        double[] matrix = new double[]{(rollCos * yawCos + rollSin * pitchSin * yawSin) * scale, (-rollSin * yawCos + rollCos * pitchSin * yawSin) * scale, pitchCos * yawSin * scale, rollSin * pitchCos * scale, rollCos * pitchCos * scale, -pitchSin * scale, (-rollCos * yawSin + rollSin * pitchSin * yawCos) * scale, (rollSin * yawSin + rollCos * pitchSin * yawCos) * scale, pitchCos * yawCos * scale};
        return (first, second, third, fourth, color, alpha) -> sink.quad(QizaiSkyNovaModel.pose(first, offsetX, offsetY, offsetZ, matrix), QizaiSkyNovaModel.pose(second, offsetX, offsetY, offsetZ, matrix), QizaiSkyNovaModel.pose(third, offsetX, offsetY, offsetZ, matrix), QizaiSkyNovaModel.pose(fourth, offsetX, offsetY, offsetZ, matrix), color, alpha);
    }

    private static QizaiGeometry.Point pose(QizaiGeometry.Point point, double offsetX, double offsetY, double offsetZ, double[] matrix) {
        return new QizaiGeometry.Point(offsetX + matrix[0] * point.x() + matrix[1] * point.y() + matrix[2] * point.z(), offsetY + matrix[3] * point.x() + matrix[4] * point.y() + matrix[5] * point.z(), offsetZ + matrix[6] * point.x() + matrix[7] * point.y() + matrix[8] * point.z());
    }

    private static void face(QizaiGeometry.Point first, QizaiGeometry.Point second, QizaiGeometry.Point third, QizaiGeometry.Point fourth, int color, float alpha, QizaiGeometry.Sink sink) {
        double normalZ;
        double normalY;
        double normalX = (second.y() - first.y()) * (fourth.z() - first.z()) - (second.z() - first.z()) * (fourth.y() - first.y());
        double length = Math.sqrt(normalX * normalX + (normalY = (second.z() - first.z()) * (fourth.x() - first.x()) - (second.x() - first.x()) * (fourth.z() - first.z())) * normalY + (normalZ = (second.x() - first.x()) * (fourth.y() - first.y()) - (second.y() - first.y()) * (fourth.x() - first.x())) * normalZ);
        double light = length < 1.0E-5 ? 0.85 : 0.72 + 0.28 * Math.abs((normalX * 0.35 + normalY * 0.8 + normalZ * 0.48) / length);
        int red = (int)((double)(color >> 16 & 0xFF) * light);
        int green = (int)((double)(color >> 8 & 0xFF) * light);
        int blue = (int)((double)(color & 0xFF) * light);
        sink.quad(first, second, third, fourth, red << 16 | green << 8 | blue, alpha);
    }

    private static QizaiGeometry.Point radial(double radius, double height, double angle) {
        return new QizaiGeometry.Point(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static double smooth(double value) {
        double bounded = QizaiSkyNovaModel.clamp(value);
        return bounded * bounded * (3.0 - 2.0 * bounded);
    }
}

