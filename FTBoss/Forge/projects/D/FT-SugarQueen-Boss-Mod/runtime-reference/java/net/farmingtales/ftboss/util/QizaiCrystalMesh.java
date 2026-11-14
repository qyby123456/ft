/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.util.QizaiGeometry;

public final class QizaiCrystalMesh {
    private static final int METAL = 2696505;
    private static final int GOLD = 11766609;
    private static final int EDGE = 15584131;
    private static final int[][] FACETS = new int[][]{{4731245, 7950502, 12423654, 9528516, 6701455, 3416910, 5453437, 10188991}, {1654114, 3243690, 9559527, 5681872, 2648199, 1650504, 2511472, 6993868}, {7556903, 11371069, 16178836, 14465126, 9660976, 4929832, 7821360, 13019496}};
    private static final List<List<Face>> CORES = List.of(QizaiCrystalMesh.core(0), QizaiCrystalMesh.core(1), QizaiCrystalMesh.core(2));
    private static final List<List<Face>> SHARDS = List.of(QizaiCrystalMesh.shard(0), QizaiCrystalMesh.shard(1), QizaiCrystalMesh.shard(2));

    private QizaiCrystalMesh() {
    }

    public static void render(int role, double age, boolean satellites, QizaiGeometry.Sink sink) {
        if (!Double.isFinite(age)) {
            return;
        }
        int palette = Math.max(0, Math.min(2, role));
        double rotation = age * 0.018;
        QizaiCrystalMesh.emit(CORES.get(palette), rotation, 1.0, 0.0, 0.0, 0.0, sink);
        if (!satellites) {
            return;
        }
        int count = palette == 1 ? 2 : 3;
        for (int index = 0; index < count; ++index) {
            double angle = -rotation * 0.8 + (double)index * Math.PI * 2.0 / (double)count;
            QizaiCrystalMesh.emit(SHARDS.get(palette), angle + 0.7853981633974483, 0.3, Math.cos(angle) * 0.68, Math.sin(rotation * 2.0 + (double)index) * 0.16, Math.sin(angle) * 0.68, sink);
        }
    }

    private static List<Face> core(int role) {
        ArrayList<Face> faces = new ArrayList<Face>();
        double width = role == 0 ? 0.27 : (role == 1 ? 0.3 : 0.34);
        QizaiCrystalMesh.gem(faces, role, width, 0.65, 0.0, 0.0, 0.0);
        QizaiCrystalMesh.band(faces, width + 0.055, width - 0.008, -0.25, -0.17, 2696505);
        QizaiCrystalMesh.band(faces, width + 0.06, width + 0.043, -0.245, -0.23, 11766609);
        QizaiCrystalMesh.band(faces, width + 0.061, width + 0.043, -0.19, -0.172, 15584131);
        for (int index = 0; index < 4; ++index) {
            double angle = (double)index * Math.PI / 2.0 + 0.39269908169872414;
            double baseRadius = width + 0.03;
            QizaiGeometry.Point lower = QizaiCrystalMesh.radial(baseRadius * 0.48, -0.43, angle);
            QizaiGeometry.Point middle = QizaiCrystalMesh.radial(baseRadius + 0.09, -0.02, angle);
            QizaiGeometry.Point tip = QizaiCrystalMesh.radial(baseRadius * 0.84, 0.29 + (role == 2 ? 0.1 : 0.0), angle);
            QizaiCrystalMesh.strut(faces, lower, middle, 0.034, 2696505, 11766609);
            QizaiCrystalMesh.strut(faces, middle, tip, 0.025, 11766609, 15584131);
        }
        if (role == 1) {
            QizaiCrystalMesh.gem(faces, role, 0.085, 0.27, -0.46, 0.05, 0.0);
            QizaiCrystalMesh.gem(faces, role, 0.085, 0.27, 0.46, 0.05, 0.0);
        }
        return List.copyOf(faces);
    }

