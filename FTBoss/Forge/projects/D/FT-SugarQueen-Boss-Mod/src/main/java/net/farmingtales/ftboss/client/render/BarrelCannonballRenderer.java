package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.client.model.BarrelCannonballModel;
import net.farmingtales.ftboss.entity.BarrelCannonballEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 酒桶炮弹渲染器(2026-09-10 新增): 巨桶模型缩到 0.18(≈0.5 格高) + 飞行时翻滚。
 * 用的就是酒桶王那套 geo/贴图, 所以没有新增模型文件。
 */
public class BarrelCannonballRenderer extends GeoEntityRenderer<BarrelCannonballEntity> {

    public BarrelCannonballRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new BarrelCannonballModel());
        this.withScale(0.18f);      // 巨桶约 3 格 -> 炮弹约 0.54 格
        this.shadowRadius = 0.25f;
    }

    @Override
    public void render(BarrelCannonballEntity entity, float entityYaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight) {
        // 翻滚(绕 X/Y 轴), 让"炮弹"飞起来有旋转感
        float t = entity.tickCount + partialTick;
        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(t * 22.0f));
        pose.mulPose(Axis.YP.rotationDegrees(t * 17.0f));
        super.render(entity, entityYaw, partialTick, pose, buffer, packedLight);
        pose.popPose();
    }
}
