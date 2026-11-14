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
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.joml.Quaternionf
 */
package net.farmingtales.ftboss.client.qizaiarcane;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Comparator;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneAssets;
import net.farmingtales.ftboss.client.qizaiarcane.ArcanePainter;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneRig;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneSigilPolicy;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.QizaiPrismRules;
import net.farmingtales.ftboss.util.QizaiRules;
import net.farmingtales.ftboss.util.QizaiSweepRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class ArcaneCompletedVisuals {
    private ArcaneCompletedVisuals() {
    }

    public static CompoundTag legacyCue(EasterBossEntity easterBossEntity) {
        int n;
        CompoundTag compoundTag = easterBossEntity.getSkillCue();
        return ArcaneSigilPolicy.suppressLegacy(n, ArcaneAssets.kind(n = compoundTag.m_128451_("qizai")) != null) ? new CompoundTag() : compoundTag;
    }

    private static Vec3 point(CompoundTag compoundTag, String string) {
        return new Vec3(compoundTag.m_128459_(string + "X"), compoundTag.m_128459_(string + "Y"), compoundTag.m_128459_(string + "Z"));
    }

    private static boolean valid(Vec3 vec3) {
        return Double.isFinite(vec3.f_82479_) && Double.isFinite(vec3.f_82480_) && Double.isFinite(vec3.f_82481_);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static int show(ArcaneRig.Effect effect, Vec3 vec3, String string, double d, double d2, boolean bl, int n, RenderLevelStageEvent renderLevelStageEvent, float f) {
        if (effect == null || !ArcaneCompletedVisuals.valid(vec3) || n <= 0 && !bl || !Double.isFinite(d) || d2 <= 0.0 || d2 > 16.0) {
            return 0;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        Vec3 vec32 = renderLevelStageEvent.getCamera().m_90583_();
        if (vec3.m_82557_(vec32) > 16384.0 || !renderLevelStageEvent.getFrustum().m_113029_(new AABB(vec3, vec3).m_82400_(Math.max(2.0, effect.radius() * d2)))) {
            return 0;
        }
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        poseStack.m_85836_();
        try {
            poseStack.m_85837_(vec3.f_82479_ - vec32.f_82479_, vec3.f_82480_ - vec32.f_82480_, vec3.f_82481_ - vec32.f_82481_);
            poseStack.m_85841_((float)d2, 1.0f, (float)d2);
            Vec3 vec33 = new Vec3(d2, 1.0, d2);
            MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
            if (bl) {
                ArcanePainter.scaled(effect, string, d, poseStack, (MultiBufferSource)bufferSource, vec3, new Quaternionf(), false, true, false, 240, 0xF000F0, f, vec33);
            }
            int n2 = ArcanePainter.scaled(effect, string, d, poseStack, (MultiBufferSource)bufferSource, vec3, new Quaternionf(), vec3.m_82557_(vec32) < 400.0, false, true, Math.max(0, n), 0xF000F0, f, vec33);
            return n2;
        }
        finally {
            poseStack.m_85849_();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static int ray(ArcaneRig.Effect effect, Vec3 vec3, Vec3 vec32, int n, RenderLevelStageEvent renderLevelStageEvent, float f) {
        if (!ArcaneCompletedVisuals.valid(vec3) || !ArcaneCompletedVisuals.valid(vec32) || vec3.m_82557_(vec32) > 4096.0 || n <= 0 || f <= 0.0f || !renderLevelStageEvent.getFrustum().m_113029_(new AABB(vec3, vec32).m_82400_(1.3))) {
            return 0;
        }
        PoseStack poseStack = renderLevelStageEvent.getPoseStack();
        Vec3 vec33 = renderLevelStageEvent.getCamera().m_90583_();
        poseStack.m_85836_();
        try {
            poseStack.m_85837_(vec3.f_82479_ - vec33.f_82479_, vec3.f_82480_ - vec33.f_82480_, vec3.f_82481_ - vec33.f_82481_);
            int n2 = ArcanePainter.beam(effect, vec3, vec32, poseStack, (MultiBufferSource)Minecraft.m_91087_().m_91269_().m_110104_(), n, f);
            return n2;
        }
        finally {
            poseStack.m_85849_();
        }
    }

    @SubscribeEvent
    public static void draw(RenderLevelStageEvent renderLevelStageEvent) {
        Object object22;
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null) {
            return;
        }
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        double d = (float)clientLevel.m_46467_() + renderLevelStageEvent.getPartialTick();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n = particleStatus == ParticleStatus.MINIMAL ? 3200 : (particleStatus == ParticleStatus.DECREASED ? 7000 : 13000);
        ArrayList<Object> arrayList = new ArrayList<Object>();
        for (Object object22 : clientLevel.m_104735_()) {
            Object object3;
            if (!(object22 instanceof EasterBossEntity) || ((EasterBossEntity)(object3 = (EasterBossEntity)object22)).getVariant() != 11 || !object3.m_6084_() || object3.m_213877_() || object3.m_20145_() || !(object3.m_20238_(vec3) < 16384.0)) continue;
            arrayList.add(object3);
        }
        arrayList.sort(Comparator.comparingDouble(easterBossEntity -> easterBossEntity.m_20238_(vec3)));
        int n2 = 0;
        for (Object object3 : arrayList) {
            double d2;
            double d3;
            Vec3 vec32;
            Vec3 vec33;
            ListTag listTag;
            if (n2++ >= 12) break;
            CompoundTag object4 = ((EasterBossEntity)object3).getSkillCue();
            int n3 = object4.m_128451_("qizai");
            int n4 = object4.m_128451_("duration");
            double d4 = d - (double)object4.m_128454_("start");
            ArcaneRig.Effect effect = ArcaneAssets.kind(n3);
            if (effect == null || n4 <= 0 || d4 < 0.0 || d4 >= (double)n4) continue;
            boolean bl = object4.m_128471_("released");
            double d5 = d4 / (double)n4;
            String string = bl ? "release" : "charge";
            double d6 = d5 * (bl ? 1.1 : 1.4);
            if (n3 == 2 || n3 == 3 || n3 == 4) {
                listTag = object4.m_128437_("marks", 10);
                for (int n5 = 0; n5 < Math.min(5, listTag.size()); ++n5) {
                    CompoundTag d7 = listTag.m_128728_(n5);
                    vec33 = new Vec3(d7.m_128459_("x"), d7.m_128459_("y"), d7.m_128459_("z"));
                    n -= ArcaneCompletedVisuals.show(effect, vec33, string, d6, n3 == 4 ? 1.0 : QizaiRules.radius(n3) / effect.radius(), n3 != 4, n, renderLevelStageEvent, 1.0f);
                }
                if (n3 != 4) continue;
                n -= ArcaneCompletedVisuals.show(effect, object3.m_20318_(renderLevelStageEvent.getPartialTick()), "release", d6, 1.0, false, n, renderLevelStageEvent, 1.0f);
                continue;
            }
            if (!(n3 != 5 && n3 != 6 || bl)) {
                listTag = object4.m_128437_("marks", 10);
                for (int d8 = 0; d8 < Math.min(5, listTag.size()); ++d8) {
                    CompoundTag i = listTag.m_128728_(d8);
                    vec33 = new Vec3(i.m_128459_("x"), i.m_128459_("y"), i.m_128459_("z"));
                    n -= ArcaneCompletedVisuals.show(effect, vec33, "charge", d5 * 1.4, QizaiRules.radius(n3) / effect.radius(), true, n, renderLevelStageEvent, 1.0f);
                }
                continue;
            }
            if (n3 == 7) {
                Vec3 vec34;
                Vec3 vec35;
                listTag = ArcaneCompletedVisuals.point(object4, "anchor");
                int n8 = object4.m_128451_("completePhase");
                double d7 = Math.max(0.0, Math.min(1.0, (d - (double)object4.m_128454_("windupStart")) / (double)Math.max(1, object4.m_128451_("windupDuration"))));
                n -= ArcaneCompletedVisuals.show(effect, (Vec3)listTag, n8 == 4 ? "release" : (bl ? "idle" : "charge"), n8 == 4 ? d5 * 1.1 : (bl ? d / 20.0 % 2.4 : d7 * 1.4), 1.0, false, n, renderLevelStageEvent, 1.0f);
                if (n8 >= 3) continue;
                vec32 = object4.m_128437_("lengths", 6);
                int n5 = object4.m_128451_("round");
                int n6 = object4.m_128451_("elapsed");
                for (int i = 0; i < Math.min(QizaiSweepRules.arms(n5), vec32.size()) && n > 0; n -= ArcaneCompletedVisuals.ray(effect, vec35, vec34, n, renderLevelStageEvent, bl ? 0.95f : 0.3f), ++i) {
                    d3 = Math.toRadians(QizaiSweepRules.angle(object4.m_128459_("initialAngle"), n6, n5, i));
                    d2 = Math.max(0.0, Math.min(32.0, vec32.m_128772_(i)));
                    vec35 = listTag.m_82520_(0.0, QizaiSweepRules.height(n5, i), 0.0);
                    vec34 = vec35.m_82520_(Math.cos(d3) * d2, 0.0, Math.sin(d3) * d2);
                }
                continue;
            }
            if (n3 == 8) {
                listTag = new Vec3(object4.m_128459_("x"), object4.m_128459_("y"), object4.m_128459_("z"));
                double d8 = object4.m_128459_("radius");
                if (!Double.isFinite(d8) || d8 <= 0.0 || d8 > 48.0) continue;
                n -= ArcaneCompletedVisuals.show(effect, (Vec3)listTag, string, d6, d8 / effect.radius(), true, n, renderLevelStageEvent, 1.0f);
                vec33 = new Vec3(listTag.f_82479_, object4.m_128459_("apex"), listTag.f_82481_);
                n -= ArcaneCompletedVisuals.show(effect, vec33, string, d6, 0.44, false, n, renderLevelStageEvent, bl ? 0.65f : 0.8f);
                if (!bl) continue;
                n -= ArcaneCompletedVisuals.ray(effect, vec33, (Vec3)listTag, n, renderLevelStageEvent, (float)(1.0 - d5));
                continue;
            }
            if (n3 != 9 && n3 != 10) continue;
            listTag = object4.m_128437_("beams", 10);
            int n7 = n3 == 9 ? 6 : 48;
            for (int i = 0; i < Math.min(n7, listTag.size()) && n > 0; ++i) {
                vec33 = listTag.m_128728_(i);
                vec32 = ArcaneCompletedVisuals.point((CompoundTag)vec33, "");
                Vec3 vec36 = ArcaneCompletedVisuals.point((CompoundTag)vec33, "end");
                double d9 = n3 == 9 ? 0.0 : (double)vec33.m_128451_("spawn");
                d3 = n3 == 9 ? (double)QizaiPrismRules.fireTick(i, Math.max(1, object4.m_128451_("trackTicks")), Math.max(1, object4.m_128451_("lockTicks")), Math.max(1, object4.m_128451_("fireGap"))) : (double)vec33.m_128451_("fire");
                d2 = d4 - d3;
                int n8 = Math.max(1, object4.m_128451_("fadeTicks"));
                if (d4 < d9 || d2 >= (double)n8) continue;
                float f = d2 < 0.0 ? 0.4f : (float)Math.max(0.0, 1.0 - d2 / (double)n8);
                n -= ArcaneCompletedVisuals.show(effect, vec32, d2 < 0.0 ? "charge" : "release", d2 < 0.0 ? Math.min(1.4, (d4 - d9) / Math.max(1.0, d3 - d9) * 1.4) : d2 / (double)n8 * 1.1, 0.42, false, n, renderLevelStageEvent, f);
                n -= ArcaneCompletedVisuals.ray(effect, vec32, vec36, n, renderLevelStageEvent, f);
            }
        }
        object22 = minecraft.m_91269_().m_110104_();
        for (String string : ArcaneAssets.IDS) {
            object22.m_109912_(RenderType.m_110470_((ResourceLocation)new ResourceLocation("ftboss", "qizai_arcane/" + string + ".png")));
        }
    }
}

