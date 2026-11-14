package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.util.QizaiGeometry;
import net.farmingtales.ftboss.util.QizaiGeometry.Point;
import net.farmingtales.ftboss.util.QizaiGeometry.Sink;

/** Procedural 3-D model used only by Qizai's arena laser barrage. */
public final class QizaiLaserBarrageModel {
    public static final int CASING = 0x581078;
    public static final int EDGE = 0xD968FF;
    public static final int TARGET = QizaiGeometry.WARNING;
    public static final int CORE = QizaiGeometry.WHITE;
    public static final double MUZZLE = 0.52;
    public static final double ACTIVE_RADIUS = 0.45;

    private record Frame(Point origin, Point forward, Point right, Point up, double length) {}

    private QizaiLaserBarrageModel() {}

    /** Opaque, depth-writing star-lens projector body for Qizai's dedicated barrage render pass. */
    public static void emitter(Point origin, Point end, boolean targeted, boolean firing, double age, int seed,
                               boolean reduced, float modelScale, Sink sink) {
        Frame frame = frame(origin, end);
        modelScale = bounded(modelScale);
        if (frame == null || modelScale <= 0 || sink == null) return;
        double phase = age * (firing ? 0.24 : 0.13) + seed * (firing ? 0.73 : 0.91);
        emitterBody(frame, phase, targeted, firing, reduced, 1.0F, scaled(frame.origin, modelScale, sink));
    }

    public static void warning(Point origin, Point end, boolean targeted, double age, int seed,
                               boolean reduced, float alpha, Sink sink) {
        Frame frame = frame(origin, end);
        alpha = bounded(alpha);
        if (frame == null || alpha <= 0 || sink == null) return;
        double phase = age * 0.13 + seed * 0.91;
        int sides = reduced ? 4 : 6;
        int color = targeted ? TARGET : QizaiGeometry.ROSE;
        tube(frame, MUZZLE, frame.length, targeted ? 0.055 : 0.035,
                targeted ? 0.036 : 0.022, sides, phase, color, alpha * (targeted ? 0.92F : 0.58F), false, sink);
        tube(frame, MUZZLE, frame.length, targeted ? 0.016 : 0.011,
                targeted ? 0.010 : 0.007, 4, -phase, CORE, alpha * (targeted ? 1.0F : 0.72F), false, sink);
        int packets = reduced ? 1 : 3;
        for (int packet = 0; packet < packets; packet++) {
            double travel = fractional(age * 0.055 + seed * 0.17 + packet / (double) packets);
            double along = MUZZLE + (frame.length - MUZZLE) * travel;
            ring(frame, along, targeted ? 0.16 : 0.10, targeted ? 0.115 : 0.072,
                    sides, phase, color, alpha * 0.72F, sink);
        }
        if (targeted) reticle(frame, phase, alpha, reduced, sink);
        else ring(frame, frame.length - 0.015, 0.13, 0.055, sides, phase, QizaiGeometry.ROSE, alpha * 0.56F, sink);
    }

    public static void firing(Point origin, Point end, boolean targeted, double age, int seed,
                              boolean reduced, float fade, Sink sink) {
        Frame frame = frame(origin, end);
        fade = bounded(fade);
        if (frame == null || fade <= 0 || sink == null) return;
        double phase = age * 0.24 + seed * 0.73;
        int shellSides = reduced ? 6 : 8;
        tube(frame, MUZZLE, frame.length, ACTIVE_RADIUS, ACTIVE_RADIUS * 0.72,
                shellSides, phase, targeted ? TARGET : QizaiGeometry.ROSE, fade * 0.34F, true, sink);
        tube(frame, MUZZLE + 0.015, frame.length, targeted ? 0.25 : 0.22, targeted ? 0.20 : 0.17,
                6, -phase * 1.2, EDGE, fade * 0.74F, true, sink);
        tube(frame, MUZZLE + 0.025, frame.length, targeted ? 0.105 : 0.082, targeted ? 0.075 : 0.058,
                reduced ? 4 : 6, phase * 1.7, CORE, fade, true, sink);
        int collars = reduced ? 1 : 3;
        for (int collar = 0; collar < collars; collar++) {
            double travel = fractional(age * 0.19 + seed * 0.11 + collar / (double) collars);
            double along = MUZZLE + (frame.length - MUZZLE) * travel;
            ring(frame, along, ACTIVE_RADIUS * 1.15, ACTIVE_RADIUS * 0.82,
                    shellSides, -phase, collar == 1 ? CORE : EDGE, fade * (collar == 1 ? 0.88F : 0.62F), sink);
        }
        impact(frame, phase, targeted, reduced, fade, sink);
    }

