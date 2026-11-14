/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneFlightTrails;
import net.farmingtales.ftboss.client.render.QizaiMissileVisual;
import net.farmingtales.ftboss.entity.QizaiCrystalMissileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public final class QizaiCrystalMissileRenderer
extends EntityRenderer<QizaiCrystalMissileEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/item/qizai_prayer_staff.png");

    public QizaiCrystalMissileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation getTextureLocation(QizaiCrystalMissileEntity entity) {
        return TEXTURE;
    }

    /*
     * WARNING - void declaration
     */
    public void render(QizaiCrystalMissileEntity qizaiCrystalMissileEntity, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        void light;
        void yaw;
        void buffers;
        void pose;
        void partial;
        void entity;
        ArcaneFlightTrails.capture((Entity)qizaiCrystalMissileEntity, f2);
        QizaiMissileVisual.render((Entity)entity, (float)partial, false, (PoseStack)pose, (MultiBufferSource)buffers);
        super.m_7392_((Entity)entity, (float)yaw, (float)partial, (PoseStack)pose, (MultiBufferSource)buffers, (int)light);
    }
}

