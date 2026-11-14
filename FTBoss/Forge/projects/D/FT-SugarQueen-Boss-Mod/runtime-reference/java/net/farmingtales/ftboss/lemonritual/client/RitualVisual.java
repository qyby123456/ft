/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.lemonritual.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.lemonritual.RitualSummonPacket;
import net.farmingtales.ftboss.lemonritual.RitualTimeline;
import net.farmingtales.ftboss.lemonritual.client.RitualAssets;
import net.farmingtales.ftboss.lemonritual.client.RitualMath;
import net.farmingtales.ftboss.skill.LemonSacrificePacket;
import net.farmingtales.ftboss.util.LemonSacrificeGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class RitualVisual {
    private static final RitualTimeline TIMELINE = new RitualTimeline();
    private static final RenderType LAYER = RenderType.m_234338_((ResourceLocation)new ResourceLocation("ftboss", "lemon_ritual/atlas.png"));

    private RitualVisual() {
    }

    public static void receive(LemonSacrificePacket lemonSacrificePacket) {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !lemonSacrificePacket.valid()) {
            return;
        }
        double[] dArray = RitualVisual.point(lemonSacrificePacket.center());
        ArrayList<double[]> arrayList = new ArrayList<double[]>();
        for (LemonSacrificeGeometry.Point point : lemonSacrificePacket.souls()) {
            arrayList.add(new double[]{dArray[0] + point.horizontal(), dArray[1] + point.height(), dArray[2] + point.depth()});
        }
        TIMELINE.accept(new RitualTimeline.Cue(true, lemonSacrificePacket.owner(), null, lemonSacrificePacket.dimension().toString(), dArray, List.copyOf(arrayList), lemonSacrificePacket.started(), 0.6, 1.8), RitualVisual.dimension(clientLevel), clientLevel.m_46467_());
    }

    public static void receiveSummon(RitualSummonPacket ritualSummonPacket) {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel != null && ritualSummonPacket.valid()) {
            TIMELINE.accept(ritualSummonPacket.cue(), RitualVisual.dimension(clientLevel), clientLevel.m_46467_());
        }
    }

    private static String dimension(ClientLevel clientLevel) {
        return clientLevel.m_46472_().m_135782_().toString();
    }

    private static double[] point(Vec3 vec3) {
        return new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null) {
            TIMELINE.clear();
        } else {
            TIMELINE.update(RitualVisual.dimension(clientLevel), clientLevel.m_46467_());
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel().m_5776_()) {
            TIMELINE.clear();
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        TIMELINE.clear();
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        VertexConsumer vertexConsumer;
        Entity entity22;
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null || renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        TIMELINE.update(RitualVisual.dimension(clientLevel), clientLevel.m_46467_());
        ArrayList<RitualTimeline.Cue> arrayList = new ArrayList<RitualTimeline.Cue>(TIMELINE.active());
        if (arrayList.isEmpty()) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        double[] dArray = RitualVisual.point(vec3);
        Vector3f vector3f = renderLevelStageEvent.getCamera().m_253058_();
        double[] dArray2 = new double[]{vector3f.x, vector3f.y, vector3f.z};
        boolean bl = minecraft.f_91066_.m_92176_().m_90612_();
        HashMap<UUID, Entity> hashMap = new HashMap<UUID, Entity>();
        ArrayList<double[][]> arrayList2 = new ArrayList<double[][]>();
        for (Entity entity22 : clientLevel.m_104735_()) {
            hashMap.put(entity22.m_20148_(), entity22);
            if (!(entity22 instanceof LivingEntity) || !(entity22.m_20238_(vec3) < 10000.0)) continue;
            vertexConsumer = entity22.m_20191_();
            arrayList2.add(new double[][]{{vertexConsumer.f_82288_, vertexConsumer.f_82289_, vertexConsumer.f_82290_}, {vertexConsumer.f_82291_, vertexConsumer.f_82292_, vertexConsumer.f_82293_}});
        }
        arrayList.sort(Comparator.comparingDouble(cue -> RitualVisual.distance(cue.anchor(), dArray)));
        PoseStack.Pose pose = renderLevelStageEvent.getPoseStack().m_85850_();
        entity22 = minecraft.m_91269_().m_110104_();
        vertexConsumer = entity22.m_6299_(LAYER);
        Painter painter = new Painter(vertexConsumer, pose.m_252922_(), pose.m_252943_(), vec3, dArray, dArray2, bl, arrayList2, minecraft.f_91066_.m_231929_().m_231551_() == ParticleStatus.ALL ? 10000 : 5000);
        int n = 0;
        for (RitualTimeline.Cue cue2 : arrayList) {
            int d11;
            Object object;
            double[] dArray3;
            double d = (float)(clientLevel.m_46467_() - cue2.started()) + renderLevelStageEvent.getPartialTick();
            if (d < 0.0 || d >= (double)cue2.life() || RitualVisual.distance(cue2.anchor(), dArray) > 9216.0) continue;
            Entity entity3 = (Entity)hashMap.get(cue2.owner());
            Entity entity4 = (Entity)hashMap.get(cue2.subject());
            if (entity3 != null && !entity3.m_6084_() || !cue2.absorb() && entity4 != null && !entity4.m_6084_()) {
                TIMELINE.remove(cue2);
                continue;
            }
            if ((entity3 == null || !cue2.absorb() && entity4 == null) && d > 10.0) {
                TIMELINE.remove(cue2);
                continue;
            }
            if (++n > 12 || painter.remaining <= 0) break;
            double[] dArray4 = dArray3 = cue2.absorb() && entity3 != null ? RitualVisual.point(entity3.m_20318_(renderLevelStageEvent.getPartialTick())) : cue2.anchor();
            if (cue2.absorb() && RitualVisual.distance(dArray3, cue2.anchor()) > 256.0) {
                TIMELINE.remove(cue2);
                continue;
            }
            double d2 = cue2.absorb() && entity3 != null ? (double)entity3.m_20205_() : cue2.width();
            double d3 = RitualMath.clamp(d2 * 0.5 + 0.28, 0.65, 3.6);
            RitualAssets.Model model = RitualAssets.model(cue2.absorb());
            float f = RitualMath.opacity(model.life(), d);
            if (f <= 0.0f) continue;
            double[][] dArrayArray = new double[model.bones().size()][];
            double[][] dArray5 = new double[4][3];
            for (RitualAssets.Bone record : model.bones()) {
                dArrayArray[record.id()] = RitualMath.pose(model, record, d);
            }
            for (RitualAssets.Face d10 : model.faces()) {
                if (painter.remaining <= 0) break;
                object = model.bones().get(d10.bone());
                for (d11 = 0; d11 < 4; ++d11) {
                    RitualMath.moveInto(d10.points()[d11], ((RitualAssets.Bone)object).pivot(), dArrayArray[((RitualAssets.Bone)object).id()], dArray5[d11]);
                    dArray5[d11][0] = dArray5[d11][0] * d3 + dArray3[0];
                    dArray5[d11][1] = dArray5[d11][1] * Math.min(d3, 1.5) + dArray3[1];
                    dArray5[d11][2] = dArray5[d11][2] * d3 + dArray3[2];
                }
                painter.face(dArray5, d10.uv(), f * d10.opacity());
            }
            if (cue2.absorb()) {
                double d4 = entity3 == null ? 1.1 : RitualMath.clamp((double)entity3.m_20206_() * 0.55, 0.65, 2.4);
                object = new double[]{dArray3[0], dArray3[1] + d4, dArray3[2]};
                for (d11 = 0; d11 < cue2.sources().size(); ++d11) {
                    double d5;
                    int n2;
                    double d6 = RitualMath.clamp((d - 2.0 - (double)d11 * 0.12) / 28.0, 0.0, 1.0);
                    double d7 = Math.max(0.0, d6 - 0.32);
                    float f2 = f * (float)RitualMath.clamp((38.0 - d) / 8.0, 0.0, 1.0);
                    for (n2 = 0; n2 < 8; ++n2) {
                        d5 = (d6 - d7) * 0.005;
                        double d8 = d7 + (d6 - d7) * (double)n2 / 8.0 + d5;
                        double d9 = d7 + (d6 - d7) * (double)(n2 + 1) / 8.0 - d5;
                        painter.beam(RitualMath.stream(cue2.sources().get(d11), (double[])object, d8, d11), RitualMath.stream(cue2.sources().get(d11), (double[])object, d9, d11), 0.013, f2 * 0.48f, 5);
                    }
                    for (n2 = 0; n2 < 3; ++n2) {
                        d5 = d6 - (double)n2 * 0.055;
                        if (d5 <= 0.0 || d5 >= 1.0) continue;
                        painter.seed(RitualMath.stream(cue2.sources().get(d11), (double[])object, d5, d11), 0.037 - (double)n2 * 0.006, f2 * 0.86f, n2 == 0 ? 3 : 2);
                    }
                }
                continue;
            }
            for (int i = 0; i < 6; ++i) {
                double d10 = (double)i * Math.PI / 3.0 + d * 0.035;
                double d12 = Math.sin(RitualMath.clamp(d / 26.0, 0.0, 1.0) * Math.PI) * Math.min(1.25, cue2.height() * 0.5);
                painter.seed(new double[]{dArray3[0] + Math.sin(d10) * d3 * 1.04, dArray3[1] + 0.18 + d12, dArray3[2] + Math.cos(d10) * d3 * 1.04}, 0.045, f * 0.8f, 3);
            }
        }
        entity22.m_109912_(LAYER);
    }

    private static double distance(double[] dArray, double[] dArray2) {
        double d = 0.0;
        for (int i = 0; i < 3; ++i) {
            d += Math.pow(dArray[i] - dArray2[i], 2.0);
        }
        return d;
    }

    private static final class Painter {
        private final VertexConsumer consumer;
        private final Matrix4f matrix;
        private final Matrix3f normal;
        private final Vec3 camera;
        private static final double[][][] UV = Painter.materials();
        private final double[] eye;
        private final double[] look;
        private final boolean first;
        private final List<double[][]> bodies;
        private int remaining;

        private static double[][][] materials() {
            double[][][] dArrayArray = new double[8][][];
            for (int i = 0; i < 8; ++i) {
                double d = (double)(i % 4 * 32 + 5) / 128.0;
                double d2 = (double)(i / 4 * 64 + 5) / 128.0;
                dArrayArray[i] = new double[][]{{d, d2}, {d + 0.15, d2}, {d + 0.15, d2 + 0.4}, {d, d2 + 0.4}};
            }
            return dArrayArray;
        }

        private Painter(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, Vec3 vec3, double[] dArray, double[] dArray2, boolean bl, List<double[][]> list, int n) {
            this.consumer = vertexConsumer;
            this.matrix = matrix4f;
            this.normal = matrix3f;
            this.camera = vec3;
            this.eye = dArray;
            this.look = dArray2;
            this.first = bl;
            this.bodies = list;
            this.remaining = n;
        }

        private void face(double[][] dArray, double[][] dArray2, float f) {
            if (this.remaining <= 0 || f < 0.015f) {
                return;
            }
            if ((f *= RitualMath.guard(dArray, this.eye, this.look, this.first, this.bodies)) < 0.015f) {
                return;
            }
            double d = (dArray[1][1] - dArray[0][1]) * (dArray[2][2] - dArray[0][2]) - (dArray[1][2] - dArray[0][2]) * (dArray[2][1] - dArray[0][1]);
            double d2 = (dArray[1][2] - dArray[0][2]) * (dArray[2][0] - dArray[0][0]) - (dArray[1][0] - dArray[0][0]) * (dArray[2][2] - dArray[0][2]);
            double d3 = (dArray[1][0] - dArray[0][0]) * (dArray[2][1] - dArray[0][1]) - (dArray[1][1] - dArray[0][1]) * (dArray[2][0] - dArray[0][0]);
            double d4 = Math.sqrt(d * d + d2 * d2 + d3 * d3);
            if (d4 < 1.0E-10) {
                return;
            }
            --this.remaining;
            for (int i = 0; i < 4; ++i) {
                this.consumer.m_252986_(this.matrix, (float)(dArray[i][0] - this.camera.f_82479_), (float)(dArray[i][1] - this.camera.f_82480_), (float)(dArray[i][2] - this.camera.f_82481_)).m_6122_(255, 255, 255, Math.round(f * 255.0f)).m_7421_((float)dArray2[i][0], (float)dArray2[i][1]).m_86008_(655360).m_85969_(0xF000F0).m_252939_(this.normal, (float)(d / d4), (float)(d2 / d4), (float)(d3 / d4)).m_5752_();
            }
        }

        private double[][] uv(int n) {
            return UV[n];
        }

        private void beam(double[] dArray, double[] dArray2, double d, float f, int n) {
            int n2;
            int n3;
            double[] dArray3;
            if (this.remaining <= 0 || f < 0.015f) {
                return;
            }
            double[] dArray4 = new double[]{dArray2[0] - dArray[0], dArray2[1] - dArray[1], dArray2[2] - dArray[2]};
            double[] dArray5 = dArray4;
            double d2 = Math.sqrt(dArray5[0] * dArray5[0] + dArray5[1] * dArray5[1] + dArray5[2] * dArray5[2]);
            if (d2 < 1.0E-5) {
                return;
            }
            int n4 = 0;
            while (n4 < 3) {
                int n5 = n4++;
                dArray5[n5] = dArray5[n5] / d2;
            }
            if (Math.abs(dArray5[1]) < 0.9) {
                double[] dArray6 = new double[3];
                dArray6[0] = -dArray5[2];
                dArray6[1] = 0.0;
                dArray3 = dArray6;
                dArray6[2] = dArray5[0];
            } else {
                double[] dArray7 = new double[3];
                dArray7[0] = 0.0;
                dArray7[1] = dArray5[2];
                dArray3 = dArray7;
                dArray7[2] = -dArray5[1];
            }
            double[] dArray8 = dArray3;
            double d3 = Math.sqrt(dArray8[0] * dArray8[0] + dArray8[1] * dArray8[1] + dArray8[2] * dArray8[2]);
            int n6 = 0;
            while (n6 < 3) {
                int n7 = n6++;
                dArray8[n7] = dArray8[n7] * (d / d3);
            }
            double[] dArray9 = new double[]{dArray5[1] * dArray8[2] - dArray5[2] * dArray8[1], dArray5[2] * dArray8[0] - dArray5[0] * dArray8[2], dArray5[0] * dArray8[1] - dArray5[1] * dArray8[0]};
            double[][] dArray10 = new double[8][3];
            for (n3 = 0; n3 < 2; ++n3) {
                for (n2 = 0; n2 < 4; ++n2) {
                    for (int i = 0; i < 3; ++i) {
                        dArray10[n3 * 4 + n2][i] = (n3 == 0 ? dArray : dArray2)[i] + dArray8[i] * Math.cos((double)n2 * Math.PI / 2.0) + dArray9[i] * Math.sin((double)n2 * Math.PI / 2.0);
                    }
                }
            }
            for (n3 = 0; n3 < 4; ++n3) {
                n2 = (n3 + 1) % 4;
                this.face(new double[][]{dArray10[n3], dArray10[n2], dArray10[n2 + 4], dArray10[n3 + 4]}, this.uv(n), f);
            }
            this.face(new double[][]{dArray10[3], dArray10[2], dArray10[1], dArray10[0]}, this.uv(n), f);
            this.face(new double[][]{dArray10[4], dArray10[5], dArray10[6], dArray10[7]}, this.uv(n), f);
        }

        private void seed(double[] dArray, double d, float f, int n) {
            if (this.remaining <= 0 || f < 0.015f) {
                return;
            }
            double[][] dArrayArray = new double[][]{{dArray[0], dArray[1] - d * 1.7, dArray[2]}, {dArray[0] - d, dArray[1], dArray[2]}, {dArray[0], dArray[1], dArray[2] - d}, {dArray[0] + d, dArray[1], dArray[2]}, {dArray[0], dArray[1], dArray[2] + d}, {dArray[0], dArray[1] + d * 1.7, dArray[2]}};
            for (int i = 0; i < 4; ++i) {
                int n2 = (i + 1) % 4 + 1;
                this.face(new double[][]{dArrayArray[0], dArrayArray[i + 1], dArrayArray[n2], dArrayArray[n2]}, this.uv(n), f);
                this.face(new double[][]{dArrayArray[5], dArrayArray[n2], dArrayArray[i + 1], dArrayArray[i + 1]}, this.uv(n), f);
            }
        }
    }
}

