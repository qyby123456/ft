package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.ManorCeremonyModel.Sink;

public final class ManorRitualGeometry {
    public static final int FULL_BUDGET = 6500;
    public static final int REDUCED_BUDGET = 3000;
    public static final double CLEAR_RADIUS = 1.65;
    public static final double MAX_RADIUS = 10.0;
    public static final double MAX_HEIGHT = 12.0;
    private static final int GOLD = 0xE7BD76;
    private static final int PALE = 0xFFF0CE;
    private static final int TEAL = 0x69CDBF;
    private static final int VIOLET = 0xAA98EB;
    private static final double TAU = Math.PI * 2;

    private ManorRitualGeometry() {}

    public static void emit(double millis, boolean reduced, Sink sink) {
        if (!Double.isFinite(millis) || millis < 0 || millis >= ManorIntroTimeline.DURATION_MILLIS) return;
        double seconds = millis / 1000.0;
        double seal = ramp(millis, 0, 8000);
        double domain = ramp(millis, 8000, 24000);
        double ascent = ramp(millis, 24000, 36000);
        double focus = ramp(millis, 36000, 43000);
        double release = ramp(millis, 43000, 46000);
        float alpha = (float) (ramp(millis, 0, 2000) * (1 - ramp(millis, 45000, 46000)));
        if (alpha < 0.001F) return;
        int segments = reduced ? 64 : 128;
        double radius = 4.0 + seal * 3.6;
        double spin = seconds * 0.035;
        ring(radius, 0.024, 0.10, 0, TAU, segments, GOLD, alpha * 0.80F, sink);
        ring(radius - 0.16, 0.012, 0.14, 0, TAU, segments, PALE, alpha * 0.55F, sink);
        ring(radius - 0.55, 0.035, 0.18, -spin, TAU * seal, segments, TEAL, alpha * 0.52F, sink);
        ring(2.1 + seal * 0.25, 0.018, 0.22, spin, TAU, segments, GOLD, alpha * 0.65F, sink);
        int marks = reduced ? 48 : 96;
        for (int mark = 0; mark < marks; mark++) {
            double angle = mark * TAU / marks + spin;
            double inner = radius - (mark % 4 == 0 ? 0.42 : 0.28);
            radialLine(inner, radius - 0.20, 0.12, angle, 0.018, GOLD, alpha * 0.67F, sink);
        }
        for (int station = 0; station < 12; station++) {
            double angle = station * TAU / 12;
            float shown = alpha * (float) ramp(millis, station * 420, station * 420 + 2200);
            double orbit = radius - 1.2;
            glyph(orbit, angle, 0.25, station, shown, reduced, sink);
            ring(orbit, 0.017, 0.20, angle + 0.085 + spin * 0.20,
                    TAU / 12 - 0.17, reduced ? 5 : 10, GOLD, shown * 0.44F, sink);
            if (!reduced) {
                radialLine(2.9, radius - 1.65, 0.16, angle, 0.012, TEAL, shown * 0.28F, sink);
                radialLine(2.9, radius - 1.65, 0.16, angle + 0.025, 0.008, GOLD, shown * 0.28F, sink);
            }
        }
        if (domain > 0) {
            double height = 3.0 + domain * 4.4 + ascent * 2.2;
            float envelope = alpha * (float) domain;
            int ribs = reduced ? 8 : 16;
            for (int rib = 0; rib < ribs; rib++) {
                double angle = rib * TAU / ribs + spin * 0.30;
                meridian(radius + 0.30, height, angle, reduced ? 16 : 32,
                        rib % 2 == 0 ? TEAL : VIOLET, envelope * 0.29F, sink);
            }
            ring(radius + 0.34, 0.026, 1.0 + domain, spin, TAU, segments,
                    TEAL, envelope * 0.24F, sink);
            int panels = reduced ? 12 : 24;
            for (int panel = 0; panel < panels; panel++) {
                double angle = panel * TAU / panels + spin;
                double bottom = 0.65 + 0.45 * Math.sin(seconds * 0.45 + panel);
                lightPillar(radius + 0.30, angle, bottom, height * 0.72, 0.09,
                        GOLD, envelope * 0.38F, sink);
            }
            if (!reduced) {
                helix(radius - 0.65, height * 0.85, seconds * 0.12, 96, TEAL, envelope * 0.28F, sink);
                helix(radius - 0.86, height * 0.85, -seconds * 0.10 + Math.PI,
                        96, GOLD, envelope * 0.23F, sink);
            }
        }
        if (ascent > 0) {
            double high = 5.4 + ascent * 2.4;
            float shown = alpha * (float) ascent;
            double crownRadius = 3.1 - focus * 0.8;
            ring(crownRadius, 0.035, high, spin * 2, TAU, segments, GOLD, shown * 0.72F, sink);
            ring(crownRadius + 0.2, 0.018, high + 0.16, -spin * 3, TAU,
                    segments, TEAL, shown * 0.50F, sink);
            int facets = reduced ? 12 : 24;
            for (int facet = 0; facet < facets; facet++) {
                double angle = facet * TAU / facets - spin;
                double extent = facet % 2 == 0 ? 0.42 : 0.20;
                radialLine(crownRadius - extent, crownRadius + 0.12, high + 0.08,
                        angle, 0.026, PALE, shown * 0.72F, sink);
                diamond(crownRadius + 0.42, high + 0.12, angle, 0.09, 0.21,
                        facet % 2 == 0 ? GOLD : VIOLET, shown * 0.68F, sink);
            }
            for (int tier = 0; tier < (reduced ? 2 : 4); tier++) {
                double crestHeight = high + 0.5 + tier * 0.4;
                ring(crownRadius * (1 - tier * 0.16), 0.012, crestHeight,
                        spin + tier, TAU * 0.72, segments / 2, GOLD, shown * 0.36F, sink);
            }
        }
        if (focus > 0) {
            int rays = reduced ? 12 : 24;
            float shown = alpha * (float) focus;
            for (int ray = 0; ray < rays; ray++) {
                double angle = ray * TAU / rays + seconds * 0.04;
                lightPillar(4.5 + 0.45 * Math.sin(ray * 1.7), angle, 0.7,
                        7.0 + 1.1 * Math.sin(ray * 0.8), 0.025, PALE, shown * 0.20F, sink);
            }
        }
        if (release > 0) {
            for (int echo = 0; echo < 3; echo++) {
                double wave = Math.max(0, Math.min(1, release * 1.5 - echo * 0.2));
                if (wave <= 0 || wave >= 1) continue;
                ring(2.2 + wave * 7.4, 0.04 + wave * 0.05, 0.30 + echo * 0.07,
                        0, TAU, segments, echo % 2 == 0 ? PALE : TEAL,
                        alpha * (float) ((1 - wave) * 0.65), sink);
            }
        }
    }

