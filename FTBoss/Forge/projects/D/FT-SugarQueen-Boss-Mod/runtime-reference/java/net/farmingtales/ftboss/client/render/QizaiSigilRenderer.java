/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.culling.Frustum
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.AABB
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.render.QizaiContrast;
import net.farmingtales.ftboss.client.render.QizaiMagicVisual;
import net.farmingtales.ftboss.client.render.QizaiScrollModelVisual;
import net.farmingtales.ftboss.entity.QizaiSigilEntity;
import net.farmingtales.ftboss.util.QizaiGeometry;
import net.farmingtales.ftboss.util.QizaiRules;
import net.farmingtales.ftboss.util.RemainingSpellModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public final class QizaiSigilRenderer
extends EntityRenderer<QizaiSigilEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/item/qizai_prayer_staff.png");

    public QizaiSigilRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation getTextureLocation(QizaiSigilEntity entity) {
        return TEXTURE;
    }

    public boolean shouldRender(QizaiSigilEntity entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        return entity.m_6000_(cameraX, cameraY, cameraZ) && frustum.m_113029_(new AABB(entity.m_20182_(), entity.m_20182_()).m_82377_(4.0, 8.0, 4.0));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void render(QizaiSigilEntity entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        double progress = entity.progress(partial);
        boolean step = entity.kind() == 4;
        boolean reduced = Minecraft.m_91087_().f_91066_.m_231929_().m_231551_() != ParticleStatus.ALL;
        pose.m_85836_();
        try {
            VertexConsumer body = buffers.m_6299_(QizaiContrast.MODEL);
            pose.m_85836_();
            try {
                if (!step) {
                    pose.m_85837_(0.0, entity.released() ? 0.3 + progress * 1.7 : 0.4 + (1.0 - progress) * 4.0, 0.0);
                }
                float scale = entity.released() ? (float)Math.max(0.001, 1.0 - progress) : 1.0f;
                pose.m_85841_(scale, scale, scale);
                RemainingSpellModels.drawScroll(entity.kind(), (float)entity.f_19797_ + partial, progress, entity.released(), QizaiScrollModelVisual.sink(pose, body));
            }
            finally {
                pose.m_85849_();
            }
            VertexConsumer vertices = buffers.m_6299_(QizaiContrast.TYPE);
            QizaiGeometry.sigil(step ? 0.8 : QizaiRules.radius(entity.kind()), progress, entity.released(), reduced, QizaiMagicVisual.sink(pose, vertices));
        }
        finally {
            pose.m_85849_();
        }
    }
}

