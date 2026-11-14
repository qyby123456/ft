package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.util.QizaiCrystalMesh;
import net.minecraft.world.phys.Vec3;

public final class QizaiCrystalModelVisual {
    private QizaiCrystalModelVisual() {}

    public static void render(Vec3 origin, double age, double size, double fade, PoseStack pose, VertexConsumer vertices) {
        float scale = (float) (size * Math.max(0, Math.min(1, fade)));
        if (scale <= 0 || !Float.isFinite(scale)) return;
        pose.pushPose();
        try {
            pose.translate(origin.x, origin.y, origin.z);
            pose.scale(scale, scale, scale);
            QizaiCrystalMesh.render(0, age, false, QizaiMagicVisual.sink(pose, vertices));
        } finally { pose.popPose(); }
    }
}
