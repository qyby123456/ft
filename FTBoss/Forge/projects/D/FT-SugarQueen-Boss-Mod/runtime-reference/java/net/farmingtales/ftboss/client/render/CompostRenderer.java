/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoEntityRenderer
 *  software.bernie.geckolib.renderer.GeoRenderer
 *  software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer
 *  software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.client.model.CompostGeoModel;
import net.farmingtales.ftboss.client.render.CompostWeaponPose;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.item.FTBossItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class CompostRenderer
extends GeoEntityRenderer<CompostBossEntity> {
    public CompostRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, (GeoModel)new CompostGeoModel());
        this.f_114477_ = 1.2f;
        this.addRenderLayer((GeoRenderLayer)new AutoGlowingGeoLayer((GeoRenderer)this));
        this.addRenderLayer((GeoRenderLayer)new BlockAndItemGeoLayer<CompostBossEntity>((GeoRenderer)this, (bone, animatable) -> {
            if (!"hand_r".equals(bone.getName())) {
                return null;
            }
            ItemStack held = animatable.m_21205_();
            return held.m_150930_((Item)FTBossItems.COMPOST_CROWN.get()) ? held : null;
        }, (bone, animatable) -> null){

            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, CompostBossEntity animatable) {
                return ItemDisplayContext.NONE;
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, CompostBossEntity animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                poseStack.m_85836_();
                try {
                    CompostWeaponPose.apply(poseStack);
                    super.renderStackForBone(poseStack, bone, stack, (GeoAnimatable)animatable, bufferSource, partialTick, packedLight, packedOverlay);
                }
                finally {
                    poseStack.m_85849_();
                }
            }
        });
    }
}

