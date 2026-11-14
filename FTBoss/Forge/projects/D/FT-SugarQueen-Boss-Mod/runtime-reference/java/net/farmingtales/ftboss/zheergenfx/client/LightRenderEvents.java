/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package net.farmingtales.ftboss.zheergenfx.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.entity.ShadowPhantomEntity;
import net.farmingtales.ftboss.zheergenfx.LightClock;
import net.farmingtales.ftboss.zheergenfx.LightPacket;
import net.farmingtales.ftboss.zheergenfx.client.LightBank;
import net.farmingtales.ftboss.zheergenfx.client.LightClient;
import net.farmingtales.ftboss.zheergenfx.client.LightSafety;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class LightRenderEvents {
    private static final RenderType LIGHT = RenderType.m_234338_((ResourceLocation)new ResourceLocation("ftboss", "fx/zheergen_remaining_lights.png"));
    private static final Map<String, Double> TERRAIN = new HashMap<String, Double>();
    private static final Map<String, Boolean> OCCLUDED = new HashMap<String, Boolean>();
    private static ClientLevel terrainWorld;
    private static long terrainEpoch;
    private static ClientLevel actorWorld;
    private static long actorTick;
    private static List<LivingEntity> nearbyActors;

    private LightRenderEvents() {
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase == TickEvent.Phase.END) {
            ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
            LightClient.world(clientLevel);
            if (actorWorld != clientLevel) {
                actorWorld = clientLevel;
                actorTick = Long.MIN_VALUE;
                nearbyActors = List.of();
            }
            if (terrainWorld != clientLevel) {
                terrainWorld = clientLevel;
                terrainEpoch = -1L;
                TERRAIN.clear();
                OCCLUDED.clear();
            }
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        Matrix3f matrix3f;
        LightBank.Shape shape;
        Object object;
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null) {
            actorWorld = null;
            nearbyActors = List.of();
            actorTick = Long.MIN_VALUE;
            return;
        }
        if (clientLevel != terrainWorld || clientLevel.m_46467_() / 4L != terrainEpoch) {
            TERRAIN.clear();
            OCCLUDED.clear();
            terrainWorld = clientLevel;
            terrainEpoch = clientLevel.m_46467_() / 4L;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        double[] dArray = new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
        float f = minecraft.m_91104_() ? 0.0f : renderLevelStageEvent.getPartialTick();
        Vec3 vec32 = renderLevelStageEvent.getCamera().m_90592_() == null ? new Vec3(0.0, 0.0, 1.0) : renderLevelStageEvent.getCamera().m_90592_().m_20252_(f);
        double[] dArray2 = new double[]{vec32.f_82479_, vec32.f_82480_, vec32.f_82481_};
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n = particleStatus == ParticleStatus.MINIMAL ? 1800 : (particleStatus == ParticleStatus.DECREASED ? 3600 : 7200);
        List<LightPacket> list = LightClient.snapshot(clientLevel);
        ArrayList<double[]> arrayList = new ArrayList<double[]>();
        if (minecraft.f_91074_ != null) {
            object = minecraft.f_91074_.m_20191_().m_82400_(0.08);
            arrayList.add(new double[]{((AABB)object).f_82288_, ((AABB)object).f_82289_, ((AABB)object).f_82290_, ((AABB)object).f_82291_, ((AABB)object).f_82292_, ((AABB)object).f_82293_});
            if (actorWorld != clientLevel || actorTick != clientLevel.m_46467_()) {
                actorWorld = clientLevel;
                actorTick = clientLevel.m_46467_();
                nearbyActors = clientLevel.m_45976_(LivingEntity.class, minecraft.f_91074_.m_20191_().m_82400_(24.0)).stream().limit(256L).toList();
            }
            for (LivingEntity lightPacket22 : nearbyActors) {
                if (lightPacket22 != minecraft.f_91074_ && arrayList.size() < 16 && lightPacket22.m_6084_() && lightPacket22.m_20182_().m_82557_(vec3) < 144.0) {
                    shape = lightPacket22.m_20191_().m_82400_(0.06);
                    arrayList.add(new double[]{((AABB)shape).f_82288_, ((AABB)shape).f_82289_, ((AABB)shape).f_82290_, ((AABB)shape).f_82291_, ((AABB)shape).f_82292_, ((AABB)shape).f_82293_});
                }
                if ((shape = lightPacket22.m_21124_((MobEffect)FTEffects.SHADOW_EROSION.get())) != null && lightPacket22.m_6084_()) {
                    list.add(new LightPacket(10, lightPacket22.m_19879_(), lightPacket22.m_20148_(), clientLevel.m_46472_().m_135782_(), clientLevel.m_46467_() - (long)Math.floorMod(clientLevel.m_46467_(), 20), lightPacket22.m_20182_(), new Vec3(0.0, 0.0, 1.0), 1.0, 20, Math.min(5, shape.m_19564_() + 1)));
                }
                if (list.size() < 96) continue;
                break;
            }
        }
        list.sort(Comparator.comparingDouble(lightPacket -> lightPacket.origin().m_82557_(vec3)));
        object = new ArrayList();
        for (LightPacket lightPacket2 : list) {
            int n2;
            shape = LightBank.shape(lightPacket2.kind(), lightPacket2.index());
            if (shape == null || lightPacket2.origin().m_82557_(vec3) > 2304.0 || (matrix3f = clientLevel.m_6815_(lightPacket2.ownerId())) != null && !matrix3f.m_20148_().equals(lightPacket2.owner()) || LightClock.charge(lightPacket2.kind()) && (matrix3f == null || !matrix3f.m_6084_()) || lightPacket2.kind() == 8 && clientLevel.m_45976_(ShadowPhantomEntity.class, new AABB(lightPacket2.origin().f_82479_ - 0.4, lightPacket2.origin().f_82480_ - 0.2, lightPacket2.origin().f_82481_ - 0.4, lightPacket2.origin().f_82479_ + 0.4, lightPacket2.origin().f_82480_ + 2.2, lightPacket2.origin().f_82481_ + 0.4)).isEmpty() || (n2 = shape.parts().stream().filter(part -> !part.detail() || particleStatus == ParticleStatus.ALL).mapToInt(part -> part.points()[0].length).sum()) > n) continue;
            object.add(lightPacket2);
            n -= n2;
        }
        Collections.reverse(object);
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        VertexConsumer vertexConsumer = bufferSource.m_6299_(LIGHT);
        shape = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
        matrix3f = renderLevelStageEvent.getPoseStack().m_85850_().m_252943_();
        double[][] dArray3 = new double[4][3];
        double[] dArray4 = new double[3];
        Iterator iterator = object.iterator();
        while (iterator.hasNext()) {
            LightPacket lightPacket3 = (LightPacket)iterator.next();
            double d = (float)clientLevel.m_46467_() + f - (float)lightPacket3.born();
            if (!LightClock.alive(d, lightPacket3.duration())) continue;
            double d2 = LightClock.progress(d, lightPacket3.duration());
            LightBank.Shape shape2 = LightBank.shape(lightPacket3.kind(), lightPacket3.index());
            double d3 = lightPacket3.direction().f_82479_;
            double d4 = lightPacket3.direction().f_82481_;
            double d5 = Math.hypot(d3, d4);
            if (d5 > 1.0E-6) {
                d3 /= d5;
                d4 /= d5;
            } else {
                d3 = 0.0;
                d4 = 1.0;
            }
            double d6 = shape2.rangeAnchored() ? lightPacket3.radius() : 1.0;
            for (LightBank.Part part2 : shape2.parts()) {
                float f2;
                if (part2.detail() && (particleStatus != ParticleStatus.ALL || lightPacket3.origin().m_82557_(vec3) > 225.0) || (f2 = LightBank.alpha(part2, d2)) <= 0.008f) continue;
                for (int i = 0; i < part2.points()[0].length; ++i) {
                    for (int j = 0; j < 4; ++j) {
                        LightBank.point(part2, d2, i, j, dArray4);
                        dArray3[j][0] = lightPacket3.origin().f_82479_ + (dArray4[0] * d4 + dArray4[2] * d3) * d6;
                        dArray3[j][1] = lightPacket3.origin().f_82480_ + dArray4[1];
                        dArray3[j][2] = lightPacket3.origin().f_82481_ + (dArray4[2] * d4 - dArray4[0] * d3) * d6;
                    }
                    if (part2.ground()) {
                        double d7;
                        var39_38 = new Vec3((dArray3[0][0] + dArray3[2][0]) * 0.5, lightPacket3.origin().f_82480_, (dArray3[0][2] + dArray3[2][2]) * 0.5);
                        var40_40 = Math.round(var39_38.f_82479_ * 32.0) + "/" + Math.round(var39_38.f_82480_ * 32.0) + "/" + Math.round(var39_38.f_82481_ * 32.0);
                        if (TERRAIN.size() > 4096) {
                            TERRAIN.clear();
                        }
                        if (!Double.isFinite(d7 = TERRAIN.computeIfAbsent((String)var40_40, string -> {
                            BlockHitResult blockHitResult = clientLevel.m_45547_(new ClipContext(var39_35.m_82520_(0.0, 1.5, 0.0), var39_35.m_82520_(0.0, -4.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)minecraft.f_91074_));
                            return blockHitResult.m_6662_() == HitResult.Type.MISS ? Double.NaN : blockHitResult.m_82450_().f_82480_;
                        }).doubleValue())) continue;
                        double d8 = d7 - lightPacket3.origin().f_82480_;
                        for (double[] dArray5 : dArray3) {
                            dArray5[1] = dArray5[1] + d8;
                        }
                    } else {
                        boolean bl2;
                        var39_38 = new Vec3((dArray3[0][0] + dArray3[2][0]) * 0.5, (dArray3[0][1] + dArray3[2][1]) * 0.5, (dArray3[0][2] + dArray3[2][2]) * 0.5);
                        var40_40 = String.valueOf(lightPacket3.owner()) + "/" + lightPacket3.born() + "/" + Math.round(var39_38.f_82479_ * 16.0) + "/" + Math.round(var39_38.f_82480_ * 16.0) + "/" + Math.round(var39_38.f_82481_ * 16.0);
                        if (OCCLUDED.size() > 4096) {
                            OCCLUDED.clear();
                        }
                        if (bl2 = OCCLUDED.computeIfAbsent((String)var40_40, string -> clientLevel.m_45547_(new ClipContext(lightPacket3.origin().m_82520_(0.0, 1.0, 0.0), var39_35, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)minecraft.f_91074_)).m_6662_() != HitResult.Type.MISS).booleanValue()) continue;
                    }
                    float f3 = f2 * LightSafety.camera(dArray3, dArray);
                    for (double[] dArray6 : arrayList) {
                        f3 *= LightSafety.actor(dArray3, dArray6);
                    }
                    if (f3 <= 0.008f) continue;
                    LightRenderEvents.quad(vertexConsumer, (Matrix4f)shape, matrix3f, dArray3, part2.tint(), f3, dArray, dArray2, bl);
                }
            }
        }
        bufferSource.m_109912_(LIGHT);
    }

    private static void quad(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, double[][] dArray, int n, float f, double[] dArray2, double[] dArray3, boolean bl) {
        double d = (dArray[1][1] - dArray[0][1]) * (dArray[2][2] - dArray[0][2]) - (dArray[1][2] - dArray[0][2]) * (dArray[2][1] - dArray[0][1]);
        double d2 = (dArray[1][2] - dArray[0][2]) * (dArray[2][0] - dArray[0][0]) - (dArray[1][0] - dArray[0][0]) * (dArray[2][2] - dArray[0][2]);
        double d3 = (dArray[1][0] - dArray[0][0]) * (dArray[2][1] - dArray[0][1]) - (dArray[1][1] - dArray[0][1]) * (dArray[2][0] - dArray[0][0]);
        double d4 = Math.max(1.0E-10, Math.sqrt(d * d + d2 * d2 + d3 * d3));
        for (int i = 0; i < 4; ++i) {
            float f2 = f * (bl ? LightSafety.sightVertex(dArray[i], dArray2, dArray3) : 1.0f);
            vertexConsumer.m_252986_(matrix4f, (float)(dArray[i][0] - dArray2[0]), (float)(dArray[i][1] - dArray2[1]), (float)(dArray[i][2] - dArray2[2])).m_6122_(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, Math.round(f2 * 255.0f)).m_7421_(i == 0 || i == 3 ? 0.05f : 0.95f, i < 2 ? 0.95f : 0.05f).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(matrix3f, (float)(d / d4), (float)(d2 / d4), (float)(d3 / d4)).m_5752_();
        }
    }

    static {
        terrainEpoch = -1L;
        actorTick = Long.MIN_VALUE;
        nearbyActors = List.of();
    }
}

