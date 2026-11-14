/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.client.qizaiarcane;

import net.farmingtales.ftboss.client.qizaiarcane.ArcaneFrameLimit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class ArcaneEntityBudget {
    private static double clock = Double.NaN;
    private static final ArcaneFrameLimit limits = new ArcaneFrameLimit();

    private ArcaneEntityBudget() {
    }

    private static void reset(double d) {
        ParticleStatus particleStatus = (ParticleStatus)Minecraft.m_91087_().f_91066_.m_231929_().m_231551_();
        limits.reset(particleStatus == ParticleStatus.MINIMAL ? 0 : (particleStatus == ParticleStatus.DECREASED ? 1 : 2));
        clock = d;
    }

    @SubscribeEvent
    public static void frame(RenderLevelStageEvent renderLevelStageEvent) {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel != null && renderLevelStageEvent.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            ArcaneEntityBudget.reset((float)clientLevel.m_46467_() + renderLevelStageEvent.getPartialTick());
        }
    }

    public static int available(boolean bl) {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null) {
            return 0;
        }
        long l = clientLevel.m_46467_();
        if (!Double.isFinite(clock) || (double)l < Math.floor(clock) || (double)l > Math.floor(clock)) {
            ArcaneEntityBudget.reset(l);
        }
        return limits.available(bl);
    }

    public static void consume(int n, boolean bl) {
        limits.consume(n, bl);
    }

    public static int take(int n, float f) {
        ArcaneEntityBudget.available(false);
        return limits.consume(n, false);
    }
}

