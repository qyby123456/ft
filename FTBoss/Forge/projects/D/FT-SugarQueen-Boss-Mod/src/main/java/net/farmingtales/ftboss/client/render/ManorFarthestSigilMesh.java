package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.boss.ai.ManorFarthestSigilRules;

/** 巨型虚空法阵与喷泉、横贯光束、天降光柱的程序化网格。 */
public final class ManorFarthestSigilMesh {
    private static final int VIOLET = 0x9665FF;
    private static final int PALE = 0xE7DBFF;
    private static final int CORE = 0xFFFFFF;
    private static final int SEGMENTS = 96;

    private ManorFarthestSigilMesh() {}

    @FunctionalInterface
    public interface Sink {
        void vertex(double positionX, double positionY, double positionZ, int color, float alpha);
    }

    public static void draw(int style, boolean release, double age, Sink sink) {
        double duration = release ? ManorFarthestSigilRules.RELEASE_TICKS : ManorFarthestSigilRules.WINDUP_TICKS + 4;
        if (style < 0 || style > 2 || !Double.isFinite(age) || age < 0 || age >= duration) return;
        double progress = Math.min(1, age / ManorFarthestSigilRules.WINDUP_TICKS);
        float alpha = release ? (float) (1 - age / duration) : (float) Math.min(0.85, 0.2 + age / 10);
        double radius = ManorFarthestSigilRules.SIGIL_RADIUS * (release ? 1 : Math.min(1, 0.3 + progress * 1.6));
        ring(sink, radius, 0.08, 0, VIOLET, alpha * 0.8F);
        ring(sink, radius - 0.35, 0.035, 0.015, PALE, alpha * 0.8F);
        ring(sink, radius * 0.67, 0.05, 0.025, VIOLET, alpha * 0.7F);
        for (int point = 0; point < 12; point++) {
            double angle = point * Math.PI / 6 + age * (release ? -0.009 : 0.008);
            double next = angle + Math.PI * 2 / 3;
            double orbit = radius * 0.67;
            line(sink, Math.cos(angle) * orbit, Math.sin(angle) * orbit,
                    Math.cos(next) * orbit, Math.sin(next) * orbit, 0.035, PALE, alpha * 0.5F);
            double inner = radius * 0.85;
            double outer = radius * 0.94;
            line(sink, Math.cos(angle) * inner, Math.sin(angle) * inner,
                    Math.cos(angle + 0.035) * outer, Math.sin(angle + 0.035) * outer, 0.055, PALE, alpha);
        }
        if (!release) {
            if (style == ManorFarthestSigilRules.BEAM) {
                line(sink, -8, -1.5, 8, -1.5, 0.06, PALE, alpha);
                line(sink, -8, 1.5, 8, 1.5, 0.06, PALE, alpha);
            } else {
                ring(sink, style == ManorFarthestSigilRules.FOUNTAIN ? 3 : 2.5, 0.09, 0.04, PALE, alpha);
            }
            ring(sink, 0.4 + (1 - progress) * 2, 0.065, 0.055, CORE, alpha);
            return;
        }
        double growth = Math.min(1, (age + 1) / 3);
        if (style == ManorFarthestSigilRules.FOUNTAIN) {
            for (int jet = 0; jet < 7; jet++) {
                double angle = jet * Math.PI * 2 / 6;
                double offset = jet == 6 ? 0 : 1.8;
                double height = (jet == 6 ? 8 : 5.5) * growth;
                double centerX = Math.cos(angle) * offset;
                double centerZ = Math.sin(angle) * offset;
                column(sink, centerX, centerZ, 0.32, 0.08, height, VIOLET, alpha * 0.65F);
                column(sink, centerX, centerZ, 0.1, 0.015, height, PALE, alpha * 0.85F);
            }
            ring(sink, 3, 0.14, 0.06, PALE, alpha);
            ring(sink, 1 + age * 0.12, 0.055, age * 0.25, VIOLET, alpha * 0.7F);
        } else if (style == ManorFarthestSigilRules.BEAM) {
            double width = 1.5 * growth;
            beam(sink, width, VIOLET, alpha * 0.25F);
            beam(sink, 0.15, CORE, alpha * 0.85F);
            ring(sink, 1.5, 0.1, 0.04, PALE, alpha);
        } else {
            column(sink, 0, 0, 2.5 * growth, 2.5 * growth, 20, VIOLET, alpha * 0.22F);
            column(sink, 0, 0, 0.28 * growth, 0.28 * growth, 20, CORE, alpha * 0.8F);
            for (int halo = 0; halo < 5; halo++) ring(sink, 2.5, 0.1, halo * 4 + age * 0.06, PALE, alpha * 0.7F);
        }
    }

