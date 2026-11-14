import java.lang.reflect.Method;
import java.util.Locale;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.util.EasterCombatMath;
import net.minecraft.client.model.geom.ModelPart;

public final class CombatProbe {
    public static void main(String[] arguments) throws Exception {
        Locale.setDefault(Locale.ROOT);
        int checks = 0;
        for (int duration : new int[]{10, 12, 14, 15, 16, 18, 20, 30, 55}) {
            for (int frameRate : new int[]{30, 60, 144}) {
                float previous = -1.0f;
                for (int frame = 0; frame <= duration * frameRate / 20 + 1; frame++) {
                    float age = frame * 20.0f / frameRate;
                    float progress = EasterCombatMath.progress(age, duration, false);
                    float weight = EasterCombatMath.weight(age, duration, false);
                    require(Float.isFinite(weight) && weight >= 0.0f && weight <= 1.0f, "weight bounds");
                    require(progress >= previous && progress <= 1.0f, "monotonic progress");
                    previous = progress;
                    checks += 2;
                }
            }
            require(EasterCombatMath.weight(duration, duration, false) == 0.0f, "animation ends");
            require(EasterCombatMath.weight(-1.0f, duration, false) == 0.0f, "future animation hidden");
            require(EasterCombatMath.weight(0.0f, duration, true) == 1.0f, "impact immediate");
            require(EasterCombatMath.progress(0.0f, duration, true) == 0.45f, "impact frame aligned");
            checks += 4;
        }
        require(EasterCombatMath.weight(1.0f, 0, false) == 0.0f, "empty state");
        System.out.println("MATH_CHECKS=" + (checks + 1));

        EasterBossModel model = new EasterBossModel(EasterBossModel.createBodyLayer(true).bakeRoot());
        Method pose = EasterBossModel.class.getDeclaredMethod("applyWeaponPose", int.class, float.class, float.class);
        pose.setAccessible(true);
        ModelPart[] parts = {model.head, model.body, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg};
        for (int style = 0; style < 3; style++) {
            for (float progress : new float[]{0.0f, 0.2f, 0.35f, 0.45f, 0.55f, 0.75f, 0.95f, 1.0f}) {
                for (ModelPart part : parts) part.resetPose();
                float weight = EasterCombatMath.weight(progress * 20.0f, 20, false);
                pose.invoke(model, style, progress, weight);
                StringBuilder output = new StringBuilder("POSE|").append(style).append('|').append(progress);
                for (ModelPart part : parts) {
                    require(Float.isFinite(part.xRot) && Float.isFinite(part.yRot) && Float.isFinite(part.zRot), "finite pose");
                    output.append(String.format("|%.6f,%.6f,%.6f", part.xRot, part.yRot, part.zRot));
                }
                System.out.println(output);
            }
        }
        for (java.lang.reflect.Field field : EasterBossModel.class.getDeclaredFields()) {
            require(!field.getName().equals("actionBlend") && !field.getName().equals("castBlend")
                    && !field.getName().equals("lastAge"), "shared render state removed");
        }
        System.out.println("POSE_CHECKS=24");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
