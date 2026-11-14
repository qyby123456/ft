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

    public record Pose(float charge, float recoil, float fold, float catchPulse, float clock) {}
    public record Bone(float moveX, float moveY, float rotateZ, float scale) {}

    private XuanlanMotionRules() {}

    public static float smooth(float value) {
        float bounded = Float.isFinite(value) ? Math.max(0, Math.min(1, value)) : 0;
        return bounded * bounded * (3 - 2 * bounded);
    }

    public static Pose sample(float useTicks, float shotAge, float throwAge, float catchAge, float clock) {
        float charge = smooth(useTicks / XuanlanRules.CHARGE_TICKS);
        float recoil = shotAge >= 0 && shotAge < 8 ? (1 - smooth(shotAge / 8)) : 0;
        float fold = throwAge >= 0 && throwAge < XuanlanRules.MAX_FLIGHT_TICKS
                ? smooth(throwAge / 3) * (1 - smooth((throwAge - XuanlanRules.MAX_FLIGHT_TICKS + 6) / 6)) : 0;
        float pulse = 0;
        if (catchAge >= 0 && catchAge < RECOVERY_TICKS) {
            fold = 1 - smooth(catchAge / 5);
            pulse = (float) Math.sin(Math.PI * Math.min(1, catchAge / RECOVERY_TICKS));
        }
        return new Pose(charge, recoil, fold, pulse, Float.isFinite(clock) ? clock : 0);
    }

    public static Bone bone(String name, Pose pose) {
        float charge = pose.charge, recoil = pose.recoil, pulse = pose.catchPulse;
        return switch (name) {
            case "gun_upper" -> new Bone(-0.5F * charge - 1.6F * recoil, 0.85F * charge, 2.8F * charge, 1);
            case "gun_lower" -> new Bone(-0.5F * charge - 1.6F * recoil, -0.85F * charge, -2.8F * charge, 1);
            case "gun_core" -> new Bone(1.5F * charge - 2.1F * recoil, 0, 0, 1 + 0.12F * charge);
            case "gun_stock" -> new Bone(-0.8F * recoil, 0, 0, 1);
            case "wheel_upper" -> new Bone(-0.7F * pose.fold, -2.2F * pose.fold + 0.6F * pulse, 32 * pose.fold - 7 * pulse, 1);
            case "wheel_lower" -> new Bone(0.7F * pose.fold, 2.2F * pose.fold - 0.6F * pulse, 32 * pose.fold - 7 * pulse, 1);
            case "wheel_locks" -> new Bone(0, 0, -24 * pose.fold + 14 * pulse, 1 - 0.18F * pose.fold);
            case "wheel_hub" -> new Bone(0, 0, 0, 1 + 0.06F * pulse);
            default -> new Bone(0, 0, 0, 1);
        };
    }

    public static int throwKind(int slot, boolean offhand) { return (slot == 0 ? THROW_MAIN : SECOND_THROW_MAIN) + (offhand ? 1 : 0); }
    public static int catchKind(int slot, boolean offhand) { return (slot == 0 ? CATCH_MAIN : SECOND_CATCH_MAIN) + (offhand ? 1 : 0); }
    public static int lostKind(int slot, boolean offhand) { return (slot == 0 ? LOST_MAIN : SECOND_LOST_MAIN) + (offhand ? 1 : 0); }
    public static boolean isThrow(int kind) { return kind == THROW_MAIN || kind == THROW_OFF || kind == SECOND_THROW_MAIN || kind == SECOND_THROW_MAIN + 1; }
    public static boolean isCatch(int kind) { return kind == CATCH_MAIN || kind == CATCH_OFF || kind == SECOND_CATCH_MAIN || kind == SECOND_CATCH_MAIN + 1; }
    public static int bladeSlot(int kind) { return kind >= SECOND_THROW_MAIN && kind < LOST_MAIN || kind >= SECOND_LOST_MAIN ? 1 : 0; }
    public static boolean action(int kind) { return kind >= SHOT_MAIN && kind <= SECOND_LOST_MAIN + 1; }
    public static boolean offhand(int kind) { return (kind & 1) != 0; }
}
