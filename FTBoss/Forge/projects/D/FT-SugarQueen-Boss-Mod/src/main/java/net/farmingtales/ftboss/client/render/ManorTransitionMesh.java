package net.farmingtales.ftboss.client.render;

/** 切换运行时网格：空心领地法阵、交叉光弧和交接波，不生成实体模型；2026-10-25重做。 */

public final class ManorTransitionMesh {
    @FunctionalInterface
    public interface Sink {
        void vertex(double posX, double posY, double posZ, int color, float alpha);
    }

    @FunctionalInterface
    public interface Surface {
        double height(double posX, double posZ);
    }

    private static final double TAU = Math.PI * 2;
    private static final int GOLD = 0xe8cb83;
    private static final int IVORY = 0xfff2cf;

    private final Sink sink;
    private final Surface surface;
    private final int limit;
    private int vertices;

    private ManorTransitionMesh(Sink sink, Surface surface, int limit) {
        this.sink = sink;
        this.surface = surface;
        this.limit = limit - limit % 4;
    }

    public static int draw(double age, int variant, int detail, int budget, Surface surface, Sink sink) {
        if (!ManorTransitionRules.valid(variant, age) || budget < 4 || surface == null || sink == null) return 0;
        ManorTransitionMesh mesh = new ManorTransitionMesh(sink, surface, Math.min(budget, ManorTransitionRules.budget(detail)));
        mesh.draw(age, variant, detail);
        return mesh.vertices;
    }

    private void draw(double age, int variant, int detail) {
        float alpha = (float) ManorTransitionRules.opacity(age);
        if (alpha <= 0.001F) return;
        int accent = ManorTransitionRules.accent(variant);
        int segments = detail == 0 ? 64 : detail == 1 ? 40 : 24;
        int seals = detail == 0 ? 12 : detail == 1 ? 8 : 4;
        double radius = ManorTransitionRules.domain(age);
        double lift = ManorTransitionRules.smooth((age - 14) / 30.0);
        double transfer = ManorTransitionRules.smooth((age - 52) / 8.0);
        double release = ManorTransitionRules.smooth((age - 60) / 30.0);
        double hold = 1 - release * 0.55;

        ring(radius, 0.055, 0.026, segments, 0, GOLD, alpha * 0.65F);
        ring(radius - 0.28, 0.041, 0.048, segments, 0, accent, alpha * 0.46F);
        ring(Math.max(1.45, radius * 0.37), 0.045, 0.075, segments, age * 0.004, IVORY, alpha * 0.5F);
        if (age >= 60) {
            double wave = 1.65 + 11.1 * ManorTransitionRules.smooth((age - 60) / 26.0);
            ring(wave, 0.09 * (1 - release) + 0.018, 0.108, segments, 0, IVORY, alpha * (float) (1 - release) * 0.72F);
        }

        for (int seal = 0; seal < seals; seal++) {
            double angle = seal * TAU / seals + 0.12;
            double runeRadius = Math.max(1.8, radius - 0.8);
            double runeSize = 0.25 + 0.1 * lift;
            glyph(Math.cos(angle) * runeRadius, Math.sin(angle) * runeRadius, angle, runeSize, 0.092, seal, accent, alpha * 0.78F);
            double spokeRadius = Math.max(1.6, radius * 0.57);
            double outerRadius = Math.max(1.8, radius - 1.5);
            groundLine(Math.cos(angle) * spokeRadius, Math.sin(angle) * spokeRadius,
                    Math.cos(angle) * outerRadius, Math.sin(angle) * outerRadius, 0.019, 0.052, GOLD, alpha * 0.27F);
            arc(radius - 0.58, 0.024, 0.062, angle - 0.07, angle + 0.07, 3, IVORY, alpha * 0.62F);
        }

        int pillars = detail == 0 ? 6 : detail == 1 ? 4 : 2;
        for (int pillar = 0; pillar < pillars; pillar++) {
            double baseAngle = pillar * TAU / pillars + age * 0.007;
            int sections = detail == 0 ? 16 : detail == 1 ? 10 : 6;
            for (int section = 0; section < sections; section++) {
                double start = section / (double) sections;
                double end = (section + 1.0) / sections;
                double startAngle = baseAngle + start * 0.66;
                double endAngle = baseAngle + end * 0.66;
                double startRadius = (radius * 0.82 - start * 2.3) * hold;
                double endRadius = (radius * 0.82 - end * 2.3) * hold;
                double startHeight = 0.25 + Math.sin(start * Math.PI * 0.72) * 7.4 * lift;
                double endHeight = 0.25 + Math.sin(end * Math.PI * 0.72) * 7.4 * lift;
                float brightness = alpha * (float) (Math.sin((start + 0.03) * Math.PI) * 0.38 * hold);
                airLine(Math.cos(startAngle) * startRadius, startHeight, Math.sin(startAngle) * startRadius,
                        Math.cos(endAngle) * endRadius, endHeight, Math.sin(endAngle) * endRadius,
                        0.025 + 0.028 * transfer, accent, brightness);
            }
        }

        if (detail < 2) {
            double orbitRadius = 2.6 + lift * 2.0 - release * 1.5;
            int orbitSegments = detail == 0 ? 40 : 16;
            for (int segment = 0; segment < orbitSegments; segment++) {
                double start = segment * TAU / orbitSegments + age * 0.018;
                double end = (segment + 0.72) * TAU / orbitSegments + age * 0.018;
                double baseHeight = 3.2 + lift * 2.5;
                airLine(Math.cos(start) * orbitRadius, baseHeight + Math.sin(start) * 1.1, Math.sin(start) * orbitRadius,
                        Math.cos(end) * orbitRadius, baseHeight + Math.sin(end) * 1.1, Math.sin(end) * orbitRadius,
                        0.038, GOLD, alpha * (float) lift * 0.6F);
            }
        }

        if (detail == 0) {
            for (int mote = 0; mote < 18; mote++) {
                double angle = mote * 2.39996 + age * 0.005;
                double moteRadius = 3.1 + mote % 5 * 1.2;
                double height = 0.5 + (mote * 0.47 + age * 0.045) % 5.8;
                double size = 0.025 + 0.018 * Math.sin(mote + age * 0.06);
                double posX = Math.cos(angle) * moteRadius;
                double posZ = Math.sin(angle) * moteRadius;
                airLine(posX, height - size * 3, posZ, posX, height + size * 3, posZ, size, IVORY, alpha * 0.5F);
            }
        }
    }

