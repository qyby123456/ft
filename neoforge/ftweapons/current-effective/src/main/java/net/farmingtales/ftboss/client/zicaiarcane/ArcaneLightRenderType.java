package net.farmingtales.ftboss.client.zicaiarcane;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.resources.ResourceLocation;

public final class ArcaneLightRenderType extends RenderType {
    public static final RenderType LIGHT = create(
        "ftboss_zicai_soft_light",
        DefaultVertexFormat.NEW_ENTITY,
        Mode.QUADS,
        262144,
        false,
        true,
        CompositeState.builder()
            .setShaderState(RENDERTYPE_EYES_SHADER)
            .setTextureState(new TextureStateShard(ResourceLocation.fromNamespaceAndPath("ftboss", "textures/fx/zicai_arcane/energy.png"), false, false))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setCullState(NO_CULL)
            .setWriteMaskState(COLOR_WRITE)
            .createCompositeState(false)
    );

    private ArcaneLightRenderType() {
        super("ftboss_zicai_soft_light", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 262144, false, true, () -> {
        }, () -> {
        });
    }
}
