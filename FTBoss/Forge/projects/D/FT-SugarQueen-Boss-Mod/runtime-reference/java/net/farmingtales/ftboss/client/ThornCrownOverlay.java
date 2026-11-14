/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RegisterGuiOverlaysEvent
 *  net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.farmingtales.ftboss.item.SilverKeyItem;
import net.farmingtales.ftboss.item.ThornCrownItem;
import net.farmingtales.ftboss.util.SayaFleshVisionRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class ThornCrownOverlay {
    private static final ResourceLocation FLESH = new ResourceLocation("ftboss", "textures/gui/overlays/saya_flesh_frame.png");
    private static final ResourceLocation VEINS = new ResourceLocation("ftboss", "textures/gui/overlays/saya_flesh_veins.png");

    private ThornCrownOverlay() {
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.VIGNETTE.id(), "thorn_crown_vision", (gui, graphics, partialTick, width, height) -> {
            Minecraft minecraft = Minecraft.m_91087_();
            if (minecraft.f_91074_ == null || !minecraft.f_91074_.m_6084_() || minecraft.f_91074_.m_5833_()) {
                return;
            }
            if (minecraft.m_91288_() != minecraft.f_91074_) {
                return;
            }
            int pieces = ThornCrownItem.equippedPieces((LivingEntity)minecraft.f_91074_);
            if (pieces <= 0 || SilverKeyItem.isEquipped((LivingEntity)minecraft.f_91074_)) {
                return;
            }
            double ticks = (float)(minecraft.f_91074_.f_19797_ % 36) + partialTick;
            graphics.m_280262_();
            try {
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                graphics.m_280509_(0, 0, width, height, SayaFleshVisionRules.bloodTint(pieces));
                graphics.m_280262_();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask((boolean)false);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                ThornCrownOverlay.draw(graphics, FLESH, width, height, SayaFleshVisionRules.fleshAlpha(ticks, pieces));
                ThornCrownOverlay.draw(graphics, VEINS, width, height, SayaFleshVisionRules.veinAlpha(ticks, pieces));
            }
            finally {
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                RenderSystem.depthMask((boolean)true);
                RenderSystem.enableDepthTest();
                RenderSystem.disableBlend();
            }
        });
    }

    private static void draw(GuiGraphics graphics, ResourceLocation texture, int width, int height, float alpha) {
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
        graphics.m_280411_(texture, 0, 0, width, height, 0.0f, 0.0f, 1024, 576, 1024, 576);
    }
}

