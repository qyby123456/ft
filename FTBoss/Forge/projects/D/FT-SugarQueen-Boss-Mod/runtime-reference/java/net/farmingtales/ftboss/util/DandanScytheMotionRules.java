/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.List;

public final class DandanScytheMotionRules {
    public static final int CHARGE_TICKS = 12;
    public static final int RECOVERY_TICKS = 28;
    public static final int SWEEP_ACTION = 4;
    public static final int BLINK_ACTION = 5;
    public static final int MAX_OWNERS = 64;
    public static final int TRAIL_LAYERS = 4;
    public static final List<Joint> JOINTS = List.of(new Joint("handle", "", 8.0f, 4.0f, 8.0f), new Joint("head", "handle", 8.0f, 23.5f, 8.0f), new Joint("blade_base", "head", 8.0f, 24.5f, 8.0f), new Joint("blade_mid", "blade_base", 0.0f, 25.0f, 8.0f), new Joint("blade_tip", "blade_mid", -7.0f, 20.5f, 8.0f), new Joint("front_core", "head", 8.0f, 24.9f, 5.9f), new Joint("back_core", "head", 8.0f, 24.9f, 10.1f), new Joint("counter", "head", 9.0f, 24.8f, 8.0f), new Joint("pommel", "handle", 8.0f, -4.8f, 8.0f));

    private DandanScytheMotionRules() {
    }

    public static float bounded(float value) {
        return Float.isFinite(value) ? Math.max(0.0f, Math.min(1.0f, value)) : 0.0f;
    }

    public static float smooth(float value) {
        float amount = DandanScytheMotionRules.bounded(value);
        return amount * amount * (3.0f - 2.0f * amount);
    }

    public static Pose sample(float useTicks, float swing, float releaseAge, boolean blink, float clock) {
        float charge = DandanScytheMotionRules.smooth(useTicks / 12.0f);
        float attack = (float)Math.sin(Math.PI * (double)DandanScytheMotionRules.bounded(swing));
        boolean releasing = Float.isFinite(releaseAge) && releaseAge >= 0.0f && releaseAge < 28.0f;
        float lingering = releasing ? 1.0f - DandanScytheMotionRules.smooth((releaseAge - 8.0f) / 20.0f) : 0.0f;
        float recoil = releasing && releaseAge < 8.0f ? (float)Math.sin(Math.PI * (double)releaseAge / 8.0) : 0.0f;
        float open = Math.max(charge, Math.max(attack * 0.35f, lingering));
        float flash = releasing ? 1.0f - DandanScytheMotionRules.smooth(releaseAge / 10.0f) : 0.0f;
        return new Pose(charge, attack, open, recoil, 0.12f + charge * 0.55f + flash * 0.75f, Math.max(attack, Math.max(charge * 0.3f, flash)), Float.isFinite(clock) ? clock : 0.0f, blink);
    }

    public static Bone bone(String name, Pose pose) {
        float open = pose.open;
        float recoil = pose.recoil;
        return switch (name) {
            case "handle" -> new Bone(0.0f, 0.0f, 0.0f, 4.0f * pose.charge - 10.0f * pose.attack - 12.0f * recoil, 1.0f);
            case "head" -> new Bone(0.0f, 0.65f * open, 0.0f, -3.0f * open, 1.0f);
            case "blade_base" -> new Bone(-0.25f * open, 0.3f * open, 0.0f, -10.0f * open + 5.0f * recoil, 1.0f);
            case "blade_mid" -> new Bone(-0.35f * open, 0.25f * open, 0.0f, -12.0f * open + 6.0f * recoil, 1.0f);
            case "blade_tip" -> new Bone(-0.25f * open, 0.1f * open, 0.0f, -16.0f * open + 7.0f * recoil, 1.0f);
            case "front_core" -> new Bone(0.0f, 0.0f, -1.7f * open, 36.0f * open + 45.0f * recoil, 1.0f + 0.22f * open);
            case "back_core" -> new Bone(0.0f, 0.0f, 1.7f * open, -36.0f * open - 45.0f * recoil, 1.0f + 0.22f * open);
            case "counter" -> new Bone(0.7f * open, 0.0f, 0.0f, 24.0f * open, 1.0f);
            case "pommel" -> new Bone(0.0f, -0.7f * open, 0.0f, 0.0f, 1.0f + 0.05f * open);
            default -> new Bone(0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
        };
    }

    public static boolean action(int kind) {
        return kind == 4 || kind == 5;
    }

    public record Pose(float charge, float attack, float open, float recoil, float glow, float trail, float clock, boolean blink) {
    }

    public record Bone(float moveX, float moveY, float moveZ, float rotateZ, float scale) {
    }

    public record Joint(String name, String parent, float pivotX, float pivotY, float pivotZ) {
    }
}

