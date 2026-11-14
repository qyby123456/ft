/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RegisterGuiOverlaysEvent
 *  net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.farmingtales.ftboss.client.render.ShadowDarknessVisual;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.util.ShadowDarknessRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class ShadowDarknessOverlay {
    private static final ResourceLocation FRAME = new ResourceLocation("ftboss", "textures/gui/overlays/zheergen_dark_frame.png");
    private static final ResourceLocation SIGILS = new ResourceLocation("ftboss", "textures/gui/overlays/zheergen_dark_sigils.png");
    private static ClientLevel world;
    private static LocalPlayer viewer;
    private static float previous;
    private static float amount;
    private static int clock;

    private ShadowDarknessOverlay() {
    }

    public static boolean isVisible() {
        Minecraft minecraft = Minecraft.m_91087_();
        return minecraft.f_91073_ == world && minecraft.f_91074_ != null && minecraft.f_91074_ == viewer && minecraft.f_91074_.m_6084_() && !minecraft.f_91074_.m_5833_() && (amount > 0.0f || previous > 0.0f);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (world != minecraft.f_91073_ || viewer != minecraft.f_91074_ || minecraft.f_91074_ == null || !minecraft.f_91074_.m_6084_() || minecraft.f_91074_.m_5833_()) {
            world = minecraft.f_91073_;
            viewer = minecraft.f_91074_;
            amount = 0.0f;
            previous = 0.0f;
            clock = 0;
        }
        if (minecraft.f_91074_ == null || !minecraft.f_91074_.m_6084_() || minecraft.f_91074_.m_5833_() || minecraft.m_91104_()) {
            return;
        }
        previous = amount;
        amount = ShadowDarknessRules.step(amount, ShadowDarknessRules.active(minecraft.f_91074_.m_21023_((MobEffect)FTEffects.SHADOW_BLIND.get()), minecraft.f_91074_.m_21023_((MobEffect)FTEffects.SHADOW_EROSION.get())));
        clock = (clock + 1) % 400;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void render(GuiGraphics guiGraphics, float f, int n, int n2) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (!ShadowDarknessOverlay.isVisible() || minecraft.m_91288_() != minecraft.f_91074_ || n <= 0 || n2 <= 0) {
            return;
        }
        float f2 = ShadowDarknessRules.interpolate(previous, amount, f);
        if (f2 <= 0.0f) {
            return;
        }
        double d = (float)clock + f;
        guiGraphics.m_280262_();
        if (ShadowDarknessVisual.render(guiGraphics, n, n2, f2, (float)d / 20.0f)) {
            return;
        }
        try {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            guiGraphics.m_280509_(0, 0, n, n2, ShadowDarknessRules.tint(f2));
            guiGraphics.m_280262_();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask((boolean)false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            ShadowDarknessOverlay.draw(guiGraphics, FRAME, n, n2, f2 * ShadowDarknessRules.frameAlpha(d));
            ShadowDarknessOverlay.draw(guiGraphics, SIGILS, n, n2, f2 * ShadowDarknessRules.sigilAlpha(d));
        }
        finally {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            RenderSystem.depthMask((boolean)true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }

    private static void draw(GuiGraphics guiGraphics, ResourceLocation resourceLocation, int n, int n2, float f) {
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f);
        guiGraphics.m_280411_(resourceLocation, 0, 0, n, n2, 0.0f, 0.0f, 1024, 576, 1024, 576);
    }

    @Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        public static void registerShadowDarknessOverlay(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.VIGNETTE.id(), "zheergen_darkness", (gui, graphics, partialTick, width, height) -> ShadowDarknessOverlay.render(graphics, partialTick, width, height));
        }
    }
}

