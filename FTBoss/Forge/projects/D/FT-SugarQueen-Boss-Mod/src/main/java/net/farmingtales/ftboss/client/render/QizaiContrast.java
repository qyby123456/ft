package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public final class QizaiContrast extends RenderType {
    public static final RenderType MODEL = create("ftboss_qizai_crystal_model", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 8192, false, false, CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER).setTransparencyState(NO_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_DEPTH_WRITE).setCullState(NO_CULL)
                    .createCompositeState(false));
    public static final RenderType TYPE = create("ftboss_qizai_contrast", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 8192, false, true, CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL)
                    .createCompositeState(false));

    private QizaiContrast() {
        super("ftboss_qizai_contrast", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                8192, false, true, () -> {}, () -> {});
    }
}
