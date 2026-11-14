/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.client.xuanlanarcane;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.xuanlanarcane.ArcaneForms;
import net.farmingtales.ftboss.client.xuanlanarcane.ArcaneRhythm;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class ArcaneInk {
    private ArcaneInk() {
    }

    public static int quad(ArcaneForms.Facet facet, ArcaneForms.Layer layer, Matrix4f matrix4f, VertexConsumer vertexConsumer, PoseStack poseStack, Vec3 vec3, boolean bl, float f, int n, Brush brush) {
        Object object;
        float f2;
        float f3;
        float f4 = f3 = Float.POSITIVE_INFINITY;
        float f5 = f3;
        float f6 = f2 = Float.NEGATIVE_INFINITY;
        float f7 = f2;
        for (int i = 0; i < 4; ++i) {
            object = facet.position()[i];
            Vector3f vector3f = matrix4f.transformPosition(brush.corners[i].set(object[0], object[1], object[2]));
            f3 = Math.min(f3, vector3f.x);
            f2 = Math.max(f2, vector3f.x);
            f4 = Math.min(f4, vector3f.y);
            f6 = Math.max(f6, vector3f.y);
            f5 = Math.min(f5, vector3f.z);
            f7 = Math.max(f7, vector3f.z);
        }
        if (bl) {
            double d = Math.max((double)f3 - vec3.f_82479_, Math.max(0.0, vec3.f_82479_ - (double)f2));
            double d2 = Math.max((double)f4 - vec3.f_82480_, Math.max(0.0, vec3.f_82480_ - (double)f6));
            double d3 = Math.max((double)f5 - vec3.f_82481_, Math.max(0.0, vec3.f_82481_ - (double)f7));
            f *= ArcaneRhythm.nearFade(Math.sqrt(d * d + d2 * d2 + d3 * d3));
        }
        if (f < 0.008f) {
            return 0;
        }
        Vector3f vector3f = brush.tangent.set((Vector3fc)brush.corners[1]).sub((Vector3fc)brush.corners[0]).cross((Vector3fc)brush.normal.set((Vector3fc)brush.corners[2]).sub((Vector3fc)brush.corners[0])).normalize();
        object = poseStack.m_85850_();
        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f2 = brush.corners[i];
            float f8 = ((float)(layer.tile() % 16 * 16 + 1) + facet.uv()[i][0] * 13.0f) / 256.0f;
            float f9 = ((float)(layer.tile() / 16 * 16 + 1) + facet.uv()[i][1] * 13.0f) / 256.0f;
            vertexConsumer.m_252986_(object.m_252922_(), (float)((double)vector3f2.x - vec3.f_82479_), (float)((double)vector3f2.y - vec3.f_82480_), (float)((double)vector3f2.z - vec3.f_82481_)).m_85950_(1.0f, 1.0f, 1.0f, f).m_7421_(f8, f9).m_86008_(OverlayTexture.f_118083_).m_85969_(n).m_252939_(object.m_252943_(), vector3f.x, vector3f.y, vector3f.z).m_5752_();
        }
        return 1;
    }

    public static final class Brush {
        private final Vector3f[] corners = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
        private final Vector3f tangent = new Vector3f();
        private final Vector3f normal = new Vector3f();
    }
}

