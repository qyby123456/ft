/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class XuanlanMotionRules {
    public static final int SHOT_MAIN = 6;
    public static final int SHOT_OFF = 7;
    public static final int THROW_MAIN = 8;
    public static final int THROW_OFF = 9;
    public static final int CATCH_MAIN = 10;
    public static final int CATCH_OFF = 11;
    public static final int SECOND_THROW_MAIN = 12;
    public static final int SECOND_CATCH_MAIN = 14;
    public static final int LOST_MAIN = 16;
    public static final int SECOND_LOST_MAIN = 18;
    public static final int RECOVERY_TICKS = 12;
    public static final int TRAIL_POINTS = 9;
    public static final int MAX_TRAILS = 64;

    private XuanlanMotionRules() {
    }

    public static float smooth(float value) {
        float bounded = Float.isFinite(value) ? Math.max(0.0f, Math.min(1.0f, value)) : 0.0f;
        return bounded * bounded * (3.0f - 2.0f * bounded);
    }

    public static Pose sample(float useTicks, float shotAge, float throwAge, float catchAge, float clock) {
        float charge = XuanlanMotionRules.smooth(useTicks / 20.0f);
        float recoil = shotAge >= 0.0f && shotAge < 8.0f ? 1.0f - XuanlanMotionRules.smooth(shotAge / 8.0f) : 0.0f;
        float fold = throwAge >= 0.0f && throwAge < 60.0f ? XuanlanMotionRules.smooth(throwAge / 3.0f) * (1.0f - XuanlanMotionRules.smooth((throwAge - 60.0f + 6.0f) / 6.0f)) : 0.0f;
        float pulse = 0.0f;
        if (catchAge >= 0.0f && catchAge < 12.0f) {
            fold = 1.0f - XuanlanMotionRules.smooth(catchAge / 5.0f);
            pulse = (float)Math.sin(Math.PI * (double)Math.min(1.0f, catchAge / 12.0f));
        }
        return new Pose(charge, recoil, fold, pulse, Float.isFinite(clock) ? clock : 0.0f);
    }

    public static Bone bone(String name, Pose pose) {
        float charge = pose.charge;
        float recoil = pose.recoil;
        float pulse = pose.catchPulse;
        return switch (name) {
            case "gun_upper" -> new Bone(-0.5f * charge - 1.6f * recoil, 0.85f * charge, 2.8f * charge, 1.0f);
            case "gun_lower" -> new Bone(-0.5f * charge - 1.6f * recoil, -0.85f * charge, -2.8f * charge, 1.0f);
            case "gun_core" -> new Bone(1.5f * charge - 2.1f * recoil, 0.0f, 0.0f, 1.0f + 0.12f * charge);
            case "gun_stock" -> new Bone(-0.8f * recoil, 0.0f, 0.0f, 1.0f);
            case "wheel_upper" -> new Bone(-0.7f * pose.fold, -2.2f * pose.fold + 0.6f * pulse, 32.0f * pose.fold - 7.0f * pulse, 1.0f);
            case "wheel_lower" -> new Bone(0.7f * pose.fold, 2.2f * pose.fold - 0.6f * pulse, 32.0f * pose.fold - 7.0f * pulse, 1.0f);
            case "wheel_locks" -> new Bone(0.0f, 0.0f, -24.0f * pose.fold + 14.0f * pulse, 1.0f - 0.18f * pose.fold);
            case "wheel_hub" -> new Bone(0.0f, 0.0f, 0.0f, 1.0f + 0.06f * pulse);
            default -> new Bone(0.0f, 0.0f, 0.0f, 1.0f);
        };
    }

    public static int throwKind(int slot, boolean offhand) {
        return (slot == 0 ? 8 : 12) + (offhand ? 1 : 0);
    }

    public static int catchKind(int slot, boolean offhand) {
        return (slot == 0 ? 10 : 14) + (offhand ? 1 : 0);
    }

    public static int lostKind(int slot, boolean offhand) {
        return (slot == 0 ? 16 : 18) + (offhand ? 1 : 0);
    }

    public static boolean isThrow(int kind) {
        return kind == 8 || kind == 9 || kind == 12 || kind == 13;
    }

    public static boolean isCatch(int kind) {
        return kind == 10 || kind == 11 || kind == 14 || kind == 15;
    }

    public static int bladeSlot(int kind) {
        return kind >= 12 && kind < 16 || kind >= 18 ? 1 : 0;
    }

    public static boolean action(int kind) {
        return kind >= 6 && kind <= 19;
    }

    public static boolean offhand(int kind) {
        return (kind & 1) != 0;
    }

    public record Pose(float charge, float recoil, float fold, float catchPulse, float clock) {
    }

    public record Bone(float moveX, float moveY, float rotateZ, float scale) {
    }
}

