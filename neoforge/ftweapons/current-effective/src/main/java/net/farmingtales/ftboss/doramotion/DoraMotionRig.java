package net.farmingtales.ftboss.doramotion;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class DoraMotionRig {
    private DoraMotionRig() {
    }

    private static Quaternionf rotation(double[] var0) {
        return new Quaternionf().rotationZYX((float)Math.toRadians(var0[2]), (float)Math.toRadians(var0[1]), (float)Math.toRadians(var0[0]));
    }

    private static DoraMotionRig.Bone bone(Vector3f var0, Quaternionf var1) {
        Quaternionf var2 = new Quaternionf(var1).normalize();
        float var3 = (float)Math.atan2((double)(2.0F * (var2.w * var2.x + var2.y * var2.z)), (double)(1.0F - 2.0F * (var2.x * var2.x + var2.y * var2.y)));
        float var4 = (float)Math.asin((double)Math.max(-1.0F, Math.min(1.0F, 2.0F * (var2.w * var2.y - var2.z * var2.x))));
        float var5 = (float)Math.atan2((double)(2.0F * (var2.w * var2.z + var2.x * var2.y)), (double)(1.0F - 2.0F * (var2.y * var2.y + var2.z * var2.z)));
        return new DoraMotionRig.Bone(new float[]{var0.x, var0.y, var0.z}, new float[]{var3, var4, var5});
    }

    private static float footHeight(Quaternionf var0) {
        float var1 = Float.NEGATIVE_INFINITY;

        for (int var5 : new int[]{-2, 2}) {
            for (int var9 : new int[]{-2, 2}) {
                var1 = Math.max(var1, var0.transform(new Vector3f((float)var5, 12.0F, (float)var9)).y);
            }
        }

        return 24.0F - var1;
    }

    public static DoraMotionRig.Bone[] solve(double[][] var0, boolean var1) {
        return solve(var0, var1, 2, 0.0);
    }

    public static DoraMotionRig.Bone[] solve(double[][] var0, boolean var1, int var2, double var3) {
        DoraMotionRig.Bone[] var5 = new DoraMotionRig.Bone[6];
        Quaternionf var6 = rotation(var0[4]);
        Quaternionf var7 = rotation(var0[5]);
        float var8 = var1 ? footHeight(var6) : 12.0F;
        float var9 = var1 ? footHeight(var7) : 12.0F;
        Vector3f var10 = new Vector3f(0.0F, (var8 + var9) * 0.5F - 12.0F, 0.0F);
        Quaternionf var11 = rotation(var0[1]);
        var5[1] = bone(var10, var11);
        var5[0] = bone(var10, rotation(var0[0]));

        for (int var12 = 2; var12 <= 3; var12++) {
            Vector3f var13 = var11.transform(new Vector3f(var12 == 2 ? -5.0F : 5.0F, 2.0F, 0.0F)).add(var10);
            Quaternionf var14 = new Quaternionf(var11).mul(rotation(var0[var12]));
            if (var12 == var2) {
                var14.slerp(rotation(var0[var12]), (float)Math.max(0.0, Math.min(1.0, var3)));
            }

            var5[var12] = bone(var13, var14);
        }

        var5[4] = bone(new Vector3f(-1.9F, var8, 0.0F), var6);
        var5[5] = bone(new Vector3f(1.9F, var9, 0.0F), var7);
        return var5;
    }

    public static record Bone(float[] position, float[] rotation) {
    }
}
