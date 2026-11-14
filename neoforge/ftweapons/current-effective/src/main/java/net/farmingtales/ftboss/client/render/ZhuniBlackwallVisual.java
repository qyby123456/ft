package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.io.IOException;
import net.farmingtales.ftboss.network.ZhuniBlackwallNetwork;
import net.farmingtales.ftboss.util.BlackwallRenderRules;
import net.farmingtales.ftboss.util.ZhuniBlackwallRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

@EventBusSubscriber(
    modid = "ftboss",
    value = {Dist.CLIENT}
)
public final class ZhuniBlackwallVisual {
    private static final ResourceLocation CELLS = ResourceLocation.fromNamespaceAndPath("ftboss", "textures/gui/blackwall_cells.png");
    private static ShaderInstance shader;
    private static TextureTarget snapshot;
    private static TextureTarget processed;
    private static ClientLevel world;
    private static int remaining;
    private static int seed;
    private static int count;

    private ZhuniBlackwallVisual() {
    }

    public static void accept(ZhuniBlackwallNetwork.Breach var0) {
        Minecraft var1 = Minecraft.getInstance();
        if (var1.level != null && var1.player != null && var1.player.isAlive() && var1.level.dimension().location().equals(var0.dimension())) {
            world = var1.level;
            remaining = 100;
            seed = var0.seed() & 65535;
            count = var0.count();
        }
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
    public static void logout(LoggingOut var0) {
        release();
    }

    @SubscribeEvent
    public static void tick(Post var0) {
        if (true) {
            Minecraft var1 = Minecraft.getInstance();
            if (var1.level == null) {
                release();
            } else if (remaining > 0) {
                if (var1.level != world || var1.player == null || !var1.player.isAlive()) {
                    release();
                } else if (!var1.isPaused() && --remaining <= 0) {
                    clear();
                }
            }
        }
    }

    private static void render(GuiGraphics var0, float var1, int var2, int var3) {
        Minecraft var4 = Minecraft.getInstance();
        if (remaining > 0 && var4.level == world && var4.player != null && var4.player.isAlive()) {
            float var5 = (float)(100 - remaining) + (var4.isPaused() ? 0.0F : var1);
            float var6 = ZhuniBlackwallRules.strength(var5);
            if (!(var6 <= 0.0F) && var2 > 0 && var3 > 0) {
                var0.flush();
                if (shader != null) {
                    distort(var4, var0, var2, var3, var5, var6);
                } else {
                    var0.fill(0, 0, var2, 5, -1600843741);
                    var0.fill(0, var3 - 5, var2, var3, -1600843741);
                }

                int var7 = Math.min(255, Math.round(255.0F * Math.min(1.0F, var6 * 1.8F)));
                var0.drawString(var4.font, "BLACKWALL // BREACH", 12, var3 / 3, var7 << 24 | 16728920);
                var0.drawString(var4.font, "0" + count + " SYSTEMS LOCKED  /  " + (remaining + 19) / 20 + "s", 12, var3 / 3 + 13, var7 << 24 | 12546682);
                var0.flush();
            }
        }
    }

    private static void targets(int var0, int var1) {
        if (snapshot == null || snapshot.width != var0 || snapshot.height != var1) {
            if (snapshot != null) {
                snapshot.destroyBuffers();
            }

            if (processed != null) {
                processed.destroyBuffers();
            }

            snapshot = new TextureTarget(var0, var1, false, Minecraft.ON_OSX);
            processed = new TextureTarget(var0, var1, false, Minecraft.ON_OSX);
            snapshot.setFilterMode(9729);
            processed.setFilterMode(9729);
        }
    }

    private static void quad(GuiGraphics var0, int var1, int var2) {
        BufferBuilder var3 = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f var4 = var0.pose().last().pose();
        var3.addVertex(var4, 0.0F, (float)var2, 0.0F).setUv(0.0F, 0.0F);
        var3.addVertex(var4, (float)var1, (float)var2, 0.0F).setUv(1.0F, 0.0F);
        var3.addVertex(var4, (float)var1, 0.0F, 0.0F).setUv(1.0F, 1.0F);
        var3.addVertex(var4, 0.0F, 0.0F, 0.0F).setUv(0.0F, 1.0F);
        BufferUploader.drawWithShader(var3.buildOrThrow());
    }

    private static void distort(Minecraft var0, GuiGraphics var1, int var2, int var3, float var4, float var5) {
        RenderTarget var6 = var0.getMainRenderTarget();
        if (var6.width > 0 && var6.height > 0) {
            targets(BlackwallRenderRules.width(var6.width, var6.height), BlackwallRenderRules.height(var6.width, var6.height));
            boolean var7 = GL11.glIsEnabled(2929);
            boolean var8 = GL11.glIsEnabled(3042);
            boolean var9 = GL11.glIsEnabled(2884);
            boolean var10 = GL11.glGetBoolean(2930);

            try {
                GL30.glBindFramebuffer(36008, var6.frameBufferId);
                GL30.glBindFramebuffer(36009, snapshot.frameBufferId);
                GL30.glBlitFramebuffer(0, 0, var6.width, var6.height, 0, 0, snapshot.width, snapshot.height, 16384, 9729);
                processed.bindWrite(true);
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.disableBlend();
                RenderSystem.disableCull();
                RenderSystem.setShader(() -> shader);
                RenderSystem.setShaderTexture(0, snapshot.getColorTextureId());
                RenderSystem.setShaderTexture(1, CELLS);
                shader.getUniform("Time").set(var4 / 20.0F);
                shader.getUniform("Strength").set(var5);
                shader.getUniform("Seed").set((float)seed);
                shader.getUniform("ScreenSize").set((float)processed.width, (float)processed.height);
                quad(var1, var2, var3);
                var6.bindWrite(true);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, processed.getColorTextureId());
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                quad(var1, var2, var3);
            } finally {
                var6.bindWrite(true);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.depthMask(var10);
                if (var7) {
                    RenderSystem.enableDepthTest();
                } else {
                    RenderSystem.disableDepthTest();
                }

                if (var8) {
                    RenderSystem.enableBlend();
                } else {
                    RenderSystem.disableBlend();
                }

                if (var9) {
                    RenderSystem.enableCull();
                } else {
                    RenderSystem.disableCull();
                }
            }
        }
    }

    @EventBusSubscriber(
        modid = "ftboss",
        value = {Dist.CLIENT},
        bus = Bus.MOD
    )
    public static final class Shaders {
        private Shaders() {
        }

        @SubscribeEvent
        public static void registerZhuniBlackwallShader(RegisterShadersEvent var0) throws IOException {
            var0.registerShader(
                new ShaderInstance(var0.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("ftboss", "zhuni_blackwall"), DefaultVertexFormat.POSITION_TEX),
                var0x -> ZhuniBlackwallVisual.shader = var0x
            );
        }

        @SubscribeEvent
        public static void registerZhuniBlackwallOverlay(RegisterGuiLayersEvent var0) {
            var0.registerBelow(
                VanillaGuiLayers.CAMERA_OVERLAYS, ResourceLocation.fromNamespaceAndPath("ftboss", "zhuni_blackwall"), (graphics, delta) -> ZhuniBlackwallVisual.render(graphics, delta.getGameTimeDeltaPartialTick(false), graphics.guiWidth(), graphics.guiHeight())
            );
        }
    }
}
