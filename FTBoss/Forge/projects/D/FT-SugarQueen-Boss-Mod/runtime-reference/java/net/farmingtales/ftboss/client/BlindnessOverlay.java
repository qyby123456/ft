/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderGuiOverlayEvent$Post
 *  net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.ShadowDarknessOverlay;
import net.farmingtales.ftboss.effect.FTEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class BlindnessOverlay {
    private static final int BLIND_FULL = -1610612736;
    private static final int BLIND_EDGE = -939524096;
    private static final int DARK_FULL = 0x78000000;
    private static final int DARK_EDGE = -1744830464;
    private static long lastLog = 0L;

    private BlindnessOverlay() {
    }

    @SubscribeEvent
    public static void onOverlay(RenderGuiOverlayEvent.Post e) {
        if (e.getOverlay() == null || e.getOverlay().id() == null) {
            return;
        }
        if (!e.getOverlay().id().equals((Object)VanillaGuiOverlay.HOTBAR.id())) {
            return;
        }
        Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91074_ == null) {
            return;
        }
        boolean shroud = BlindnessOverlay.hasShroud((LivingEntity)mc.f_91074_);
        if (shroud || ShadowDarknessOverlay.isVisible()) {
            return;
        }
        boolean blind = mc.f_91074_.m_21023_(MobEffects.f_19610_);
        boolean dark = mc.f_91074_.m_21023_(MobEffects.f_216964_);
        if (!blind && !dark) {
            return;
        }
        GuiGraphics g = e.getGuiGraphics();
        int w = mc.m_91268_().m_85445_();
        int h = mc.m_91268_().m_85446_();
        int full = blind ? -1610612736 : 0x78000000;
        int edge = blind ? -939524096 : -1744830464;
        g.m_280509_(0, 0, w, h, full);
        int bandV = Math.max(1, h / 6);
        int bandH = Math.max(1, w / 8);
        g.m_280509_(0, 0, w, bandV, edge);
        g.m_280509_(0, h - bandV, w, h, edge);
        g.m_280509_(0, 0, bandH, h, edge);
        g.m_280509_(w - bandH, 0, w, h, edge);
    }

    private static boolean hasShroud(LivingEntity e) {
        MobEffect eff = (MobEffect)FTEffects.SHADOW_BLIND.get();
        return eff != null && e.m_21023_(eff);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91074_ == null || mc.f_91073_ == null) {
            return;
        }
        boolean blind = mc.f_91074_.m_21023_(MobEffects.f_19610_);
        boolean dark = mc.f_91074_.m_21023_(MobEffects.f_216964_);
        boolean nausea = mc.f_91074_.m_21023_(MobEffects.f_19604_);
        boolean shroud = BlindnessOverlay.hasShroud((LivingEntity)mc.f_91074_);
        if (!(blind || dark || nausea || shroud)) {
            return;
        }
        long now = mc.f_91073_.m_46467_();
        if (now - lastLog < 100L) {
            return;
        }
        lastLog = now;
        FTBossMod.LOGGER.info("[FTBoss][client] \u5ba2\u6237\u7aef\u73a9\u5bb6\u8eab\u4e0a: \u5931\u660e={} \u9ed1\u6697={} \u53cd\u80c3={} \u6697\u5f71\u7b3c\u7f69={}(>=1 \u5c31\u4f1a\u81ea\u5df1\u753b\u906e\u7f69)", new Object[]{blind, dark, nausea, shroud});
    }
}

