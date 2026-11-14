/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.util.QizaiGeometry;

public final class QizaiLaserBarrageModel {
    public static final int CASING = 5771384;
    public static final int EDGE = 14248191;
    public static final int TARGET = 15219456;
    public static final int CORE = 16769098;
    public static final double MUZZLE = 0.52;
    public static final double ACTIVE_RADIUS = 0.45;

    private QizaiLaserBarrageModel() {
    }

    public static void emitter(QizaiGeometry.Point origin, QizaiGeometry.Point end, boolean targeted, boolean firing, double age, int seed, boolean reduced, float modelScale, QizaiGeometry.Sink sink) {
        Frame frame = QizaiLaserBarrageModel.frame(origin, end);
        modelScale = QizaiLaserBarrageModel.bounded(modelScale);
        if (frame == null || modelScale <= 0.0f || sink == null) {
            return;
        }
        double phase = age * (firing ? 0.24 : 0.13) + (double)seed * (firing ? 0.73 : 0.91);
        QizaiLaserBarrageModel.emitterBody(frame, phase, targeted, firing, reduced, 1.0f, QizaiLaserBarrageModel.scaled(frame.origin, modelScale, sink));
    }

    public static void warning(QizaiGeometry.Point origin, QizaiGeometry.Point end, boolean targeted, double age, int seed, boolean reduced, float alpha, QizaiGeometry.Sink sink) {
        Frame frame = QizaiLaserBarrageModel.frame(origin, end);
        alpha = QizaiLaserBarrageModel.bounded(alpha);
        if (frame == null || alpha <= 0.0f || sink == null) {
            return;
        }
        double phase = age * 0.13 + (double)seed * 0.91;
        int sides = reduced ? 4 : 6;
        int color = targeted ? 15219456 : 10821083;
        QizaiLaserBarrageModel.tube(frame, 0.52, frame.length, targeted ? 0.055 : 0.035, targeted ? 0.036 : 0.022, sides, phase, color, alpha * (targeted ? 0.92f : 0.58f), false, sink);
        QizaiLaserBarrageModel.tube(frame, 0.52, frame.length, targeted ? 0.016 : 0.011, targeted ? 0.01 : 0.007, 4, -phase, 16769098, alpha * (targeted ? 1.0f : 0.72f), false, sink);
        int packets = reduced ? 1 : 3;
        for (int packet = 0; packet < packets; ++packet) {
            double travel = QizaiLaserBarrageModel.fractional(age * 0.055 + (double)seed * 0.17 + (double)packet / (double)packets);
            double along = 0.52 + (frame.length - 0.52) * travel;
            QizaiLaserBarrageModel.ring(frame, along, targeted ? 0.16 : 0.1, targeted ? 0.115 : 0.072, sides, phase, color, alpha * 0.72f, sink);
        }
        if (targeted) {
            QizaiLaserBarrageModel.reticle(frame, phase, alpha, reduced, sink);
        } else {
            QizaiLaserBarrageModel.ring(frame, frame.length - 0.015, 0.13, 0.055, sides, phase, 10821083, alpha * 0.56f, sink);
        }
    }

    public static void firing(QizaiGeometry.Point origin, QizaiGeometry.Point end, boolean targeted, double age, int seed, boolean reduced, float fade, QizaiGeometry.Sink sink) {
        Frame frame = QizaiLaserBarrageModel.frame(origin, end);
        fade = QizaiLaserBarrageModel.bounded(fade);
        if (frame == null || fade <= 0.0f || sink == null) {
            return;
        }
        double phase = age * 0.24 + (double)seed * 0.73;
        int shellSides = reduced ? 6 : 8;
        QizaiLaserBarrageModel.tube(frame, 0.52, frame.length, 0.45, 0.324, shellSides, phase, targeted ? 15219456 : 10821083, fade * 0.34f, true, sink);
        QizaiLaserBarrageModel.tube(frame, 0.535, frame.length, targeted ? 0.25 : 0.22, targeted ? 0.2 : 0.17, 6, -phase * 1.2, 14248191, fade * 0.74f, true, sink);
        QizaiLaserBarrageModel.tube(frame, 0.545, frame.length, targeted ? 0.105 : 0.082, targeted ? 0.075 : 0.058, reduced ? 4 : 6, phase * 1.7, 16769098, fade, true, sink);
        int collars = reduced ? 1 : 3;
        for (int collar = 0; collar < collars; ++collar) {
            double travel = QizaiLaserBarrageModel.fractional(age * 0.19 + (double)seed * 0.11 + (double)collar / (double)collars);
            double along = 0.52 + (frame.length - 0.52) * travel;
            QizaiLaserBarrageModel.ring(frame, along, 0.5175, 0.369, shellSides, -phase, collar == 1 ? 16769098 : 14248191, fade * (collar == 1 ? 0.88f : 0.62f), sink);
        }
        QizaiLaserBarrageModel.impact(frame, phase, targeted, reduced, fade, sink);
    }

