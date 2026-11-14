/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.client.qizaiarcane;

import java.util.ArrayList;
import java.util.Comparator;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneAssets;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneCompletedVisuals;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneRig;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class ArcaneArsenalVisuals {
    private ArcaneArsenalVisuals() {
    }

    public static boolean available(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getVariant() == 11 && easterBossEntity.m_6084_() && !easterBossEntity.m_213877_() && ArcaneAssets.get("qizai_prism_lock") != null && ArcaneAssets.get("qizai_crimson_prayer") != null && ArcaneAssets.get("qizai_invulnerable_aegis") != null;
    }

    private static Vec3 point(CompoundTag compoundTag, String string) {
        return new Vec3(compoundTag.m_128459_(string + "x"), compoundTag.m_128459_(string + "y"), compoundTag.m_128459_(string + "z"));
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
        int n = particleStatus == ParticleStatus.MINIMAL ? 2200 : (particleStatus == ParticleStatus.DECREASED ? 5000 : 9000);
        ArrayList<EasterBossEntity> arrayList = new ArrayList<EasterBossEntity>();
        for (Object object22 : clientLevel.m_104735_()) {
            EasterBossEntity easterBossEntity2;
            if (!(object22 instanceof EasterBossEntity) || !ArcaneArsenalVisuals.available(easterBossEntity2 = (EasterBossEntity)object22) || easterBossEntity2.m_20145_() || !(easterBossEntity2.m_20238_(vec3) < 4096.0)) continue;
            arrayList.add(easterBossEntity2);
        }
        arrayList.sort(Comparator.comparingDouble(easterBossEntity -> easterBossEntity.m_20238_(vec3)));
        ArcaneRig.Effect effect = ArcaneAssets.get("qizai_prism_lock");
        object22 = ArcaneAssets.get("qizai_crimson_prayer");
        int n2 = 0;
        for (EasterBossEntity stringArray : arrayList) {
            double d2;
            double d3;
            if (n2++ >= 12) break;
            CompoundTag compoundTag = stringArray.getQizaiArsenalCue();
            ListTag listTag = compoundTag.m_128437_("salvos", 10);
            for (int compoundTag4 = 0; compoundTag4 < Math.min(4, listTag.size()) && n > 0; ++compoundTag4) {
                CompoundTag bl = listTag.m_128728_(compoundTag4);
                d3 = d - (double)bl.m_128454_("start");
                if (d3 < 0.0 || d3 >= 42.0) continue;
                ListTag d22 = bl.m_128437_("crystals", 10);
                for (int i = 0; i < Math.min(6, d22.size()) && n > 0; ++i) {
                    CompoundTag compoundTag2 = d22.m_128728_(i);
                    Vec3 vec32 = ArcaneArsenalVisuals.point(compoundTag2, "");
                    Vec3 vec33 = ArcaneArsenalVisuals.point(compoundTag2, "end");
                    double d4 = d3 - 24.0 - (double)(i * 2);
                    if (d4 >= 8.0 || vec32.m_82557_(vec33) > 1024.01 || stringArray.m_20238_(vec32) > 4096.0) continue;
                    float f = d4 < 0.0 ? 0.35f : (float)(1.0 - d4 / 8.0);
                    n -= ArcaneCompletedVisuals.show(effect, vec32, d4 < 0.0 ? "charge" : "release", d4 < 0.0 ? Math.min(1.4, d3 / 24.0 * 1.4) : d4 / 8.0 * 1.1, 0.25, false, n, renderLevelStageEvent, f);
                    n -= ArcaneCompletedVisuals.ray(effect, vec32, vec33, n, renderLevelStageEvent, f);
                }
            }
            if (!compoundTag.m_128425_("push", 10)) continue;
            CompoundTag string = compoundTag.m_128469_("push");
            boolean bl = string.m_128471_("released");
            d3 = d - (double)string.m_128454_("start");
            double d5 = d2 = bl ? 12.0 : 16.0;
            if (!(d3 >= 0.0) || !(d3 < d2)) continue;
            n -= ArcaneCompletedVisuals.show((ArcaneRig.Effect)object22, ArcaneArsenalVisuals.point(string, ""), bl ? "release" : "charge", d3 / d2 * (bl ? 1.1 : 1.4), 7.0 / ((ArcaneRig.Effect)object22).radius(), true, n, renderLevelStageEvent, 1.0f);
        }
        MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
        for (String string : new String[]{"qizai_prism_lock", "qizai_crimson_prayer"}) {
            bufferSource.m_109912_(RenderType.m_110470_((ResourceLocation)new ResourceLocation("ftboss", "qizai_arcane/" + string + ".png")));
        }
    }
}

