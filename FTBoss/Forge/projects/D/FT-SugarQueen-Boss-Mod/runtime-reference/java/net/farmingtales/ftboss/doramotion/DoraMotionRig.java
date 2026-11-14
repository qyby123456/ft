/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.doramotion;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class DoraMotionRig {
    private DoraMotionRig() {
    }

    private static Quaternionf rotation(double[] dArray) {
        return new Quaternionf().rotationZYX((float)Math.toRadians(dArray[2]), (float)Math.toRadians(dArray[1]), (float)Math.toRadians(dArray[0]));
    }

    private static Bone bone(Vector3f vector3f, Quaternionf quaternionf) {
        Quaternionf quaternionf2 = new Quaternionf((Quaternionfc)quaternionf).normalize();
        float f = (float)Math.atan2(2.0f * (quaternionf2.w * quaternionf2.x + quaternionf2.y * quaternionf2.z), 1.0f - 2.0f * (quaternionf2.x * quaternionf2.x + quaternionf2.y * quaternionf2.y));
        float f2 = (float)Math.asin(Math.max(-1.0f, Math.min(1.0f, 2.0f * (quaternionf2.w * quaternionf2.y - quaternionf2.z * quaternionf2.x))));
        float f3 = (float)Math.atan2(2.0f * (quaternionf2.w * quaternionf2.z + quaternionf2.x * quaternionf2.y), 1.0f - 2.0f * (quaternionf2.y * quaternionf2.y + quaternionf2.z * quaternionf2.z));
        return new Bone(new float[]{vector3f.x, vector3f.y, vector3f.z}, new float[]{f, f2, f3});
    }

    private static float footHeight(Quaternionf quaternionf) {
        float f = Float.NEGATIVE_INFINITY;
        for (int n : new int[]{-2, 2}) {
            for (int n2 : new int[]{-2, 2}) {
                f = Math.max(f, quaternionf.transform((Vector3f)new Vector3f((float)((float)n), (float)12.0f, (float)((float)n2))).y);
            }
        }
        return 24.0f - f;
    }

    public static Bone[] solve(double[][] dArray, boolean bl) {
        return DoraMotionRig.solve(dArray, bl, 2, 0.0);
    }

    public static Bone[] solve(double[][] dArray, boolean bl, int n, double d) {
        Bone[] boneArray = new Bone[6];
        Quaternionf quaternionf = DoraMotionRig.rotation(dArray[4]);
        Quaternionf quaternionf2 = DoraMotionRig.rotation(dArray[5]);
        float f = bl ? DoraMotionRig.footHeight(quaternionf) : 12.0f;
        float f2 = bl ? DoraMotionRig.footHeight(quaternionf2) : 12.0f;
        Vector3f vector3f = new Vector3f(0.0f, (f + f2) * 0.5f - 12.0f, 0.0f);
        Quaternionf quaternionf3 = DoraMotionRig.rotation(dArray[1]);
        boneArray[1] = DoraMotionRig.bone(vector3f, quaternionf3);
        boneArray[0] = DoraMotionRig.bone(vector3f, DoraMotionRig.rotation(dArray[0]));
        for (int i = 2; i <= 3; ++i) {
            Vector3f vector3f2 = quaternionf3.transform(new Vector3f(i == 2 ? -5.0f : 5.0f, 2.0f, 0.0f)).add((Vector3fc)vector3f);
            Quaternionf quaternionf4 = new Quaternionf((Quaternionfc)quaternionf3).mul((Quaternionfc)DoraMotionRig.rotation(dArray[i]));
            if (i == n) {
                quaternionf4.slerp((Quaternionfc)DoraMotionRig.rotation(dArray[i]), (float)Math.max(0.0, Math.min(1.0, d)));
            }
            boneArray[i] = DoraMotionRig.bone(vector3f2, quaternionf4);
        }
        boneArray[4] = DoraMotionRig.bone(new Vector3f(-1.9f, f, 0.0f), quaternionf);
        boneArray[5] = DoraMotionRig.bone(new Vector3f(1.9f, f2, 0.0f), quaternionf2);
        return boneArray;
    }

    public record Bone(float[] position, float[] rotation) {
    }
}

