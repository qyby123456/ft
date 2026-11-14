package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.QizaiGeometry.Point;
import net.farmingtales.ftboss.util.QizaiGeometry.Sink;

public final class QizaiCrystalGeometry {
    public static final int LASER_COLOR = 0xA51DDB;
    public static final int MISSILE_COLOR = 0x007EE8;
    public static final int GUARD_COLOR = 0xFFAF00;

    private QizaiCrystalGeometry() {}

    public static int color(int role) { return role == 0 ? LASER_COLOR : role == 1 ? MISSILE_COLOR : GUARD_COLOR; }

    public static void crystal(int role, double age, float ward, Sink sink) {
        if (!Double.isFinite(age) || !Float.isFinite(ward)) return;
        QizaiCrystalMesh.render(role, age, true, sink);
        aura(role, age, ward, sink);
    }

    public static void aura(int role, double age, float ward, Sink sink) {
        if (!Double.isFinite(age) || !Float.isFinite(ward)) return;
        int color = color(role);
        if (role == 2 && ward > 0) {
            ring(0.9, 0.018, age * 0.01, color, Math.min(0.5F, ward), sink);
        }
    }

    public static void beam(Point end, boolean firing, Sink sink) {
        double length = Math.sqrt(end.x() * end.x() + end.y() * end.y() + end.z() * end.z());
        if (!Double.isFinite(length) || length < 0.001 || length > 40) return;
        double horizontal = Math.hypot(end.x(), end.z());
        Point side = horizontal < 0.001 ? new Point(1, 0, 0) : new Point(-end.z() / horizontal, 0, end.x() / horizontal);
        Point up = new Point(end.y() * side.z() / length, (end.z() * side.x() - end.x() * side.z()) / length, -end.y() * side.x() / length);
        for (int layer = 0; layer < 2; layer++) {
            double width = firing ? (layer == 0 ? 0.13 : 0.06) : (layer == 0 ? 0.06 : 0.025);
            int color = firing ? (layer == 1 ? QizaiGeometry.WHITE : LASER_COLOR)
                    : (layer == 0 ? QizaiGeometry.WARNING : QizaiGeometry.WHITE);
            float alpha = firing ? (layer == 0 ? 0.65F : 1.0F) : 0.9F;
            ribbon(end, side, width, color, alpha, sink);
            ribbon(end, up, width, color, alpha, sink);
        }
    }

    private static void ribbon(Point end, Point direction, double width, int color, float alpha, Sink sink) {
        Point side = new Point(direction.x() * width, direction.y() * width, direction.z() * width);
        sink.quad(side, offset(end, side.x(), side.y(), side.z()), offset(end, -side.x(), -side.y(), -side.z()),
                new Point(-side.x(), -side.y(), -side.z()), color, alpha);
    }

    private static void ring(double radius, double width, double angle, int color, float alpha, Sink sink) {
        for (int segment = 0; segment < 24; segment++) {
            double start = angle + segment * Math.PI / 12, end = start + Math.PI / 12;
            sink.quad(new Point(Math.cos(start) * radius, 0, Math.sin(start) * radius),
                    new Point(Math.cos(end) * radius, 0, Math.sin(end) * radius),
                    new Point(Math.cos(end) * (radius - width), 0, Math.sin(end) * (radius - width)),
                    new Point(Math.cos(start) * (radius - width), 0, Math.sin(start) * (radius - width)), color, alpha);
        }
    }

    private static Point offset(Point point, double horizontal, double vertical, double depth) {
        return new Point(point.x() + horizontal, point.y() + vertical, point.z() + depth);
    }
}
