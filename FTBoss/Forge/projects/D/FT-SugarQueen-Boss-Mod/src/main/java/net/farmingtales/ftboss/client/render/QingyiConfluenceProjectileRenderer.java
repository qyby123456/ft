package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.client.model.PathGeoModel;
import net.farmingtales.ftboss.entity.QingyiConfluenceTridentEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class QingyiConfluenceProjectileRenderer extends GeoEntityRenderer<QingyiConfluenceTridentEntity> {
    public QingyiConfluenceProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new PathGeoModel<>("geo/qingyi_confluence_trident.geo.json",
                "textures/item/qingyi_confluence_trident.png", "animations/qingyi_confluence_trident.animation.json"));
        shadowRadius = 0.1F;
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    protected void applyRotations(QingyiConfluenceTridentEntity entity, PoseStack pose, float age, float yaw, float partialTick) {
        pose.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) - 90.0F));
        pose.scale(0.65F, 0.65F, 0.65F);
        pose.translate(0, -47.0 / 16.0, 0);
    }
}
