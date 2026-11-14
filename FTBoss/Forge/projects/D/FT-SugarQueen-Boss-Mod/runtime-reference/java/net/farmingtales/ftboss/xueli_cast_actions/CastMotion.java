/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.xueli_cast_actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CastMotion {
    public static final String[] NAMES = new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    public static final double[][] BASE = new double[][]{{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {-5.0, 2.5, 0.0}, {5.0, 2.5, 0.0}, {-1.9, 12.0, 0.0}, {1.9, 12.0, 0.0}};
    private static final Map<String, Clip> CLIPS = CastMotion.load();

    private CastMotion() {
    }

    private static Map<String, Clip> load() {
        Map map;
        block15: {
            InputStream inputStream = CastMotion.class.getResourceAsStream("/assets/ftboss/xueli_cast_actions/motions.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing Xueli cast motions");
                }
                JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                LinkedHashMap<Object, Clip> linkedHashMap = new LinkedHashMap<Object, Clip>();
                for (JsonElement jsonElement : jsonObject.getAsJsonArray("clips")) {
                    JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                    ArrayList<Key> arrayList = new ArrayList<Key>();
                    double d = -1.0;
                    for (JsonElement jsonElement2 : jsonObject2.getAsJsonArray("keys")) {
                        JsonObject jsonObject3 = jsonElement2.getAsJsonObject();
                        double d2 = jsonObject3.get("phase").getAsDouble();
                        if (!Double.isFinite(d2) || d2 < 0.0 || d2 > 1.0 || d2 <= d) {
                            throw new IllegalArgumentException("Invalid Xueli motion phases");
                        }
                        d = d2;
                        double[][] dArray = new double[6][3];
                        for (int i = 0; i < 6; ++i) {
                            for (int j = 0; j < 3; ++j) {
                                dArray[i][j] = jsonObject3.getAsJsonArray("angles").get(i).getAsJsonArray().get(j).getAsDouble();
                                if (Double.isFinite(dArray[i][j]) && !(Math.abs(dArray[i][j]) > 165.0)) continue;
                                throw new IllegalArgumentException("Unsafe Xueli rotation");
                            }
                        }
                        arrayList.add(new Key(d2, dArray));
                    }
                    String string = jsonObject2.get("id").getAsString();
                    if (((Key)arrayList.get(0)).phase() == 0.0 && ((Key)arrayList.get(arrayList.size() - 1)).phase() == 1.0 && linkedHashMap.put(string, new Clip(string, List.copyOf(arrayList))) == null) continue;
                    throw new IllegalArgumentException("Bad cast clip");
                }
                if (linkedHashMap.size() != 13) {
                    throw new IllegalArgumentException("Expected thirteen Xueli cast clips");
                }
                map = Collections.unmodifiableMap(linkedHashMap);
                if (inputStream == null) break block15;
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
                    throw new IllegalStateException("Xueli cast asset load failed", exception);
                }
            }
            inputStream.close();
        }
        return map;
    }

    public static Map<String, Clip> clips() {
        return CLIPS;
    }

    public static double smooth(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double phase(String string, double d, int n) {
        double d2 = Math.max(0.0, Math.min(1.0, d / (double)Math.max(1, n)));
        return string.equals("charge") ? 0.46 * d2 : (string.equals("hold") ? 0.46 : 0.46 + 0.54 * d2);
    }

    public static double weight(String string, double d, int n) {
        if (!Double.isFinite(d) || d < 0.0 || d >= (double)n || n < 1 || n > 240) {
            return 0.0;
        }
        double d2 = string.equals("charge") || string.equals("hold") ? CastMotion.smooth(d / (double)Math.min(2, n)) : (string.equals("instant") ? CastMotion.smooth(d / (double)Math.min(1, n)) : 1.0);
        return string.equals("charge") ? d2 : d2 * CastMotion.smooth(((double)n - d) / Math.min(4.0, (double)n * 0.5));
    }

    public static State combined(String string, String string2, double d, boolean bl) {
        double[][] dArray = CastMotion.sampleAngles(string, d, bl);
        if (!string.equals(string2) && CLIPS.containsKey(string2)) {
            int n = bl ? 2 : 3;
            dArray[n] = CastMotion.sampleAngles(string2, d, bl)[n];
        }
        return new State(dArray, CastMotion.positions(dArray));
    }

    public static State sample(String string, double d, boolean bl) {
        double[][] dArray = CastMotion.sampleAngles(string, d, bl);
        return new State(dArray, CastMotion.positions(dArray));
    }

    private static double[][] sampleAngles(String string, double d, boolean bl) {
        Key key;
        Clip clip = CLIPS.get(string);
        if (clip == null || !Double.isFinite(d)) {
            throw new IllegalArgumentException("Invalid Xueli cast sample");
        }
        double d2 = Math.max(0.0, Math.min(1.0, d));
        Key key2 = key = clip.keys().get(0);
        Iterator<Key> iterator = clip.keys().iterator();
        while (iterator.hasNext()) {
            Key key3;
            key2 = key3 = iterator.next();
            if (key3.phase() >= d2) break;
            key = key3;
        }
        double d3 = key2.phase() > key.phase() ? CastMotion.smooth((d2 - key.phase()) / (key2.phase() - key.phase())) : 0.0;
        double[][] dArray = new double[6][3];
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 3; ++j) {
                dArray[i][j] = key.angles()[i][j] + (key2.angles()[i][j] - key.angles()[i][j]) * d3;
            }
        }
        if (bl) {
            double[][] dArray2 = new double[6][3];
            int[] nArray = new int[]{0, 1, 3, 2, 5, 4};
            for (int i = 0; i < 6; ++i) {
                for (int j = 0; j < 3; ++j) {
                    dArray2[i][j] = dArray[nArray[i]][j] * (double)(j == 0 ? 1 : -1);
                }
            }
            dArray = dArray2;
        }
        return dArray;
    }

    public static double[] rotate(double[] dArray, double[] dArray2) {
        double d = dArray[0];
        double d2 = dArray[1];
        double d3 = dArray[2];
        double d4 = Math.toRadians(dArray2[0]);
        double d5 = Math.toRadians(dArray2[1]);
        double d6 = Math.toRadians(dArray2[2]);
        double d7 = d2 * Math.cos(d4) - d3 * Math.sin(d4);
        double d8 = d2 * Math.sin(d4) + d3 * Math.cos(d4);
        d2 = d7;
        d3 = d8;
        double d9 = d * Math.cos(d5) + d3 * Math.sin(d5);
        d8 = -d * Math.sin(d5) + d3 * Math.cos(d5);
        d = d9;
        d3 = d8;
        d9 = d * Math.cos(d6) - d2 * Math.sin(d6);
        d7 = d * Math.sin(d6) + d2 * Math.cos(d6);
        return new double[]{d9, d7, d3};
    }

    public static double[][] positions(double[][] dArray) {
        double[][] dArray2 = new double[6][3];
        double[] dArray3 = CastMotion.rotate(new double[]{0.0, -12.0, 0.0}, dArray[1]);
        dArray2[1] = new double[]{dArray3[0], 12.0 + dArray3[1], dArray3[2]};
        for (int n : new int[]{0, 2, 3}) {
            double[] dArray4 = CastMotion.rotate(new double[]{BASE[n][0], BASE[n][1] - 12.0, BASE[n][2]}, dArray[1]);
            dArray2[n] = new double[]{dArray4[0], 12.0 + dArray4[1], dArray4[2]};
        }
        for (int i = 4; i < 6; ++i) {
            double d = -1.7976931348623157E308;
            for (double d2 : new double[]{-2.0, 2.0}) {
                for (double d3 : new double[]{-2.0, 2.0}) {
                    d = Math.max(d, CastMotion.rotate(new double[]{d2, 12.0, d3}, dArray[i])[1]);
                }
            }
            double[] dArray5 = CastMotion.rotate(new double[]{0.0, 12.0, 0.0}, dArray[i]);
            dArray2[i] = new double[]{BASE[i][0] - dArray5[0], 24.0 - d, -dArray5[2]};
        }
        return dArray2;
    }

    public record Key(double phase, double[][] angles) {
    }

    public record Clip(String id, List<Key> keys) {
    }

    public record State(double[][] angles, double[][] positions) {
    }
}

