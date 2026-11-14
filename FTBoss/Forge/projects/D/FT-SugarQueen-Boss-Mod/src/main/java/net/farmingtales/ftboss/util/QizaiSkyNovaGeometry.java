package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.client.model.QizaiSkyNovaModel;
import net.farmingtales.ftboss.util.QizaiGeometry.Point;
import net.farmingtales.ftboss.util.QizaiGeometry.Sink;

public final class QizaiSkyNovaGeometry {
    public static final int FULL_QUAD_BUDGET = 740;
    public static final int REDUCED_QUAD_BUDGET = 460;
    public static final int FALL_TICKS = 12;

    private QizaiSkyNovaGeometry() {}

    public static double fall(double age, int riseTicks, int chargeTicks) {
        double progress = QizaiAdvancedGeometry.progress(age, riseTicks + chargeTicks - FALL_TICKS, FALL_TICKS);
        return progress * progress;
    }

    public static double modelHeight(double age, double height, int riseTicks, int chargeTicks, double scale) {
        double rise = QizaiAdvancedGeometry.progress(age, 0, riseTicks);
        double landing = -QizaiSkyNovaModel.TIP * scale + 0.12;
        double altitude = Math.max(landing, (height + 5) * rise * rise * (3 - 2 * rise));
        return altitude + (landing - altitude) * fall(age, riseTicks, chargeTicks);
    }

    public static void render(double age, boolean released, double radius, double height,
                              int riseTicks, int chargeTicks, int recoveryTicks, boolean reduced, Sink sink) {
        if (!Double.isFinite(age + radius + height) || age < 0 || radius <= 0 || height <= 0
                || riseTicks <= 0 || chargeTicks <= 0 || recoveryTicks <= 0) return;
        double charge = QizaiAdvancedGeometry.progress(age, riseTicks, chargeTicks);
        double burst = QizaiAdvancedGeometry.progress(age, 0, recoveryTicks);
        float fade = released ? (float) (1 - burst) : 1;
        int segments = reduced ? 32 : 64;
        ring(radius, 0.55, 0.08, segments, QizaiGeometry.WARNING, fade * 0.96F, sink);
        double size = Math.min(1, radius / 16);
        if (!released) {
            ring(Math.max(Math.min(radius, 8), radius * (1 - charge)), 0.23, 0.12, segments,
                    QizaiSkyNovaModel.EDGE, 0.85F, sink);
            for (int rune = 0; rune < 12; rune++) {
                double angle = rune * Math.PI / 6;
                double outer = radius - Math.min(radius * 0.1, 1);
                double inner = Math.max(0, outer - 1.8);
                sink.quad(radial(outer, 0.1, angle), radial(inner, 0.1, angle + 0.025),
                        radial(Math.max(0, inner - 0.8), 0.1, angle), radial(inner, 0.1, angle - 0.025),
                        QizaiSkyNovaModel.EDGE, rune < charge * 12 ? 0.95F : 0.28F);
            }
            double scale = (0.55 + 0.95 * charge) * size;
            double centerY = modelHeight(age, height, riseTicks, chargeTicks, scale);
            double rise = QizaiAdvancedGeometry.progress(age, 0, riseTicks);
            QizaiSkyNovaModel.render(age, charge, 0, reduced, (float) (0.3 + rise * 0.7),
                    QizaiSkyNovaModel.transform(0, centerY, 0, 0, 0, 0, scale, sink));
            if (fall(age, riseTicks, chargeTicks) > 0) {
                for (int trail = 1; trail <= (reduced ? 2 : 4); trail++) {
                    double trailHeight = Math.min(height + 9, centerY + trail * 2.4);
                    QizaiSkyNovaModel.fragment((0.45 - trail * 0.07) * size, 1.5 * size,
                            (float) ((0.42 - trail * 0.065) * fall(age, riseTicks, chargeTicks)),
                            QizaiSkyNovaModel.transform(0, trailHeight, 0, age * 0.04, 0, 0, 1, sink));
                }
            }
            return;
        }
        double collapse = QizaiAdvancedGeometry.progress(age, 0, 8);
        QizaiSkyNovaModel.render(riseTicks + chargeTicks, 1, collapse, reduced, (float) (1 - collapse),
                QizaiSkyNovaModel.transform(0, -QizaiSkyNovaModel.TIP * 1.5 * size + 0.12, 0, 0, 0, 0, 1.5 * size, sink));
        ring(radius * Math.sqrt(burst), 0.65 * fade + 0.12, 0.18, segments, QizaiSkyNovaModel.EDGE, fade * 0.9F, sink);
        ring(radius * burst * 0.78, 0.4, 0.32, reduced ? 24 : 48, QizaiSkyNovaModel.EMBER, fade * 0.7F, sink);
        for (int shard = 0; shard < (reduced ? 6 : 12); shard++) {
            double angle = shard * Math.PI * 2 / (reduced ? 6 : 12) + 0.2;
            double distance = size * 3 + burst * radius * 0.5;
            double shardY = 1.0 + Math.sin(burst * Math.PI) * (3 + shard % 3) * size;
            QizaiSkyNovaModel.fragment(0.16 * size * fade, 0.65 * size * fade, fade * fade,
                    QizaiSkyNovaModel.transform(Math.cos(angle) * distance, shardY, Math.sin(angle) * distance,
                            angle, burst * 3, burst * 2, 1, sink));
        }
    }

    private static void ring(double radius, double width, double height, int segments, int color, float alpha, Sink sink) {
        double inner = Math.max(0, radius - width);
        for (int segment = 0; segment < segments; segment++) {
            double first = segment * Math.PI * 2 / segments, second = (segment + 1) * Math.PI * 2 / segments;
            sink.quad(radial(radius, height, first), radial(radius, height, second),
                    radial(inner, height, second), radial(inner, height, first), color, alpha);
        }
    }

    private static Point radial(double radius, double height, double angle) {
        return new Point(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
    }
}
