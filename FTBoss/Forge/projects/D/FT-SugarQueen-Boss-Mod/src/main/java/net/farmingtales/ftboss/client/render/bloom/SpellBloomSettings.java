package net.farmingtales.ftboss.client.render.bloom;

public record SpellBloomSettings(boolean enabled, float strength, float radius, float threshold, float resolutionScale,
                                int maxPasses, boolean allowShaderPacks) {
    public static SpellBloomSettings defaults() {
        return new SpellBloomSettings(true, 0.50F, 0.40F, 0.75F, 0.50F, 6, false);
    }

    public SpellBloomSettings sanitized() {
        return new SpellBloomSettings(enabled, bounded(strength, 0, 1.5F, 0.50F), bounded(radius, 0, 1, 0.40F),
                bounded(threshold, 0, 1, 0.75F), bounded(resolutionScale, 0.25F, 0.50F, 0.50F),
                Math.max(1, Math.min(8, maxPasses)), allowShaderPacks);
    }

    private static float bounded(float value, float minimum, float maximum, float fallback) {
        return Float.isFinite(value) ? Math.max(minimum, Math.min(maximum, value)) : fallback;
    }
}
