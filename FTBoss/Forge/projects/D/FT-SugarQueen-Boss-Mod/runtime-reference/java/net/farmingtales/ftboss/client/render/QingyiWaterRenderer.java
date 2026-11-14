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
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.client.render.QingyiPlacement;
import net.farmingtales.ftboss.client.render.QingyiSpellRenderer;
import net.farmingtales.ftboss.client.render.QingyiWaterTiming;
import net.farmingtales.ftboss.client.render.QingyiWaveModel;
import net.farmingtales.ftboss.client.render.QingyiWaveTiming;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
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

public final class QingyiWaterRenderer {
    private static final Map<String, RenderType> TYPES = QingyiWaterTiming.IDS.stream().collect(Collectors.toUnmodifiableMap(string -> string, string -> RenderType.m_234338_((ResourceLocation)new ResourceLocation("ftboss", "qingyi/" + string + ".png"))));
    private static final Map<String, int[]> COSTS = QingyiWaterTiming.IDS.stream().collect(Collectors.toUnmodifiableMap(string -> string, string -> {
        QingyiWaveModel.Face[] faceArray = QingyiWaveModel.asset(string).faces();
        return new int[]{(int)Arrays.stream(faceArray).filter(face -> !face.detail()).count(), faceArray.length};
    }));

    private QingyiWaterRenderer() {
    }

