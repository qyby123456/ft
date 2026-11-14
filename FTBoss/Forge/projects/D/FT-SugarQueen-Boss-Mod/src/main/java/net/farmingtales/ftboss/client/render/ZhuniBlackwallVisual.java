package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import net.farmingtales.ftboss.network.ZhuniBlackwallNetwork.Breach;
import net.farmingtales.ftboss.util.BlackwallRenderRules;
import net.farmingtales.ftboss.util.ZhuniBlackwallRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

@Mod.EventBusSubscriber(modid = "ftboss", value = Dist.CLIENT)
public final class ZhuniBlackwallVisual {
    private static final ResourceLocation CELLS = new ResourceLocation("ftboss", "textures/gui/blackwall_cells.png");
    private static ShaderInstance shader;
    private static TextureTarget snapshot;
    private static TextureTarget processed;
    private static ClientLevel world;
    private static int remaining;
    private static int seed;
    private static int count;
    private ZhuniBlackwallVisual() {}

    public static void accept(Breach packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || !minecraft.player.isAlive()
                || !minecraft.level.dimension().location().equals(packet.dimension())) return;
        world = minecraft.level;
        remaining = ZhuniBlackwallRules.DURATION_TICKS;
        seed = packet.seed() & 65535;
        count = packet.count();
    }

    private static void clear() {
        remaining = 0;
        world = null;
    }

    private static void release() {
        clear();
        if (processed != null) {
            processed.destroyBuffers();
            processed = null;
        }
        if (snapshot != null) {
            snapshot.destroyBuffers();
            snapshot = null;
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { release(); }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) { release(); return; }
        if (remaining <= 0) return;
        if (minecraft.level != world || minecraft.player == null || !minecraft.player.isAlive()) release();
        else if (!minecraft.isPaused() && --remaining <= 0) clear();
    }

    private static void render(GuiGraphics graphics, float partialTick, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        if (remaining <= 0 || minecraft.level != world || minecraft.player == null || !minecraft.player.isAlive()) return;
        float age = ZhuniBlackwallRules.DURATION_TICKS - remaining + (minecraft.isPaused() ? 0 : partialTick);
        float strength = ZhuniBlackwallRules.strength(age);
        if (strength <= 0 || width <= 0 || height <= 0) return;
        graphics.flush();
        if (shader != null) distort(minecraft, graphics, width, height, age, strength);
        else {
            graphics.fill(0, 0, width, 5, 0xA0951023);
            graphics.fill(0, height - 5, width, height, 0xA0951023);
        }
        int alpha = Math.min(255, Math.round(255 * Math.min(1, strength * 1.8f)));
        graphics.drawString(minecraft.font, "BLACKWALL // BREACH", 12, height / 3, alpha << 24 | 0xFF4358);
        graphics.drawString(minecraft.font, "0" + count + " SYSTEMS LOCKED  /  " + ((remaining + 19) / 20) + "s", 12, height / 3 + 13,
                alpha << 24 | 0xBF727A);
        graphics.flush();
    }

    private static void targets(int width, int height) {
        if (snapshot != null && snapshot.width == width && snapshot.height == height) return;
        if (snapshot != null) snapshot.destroyBuffers();
        if (processed != null) processed.destroyBuffers();
        snapshot = new TextureTarget(width, height, false, Minecraft.ON_OSX);
        processed = new TextureTarget(width, height, false, Minecraft.ON_OSX);
        snapshot.setFilterMode(GL11.GL_LINEAR);
        processed.setFilterMode(GL11.GL_LINEAR);
    }

    private static void quad(GuiGraphics graphics, int width, int height) {
        BufferBuilder buffer = com.mojang.blaze3d.vertex.Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        var pose = graphics.pose().last().pose();
        buffer.vertex(pose, 0, height, 0).uv(0, 0).endVertex();
        buffer.vertex(pose, width, height, 0).uv(1, 0).endVertex();
        buffer.vertex(pose, width, 0, 0).uv(1, 1).endVertex();
        buffer.vertex(pose, 0, 0, 0).uv(0, 1).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }

    private static void distort(Minecraft minecraft, GuiGraphics graphics, int width, int height, float age, float strength) {
        var main = minecraft.getMainRenderTarget();
        if (main.width <= 0 || main.height <= 0) return;
        targets(BlackwallRenderRules.width(main.width, main.height), BlackwallRenderRules.height(main.width, main.height));
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        try {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, snapshot.frameBufferId);
            GL30.glBlitFramebuffer(0, 0, main.width, main.height, 0, 0, snapshot.width, snapshot.height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_LINEAR);
            processed.bindWrite(true);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableBlend();
            RenderSystem.disableCull();
            RenderSystem.setShader(() -> shader);
            RenderSystem.setShaderTexture(0, snapshot.getColorTextureId());
            RenderSystem.setShaderTexture(1, CELLS);
            shader.getUniform("Time").set(age / 20f);
            shader.getUniform("Strength").set(strength);
            shader.getUniform("Seed").set((float) seed);
            shader.getUniform("ScreenSize").set((float) processed.width, (float) processed.height);
            quad(graphics, width, height);
            main.bindWrite(true);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, processed.getColorTextureId());
            RenderSystem.setShaderColor(1, 1, 1, 1);
            quad(graphics, width, height);
        } finally {
            main.bindWrite(true);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.depthMask(depthMask);
            if (depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
            if (blend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
            if (cull) RenderSystem.enableCull(); else RenderSystem.disableCull();
        }
    }

    @Mod.EventBusSubscriber(modid = "ftboss", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Shaders {
        private Shaders() {}
        @SubscribeEvent
        public static void registerZhuniBlackwallShader(RegisterShadersEvent event) throws IOException {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("ftboss", "zhuni_blackwall"),
                    DefaultVertexFormat.POSITION_TEX), loaded -> shader = loaded);
        }

        @SubscribeEvent
        public static void registerZhuniBlackwallOverlay(RegisterGuiOverlaysEvent event) {
            event.registerBelow(VanillaGuiOverlay.VIGNETTE.id(), "zhuni_blackwall",
                    (gui, graphics, partialTick, width, height) -> render(graphics, partialTick, width, height));
        }
    }
}
