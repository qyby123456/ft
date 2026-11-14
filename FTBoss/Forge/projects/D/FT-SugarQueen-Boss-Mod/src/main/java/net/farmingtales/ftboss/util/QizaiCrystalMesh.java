package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.util.QizaiGeometry.Point;
import net.farmingtales.ftboss.util.QizaiGeometry.Sink;

public final class QizaiCrystalMesh {
    private static final int METAL = 0x292539;
    private static final int GOLD = 0xB38B51;
    private static final int EDGE = 0xEDCB83;
    private static final int[][] FACETS = {
            {0x48316D, 0x7950A6, 0xBD91E6, 0x9164C4, 0x66418F, 0x34234E, 0x53367D, 0x9B78BF},
            {0x193D62, 0x317EAA, 0x91DDE7, 0x56B2D0, 0x286887, 0x192F48, 0x265270, 0x6AB7CC},
            {0x734F27, 0xAD823D, 0xF6DE94, 0xDCB866, 0x936A30, 0x4B3928, 0x775830, 0xC6A968}
    };
    private static final List<List<Face>> CORES = List.of(core(0), core(1), core(2));
    private static final List<List<Face>> SHARDS = List.of(shard(0), shard(1), shard(2));

    private record Face(Point first, Point second, Point third, Point fourth, int color) {}

    private QizaiCrystalMesh() {}

    public static void render(int role, double age, boolean satellites, Sink sink) {
        if (!Double.isFinite(age)) return;
        int palette = Math.max(0, Math.min(2, role));
        double rotation = age * 0.018;
        emit(CORES.get(palette), rotation, 1, 0, 0, 0, sink);
        if (!satellites) return;
        int count = palette == 1 ? 2 : 3;
        for (int index = 0; index < count; index++) {
            double angle = -rotation * 0.8 + index * Math.PI * 2 / count;
            emit(SHARDS.get(palette), angle + Math.PI / 4, 0.3,
                    Math.cos(angle) * 0.68, Math.sin(rotation * 2 + index) * 0.16,
                    Math.sin(angle) * 0.68, sink);
        }
    }

    private static List<Face> core(int role) {
        List<Face> faces = new ArrayList<>();
        double width = role == 0 ? 0.27 : role == 1 ? 0.3 : 0.34;
        gem(faces, role, width, 0.65, 0, 0, 0);
        band(faces, width + 0.055, width - 0.008, -0.25, -0.17, METAL);
        band(faces, width + 0.06, width + 0.043, -0.245, -0.23, GOLD);
        band(faces, width + 0.061, width + 0.043, -0.19, -0.172, EDGE);
        for (int index = 0; index < 4; index++) {
            double angle = index * Math.PI / 2 + Math.PI / 8;
            double baseRadius = width + 0.03;
            Point lower = radial(baseRadius * 0.48, -0.43, angle);
            Point middle = radial(baseRadius + 0.09, -0.02, angle);
            Point tip = radial(baseRadius * 0.84, 0.29 + (role == 2 ? 0.1 : 0), angle);
            strut(faces, lower, middle, 0.034, METAL, GOLD);
            strut(faces, middle, tip, 0.025, GOLD, EDGE);
        }
        if (role == 1) {
            gem(faces, role, 0.085, 0.27, -0.46, 0.05, 0);
            gem(faces, role, 0.085, 0.27, 0.46, 0.05, 0);
        }
        return List.copyOf(faces);
    }

    private static List<Face> shard(int role) {
        List<Face> faces = new ArrayList<>();
        gem(faces, role, 0.21, 0.48, 0, 0, 0);
        band(faces, 0.23, 0.20, -0.12, -0.06, GOLD);
        return List.copyOf(faces);
    }

