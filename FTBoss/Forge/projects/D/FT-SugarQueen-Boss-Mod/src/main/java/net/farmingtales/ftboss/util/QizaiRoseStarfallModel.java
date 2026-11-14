package net.farmingtales.ftboss.util;

/** Dedicated procedural model for Rose Starfall's descending strike beam. */
public final class QizaiRoseStarfallModel {
    public static final int CASING = 0x30143F;
    public static final int METAL = 0x6A285B;
    public static final int ROSE = 0xF04483;
    public static final int EDGE = 0xFF79BE;
    public static final int GOLD = 0xFFD45F;
    public static final int CORE = 0xFFF3CF;
    public static final double OUTER_RADIUS = 0.56;
    public static final int CHARGED_ORB_REFERENCE_QUADS = RemainingSpellModels.CHARGED_ORB_REFERENCE_QUADS;

    public record Point(double x, double y, double z) {}
    @FunctionalInterface public interface Sink {
        void quad(Point first, Point second, Point third, Point fourth, int color, float alpha);
    }
    @FunctionalInterface public interface SolidSink {
        void quad(Point first, Point second, Point third, Point fourth, int color);
    }

    private QizaiRoseStarfallModel() {}

    /** Translucent three-layer strike, traveling collars and the impact rose crown. */
    public static void beam(double age, double length, boolean reduced, float alpha, Sink sink) {
        alpha = bounded(alpha);
        if (!valid(age, length) || alpha <= 0 || sink == null) return;
        double phase = age * 0.24;
        int shellSides = reduced ? 8 : 16;
        tube(0, length, 0.38, OUTER_RADIUS, shellSides, phase, ROSE, alpha * 0.28F, false, sink);
        tube(0.03, length, 0.24, 0.31, reduced ? 7 : 14, -phase * 1.35, EDGE, alpha * 0.68F, false, sink);
        tube(0.06, length, 0.085, 0.14, reduced ? 6 : 12, phase * 1.8, CORE, alpha, true, sink);

        int collars = reduced ? 2 : 4;
        for (int index = 0; index < collars; index++) {
            double travel = fractional(age * 0.21 + index / (double) collars);
            double z = 0.18 + Math.max(0, length - 0.36) * travel;
            ring(z, OUTER_RADIUS * 1.14, OUTER_RADIUS * 0.78, shellSides,
                    -phase + index * 0.37, index % 2 == 0 ? GOLD : EDGE, alpha * 0.72F, sink);
        }
        sourceCrown(phase, reduced, alpha, sink);
        impactCrown(length, phase, reduced, alpha, sink);
    }

    /** Opaque depth-writing sky lens and the faceted star core that falls down the beam. */
    public static void solid(double age, double length, boolean reduced, float scale, SolidSink output) {
        scale = bounded(scale);
        if (!valid(age, length) || scale <= 0 || output == null) return;
        double phase = age * 0.16;
        Sink source = scaled(new Point(0, 0, 0), scale,
                (a, b, c, d, color, alpha) -> output.quad(a, b, c, d, color));
        int sides = reduced ? 10 : 22;
        tube(-0.58, -0.16, 0.08, 0.34, sides, phase, CASING, 1, true, source);
        tube(-0.16, 0.22, 0.34, 0.22, sides, -phase, METAL, 1, true, source);
        ring(-0.12, 0.55, 0.39, reduced ? 10 : 20, phase, GOLD, 1, source);
        int vanes = reduced ? 6 : 10;
        for (int vane = 0; vane < vanes; vane++) {
            double angle = phase * 0.45 + vane * Math.PI * 2 / vanes;
            blade(-0.08, angle, 0.30, 0.72, 0.075, vane % 2 == 0 ? ROSE : METAL, 1, source);
        }

        double progress = smooth(Math.min(1, (age + 1) / 4));
        double head = Math.min(length, length * progress);
        Sink falling = scaled(new Point(0, 0, head), scale,
                (a, b, c, d, color, alpha) -> output.quad(a, b, c, d, color));
        starCore(head, phase, reduced, falling);
    }

