/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.qingyi_basic;

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

public final class BasicMotion {
    public static final String[] NAMES = new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    public static final double[][] PIVOTS = new double[][]{{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {-5.0, 2.5, 0.0}, {5.0, 2.5, 0.0}, {-1.9, 12.0, 0.0}, {1.9, 12.0, 0.0}};
    private static final Map<String, Clip> CLIPS = BasicMotion.load();
    private static final int[] MIRROR = new int[]{0, 1, 3, 2, 5, 4};

    private BasicMotion() {
    }

    private static Map<String, Clip> load() {
        Map map;
        block15: {
            InputStream inputStream = BasicMotion.class.getResourceAsStream("/assets/ftboss/qingyi_basic_actions/motions.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing Qingyi melee motions");
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
                            throw new IllegalArgumentException("Unordered melee keys");
                        }
                        d = d2;
                        double[][] dArray = new double[6][3];
                        for (int i = 0; i < 6; ++i) {
                            for (int j = 0; j < 3; ++j) {
                                dArray[i][j] = jsonObject3.getAsJsonArray("angles").get(i).getAsJsonArray().get(j).getAsDouble();
                                if (Double.isFinite(dArray[i][j]) && !(Math.abs(dArray[i][j]) > 165.0)) continue;
                                throw new IllegalArgumentException("Unsafe melee key");
                            }
                        }
                        arrayList.add(new Key(d2, dArray));
                    }
                    if (((Key)arrayList.get(0)).phase() != 0.0 || ((Key)arrayList.get(arrayList.size() - 1)).phase() != 1.0) {
                        throw new IllegalArgumentException("Incomplete melee clip");
                    }
                    String string = jsonObject2.get("id").getAsString();
                    if (linkedHashMap.put(string, new Clip(string, List.copyOf(arrayList))) == null) continue;
                    throw new IllegalArgumentException("Duplicate melee id");
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
                    throw new IllegalStateException("Cannot load Qingyi melee motions", exception);
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

    public static int preparation(int n) {
        return Math.min(4, Math.max(1, n / 3));
    }

    public static int recovery(int n) {
        return Math.min(10, Math.max(1, n - BasicMotion.preparation(n)));
    }

    public static double phase(boolean bl, double d, int n) {
        double d2 = Math.max(0.0, Math.min(1.0, d / (double)Math.max(1, n)));
        return bl ? 0.33 * d2 : 0.42 + 0.58 * d2;
    }

    public static double weight(boolean bl, double d, int n) {
        if (!Double.isFinite(d) || d < 0.0 || d >= (double)n || n < 1 || n > 40) {
            return 0.0;
        }
        return bl ? BasicMotion.smooth((d + 0.1) / (double)Math.min(2, n)) : BasicMotion.smooth(((double)n - d) / Math.min(3.0, (double)n * 0.4));
    }

    public static State sample(String string, double d, boolean bl) {
        double[][] dArray = BasicMotion.sampleAngles(string, d, bl);
        return new State(dArray, BasicMotion.positions(dArray));
    }

    public static double[][] sampleAngles(String string, double d, boolean bl) {
        Key key;
        Clip clip = CLIPS.get(string);
        if (clip == null || !Double.isFinite(d)) {
            throw new IllegalArgumentException("Invalid melee sample");
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
        double d3 = key2.phase() > key.phase() ? BasicMotion.smooth((d2 - key.phase()) / (key2.phase() - key.phase())) : 0.0;
        double[][] dArray = new double[6][3];
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 3; ++j) {
                int n = bl ? MIRROR[i] : i;
                dArray[i][j] = (key.angles()[n][j] + (key2.angles()[n][j] - key.angles()[n][j]) * d3) * (double)(bl && j != 0 ? -1 : 1);
            }
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

    private static double[] matrix(double[] dArray) {
        double d = Math.toRadians(dArray[0]);
        double d2 = Math.toRadians(dArray[1]);
        double d3 = Math.toRadians(dArray[2]);
        double d4 = Math.cos(d);
        double d5 = Math.sin(d);
        double d6 = Math.cos(d2);
        double d7 = Math.sin(d2);
        double d8 = Math.cos(d3);
        double d9 = Math.sin(d3);
        return new double[]{d8 * d6, d8 * d7 * d5 - d9 * d4, d8 * d7 * d4 + d9 * d5, d9 * d6, d9 * d7 * d5 + d8 * d4, d9 * d7 * d4 - d8 * d5, -d7, d6 * d5, d6 * d4};
    }

    public static double[][] positions(double[][] dArray) {
        return BasicMotion.positions(dArray, true);
    }

    public static double[][] positions(double[][] dArray, boolean bl) {
        int n;
        double[][] dArray2 = new double[6][3];
        double[] dArray3 = BasicMotion.matrix(dArray[1]);
        for (n = 0; n < 4; ++n) {
            double d = PIVOTS[n][0];
            double d2 = PIVOTS[n][1] - 12.0;
            dArray2[n][0] = dArray3[0] * d + dArray3[1] * d2;
            dArray2[n][1] = 12.0 + dArray3[3] * d + dArray3[4] * d2;
            dArray2[n][2] = dArray3[6] * d + dArray3[7] * d2;
        }
        if (bl) {
            for (n = 4; n < 6; ++n) {
                double[] dArray4 = BasicMotion.matrix(dArray[n]);
                double d = 12.0 * dArray4[4] + 2.0 * Math.abs(dArray4[3]) + 2.0 * Math.abs(dArray4[5]);
                dArray2[n][0] = PIVOTS[n][0] - 12.0 * dArray4[1];
                dArray2[n][1] = 24.0 - d;
                dArray2[n][2] = -12.0 * dArray4[7];
            }
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

