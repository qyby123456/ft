/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.qingyilight;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.qingyilight.LightRules;

public final class LightGeometry {
    private static final int CYAN = 8251885;
    private static final int WHITE = 15269876;
    private static final int PINK = 15824802;
    private static final int INDIGO = 9870591;
    private static final int GOLD = 16176258;
    private static final int[] ELEMENTS = new int[]{8251885, 15824802, 9870591, 16176258};

    private LightGeometry() {
    }

    private static double[] point(double d, double d2, double d3) {
        return new double[]{Math.sin(d) * d2, d3, Math.cos(d) * d2};
    }

    private static double[] subtract(double[] dArray, double[] dArray2) {
        return new double[]{dArray[0] - dArray2[0], dArray[1] - dArray2[1], dArray[2] - dArray2[2]};
    }

    private static double[] cross(double[] dArray, double[] dArray2) {
        return new double[]{dArray[1] * dArray2[2] - dArray[2] * dArray2[1], dArray[2] * dArray2[0] - dArray[0] * dArray2[2], dArray[0] * dArray2[1] - dArray[1] * dArray2[0]};
    }

    private static double[] normalize(double[] dArray) {
        double d = Math.sqrt(dArray[0] * dArray[0] + dArray[1] * dArray[1] + dArray[2] * dArray[2]);
        return new double[]{dArray[0] / d, dArray[1] / d, dArray[2] / d};
    }

    public static void prism(List<double[]> list, double[] dArray, double[] dArray2, double d) {
        double[] dArray3;
        double[] dArray4 = LightGeometry.subtract(dArray2, dArray);
        double d2 = Math.sqrt(dArray4[0] * dArray4[0] + dArray4[1] * dArray4[1] + dArray4[2] * dArray4[2]);
        if (d2 < 1.0E-6 || d <= 0.0) {
            return;
        }
        if (Math.abs((dArray4 = LightGeometry.normalize(dArray4))[1]) < 0.9) {
            double[] dArray5 = new double[3];
            dArray5[0] = 0.0;
            dArray5[1] = 1.0;
            dArray3 = dArray5;
            dArray5[2] = 0.0;
        } else {
            double[] dArray6 = new double[3];
            dArray6[0] = 1.0;
            dArray6[1] = 0.0;
            dArray3 = dArray6;
            dArray6[2] = 0.0;
        }
        double[] dArray7 = LightGeometry.normalize(LightGeometry.cross(dArray4, dArray3));
        double[] dArray8 = LightGeometry.cross(dArray4, dArray7);
        double[][] dArray9 = new double[8][3];
        for (int i = 0; i < 8; ++i) {
            double[] dArray10 = i < 4 ? dArray : dArray2;
            double d3 = i % 4 == 0 || i % 4 == 3 ? -d : d;
            double d4 = i % 4 < 2 ? -d : d;
            for (int j = 0; j < 3; ++j) {
                dArray9[i][j] = dArray10[j] + dArray7[j] * d3 + dArray8[j] * d4;
            }
        }
        for (int[] nArray : new int[][]{{0, 3, 2, 1}, {4, 5, 6, 7}, {0, 1, 5, 4}, {1, 2, 6, 5}, {2, 3, 7, 6}, {3, 0, 4, 7}}) {
            double[] dArray11 = new double[12];
            for (int i = 0; i < 4; ++i) {
                System.arraycopy(dArray9[nArray[i]], 0, dArray11, i * 3, 3);
            }
            list.add(dArray11);
        }
    }

    private static void arc(List<double[]> list, double d, double d2, double d3, double d4, int n, double d5) {
        for (int i = 0; i < n; ++i) {
            LightGeometry.prism(list, LightGeometry.point(d3 + (d4 - d3) * (double)i / (double)n, d, d2), LightGeometry.point(d3 + (d4 - d3) * (double)(i + 1) / (double)n, d, d2), d5);
        }
    }

    private static Layer layer(boolean bl, String string, double d, int n) {
        return new Layer(bl, string, d, n, new ArrayList<double[]>());
    }

