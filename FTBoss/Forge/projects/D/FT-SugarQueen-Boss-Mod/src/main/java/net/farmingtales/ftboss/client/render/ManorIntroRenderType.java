package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class ManorIntroRenderType extends RenderType {
    public static final RenderType TYPE = create("ftboss_manor_intro", DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 262144, false, false, CompositeState.builder()
                    .setShaderState(RENDERTYPE_EYES_SHADER)
                    .setTextureState(new TextureStateShard(
                            new ResourceLocation(FTBossMod.MOD_ID, "textures/fx/manor_intro_white.png"), false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    private ManorIntroRenderType() {
        super("ftboss_manor_intro", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                262144, false, false, () -> {}, () -> {});
    }
}
