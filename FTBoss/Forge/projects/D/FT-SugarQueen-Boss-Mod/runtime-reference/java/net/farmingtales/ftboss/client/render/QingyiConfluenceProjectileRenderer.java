/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.util.Mth
 *  software.bernie.geckolib.renderer.GeoEntityRenderer
 *  software.bernie.geckolib.renderer.GeoRenderer
 *  software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.client.model.PathGeoModel;
import net.farmingtales.ftboss.entity.QingyiConfluenceTridentEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class QingyiConfluenceProjectileRenderer
extends GeoEntityRenderer<QingyiConfluenceTridentEntity> {
    public QingyiConfluenceProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new PathGeoModel("geo/qingyi_confluence_trident.geo.json", "textures/item/qingyi_confluence_trident.png", "animations/qingyi_confluence_trident.animation.json"));
        this.f_114477_ = 0.1f;
        this.addRenderLayer((GeoRenderLayer)new AutoGlowingGeoLayer((GeoRenderer)this));
    }

    protected void applyRotations(QingyiConfluenceTridentEntity entity, PoseStack pose, float age, float yaw, float partialTick) {
        pose.m_252781_(Axis.f_252436_.m_252977_(Mth.m_14189_((float)partialTick, (float)entity.f_19859_, (float)entity.m_146908_()) - 90.0f));
        pose.m_252781_(Axis.f_252403_.m_252977_(Mth.m_14179_((float)partialTick, (float)entity.f_19860_, (float)entity.m_146909_()) - 90.0f));
        pose.m_85841_(0.65f, 0.65f, 0.65f);
        pose.m_85837_(0.0, -2.9375, 0.0);
    }
}

