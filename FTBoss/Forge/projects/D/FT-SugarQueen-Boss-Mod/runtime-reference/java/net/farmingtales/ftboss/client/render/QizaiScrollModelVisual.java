/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.util.ManorStandaloneSpellModels;

public final class QizaiScrollModelVisual {
    private QizaiScrollModelVisual() {
    }

    public static ManorStandaloneSpellModels.Sink sink(PoseStack pose, VertexConsumer vertices) {
        return (first, second, third, fourth, color) -> {
            QizaiScrollModelVisual.vertex(pose, vertices, first, color);
            QizaiScrollModelVisual.vertex(pose, vertices, second, color);
            QizaiScrollModelVisual.vertex(pose, vertices, third, color);
            QizaiScrollModelVisual.vertex(pose, vertices, fourth, color);
        };
    }

    private static void vertex(PoseStack pose, VertexConsumer vertices, ManorStandaloneSpellModels.Point point, int color) {
        vertices.m_252986_(pose.m_85850_().m_252922_(), (float)point.x(), (float)point.y(), (float)point.z()).m_6122_(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, 255).m_5752_();
    }
}

