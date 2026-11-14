/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 */
package net.farmingtales.ftboss.client.wineking;

import org.joml.Matrix4f;

public final class WineSpellMotion {
    private static final double TAU = Math.PI * 2;

    private WineSpellMotion() {
    }

    public static Matrix4f writeRigMatrix(Matrix4f matrix4f, double[] dArray, Pose pose) {
        return matrix4f.identity().translate((float)(dArray[0] + pose.position()[0]), (float)(dArray[1] + pose.position()[1]), (float)(dArray[2] + pose.position()[2])).rotateXYZ((float)pose.rotation()[0], (float)pose.rotation()[1], (float)pose.rotation()[2]).scale((float)pose.scale()).translate((float)(-dArray[0]), (float)(-dArray[1]), (float)(-dArray[2]));
    }

    public static int duration(String string, String string2) {
        if (string2.equals("CHARGE")) {
            return switch (string) {
                case "cinder_breath" -> 23;
                case "intoxicating_toast" -> 10;
                case "vintage_minefield" -> 34;
                default -> 32;
            };
        }
        if (string2.equals("RELEASE")) {
            return string.equals("cask_rupture") ? 16 : (string.equals("vintage_minefield") ? 46 : 12);
        }
        return switch (string) {
            case "cask_rupture" -> 24;
            case "cinder_breath" -> 16;
            case "intoxicating_toast" -> 60;
            case "vintage_minefield" -> 18;
            default -> 32;
        };
    }

    public static Pose pose(String string, double d, double d2, String string2, String string3) {
        d2 = Math.max(0.0, Math.min(1.0, d2));
        if (string.startsWith("mine_")) {
            int n = (int)Math.floor(d / (Math.PI * 2));
            double d3 = d - (double)n * (Math.PI * 2);
            if (!string2.equals("PULSE")) {
                return WineSpellMotion.pose("mine", n, d2, string2, string3);
            }
            if (n != 0) {
                return new Pose(new double[]{0.0, 0.0, 0.0}, new double[]{0.0, 0.0, 0.0}, 1.0, 0.0);
            }
            if (string.equals("mine_warning")) {
                return new Pose(new double[]{-2.5, 0.0, 0.0}, new double[]{0.0, 0.0, 0.0}, 0.05 + 0.95 * d2, 0.48 * Math.sin(d2 * Math.PI));
            }
            Pose pose = WineSpellMotion.pose(string.substring(5), d3, d2, string2, string3);
            return new Pose(new double[]{pose.position()[0] * 0.58 - 2.5, pose.position()[1] * 0.58, pose.position()[2] * 0.58}, pose.rotation(), pose.scale(), pose.alpha());
        }
        double[] dArray = new double[]{0.0, 0.0, 0.0};
        double[] dArray2 = new double[]{0.0, 0.0, 0.0};
        double d4 = 1.0;
        double d5 = 1.0;
        boolean bl = string2.equals("PULSE");
        double d6 = d2 * (double)WineSpellMotion.duration(string3, string2);
        if (string.equals("orbit")) {
            dArray2[1] = d2 * (string2.equals("CHARGE") ? 1.6 : Math.PI * 2);
            dArray[1] = 0.06 * Math.sin(d2 * (Math.PI * 2) + d);
        } else if (string.equals("flow")) {
            dArray2[1] = d2 * (Math.PI * 2) * 0.8;
            d4 = 0.88 + 0.12 * Math.sin(d2 * Math.PI);
            d5 = 0.55;
        } else if ((string.equals("stave") || string.equals("cap") || string.equals("hoop")) && bl) {
            double d7 = Math.sin(d2 * Math.PI / 2.0);
            if (string.equals("stave")) {
                dArray = new double[]{Math.cos(d) * d7 * 1.3, d7 * 0.7 - d2 * d2 * 0.6, Math.sin(d) * d7 * 1.3};
                dArray2 = new double[]{d7 * 0.5, d7 * d * 0.2, d7 * 0.35};
            } else {
                dArray[1] = d7 * (0.6 + d * 0.4);
                dArray2[2] = d2 * (d + 1.0) * 1.1;
            }
            d5 = Math.max(0.0, 1.0 - d2 * 1.1);
        } else if (string.equals("shock")) {
            d4 = bl ? 0.1 + 4.9 * d2 : 1.0;
            d5 = bl ? Math.sin(d2 * Math.PI) * 0.48 : 0.0;
        } else if (string.equals("spark")) {
            dArray[1] = 0.35 * Math.sin(d2 * Math.PI + d);
            dArray2[1] = d2 * (Math.PI * 2);
            d5 = 0.65 * Math.sin(d2 * Math.PI);
        } else if (string.equals("flame")) {
            d5 = (string2.equals("CHARGE") ? 0.22 : 0.65) * Math.sin(Math.min(0.99, Math.max(0.01, d2)) * Math.PI);
            d4 = string2.equals("CHARGE") ? 0.2 + 0.07 * d2 : 0.94 + 0.06 * Math.sin(d2 * (Math.PI * 2) + d);
            dArray[1] = 0.035 * Math.sin(d2 * (Math.PI * 2) + d);
        } else if (string.equals("chalice")) {
            dArray[1] = 0.07 * Math.sin(d2 * (Math.PI * 2));
            dArray2[2] = -0.12 * Math.sin(d2 * Math.PI);
        } else if (string.equals("toastlight")) {
            dArray2[1] = d2 * (Math.PI * 2) * 0.45;
            dArray[1] = d2 * 0.25;
            d5 = 0.6;
        } else if (string.equals("mine")) {
            if (bl) {
                dArray = new double[]{-2.5, 0.32 * Math.sin(d2 * Math.PI), 0.0};
                d4 = 1.0 + d2 * 0.8;
                d5 = d == 0.0 ? 1.0 - d2 : 0.0;
            } else if (string2.equals("RELEASE")) {
                d5 = d6 < 20.0 + d * 12.0 ? 1.0 : 0.0;
            } else {
                dArray[1] = 0.015 * Math.sin(d6 * 0.2 + d);
            }
        } else if (string.equals("fuse")) {
            d5 = 0.45 + 0.4 * Math.pow(Math.sin(d6 * 0.8), 2.0);
            if (bl) {
                dArray[1] = Math.sin(d2 * Math.PI / 2.0);
                d5 *= Math.max(0.0, 1.0 - d2 * 5.0);
            }
        } else if (string.equals("warning")) {
            double d8 = d5 = bl ? Math.max(0.0, 1.0 - d2 * 4.0) * 0.7 : 0.7;
        }
        if (!(string2.equals("CHARGE") || string.equals("mine") || string.equals("shock") || string.equals("stave") || string.equals("cap") || string.equals("hoop"))) {
            d5 *= Math.min(1.0, (1.0 - d2) * 6.0);
        }
        return new Pose(dArray, dArray2, d4, Math.max(0.0, d5));
    }