    private static void emitterBody(Frame frame, double phase, boolean targeted, boolean firing, boolean reduced, float alpha, QizaiGeometry.Sink sink) {
        int sides = reduced ? 5 : 6;
        QizaiLaserBarrageModel.tube(frame, -0.9, -0.38, 0.035, 0.31, sides, phase, 5771384, alpha * 0.82f, true, sink);
        QizaiLaserBarrageModel.tube(frame, -0.38, 0.18, 0.31, 0.24, sides, -phase, targeted ? 15219456 : 14248191, alpha * 0.78f, true, sink);
        QizaiLaserBarrageModel.tube(frame, 0.18, 0.52, 0.24, 0.075, sides, phase * 1.4, firing ? 16769098 : 14248191, alpha * 0.92f, true, sink);
        int fins = reduced ? 4 : 5;
        for (int fin = 0; fin < fins; ++fin) {
            double angle = phase * 0.35 + (double)fin * Math.PI * 2.0 / (double)fins;
            sink.quad(QizaiLaserBarrageModel.radial(frame, -0.47, 0.2, angle - 0.16), QizaiLaserBarrageModel.radial(frame, -0.11, firing ? 0.78 : 0.66, angle), QizaiLaserBarrageModel.radial(frame, 0.3, 0.27, angle + 0.13), QizaiLaserBarrageModel.radial(frame, -0.11, 0.13, angle), fin % 2 == 0 ? (targeted ? 15219456 : 14248191) : 5771384, alpha * 0.76f);
        }
        QizaiLaserBarrageModel.ring(frame, -0.29, firing ? 0.57 : 0.5, firing ? 0.45 : 0.4, reduced ? 6 : 10, phase, targeted ? 15219456 : 10821083, alpha * 0.72f, sink);
        QizaiLaserBarrageModel.ring(frame, 0.42, firing ? 0.39 : 0.32, firing ? 0.16 : 0.19, reduced ? 6 : 10, -phase, firing ? 16769098 : 14248191, alpha * 0.92f, sink);
    }

    private static void reticle(Frame frame, double phase, float alpha, boolean reduced, QizaiGeometry.Sink sink) {
        int sides = reduced ? 8 : 12;
        double pulse = 0.04 * Math.sin(phase * 2.3);
        QizaiLaserBarrageModel.ring(frame, frame.length - 0.02, 0.58 + pulse, 0.49 + pulse, sides, phase, 15219456, alpha * 0.92f, sink);
        QizaiLaserBarrageModel.ring(frame, frame.length - 0.035, 0.34 - pulse, 0.27 - pulse, sides, -phase, 16769098, alpha * 0.88f, sink);
        int ticks = reduced ? 4 : 8;
        for (int tick = 0; tick < ticks; ++tick) {
            QizaiLaserBarrageModel.blade(frame, frame.length - 0.045, (double)tick * Math.PI * 2.0 / (double)ticks + phase * 0.08, 0.63, 0.82, 0.035, tick % 2 == 0 ? 16769098 : 15219456, alpha * 0.86f, sink);
        }
    }

    private static void impact(Frame frame, double phase, boolean targeted, boolean reduced, float fade, QizaiGeometry.Sink sink) {
        int sides = reduced ? 6 : 10;
        QizaiLaserBarrageModel.ring(frame, frame.length - 0.035, targeted ? 0.78 : 0.64, targeted ? 0.51 : 0.42, sides, phase, targeted ? 15219456 : 14248191, fade * 0.78f, sink);
        QizaiLaserBarrageModel.ring(frame, frame.length - 0.055, targeted ? 0.43 : 0.36, 0.14, sides, -phase, 16769098, fade * 0.92f, sink);
        int rays = reduced ? 4 : 6;
        for (int ray = 0; ray < rays; ++ray) {
            double angle = (double)ray * Math.PI * 2.0 / (double)rays + phase * 0.16;
            QizaiLaserBarrageModel.blade(frame, frame.length - 0.07, angle, 0.16, targeted ? 1.02 : 0.82, targeted ? 0.085 : 0.06, ray % 2 == 0 ? 16769098 : 14248191, fade * 0.82f, sink);
        }
    }