    private static double ramp(double value, double start, double end) {
        double unit = Math.max(0, Math.min(1, (value - start) / (end - start)));
        return unit * unit * (3 - 2 * unit);
    }

    private static void ring(double radius, double width, double height, double start, double sweep,
                             int segments, int color, float alpha, Sink sink) {
        if (alpha < 0.001F || sweep <= 0) return;
        ringLayer(radius, width * 4.5, height, start, sweep, segments, color, alpha * 0.13F, sink);
        ringLayer(radius, width, height + 0.002, start, sweep, segments, color, alpha, sink);
    }

    private static void ringLayer(double radius, double width, double height, double start, double sweep,
                                  int segments, int color, float alpha, Sink sink) {
        for (int segment = 0; segment < segments; segment++) {
            double first = start + sweep * segment / segments;
            double second = start + sweep * (segment + 1) / segments;
            point(radius - width, height, first, color, alpha, sink);
            point(radius + width, height, first, color, alpha, sink);
            point(radius + width, height, second, color, alpha, sink);
            point(radius - width, height, second, color, alpha, sink);
        }
    }

    private static void radialLine(double inner, double outer, double height, double angle,
                                   double width, int color, float alpha, Sink sink) {
        double offsetX = -Math.sin(angle) * width, offsetZ = Math.cos(angle) * width;
        double innerX = Math.cos(angle) * inner, innerZ = Math.sin(angle) * inner;
        double outerX = Math.cos(angle) * outer, outerZ = Math.sin(angle) * outer;
        sink.vertex(innerX - offsetX, height, innerZ - offsetZ, color, alpha);
        sink.vertex(outerX - offsetX, height, outerZ - offsetZ, color, alpha);
        sink.vertex(outerX + offsetX, height, outerZ + offsetZ, color, alpha);
        sink.vertex(innerX + offsetX, height, innerZ + offsetZ, color, alpha);
    }

