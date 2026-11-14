package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.entity.FlywheelProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * 【飞轮渲染器】(2026-09-12 v42 按用户要求"玄蓝的飞轮可以旋转")。
 *
 * <p>原版 {@code ThrownItemRenderer} 不会让物品自转, 所以这里自己画:
 * 先按飞行朝向(实体 yaw)转, 再绕**盘面法线**持续自转(模型盘面在 XY 平面, 所以转 Z 轴),
 * 看上去就是一枚呼呼转的飞轮。物品底座沿用 GROUND 变换(缩放 0.4)。
 */
public class FlywheelRenderer extends EntityRenderer<FlywheelProjectileEntity> {

    public static final float PROJECTILE_SCALE = 0.75F;

    private final net.minecraft.client.renderer.entity.ItemRenderer itemRenderer;

    public FlywheelRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(FlywheelProjectileEntity entity, float entityYaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight) {
        if (entity.isInvisible()) return;
        pose.pushPose();
        pose.scale(PROJECTILE_SCALE, PROJECTILE_SCALE, PROJECTILE_SCALE);
        pose.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        /* 自转: 约 25 度/tick(≈1.4 圈/秒) */
        pose.mulPose(Axis.ZP.rotationDegrees((entity.tickCount + partialTick) * 25.0F));
        this.itemRenderer.renderStatic(entity.getItem(), ItemDisplayContext.GROUND, packedLight,
                OverlayTexture.NO_OVERLAY, pose, buffer, entity.level(), entity.getId());
        pose.popPose();
        super.render(entity, entityYaw, partialTick, pose, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FlywheelProjectileEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