    private static void emitterBody(Frame frame, double phase, boolean targeted, boolean firing,
                                    boolean reduced, float alpha, Sink sink) {
        int sides = reduced ? 5 : 6;
        tube(frame, -0.90, -0.38, 0.035, 0.31, sides, phase, CASING, alpha * 0.82F, true, sink);
        tube(frame, -0.38, 0.18, 0.31, 0.24, sides, -phase, targeted ? TARGET : EDGE, alpha * 0.78F, true, sink);
        tube(frame, 0.18, MUZZLE, 0.24, 0.075, sides, phase * 1.4, firing ? CORE : EDGE, alpha * 0.92F, true, sink);
        int fins = reduced ? 4 : 5;
        for (int fin = 0; fin < fins; fin++) {
            double angle = phase * 0.35 + fin * Math.PI * 2 / fins;
            sink.quad(radial(frame, -0.47, 0.20, angle - 0.16),
                    radial(frame, -0.11, firing ? 0.78 : 0.66, angle),
                    radial(frame, 0.30, 0.27, angle + 0.13),
                    radial(frame, -0.11, 0.13, angle),
                    fin % 2 == 0 ? (targeted ? TARGET : EDGE) : CASING, alpha * 0.76F);
        }
        ring(frame, -0.29, firing ? 0.57 : 0.50, firing ? 0.45 : 0.40,
                reduced ? 6 : 10, phase, targeted ? TARGET : QizaiGeometry.ROSE, alpha * 0.72F, sink);
        ring(frame, 0.42, firing ? 0.39 : 0.32, firing ? 0.16 : 0.19,
                reduced ? 6 : 10, -phase, firing ? CORE : EDGE, alpha * 0.92F, sink);
    }

    private static void reticle(Frame frame, double phase, float alpha, boolean reduced, Sink sink) {
        int sides = reduced ? 8 : 12;
        double pulse = 0.04 * Math.sin(phase * 2.3);
        ring(frame, frame.length - 0.02, 0.58 + pulse, 0.49 + pulse, sides, phase, TARGET, alpha * 0.92F, sink);
        ring(frame, frame.length - 0.035, 0.34 - pulse, 0.27 - pulse, sides, -phase, CORE, alpha * 0.88F, sink);
        int ticks = reduced ? 4 : 8;
        for (int tick = 0; tick < ticks; tick++) {
            blade(frame, frame.length - 0.045, tick * Math.PI * 2 / ticks + phase * 0.08,
                    0.63, 0.82, 0.035, tick % 2 == 0 ? CORE : TARGET, alpha * 0.86F, sink);
        }
    }

    private static void impact(Frame frame, double phase, boolean targeted, boolean reduced, float fade, Sink sink) {
        int sides = reduced ? 6 : 10;
        ring(frame, frame.length - 0.035, targeted ? 0.78 : 0.64, targeted ? 0.51 : 0.42,
                sides, phase, targeted ? TARGET : EDGE, fade * 0.78F, sink);
        ring(frame, frame.length - 0.055, targeted ? 0.43 : 0.36, 0.14,
                sides, -phase, CORE, fade * 0.92F, sink);
        int rays = reduced ? 4 : 6;
        for (int ray = 0; ray < rays; ray++) {
            double angle = ray * Math.PI * 2 / rays + phase * 0.16;
            blade(frame, frame.length - 0.07, angle, 0.16, targeted ? 1.02 : 0.82,
                    targeted ? 0.085 : 0.06, ray % 2 == 0 ? CORE : EDGE, fade * 0.82F, sink);
        }
    }

    private static void blade(Frame frame, double along, double angle, double inner, double outer,
                              double halfWidth, int color, float alpha, Sink sink) {
        double radialX = Math.cos(angle), radialY = Math.sin(angle);
        double tangentX = -radialY, tangentY = radialX;
        sink.quad(at(frame, along, radialX * inner - tangentX * halfWidth, radialY * inner - tangentY * halfWidth),
                at(frame, along, radialX * outer - tangentX * halfWidth, radialY * outer - tangentY * halfWidth),
                at(frame, along, radialX * outer + tangentX * halfWidth, radialY * outer + tangentY * halfWidth),
                at(frame, along, radialX * inner + tangentX * halfWidth, radialY * inner + tangentY * halfWidth),
                color, bounded(alpha));
    }