    private void glyph(double centerX, double centerZ, double angle, double size, double height, int index, int color, float alpha) {
        double radialX = Math.cos(angle) * size;
        double radialZ = Math.sin(angle) * size;
        double tangentX = -radialZ;
        double tangentZ = radialX;
        groundLine(centerX - radialX, centerZ - radialZ, centerX + tangentX, centerZ + tangentZ, 0.025, height, color, alpha);
        groundLine(centerX + tangentX, centerZ + tangentZ, centerX + radialX, centerZ + radialZ, 0.025, height, color, alpha);
        groundLine(centerX + radialX, centerZ + radialZ, centerX - tangentX, centerZ - tangentZ, 0.025, height, color, alpha);
        if ((index & 1) == 0) {
            groundLine(centerX - tangentX, centerZ - tangentZ, centerX - radialX, centerZ - radialZ, 0.025, height, color, alpha);
        }
        groundLine(centerX - radialX * 1.5, centerZ - radialZ * 1.5,
                centerX + radialX * 1.5, centerZ + radialZ * 1.5, 0.017, height + 0.008, IVORY, alpha * 0.65F);
    }

    private void ring(double radius, double width, double height, int segments, double rotation, int color, float alpha) {
        if (radius > 0) arc(radius, width, height, rotation, rotation + TAU, segments, color, alpha);
    }

    private void arc(double radius, double width, double height, double start, double end, int segments, int color, float alpha) {
        for (int segment = 0; segment < segments; segment++) {
            double angle = start + (end - start) * segment / segments;
            double next = start + (end - start) * (segment + 1.0) / segments;
            groundLine(Math.cos(angle) * radius, Math.sin(angle) * radius, Math.cos(next) * radius, Math.sin(next) * radius,
                    width, height, color, alpha);
        }
    }

    private void groundLine(double startX, double startZ, double endX, double endZ,
                            double width, double height, int color, float alpha) {
        double startY = surface.height(startX, startZ);
        double endY = surface.height(endX, endZ);
        if (!Double.isFinite(startY) || !Double.isFinite(endY) || Math.abs(startY - endY) > 0.2) return;
        double length = Math.hypot(endX - startX, endZ - startZ);
        if (length < 0.000001) return;
        double marginX = -(endZ - startZ) / length * width * 1.5;
        double marginZ = (endX - startX) / length * width * 1.5;
        for (int side : new int[]{-1, 1}) {
            double first = surface.height(startX + marginX * side, startZ + marginZ * side);
            double last = surface.height(endX + marginX * side, endZ + marginZ * side);
            if (!Double.isFinite(first) || !Double.isFinite(last) || Math.abs(first - startY) > 0.02 || Math.abs(last - endY) > 0.02) return;
        }
        line(startX, startY + height, startZ, endX, endY + height, endZ, width, color, alpha, false);
    }

    private void airLine(double startX, double startY, double startZ, double endX, double endY, double endZ,
                         double width, int color, float alpha) {
        line(startX, startY, startZ, endX, endY, endZ, width, color, alpha, true);
    }

    private void line(double startX, double startY, double startZ, double endX, double endY, double endZ,
                      double width, int color, float alpha, boolean crossed) {
        if (alpha <= 0.002F || vertices + (crossed ? 24 : 12) > limit) return;
        double deltaX = endX - startX;
        double deltaY = endY - startY;
        double deltaZ = endZ - startZ;
        double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        if (length < 0.000001) return;
        double horizontal = Math.hypot(deltaX, deltaZ);
        double normalX = horizontal > 0.000001 ? -deltaZ / horizontal : 1;
        double normalZ = horizontal > 0.000001 ? deltaX / horizontal : 0;
        strip(startX, startY, startZ, endX, endY, endZ, normalX, 0, normalZ, width, color, alpha);
        if (crossed) strip(startX, startY, startZ, endX, endY, endZ,
                -deltaY * normalZ / length, (deltaX * normalZ - deltaZ * normalX) / length,
                deltaY * normalX / length, width, color, alpha * 0.65F);
    }

    private void strip(double startX, double startY, double startZ, double endX, double endY, double endZ,
                       double normalX, double normalY, double normalZ, double width, int color, float alpha) {
        for (int band = 0; band < 3; band++) {
            double first = (band - 1.5) * width;
            double second = (band - 0.5) * width;
            float firstAlpha = band == 0 ? 0 : alpha;
            float secondAlpha = band == 2 ? 0 : alpha;
            vertex(startX + normalX * first, startY + normalY * first, startZ + normalZ * first, color, firstAlpha);
            vertex(endX + normalX * first, endY + normalY * first, endZ + normalZ * first, color, firstAlpha);
            vertex(endX + normalX * second, endY + normalY * second, endZ + normalZ * second, color, secondAlpha);
            vertex(startX + normalX * second, startY + normalY * second, startZ + normalZ * second, color, secondAlpha);
        }
    }

    private void vertex(double posX, double posY, double posZ, int color, float alpha) {
        sink.vertex(posX, posY, posZ, color, alpha);
        vertices++;
    }
}
