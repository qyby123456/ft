package net.farmingtales.ftboss.util;

public final class ManorCeremonyModel {
    public static final int STONE = 0x343C50;
    public static final int GOLD = 0xDDB56E;
    public static final int IVORY = 0xFFE8B5;
    public static final int APPLE = 0xC94865;
    public static final int LEAF = 0x62B995;
    public static final int[] CRESTS = {0xE87EB1, 0xA781E4, 0xDDA856, 0xFFB945, 0x65C3EB, 0x53BCA6,
            0xA7D885, 0xA087DC, 0xDA677E, 0x90C6DE, 0xEAD188, 0xB88CCD};

    public record Point(double x, double y, double z) {}
    @FunctionalInterface public interface Sink { void vertex(double posX, double posY, double posZ, int color, float alpha); }
    private ManorCeremonyModel() {}

    public static void box(double width, double height, double depth, int color, float alpha, Sink sink) {
        double halfWidth = width / 2, halfDepth = depth / 2;
        Point[] corners = {new Point(-halfWidth, 0, -halfDepth), new Point(halfWidth, 0, -halfDepth),
                new Point(halfWidth, height, -halfDepth), new Point(-halfWidth, height, -halfDepth),
                new Point(-halfWidth, 0, halfDepth), new Point(halfWidth, 0, halfDepth),
                new Point(halfWidth, height, halfDepth), new Point(-halfWidth, height, halfDepth)};
        int[][] faces = {{0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7}, {1, 5, 6, 2}, {3, 2, 6, 7}, {4, 5, 1, 0}};
        for (int[] face : faces) quad(corners[face[0]], corners[face[1]], corners[face[2]], corners[face[3]], color, alpha, sink);
    }

    public static void gem(double width, double height, int color, float alpha, Sink sink) {
        for (int facet = 0; facet < 6; facet++) {
            Point first = radial(width, 0, facet * Math.PI / 3), second = radial(width, 0, (facet + 1) * Math.PI / 3);
            quad(first, second, new Point(0, height, 0), new Point(0, height, 0), color, alpha, sink);
            quad(second, first, new Point(0, -height, 0), new Point(0, -height, 0), color, alpha, sink);
        }
    }

    public static void band(double radius, double width, double height, int segments, int color, float alpha, Sink sink) {
        for (int segment = 0; segment < segments; segment++) {
            double first = segment * Math.PI * 2 / segments, second = (segment + 1) * Math.PI * 2 / segments;
            Point outerFirst = radial(radius, height, first), outerSecond = radial(radius, height, second);
            Point innerFirst = radial(radius - width, height, first), innerSecond = radial(radius - width, height, second);
            quad(outerFirst, outerSecond, innerSecond, innerFirst, color, alpha, sink);
            quad(radial(radius, 0, second), radial(radius, 0, first), radial(radius - width, 0, first), radial(radius - width, 0, second), color, alpha, sink);
            quad(outerSecond, outerFirst, radial(radius, 0, first), radial(radius, 0, second), color, alpha, sink);
            quad(innerFirst, innerSecond, radial(radius - width, 0, second), radial(radius - width, 0, first), color, alpha, sink);
        }
    }

    public static void apple(float alpha, Sink sink) {
        Point[] outline = {new Point(0, -0.76, 0), new Point(-0.55, -0.61, 0), new Point(-0.9, -0.08, 0),
                new Point(-0.8, 0.46, 0), new Point(-0.35, 0.63, 0), new Point(0, 0.43, 0),
                new Point(0.35, 0.63, 0), new Point(0.8, 0.46, 0), new Point(0.9, -0.08, 0), new Point(0.55, -0.61, 0)};
        for (int corner = 0; corner < outline.length; corner++) {
            Point first = outline[corner], next = outline[(corner + 1) % outline.length];
            Point front = new Point(first.x(), first.y(), -0.14), frontNext = new Point(next.x(), next.y(), -0.14);
            Point back = new Point(first.x(), first.y(), 0.14), backNext = new Point(next.x(), next.y(), 0.14);
            quad(front, frontNext, new Point(0, 0, -0.42), new Point(0, 0, -0.42), APPLE, alpha, sink);
            quad(backNext, back, new Point(0, 0, 0.42), new Point(0, 0, 0.42), APPLE, alpha, sink);
            quad(frontNext, front, back, backNext, GOLD, alpha, sink);
        }
        box(0.12, 0.44, 0.13, GOLD, alpha, pose(0, 0.48, 0, 0, 0, -0.2, 1, sink));
        gem(0.18, 0.44, LEAF, alpha, pose(0.4, 0.84, 0, 0, 0, -0.9, 1, sink));
    }

    public static void crown(boolean reduced, float alpha, Sink sink) {
        band(1.5, 0.15, 0.24, reduced ? 12 : 20, GOLD, alpha, sink);
        for (int spike = 0; spike < 7; spike++) {
            double angle = spike * Math.PI * 2 / 7;
            Sink point = pose(Math.cos(angle) * 1.4, 0.5, Math.sin(angle) * 1.4, -angle, 0, 0, 1, sink);
            gem(0.22, 0.65, GOLD, alpha, point);
            gem(0.1, 0.17, APPLE, alpha, pose(0, 0.64, 0, 0, 0, 0, 1, point));
        }
    }

    public static void crest(int variant, float alpha, Sink sink) {
        int color = variant < 0 ? GOLD : CRESTS[Math.floorMod(variant, CRESTS.length)];
        gem(0.4, 0.64, GOLD, alpha, sink);
        gem(0.30, 0.48, color, alpha, pose(0, 0, -0.36, 0, 0, 0, 1, sink));
        box(0.06, 0.26, 0.06, IVORY, alpha, pose(0, -0.12, -0.67, 0, 0, 0, 1, sink));
    }