    private static void tube(Frame frame, double from, double to, double fromRadius, double toRadius,
                             int sides, double phase, int color, float alpha, boolean caps, Sink sink) {
        if (to <= from || fromRadius < 0 || toRadius < 0 || sides < 3 || (alpha = bounded(alpha)) <= 0) return;
        Point[] near = new Point[sides], far = new Point[sides];
        for (int side = 0; side < sides; side++) {
            double angle = phase + side * Math.PI * 2 / sides;
            near[side] = radial(frame, from, fromRadius, angle);
            far[side] = radial(frame, to, toRadius, angle);
        }
        for (int side = 0; side < sides; side++) {
            int next = (side + 1) % sides;
            sink.quad(near[side], near[next], far[next], far[side], color, alpha);
        }
        if (!caps) return;
        Point nearCenter = at(frame, from, 0, 0), farCenter = at(frame, to, 0, 0);
        for (int side = 0; side < sides; side++) {
            int next = (side + 1) % sides;
            sink.quad(nearCenter, near[next], near[side], nearCenter, color, alpha);
            sink.quad(farCenter, far[side], far[next], farCenter, color, alpha);
        }
    }

    private static void ring(Frame frame, double along, double outer, double inner, int sides,
                             double phase, int color, float alpha, Sink sink) {
        if (outer <= 0 || inner < 0 || inner >= outer || sides < 3 || (alpha = bounded(alpha)) <= 0) return;
        for (int side = 0; side < sides; side++) {
            double first = phase + side * Math.PI * 2 / sides;
            double second = phase + (side + 1) * Math.PI * 2 / sides;
            sink.quad(radial(frame, along, outer, first), radial(frame, along, outer, second),
                    radial(frame, along, inner, second), radial(frame, along, inner, first), color, alpha);
        }
    }

    private static Sink scaled(Point origin, double factor, Sink sink) {
        return (first, second, third, fourth, color, alpha) -> sink.quad(
                scaleAround(origin, first, factor), scaleAround(origin, second, factor),
                scaleAround(origin, third, factor), scaleAround(origin, fourth, factor), color, 1.0F);
    }

    private static Point scaleAround(Point origin, Point point, double factor) {
        return add(origin, scale(subtract(point, origin), factor));
    }

    private static Frame frame(Point origin, Point end) {
        if (!finite(origin) || !finite(end)) return null;
        Point delta = subtract(end, origin);
        double length = length(delta);
        if (!Double.isFinite(length) || length < 0.01 || length > 96) return null;
        Point forward = scale(delta, 1 / length);
        Point reference = Math.abs(forward.y()) < 0.92 ? new Point(0, 1, 0) : new Point(1, 0, 0);
        Point right = normalize(cross(forward, reference));
        Point up = normalize(cross(right, forward));
        return finite(right) && finite(up) ? new Frame(origin, forward, right, up, length) : null;
    }

    private static Point radial(Frame frame, double along, double radius, double angle) {
        return at(frame, along, Math.cos(angle) * radius, Math.sin(angle) * radius);
    }
    private static Point at(Frame frame, double along, double right, double up) {
        return add(frame.origin, add(scale(frame.forward, along), add(scale(frame.right, right), scale(frame.up, up))));
    }
    private static Point add(Point first, Point second) { return new Point(first.x() + second.x(), first.y() + second.y(), first.z() + second.z()); }
    private static Point subtract(Point first, Point second) { return new Point(first.x() - second.x(), first.y() - second.y(), first.z() - second.z()); }
    private static Point scale(Point point, double scale) { return new Point(point.x() * scale, point.y() * scale, point.z() * scale); }
    private static Point cross(Point first, Point second) { return new Point(first.y() * second.z() - first.z() * second.y(), first.z() * second.x() - first.x() * second.z(), first.x() * second.y() - first.y() * second.x()); }
    private static double length(Point point) { return Math.sqrt(point.x() * point.x() + point.y() * point.y() + point.z() * point.z()); }
    private static Point normalize(Point point) { double length = length(point); return length < 1.0E-8 ? new Point(Double.NaN, 0, 0) : scale(point, 1 / length); }
    private static boolean finite(Point point) { return point != null && Double.isFinite(point.x() + point.y() + point.z()); }
    private static double fractional(double value) { return value - Math.floor(value); }
    private static float bounded(float alpha) { return Float.isFinite(alpha) ? Math.max(0, Math.min(1, alpha)) : 0; }
}
