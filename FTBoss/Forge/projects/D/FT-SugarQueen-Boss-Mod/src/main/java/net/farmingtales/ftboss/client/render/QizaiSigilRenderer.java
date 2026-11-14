package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.entity.QizaiSigilEntity;
import net.farmingtales.ftboss.util.QizaiGeometry;
import net.farmingtales.ftboss.util.QizaiRules;
import net.farmingtales.ftboss.util.RemainingSpellModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public final class QizaiSigilRenderer extends EntityRenderer<QizaiSigilEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ftboss", "textures/item/qizai_prayer_staff.png");
    public QizaiSigilRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(QizaiSigilEntity entity) { return TEXTURE; }
    @Override public boolean shouldRender(QizaiSigilEntity entity, net.minecraft.client.renderer.culling.Frustum frustum,
                                          double cameraX, double cameraY, double cameraZ) {
        return entity.shouldRender(cameraX, cameraY, cameraZ)
                && frustum.isVisible(new AABB(entity.position(), entity.position()).inflate(
                        4 * net.farmingtales.ftboss.skill.SpellVisuals.playerRadiusScale(entity.getOwner()), 8,
                        4 * net.farmingtales.ftboss.skill.SpellVisuals.playerRadiusScale(entity.getOwner())));
    }

    @Override public void render(QizaiSigilEntity entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        double progress = entity.progress(partial);
        boolean step = entity.kind() == QizaiRules.STEP;
        boolean reduced = Minecraft.getInstance().options.particles().get() != ParticleStatus.ALL;
        pose.pushPose();
        try {
            var body = buffers.getBuffer(QizaiContrast.MODEL);
            pose.pushPose();
            try {
                if (!step) pose.translate(0, entity.released() ? 0.3 + progress * 1.7 : 0.4 + (1 - progress) * 4, 0);
                float scale = entity.released() ? (float) Math.max(0.001, 1 - progress) : 1;
                pose.scale(scale, scale, scale);
                RemainingSpellModels.drawScroll(entity.kind(), entity.tickCount + partial, progress, entity.released(),
                        QizaiScrollModelVisual.sink(pose, body));
            } finally { pose.popPose(); }
            var vertices = buffers.getBuffer(QizaiContrast.TYPE);
            QizaiGeometry.sigil(step ? 0.8 : QizaiRules.radius(entity.kind())
                    * net.farmingtales.ftboss.skill.SpellVisuals.playerRadiusScale(entity.getOwner()),
                    progress, entity.released(), reduced, QizaiMagicVisual.sink(pose, vertices));
        } finally { pose.popPose(); }
    }
}
