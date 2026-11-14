/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.world.level.Level
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
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.client.maque;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.maque.MaqueArcaneAssets;
import net.farmingtales.ftboss.client.maque.MaqueArcaneLayers;
import net.farmingtales.ftboss.client.maque.MaqueArcanePose;
import net.farmingtales.ftboss.client.maque.MaqueHumanoidAnimations;
import net.farmingtales.ftboss.client.maque.MaqueRicochetPose;
import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.Level;
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
import org.joml.Quaternionfc;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class MaqueFeatherFlightRenderer {
    private static final Map<UUID, Flight> ACTIVE = new LinkedHashMap<UUID, Flight>();
    private static ClientLevel identity;
    private static MaqueArcaneAssets.Surface[] surfaces;
    private static double[] origin;

    private MaqueFeatherFlightRenderer() {
    }

    private static void bind(ClientLevel clientLevel) {
        if (identity != clientLevel) {
            ACTIVE.clear();
            identity = clientLevel;
        }
    }

    public static boolean accept(SpellVisualPacket spellVisualPacket, Map<String, ?> map) {
        if (!spellVisualPacket.spell().id.equals("feather_fan")) {
            return false;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !spellVisualPacket.valid() || !clientLevel.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension())) {
            return true;
        }
        MaqueFeatherFlightRenderer.bind(clientLevel);
        if (spellVisualPacket.ownerId() != Integer.MAX_VALUE) {
            if (spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE) {
                return false;
            }
            map.remove(String.valueOf(spellVisualPacket.owner()) + ":" + spellVisualPacket.spell().id);
            MaqueHumanoidAnimations.spell((Level)clientLevel, spellVisualPacket);
            return true;
        }
        if (spellVisualPacket.phase() == SpellVisualRules.Phase.PULSE) {
            ACTIVE.remove(spellVisualPacket.owner());
            return true;
        }
        if (spellVisualPacket.phase() != SpellVisualRules.Phase.BEAM) {
            return true;
        }
        long l = clientLevel.m_46467_();
        Flight flight = ACTIVE.computeIfAbsent(spellVisualPacket.owner(), uUID -> new Flight());
        flight.position = spellVisualPacket.start();
        flight.velocity = spellVisualPacket.end().m_82546_(spellVisualPacket.start());
        flight.updated = l;
        flight.history.addFirst(spellVisualPacket.start());
        while (flight.history.size() > 8) {
            flight.history.removeLast();
        }
        if (ACTIVE.size() > 320) {
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        }
        return true;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        MaqueFeatherFlightRenderer.bind(clientLevel);
        if (clientLevel != null) {
            ACTIVE.values().removeIf(flight -> clientLevel.m_46467_() - flight.updated > 8L);
        }
    }

    @SubscribeEvent
    public static void disconnect(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        MaqueFeatherFlightRenderer.bind(null);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel() == identity) {
            MaqueFeatherFlightRenderer.bind(null);
        }
    }

    private static void assets() {
        if (surfaces != null) {
            return;
        }
        MaqueArcaneAssets.Art art = MaqueArcaneAssets.get("feather_fan");
        surfaces = (MaqueArcaneAssets.Surface[])Arrays.stream(art.lite()).filter(surface -> surface.bone() == 2).toArray(MaqueArcaneAssets.Surface[]::new);
        double[] dArray = art.bones()[2].pivot();
        origin = new double[]{dArray[0], dArray[1] + 0.09, 1.08};
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty() || !MaqueArcaneLayers.ready()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ != identity) {
            return;
        }
        MaqueFeatherFlightRenderer.assets();
        boolean bl = SpellBloomRenderer.begin(renderLevelStageEvent, true);
        try {
            Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
            double[] dArray = new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
            Vector3f vector3f = new Vector3f(0.0f, 0.0f, -1.0f).rotate((Quaternionfc)renderLevelStageEvent.getCamera().m_253121_());
            double[] dArray2 = new double[]{vector3f.x, vector3f.y, vector3f.z};
            boolean bl2 = !renderLevelStageEvent.getCamera().m_90594_();
            Matrix4f matrix4f = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
            Matrix3f matrix3f = renderLevelStageEvent.getPoseStack().m_85850_().m_252943_();
            MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
            List<Flight> list = ACTIVE.values().stream().filter(flight -> flight.position.m_82557_(vec3) <= 9216.0).sorted(Comparator.comparingDouble(flight -> flight.position.m_82557_(vec3))).limit(32L).toList();
            for (boolean bl3 : new boolean[]{true, false}) {
                VertexConsumer vertexConsumer = bufferSource.m_6299_(bl3 ? MaqueArcaneLayers.REFRACTION : MaqueArcaneLayers.RADIANCE);
                for (Flight flight2 : list) {
                    Vec3 vec32 = flight2.position.m_82549_(flight2.velocity.m_82490_((double)Math.max(0.0f, Math.min(2.0f, (float)(identity.m_46467_() - flight2.updated) + renderLevelStageEvent.getPartialTick()))));
                    double[] dArray3 = new double[]{vec32.f_82479_, vec32.f_82480_, vec32.f_82481_};
                    double[] dArray4 = new double[]{flight2.velocity.f_82479_, flight2.velocity.f_82480_, flight2.velocity.f_82481_};
                    for (MaqueArcaneAssets.Surface surface : surfaces) {
                        if (surface.material() >= 4 != bl3) continue;
                        double[][] dArrayArray = new double[4][];
                        for (int i = 0; i < 4; ++i) {
                            dArrayArray[i] = MaqueRicochetPose.transform(surface.points()[i], origin, dArray3, dArray4);
                        }
                        MaqueFeatherFlightRenderer.draw(vertexConsumer, matrix4f, matrix3f, dArrayArray, surface.material(), surface.opacity(), dArray, dArray2, bl2);
                    }
                    if (bl3) continue;
                    ArrayList<Vec3> arrayList = new ArrayList<Vec3>(flight2.history);
                    Object object = vec32;
                    for (int i = 0; i < arrayList.size(); ++i) {
                        MaqueArcaneAssets.Surface surface;
                        surface = (Vec3)arrayList.get(i);
                        if (object.m_82557_((Vec3)surface) > 1.0E-6) {
                            for (int j = 0; j < 2; ++j) {
                                double[][] dArray5 = MaqueRicochetPose.trailQuad(new double[]{object.f_82479_, object.f_82480_, object.f_82481_}, new double[]{((Vec3)surface).f_82479_, ((Vec3)surface).f_82480_, ((Vec3)surface).f_82481_}, i, arrayList.size(), j);
                                MaqueFeatherFlightRenderer.draw(vertexConsumer, matrix4f, matrix3f, dArray5, j, MaqueRicochetPose.trailAlpha(i, arrayList.size()), MaqueRicochetPose.trailAlpha(i + 1, arrayList.size()), dArray, dArray2, bl2);
                            }
                        }
                        object = surface;
                    }
                }
                bufferSource.m_109912_(bl3 ? MaqueArcaneLayers.REFRACTION : MaqueArcaneLayers.RADIANCE);
            }
        }
        finally {
            SpellBloomRenderer.end(bl);
        }
    }

    private static void draw(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, double[][] dArray, int n, double d, double[] dArray2, double[] dArray3, boolean bl) {
        MaqueFeatherFlightRenderer.draw(vertexConsumer, matrix4f, matrix3f, dArray, n, d, d, dArray2, dArray3, bl);
    }

    private static void draw(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, double[][] dArray, int n, double d, double d2, double[] dArray2, double[] dArray3, boolean bl) {
        double d3;
        double[] dArray4 = new double[3];
        for (double[] dArray5 : dArray) {
            for (int i = 0; i < 3; ++i) {
                int n2 = i;
                dArray4[n2] = dArray4[n2] + dArray5[i] * 0.25;
            }
        }
        double d4 = d3 = bl ? FirstPersonSpellVisibility.cameraFactor(Math.sqrt(Math.pow(dArray4[0] - dArray2[0], 2.0) + Math.pow(dArray4[1] - dArray2[1], 2.0) + Math.pow(dArray4[2] - dArray2[2], 2.0)), true) : 1.0;
        if (bl) {
            d3 *= MaqueArcanePose.sightFade(dArray4, dArray2, dArray3, true);
        }
        if (Math.max(d, d2) * d3 < 0.01) {
            return;
        }
        double d5 = (dArray[1][1] - dArray[0][1]) * (dArray[2][2] - dArray[0][2]) - (dArray[1][2] - dArray[0][2]) * (dArray[2][1] - dArray[0][1]);
        double d6 = (dArray[1][2] - dArray[0][2]) * (dArray[2][0] - dArray[0][0]) - (dArray[1][0] - dArray[0][0]) * (dArray[2][2] - dArray[0][2]);
        double d7 = (dArray[1][0] - dArray[0][0]) * (dArray[2][1] - dArray[0][1]) - (dArray[1][1] - dArray[0][1]) * (dArray[2][0] - dArray[0][0]);
        double d8 = Math.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
        if (d8 < 1.0E-10) {
            return;
        }
        for (int i = 0; i < 4; ++i) {
            double d9 = (i < 2 ? d : d2) * d3;
            float f = (float)((double)(n * 64 + (i == 1 || i == 2 ? 59 : 4)) / 384.0);
            float f2 = (float)((double)(i >= 2 ? 251 : 4) / 256.0);
            vertexConsumer.m_252986_(matrix4f, (float)(dArray[i][0] - dArray2[0]), (float)(dArray[i][1] - dArray2[1]), (float)(dArray[i][2] - dArray2[2])).m_6122_(255, 255, 255, (int)Math.round(Math.min(1.0, d9) * 255.0)).m_7421_(f, f2).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(matrix3f, (float)(d5 / d8), (float)(d6 / d8), (float)(d7 / d8)).m_5752_();
        }
    }

    private static final class Flight {
        final ArrayDeque<Vec3> history = new ArrayDeque();
        Vec3 position;
        Vec3 velocity;
        long updated;

        private Flight() {
        }
    }
}

