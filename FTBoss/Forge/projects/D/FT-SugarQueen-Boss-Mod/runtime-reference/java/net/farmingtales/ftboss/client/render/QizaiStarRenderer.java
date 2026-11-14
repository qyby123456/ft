/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneFlightTrails;
import net.farmingtales.ftboss.client.render.QizaiContrast;
import net.farmingtales.ftboss.client.render.QizaiMissileVisual;
import net.farmingtales.ftboss.client.render.QizaiScrollModelVisual;
import net.farmingtales.ftboss.entity.QizaiStarEntity;
import net.farmingtales.ftboss.util.RemainingSpellModels;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public final class QizaiStarRenderer
extends EntityRenderer<QizaiStarEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/item/qizai_prayer_staff.png");

    public QizaiStarRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation getTextureLocation(QizaiStarEntity star) {
        return TEXTURE;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * WARNING - void declaration
     */
    public void render(QizaiStarEntity qizaiStarEntity, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        void light;
        void yaw;
        void buffers;
        void pose;
        void partial;
        void star;
        ArcaneFlightTrails.capture((Entity)qizaiStarEntity, f2);
        if (star.isTrackingMissile()) {
            QizaiMissileVisual.render((Entity)star, (float)partial, true, (PoseStack)pose, (MultiBufferSource)buffers);
            super.m_7392_((Entity)star, (float)yaw, (float)partial, (PoseStack)pose, (MultiBufferSource)buffers, (int)light);
            return;
        }
        pose.m_85836_();
        try {
            pose.m_252781_(Axis.f_252436_.m_252977_(star.m_146908_()));
            pose.m_252781_(Axis.f_252529_.m_252977_(90.0f - star.m_146909_()));
            VertexConsumer body = buffers.m_6299_(QizaiContrast.MODEL);
            RemainingSpellModels.drawScroll(1, (float)star.f_19797_ + partial, 0.0, false, QizaiScrollModelVisual.sink((PoseStack)pose, body));
        }
        finally {
            pose.m_85849_();
        }
        super.m_7392_((Entity)star, (float)yaw, (float)partial, (PoseStack)pose, (MultiBufferSource)buffers, (int)light);
    }
}

