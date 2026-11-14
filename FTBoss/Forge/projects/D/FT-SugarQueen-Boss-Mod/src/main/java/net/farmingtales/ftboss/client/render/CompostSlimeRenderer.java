package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.CompostSlimeEntity;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * 棕黑史莱姆渲染器(2026-09-12): 复用原版 {@link SlimeModel} 几何, 只换贴图。
 * 挤压形变(scale)是从原版 {@code SlimeRenderer} 抄的 —— 不继承它是因为泛型绑死在原版 Slime 上。
 */
public class CompostSlimeRenderer extends MobRenderer<CompostSlimeEntity, SlimeModel<CompostSlimeEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FTBossMod.MOD_ID, "textures/entity/compost_slime.png");

    public CompostSlimeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new SlimeModel<>(ctx.bakeLayer(ModelLayers.SLIME)), 0.25f);
        this.addLayer(new SlimeOuterLayer<>(this, ctx.getModelSet()));   // 半透明外壳
    }

    @Override
    public ResourceLocation getTextureLocation(CompostSlimeEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(CompostSlimeEntity entity, PoseStack poseStack, float partialTicks) {
        float size = (float) entity.getSize();
        float squish = Mth.lerp(partialTicks, entity.oSquish, entity.squish) / (size * 0.5f + 1.0f);
        float f = 1.0f / (squish + 1.0f);
        poseStack.scale(0.999f, 0.999f, 0.999f);
        poseStack.translate(0.0f, 0.001f, 0.0f);
        poseStack.scale(f * size, 1.0f / f * size, f * size);
    }
}
