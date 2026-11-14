/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 */
package net.farmingtales.ftboss.client.qingyicast;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public final class QingyiCastPose {
    private static final Profile[] PROFILES = QingyiCastPose.load();

    private QingyiCastPose() {
    }

    private static void matrix(double[][] dArray) {
        if (dArray == null || dArray.length != 6) {
            throw new IllegalStateException("Invalid Qingyi bone array");
        }
        for (double[] dArray2 : dArray) {
            if (dArray2 != null && dArray2.length == 3 && Double.isFinite(dArray2[0]) && Double.isFinite(dArray2[1]) && Double.isFinite(dArray2[2])) continue;
            throw new IllegalStateException("Invalid Qingyi bone axes");
        }
    }

    private static Profile[] load() {
        Profile[] profileArray;
        InputStream inputStream = QingyiCastPose.class.getResourceAsStream("/assets/ftboss/qingyi_cast/casts.json");
        if (inputStream == null) {
            throw new IllegalStateException("Missing Qingyi character casts");
        }
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        try {
            Catalogue catalogue = (Catalogue)new Gson().fromJson((Reader)inputStreamReader, Catalogue.class);
            if (catalogue == null || !"qingyi-skill-casts-v1".equals(catalogue.schema) || catalogue.profiles == null || catalogue.profiles.length != 8) {
                throw new IllegalStateException("Invalid Qingyi cast catalogue");
            }
            for (Profile profile : catalogue.profiles) {
                int n;
                QingyiCastPose.matrix(profile.bind);
                if (profile.keys == null || profile.keys.length < 2 || profile.keys[0].at != 0.0 || profile.keys[profile.keys.length - 1].at != 1.0) {
                    throw new IllegalStateException("Incomplete Qingyi keyframes");
                }
                for (n = 0; n < profile.keys.length; ++n) {
                    Key key = profile.keys[n];
                    QingyiCastPose.matrix(key.rotation);
                    if (Double.isFinite(key.at) && (n <= 0 || !(key.at <= profile.keys[n - 1].at))) continue;
                    throw new IllegalStateException("Unordered Qingyi keyframes");
                }
                profile.slopes = new double[profile.keys.length][6][3];
                for (n = 0; n < profile.keys.length; ++n) {
                    for (int i = 0; i < 6; ++i) {
                        for (int j = 0; j < 3; ++j) {
                            profile.slopes[n][i][j] = QingyiCastPose.tangent(profile, n, i, j);
                        }
                    }
                }
            }
            profileArray = catalogue.profiles;
        }
        catch (Throwable throwable) {
            try {
                try {
                    inputStreamReader.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException iOException) {
                throw new IllegalStateException("Cannot read Qingyi keyframes", iOException);
            }
        }
        inputStreamReader.close();
        return profileArray;
    }

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, Double.isFinite(d) ? d : 0.0));
    }

    private static double tangent(Profile profile, int n, int n2, int n3) {
        if (n == 0 || n == profile.keys.length - 1) {
            return 0.0;
        }
        Key key = profile.keys[n - 1];
        Key key2 = profile.keys[n];
        Key key3 = profile.keys[n + 1];
        double d = key2.at - key.at;
        double d2 = (key2.rotation[n2][n3] - key.rotation[n2][n3]) / d;
        double d3 = key3.at - key2.at;
        double d4 = (key3.rotation[n2][n3] - key2.rotation[n2][n3]) / d3;
        if (d2 * d4 <= 0.0) {
            return 0.0;
        }
        double d5 = 2.0 * d3 + d;
        double d6 = d3 + 2.0 * d;
        return (d5 + d6) / (d5 / d2 + d6 / d4);
    }

    private static double[] rotate(double[] dArray, double[] dArray2) {
        double d = dArray[0];
        double d2 = dArray[1];
        double d3 = dArray[2];
        double d4 = d2 * Math.cos(dArray2[0]) - d3 * Math.sin(dArray2[0]);
        d3 = d2 * Math.sin(dArray2[0]) + d3 * Math.cos(dArray2[0]);
        d2 = d4;
        d4 = d * Math.cos(dArray2[1]) + d3 * Math.sin(dArray2[1]);
        d3 = -d * Math.sin(dArray2[1]) + d3 * Math.cos(dArray2[1]);
        d = d4;
        d4 = d * Math.cos(dArray2[2]) - d2 * Math.sin(dArray2[2]);
        d2 = d * Math.sin(dArray2[2]) + d2 * Math.cos(dArray2[2]);
        return new double[]{d4, d2, d3};
    }

    /*
     * WARNING - void declaration
     */
    public static Pose sample(int n, double d, boolean bl, double d2, double d3, double d4, double d5) {
        void object;
        int n2;
        if (n < 0 || n >= PROFILES.length) {
            throw new IllegalArgumentException("Unknown Qingyi cast");
        }
        Profile profile = PROFILES[n];
        double d6 = QingyiCastPose.clamp(d);
        for (n2 = 1; n2 < profile.keys.length - 1 && profile.keys[n2].at < d6; ++n2) {
        }
        Key key = profile.keys[n2 - 1];
        Key key2 = profile.keys[n2];
        double d7 = key2.at - key.at;
        double d8 = QingyiCastPose.clamp((d6 - key.at) / d7);
        double d9 = d8 * d8;
        double d10 = d9 * d8;
        Pose pose = new Pose();
        boolean i = false;
        while (object < 6) {
            for (int j = 0; j < 3; ++j) {
                double d11 = (2.0 * d10 - 3.0 * d9 + 1.0) * key.rotation[object][j] + (d10 - 2.0 * d9 + d8) * d7 * profile.slopes[n2 - 1][object][j] + (-2.0 * d10 + 3.0 * d9) * key2.rotation[object][j] + (d10 - d9) * d7 * profile.slopes[n2][object][j];
                pose.rotation[object][j] = d11 * Math.PI / 180.0;
                pose.position[object][j] = profile.bind[object][j];
            }
            ++object;
        }
        if (bl) {
            for (double[] dArray : pose.rotation) {
                dArray[1] = dArray[1] * -1.0;
                dArray[2] = dArray[2] * -1.0;
            }
            double[] dArray = pose.rotation[2];
            pose.rotation[2] = pose.rotation[3];
            pose.rotation[3] = dArray;
            double[] dArray2 = pose.rotation[4];
            pose.rotation[4] = pose.rotation[5];
            pose.rotation[5] = dArray2;
        }
        double[] dArray = pose.rotation[0];
        dArray[0] = dArray[0] + (Double.isFinite(d5) ? d5 : 0.0);
        double[] dArray3 = pose.rotation[0];
        dArray3[1] = dArray3[1] + (Double.isFinite(d4) ? d4 : 0.0);
        double d12 = Math.cos(Double.isFinite(d3) ? d3 * 0.6662 : 0.0) * 1.4 * QingyiCastPose.clamp(d2);
        double[] dArray4 = pose.rotation[4];
        dArray4[0] = dArray4[0] + d12;
        double[] dArray5 = pose.rotation[5];
        dArray5[0] = dArray5[0] - d12;
        double[] dArray6 = QingyiCastPose.rotate(new double[]{0.0, 12.0, 0.0}, pose.rotation[1]);
        pose.position[1] = new double[]{-dArray6[0], 12.0 - dArray6[1], -dArray6[2]};
        pose.position[0] = (double[])pose.position[1].clone();
        for (int n3 : new int[]{2, 3}) {
            double[] dArray7 = QingyiCastPose.rotate(profile.bind[n3], pose.rotation[1]);
            for (int j = 0; j < 3; ++j) {
                pose.position[n3][j] = dArray7[j] + pose.position[1][j];
            }
        }
        double d13 = 1.0 - Math.min(1.0, QingyiCastPose.clamp(d2) * 2.0);
        for (int j : new int[]{4, 5}) {
            double[] dArray8 = QingyiCastPose.rotate(new double[]{0.0, 12.0, 0.0}, pose.rotation[j]);
            double d14 = Double.NEGATIVE_INFINITY;
            for (int n4 : new int[]{-2, 2}) {
                for (int n5 : new int[]{0, 12}) {
                    for (int n6 : new int[]{-2, 2}) {
                        d14 = Math.max(d14, QingyiCastPose.rotate(new double[]{n4, n5, n6}, pose.rotation[j])[1]);
                    }
                }
            }
            double[] dArray9 = pose.position[j];
            dArray9[0] = dArray9[0] - dArray8[0] * d13;
            pose.position[j][1] = Math.min(pose.position[j][1] + (12.0 - d14) * d13, 24.0 - d14);
            double[] dArray10 = pose.position[j];
            dArray10[2] = dArray10[2] - dArray8[2] * d13;
        }
        return pose;
    }

    public static final class Catalogue {
        public String schema;
        public Profile[] profiles;
    }

    public static final class Profile {
        public String id;
        public double[][] bind;
        public Key[] keys;
        public transient double[][][] slopes;
    }

    public static final class Key {
        public double at;
        public double[][] rotation;
    }

    public static final class Pose {
        public final double[][] rotation = new double[6][3];
        public final double[][] position = new double[6][3];
    }
}

