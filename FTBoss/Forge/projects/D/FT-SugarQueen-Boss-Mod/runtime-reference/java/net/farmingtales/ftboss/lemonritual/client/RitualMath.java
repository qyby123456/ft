/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 */
package net.farmingtales.ftboss.lemonritual.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;
import net.farmingtales.ftboss.lemonritual.client.RitualAssets;

public final class RitualMath {
    private static final JsonArray UNFOLD = RitualAssets.json("native-motion.json").getAsJsonArray("unfold");
    private static final JsonArray BREATH = RitualAssets.json("native-motion.json").getAsJsonArray("breath");

    private RitualMath() {
    }

    public static double clamp(double d, double d2, double d3) {
        return Math.max(d2, Math.min(d3, d));
    }

    private static double sample(JsonArray jsonArray, double d) {
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        if (d <= jsonObject.get("time").getAsDouble()) {
            return jsonObject.get("value").getAsDouble();
        }
        for (int i = 1; i < jsonArray.size(); ++i) {
            JsonObject jsonObject2 = jsonArray.get(i - 1).getAsJsonObject();
            JsonObject jsonObject3 = jsonArray.get(i).getAsJsonObject();
            if (!(d <= jsonObject3.get("time").getAsDouble())) continue;
            double d2 = RitualMath.clamp((d - jsonObject2.get("time").getAsDouble()) / (jsonObject3.get("time").getAsDouble() - jsonObject2.get("time").getAsDouble()), 0.0, 1.0);
            if (jsonObject3.get("easing").getAsString().equals("easeInOutSine")) {
                d2 = -(Math.cos(Math.PI * d2) - 1.0) / 2.0;
            }
            if (jsonObject3.get("easing").getAsString().equals("easeInElastic")) {
                d2 = d2 == 0.0 || d2 == 1.0 ? d2 : -Math.pow(2.0, 10.0 * d2 - 10.0) * Math.sin((d2 * 10.0 - 10.75) * Math.PI * 2.0 / 3.0);
            }
            return jsonObject2.get("value").getAsDouble() + (jsonObject3.get("value").getAsDouble() - jsonObject2.get("value").getAsDouble()) * d2;
        }
        return jsonArray.get(jsonArray.size() - 1).getAsJsonObject().get("value").getAsDouble();
    }

    public static double[] pose(RitualAssets.Model model, RitualAssets.Bone bone, double d) {
        double d2 = RitualMath.clamp(d / 8.0 - bone.phase() * 0.08, 0.0, 1.0);
        double d3 = RitualMath.clamp(RitualMath.sample(UNFOLD, d2 * 0.72), 0.001, 1.06);
        double d4 = RitualMath.clamp(((double)model.life() - d) / 10.0, 0.0, 1.0);
        double d5 = RitualMath.sample(BREATH, d / 20.0);
        double d6 = d3 * (1.0 + (d5 - 1.0) * 0.35) * (bone.type().equals("seal") ? 1.0 : Math.sqrt(d4));
        double d7 = bone.type().equals("seal") ? 0.0 : (1.0 - d3) * -0.035 + Math.sin(d * 0.13 + bone.phase() * 6.0) * 0.025;
        return new double[]{0.0, d7, 0.0, 0.0, bone.type().equals("seal") ? d * 0.012 : 0.0, 0.0, d6};
    }

    public static double[] move(double[] dArray, double[] dArray2, double[] dArray3) {
        double[] dArray4 = new double[3];
        RitualMath.moveInto(dArray, dArray2, dArray3, dArray4);
        return dArray4;
    }

    public static void moveInto(double[] dArray, double[] dArray2, double[] dArray3, double[] dArray4) {
        double d = (dArray[0] - dArray2[0]) * dArray3[6];
        double d2 = (dArray[1] - dArray2[1]) * dArray3[6];
        double d3 = (dArray[2] - dArray2[2]) * dArray3[6];
        double d4 = Math.cos(dArray3[4]);
        double d5 = Math.sin(dArray3[4]);
        dArray4[0] = d * d4 + d3 * d5 + dArray2[0] + dArray3[0];
        dArray4[1] = d2 + dArray2[1] + dArray3[1];
        dArray4[2] = -d * d5 + d3 * d4 + dArray2[2] + dArray3[2];
    }

    public static float opacity(int n, double d) {
        return (float)(RitualMath.clamp(d / 3.0, 0.0, 1.0) * RitualMath.clamp(((double)n - d) / 10.0, 0.0, 1.0));
    }

    public static double[] stream(double[] dArray, double[] dArray2, double d, int n) {
        double d2 = Math.hypot(dArray2[0] - dArray[0], dArray2[2] - dArray[2]);
        double d3 = Math.sin(d * Math.PI) * Math.min(0.8, d2 * 0.16);
        double d4 = (double)n * 2.399963229728653;
        double[] dArray3 = new double[3];
        for (int i = 0; i < 3; ++i) {
            dArray3[i] = dArray[i] + (dArray2[i] - dArray[i]) * d + (i == 1 ? d3 * 0.5 : Math.cos(d4 + (i == 2 ? 1.5707963267948966 : 0.0)) * d3);
        }
        return dArray3;
    }

    public static float guard(double[][] dArray, double[] dArray2, double[] dArray3, boolean bl, List<double[][]> list) {
        double[] dArray4 = new double[3];
        double[] dArray5 = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        double[] dArray6 = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double[] dArray7 : dArray) {
            for (int i = 0; i < 3; ++i) {
                int n = i;
                dArray4[n] = dArray4[n] + dArray7[i] / (double)dArray.length;
                dArray5[i] = Math.min(dArray5[i], dArray7[i]);
                dArray6[i] = Math.max(dArray6[i], dArray7[i]);
            }
        }
        double d = dArray4[0] - dArray2[0];
        double d2 = dArray4[1] - dArray2[1];
        double d3 = dArray4[2] - dArray2[2];
        double d4 = FirstPersonSpellVisibility.cameraDistance(Math.sqrt(d * d + d2 * d2 + d3 * d3), bl);
        if (d4 < 0.4) {
            return 0.0f;
        }
        double d5 = RitualMath.clamp((d4 - 0.4) / 0.45, 0.0, 1.0);
        double d6 = (d * dArray3[0] + d2 * dArray3[1] + d3 * dArray3[2]) / d4;
        if (bl && d4 < 4.0 && d6 > 0.945) {
            return 0.0f;
        }
        if (bl && d4 < 5.0 && d6 > 0.86) {
            d5 *= 0.2;
        }
        for (double[][] dArray8 : list) {
            if (!(dArray6[0] > dArray8[0][0] + 0.012) || !(dArray5[0] < dArray8[1][0] - 0.012) || !(dArray6[1] > dArray8[0][1] + 0.012) || !(dArray5[1] < dArray8[1][1] - 0.012) || !(dArray6[2] > dArray8[0][2] + 0.012) || !(dArray5[2] < dArray8[1][2] - 0.012)) continue;
            return 0.0f;
        }
        return (float)d5;
    }
}

