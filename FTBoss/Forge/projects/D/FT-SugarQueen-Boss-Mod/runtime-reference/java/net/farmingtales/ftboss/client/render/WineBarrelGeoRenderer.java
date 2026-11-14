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
 *  software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.client.model.GiantBarrelModel;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
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
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class WineBarrelGeoRenderer
extends GeoEntityRenderer<WineBarrelBossEntity> {
    public static final String CROSSBOW_BONE = "spout";
    public static final float OFF_X = 0.0f;
    public static final float OFF_Y = 0.0f;
    public static final float OFF_Z = 0.0f;
    public static final float SCALE = 1.0f;
    public static final float YAW_DEG = 0.0f;

    public WineBarrelGeoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, (GeoModel)new GiantBarrelModel());
        this.f_114477_ = 1.4f;
        this.addRenderLayer((GeoRenderLayer)new CrossbowLayer((GeoRenderer<WineBarrelBossEntity>)this));
    }

    private static final class CrossbowLayer
    extends BlockAndItemGeoLayer<WineBarrelBossEntity> {
        CrossbowLayer(GeoRenderer<WineBarrelBossEntity> renderer) {
            super(renderer, (bone, animatable) -> {
                if (!WineBarrelGeoRenderer.CROSSBOW_BONE.equals(bone.getName())) {
                    return null;
                }
                ItemStack held = animatable.m_21205_();
                boolean isCrossbow = held.m_150930_((Item)FTBossItems.BARREL_CROSSBOW.get()) || held.m_150930_((Item)FTBossItems.BARREL_CROSSBOW_READY.get());
                return isCrossbow ? held : null;
            }, (bone, animatable) -> null);
        }

        protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, WineBarrelBossEntity animatable) {
            return ItemDisplayContext.NONE;
        }

        protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, WineBarrelBossEntity animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
            poseStack.m_85836_();
            super.renderStackForBone(poseStack, bone, stack, (GeoAnimatable)animatable, bufferSource, partialTick, packedLight, packedOverlay);
            poseStack.m_85849_();
        }
    }
}

