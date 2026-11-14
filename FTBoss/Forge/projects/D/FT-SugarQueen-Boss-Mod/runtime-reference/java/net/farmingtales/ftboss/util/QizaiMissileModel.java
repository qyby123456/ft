/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import net.farmingtales.ftboss.util.QizaiGeometry;

public final class QizaiMissileModel {
    public static final int AZURE = 4444159;
    public static final int VIOLET = 13138431;
    public static final int GOLD = 14526807;
    private static final int[] ARMOR = new int[]{14412017, 9087938, 3953518, 2372687, 3360095, 5862801, 10599377, 15726586};
    private static final Face[][] BODIES = new Face[][]{QizaiMissileModel.buildBody(false), QizaiMissileModel.buildBody(true)};

    private QizaiMissileModel() {
    }

    public static void body(boolean arsenal, QizaiGeometry.Sink sink) {
        for (Face face : BODIES[arsenal ? 1 : 0]) {
            face.draw(sink);
        }
    }

    public static void glow(double age, boolean arsenal, QizaiGeometry.Sink sink) {
        int segment;
        if (!Double.isFinite(age)) {
            return;
        }
        int color = arsenal ? 13138431 : 4444159;
        double phase = age * 0.075;
        float pulse = (float)(0.83 + 0.12 * Math.sin(age * 0.32));
        for (int quadrant = 0; quadrant < 4; ++quadrant) {
            double angle = phase + (double)quadrant * Math.PI / 2.0;
            for (segment = 0; segment < 5; ++segment) {
                double start = angle + (double)segment * 0.17;
                double end = start + 0.16;
                sink.quad(QizaiMissileModel.radial(0.226, start, -0.25), QizaiMissileModel.radial(0.226, end, -0.25), QizaiMissileModel.radial(0.21, end, -0.25), QizaiMissileModel.radial(0.21, start, -0.25), color, pulse);
            }
            sink.quad(QizaiMissileModel.radial(0.251, angle, -0.25), QizaiMissileModel.radial(0.275, angle + 0.055, -0.25), QizaiMissileModel.radial(0.251, angle + 0.11, -0.25), QizaiMissileModel.radial(0.237, angle + 0.055, -0.25), 14526807, 0.9f);
        }
        double tailEnd = -1.48 - 0.12 * Math.sin(age * 0.41);
        QizaiMissileModel.taper(-0.54, 0.07, -0.83, 0.043, color, 0.7f, sink);
        QizaiMissileModel.taper(-0.83, 0.043, tailEnd, 0.0, color, 0.3f, sink);
        QizaiMissileModel.taper(-0.55, 0.026, -1.09, 0.0, 15268607, 0.9f, sink);
        for (int strand = 0; strand < 2; ++strand) {
            for (segment = 0; segment < 14; ++segment) {
                double progress = (double)segment / 14.0;
                double next = (double)(segment + 1) / 14.0;
                double angle = (double)strand * Math.PI + phase - progress * 4.6;
                double nextAngle = (double)strand * Math.PI + phase - next * 4.6;
                double radius = 0.065 + progress * 0.045;
                double nextRadius = 0.065 + next * 0.045;
                double width = 0.011 * (1.0 - progress);
                double nextWidth = 0.011 * (1.0 - next);
                double depth = -0.56 - progress * 1.15;
                double nextDepth = -0.56 - next * 1.15;
                sink.quad(QizaiMissileModel.radial(radius + width, angle, depth), QizaiMissileModel.radial(nextRadius + nextWidth, nextAngle, nextDepth), QizaiMissileModel.radial(nextRadius - nextWidth, nextAngle, nextDepth), QizaiMissileModel.radial(radius - width, angle, depth), color, (float)(0.5 * (1.0 - progress)));
            }
        }
    }