    public static void draw(List<OptimizedSpellVisual> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel, int n) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (n <= 0 || list.isEmpty() || clientLevel == null || clientLevel != minecraft.f_91073_) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        float f = minecraft.m_91104_() ? 0.0f : renderLevelStageEvent.getPartialTick();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        Matrix4f matrix4f = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
        Matrix3f matrix3f = new Matrix3f((Matrix4fc)matrix4f);
        Vector3f[] vector3fArray = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
        Vector3f vector3f = new Vector3f();
        Vector3f vector3f2 = new Vector3f();
        HashSet<RenderType> hashSet = new HashSet<RenderType>();
        list.sort(Comparator.comparingDouble(optimizedSpellVisual -> optimizedSpellVisual.packet().start().m_82557_(vec3)));
        int n2 = 0;
        for (OptimizedSpellVisual optimizedSpellVisual2 : list) {
            Vec3 vec32;
            SpellVisualPacket spellVisualPacket = optimizedSpellVisual2.packet();
            String string = spellVisualPacket.spell().id;
            if (!QingyiWaterTiming.IDS.contains(string) || !spellVisualPacket.valid() || !clientLevel.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension())) continue;
            double d = (float)(clientLevel.m_46467_() - optimizedSpellVisual2.refreshed()) + f;
            double d2 = (float)(clientLevel.m_46467_() - optimizedSpellVisual2.born()) + f;
            QingyiWaterTiming.State state = QingyiWaterTiming.state(string, spellVisualPacket.phase().name(), d, d2, spellVisualPacket.spell().lifetime(spellVisualPacket.phase()));
            if ((double)state.opacity() < 0.012) continue;
            Entity entity = clientLevel.m_6815_(spellVisualPacket.ownerId());
            if (entity != null && !entity.m_20148_().equals(spellVisualPacket.owner())) {
                entity = null;
            }
            double d3 = QingyiWaveTiming.scale(SpellVisuals.radiusScale(entity, string));
            boolean bl2 = "tidal_lotus".equals(string) || "abyssal_carapace".equals(string);
            Vec3 vec33 = bl2 && entity != null ? entity.m_20318_(f) : spellVisualPacket.start();
            Vec3 vec34 = spellVisualPacket.end().m_82546_(spellVisualPacket.start()).m_82542_(1.0, 0.0, 1.0).m_82541_();
            if (vec34.m_82556_() < 0.001) {
                vec34 = new Vec3(0.0, 0.0, 1.0);
            }
            if (optimizedSpellVisual2 instanceof QingyiSpellRenderer.Visual) {
                QingyiSpellRenderer.Visual visual = (QingyiSpellRenderer.Visual)optimizedSpellVisual2;
                vec32 = visual.castOrigin();
            } else {
                vec32 = entity == null ? null : entity.m_20182_();
            }
            Vec3 vec35 = vec32;
            boolean bl3 = state.warning() && "riptide_corridor".equals(string) && vec35 != null;
            int n3 = bl3 ? 3 : 1;
            QingyiWaveModel.Asset asset = QingyiWaveModel.asset(string);
            Matrix4f[] matrix4fArray = QingyiWaveModel.pose(asset, state.clip(), state.time());
            for (int i = 0; i < n3; ++i) {
                BlockHitResult blockHitResult;
                Vec3 vec36 = bl3 ? vec35.m_82549_(vec34.m_82490_((double)(5 + i * 5))) : vec33;
                AABB aABB = new AABB(vec36, vec36).m_82400_(Math.max(3.2, (double)asset.width() * 0.55) * d3);
                if (aABB.m_272282_(vec3) > 4096.0 || !renderLevelStageEvent.getFrustum().m_113029_(aABB)) continue;
                if (!bl2 && (blockHitResult = clientLevel.m_45547_(new ClipContext(vec36.m_82520_(0.0, 0.8, 0.0), vec36.m_82520_(0.0, -1.5, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity))).m_6662_() != HitResult.Type.MISS) {
                    vec36 = new Vec3(vec36.f_82479_, Math.max(vec36.f_82480_, blockHitResult.m_82450_().f_82480_ + 0.015), vec36.f_82481_);
                }
                boolean bl4 = particleStatus == ParticleStatus.ALL && vec36.m_82557_(vec3) <= 576.0;
                int n4 = COSTS.get(string)[bl4 ? 1 : 0];
                if (n2 + n4 > n) continue;
                Matrix4f matrix4f2 = QingyiPlacement.frame(vec36.f_82479_ - vec3.f_82479_, vec36.f_82480_ - vec3.f_82480_, vec36.f_82481_ - vec3.f_82481_, vec34.f_82479_, vec34.f_82481_, d3);
                RenderType renderType = TYPES.get(string);
                VertexConsumer vertexConsumer = bufferSource.m_6299_(renderType);
                hashSet.add(renderType);
                for (QingyiWaveModel.Face face : asset.faces()) {
                    if (face.detail() && !bl4) continue;
                    for (int j = 0; j < 4; ++j) {
                        matrix4f2.transformPosition(QingyiWaveModel.vertex(face, j, matrix4fArray, vector3fArray[j]));
                    }
                    float f2 = state.opacity() * face.opacity() * QingyiWaterTiming.faceOpacity(string, face.part(), state.time(), bl && entity == minecraft.f_91074_) * QingyiWaterRenderer.nearCamera(vector3fArray, bl);
                    if ((double)f2 < 0.012) continue;
                    vector3f.set((Vector3fc)vector3fArray[1]).sub((Vector3fc)vector3fArray[0]).cross((Vector3fc)vector3f2.set((Vector3fc)vector3fArray[2]).sub((Vector3fc)vector3fArray[0]));
                    if (vector3f.lengthSquared() < 1.0E-10f) {
                        vector3f.set((Vector3fc)vector3fArray[1]).sub((Vector3fc)vector3fArray[0]).cross((Vector3fc)vector3f2.set((Vector3fc)vector3fArray[3]).sub((Vector3fc)vector3fArray[0]));
                    }
                    if (vector3f.lengthSquared() < 1.0E-10f) continue;
                    vector3f.normalize();
                    for (int j = 0; j < 4; ++j) {
                        vertexConsumer.m_252986_(matrix4f, vector3fArray[j].x, vector3fArray[j].y, vector3fArray[j].z).m_6122_(255, 255, 255, Math.round(f2 * 255.0f)).m_7421_(face.uv()[j * 2], face.uv()[j * 2 + 1]).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(matrix3f, vector3f.x, vector3f.y, vector3f.z).m_5752_();
                    }
                    ++n2;
                }
            }
        }
        for (RenderType renderType : hashSet) {
            bufferSource.m_109912_(renderType);
        }
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
            f3 = Math.min(f3, vector3f.y);
            f4 = Math.min(f4, vector3f.z);
            f = Math.max(f, vector3f.x);
            f5 = Math.max(f5, vector3f.y);
            f6 = Math.max(f6, vector3f.z);
        }
        return QingyiWaveTiming.cameraOpacity(f2, f3, f4, f, f5, f6, bl);
    }
}

