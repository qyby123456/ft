package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import net.farmingtales.ftboss.util.QizaiGeometry.Point;
import net.farmingtales.ftboss.util.QizaiGeometry.Sink;

public final class QizaiMissileModel {
    public static final int AZURE = 0x43CFFF;
    public static final int VIOLET = 0xC879FF;
    public static final int GOLD = 0xDDA957;
    private static final int[] ARMOR = {0xDBE8F1, 0x8AABC2, 0x3C536E, 0x24344F, 0x33455F, 0x597591, 0xA1BBD1, 0xEFF7FA};
    private static final Face[][] BODIES = {buildBody(false), buildBody(true)};

    private record Face(Point first, Point second, Point third, Point fourth, int color, float alpha) {
        void draw(Sink sink) { sink.quad(first, second, third, fourth, color, alpha); }
    }

    private QizaiMissileModel() {}

    public static void body(boolean arsenal, Sink sink) {
        for (Face face : BODIES[arsenal ? 1 : 0]) face.draw(sink);
    }

    public static void glow(double age, boolean arsenal, Sink sink) {
        if (!Double.isFinite(age)) return;
        int color = arsenal ? VIOLET : AZURE;
        double phase = age * 0.075;
        float pulse = (float) (0.83 + 0.12 * Math.sin(age * 0.32));
        for (int quadrant = 0; quadrant < 4; quadrant++) {
            double angle = phase + quadrant * Math.PI / 2;
            for (int segment = 0; segment < 5; segment++) {
                double start = angle + segment * 0.17;
                double end = start + 0.16;
                sink.quad(radial(0.226, start, -0.25), radial(0.226, end, -0.25),
                        radial(0.21, end, -0.25), radial(0.21, start, -0.25), color, pulse);
            }
            sink.quad(radial(0.251, angle, -0.25), radial(0.275, angle + 0.055, -0.25),
                    radial(0.251, angle + 0.11, -0.25), radial(0.237, angle + 0.055, -0.25), GOLD, 0.9F);
        }
        double tailEnd = -1.48 - 0.12 * Math.sin(age * 0.41);
        taper(-0.54, 0.07, -0.83, 0.043, color, 0.7F, sink);
        taper(-0.83, 0.043, tailEnd, 0, color, 0.3F, sink);
        taper(-0.55, 0.026, -1.09, 0, 0xE8FAFF, 0.9F, sink);
        for (int strand = 0; strand < 2; strand++) {
            for (int segment = 0; segment < 14; segment++) {
                double progress = segment / 14.0;
                double next = (segment + 1) / 14.0;
                double angle = strand * Math.PI + phase - progress * 4.6;
                double nextAngle = strand * Math.PI + phase - next * 4.6;
                double radius = 0.065 + progress * 0.045;
                double nextRadius = 0.065 + next * 0.045;
                double width = 0.011 * (1 - progress);
                double nextWidth = 0.011 * (1 - next);
                double depth = -0.56 - progress * 1.15;
                double nextDepth = -0.56 - next * 1.15;
                sink.quad(radial(radius + width, angle, depth), radial(nextRadius + nextWidth, nextAngle, nextDepth),
                        radial(nextRadius - nextWidth, nextAngle, nextDepth), radial(radius - width, angle, depth),
                        color, (float) (0.5 * (1 - progress)));
            }
        }
    }

