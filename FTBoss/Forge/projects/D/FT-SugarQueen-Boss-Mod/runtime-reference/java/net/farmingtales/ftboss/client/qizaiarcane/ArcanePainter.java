/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.client.qizaiarcane;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneBeam;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneEntityBudget;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneRig;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneSafety;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class ArcanePainter {
    private ArcanePainter() {
    }

    public static int effect(ArcaneRig.Effect effect, String string, double d, PoseStack poseStack, MultiBufferSource multiBufferSource, Vec3 vec3, Quaternionf quaternionf, boolean bl, boolean bl2, boolean bl3, int n, int n2, float f) {
        return ArcanePainter.scaled(effect, string, d, poseStack, multiBufferSource, vec3, quaternionf, bl, bl2, bl3, n, n2, f, new Vec3(1.0, 1.0, 1.0));
    }

    public static int scaled(ArcaneRig.Effect effect, String string, double d, PoseStack poseStack, MultiBufferSource multiBufferSource, Vec3 vec3, Quaternionf quaternionf, boolean bl3, boolean bl4, boolean bl5, int n, int n3, float f, Vec3 vec32) {
        double[] dArray;
        AABB aABB;
        boolean bl6 = effect.id().equals("qizai_invulnerable_aegis");
        if (bl5) {
            n = Math.min(n, ArcaneEntityBudget.available(bl6));
        }
        if (n <= 0) {
            return 0;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        Vec3 vec33 = minecraft.f_91063_.m_109153_().m_90583_();
        double[] dArray2 = new double[]{vec33.f_82479_, vec33.f_82480_, vec33.f_82481_};
        AABB aABB2 = aABB = minecraft.f_91074_ == null ? null : minecraft.f_91074_.m_20191_().m_82400_(0.1);
        if (aABB == null) {
            dArray = null;
        } else {
            double[] dArray3 = new double[6];
            dArray3[0] = aABB.f_82288_;
            dArray3[1] = aABB.f_82289_;
            dArray3[2] = aABB.f_82290_;
            dArray3[3] = aABB.f_82291_;
            dArray3[4] = aABB.f_82292_;
            dArray = dArray3;
            dArray3[5] = aABB.f_82293_;
        }
        double[] dArray7 = dArray;
        VertexConsumer vertexConsumer = multiBufferSource.m_6299_(RenderType.m_110470_((ResourceLocation)new ResourceLocation(effect.texture())));
        PoseStack.Pose pose = poseStack.m_85850_();
        Vector3f vector3f = new Vector3f();
        double[][] dArray8 = new double[4][3];
        int n4 = ArcaneRig.emit(effect, string, d, bl3, bl4, bl5, n, (string2, n2, dArray4, dArray5, dArray6, f2, bl, bl2) -> {
            int n3;
            if (effect.id().equals("qizai_star_volley") && string2.startsWith("offline_stream_")) {
                return;
            }
            if (string.equals("release") && effect.id().equals("star_cross") && string2.startsWith("orthogonal_arcane_cut_")) {
                return;
            }
            for (n3 = 0; n3 < 4; ++n3) {
                vector3f.set((float)(dArray4[n3][0] * vec3.f_82479_), (float)(dArray4[n3][1] * vec3.f_82480_), (float)(dArray4[n3][2] * vec3.f_82481_)).rotate((Quaternionfc)quaternionf);
                dArray[n3][0] = vec32.f_82479_ + (double)vector3f.x;
                dArray[n3][1] = vec32.f_82480_ + (double)vector3f.y;
                dArray[n3][2] = vec32.f_82481_ + (double)vector3f.z;
            }
            n3 = Math.round(255.0f * f2 * f * ArcaneSafety.fade(dArray8, dArray2, dArray7, bl));
            if (n3 < 2) {
                return;
            }
            for (int i = 0; i < 4; ++i) {
                vertexConsumer.m_252986_(pose.m_252922_(), (float)dArray4[i][0], (float)dArray4[i][1], (float)dArray4[i][2]).m_6122_(255, 255, 255, n3).m_7421_((float)dArray5[i][0], (float)dArray5[i][1]).m_86008_(OverlayTexture.f_118083_).m_85969_(bl || bl2 ? 0xF000F0 : n3).m_252939_(pose.m_252943_(), (float)dArray6[0], (float)dArray6[1], (float)dArray6[2]).m_5752_();
            }
        });
        if (bl5) {
            ArcaneEntityBudget.consume(n4, bl6);
        }
        return n4;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int beam(ArcaneRig.Effect effect, Vec3 vec3, Vec3 vec32, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, float f) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null) {
            return 0;
        }
        Vec3 vec33 = minecraft.f_91073_.m_45547_(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)minecraft.f_91074_)).m_82450_();
        Vec3 vec34 = vec33.m_82546_(vec3);
        double d = vec34.m_82553_();
        if (d < 0.04) {
            return 0;
        }
        Quaternionf quaternionf = new Quaternionf().rotationTo((Vector3fc)new Vector3f(0.0f, 0.0f, 1.0f), (Vector3fc)new Vector3f((float)vec34.f_82479_, (float)vec34.f_82480_, (float)vec34.f_82481_).normalize());
        ArcaneRig.Effect effect2 = ArcaneBeam.mesh(effect, d);
        poseStack.m_85836_();
        try {
            poseStack.m_252781_(quaternionf);
            int n2 = ArcanePainter.effect(effect2, "idle", 0.0, poseStack, multiBufferSource, vec3, quaternionf, false, false, true, n, 0xF000F0, f);
            return n2;
        }
        finally {
            poseStack.m_85849_();
        }
    }
}