    private static List<Face> shard(int role) {
        ArrayList<Face> faces = new ArrayList<Face>();
        QizaiCrystalMesh.gem(faces, role, 0.21, 0.48, 0.0, 0.0, 0.0);
        QizaiCrystalMesh.band(faces, 0.23, 0.2, -0.12, -0.06, 11766609);
        return List.copyOf(faces);
    }

    private static void gem(List<Face> faces, int role, double width, double height, double horizontal, double vertical, double depth) {
        double[] radii = new double[]{0.015, width * 0.78, width, width * 0.84, 0.012};
        double[] heights = new double[]{-height, -height * 0.34, height * 0.22, height * 0.42, height};
        for (int layer = 0; layer < radii.length - 1; ++layer) {
            for (int side = 0; side < 8; ++side) {
                double start = (double)side * Math.PI / 4.0;
                double end = start + 0.7853981633974483;
                QizaiGeometry.Point first = QizaiCrystalMesh.offset(QizaiCrystalMesh.radial(radii[layer], heights[layer], start), horizontal, vertical, depth);
                QizaiGeometry.Point second = QizaiCrystalMesh.offset(QizaiCrystalMesh.radial(radii[layer], heights[layer], end), horizontal, vertical, depth);
                QizaiGeometry.Point third = QizaiCrystalMesh.offset(QizaiCrystalMesh.radial(radii[layer + 1], heights[layer + 1], end), horizontal, vertical, depth);
                QizaiGeometry.Point fourth = QizaiCrystalMesh.offset(QizaiCrystalMesh.radial(radii[layer + 1], heights[layer + 1], start), horizontal, vertical, depth);
                int color = FACETS[role][side];
                if (layer == 2) {
                    color = QizaiCrystalMesh.mix(color, 15261685, 0.28);
                }
                if (layer == 0) {
                    color = QizaiCrystalMesh.mix(color, 1448752, 0.22);
                }
                faces.add(new Face(first, second, third, fourth, color));
            }
        }
        for (int side = 0; side < 8; ++side) {
            double start = (double)side * Math.PI / 4.0;
            double end = start + 0.7853981633974483;
            QizaiGeometry.Point top = new QizaiGeometry.Point(horizontal, vertical + height, depth);
            QizaiGeometry.Point bottom = new QizaiGeometry.Point(horizontal, vertical - height, depth);
            faces.add(new Face(top, QizaiCrystalMesh.offset(QizaiCrystalMesh.radial(0.012, height, start), horizontal, vertical, depth), QizaiCrystalMesh.offset(QizaiCrystalMesh.radial(0.012, height, end), horizontal, vertical, depth), top, FACETS[role][2]));
            faces.add(new Face(bottom, QizaiCrystalMesh.offset(QizaiCrystalMesh.radial(0.015, -height, end), horizontal, vertical, depth), QizaiCrystalMesh.offset(QizaiCrystalMesh.radial(0.015, -height, start), horizontal, vertical, depth), bottom, FACETS[role][0]));
        }
    }

    private static void band(List<Face> faces, double outer, double inner, double lower, double upper, int color) {
        for (int side = 0; side < 8; ++side) {
            double start = (double)side * Math.PI / 4.0;
            double end = start + 0.7853981633974483;
            QizaiGeometry.Point outerLow = QizaiCrystalMesh.radial(outer, lower, start);
            QizaiGeometry.Point outerNextLow = QizaiCrystalMesh.radial(outer, lower, end);
            QizaiGeometry.Point outerHigh = QizaiCrystalMesh.radial(outer, upper, start);
            QizaiGeometry.Point outerNextHigh = QizaiCrystalMesh.radial(outer, upper, end);
            QizaiGeometry.Point innerLow = QizaiCrystalMesh.radial(inner, lower, start);
            QizaiGeometry.Point innerNextLow = QizaiCrystalMesh.radial(inner, lower, end);
            QizaiGeometry.Point innerHigh = QizaiCrystalMesh.radial(inner, upper, start);
            QizaiGeometry.Point innerNextHigh = QizaiCrystalMesh.radial(inner, upper, end);
            faces.add(new Face(outerLow, outerNextLow, outerNextHigh, outerHigh, QizaiCrystalMesh.mix(color, 1512739, (double)(side % 3) * 0.13)));
            faces.add(new Face(innerLow, innerHigh, innerNextHigh, innerNextLow, QizaiCrystalMesh.mix(color, 1512739, 0.25)));
            faces.add(new Face(outerHigh, outerNextHigh, innerNextHigh, innerHigh, color));
            faces.add(new Face(outerLow, innerLow, innerNextLow, outerNextLow, QizaiCrystalMesh.mix(color, 1512739, 0.3)));
        }
    }

