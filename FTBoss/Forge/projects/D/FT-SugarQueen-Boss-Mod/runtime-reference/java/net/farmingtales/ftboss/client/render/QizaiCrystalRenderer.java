/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.culling.Frustum
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.render.QizaiContrast;
import net.farmingtales.ftboss.client.render.QizaiMagicVisual;
import net.farmingtales.ftboss.entity.QizaiCrystalEntity;
import net.farmingtales.ftboss.util.QizaiCrystalGeometry;
import net.farmingtales.ftboss.util.QizaiCrystalMesh;
import net.farmingtales.ftboss.util.QizaiGeometry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class QizaiCrystalRenderer
extends EntityRenderer<QizaiCrystalEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/item/qizai_prayer_staff.png");

    public QizaiCrystalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation getTextureLocation(QizaiCrystalEntity entity) {
        return TEXTURE;
    }

    public boolean shouldRender(QizaiCrystalEntity entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        if (super.m_5523_((Entity)entity, frustum, cameraX, cameraY, cameraZ)) {
            return true;
        }
        CompoundTag cue = entity.cue();
        Vec3 end = new Vec3(cue.m_128459_("x"), cue.m_128459_("y"), cue.m_128459_("z"));
        return !cue.m_128456_() && entity.m_6000_(cameraX, cameraY, cameraZ) && end.m_82557_(entity.m_20182_()) <= 1600.0 && frustum.m_113029_(new AABB(entity.m_20182_(), end).m_82400_(1.0));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void render(QizaiCrystalEntity entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.m_85836_();
        try {
            pose.m_85837_(0.0, 0.675, 0.0);
            VertexConsumer body = buffers.m_6299_(QizaiContrast.MODEL);
            QizaiCrystalMesh.render(entity.role(), (float)entity.f_19797_ + partial, true, QizaiMagicVisual.sink(pose, body));
            VertexConsumer vertices = buffers.m_6299_(QizaiContrast.TYPE);
            QizaiCrystalGeometry.aura(entity.role(), (float)entity.f_19797_ + partial, entity.ward() / 240.0f, QizaiMagicVisual.sink(pose, vertices));
            CompoundTag cue = entity.cue();
            if (!cue.m_128456_() && entity.m_9236_().m_46467_() <= cue.m_128454_("until")) {
                pose.m_85837_(0.0, (double)entity.m_20192_() - 0.675, 0.0);
                Vec3 start = entity.m_20318_(partial).m_82520_(0.0, (double)entity.m_20192_(), 0.0);
                Vec3 end = new Vec3(cue.m_128459_("x"), cue.m_128459_("y"), cue.m_128459_("z")).m_82546_(start);
                QizaiCrystalGeometry.beam(new QizaiGeometry.Point(end.f_82479_, end.f_82480_, end.f_82481_), cue.m_128451_("phase") == 2, QizaiMagicVisual.sink(pose, vertices));
            }
        }
        finally {
            pose.m_85849_();
        }
        super.m_7392_((Entity)entity, yaw, partial, pose, buffers, light);
    }
}

