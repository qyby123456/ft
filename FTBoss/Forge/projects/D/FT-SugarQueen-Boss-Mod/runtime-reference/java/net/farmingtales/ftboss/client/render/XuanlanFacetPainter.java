/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import net.farmingtales.ftboss.client.render.XuanlanSpellMesh;
import net.farmingtales.ftboss.client.render.XuanlanSpellMotion;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class XuanlanFacetPainter {
    private XuanlanFacetPainter() {
    }

    public static int draw(VertexConsumer vertexConsumer, PoseStack poseStack, XuanlanSpellMesh.Face face, Vec3 vec3, Quaternionf quaternionf, double d, double d2, double d3, float f, Vec3 vec32, boolean bl, Entity entity, boolean bl2, int n, Brush brush) {
        double[][] dArray = brush.points;
        Vector3f vector3f = brush.scratch;
        double[] dArray2 = brush.minimum;
        double[] dArray3 = brush.maximum;
        Arrays.fill(dArray2, Double.POSITIVE_INFINITY);
        Arrays.fill(dArray3, Double.NEGATIVE_INFINITY);
        for (int i = 0; i < 4; ++i) {
            XuanlanSpellMotion.local(face, i, d3, dArray[i]);
            quaternionf.transform(vector3f.set((float)(dArray[i][0] * d), (float)(dArray[i][1] * d), (float)(dArray[i][2] * d2)));
            dArray[i][0] = vec3.f_82479_ + (double)vector3f.x;
            dArray[i][1] = vec3.f_82480_ + (double)vector3f.y;
            dArray[i][2] = vec3.f_82481_ + (double)vector3f.z;
            for (int j = 0; j < 3; ++j) {
                dArray2[j] = Math.min(dArray2[j], dArray[i][j]);
                dArray3[j] = Math.max(dArray3[j], dArray[i][j]);
            }
        }
        double d4 = 0.0;
        double[] dArray4 = brush.camera;
        dArray4[0] = vec32.f_82479_;
        dArray4[1] = vec32.f_82480_;
        dArray4[2] = vec32.f_82481_;
        for (int i = 0; i < 3; ++i) {
            double d5 = Math.max(0.0, Math.max(dArray2[i] - dArray4[i], dArray4[i] - dArray3[i]));
            d4 += d5 * d5;
        }
        if (!bl2 && entity != null) {
            AABB aABB = entity.m_20191_();
            if (dArray3[0] >= aABB.f_82288_ - 0.15 && dArray2[0] <= aABB.f_82291_ + 0.15 && dArray3[1] >= aABB.f_82289_ - 0.15 && dArray2[1] <= aABB.f_82292_ + 0.15 && dArray3[2] >= aABB.f_82290_ - 0.15 && dArray2[2] <= aABB.f_82293_ + 0.15) {
                f *= 0.12f;
            }
        }
        if (bl && !bl2) {
            f *= XuanlanSpellMotion.cameraFade(Math.sqrt(d4));
        }
        if (f <= 0.005f) {
            return 0;
        }
        PoseStack.Pose pose = poseStack.m_85850_();
        Vector3f vector3f2 = brush.edge.set((float)(dArray[1][0] - dArray[0][0]), (float)(dArray[1][1] - dArray[0][1]), (float)(dArray[1][2] - dArray[0][2]));
        Vector3f vector3f3 = vector3f2.cross((Vector3fc)brush.normal.set((float)(dArray[2][0] - dArray[0][0]), (float)(dArray[2][1] - dArray[0][1]), (float)(dArray[2][2] - dArray[0][2]))).normalize();
        for (int i = 0; i < 4; ++i) {
            float f2 = (float)(((double)(face.material() % 16 * 16 + 2) + face.uv()[i][0] * 11.0) / 256.0);
            float f3 = (float)(((double)(face.material() / 16 * 16 + 2) + face.uv()[i][1] * 11.0) / 256.0);
            vertexConsumer.m_252986_(pose.m_252922_(), (float)(dArray[i][0] - vec32.f_82479_), (float)(dArray[i][1] - vec32.f_82480_), (float)(dArray[i][2] - vec32.f_82481_)).m_85950_(1.0f, 1.0f, 1.0f, f).m_7421_(f2, f3).m_86008_(OverlayTexture.f_118083_).m_85969_(n).m_252939_(pose.m_252943_(), vector3f3.x, vector3f3.y, vector3f3.z).m_5752_();
        }
        return 1;
    }

    public static final class Brush {
        private final double[][] points = new double[4][3];
        private final double[] minimum = new double[3];
        private final double[] maximum = new double[3];
        private final double[] camera = new double[3];
        private final Vector3f scratch = new Vector3f();
        private final Vector3f edge = new Vector3f();
        private final Vector3f normal = new Vector3f();
    }
}

