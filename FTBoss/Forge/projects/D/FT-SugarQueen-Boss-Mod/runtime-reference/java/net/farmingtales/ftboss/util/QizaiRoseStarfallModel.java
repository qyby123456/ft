/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class QizaiRoseStarfallModel {
    public static final int CASING = 3150911;
    public static final int METAL = 6957147;
    public static final int ROSE = 15746179;
    public static final int EDGE = 16742846;
    public static final int GOLD = 16766047;
    public static final int CORE = 0xFFF3CF;
    public static final double OUTER_RADIUS = 0.56;
    public static final int CHARGED_ORB_REFERENCE_QUADS = 258;

    private QizaiRoseStarfallModel() {
    }

    public static void beam(double age, double length, boolean reduced, float alpha, Sink sink) {
        alpha = QizaiRoseStarfallModel.bounded(alpha);
        if (!QizaiRoseStarfallModel.valid(age, length) || alpha <= 0.0f || sink == null) {
            return;
        }
        double phase = age * 0.24;
        int shellSides = reduced ? 8 : 16;
        QizaiRoseStarfallModel.tube(0.0, length, 0.38, 0.56, shellSides, phase, 15746179, alpha * 0.28f, false, sink);
        QizaiRoseStarfallModel.tube(0.03, length, 0.24, 0.31, reduced ? 7 : 14, -phase * 1.35, 16742846, alpha * 0.68f, false, sink);
        QizaiRoseStarfallModel.tube(0.06, length, 0.085, 0.14, reduced ? 6 : 12, phase * 1.8, 0xFFF3CF, alpha, true, sink);
        int collars = reduced ? 2 : 4;
        for (int index = 0; index < collars; ++index) {
            double travel = QizaiRoseStarfallModel.fractional(age * 0.21 + (double)index / (double)collars);
            double z = 0.18 + Math.max(0.0, length - 0.36) * travel;
            QizaiRoseStarfallModel.ring(z, 0.6384, 0.4368000000000001, shellSides, -phase + (double)index * 0.37, index % 2 == 0 ? 16766047 : 16742846, alpha * 0.72f, sink);
        }
        QizaiRoseStarfallModel.sourceCrown(phase, reduced, alpha, sink);
        QizaiRoseStarfallModel.impactCrown(length, phase, reduced, alpha, sink);
    }

    public static void solid(double age, double length, boolean reduced, float scale, SolidSink output) {
        scale = QizaiRoseStarfallModel.bounded(scale);
        if (!QizaiRoseStarfallModel.valid(age, length) || scale <= 0.0f || output == null) {
            return;
        }
        double phase = age * 0.16;
        Sink source = QizaiRoseStarfallModel.scaled(new Point(0.0, 0.0, 0.0), scale, (a, b, c, d, color, alpha) -> output.quad(a, b, c, d, color));
        int sides = reduced ? 10 : 22;
        QizaiRoseStarfallModel.tube(-0.58, -0.16, 0.08, 0.34, sides, phase, 3150911, 1.0f, true, source);
        QizaiRoseStarfallModel.tube(-0.16, 0.22, 0.34, 0.22, sides, -phase, 6957147, 1.0f, true, source);
        QizaiRoseStarfallModel.ring(-0.12, 0.55, 0.39, reduced ? 10 : 20, phase, 16766047, 1.0f, source);
        int vanes = reduced ? 6 : 10;
        for (int vane = 0; vane < vanes; ++vane) {
            double angle = phase * 0.45 + (double)vane * Math.PI * 2.0 / (double)vanes;
            QizaiRoseStarfallModel.blade(-0.08, angle, 0.3, 0.72, 0.075, vane % 2 == 0 ? 15746179 : 6957147, 1.0f, source);
        }
        double progress = QizaiRoseStarfallModel.smooth(Math.min(1.0, (age + 1.0) / 4.0));
        double head = Math.min(length, length * progress);
        Sink falling = QizaiRoseStarfallModel.scaled(new Point(0.0, 0.0, head), scale, (a, b, c, d, color, alpha) -> output.quad(a, b, c, d, color));
        QizaiRoseStarfallModel.starCore(head, phase, reduced, falling);
    }

    private static void sourceCrown(double phase, boolean reduced, float alpha, Sink sink) {
        int sides = reduced ? 10 : 20;
        QizaiRoseStarfallModel.ring(0.035, 0.67, 0.53, sides, phase, 16766047, alpha * 0.84f, sink);
        QizaiRoseStarfallModel.ring(0.055, 0.42, 0.3, sides, -phase, 0xFFF3CF, alpha * 0.88f, sink);
    }

    private static void impactCrown(double z, double phase, boolean reduced, float alpha, Sink sink) {
        int sides = reduced ? 8 : 14;
        double pulse = 0.05 * Math.sin(phase * 2.6);
        QizaiRoseStarfallModel.ring(z - 0.02, 0.96 + pulse, 0.68 + pulse, sides, -phase, 15746179, alpha * 0.72f, sink);
        QizaiRoseStarfallModel.ring(z - 0.035, 0.61 - pulse, 0.35 - pulse, sides, phase, 16766047, alpha * 0.88f, sink);
        QizaiRoseStarfallModel.ring(z - 0.055, 0.3, 0.11, reduced ? 8 : 16, -phase * 1.4, 0xFFF3CF, alpha, sink);
        int petals = reduced ? 8 : 14;
        for (int petal = 0; petal < petals; ++petal) {
            double angle = (double)petal * Math.PI * 2.0 / (double)petals + phase * 0.18;
            QizaiRoseStarfallModel.blade(z - 0.07, angle, 0.38, petal % 2 == 0 ? 1.24 : 1.05, petal % 2 == 0 ? 0.095 : 0.065, petal % 2 == 0 ? 16742846 : 16766047, alpha * 0.82f, sink);
        }
    }

    private static void starCore(double head, double phase, boolean reduced, Sink sink) {
        int side;
        int sides = reduced ? 8 : 14;
        Point front = new Point(0.0, 0.0, head + 0.42);
        Point rear = new Point(0.0, 0.0, head - 0.72);
        Point[] belt = new Point[sides];
        for (side = 0; side < sides; ++side) {
            double angle = phase + (double)side * Math.PI * 2.0 / (double)sides;
            belt[side] = QizaiRoseStarfallModel.radial(head - 0.08, 0.34, angle);
        }
        for (side = 0; side < sides; ++side) {
            int next = (side + 1) % sides;
            sink.quad(belt[side], belt[next], front, front, side % 2 == 0 ? 0xFFF3CF : 16766047, 1.0f);
            sink.quad(rear, rear, belt[next], belt[side], side % 2 == 0 ? 15746179 : 6957147, 1.0f);
        }
        QizaiRoseStarfallModel.ring(head - 0.18, 0.48, 0.32, reduced ? 10 : 20, -phase, 16766047, 1.0f, sink);
        int fins = reduced ? 6 : 10;
        for (int fin = 0; fin < fins; ++fin) {
            double angle = (double)fin * Math.PI * 2.0 / (double)fins - phase * 0.55;
            Point innerRear = QizaiRoseStarfallModel.radial(head - 0.5, 0.12, angle);
            Point outerRear = QizaiRoseStarfallModel.radial(head - 0.58, 0.57, angle);
            Point outerFront = QizaiRoseStarfallModel.radial(head - 0.06, 0.42, angle + 0.12);
            Point innerFront = QizaiRoseStarfallModel.radial(head + 0.08, 0.16, angle + 0.05);
            sink.quad(innerRear, outerRear, outerFront, innerFront, fin % 2 == 0 ? 16742846 : 3150911, 1.0f);
        }
    }

    private static void tube(double from, double to, double fromRadius, double toRadius, int sides, double phase, int color, float alpha, boolean caps, Sink sink) {
        int side;
        block8: {
            block7: {
                float f;
                if (to <= from || fromRadius < 0.0 || toRadius < 0.0 || sides < 3) break block7;
                alpha = QizaiRoseStarfallModel.bounded(alpha);
                if (!(f <= 0.0f)) break block8;
            }
            return;
        }
        Point[] near = new Point[sides];
        Point[] far = new Point[sides];
        for (side = 0; side < sides; ++side) {
            double angle = phase + (double)side * Math.PI * 2.0 / (double)sides;
            near[side] = QizaiRoseStarfallModel.radial(from, fromRadius, angle);
            far[side] = QizaiRoseStarfallModel.radial(to, toRadius, angle);
        }
        for (side = 0; side < sides; ++side) {
            int next = (side + 1) % sides;
            sink.quad(near[side], near[next], far[next], far[side], color, alpha);
        }
        if (!caps) {
            return;
        }
        Point nearCenter = new Point(0.0, 0.0, from);
        Point farCenter = new Point(0.0, 0.0, to);
        for (int side2 = 0; side2 < sides; ++side2) {
            int next = (side2 + 1) % sides;
            sink.quad(nearCenter, near[next], near[side2], nearCenter, color, alpha);
            sink.quad(farCenter, far[side2], far[next], farCenter, color, alpha);
        }
    }

    private static void ring(double z, double outer, double inner, int sides, double phase, int color, float alpha, Sink sink) {
        block4: {
            block3: {
                float f;
                if (outer <= inner || inner < 0.0 || sides < 3) break block3;
                alpha = QizaiRoseStarfallModel.bounded(alpha);
                if (!(f <= 0.0f)) break block4;
            }
            return;
        }
        double halfDepth = Math.max(0.006, Math.min(0.028, (outer - inner) * 0.12));
        for (int side = 0; side < sides; ++side) {
            double first = phase + (double)side * Math.PI * 2.0 / (double)sides;
            double second = phase + (double)(side + 1) * Math.PI * 2.0 / (double)sides;
            Point outerRear = QizaiRoseStarfallModel.radial(z - halfDepth, outer, first);
            Point outerNextRear = QizaiRoseStarfallModel.radial(z - halfDepth, outer, second);
            Point outerFront = QizaiRoseStarfallModel.radial(z + halfDepth, outer, first);
            Point outerNextFront = QizaiRoseStarfallModel.radial(z + halfDepth, outer, second);
            Point innerRear = QizaiRoseStarfallModel.radial(z - halfDepth, inner, first);
            Point innerNextRear = QizaiRoseStarfallModel.radial(z - halfDepth, inner, second);
            Point innerFront = QizaiRoseStarfallModel.radial(z + halfDepth, inner, first);
            Point innerNextFront = QizaiRoseStarfallModel.radial(z + halfDepth, inner, second);
            sink.quad(outerFront, outerNextFront, innerNextFront, innerFront, color, alpha);
            sink.quad(innerRear, innerNextRear, outerNextRear, outerRear, color, alpha * 0.72f);
            sink.quad(outerRear, outerNextRear, outerNextFront, outerFront, color, alpha * 0.82f);
            sink.quad(innerFront, innerNextFront, innerNextRear, innerRear, color, alpha * 0.62f);
        }
    }

    private static void blade(double z, double angle, double inner, double outer, double halfWidth, int color, float alpha, Sink sink) {
        if ((alpha = QizaiRoseStarfallModel.bounded(alpha)) <= 0.0f) {
            return;
        }
        double x = Math.cos(angle);
        double y = Math.sin(angle);
        double tx = -y;
        double ty = x;
        double depth = Math.max(0.012, halfWidth * 0.22);
        Point[] rear = new Point[]{new Point(x * inner - tx * halfWidth, y * inner - ty * halfWidth, z - depth), new Point(x * outer - tx * halfWidth, y * outer - ty * halfWidth, z - depth), new Point(x * outer + tx * halfWidth, y * outer + ty * halfWidth, z - depth), new Point(x * inner + tx * halfWidth, y * inner + ty * halfWidth, z - depth)};
        Point[] front = new Point[]{new Point(rear[0].x, rear[0].y, z + depth), new Point(rear[1].x, rear[1].y, z + depth), new Point(rear[2].x, rear[2].y, z + depth), new Point(rear[3].x, rear[3].y, z + depth)};
        sink.quad(front[0], front[1], front[2], front[3], color, alpha);
        sink.quad(rear[3], rear[2], rear[1], rear[0], color, alpha * 0.68f);
        for (int edge = 0; edge < 4; ++edge) {
            int next = (edge + 1) % 4;
            sink.quad(rear[edge], rear[next], front[next], front[edge], color, alpha * 0.82f);
        }
    }

    private static Point radial(double z, double radius, double angle) {
        return new Point(Math.cos(angle) * radius, Math.sin(angle) * radius, z);
    }

    private static Sink scaled(Point center, double scale, Sink sink) {
        return (a, b, c, d, color, alpha) -> sink.quad(QizaiRoseStarfallModel.scale(center, a, scale), QizaiRoseStarfallModel.scale(center, b, scale), QizaiRoseStarfallModel.scale(center, c, scale), QizaiRoseStarfallModel.scale(center, d, scale), color, alpha);
    }

    private static Point scale(Point center, Point point, double scale) {
        return new Point(center.x + (point.x - center.x) * scale, center.y + (point.y - center.y) * scale, center.z + (point.z - center.z) * scale);
    }

    private static boolean valid(double age, double length) {
        return Double.isFinite(age) && age >= 0.0 && Double.isFinite(length) && length >= 0.05 && length <= 64.0;
    }

    private static double fractional(double value) {
        return value - Math.floor(value);
    }

    private static double smooth(double value) {
        double v = Math.max(0.0, Math.min(1.0, value));
        return v * v * (3.0 - 2.0 * v);
    }

    private static float bounded(float value) {
        return Float.isFinite(value) ? Math.max(0.0f, Math.min(1.0f, value)) : 0.0f;
    }

    @FunctionalInterface
    public static interface Sink {
        public void quad(Point var1, Point var2, Point var3, Point var4, int var5, float var6);
    }

    public record Point(double x, double y, double z) {
    }

    @FunctionalInterface
    public static interface SolidSink {
        public void quad(Point var1, Point var2, Point var3, Point var4, int var5);
    }
}

