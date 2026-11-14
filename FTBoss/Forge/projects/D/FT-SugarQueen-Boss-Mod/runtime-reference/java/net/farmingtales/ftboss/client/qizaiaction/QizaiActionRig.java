/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.client.qizaiaction;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class QizaiActionRig {
    private final Map<String, Clip> clips;

    private QizaiActionRig(Map<String, Clip> map) {
        this.clips = Map.copyOf(map);
    }

    public static QizaiActionRig read(Reader reader) {
        JsonObject jsonObject = JsonParser.parseReader((Reader)reader).getAsJsonObject();
        if (jsonObject.get("format").getAsInt() != 1 || jsonObject.getAsJsonArray("channels").size() != 14) {
            throw new IllegalArgumentException("Unsupported action data");
        }
        LinkedHashMap<String, Clip> linkedHashMap = new LinkedHashMap<String, Clip>();
        for (Map.Entry object : jsonObject.getAsJsonObject("profiles").entrySet()) {
            JsonObject jsonObject2 = ((JsonElement)object.getValue()).getAsJsonObject();
            double d = jsonObject2.get("length").getAsDouble();
            if (!Double.isFinite(d) || d <= 0.0 || d > 20.0) {
                throw new IllegalArgumentException("Invalid clip duration");
            }
            ArrayList<Key> arrayList = new ArrayList<Key>();
            double d2 = -1.0;
            for (JsonElement jsonElement : jsonObject2.getAsJsonArray("keys")) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                if (jsonArray.size() != 15) {
                    throw new IllegalArgumentException("Invalid key width");
                }
                double d3 = jsonArray.get(0).getAsDouble();
                if (!Double.isFinite(d3) || d3 < 0.0 || d3 > 1.0 || d3 <= d2) {
                    throw new IllegalArgumentException("Invalid key ordering");
                }
                double[] dArray = new double[14];
                for (int i = 0; i < 14; ++i) {
                    dArray[i] = jsonArray.get(i + 1).getAsDouble();
                    if (Double.isFinite(dArray[i]) && !(Math.abs(dArray[i]) > 180.0)) continue;
                    throw new IllegalArgumentException("Invalid action value");
                }
                arrayList.add(new Key(d3, dArray));
                d2 = d3;
            }
            if (arrayList.size() < 2 || arrayList.size() > 64 || ((Key)arrayList.get(0)).time() != 0.0 || ((Key)arrayList.get(arrayList.size() - 1)).time() != 1.0) {
                throw new IllegalArgumentException("Incomplete action keys");
            }
            linkedHashMap.put((String)object.getKey(), new Clip(d, List.copyOf(arrayList)));
        }
        for (String string : List.of("ready", "volley_charge", "volley_hold", "volley_release", "nova_charge", "nova_release", "fall_charge", "fall_hold", "fall_release", "prayer_charge", "prayer_hold", "prayer_release", "step_release", "cross_charge", "cross_hold", "cross_release", "refrain_charge", "refrain_hold", "refrain_release", "sweep_charge", "sweep_hold", "sweep_release", "prism_charge", "prism_hold", "prism_release", "barrage_charge", "barrage_hold", "barrage_release")) {
            if (linkedHashMap.containsKey(string)) continue;
            throw new IllegalArgumentException("Missing caster clip: " + string);
        }
        return new QizaiActionRig(linkedHashMap);
    }

    private static double slope(List<Key> list, int n, int n2) {
        double d;
        if (n == 0 || n == list.size() - 1) {
            return 0.0;
        }
        Key key = list.get(n - 1);
        Key key2 = list.get(n);
        Key key3 = list.get(n + 1);
        double d2 = (key2.values()[n2] - key.values()[n2]) / (key2.time() - key.time());
        if (d2 * (d = (key3.values()[n2] - key2.values()[n2]) / (key3.time() - key2.time())) <= 0.0) {
            return 0.0;
        }
        double d3 = key2.time() - key.time();
        double d4 = key3.time() - key2.time();
        double d5 = 2.0 * d4 + d3;
        double d6 = d4 + 2.0 * d3;
        return (d5 + d6) / (d5 / d2 + d6 / d);
    }

    public double[] channels(String string, double d) {
        Clip clip = this.clips.get(string);
        if (clip == null || !Double.isFinite(d)) {
            throw new IllegalArgumentException("Invalid action sample");
        }
        List<Key> list = clip.keys();
        double d2 = Math.max(0.0, Math.min(1.0, d));
        if (d2 == 0.0) {
            return (double[])list.get(0).values().clone();
        }
        if (d2 == 1.0) {
            return (double[])list.get(list.size() - 1).values().clone();
        }
        int n = 1;
        while (list.get(n).time() < d2) {
            ++n;
        }
        Key key = list.get(n - 1);
        Key key2 = list.get(n);
        double d3 = key2.time() - key.time();
        double d4 = (d2 - key.time()) / d3;
        double d5 = d4 * d4;
        double d6 = d5 * d4;
        double[] dArray = new double[14];
        for (int i = 0; i < 14; ++i) {
            dArray[i] = (2.0 * d6 - 3.0 * d5 + 1.0) * key.values()[i] + (d6 - 2.0 * d5 + d4) * d3 * QizaiActionRig.slope(list, n - 1, i) + (-2.0 * d6 + 3.0 * d5) * key2.values()[i] + (d6 - d5) * d3 * QizaiActionRig.slope(list, n, i);
        }
        return dArray;
    }

    public Pose sample(String string, double d, boolean bl, double d2, double d3) {
        return QizaiActionRig.pose(this.channels(string, d), bl, d2, d3);
    }

    public static double[] rotate(double[] dArray, double[] dArray2) {
        double d = dArray[0];
        double d2 = dArray[1];
        double d3 = dArray[2];
        double d4 = dArray2[0];
        double d5 = dArray2[1];
        double d6 = dArray2[2];
        double d7 = d2 * Math.cos(d4) - d3 * Math.sin(d4);
        d3 = d2 * Math.sin(d4) + d3 * Math.cos(d4);
        d2 = d7;
        d7 = d * Math.cos(d5) + d3 * Math.sin(d5);
        d3 = -d * Math.sin(d5) + d3 * Math.cos(d5);
        d = d7;
        return new double[]{d * Math.cos(d6) - d2 * Math.sin(d6), d * Math.sin(d6) + d2 * Math.cos(d6), d3};
    }

    private static double[] angles(double[] dArray, int n) {
        return new double[]{Math.toRadians(dArray[n]), Math.toRadians(dArray[n + 1]), Math.toRadians(dArray[n + 2])};
    }

    private static double[] point(double[] dArray, double[] dArray2, double[] dArray3) {
        double[] dArray4 = QizaiActionRig.rotate(dArray, dArray3);
        for (int i = 0; i < 3; ++i) {
            int n = i;
            dArray4[n] = dArray4[n] + dArray2[i];
        }
        return dArray4;
    }

    private static double[] compose(double[] dArray, double[] dArray2) {
        double[][] dArrayArray = new double[][]{QizaiActionRig.rotate(QizaiActionRig.rotate(new double[]{1.0, 0.0, 0.0}, dArray2), dArray), QizaiActionRig.rotate(QizaiActionRig.rotate(new double[]{0.0, 1.0, 0.0}, dArray2), dArray), QizaiActionRig.rotate(QizaiActionRig.rotate(new double[]{0.0, 0.0, 1.0}, dArray2), dArray)};
        return new double[]{Math.atan2(dArrayArray[1][2], dArrayArray[2][2]), Math.asin(Math.max(-1.0, Math.min(1.0, -dArrayArray[0][2]))), Math.atan2(dArrayArray[0][1], dArrayArray[0][0])};
    }

    public static Pose pose(double[] dArray, boolean bl, double d, double d2) {
        double[] dArray2 = QizaiActionRig.angles(dArray, 0);
        double[] dArray3 = QizaiActionRig.angles(dArray, 3);
        double[] dArray4 = QizaiActionRig.angles(dArray, 6);
        double d3 = dArray[9];
        if (bl) {
            dArray2[1] = dArray2[1] * -1.0;
            dArray2[2] = dArray2[2] * -1.0;
            dArray3[1] = dArray3[1] * -1.0;
            dArray3[2] = dArray3[2] * -1.0;
            dArray4[1] = dArray4[1] * -1.0;
            dArray4[2] = dArray4[2] * -1.0;
            d3 *= -1.0;
        }
        double[][] dArray5 = new double[6][3];
        double[][] dArray6 = new double[6][3];
        double[] dArray7 = new double[]{-dArray[10] / 12.0 + dArray2[2] * 0.2, -dArray[10] / 12.0 - dArray2[2] * 0.2};
        for (int i = 0; i < 2; ++i) {
            double d4 = dArray7[i];
            dArray5[i + 4] = new double[]{i == 0 ? -1.9 : 1.9, 24.0 - 12.0 * Math.cos(d4) - 2.0 * Math.abs(Math.sin(d4)), -12.0 * Math.sin(d4)};
            dArray6[i + 4] = new double[]{d4, 0.0, 0.0};
        }
        double[] dArray8 = new double[]{d3, (dArray5[4][1] + dArray5[5][1]) / 2.0, dArray[10]};
        double[] dArray9 = QizaiActionRig.rotate(new double[]{0.0, 12.0, 0.0}, dArray2);
        double[] dArray10 = new double[3];
        for (int i = 0; i < 3; ++i) {
            dArray10[i] = dArray8[i] - dArray9[i];
        }
        dArray5[0] = (double[])dArray10.clone();
        dArray5[1] = (double[])dArray10.clone();
        dArray6[1] = dArray2;
        dArray6[0] = new double[]{Math.max(-0.65, Math.min(0.45, d2 + Math.toRadians(dArray[11]))), Math.max(-1.35, Math.min(1.35, d + Math.toRadians(dArray[12]) * (double)(bl ? -1 : 1))), Math.toRadians(dArray[13]) * (double)(bl ? -1 : 1)};
        dArray5[2] = QizaiActionRig.point(new double[]{-5.0, 2.5, 0.0}, dArray10, dArray2);
        dArray5[3] = QizaiActionRig.point(new double[]{5.0, 2.5, 0.0}, dArray10, dArray2);
        dArray6[2] = QizaiActionRig.compose(dArray2, bl ? dArray4 : dArray3);
        dArray6[3] = QizaiActionRig.compose(dArray2, bl ? dArray3 : dArray4);
        return new Pose(dArray5, dArray6);
    }

    public record Key(double time, double[] values) {
    }

    public record Clip(double length, List<Key> keys) {
    }

    public record Pose(double[][] position, double[][] rotation) {
    }
}

