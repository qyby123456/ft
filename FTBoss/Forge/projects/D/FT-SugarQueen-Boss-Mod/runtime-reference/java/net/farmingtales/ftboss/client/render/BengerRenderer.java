/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  org.jetbrains.annotations.Nullable
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.object.Color
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.model.PathGeoModel;
import net.farmingtales.ftboss.client.render.PathGeoRenderer;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.object.Color;

public class BengerRenderer
extends PathGeoRenderer<BengerEntity> {
    public BengerRenderer(EntityRendererProvider.Context ctx, float shadow) {
        super(ctx, new PathGeoModel("geo/benger.geo.json", "textures/entity/benger.png", "animations/benger.animation.json"), shadow);
    }

    public RenderType getRenderType(BengerEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        if (animatable.isIntangible()) {
            return RenderType.m_110473_((ResourceLocation)texture);
        }
        return super.getRenderType((GeoAnimatable)animatable, texture, bufferSource, partialTick);
    }

    public Color getRenderColor(BengerEntity bengerEntity, float f, int n) {
        if (!bengerEntity.isIntangible()) {
            return super.getRenderColor((GeoAnimatable)bengerEntity, f, n);
        }
        float f2 = (float)Math.sin((double)((float)bengerEntity.f_19797_ + f) * 0.45);
        return Color.ofRGBA((float)0.55f, (float)0.85f, (float)1.0f, (float)(0.28f + f2 * 0.08f));
    }
}

