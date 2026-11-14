package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.client.model.CompostGeoModel;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.item.FTBossItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** 炫彩答辩超人渲染器(2026-09-12): 带发光膜层(彩虹油膜/稻草/黄铜发光) */
public class CompostRenderer extends GeoEntityRenderer<CompostBossEntity> {
    public CompostRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CompostGeoModel());
        this.shadowRadius = 1.2f;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        this.addRenderLayer(new BlockAndItemGeoLayer<CompostBossEntity>(this,
                (bone, animatable) -> {
                    if (!"hand_r".equals(bone.getName())) return null;
                    ItemStack held = animatable.getMainHandItem();
                    return held.is(FTBossItems.COMPOST_CROWN.get()) ? held : null;
                },
                (bone, animatable) -> null) {
            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, CompostBossEntity animatable) {
                return ItemDisplayContext.NONE;
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack,
                                              CompostBossEntity animatable, MultiBufferSource bufferSource,
                                              float partialTick, int packedLight, int packedOverlay) {
                poseStack.pushPose();
                try {
                    CompostWeaponPose.apply(poseStack);
                    super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource,
                            partialTick, packedLight, packedOverlay);
                } finally {
                    poseStack.popPose();
                }
            }
        });
    }
}