    private static void boundary(Layer layer, int n, double d, double d2, double d3) {
        List<double[]> list = layer.faces();
        if (n == 0) {
            double d4 = Math.toRadians(50.0);
            LightGeometry.arc(list, d, d2, -d4, d4, 32, d3);
            LightGeometry.prism(list, new double[]{0.0, d2, 0.0}, LightGeometry.point(-d4, d, d2), d3);
            LightGeometry.prism(list, new double[]{0.0, d2, 0.0}, LightGeometry.point(d4, d, d2), d3);
        } else if (n == 2) {
            double d5 = 1.15;
            double[][] dArrayArray = new double[][]{{-d5, d2, d}, {d5, d2, d}, {d5, d2, d5}, {d, d2, d5}, {d, d2, -d5}, {d5, d2, -d5}, {d5, d2, -d}, {-d5, d2, -d}, {-d5, d2, -d5}, {-d, d2, -d5}, {-d, d2, d5}, {-d5, d2, d5}};
            for (int i = 0; i < dArrayArray.length; ++i) {
                LightGeometry.prism(list, dArrayArray[i], dArrayArray[(i + 1) % dArrayArray.length], d3);
            }
        } else {
            LightGeometry.arc(list, d, d2, 0.0, Math.PI * 2, 64, d3);
        }
    }

