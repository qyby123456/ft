/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.List;

public final class LemonSacrificeGeometry {
    public static final int DURATION = 60;
    public static final int MAX_SOULS = 12;
    public static final double SOUL_RANGE = 24.0;
    public static final double RADIUS = 8.0;
    public static final double HEIGHT = 10.0;
    private static final double TAU = Math.PI * 2;
    private static final int GOLD = 16769930;
    private static final int CITRUS = 16104768;
    private static final int MINT = 8907430;

    private LemonSacrificeGeometry() {
    }

    public static boolean alive(double age) {
        return Double.isFinite(age) && age >= 0.0 && age < 60.0;
    }

    public static float opacity(double age) {
        return LemonSacrificeGeometry.alive(age) ? (float)Math.min(1.0, Math.min((age + 1.0) / 8.0, (60.0 - age) / 12.0)) : 0.0f;
    }

    public static void draw(double age, boolean reduced, List<Point> souls, Sink sink) {
        double progress;
        int step;
        if (!LemonSacrificeGeometry.alive(age)) {
            return;
        }
        float alpha = LemonSacrificeGeometry.opacity(age);
        double opening = LemonSacrificeGeometry.smooth(age / 12.0);
        double radius = 0.5 + 7.5 * opening;
        double turn = age * 0.025;
        int segments = reduced ? 32 : 64;
        LemonSacrificeGeometry.ring(sink, new Point(0.0, 0.09, 0.0), radius, 0.1, turn, Math.PI * 2, segments, 16769930, alpha * 0.7f);
        LemonSacrificeGeometry.ring(sink, new Point(0.0, 0.15, 0.0), radius * 0.86, 0.07, -turn, Math.PI * 2, segments, 8907430, alpha * 0.5f);
        for (int wedge = 0; wedge < 12; ++wedge) {
            double angle = (double)wedge * (Math.PI * 2) / 12.0 + turn * 0.2;
            Point outer = LemonSacrificeGeometry.polar(radius * 0.93, angle, 0.12);
            Point inner = LemonSacrificeGeometry.polar(radius * 0.66, angle, 0.12);
            LemonSacrificeGeometry.line(sink, inner, outer, 0.035, 16104768, alpha * 0.65f);
            LemonSacrificeGeometry.ring(sink, new Point(0.0, 0.12, 0.0), radius * 0.66, 0.035, angle + 0.05, 0.42359877559829884, reduced ? 3 : 5, 16104768, alpha * 0.5f);
            LemonSacrificeGeometry.crystal(sink, LemonSacrificeGeometry.polar(radius * 0.95, angle, 0.24), 0.1, 0.38 + Math.sin(turn + (double)wedge) * 0.1, 16769930, alpha);
        }
        if (age < 40.0) {
            double gather = LemonSacrificeGeometry.smooth((age - 8.0) / 32.0);
            int count = Math.min(12, souls.size());
            for (int index = 0; index < count; ++index) {
                Point source = souls.get(index);
                if (source == null || !source.valid()) continue;
                Point previous = source;
                int steps = reduced ? 10 : 20;
                for (step = 1; step <= steps; ++step) {
                    progress = (double)step / (double)steps;
                    Point next = LemonSacrificeGeometry.siphon(source, progress, index, turn);
                    LemonSacrificeGeometry.line(sink, previous, next, 0.025, index % 2 == 0 ? 8907430 : 16769930, alpha * (float)((1.0 - gather) * 0.45));
                    previous = next;
                }
                LemonSacrificeGeometry.crystal(sink, LemonSacrificeGeometry.siphon(source, gather, index, turn), 0.16 * (1.0 - gather * 0.45), 0.45, 16769930, alpha);
                LemonSacrificeGeometry.ring(sink, new Point(source.horizontal, source.height - 0.15, source.depth), 0.3 + (1.0 - gather) * 0.8, 0.035, turn, Math.PI * 2, reduced ? 8 : 16, 8907430, alpha * (float)(1.0 - gather));
            }
        }
        double ascent = LemonSacrificeGeometry.smooth((age - 10.0) / 22.0);
        for (int tier = 0; tier < 3; ++tier) {
            LemonSacrificeGeometry.ring(sink, new Point(0.0, 0.3 + (double)tier * 0.55 * ascent, 0.0), 1.2 + (double)tier * 0.5, 0.07, turn * (double)(tier % 2 == 0 ? 1 : -1), Math.PI * 2, segments / 2, 16769930, alpha * 0.65f);
        }
        Point heart = new Point(0.0, 1.6 + ascent * 1.4, 0.0);
        LemonSacrificeGeometry.crystal(sink, heart, 0.45 + Math.sin(age * 0.45) * 0.05, 1.3, 16769930, alpha * 0.85f);
        for (int arm = 0; arm < (reduced ? 3 : 6); ++arm) {
            double offset = (double)arm * (Math.PI * 2) / (double)(reduced ? 3 : 6);
            Point previous = LemonSacrificeGeometry.polar(2.3, offset + turn, 0.2);
            for (step = 1; step <= (reduced ? 16 : 28); ++step) {
                progress = (double)step / (reduced ? 16.0 : 28.0);
                double height = progress * (3.0 + ascent * 5.0);
                double taper = age < 40.0 ? 1.0 - progress * 0.75 : 0.25 + progress * 0.6;
                Point next = LemonSacrificeGeometry.polar(2.3 * taper, offset + turn * 3.0 + progress * (Math.PI * 2), height + 0.2);
                LemonSacrificeGeometry.line(sink, previous, next, 0.1 + (age >= 40.0 ? 0.07 : 0.0), arm % 2 == 0 ? 16769930 : 8907430, alpha * 0.6f);
                previous = next;
            }
        }
        if (age >= 16.0) {
            float crestAlpha = alpha * (float)LemonSacrificeGeometry.smooth((age - 16.0) / 10.0);
            double height = 4.6 + ascent * 0.5;
            Point[] crest = new Point[]{new Point(-1.25, height, 0.0), new Point(-1.65, height + 1.6, 0.0), new Point(-0.55, height + 1.05, 0.0), new Point(0.55, height + 1.05, 0.0), new Point(1.65, height + 1.6, 0.0), new Point(1.25, height, 0.0), new Point(0.0, height - 0.65, 0.0)};
            for (int index = 0; index < crest.length; ++index) {
                LemonSacrificeGeometry.line(sink, crest[index], crest[(index + 1) % crest.length], 0.07, 16769930, crestAlpha);
            }
            for (int side : new int[]{-1, 1}) {
                LemonSacrificeGeometry.line(sink, new Point((double)side * 0.3, height + 0.2, 0.0), new Point((double)side * 0.8, height + 0.35, 0.0), 0.06, 8907430, crestAlpha);
                LemonSacrificeGeometry.line(sink, new Point((double)side * 0.8, height - 0.1, 0.0), new Point((double)side * 1.9, height - 0.25, 0.0), 0.035, 16104768, crestAlpha);
            }
        }
        if (age >= 40.0) {
            double burst = LemonSacrificeGeometry.smooth((age - 40.0) / 20.0);
            LemonSacrificeGeometry.ring(sink, new Point(0.0, 0.25 + burst * 0.8, 0.0), 1.0 + burst * 8.5, 0.14 * (1.0 - burst * 0.7), 0.0, Math.PI * 2, segments, 16769930, alpha * 0.8f);
            LemonSacrificeGeometry.ring(sink, new Point(0.0, 2.0 + burst * 6.0, 0.0), 0.8 + burst * 2.8, 0.07, 0.0, Math.PI * 2, segments / 2, 8907430, alpha * 0.65f);
        }
        int motes = reduced ? 8 : 20;
        for (int index = 0; index < motes; ++index) {
            double rise = (age / 36.0 + (double)index / (double)motes) % 1.0;
            LemonSacrificeGeometry.crystal(sink, LemonSacrificeGeometry.polar((1.0 - rise * 0.7) * radius * 0.7, (double)index * 2.4 + turn, 0.2 + rise * 7.8 * ascent), 0.06, 0.2, index % 2 == 0 ? 16104768 : 8907430, alpha * 0.6f);
        }
    }