    private static void strut(List<Face> faces, QizaiGeometry.Point start, QizaiGeometry.Point end, double width, int color, int edge) {
        int side;
        QizaiGeometry.Point[] lower = new QizaiGeometry.Point[4];
        QizaiGeometry.Point[] upper = new QizaiGeometry.Point[4];
        for (side = 0; side < 4; ++side) {
            double angle = (double)side * Math.PI / 2.0;
            lower[side] = QizaiCrystalMesh.offset(start, Math.cos(angle) * width, 0.0, Math.sin(angle) * width);
            upper[side] = QizaiCrystalMesh.offset(end, Math.cos(angle) * width * 0.6, 0.0, Math.sin(angle) * width * 0.6);
        }
        for (side = 0; side < 4; ++side) {
            int next = (side + 1) % 4;
            faces.add(new Face(lower[side], lower[next], upper[next], upper[side], side % 2 == 0 ? color : edge));
        }
        faces.add(new Face(lower[3], lower[2], lower[1], lower[0], color));
        faces.add(new Face(upper[0], upper[1], upper[2], upper[3], edge));
    }

    private static void emit(List<Face> faces, double angle, double scale, double horizontal, double vertical, double depth, QizaiGeometry.Sink sink) {
        double cosine = Math.cos(angle);
        double sine = Math.sin(angle);
        for (Face face : faces) {
            sink.quad(QizaiCrystalMesh.transform(face.first, cosine, sine, scale, horizontal, vertical, depth), QizaiCrystalMesh.transform(face.second, cosine, sine, scale, horizontal, vertical, depth), QizaiCrystalMesh.transform(face.third, cosine, sine, scale, horizontal, vertical, depth), QizaiCrystalMesh.transform(face.fourth, cosine, sine, scale, horizontal, vertical, depth), face.color, 1.0f);
        }
    }

    private static QizaiGeometry.Point transform(QizaiGeometry.Point point, double cosine, double sine, double scale, double horizontal, double vertical, double depth) {
        return new QizaiGeometry.Point((point.x() * cosine - point.z() * sine) * scale + horizontal, point.y() * scale + vertical, (point.x() * sine + point.z() * cosine) * scale + depth);
    }

    private static QizaiGeometry.Point radial(double radius, double height, double angle) {
        return new QizaiGeometry.Point(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
    }

    private static QizaiGeometry.Point offset(QizaiGeometry.Point point, double horizontal, double vertical, double depth) {
        return new QizaiGeometry.Point(point.x() + horizontal, point.y() + vertical, point.z() + depth);
    }

    private static int mix(int first, int second, double amount) {
        int color = 0;
        for (int shift : new int[]{0, 8, 16}) {
            int channel = (int)Math.round((double)(first >> shift & 0xFF) * (1.0 - amount) + (double)(second >> shift & 0xFF) * amount);
            color |= channel << shift;
        }
        return color;
    }

    private record Face(QizaiGeometry.Point first, QizaiGeometry.Point second, QizaiGeometry.Point third, QizaiGeometry.Point fourth, int color) {
    }
}

