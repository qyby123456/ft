/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
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
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.maque.MaqueArcaneAssets;
import net.farmingtales.ftboss.client.maque.MaqueArcaneLayers;
import net.farmingtales.ftboss.client.maque.MaqueArcanePose;
import net.farmingtales.ftboss.client.maque.MaqueHumanoidAnimations;
import net.farmingtales.ftboss.client.maque.MaqueSupportAssets;
import net.farmingtales.ftboss.client.maque.MaqueSupportPose;
import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.item.weapons.MaqueBlessingFeatherItem;
import net.farmingtales.ftboss.maque.support.MaqueSupportPacket;
import net.farmingtales.ftboss.skill.MaqueFeatherPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
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
public final class MaqueSupportRenderer {
    private static final Map<String, Active> ACTIVE = new LinkedHashMap<String, Active>();
    private static final Map<String, Long> SEEN = new LinkedHashMap<String, Long>();
    private static ClientLevel world;
    private static long ambientSequence;

    private MaqueSupportRenderer() {
    }

    private static void bind(ClientLevel clientLevel) {
        if (world != clientLevel) {
            ACTIVE.clear();
            SEEN.clear();
            world = clientLevel;
            if (clientLevel == null) {
                MaqueHumanoidAnimations.clear();
            }
        }
    }

