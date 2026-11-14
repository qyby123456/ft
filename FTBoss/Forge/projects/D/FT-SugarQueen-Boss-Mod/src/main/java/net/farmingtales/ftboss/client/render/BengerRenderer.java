package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.model.PathGeoModel;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.object.Color;

/**
 * 【benger 的渲染器】(2026-09-14 v133 按用户要求「虚化时透明度变成一半」)。
 *
 * <p>就是 {@link PathGeoRenderer} 加一件事：**虚影（无敌）期间换成半透明渲染**。
 * 关键一步是换掉渲染管线 —— 不透明管线（entityCutoutNoCull）**根本不看 alpha**，
 * 所以虚影时必须返回 {@link RenderType#entityTranslucent}，模型才会按贴图的 alpha/着色器的 alpha 画出来。
 * （贴图本身是全不透明的，GeckoLib 会把实体的 renderColor 交给 {@code RenderSystem.setShaderColor}，
 * 半透明管线才吃这个 alpha；这一步由 GeckoLib 自己在虚影实体的 `getRenderColor` 里处理，
 * 我们只需要让"虚影标记"能被它读到 —— 见 {@code BengerEntity.isIntangible()}。）
 */
public class BengerRenderer extends PathGeoRenderer<BengerEntity> {

    public BengerRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context ctx, float shadow) {
        super(ctx, new PathGeoModel<>("geo/benger.geo.json", "textures/entity/benger.png",
                "animations/benger.animation.json"), shadow);
    }

    @Override
    public RenderType getRenderType(BengerEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        if (animatable.isIntangible()) {
            return RenderType.entityTranslucent(texture);
        }
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    @Override
    public Color getRenderColor(BengerEntity animatable, float partialTick, int packedLight) {
        if (!animatable.isIntangible()) return super.getRenderColor(animatable, partialTick, packedLight);
        float pulse = (float) Math.sin((animatable.tickCount + partialTick) * 0.45);
        return Color.ofRGBA(0.55f, 0.85f, 1.0f, 0.28f + pulse * 0.08f);
    }
}
