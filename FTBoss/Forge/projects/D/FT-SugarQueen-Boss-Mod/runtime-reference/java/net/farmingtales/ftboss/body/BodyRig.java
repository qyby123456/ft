/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.body;

import java.util.Set;
import net.farmingtales.ftboss.body.BodyMotion;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class BodyRig {
    private static final Set<String> AIMED = Set.of("beam", "combo", "rail", "contact");

    private BodyRig() {
    }

    private static Quaternionf rotation(double[] dArray) {
        double d = Math.toRadians(dArray[0]) * 0.5;
        double d2 = Math.toRadians(dArray[1]) * 0.5;
        double d3 = Math.toRadians(dArray[2]) * 0.5;
        double d4 = Math.cos(d);
        double d5 = Math.sin(d);
        double d6 = Math.cos(d2);
        double d7 = Math.sin(d2);
        double d8 = Math.cos(d3);
        double d9 = Math.sin(d3);
        return new Quaternionf((float)(d5 * d6 * d8 - d4 * d7 * d9), (float)(d4 * d7 * d8 + d5 * d6 * d9), (float)(d4 * d6 * d9 - d5 * d7 * d8), (float)(d4 * d6 * d8 + d5 * d7 * d9));
    }

    public static float footHeight(Quaternionf quaternionf) {
        float f = -3.4028235E38f;
        for (float f2 : new float[]{-2.0f, 2.0f}) {
            for (float f3 : new float[]{-2.0f, 2.0f}) {
                f = Math.max(f, quaternionf.transform((Vector3f)new Vector3f((float)f2, (float)12.0f, (float)f3)).y);
            }
        }
        return f;
    }

    public static Bone[] solve(BodyMotion.Pose pose, float f, float f2, float f3) {
        double[][] dArrayArray = new double[6][];
        for (int i = 0; i < 6; ++i) {
            dArrayArray[i] = (double[])pose.angles()[i].clone();
        }
        float f4 = Math.min(1.0f, Math.max(0.0f, f2)) * (float)(1.0 - pose.activity() * 0.85);
        double[] dArray = dArrayArray[4];
        dArray[0] = dArray[0] + Math.toDegrees(Math.cos((double)f * 0.6662) * (double)f4 * 0.8);
        double[] dArray2 = dArrayArray[5];
        dArray2[0] = dArray2[0] - Math.toDegrees(Math.cos((double)f * 0.6662) * (double)f4 * 0.8);
        double[] dArray3 = dArrayArray[2];
        dArray3[0] = dArray3[0] - Math.toDegrees(Math.cos((double)f * 0.6662 + Math.PI) * (double)f4 * 0.08);
        double[] dArray4 = dArrayArray[3];
        dArray4[0] = dArray4[0] - Math.toDegrees(Math.cos((double)f * 0.6662) * (double)f4 * 0.12);
        dArrayArray[1][1] = dArrayArray[1][1] * (1.0 - pose.aim()) + pose.yaw() * pose.aim();
        double[] dArray5 = dArrayArray[4];
        dArray5[1] = dArray5[1] + pose.yaw() * pose.aim() * 0.85;
        double[] dArray6 = dArrayArray[5];
        dArray6[1] = dArray6[1] + pose.yaw() * pose.aim() * 0.85;
        Quaternionf quaternionf = BodyRig.rotation(dArrayArray[1]);
        Quaternionf quaternionf2 = BodyRig.rotation(dArrayArray[4]);
        Quaternionf quaternionf3 = BodyRig.rotation(dArrayArray[5]);
        float f5 = pose.air() || (double)f2 > 0.1 ? 0.0f : 1.0f;
        float f6 = 12.0f + (12.0f - BodyRig.footHeight(quaternionf2)) * f5;
        float f7 = 12.0f + (12.0f - BodyRig.footHeight(quaternionf3)) * f5;
        float f8 = (f6 + f7) * 0.5f - 12.0f;
        Vector3f vector3f = new Vector3f(0.0f, 12.0f + f8, (float)pose.shift());
        Vector3f vector3f2 = quaternionf.transform(new Vector3f(0.0f, -12.0f, 0.0f)).add((Vector3fc)vector3f);
        Bone[] boneArray = new Bone[6];
        boneArray[1] = new Bone(vector3f2, quaternionf);
        double d = pose.yaw() - dArrayArray[1][1];
        Quaternionf quaternionf4 = BodyRig.rotation(new double[]{Math.max(-65.0, Math.min(65.0, pose.pitch())) + dArrayArray[0][0] + dArrayArray[1][0] * 0.15, dArrayArray[1][1] + Math.max(-70.0, Math.min(70.0, d)) + dArrayArray[0][1], dArrayArray[0][2] + dArrayArray[1][2] * 0.15});
        boneArray[0] = new Bone(new Vector3f((Vector3fc)vector3f2), quaternionf4);
        for (int i = 2; i <= 3; ++i) {
            float f9 = i == 2 ? -1.0f : 1.0f;
            Vector3f vector3f3 = quaternionf.transform(new Vector3f(f9 * 5.0f, 2.5f, 0.0f)).add((Vector3fc)vector3f2);
            Quaternionf quaternionf5 = new Quaternionf((Quaternionfc)quaternionf).mul((Quaternionfc)BodyRig.rotation(dArrayArray[i]));
            if (i == (pose.mirror() ? 3 : 2) && AIMED.contains(pose.id())) {
                quaternionf5 = BodyRig.rotation(new double[]{dArrayArray[i][0] + pose.pitch() * pose.activity() + dArrayArray[1][0] * (1.0 - pose.aim()), dArrayArray[i][1] + dArrayArray[1][1], dArrayArray[i][2] + dArrayArray[1][2] * (1.0 - pose.aim())});
            }
            boneArray[i] = new Bone(vector3f3, quaternionf5);
        }
        boneArray[4] = new Bone(new Vector3f((float)(-1.9 - pose.spread()), f6, (float)pose.shift()), quaternionf2);
        boneArray[5] = new Bone(new Vector3f((float)(1.9 + pose.spread()), f7, (float)pose.shift()), quaternionf3);
        return boneArray;
    }

    public static Bone[] blend(Bone[] boneArray, Bone[] boneArray2, double d, boolean bl) {
        Bone[] boneArray3 = new Bone[6];
        for (int i = 0; i < 6; ++i) {
            boneArray3[i] = new Bone(new Vector3f((Vector3fc)boneArray[i].position()).lerp((Vector3fc)boneArray2[i].position(), (float)d), new Quaternionf((Quaternionfc)boneArray[i].rotation()).slerp((Quaternionfc)boneArray2[i].rotation(), (float)d));
        }
        if (bl) {
            float f = (boneArray3[4].position().y + boneArray3[5].position().y) * 0.5f;
            for (int i = 4; i < 6; ++i) {
                boneArray3[i].position().y = 24.0f - BodyRig.footHeight(boneArray3[i].rotation());
            }
            float f2 = (boneArray3[4].position().y + boneArray3[5].position().y) * 0.5f - f;
            for (int i = 0; i < 4; ++i) {
                boneArray3[i].position().y += f2;
            }
        }
        return boneArray3;
    }

    public record Bone(Vector3f position, Quaternionf rotation) {
        public Matrix4f matrix() {
            return new Matrix4f().translationRotateScale((Vector3fc)this.position, (Quaternionfc)this.rotation, (Vector3fc)new Vector3f(1.0f));
        }
    }
}

