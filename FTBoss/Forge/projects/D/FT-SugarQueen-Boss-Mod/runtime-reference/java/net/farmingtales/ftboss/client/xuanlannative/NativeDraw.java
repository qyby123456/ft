/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.client.xuanlannative;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.xuanlannative.NativeForms;
import net.farmingtales.ftboss.client.xuanlannative.NativeMotion;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class NativeDraw {
    private NativeDraw() {
    }

    public static int quad(NativeForms.Face face, NativeForms.Layer layer, Matrix4f matrix4f, PoseStack poseStack, Vec3 vec3, boolean bl, float f, VertexConsumer vertexConsumer, Brush brush) {
        Object object;
        float f2;
        float f3;
        float f4 = f3 = Float.POSITIVE_INFINITY;
        float f5 = f3;
        float f6 = f2 = Float.NEGATIVE_INFINITY;
        float f7 = f2;
        for (int i = 0; i < 4; ++i) {
            object = face.position()[i];
            Vector3f vector3f = matrix4f.transformPosition(brush.corners[i].set(object[0], object[1], object[2]));
            f3 = Math.min(f3, vector3f.x);
            f4 = Math.min(f4, vector3f.y);
            f5 = Math.min(f5, vector3f.z);
            f2 = Math.max(f2, vector3f.x);
            f6 = Math.max(f6, vector3f.y);
            f7 = Math.max(f7, vector3f.z);
        }
        if (bl) {
            double d = Math.max(0.0, Math.max((double)f3 - vec3.f_82479_, vec3.f_82479_ - (double)f2));
            double d2 = Math.max(0.0, Math.max((double)f4 - vec3.f_82480_, vec3.f_82480_ - (double)f6));
            double d3 = Math.max(0.0, Math.max((double)f5 - vec3.f_82481_, vec3.f_82481_ - (double)f7));
            f *= NativeMotion.near(Math.sqrt(d * d + d2 * d2 + d3 * d3));
        }
        if (f < 0.008f) {
            return 0;
        }
        Vector3f vector3f = brush.normal.set((Vector3fc)brush.corners[1]).sub((Vector3fc)brush.corners[0]).cross((Vector3fc)brush.side.set((Vector3fc)brush.corners[2]).sub((Vector3fc)brush.corners[0])).normalize();
        object = poseStack.m_85850_();
        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f2 = brush.corners[i];
            float f8 = ((float)(layer.tile() * 16 + 1) + face.uv()[i][0] * 13.0f) / 256.0f;
            float f9 = (1.0f + face.uv()[i][1] * 13.0f) / 256.0f;
            vertexConsumer.m_252986_(object.m_252922_(), (float)((double)vector3f2.x - vec3.f_82479_), (float)((double)vector3f2.y - vec3.f_82480_), (float)((double)vector3f2.z - vec3.f_82481_)).m_85950_(1.0f, 1.0f, 1.0f, f).m_7421_(f8, f9).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(object.m_252943_(), vector3f.x, vector3f.y, vector3f.z).m_5752_();
        }
        return 1;
    }

    public static int model(String string, Matrix4f matrix4f, Matrix4f matrix4f2, double d, double d2, float f, boolean bl, boolean bl2, Vec3 vec3, PoseStack poseStack, VertexConsumer vertexConsumer, int n) {
        NativeForms.Model model = NativeForms.get(string);
        if (model == null || n <= 0 || !Float.isFinite(f) || f < 0.008f) {
            return 0;
        }
        Brush brush = new Brush();
        int n2 = 0;
        for (int i = 0; i < 2; ++i) {
            for (NativeForms.Layer layer : model.layers()) {
                boolean bl3 = layer.role().equals("boundary");
                if (bl3 != (i == 0) || bl && layer.role().equals("solid") || layer.facets().size() > n - n2) continue;
                float f2 = f * layer.alpha() * (bl && !bl3 && !layer.role().equals("beam") ? 0.4f : 1.0f);
                if (!bl3 && !layer.role().equals("solid")) {
                    f2 *= (float)(0.88 + 0.12 * Math.sin(d * 0.19 + (double)layer.tile() * 0.71));
                }
                if (!Float.isFinite(f2) || f2 < 0.008f) continue;
                Matrix4f matrix4f3 = new Matrix4f((Matrix4fc)(bl3 ? matrix4f : matrix4f2));
                if (layer.joint() >= 0) {
                    matrix4f3.mul((Matrix4fc)NativeMotion.joint(model.joints().get(layer.joint()), d, d2));
                }
                for (NativeForms.Face face : layer.facets()) {
                    n2 += NativeDraw.quad(face, layer, matrix4f3, poseStack, vec3, bl2, f2, vertexConsumer, brush);
                }
            }
        }
        return n2;
    }

    public static final class Brush {
        final Vector3f[] corners = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
        final Vector3f normal = new Vector3f();
        final Vector3f side = new Vector3f();
    }
}