    private static Face[] buildBody(boolean arsenal) {
        var faces = new ArrayList<Face>();
        Sink sink = (first, second, third, fourth, color, alpha) -> faces.add(new Face(first, second, third, fourth, color, alpha));
        int color = arsenal ? VIOLET : AZURE;
        shell(-0.52, 0.072, -0.39, 0.137, 0xA8C8DB, sink);
        shell(-0.39, 0.137, 0.18, 0.106, 0x7895B3, sink);
        shell(0.18, 0.106, 0.28, 0.08, 0xB9D5E8, sink);
        taper(0.22, 0.062, 0.4, 0.112, color, 1, sink);
        taper(0.4, 0.112, 0.84, 0, color, 1, sink);
        collar(-0.03, 0.04, 0.17, GOLD, sink);
        collar(-0.52, -0.46, 0.106, 0x85A6BD, sink);
        for (int wing = 0; wing < 4; wing++) {
            double angle = Math.PI / 4 + wing * Math.PI / 2;
            Point[] edge = {new Point(0.115, 0, 0.28), new Point(0.355, 0, -0.34),
                    new Point(0.27, 0, -0.53), new Point(0.115, 0, -0.37)};
            Point[] front = new Point[4];
            Point[] back = new Point[4];
            for (int vertex = 0; vertex < 4; vertex++) {
                front[vertex] = rotate(new Point(edge[vertex].x(), 0.018, edge[vertex].z()), angle);
                back[vertex] = rotate(new Point(edge[vertex].x(), -0.018, edge[vertex].z()), angle);
            }
            sink.quad(front[0], front[1], front[2], front[3], 0xBBD3E4, 1);
            sink.quad(back[3], back[2], back[1], back[0], 0x465F7C, 1);
            for (int side = 0; side < 4; side++) {
                int next = (side + 1) % 4;
                sink.quad(front[side], back[side], back[next], front[next], side == 0 ? GOLD : 0x253A55, 1);
            }
            for (int sign : new int[]{-1, 1}) {
                Sink detail = rotated(angle, sink);
                detail.quad(new Point(0.161, sign * 0.032, 0.11), new Point(0.321, sign * 0.032, -0.337),
                        new Point(0.29, sign * 0.032, -0.39), new Point(0.149, sign * 0.032, -0.02), color, 1);
            }
        }
        return faces.toArray(Face[]::new);
    }

    private static Sink rotated(double angle, Sink sink) {
        return (first, second, third, fourth, color, alpha) ->
                sink.quad(rotate(first, angle), rotate(second, angle), rotate(third, angle), rotate(fourth, angle), color, alpha);
    }

    private static Point rotate(Point point, double angle) {
        return new Point(point.x() * Math.cos(angle) - point.y() * Math.sin(angle),
                point.x() * Math.sin(angle) + point.y() * Math.cos(angle), point.z());
    }

    private static void collar(double rear, double front, double radius, int color, Sink sink) {
        taper(rear, radius * 0.84, rear + 0.012, radius, color, 1, sink);
        taper(rear + 0.012, radius, front - 0.012, radius, color, 1, sink);
        taper(front - 0.012, radius, front, radius * 0.84, color, 1, sink);
    }

    private static void shell(double rear, double rearRadius, double front, double frontRadius, int color, Sink sink) {
        for (int side = 0; side < 8; side++) {
            double angle = side * Math.PI / 4;
            double next = angle + Math.PI / 4;
            sink.quad(radial(rearRadius, angle, rear), radial(rearRadius, next, rear),
                    radial(frontRadius, next, front), radial(frontRadius, angle, front), mix(ARMOR[side], color, 0.3), 1);
        }
    }

    private static void taper(double rear, double rearRadius, double front, double frontRadius, int color, float alpha, Sink sink) {
        for (int side = 0; side < 8; side++) {
            double angle = side * Math.PI / 4;
            double next = angle + Math.PI / 4;
            int shade = mix(color, side < 3 ? 0xF2FFFF : 0x193464, side < 3 ? 0.12 + side * 0.16 : 0.14 + (side - 3) * 0.09);
            sink.quad(radial(rearRadius, angle, rear), radial(rearRadius, next, rear),
                    radial(frontRadius, next, front), radial(frontRadius, angle, front), shade, alpha);
        }
    }

    private static int mix(int first, int second, double amount) {
        int result = 0;
        for (int shift = 0; shift <= 16; shift += 8) {
            result |= (int) Math.round(((first >> shift) & 255) * (1 - amount) + ((second >> shift) & 255) * amount) << shift;
        }
        return result;
    }

    private static Point radial(double radius, double angle, double depth) {
        return new Point(Math.cos(angle) * radius, Math.sin(angle) * radius, depth);
    }
}
