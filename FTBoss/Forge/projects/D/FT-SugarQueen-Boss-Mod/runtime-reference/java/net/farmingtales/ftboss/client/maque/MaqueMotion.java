/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.client.maque;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;

public final class MaqueMotion {
    private static final double[][] LEFT_WING = MaqueMotion.nativeFrames("left_wing");
    private static final double[][] RIGHT_WING = MaqueMotion.nativeFrames("right_wing");

    private MaqueMotion() {
    }

    private static double[][] nativeFrames(String string) {
        double[][] dArray;
        block10: {
            InputStream inputStream = MaqueMotion.class.getResourceAsStream("/assets/ftboss/maque/wing_motion.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing native wing timing reference");
                }
                JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                JsonArray jsonArray = jsonObject.getAsJsonArray(string);
                double[][] dArray2 = new double[3][2];
                double d = (jsonArray.get(0).getAsJsonObject().get("angle").getAsDouble() + jsonArray.get(1).getAsJsonObject().get("angle").getAsDouble()) / 2.0;
                double d2 = jsonObject.get("gain").getAsDouble();
                for (int i = 0; i < 3; ++i) {
                    JsonObject jsonObject2 = jsonArray.get(i).getAsJsonObject();
                    dArray2[i][0] = jsonObject2.get("time").getAsDouble();
                    dArray2[i][1] = Math.toRadians((jsonObject2.get("angle").getAsDouble() - d) * d2);
                }
                dArray = dArray2;
                if (inputStream == null) break block10;
            }
            catch (Throwable throwable) {
                try {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception exception) {
                    throw new IllegalStateException("Cannot read native wing timing", exception);
                }
            }
            inputStream.close();
        }
        return dArray;
    }

    public static double wingAngle(int n, double d) {
        double[][] dArray = n == 1 ? LEFT_WING : RIGHT_WING;
        double d2 = Math.max(0.0, d) / 20.0 % dArray[2][0];
        for (int i = 1; i < dArray.length; ++i) {
            if (!(d2 <= dArray[i][0])) continue;
            double d3 = (d2 - dArray[i - 1][0]) / (dArray[i][0] - dArray[i - 1][0]);
            return dArray[i - 1][1] + (dArray[i][1] - dArray[i - 1][1]) * d3;
        }
        return dArray[0][1];
    }

    public static void point(String string, int n, double[] dArray, double d, double[] dArray2, double[] dArray3) {
        double d2;
        double d3;
        double d4;
        System.arraycopy(dArray2, 0, dArray3, 0, 3);
        if (string.equals("wingstep") && n > 0) {
            d4 = 0.0;
            d3 = MaqueMotion.wingAngle(n, d);
            d2 = dArray2[0] - dArray[0];
            double d5 = dArray2[1] - dArray[1];
            double d6 = dArray2[2] - dArray[2];
            double d7 = d2 * Math.cos(d4) - d5 * Math.sin(d4);
            dArray3[0] = dArray[0] + d7 * Math.cos(d3) + d6 * Math.sin(d3);
            dArray3[1] = dArray[1] + d2 * Math.sin(d4) + d5 * Math.cos(d4);
            dArray3[2] = dArray[2] - d7 * Math.sin(d3) + d6 * Math.cos(d3);
        }
        if (string.equals("feather_fan") && n >= 3) {
            d4 = n - 5;
            d3 = Math.max(0.0, Math.min(1.0, d / 18.0));
            d2 = d3 * d3 * 0.64;
            dArray3[0] = dArray3[0] + Math.sin(d4 * 0.22) * d2;
            dArray3[2] = dArray3[2] + Math.cos(d4 * 0.22) * d2;
            dArray3[1] = dArray3[1] + 0.025 * Math.sin(d * 0.24 + d4);
        }
        if (string.equals("feather_refuge")) {
            dArray3[1] = dArray3[1] + 0.035 * Math.sin(d * 0.12);
        }
    }

    public static double bodyVisibility(double[][] dArray, double[] dArray2) {
        double d = 0.0;
        for (int i = 0; i < 3; ++i) {
            double d2 = Double.POSITIVE_INFINITY;
            double d3 = Double.NEGATIVE_INFINITY;
            for (double[] dArray3 : dArray) {
                d2 = Math.min(d2, dArray3[i]);
                d3 = Math.max(d3, dArray3[i]);
            }
            double d4 = Math.max(0.0, Math.max(dArray2[i] - d3, d2 - dArray2[i + 3]));
            d += d4 * d4;
        }
        return MaqueMotion.smooth(Math.sqrt(d) / 0.16);
    }

    public static double cameraVisibility(double[] dArray, double[] dArray2, double[] dArray3, boolean bl) {
        if (bl) {
            return 1.0;
        }
        return FirstPersonSpellVisibility.featherOpacity(dArray, dArray2, dArray3, bl);
    }

    public static boolean intersectsViewRay(double[][] dArray, double[] dArray2, double[] dArray3) {
        if (FirstPersonSpellVisibility.firstPerson()) {
            return false;
        }
        return FirstPersonSpellVisibility.intersectsNearViewRay(dArray, dArray2, dArray3);
    }

    private static boolean rayTriangle(double[] dArray, double[] dArray2, double[] dArray3, double[] dArray4, double[] dArray5) {
        double d = dArray2[0] - dArray[0];
        double d2 = dArray3[2] - dArray[2];
        double d3 = dArray3[1] - dArray[1];
        double d4 = dArray5[1] * d2 - dArray5[2] * d3;
        double d5 = dArray2[1] - dArray[1];
        double d6 = dArray3[0] - dArray[0];
        double d7 = dArray5[2] * d6 - dArray5[0] * d2;
        double d8 = dArray2[2] - dArray[2];
        double d9 = dArray5[0] * d3 - dArray5[1] * d6;
        double d10 = d * d4 + d5 * d7 + d8 * d9;
        if (Math.abs(d10) < 1.0E-8) {
            return false;
        }
        double d11 = dArray4[0] - dArray[0];
        double d12 = dArray4[1] - dArray[1];
        double d13 = dArray4[2] - dArray[2];
        double d14 = (d11 * d4 + d12 * d7 + d13 * d9) / d10;
        if (d14 < 0.0 || d14 > 1.0) {
            return false;
        }
        double d15 = d12 * d8 - d13 * d5;
        double d16 = d13 * d - d11 * d8;
        double d17 = d11 * d5 - d12 * d;
        double d18 = (dArray5[0] * d15 + dArray5[1] * d16 + dArray5[2] * d17) / d10;
        if (d18 < 0.0 || d14 + d18 > 1.0) {
            return false;
        }
        return (d6 * d15 + d3 * d16 + d2 * d17) / d10 > 0.0;
    }

    public static double smooth(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }
}

