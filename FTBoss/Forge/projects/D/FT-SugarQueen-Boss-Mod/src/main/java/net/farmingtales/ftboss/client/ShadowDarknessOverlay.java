package net.farmingtales.ftboss.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.util.ShadowDarknessRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ShadowDarknessOverlay {
    private static final ResourceLocation FRAME = new ResourceLocation(FTBossMod.MOD_ID, "textures/gui/overlays/zheergen_dark_frame.png");
    private static final ResourceLocation SIGILS = new ResourceLocation(FTBossMod.MOD_ID, "textures/gui/overlays/zheergen_dark_sigils.png");
    private static ClientLevel world;
    private static LocalPlayer viewer;
    private static float previous;
    private static float amount;
    private static int clock;

    private ShadowDarknessOverlay() {}

    public static boolean isVisible() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == world && minecraft.player != null && minecraft.player == viewer
                && minecraft.player.isAlive() && !minecraft.player.isSpectator() && (amount > 0 || previous > 0);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (world != minecraft.level || viewer != minecraft.player
                || minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.isSpectator()) {
            world = minecraft.level;
            viewer = minecraft.player;
            previous = amount = 0;
            clock = 0;
        }
        if (minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.isSpectator() || minecraft.isPaused()) return;
        previous = amount;
        amount = ShadowDarknessRules.step(amount, ShadowDarknessRules.active(
                minecraft.player.hasEffect(FTEffects.SHADOW_BLIND.get()), minecraft.player.hasEffect(FTEffects.SHADOW_EROSION.get())));
        clock = (clock + 1) % ShadowDarknessRules.ANIMATION_PERIOD_TICKS;
    }

    private static void render(GuiGraphics graphics, float partialTick, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isVisible() || minecraft.getCameraEntity() != minecraft.player || width <= 0 || height <= 0) return;
        float strength = ShadowDarknessRules.interpolate(previous, amount, partialTick);
        if (strength <= 0) return;
        double time = clock + partialTick;
        graphics.flush();
        if (net.farmingtales.ftboss.client.render.ShadowDarknessVisual.render(graphics, width, height, strength, (float) time / 20f)) return;
        try {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            graphics.fill(0, 0, width, height, ShadowDarknessRules.tint(strength));
            graphics.flush();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            draw(graphics, FRAME, width, height, strength * ShadowDarknessRules.frameAlpha(time));
            draw(graphics, SIGILS, width, height, strength * ShadowDarknessRules.sigilAlpha(time));
        } finally {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }

    private static void draw(GuiGraphics graphics, ResourceLocation texture, int width, int height, float alpha) {
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        graphics.blit(texture, 0, 0, width, height, 0F, 0F,
                ShadowDarknessRules.TEXTURE_WIDTH, ShadowDarknessRules.TEXTURE_HEIGHT,
                ShadowDarknessRules.TEXTURE_WIDTH, ShadowDarknessRules.TEXTURE_HEIGHT);
    }

    @Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void registerShadowDarknessOverlay(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.VIGNETTE.id(), "zheergen_darkness",
                    (gui, graphics, partialTick, width, height) -> render(graphics, partialTick, width, height));
        }
    }
}
