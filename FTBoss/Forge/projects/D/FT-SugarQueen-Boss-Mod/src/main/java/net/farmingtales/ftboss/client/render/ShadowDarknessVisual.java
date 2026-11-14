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
import org.lwjgl.opengl.GL11;

public final class ShadowDarknessVisual {
    private static final ResourceLocation FRAME = new ResourceLocation("ftboss", "textures/gui/overlays/zheergen_dark_frame.png");
    private static final ResourceLocation SIGILS = new ResourceLocation("ftboss", "textures/gui/overlays/zheergen_dark_sigils.png");
    private static ShaderInstance shader;

    private ShadowDarknessVisual() {}

    public static boolean render(GuiGraphics graphics, int width, int height, float strength, float seconds) {
        if (shader == null) return false;
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        try {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(() -> shader);
            RenderSystem.setShaderTexture(0, FRAME);
            RenderSystem.setShaderTexture(1, SIGILS);
            shader.getUniform("Time").set(seconds);
            shader.getUniform("Strength").set(strength);
            shader.getUniform("Aspect").set((float) width / height);
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            var pose = graphics.pose().last().pose();
            buffer.vertex(pose, 0, height, 0).uv(0, 1).endVertex();
            buffer.vertex(pose, width, height, 0).uv(1, 1).endVertex();
            buffer.vertex(pose, width, 0, 0).uv(1, 0).endVertex();
            buffer.vertex(pose, 0, 0, 0).uv(0, 0).endVertex();
            BufferUploader.drawWithShader(buffer.end());
            return true;
        } finally {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.depthMask(depthMask);
            if (depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
            if (blend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
            if (cull) RenderSystem.enableCull(); else RenderSystem.disableCull();
        }
    }

    @Mod.EventBusSubscriber(modid = "ftboss", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void registerAnimatedShadowShader(RegisterShadersEvent event) throws IOException {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("ftboss", "shadow_darkness"),
                    DefaultVertexFormat.POSITION_TEX), loaded -> shader = loaded);
        }
    }
}
