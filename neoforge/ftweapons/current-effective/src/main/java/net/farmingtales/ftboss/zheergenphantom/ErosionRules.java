package net.farmingtales.ftboss.zheergenphantom;

public final class ErosionRules {
    private ErosionRules() {
    }

    public static float damage(int var0) {
        return (float)(4 * (Math.max(0, Math.min(4, var0)) + 1));
    }

    public static float unscaledDamage(float var0) {
        return Float.isFinite(var0) ? Math.max(4.0F, Math.min(20.0F, var0)) : 4.0F;
    }

    public static float strength(int var0) {
        return switch (Math.max(0, Math.min(5, var0))) {
            case 1 -> 0.45F;
            case 2 -> 0.62F;
            case 3 -> 0.78F;
            case 4 -> 0.91F;
            case 5 -> 1.0F;
            default -> 0.0F;
        };
    }

    public static float step(float var0, float var1) {
        float var2 = Float.isFinite(var0) ? Math.max(0.0F, Math.min(1.0F, var0)) : 0.0F;
        float var3 = Float.isFinite(var1) ? Math.max(0.0F, Math.min(1.0F, var1)) : 0.0F;
        return var2 + Math.max(-0.1F, Math.min(0.1F, var3 - var2));
    }

    private static double breathing(double var0) {
        return Double.isFinite(var0) ? 0.5 + 0.5 * Math.sin(var0 % 100.0 * Math.PI * 2.0 / 100.0) : 0.0;
    }

    public static float frameAlpha(double var0) {
        return (float)(0.985 + 0.01 * breathing(var0));
    }

    public static float sigilAlpha(double var0) {
        return (float)(0.12 + 0.04 * breathing(var0));
    }

    public static int tint(float var0) {
        return Math.round(242.0F * (Float.isFinite(var0) ? Math.max(0.0F, Math.min(1.0F, var0)) : 0.0F)) << 24 | 65795;
    }
}

