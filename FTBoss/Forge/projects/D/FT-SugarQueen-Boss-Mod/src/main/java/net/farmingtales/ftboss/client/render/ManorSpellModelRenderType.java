package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/** Full-bright depth-writing model pass with fixed-size alpha disappearance. */
public final class ManorSpellModelRenderType extends RenderType {
    public static final RenderType TYPE = create("ftboss_manor_spell_models", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 4_194_304, false, true, CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_DEPTH_WRITE).setCullState(NO_CULL)
                    .createCompositeState(false));

    private ManorSpellModelRenderType() {
        super("ftboss_manor_spell_models", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                4_194_304, false, true, () -> {}, () -> {});
    }
}