    private static void blade(Frame frame, double along, double angle, double inner, double outer, double halfWidth, int color, float alpha, QizaiGeometry.Sink sink) {
        double radialX = Math.cos(angle);
        double radialY = Math.sin(angle);
        double tangentX = -radialY;
        double tangentY = radialX;
        sink.quad(QizaiLaserBarrageModel.at(frame, along, radialX * inner - tangentX * halfWidth, radialY * inner - tangentY * halfWidth), QizaiLaserBarrageModel.at(frame, along, radialX * outer - tangentX * halfWidth, radialY * outer - tangentY * halfWidth), QizaiLaserBarrageModel.at(frame, along, radialX * outer + tangentX * halfWidth, radialY * outer + tangentY * halfWidth), QizaiLaserBarrageModel.at(frame, along, radialX * inner + tangentX * halfWidth, radialY * inner + tangentY * halfWidth), color, QizaiLaserBarrageModel.bounded(alpha));
    }

    private static void tube(Frame frame, double from, double to, double fromRadius, double toRadius, int sides, double phase, int color, float alpha, boolean caps, QizaiGeometry.Sink sink) {
        int side;
        block8: {
            block7: {
                float f;
                if (to <= from || fromRadius < 0.0 || toRadius < 0.0 || sides < 3) break block7;
                alpha = QizaiLaserBarrageModel.bounded(alpha);
                if (!(f <= 0.0f)) break block8;
            }
            return;
        }
        QizaiGeometry.Point[] near = new QizaiGeometry.Point[sides];
        QizaiGeometry.Point[] far = new QizaiGeometry.Point[sides];
        for (side = 0; side < sides; ++side) {
            double angle = phase + (double)side * Math.PI * 2.0 / (double)sides;
            near[side] = QizaiLaserBarrageModel.radial(frame, from, fromRadius, angle);
            far[side] = QizaiLaserBarrageModel.radial(frame, to, toRadius, angle);
        }
        for (side = 0; side < sides; ++side) {
            int next = (side + 1) % sides;
            sink.quad(near[side], near[next], far[next], far[side], color, alpha);
        }
        if (!caps) {
            return;
        }
        QizaiGeometry.Point nearCenter = QizaiLaserBarrageModel.at(frame, from, 0.0, 0.0);
        QizaiGeometry.Point farCenter = QizaiLaserBarrageModel.at(frame, to, 0.0, 0.0);
        for (int side2 = 0; side2 < sides; ++side2) {
            int next = (side2 + 1) % sides;
            sink.quad(nearCenter, near[next], near[side2], nearCenter, color, alpha);
            sink.quad(farCenter, far[side2], far[next], farCenter, color, alpha);
        }
    }

    private static void ring(Frame frame, double along, double outer, double inner, int sides, double phase, int color, float alpha, QizaiGeometry.Sink sink) {
        block4: {
            block3: {
                float f;
                if (outer <= 0.0 || inner < 0.0 || inner >= outer || sides < 3) break block3;
                alpha = QizaiLaserBarrageModel.bounded(alpha);
                if (!(f <= 0.0f)) break block4;
            }
            return;
        }
        for (int side = 0; side < sides; ++side) {
            double first = phase + (double)side * Math.PI * 2.0 / (double)sides;
            double second = phase + (double)(side + 1) * Math.PI * 2.0 / (double)sides;
            sink.quad(QizaiLaserBarrageModel.radial(frame, along, outer, first), QizaiLaserBarrageModel.radial(frame, along, outer, second), QizaiLaserBarrageModel.radial(frame, along, inner, second), QizaiLaserBarrageModel.radial(frame, along, inner, first), color, alpha);
        }
    }

    private static QizaiGeometry.Sink scaled(QizaiGeometry.Point origin, double factor, QizaiGeometry.Sink sink) {
        return (first, second, third, fourth, color, alpha) -> sink.quad(QizaiLaserBarrageModel.scaleAround(origin, first, factor), QizaiLaserBarrageModel.scaleAround(origin, second, factor), QizaiLaserBarrageModel.scaleAround(origin, third, factor), QizaiLaserBarrageModel.scaleAround(origin, fourth, factor), color, 1.0f);
    }

