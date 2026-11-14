package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.util.QizaiGeometry.Point;
import net.farmingtales.ftboss.util.QizaiGeometry.Sink;

public final class QizaiSkyNovaModel {
    public static final int SHELL = 0x452F60;
    public static final int ARMOR = 0x9D4D76;
    public static final int EDGE = 0xF2C58C;
    public static final int CORE = 0xFFF0C2;
    public static final int EMBER = 0xEF739A;
    public static final double TIP = -3.4;
    private static final Point[] PETAL = {
            new Point(1.15, -1.25, 0), new Point(3.8, 2.1, 0),
            new Point(3.2, 3.3, 0), new Point(1.5, 0.65, 0)
    };

    private QizaiSkyNovaModel() {}

    public static void render(double time, double charge, double rupture, boolean reduced, float alpha, Sink sink) {
        if (!Double.isFinite(time + charge + rupture) || !Float.isFinite(alpha) || alpha <= 0) return;
        charge = clamp(charge);
        rupture = clamp(rupture);
        alpha = Math.min(1, alpha);
        double closing = smooth((charge - 0.84) / 0.16);
        double opening = smooth(charge / 0.6) * (1 - closing);
        double spin = time * 0.012;
        Sink root = transform(0, 0, 0, spin, 0, 0, 1, sink);
        core(alpha, root);
        for (int petal = 0; petal < 6; petal++) {
            double heading = petal * Math.PI / 3;
            double spread = opening * 0.55 + rupture * 4;
            Sink wing = transform(Math.cos(heading) * spread, rupture * 2, Math.sin(heading) * spread,
                    -heading, 0, -opening * 0.24 - rupture * 0.8, 1, root);
            plate(alpha, wing);
        }
        int segments = reduced ? 12 : 24;
        double orbit = 4.35 + opening * 0.65 + rupture * 2;
        hoop(orbit, 0.10, 0.10, segments, alpha * 0.9F,
                transform(0, 0.45, 0, -spin * 1.8, 0.52, 0, 1, sink));
        hoop(orbit * 0.88, 0.075, 0.08, segments, alpha * 0.75F,
                transform(0, 0.85, 0, spin * 1.5, -0.68, 0, 1, sink));
        for (int jewel = 0; jewel < 6; jewel++) {
            double heading = jewel * Math.PI / 3 - spin * 1.8;
            double orbitY = 0.45 + Math.sin(heading) * orbit * Math.sin(0.52);
            fragment(0.17, 0.4, alpha, transform(Math.cos(heading) * orbit, orbitY,
                    Math.sin(heading) * orbit * Math.cos(0.52), heading, 0, 0, 1, sink));
        }
    }

    private static void core(float alpha, Sink sink) {
        double[] heights = {TIP, -1.15, 0.35, 1.65, 2.65};
        double[] widths = {0, 0.75, 1.18, 0.58, 0};
        int[] colors = {EMBER, CORE, CORE, EDGE};
        for (int band = 0; band < heights.length - 1; band++) for (int facet = 0; facet < 8; facet++) {
            double first = facet * Math.PI / 4, second = (facet + 1) * Math.PI / 4;
            face(radial(widths[band], heights[band], first), radial(widths[band], heights[band], second),
                    radial(widths[band + 1], heights[band + 1], second), radial(widths[band + 1], heights[band + 1], first),
                    colors[band], alpha, sink);
        }
    }

    private static void plate(float alpha, Sink sink) {
        Point[] front = new Point[4], back = new Point[4], inset = new Point[4];
        double centerX = 2.4125, centerY = 1.2;
        for (int corner = 0; corner < 4; corner++) {
            Point point = PETAL[corner];
            front[corner] = new Point(point.x(), point.y(), -0.23);
            back[corner] = new Point(point.x(), point.y(), 0.23);
            inset[corner] = new Point(centerX + (point.x() - centerX) * 0.81,
                    centerY + (point.y() - centerY) * 0.81, -0.32);
        }
        face(back[3], back[2], back[1], back[0], SHELL, alpha, sink);
        face(inset[0], inset[1], inset[2], inset[3], ARMOR, alpha, sink);
        for (int corner = 0; corner < 4; corner++) {
            int next = (corner + 1) % 4;
            face(front[corner], back[corner], back[next], front[next], SHELL, alpha, sink);
            face(front[corner], front[next], inset[next], inset[corner], EDGE, alpha, sink);
        }
        face(new Point(1.62, 0.23, -0.325), new Point(1.74, 0.25, -0.325),
                new Point(3.12, 2.61, -0.325), new Point(2.94, 2.43, -0.325), CORE, alpha, sink);
    }