    private static void gem(List<Face> faces, int role, double width, double height, double horizontal, double vertical, double depth) {
        double[] radii = {0.015, width * 0.78, width, width * 0.84, 0.012};
        double[] heights = {-height, -height * 0.34, height * 0.22, height * 0.42, height};
        for (int layer = 0; layer < radii.length - 1; layer++) {
            for (int side = 0; side < 8; side++) {
                double start = side * Math.PI / 4, end = start + Math.PI / 4;
                Point first = offset(radial(radii[layer], heights[layer], start), horizontal, vertical, depth);
                Point second = offset(radial(radii[layer], heights[layer], end), horizontal, vertical, depth);
                Point third = offset(radial(radii[layer + 1], heights[layer + 1], end), horizontal, vertical, depth);
                Point fourth = offset(radial(radii[layer + 1], heights[layer + 1], start), horizontal, vertical, depth);
                int color = FACETS[role][side];
                if (layer == 2) color = mix(color, 0xE8DFF5, 0.28);
                if (layer == 0) color = mix(color, 0x161B30, 0.22);
                faces.add(new Face(first, second, third, fourth, color));
            }
        }
        for (int side = 0; side < 8; side++) {
            double start = side * Math.PI / 4, end = start + Math.PI / 4;
            Point top = new Point(horizontal, vertical + height, depth);
            Point bottom = new Point(horizontal, vertical - height, depth);
            faces.add(new Face(top, offset(radial(0.012, height, start), horizontal, vertical, depth),
                    offset(radial(0.012, height, end), horizontal, vertical, depth), top, FACETS[role][2]));
            faces.add(new Face(bottom, offset(radial(0.015, -height, end), horizontal, vertical, depth),
                    offset(radial(0.015, -height, start), horizontal, vertical, depth), bottom, FACETS[role][0]));
        }
    }

    private static void band(List<Face> faces, double outer, double inner, double lower, double upper, int color) {
        for (int side = 0; side < 8; side++) {
            double start = side * Math.PI / 4, end = start + Math.PI / 4;
            Point outerLow = radial(outer, lower, start), outerNextLow = radial(outer, lower, end);
            Point outerHigh = radial(outer, upper, start), outerNextHigh = radial(outer, upper, end);
            Point innerLow = radial(inner, lower, start), innerNextLow = radial(inner, lower, end);
            Point innerHigh = radial(inner, upper, start), innerNextHigh = radial(inner, upper, end);
            faces.add(new Face(outerLow, outerNextLow, outerNextHigh, outerHigh, mix(color, 0x171523, side % 3 * 0.13)));
            faces.add(new Face(innerLow, innerHigh, innerNextHigh, innerNextLow, mix(color, 0x171523, 0.25)));
            faces.add(new Face(outerHigh, outerNextHigh, innerNextHigh, innerHigh, color));
            faces.add(new Face(outerLow, innerLow, innerNextLow, outerNextLow, mix(color, 0x171523, 0.3)));
        }
    }

    private static void strut(List<Face> faces, Point start, Point end, double width, int color, int edge) {
        Point[] lower = new Point[4], upper = new Point[4];
        for (int side = 0; side < 4; side++) {
            double angle = side * Math.PI / 2;
            lower[side] = offset(start, Math.cos(angle) * width, 0, Math.sin(angle) * width);
            upper[side] = offset(end, Math.cos(angle) * width * 0.6, 0, Math.sin(angle) * width * 0.6);
        }
        for (int side = 0; side < 4; side++) {
            int next = (side + 1) % 4;
            faces.add(new Face(lower[side], lower[next], upper[next], upper[side], side % 2 == 0 ? color : edge));
        }
        faces.add(new Face(lower[3], lower[2], lower[1], lower[0], color));
        faces.add(new Face(upper[0], upper[1], upper[2], upper[3], edge));
    }

    private static void emit(List<Face> faces, double angle, double scale, double horizontal, double vertical, double depth, Sink sink) {
        double cosine = Math.cos(angle), sine = Math.sin(angle);
        for (Face face : faces) {
            sink.quad(transform(face.first, cosine, sine, scale, horizontal, vertical, depth),
                    transform(face.second, cosine, sine, scale, horizontal, vertical, depth),
                    transform(face.third, cosine, sine, scale, horizontal, vertical, depth),
                    transform(face.fourth, cosine, sine, scale, horizontal, vertical, depth), face.color, 1);
        }
    }

    private static Point transform(Point point, double cosine, double sine, double scale, double horizontal, double vertical, double depth) {
        return new Point((point.x() * cosine - point.z() * sine) * scale + horizontal,
                point.y() * scale + vertical, (point.x() * sine + point.z() * cosine) * scale + depth);
    }

    private static Point radial(double radius, double height, double angle) {
        return new Point(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
    }

    private static Point offset(Point point, double horizontal, double vertical, double depth) {
        return new Point(point.x() + horizontal, point.y() + vertical, point.z() + depth);
    }

    private static int mix(int first, int second, double amount) {
        int color = 0;
        for (int shift : new int[]{0, 8, 16}) {
            int channel = (int) Math.round(((first >> shift) & 255) * (1 - amount) + ((second >> shift) & 255) * amount);
            color |= channel << shift;
        }
        return color;
    }
}
