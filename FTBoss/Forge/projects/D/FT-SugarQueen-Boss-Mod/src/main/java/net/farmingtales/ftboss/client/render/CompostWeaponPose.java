package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public final class CompostWeaponPose {
    public static final float SCALE = 0.63f;
    public static final float GRIP_OFFSET_X = 0.0f;
    public static final float GRIP_OFFSET_Y = -1.8f;
    public static final float GRIP_OFFSET_Z = -0.5f;
    public static final float MODEL_GRIP_X = 8.0f;
    public static final float MODEL_GRIP_Y = -0.5f;
    public static final float MODEL_GRIP_Z = 8.0f;

    private CompostWeaponPose() {}

    public static void apply(PoseStack poseStack) {
        poseStack.translate(GRIP_OFFSET_X / 16.0f, GRIP_OFFSET_Y / 16.0f, GRIP_OFFSET_Z / 16.0f);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.translate((8.0f - MODEL_GRIP_X) / 16.0f,
                (8.0f - MODEL_GRIP_Y) / 16.0f, (8.0f - MODEL_GRIP_Z) / 16.0f);
    }
}
