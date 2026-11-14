package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.model.DefectCoreGeoModel;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 故障机器人渲染器(方块方案): GeckoLib 模型 + 自动发光层; 胸口核心按球种换 tint 贴图。
 *
 * <p>(2026-09-11 按用户要求"如果模型没法精细就把模型做大一点") —— 模型整体**放大 {@value #MODEL_SCALE}×**:
 * 方块模型的细节受"1 个单位 = 1 像素"限制, 与其硬塞小方块, 不如把本体做大让细节看得见。
 * 用的是 GeckoLib 官方钩子 {@link #scaleModelForRender}(同时作用于本体与发光层),
 * 而不是去改几何坐标 —— 这样贴图 UV 一点不用动。
 * 实体碰撞箱已同步放大到 2.4×3.8(见 {@code FTEntities}), 视觉与判定一致。
 */
public class DefectCoreRenderer extends GeoEntityRenderer<DefectCoreEntity> {

    /** 模型放大倍数(几何 3.09 格高 → 视觉约 3.7 格) */
    public static final float MODEL_SCALE = 1.2F;

    public DefectCoreRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new DefectCoreGeoModel());
        this.shadowRadius = 1.0f;      // 跟着做大
        this.addRenderLayer(new software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void renderRecursively(PoseStack poseStack, DefectCoreEntity entity, GeoBone bone, RenderType renderType,
                                  MultiBufferSource buffers, VertexConsumer vertices, boolean isReRender,
                                  float partialTick, int light, int overlay, float red, float green, float blue, float alpha) {
        boolean anchor = bone.getName().equals("core") || bone.getName().equals("hand_l") || bone.getName().equals("hand_r");
        if (anchor) bone.setTrackingMatrices(true);
        super.renderRecursively(poseStack, entity, bone, renderType, buffers, vertices, isReRender,
                partialTick, light, overlay, red, green, blue, alpha);
        if (anchor && !isReRender) DefectOrbVisual.track(entity, bone.getName(), bone.getWorldPosition());
    }

    @Override
    protected float getDeathMaxRotation(DefectCoreEntity entity) {
        return 0.0F;
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                    DefectCoreEntity animatable, BakedGeoModel model, boolean isReRender,
                                    float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(widthScale * MODEL_SCALE, heightScale * MODEL_SCALE, poseStack, animatable, model,
                isReRender, partialTick, packedLight, packedOverlay);
    }
}
