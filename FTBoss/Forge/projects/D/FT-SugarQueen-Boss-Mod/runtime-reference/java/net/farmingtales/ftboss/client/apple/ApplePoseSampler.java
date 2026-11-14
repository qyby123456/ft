/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.apple;

import java.util.Map;
import net.farmingtales.ftboss.client.apple.AppleKnightEffects;

public final class ApplePoseSampler {
    private ApplePoseSampler() {
    }

    public static AppleKnightEffects.Pose sample(Map<String, AppleKnightEffects.Pose>[] mapArray, String string, double d) {
        AppleKnightEffects.Pose pose = ApplePoseSampler.reusablePose();
        double d2 = Math.max(0.0, Math.min(1.0, d)) * (double)(mapArray.length - 1);
        int n = Math.min(mapArray.length - 2, (int)d2);
        ApplePoseSampler.interpolate(mapArray[n].get(string), mapArray[n + 1].get(string), d2 - (double)n, pose);
        return pose;
    }

    public static AppleKnightEffects.Pose reusablePose() {
        AppleKnightEffects.Pose pose = new AppleKnightEffects.Pose();
        pose.position = new double[3];
        pose.rotation = new double[3];
        pose.scale = new double[3];
        return pose;
    }

    public static void sampleInto(AppleKnightEffects.Pose[] poseArray, double d, AppleKnightEffects.Pose pose) {
        if (!Double.isFinite(d)) {
            throw new IllegalArgumentException("Non-finite pose progress");
        }
        double d2 = Math.max(0.0, Math.min(1.0, d)) * (double)(poseArray.length - 1);
        int n = Math.min(poseArray.length - 2, (int)d2);
        ApplePoseSampler.interpolate(poseArray[n], poseArray[n + 1], d2 - (double)n, pose);
    }

    public static double segmentDistanceSquared(double[] dArray, double[] dArray2, double[] dArray3) {
        return ApplePoseSampler.segmentDistanceSquared(dArray[0], dArray[1], dArray[2], dArray2[0], dArray2[1], dArray2[2], dArray3[0], dArray3[1], dArray3[2]);
    }

    public static double segmentDistanceSquared(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9) {
        double d10 = d7 - d4;
        double d11 = d8 - d5;
        double d12 = d9 - d6;
        double d13 = d - d4;
        double d14 = d2 - d5;
        double d15 = d3 - d6;
        double d16 = d10 * d10 + d11 * d11 + d12 * d12;
        double d17 = d13 * d10 + d14 * d11 + d15 * d12;
        double d18 = d16 < 1.0E-4 ? 0.0 : Math.max(0.0, Math.min(1.0, d17 / d16));
        return (d13 -= d10 * d18) * d13 + (d14 -= d11 * d18) * d14 + (d15 -= d12 * d18) * d15;
    }

    private static void interpolate(AppleKnightEffects.Pose pose, AppleKnightEffects.Pose pose2, double d, AppleKnightEffects.Pose pose3) {
        ApplePoseSampler.mix(pose.position, pose2.position, d, pose3.position);
        ApplePoseSampler.mix(pose.rotation, pose2.rotation, d, pose3.rotation);
        ApplePoseSampler.mix(pose.scale, pose2.scale, d, pose3.scale);
        pose3.alpha = pose.alpha + (pose2.alpha - pose.alpha) * d;
    }

    private static void mix(double[] dArray, double[] dArray2, double d, double[] dArray3) {
        for (int i = 0; i < 3; ++i) {
            dArray3[i] = dArray[i] + (dArray2[i] - dArray[i]) * d;
        }
    }
}

