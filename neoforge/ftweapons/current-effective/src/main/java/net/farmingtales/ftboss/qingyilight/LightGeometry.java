package net.farmingtales.ftboss.qingyilight;

import java.util.ArrayList;
import java.util.List;

public final class LightGeometry {
    private static final int CYAN = 8251885;
    private static final int WHITE = 15269876;
    private static final int PINK = 15824802;
    private static final int INDIGO = 9870591;
    private static final int GOLD = 16176258;
    private static final int[] ELEMENTS = new int[]{8251885, 15824802, 9870591, 16176258};

    private LightGeometry() {
    }

    private static double[] point(double var0, double var2, double var4) {
        return new double[]{Math.sin(var0) * var2, var4, Math.cos(var0) * var2};
    }

    private static double[] subtract(double[] var0, double[] var1) {
        return new double[]{var0[0] - var1[0], var0[1] - var1[1], var0[2] - var1[2]};
    }

    private static double[] cross(double[] var0, double[] var1) {
        return new double[]{var0[1] * var1[2] - var0[2] * var1[1], var0[2] * var1[0] - var0[0] * var1[2], var0[0] * var1[1] - var0[1] * var1[0]};
    }

    private static double[] normalize(double[] var0) {
        double var1 = Math.sqrt(var0[0] * var0[0] + var0[1] * var0[1] + var0[2] * var0[2]);
        return new double[]{var0[0] / var1, var0[1] / var1, var0[2] / var1};
    }

    public static void prism(List<double[]> var0, double[] var1, double[] var2, double var3) {
        double[] var5 = subtract(var2, var1);
        double var6 = Math.sqrt(var5[0] * var5[0] + var5[1] * var5[1] + var5[2] * var5[2]);
        if (!(var6 < 1.0E-6) && !(var3 <= 0.0)) {
            var5 = normalize(var5);
            double[] var8 = normalize(cross(var5, Math.abs(var5[1]) < 0.9 ? new double[]{0.0, 1.0, 0.0} : new double[]{1.0, 0.0, 0.0}));
            double[] var9 = cross(var5, var8);
            double[][] var10 = new double[8][3];

            for (int var11 = 0; var11 < 8; var11++) {
                double[] var12 = var11 < 4 ? var1 : var2;
                double var13 = var11 % 4 != 0 && var11 % 4 != 3 ? var3 : -var3;
                double var15 = var11 % 4 < 2 ? -var3 : var3;

                for (int var17 = 0; var17 < 3; var17++) {
                    var10[var11][var17] = var12[var17] + var8[var17] * var13 + var9[var17] * var15;
                }
            }

            for (int[] var14 : new int[][]{{0, 3, 2, 1}, {4, 5, 6, 7}, {0, 1, 5, 4}, {1, 2, 6, 5}, {2, 3, 7, 6}, {3, 0, 4, 7}}) {
                double[] var22 = new double[12];

                for (int var16 = 0; var16 < 4; var16++) {
                    System.arraycopy(var10[var14[var16]], 0, var22, var16 * 3, 3);
                }

                var0.add(var22);
            }
        }
    }

    private static void arc(List<double[]> var0, double var1, double var3, double var5, double var7, int var9, double var10) {
        for (int var12 = 0; var12 < var9; var12++) {
            prism(
                var0,
                point(var5 + (var7 - var5) * (double)var12 / (double)var9, var1, var3),
                point(var5 + (var7 - var5) * (double)(var12 + 1) / (double)var9, var1, var3),
                var10
            );
        }
    }

    private static LightGeometry.Layer layer(boolean var0, String var1, double var2, int var4) {
        return new LightGeometry.Layer(var0, var1, var2, var4, new ArrayList<>());
    }