    private static void ring(Sink sink, double radius, double width, double height, int color, float alpha) {
        for (int segment = 0; segment < SEGMENTS; segment++) {
            double first = segment * Math.PI * 2 / SEGMENTS;
            double second = (segment + 1) * Math.PI * 2 / SEGMENTS;
            quad(sink, Math.cos(first) * (radius - width), height, Math.sin(first) * (radius - width),
                    Math.cos(first) * (radius + width), height, Math.sin(first) * (radius + width),
                    Math.cos(second) * (radius + width), height, Math.sin(second) * (radius + width),
                    Math.cos(second) * (radius - width), height, Math.sin(second) * (radius - width), color, alpha);
        }
    }

    private static void line(Sink sink, double startX, double startZ, double endX, double endZ, double width, int color, float alpha) {
        double length = Math.hypot(endX - startX, endZ - startZ);
        if (length < 0.0001) return;
        double normalX = -(endZ - startZ) * width / length;
        double normalZ = (endX - startX) * width / length;
        quad(sink, startX + normalX, 0.035, startZ + normalZ, endX + normalX, 0.035, endZ + normalZ,
                endX - normalX, 0.035, endZ - normalZ, startX - normalX, 0.035, startZ - normalZ, color, alpha);
    }

    private static void column(Sink sink, double centerX, double centerZ, double bottom, double top,
            double height, int color, float alpha) {
        for (int segment = 0; segment < 24; segment++) {
            double first = segment * Math.PI / 12;
            double second = (segment + 1) * Math.PI / 12;
            quad(sink, centerX + Math.cos(first) * bottom, 0, centerZ + Math.sin(first) * bottom,
                    centerX + Math.cos(second) * bottom, 0, centerZ + Math.sin(second) * bottom,
                    centerX + Math.cos(second) * top, height, centerZ + Math.sin(second) * top,
                    centerX + Math.cos(first) * top, height, centerZ + Math.sin(first) * top, color, alpha);
        }
    }

    private static void beam(Sink sink, double radius, int color, float alpha) {
        for (int segment = 0; segment < 24; segment++) {
            double first = segment * Math.PI / 12;
            double second = (segment + 1) * Math.PI / 12;
            double firstY = 2 + Math.cos(first) * radius;
            double firstZ = Math.sin(first) * radius;
            double secondY = 2 + Math.cos(second) * radius;
            double secondZ = Math.sin(second) * radius;
            quad(sink, -8, firstY, firstZ, 8, firstY, firstZ, 8, secondY, secondZ, -8, secondY, secondZ, color, alpha);
            for (double end : new double[]{-8, 8})
                quad(sink, end, 2, 0, end, firstY, firstZ, end, secondY, secondZ, end, 2, 0, color, alpha);
        }
    }

    private static void quad(Sink sink, double firstX, double firstY, double firstZ, double secondX, double secondY, double secondZ,
            double thirdX, double thirdY, double thirdZ, double fourthX, double fourthY, double fourthZ, int color, float alpha) {
        sink.vertex(firstX, firstY, firstZ, color, alpha);
        sink.vertex(secondX, secondY, secondZ, color, alpha);
        sink.vertex(thirdX, thirdY, thirdZ, color, alpha);
        sink.vertex(fourthX, fourthY, fourthZ, color, alpha);
    }
}
