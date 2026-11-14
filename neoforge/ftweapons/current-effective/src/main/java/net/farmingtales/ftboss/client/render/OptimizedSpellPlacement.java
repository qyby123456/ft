package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.util.SpellVisualRules.Spell;

public final class OptimizedSpellPlacement {
    private OptimizedSpellPlacement() {
    }

    public static boolean casterSpace(OptimizedSpellModelLibrary.Scene var0) {
        String var1 = var0.anchor();

        return switch (var1) {
            case "FRONT", "SIDE", "BACK", "AROUND" -> true;
            default -> false;
        };
    }

    public static double scale(OptimizedSpellModelLibrary.Model var0, double var1, double var3) {
        double var5 = Double.isFinite(var1) && var1 > 0.0 ? Math.max(0.8, Math.min(1.8, Math.sqrt(var1))) : 1.0;
        double var7 = 0.86 + 0.14 * Math.min(1.0, Math.max(0.0, Double.isFinite(var3) ? var3 : 0.0) / 4.0);
        return var0.scene().span() / var0.scene().nativeSpan() * var5 * var7;
    }

    public static double[] offset(OptimizedSpellModelLibrary.Scene var0, double[] var1, double[] var2, double var3, double var5) {
        var3 = Double.isFinite(var3) ? Math.max(0.6, Math.min(12.0, var3)) : 0.6;
        var5 = Double.isFinite(var5) ? Math.max(1.8, Math.min(16.0, var5)) : 1.8;
        double[] var7 = new double[]{0.0, Math.max(0.3 - var1[1], var0.height() - (var1[1] + var2[1]) / 2.0), 0.0};
        String var8 = var0.anchor();
        switch (var8) {
            case "FRONT":
                var7[2] = var3 / 2.0 + 0.4 - var1[2];
                break;
            case "SIDE":
                var7[0] = var3 / 2.0 + 0.4 - var1[0];
                break;
            case "BACK":
                var7[2] = -var3 / 2.0 - 0.45 - var2[2];
                break;
            case "ABOVE":
                var7[1] = Math.max(var5 + 0.65 - var1[1], var7[1]);
            case "TARGET":
            case "AROUND":
                break;
            default:
                throw new IllegalArgumentException("Unknown spell anchor");
        }

        return var7;
    }

    public static float actorOpacity(double[][] var0, double[] var1) {
        double var2 = 0.0;

        for (int var4 = 0; var4 < 3; var4++) {
            double var5 = Double.POSITIVE_INFINITY;
            double var7 = Double.NEGATIVE_INFINITY;

            for (double[] var12 : var0) {
                var5 = Math.min(var5, var12[var4]);
                var7 = Math.max(var7, var12[var4]);
            }

            double var14 = Math.max(0.0, Math.max(var1[var4] - var7, var5 - var1[var4 + 3]));
            var2 += var14 * var14;
        }

        double var13 = Math.max(0.0, Math.min(1.0, Math.sqrt(var2) / 0.22));
        return (float)(var13 * var13 * (3.0 - 2.0 * var13));
    }

    public static String cue(Spell var0) {
        OptimizedSpellModelLibrary.Model var1 = OptimizedSpellModelLibrary.find(var0.id);
        return var1 == null ? "BLAST" : var1.scene().cue();
    }
}