    private static Face[] buildBody(boolean arsenal) {
        ArrayList faces = new ArrayList();
        QizaiGeometry.Sink sink = (first, second, third, fourth, color, alpha) -> faces.add(new Face(first, second, third, fourth, color, alpha));
        int color2 = arsenal ? 13138431 : 4444159;
        QizaiMissileModel.shell(-0.52, 0.072, -0.39, 0.137, 11061467, sink);
        QizaiMissileModel.shell(-0.39, 0.137, 0.18, 0.106, 7902643, sink);
        QizaiMissileModel.shell(0.18, 0.106, 0.28, 0.08, 12178920, sink);
        QizaiMissileModel.taper(0.22, 0.062, 0.4, 0.112, color2, 1.0f, sink);
        QizaiMissileModel.taper(0.4, 0.112, 0.84, 0.0, color2, 1.0f, sink);
        QizaiMissileModel.collar(-0.03, 0.04, 0.17, 14526807, sink);
        QizaiMissileModel.collar(-0.52, -0.46, 0.106, 8758973, sink);
        for (int wing = 0; wing < 4; ++wing) {
            double angle = 0.7853981633974483 + (double)wing * Math.PI / 2.0;
            QizaiGeometry.Point[] edge = new QizaiGeometry.Point[]{new QizaiGeometry.Point(0.115, 0.0, 0.28), new QizaiGeometry.Point(0.355, 0.0, -0.34), new QizaiGeometry.Point(0.27, 0.0, -0.53), new QizaiGeometry.Point(0.115, 0.0, -0.37)};
            QizaiGeometry.Point[] front = new QizaiGeometry.Point[4];
            QizaiGeometry.Point[] back = new QizaiGeometry.Point[4];
            for (int vertex = 0; vertex < 4; ++vertex) {
                front[vertex] = QizaiMissileModel.rotate(new QizaiGeometry.Point(edge[vertex].x(), 0.018, edge[vertex].z()), angle);
                back[vertex] = QizaiMissileModel.rotate(new QizaiGeometry.Point(edge[vertex].x(), -0.018, edge[vertex].z()), angle);
            }
            sink.quad(front[0], front[1], front[2], front[3], 12309476, 1.0f);
            sink.quad(back[3], back[2], back[1], back[0], 4611964, 1.0f);
            for (int side = 0; side < 4; ++side) {
                int next = (side + 1) % 4;
                sink.quad(front[side], back[side], back[next], front[next], side == 0 ? 14526807 : 2439765, 1.0f);
            }
            for (int sign : new int[]{-1, 1}) {
                QizaiGeometry.Sink detail = QizaiMissileModel.rotated(angle, sink);
                detail.quad(new QizaiGeometry.Point(0.161, (double)sign * 0.032, 0.11), new QizaiGeometry.Point(0.321, (double)sign * 0.032, -0.337), new QizaiGeometry.Point(0.29, (double)sign * 0.032, -0.39), new QizaiGeometry.Point(0.149, (double)sign * 0.032, -0.02), color2, 1.0f);
            }
        }
        return (Face[])faces.toArray(Face[]::new);
    }

    private static QizaiGeometry.Sink rotated(double angle, QizaiGeometry.Sink sink) {
        return (first, second, third, fourth, color, alpha) -> sink.quad(QizaiMissileModel.rotate(first, angle), QizaiMissileModel.rotate(second, angle), QizaiMissileModel.rotate(third, angle), QizaiMissileModel.rotate(fourth, angle), color, alpha);
    }

    private static QizaiGeometry.Point rotate(QizaiGeometry.Point point, double angle) {
        return new QizaiGeometry.Point(point.x() * Math.cos(angle) - point.y() * Math.sin(angle), point.x() * Math.sin(angle) + point.y() * Math.cos(angle), point.z());
    }

    private static void collar(double rear, double front, double radius, int color, QizaiGeometry.Sink sink) {
        QizaiMissileModel.taper(rear, radius * 0.84, rear + 0.012, radius, color, 1.0f, sink);
        QizaiMissileModel.taper(rear + 0.012, radius, front - 0.012, radius, color, 1.0f, sink);
        QizaiMissileModel.taper(front - 0.012, radius, front, radius * 0.84, color, 1.0f, sink);
    }

    private static void shell(double rear, double rearRadius, double front, double frontRadius, int color, QizaiGeometry.Sink sink) {
        for (int side = 0; side < 8; ++side) {
            double angle = (double)side * Math.PI / 4.0;
            double next = angle + 0.7853981633974483;
            sink.quad(QizaiMissileModel.radial(rearRadius, angle, rear), QizaiMissileModel.radial(rearRadius, next, rear), QizaiMissileModel.radial(frontRadius, next, front), QizaiMissileModel.radial(frontRadius, angle, front), QizaiMissileModel.mix(ARMOR[side], color, 0.3), 1.0f);
        }
    }

    private static void taper(double rear, double rearRadius, double front, double frontRadius, int color, float alpha, QizaiGeometry.Sink sink) {
        for (int side = 0; side < 8; ++side) {
            double angle = (double)side * Math.PI / 4.0;
            double next = angle + 0.7853981633974483;
            int shade = QizaiMissileModel.mix(color, side < 3 ? 0xF2FFFF : 1651812, side < 3 ? 0.12 + (double)side * 0.16 : 0.14 + (double)(side - 3) * 0.09);
            sink.quad(QizaiMissileModel.radial(rearRadius, angle, rear), QizaiMissileModel.radial(rearRadius, next, rear), QizaiMissileModel.radial(frontRadius, next, front), QizaiMissileModel.radial(frontRadius, angle, front), shade, alpha);
        }
    }

    private static int mix(int first, int second, double amount) {
        int result = 0;
        for (int shift = 0; shift <= 16; shift += 8) {
            result |= (int)Math.round((double)(first >> shift & 0xFF) * (1.0 - amount) + (double)(second >> shift & 0xFF) * amount) << shift;
        }
        return result;
    }

    private static QizaiGeometry.Point radial(double radius, double angle, double depth) {
        return new QizaiGeometry.Point(Math.cos(angle) * radius, Math.sin(angle) * radius, depth);
    }

    private record Face(QizaiGeometry.Point first, QizaiGeometry.Point second, QizaiGeometry.Point third, QizaiGeometry.Point fourth, int color, float alpha) {
        void draw(QizaiGeometry.Sink sink) {
            sink.quad(this.first, this.second, this.third, this.fourth, this.color, this.alpha);
        }
    }
}

