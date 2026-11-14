package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.ManorCeremonyModel.Sink;
import static net.farmingtales.ftboss.util.ManorCeremonyModel.*;

public final class ManorCeremonyGeometry {
    public static final int SWITCH_TICKS = 60;
    public static final int TRANSITION_DURATION = 90;
    public static final int FULL_BUDGET = 1500;
    public static final int REDUCED_BUDGET = 1100;
    private static final int[] ROSTER = {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11};
    private ManorCeremonyGeometry() {}

    public static double smooth(double value) { double bounded = Math.max(0, Math.min(1, value)); return bounded * bounded * (3 - 2 * bounded); }

    public static void intro(double millis, boolean reduced, Sink sink) {
        if (!Double.isFinite(millis) || millis < 0 || millis >= ManorIntroTimeline.DURATION_MILLIS) return;
        float alpha = (float) (smooth(millis / 2000) * (1 - smooth((millis - 45000) / 1000)));
        double assembly = smooth(millis / 8000), orbit = smooth((millis - 8000) / 16000);
        double rising = smooth((millis - 24000) / 12000), closing = smooth((millis - 36000) / 7000);
        double release = smooth((millis - 43000) / 3000);
        band(5.4 * assembly + 0.3, 0.18, 0.2, reduced ? 20 : 40, GOLD, alpha, sink);
        gate(release, -1, reduced, alpha * (float) smooth((millis - 6000) / 10000),
                pose(0, 0, 2.5, 0, 0, 0, 0.4 + rising * 0.6, sink));
        for (int crest = 0; crest < ROSTER.length; crest++) {
            double angle = crest * Math.PI * 2 / ROSTER.length + millis * 0.00012;
            double radius = 4.5 - closing * 1.1 + release;
            float shown = alpha * (float) smooth((millis - crest * 500) / 3000);
            crest(ROSTER[crest], shown, pose(Math.cos(angle) * radius, 0.75 + orbit * 1.1 + rising * 1.6,
                    Math.sin(angle) * radius, -angle + Math.PI / 2, 0, 0, 0.75, sink));
        }
        apple(alpha, pose(0, 1.4 + orbit * 1.8 + rising * 1.7, 0, millis * 0.00025, 0, 0, 0.55 + closing * 0.6, sink));
        crown(reduced, alpha * (float) rising, pose(0, 5.6 + rising, 0, -millis * 0.00018, 0, 0, 1, sink));
        if (!reduced) band(2.2, 0.07, 0.08, 32, LEAF, alpha * 0.7F,
                pose(0, 3.8 + rising, 0, millis * 0.0002, 0.55, 0, 1, sink));
    }

    public static void transition(double age, int variant, boolean reduced, Sink sink) {
        if (!Double.isFinite(age) || age < 0 || age >= TRANSITION_DURATION) return;
        float alpha = (float) (smooth((age + 1) / 10) * (1 - smooth((age - 72) / 18)));
        double open = smooth((age - 52) / 8);
        gate(open, variant, reduced, alpha, pose(0, 0, 1.4, 0, 0, 0, 0.64, sink));
        band(3.0, 0.12, 0.15, reduced ? 20 : 32, GOLD, alpha, sink);
        if (age < SWITCH_TICKS) crest(variant, alpha, pose(0, 2.1 + Math.sin(age * 0.06) * 0.15,
                0, age * 0.035, 0, 0, 1.35, sink));
        if (age >= SWITCH_TICKS) band(0.5 + smooth((age - SWITCH_TICKS) / 30) * 4.5, 0.12, 0.13,
                reduced ? 20 : 32, IVORY, alpha * 0.7F, sink);
    }

    public static void victory(double age, boolean reduced, Sink sink) {
        if (!Double.isFinite(age) || age < 0 || age >= ManorVictoryGeometry.DURATION) return;
        float alpha = (float) (smooth((age + 1) / 12) * (1 - smooth((age - 104) / 16)));
        double rise = smooth(age / 30), gather = smooth((age - 55) / 42), reveal = smooth((age - 85) / 20);
        double orbit = 4.6 - gather * 3.7;
        band(5.3 * rise + 0.15, 0.15, 0.16, reduced ? 20 : 40, GOLD, alpha, sink);
        for (int crest = 0; crest < ROSTER.length; crest++) {
            double angle = crest * Math.PI * 2 / ROSTER.length + age * 0.012;
            crest(ROSTER[crest], alpha * (float) (1 - gather), pose(Math.cos(angle) * orbit,
                    0.8 + rise * 1.6 - gather, Math.sin(angle) * orbit, -angle + Math.PI / 2, 0, 0, 0.7, sink));
        }
        crown(reduced, alpha, pose(0, 2.4 + rise * 0.5, 0, age * 0.012, 0, 0, 0.8, sink));
        apple(alpha * (float) (1 - reveal), pose(0, 1.9, 0, -age * 0.015, 0, 0, 0.72, sink));
        for (int side : new int[]{-1, 1}) for (int leaf = 0; leaf < (reduced ? 6 : 9); leaf++) {
            double amount = leaf / (reduced ? 5.0 : 8.0);
            double angle = amount * Math.PI * 0.75;
            gem(0.14, 0.38, leaf % 2 == 0 ? LEAF : GOLD, alpha * (float) rise,
                    pose(side * (1.1 + Math.sin(angle) * 0.9), 0.4 + amount * 3.4, 0.4,
                            0, 0.3, -side * (0.7 - amount * 0.9), 1, sink));
        }
        chest(smooth((age - 95) / 18), alpha * (float) reveal, pose(0, 0.15, 0, 0, 0, 0, 1, sink));
        if (!reduced) for (int piece = 0; piece < 12; piece++) {
            double angle = piece * Math.PI / 6 + age * 0.017;
            gem(0.05, 0.13, GOLD, alpha * 0.6F, pose(Math.cos(angle) * 3.5, 0.4 + ((age * 0.02 + piece * 0.27) % 3.7),
                    Math.sin(angle) * 3.5, angle, 0, 0.4, 1, sink));
        }
    }
}
