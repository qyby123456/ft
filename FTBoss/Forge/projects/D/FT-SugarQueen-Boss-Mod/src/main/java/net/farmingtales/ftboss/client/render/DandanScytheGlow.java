package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public final class DandanScytheGlow extends RenderType {
    public static final RenderType TYPE = create("ftboss_dandan_candle", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 8192, false, true, CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL)
                    .createCompositeState(false));

    private DandanScytheGlow() {
        super("ftboss_dandan_candle", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                8192, false, true, () -> {}, () -> {});
    }
}