    public static double viewAlpha(double d, double d2, boolean bl, boolean bl2) {
        if (bl2) {
            return 1.0;
        }
        if (d < 0.28) {
            return 0.0;
        }
        if (!bl2 || d2 < 0.94 || d > 10.0) {
            return 1.0;
        }
        double d3 = Math.max(0.0, Math.min(1.0, (1.0 - d2) / 0.06));
        return bl ? d3 * d3 : 0.12 + 0.88 * d3;
    }

    public static boolean replace(String string, String string2, String string3, double d) {
        if (string2.equals("RELEASE")) {
            return true;
        }
        if (string.equals("CHARGE")) {
            return true;
        }
        if (string.equals("RELEASE")) {
            return !string3.equals("vintage_minefield");
        }
        return string.equals(string2) && (!string3.equals("vintage_minefield") || d < 0.25);
    }

    public static double headingYaw(String string, double d, double d2) {
        return string.equals("vintage_minefield") ? 0.0 : Math.atan2(d, d2);
    }

    public static BeamAnchor breathAnchor(double[] dArray, double[] dArray2) {
        double d = dArray2[0] - dArray[0];
        double d2 = dArray2[2] - dArray[2];
        double d3 = Math.hypot(d, d2);
        if (d3 < 0.001 || d3 > 7.001) {
            return null;
        }
        double d4 = d3 / 0.7;
        double d5 = d2 / d3;
        double d6 = -d / d3;
        return new BeamAnchor(new double[]{(dArray[0] + dArray2[0]) * 0.5 - d5 * d4, (dArray[1] + dArray2[1]) * 0.5 - 0.8, (dArray[2] + dArray2[2]) * 0.5 - d6 * d4}, Math.atan2(d5, d6));
    }

    public record Pose(double[] position, double[] rotation, double scale, double alpha) {
    }

    public record BeamAnchor(double[] position, double yaw) {
    }
}