    public static void gate(double openness, int variant, boolean reduced, float alpha, Sink sink) {
        for (int side : new int[]{-1, 1}) {
            Sink pillar = pose(side * 3.3, 0, 0, 0, 0, 0, 1, sink);
            box(1, 0.35, 1.3, GOLD, alpha, pillar);
            box(0.65, 5.8, 0.8, STONE, alpha, pose(0, 0.35, 0, 0, 0, 0, 1, pillar));
            box(0.1, 5.3, 0.1, GOLD, alpha, pose(0, 0.5, -0.43, 0, 0, 0, 1, pillar));
            box(1, 0.28, 1.1, GOLD, alpha, pose(0, 5.85, 0, 0, 0, 0, 1, pillar));
            gem(0.42, 0.9, variant < 0 ? LEAF : CRESTS[Math.floorMod(variant, CRESTS.length)], alpha,
                    pose(0, 6.65, 0, 0, 0, 0, 1, pillar));
            box(0.36, 3.65, 0.6, GOLD, alpha, pose(side * 3.15, 5.85, 0, 0, 0, side * Math.PI / 3, 1, sink));
            Sink hinge = pose(side * 2.9, 0.35, 0.13, -side * openness * 1.4, 0, 0, 1, sink);
            Sink door = pose(-side * 1.4, 0, 0, 0, 0, 0, 1, hinge);
            box(2.8, 5.2, 0.18, STONE, alpha * 0.83F, door);
            for (int rail : new int[]{-1, 1}) box(0.1, 5.2, 0.3, GOLD, alpha, pose(rail * 1.3, 0, 0, 0, 0, 0, 1, door));
            for (double height : new double[]{0.1, 2.6, 5.0}) box(2.6, 0.09, 0.3, GOLD, alpha, pose(0, height, 0, 0, 0, 0, 1, door));
            crest(variant, alpha, pose(0, 3.3, -0.22, 0, 0, 0, 1.5, door));
        }
        apple(alpha, pose(0, 8.1, 0, 0, 0, 0, 1.1, sink));
        if (!reduced) crown(false, alpha, pose(0, 8.8, 0, 0, 0, 0, 0.6, sink));
    }

    public static void chest(double open, float alpha, Sink sink) {
        box(1.45, 0.8, 1.05, STONE, alpha, sink);
        for (int side : new int[]{-1, 1}) box(0.15, 0.86, 1.1, GOLD, alpha, pose(side * 0.55, 0, 0, 0, 0, 0, 1, sink));
        Sink lid = pose(0, 0.82, 0.525, 0, -open * 1.1, 0, 1, sink);
        box(1.48, 0.2, 1.05, GOLD, alpha, pose(0, 0, -0.525, 0, 0, 0, 1, lid));
        box(1.1, 0.1, 0.8, APPLE, alpha, pose(0, 0.2, -0.525, 0, 0, 0, 1, lid));
        gem(0.13, 0.2, IVORY, alpha, pose(0, 0.63, -0.59, 0, 0, 0, 1, sink));
    }

    public static Sink pose(double offsetX, double offsetY, double offsetZ, double yaw, double pitch, double roll, double scale, Sink sink) {
        double yawCos = Math.cos(yaw), yawSin = Math.sin(yaw), pitchCos = Math.cos(pitch), pitchSin = Math.sin(pitch);
        double rollCos = Math.cos(roll), rollSin = Math.sin(roll);
        double[] matrix = {(rollCos * yawCos + rollSin * pitchSin * yawSin) * scale,
                (-rollSin * yawCos + rollCos * pitchSin * yawSin) * scale, pitchCos * yawSin * scale,
                rollSin * pitchCos * scale, rollCos * pitchCos * scale, -pitchSin * scale,
                (-rollCos * yawSin + rollSin * pitchSin * yawCos) * scale,
                (rollSin * yawSin + rollCos * pitchSin * yawCos) * scale, pitchCos * yawCos * scale};
        return (posX, posY, posZ, color, alpha) -> sink.vertex(offsetX + matrix[0] * posX + matrix[1] * posY + matrix[2] * posZ,
                offsetY + matrix[3] * posX + matrix[4] * posY + matrix[5] * posZ,
                offsetZ + matrix[6] * posX + matrix[7] * posY + matrix[8] * posZ, color, alpha);
    }

    public static void quad(Point first, Point second, Point third, Point fourth, int color, float alpha, Sink sink) {
        double normalX = (second.y - first.y) * (fourth.z - first.z) - (second.z - first.z) * (fourth.y - first.y);
        double normalY = (second.z - first.z) * (fourth.x - first.x) - (second.x - first.x) * (fourth.z - first.z);
        double normalZ = (second.x - first.x) * (fourth.y - first.y) - (second.y - first.y) * (fourth.x - first.x);
        double length = Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        double light = length < 0.00001 ? 0.9 : 0.72 + 0.28 * Math.abs((normalX * 0.35 + normalY * 0.8 + normalZ * 0.48) / length);
        int shaded = (int) (((color >> 16) & 255) * light) << 16 | (int) (((color >> 8) & 255) * light) << 8 | (int) ((color & 255) * light);
        sink.vertex(first.x, first.y, first.z, shaded, alpha);
        sink.vertex(second.x, second.y, second.z, shaded, alpha);
        sink.vertex(third.x, third.y, third.z, shaded, alpha);
        sink.vertex(fourth.x, fourth.y, fourth.z, shaded, alpha);
    }

    private static Point radial(double radius, double height, double angle) { return new Point(Math.cos(angle) * radius, height, Math.sin(angle) * radius); }
}
