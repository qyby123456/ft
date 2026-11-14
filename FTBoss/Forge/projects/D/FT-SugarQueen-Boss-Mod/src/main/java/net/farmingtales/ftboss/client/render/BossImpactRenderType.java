package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class BossImpactRenderType extends RenderType {
    public static final RenderType TYPE = create("ftboss_boss_impact", DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 32768, false, false, CompositeState.builder()
                    .setShaderState(RENDERTYPE_EYES_SHADER)
                    .setTextureState(new TextureStateShard(new ResourceLocation("ftboss", "textures/fx/manor_intro_white.png"), false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY).setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE).createCompositeState(false));

    private BossImpactRenderType() {
        super("ftboss_boss_impact", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 32768,
                false, false, () -> {}, () -> {});
    }
}
