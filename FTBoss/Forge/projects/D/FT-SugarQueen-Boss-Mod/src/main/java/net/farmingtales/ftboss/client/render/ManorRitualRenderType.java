package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public final class ManorRitualRenderType extends RenderType {
    public static final RenderType TYPE = create("ftboss_manor_ritual", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 262144, false, false, CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER).setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL).createCompositeState(false));

    private ManorRitualRenderType() {
        super("ftboss_manor_ritual", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                262144, false, false, () -> {}, () -> {});
    }
}
