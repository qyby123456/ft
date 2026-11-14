package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.entity.QizaiStarEntity;
import net.farmingtales.ftboss.util.RemainingSpellModels;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class QizaiStarRenderer extends EntityRenderer<QizaiStarEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/item/qizai_prayer_staff.png");
    public QizaiStarRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(QizaiStarEntity star) { return TEXTURE; }

    @Override
    public void render(QizaiStarEntity star, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        if (star.isTrackingMissile()) {
            QizaiMissileVisual.render(star, partial, true, pose, buffers);
            super.render(star, yaw, partial, pose, buffers, light);
            return;
        }
        pose.pushPose();
        try {
            pose.mulPose(Axis.YP.rotationDegrees(star.getYRot()));
            pose.mulPose(Axis.XP.rotationDegrees(90 - star.getXRot()));
            var body = buffers.getBuffer(QizaiContrast.MODEL);
            RemainingSpellModels.drawScroll(1, star.tickCount + partial, 0, false, QizaiScrollModelVisual.sink(pose, body));
        } finally { pose.popPose(); }
        super.render(star, yaw, partial, pose, buffers, light);
    }
}
