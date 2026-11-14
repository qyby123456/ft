import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Method;
import java.util.Locale;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;

public final class DoraSniperPoseProbe {
    public static void main(String[] arguments) throws Exception {
        Locale.setDefault(Locale.ROOT);
        EasterBossModel model = new EasterBossModel(EasterBossModel.createBodyLayer(true).bakeRoot());
        Method poseMethod = EasterBossModel.class.getDeclaredMethod("applyDoraSniperPose", boolean.class, float.class);
        poseMethod.setAccessible(true);
        ModelPart[] parts = {model.head, model.body, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg};
        float[][] boxes = {
                {-4, -8, -4, 4, 0, 4}, {-4, 0, -2, 4, 12, 2},
                {-2, -2, -2, 1, 10, 2}, {-1, -2, -2, 2, 10, 2},
                {-2, 0, -2, 2, 12, 2}, {-2, 0, -2, 2, 12, 2}
        };
        int checks = 0;
        for (boolean fired : new boolean[]{false, true}) {
            for (int frame = 0; frame <= 60; frame++) {
                for (ModelPart part : parts) part.resetPose();
                poseMethod.invoke(model, fired, frame / 60.0F);
                for (ModelPart part : parts) {
                    if (!Float.isFinite(part.xRot) || !Float.isFinite(part.yRot) || !Float.isFinite(part.zRot)) {
                        throw new AssertionError("Nonfinite sniper pose");
                    }
                    checks++;
                }
                if (model.leftArm.xRot != -0.20F) throw new AssertionError("Spear must stay lowered during sniper shot");
                if (!fired && Math.abs(model.rightArm.xRot + 1.52F) > 0.00001F) throw new AssertionError("Charge pose must be steady");
                if ((frame == 0 || frame == 60) && Math.abs(model.rightArm.xRot + 1.52F) > 0.00001F) {
                    throw new AssertionError("Recoil endpoints must match aiming pose");
                }
                if (frame % 12 != 0) continue;
                StringBuilder output = new StringBuilder("POSE_JSON={\"skill\":\"dora_sniper\",\"released\":")
                        .append(fired).append(",\"progress\":").append(frame / 60.0F).append(",\"parts\":[");
                for (int partIndex = 0; partIndex < parts.length; partIndex++) {
                    if (partIndex > 0) output.append(',');
                    output.append('[');
                    PoseStack pose = new PoseStack();
                    parts[partIndex].translateAndRotate(pose);
                    float[] box = boxes[partIndex];
                    for (int corner = 0; corner < 8; corner++) {
                        if (corner > 0) output.append(',');
                        Vector3f vertex = new Vector3f(box[(corner & 1) == 0 ? 0 : 3] / 16.0F,
                                box[(corner & 2) == 0 ? 1 : 4] / 16.0F, box[(corner & 4) == 0 ? 2 : 5] / 16.0F)
                                .mulPosition(pose.last().pose());
                        output.append(String.format("[%.6f,%.6f,%.6f]", vertex.x(), vertex.y(), vertex.z()));
                    }
                    output.append(']');
                }
                System.out.println(output.append("]}"));
            }
        }
        System.out.println("PASS: " + checks + " actual Dora model pose checks");
    }
}
