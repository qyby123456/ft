package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.resources.ResourceLocation;

public final class GongdiSpellGlowType extends RenderType {
    private GongdiSpellGlowType() {
        super("ftboss_gongdi_glow", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 16384, false, false, () -> {
        }, () -> {
        });
    }

    public static RenderType layer(ResourceLocation var0) {
        return create(
            "ftboss_gongdi_glow",
            DefaultVertexFormat.NEW_ENTITY,
            Mode.QUADS,
            16384,
            false,
            false,
            CompositeState.builder()
                .setShaderState(RENDERTYPE_EYES_SHADER)
                .setTextureState(new TextureStateShard(var0, false, false))
                .setTransparencyState(ADDITIVE_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setDepthTestState(NO_DEPTH_TEST)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false)
        );
    }
}
