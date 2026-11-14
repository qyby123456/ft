package net.farmingtales.ftboss.util;

import java.util.List;

public final class LemonSacrificeGeometry {
    public static final int DURATION = 60;
    public static final int MAX_SOULS = 12;
    public static final double SOUL_RANGE = 24;
    public static final double RADIUS = 8;
    public static final double HEIGHT = 10;
    private static final double TAU = Math.PI * 2;
    private static final int GOLD = 0xFFE38A;
    private static final int CITRUS = 0xF5BD40;
    private static final int MINT = 0x87EAA6;

    public record Point(double horizontal, double height, double depth) {
        public Point add(Point other) { return new Point(horizontal + other.horizontal, height + other.height, depth + other.depth); }
        public Point scale(double amount) { return new Point(horizontal * amount, height * amount, depth * amount); }
        public boolean valid() { return Double.isFinite(horizontal) && Double.isFinite(height) && Double.isFinite(depth)
                && Math.abs(horizontal) <= SOUL_RANGE && Math.abs(depth) <= SOUL_RANGE && Math.abs(height) <= SOUL_RANGE; }
    }

    @FunctionalInterface
    public interface Sink { void vertex(double horizontal, double height, double depth, int color, float alpha); }

    private LemonSacrificeGeometry() {}

    public static boolean alive(double age) { return Double.isFinite(age) && age >= 0 && age < DURATION; }

    public static float opacity(double age) {
        return alive(age) ? (float) Math.min(1, Math.min((age + 1) / 8, (DURATION - age) / 12)) : 0;
    }

    public static void draw(double age, boolean reduced, List<Point> souls, Sink sink) {
        if (!alive(age)) return;
        float alpha = opacity(age);
        double opening = smooth(age / 12);
        double radius = 0.5 + (RADIUS - 0.5) * opening;
        double turn = age * 0.025;
        int segments = reduced ? 32 : 64;
        ring(sink, new Point(0, 0.09, 0), radius, 0.10, turn, TAU, segments, GOLD, alpha * 0.7F);
        ring(sink, new Point(0, 0.15, 0), radius * 0.86, 0.07, -turn, TAU, segments, MINT, alpha * 0.5F);
        for (int wedge = 0; wedge < 12; wedge++) {
            double angle = wedge * TAU / 12 + turn * 0.2;
            Point outer = polar(radius * 0.93, angle, 0.12);
            Point inner = polar(radius * 0.66, angle, 0.12);
            line(sink, inner, outer, 0.035, CITRUS, alpha * 0.65F);
            ring(sink, new Point(0, 0.12, 0), radius * 0.66, 0.035, angle + 0.05, TAU / 12 - 0.1,
                    reduced ? 3 : 5, CITRUS, alpha * 0.5F);
            crystal(sink, polar(radius * 0.95, angle, 0.24), 0.1, 0.38 + Math.sin(turn + wedge) * 0.1, GOLD, alpha);
        }
        if (age < 40) {
            double gather = smooth((age - 8) / 32);
            int count = Math.min(MAX_SOULS, souls.size());
            for (int index = 0; index < count; index++) {
                Point source = souls.get(index);
                if (source == null || !source.valid()) continue;
                Point previous = source;
                int steps = reduced ? 10 : 20;
                for (int step = 1; step <= steps; step++) {
                    double progress = step / (double) steps;
                    Point next = siphon(source, progress, index, turn);
                    line(sink, previous, next, 0.025, index % 2 == 0 ? MINT : GOLD,
                            alpha * (float) ((1 - gather) * 0.45));
                    previous = next;
                }
                crystal(sink, siphon(source, gather, index, turn), 0.16 * (1 - gather * 0.45), 0.45, GOLD, alpha);
                ring(sink, new Point(source.horizontal, source.height - 0.15, source.depth),
                        0.3 + (1 - gather) * 0.8, 0.035, turn, TAU, reduced ? 8 : 16, MINT, alpha * (float) (1 - gather));
            }
        }
        double ascent = smooth((age - 10) / 22);
        for (int tier = 0; tier < 3; tier++) {
            ring(sink, new Point(0, 0.3 + tier * 0.55 * ascent, 0), 1.2 + tier * 0.5, 0.07,
                    turn * (tier % 2 == 0 ? 1 : -1), TAU, segments / 2, GOLD, alpha * 0.65F);
        }
        Point heart = new Point(0, 1.6 + ascent * 1.4, 0);
        crystal(sink, heart, 0.45 + Math.sin(age * 0.45) * 0.05, 1.3, GOLD, alpha * 0.85F);
        for (int arm = 0; arm < (reduced ? 3 : 6); arm++) {
            double offset = arm * TAU / (reduced ? 3 : 6);
            Point previous = polar(2.3, offset + turn, 0.2);
            for (int step = 1; step <= (reduced ? 16 : 28); step++) {
                double progress = step / (reduced ? 16.0 : 28.0);
                double height = progress * (3 + ascent * 5);
                double taper = age < 40 ? 1 - progress * 0.75 : 0.25 + progress * 0.6;
                Point next = polar(2.3 * taper, offset + turn * 3 + progress * TAU, height + 0.2);
                line(sink, previous, next, 0.10 + (age >= 40 ? 0.07 : 0), arm % 2 == 0 ? GOLD : MINT, alpha * 0.6F);
                previous = next;
            }
        }
        if (age >= 16) {
            float crestAlpha = alpha * (float) smooth((age - 16) / 10);
            double height = 4.6 + ascent * 0.5;
            Point[] crest = {new Point(-1.25, height, 0), new Point(-1.65, height + 1.6, 0),
                    new Point(-0.55, height + 1.05, 0), new Point(0.55, height + 1.05, 0),
                    new Point(1.65, height + 1.6, 0), new Point(1.25, height, 0), new Point(0, height - 0.65, 0)};
            for (int index = 0; index < crest.length; index++) line(sink, crest[index], crest[(index + 1) % crest.length], 0.07, GOLD, crestAlpha);
            for (int side : new int[]{-1, 1}) {
                line(sink, new Point(side * 0.3, height + 0.2, 0), new Point(side * 0.8, height + 0.35, 0), 0.06, MINT, crestAlpha);
                line(sink, new Point(side * 0.8, height - 0.1, 0), new Point(side * 1.9, height - 0.25, 0), 0.035, CITRUS, crestAlpha);
            }
        }
        if (age >= 40) {
            double burst = smooth((age - 40) / 20);
            ring(sink, new Point(0, 0.25 + burst * 0.8, 0), 1 + burst * 8.5, 0.14 * (1 - burst * 0.7),
                    0, TAU, segments, GOLD, alpha * 0.8F);
            ring(sink, new Point(0, 2 + burst * 6, 0), 0.8 + burst * 2.8, 0.07,
                    0, TAU, segments / 2, MINT, alpha * 0.65F);
        }
        int motes = reduced ? 8 : 20;
        for (int index = 0; index < motes; index++) {
            double rise = (age / 36 + index / (double) motes) % 1;
            crystal(sink, polar((1 - rise * 0.7) * radius * 0.7, index * 2.4 + turn, 0.2 + rise * 7.8 * ascent),
                    0.06, 0.2, index % 2 == 0 ? CITRUS : MINT, alpha * 0.6F);
        }
    }