    private static QizaiGeometry.Point scaleAround(QizaiGeometry.Point origin, QizaiGeometry.Point point, double factor) {
        return QizaiLaserBarrageModel.add(origin, QizaiLaserBarrageModel.scale(QizaiLaserBarrageModel.subtract(point, origin), factor));
    }

    private static Frame frame(QizaiGeometry.Point origin, QizaiGeometry.Point end) {
        if (!QizaiLaserBarrageModel.finite(origin) || !QizaiLaserBarrageModel.finite(end)) {
            return null;
        }
        QizaiGeometry.Point delta = QizaiLaserBarrageModel.subtract(end, origin);
        double length = QizaiLaserBarrageModel.length(delta);
        if (!Double.isFinite(length) || length < 0.01 || length > 64.0) {
            return null;
        }
        QizaiGeometry.Point forward = QizaiLaserBarrageModel.scale(delta, 1.0 / length);
        QizaiGeometry.Point reference = Math.abs(forward.y()) < 0.92 ? new QizaiGeometry.Point(0.0, 1.0, 0.0) : new QizaiGeometry.Point(1.0, 0.0, 0.0);
        QizaiGeometry.Point right = QizaiLaserBarrageModel.normalize(QizaiLaserBarrageModel.cross(forward, reference));
        QizaiGeometry.Point up = QizaiLaserBarrageModel.normalize(QizaiLaserBarrageModel.cross(right, forward));
        return QizaiLaserBarrageModel.finite(right) && QizaiLaserBarrageModel.finite(up) ? new Frame(origin, forward, right, up, length) : null;
    }

    private static QizaiGeometry.Point radial(Frame frame, double along, double radius, double angle) {
        return QizaiLaserBarrageModel.at(frame, along, Math.cos(angle) * radius, Math.sin(angle) * radius);
    }

    private static QizaiGeometry.Point at(Frame frame, double along, double right, double up) {
        return QizaiLaserBarrageModel.add(frame.origin, QizaiLaserBarrageModel.add(QizaiLaserBarrageModel.scale(frame.forward, along), QizaiLaserBarrageModel.add(QizaiLaserBarrageModel.scale(frame.right, right), QizaiLaserBarrageModel.scale(frame.up, up))));
    }

    private static QizaiGeometry.Point add(QizaiGeometry.Point first, QizaiGeometry.Point second) {
        return new QizaiGeometry.Point(first.x() + second.x(), first.y() + second.y(), first.z() + second.z());
    }

    private static QizaiGeometry.Point subtract(QizaiGeometry.Point first, QizaiGeometry.Point second) {
        return new QizaiGeometry.Point(first.x() - second.x(), first.y() - second.y(), first.z() - second.z());
    }

    private static QizaiGeometry.Point scale(QizaiGeometry.Point point, double scale) {
        return new QizaiGeometry.Point(point.x() * scale, point.y() * scale, point.z() * scale);
    }

    private static QizaiGeometry.Point cross(QizaiGeometry.Point first, QizaiGeometry.Point second) {
        return new QizaiGeometry.Point(first.y() * second.z() - first.z() * second.y(), first.z() * second.x() - first.x() * second.z(), first.x() * second.y() - first.y() * second.x());
    }

    private static double length(QizaiGeometry.Point point) {
        return Math.sqrt(point.x() * point.x() + point.y() * point.y() + point.z() * point.z());
    }

    private static QizaiGeometry.Point normalize(QizaiGeometry.Point point) {
        double length = QizaiLaserBarrageModel.length(point);
        return length < 1.0E-8 ? new QizaiGeometry.Point(Double.NaN, 0.0, 0.0) : QizaiLaserBarrageModel.scale(point, 1.0 / length);
    }

    private static boolean finite(QizaiGeometry.Point point) {
        return point != null && Double.isFinite(point.x() + point.y() + point.z());
    }

    private static double fractional(double value) {
        return value - Math.floor(value);
    }

    private static float bounded(float alpha) {
        return Float.isFinite(alpha) ? Math.max(0.0f, Math.min(1.0f, alpha)) : 0.0f;
    }

    private record Frame(QizaiGeometry.Point origin, QizaiGeometry.Point forward, QizaiGeometry.Point right, QizaiGeometry.Point up, double length) {
    }
}

