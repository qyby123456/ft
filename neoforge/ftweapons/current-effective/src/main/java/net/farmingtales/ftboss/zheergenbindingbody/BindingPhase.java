package net.farmingtales.ftboss.zheergenbindingbody;

public record BindingPhase(boolean active, String track, double progress, double weight) {
    public static final String KEY = "zheergenBindingBody";

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, var0));
    }

    private static double ease(double var0) {
        double var2 = clamp(var0);
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static BindingPhase sample(String var0, double var1, int var3, double var4) {
        if (Double.isFinite(var1) && !(var1 < 0.0) && var3 >= 1 && var3 <= 200 && (!var0.equals("cancel") || Double.isFinite(var4))) {
            return switch (var0) {
                case "charge" -> new BindingPhase(var1 <= (double)(var3 + 3), "charge", clamp(var1 / (double)var3), 1.0);
                case "release" -> new BindingPhase(var1 < (double)var3, "release", clamp(var1 / (double)var3), 1.0 - ease((var1 / (double)var3 - 0.72) / 0.28));
                case "cancel" -> new BindingPhase(var1 < (double)var3, "charge", clamp(var4), 1.0 - ease(var1 / (double)var3));
                default -> new BindingPhase(false, "charge", 0.0, 0.0);
            };
        } else {
            return new BindingPhase(false, "charge", 0.0, 0.0);
        }
    }
}
