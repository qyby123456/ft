/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.xueli_basic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BasicMath {
    public static final String[] NAMES = new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    public static final double[][] BASES = new double[][]{{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {-5.0, 2.5, 0.0}, {5.0, 2.5, 0.0}, {-1.9, 12.0, 0.0}, {1.9, 12.0, 0.0}};
    private static final Map<String, Attack> ATTACKS = BasicMath.load();

    private BasicMath() {
    }

    public static Map<String, Attack> attacks() {
        return ATTACKS;
    }

    private static Map<String, Attack> load() {
        Map map;
        block15: {
            InputStream inputStream = BasicMath.class.getResourceAsStream("/assets/ftboss/xueli_basic/attacks.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing weapon-specific basic motions");
                }
                JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                LinkedHashMap<Object, Attack> linkedHashMap = new LinkedHashMap<Object, Attack>();
                for (JsonElement jsonElement : jsonObject.getAsJsonArray("attacks")) {
                    JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                    ArrayList<Key> arrayList = new ArrayList<Key>();
                    double d = -1.0;
                    for (JsonElement jsonElement2 : jsonObject2.getAsJsonArray("keys")) {
                        JsonObject jsonObject3 = jsonElement2.getAsJsonObject();
                        double d2 = jsonObject3.get("phase").getAsDouble();
                        if (!Double.isFinite(d2) || d2 < 0.0 || d2 > 1.0 || d2 <= d) {
                            throw new IllegalArgumentException("Invalid motion key order");
                        }
                        d = d2;
                        double[][] dArray = new double[6][3];
                        for (int i = 0; i < 6; ++i) {
                            for (int j = 0; j < 3; ++j) {
                                dArray[i][j] = jsonObject3.getAsJsonObject("angles").getAsJsonArray(NAMES[i]).get(j).getAsDouble();
                                if (Double.isFinite(dArray[i][j]) && !(Math.abs(dArray[i][j]) > 165.0)) continue;
                                throw new IllegalArgumentException("Invalid joint rotation");
                            }
                        }
                        arrayList.add(new Key(d2, dArray));
                    }
                    String string = jsonObject2.get("id").getAsString();
                    linkedHashMap.put(string, new Attack(string, jsonObject2.get("weapon").getAsString(), jsonObject2.get("seconds").getAsDouble(), List.copyOf(arrayList)));
                }
                if (linkedHashMap.size() != 6) {
                    throw new IllegalArgumentException("Exactly six basic clips required");
                }
                map = Map.copyOf(linkedHashMap);
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
                    throw new IllegalStateException("Cannot load Xueli basic motion", exception);
                }
            }
            inputStream.close();
        }
        return map;
    }

    public static int wind(int n) {
        return Math.min(5, Math.max(1, n / 3));
    }

    public static int recovery(int n) {
        return Math.max(1, Math.min(12, n - BasicMath.wind(n)));
    }

    public static double smooth(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double timing(boolean bl, double d, int n) {
        return bl ? Math.min(0.4, Math.max(0.0, d) / (double)Math.max(1, n) * 0.4) : Math.min(1.0, 0.4 + Math.max(0.0, d) / (double)Math.max(1, n) * 0.6);
    }

    public static double envelope(boolean bl, double d, int n) {
        if (d < 0.0 || d >= (double)n) {
            return 0.0;
        }
        return bl ? BasicMath.smooth(d / (double)Math.min(2, n)) : 1.0 - BasicMath.smooth((d - (double)n * 0.65) / ((double)n * 0.35));
    }

    public static double[] rotate(double[] dArray, double[] dArray2) {
        double d = dArray[0];
        double d2 = dArray[1];
        double d3 = dArray[2];
        double[] dArray3 = new double[]{Math.toRadians(dArray2[0]), Math.toRadians(dArray2[1]), Math.toRadians(dArray2[2])};
        double d4 = d2 * Math.cos(dArray3[0]) - d3 * Math.sin(dArray3[0]);
        d3 = d2 * Math.sin(dArray3[0]) + d3 * Math.cos(dArray3[0]);
        d2 = d4;
        d4 = d * Math.cos(dArray3[1]) + d3 * Math.sin(dArray3[1]);
        d3 = -d * Math.sin(dArray3[1]) + d3 * Math.cos(dArray3[1]);
        d = d4;
        d4 = d * Math.cos(dArray3[2]) - d2 * Math.sin(dArray3[2]);
        d2 = d * Math.sin(dArray3[2]) + d2 * Math.cos(dArray3[2]);
        d = d4;
        return new double[]{d, d2, d3};
    }

    public static Pose sample(String string, double d, boolean bl) {
        int n;
        Attack attack = ATTACKS.get(string);
        if (attack == null) {
            throw new IllegalArgumentException("Unknown basic attack");
        }
        double d2 = Math.max(0.0, Math.min(1.0, d));
        for (n = 0; n < attack.keys().size() - 1 && attack.keys().get(n).phase() < d2; ++n) {
        }
        Key key = attack.keys().get(Math.max(0, n - 1));
        Key key2 = attack.keys().get(n);
        double d3 = BasicMath.smooth(key.phase() == key2.phase() ? 0.0 : (d2 - key.phase()) / (key2.phase() - key.phase()));
        double[][] dArray = new double[6][3];
        for (int i = 0; i < 6; ++i) {
            int n2 = bl && i >= 2 ? (i % 2 == 0 ? i + 1 : i - 1) : i;
            for (int j = 0; j < 3; ++j) {
                dArray[i][j] = key.angles()[n2][j] + (key2.angles()[n2][j] - key.angles()[n2][j]) * d3;
                if (!bl || j == 0) continue;
                double[] dArray2 = dArray[i];
                int n3 = j;
                dArray2[n3] = dArray2[n3] * -1.0;
            }
        }
        return new Pose(dArray, BasicMath.stance(dArray));
    }

    public static double[][] stance(double[][] dArray) {
        int n;
        double[][] dArray2 = new double[6][3];
        double[] dArray3 = BasicMath.rotate(new double[]{0.0, 12.0, 0.0}, dArray[1]);
        dArray2[1] = new double[]{-dArray3[0], 12.0 - dArray3[1], -dArray3[2]};
        dArray2[0] = (double[])dArray2[1].clone();
        for (n = 2; n < 4; ++n) {
            dArray2[n] = BasicMath.rotate(BASES[n], dArray[1]);
            for (int i = 0; i < 3; ++i) {
                double[] dArray4 = dArray2[n];
                int n2 = i;
                dArray4[n2] = dArray4[n2] + dArray2[1][i];
            }
        }
        for (n = 4; n < 6; ++n) {
            double d = Double.NEGATIVE_INFINITY;
            for (double d2 : new double[]{-2.0, 2.0}) {
                for (double d3 : new double[]{0.0, 12.0}) {
                    for (double d4 : new double[]{-2.0, 2.0}) {
                        d = Math.max(d, BasicMath.rotate(new double[]{d2, d3, d4}, dArray[n])[1]);
                    }
                }
            }
            double[] dArray5 = BasicMath.rotate(new double[]{0.0, 12.0, 0.0}, dArray[n]);
            dArray2[n] = new double[]{BASES[n][0] - dArray5[0], 24.0 - d, BASES[n][2] - dArray5[2]};
        }
        return dArray2;
    }

    public record Key(double phase, double[][] angles) {
    }

    public record Attack(String id, String weapon, double seconds, List<Key> keys) {
    }

    public record Pose(double[][] angles, double[][] positions) {
    }
}

