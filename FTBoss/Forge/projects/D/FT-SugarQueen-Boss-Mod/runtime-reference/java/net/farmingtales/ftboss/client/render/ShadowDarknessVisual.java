/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.BufferBuilder$RenderedBuffer
 *  com.mojang.blaze3d.vertex.BufferUploader
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.ShaderInstance
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RegisterShadersEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Matrix4f
 *  org.lwjgl.opengl.GL11
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public final class ShadowDarknessVisual {
    private static final ResourceLocation FRAME = new ResourceLocation("ftboss", "textures/gui/overlays/zheergen_dark_frame.png");
    private static final ResourceLocation SIGILS = new ResourceLocation("ftboss", "textures/gui/overlays/zheergen_dark_sigils.png");
    private static ShaderInstance shader;

    private ShadowDarknessVisual() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean render(GuiGraphics guiGraphics, int n, int n2, float f, float f2) {
        if (shader == null) {
            return false;
        }
        boolean bl = GL11.glIsEnabled((int)2929);
        boolean bl2 = GL11.glIsEnabled((int)3042);
        boolean bl3 = GL11.glIsEnabled((int)2884);
        boolean bl4 = GL11.glGetBoolean((int)2930);
        try {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask((boolean)false);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(() -> shader);
            RenderSystem.setShaderTexture((int)0, (ResourceLocation)FRAME);
            RenderSystem.setShaderTexture((int)1, (ResourceLocation)SIGILS);
            shader.m_173348_("Time").m_5985_(f2);
            shader.m_173348_("Strength").m_5985_(f);
            shader.m_173348_("Aspect").m_5985_((float)n / (float)n2);
            BufferBuilder bufferBuilder = Tesselator.m_85913_().m_85915_();
            bufferBuilder.m_166779_(VertexFormat.Mode.QUADS, DefaultVertexFormat.f_85817_);
            Matrix4f matrix4f = guiGraphics.m_280168_().m_85850_().m_252922_();
            bufferBuilder.m_252986_(matrix4f, 0.0f, (float)n2, 0.0f).m_7421_(0.0f, 1.0f).m_5752_();
            bufferBuilder.m_252986_(matrix4f, (float)n, (float)n2, 0.0f).m_7421_(1.0f, 1.0f).m_5752_();
            bufferBuilder.m_252986_(matrix4f, (float)n, 0.0f, 0.0f).m_7421_(1.0f, 0.0f).m_5752_();
            bufferBuilder.m_252986_(matrix4f, 0.0f, 0.0f, 0.0f).m_7421_(0.0f, 0.0f).m_5752_();
            BufferUploader.m_231202_((BufferBuilder.RenderedBuffer)bufferBuilder.m_231175_());
            boolean bl5 = true;
            return bl5;
        }
        finally {
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
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        public static void registerAnimatedShadowShader(RegisterShadersEvent registerShadersEvent) throws IOException {
            registerShadersEvent.registerShader(new ShaderInstance(registerShadersEvent.getResourceProvider(), new ResourceLocation("ftboss", "shadow_darkness"), DefaultVertexFormat.f_85817_), shaderInstance -> {
                shader = shaderInstance;
            });
        }
    }
}

