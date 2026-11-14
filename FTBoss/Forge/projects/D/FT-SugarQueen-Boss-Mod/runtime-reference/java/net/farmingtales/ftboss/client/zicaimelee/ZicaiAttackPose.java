/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 */
package net.farmingtales.ftboss.client.zicaimelee;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public final class ZicaiAttackPose {
    private static final Profile[] PROFILES = ZicaiAttackPose.load();

    private ZicaiAttackPose() {
    }

    private static Profile[] load() {
        Profile[] profileArray;
        InputStream inputStream = ZicaiAttackPose.class.getResourceAsStream("/assets/ftboss/zicai_melee/attacks.json");
        if (inputStream == null) {
            throw new IllegalStateException("Missing independent Zicai melee track");
        }
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        try {
            int n;
            Catalogue catalogue = (Catalogue)new Gson().fromJson((Reader)inputStreamReader, Catalogue.class);
            if (!"zicai-humanoid-melee-set-v2".equals(catalogue.schema) || catalogue.profiles.length != 4) {
                throw new IllegalStateException("Invalid Zicai melee catalogue");
            }
            for (Profile profile : catalogue.profiles) {
                if (!"zicai-humanoid-melee-v1".equals(profile.schema) || profile.bind.length != 6 || profile.keys.length < 2 || profile.keys[0].at != 0.0 || profile.keys[profile.keys.length - 1].at != 1.0) {
                    throw new IllegalStateException("Invalid Zicai melee profile");
                }
                for (n = 0; n < profile.keys.length; ++n) {
                    Key key = profile.keys[n];
                    if (!Double.isFinite(key.at) || key.rotation.length != 6 || n > 0 && key.at <= profile.keys[n - 1].at) {
                        throw new IllegalStateException("Invalid ordered keyframe");
                    }
                    for (double[] dArray : key.rotation) {
                        if (dArray.length == 3 && Double.isFinite(dArray[0]) && Double.isFinite(dArray[1]) && Double.isFinite(dArray[2])) continue;
                        throw new IllegalStateException("Invalid bone rotation");
                    }
                }
            }
            for (Profile profile : catalogue.profiles) {
                profile.slopes = new double[profile.keys.length][6][3];
                for (n = 1; n < profile.keys.length - 1; ++n) {
                    for (int i = 0; i < 6; ++i) {
                        for (int j = 0; j < 3; ++j) {
                            profile.slopes[n][i][j] = ZicaiAttackPose.tangent(profile, n, i, j);
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
                throw new IllegalStateException("Cannot read Zicai melee track", iOException);
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

    private static double[] rotate(double d, double d2, double d3, double[] dArray) {
        double d4 = d2 * Math.cos(dArray[0]) - d3 * Math.sin(dArray[0]);
        d3 = d2 * Math.sin(dArray[0]) + d3 * Math.cos(dArray[0]);
        d2 = d4;
        d4 = d * Math.cos(dArray[1]) + d3 * Math.sin(dArray[1]);
        d3 = -d * Math.sin(dArray[1]) + d3 * Math.cos(dArray[1]);
        d = d4;
        d4 = d * Math.cos(dArray[2]) - d2 * Math.sin(dArray[2]);
        d2 = d * Math.sin(dArray[2]) + d2 * Math.cos(dArray[2]);
        return new double[]{d4, d2, d3};
    }

    private static double soleHeight(double[] dArray) {
        double d = Math.sin(dArray[0]);
        double d2 = Math.cos(dArray[0]);
        double d3 = Math.sin(dArray[1]);
        double d4 = Math.cos(dArray[1]);
        double d5 = Math.sin(dArray[2]);
        double d6 = Math.cos(dArray[2]);
        return 2.0 * Math.abs(d5 * d4) + 12.0 * Math.max(0.0, d5 * d3 * d + d6 * d2) + 2.0 * Math.abs(d5 * d3 * d2 - d6 * d);
    }

    public static Pose sample(double d, double d2, boolean bl, double d3, double d4, double d5, double d6) {
        return ZicaiAttackPose.sample(0, d, d2, bl, d3, d4, d5, d6);
    }

    public static int selectAttack(long l, long l2, long l3) {
        long l4 = (l ^ l2 ^ Long.rotateLeft(l3, 21)) + -7046029254386353131L;
        l4 = (l4 ^ l4 >>> 30) * -4658895280553007687L;
        l4 = (l4 ^ l4 >>> 27) * -7723592293110705685L;
        return (int)((l4 ^ l4 >>> 31) & 3L);
    }

    /*
     * WARNING - void declaration
     */
    public static Pose sample(int n, double d, double d2, boolean bl, double d3, double d4, double d5, double d6) {
        double d7;
        void object;
        int n2;
        if (n < 0 || n >= PROFILES.length) {
            throw new IllegalArgumentException("Unknown Zicai melee track");
        }
        Profile profile = PROFILES[n];
        double d8 = ZicaiAttackPose.clamp(d);
        double d9 = ZicaiAttackPose.clamp(d2);
        double d10 = ZicaiAttackPose.clamp(d4);
        for (n2 = 1; n2 < profile.keys.length - 1 && profile.keys[n2].at < d8; ++n2) {
        }
        Key key = profile.keys[n2 - 1];
        Key key2 = profile.keys[n2];
        double d11 = key2.at - key.at;
        double d12 = ZicaiAttackPose.clamp((d8 - key.at) / d11);
        double d13 = d12 * d12;
        double d14 = d13 * d12;
        double d15 = Math.cos(d3 * 0.6662) * 1.4 * d10;
        double[][] dArrayArray = new double[][]{{d6, d5, 0.0}, {0.0, 0.0, 0.0}, {-0.314159 - d15 * 0.32, 0.0, 0.08}, {d15 * 0.6, 0.0, -0.08}, {d15, 0.0, 0.0}, {-d15, 0.0, 0.0}};
        Pose pose = new Pose();
        boolean i = false;
        while (object < 6) {
            for (int j = 0; j < 3; ++j) {
                double d16 = ((2.0 * d14 - 3.0 * d13 + 1.0) * key.rotation[object][j] + (d14 - 2.0 * d13 + d12) * d11 * profile.slopes[n2 - 1][object][j] + (-2.0 * d14 + 3.0 * d13) * key2.rotation[object][j] + (d14 - d13) * d11 * profile.slopes[n2][object][j]) * Math.PI / 180.0;
                double d17 = object == false ? dArrayArray[0][j] : 0.0;
                d7 = object >= 4 ? dArrayArray[object][j] * d10 : 0.0;
                pose.rotation[object][j] = dArrayArray[object][j] * (1.0 - d9) + (d16 + d17 + d7) * d9;
                pose.position[object][j] = profile.bind[object][j];
            }
            ++object;
        }
        if (bl) {
            for (double[] dArray : pose.rotation) {
                dArray[1] = dArray[1] * -1.0;
                dArray[2] = dArray[2] * -1.0;
            }
            double[] dArray = pose.rotation[0];
            dArray[1] = dArray[1] + 2.0 * d5;
            double[] dArray2 = pose.rotation[2];
            pose.rotation[2] = pose.rotation[3];
            pose.rotation[3] = dArray2;
            double[] dArray3 = pose.rotation[4];
            pose.rotation[4] = pose.rotation[5];
            pose.rotation[5] = dArray3;
        }
        double[] dArray = ZicaiAttackPose.rotate(-5.0, 2.5, 0.0, new double[]{0.0, pose.rotation[1][1], pose.rotation[1][2]});
        pose.position[2] = dArray;
        pose.position[3] = new double[]{-dArray[0], 5.0 - dArray[1], -dArray[2]};
        for (int n3 : new int[]{4, 5}) {
            double[] dArray4 = ZicaiAttackPose.rotate(0.0, 12.0, 0.0, pose.rotation[n3]);
            d7 = d9 * (1.0 - Math.min(1.0, d10 * 2.0));
            double d18 = ZicaiAttackPose.soleHeight(pose.rotation[n3]);
            double[] dArray5 = pose.position[n3];
            dArray5[0] = dArray5[0] - dArray4[0] * d7;
            pose.position[n3][1] = Math.min(pose.position[n3][1] + (12.0 - d18) * d7, 24.0 - d18);
            double[] dArray6 = pose.position[n3];
            dArray6[2] = dArray6[2] - dArray4[2] * d7;
        }
        return pose;
    }

    public static final class Catalogue {
        public String schema;
        public Profile[] profiles;
    }

    public static final class Profile {
        public String schema;
        public double[][] bind;
        public Key[] keys;
        private double[][][] slopes;
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

