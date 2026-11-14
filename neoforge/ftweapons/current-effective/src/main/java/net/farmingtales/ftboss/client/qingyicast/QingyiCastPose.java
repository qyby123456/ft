package net.farmingtales.ftboss.client.qingyicast;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class QingyiCastPose {
    private static final QingyiCastPose.Profile[] PROFILES = load();

    private QingyiCastPose() {
    }

    private static void matrix(double[][] var0) {
        if (var0 != null && var0.length == 6) {
            for (double[] var4 : var0) {
                if (var4 == null || var4.length != 3 || !Double.isFinite(var4[0]) || !Double.isFinite(var4[1]) || !Double.isFinite(var4[2])) {
                    throw new IllegalStateException("Invalid Qingyi bone axes");
                }
            }
        } else {
            throw new IllegalStateException("Invalid Qingyi bone array");
        }
    }

    private static QingyiCastPose.Profile[] load() {
        InputStream var0 = QingyiCastPose.class.getResourceAsStream("/assets/ftboss/qingyi_cast/casts.json");
        if (var0 == null) {
            throw new IllegalStateException("Missing Qingyi character casts");
        } else {
            try {
                QingyiCastPose.Profile[] var13;
                try (InputStreamReader var1 = new InputStreamReader(var0, StandardCharsets.UTF_8)) {
                    QingyiCastPose.Catalogue var2 = new Gson().fromJson(var1, QingyiCastPose.Catalogue.class);
                    if (var2 == null || !"qingyi-skill-casts-v1".equals(var2.schema) || var2.profiles == null || var2.profiles.length != 8) {
                        throw new IllegalStateException("Invalid Qingyi cast catalogue");
                    }

                    for (QingyiCastPose.Profile var6 : var2.profiles) {
                        matrix(var6.bind);
                        if (var6.keys == null || var6.keys.length < 2 || var6.keys[0].at != 0.0 || var6.keys[var6.keys.length - 1].at != 1.0) {
                            throw new IllegalStateException("Incomplete Qingyi keyframes");
                        }

                        for (int var7 = 0; var7 < var6.keys.length; var7++) {
                            QingyiCastPose.Key var8 = var6.keys[var7];
                            matrix(var8.rotation);
                            if (!Double.isFinite(var8.at) || var7 > 0 && var8.at <= var6.keys[var7 - 1].at) {
                                throw new IllegalStateException("Unordered Qingyi keyframes");
                            }
                        }

                        var6.slopes = new double[var6.keys.length][6][3];

                        for (int var14 = 0; var14 < var6.keys.length; var14++) {
                            for (int var15 = 0; var15 < 6; var15++) {
                                for (int var9 = 0; var9 < 3; var9++) {
                                    var6.slopes[var14][var15][var9] = tangent(var6, var14, var15, var9);
                                }
                            }
                        }
                    }

                    var13 = var2.profiles;
                }

                return var13;
            } catch (IOException var12) {
                throw new IllegalStateException("Cannot read Qingyi keyframes", var12);
            }
        }
    }

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, Double.isFinite(var0) ? var0 : 0.0));
    }

    private static double tangent(QingyiCastPose.Profile var0, int var1, int var2, int var3) {
        if (var1 != 0 && var1 != var0.keys.length - 1) {
            QingyiCastPose.Key var4 = var0.keys[var1 - 1];
            QingyiCastPose.Key var5 = var0.keys[var1];
            QingyiCastPose.Key var6 = var0.keys[var1 + 1];
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

    private static double[] rotate(double[] var0, double[] var1) {
        double var2 = var0[0];
        double var4 = var0[1];
        double var6 = var0[2];
        double var8 = var4 * Math.cos(var1[0]) - var6 * Math.sin(var1[0]);
        var6 = var4 * Math.sin(var1[0]) + var6 * Math.cos(var1[0]);
        double var15 = var2 * Math.cos(var1[1]) + var6 * Math.sin(var1[1]);
        var6 = -var2 * Math.sin(var1[1]) + var6 * Math.cos(var1[1]);
        double var16 = var15 * Math.cos(var1[2]) - var8 * Math.sin(var1[2]);
        var4 = var15 * Math.sin(var1[2]) + var8 * Math.cos(var1[2]);
        return new double[]{var16, var4, var6};
    }

    public static QingyiCastPose.Pose sample(int var0, double var1, boolean var3, double var4, double var6, double var8, double var10) {
        if (var0 >= 0 && var0 < PROFILES.length) {
            QingyiCastPose.Profile var12 = PROFILES[var0];
            double var13 = clamp(var1);
            int var15 = 1;

            while (var15 < var12.keys.length - 1 && var12.keys[var15].at < var13) {
                var15++;
            }

            QingyiCastPose.Key var16 = var12.keys[var15 - 1];
            QingyiCastPose.Key var17 = var12.keys[var15];
            double var18 = var17.at - var16.at;
            double var20 = clamp((var13 - var16.at) / var18);
            double var22 = var20 * var20;
            double var24 = var22 * var20;
            QingyiCastPose.Pose var26 = new QingyiCastPose.Pose();

            for (int var27 = 0; var27 < 6; var27++) {
                for (int var28 = 0; var28 < 3; var28++) {
                    double var29 = (2.0 * var24 - 3.0 * var22 + 1.0) * var16.rotation[var27][var28]
                        + (var24 - 2.0 * var22 + var20) * var18 * var12.slopes[var15 - 1][var27][var28]
                        + (-2.0 * var24 + 3.0 * var22) * var17.rotation[var27][var28]
                        + (var24 - var22) * var18 * var12.slopes[var15][var27][var28];
                    var26.rotation[var27][var28] = var29 * Math.PI / 180.0;
                    var26.position[var27][var28] = var12.bind[var27][var28];
                }
            }

            if (var3) {
                for (double[] var30 : var26.rotation) {
                    var30[1] *= -1.0;
                    var30[2] *= -1.0;
                }

                double[] var52 = var26.rotation[2];
                var26.rotation[2] = var26.rotation[3];
                var26.rotation[3] = var52;
                var52 = var26.rotation[4];
                var26.rotation[4] = var26.rotation[5];
                var26.rotation[5] = var52;
            }

            var26.rotation[0][0] = var26.rotation[0][0] + (Double.isFinite(var10) ? var10 : 0.0);
            var26.rotation[0][1] = var26.rotation[0][1] + (Double.isFinite(var8) ? var8 : 0.0);
            double var54 = Math.cos(Double.isFinite(var6) ? var6 * 0.6662 : 0.0) * 1.4 * clamp(var4);
            var26.rotation[4][0] = var26.rotation[4][0] + var54;
            var26.rotation[5][0] = var26.rotation[5][0] - var54;
            double[] var57 = rotate(new double[]{0.0, 12.0, 0.0}, var26.rotation[1]);
            var26.position[1] = new double[]{-var57[0], 12.0 - var57[1], -var57[2]};
            var26.position[0] = (double[])var26.position[1].clone();

            for (int var33 : new int[]{2, 3}) {
                double[] var34 = rotate(var12.bind[var33], var26.rotation[1]);

                for (int var35 = 0; var35 < 3; var35++) {
                    var26.position[var33][var35] = var34[var35] + var26.position[1][var35];
                }
            }

            double var59 = 1.0 - Math.min(1.0, clamp(var4) * 2.0);

            for (int var63 : new int[]{4, 5}) {
                double[] var36 = rotate(new double[]{0.0, 12.0, 0.0}, var26.rotation[var63]);
                double var37 = Double.NEGATIVE_INFINITY;

                for (int var42 : new int[]{-2, 2}) {
                    for (int var46 : new int[]{0, 12}) {
                        for (int var50 : new int[]{-2, 2}) {
                            var37 = Math.max(var37, rotate(new double[]{(double)var42, (double)var46, (double)var50}, var26.rotation[var63])[1]);
                        }
                    }
                }

                var26.position[var63][0] = var26.position[var63][0] - var36[0] * var59;
                var26.position[var63][1] = Math.min(var26.position[var63][1] + (12.0 - var37) * var59, 24.0 - var37);
                var26.position[var63][2] = var26.position[var63][2] - var36[2] * var59;
            }

            return var26;
        } else {
            throw new IllegalArgumentException("Unknown Qingyi cast");
        }
    }

    public static final class Catalogue {
        public String schema;
        public QingyiCastPose.Profile[] profiles;
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
        public String id;
        public double[][] bind;
        public QingyiCastPose.Key[] keys;
        public transient double[][][] slopes;
    }
}
