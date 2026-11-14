package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** 万味胚刃流光材质；双面加色混合，只写颜色，保留场景深度遮挡。 */
public final class GourmetBladeStreamRenderType extends RenderType {
    public static final RenderType TYPE = create("ftboss_gourmet_blade_stream", DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 16384, false, true, CompositeState.builder()
                    .setShaderState(RENDERTYPE_EYES_SHADER)
                    .setTextureState(new TextureStateShard(new ResourceLocation("ftboss", "textures/fx/gourmet_blade_stream.png"), false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY).setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).createCompositeState(false));

    private GourmetBladeStreamRenderType() {
        super("ftboss_gourmet_blade_stream", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                16384, false, true, () -> {}, () -> {});
    }
}
