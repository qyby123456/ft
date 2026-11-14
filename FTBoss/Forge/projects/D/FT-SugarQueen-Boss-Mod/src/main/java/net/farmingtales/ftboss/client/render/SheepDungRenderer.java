package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.SheepDungEntity;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * 羊屎蛋渲染器(2026-09-12): 复用**岩浆怪**的 {@link LavaSlimeModel} + 挤压形变, 只换暗棕黑贴图。
 * 刻意不覆盖亮度 —— 岩浆怪那种"自发光"观感来自它自己的亮橙贴图, 换成棕黑后就是普通羊粪蛋。
 */
public class SheepDungRenderer extends MobRenderer<SheepDungEntity, LavaSlimeModel<SheepDungEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FTBossMod.MOD_ID, "textures/entity/sheep_dung.png");

    public SheepDungRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new LavaSlimeModel<>(ctx.bakeLayer(ModelLayers.MAGMA_CUBE)), 0.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(SheepDungEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(SheepDungEntity entity, PoseStack poseStack, float partialTicks) {
        float size = (float) entity.getSize();
        float squish = Mth.lerp(partialTicks, entity.oSquish, entity.squish) / (size * 0.5f + 1.0f);
        float f = 1.0f / (squish + 1.0f);
        poseStack.scale(0.999f, 0.999f, 0.999f);
        poseStack.translate(0.0f, 0.001f, 0.0f);
        poseStack.scale(f * size, 1.0f / f * size, f * size);
    }
}
