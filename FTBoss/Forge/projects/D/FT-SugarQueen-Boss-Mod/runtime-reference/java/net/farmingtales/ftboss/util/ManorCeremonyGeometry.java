/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.ManorCeremonyModel;

public final class ManorCeremonyGeometry {
    public static final int SWITCH_TICKS = 60;
    public static final int TRANSITION_DURATION = 90;
    public static final int FULL_BUDGET = 1500;
    public static final int REDUCED_BUDGET = 1100;
    private static final int[] ROSTER = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11};

    private ManorCeremonyGeometry() {
    }

    public static double smooth(double value) {
        double bounded = Math.max(0.0, Math.min(1.0, value));
        return bounded * bounded * (3.0 - 2.0 * bounded);
    }

    public static void intro(double millis, boolean reduced, ManorCeremonyModel.Sink sink) {
        if (!Double.isFinite(millis) || millis < 0.0 || millis >= 46000.0) {
            return;
        }
        float alpha = (float)(ManorCeremonyGeometry.smooth(millis / 2000.0) * (1.0 - ManorCeremonyGeometry.smooth((millis - 45000.0) / 1000.0)));
        double assembly = ManorCeremonyGeometry.smooth(millis / 8000.0);
        double orbit = ManorCeremonyGeometry.smooth((millis - 8000.0) / 16000.0);
        double rising = ManorCeremonyGeometry.smooth((millis - 24000.0) / 12000.0);
        double closing = ManorCeremonyGeometry.smooth((millis - 36000.0) / 7000.0);
        double release = ManorCeremonyGeometry.smooth((millis - 43000.0) / 3000.0);
        ManorCeremonyModel.band(5.4 * assembly + 0.3, 0.18, 0.2, reduced ? 20 : 40, 14529902, alpha, sink);
        ManorCeremonyModel.gate(release, -1, reduced, alpha * (float)ManorCeremonyGeometry.smooth((millis - 6000.0) / 10000.0), ManorCeremonyModel.pose(0.0, 0.0, 2.5, 0.0, 0.0, 0.0, 0.4 + rising * 0.6, sink));
        for (int crest = 0; crest < ROSTER.length; ++crest) {
            double angle = (double)crest * Math.PI * 2.0 / (double)ROSTER.length + millis * 1.2E-4;
            double radius = 4.5 - closing * 1.1 + release;
            float shown = alpha * (float)ManorCeremonyGeometry.smooth((millis - (double)(crest * 500)) / 3000.0);
            ManorCeremonyModel.crest(ROSTER[crest], shown, ManorCeremonyModel.pose(Math.cos(angle) * radius, 0.75 + orbit * 1.1 + rising * 1.6, Math.sin(angle) * radius, -angle + 1.5707963267948966, 0.0, 0.0, 0.75, sink));
        }
        ManorCeremonyModel.apple(alpha, ManorCeremonyModel.pose(0.0, 1.4 + orbit * 1.8 + rising * 1.7, 0.0, millis * 2.5E-4, 0.0, 0.0, 0.55 + closing * 0.6, sink));
        ManorCeremonyModel.crown(reduced, alpha * (float)rising, ManorCeremonyModel.pose(0.0, 5.6 + rising, 0.0, -millis * 1.8E-4, 0.0, 0.0, 1.0, sink));
        if (!reduced) {
            ManorCeremonyModel.band(2.2, 0.07, 0.08, 32, 6470037, alpha * 0.7f, ManorCeremonyModel.pose(0.0, 3.8 + rising, 0.0, millis * 2.0E-4, 0.55, 0.0, 1.0, sink));
        }
    }

    public static void transition(double age, int variant, boolean reduced, ManorCeremonyModel.Sink sink) {
        if (!Double.isFinite(age) || age < 0.0 || age >= 90.0) {
            return;
        }
        float alpha = (float)(ManorCeremonyGeometry.smooth((age + 1.0) / 10.0) * (1.0 - ManorCeremonyGeometry.smooth((age - 72.0) / 18.0)));
        double open = ManorCeremonyGeometry.smooth((age - 52.0) / 8.0);
        ManorCeremonyModel.gate(open, variant, reduced, alpha, ManorCeremonyModel.pose(0.0, 0.0, 1.4, 0.0, 0.0, 0.0, 0.64, sink));
        ManorCeremonyModel.band(3.0, 0.12, 0.15, reduced ? 20 : 32, 14529902, alpha, sink);
        if (age < 60.0) {
            ManorCeremonyModel.crest(variant, alpha, ManorCeremonyModel.pose(0.0, 2.1 + Math.sin(age * 0.06) * 0.15, 0.0, age * 0.035, 0.0, 0.0, 1.35, sink));
        }
        if (age >= 60.0) {
            ManorCeremonyModel.band(0.5 + ManorCeremonyGeometry.smooth((age - 60.0) / 30.0) * 4.5, 0.12, 0.13, reduced ? 20 : 32, 16771253, alpha * 0.7f, sink);
        }
    }

    public static void victory(double age, boolean reduced, ManorCeremonyModel.Sink sink) {
        if (!Double.isFinite(age) || age < 0.0 || age >= 120.0) {
            return;
        }
        float alpha = (float)(ManorCeremonyGeometry.smooth((age + 1.0) / 12.0) * (1.0 - ManorCeremonyGeometry.smooth((age - 104.0) / 16.0)));
        double rise = ManorCeremonyGeometry.smooth(age / 30.0);
        double gather = ManorCeremonyGeometry.smooth((age - 55.0) / 42.0);
        double reveal = ManorCeremonyGeometry.smooth((age - 85.0) / 20.0);
        double orbit = 4.6 - gather * 3.7;
        ManorCeremonyModel.band(5.3 * rise + 0.15, 0.15, 0.16, reduced ? 20 : 40, 14529902, alpha, sink);
        for (int crest = 0; crest < ROSTER.length; ++crest) {
            double angle = (double)crest * Math.PI * 2.0 / (double)ROSTER.length + age * 0.012;
            ManorCeremonyModel.crest(ROSTER[crest], alpha * (float)(1.0 - gather), ManorCeremonyModel.pose(Math.cos(angle) * orbit, 0.8 + rise * 1.6 - gather, Math.sin(angle) * orbit, -angle + 1.5707963267948966, 0.0, 0.0, 0.7, sink));
        }
        ManorCeremonyModel.crown(reduced, alpha, ManorCeremonyModel.pose(0.0, 2.4 + rise * 0.5, 0.0, age * 0.012, 0.0, 0.0, 0.8, sink));
        ManorCeremonyModel.apple(alpha * (float)(1.0 - reveal), ManorCeremonyModel.pose(0.0, 1.9, 0.0, -age * 0.015, 0.0, 0.0, 0.72, sink));
        for (int side : new int[]{-1, 1}) {
            for (int leaf = 0; leaf < (reduced ? 6 : 9); ++leaf) {
                double amount = (double)leaf / (reduced ? 5.0 : 8.0);
                double angle = amount * Math.PI * 0.75;
                ManorCeremonyModel.gem(0.14, 0.38, leaf % 2 == 0 ? 6470037 : 14529902, alpha * (float)rise, ManorCeremonyModel.pose((double)side * (1.1 + Math.sin(angle) * 0.9), 0.4 + amount * 3.4, 0.4, 0.0, 0.3, (double)(-side) * (0.7 - amount * 0.9), 1.0, sink));
            }
        }
        ManorCeremonyModel.chest(ManorCeremonyGeometry.smooth((age - 95.0) / 18.0), alpha * (float)reveal, ManorCeremonyModel.pose(0.0, 0.15, 0.0, 0.0, 0.0, 0.0, 1.0, sink));
        if (!reduced) {
            for (int piece = 0; piece < 12; ++piece) {
                double angle = (double)piece * Math.PI / 6.0 + age * 0.017;
                ManorCeremonyModel.gem(0.05, 0.13, 14529902, alpha * 0.6f, ManorCeremonyModel.pose(Math.cos(angle) * 3.5, 0.4 + (age * 0.02 + (double)piece * 0.27) % 3.7, Math.sin(angle) * 3.5, angle, 0.0, 0.4, 1.0, sink));
            }
        }
    }
}

