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
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.client.render.QingyiWaterRenderer;
import net.farmingtales.ftboss.client.render.QingyiWaterTiming;
import net.farmingtales.ftboss.client.render.QingyiWaveModel;
import net.farmingtales.ftboss.client.render.QingyiWaveTiming;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
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
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class QingyiSpellRenderer {
    private static final RenderType WATER = RenderType.m_234338_((ResourceLocation)new ResourceLocation("ftboss", "qingyi/four_tides.png"));
    private static final int[] COSTS = QingyiSpellRenderer.costs();
    private static final List<Visual> ACTIVE = new ArrayList<Visual>();
    private static ClientLevel activeWorld;

    private QingyiSpellRenderer() {
    }

    public static boolean handles(String string) {
        return QingyiWaterTiming.handles(string);
    }

    public static boolean accept(SpellVisualPacket spellVisualPacket) {
        if (spellVisualPacket == null || !QingyiSpellRenderer.handles(spellVisualPacket.spell().id)) {
            return false;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        QingyiSpellRenderer.synchronize(clientLevel);
        if (clientLevel == null || !spellVisualPacket.valid() || !clientLevel.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension())) {
            return true;
        }
        long l = clientLevel.m_46467_();
        Entity entity = clientLevel.m_6815_(spellVisualPacket.ownerId());
        if (entity != null && !entity.m_20148_().equals(spellVisualPacket.owner())) {
            entity = null;
        }
        if (spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE) {
            for (int i = 0; i < ACTIVE.size(); ++i) {
                Visual visual2 = ACTIVE.get(i);
                if (!visual2.same(spellVisualPacket) || visual2.packet().phase() != spellVisualPacket.phase()) continue;
                ACTIVE.set(i, new Visual(spellVisualPacket, visual2.born(), l, entity == null ? null : entity.m_20182_()));
                return true;
            }
        } else {
            ACTIVE.removeIf(visual -> visual.same(spellVisualPacket) && (visual.packet().phase() == SpellVisualRules.Phase.CHARGE || spellVisualPacket.phase() != SpellVisualRules.Phase.BEAM && visual.packet().phase() != SpellVisualRules.Phase.BEAM && (!"riptide_corridor".equals(spellVisualPacket.spell().id) || visual.packet().phase() != SpellVisualRules.Phase.PULSE || visual.packet().start().m_82557_(spellVisualPacket.start()) < 0.01)));
        }
        if (ACTIVE.size() >= 96) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new Visual(spellVisualPacket, l, l, entity == null ? null : entity.m_20182_()));
        return true;
    }

    private static void synchronize(ClientLevel clientLevel) {
        if (activeWorld != clientLevel) {
            ACTIVE.clear();
            activeWorld = clientLevel;
        }
        if (clientLevel == null) {
            return;
        }
        long l = clientLevel.m_46467_();
        ACTIVE.removeIf(visual -> {
            long l2 = l - visual.refreshed();
            Entity entity = clientLevel.m_6815_(visual.packet().ownerId());
            return l2 < 0L || l2 >= (long)QingyiWaterTiming.lifetime(visual.packet().spell().id, visual.packet().phase().name(), visual.packet().spell().lifetime(visual.packet().phase())) || entity != null && entity.m_20148_().equals(visual.packet().owner()) && !entity.m_6084_();
        });
    }

    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        QingyiSpellRenderer.synchronize(clientLevel);
        if (clientLevel != null && !ACTIVE.isEmpty() && renderLevelStageEvent.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            QingyiSpellRenderer.drawAll(new ArrayList<OptimizedSpellVisual>(ACTIVE), renderLevelStageEvent, clientLevel);
        }
    }

    private static int[] costs() {
        int[] nArray = new int[4];
        for (QingyiWaveModel.Face face : QingyiWaveModel.asset().faces()) {
            for (int i = 0; i < 4; ++i) {
                if (face.interior() && (i & 1) != 0 || face.detail() && (i & 2) == 0) continue;
                int n = i;
                nArray[n] = nArray[n] + 1;
            }
        }
        return nArray;
    }

    public static List<?> renderAndFilter(List<?> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel) {
        if (list.isEmpty()) {
            return list;
        }
        ArrayList arrayList = new ArrayList();
        ArrayList<OptimizedSpellVisual> arrayList2 = new ArrayList<OptimizedSpellVisual>();
        for (Object obj : list) {
            if (obj instanceof OptimizedSpellVisual) {
                OptimizedSpellVisual optimizedSpellVisual = (OptimizedSpellVisual)obj;
                if (QingyiSpellRenderer.handles(optimizedSpellVisual.packet().spell().id)) {
                    arrayList2.add(optimizedSpellVisual);
                    continue;
                }
            }
            arrayList.add(obj);
        }
        if (arrayList2.isEmpty()) {
            return list;
        }
        if (renderLevelStageEvent.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            QingyiSpellRenderer.drawAll(arrayList2, renderLevelStageEvent, clientLevel);
        }
        return arrayList;
    }

    private static void drawAll(List<OptimizedSpellVisual> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel) {
        ParticleStatus particleStatus = (ParticleStatus)Minecraft.m_91087_().f_91066_.m_231929_().m_231551_();
        int n = particleStatus == ParticleStatus.MINIMAL ? 4500 : (particleStatus == ParticleStatus.DECREASED ? 9000 : 18000);
        ArrayList<OptimizedSpellVisual> arrayList = new ArrayList<OptimizedSpellVisual>();
        ArrayList<OptimizedSpellVisual> arrayList2 = new ArrayList<OptimizedSpellVisual>();
        for (OptimizedSpellVisual optimizedSpellVisual : list) {
            ("four_tides".equals(optimizedSpellVisual.packet().spell().id) ? arrayList : arrayList2).add(optimizedSpellVisual);
        }
        int n2 = QingyiSpellRenderer.draw(arrayList, renderLevelStageEvent, clientLevel, n);
        QingyiWaterRenderer.draw(arrayList2, renderLevelStageEvent, clientLevel, n - n2);
    }

    private static int draw(List<OptimizedSpellVisual> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel, int n) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (clientLevel == null || clientLevel != minecraft.f_91073_ || list.isEmpty() || n <= 0) {
            return 0;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        float f = minecraft.m_91104_() ? 0.0f : renderLevelStageEvent.getPartialTick();
        list.sort(Comparator.comparingDouble(optimizedSpellVisual -> optimizedSpellVisual.packet().start().m_82557_(vec3)));
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        VertexConsumer vertexConsumer = bufferSource.m_6299_(WATER);
        Matrix4f matrix4f = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
        Matrix3f matrix3f = new Matrix3f((Matrix4fc)matrix4f);
        Vector3f[] vector3fArray = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
        Vector3f vector3f = new Vector3f();
        Vector3f vector3f2 = new Vector3f();
        Vector3f vector3f3 = new Vector3f();
        int n2 = 0;
        for (OptimizedSpellVisual optimizedSpellVisual2 : list) {
            boolean bl;
            String string;
            float f2;
            double d;
            Vec3 vec32;
            Vec3 vec33;
            int n3;
            float f3;
            Vec3 vec34;
            SpellVisualPacket spellVisualPacket = optimizedSpellVisual2.packet();
            if (!spellVisualPacket.valid() || !clientLevel.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension())) continue;
            double d2 = (float)(clientLevel.m_46467_() - optimizedSpellVisual2.refreshed()) + f;
            Entity entity = clientLevel.m_6815_(spellVisualPacket.ownerId());
            if (entity != null && !entity.m_20148_().equals(spellVisualPacket.owner())) {
                entity = null;
            }
            double d3 = QingyiWaveTiming.scale(SpellVisuals.radiusScale(entity, spellVisualPacket.spell().id));
            if (spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM) {
                Vec3 vec35;
                vec34 = spellVisualPacket.end().m_82546_(spellVisualPacket.start());
                double d4 = Math.sqrt(vec34.f_82479_ * vec34.f_82479_ + vec34.f_82481_ * vec34.f_82481_);
                if (d4 < 0.1 || d4 > 64.0 || (double)(f3 = QingyiWaveTiming.opacity(d2, n3 = QingyiWaveTiming.slice(d4, d3))) < 0.01) continue;
                vec33 = spellVisualPacket.start().m_82549_(vec34.m_82490_(0.5)).m_82520_(0.0, -0.8, 0.0);
                vec32 = new Vec3(vec34.f_82481_, 0.0, -vec34.f_82479_).m_82541_();
                Vec3 vec36 = vec35 = entity == null ? null : vec33.m_82546_(entity.m_20182_());
                if (vec35 != null && vec32.m_82526_(vec35) < 0.0) {
                    vec32 = vec32.m_82490_(-1.0);
                }
                vec33 = vec33.m_82549_(vec32.m_82490_(QingyiWaveTiming.travel(d2, n3) * d3));
                d = d4 / (double)QingyiWaveModel.asset().width();
                f2 = QingyiWaveTiming.time(d2, n3);
                string = "surge";
            } else {
                boolean bl2;
                boolean bl3 = bl2 = spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE;
                if (!bl2 && spellVisualPacket.phase() != SpellVisualRules.Phase.RELEASE || list.stream().anyMatch(optimizedSpellVisual -> optimizedSpellVisual.packet().phase() == SpellVisualRules.Phase.BEAM && optimizedSpellVisual.packet().owner().equals(spellVisualPacket.owner()))) continue;
                vec33 = bl2 && entity != null ? entity.m_20318_(f) : spellVisualPacket.start();
                vec32 = spellVisualPacket.end().m_82546_(spellVisualPacket.start()).m_82542_(1.0, 0.0, 1.0).m_82541_();
                if (vec32.m_82556_() < 0.001) {
                    vec32 = entity == null ? new Vec3(0.0, 0.0, 1.0) : entity.m_20252_(f).m_82542_(1.0, 0.0, 1.0).m_82541_();
                }
                vec33 = vec33.m_82549_(vec32.m_82490_(0.85 * d3));
                d = 0.55 * d3;
                string = "gather";
                f2 = bl2 ? (float)Math.min(0.5, (double)((float)(clientLevel.m_46467_() - optimizedSpellVisual2.born()) + f) * 0.05) : 0.5f;
                float f4 = f3 = bl2 ? (float)(0.6 * QingyiWaveTiming.smooth(d2 / 2.0)) : (float)(0.45 * (1.0 - QingyiWaveTiming.smooth(d2 / 8.0)));
            }
            if ((double)f3 < 0.01 || (vec34 = new AABB(vec33, vec33).m_82400_(Math.max(d * 2.5, d3 * 3.0))).m_272282_(vec3) > 4096.0 || !renderLevelStageEvent.getFrustum().m_113029_((AABB)vec34)) continue;
            BlockHitResult blockHitResult = clientLevel.m_45547_(new ClipContext(vec33.m_82520_(0.0, 0.8, 0.0), vec33.m_82520_(0.0, -1.5, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
            if (blockHitResult.m_6662_() != HitResult.Type.MISS) {
                vec33 = new Vec3(vec33.f_82479_, Math.max(vec33.f_82480_, blockHitResult.m_82450_().f_82480_ + 0.015), vec33.f_82481_);
            }
            Matrix4f[] matrix4fArray = QingyiWaveModel.pose(string, f2);
            n3 = particleStatus != ParticleStatus.ALL || vec33.m_82557_(vec3) > 576.0 ? 1 : 0;
            int n4 = COSTS[(n3 != 0 ? 1 : 0) | ((bl = particleStatus != ParticleStatus.MINIMAL && vec33.m_82557_(vec3) <= 256.0) ? 2 : 0)];
            if (n2 + n4 > n) continue;
            double d5 = d3 * Math.sqrt(d / d3);
            for (QingyiWaveModel.Face face : QingyiWaveModel.asset().faces()) {
                if (face.interior() && n3 != 0 || face.detail() && !bl) continue;
                if (face.featurePivot() != null) {
                    matrix4fArray[face.bone()].transformPosition(vector3f3.set(face.featurePivot()));
                }
                for (int i = 0; i < 4; ++i) {
                    Vector3f vector3f4 = QingyiWaveModel.vertex(face, i, matrix4fArray, vector3fArray[i]);
                    double d6 = face.featurePivot() == null ? (double)vector3f4.x * d : (double)vector3f3.x * d + (double)(vector3f4.x - vector3f3.x) * d5;
                    double d7 = (double)vector3f4.y * d3;
                    double d8 = (double)vector3f4.z * d5;
                    vector3f4.set((float)(vec33.f_82479_ + vec32.f_82481_ * d6 + vec32.f_82479_ * d8 - vec3.f_82479_), (float)(vec33.f_82480_ + d7 - vec3.f_82480_), (float)(vec33.f_82481_ - vec32.f_82479_ * d6 + vec32.f_82481_ * d8 - vec3.f_82481_));
                }
                float f5 = f3 * face.opacity() * QingyiSpellRenderer.nearCamera(vector3fArray, !renderLevelStageEvent.getCamera().m_90594_());
                if ((double)f5 < 0.012) continue;
                vector3f.set((Vector3fc)vector3fArray[1]).sub((Vector3fc)vector3fArray[0]).cross((Vector3fc)vector3f2.set((Vector3fc)vector3fArray[2]).sub((Vector3fc)vector3fArray[0]));
                if (vector3f.lengthSquared() < 1.0E-10f) {
                    vector3f.set((Vector3fc)vector3fArray[1]).sub((Vector3fc)vector3fArray[0]).cross((Vector3fc)vector3f2.set((Vector3fc)vector3fArray[3]).sub((Vector3fc)vector3fArray[0]));
                }
                if (vector3f.lengthSquared() < 1.0E-10f) continue;
                vector3f.normalize();
                float f6 = 1.0f;
                float f7 = 0.0f;
                for (int i = 0; i < 4; ++i) {
                    f6 = Math.min(f6, face.uv()[i * 2]);
                    f7 = Math.max(f7, face.uv()[i * 2]);
                }
                float f8 = (float)Math.min(31.0, (double)((f7 - f6) * 128.0f) * d / d3);
                float f9 = face.tile() < 0 ? 0.0f : (f6 * 128.0f - (float)face.tile()) % (32.0f - f8);
                for (int i = 0; i < 4; ++i) {
                    float f10 = face.uv()[i * 2];
                    if (face.tile() >= 0 && f7 > f6) {
                        f10 = ((float)face.tile() + f9 + (f10 - f6) / (f7 - f6) * f8) / 128.0f;
                    }
                    vertexConsumer.m_252986_(matrix4f, vector3fArray[i].x, vector3fArray[i].y, vector3fArray[i].z).m_6122_(255, 255, 255, Math.round(f5 * 255.0f)).m_7421_(f10, face.uv()[i * 2 + 1]).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(matrix3f, vector3f.x, vector3f.y, vector3f.z).m_5752_();
                }
                ++n2;
            }
            if (n2 < n) continue;
            break;
        }
        bufferSource.m_109912_(WATER);
        return n2;
    }

    private static float nearCamera(Vector3f[] vector3fArray, boolean bl) {
        float f;
        float f2;
        float f3 = f2 = Float.POSITIVE_INFINITY;
        float f4 = f2;
        float f5 = f = Float.NEGATIVE_INFINITY;
        float f6 = f;
        for (Vector3f vector3f : vector3fArray) {
            f2 = Math.min(f2, vector3f.x);
            f = Math.max(f, vector3f.x);
            f3 = Math.min(f3, vector3f.y);
            f5 = Math.max(f5, vector3f.y);
            f4 = Math.min(f4, vector3f.z);
            f6 = Math.max(f6, vector3f.z);
        }
        return QingyiWaveTiming.cameraOpacity(f2, f3, f4, f, f5, f6, bl);
    }

    record Visual(SpellVisualPacket packet, long born, long refreshed, Vec3 castOrigin) implements OptimizedSpellVisual
    {
        @Override
        public boolean nativeLight() {
            return false;
        }

        boolean same(SpellVisualPacket spellVisualPacket) {
            return this.packet.owner().equals(spellVisualPacket.owner()) && this.packet.spell() == spellVisualPacket.spell();
        }
    }
}

