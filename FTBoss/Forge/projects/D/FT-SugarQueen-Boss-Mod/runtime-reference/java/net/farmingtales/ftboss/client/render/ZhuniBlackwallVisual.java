/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.pipeline.TextureTarget
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.BufferBuilder$RenderedBuffer
 *  com.mojang.blaze3d.vertex.BufferUploader
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.ShaderInstance
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.RegisterGuiOverlaysEvent
 *  net.minecraftforge.client.event.RegisterShadersEvent
 *  net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Matrix4f
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL30
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class ZhuniBlackwallVisual {
    private static final ResourceLocation CELLS = new ResourceLocation("ftboss", "textures/gui/blackwall_cells.png");
    private static ShaderInstance shader;
    private static TextureTarget snapshot;
    private static TextureTarget processed;
    private static ClientLevel world;
    private static int remaining;
    private static int seed;
    private static int count;

    private ZhuniBlackwallVisual() {
    }

    public static void accept(ZhuniBlackwallNetwork.Breach breach) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null || minecraft.f_91074_ == null || !minecraft.f_91074_.m_6084_() || !minecraft.f_91073_.m_46472_().m_135782_().equals((Object)breach.dimension())) {
            return;
        }
        world = minecraft.f_91073_;
        remaining = 100;
        seed = breach.seed() & 0xFFFF;
        count = breach.count();
    }

    private static void clear() {
        remaining = 0;
        world = null;
    }

    private static void release() {
        ZhuniBlackwallVisual.clear();
        if (processed != null) {
            processed.m_83930_();
            processed = null;
        }
        if (snapshot != null) {
            snapshot.m_83930_();
            snapshot = null;
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        ZhuniBlackwallVisual.release();
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null) {
            ZhuniBlackwallVisual.release();
            return;
        }
        if (remaining <= 0) {
            return;
        }
        if (minecraft.f_91073_ != world || minecraft.f_91074_ == null || !minecraft.f_91074_.m_6084_()) {
            ZhuniBlackwallVisual.release();
        } else if (!minecraft.m_91104_() && --remaining <= 0) {
            ZhuniBlackwallVisual.clear();
        }
    }

    private static void render(GuiGraphics guiGraphics, float f, int n, int n2) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (remaining <= 0 || minecraft.f_91073_ != world || minecraft.f_91074_ == null || !minecraft.f_91074_.m_6084_()) {
            return;
        }
        float f2 = (float)(100 - remaining) + (minecraft.m_91104_() ? 0.0f : f);
        float f3 = ZhuniBlackwallRules.strength(f2);
        if (f3 <= 0.0f || n <= 0 || n2 <= 0) {
            return;
        }
        guiGraphics.m_280262_();
        if (shader != null) {
            ZhuniBlackwallVisual.distort(minecraft, guiGraphics, n, n2, f2, f3);
        } else {
            guiGraphics.m_280509_(0, 0, n, 5, -1600843741);
            guiGraphics.m_280509_(0, n2 - 5, n, n2, -1600843741);
        }
        int n3 = Math.min(255, Math.round(255.0f * Math.min(1.0f, f3 * 1.8f)));
        guiGraphics.m_280488_(minecraft.f_91062_, "BLACKWALL // BREACH", 12, n2 / 3, n3 << 24 | 0xFF4358);
        guiGraphics.m_280488_(minecraft.f_91062_, "0" + count + " SYSTEMS LOCKED  /  " + (remaining + 19) / 20 + "s", 12, n2 / 3 + 13, n3 << 24 | 0xBF727A);
        guiGraphics.m_280262_();
    }

    private static void targets(int n, int n2) {
        if (snapshot != null && ZhuniBlackwallVisual.snapshot.f_83915_ == n && ZhuniBlackwallVisual.snapshot.f_83916_ == n2) {
            return;
        }
        if (snapshot != null) {
            snapshot.m_83930_();
        }
        if (processed != null) {
            processed.m_83930_();
        }
        snapshot = new TextureTarget(n, n2, false, Minecraft.f_91002_);
        processed = new TextureTarget(n, n2, false, Minecraft.f_91002_);
        snapshot.m_83936_(9729);
        processed.m_83936_(9729);
    }

    private static void quad(GuiGraphics guiGraphics, int n, int n2) {
        BufferBuilder bufferBuilder = Tesselator.m_85913_().m_85915_();
        bufferBuilder.m_166779_(VertexFormat.Mode.QUADS, DefaultVertexFormat.f_85817_);
        Matrix4f matrix4f = guiGraphics.m_280168_().m_85850_().m_252922_();
        bufferBuilder.m_252986_(matrix4f, 0.0f, (float)n2, 0.0f).m_7421_(0.0f, 0.0f).m_5752_();
        bufferBuilder.m_252986_(matrix4f, (float)n, (float)n2, 0.0f).m_7421_(1.0f, 0.0f).m_5752_();
        bufferBuilder.m_252986_(matrix4f, (float)n, 0.0f, 0.0f).m_7421_(1.0f, 1.0f).m_5752_();
        bufferBuilder.m_252986_(matrix4f, 0.0f, 0.0f, 0.0f).m_7421_(0.0f, 1.0f).m_5752_();
        BufferUploader.m_231202_((BufferBuilder.RenderedBuffer)bufferBuilder.m_231175_());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void distort(Minecraft minecraft, GuiGraphics guiGraphics, int n, int n2, float f, float f2) {
        RenderTarget renderTarget = minecraft.m_91385_();
        if (renderTarget.f_83915_ <= 0 || renderTarget.f_83916_ <= 0) {
            return;
        }
        ZhuniBlackwallVisual.targets(BlackwallRenderRules.width(renderTarget.f_83915_, renderTarget.f_83916_), BlackwallRenderRules.height(renderTarget.f_83915_, renderTarget.f_83916_));
        boolean bl = GL11.glIsEnabled((int)2929);
        boolean bl2 = GL11.glIsEnabled((int)3042);
        boolean bl3 = GL11.glIsEnabled((int)2884);
        boolean bl4 = GL11.glGetBoolean((int)2930);
        try {
            GL30.glBindFramebuffer((int)36008, (int)renderTarget.f_83920_);
            GL30.glBindFramebuffer((int)36009, (int)ZhuniBlackwallVisual.snapshot.f_83920_);
            GL30.glBlitFramebuffer((int)0, (int)0, (int)renderTarget.f_83915_, (int)renderTarget.f_83916_, (int)0, (int)0, (int)ZhuniBlackwallVisual.snapshot.f_83915_, (int)ZhuniBlackwallVisual.snapshot.f_83916_, (int)16384, (int)9729);
            processed.m_83947_(true);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask((boolean)false);
            RenderSystem.disableBlend();
            RenderSystem.disableCull();
            RenderSystem.setShader(() -> shader);
            RenderSystem.setShaderTexture((int)0, (int)snapshot.m_83975_());
            RenderSystem.setShaderTexture((int)1, (ResourceLocation)CELLS);
            shader.m_173348_("Time").m_5985_(f / 20.0f);
            shader.m_173348_("Strength").m_5985_(f2);
            shader.m_173348_("Seed").m_5985_((float)seed);
            shader.m_173348_("ScreenSize").m_7971_((float)ZhuniBlackwallVisual.processed.f_83915_, (float)ZhuniBlackwallVisual.processed.f_83916_);
            ZhuniBlackwallVisual.quad(guiGraphics, n, n2);
            renderTarget.m_83947_(true);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::m_172817_);
            RenderSystem.setShaderTexture((int)0, (int)processed.m_83975_());
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            ZhuniBlackwallVisual.quad(guiGraphics, n, n2);
        }
        finally {
            renderTarget.m_83947_(true);
            RenderSystem.setShader(GameRenderer::m_172817_);
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            RenderSystem.depthMask((boolean)bl4);
            if (bl) {
                RenderSystem.enableDepthTest();
            } else {
                RenderSystem.disableDepthTest();
            }
            if (bl2) {
                RenderSystem.enableBlend();
            } else {
                RenderSystem.disableBlend();
            }
            if (bl3) {
                RenderSystem.enableCull();
            } else {
                RenderSystem.disableCull();
            }
        }
    }

    @Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Shaders {
        private Shaders() {
        }

        @SubscribeEvent
        public static void registerZhuniBlackwallShader(RegisterShadersEvent registerShadersEvent) throws IOException {
            registerShadersEvent.registerShader(new ShaderInstance(registerShadersEvent.getResourceProvider(), new ResourceLocation("ftboss", "zhuni_blackwall"), DefaultVertexFormat.f_85817_), shaderInstance -> {
                shader = shaderInstance;
            });
        }

        @SubscribeEvent
        public static void registerZhuniBlackwallOverlay(RegisterGuiOverlaysEvent registerGuiOverlaysEvent) {
            registerGuiOverlaysEvent.registerBelow(VanillaGuiOverlay.VIGNETTE.id(), "zhuni_blackwall", (forgeGui, guiGraphics, f, n, n2) -> ZhuniBlackwallVisual.render(guiGraphics, f, n, n2));
        }
    }
}

