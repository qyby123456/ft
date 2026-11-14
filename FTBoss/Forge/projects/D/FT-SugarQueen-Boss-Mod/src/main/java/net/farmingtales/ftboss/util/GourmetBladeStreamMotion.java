package net.farmingtales.ftboss.util;

/** 万味胚刃流光的持刀侧推进及视角方向规则；8 tick 后完全消失。 */
public final class GourmetBladeStreamMotion {
    public static final int LIFE_TICKS = 8;
    public static final float FIRST_PERSON_ROLL_DEGREES = 32;
    public static final float WORLD_LOCAL_YAW_DEGREES = 180;

    private GourmetBladeStreamMotion() {}

    public static float opacity(float age, float along) {
        if (!Float.isFinite(age) || !Float.isFinite(along) || age <= 0 || age >= LIFE_TICKS) return 0;
        if (along < 0 || along > 1) return 0;
        float travel = 1 - along;
        float head = clamp((age - 0.15F) / 3.25F);
        float tail = clamp((age - 2.0F) / 6.0F);
        float attack = clamp(age / 0.75F);
        float release = clamp((LIFE_TICKS - age) / 2.5F);
        return attack * release * clamp((head - travel) / 0.12F) * clamp((travel - tail) / 0.22F);
    }

    private static float clamp(float value) {
        return Math.max(0, Math.min(1, value));
    }
}