    public static void accept(MaqueSupportPacket maqueSupportPacket) {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !maqueSupportPacket.valid() || !clientLevel.m_46472_().m_135782_().equals((Object)maqueSupportPacket.dimension())) {
            return;
        }
        MaqueSupportRenderer.bind(clientLevel);
        String string = String.valueOf(maqueSupportPacket.source()) + ":" + maqueSupportPacket.sequence();
        if (SEEN.containsKey(string)) {
            return;
        }
        MaqueHumanoidAnimations.support((Level)clientLevel, maqueSupportPacket);
        SEEN.put(string, clientLevel.m_46467_());
        if (SEEN.size() > 256) {
            SEEN.remove(SEEN.keySet().iterator().next());
        }
        MaqueSupportRenderer.add(maqueSupportPacket, clientLevel.m_46467_());
    }

    private static void add(MaqueSupportPacket maqueSupportPacket, long l) {
        String string = String.valueOf(maqueSupportPacket.source()) + ":" + String.valueOf(maqueSupportPacket.target()) + ":" + maqueSupportPacket.kind();
        Active active = ACTIVE.get(string);
        boolean bl = active != null && l - active.updated < 3L && l - active.born < (long)(MaqueSupportAssets.get(maqueSupportPacket.kind()).duration() - 8);
        ACTIVE.put(string, new Active(maqueSupportPacket, bl ? active.born : l, l));
        if (ACTIVE.size() > 32) {
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        }
    }

    public static void receiveFeather(MaqueFeatherPacket maqueFeatherPacket) {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !clientLevel.m_46472_().m_135782_().equals((Object)maqueFeatherPacket.dimension())) {
            return;
        }
        MaqueSupportRenderer.bind(clientLevel);
        Entity entity = clientLevel.m_6815_(maqueFeatherPacket.casterId());
        UUID uUID = entity == null ? new UUID(0L, 0L) : entity.m_20148_();
        MaqueSupportPacket maqueSupportPacket = new MaqueSupportPacket(maqueFeatherPacket.dimension(), uUID, maqueFeatherPacket.casterId(), uUID, maqueFeatherPacket.casterId(), maqueFeatherPacket.position(), maqueFeatherPacket.position(), maqueFeatherPacket.wind() ? 8 : 7, --ambientSequence);
        if (maqueSupportPacket.valid()) {
            MaqueSupportRenderer.add(maqueSupportPacket, clientLevel.m_46467_());
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        MaqueSupportRenderer.bind(clientLevel);
        if (clientLevel != null) {
            long l = clientLevel.m_46467_();
            ACTIVE.values().removeIf(active -> l < active.born || l - active.born >= (long)MaqueSupportAssets.get(active.packet.kind()).duration());
            SEEN.values().removeIf(l2 -> l < l2 || l - l2 > 240L);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel() == world) {
            MaqueSupportRenderer.bind(null);
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        MaqueSupportRenderer.bind(null);
    }

    private static Entity resolve(ClientLevel clientLevel, int n, UUID uUID) {
        Entity entity = clientLevel.m_6815_(n);
        return entity != null && entity.m_20148_().equals(uUID) && !entity.m_213877_() ? entity : null;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || !MaqueArcaneLayers.ready()) {
            return;
        }
        boolean bl = SpellBloomRenderer.begin(renderLevelStageEvent, !ACTIVE.isEmpty());
        try {
            MaqueSupportRenderer.renderEffects(renderLevelStageEvent);
        }
        finally {
            SpellBloomRenderer.end(bl);
        }
    }

    private static void renderEffects(RenderLevelStageEvent renderLevelStageEvent) {
        Player player2;
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null || world != clientLevel || minecraft.f_91074_ == null) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        Vector3f vector3f = new Vector3f(0.0f, 0.0f, -1.0f).rotate((Quaternionfc)renderLevelStageEvent.getCamera().m_253121_());
        double[] dArray = new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
        double[] dArray2 = new double[]{vector3f.x, vector3f.y, vector3f.z};
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        boolean bl2 = !bl || FirstPersonSpellVisibility.cameraFactor(0.0, bl) < 1.0;
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n = particleStatus == ParticleStatus.MINIMAL ? 8000 : (particleStatus == ParticleStatus.DECREASED ? 12000 : 18000);
        ArrayList<Active> arrayList = new ArrayList<Active>(ACTIVE.values().stream().sorted(Comparator.comparingDouble(active -> active.packet.end().m_82557_(vec3))).limit(8L).toList());
        int n2 = 0;
        for (Player player2 : clientLevel.m_6907_()) {
            if (n2 >= 4 || !player2.m_6084_() || player2.m_5833_() || player2.m_20177_((Player)minecraft.f_91074_) || player2.m_20182_().m_82557_(vec3) > 1024.0 || !(player2.m_21205_().m_41720_() instanceof MaqueBlessingFeatherItem) && !(player2.m_21206_().m_41720_() instanceof MaqueBlessingFeatherItem)) continue;
            MaqueSupportPacket maqueSupportPacket = new MaqueSupportPacket(clientLevel.m_46472_().m_135782_(), player2.m_20148_(), player2.m_19879_(), player2.m_20148_(), player2.m_19879_(), player2.m_20182_(), player2.m_20182_(), 10, 0L);
            arrayList.add(new Active(maqueSupportPacket, clientLevel.m_46467_() - (long)Math.floorMod(player2.f_19797_, 64), clientLevel.m_46467_()));
            ++n2;
        }
        if (arrayList.isEmpty()) {
            return;
        }
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        player2 = null;
        boolean bl3 = false;
        Matrix4f matrix4f = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
        Matrix3f matrix3f = renderLevelStageEvent.getPoseStack().m_85850_().m_252943_();
        double[][] dArray3 = new double[4][3];
        double[] dArray4 = new double[3];
        double[] dArray5 = new double[3];
        for (Active active2 : arrayList) {
            double d;
            MaqueArcaneAssets.Surface[] surfaceArray;
            Vec3 vec32;
            MaqueSupportPacket maqueSupportPacket = active2.packet;
            MaqueSupportAssets.Effect effect = MaqueSupportAssets.get(maqueSupportPacket.kind());
            double d2 = (float)(clientLevel.m_46467_() - active2.born) + renderLevelStageEvent.getPartialTick();
            Entity entity = MaqueSupportRenderer.resolve(clientLevel, maqueSupportPacket.targetId(), maqueSupportPacket.target());
            Entity entity2 = MaqueSupportRenderer.resolve(clientLevel, maqueSupportPacket.sourceId(), maqueSupportPacket.source());
            Vec3 vec33 = entity == null || maqueSupportPacket.kind() == 1 || maqueSupportPacket.kind() == 7 ? maqueSupportPacket.end() : entity.m_20318_(renderLevelStageEvent.getPartialTick());
            Vec3 vec34 = vec32 = entity2 == null ? maqueSupportPacket.start() : entity2.m_20318_(renderLevelStageEvent.getPartialTick());
            if (vec33.m_82557_(vec3) > 9216.0) continue;
            boolean bl4 = particleStatus == ParticleStatus.MINIMAL || vec33.m_82557_(vec3) > 576.0 || maqueSupportPacket.kind() == 10;
            MaqueArcaneAssets.Surface[] surfaceArray2 = surfaceArray = bl4 ? effect.lite() : effect.faces();
            if (surfaceArray.length > n) {
                surfaceArray = effect.lite();
            }
            if (surfaceArray.length > n) continue;
            n -= surfaceArray.length;
            double d3 = entity == null || maqueSupportPacket.kind() == 7 || maqueSupportPacket.kind() == 10 ? 1.0 : Math.max(1.0, Math.min(1.6, ((double)entity.m_20205_() + 0.24) / 0.84));
            double d4 = d = entity == null || maqueSupportPacket.kind() == 7 ? 0.0 : Math.max(0.0, (double)entity.m_20206_() - 1.8) * 0.5;
            if (maqueSupportPacket.kind() == 10 && entity instanceof Player) {
                Player player3 = (Player)entity;
                double d5 = (player3.m_21205_().m_41720_() instanceof MaqueBlessingFeatherItem ? player3.m_5737_() : player3.m_5737_().m_20828_()) == HumanoidArm.RIGHT ? 1.0 : -1.0;
                double d6 = Math.toRadians(player3.f_20883_);
                vec33 = vec33.m_82520_(Math.cos(d6) * d5 * 0.42 - Math.sin(d6) * 0.22, 0.0, Math.sin(d6) * d5 * 0.42 + Math.cos(d6) * 0.22);
            }
            double d7 = entity2 == null ? 1.16 : (double)entity2.m_20206_() * 0.6444444444444444;
            double[] dArray6 = new double[]{(vec32.f_82479_ - vec33.f_82479_) / d3, (vec32.f_82480_ + d7 - vec33.f_82480_ - d - 1.16 * d3) / d3, (vec32.f_82481_ - vec33.f_82481_) / d3};
            boolean bl5 = maqueSupportPacket.sourceId() != maqueSupportPacket.targetId() && vec32.m_82557_(vec33) > 0.04 && vec32.m_82557_(vec33) <= 4096.0;
            double[] dArray7 = new double[]{dArray[0] + dArray2[0] * 0.4, dArray[1] - 0.38 + dArray2[1] * 0.22, dArray[2] + dArray2[2] * 0.4};
            double[] dArray8 = new double[]{dArray7[0] - 0.6, dArray7[1] - 0.34, dArray7[2] - 0.6, dArray7[0] + 0.6, dArray7[1] + 0.3, dArray7[2] + 0.6};
            double[] dArray9 = entity == null || maqueSupportPacket.kind() == 10 ? null : MaqueSupportRenderer.bounds(entity.m_20191_().m_82400_(0.1));
            double[] dArray10 = entity2 == null || entity2 == entity ? null : MaqueSupportRenderer.bounds(entity2.m_20191_().m_82400_(0.1));
            double[] dArray11 = new double[effect.bones().length];
            MaqueSupportPose.Frame[] frameArray = new MaqueSupportPose.Frame[effect.bones().length];
            for (int i = 0; i < effect.bones().length; ++i) {
                MaqueArcaneAssets.Bone bone = effect.bones()[i];
                dArray11[i] = MaqueSupportPose.visibility(bone, d2, effect.duration());
                frameArray[i] = MaqueSupportPose.frame(bone.role().kind(), bone.role().lane(), d2, effect.duration());
            }
            for (MaqueArcaneAssets.Surface surface : surfaceArray) {
                double d8;
                double d9;
                double d10;
                double d11;
                double d12;
                MaqueArcaneAssets.Bone bone = effect.bones()[surface.bone()];
                if (bone.role().kind().equals("link") && !bl5 || (d12 = surface.opacity() * dArray11[surface.bone()]) < 0.014) continue;
                Arrays.fill(dArray5, 0.0);
                for (int i = 0; i < 4; ++i) {
                    MaqueSupportPose.transform(bone, surface.points()[i], frameArray[surface.bone()], dArray6, dArray4);
                    dArray3[i][0] = vec33.f_82479_ + dArray4[0] * d3;
                    dArray3[i][1] = vec33.f_82480_ + d + dArray4[1] * d3;
                    dArray3[i][2] = vec33.f_82481_ + dArray4[2] * d3;
                    for (int j = 0; j < 3; ++j) {
                        int n3 = j;
                        dArray5[n3] = dArray5[n3] + dArray3[i][j] * 0.25;
                    }
                    d12 = Math.min(d12, surface.opacity() * dArray11[surface.bone()] * (bl2 ? MaqueArcanePose.sightFade(dArray3[i], dArray, dArray2, bl) : 1.0));
                }
                if (bl && bl2 && MaqueArcanePose.rayTouches(dArray3, dArray, dArray2)) continue;
                d12 *= bl2 ? MaqueArcanePose.sightFade(dArray5, dArray, dArray2, bl) : 1.0;
                if (dArray9 != null) {
                    d12 *= MaqueArcanePose.bodyGap(dArray3, dArray9);
                }
                if (dArray10 != null) {
                    d12 *= MaqueArcanePose.bodyGap(dArray3, dArray10);
                }
                if (bl) {
                    d12 *= MaqueArcanePose.bodyGap(dArray3, dArray8);
                }
                if (d12 < 0.014 || (d11 = Math.sqrt((d10 = (dArray3[1][1] - dArray3[0][1]) * (dArray3[2][2] - dArray3[0][2]) - (dArray3[1][2] - dArray3[0][2]) * (dArray3[2][1] - dArray3[0][1])) * d10 + (d9 = (dArray3[1][2] - dArray3[0][2]) * (dArray3[2][0] - dArray3[0][0]) - (dArray3[1][0] - dArray3[0][0]) * (dArray3[2][2] - dArray3[0][2])) * d9 + (d8 = (dArray3[1][0] - dArray3[0][0]) * (dArray3[2][1] - dArray3[0][1]) - (dArray3[1][1] - dArray3[0][1]) * (dArray3[2][0] - dArray3[0][0])) * d8)) < 1.0E-10) continue;
                if (player2 == null || bl3 != surface.material() >= 4) {
                    bl3 = surface.material() >= 4;
                    player2 = bufferSource.m_6299_(bl3 ? MaqueArcaneLayers.REFRACTION : MaqueArcaneLayers.RADIANCE);
                }
                for (int i = 0; i < 4; ++i) {
                    player2.m_252986_(matrix4f, (float)(dArray3[i][0] - dArray[0]), (float)(dArray3[i][1] - dArray[1]), (float)(dArray3[i][2] - dArray[2])).m_6122_(255, 255, 255, (int)Math.round(d12 * MaqueSupportPose.linkGain(bone, d2, effect.duration(), surface.points()[i], maqueSupportPacket.kind() == 6) * 255.0)).m_7421_((float)((double)(surface.material() * 64 + (i == 1 || i == 2 ? 59 : 4)) / 384.0), (float)((double)(i >= 2 ? 251 : 4) / 256.0)).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(matrix3f, (float)(d10 / d11), (float)(d9 / d11), (float)(d8 / d11)).m_5752_();
                }
            }
        }
        bufferSource.m_109912_(MaqueArcaneLayers.REFRACTION);
        bufferSource.m_109912_(MaqueArcaneLayers.RADIANCE);
    }

    private static double[] bounds(AABB aABB) {
        return new double[]{aABB.f_82288_, aABB.f_82289_, aABB.f_82290_, aABB.f_82291_, aABB.f_82292_, aABB.f_82293_};
    }

    private record Active(MaqueSupportPacket packet, long born, long updated) {
    }
}

