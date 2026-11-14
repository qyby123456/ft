/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 */
package net.farmingtales.ftboss.client.xuanlannative;

import net.farmingtales.ftboss.client.xuanlannative.NativeForms;
import org.joml.Matrix4f;

public final class NativeMotion {
    private NativeMotion() {
    }

    public static float ease(double d) {
        double d2 = Double.isFinite(d) ? Math.max(0.0, Math.min(1.0, d)) : 0.0;
        return (float)(d2 * d2 * (3.0 - 2.0 * d2));
    }

    public static Pose pose(NativeForms.Joint joint, double d, double d2) {
        double d3 = Math.max(0.0, Math.min(1.0, d / Math.max(1.0, d2)));
        float f = 0.0f;
        float f2 = 1.0f;
        float f3 = 0.0f;
        switch (joint.rhythm()) {
            case "focus": {
                f = (float)(0.09 * Math.sin(d * 0.22));
                f2 = 0.88f + 0.12f * NativeMotion.ease(d / 6.0);
                break;
            }
            case "compress": {
                f = (float)((double)(joint.slot() % 2 == 0 ? 1 : -1) * d * 0.028);
                f2 = 1.0f - 0.35f * NativeMotion.ease(d3);
                break;
            }
            case "orbit": {
                f = (float)(d * 25.0 * Math.PI / 180.0);
                break;
            }
            case "wave": {
                f = (float)(0.08 * Math.sin(d * 0.2 + (double)joint.slot() * 0.71));
                f2 = 0.9f + 0.1f * NativeMotion.ease(d3);
                f3 = (float)(0.04 * Math.sin(d * 0.18 + (double)joint.slot()));
                break;
            }
            case "countdown": {
                f2 = 1.0f - 0.94f * NativeMotion.ease(d3);
                break;
            }
            case "fracture": {
                f = (float)(joint.slot() % 2 == 0 ? 1 : -1) * 0.37f * NativeMotion.ease(d3);
                f2 = 1.0f + 0.65f * NativeMotion.ease(d3);
                f3 = 0.02f * NativeMotion.ease(d3);
                break;
            }
            case "rise": {
                f = (float)(d * 0.043);
                f3 = 24.0f * NativeMotion.ease(d3);
                break;
            }
            case "return": {
                f = (float)(-d * 0.043);
                f3 = 24.0f * NativeMotion.ease(1.0 - d3);
                break;
            }
            case "vent": {
                f = (float)((double)(joint.slot() % 2 == 0 ? 1 : -1) * d * 0.013);
                f2 = 1.0f + 0.1f * NativeMotion.ease(d3);
            }
        }
        return new Pose(f, f2, f3);
    }

    public static Matrix4f joint(NativeForms.Joint joint, double d, double d2) {
        Pose pose = NativeMotion.pose(joint, d, d2);
        float[] fArray = joint.pivot();
        float[] fArray2 = joint.axis();
        return new Matrix4f().translation(fArray[0], fArray[1] + pose.lift(), fArray[2]).rotate(pose.angle(), fArray2[0], fArray2[1], fArray2[2]).scale(pose.size()).translate(-fArray[0], -fArray[1], -fArray[2]);
    }

    public static float fade(double d, double d2) {
        return NativeMotion.ease((d2 - d) / Math.min(4.0, d2));
    }

    public static float near(double d) {
        return NativeMotion.ease((d - 0.18) / 0.55);
    }

    public static double waveRadius(double d, int n, int n2) {
        return (d - 1.0) * (double)(n + 1) / (double)n2 + 1.0;
    }

    public record Pose(float angle, float size, float lift) {
    }
}

