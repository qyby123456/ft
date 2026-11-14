/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render.bloom;

public record SpellBloomSettings(boolean enabled, float strength, float radius, float threshold, float resolutionScale, int maxPasses, boolean allowShaderPacks) {
    public static SpellBloomSettings defaults() {
        return new SpellBloomSettings(true, 0.5f, 0.4f, 0.75f, 0.5f, 6, false);
    }

    public SpellBloomSettings sanitized() {
        return new SpellBloomSettings(this.enabled, SpellBloomSettings.bounded(this.strength, 0.0f, 1.5f, 0.5f), SpellBloomSettings.bounded(this.radius, 0.0f, 1.0f, 0.4f), SpellBloomSettings.bounded(this.threshold, 0.0f, 1.0f, 0.75f), SpellBloomSettings.bounded(this.resolutionScale, 0.25f, 0.5f, 0.5f), Math.max(1, Math.min(8, this.maxPasses)), this.allowShaderPacks);
    }

    private static float bounded(float f, float f2, float f3, float f4) {
        return Float.isFinite(f) ? Math.max(f2, Math.min(f3, f)) : f4;
    }
}

