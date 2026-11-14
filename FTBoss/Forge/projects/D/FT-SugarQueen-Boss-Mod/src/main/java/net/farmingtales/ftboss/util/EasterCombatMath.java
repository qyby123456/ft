package net.farmingtales.ftboss.util;

public final class EasterCombatMath {
    private EasterCombatMath() {}

    public static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    public static float smooth(float value) {
        float progress = clamp(value);
        return progress * progress * (3.0f - 2.0f * progress);
    }

    public static float progress(float age, int duration, boolean impact) {
        float progress = clamp(age / Math.max(1, duration));
        return impact ? 0.45f + progress * 0.55f : progress;
    }

    public static float weight(float age, int duration, boolean impact) {
        if (age < 0.0f || age >= duration || duration <= 0) return 0.0f;
        float fadeIn = impact ? 1.0f : smooth((age + 1.0f) / 4.0f);
        return fadeIn * smooth((duration - age) / Math.min(6.0f, duration * 0.4f));
    }

    public static float castWeight(float age, int duration) {
        if (age < 0.0F || age >= duration || duration <= 0) return 0.0F;
        float fadeIn = smooth((age + 1.0F) / Math.min(HiddenBossCastRules.POSE_BLEND_TICKS, duration));
        return fadeIn * smooth((duration - age) / Math.min(3.0F, duration * 0.25F));
    }
}
