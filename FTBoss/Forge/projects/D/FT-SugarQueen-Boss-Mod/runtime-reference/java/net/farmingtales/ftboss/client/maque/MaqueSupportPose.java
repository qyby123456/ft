/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.maque;

import net.farmingtales.ftboss.client.maque.MaqueArcaneAssets;
import net.farmingtales.ftboss.client.maque.MaqueArcanePose;

public final class MaqueSupportPose {
    private MaqueSupportPose() {
    }

    public static Frame frame(String string, int n, double d, double d2) {
        double d3 = Math.max(0.0, Math.min(1.0, d / d2));
        double d4 = MaqueArcanePose.smooth(d / Math.min(22.0, d2 * 0.48));
        return switch (string) {
            case "link" -> new Frame(1.0, 0.0, 0.0);
            case "arrival" -> new Frame(0.64 + 0.36 * d4, -0.28 * (1.0 - d4), 0.9 * (1.0 - d4));
            case "departure" -> new Frame(1.0 - 0.3 * MaqueArcanePose.smooth(d3), 0.18 * MaqueArcanePose.smooth(d3), 1.25 * MaqueArcanePose.smooth(d3));
            case "cleanse" -> new Frame(0.68 + 0.65 * MaqueArcanePose.smooth(d3), 0.12 * MaqueArcanePose.smooth(d3), 0.18 * MaqueArcanePose.smooth(d3));
            case "wave" -> new Frame(0.06 + 0.94 * MaqueArcanePose.smooth(d3), 0.0, 0.0);
            case "wind" -> new Frame(1.0, 0.025 * Math.sin(d * Math.PI / 80.0), 0.055 * Math.sin(d * Math.PI / 80.0));
            case "held" -> new Frame(1.0, d * Math.PI * 2.0 / d2, 0.012 * Math.sin(d * Math.PI * 2.0 / d2));
            default -> new Frame(0.86 + 0.14 * d4, 0.035 * Math.sin(d3 * Math.PI), 0.11 * MaqueArcanePose.smooth(d3) + 0.02 * Math.sin(d3 * Math.PI));
        };
    }

    public static double visibility(MaqueArcaneAssets.Bone bone, double d, double d2) {
        if (!Double.isFinite(d) || d < 0.0 || d >= d2) {
            return 0.0;
        }
        if (bone.role().kind().equals("held")) {
            return 1.0;
        }
        double d3 = bone.role().kind().equals("empower") ? (double)(bone.role().lane() * 2) : (bone.role().kind().equals("arrival") ? (double)bone.role().lane() * 1.6 : 0.0);
        return MaqueArcanePose.smooth((d - d3) / 6.0) * MaqueArcanePose.smooth((d2 - d) / Math.min(14.0, d2 * 0.4));
    }

    public static double linkGain(MaqueArcaneAssets.Bone bone, double d, double d2, double[] dArray, boolean bl) {
        if (!bone.role().kind().equals("link")) {
            return 1.0;
        }
        double d3 = MaqueArcanePose.smooth(d / d2);
        double d4 = bl ? d3 : 1.0 - d3;
        double d5 = dArray[0] / 3.6 - d4;
        return 0.24 + 0.76 * Math.exp(-d5 * d5 / 0.022);
    }

    public static void transform(MaqueArcaneAssets.Bone bone, double d, double[] dArray, double d2, double[] dArray2, double[] dArray3) {
        MaqueSupportPose.transform(bone, dArray, MaqueSupportPose.frame(bone.role().kind(), bone.role().lane(), d, d2), dArray2, dArray3);
    }

    public static void transform(MaqueArcaneAssets.Bone bone, double[] dArray, Frame frame, double[] dArray2, double[] dArray3) {
        if (bone.role().kind().equals("link")) {
            double d = Math.hypot(dArray2[0], dArray2[2]);
            if (Math.sqrt(dArray2[0] * dArray2[0] + dArray2[1] * dArray2[1] + dArray2[2] * dArray2[2]) < 0.05) {
                System.arraycopy(dArray, 0, dArray3, 0, 3);
                return;
            }
            double d2 = d < 0.001 ? 1.0 : dArray2[0] / d;
            double d3 = d < 0.001 ? 0.0 : dArray2[2] / d;
            dArray3[0] = dArray[0] / 3.6 * dArray2[0] - dArray[2] * d3;
            dArray3[1] = dArray[0] / 3.6 * dArray2[1] + dArray[1];
            dArray3[2] = dArray[0] / 3.6 * dArray2[2] + dArray[2] * d2;
            return;
        }
        double d = (dArray[0] - bone.pivot()[0]) * frame.scale;
        double d4 = (dArray[2] - bone.pivot()[2]) * frame.scale;
        dArray3[0] = bone.pivot()[0] + d * Math.cos(frame.turn) + d4 * Math.sin(frame.turn);
        dArray3[1] = bone.pivot()[1] + (dArray[1] - bone.pivot()[1]) * frame.scale + frame.height;
        dArray3[2] = bone.pivot()[2] - d * Math.sin(frame.turn) + d4 * Math.cos(frame.turn);
    }

    public record Frame(double scale, double turn, double height) {
    }
}