    public static Frame frame(int n, int n2, double d, int n3, double d2, int n4) {
        ArrayList<Layer> arrayList = new ArrayList<Layer>();
        double d3 = LightRules.opacity(d, n3);
        if (d3 <= 0.0 || n == 11 || !LightRules.valid(n, n2, n3, d2, n4)) {
            return new Frame(arrayList);
        }
        double d4 = Math.max(0.0, Math.min(1.0, d / (double)n3));
        if (n <= 3) {
            double d5;
            int n5;
            Layer layer = LightGeometry.layer(true, "actual_center_boundary", d3 * (n2 == 0 ? 0.67 : 0.4), 8251885);
            LightGeometry.boundary(layer, n, d2, 0.075, 0.013);
            arrayList.add(layer);
            Layer layer2 = LightGeometry.layer(true, "vertical_extent_markers", d3 * (n2 == 0 ? 0.35 : 0.18), 15269876);
            for (n5 = 0; n5 < 4; ++n5) {
                double d6 = (double)n5 * Math.PI / 2.0;
                double[] dArray = n == 2 ? LightGeometry.point(d6, d2, 0.0) : (n == 0 ? LightGeometry.point(((double)n5 / 3.0 - 0.5) * Math.toRadians(100.0), d2, 0.0) : LightGeometry.point(d6, d2, 0.0));
                d5 = -3.0;
                LightGeometry.prism(layer2.faces(), new double[]{dArray[0], d5 + 0.12, dArray[2]}, new double[]{dArray[0], d5 + 0.4, dArray[2]}, 0.012);
                LightGeometry.prism(layer2.faces(), new double[]{dArray[0], 2.72, dArray[2]}, new double[]{dArray[0], 2.98, dArray[2]}, 0.012);
            }
            arrayList.add(layer2);
            if (n == 3) {
                for (n5 = 0; n5 < 4; ++n5) {
                    Layer layer3 = LightGeometry.layer(false, "four_element_" + n5, d3 * (n2 == 0 ? 0.63 : 0.84), ELEMENTS[n5]);
                    double d7 = (double)n5 * Math.PI / 2.0;
                    d5 = n2 == 0 ? d * 0.045 : 0.0;
                    LightGeometry.arc(layer3.faces(), d2 * (n2 == 0 ? 0.72 + 0.22 * (1.0 - d4) : 0.2 + 0.73 * LightRules.smooth(d4)), 0.24, d7 + 0.13 + d5, d7 + 1.19 + d5, 10, 0.018);
                    for (int i = 0; i < 9; ++i) {
                        double d8 = (double)i / 9.0;
                        double d9 = (double)(i + 1) / 9.0;
                        double d10 = n2 == 0 ? d2 * (1.0 - 0.75 * d4) : d2 * (0.28 + 0.56 * d4);
                        double d11 = d7 + d8 * (n2 == 0 ? 0.55 : 0.9);
                        double d12 = d7 + d9 * (n2 == 0 ? 0.55 : 0.9);
                        LightGeometry.prism(layer3.faces(), LightGeometry.point(d11, d10 * (1.0 - 0.4 * d8), 0.24 + d8 * (n2 == 0 ? 1.8 : 0.8)), LightGeometry.point(d12, d10 * (1.0 - 0.4 * d9), 0.24 + d9 * (n2 == 0 ? 1.8 : 0.8)), 0.021);
                    }
                    arrayList.add(layer3);
                }
            } else {
                Layer layer4 = LightGeometry.layer(false, n2 == 0 ? "gathering_resonance" : "released_thunder", d3 * (n2 == 0 ? 0.38 : 0.85), n2 == 0 ? 9870591 : 15269876);
                if (n == 0) {
                    for (var16_22 = 0; var16_22 < 5; ++var16_22) {
                        double d13 = ((double)var16_22 / 4.0 - 0.5) * Math.toRadians(80.0);
                        for (int i = 0; i < 8; ++i) {
                            double d14 = ((double)i + 0.1) / 8.0;
                            double d15 = ((double)i + 0.8) / 8.0;
                            double d16 = d2 * (n2 == 0 ? 0.08 + 0.24 * d4 : 0.15 + 0.8 * LightRules.smooth(d4));
                            double d17 = i % 2 == 0 ? -0.012 : 0.012;
                            double[] dArray = LightGeometry.point(d13 + d17, d16 * d14, 0.22 + Math.sin(d14 * Math.PI) * 0.3);
                            double[] dArray2 = LightGeometry.point(d13 - d17, d16 * d15, 0.28 + Math.sin(d15 * Math.PI) * 0.3);
                            LightGeometry.prism(layer4.faces(), dArray, dArray2, 0.022);
                        }
                    }
                } else if (n == 2) {
                    for (var16_22 = 0; var16_22 < 4; ++var16_22) {
                        double d18 = (double)var16_22 * Math.PI / 2.0;
                        for (int i = 0; i < 9; ++i) {
                            double d19 = d2 * (double)(i + 1) / 10.0;
                            double d20 = n2 == 0 ? 0.26 : 0.55 + 0.3 * (1.0 - d4);
                            double d21 = n2 == 0 ? 0.22 : 0.18 + 0.28 * Math.sin(d4 * Math.PI);
                            double[] dArray = LightGeometry.point(d18, d19, d21);
                            double[] dArray3 = LightGeometry.point(d18, d19 + d20, d21);
                            LightGeometry.prism(layer4.faces(), dArray, dArray3, 0.022);
                            if (n2 <= 0) continue;
                            for (int n6 : new int[]{-1, 1}) {
                                LightGeometry.prism(layer4.faces(), LightGeometry.point(d18 + (double)n6 * 0.012, d19 + d20 - 0.22, d21 + 0.07), dArray3, 0.015);
                            }
                        }
                    }
                } else {
                    double d22;
                    for (var16_22 = 0; var16_22 < 8; ++var16_22) {
                        d22 = (double)var16_22 * Math.PI / 4.0 + 0.15;
                        d5 = d2 * 0.72;
                        for (int i = 0; i < 5; ++i) {
                            double[] dArray = LightGeometry.point(d22 + (i % 2 == 0 ? 0.015 : -0.015), d5, 0.12 + (double)i * 0.26);
                            double[] dArray4 = LightGeometry.point(d22 + (i % 2 == 0 ? -0.015 : 0.015), d5, 0.12 + (double)(i + 1) * 0.26);
                            LightGeometry.prism(layer4.faces(), dArray, dArray4, 0.017);
                        }
                    }
                    if (n2 > 0) {
                        for (var16_22 = 0; var16_22 < 8; ++var16_22) {
                            d22 = var16_22 % 2 == 0 ? -0.11 : 0.11;
                            LightGeometry.prism(layer4.faces(), new double[]{d22, 0.18 + (double)var16_22 * 0.34, 0.0}, new double[]{-d22, 0.18 + (double)(var16_22 + 1) * 0.34, 0.0}, 0.024);
                        }
                    }
                }
                arrayList.add(layer4);
                Layer layer5 = LightGeometry.layer(false, "broken_luminous_front", d3 * 0.42, 8251885);
                if (n != 2) {
                    LightGeometry.arc(layer5.faces(), d2 * (n2 == 0 ? 0.25 + 0.7 * d4 : 0.18 + 0.72 * d4), 0.15, n == 0 ? -Math.toRadians(48.0) : d * 0.03, n == 0 ? Math.toRadians(48.0) : d * 0.03 + 4.71238898038469, 32, 0.019);
                }
                arrayList.add(layer5);
            }
        } else {
            int n7 = n == 8 ? 15824802 : (n == 9 ? 16176258 : (n == 7 ? ELEMENTS[n4 % 4] : 8251885));
            Layer layer = LightGeometry.layer(true, "short_emissive_core", d3 * 0.88, n7);
            if (n == 4) {
                for (int n8 : new int[]{-1, 1}) {
                    for (int i = 0; i < 12; ++i) {
                        double d23 = (double)i / 12.0;
                        double d24 = (double)(i + 1) / 12.0;
                        LightGeometry.prism(layer.faces(), new double[]{(double)n8 * (0.42 + 0.15 * Math.sin(d23 * Math.PI)), 0.45 + 0.5 * d23, -0.75 + d23 * 0.8}, new double[]{(double)n8 * (0.42 + 0.15 * Math.sin(d24 * Math.PI)), 0.45 + 0.5 * d24, -0.75 + d24 * 0.8}, 0.022);
                    }
                }
            } else if (n == 10) {
                for (int i = 0; i < 9; ++i) {
                    double d25 = (i % 2 == 0 ? -0.08 : 0.08) * (1.0 - d4);
                    LightGeometry.prism(layer.faces(), new double[]{d25, (double)i * 0.38, 0.0}, new double[]{-d25, (double)(i + 1) * 0.38, 0.0}, 0.018);
                }
                LightGeometry.arc(layer.faces(), d2 * (0.5 + d4), 0.08, 0.0, Math.PI * 2, 24, 0.013);
            } else if ((n == 6 || n == 8) && n2 == 0 || n == 6) {
                double d26 = n == 6 ? Math.max(0.14, 1.0 - d / 9.0) : 1.0;
                for (var17_41 = 0; var17_41 < 3; ++var17_41) {
                    double[] dArray = new double[]{0.0, 0.0, 0.0};
                    double[] dArray5 = new double[]{0.0, 0.0, 0.0};
                    dArray[var17_41] = -0.12 * d26;
                    dArray5[var17_41] = 0.12 * d26;
                    LightGeometry.prism(layer.faces(), dArray, dArray5, 0.013);
                }
                LightGeometry.arc(layer.faces(), 0.15, 0.02, d * 0.06, d * 0.06 + 4.39822971502571, 16, 0.008);
            } else {
                double d27 = d2 * (0.4 + 0.5 * LightRules.smooth(d4));
                for (var17_41 = 0; var17_41 < 8; ++var17_41) {
                    double d28 = (double)var17_41 * Math.PI / 4.0;
                    LightGeometry.prism(layer.faces(), LightGeometry.point(d28, d27 * 0.62, 0.15), LightGeometry.point(d28, d27, 0.18 + 0.16 * Math.sin(var17_41)), 0.018);
                }
                LightGeometry.arc(layer.faces(), d27, 0.08, 0.0, 4.71238898038469, 24, 0.012);
            }
            arrayList.add(layer);
            if (n == 5 || n == 9 || n == 7 || n == 8 && n2 == 1) {
                Layer layer6 = LightGeometry.layer(false, "detached_pixel_shards", d3 * 0.57, n == 8 ? 10602886 : 15269876);
                for (int i = 0; i < 10; ++i) {
                    double d29 = (double)i * 2.399963 + d * 0.04;
                    double[] dArray = LightGeometry.point(d29, d2 * (0.7 + 0.3 * d4), 0.2 + (double)(i % 4) * 0.16);
                    double[] dArray6 = (double[])dArray.clone();
                    dArray6[1] = dArray6[1] + 0.07;
                    LightGeometry.prism(layer6.faces(), dArray, dArray6, 0.018);
                }
                arrayList.add(layer6);
            }
        }
        return new Frame(arrayList);
    }

    public record Layer(boolean essential, String name, double alpha, int color, List<double[]> faces) {
    }

    public record Frame(List<Layer> layers) {
        public int faces() {
            return this.layers.stream().mapToInt(layer -> layer.faces().size()).sum();
        }
    }
}

