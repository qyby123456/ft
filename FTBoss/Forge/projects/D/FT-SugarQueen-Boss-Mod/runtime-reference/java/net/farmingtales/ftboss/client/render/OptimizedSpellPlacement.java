/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.render.OptimizedSpellModelLibrary;
import net.farmingtales.ftboss.util.SpellVisualRules;

public final class OptimizedSpellPlacement {
    private OptimizedSpellPlacement() {
    }

    public static boolean casterSpace(OptimizedSpellModelLibrary.Scene scene) {
        return switch (scene.anchor()) {
            case "FRONT", "SIDE", "BACK", "AROUND" -> true;
            default -> false;
        };
    }

    public static double scale(OptimizedSpellModelLibrary.Model model, double d, double d2) {
        double d3 = Double.isFinite(d) && d > 0.0 ? Math.max(0.8, Math.min(1.8, Math.sqrt(d))) : 1.0;
        double d4 = 0.86 + 0.14 * Math.min(1.0, Math.max(0.0, Double.isFinite(d2) ? d2 : 0.0) / 4.0);
        return model.scene().span() / model.scene().nativeSpan() * d3 * d4;
    }

    public static double[] offset(OptimizedSpellModelLibrary.Scene scene, double[] dArray, double[] dArray2, double d, double d2) {
        d = Double.isFinite(d) ? Math.max(0.6, Math.min(12.0, d)) : 0.6;
        d2 = Double.isFinite(d2) ? Math.max(1.8, Math.min(16.0, d2)) : 1.8;
        double[] dArray3 = new double[]{0.0, Math.max(0.3 - dArray[1], scene.height() - (dArray[1] + dArray2[1]) / 2.0), 0.0};
        switch (scene.anchor()) {
            case "FRONT": {
                dArray3[2] = d / 2.0 + 0.4 - dArray[2];
                break;
            }
            case "SIDE": {
                dArray3[0] = d / 2.0 + 0.4 - dArray[0];
                break;
            }
            case "BACK": {
                dArray3[2] = -d / 2.0 - 0.45 - dArray2[2];
                break;
            }
            case "ABOVE": {
                dArray3[1] = Math.max(d2 + 0.65 - dArray[1], dArray3[1]);
                break;
            }
            case "TARGET": 
            case "AROUND": {
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown spell anchor");
            }
        }
        return dArray3;
    }

    public static float actorOpacity(double[][] dArray, double[] dArray2) {
        double d = 0.0;
        for (int i = 0; i < 3; ++i) {
            double d2 = Double.POSITIVE_INFINITY;
            double d3 = Double.NEGATIVE_INFINITY;
            for (double[] dArray3 : dArray) {
                d2 = Math.min(d2, dArray3[i]);
                d3 = Math.max(d3, dArray3[i]);
            }
            double d4 = Math.max(0.0, Math.max(dArray2[i] - d3, d2 - dArray2[i + 3]));
            d += d4 * d4;
        }
        double d5 = Math.max(0.0, Math.min(1.0, Math.sqrt(d) / 0.22));
        return (float)(d5 * d5 * (3.0 - 2.0 * d5));
    }

    public static String cue(SpellVisualRules.Spell spell) {
        OptimizedSpellModelLibrary.Model model = OptimizedSpellModelLibrary.find(spell.id);
        return model == null ? "BLAST" : model.scene().cue();
    }
}

