package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;

public final class ManorDomainRenderType extends RenderType {
    public static final RenderType SOLID = create(
        "ftboss_manor_domain_stone_v2",
        DefaultVertexFormat.POSITION_COLOR,
        Mode.QUADS,
        262144,
        false,
        true,
        CompositeState.builder()
            .setShaderState(POSITION_COLOR_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .setCullState(NO_CULL)
            .createCompositeState(false)
    );
    public static final RenderType GLOW = create(
        "ftboss_manor_domain_light_v2",
        DefaultVertexFormat.POSITION_COLOR,
        Mode.QUADS,
        262144,
        false,
        false,
        CompositeState.builder()
            .setShaderState(POSITION_COLOR_SHADER)
            .setTransparencyState(LIGHTNING_TRANSPARENCY)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .createCompositeState(false)
    );

    private ManorDomainRenderType() {
        super("ftboss_manor_domain_v2", DefaultVertexFormat.POSITION_COLOR, Mode.QUADS, 262144, false, false, () -> {
        }, () -> {
        });
    }
}
