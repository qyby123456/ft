/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.render.QizaiMagicVisual;
import net.farmingtales.ftboss.util.QizaiCrystalMesh;
import net.minecraft.world.phys.Vec3;

public final class QizaiCrystalModelVisual {
    private QizaiCrystalModelVisual() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(Vec3 origin, double age, double size, double fade, PoseStack pose, VertexConsumer vertices) {
        float scale = (float)(size * Math.max(0.0, Math.min(1.0, fade)));
        if (scale <= 0.0f || !Float.isFinite(scale)) {
            return;
        }
        pose.m_85836_();
        try {
            pose.m_85837_(origin.f_82479_, origin.f_82480_, origin.f_82481_);
            pose.m_85841_(scale, scale, scale);
            QizaiCrystalMesh.render(0, age, false, QizaiMagicVisual.sink(pose, vertices));
        }
        finally {
            pose.m_85849_();
        }
    }
}

