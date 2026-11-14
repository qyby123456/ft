import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Method;
import java.util.Locale;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;

public final class GongdiPoseProbe {
    public static void main(String[] arguments) throws Exception {
        Locale.setDefault(Locale.ROOT);
        EasterBossModel model = new EasterBossModel(EasterBossModel.createBodyLayer(true).bakeRoot());
        Method poseMethod = EasterBossModel.class.getDeclaredMethod("applyGongdiPose", String.class, boolean.class, float.class, float.class);
        poseMethod.setAccessible(true);
        ModelPart[] parts = {model.head, model.body, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg};
        float[][] bounds = {
                {-4, -8, -4, 4, 0, 4}, {-4, 0, -2, 4, 12, 2},
                {-2, -2, -2, 1, 10, 2}, {-1, -2, -2, 2, 10, 2},
                {-2, 0, -2, 2, 12, 2}, {-2, 0, -2, 2, 12, 2}
        };
        int checks = 0;
        for (String skill : new String[]{"decimate", "crippling_strike", "apprehend", "guillotine"}) {
            for (ModelPart part : parts) part.resetPose();
            poseMethod.invoke(model, skill, false, 1.0f, 1.0f);
            float[][] impact = new float[parts.length][3];
            for (int partIndex = 0; partIndex < parts.length; partIndex++) {
                impact[partIndex] = new float[]{parts[partIndex].xRot, parts[partIndex].yRot, parts[partIndex].zRot};
                parts[partIndex].resetPose();
            }
            poseMethod.invoke(model, skill, true, 0.0f, 1.0f);
            for (int partIndex = 0; partIndex < parts.length; partIndex++) {
                ModelPart part = parts[partIndex];
                if (Math.abs(part.xRot - impact[partIndex][0]) > 0.0001f
                        || Math.abs(part.yRot - impact[partIndex][1]) > 0.0001f
                        || Math.abs(part.zRot - impact[partIndex][2]) > 0.0001f) {
                    throw new AssertionError("Discontinuous impact: " + skill);
                }
                checks++;
            }
            for (boolean released : new boolean[]{false, true}) {
                for (int frame = 0; frame <= 60; frame++) {
                    float progress = frame / 60.0f;
                    for (ModelPart part : parts) part.resetPose();
                    poseMethod.invoke(model, skill, released, progress, 1.0f);
                    for (ModelPart part : parts) {
                        if (!Float.isFinite(part.xRot) || !Float.isFinite(part.yRot) || !Float.isFinite(part.zRot)) {
                            throw new AssertionError("Nonfinite pose: " + skill);
                        }
                        checks++;
                    }
                    if ((!released && frame == 39) || (released && (frame == 0 || frame == 24))) {
                        StringBuilder output = new StringBuilder("POSE_JSON={\"skill\":\"").append(skill)
                                .append("\",\"released\":").append(released).append(",\"progress\":").append(progress)
                                .append(",\"parts\":[");
                        for (int partIndex = 0; partIndex < parts.length; partIndex++) {
                            if (partIndex > 0) output.append(',');
                            output.append('[');
                            PoseStack pose = new PoseStack();
                            parts[partIndex].translateAndRotate(pose);
                            float[] box = bounds[partIndex];
                            for (int corner = 0; corner < 8; corner++) {
                                if (corner > 0) output.append(',');
                                Vector3f vertex = new Vector3f(box[(corner & 1) == 0 ? 0 : 3] / 16.0f,
                                        box[(corner & 2) == 0 ? 1 : 4] / 16.0f, box[(corner & 4) == 0 ? 2 : 5] / 16.0f)
                                        .mulPosition(pose.last().pose());
                                output.append(String.format("[%.6f,%.6f,%.6f]", vertex.x(), vertex.y(), vertex.z()));
                            }
                            output.append(']');
                        }
                        System.out.println(output.append("]}"));
                    }
                }
            }
        }
        System.out.println("GONGDI_POSE_CHECKS=" + checks);
    }
}
