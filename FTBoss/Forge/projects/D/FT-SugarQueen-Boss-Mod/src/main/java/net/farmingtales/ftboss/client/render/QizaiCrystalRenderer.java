package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.entity.QizaiCrystalEntity;
import net.farmingtales.ftboss.util.QizaiCrystalGeometry;
import net.farmingtales.ftboss.util.QizaiCrystalMesh;
import net.farmingtales.ftboss.util.QizaiCrystalRules;
import net.farmingtales.ftboss.util.QizaiGeometry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class QizaiCrystalRenderer extends EntityRenderer<QizaiCrystalEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/item/qizai_prayer_staff.png");

    public QizaiCrystalRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(QizaiCrystalEntity entity) { return TEXTURE; }

    @Override public boolean shouldRender(QizaiCrystalEntity entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        if (super.shouldRender(entity, frustum, cameraX, cameraY, cameraZ)) return true;
        var cue = entity.cue();
        Vec3 end = new Vec3(cue.getDouble("x"), cue.getDouble("y"), cue.getDouble("z"));
        return !cue.isEmpty() && entity.shouldRender(cameraX, cameraY, cameraZ)
                && end.distanceToSqr(entity.position()) <= 40 * 40 && frustum.isVisible(new AABB(entity.position(), end).inflate(1));
    }

    @Override public void render(QizaiCrystalEntity entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        try {
            pose.translate(0, 0.675, 0);
            var body = buffers.getBuffer(QizaiContrast.MODEL);
            QizaiCrystalMesh.render(entity.role(), entity.tickCount + partial, true, QizaiMagicVisual.sink(pose, body));
            var vertices = buffers.getBuffer(QizaiContrast.TYPE);
            QizaiCrystalGeometry.aura(entity.role(), entity.tickCount + partial,
                    entity.ward() / QizaiCrystalRules.WARD_CAPACITY, QizaiMagicVisual.sink(pose, vertices));
            var cue = entity.cue();
            if (!cue.isEmpty() && entity.level().getGameTime() <= cue.getLong("until")) {
                pose.translate(0, entity.getEyeHeight() - 0.675, 0);
                Vec3 start = entity.getPosition(partial).add(0, entity.getEyeHeight(), 0);
                Vec3 end = new Vec3(cue.getDouble("x"), cue.getDouble("y"), cue.getDouble("z")).subtract(start);
                QizaiCrystalGeometry.beam(new QizaiGeometry.Point(end.x, end.y, end.z), cue.getInt("phase") == 2,
                        QizaiMagicVisual.sink(pose, vertices));
            }
        } finally { pose.popPose(); }
        super.render(entity, yaw, partial, pose, buffers, light);
    }
}
