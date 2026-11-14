/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Matrix3f
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.client.sugarqueen;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.client.sugarqueen.QueenBridge;
import net.farmingtales.ftboss.client.sugarqueen.QueenMesh;
import net.farmingtales.ftboss.client.sugarqueen.QueenPolicy;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class QueenRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.m_135820_((String)"ftboss:sugarqueen/materials.png");
    private static final List<Visual> ACTIVE = new ArrayList<Visual>();
    private static ClientLevel scene;
    private static final Matrix4f MODEL_TRANSFORM;
    private static final Vector3f VIEW_NORMAL;

    private QueenRenderer() {
    }

    public static boolean accept(SpellVisualPacket spellVisualPacket) {
        Entity entity;
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !spellVisualPacket.valid() || !spellVisualPacket.dimension().equals((Object)clientLevel.m_46472_().m_135782_())) {
            return false;
        }
        if (scene != clientLevel) {
            ACTIVE.clear();
            scene = clientLevel;
        }
        if ((entity = QueenRenderer.owner(clientLevel, spellVisualPacket.ownerId(), spellVisualPacket.owner())) == null || !QueenPolicy.accepts(EntityType.m_20613_((EntityType)entity.m_6095_()).toString(), spellVisualPacket.spell().id)) {
            return false;
        }
        String string = spellVisualPacket.phase().name();
        if (!QueenPolicy.phase(spellVisualPacket.spell().id, string)) {
            return false;
        }
        long l = clientLevel.m_46467_();
        ACTIVE.removeIf(visual -> visual.expires <= l);
        if ("PULSE".equals(string) && ACTIVE.stream().anyMatch(visual -> QueenRenderer.matches(visual, spellVisualPacket) && !visual.packet.phase().name().equals("CHARGE"))) {
            return true;
        }
        ACTIVE.removeIf(visual -> QueenRenderer.matches(visual, spellVisualPacket));
        if (ACTIVE.size() >= 32) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new Visual(spellVisualPacket, l, l + (long)QueenPolicy.lifeTicks(spellVisualPacket.spell().id, string)));
        return true;
    }

    private static boolean matches(Visual visual, SpellVisualPacket spellVisualPacket) {
        return visual.packet.owner().equals(spellVisualPacket.owner()) && visual.packet.spell() == spellVisualPacket.spell();
    }

    private static Entity owner(ClientLevel clientLevel, int n, UUID uUID) {
        Entity entity = clientLevel.m_6815_(n);
        return entity != null && uUID.equals(entity.m_20148_()) ? entity : null;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (scene != clientLevel || clientLevel == null) {
            ACTIVE.clear();
            scene = clientLevel;
            return;
        }
        long l = clientLevel.m_46467_();
        ACTIVE.removeIf(visual -> visual.expires <= l || QueenRenderer.owner(clientLevel, visual.packet.ownerId(), visual.packet.owner()) == null);
        if (ACTIVE.isEmpty()) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        RenderType renderType = RenderType.m_110470_((ResourceLocation)TEXTURE);
        VertexConsumer vertexConsumer = bufferSource.m_6299_(renderType);
        Matrix4f matrix4f = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
        Matrix3f matrix3f = renderLevelStageEvent.getPoseStack().m_85850_().m_252943_();
        int n = 16000;
        for (Visual visual2 : ACTIVE) {
            float f;
            float f2;
            boolean bl;
            SpellVisualPacket spellVisualPacket = visual2.packet;
            String string = spellVisualPacket.spell().id;
            String string2 = spellVisualPacket.phase().name();
            QueenMesh queenMesh = QueenMesh.load(string);
            Entity entity = QueenRenderer.owner(clientLevel, spellVisualPacket.ownerId(), spellVisualPacket.owner());
            Vec3 vec32 = spellVisualPacket.start();
            boolean bl2 = bl = "beam".equals(queenMesh.anchor) && !"BEAM".equals(string2);
            if (("owner".equals(queenMesh.anchor) || bl) && entity != null) {
                float f3 = renderLevelStageEvent.getPartialTick();
                vec32 = new Vec3(entity.f_19854_ + (entity.m_20185_() - entity.f_19854_) * (double)f3, entity.f_19855_ + (entity.m_20186_() - entity.f_19855_) * (double)f3, entity.f_19856_ + (entity.m_20189_() - entity.f_19856_) * (double)f3);
                if (bl) {
                    vec32 = vec32.m_82520_(0.0, 1.3, 0.0).m_82549_(spellVisualPacket.end().m_82546_(spellVisualPacket.start()).m_82541_().m_82490_(0.5));
                }
            }
            if (vec32.m_82557_(vec3) > 80.0 && !"BEAM".equals(string2)) continue;
            Vec3 vec33 = spellVisualPacket.end().m_82546_(spellVisualPacket.start());
            double d = vec33.m_82553_();
            Matrix4f matrix4f2 = MODEL_TRANSFORM.translation((float)(vec32.f_82479_ - vec3.f_82479_), (float)(vec32.f_82480_ - vec3.f_82480_), (float)(vec32.f_82481_ - vec3.f_82481_));
            if ("beam".equals(queenMesh.anchor)) {
                if (d < 0.01 || d > 64.0) continue;
                f2 = (float)Math.atan2(vec33.f_82479_, vec33.f_82481_);
                f = (float)(-Math.asin(Math.max(-1.0, Math.min(1.0, vec33.f_82480_ / d))));
                matrix4f2.rotateY(f2).rotateX(f);
            } else {
                f2 = "owner".equals(queenMesh.anchor) && entity != null ? (float)Math.toRadians(-entity.m_146908_()) : (float)Math.atan2(vec33.f_82479_, vec33.f_82481_);
                matrix4f2.rotateY(f2);
            }
            QueenPolicy.spatial(matrix4f2, "beam".equals(queenMesh.anchor), string2, QueenBridge.radiusScale(entity, string), d);
            f2 = (float)(l - visual2.born) + renderLevelStageEvent.getPartialTick();
            f = "CHARGE".equals(string2) ? QueenBridge.chargeTime(entity, string, renderLevelStageEvent.getPartialTick()) : QueenPolicy.time(string, string2, f2);
            float f4 = QueenPolicy.opacity(string2, f2, (int)(visual2.expires - visual2.born));
            queenMesh.updatePose(f);
            boolean bl3 = vec32.m_82557_(vec3) > 30.0;
            for (QueenMesh.Part part : queenMesh.parts) {
                if (!part.binding.visible || bl3 && QueenPolicy.detailPart(part.name)) continue;
                QueenMesh.transformPart(part, matrix4f2, matrix4f);
                for (QueenMesh.Face face : part.faces) {
                    int n2;
                    float f5 = 1.0f;
                    int[] nArray = face.indices;
                    int n3 = nArray.length;
                    for (n2 = 0; n2 < n3; ++n2) {
                        int n4 = nArray[n2];
                        f5 = Math.min(f5, Math.max(0.0f, Math.min(1.0f, (part.cameraVertices[n4].length() - 0.4f) / 0.45f)));
                    }
                    if (f5 * f4 < 0.02f) continue;
                    if (--n < 0) break;
                    Vector3f vector3f = VIEW_NORMAL.set((Vector3fc)face.localNormal).mul((Matrix3fc)part.normalTransform).mul((Matrix3fc)matrix3f).normalize();
                    for (n3 = 0; n3 < 4; ++n3) {
                        n2 = Math.min(n3, face.indices.length - 1);
                        Vector3f vector3f2 = part.viewVertices[face.indices[n2]];
                        float[] fArray = face.normalizedUv[n2];
                        vertexConsumer.m_5954_(vector3f2.x, vector3f2.y, vector3f2.z, 1.0f, 1.0f, 1.0f, f5 * f4, fArray[0], fArray[1], OverlayTexture.f_118083_, 0xF000F0, vector3f.x, vector3f.y, vector3f.z);
                    }
                }
                if (n >= 0) continue;
                break;
            }
            if (n >= 0) continue;
            break;
        }
        bufferSource.m_109912_(renderType);
    }

    static {
        MODEL_TRANSFORM = new Matrix4f();
        VIEW_NORMAL = new Vector3f();
    }

    private record Visual(SpellVisualPacket packet, long born, long expires) {
    }
}

