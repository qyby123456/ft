package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public final class XuanlanGlow extends RenderType {
    public static final RenderType TYPE = create("ftboss_xuanlan_glow", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 8192, false, true, CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL)
                    .createCompositeState(false));

    private XuanlanGlow() {
        super("ftboss_xuanlan_glow", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                8192, false, true, () -> {}, () -> {});
    }
}
