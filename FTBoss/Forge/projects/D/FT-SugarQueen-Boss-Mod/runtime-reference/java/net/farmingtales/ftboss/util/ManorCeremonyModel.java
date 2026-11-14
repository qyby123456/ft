/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.ManorAuthoredGeometry;

public final class ManorCeremonyModel {
    public static final int STONE = 3423312;
    public static final int GOLD = 14529902;
    public static final int IVORY = 16771253;
    public static final int APPLE = 13191269;
    public static final int LEAF = 6470037;
    public static final int[] CRESTS = new int[]{15236785, 10977764, 14526550, 16759109, 6669291, 5487782, 10999941, 10520540, 14313342, 9488094, 15389064, 12094669};

    private ManorCeremonyModel() {
    }

    public static void box(double width, double height, double depth, int color, float alpha, Sink sink) {
        int[][] faces;
        double halfWidth = width / 2.0;
        double halfDepth = depth / 2.0;
        Point[] corners = new Point[]{new Point(-halfWidth, 0.0, -halfDepth), new Point(halfWidth, 0.0, -halfDepth), new Point(halfWidth, height, -halfDepth), new Point(-halfWidth, height, -halfDepth), new Point(-halfWidth, 0.0, halfDepth), new Point(halfWidth, 0.0, halfDepth), new Point(halfWidth, height, halfDepth), new Point(-halfWidth, height, halfDepth)};
        for (int[] face : faces = new int[][]{{0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7}, {1, 5, 6, 2}, {3, 2, 6, 7}, {4, 5, 1, 0}}) {
            ManorCeremonyModel.quad(corners[face[0]], corners[face[1]], corners[face[2]], corners[face[3]], color, alpha, sink);
        }
    }

    public static void gem(double width, double height, int color, float alpha, Sink sink) {
        for (int facet = 0; facet < 6; ++facet) {
            Point first = ManorCeremonyModel.radial(width, 0.0, (double)facet * Math.PI / 3.0);
            Point second = ManorCeremonyModel.radial(width, 0.0, (double)(facet + 1) * Math.PI / 3.0);
            ManorCeremonyModel.quad(first, second, new Point(0.0, height, 0.0), new Point(0.0, height, 0.0), color, alpha, sink);
            ManorCeremonyModel.quad(second, first, new Point(0.0, -height, 0.0), new Point(0.0, -height, 0.0), color, alpha, sink);
        }
    }

    public static void band(double radius, double width, double height, int segments, int color, float alpha, Sink sink) {
        for (int segment = 0; segment < segments; ++segment) {
            double first = (double)segment * Math.PI * 2.0 / (double)segments;
            double second = (double)(segment + 1) * Math.PI * 2.0 / (double)segments;
            Point outerFirst = ManorCeremonyModel.radial(radius, height, first);
            Point outerSecond = ManorCeremonyModel.radial(radius, height, second);
            Point innerFirst = ManorCeremonyModel.radial(radius - width, height, first);
            Point innerSecond = ManorCeremonyModel.radial(radius - width, height, second);
            ManorCeremonyModel.quad(outerFirst, outerSecond, innerSecond, innerFirst, color, alpha, sink);
            ManorCeremonyModel.quad(ManorCeremonyModel.radial(radius, 0.0, second), ManorCeremonyModel.radial(radius, 0.0, first), ManorCeremonyModel.radial(radius - width, 0.0, first), ManorCeremonyModel.radial(radius - width, 0.0, second), color, alpha, sink);
            ManorCeremonyModel.quad(outerSecond, outerFirst, ManorCeremonyModel.radial(radius, 0.0, first), ManorCeremonyModel.radial(radius, 0.0, second), color, alpha, sink);
            ManorCeremonyModel.quad(innerFirst, innerSecond, ManorCeremonyModel.radial(radius - width, 0.0, second), ManorCeremonyModel.radial(radius - width, 0.0, first), color, alpha, sink);
        }
    }

    public static void apple(float f, Sink sink) {
        ManorAuthoredGeometry.apple(f, sink);
    }

    public static void crown(boolean bl, float f, Sink sink) {
        ManorAuthoredGeometry.crown(bl, f, sink);
    }

    public static void crest(int n, float f, Sink sink) {
        ManorAuthoredGeometry.crest(n, f, sink);
    }

    public static void gate(double d, int n, boolean bl, float f, Sink sink) {
        ManorAuthoredGeometry.gate(d, n, bl, f, sink);
    }

    public static void chest(double d, float f, Sink sink) {
        ManorAuthoredGeometry.chest(d, f, sink);
    }

    public static Sink pose(double offsetX, double offsetY, double offsetZ, double yaw, double pitch, double roll, double scale, Sink sink) {
        double yawCos = Math.cos(yaw);
        double yawSin = Math.sin(yaw);
        double pitchCos = Math.cos(pitch);
        double pitchSin = Math.sin(pitch);
        double rollCos = Math.cos(roll);
        double rollSin = Math.sin(roll);
        double[] matrix = new double[]{(rollCos * yawCos + rollSin * pitchSin * yawSin) * scale, (-rollSin * yawCos + rollCos * pitchSin * yawSin) * scale, pitchCos * yawSin * scale, rollSin * pitchCos * scale, rollCos * pitchCos * scale, -pitchSin * scale, (-rollCos * yawSin + rollSin * pitchSin * yawCos) * scale, (rollSin * yawSin + rollCos * pitchSin * yawCos) * scale, pitchCos * yawCos * scale};
        return (posX, posY, posZ, color, alpha) -> sink.vertex(offsetX + matrix[0] * posX + matrix[1] * posY + matrix[2] * posZ, offsetY + matrix[3] * posX + matrix[4] * posY + matrix[5] * posZ, offsetZ + matrix[6] * posX + matrix[7] * posY + matrix[8] * posZ, color, alpha);
    }

    public static void quad(Point first, Point second, Point third, Point fourth, int color, float alpha, Sink sink) {
        double normalX = (second.y - first.y) * (fourth.z - first.z) - (second.z - first.z) * (fourth.y - first.y);
        double normalY = (second.z - first.z) * (fourth.x - first.x) - (second.x - first.x) * (fourth.z - first.z);
        double normalZ = (second.x - first.x) * (fourth.y - first.y) - (second.y - first.y) * (fourth.x - first.x);
        double length = Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        double light = length < 1.0E-5 ? 0.9 : 0.72 + 0.28 * Math.abs((normalX * 0.35 + normalY * 0.8 + normalZ * 0.48) / length);
        int shaded = (int)((double)(color >> 16 & 0xFF) * light) << 16 | (int)((double)(color >> 8 & 0xFF) * light) << 8 | (int)((double)(color & 0xFF) * light);
        sink.vertex(first.x, first.y, first.z, shaded, alpha);
        sink.vertex(second.x, second.y, second.z, shaded, alpha);
        sink.vertex(third.x, third.y, third.z, shaded, alpha);
        sink.vertex(fourth.x, fourth.y, fourth.z, shaded, alpha);
    }

    private static Point radial(double radius, double height, double angle) {
        return new Point(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
    }

    public record Point(double x, double y, double z) {
    }

    @FunctionalInterface
    public static interface Sink {
        public void vertex(double var1, double var3, double var5, int var7, float var8);
    }
}