    private static void boundary(LightGeometry.Layer var0, int var1, double var2, double var4, double var6) {
        List var8 = var0.faces();
        if (var1 == 0) {
            double var9 = Math.toRadians(50.0);
            arc(var8, var2, var4, -var9, var9, 32, var6);
            prism(var8, new double[]{0.0, var4, 0.0}, point(-var9, var2, var4), var6);
            prism(var8, new double[]{0.0, var4, 0.0}, point(var9, var2, var4), var6);
        } else if (var1 == 2) {
            double var13 = 1.15;
            double[][] var11 = new double[][]{
                {-var13, var4, var2},
                {var13, var4, var2},
                {var13, var4, var13},
                {var2, var4, var13},
                {var2, var4, -var13},
                {var13, var4, -var13},
                {var13, var4, -var2},
                {-var13, var4, -var2},
                {-var13, var4, -var13},
                {-var2, var4, -var13},
                {-var2, var4, var13},
                {-var13, var4, var13}
            };

            for (int var12 = 0; var12 < var11.length; var12++) {
                prism(var8, var11[var12], var11[(var12 + 1) % var11.length], var6);
            }
        } else {
            arc(var8, var2, var4, 0.0, Math.PI * 2, 64, var6);
        }
    }

    public static LightGeometry.Frame frame(int var0, int var1, double var2, int var4, double var5, int var7) {
        ArrayList var8 = new ArrayList();
        double var9 = LightRules.opacity(var2, var4);
        if (!(var9 <= 0.0) && var0 != 11 && LightRules.valid(var0, var1, var4, var5, var7)) {
            double var11 = Math.max(0.0, Math.min(1.0, var2 / (double)var4));
            if (var0 <= 3) {
                LightGeometry.Layer var13 = layer(true, "actual_center_boundary", var9 * (var1 == 0 ? 0.67 : 0.4), 8251885);
                boundary(var13, var0, var5, 0.075, 0.013);
                var8.add(var13);
                LightGeometry.Layer var14 = layer(true, "vertical_extent_markers", var9 * (var1 == 0 ? 0.35 : 0.18), 15269876);

                for (int var15 = 0; var15 < 4; var15++) {
                    double var16 = (double)var15 * Math.PI / 2.0;
                    double[] var18 = var0 == 2
                        ? point(var16, var5, 0.0)
                        : (var0 == 0 ? point(((double)var15 / 3.0 - 0.5) * Math.toRadians(100.0), var5, 0.0) : point(var16, var5, 0.0));
                    double var19 = -3.0;
                    prism(var14.faces(), new double[]{var18[0], var19 + 0.12, var18[2]}, new double[]{var18[0], var19 + 0.4, var18[2]}, 0.012);
                    prism(var14.faces(), new double[]{var18[0], 2.72, var18[2]}, new double[]{var18[0], 2.98, var18[2]}, 0.012);
                }

                var8.add(var14);
                if (var0 == 3) {
                    for (int var34 = 0; var34 < 4; var34++) {
                        LightGeometry.Layer var41 = layer(false, "four_element_" + var34, var9 * (var1 == 0 ? 0.63 : 0.84), ELEMENTS[var34]);
                        double var17 = (double)var34 * Math.PI / 2.0;
                        double var61 = var1 == 0 ? var2 * 0.045 : 0.0;
                        arc(
                            var41.faces(),
                            var5 * (var1 == 0 ? 0.72 + 0.22 * (1.0 - var11) : 0.2 + 0.73 * LightRules.smooth(var11)),
                            0.24,
                            var17 + 0.13 + var61,
                            var17 + 1.19 + var61,
                            10,
                            0.018
                        );

                        for (int var21 = 0; var21 < 9; var21++) {
                            double var22 = (double)var21 / 9.0;
                            double var24 = (double)(var21 + 1) / 9.0;
                            double var26 = var1 == 0 ? var5 * (1.0 - 0.75 * var11) : var5 * (0.28 + 0.56 * var11);
                            double var28 = var17 + var22 * (var1 == 0 ? 0.55 : 0.9);
                            double var30 = var17 + var24 * (var1 == 0 ? 0.55 : 0.9);
                            prism(
                                var41.faces(),
                                point(var28, var26 * (1.0 - 0.4 * var22), 0.24 + var22 * (var1 == 0 ? 1.8 : 0.8)),
                                point(var30, var26 * (1.0 - 0.4 * var24), 0.24 + var24 * (var1 == 0 ? 1.8 : 0.8)),
                                0.021
                            );
                        }

                        var8.add(var41);
                    }
                } else {
                    LightGeometry.Layer var35 = layer(
                        false, var1 == 0 ? "gathering_resonance" : "released_thunder", var9 * (var1 == 0 ? 0.38 : 0.85), var1 == 0 ? 9870591 : 15269876
                    );
                    if (var0 == 0) {
                        for (int var45 = 0; var45 < 5; var45++) {
                            double var53 = ((double)var45 / 4.0 - 0.5) * Math.toRadians(80.0);

                            for (int var64 = 0; var64 < 8; var64++) {
                                double var68 = ((double)var64 + 0.1) / 8.0;
                                double var74 = ((double)var64 + 0.8) / 8.0;
                                double var77 = var5 * (var1 == 0 ? 0.08 + 0.24 * var11 : 0.15 + 0.8 * LightRules.smooth(var11));
                                double var79 = var64 % 2 == 0 ? -0.012 : 0.012;
                                double[] var81 = point(var53 + var79, var77 * var68, 0.22 + Math.sin(var68 * Math.PI) * 0.3);
                                double[] var82 = point(var53 - var79, var77 * var74, 0.28 + Math.sin(var74 * Math.PI) * 0.3);
                                prism(var35.faces(), var81, var82, 0.022);
                            }
                        }
                    } else if (var0 == 2) {
                        for (int var44 = 0; var44 < 4; var44++) {
                            double var52 = (double)var44 * Math.PI / 2.0;

                            for (int var63 = 0; var63 < 9; var63++) {
                                double var20 = var5 * (double)(var63 + 1) / 10.0;
                                double var73 = var1 == 0 ? 0.26 : 0.55 + 0.3 * (1.0 - var11);
                                double var76 = var1 == 0 ? 0.22 : 0.18 + 0.28 * Math.sin(var11 * Math.PI);
                                double[] var78 = point(var52, var20, var76);
                                double[] var27 = point(var52, var20 + var73, var76);
                                prism(var35.faces(), var78, var27, 0.022);
                                if (var1 > 0) {
                                    for (int var31 : new int[]{-1, 1}) {
                                        prism(var35.faces(), point(var52 + (double)var31 * 0.012, var20 + var73 - 0.22, var76 + 0.07), var27, 0.015);
                                    }
                                }
                            }
                        }
                    } else {
                        for (int var42 = 0; var42 < 8; var42++) {
                            double var50 = (double)var42 * Math.PI / 4.0 + 0.15;
                            double var62 = var5 * 0.72;

                            for (int var71 = 0; var71 < 5; var71++) {
                                double[] var72 = point(var50 + (var71 % 2 == 0 ? 0.015 : -0.015), var62, 0.12 + (double)var71 * 0.26);
                                double[] var23 = point(var50 + (var71 % 2 == 0 ? -0.015 : 0.015), var62, 0.12 + (double)(var71 + 1) * 0.26);
                                prism(var35.faces(), var72, var23, 0.017);
                            }
                        }

                        if (var1 > 0) {
                            for (int var43 = 0; var43 < 8; var43++) {
                                double var51 = var43 % 2 == 0 ? -0.11 : 0.11;
                                prism(
                                    var35.faces(),
                                    new double[]{var51, 0.18 + (double)var43 * 0.34, 0.0},
                                    new double[]{-var51, 0.18 + (double)(var43 + 1) * 0.34, 0.0},
                                    0.024
                                );
                            }
                        }
                    }

                    var8.add(var35);
                    LightGeometry.Layer var46 = layer(false, "broken_luminous_front", var9 * 0.42, 8251885);
                    if (var0 != 2) {
                        arc(
                            var46.faces(),
                            var5 * (var1 == 0 ? 0.25 + 0.7 * var11 : 0.18 + 0.72 * var11),
                            0.15,
                            var0 == 0 ? -Math.toRadians(48.0) : var2 * 0.03,
                            var0 == 0 ? Math.toRadians(48.0) : var2 * 0.03 + (Math.PI * 3.0 / 2.0),
                            32,
                            0.019
                        );
                    }

                    var8.add(var46);
                }
            } else {
                int var32 = var0 == 8 ? 15824802 : (var0 == 9 ? 16176258 : (var0 == 7 ? ELEMENTS[var7 % 4] : 8251885));
                LightGeometry.Layer var33 = layer(true, "short_emissive_core", var9 * 0.88, var32);
                if (var0 == 4) {
                    for (int var60 : new int[]{-1, 1}) {
                        for (int var66 = 0; var66 < 12; var66++) {
                            double var69 = (double)var66 / 12.0;
                            double var75 = (double)(var66 + 1) / 12.0;
                            prism(
                                var33.faces(),
                                new double[]{(double)var60 * (0.42 + 0.15 * Math.sin(var69 * Math.PI)), 0.45 + 0.5 * var69, -0.75 + var69 * 0.8},
                                new double[]{(double)var60 * (0.42 + 0.15 * Math.sin(var75 * Math.PI)), 0.45 + 0.5 * var75, -0.75 + var75 * 0.8},
                                0.022
                            );
                        }
                    }
                } else if (var0 == 10) {
                    for (int var38 = 0; var38 < 9; var38++) {
                        double var47 = (var38 % 2 == 0 ? -0.08 : 0.08) * (1.0 - var11);
                        prism(var33.faces(), new double[]{var47, (double)var38 * 0.38, 0.0}, new double[]{-var47, (double)(var38 + 1) * 0.38, 0.0}, 0.018);
                    }

                    arc(var33.faces(), var5 * (0.5 + var11), 0.08, 0.0, Math.PI * 2, 24, 0.013);
                } else if ((var0 != 6 && var0 != 8 || var1 != 0) && var0 != 6) {
                    double var37 = var5 * (0.4 + 0.5 * LightRules.smooth(var11));

                    for (int var55 = 0; var55 < 8; var55++) {
                        double var59 = (double)var55 * Math.PI / 4.0;
                        prism(var33.faces(), point(var59, var37 * 0.62, 0.15), point(var59, var37, 0.18 + 0.16 * Math.sin((double)var55)), 0.018);
                    }

                    arc(var33.faces(), var37, 0.08, 0.0, Math.PI * 3.0 / 2.0, 24, 0.012);
                } else {
                    double var36 = var0 == 6 ? Math.max(0.14, 1.0 - var2 / 9.0) : 1.0;

                    for (int var54 = 0; var54 < 3; var54++) {
                        double[] var58 = new double[]{0.0, 0.0, 0.0};
                        double[] var65 = new double[]{0.0, 0.0, 0.0};
                        var58[var54] = -0.12 * var36;
                        var65[var54] = 0.12 * var36;
                        prism(var33.faces(), var58, var65, 0.013);
                    }

                    arc(var33.faces(), 0.15, 0.02, var2 * 0.06, var2 * 0.06 + (Math.PI * 7.0 / 5.0), 16, 0.008);
                }

                var8.add(var33);
                if (var0 == 5 || var0 == 9 || var0 == 7 || var0 == 8 && var1 == 1) {
                    LightGeometry.Layer var40 = layer(false, "detached_pixel_shards", var9 * 0.57, var0 == 8 ? 10602886 : 15269876);

                    for (int var49 = 0; var49 < 10; var49++) {
                        double var57 = (double)var49 * 2.399963 + var2 * 0.04;
                        double[] var67 = point(var57, var5 * (0.7 + 0.3 * var11), 0.2 + (double)(var49 % 4) * 0.16);
                        double[] var70 = (double[])var67.clone();
                        var70[1] += 0.07;
                        prism(var40.faces(), var67, var70, 0.018);
                    }

                    var8.add(var40);
                }
            }

            return new LightGeometry.Frame(var8);
        } else {
            return new LightGeometry.Frame(var8);
        }
    }

    public static record Frame(List<LightGeometry.Layer> layers) {
        public int faces() {
            return this.layers.stream().mapToInt(var0 -> var0.faces().size()).sum();
        }
    }

    public static record Layer(boolean essential, String name, double alpha, int color, List<double[]> faces) {
    }
}