    private static void glyph(double radius, double angle, double height, int variant,
                              float alpha, boolean reduced, Sink sink) {
        double centerX = Math.cos(angle) * radius, centerZ = Math.sin(angle) * radius;
        Sink local = (posX, posY, posZ, color, opacity) -> sink.vertex(
                centerX + posX * Math.cos(angle) - posZ * Math.sin(angle), posY,
                centerZ + posX * Math.sin(angle) + posZ * Math.cos(angle), color, opacity);
        ring(0.34, 0.014, height, angle, TAU, reduced ? 12 : 24, GOLD, alpha * 0.74F, local);
        int strokes = 3 + variant % 4;
        for (int stroke = 0; stroke < strokes; stroke++) {
            radialLine(0.09, 0.26, height + 0.012, stroke * TAU / strokes + variant * 0.25,
                    0.014, variant % 2 == 0 ? TEAL : VIOLET, alpha * 0.78F, local);
        }
        if (!reduced) ring(0.42, 0.008, height + 0.03, -angle, TAU * 0.68, 18, PALE, alpha * 0.4F, local);
    }

    private static void meridian(double radius, double height, double angle, int segments,
                                 int color, float alpha, Sink sink) {
        for (int segment = 0; segment < segments; segment++) {
            double first = segment / (double) segments, second = (segment + 1) / (double) segments;
            double firstRadius = radius * Math.cos(first * Math.PI * 0.46);
            double secondRadius = radius * Math.cos(second * Math.PI * 0.46);
            double firstHeight = 0.65 + Math.sin(first * Math.PI / 2) * height;
            double secondHeight = 0.65 + Math.sin(second * Math.PI / 2) * height;
            point(firstRadius, firstHeight, angle - 0.0045, color, alpha * (float) (1 - first * 0.7), sink);
            point(firstRadius, firstHeight, angle + 0.0045, color, alpha * (float) (1 - first * 0.7), sink);
            point(secondRadius, secondHeight, angle + 0.0045, color, alpha * (float) (1 - second * 0.7), sink);
            point(secondRadius, secondHeight, angle - 0.0045, color, alpha * (float) (1 - second * 0.7), sink);
        }
    }

    private static void helix(double radius, double height, double spin, int segments,
                              int color, float alpha, Sink sink) {
        for (int segment = 0; segment < segments; segment++) {
            double first = segment / (double) segments, second = (segment + 1) / (double) segments;
            double firstAngle = spin + first * TAU * 1.25, secondAngle = spin + second * TAU * 1.25;
            point(radius, 0.7 + first * height, firstAngle - 0.002, color, alpha, sink);
            point(radius, 0.7 + first * height, firstAngle + 0.002, color, alpha, sink);
            point(radius, 0.7 + second * height, secondAngle + 0.002, color, alpha, sink);
            point(radius, 0.7 + second * height, secondAngle - 0.002, color, alpha, sink);
        }
    }

    private static void lightPillar(double radius, double angle, double bottom, double top,
                                    double width, int color, float alpha, Sink sink) {
        double halfAngle = width / radius;
        point(radius, bottom, angle - halfAngle, color, 0, sink);
        point(radius, bottom, angle + halfAngle, color, 0, sink);
        point(radius, (bottom + top) / 2, angle + halfAngle, color, alpha, sink);
        point(radius, (bottom + top) / 2, angle - halfAngle, color, alpha, sink);
        point(radius, (bottom + top) / 2, angle - halfAngle, color, alpha, sink);
        point(radius, (bottom + top) / 2, angle + halfAngle, color, alpha, sink);
        point(radius, top, angle + halfAngle, color, 0, sink);
        point(radius, top, angle - halfAngle, color, 0, sink);
    }

    private static void diamond(double radius, double height, double angle, double width,
                                double extent, int color, float alpha, Sink sink) {
        point(radius, height - extent, angle, color, alpha * 0.3F, sink);
        point(radius, height, angle + width / radius, color, alpha, sink);
        point(radius, height + extent, angle, color, alpha * 0.3F, sink);
        point(radius, height, angle - width / radius, color, alpha, sink);
    }

    private static void point(double radius, double height, double angle, int color, float alpha, Sink sink) {
        sink.vertex(Math.cos(angle) * radius, height, Math.sin(angle) * radius, color, alpha);
    }
}
