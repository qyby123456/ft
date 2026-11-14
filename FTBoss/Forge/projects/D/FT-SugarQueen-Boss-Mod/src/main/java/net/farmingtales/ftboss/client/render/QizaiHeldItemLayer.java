package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.QizaiRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

public final class QizaiHeldItemLayer extends ItemInHandLayer<EasterBossEntity, EasterBossModel> {
    public QizaiHeldItemLayer(RenderLayerParent<EasterBossEntity, EasterBossModel> parent, ItemInHandRenderer renderer) {
        super(parent, renderer);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, EasterBossEntity boss,
                       float limbSwing, float limbAmount, float partial, float age, float yaw, float pitch) {
        var cue = boss.getSkillCue();
        if (boss.getVariant() != EasterBossEntity.QIZAI_VARIANT || cue.getInt("qizai") != QizaiRules.PRAYER
                || cue.getBoolean("released")) {
            super.render(pose, buffers, light, boss, limbSwing, limbAmount, partial, age, yaw, pitch);
            return;
        }
        pose.pushPose();
        try {
            pose.translate(0, 0.6 + Math.sin(age * 0.1) * 0.035, -0.75);
            pose.mulPose(Axis.ZP.rotationDegrees(180));
            pose.mulPose(Axis.YP.rotationDegrees((float) Math.sin(age * 0.06) * 12));
            pose.scale(0.8F, 0.8F, 0.8F);
            Minecraft.getInstance().getItemRenderer().renderStatic(boss, boss.getMainHandItem(), ItemDisplayContext.NONE,
                    false, pose, buffers, boss.level(), light, OverlayTexture.NO_OVERLAY, boss.getId());
        } finally { pose.popPose(); }
    }
}
