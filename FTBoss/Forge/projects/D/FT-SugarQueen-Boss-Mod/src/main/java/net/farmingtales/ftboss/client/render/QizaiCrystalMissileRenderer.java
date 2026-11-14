package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.entity.QizaiCrystalMissileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class QizaiCrystalMissileRenderer extends EntityRenderer<QizaiCrystalMissileEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/item/qizai_prayer_staff.png");
    public QizaiCrystalMissileRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(QizaiCrystalMissileEntity entity) { return TEXTURE; }

    @Override public void render(QizaiCrystalMissileEntity entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        QizaiMissileVisual.render(entity, partial, false, pose, buffers);
        super.render(entity, yaw, partial, pose, buffers, light);
    }
}
