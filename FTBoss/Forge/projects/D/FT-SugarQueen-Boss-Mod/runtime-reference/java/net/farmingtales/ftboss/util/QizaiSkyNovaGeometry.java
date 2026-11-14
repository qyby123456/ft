/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.client.model.QizaiSkyNovaModel;
import net.farmingtales.ftboss.util.QizaiAdvancedGeometry;
import net.farmingtales.ftboss.util.QizaiGeometry;

public final class QizaiSkyNovaGeometry {
    public static final int FULL_QUAD_BUDGET = 740;
    public static final int REDUCED_QUAD_BUDGET = 460;
    public static final int FALL_TICKS = 12;

    private QizaiSkyNovaGeometry() {
    }

    public static double fall(double age, int riseTicks, int chargeTicks) {
        double progress = QizaiAdvancedGeometry.progress(age, riseTicks + chargeTicks - 12, 12.0);
        return progress * progress;
    }

    public static double modelHeight(double age, double height, int riseTicks, int chargeTicks, double scale) {
        double rise = QizaiAdvancedGeometry.progress(age, 0.0, riseTicks);
        double landing = 3.4 * scale + 0.12;
        double altitude = Math.max(landing, (height + 5.0) * rise * rise * (3.0 - 2.0 * rise));
        return altitude + (landing - altitude) * QizaiSkyNovaGeometry.fall(age, riseTicks, chargeTicks);
    }

    public static void render(double age, boolean released, double radius, double height, int riseTicks, int chargeTicks, int recoveryTicks, boolean reduced, QizaiGeometry.Sink sink) {
        if (!Double.isFinite(age + radius + height) || age < 0.0 || radius <= 0.0 || height <= 0.0 || riseTicks <= 0 || chargeTicks <= 0 || recoveryTicks <= 0) {
            return;
        }
        double charge = QizaiAdvancedGeometry.progress(age, riseTicks, chargeTicks);
        double burst = QizaiAdvancedGeometry.progress(age, 0.0, recoveryTicks);
        float fade = released ? (float)(1.0 - burst) : 1.0f;
        int segments = reduced ? 32 : 64;
        QizaiSkyNovaGeometry.ring(radius, 0.55, 0.08, segments, 15219456, fade * 0.96f, sink);
        double size = Math.min(1.0, radius / 16.0);
        if (!released) {
            QizaiSkyNovaGeometry.ring(Math.max(Math.min(radius, 8.0), radius * (1.0 - charge)), 0.23, 0.12, segments, 15910284, 0.85f, sink);
            for (int rune = 0; rune < 12; ++rune) {
                double angle = (double)rune * Math.PI / 6.0;
                double outer = radius - Math.min(radius * 0.1, 1.0);
                double inner = Math.max(0.0, outer - 1.8);
                sink.quad(QizaiSkyNovaGeometry.radial(outer, 0.1, angle), QizaiSkyNovaGeometry.radial(inner, 0.1, angle + 0.025), QizaiSkyNovaGeometry.radial(Math.max(0.0, inner - 0.8), 0.1, angle), QizaiSkyNovaGeometry.radial(inner, 0.1, angle - 0.025), 15910284, (double)rune < charge * 12.0 ? 0.95f : 0.28f);
            }
            double scale = (0.55 + 0.95 * charge) * size;
            double centerY = QizaiSkyNovaGeometry.modelHeight(age, height, riseTicks, chargeTicks, scale);
            double rise = QizaiAdvancedGeometry.progress(age, 0.0, riseTicks);
            QizaiSkyNovaModel.render(age, charge, 0.0, reduced, (float)(0.3 + rise * 0.7), QizaiSkyNovaModel.transform(0.0, centerY, 0.0, 0.0, 0.0, 0.0, scale, sink));
            if (QizaiSkyNovaGeometry.fall(age, riseTicks, chargeTicks) > 0.0) {
                for (int trail = 1; trail <= (reduced ? 2 : 4); ++trail) {
                    double trailHeight = Math.min(height + 9.0, centerY + (double)trail * 2.4);
                    QizaiSkyNovaModel.fragment((0.45 - (double)trail * 0.07) * size, 1.5 * size, (float)((0.42 - (double)trail * 0.065) * QizaiSkyNovaGeometry.fall(age, riseTicks, chargeTicks)), QizaiSkyNovaModel.transform(0.0, trailHeight, 0.0, age * 0.04, 0.0, 0.0, 1.0, sink));
                }
            }
            return;
        }
        double collapse = QizaiAdvancedGeometry.progress(age, 0.0, 8.0);
        QizaiSkyNovaModel.render(riseTicks + chargeTicks, 1.0, collapse, reduced, (float)(1.0 - collapse), QizaiSkyNovaModel.transform(0.0, 5.1 * size + 0.12, 0.0, 0.0, 0.0, 0.0, 1.5 * size, sink));
        QizaiSkyNovaGeometry.ring(radius * Math.sqrt(burst), 0.65 * (double)fade + 0.12, 0.18, segments, 15910284, fade * 0.9f, sink);
        QizaiSkyNovaGeometry.ring(radius * burst * 0.78, 0.4, 0.32, reduced ? 24 : 48, 15692698, fade * 0.7f, sink);
        for (int shard = 0; shard < (reduced ? 6 : 12); ++shard) {
            double angle = (double)shard * Math.PI * 2.0 / (double)(reduced ? 6 : 12) + 0.2;
            double distance = size * 3.0 + burst * radius * 0.5;
            double shardY = 1.0 + Math.sin(burst * Math.PI) * (double)(3 + shard % 3) * size;
            QizaiSkyNovaModel.fragment(0.16 * size * (double)fade, 0.65 * size * (double)fade, fade * fade, QizaiSkyNovaModel.transform(Math.cos(angle) * distance, shardY, Math.sin(angle) * distance, angle, burst * 3.0, burst * 2.0, 1.0, sink));
        }
    }

    private static void ring(double radius, double width, double height, int segments, int color, float alpha, QizaiGeometry.Sink sink) {
        double inner = Math.max(0.0, radius - width);
        for (int segment = 0; segment < segments; ++segment) {
            double first = (double)segment * Math.PI * 2.0 / (double)segments;
            double second = (double)(segment + 1) * Math.PI * 2.0 / (double)segments;
            sink.quad(QizaiSkyNovaGeometry.radial(radius, height, first), QizaiSkyNovaGeometry.radial(radius, height, second), QizaiSkyNovaGeometry.radial(inner, height, second), QizaiSkyNovaGeometry.radial(inner, height, first), color, alpha);
        }
    }

    private static QizaiGeometry.Point radial(double radius, double height, double angle) {
        return new QizaiGeometry.Point(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
    }
}

