/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.BlockAndTintGetter
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
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.client.compostzero;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.client.compostzero.CompostAssets;
import net.farmingtales.ftboss.client.compostzero.CompostMotion;
import net.farmingtales.ftboss.client.compostzero.CompostTiming;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class CompostRenderer {
    private static final Set<String> IDS = Set.of("prismatic_blight", "virulent_harvest", "spore_eruption", "rotting_requiem", "living_mulch");
    private static final List<Visual> ACTIVE = new ArrayList<Visual>();
    private static final Map<UUID, Sequence> WAVES = new HashMap<UUID, Sequence>();
    private static final RenderType SOLID = RenderType.m_110473_((ResourceLocation)CompostAssets.ATLAS);
    private static final RenderType GLOW = RenderType.m_234338_((ResourceLocation)CompostAssets.ATLAS);
    private static ClientLevel world;

    private CompostRenderer() {
    }

    public static boolean acceptsId(String string) {
        return IDS.contains(string);
    }

    public static boolean accept(SpellVisualPacket spellVisualPacket) {
        if (spellVisualPacket == null || spellVisualPacket.spell() == null || !CompostRenderer.acceptsId(spellVisualPacket.spell().id)) {
            return false;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null || !spellVisualPacket.valid() || !clientLevel.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension())) {
            return true;
        }
        if (world != clientLevel) {
            CompostRenderer.clear(clientLevel);
        }
        long l = clientLevel.m_46467_();
        int n = 0;
        if (spellVisualPacket.spell().id.equals("rotting_requiem")) {
            if (spellVisualPacket.phase() == SpellVisualRules.Phase.RELEASE) {
                WAVES.put(spellVisualPacket.owner(), new Sequence(l, l - 1L, -1));
            }
            if (spellVisualPacket.phase() == SpellVisualRules.Phase.PULSE) {
                Sequence sequence = WAVES.get(spellVisualPacket.owner());
                if (sequence == null || l - sequence.last > 45L) {
                    sequence = new Sequence(l, l - 1L, -1);
                }
                n = sequence.last == l ? Math.max(0, sequence.index) : Math.min(2, sequence.index + 1);
                WAVES.put(spellVisualPacket.owner(), new Sequence(sequence.began, l, n));
            }
        }
        if (spellVisualPacket.phase() != SpellVisualRules.Phase.BEAM) {
            ACTIVE.removeIf(visual -> visual.packet.owner().equals(spellVisualPacket.owner()) && visual.packet.spell() == spellVisualPacket.spell() && visual.packet.phase() != SpellVisualRules.Phase.BEAM);
        }
        if (ACTIVE.size() >= 32) {
            ACTIVE.remove(0);
        }
        int n2 = CompostTiming.duration(spellVisualPacket.spell().id, spellVisualPacket.phase().name(), spellVisualPacket.spell().duration);
        Entity entity = clientLevel.m_6815_(spellVisualPacket.ownerId());
        if (entity != null && !entity.m_20148_().equals(spellVisualPacket.owner())) {
            entity = null;
        }
        double d = CompostTiming.radiusScale(entity == null ? 1.0 : SpellVisuals.radiusScale(entity, spellVisualPacket.spell().id));
        ACTIVE.add(new Visual(spellVisualPacket, l, n2, n, d));
        return true;
    }

    private static void clear(ClientLevel clientLevel) {
        ACTIVE.clear();
        WAVES.clear();
        world = clientLevel;
        CompostAssets.reset();
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || clientLevel != world) {
            CompostRenderer.clear(clientLevel);
            return;
        }
        long l = clientLevel.m_46467_();
        ACTIVE.removeIf(visual -> CompostTiming.expired(l, visual.born, visual.duration));
        WAVES.entrySet().removeIf(entry -> l - ((Sequence)entry.getValue()).last > 100L);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel() == world) {
            CompostRenderer.clear(null);
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        CompostRenderer.clear(null);
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        CompostAssets.Model model;
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null || clientLevel != world) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        boolean bl = minecraft.f_91066_.m_92176_().m_90612_();
        Vector3f vector3f = renderLevelStageEvent.getCamera().m_253058_();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        int n = 18000;
        ArrayList<Visual> arrayList = new ArrayList<Visual>(ACTIVE);
        arrayList.sort((visual, visual2) -> Double.compare(visual.packet.start().m_82554_(vec3), visual2.packet.start().m_82554_(vec3)));
        HashMap<Visual, Boolean> hashMap = new HashMap<Visual, Boolean>();
        ArrayList<Visual> arrayList2 = new ArrayList<Visual>();
        for (Visual visual3 : arrayList) {
            int n2;
            if (visual3.packet.phase() == SpellVisualRules.Phase.BEAM) {
                if (n < 128) continue;
                arrayList2.add(visual3);
                n -= 128;
                continue;
            }
            model = CompostAssets.get(visual3.packet.spell().id);
            if (model == null) continue;
            boolean bl2 = visual3.packet.start().m_82554_(vec3) > 784.0 || ACTIVE.size() > 8 || n < model.faces;
            int n3 = n2 = bl2 ? model.liteFaces : model.faces;
            if (n < n2) continue;
            arrayList2.add(visual3);
            hashMap.put(visual3, bl2);
            n -= n2;
        }
        arrayList = arrayList2;
        arrayList.sort((visual, visual2) -> Double.compare(visual2.packet.start().m_82554_(vec3), visual.packet.start().m_82554_(vec3)));
        for (int i = 0; i < 2; ++i) {
            boolean bl3 = i == 1;
            model = bl3 ? GLOW : SOLID;
            VertexConsumer vertexConsumer = bufferSource.m_6299_((RenderType)model);
            for (Visual visual4 : arrayList) {
                Vec3 vec32;
                boolean bl4;
                SpellVisualPacket spellVisualPacket = visual4.packet;
                Entity entity = clientLevel.m_6815_(spellVisualPacket.ownerId());
                if (entity != null && !entity.m_20148_().equals(spellVisualPacket.owner())) {
                    entity = null;
                }
                if ((bl4 = spellVisualPacket.spell().followsCaster(spellVisualPacket.phase())) && entity == null) continue;
                Vec3 vec33 = vec32 = bl4 ? new Vec3(entity.f_19790_ + (entity.m_20185_() - entity.f_19790_) * (double)renderLevelStageEvent.getPartialTick(), entity.f_19791_ + (entity.m_20186_() - entity.f_19791_) * (double)renderLevelStageEvent.getPartialTick(), entity.f_19792_ + (entity.m_20189_() - entity.f_19792_) * (double)renderLevelStageEvent.getPartialTick()) : spellVisualPacket.start();
                double d = (float)(clientLevel.m_46467_() - visual4.born) + renderLevelStageEvent.getPartialTick();
                if (d < 0.0 || d >= (double)visual4.duration || vec32.m_82554_(vec3) > 6400.0) continue;
                if (spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM) {
                    if (!bl3 || !spellVisualPacket.spell().id.equals("virulent_harvest")) continue;
                    CompostRenderer.beam(spellVisualPacket, d, vec3, bufferSource, renderLevelStageEvent.getPoseStack());
                    continue;
                }
                CompostAssets.Model model2 = CompostAssets.get(spellVisualPacket.spell().id);
                if (model2 == null) continue;
                boolean bl5 = hashMap.getOrDefault(visual4, true);
                double d2 = Math.max(0.0, Math.min(1.0, ((double)visual4.duration - d) / 4.0));
                int n4 = bl3 ? 0xF000F0 : LevelRenderer.m_109541_((BlockAndTintGetter)clientLevel, (BlockPos)BlockPos.m_274561_((double)vec32.f_82479_, (double)vec32.f_82480_, (double)vec32.f_82481_));
                double d3 = visual4.radiusScale;
                PoseStack poseStack = renderLevelStageEvent.getPoseStack();
                poseStack.m_85836_();
                poseStack.m_85837_(vec32.f_82479_ - vec3.f_82479_, vec32.f_82480_ - vec3.f_82480_ + 0.009, vec32.f_82481_ - vec3.f_82481_);
                double d4 = bl4 && entity != null ? Math.toRadians(-entity.m_146908_()) : Math.atan2(spellVisualPacket.end().f_82479_ - spellVisualPacket.start().f_82479_, spellVisualPacket.end().f_82481_ - spellVisualPacket.start().f_82481_);
                Matrix4f matrix4f = new Matrix4f((Matrix4fc)poseStack.m_85850_().m_252922_()).rotateY((float)d4).scale((float)d3, 1.0f, (float)d3);
                Matrix4f matrix4f2 = new Matrix4f();
                Vector3f vector3f2 = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
                Vector3f vector3f3 = new Vector3f();
                Vector3f vector3f4 = new Vector3f();
                for (CompostAssets.Part part : model2.parts) {
                    CompostMotion.State state;
                    if (bl5 && part.detail || (state = CompostMotion.pose(part.motion, part.params, part.detail, d, spellVisualPacket.phase().name(), visual4.pulse)).alpha() * d2 < 0.005) continue;
                    matrix4f2.set((Matrix4fc)matrix4f).translate((float)(part.pivot[0] + state.position()[0]), (float)(part.pivot[1] + state.position()[1]), (float)(part.pivot[2] + state.position()[2])).rotateZ((float)Math.toRadians(state.rotation()[2])).rotateY((float)Math.toRadians(state.rotation()[1])).rotateX((float)Math.toRadians(state.rotation()[0])).scale((float)state.scale()[0], (float)state.scale()[1], (float)state.scale()[2]);
                    for (CompostAssets.Face face : part.faces) {
                        int n5;
                        if (face.glow != bl3) continue;
                        VertexConsumer vertexConsumer2 = vertexConsumer;
                        for (int j = 0; j < 4; ++j) {
                            double[] dArray = face.points[Math.min(j, face.points.length - 1)];
                            vector3f2[j].set((float)(dArray[0] - part.pivot[0]), (float)(dArray[1] - part.pivot[1]), (float)(dArray[2] - part.pivot[2]));
                            matrix4f2.transformPosition(vector3f2[j]);
                        }
                        vector3f3.set((Vector3fc)vector3f2[1]).sub((Vector3fc)vector3f2[0]).cross((Vector3fc)vector3f4.set((Vector3fc)vector3f2[2]).sub((Vector3fc)vector3f2[0]));
                        if (vector3f3.lengthSquared() < 1.0E-12f) continue;
                        vector3f3.normalize();
                        float f = (float)(face.alpha * d2 * state.alpha());
                        double d5 = Double.POSITIVE_INFINITY;
                        Vector3f vector3f5 = vector3f2;
                        int n6 = ((Vector3f[])vector3f5).length;
                        for (n5 = 0; n5 < n6; ++n5) {
                            Vector3f vector3f6 = vector3f5[n5];
                            d5 = Math.min(d5, (double)vector3f6.lengthSquared());
                        }
                        if (d5 < 0.09) {
                            f *= 0.04f;
                        }
                        if (bl) {
                            vector3f5 = vector3f4.set((Vector3fc)vector3f2[0]).add((Vector3fc)vector3f2[1]).add((Vector3fc)vector3f2[2]).add((Vector3fc)vector3f2[3]).mul(0.25f);
                            float f2 = vector3f5.dot((Vector3fc)vector3f);
                            float f3 = (float)Math.sqrt(Math.max(vector3f2[0].distanceSquared((Vector3fc)vector3f5), vector3f2[2].distanceSquared((Vector3fc)vector3f5)));
                            if ((double)vector3f5.lengthSquared() < 11.559999999999999 && f2 > 0.0f && (double)(vector3f5.lengthSquared() - f2 * f2) < ((double)f3 + (double)f2 * 0.11) * ((double)f3 + (double)f2 * 0.11)) {
                                f *= face.glow ? 0.28f : 0.14f;
                            }
                        }
                        float f4 = (float)(face.material % 8 * 32 + 1) / 256.0f;
                        float f5 = (float)(face.material / 8 * 32 + 1) / 128.0f;
                        for (n5 = 0; n5 < 4; ++n5) {
                            CompostRenderer.vertex(vertexConsumer2, vector3f2[n5], vector3f3, f4 + (n5 == 1 || n5 == 2 ? 0.1171875f : 0.0f), f5 + (n5 >= 2 ? 0.234375f : 0.0f), f, n4);
                        }
                    }
                }
                poseStack.m_85849_();
            }
            bufferSource.m_109912_((RenderType)model);
        }
    }

    private static void vertex(VertexConsumer vertexConsumer, Vector3f vector3f, Vector3f vector3f2, float f, float f2, float f3, int n) {
        vertexConsumer.m_5483_((double)vector3f.x, (double)vector3f.y, (double)vector3f.z).m_6122_(255, 255, 255, (int)(Math.max(0.0f, Math.min(1.0f, f3)) * 255.0f)).m_7421_(f, f2).m_86008_(OverlayTexture.f_118083_).m_85969_(n).m_5601_(vector3f2.x, vector3f2.y, vector3f2.z).m_5752_();
    }

    private static void beam(SpellVisualPacket spellVisualPacket, double d, Vec3 vec3, MultiBufferSource.BufferSource bufferSource, PoseStack poseStack) {
        Vec3 vec32 = spellVisualPacket.end().m_82546_(spellVisualPacket.start());
        double d2 = vec32.m_82553_();
        if (d2 < 0.001 || d2 > 80.0) {
            return;
        }
        Vector3f vector3f = new Vector3f((float)vec32.f_82479_, (float)vec32.f_82480_, (float)vec32.f_82481_).normalize();
        Vector3f vector3f2 = new Vector3f((Vector3fc)vector3f).cross((Vector3fc)(Math.abs(vector3f.y) < 0.9f ? new Vector3f(0.0f, 1.0f, 0.0f) : new Vector3f(1.0f, 0.0f, 0.0f))).normalize();
        Vector3f vector3f3 = new Vector3f((Vector3fc)vector3f).cross((Vector3fc)vector3f2).normalize();
        Matrix4f matrix4f = poseStack.m_85850_().m_252922_();
        VertexConsumer vertexConsumer = bufferSource.m_6299_(GLOW);
        Vector3f[] vector3fArray = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
        Vector3f vector3f4 = new Vector3f();
        Vector3f vector3f5 = new Vector3f();
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 4; ++k) {
                    for (int i2 = 0; i2 < 4; ++i2) {
                        double d3 = (double)(j + (i2 >= 2 ? 1 : 0)) / 16.0;
                        double d4 = (double)(k + (i2 == 1 || i2 == 2 ? 1 : 0)) * Math.PI / 2.0;
                        float f = i == 0 ? 0.026f : 0.007f;
                        float f2 = (float)Math.cos(d4) * f;
                        float f3 = (float)Math.sin(d4) * f;
                        Vector3f vector3f6 = vector3fArray[i2].set((float)(spellVisualPacket.start().f_82479_ + vec32.f_82479_ * d3 - vec3.f_82479_), (float)(spellVisualPacket.start().f_82480_ + vec32.f_82480_ * d3 - vec3.f_82480_), (float)(spellVisualPacket.start().f_82481_ + vec32.f_82481_ * d3 - vec3.f_82481_));
                        vector3f6.add(vector3f2.x * f2 + vector3f3.x * f3, vector3f2.y * f2 + vector3f3.y * f3, vector3f2.z * f2 + vector3f3.z * f3);
                        matrix4f.transformPosition(vector3f6);
                    }
                    float f = (float)(Math.max(0.0, Math.min(1.0, (12.0 - d) / 4.0)) * (i == 0 ? 0.16 : 0.62));
                    float f4 = (float)((i == 0 ? 5 : 6) * 32 + 16) / 256.0f;
                    float f5 = 0.125f;
                    vector3f4.set((Vector3fc)vector3fArray[1]).sub((Vector3fc)vector3fArray[0]).cross((Vector3fc)vector3f5.set((Vector3fc)vector3fArray[2]).sub((Vector3fc)vector3fArray[0])).normalize();
                    for (Vector3f vector3f7 : vector3fArray) {
                        CompostRenderer.vertex(vertexConsumer, vector3f7, vector3f4, f4, f5, f, 0xF000F0);
                    }
                }
            }
        }
    }

    private record Sequence(long began, long last, int index) {
    }

    private record Visual(SpellVisualPacket packet, long born, int duration, int pulse, double radiusScale) {
    }
}

