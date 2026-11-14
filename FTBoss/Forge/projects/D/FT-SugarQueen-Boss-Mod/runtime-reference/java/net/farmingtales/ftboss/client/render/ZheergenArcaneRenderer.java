/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.client.render.ZheergenArcaneData;
import net.farmingtales.ftboss.client.render.ZheergenArcaneMotion;
import net.farmingtales.ftboss.client.render.ZheergenArcanePhases;
import net.farmingtales.ftboss.client.render.ZheergenArcanePlayback;
import net.farmingtales.ftboss.client.render.ZheergenArcaneSafety;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.zheergenrevision.VisualScale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class ZheergenArcaneRenderer {
    private static final ResourceLocation ATLAS = new ResourceLocation("ftboss", "fx/zheergen_arcane_light.png");
    private static final RenderType VOLUME = RenderType.m_110473_((ResourceLocation)ATLAS);
    private static final RenderType LIGHT = RenderType.m_234338_((ResourceLocation)ATLAS);
    private static final double[][] TRACE_UV = new double[][]{{0.517578125, 0.017578125}, {0.732421875, 0.017578125}, {0.732421875, 0.232421875}, {0.517578125, 0.232421875}};

    private ZheergenArcaneRenderer() {
    }

    public static List<?> dispatch(List<?> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || list.isEmpty()) {
            return list;
        }
        ArrayList arrayList = new ArrayList();
        LinkedHashMap<CallSite, OptimizedSpellVisual> linkedHashMap = new LinkedHashMap<CallSite, OptimizedSpellVisual>();
        for (Object obj : list) {
            OptimizedSpellVisual optimizedSpellVisual;
            OptimizedSpellVisual optimizedSpellVisual2;
            if (!(obj instanceof OptimizedSpellVisual) || (optimizedSpellVisual2 = (OptimizedSpellVisual)obj).packet() == null || optimizedSpellVisual2.packet().spell() == null || !ZheergenArcaneData.SPELLS.contains(optimizedSpellVisual2.packet().spell().id)) {
                arrayList.add(obj);
                continue;
            }
            SpellVisualPacket spellVisualPacket = optimizedSpellVisual2.packet();
            if (!spellVisualPacket.valid() || !clientLevel.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension())) continue;
            double d = Math.max(0L, clientLevel.m_46467_() - (spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE ? optimizedSpellVisual2.born() : optimizedSpellVisual2.refreshed()));
            if (!ZheergenArcanePlayback.alive(d, spellVisualPacket.spell().lifetime(spellVisualPacket.phase()))) continue;
            String string = Math.round(spellVisualPacket.start().f_82479_ * 32.0) + "/" + Math.round(spellVisualPacket.start().f_82480_ * 32.0) + "/" + Math.round(spellVisualPacket.start().f_82481_ * 32.0);
            String string2 = spellVisualPacket.spell().id + "/" + String.valueOf(spellVisualPacket.owner()) + "/" + string;
            if (spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM) {
                string2 = string2 + "/beam/" + String.valueOf(spellVisualPacket.end());
            }
            if ((optimizedSpellVisual = (OptimizedSpellVisual)linkedHashMap.get(string2)) != null && !ZheergenArcanePhases.prefer(spellVisualPacket.phase().name(), optimizedSpellVisual2.refreshed(), optimizedSpellVisual.packet().phase().name(), optimizedSpellVisual.refreshed())) continue;
            linkedHashMap.put((CallSite)((Object)string2), optimizedSpellVisual2);
        }
        if (!linkedHashMap.isEmpty()) {
            ZheergenArcaneRenderer.draw(new ArrayList<OptimizedSpellVisual>(linkedHashMap.values()), renderLevelStageEvent, clientLevel);
        }
        return arrayList;
    }

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    private static void draw(List<OptimizedSpellVisual> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel) {
        int n;
        Object object;
        SpellVisualPacket spellVisualPacket;
        Object object322;
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ != clientLevel) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        double[] dArray = new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
        float f = minecraft.m_91104_() ? 0.0f : renderLevelStageEvent.getPartialTick();
        Vec3 vec32 = renderLevelStageEvent.getCamera().m_90592_() == null ? new Vec3(0.0, 0.0, 1.0) : renderLevelStageEvent.getCamera().m_90592_().m_20252_(f);
        double[] dArray2 = new double[]{vec32.f_82479_, vec32.f_82480_, vec32.f_82481_};
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n2 = particleStatus == ParticleStatus.MINIMAL ? 4000 : (particleStatus == ParticleStatus.DECREASED ? 8000 : 16000);
        ArrayList<double[]> arrayList = new ArrayList<double[]>();
        for (Object object322 : clientLevel.m_6907_()) {
            if (object322.m_20182_().m_82557_(vec3) > 900.0) continue;
            AABB object22 = object322.m_20191_().m_82400_(0.08);
            arrayList.add(new double[]{object22.f_82288_, object22.f_82289_, object22.f_82290_, object22.f_82291_, object22.f_82292_, object22.f_82293_});
        }
        list.sort(Comparator.comparingDouble(optimizedSpellVisual -> optimizedSpellVisual.packet().start().m_82557_(vec3)));
        ArrayList arrayList2 = new ArrayList();
        for (OptimizedSpellVisual optimizedSpellVisual2 : list) {
            boolean bl2;
            spellVisualPacket = optimizedSpellVisual2.packet();
            boolean bl3 = spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE;
            double d = Math.max(0.0f, (float)(clientLevel.m_46467_() - (bl3 ? optimizedSpellVisual2.born() : optimizedSpellVisual2.refreshed())) + f);
            if (d >= (double)spellVisualPacket.spell().lifetime(spellVisualPacket.phase()) || spellVisualPacket.start().m_82557_(vec3) > 3600.0) continue;
            object = ZheergenArcaneData.shape(spellVisualPacket.spell().id);
            boolean bl4 = bl2 = particleStatus == ParticleStatus.ALL && spellVisualPacket.start().m_82557_(vec3) < 225.0;
            n = spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM ? 6 : (bl2 ? ((ZheergenArcaneData.Shape)object).surfaces().size() : ((ZheergenArcaneData.Shape)object).essential()) + (bl3 ? 12 : 0);
            if (n > n2) continue;
            arrayList2.add(optimizedSpellVisual2);
            n2 -= n;
        }
        Collections.reverse(arrayList2);
        object322 = minecraft.m_91269_().m_110104_();
        VertexConsumer vertexConsumer = object322.m_6299_(VOLUME);
        spellVisualPacket = object322.m_6299_(LIGHT);
        Matrix4f matrix4f = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
        Matrix3f matrix3f = renderLevelStageEvent.getPoseStack().m_85850_().m_252943_();
        Iterator iterator = arrayList2.iterator();
        while (iterator.hasNext()) {
            int n3;
            object = (OptimizedSpellVisual)iterator.next();
            SpellVisualPacket spellVisualPacket2 = object.packet();
            n = spellVisualPacket2.phase() == SpellVisualRules.Phase.CHARGE ? 1 : 0;
            double d = Math.max(0.0f, (float)(clientLevel.m_46467_() - (n != 0 ? object.born() : object.refreshed())) + f);
            if (!ZheergenArcanePlayback.alive(d, n3 = spellVisualPacket2.spell().lifetime(spellVisualPacket2.phase()))) continue;
            float f2 = ZheergenArcanePlayback.envelope(d, n3);
            Entity entity = clientLevel.m_6815_(spellVisualPacket2.ownerId());
            if (entity != null && !entity.m_20148_().equals(spellVisualPacket2.owner())) {
                entity = null;
            }
            if (spellVisualPacket2.phase() == SpellVisualRules.Phase.BEAM) {
                ZheergenArcaneRenderer.trace((VertexConsumer)spellVisualPacket, matrix4f, matrix3f, spellVisualPacket2.start(), spellVisualPacket2.end(), clientLevel, entity, f2, dArray, dArray2, bl, arrayList);
                continue;
            }
            ZheergenArcaneData.Shape shape = ZheergenArcaneData.shape(spellVisualPacket2.spell().id);
            Vec3 vec33 = spellVisualPacket2.start();
            if (shape == null || vec33.m_82557_(vec3) > 3600.0) continue;
            double d2 = SpellVisuals.radiusScale(entity, spellVisualPacket2.spell().id);
            if (!Double.isFinite(d2) || d2 <= 0.0) {
                d2 = 1.0;
            }
            double d3 = shape.scale() * VisualScale.scale(d2);
            double d4 = ZheergenArcanePlayback.elevation(shape.height(), shape.lower()[1], shape.upper()[1], d3);
            double d5 = spellVisualPacket2.end().f_82479_ - vec33.f_82479_;
            double d6 = spellVisualPacket2.end().f_82481_ - vec33.f_82481_;
            double d7 = Math.hypot(d5, d6);
            if (d7 > 1.0E-5) {
                d5 /= d7;
                d6 /= d7;
            } else {
                d5 = 0.0;
                d6 = 1.0;
            }
            double d8 = Math.hypot(Math.max(Math.abs(shape.lower()[0]), Math.abs(shape.upper()[0])), Math.max(Math.abs(shape.lower()[2]), Math.abs(shape.upper()[2]))) * d3;
            AABB aABB = new AABB(vec33.f_82479_ - d8, vec33.f_82480_ + d4 + shape.lower()[1] * d3, vec33.f_82481_ - d8, vec33.f_82479_ + d8, vec33.f_82480_ + d4 + shape.upper()[1] * d3, vec33.f_82481_ + d8);
            if (!renderLevelStageEvent.getFrustum().m_113029_(aABB)) continue;
            if (n != 0) {
                ZheergenArcaneRenderer.warning((VertexConsumer)spellVisualPacket, matrix4f, matrix3f, vec33, clientLevel, entity, spellVisualPacket2.spell().radius * d2, f2, dArray);
            }
            boolean bl5 = particleStatus == ParticleStatus.ALL && vec33.m_82557_(vec3) < 225.0;
            ZheergenArcaneMotion.Frame[] frameArray = new ZheergenArcaneMotion.Frame[45];
            for (int i = 20; i <= 44; ++i) {
                frameArray[i] = ZheergenArcaneMotion.frame(i, d, n != 0);
            }
            double[] dArray3 = new double[45];
            for (int i = 33; i <= 37; ++i) {
                dArray3[i] = ZheergenArcaneMotion.frame(i, Math.max(0.0, d - 1.8), n != 0).alpha();
            }
            double[][] dArray4 = new double[4][3];
            double[] dArray5 = new double[3];
            float[] fArray = new float[4];
            for (ZheergenArcaneData.Surface surface : shape.surfaces()) {
                if (surface.detail() && !bl5) continue;
                ZheergenArcaneMotion.Frame frame = frameArray[surface.channel()];
                for (int i = 0; i < 4; ++i) {
                    ZheergenArcaneMotion.position(surface.positions()[i], surface.pivot(), frame, dArray5);
                    dArray4[i][0] = vec33.f_82479_ + (dArray5[0] * d6 + dArray5[2] * d5) * d3;
                    dArray4[i][1] = vec33.f_82480_ + d4 + dArray5[1] * d3;
                    dArray4[i][2] = vec33.f_82481_ + (dArray5[2] * d6 - dArray5[0] * d5) * d3;
                }
                double d9 = surface.alphaLag() > 0.0 && n == 0 ? dArray3[surface.channel()] : frame.alpha();
                float f3 = f2 * surface.opacity() * (float)d9 * ZheergenArcaneRenderer.protection(dArray4, dArray, dArray2, false, arrayList);
                for (int i = 0; i < 4; ++i) {
                    fArray[i] = f3 * (bl ? ZheergenArcaneSafety.sightVertex(dArray4[i], dArray, dArray2) : 1.0f);
                }
                if (!(f3 > 0.012f)) continue;
                ZheergenArcaneRenderer.quad((VertexConsumer)(surface.opacity() >= 0.95f ? vertexConsumer : spellVisualPacket), matrix4f, matrix3f, dArray4, surface.uv(), surface.tint(), fArray, dArray);
            }
        }
        object322.m_109912_(VOLUME);
        object322.m_109912_(LIGHT);
    }

    private static float protection(double[][] dArray, double[] dArray2, double[] dArray3, boolean bl, List<double[]> list) {
        float f = 1.0f;
        for (double[] dArray4 : list) {
            f = Math.min(f, ZheergenArcaneSafety.actor(dArray, dArray4));
        }
        return f * ZheergenArcaneSafety.camera(dArray, dArray2) * (bl ? ZheergenArcaneSafety.sight(dArray, dArray2, dArray3) : 1.0f);
    }

    private static void quad(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, double[][] dArray, double[][] dArray2, int n, float f, double[] dArray3) {
        ZheergenArcaneRenderer.quad(vertexConsumer, matrix4f, matrix3f, dArray, dArray2, n, new float[]{f, f, f, f}, dArray3);
    }

    private static void quad(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, double[][] dArray, double[][] dArray2, int n, float[] fArray, double[] dArray3) {
        double d = dArray[1][1] - dArray[0][1];
        double d2 = dArray[2][2] - dArray[0][2];
        double d3 = dArray[1][2] - dArray[0][2];
        double d4 = dArray[2][1] - dArray[0][1];
        double d5 = d * d2 - d3 * d4;
        double d6 = dArray[2][0] - dArray[0][0];
        double d7 = dArray[1][0] - dArray[0][0];
        double d8 = d3 * d6 - d7 * d2;
        double d9 = d7 * d4 - d * d6;
        double d10 = Math.sqrt(d5 * d5 + d8 * d8 + d9 * d9);
        if (d10 < 1.0E-12) {
            return;
        }
        for (int i = 0; i < 4; ++i) {
            int n2 = Math.min(255, Math.max(0, Math.round(fArray[i] * 255.0f)));
            vertexConsumer.m_252986_(matrix4f, (float)(dArray[i][0] - dArray3[0]), (float)(dArray[i][1] - dArray3[1]), (float)(dArray[i][2] - dArray3[2])).m_6122_(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, n2).m_7421_((float)dArray2[i][0], (float)dArray2[i][1]).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(matrix3f, (float)(d5 / d10), (float)(d8 / d10), (float)(d9 / d10)).m_5752_();
        }
    }

    private static void trace(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, Vec3 vec3, Vec3 vec32, ClientLevel clientLevel, Entity entity, float f, double[] dArray, double[] dArray2, boolean bl, List<double[]> list) {
        double[] dArray3;
        BlockHitResult blockHitResult = clientLevel.m_45547_(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        Vec3 vec33 = blockHitResult.m_6662_() == HitResult.Type.MISS ? vec32 : blockHitResult.m_82450_();
        double[] dArray4 = new double[]{vec33.f_82479_ - vec3.f_82479_, vec33.f_82480_ - vec3.f_82480_, vec33.f_82481_ - vec3.f_82481_};
        double d = Math.sqrt(dArray4[0] * dArray4[0] + dArray4[1] * dArray4[1] + dArray4[2] * dArray4[2]);
        if (d < 0.02 || d > 64.0) {
            return;
        }
        int n = 0;
        while (n < 3) {
            int n2 = n++;
            dArray4[n2] = dArray4[n2] / d;
        }
        double d2 = Math.hypot(dArray4[0], dArray4[2]);
        if (d2 > 1.0E-4) {
            double[] dArray5 = new double[3];
            dArray5[0] = -dArray4[2] / d2;
            dArray5[1] = 0.0;
            dArray3 = dArray5;
            dArray5[2] = dArray4[0] / d2;
        } else {
            double[] dArray6 = new double[3];
            dArray6[0] = 1.0;
            dArray6[1] = 0.0;
            dArray3 = dArray6;
            dArray6[2] = 0.0;
        }
        double[] dArray7 = dArray3;
        double[] dArray8 = new double[]{dArray4[1] * dArray7[2] - dArray4[2] * dArray7[1], dArray4[2] * dArray7[0] - dArray4[0] * dArray7[2], dArray4[0] * dArray7[1] - dArray4[1] * dArray7[0]};
        double[][] dArray9 = new double[8][3];
        for (int i = 0; i < 8; ++i) {
            Vec3 vec34 = i < 4 ? vec3 : vec33;
            double[] dArray10 = new double[]{vec34.f_82479_, vec34.f_82480_, vec34.f_82481_};
            for (int j = 0; j < 3; ++j) {
                dArray9[i][j] = dArray10[j] + dArray7[j] * (i % 4 < 2 ? -0.018 : 0.018) + dArray8[j] * (i % 4 == 0 || i % 4 == 3 ? -0.014 : 0.014);
            }
        }
        for (int[] nArray : new int[][]{{0, 1, 2, 3}, {7, 6, 5, 4}, {0, 4, 5, 1}, {1, 5, 6, 2}, {2, 6, 7, 3}, {3, 7, 4, 0}}) {
            double[][] dArrayArray = new double[][]{dArray9[nArray[0]], dArray9[nArray[1]], dArray9[nArray[2]], dArray9[nArray[3]]};
            ZheergenArcaneRenderer.quad(vertexConsumer, matrix4f, matrix3f, (double[][])dArrayArray, TRACE_UV, 14995967, f * 0.66f * ZheergenArcaneRenderer.protection(dArrayArray, dArray, dArray2, bl, list), dArray);
        }
    }

    private static void warning(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, Vec3 vec3, ClientLevel clientLevel, Entity entity, double d, float f, double[] dArray) {
        if (!Double.isFinite(d) || d <= 0.0) {
            return;
        }
        double d2 = Math.min(16.0, d);
        Vec3 vec32 = new Vec3(vec3.f_82479_, vec3.f_82480_ + 0.25, vec3.f_82481_);
        Vec3 vec33 = new Vec3(vec3.f_82479_, vec3.f_82480_ - 4.0, vec3.f_82481_);
        BlockHitResult blockHitResult = clientLevel.m_45547_(new ClipContext(vec32, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        if (blockHitResult.m_6662_() == HitResult.Type.MISS) {
            return;
        }
        double d3 = blockHitResult.m_82450_().f_82480_ + 0.018;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 4; ++j) {
                double d4 = (double)i * Math.PI * 2.0 / 3.0 + (double)j * 0.055;
                double d5 = d4 + 0.055;
                double[][] dArrayArray = new double[][]{{vec3.f_82479_ + Math.cos(d4) * d2, d3, vec3.f_82481_ + Math.sin(d4) * d2}, {vec3.f_82479_ + Math.cos(d4) * (d2 - 0.028), d3, vec3.f_82481_ + Math.sin(d4) * (d2 - 0.028)}, {vec3.f_82479_ + Math.cos(d5) * (d2 - 0.028), d3, vec3.f_82481_ + Math.sin(d5) * (d2 - 0.028)}, {vec3.f_82479_ + Math.cos(d5) * d2, d3, vec3.f_82481_ + Math.sin(d5) * d2}};
                ZheergenArcaneRenderer.quad(vertexConsumer, matrix4f, matrix3f, (double[][])dArrayArray, TRACE_UV, 11837903, f * 0.44f, dArray);
            }
        }
    }
}

