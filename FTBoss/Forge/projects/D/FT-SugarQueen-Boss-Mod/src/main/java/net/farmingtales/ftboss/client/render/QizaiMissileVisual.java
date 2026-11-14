package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.util.QizaiMissileModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public final class QizaiMissileVisual {
    private QizaiMissileVisual() {}

    public static void render(Entity missile, float partial, boolean arsenal, PoseStack pose, MultiBufferSource buffers) {
        pose.pushPose();
        try {
            pose.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(partial, missile.yRotO, missile.getYRot())));
            pose.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(partial, missile.xRotO, missile.getXRot())));
            double age = missile.tickCount + partial;
            pose.mulPose(Axis.ZP.rotationDegrees((float) (age * 1.7 + missile.getId() * 37 % 360)));
            var body = buffers.getBuffer(Hull.TYPE);
            QizaiMissileModel.body(arsenal, QizaiMagicVisual.sink(pose, body));
            var glow = buffers.getBuffer(QizaiContrast.TYPE);
            QizaiMissileModel.glow(age, arsenal, QizaiMagicVisual.sink(pose, glow));
        } finally { pose.popPose(); }
    }

    private static final class Hull extends RenderType {
        private static final RenderType TYPE = create("ftboss_qizai_missile_hull", DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS, 8192, false, false, CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER).setTransparencyState(NO_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_DEPTH_WRITE).setCullState(NO_CULL)
                        .createCompositeState(false));

        private Hull() {
            super("ftboss_qizai_missile_hull", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                    8192, false, false, () -> {}, () -> {});
        }
    }
}
