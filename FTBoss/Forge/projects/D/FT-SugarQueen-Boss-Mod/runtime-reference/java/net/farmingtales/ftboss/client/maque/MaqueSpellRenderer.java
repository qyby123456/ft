/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.maque.MaqueArcaneAssets;
import net.farmingtales.ftboss.client.maque.MaqueArcaneLayers;
import net.farmingtales.ftboss.client.maque.MaqueArcanePose;
import net.farmingtales.ftboss.client.maque.MaqueArcaneTrail;
import net.farmingtales.ftboss.client.maque.MaqueFeatherFlightRenderer;
import net.farmingtales.ftboss.client.maque.MaqueHumanoidAnimations;
import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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
public final class MaqueSpellRenderer {
    private static final Map<String, Active> ACTIVE = new LinkedHashMap<String, Active>();
    private static ClientLevel levelIdentity;

    private MaqueSpellRenderer() {
    }

    public static boolean accept(SpellVisualPacket spellVisualPacket) {
        if (spellVisualPacket == null || spellVisualPacket.spell() == null || !spellVisualPacket.spell().id.equals("feather_fan") && !spellVisualPacket.spell().id.equals("wingstep") && !spellVisualPacket.spell().id.equals("feather_refuge")) {
            return false;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !spellVisualPacket.valid() || !clientLevel.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension())) {
            return true;
        }
        MaqueSpellRenderer.bind(clientLevel);
        if (MaqueFeatherFlightRenderer.accept(spellVisualPacket, ACTIVE)) {
            return true;
        }
        MaqueHumanoidAnimations.spell((Level)clientLevel, spellVisualPacket);
        String string = String.valueOf(spellVisualPacket.owner()) + ":" + spellVisualPacket.spell().id;
        long l = clientLevel.m_46467_();
        Active active = ACTIVE.get(string);
        boolean bl = active != null && active.packet.phase() == spellVisualPacket.phase() && l - active.updated < 4L;
        ACTIVE.put(string, new Active(spellVisualPacket, bl ? active.born : l, l));
        if (ACTIVE.size() > 32) {
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        }
        return true;
    }

    private static void bind(ClientLevel clientLevel) {
        if (levelIdentity != clientLevel) {
            ACTIVE.clear();
            levelIdentity = clientLevel;
            if (clientLevel == null) {
                MaqueHumanoidAnimations.clear();
            }
        }
    }

    private static int lifetime(SpellVisualPacket spellVisualPacket) {
        int n = MaqueArcanePose.visualLifetime(spellVisualPacket.spell().id, spellVisualPacket.phase().name(), spellVisualPacket.spell().lifetime(spellVisualPacket.phase()));
        return spellVisualPacket.spell().id.equals("feather_fan") && !spellVisualPacket.phase().name().equals("CHARGE") ? Math.max(n, (int)Math.ceil(MaqueArcaneTrail.endTick())) : n;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        MaqueSpellRenderer.bind(clientLevel);
        if (clientLevel != null) {
            ACTIVE.values().removeIf(active -> clientLevel.m_46467_() - active.born > (long)(MaqueSpellRenderer.lifetime(active.packet) + 2));
        }
    }

    @SubscribeEvent
    public static void disconnect(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        MaqueSpellRenderer.bind(null);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel() == levelIdentity) {
            MaqueSpellRenderer.bind(null);
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty() || !MaqueArcaneLayers.ready()) {
            return;
        }
        boolean bl = SpellBloomRenderer.begin(renderLevelStageEvent, true);
        try {
            MaqueSpellRenderer.renderEffects(renderLevelStageEvent);
        }
        finally {
            SpellBloomRenderer.end(bl);
        }
    }

    private static void renderEffects(RenderLevelStageEvent renderLevelStageEvent) {
        Object object;
        AbstractClientPlayer abstractClientPlayer2;
        int n;
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null || clientLevel != levelIdentity) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        Vector3f vector3f = new Vector3f(0.0f, 0.0f, -1.0f).rotate((Quaternionfc)renderLevelStageEvent.getCamera().m_253121_());
        double[] dArray = new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
        double[] dArray2 = new double[]{vector3f.x, vector3f.y, vector3f.z};
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        boolean bl2 = !bl || FirstPersonSpellVisibility.cameraFactor(0.0, bl) < 1.0;
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n2 = particleStatus == ParticleStatus.MINIMAL ? 3 : (n = particleStatus == ParticleStatus.DECREASED ? 5 : 8);
        int n3 = particleStatus == ParticleStatus.MINIMAL ? 15000 : (particleStatus == ParticleStatus.DECREASED ? 24000 : 32000);
        List<Active> list = ACTIVE.values().stream().sorted(Comparator.comparingDouble(active -> active.packet.start().m_82557_(vec3))).limit(n).toList();
        ArrayList<double[]> arrayList = new ArrayList<double[]>();
        for (AbstractClientPlayer abstractClientPlayer2 : clientLevel.m_6907_()) {
            if (!(abstractClientPlayer2.m_20182_().m_82557_(vec3) < 1024.0)) continue;
            arrayList.add(MaqueSpellRenderer.bounds(abstractClientPlayer2.m_20191_().m_82400_(0.1)));
        }
        if (bl) {
            object = new double[]{dArray[0] + dArray2[0] * 0.4, dArray[1] - 0.38 + dArray2[1] * 0.22, dArray[2] + dArray2[2] * 0.4};
            arrayList.add(new double[]{(double)(object[0] - 0.6), (double)(object[1] - 0.34), (double)(object[2] - 0.6), (double)(object[0] + 0.6), (double)(object[1] + 0.3), (double)(object[2] + 0.6)});
        }
        object = minecraft.m_91269_().m_110104_();
        abstractClientPlayer2 = null;
        boolean bl3 = false;
        Matrix4f matrix4f = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
        Matrix3f matrix3f = renderLevelStageEvent.getPoseStack().m_85850_().m_252943_();
        double[][] dArray3 = new double[4][3];
        double[] dArray4 = new double[3];
        double[] dArray5 = new double[3];
        for (Active active2 : list) {
            int n4;
            double d;
            Vec3 vec32;
            double d2;
            Entity entity;
            SpellVisualPacket spellVisualPacket = active2.packet;
            MaqueArcaneAssets.Art art = MaqueArcaneAssets.get(spellVisualPacket.spell().id);
            Entity entity2 = clientLevel.m_6815_(spellVisualPacket.ownerId());
            Entity entity3 = entity = entity2 != null && entity2.m_20148_().equals(spellVisualPacket.owner()) ? entity2 : null;
            if (entity != null && entity.m_213877_() || (d2 = (vec32 = entity != null && !art.motion().equals("scatter") ? entity.m_20318_(renderLevelStageEvent.getPartialTick()) : spellVisualPacket.start()).m_82557_(vec3)) > 9216.0) continue;
            double d3 = (float)(clientLevel.m_46467_() - active2.born) + renderLevelStageEvent.getPartialTick();
            double d4 = MaqueArcanePose.envelope(art.motion(), d3, MaqueSpellRenderer.lifetime(spellVisualPacket));
            double d5 = d = art.motion().equals("scatter") && !spellVisualPacket.phase().name().equals("CHARGE") ? MaqueArcaneTrail.opacity(d3) : d4;
            if (art.motion().equals("scatter") && !spellVisualPacket.phase().name().equals("CHARGE")) {
                d4 *= MaqueArcanePose.smooth((16.0 - d3) / 4.0);
            }
            if (d <= 0.008) continue;
            boolean bl4 = particleStatus == ParticleStatus.MINIMAL || d2 > 576.0;
            boolean bl5 = art.motion().equals("scatter") && !spellVisualPacket.phase().name().equals("CHARGE") && d3 > 0.0 && d3 < MaqueArcaneTrail.endTick();
            MaqueArcaneAssets.Surface[] surfaceArray = bl4 ? art.lite() : art.faces();
            int n5 = n4 = bl5 ? art.bones().length * MaqueArcaneTrail.maximumFaces(bl4) : 0;
            if (surfaceArray.length + n4 > n3) {
                bl4 = true;
                surfaceArray = art.lite();
                int n6 = n4 = bl5 ? art.bones().length * MaqueArcaneTrail.maximumFaces(true) : 0;
            }
            if (surfaceArray.length + n4 > n3) continue;
            n3 -= surfaceArray.length + n4;
            double d6 = entity == null ? 1.0 : SpellVisuals.radiusScale(entity, spellVisualPacket.spell().id);
            double d7 = Math.max(0.9, Math.min(1.35, Math.sqrt(Math.max(0.01, d6)))) * art.scale();
            Vec3 vec33 = spellVisualPacket.end().m_82546_(spellVisualPacket.start());
            if (vec33.f_82479_ * vec33.f_82479_ + vec33.f_82481_ * vec33.f_82481_ < 1.0E-4) {
                vec33 = entity == null ? new Vec3(0.0, 0.0, 1.0) : entity.m_20252_(renderLevelStageEvent.getPartialTick());
            }
            double d8 = Math.atan2(vec33.f_82479_, vec33.f_82481_);
            double d9 = Math.cos(d8);
            double d10 = Math.sin(d8);
            double d11 = Math.max(0.0, ((entity == null ? 0.6 : (double)entity.m_20205_()) + 0.24) / Math.sqrt(2.0) - 0.42) + Math.max(0.0, d7 - 1.0) * 0.6;
            double d12 = art.offset()[2] + Math.copySign(d11, art.offset()[2]);
            double d13 = art.offset()[1] + Math.max(0.0, (entity == null ? 1.8 : (double)entity.m_20206_()) - 1.8) * 0.5;
            MaqueArcanePose.Flight[] flightArray = new MaqueArcanePose.Flight[art.bones().length];
            ArrayList<MaqueArcaneTrail.Face> arrayList2 = new ArrayList<MaqueArcaneTrail.Face>(n4);
            if (art.motion().equals("scatter")) {
                double d14 = vec33.f_82479_ * d9 - vec33.f_82481_ * d10;
                double d15 = vec33.f_82479_ * d10 + vec33.f_82481_ * d9;
                double[] dArray6 = new double[]{d14 / d7, Math.max(-1.0, Math.min(6.0, (spellVisualPacket.end().f_82480_ - vec32.f_82480_ - d13) / d7)), Math.max(1.14, Math.min(24.0, (d15 - d12) / d7))};
                for (int i = 0; i < flightArray.length; ++i) {
                    flightArray[i] = MaqueArcanePose.flight(art.bones()[i], spellVisualPacket.phase().name().equals("CHARGE") ? 0.0 : d3, dArray6, true);
                    if (!bl5) continue;
                    arrayList2.addAll(MaqueArcaneTrail.faces(art.bones()[i], i, d3, dArray6, true, bl4));
                }
            }
            double[] dArray7 = entity == null ? null : MaqueSpellRenderer.bounds(entity.m_20191_().m_82400_(0.1));
            for (int i = 0; i < surfaceArray.length + arrayList2.size(); ++i) {
                int n7;
                double d16;
                double d17;
                double d18;
                double d19;
                double d20;
                MaqueArcaneTrail.Face face = i >= surfaceArray.length ? (MaqueArcaneTrail.Face)arrayList2.get(i - surfaceArray.length) : null;
                MaqueArcaneAssets.Surface surface = face == null ? surfaceArray[i] : face.surface();
                double d21 = face == null ? d4 : d;
                double d22 = surface.opacity() * d21;
                if (d22 < 0.014) continue;
                Arrays.fill(dArray5, 0.0);
                for (int j = 0; j < 4; ++j) {
                    if (face == null) {
                        MaqueArcanePose.transform(art.bones()[surface.bone()], d3, surface.points()[j], flightArray[surface.bone()], dArray4);
                    } else {
                        System.arraycopy(surface.points()[j], 0, dArray4, 0, 3);
                    }
                    double d23 = dArray4[0] * d7;
                    double d24 = dArray4[2] * d7 + d12;
                    dArray3[j][0] = vec32.f_82479_ + d23 * d9 + d24 * d10;
                    dArray3[j][1] = vec32.f_82480_ + dArray4[1] * d7 + d13;
                    dArray3[j][2] = vec32.f_82481_ - d23 * d10 + d24 * d9;
                    for (int k = 0; k < 3; ++k) {
                        int n8 = k;
                        dArray5[n8] = dArray5[n8] + dArray3[j][k] * 0.25;
                    }
                    d22 = Math.min(d22, surface.opacity() * d21 * (bl2 ? MaqueArcanePose.sightFade(dArray3[j], dArray, dArray2, bl) : 1.0));
                }
                if (bl && bl2 && MaqueArcanePose.rayTouches(dArray3, dArray, dArray2)) continue;
                d22 = Math.min(d22, surface.opacity() * d21 * (bl2 ? MaqueArcanePose.sightFade(dArray5, dArray, dArray2, bl) : 1.0));
                if (dArray7 != null) {
                    d22 *= MaqueArcanePose.bodyGap(dArray3, dArray7);
                }
                for (double[] dArray8 : arrayList) {
                    d22 *= MaqueArcanePose.bodyGap(dArray3, dArray8);
                }
                double d25 = d20 = face == null ? 1.0 : 0.0;
                if (face != null) {
                    for (double d26 : face.alpha()) {
                        d20 = Math.max(d20, d26);
                    }
                }
                if (d22 * d20 < 0.014 || (d19 = Math.sqrt((d18 = (dArray3[1][1] - dArray3[0][1]) * (dArray3[2][2] - dArray3[0][2]) - (dArray3[1][2] - dArray3[0][2]) * (dArray3[2][1] - dArray3[0][1])) * d18 + (d17 = (dArray3[1][2] - dArray3[0][2]) * (dArray3[2][0] - dArray3[0][0]) - (dArray3[1][0] - dArray3[0][0]) * (dArray3[2][2] - dArray3[0][2])) * d17 + (d16 = (dArray3[1][0] - dArray3[0][0]) * (dArray3[2][1] - dArray3[0][1]) - (dArray3[1][1] - dArray3[0][1]) * (dArray3[2][0] - dArray3[0][0])) * d16)) < 1.0E-10) continue;
                int n9 = n7 = surface.material() >= 4 ? (int)Math.round(255.0 * (0.72 + 0.15 * Math.abs(d17 / d19) + 0.13 * Math.abs(d16 / d19))) : 255;
                if (abstractClientPlayer2 == null || bl3 != surface.material() >= 4) {
                    bl3 = surface.material() >= 4;
                    abstractClientPlayer2 = object.m_6299_(bl3 ? MaqueArcaneLayers.REFRACTION : MaqueArcaneLayers.RADIANCE);
                }
                for (int j = 0; j < 4; ++j) {
                    float f = (float)((double)(surface.material() * 64 + (j == 1 || j == 2 ? 59 : 4)) / 384.0);
                    float f2 = (float)((double)(j >= 2 ? 251 : 4) / 256.0);
                    abstractClientPlayer2.m_252986_(matrix4f, (float)(dArray3[j][0] - dArray[0]), (float)(dArray3[j][1] - dArray[1]), (float)(dArray3[j][2] - dArray[2])).m_6122_(n7, n7, n7, (int)Math.round(d22 * (face == null ? 1.0 : face.alpha()[j]) * 255.0)).m_7421_(f, f2).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(matrix3f, (float)(d18 / d19), (float)(d17 / d19), (float)(d16 / d19)).m_5752_();
                }
            }
        }
        object.m_109912_(MaqueArcaneLayers.REFRACTION);
        object.m_109912_(MaqueArcaneLayers.RADIANCE);
    }

    private static double[] bounds(AABB aABB) {
        return new double[]{aABB.f_82288_, aABB.f_82289_, aABB.f_82290_, aABB.f_82291_, aABB.f_82292_, aABB.f_82293_};
    }

    private record Active(SpellVisualPacket packet, long born, long updated) {
    }
}