    private static Point siphon(Point source, double progress, int index, double turn) {
        Point center = source.scale(1.0 - progress).add(new Point(0.0, 3.0 * progress, 0.0));
        return center.add(LemonSacrificeGeometry.polar(Math.sin(progress * Math.PI) * 0.55, progress * (Math.PI * 2) + (double)index + turn, Math.sin(progress * Math.PI) * 1.4));
    }

    private static double smooth(double value) {
        double bounded = Math.max(0.0, Math.min(1.0, value));
        return bounded * bounded * (3.0 - 2.0 * bounded);
    }

    private static Point polar(double radius, double angle, double height) {
        return new Point(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
    }

    private static void ring(Sink sink, Point center, double radius, double width, double turn, double arc, int segments, int color, float alpha) {
        Point previous = center.add(LemonSacrificeGeometry.polar(radius, turn, 0.0));
        for (int index = 1; index <= segments; ++index) {
            Point next = center.add(LemonSacrificeGeometry.polar(radius, turn + (double)index * arc / (double)segments, 0.0));
            LemonSacrificeGeometry.line(sink, previous, next, width, color, alpha);
            previous = next;
        }
    }

    private static void crystal(Sink sink, Point center, double radius, double height, int color, float alpha) {
        for (int face = 0; face < 4; ++face) {
            Point left = center.add(LemonSacrificeGeometry.polar(radius, (double)face * Math.PI / 2.0, 0.0));
            Point right = center.add(LemonSacrificeGeometry.polar(radius, (double)(face + 1) * Math.PI / 2.0, 0.0));
            Point top = center.add(new Point(0.0, height * 0.7, 0.0));
            Point bottom = center.add(new Point(0.0, -height * 0.3, 0.0));
            LemonSacrificeGeometry.quad(sink, left, top, right, right, color, alpha);
            LemonSacrificeGeometry.quad(sink, right, bottom, left, left, color, alpha * 0.65f);
        }
    }

    private static void line(Sink sink, Point first, Point second, double width, int color, float alpha) {
        Point direction = second.add(first.scale(-1.0));
        double flat = Math.hypot(direction.horizontal, direction.depth);
        Point side = flat < 0.001 ? new Point(width, 0.0, 0.0) : new Point(direction.depth / flat * width, 0.0, -direction.horizontal / flat * width);
        LemonSacrificeGeometry.quad(sink, first.add(side), second.add(side), second.add(side.scale(-1.0)), first.add(side.scale(-1.0)), color, alpha);
        Point up = flat < 0.001 ? new Point(0.0, 0.0, width) : new Point(0.0, width, 0.0);
        LemonSacrificeGeometry.quad(sink, first.add(up), second.add(up), second.add(up.scale(-1.0)), first.add(up.scale(-1.0)), color, alpha);
    }

    private static void quad(Sink sink, Point first, Point second, Point third, Point fourth, int color, float alpha) {
        for (Point point : new Point[]{first, second, third, fourth}) {
            sink.vertex(point.horizontal, point.height, point.depth, color, alpha);
        }
    }

    public record Point(double horizontal, double height, double depth) {
        public Point add(Point other) {
            return new Point(this.horizontal + other.horizontal, this.height + other.height, this.depth + other.depth);
        }

        public Point scale(double amount) {
            return new Point(this.horizontal * amount, this.height * amount, this.depth * amount);
        }

        public boolean valid() {
            return Double.isFinite(this.horizontal) && Double.isFinite(this.height) && Double.isFinite(this.depth) && Math.abs(this.horizontal) <= 24.0 && Math.abs(this.depth) <= 24.0 && Math.abs(this.height) <= 24.0;
        }
    }

    @FunctionalInterface
    public static interface Sink {
        public void vertex(double var1, double var3, double var5, int var7, float var8);
    }
}