    private static void hoop(double radius, double width, double thickness, int segments, float alpha, Sink sink) {
        for (int segment = 0; segment < segments; segment++) {
            double first = segment * Math.PI * 2 / segments, second = (segment + 0.84) * Math.PI * 2 / segments;
            Point outerFirst = radial(radius, thickness, first), outerSecond = radial(radius, thickness, second);
            Point innerFirst = radial(radius - width, thickness, first), innerSecond = radial(radius - width, thickness, second);
            face(outerFirst, outerSecond, innerSecond, innerFirst, EDGE, alpha, sink);
            face(radial(radius, -thickness, second), radial(radius, -thickness, first),
                    radial(radius - width, -thickness, first), radial(radius - width, -thickness, second), SHELL, alpha, sink);
            face(outerSecond, outerFirst, radial(radius, -thickness, first), radial(radius, -thickness, second), ARMOR, alpha, sink);
            face(innerFirst, innerSecond, radial(radius - width, -thickness, second), radial(radius - width, -thickness, first), EDGE, alpha, sink);
            face(outerFirst, innerFirst, radial(radius - width, -thickness, first), radial(radius, -thickness, first), EDGE, alpha, sink);
            face(innerSecond, outerSecond, radial(radius, -thickness, second), radial(radius - width, -thickness, second), EDGE, alpha, sink);
        }
    }

    public static void fragment(double width, double length, float alpha, Sink sink) {
        for (int facet = 0; facet < 4; facet++) {
            Point first = radial(width, 0, facet * Math.PI / 2), second = radial(width, 0, (facet + 1) * Math.PI / 2);
            face(first, second, new Point(0, length, 0), new Point(0, length, 0), EDGE, alpha, sink);
            face(second, first, new Point(0, -length, 0), new Point(0, -length, 0), EMBER, alpha, sink);
        }
    }

    public static Sink transform(double offsetX, double offsetY, double offsetZ, double yaw, double pitch, double roll,
                                 double scale, Sink sink) {
        double yawCos = Math.cos(yaw), yawSin = Math.sin(yaw), pitchCos = Math.cos(pitch), pitchSin = Math.sin(pitch);
        double rollCos = Math.cos(roll), rollSin = Math.sin(roll);
        double[] matrix = {
                (rollCos * yawCos + rollSin * pitchSin * yawSin) * scale,
                (-rollSin * yawCos + rollCos * pitchSin * yawSin) * scale, pitchCos * yawSin * scale,
                rollSin * pitchCos * scale, rollCos * pitchCos * scale, -pitchSin * scale,
                (-rollCos * yawSin + rollSin * pitchSin * yawCos) * scale,
                (rollSin * yawSin + rollCos * pitchSin * yawCos) * scale, pitchCos * yawCos * scale
        };
        return (first, second, third, fourth, color, alpha) -> sink.quad(
                pose(first, offsetX, offsetY, offsetZ, matrix), pose(second, offsetX, offsetY, offsetZ, matrix),
                pose(third, offsetX, offsetY, offsetZ, matrix), pose(fourth, offsetX, offsetY, offsetZ, matrix), color, alpha);
    }

    private static Point pose(Point point, double offsetX, double offsetY, double offsetZ, double[] matrix) {
        return new Point(offsetX + matrix[0] * point.x() + matrix[1] * point.y() + matrix[2] * point.z(),
                offsetY + matrix[3] * point.x() + matrix[4] * point.y() + matrix[5] * point.z(),
                offsetZ + matrix[6] * point.x() + matrix[7] * point.y() + matrix[8] * point.z());
    }

    private static void face(Point first, Point second, Point third, Point fourth, int color, float alpha, Sink sink) {
        double normalX = (second.y() - first.y()) * (fourth.z() - first.z()) - (second.z() - first.z()) * (fourth.y() - first.y());
        double normalY = (second.z() - first.z()) * (fourth.x() - first.x()) - (second.x() - first.x()) * (fourth.z() - first.z());
        double normalZ = (second.x() - first.x()) * (fourth.y() - first.y()) - (second.y() - first.y()) * (fourth.x() - first.x());
        double length = Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        double light = length < 0.00001 ? 0.85 : 0.72 + 0.28 * Math.abs((normalX * 0.35 + normalY * 0.8 + normalZ * 0.48) / length);
        int red = (int) (((color >> 16) & 255) * light), green = (int) (((color >> 8) & 255) * light), blue = (int) ((color & 255) * light);
        sink.quad(first, second, third, fourth, (red << 16) | (green << 8) | blue, alpha);
    }

    private static Point radial(double radius, double height, double angle) {
        return new Point(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
    }

    private static double clamp(double value) { return Math.max(0, Math.min(1, value)); }
    private static double smooth(double value) { double bounded = clamp(value); return bounded * bounded * (3 - 2 * bounded); }
}