    private static void sourceCrown(double phase, boolean reduced, float alpha, Sink sink) {
        int sides = reduced ? 10 : 20;
        ring(0.035, 0.67, 0.53, sides, phase, GOLD, alpha * 0.84F, sink);
        ring(0.055, 0.42, 0.30, sides, -phase, CORE, alpha * 0.88F, sink);
    }

    private static void impactCrown(double z, double phase, boolean reduced, float alpha, Sink sink) {
        int sides = reduced ? 8 : 14;
        double pulse = 0.05 * Math.sin(phase * 2.6);
        ring(z - 0.02, 0.96 + pulse, 0.68 + pulse, sides, -phase, ROSE, alpha * 0.72F, sink);
        ring(z - 0.035, 0.61 - pulse, 0.35 - pulse, sides, phase, GOLD, alpha * 0.88F, sink);
        ring(z - 0.055, 0.30, 0.11, reduced ? 8 : 16, -phase * 1.4, CORE, alpha, sink);
        int petals = reduced ? 8 : 14;
        for (int petal = 0; petal < petals; petal++) {
            double angle = petal * Math.PI * 2 / petals + phase * 0.18;
            blade(z - 0.07, angle, 0.38, petal % 2 == 0 ? 1.24 : 1.05,
                    petal % 2 == 0 ? 0.095 : 0.065, petal % 2 == 0 ? EDGE : GOLD, alpha * 0.82F, sink);
        }
    }

    private static void starCore(double head, double phase, boolean reduced, Sink sink) {
        int sides = reduced ? 8 : 14;
        Point front = new Point(0, 0, head + 0.42);
        Point rear = new Point(0, 0, head - 0.72);
        Point[] belt = new Point[sides];
        for (int side = 0; side < sides; side++) {
            double angle = phase + side * Math.PI * 2 / sides;
            belt[side] = radial(head - 0.08, 0.34, angle);
        }
        for (int side = 0; side < sides; side++) {
            int next = (side + 1) % sides;
            sink.quad(belt[side], belt[next], front, front, side % 2 == 0 ? CORE : GOLD, 1);
            sink.quad(rear, rear, belt[next], belt[side], side % 2 == 0 ? ROSE : METAL, 1);
        }
        ring(head - 0.18, 0.48, 0.32, reduced ? 10 : 20, -phase, GOLD, 1, sink);
        int fins = reduced ? 6 : 10;
        for (int fin = 0; fin < fins; fin++) {
            double angle = fin * Math.PI * 2 / fins - phase * 0.55;
            Point innerRear = radial(head - 0.50, 0.12, angle);
            Point outerRear = radial(head - 0.58, 0.57, angle);
            Point outerFront = radial(head - 0.06, 0.42, angle + 0.12);
            Point innerFront = radial(head + 0.08, 0.16, angle + 0.05);
            sink.quad(innerRear, outerRear, outerFront, innerFront, fin % 2 == 0 ? EDGE : CASING, 1);
        }
    }

    private static void tube(double from, double to, double fromRadius, double toRadius, int sides,
                             double phase, int color, float alpha, boolean caps, Sink sink) {
        if (to <= from || fromRadius < 0 || toRadius < 0 || sides < 3 || (alpha = bounded(alpha)) <= 0) return;
        Point[] near = new Point[sides], far = new Point[sides];
        for (int side = 0; side < sides; side++) {
            double angle = phase + side * Math.PI * 2 / sides;
            near[side] = radial(from, fromRadius, angle);
            far[side] = radial(to, toRadius, angle);
        }
        for (int side = 0; side < sides; side++) {
            int next = (side + 1) % sides;
            sink.quad(near[side], near[next], far[next], far[side], color, alpha);
        }
        if (!caps) return;
        Point nearCenter = new Point(0, 0, from), farCenter = new Point(0, 0, to);
        for (int side = 0; side < sides; side++) {
            int next = (side + 1) % sides;
            sink.quad(nearCenter, near[next], near[side], nearCenter, color, alpha);
            sink.quad(farCenter, far[side], far[next], farCenter, color, alpha);
        }
    }

