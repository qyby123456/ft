/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.client.render.ZheergenSpellGeometry;
import net.farmingtales.ftboss.client.render.ZheergenSpellMotion;
import net.farmingtales.ftboss.client.render.ZheergenSpellOcclusion;
import net.farmingtales.ftboss.client.render.ZheergenSpellSelection;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
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

public final class ZheergenSpellRenderer {
    private static final RenderType MATERIALS = RenderType.m_110473_((ResourceLocation)new ResourceLocation("ftboss", "fx/zheergen_materials.png"));
    private static final RenderType GHOSTS = RenderType.m_234338_((ResourceLocation)new ResourceLocation("ftboss", "fx/zheergen_materials.png"));

    private ZheergenSpellRenderer() {
    }

    public static List<?> dispatch(List<?> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || list.isEmpty()) {
            return list;
        }
        ArrayList arrayList = new ArrayList();
        ArrayList<OptimizedSpellVisual> arrayList2 = new ArrayList<OptimizedSpellVisual>();
        for (Object obj : list) {
            OptimizedSpellVisual optimizedSpellVisual;
            if (obj instanceof OptimizedSpellVisual && (optimizedSpellVisual = (OptimizedSpellVisual)obj).packet() != null && optimizedSpellVisual.packet().spell() != null && ZheergenSpellGeometry.IDS.contains(optimizedSpellVisual.packet().spell().id)) {
                arrayList2.add(optimizedSpellVisual);
                continue;
            }
            arrayList.add(obj);
        }
        if (!arrayList2.isEmpty()) {
            ZheergenSpellRenderer.render(arrayList2, renderLevelStageEvent, clientLevel);
        }
        return arrayList;
    }

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    private static float fade(double d, int n) {
        return (float)(1.0 - ZheergenSpellRenderer.clamp((d - (double)n * 0.65) / ((double)n * 0.35)));
    }

    private static void render(List<OptimizedSpellVisual> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel) {
        Object object;
        AABB aABB;
        AbstractClientPlayer abstractClientPlayer22;
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ != clientLevel) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        float f = minecraft.m_91104_() ? 0.0f : renderLevelStageEvent.getPartialTick();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n = particleStatus == ParticleStatus.MINIMAL ? 6000 : (particleStatus == ParticleStatus.DECREASED ? 12000 : 24000);
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        VertexConsumer vertexConsumer = bufferSource.m_6299_(MATERIALS);
        VertexConsumer vertexConsumer2 = bufferSource.m_6299_(GHOSTS);
        Matrix4f matrix4f = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
        Matrix3f matrix3f = renderLevelStageEvent.getPoseStack().m_85850_().m_252943_();
        ArrayList<double[]> arrayList = new ArrayList<double[]>();
        for (AbstractClientPlayer abstractClientPlayer22 : clientLevel.m_6907_()) {
            if (!(abstractClientPlayer22.m_20182_().m_82557_(vec3) < 1024.0)) continue;
            aABB = abstractClientPlayer22.m_20191_().m_82400_(0.08);
            arrayList.add(new double[]{aABB.f_82288_, aABB.f_82289_, aABB.f_82290_, aABB.f_82291_, aABB.f_82292_, aABB.f_82293_});
        }
        Object object2 = new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
        abstractClientPlayer22 = renderLevelStageEvent.getCamera().m_90592_();
        aABB = abstractClientPlayer22 == null ? new Vec3(0.0, 0.0, 1.0) : abstractClientPlayer22.m_20252_(f);
        double[] dArray = new double[]{aABB.f_82479_, aABB.f_82480_, aABB.f_82481_};
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        List<OptimizedSpellVisual> list2 = list.stream().filter(optimizedSpellVisual -> optimizedSpellVisual.packet().valid() && clientLevel.m_46472_().m_135782_().equals((Object)optimizedSpellVisual.packet().dimension())).toList();
        ArrayList<ZheergenSpellSelection.Candidate> arrayList2 = new ArrayList<ZheergenSpellSelection.Candidate>();
        for (int i = 0; i < list2.size(); ++i) {
            OptimizedSpellVisual optimizedSpellVisual2 = list2.get(i);
            SpellVisualPacket object3 = optimizedSpellVisual2.packet();
            object = object3.spell().id + "/" + String.valueOf(object3.owner()) + "/" + Math.round(object3.start().f_82479_ * 32.0) + "/" + Math.round(object3.start().f_82480_ * 32.0) + "/" + Math.round(object3.start().f_82481_ * 32.0);
            if (object3.phase() == SpellVisualRules.Phase.BEAM) {
                object = (String)object + "/beam/" + String.valueOf(object3.end());
            }
            arrayList2.add(new ZheergenSpellSelection.Candidate((String)object, object3.phase().name(), optimizedSpellVisual2.refreshed(), i));
        }
        List<OptimizedSpellVisual> list3 = ZheergenSpellSelection.choose(arrayList2).stream().map(list2::get).sorted(Comparator.comparingDouble(optimizedSpellVisual -> optimizedSpellVisual.packet().start().m_82557_(vec3))).toList();
        for (OptimizedSpellVisual optimizedSpellVisual2 : list3) {
            int n2;
            object = optimizedSpellVisual2.packet();
            boolean bl2 = ((SpellVisualPacket)object).phase() == SpellVisualRules.Phase.CHARGE;
            double d = Math.max(0.0f, (float)(clientLevel.m_46467_() - (bl2 ? optimizedSpellVisual2.born() : optimizedSpellVisual2.refreshed())) + f);
            float f2 = ZheergenSpellRenderer.fade(d, ((SpellVisualPacket)object).spell().lifetime(((SpellVisualPacket)object).phase())) * (bl2 ? 0.48f : 0.86f);
            if (f2 <= 0.0f) continue;
            Entity entity = clientLevel.m_6815_(((SpellVisualPacket)object).ownerId());
            if (entity != null && !entity.m_20148_().equals(((SpellVisualPacket)object).owner())) {
                entity = null;
            }
            if (((SpellVisualPacket)object).phase() == SpellVisualRules.Phase.BEAM) {
                ZheergenSpellRenderer.beam(vertexConsumer, matrix4f, matrix3f, (SpellVisualPacket)object, clientLevel, entity, vec3, f2);
                continue;
            }
            ZheergenSpellGeometry.Model model = ZheergenSpellGeometry.find(((SpellVisualPacket)object).spell().id);
            if (model == null) continue;
            double d2 = SpellVisuals.radiusScale(entity, ((SpellVisualPacket)object).spell().id);
            if (!Double.isFinite(d2) || d2 <= 0.0) {
                d2 = 1.0;
            }
            double d3 = model.scale() * Math.max(0.85, Math.min(1.45, Math.sqrt(d2)));
            double d4 = Math.max(0.1 - model.minimum()[1] * d3, model.height() - (model.minimum()[1] + model.maximum()[1]) * 0.5 * d3);
            Vec3 vec32 = ((SpellVisualPacket)object).start();
            double d5 = ((SpellVisualPacket)object).end().f_82479_ - vec32.f_82479_;
            double d6 = ((SpellVisualPacket)object).end().f_82481_ - vec32.f_82481_;
            double d7 = Math.hypot(d5, d6);
            if (d7 < 1.0E-5) {
                d5 = 0.0;
                d6 = 1.0;
            } else {
                d5 /= d7;
                d6 /= d7;
            }
            double d8 = Math.hypot(Math.max(Math.abs(model.minimum()[0]), Math.abs(model.maximum()[0])), Math.max(Math.abs(model.minimum()[2]), Math.abs(model.maximum()[2]))) * d3 + 0.05;
            AABB aABB2 = new AABB(vec32.f_82479_ - d8, vec32.f_82480_ - 0.2, vec32.f_82481_ - d8, vec32.f_82479_ + d8, vec32.f_82480_ + d4 + model.maximum()[1] * d3 + 0.1, vec32.f_82481_ + d8);
            if (vec32.m_82557_(vec3) > 4096.0 || !renderLevelStageEvent.getFrustum().m_113029_(aABB2)) continue;
            ZheergenSpellRenderer.markers(vertexConsumer, matrix4f, matrix3f, vec32, d5, d6, ((SpellVisualPacket)object).spell().id, ((SpellVisualPacket)object).spell().radius * d2, vec3, f2);
            boolean bl3 = particleStatus == ParticleStatus.ALL && vec32.m_82557_(vec3) < 196.0;
            int n3 = n2 = bl3 ? model.faces().size() : model.bodyFaces();
            if (n2 > n) continue;
            n -= n2;
            ZheergenSpellMotion.State[] stateArray = new ZheergenSpellMotion.State[45];
            for (int i = 0; i < stateArray.length; ++i) {
                stateArray[i] = ZheergenSpellMotion.state(i, d, bl2);
            }
            double[][] dArray2 = new double[4][3];
            double[] dArray3 = new double[3];
            for (ZheergenSpellGeometry.Face face : model.faces()) {
                if (face.detail() && !bl3) continue;
                for (int i = 0; i < 4; ++i) {
                    ZheergenSpellMotion.apply(face.points()[i], face.pivot(), stateArray[face.bone()], dArray3);
                    dArray2[i][0] = vec32.f_82479_ + (dArray3[0] * d6 + dArray3[2] * d5) * d3;
                    dArray2[i][1] = vec32.f_82480_ + d4 + dArray3[1] * d3;
                    dArray2[i][2] = vec32.f_82481_ + (-dArray3[0] * d5 + dArray3[2] * d6) * d3;
                }
                float f3 = f2 * face.opacity() * ZheergenSpellOcclusion.camera(dArray2, (double[])object2);
                for (double[] dArray4 : arrayList) {
                    f3 = Math.min(f3, f2 * face.opacity() * ZheergenSpellOcclusion.actor(dArray2, dArray4));
                }
                if (bl) {
                    f3 *= ZheergenSpellOcclusion.sight(dArray2, (double[])object2, dArray);
                }
                if (face.bone() >= 34) {
                    f3 = (float)((double)f3 * (0.4 + 0.6 * Math.sin(ZheergenSpellRenderer.clamp(d / 12.0) * Math.PI)));
                }
                if (!((double)f3 > 0.008)) continue;
                ZheergenSpellRenderer.emit((double)face.opacity() >= 0.95 ? vertexConsumer : vertexConsumer2, matrix4f, matrix3f, dArray2, face.uv(), face.color(), f3, vec3);
            }
        }
        bufferSource.m_109912_(MATERIALS);
        bufferSource.m_109912_(GHOSTS);
    }

    private static void emit(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, double[][] dArray, double[][] dArray2, int n, float f, Vec3 vec3) {
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
        if (d10 < 1.0E-10) {
            return;
        }
        int n2 = Math.max(0, Math.min(255, Math.round(f * 255.0f)));
        for (int i = 0; i < 4; ++i) {
            vertexConsumer.m_252986_(matrix4f, (float)(dArray[i][0] - vec3.f_82479_), (float)(dArray[i][1] - vec3.f_82480_), (float)(dArray[i][2] - vec3.f_82481_)).m_6122_(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, n2).m_7421_((float)dArray2[i][0], (float)dArray2[i][1]).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(matrix3f, (float)(d5 / d10), (float)(d8 / d10), (float)(d9 / d10)).m_5752_();
        }
    }

    private static double[][] scarUv() {
        return new double[][]{{0.767578125, 0.767578125}, {0.982421875, 0.767578125}, {0.982421875, 0.982421875}, {0.767578125, 0.982421875}};
    }

    private static void markers(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, Vec3 vec3, double d, double d2, String string, double d3, Vec3 vec32, float f) {
        int n = string.equals("shadow_brand") ? 3 : 4;
        for (int i = 0; i < n; ++i) {
            double d4 = (double)i * Math.PI * 2.0 / (double)n + (string.equals("nightfall_blades") ? 0.7853981633974483 : 0.19);
            double d5 = Math.cos(d4) * d3;
            double d6 = Math.sin(d4) * d3;
            double d7 = 0.17;
            double[][] dArrayArray = new double[][]{{d5 - d7, 0.025, d6 - 0.026}, {d5 + d7, 0.025, d6 + 0.026}, {d5 + d7, 0.025, d6 + 0.052}, {d5 - d7, 0.025, d6 + 0.001}};
            double[][] dArrayArray2 = new double[4][];
            for (int j = 0; j < 4; ++j) {
                dArrayArray2[j] = new double[]{vec3.f_82479_ + dArrayArray[j][0] * d2 + dArrayArray[j][2] * d, vec3.f_82480_ + dArrayArray[j][1], vec3.f_82481_ - dArrayArray[j][0] * d + dArrayArray[j][2] * d2};
            }
            ZheergenSpellRenderer.emit(vertexConsumer, matrix4f, matrix3f, dArrayArray2, ZheergenSpellRenderer.scarUv(), 13672946, f * 0.66f, vec32);
        }
    }

    private static void beam(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, SpellVisualPacket spellVisualPacket, ClientLevel clientLevel, Entity entity, Vec3 vec3, float f) {
        double d;
        double d2;
        double d3;
        double d4;
        Vec3 vec32 = spellVisualPacket.end();
        BlockHitResult blockHitResult = clientLevel.m_45547_(new ClipContext(spellVisualPacket.start(), vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        if (blockHitResult.m_6662_() != HitResult.Type.MISS) {
            vec32 = blockHitResult.m_82450_();
        }
        if ((d4 = Math.sqrt((d3 = vec32.f_82479_ - spellVisualPacket.start().f_82479_) * d3 + (d2 = vec32.f_82480_ - spellVisualPacket.start().f_82480_) * d2 + (d = vec32.f_82481_ - spellVisualPacket.start().f_82481_) * d)) < 0.02 || d4 > 64.0) {
            return;
        }
        double d5 = Math.hypot(d3, d);
        double d6 = d5 < 1.0E-4 ? 1.0 : -d / d5;
        double d7 = d5 < 1.0E-4 ? 0.0 : d3 / d5;
        for (int i = 0; i < 2; ++i) {
            double d8 = i == 0 ? 0.036 : 0.015;
            double d9 = i == 0 ? 0.0 : 0.045;
            double[][] dArrayArray = new double[][]{{spellVisualPacket.start().f_82479_ + d6 * d8, spellVisualPacket.start().f_82480_ + d9, spellVisualPacket.start().f_82481_ + d7 * d8}, {vec32.f_82479_ + d6 * d8, vec32.f_82480_ + d9, vec32.f_82481_ + d7 * d8}, {vec32.f_82479_ - d6 * d8, vec32.f_82480_ + d9, vec32.f_82481_ - d7 * d8}, {spellVisualPacket.start().f_82479_ - d6 * d8, spellVisualPacket.start().f_82480_ + d9, spellVisualPacket.start().f_82481_ - d7 * d8}};
            ZheergenSpellRenderer.emit(vertexConsumer, matrix4f, matrix3f, dArrayArray, ZheergenSpellRenderer.scarUv(), i == 0 ? 8807321 : 15054335, f * (i == 0 ? 0.45f : 0.8f), vec3);
        }
    }
}

