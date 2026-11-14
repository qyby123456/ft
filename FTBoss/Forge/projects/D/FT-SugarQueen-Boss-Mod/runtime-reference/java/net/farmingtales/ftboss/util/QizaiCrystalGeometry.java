/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.QizaiCrystalMesh;
import net.farmingtales.ftboss.util.QizaiGeometry;

public final class QizaiCrystalGeometry {
    public static final int LASER_COLOR = 10821083;
    public static final int MISSILE_COLOR = 32488;
    public static final int GUARD_COLOR = 0xFFAF00;

    private QizaiCrystalGeometry() {
    }

    public static int color(int role) {
        return role == 0 ? 10821083 : (role == 1 ? 32488 : 0xFFAF00);
    }

    public static void crystal(int role, double age, float ward, QizaiGeometry.Sink sink) {
        if (!Double.isFinite(age) || !Float.isFinite(ward)) {
            return;
        }
        QizaiCrystalMesh.render(role, age, true, sink);
        QizaiCrystalGeometry.aura(role, age, ward, sink);
    }

    public static void aura(int role, double age, float ward, QizaiGeometry.Sink sink) {
        if (!Double.isFinite(age) || !Float.isFinite(ward)) {
            return;
        }
        int color = QizaiCrystalGeometry.color(role);
        if (role == 2 && ward > 0.0f) {
            QizaiCrystalGeometry.ring(0.9, 0.018, age * 0.01, color, Math.min(0.5f, ward), sink);
        }
    }

    public static void beam(QizaiGeometry.Point end, boolean firing, QizaiGeometry.Sink sink) {
        double length = Math.sqrt(end.x() * end.x() + end.y() * end.y() + end.z() * end.z());
        if (!Double.isFinite(length) || length < 0.001 || length > 40.0) {
            return;
        }
        double horizontal = Math.hypot(end.x(), end.z());
        QizaiGeometry.Point side = horizontal < 0.001 ? new QizaiGeometry.Point(1.0, 0.0, 0.0) : new QizaiGeometry.Point(-end.z() / horizontal, 0.0, end.x() / horizontal);
        QizaiGeometry.Point up = new QizaiGeometry.Point(end.y() * side.z() / length, (end.z() * side.x() - end.x() * side.z()) / length, -end.y() * side.x() / length);
        for (int layer = 0; layer < 2; ++layer) {
            int color;
            double width;
            double d = firing ? (layer == 0 ? 0.13 : 0.06) : (width = layer == 0 ? 0.06 : 0.025);
            int n = firing ? (layer == 1 ? 16769098 : 10821083) : (color = layer == 0 ? 15219456 : 16769098);
            float alpha = firing ? (layer == 0 ? 0.65f : 1.0f) : 0.9f;
            QizaiCrystalGeometry.ribbon(end, side, width, color, alpha, sink);
            QizaiCrystalGeometry.ribbon(end, up, width, color, alpha, sink);
        }
    }

    private static void ribbon(QizaiGeometry.Point end, QizaiGeometry.Point direction, double width, int color, float alpha, QizaiGeometry.Sink sink) {
        QizaiGeometry.Point side = new QizaiGeometry.Point(direction.x() * width, direction.y() * width, direction.z() * width);
        sink.quad(side, QizaiCrystalGeometry.offset(end, side.x(), side.y(), side.z()), QizaiCrystalGeometry.offset(end, -side.x(), -side.y(), -side.z()), new QizaiGeometry.Point(-side.x(), -side.y(), -side.z()), color, alpha);
    }

    private static void ring(double radius, double width, double angle, int color, float alpha, QizaiGeometry.Sink sink) {
        for (int segment = 0; segment < 24; ++segment) {
            double start = angle + (double)segment * Math.PI / 12.0;
            double end = start + 0.2617993877991494;
            sink.quad(new QizaiGeometry.Point(Math.cos(start) * radius, 0.0, Math.sin(start) * radius), new QizaiGeometry.Point(Math.cos(end) * radius, 0.0, Math.sin(end) * radius), new QizaiGeometry.Point(Math.cos(end) * (radius - width), 0.0, Math.sin(end) * (radius - width)), new QizaiGeometry.Point(Math.cos(start) * (radius - width), 0.0, Math.sin(start) * (radius - width)), color, alpha);
        }
    }

    private static QizaiGeometry.Point offset(QizaiGeometry.Point point, double horizontal, double vertical, double depth) {
        return new QizaiGeometry.Point(point.x() + horizontal, point.y() + vertical, point.z() + depth);
    }
}

