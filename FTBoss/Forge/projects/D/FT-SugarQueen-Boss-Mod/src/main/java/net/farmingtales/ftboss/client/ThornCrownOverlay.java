package net.farmingtales.ftboss.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.SilverKeyItem;
import net.farmingtales.ftboss.item.ThornCrownItem;
import net.farmingtales.ftboss.util.SayaFleshVisionRules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ThornCrownOverlay {
    private static final ResourceLocation FLESH = new ResourceLocation(FTBossMod.MOD_ID, "textures/gui/overlays/saya_flesh_dynamic.png");
    private static final ResourceLocation VEINS = new ResourceLocation(FTBossMod.MOD_ID, "textures/gui/overlays/saya_veins_dynamic.png");

    private ThornCrownOverlay() {}

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.VIGNETTE.id(), "thorn_crown_vision", (gui, graphics, partialTick, width, height) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.isSpectator()) return;
            if (minecraft.getCameraEntity() != minecraft.player) return;
            int pieces = ThornCrownItem.equippedPieces(minecraft.player);
            if (pieces <= 0 || SilverKeyItem.isEquipped(minecraft.player)) return;
            double ticks = minecraft.player.tickCount % SayaFleshVisionRules.PERIOD_TICKS + partialTick;
            int frame = SayaFleshVisionRules.animationFrame(ticks);
            graphics.flush();
            try {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                graphics.fill(0, 0, width, height, SayaFleshVisionRules.bloodTint(pieces));
                graphics.flush();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                draw(graphics, FLESH, width, height, frame, SayaFleshVisionRules.fleshAlpha(ticks, pieces));
                draw(graphics, VEINS, width, height, frame, SayaFleshVisionRules.veinAlpha(ticks, pieces));
            } finally {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
                RenderSystem.disableBlend();
            }
        });
    }

    private static void draw(GuiGraphics graphics, ResourceLocation texture, int width, int height, int frame, float alpha) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        float sourceX = (frame % SayaFleshVisionRules.ATLAS_COLUMNS) * SayaFleshVisionRules.FRAME_WIDTH;
        float sourceY = (frame / SayaFleshVisionRules.ATLAS_COLUMNS) * SayaFleshVisionRules.FRAME_HEIGHT;
        graphics.blit(texture, 0, 0, width, height, sourceX, sourceY,
                SayaFleshVisionRules.FRAME_WIDTH, SayaFleshVisionRules.FRAME_HEIGHT,
                SayaFleshVisionRules.ATLAS_WIDTH, SayaFleshVisionRules.ATLAS_HEIGHT);
    }
}
