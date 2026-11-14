package net.farmingtales.ftboss.body;

import java.util.Set;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class BodyRig {
    private static final Set<String> AIMED = Set.of("beam", "combo", "rail", "contact");

    private BodyRig() {
    }

    private static Quaternionf rotation(double[] var0) {
        double var1 = Math.toRadians(var0[0]) * 0.5;
        double var3 = Math.toRadians(var0[1]) * 0.5;
        double var5 = Math.toRadians(var0[2]) * 0.5;
        double var7 = Math.cos(var1);
        double var9 = Math.sin(var1);
        double var11 = Math.cos(var3);
        double var13 = Math.sin(var3);
        double var15 = Math.cos(var5);
        double var17 = Math.sin(var5);
        return new Quaternionf(
            (float)(var9 * var11 * var15 - var7 * var13 * var17),
            (float)(var7 * var13 * var15 + var9 * var11 * var17),
            (float)(var7 * var11 * var17 - var9 * var13 * var15),
            (float)(var7 * var11 * var15 + var9 * var13 * var17)
        );
    }

    public static float footHeight(Quaternionf var0) {
        float var1 = -Float.MAX_VALUE;

        for (float var5 : new float[]{-2.0F, 2.0F}) {
            for (float var9 : new float[]{-2.0F, 2.0F}) {
                var1 = Math.max(var1, var0.transform(new Vector3f(var5, 12.0F, var9)).y);
            }
        }

        return var1;
    }

    public static BodyRig.Bone[] solve(BodyMotion.Pose var0, float var1, float var2, float var3) {
        double[][] var4 = new double[6][];

        for (int var5 = 0; var5 < 6; var5++) {
            var4[var5] = (double[])var0.angles()[var5].clone();
        }

        float var23 = Math.min(1.0F, Math.max(0.0F, var2)) * (float)(1.0 - var0.activity() * 0.85);
        var4[4][0] = var4[4][0] + Math.toDegrees(Math.cos((double)var1 * 0.6662) * (double)var23 * 0.8);
        var4[5][0] = var4[5][0] - Math.toDegrees(Math.cos((double)var1 * 0.6662) * (double)var23 * 0.8);
        var4[2][0] = var4[2][0] - Math.toDegrees(Math.cos((double)var1 * 0.6662 + Math.PI) * (double)var23 * 0.08);
        var4[3][0] = var4[3][0] - Math.toDegrees(Math.cos((double)var1 * 0.6662) * (double)var23 * 0.12);
        var4[1][1] = var4[1][1] * (1.0 - var0.aim()) + var0.yaw() * var0.aim();
        var4[4][1] = var4[4][1] + var0.yaw() * var0.aim() * 0.85;
        var4[5][1] = var4[5][1] + var0.yaw() * var0.aim() * 0.85;
        Quaternionf var6 = rotation(var4[1]);
        Quaternionf var7 = rotation(var4[4]);
        Quaternionf var8 = rotation(var4[5]);
        float var9 = !var0.air() && !((double)var2 > 0.1) ? 1.0F : 0.0F;
        float var10 = 12.0F + (12.0F - footHeight(var7)) * var9;
        float var11 = 12.0F + (12.0F - footHeight(var8)) * var9;
        float var12 = (var10 + var11) * 0.5F - 12.0F;
        Vector3f var13 = new Vector3f(0.0F, 12.0F + var12, (float)var0.shift());
        Vector3f var14 = var6.transform(new Vector3f(0.0F, -12.0F, 0.0F)).add(var13);
        BodyRig.Bone[] var15 = new BodyRig.Bone[6];
        var15[1] = new BodyRig.Bone(var14, var6);
        double var16 = var0.yaw() - var4[1][1];
        Quaternionf var18 = rotation(
            new double[]{
                Math.max(-65.0, Math.min(65.0, var0.pitch())) + var4[0][0] + var4[1][0] * 0.15,
                var4[1][1] + Math.max(-70.0, Math.min(70.0, var16)) + var4[0][1],
                var4[0][2] + var4[1][2] * 0.15
            }
        );
        var15[0] = new BodyRig.Bone(new Vector3f(var14), var18);

        for (int var19 = 2; var19 <= 3; var19++) {
            float var20 = var19 == 2 ? -1.0F : 1.0F;
            Vector3f var21 = var6.transform(new Vector3f(var20 * 5.0F, 2.5F, 0.0F)).add(var14);
            Quaternionf var22 = new Quaternionf(var6).mul(rotation(var4[var19]));
            if (var19 == (var0.mirror() ? 3 : 2) && AIMED.contains(var0.id())) {
                var22 = rotation(
                    new double[]{
                        var4[var19][0] + var0.pitch() * var0.activity() + var4[1][0] * (1.0 - var0.aim()),
                        var4[var19][1] + var4[1][1],
                        var4[var19][2] + var4[1][2] * (1.0 - var0.aim())
                    }
                );
            }

            var15[var19] = new BodyRig.Bone(var21, var22);
        }

        var15[4] = new BodyRig.Bone(new Vector3f((float)(-1.9 - var0.spread()), var10, (float)var0.shift()), var7);
        var15[5] = new BodyRig.Bone(new Vector3f((float)(1.9 + var0.spread()), var11, (float)var0.shift()), var8);
        return var15;
    }

    public static BodyRig.Bone[] blend(BodyRig.Bone[] var0, BodyRig.Bone[] var1, double var2, boolean var4) {
        BodyRig.Bone[] var5 = new BodyRig.Bone[6];

        for (int var6 = 0; var6 < 6; var6++) {
            var5[var6] = new BodyRig.Bone(
                new Vector3f(var0[var6].position()).lerp(var1[var6].position(), (float)var2),
                new Quaternionf(var0[var6].rotation()).slerp(var1[var6].rotation(), (float)var2)
            );
        }

        if (var4) {
            float var9 = (var5[4].position().y + var5[5].position().y) * 0.5F;

            for (int var7 = 4; var7 < 6; var7++) {
                var5[var7].position().y = 24.0F - footHeight(var5[var7].rotation());
            }

            float var10 = (var5[4].position().y + var5[5].position().y) * 0.5F - var9;

            for (int var8 = 0; var8 < 4; var8++) {
                var5[var8].position().y += var10;
            }
        }

        return var5;
    }

    public static record Bone(Vector3f position, Quaternionf rotation) {
        public Matrix4f matrix() {
            return new Matrix4f().translationRotateScale(this.position, this.rotation, new Vector3f(1.0F));
        }
    }
}