    private static Point siphon(Point source, double progress, int index, double turn) {
        Point center = source.scale(1 - progress).add(new Point(0, 3 * progress, 0));
        return center.add(polar(Math.sin(progress * Math.PI) * 0.55, progress * TAU + index + turn,
                Math.sin(progress * Math.PI) * 1.4));
    }

    private static double smooth(double value) { double bounded = Math.max(0, Math.min(1, value)); return bounded * bounded * (3 - 2 * bounded); }
    private static Point polar(double radius, double angle, double height) { return new Point(Math.cos(angle) * radius, height, Math.sin(angle) * radius); }

    private static void ring(Sink sink, Point center, double radius, double width, double turn, double arc,
                             int segments, int color, float alpha) {
        Point previous = center.add(polar(radius, turn, 0));
        for (int index = 1; index <= segments; index++) {
            Point next = center.add(polar(radius, turn + index * arc / segments, 0));
            line(sink, previous, next, width, color, alpha);
            previous = next;
        }
    }

    private static void crystal(Sink sink, Point center, double radius, double height, int color, float alpha) {
        for (int face = 0; face < 4; face++) {
            Point left = center.add(polar(radius, face * Math.PI / 2, 0));
            Point right = center.add(polar(radius, (face + 1) * Math.PI / 2, 0));
            Point top = center.add(new Point(0, height * 0.7, 0));
            Point bottom = center.add(new Point(0, -height * 0.3, 0));
            quad(sink, left, top, right, right, color, alpha);
            quad(sink, right, bottom, left, left, color, alpha * 0.65F);
        }
    }

    private static void line(Sink sink, Point first, Point second, double width, int color, float alpha) {
        Point direction = second.add(first.scale(-1));
        double flat = Math.hypot(direction.horizontal, direction.depth);
        Point side = flat < 0.001 ? new Point(width, 0, 0) : new Point(direction.depth / flat * width, 0, -direction.horizontal / flat * width);
        quad(sink, first.add(side), second.add(side), second.add(side.scale(-1)), first.add(side.scale(-1)), color, alpha);
        Point up = flat < 0.001 ? new Point(0, 0, width) : new Point(0, width, 0);
        quad(sink, first.add(up), second.add(up), second.add(up.scale(-1)), first.add(up.scale(-1)), color, alpha);
    }

    private static void quad(Sink sink, Point first, Point second, Point third, Point fourth, int color, float alpha) {
        for (Point point : new Point[]{first, second, third, fourth}) sink.vertex(point.horizontal, point.height, point.depth, color, alpha);
    }
}
