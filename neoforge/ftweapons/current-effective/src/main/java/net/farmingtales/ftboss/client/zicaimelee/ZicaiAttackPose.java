package net.farmingtales.ftboss.client.zicaimelee;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class ZicaiAttackPose {
    private static final ZicaiAttackPose.Profile[] PROFILES = load();

    private ZicaiAttackPose() {
    }

    private static ZicaiAttackPose.Profile[] load() {
        InputStream var0 = ZicaiAttackPose.class.getResourceAsStream("/assets/ftboss/zicai_melee/attacks.json");
        if (var0 == null) {
            throw new IllegalStateException("Missing independent Zicai melee track");
        } else {
            try {
                ZicaiAttackPose.Profile[] var17;
                try (InputStreamReader var1 = new InputStreamReader(var0, StandardCharsets.UTF_8)) {
                    ZicaiAttackPose.Catalogue var2 = new Gson().fromJson(var1, ZicaiAttackPose.Catalogue.class);
                    if (!"zicai-humanoid-melee-set-v2".equals(var2.schema) || var2.profiles.length != 4) {
                        throw new IllegalStateException("Invalid Zicai melee catalogue");
                    }

                    for (ZicaiAttackPose.Profile var6 : var2.profiles) {
                        if (!"zicai-humanoid-melee-v1".equals(var6.schema)
                            || var6.bind.length != 6
                            || var6.keys.length < 2
                            || var6.keys[0].at != 0.0
                            || var6.keys[var6.keys.length - 1].at != 1.0) {
                            throw new IllegalStateException("Invalid Zicai melee profile");
                        }

                        for (int var7 = 0; var7 < var6.keys.length; var7++) {
                            ZicaiAttackPose.Key var8 = var6.keys[var7];
                            if (!Double.isFinite(var8.at) || var8.rotation.length != 6 || var7 > 0 && var8.at <= var6.keys[var7 - 1].at) {
                                throw new IllegalStateException("Invalid ordered keyframe");
                            }

                            for (double[] var12 : var8.rotation) {
                                if (var12.length != 3 || !Double.isFinite(var12[0]) || !Double.isFinite(var12[1]) || !Double.isFinite(var12[2])) {
                                    throw new IllegalStateException("Invalid bone rotation");
                                }
                            }
                        }
                    }

                    for (ZicaiAttackPose.Profile var20 : var2.profiles) {
                        var20.slopes = new double[var20.keys.length][6][3];

                        for (int var21 = 1; var21 < var20.keys.length - 1; var21++) {
                            for (int var22 = 0; var22 < 6; var22++) {
                                for (int var23 = 0; var23 < 3; var23++) {
                                    var20.slopes[var21][var22][var23] = tangent(var20, var21, var22, var23);
                                }
                            }
                        }
                    }

                    var17 = var2.profiles;
                }

                return var17;
            } catch (IOException var15) {
                throw new IllegalStateException("Cannot read Zicai melee track", var15);
            }
        }
    }

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, Double.isFinite(var0) ? var0 : 0.0));
    }

    private static double tangent(ZicaiAttackPose.Profile var0, int var1, int var2, int var3) {
        if (var1 != 0 && var1 != var0.keys.length - 1) {
            ZicaiAttackPose.Key var4 = var0.keys[var1 - 1];
            ZicaiAttackPose.Key var5 = var0.keys[var1];
            ZicaiAttackPose.Key var6 = var0.keys[var1 + 1];
            double var7 = var5.at - var4.at;
            double var9 = var6.at - var5.at;
            double var11 = (var5.rotation[var2][var3] - var4.rotation[var2][var3]) / var7;
            double var13 = (var6.rotation[var2][var3] - var5.rotation[var2][var3]) / var9;
            if (var11 * var13 <= 0.0) {
                return 0.0;
            } else {
                double var15 = 2.0 * var9 + var7;
                double var17 = var9 + 2.0 * var7;
                return (var15 + var17) / (var15 / var11 + var17 / var13);
            }
        } else {
            return 0.0;
        }
    }

    private static double[] rotate(double var0, double var2, double var4, double[] var6) {
        double var7 = var2 * Math.cos(var6[0]) - var4 * Math.sin(var6[0]);
        var4 = var2 * Math.sin(var6[0]) + var4 * Math.cos(var6[0]);
        double var14 = var0 * Math.cos(var6[1]) + var4 * Math.sin(var6[1]);
        var4 = -var0 * Math.sin(var6[1]) + var4 * Math.cos(var6[1]);
        double var15 = var14 * Math.cos(var6[2]) - var7 * Math.sin(var6[2]);
        var2 = var14 * Math.sin(var6[2]) + var7 * Math.cos(var6[2]);
        return new double[]{var15, var2, var4};
    }

    private static double soleHeight(double[] var0) {
        double var1 = Math.sin(var0[0]);
        double var3 = Math.cos(var0[0]);
        double var5 = Math.sin(var0[1]);
        double var7 = Math.cos(var0[1]);
        double var9 = Math.sin(var0[2]);
        double var11 = Math.cos(var0[2]);
        return 2.0 * Math.abs(var9 * var7) + 12.0 * Math.max(0.0, var9 * var5 * var1 + var11 * var3) + 2.0 * Math.abs(var9 * var5 * var3 - var11 * var1);
    }

    public static ZicaiAttackPose.Pose sample(double var0, double var2, boolean var4, double var5, double var7, double var9, double var11) {
        return sample(0, var0, var2, var4, var5, var7, var9, var11);
    }

    public static int selectAttack(long var0, long var2, long var4) {
        long var6 = (var0 ^ var2 ^ Long.rotateLeft(var4, 21)) + -7046029254386353131L;
        var6 = (var6 ^ var6 >>> 30) * -4658895280553007687L;
        var6 = (var6 ^ var6 >>> 27) * -7723592293110705685L;
        return (int)((var6 ^ var6 >>> 31) & 3L);
    }

    public static ZicaiAttackPose.Pose sample(int var0, double var1, double var3, boolean var5, double var6, double var8, double var10, double var12) {
        if (var0 >= 0 && var0 < PROFILES.length) {
            ZicaiAttackPose.Profile var14 = PROFILES[var0];
            double var15 = clamp(var1);
            double var17 = clamp(var3);
            double var19 = clamp(var8);
            int var21 = 1;

            while (var21 < var14.keys.length - 1 && var14.keys[var21].at < var15) {
                var21++;
            }

            ZicaiAttackPose.Key var22 = var14.keys[var21 - 1];
            ZicaiAttackPose.Key var23 = var14.keys[var21];
            double var24 = var23.at - var22.at;
            double var26 = clamp((var15 - var22.at) / var24);
            double var28 = var26 * var26;
            double var30 = var28 * var26;
            double var32 = Math.cos(var6 * 0.6662) * 1.4 * var19;
            double[][] var34 = new double[][]{
                {var12, var10, 0.0}, {0.0, 0.0, 0.0}, {-0.314159 - var32 * 0.32, 0.0, 0.08}, {var32 * 0.6, 0.0, -0.08}, {var32, 0.0, 0.0}, {-var32, 0.0, 0.0}
            };
            ZicaiAttackPose.Pose var35 = new ZicaiAttackPose.Pose();

            for (int var36 = 0; var36 < 6; var36++) {
                for (int var37 = 0; var37 < 3; var37++) {
                    double var38 = (
                            (2.0 * var30 - 3.0 * var28 + 1.0) * var22.rotation[var36][var37]
                                + (var30 - 2.0 * var28 + var26) * var24 * var14.slopes[var21 - 1][var36][var37]
                                + (-2.0 * var30 + 3.0 * var28) * var23.rotation[var36][var37]
                                + (var30 - var28) * var24 * var14.slopes[var21][var36][var37]
                        )
                        * Math.PI
                        / 180.0;
                    double var40 = var36 == 0 ? var34[0][var37] : 0.0;
                    double var42 = var36 >= 4 ? var34[var36][var37] * var19 : 0.0;
                    var35.rotation[var36][var37] = var34[var36][var37] * (1.0 - var17) + (var38 + var40 + var42) * var17;
                    var35.position[var36][var37] = var14.bind[var36][var37];
                }
            }

            if (var5) {
                for (double[] var39 : var35.rotation) {
                    var39[1] *= -1.0;
                    var39[2] *= -1.0;
                }

                var35.rotation[0][1] = var35.rotation[0][1] + 2.0 * var10;
                double[] var47 = var35.rotation[2];
                var35.rotation[2] = var35.rotation[3];
                var35.rotation[3] = var47;
                var47 = var35.rotation[4];
                var35.rotation[4] = var35.rotation[5];
                var35.rotation[5] = var47;
            }

            double[] var49 = rotate(-5.0, 2.5, 0.0, new double[]{0.0, var35.rotation[1][1], var35.rotation[1][2]});
            var35.position[2] = var49;
            var35.position[3] = new double[]{-var49[0], 5.0 - var49[1], -var49[2]};

            for (int var55 : new int[]{4, 5}) {
                double[] var41 = rotate(0.0, 12.0, 0.0, var35.rotation[var55]);
                double var56 = var17 * (1.0 - Math.min(1.0, var19 * 2.0));
                double var44 = soleHeight(var35.rotation[var55]);
                var35.position[var55][0] = var35.position[var55][0] - var41[0] * var56;
                var35.position[var55][1] = Math.min(var35.position[var55][1] + (12.0 - var44) * var56, 24.0 - var44);
                var35.position[var55][2] = var35.position[var55][2] - var41[2] * var56;
            }

            return var35;
        } else {
            throw new IllegalArgumentException("Unknown Zicai melee track");
        }
    }

    public static final class Catalogue {
        public String schema;
        public ZicaiAttackPose.Profile[] profiles;
    }

    public static final class Key {
        public double at;
        public double[][] rotation;
    }

    public static final class Pose {
        public final double[][] rotation = new double[6][3];
        public final double[][] position = new double[6][3];
    }

    public static final class Profile {
        public String schema;
        public double[][] bind;
        public ZicaiAttackPose.Key[] keys;
        private double[][][] slopes;
    }
}
