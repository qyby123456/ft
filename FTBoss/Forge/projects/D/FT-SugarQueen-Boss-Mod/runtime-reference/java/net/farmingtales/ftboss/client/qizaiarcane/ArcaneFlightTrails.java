/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.client.qizaiarcane;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneEntityBudget;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneRig;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneSafety;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneTrail;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class ArcaneFlightTrails {
    private static final Map<UUID, ArcaneTrail> trails = new LinkedHashMap<UUID, ArcaneTrail>();
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "qizai_arcane/qizai_star_volley.png");
    private static ClientLevel scene;

    private ArcaneFlightTrails() {
    }

    public static void capture(Entity entity, float f) {
        UUID uUID2;
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !entity.m_6084_() || entity.m_213877_()) {
            return;
        }
        if (scene != clientLevel) {
            trails.clear();
            scene = clientLevel;
        }
        if (!trails.containsKey(uUID2 = entity.m_20148_()) && trails.size() >= 96) {
            trails.remove(trails.keySet().iterator().next());
        }
        Vec3 vec3 = entity.m_20318_(f);
        trails.computeIfAbsent(uUID2, uUID -> new ArcaneTrail()).offer((float)clientLevel.m_46467_() + f, new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_});
    }

    @SubscribeEvent
    public static void unloaded(LevelEvent.Unload unload) {
        if (unload.getLevel() == scene) {
            trails.clear();
            scene = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void draw(RenderLevelStageEvent renderLevelStageEvent) {
        boolean bl;
        double[] dArray;
        AABB aABB;
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ != scene || scene == null) {
            trails.clear();
            scene = minecraft.f_91073_;
            return;
        }
        double d = (float)scene.m_46467_() + renderLevelStageEvent.getPartialTick();
        trails.values().removeIf(arcaneTrail -> arcaneTrail.expired(d));
        if (trails.isEmpty()) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        double[] dArray2 = new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
        AABB aABB2 = aABB = minecraft.f_91074_ == null ? null : minecraft.f_91074_.m_20191_().m_82400_(0.08);
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
        double[] dArray4 = dArray;
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        boolean bl2 = bl = particleStatus == ParticleStatus.ALL;
        int n = particleStatus == ParticleStatus.MINIMAL ? 900 : (particleStatus == ParticleStatus.DECREASED ? 2000 : 4200);
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        RenderType renderType = RenderType.m_110470_((ResourceLocation)TEXTURE);
        VertexConsumer vertexConsumer = bufferSource.m_6299_(renderType);
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        poseStack.m_85836_();
        try {
            poseStack.m_85837_(-vec3.f_82479_, -vec3.f_82480_, -vec3.f_82481_);
            PoseStack.Pose pose = poseStack.m_85850_();
            for (ArcaneTrail arcaneTrail2 : trails.values()) {
                if (n < 6) break;
                if (ArcaneEntityBudget.available(false) < 6) {
                    break;
                }
                List<ArcaneTrail.Strip> list = arcaneTrail2.strips(d, bl);
                for (int i = 0; i < list.size() && n >= 6 && ArcaneEntityBudget.available(false) >= 6; i += 6) {
                    n -= ArcaneEntityBudget.take(6, renderLevelStageEvent.getPartialTick());
                    for (int j = 0; j < 6; ++j) {
                        int n2;
                        ArcaneTrail.Strip strip = list.get(i + j);
                        double[] dArray5 = strip.points()[0];
                        double[] dArray6 = new double[]{dArray5[0] - dArray2[0], dArray5[1] - dArray2[1], dArray5[2] - dArray2[2]};
                        if (ArcaneRig.dot(dArray6, dArray6) > 4096.0 || (n2 = (int)Math.round(255.0 * strip.opacity() * (double)ArcaneSafety.fade(strip.points(), dArray2, dArray4, false))) < 2) continue;
                        int n3 = strip.tile();
                        double d2 = (double)(n3 % 4 * 16 + 2) / 64.0;
                        double d3 = (double)(n3 / 4 * 16 + 2) / 64.0;
                        double d4 = d2 + 0.171875;
                        double d5 = d3 + 0.171875;
                        double[][] dArrayArray = new double[][]{{d2, d5}, {d4, d5}, {d4, d3}, {d2, d3}};
                        for (int k = 0; k < 4; ++k) {
                            double[] dArray7 = strip.points()[k];
                            vertexConsumer.m_252986_(pose.m_252922_(), (float)dArray7[0], (float)dArray7[1], (float)dArray7[2]).m_6122_(255, 255, 255, n2).m_7421_((float)dArrayArray[k][0], (float)dArrayArray[k][1]).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(pose.m_252943_(), (float)strip.normal()[0], (float)strip.normal()[1], (float)strip.normal()[2]).m_5752_();
                        }
                    }
                }
            }
        }
        finally {
            poseStack.m_85849_();
        }
        bufferSource.m_109912_(renderType);
    }
}

