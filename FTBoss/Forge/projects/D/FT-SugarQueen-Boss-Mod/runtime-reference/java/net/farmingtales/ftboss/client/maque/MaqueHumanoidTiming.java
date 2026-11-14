/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.maque;

import net.farmingtales.ftboss.client.maque.MaqueHumanoidPose;

public final class MaqueHumanoidTiming {
    private MaqueHumanoidTiming() {
    }

    public static boolean merge(boolean bl, boolean bl2, boolean bl3, long l, long l2, long l3, int n) {
        return bl && bl2 == bl3 && l >= l3 && l - l3 < 4L && l >= l2 && l - l2 < (long)(n - 6);
    }

    public static Sample sample(String string, boolean bl, double d, int n, double d2) {
        double d3;
        if (!Double.isFinite(d) || d < 0.0 || d >= (double)n) {
            return new Sample(1.0, 0.0, false);
        }
        double d4 = d3 = bl ? Math.min(0.4, d / 12.0 * 0.4) : d2 + d / (double)n * (1.0 - d2);
        double d5 = d2 > 0.0 && !bl ? 1.0 : MaqueHumanoidPose.smooth(d / (double)(string.equals("arrival") ? 6 : (bl ? 4 : 3)));
        double d6 = MaqueHumanoidPose.smooth(((double)n - d) / 6.0);
        return new Sample(d3, d5 * d6, true);
    }

    public static double transition(double d) {
        return MaqueHumanoidPose.smooth(d / 4.0);
    }

    public record Sample(double progress, double weight, boolean active) {
    }
}

