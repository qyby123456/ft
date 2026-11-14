/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.joml.Quaternionf
 */
package net.farmingtales.ftboss.client.qizaiarcane;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneAssets;
import net.farmingtales.ftboss.client.qizaiarcane.ArcanePainter;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneRig;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneSafety;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneWardClock;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneWardPacket;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiCrystalEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class ArcaneActorVisuals {
    private static final Map<UUID, Status> wards = new LinkedHashMap<UUID, Status>();
    private static ClientLevel scene;

    private ArcaneActorVisuals() {
    }

    public static void receive(ArcaneWardPacket arcaneWardPacket) {
        Status status;
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !clientLevel.m_46472_().m_135782_().equals((Object)arcaneWardPacket.dimension())) {
            return;
        }
        if (scene != clientLevel) {
            wards.clear();
            scene = clientLevel;
        }
        if (!wards.containsKey(arcaneWardPacket.identity()) && wards.size() >= 128) {
            wards.remove(wards.keySet().iterator().next());
        }
        if ((status = wards.get(arcaneWardPacket.identity())) == null || status.entityId() != arcaneWardPacket.entityId()) {
            status = new Status(arcaneWardPacket.entityId(), new ArcaneWardClock());
            wards.put(arcaneWardPacket.identity(), status);
        }
        status.clock().update(arcaneWardPacket.tick(), clientLevel.m_46467_(), arcaneWardPacket.active());
    }

    @SubscribeEvent
    public static void unloaded(LevelEvent.Unload unload) {
        if (unload.getLevel() == scene) {
            wards.clear();
            scene = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void draw(RenderLevelStageEvent renderLevelStageEvent) {
        EasterBossEntity easterBossEntity;
        boolean bl;
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null) {
            return;
        }
        if (scene != clientLevel) {
            wards.clear();
            scene = clientLevel;
        }
        double d = (float)clientLevel.m_46467_() + renderLevelStageEvent.getPartialTick();
        wards.values().removeIf(status -> status.clock().expired(d));
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        boolean bl2 = bl = particleStatus == ParticleStatus.ALL;
        int n = particleStatus == ParticleStatus.MINIMAL ? 900 : (particleStatus == ParticleStatus.DECREASED ? 1800 : 3200);
        ArrayList<Entity> arrayList = new ArrayList<Entity>();
        for (Entity entity2 : clientLevel.m_104735_()) {
            if ((!(entity2 instanceof EasterBossEntity) || (easterBossEntity = (EasterBossEntity)entity2).getVariant() != 11) && !(entity2 instanceof QizaiCrystalEntity) || !entity2.m_6084_() || entity2.m_213877_() || entity2.m_20275_(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_) > 2304.0 || !renderLevelStageEvent.getFrustum().m_113029_(entity2.m_20191_().m_82400_(Math.max(1.5, 2.5 * (double)ArcaneSafety.scale(entity2.m_20205_(), 0.6))))) continue;
            arrayList.add(entity2);
        }
        arrayList.sort(Comparator.comparingInt(entity -> ArcaneActorVisuals.priority(entity, d)).thenComparingDouble(entity -> entity.m_20275_(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_)));
        for (Entity entity2 : arrayList) {
            QizaiCrystalEntity object;
            EasterBossEntity easterBossEntity2;
            if (n <= 0) break;
            if (entity2 instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)entity2).getVariant() == 11) {
                Object object2;
                easterBossEntity2 = easterBossEntity.m_20318_(renderLevelStageEvent.getPartialTick());
                Status status2 = wards.get(easterBossEntity.m_20148_());
                if (status2 != null && status2.entityId() == easterBossEntity.m_19879_() && status2.clock().active(d)) {
                    object2 = ArcaneAssets.get("qizai_invulnerable_aegis");
                    float f = ArcaneSafety.scale(easterBossEntity.m_20205_(), 0.6);
                    float f2 = ArcaneSafety.scale(easterBossEntity.m_20206_(), 1.8);
                    if (object2 != null) {
                        poseStack.m_85836_();
                        try {
                            poseStack.m_85837_(((Vec3)easterBossEntity2).f_82479_ - vec3.f_82479_, ((Vec3)easterBossEntity2).f_82480_ - vec3.f_82480_, ((Vec3)easterBossEntity2).f_82481_ - vec3.f_82481_);
                            poseStack.m_85841_(f, f2, f);
                            n -= ArcanePainter.scaled((ArcaneRig.Effect)object2, "idle", d / 20.0 % 2.4, poseStack, (MultiBufferSource)bufferSource, (Vec3)easterBossEntity2, new Quaternionf(), bl, false, true, n, 0xF000F0, status2.clock().opacity(d), new Vec3((double)f, (double)f2, (double)f));
                        }
                        finally {
                            poseStack.m_85849_();
                        }
                    }
                }
                object2 = easterBossEntity.getSkillCue();
                int n2 = object2.m_128451_("qizai");
                int n3 = object2.m_128451_("duration");
                double d2 = d - (double)object2.m_128454_("start");
                if (n2 < 1 || n2 > 10 || n3 <= 0 || !(d2 >= 0.0) || !(d2 < (double)n3)) continue;
                double d3 = Math.toRadians(-easterBossEntity.m_146908_());
                double d4 = Math.max(0.45, (double)easterBossEntity.m_20205_() * 0.6);
                Vec3 vec32 = easterBossEntity2.m_82520_(Math.sin(d3) * d4, (double)easterBossEntity.m_20206_() * 0.6, Math.cos(d3) * d4);
                boolean bl3 = object2.m_128471_("released");
                n -= ArcaneActorVisuals.emission(vec32, bl3 ? "release" : "charge", d2 / (double)n3 * (bl3 ? 1.1 : 1.4), bl3 ? 0.75f : 0.55f, bl, n, renderLevelStageEvent, (MultiBufferSource)bufferSource);
                continue;
            }
            if (!(entity2 instanceof QizaiCrystalEntity) || (easterBossEntity2 = (object = (QizaiCrystalEntity)entity2).getOwner()) == null || easterBossEntity2.getVariant() != 11 || !easterBossEntity2.m_6084_()) continue;
            float f = 0.17f + 0.11f * (float)((Math.sin(d * 0.14 + (double)object.role()) + 1.0) * 0.5);
            n -= ArcaneActorVisuals.emission(object.m_20318_(renderLevelStageEvent.getPartialTick()).m_82520_(0.0, -0.12, 0.0), "idle", d / 20.0 % 2.4, f, false, n, renderLevelStageEvent, (MultiBufferSource)bufferSource);
        }
        for (Iterator<Object> iterator : new String[]{"qizai_invulnerable_aegis", "qizai_resonance_emission"}) {
            bufferSource.m_109912_(RenderType.m_110470_((ResourceLocation)new ResourceLocation("ftboss", "qizai_arcane/" + iterator + ".png")));
        }
    }

    private static int priority(Entity entity, double d) {
        Status status = wards.get(entity.m_20148_());
        return status != null && status.entityId() == entity.m_19879_() && status.clock().active(d) ? 0 : (entity instanceof EasterBossEntity ? 1 : 2);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static int emission(Vec3 vec3, String string, double d, float f, boolean bl, int n, RenderLevelStageEvent renderLevelStageEvent, MultiBufferSource multiBufferSource) {
        ArcaneRig.Effect effect = ArcaneAssets.get("qizai_resonance_emission");
        if (effect == null || n <= 0) {
            return 0;
        }
        Vec3 vec32 = renderLevelStageEvent.getCamera().m_90583_();
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        poseStack.m_85836_();
        try {
            poseStack.m_85837_(vec3.f_82479_ - vec32.f_82479_, vec3.f_82480_ - vec32.f_82480_, vec3.f_82481_ - vec32.f_82481_);
            int n2 = ArcanePainter.effect(effect, string, d, poseStack, multiBufferSource, vec3, new Quaternionf(), bl, false, true, n, 0xF000F0, f);
            return n2;
        }
        finally {
            poseStack.m_85849_();
        }
    }

    private record Status(int entityId, ArcaneWardClock clock) {
    }
}

