package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/** 切换专用光效材质：深度测试、只写颜色、加法混合不依赖透明排序；2026-10-25新增。 */
public final class ManorTransitionRenderType extends RenderType {
    public static final RenderType TYPE = create("ftboss_manor_transition_light", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 524288, false, false, CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER).setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL)
                    .createCompositeState(false));

    private ManorTransitionRenderType() {
        super("ftboss_manor_transition_light", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                524288, false, false, () -> {}, () -> {});
    }
}
