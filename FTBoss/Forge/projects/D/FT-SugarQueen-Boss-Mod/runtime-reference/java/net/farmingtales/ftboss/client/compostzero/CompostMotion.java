/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.compostzero;

public final class CompostMotion {
    private CompostMotion() {
    }

    public static State pose(String string, double[] dArray, boolean bl, double d, String string2, int n) {
        double[] dArray2 = new double[]{0.0, 0.0, 0.0};
        double[] dArray3 = new double[]{0.0, 0.0, 0.0};
        double[] dArray4 = new double[]{1.0, 1.0, 1.0};
        double d2 = 1.0;
        switch (string) {
            case "breath": {
                dArray4 = CompostMotion.uniform(1.0 + 0.028 * Math.sin(d * 0.2));
                break;
            }
            case "float": {
                dArray2[1] = 0.065 * Math.sin(d * 0.18 + dArray[0]);
                dArray3[1] = d * 1.4 + dArray[0] * 8.0;
                break;
            }
            case "inward": {
                double d3 = Math.min(1.0, d / 18.0) * (bl ? 1.5 : 0.72);
                dArray2 = new double[]{-Math.cos(dArray[0]) * d3, 0.15 * Math.sin(Math.min(1.0, d / 18.0) * Math.PI), -Math.sin(dArray[0]) * d3};
                break;
            }
            case "collapse": {
                dArray4 = CompostMotion.uniform(Math.max(0.05, 1.0 - d / 30.0));
                dArray3[1] = d * 4.0;
                break;
            }
            case "harvest_claw": {
                double d4 = Math.max(0.0, Math.min(1.0, (d - dArray[1] * 0.6) / 12.0));
                d4 = d4 * d4 * (3.0 - 2.0 * d4);
                if (string2.equals("CHARGE")) {
                    d4 *= 0.12;
                }
                double d5 = d4 * 0.23;
                dArray2 = new double[]{-Math.cos(dArray[0]) * d5, 0.035 * Math.sin(d4 * Math.PI), -Math.sin(dArray[0]) * d5};
                dArray3[1] = -d4 * 24.0;
                break;
            }
            case "harvest_core": {
                double d6 = Math.min(1.0, d / 14.0);
                double d7 = 0.035 * Math.sin(d6 * Math.PI);
                dArray4 = new double[]{1.0 + d7, 1.0 - d7 * 0.7, 1.0 + d7};
                break;
            }
            case "harvest_flow": {
                double d8 = Math.max(0.0, Math.min(1.0, (d - dArray[1] * 1.3) / 13.0));
                double d9 = d8 * 2.43;
                dArray2 = new double[]{-Math.cos(dArray[0]) * d9, 0.2 * Math.sin(d8 * Math.PI), -Math.sin(dArray[0]) * d9};
                dArray4 = CompostMotion.uniform(1.0 - d8 * 0.4);
                d2 = string2.equals("CHARGE") ? 0.0 : 0.68 * Math.max(0.0, 1.0 - d8 * d8);
                break;
            }
            case "bud": {
                d2 = string2.equals("PULSE") ? 0.0 : 1.0;
                dArray4 = new double[]{1.0 + 0.07 * Math.sin(d * 0.6), 1.0 + Math.min(d / 16.0, 1.0) * 0.12, 1.0 + 0.07 * Math.sin(d * 0.6)};
                break;
            }
            case "petal": 
            case "launch": 
            case "geyser": {
                double d10 = d2 = string2.equals("PULSE") ? 1.0 : 0.0;
                if (string.equals("petal")) {
                    double d11 = Math.min(1.0, d / 12.0) * 0.65;
                    dArray2 = new double[]{Math.cos(dArray[0]) * d11, -0.05 * Math.min(d / 12.0, 1.0), Math.sin(dArray[0]) * d11};
                    dArray3 = new double[]{Math.sin(dArray[0]) * -d * 1.5, 0.0, Math.cos(dArray[0]) * d * 1.5};
                    break;
                }
                if (string.equals("launch")) {
                    double d12 = 0.05 + dArray[1] * 0.007;
                    dArray2 = new double[]{Math.cos(dArray[0]) * d * d12, Math.max(-0.15, d * (0.2 + dArray[1] * 0.02) - 0.008 * d * d), Math.sin(dArray[0]) * d * d12};
                    dArray3 = new double[]{d * 3.0, d * 2.0, 0.0};
                    break;
                }
                dArray4 = new double[]{1.0, Math.max(0.02, Math.min(1.0, (d + 1.0) / 3.0)) * Math.max(0.05, 1.0 - d / 26.0), 1.0};
                break;
            }
            case "wave": {
                double d13 = (double)(n * 2) - 0.14 * (1.0 - Math.min(1.0, d / 3.0));
                dArray2 = new double[]{Math.cos(dArray[0]) * d13, 0.025 * Math.sin(d * 0.23), Math.sin(dArray[0]) * d13};
                break;
            }
            case "wave_edge": {
                dArray4 = new double[]{(double)(2 + n * 2) / 2.0, 1.0, (double)(2 + n * 2) / 2.0};
                break;
            }
            case "heal": {
                dArray2[1] = 0.025 * Math.sin(d * 0.28 + dArray[0]);
                break;
            }
            case "cleanse": {
                dArray2[1] = Math.min(0.32, d * 0.02);
                d2 = Math.max(0.0, 1.0 - d / 26.0);
                break;
            }
        }
        if (string2.equals("CHARGE")) {
            d2 *= 0.45;
        }
        return new State(dArray2, dArray3, dArray4, d2);
    }

    private static double[] uniform(double d) {
        return new double[]{d, d, d};
    }

    public record State(double[] position, double[] rotation, double[] scale, double alpha) {
    }
}

