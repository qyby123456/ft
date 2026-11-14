package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.util.ManorStandaloneSpellModels.Point;
import net.farmingtales.ftboss.util.ManorStandaloneSpellModels.Sink;

public final class QizaiScrollModelVisual {
    private QizaiScrollModelVisual() {}

    public static Sink sink(PoseStack pose, VertexConsumer vertices) {
        return (first, second, third, fourth, color) -> {
            vertex(pose, vertices, first, color);
            vertex(pose, vertices, second, color);
            vertex(pose, vertices, third, color);
            vertex(pose, vertices, fourth, color);
        };
    }

    private static void vertex(PoseStack pose, VertexConsumer vertices, Point point, int color) {
        vertices.vertex(pose.last().pose(), (float) point.x(), (float) point.y(), (float) point.z())
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255).endVertex();
    }
}
