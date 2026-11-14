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
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.client.lemoncat;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.client.lemoncat.LemoncatArtAssets;
import net.farmingtales.ftboss.client.lemoncat.LemoncatArtGuard;
import net.farmingtales.ftboss.client.lemoncat.LemoncatArtMotion;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class LemoncatArtRenderer {
    private static final RenderType LAYER = RenderType.m_234338_((ResourceLocation)new ResourceLocation("ftboss", "lemoncat_v3/atlas.png"));

    private LemoncatArtRenderer() {
    }

    public static boolean handles(String string) {
        return string.equals("searing_pan") || string.equals("hearth_feast") || string.equals("citrus_nova");
    }

    public static void renderIndependent(List<?> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel) {
        LemoncatArtRenderer.renderIndependent(list, renderLevelStageEvent, clientLevel, 1.0, 1.0);
    }

    public static void renderIndependent(List<?> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel, double d, double d2) {
        LivingEntity livingEntity;
        OptimizedSpellVisual optimizedSpellVisual22;
        Object object2;
        if (clientLevel == null || renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        if (!Double.isFinite(d) || !Double.isFinite(d2)) {
            return;
        }
        d2 = Math.max(0.5, Math.min(3.0, d2));
        Minecraft minecraft = Minecraft.m_91087_();
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        Vector3f vector3f = renderLevelStageEvent.getCamera().m_253058_();
        double[] dArray = new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
        double[] dArray2 = new double[]{vector3f.x, vector3f.y, vector3f.z};
        boolean bl = minecraft.f_91066_.m_92176_().m_90612_();
        ArrayList<OptimizedSpellVisual> arrayList = new ArrayList<OptimizedSpellVisual>();
        for (Object object2 : list) {
            if (!(object2 instanceof OptimizedSpellVisual)) continue;
            optimizedSpellVisual22 = (OptimizedSpellVisual)object2;
            if (!LemoncatArtRenderer.handles(optimizedSpellVisual22.packet().spell().id)) continue;
            arrayList.add(optimizedSpellVisual22);
        }
        if (arrayList.isEmpty()) {
            return;
        }
        arrayList.sort(Comparator.comparingDouble(optimizedSpellVisual -> optimizedSpellVisual.packet().start().m_82557_(vec3)));
        ArrayList arrayList2 = new ArrayList();
        for (OptimizedSpellVisual optimizedSpellVisual22 : clientLevel.m_104735_()) {
            if (!(optimizedSpellVisual22 instanceof LivingEntity)) continue;
            livingEntity = (LivingEntity)optimizedSpellVisual22;
            if (!(optimizedSpellVisual22.m_20238_(vec3) < 10000.0)) continue;
            arrayList2.add(livingEntity);
        }
        object2 = renderLevelStageEvent.getPoseStack().m_85850_();
        optimizedSpellVisual22 = object2.m_252922_();
        livingEntity = object2.m_252943_();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        VertexConsumer vertexConsumer = bufferSource.m_6299_(LAYER);
        int n = minecraft.f_91066_.m_231929_().m_231551_() == ParticleStatus.ALL ? 8192 : 4096;
        int n2 = 0;
        for (OptimizedSpellVisual optimizedSpellVisual3 : arrayList) {
            Object object3;
            Object object4;
            Vec3 vec32;
            int n3;
            SpellVisualPacket spellVisualPacket = optimizedSpellVisual3.packet();
            if (!spellVisualPacket.dimension().equals((Object)clientLevel.m_46472_().m_135782_())) continue;
            double d3 = (double)Math.max(0.0f, (float)(clientLevel.m_46467_() - optimizedSpellVisual3.born()) + renderLevelStageEvent.getPartialTick()) * d2;
            double d4 = (double)Math.max(0.0f, (float)(clientLevel.m_46467_() - optimizedSpellVisual3.refreshed()) + renderLevelStageEvent.getPartialTick()) * d2;
            if (d4 >= (double)(n3 = spellVisualPacket.spell().lifetime(spellVisualPacket.phase()))) continue;
            LemoncatArtAssets.Sculpture sculpture = LemoncatArtAssets.find(spellVisualPacket.spell().id);
            Entity entity = clientLevel.m_6815_(spellVisualPacket.ownerId());
            if (entity != null && !entity.m_20148_().equals(spellVisualPacket.owner())) {
                entity = null;
            }
            if ((vec32 = sculpture.anchor().equals("FRONT") && entity != null ? entity.m_20318_(renderLevelStageEvent.getPartialTick()) : spellVisualPacket.start()).m_82557_(vec3) > 6400.0) continue;
            if (n2++ >= 12) break;
            if (n < sculpture.faces().size()) continue;
            boolean bl2 = spellVisualPacket.phase() != SpellVisualRules.Phase.CHARGE;
            Vec3 vec33 = spellVisualPacket.end().m_82546_(spellVisualPacket.start());
            double d5 = Math.hypot(vec33.f_82479_, vec33.f_82481_);
            if (d5 < 1.0E-6 && entity != null) {
                vec33 = entity.m_20252_(renderLevelStageEvent.getPartialTick());
                d5 = Math.hypot(vec33.f_82479_, vec33.f_82481_);
            }
            double d6 = d5 < 1.0E-6 ? 0.0 : vec33.f_82479_ / d5;
            double d7 = d5 < 1.0E-6 ? 1.0 : vec33.f_82481_ / d5;
            double[] dArray3 = LemoncatArtMotion.layout(sculpture, spellVisualPacket.spell().radius, entity == null ? 0.6 : (double)entity.m_20205_(), d);
            double d8 = dArray3[0];
            float f = (float)Math.min(1.0, ((double)n3 - d4) / 5.0);
            ArrayList<double[][]> arrayList3 = new ArrayList<double[][]>();
            Object object5 = arrayList2.iterator();
            while (object5.hasNext()) {
                object4 = (LivingEntity)object5.next();
                if (!(object4.m_20238_(vec32) < 100.0)) continue;
                object3 = object4.m_20191_();
                arrayList3.add(new double[][]{{object3.f_82288_, object3.f_82289_, object3.f_82290_}, {object3.f_82291_, object3.f_82292_, object3.f_82293_}});
            }
            if (!bl2 && sculpture.anchor().equals("TARGET")) {
                LemoncatArtRenderer.markers(vertexConsumer, (Matrix4f)optimizedSpellVisual22, (Matrix3f)livingEntity, vec3, vec32, spellVisualPacket.spell().radius * Math.max(0.5, Math.min(3.0, d)), f, sculpture.id().equals("hearth_feast"));
            }
            object5 = new double[243][];
            object4 = new double[4][3];
            object3 = new double[3];
            for (LemoncatArtAssets.Surface surface : sculpture.faces()) {
                float f2;
                if (bl && renderLevelStageEvent.getCamera().m_90592_() == entity && surface.bone() == 201 || (f2 = f * surface.opacity() * LemoncatArtMotion.opacity(sculpture, surface.bone(), d3, bl2)) < 0.015f) continue;
                if (object5[surface.bone()] == null) {
                    object5[surface.bone()] = LemoncatArtMotion.pose(surface.bone(), d3, bl2);
                }
                for (int i = 0; i < 4; ++i) {
                    LemoncatArtMotion.move(surface.points()[i], surface.pivot(), (double[])object5[surface.bone()], object3);
                    double d9 = object3[0] * d8 + dArray3[1];
                    double d10 = object3[2] * d8 + dArray3[3];
                    object4[i][0] = (LivingEntity)(vec32.f_82479_ + d9 * d7 + d10 * d6);
                    object4[i][1] = (LivingEntity)(vec32.f_82480_ + object3[1] * d8 + dArray3[2]);
                    object4[i][2] = (LivingEntity)(vec32.f_82481_ - d9 * d6 + d10 * d7);
                }
                if ((f2 *= LemoncatArtGuard.alpha((double[][])object4, dArray, dArray2, bl, arrayList3)) < 0.015f) continue;
                LemoncatArtRenderer.surface(vertexConsumer, (Matrix4f)optimizedSpellVisual22, (Matrix3f)livingEntity, vec3, (double[][])object4, surface.uv(), f2, 255, 255, 255);
            }
            n -= sculpture.faces().size();
        }
        bufferSource.m_109912_(LAYER);
    }

    private static void markers(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, Vec3 vec3, Vec3 vec32, double d, float f, boolean bl) {
        double[][] dArrayArray = new double[][]{{0.52, 0.02}, {0.56, 0.02}, {0.56, 0.06}, {0.52, 0.06}};
        for (int i = 0; i < 8; ++i) {
            double d2 = (double)i * Math.PI / 4.0;
            double d3 = vec32.f_82479_ + Math.cos(d2) * d;
            double d4 = vec32.f_82481_ + Math.sin(d2) * d;
            double d5 = 0.035;
            double[][] dArrayArray2 = new double[][]{{d3 - d5, vec32.f_82480_ + 0.03, d4}, {d3, vec32.f_82480_ + 0.03, d4 + d5}, {d3 + d5, vec32.f_82480_ + 0.03, d4}, {d3, vec32.f_82480_ + 0.03, d4 - d5}};
            LemoncatArtRenderer.surface(vertexConsumer, matrix4f, matrix3f, vec3, dArrayArray2, dArrayArray, f * 0.45f, bl ? 172 : 255, bl ? 225 : 191, bl ? 132 : 75);
        }
    }

    private static void surface(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, Vec3 vec3, double[][] dArray, double[][] dArray2, float f, int n, int n2, int n3) {
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
        for (int i = 0; i < 4; ++i) {
            vertexConsumer.m_252986_(matrix4f, (float)(dArray[i][0] - vec3.f_82479_), (float)(dArray[i][1] - vec3.f_82480_), (float)(dArray[i][2] - vec3.f_82481_)).m_6122_(n, n2, n3, Math.round(f * 255.0f)).m_7421_((float)dArray2[i][0], (float)dArray2[i][1]).m_86008_(655360).m_85969_(0xF000F0).m_252939_(matrix3f, (float)(d5 / d10), (float)(d8 / d10), (float)(d9 / d10)).m_5752_();
        }
    }
}

