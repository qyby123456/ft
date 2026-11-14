package net.farmingtales.ftboss.util;

import java.util.List;

public final class DandanScytheMotionRules {
    public static final int CHARGE_TICKS = 12;
    public static final int RECOVERY_TICKS = 28;
    public static final int SWEEP_ACTION = 4;
    public static final int BLINK_ACTION = 5;
    public static final int MAX_OWNERS = 64;
    public static final int TRAIL_LAYERS = 4;

    public record Joint(String name, String parent, float pivotX, float pivotY, float pivotZ) {}
    public record Bone(float moveX, float moveY, float moveZ, float rotateZ, float scale) {}
    public record Pose(float charge, float attack, float open, float recoil, float glow, float trail, float clock, boolean blink) {}

    public static final List<Joint> JOINTS = List.of(
            new Joint("handle", "", 8, 4, 8),
            new Joint("head", "handle", 8, 23.5F, 8),
            new Joint("blade_base", "head", 8, 24.5F, 8),
            new Joint("blade_mid", "blade_base", 0, 25, 8),
            new Joint("blade_tip", "blade_mid", -7, 20.5F, 8),
            new Joint("front_core", "head", 8, 24.9F, 5.9F),
            new Joint("back_core", "head", 8, 24.9F, 10.1F),
            new Joint("counter", "head", 9, 24.8F, 8),
            new Joint("pommel", "handle", 8, -4.8F, 8));

    private DandanScytheMotionRules() {}

    public static float bounded(float value) {
        return Float.isFinite(value) ? Math.max(0, Math.min(1, value)) : 0;
    }

    public static float smooth(float value) {
        float amount = bounded(value);
        return amount * amount * (3 - 2 * amount);
    }

    public static Pose sample(float useTicks, float swing, float releaseAge, boolean blink, float clock) {
        float charge = smooth(useTicks / CHARGE_TICKS);
        float attack = (float) Math.sin(Math.PI * bounded(swing));
        boolean releasing = Float.isFinite(releaseAge) && releaseAge >= 0 && releaseAge < RECOVERY_TICKS;
        float lingering = releasing ? 1 - smooth((releaseAge - 8) / (RECOVERY_TICKS - 8)) : 0;
        float recoil = releasing && releaseAge < 8 ? (float) Math.sin(Math.PI * releaseAge / 8) : 0;
        float open = Math.max(charge, Math.max(attack * 0.35F, lingering));
        float flash = releasing ? 1 - smooth(releaseAge / 10) : 0;
        return new Pose(charge, attack, open, recoil, 0.12F + charge * 0.55F + flash * 0.75F,
                Math.max(attack, Math.max(charge * 0.3F, flash)), Float.isFinite(clock) ? clock : 0, blink);
    }

    public static Bone bone(String name, Pose pose) {
        float open = pose.open, recoil = pose.recoil;
        return switch (name) {
            case "handle" -> new Bone(0, 0, 0, 4 * pose.charge - 10 * pose.attack - 12 * recoil, 1);
            case "head" -> new Bone(0, 0.65F * open, 0, -3 * open, 1);
            case "blade_base" -> new Bone(-0.25F * open, 0.3F * open, 0, -10 * open + 5 * recoil, 1);
            case "blade_mid" -> new Bone(-0.35F * open, 0.25F * open, 0, -12 * open + 6 * recoil, 1);
            case "blade_tip" -> new Bone(-0.25F * open, 0.1F * open, 0, -16 * open + 7 * recoil, 1);
            case "front_core" -> new Bone(0, 0, -1.7F * open, 36 * open + 45 * recoil, 1 + 0.22F * open);
            case "back_core" -> new Bone(0, 0, 1.7F * open, -36 * open - 45 * recoil, 1 + 0.22F * open);
            case "counter" -> new Bone(0.7F * open, 0, 0, 24 * open, 1);
            case "pommel" -> new Bone(0, -0.7F * open, 0, 0, 1 + 0.05F * open);
            default -> new Bone(0, 0, 0, 0, 1);
        };
    }

    public static boolean action(int kind) {
        return kind == SWEEP_ACTION || kind == BLINK_ACTION;
    }
}
