package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/** Depth-tested translucent warning fence for the Manor's moving recall threshold. */
public final class ManorRecallBoundaryRenderType extends RenderType {
    public static final RenderType TYPE = create("ftboss_manor_recall_boundary", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 32768, false, true, CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL)
                    .createCompositeState(false));

    private ManorRecallBoundaryRenderType() {
        super("ftboss_manor_recall_boundary", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                32768, false, true, () -> {}, () -> {});
    }
}
