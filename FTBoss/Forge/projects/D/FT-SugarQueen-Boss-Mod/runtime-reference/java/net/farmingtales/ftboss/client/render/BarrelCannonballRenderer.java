/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.world.entity.Entity
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoEntityRenderer
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.client.model.BarrelCannonballModel;
import net.farmingtales.ftboss.entity.BarrelCannonballEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BarrelCannonballRenderer
extends GeoEntityRenderer<BarrelCannonballEntity> {
    public BarrelCannonballRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, (GeoModel)new BarrelCannonballModel());
        this.withScale(0.18f);
        this.f_114477_ = 0.25f;
    }

    public void render(BarrelCannonballEntity entity, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight) {
        float t = (float)entity.f_19797_ + partialTick;
        pose.m_85836_();
        pose.m_252781_(Axis.f_252529_.m_252977_(t * 22.0f));
        pose.m_252781_(Axis.f_252436_.m_252977_(t * 17.0f));
        super.m_7392_((Entity)entity, entityYaw, partialTick, pose, buffer, packedLight);
        pose.m_85849_();
    }
}

