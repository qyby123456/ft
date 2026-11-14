/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.world.entity.Entity
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoEntityRenderer
 *  software.bernie.geckolib.renderer.GeoRenderer
 *  software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.defectzero.DefectSkillAnchors;
import net.farmingtales.ftboss.client.defectzero.DefectSkillGeoModel;
import net.farmingtales.ftboss.client.render.DefectOrbVisual;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class DefectCoreRenderer
extends GeoEntityRenderer<DefectCoreEntity> {
    public static final float MODEL_SCALE = 1.2f;

    public DefectCoreRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, (GeoModel)new DefectSkillGeoModel());
        this.f_114477_ = 1.0f;
        this.addRenderLayer((GeoRenderLayer)new AutoGlowingGeoLayer((GeoRenderer)this));
    }

    public void renderRecursively(PoseStack poseStack, DefectCoreEntity entity, GeoBone bone, RenderType renderType, MultiBufferSource buffers, VertexConsumer vertices, boolean isReRender, float partialTick, int light, int overlay, float red, float green, float blue, float alpha) {
        boolean anchor;
        boolean bl = anchor = bone.getName().equals("core") || bone.getName().equals("hand_l") || bone.getName().equals("hand_r");
        if (anchor) {
            bone.setTrackingMatrices(true);
        }
        super.renderRecursively(poseStack, (Entity)entity, bone, renderType, buffers, vertices, isReRender, partialTick, light, overlay, red, green, blue, alpha);
        if (anchor && !isReRender) {
            DefectOrbVisual.track(entity, bone.getName(), bone.getWorldPosition());
        }
        DefectSkillAnchors.capture(entity, bone, isReRender);
    }

    protected float getDeathMaxRotation(DefectCoreEntity entity) {
        return 0.0f;
    }

    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, DefectCoreEntity animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(widthScale * 1.2f, heightScale * 1.2f, poseStack, (GeoAnimatable)animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}