    private static void ring(double z, double outer, double inner, int sides, double phase,
                             int color, float alpha, Sink sink) {
        if (outer <= inner || inner < 0 || sides < 3 || (alpha = bounded(alpha)) <= 0) return;
        double halfDepth = Math.max(0.006, Math.min(0.028, (outer - inner) * 0.12));
        for (int side = 0; side < sides; side++) {
            double first = phase + side * Math.PI * 2 / sides;
            double second = phase + (side + 1) * Math.PI * 2 / sides;
            Point outerRear = radial(z - halfDepth, outer, first), outerNextRear = radial(z - halfDepth, outer, second);
            Point outerFront = radial(z + halfDepth, outer, first), outerNextFront = radial(z + halfDepth, outer, second);
            Point innerRear = radial(z - halfDepth, inner, first), innerNextRear = radial(z - halfDepth, inner, second);
            Point innerFront = radial(z + halfDepth, inner, first), innerNextFront = radial(z + halfDepth, inner, second);
            sink.quad(outerFront, outerNextFront, innerNextFront, innerFront, color, alpha);
            sink.quad(innerRear, innerNextRear, outerNextRear, outerRear, color, alpha * 0.72F);
            sink.quad(outerRear, outerNextRear, outerNextFront, outerFront, color, alpha * 0.82F);
            sink.quad(innerFront, innerNextFront, innerNextRear, innerRear, color, alpha * 0.62F);
        }
    }

    private static void blade(double z, double angle, double inner, double outer, double halfWidth,
                              int color, float alpha, Sink sink) {
        alpha = bounded(alpha);
        if (alpha <= 0) return;
        double x = Math.cos(angle), y = Math.sin(angle), tx = -y, ty = x;
        double depth = Math.max(0.012, halfWidth * 0.22);
        Point[] rear = {new Point(x * inner - tx * halfWidth, y * inner - ty * halfWidth, z - depth),
                new Point(x * outer - tx * halfWidth, y * outer - ty * halfWidth, z - depth),
                new Point(x * outer + tx * halfWidth, y * outer + ty * halfWidth, z - depth),
                new Point(x * inner + tx * halfWidth, y * inner + ty * halfWidth, z - depth)};
        Point[] front = {new Point(rear[0].x, rear[0].y, z + depth), new Point(rear[1].x, rear[1].y, z + depth),
                new Point(rear[2].x, rear[2].y, z + depth), new Point(rear[3].x, rear[3].y, z + depth)};
        sink.quad(front[0], front[1], front[2], front[3], color, alpha);
        sink.quad(rear[3], rear[2], rear[1], rear[0], color, alpha * 0.68F);
        for (int edge = 0; edge < 4; edge++) {
            int next = (edge + 1) % 4;
            sink.quad(rear[edge], rear[next], front[next], front[edge], color, alpha * 0.82F);
        }
    }

    private static Point radial(double z, double radius, double angle) {
        return new Point(Math.cos(angle) * radius, Math.sin(angle) * radius, z);
    }

    private static Sink scaled(Point center, double scale, Sink sink) {
        return (a, b, c, d, color, alpha) -> sink.quad(scale(center, a, scale), scale(center, b, scale),
                scale(center, c, scale), scale(center, d, scale), color, alpha);
    }

    private static Point scale(Point center, Point point, double scale) {
        return new Point(center.x + (point.x - center.x) * scale,
                center.y + (point.y - center.y) * scale, center.z + (point.z - center.z) * scale);
    }

    private static boolean valid(double age, double length) {
        return Double.isFinite(age) && age >= 0 && Double.isFinite(length) && length >= 0.05 && length <= 64;
    }
    private static double fractional(double value) { return value - Math.floor(value); }
    private static double smooth(double value) { double v = Math.max(0, Math.min(1, value)); return v * v * (3 - 2 * v); }
    private static float bounded(float value) { return Float.isFinite(value) ? Math.max(0, Math.min(1, value)) : 0; }
}
