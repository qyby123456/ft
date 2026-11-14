/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class EasterCombatMath {
    private EasterCombatMath() {
    }

    public static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    public static float smooth(float value) {
        float progress = EasterCombatMath.clamp(value);
        return progress * progress * (3.0f - 2.0f * progress);
    }

    public static float progress(float age, int duration, boolean impact) {
        float progress = EasterCombatMath.clamp(age / (float)Math.max(1, duration));
        return impact ? 0.45f + progress * 0.55f : progress;
    }

    public static float weight(float age, int duration, boolean impact) {
        if (age < 0.0f || age >= (float)duration || duration <= 0) {
            return 0.0f;
        }
        float fadeIn = impact ? 1.0f : EasterCombatMath.smooth((age + 1.0f) / 4.0f);
        return fadeIn * EasterCombatMath.smooth(((float)duration - age) / Math.min(6.0f, (float)duration * 0.4f));
    }

    public static float castWeight(float age, int duration) {
        if (age < 0.0f || age >= (float)duration || duration <= 0) {
            return 0.0f;
        }
        float fadeIn = EasterCombatMath.smooth((age + 1.0f) / Math.min(2.0f, (float)duration));
        return fadeIn * EasterCombatMath.smooth(((float)duration - age) / Math.min(3.0f, (float)duration * 0.25f));
    }
}

