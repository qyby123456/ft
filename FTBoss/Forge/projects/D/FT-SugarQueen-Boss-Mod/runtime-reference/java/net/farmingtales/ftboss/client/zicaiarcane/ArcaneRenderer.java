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
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  org.joml.Matrix4f
 */
package net.farmingtales.ftboss.client.zicaiarcane;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.client.zicaiarcane.ArcaneField;
import net.farmingtales.ftboss.client.zicaiarcane.ArcaneLightRenderType;
import net.farmingtales.ftboss.client.zicaiarcane.ArcaneRig;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public final class ArcaneRenderer {
    private static final RenderType ENERGY = RenderType.m_110458_((ResourceLocation)new ResourceLocation("ftboss", "textures/fx/zicai_arcane/energy.png"));
    private static final List<Batch> BATCHES = new ArrayList<Batch>();
    private static final List<Visible> CACHE = new ArrayList<Visible>();

    public static List<?> consume(List<?> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel) {
        Object object;
        Visible visible2;
        Entity entity;
        Object object222;
        if (list.isEmpty() || renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return list;
        }
        ArrayList arrayList = new ArrayList();
        ArrayList<OptimizedSpellVisual> arrayList2 = new ArrayList<OptimizedSpellVisual>();
        for (Object object222 : list) {
            if (object222 instanceof OptimizedSpellVisual) {
                OptimizedSpellVisual optimizedSpellVisual2 = (OptimizedSpellVisual)object222;
                if (ArcaneField.IDS.contains(optimizedSpellVisual2.packet().spell().id)) {
                    arrayList2.add(optimizedSpellVisual2);
                    continue;
                }
            }
            arrayList.add(object222);
        }
        if (arrayList2.isEmpty()) {
            return list;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (clientLevel == null || minecraft.f_91073_ != clientLevel) {
            return arrayList;
        }
        object222 = renderLevelStageEvent.getCamera().m_90583_();
        float f = minecraft.m_91104_() ? 0.0f : renderLevelStageEvent.getPartialTick();
        Entity entity2 = renderLevelStageEvent.getCamera().m_90592_();
        Vec3 vec3 = entity2 == null ? new Vec3(0.0, 0.0, -1.0) : entity2.m_20252_(f);
        double[] dArray = new double[]{((Vec3)object222).f_82479_, ((Vec3)object222).f_82480_, ((Vec3)object222).f_82481_};
        double[] dArray2 = new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n = particleStatus == ParticleStatus.MINIMAL ? 0 : (particleStatus == ParticleStatus.DECREASED ? 1 : 2);
        int n2 = ArcaneField.visibleFields(n);
        int n3 = ArcaneField.triangleBudget(n);
        arrayList2.sort(Comparator.comparingDouble(optimizedSpellVisual -> optimizedSpellVisual.packet().start().m_82557_(object222)));
        ArrayList<ArcaneRig.Box> arrayList3 = new ArrayList<ArcaneRig.Box>();
        for (AbstractClientPlayer abstractClientPlayer : clientLevel.m_6907_()) {
            if (!(abstractClientPlayer.m_20318_(f).m_82557_(object222) < 1024.0)) continue;
            arrayList3.add(ArcaneRenderer.body(abstractClientPlayer.m_20191_().m_82400_(0.08)));
        }
        ArrayList arrayList4 = new ArrayList();
        int n4 = 0;
        int n5 = 0;
        for (OptimizedSpellVisual optimizedSpellVisual3 : arrayList2) {
            double d;
            double d2;
            int n6;
            int n7;
            if (n4 >= n2) break;
            SpellVisualPacket spellVisualPacket = optimizedSpellVisual3.packet();
            if (!spellVisualPacket.valid() || !spellVisualPacket.dimension().equals((Object)clientLevel.m_46472_().m_135782_())) continue;
            ArcaneField.Model model = ArcaneField.MODELS.get(spellVisualPacket.spell().id);
            entity = clientLevel.m_6815_(spellVisualPacket.ownerId());
            if (entity != null && !entity.m_20148_().equals(spellVisualPacket.owner())) {
                entity = null;
            }
            visible2 = entity == null ? null : entity.m_20318_(f);
            object = ArcaneRig.floorAnchor(model.anchor(), spellVisualPacket.phase().name(), ArcaneRenderer.coordinates(spellVisualPacket.start()), ArcaneRenderer.coordinates(spellVisualPacket.end()), visible2 == null ? null : ArcaneRenderer.coordinates((Vec3)visible2));
            Vec3 vec32 = new Vec3((double)object[0], (double)object[1], (double)object[2]);
            double d3 = SpellVisuals.radiusScale(entity, model.id());
            double d22 = ArcaneRig.spatialScale(d3);
            if (vec32.m_82557_(object222) > 4096.0 || !renderLevelStageEvent.getFrustum().m_113029_(new AABB(vec32, vec32).m_82400_(7.0 * d22 + 1.0))) continue;
            boolean bl2 = particleStatus == ParticleStatus.ALL && vec32.m_82557_(object222) <= 484.0;
            int n8 = 0;
            for (ArcaneField.Triangle triangle : model.triangles()) {
                if (!bl2 && triangle.detail()) continue;
                ++n8;
            }
            if (n5 + n8 > n3) continue;
            boolean bl3 = spellVisualPacket.phase().name().equals("CHARGE");
            int n9 = spellVisualPacket.spell().lifetime(spellVisualPacket.phase());
            double d32 = ArcaneRig.animationAge(model.id(), bl3, (float)(clientLevel.m_46467_() - optimizedSpellVisual3.born()) + f, n9);
            double d4 = Math.max(0.0f, (float)(clientLevel.m_46467_() - optimizedSpellVisual3.refreshed()) + f);
            double d5 = ArcaneRig.persistence(d4, n9);
            if (!bl3 && !model.id().equals("halo_sanctuary")) {
                d5 *= 1.0 - 0.9 * ArcaneRig.ease((d32 - 47.0) / 33.0);
            }
            if (d5 <= 0.005) continue;
            Vec3 vec33 = spellVisualPacket.end().m_82546_(spellVisualPacket.start());
            if (vec33.f_82479_ * vec33.f_82479_ + vec33.f_82481_ * vec33.f_82481_ < 1.0E-5 && entity != null) {
                vec33 = entity.m_20252_(f);
            }
            double d6 = Math.atan2(-vec33.f_82479_, -vec33.f_82481_);
            double d7 = Math.cos(d6);
            double d8 = Math.sin(d6);
            if (n4 == BATCHES.size()) {
                BATCHES.add(new Batch());
            }
            Batch batch = BATCHES.get(n4++);
            batch.fit(model);
            n5 += n8;
            for (n7 = 0; n7 < model.bones().size(); ++n7) {
                batch.frames[n7] = ArcaneRig.frame(model.bones().get(n7).behavior(), d32, bl3);
            }
            for (n7 = 0; n7 < model.vertices().length; ++n7) {
                n6 = model.vertexBones()[n7];
                double[] dArray3 = batch.positions[n7];
                ArcaneRig.position(model.vertices()[n7], model.bones().get(n6), batch.frames[n6], dArray3);
                d2 = dArray3[0];
                d = dArray3[2];
                dArray3[0] = vec32.f_82479_ + d22 * (d2 * d7 + d * d8);
                dArray3[1] = vec32.f_82480_ + d22 * dArray3[1];
                dArray3[2] = vec32.f_82481_ + d22 * (-d2 * d8 + d * d7);
            }
            n7 = arrayList3.size();
            if (entity != null) {
                arrayList3.add(ArcaneRenderer.body(entity.m_20191_().m_82400_(0.08)));
            }
            if (model.softLight()) {
                for (n6 = 0; n6 < model.vertices().length; ++n6) {
                    batch.visibility[n6] = ArcaneRig.vertexVisibility(batch.positions[n6], dArray, dArray2, arrayList3, bl);
                }
            }
            for (ArcaneField.Triangle triangle : model.triangles()) {
                if (triangle.detail() && !bl2) continue;
                d2 = triangle.alpha() * d5 * batch.frames[model.vertexBones()[triangle.indices()[0]]].opacity();
                d = 0.0;
                if (model.softLight()) {
                    for (int n10 : triangle.indices()) {
                        d = Math.max(d, batch.visibility[n10]);
                    }
                } else {
                    d2 *= ArcaneRig.visibility(batch.positions, triangle.indices(), dArray, dArray2, arrayList3, bl);
                    d = 1.0;
                }
                if (d2 * d < 0.008) continue;
                if (arrayList4.size() == CACHE.size()) {
                    CACHE.add(new Visible());
                }
                Visible visible3 = CACHE.get(arrayList4.size());
                visible3.batch = batch;
                visible3.triangle = triangle;
                visible3.opacity = (float)d2;
                double d9 = 0.0;
                double d10 = 0.0;
                double d11 = 0.0;
                for (int n11 : triangle.indices()) {
                    d9 += batch.positions[n11][0] / 3.0;
                    d10 += batch.positions[n11][1] / 3.0;
                    d11 += batch.positions[n11][2] / 3.0;
                }
                double d12 = d9 - dArray[0];
                double d13 = d10 - dArray[1];
                double d14 = d11 - dArray[2];
                visible3.distance = d12 * d12 + d13 * d13 + d14 * d14;
                arrayList4.add(visible3);
            }
            if (arrayList3.size() <= n7) continue;
            arrayList3.remove(arrayList3.size() - 1);
        }
        if (!arrayList4.isEmpty()) {
            OptimizedSpellVisual optimizedSpellVisual3;
            arrayList4.sort(Comparator.comparingDouble(visible -> visible.distance).reversed());
            MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
            optimizedSpellVisual3 = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
            boolean bl4 = false;
            boolean bl5 = false;
            entity = arrayList4.iterator();
            while (entity.hasNext()) {
                visible2 = (Visible)entity.next();
                object = visible2.batch.softLight ? ArcaneLightRenderType.LIGHT : ENERGY;
                ArcaneRenderer.drawTriangle(bufferSource.m_6299_(object), (Matrix4f)optimizedSpellVisual3, dArray, visible2);
                if (visible2.batch.softLight) {
                    bl4 = true;
                    continue;
                }
                bl5 = true;
            }
            if (bl4) {
                bufferSource.m_109912_(ArcaneLightRenderType.LIGHT);
            }
            if (bl5) {
                bufferSource.m_109912_(ENERGY);
            }
        }
        return arrayList;
    }

    private static ArcaneRig.Box body(AABB aABB) {
        return new ArcaneRig.Box(aABB.f_82288_, aABB.f_82289_, aABB.f_82290_, aABB.f_82291_, aABB.f_82292_, aABB.f_82293_);
    }

    private static double[] coordinates(Vec3 vec3) {
        return new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
    }

    private static void drawTriangle(VertexConsumer vertexConsumer, Matrix4f matrix4f, double[] dArray, Visible visible) {
        double d;
        double d2;
        double d3;
        double d4;
        double d5;
        double d6;
        double[] dArray2;
        double d7;
        double[] dArray3;
        double[][] dArray4 = visible.batch.positions;
        int[] nArray = visible.triangle.indices();
        double[] dArray5 = dArray4[nArray[1]];
        double d8 = dArray5[1] - (dArray3 = dArray4[nArray[0]])[1];
        double d9 = d8 * (d7 = (dArray2 = dArray4[nArray[2]])[2] - dArray3[2]) - (d6 = dArray5[2] - dArray3[2]) * (d5 = dArray2[1] - dArray3[1]);
        double d10 = Math.sqrt(d9 * d9 + (d4 = d6 * (d3 = dArray2[0] - dArray3[0]) - (d2 = dArray5[0] - dArray3[0]) * d7) * d4 + (d = d2 * d5 - d8 * d3) * d);
        if (d10 < 1.0E-10) {
            return;
        }
        for (int i = 0; i < 4; ++i) {
            int n = Math.min(2, i);
            double[] dArray6 = dArray4[nArray[n]];
            double[] dArray7 = visible.triangle.uv()[n];
            float f = visible.batch.softLight ? (float)((double)visible.opacity * visible.batch.visibility[nArray[n]]) : visible.opacity;
            vertexConsumer.m_252986_(matrix4f, (float)(dArray6[0] - dArray[0]), (float)(dArray6[1] - dArray[1]), (float)(dArray6[2] - dArray[2])).m_85950_(1.0f, 1.0f, 1.0f, f).m_7421_((float)dArray7[0], (float)dArray7[1]).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_5601_((float)(d9 / d10), (float)(d4 / d10), (float)(d / d10)).m_5752_();
        }
    }

    private ArcaneRenderer() {
    }

    private static final class Batch {
        double[][] positions = new double[0][3];
        double[] visibility = new double[0];
        boolean softLight;
        ArcaneRig.Frame[] frames = new ArcaneRig.Frame[0];

        private Batch() {
        }

        void fit(ArcaneField.Model model) {
            if (this.positions.length < model.vertices().length) {
                this.positions = new double[model.vertices().length][3];
            }
            if (this.visibility.length < model.vertices().length) {
                this.visibility = new double[model.vertices().length];
            }
            this.softLight = model.softLight();
            if (this.frames.length < model.bones().size()) {
                this.frames = new ArcaneRig.Frame[model.bones().size()];
            }
        }
    }

    private static final class Visible {
        Batch batch;
        ArcaneField.Triangle triangle;
        double distance;
        float opacity;

        private Visible() {
        }
    }
}

