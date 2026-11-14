/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.client.xueli_run;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RunMath {
    public static final String[] NAMES = new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    private static final Map<String, Clip> CLIPS = new LinkedHashMap<String, Clip>();
    public static final double START;
    public static final double FULL;
    public static final double MAX_STEP;
    public static final double BLEND_TICKS;
    public static final double PHASE_FACTOR;

    private RunMath() {
    }

    public static Map<String, Clip> clips() {
        return Collections.unmodifiableMap(CLIPS);
    }

    public static double activation(double d) {
        if (!Double.isFinite(d) || d < 0.0 || d > MAX_STEP) {
            return 0.0;
        }
        double d2 = Math.max(0.0, Math.min(1.0, (d - START) / (FULL - START)));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double smooth(double d, double d2, double d3) {
        return d + (d2 - d) * (1.0 - Math.exp(-Math.max(0.0, Math.min(4.0, d3)) / BLEND_TICKS));
    }

    public static boolean eligible(int n, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, boolean bl7, double d, float f) {
        return n == 10 && bl && !bl2 && bl3 && !bl4 && !bl5 && !bl6 && !bl7 && Double.isFinite(d) && d <= MAX_STEP && Float.isFinite(f) && (double)Math.abs(f) > 0.02;
    }

    public static double[] rotate(double[] dArray, double[] dArray2) {
        double d = Math.toRadians(dArray2[0]);
        double d2 = Math.toRadians(dArray2[1]);
        double d3 = Math.toRadians(dArray2[2]);
        double d4 = dArray[1] * Math.cos(d) - dArray[2] * Math.sin(d);
        double d5 = dArray[1] * Math.sin(d) + dArray[2] * Math.cos(d);
        double d6 = dArray[0] * Math.cos(d2) + d5 * Math.sin(d2);
        d5 = -dArray[0] * Math.sin(d2) + d5 * Math.cos(d2);
        return new double[]{d6 * Math.cos(d3) - d4 * Math.sin(d3), d6 * Math.sin(d3) + d4 * Math.cos(d3), d5};
    }

    public static double[][] positions(double[][] dArray, double d, double d2) {
        double d3 = Math.sin(d * Math.PI * 2.0);
        double d4 = -100.0;
        for (int n : new int[]{4, 5}) {
            for (double d9 : new double[]{-2.0, 2.0}) {
                for (double d5 : new double[]{0.0, 12.0}) {
                    for (double d6 : new double[]{-2.0, 2.0}) {
                        d4 = Math.max(d4, RunMath.rotate(new double[]{d9, d5, d6}, dArray[n])[1]);
                    }
                }
            }
        }
        double d8 = 24.0 - d4;
        double[][] dArray2 = new double[6][3];
        dArray2[4] = new double[]{-1.9, d8 - Math.max(0.0, d3) * 0.85 * d2, 0.0};
        dArray2[5] = new double[]{1.9, d8 - Math.max(0.0, -d3) * 0.85 * d2, 0.0};
        double[] dArray3 = RunMath.rotate(new double[]{0.0, -12.0, 0.0}, dArray[1]);
        dArray2[1] = new double[]{dArray3[0], d8 + dArray3[1], dArray3[2]};
        dArray2[0] = (double[])dArray2[1].clone();
        for (int n : new int[]{2, 3}) {
            double[] dArray4 = RunMath.rotate(new double[]{n == 2 ? -5.0 : 5.0, -9.5, 0.0}, dArray[1]);
            dArray2[n] = new double[]{dArray4[0], d8 + dArray4[1], dArray4[2]};
        }
        return dArray2;
    }

    public static State sample(String string, double d, boolean bl, double d2) {
        Clip clip = CLIPS.get(string);
        if (clip == null) {
            throw new IllegalArgumentException("Unknown running clip: " + string);
        }
        double d3 = Math.sin(d * Math.PI * 2.0);
        double d4 = 9.0 + 3.0 * d2 + 1.4 * Math.cos(d * Math.PI * 4.0);
        double d5 = (30.0 + 7.0 * d2) * d3;
        int n = bl ? 3 : 2;
        int n2 = bl ? 2 : 3;
        int n3 = bl ? -1 : 1;
        double[][] dArrayArray = new double[][]{{-d4 * 0.3, 0.0, -d3}, {d4, 2.6 * d3, 1.6 * d3}, {-12.0 + 25.0 * d3, 0.0, 9.0}, {-12.0 - 25.0 * d3, 0.0, -9.0}, {-d5, 0.0, 0.0}, {d5, 0.0, 0.0}};
        dArrayArray[n] = new double[]{clip.pitch() + (double)n3 * clip.swing() * d3, -n3 * 7, (double)n3 * clip.roll()};
        if (string.equals("empty")) {
            dArrayArray[n2] = new double[]{clip.pitch() - (double)n3 * clip.swing() * d3, n3 * 7, (double)(-n3) * clip.roll()};
        }
        return new State(dArrayArray, RunMath.positions(dArrayArray, d, 1.0));
    }

    static {
        try (InputStream inputStream = RunMath.class.getResourceAsStream("/assets/ftboss/xueli_run/gait.json");){
            if (inputStream == null) {
                throw new IllegalStateException("Missing authored Xueli running resource");
            }
            JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
            START = jsonObject.get("startSpeed").getAsDouble();
            FULL = jsonObject.get("fullSpeed").getAsDouble();
            MAX_STEP = jsonObject.get("maxTrustedStep").getAsDouble();
            BLEND_TICKS = jsonObject.get("blendTicks").getAsDouble();
            PHASE_FACTOR = jsonObject.get("phaseFactor").getAsDouble();
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("clips")) {
                JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                String string = jsonObject2.get("id").getAsString();
                CLIPS.put(string, new Clip(string, jsonObject2.get("carryPitch").getAsDouble(), jsonObject2.get("carrySwing").getAsDouble(), jsonObject2.get("carryRoll").getAsDouble()));
            }
            if (!(START > 0.0 && FULL > START && MAX_STEP > FULL && BLEND_TICKS > 0.0 && CLIPS.size() == 3)) {
                throw new IllegalStateException("Invalid authored running configuration");
            }
        }
        catch (Exception exception) {
            throw new IllegalStateException("Cannot load Xueli running motion", exception);
        }
    }

    public record Clip(String id, double pitch, double swing, double roll) {
    }

    public record State(double[][] angles, double[][] positions) {
    }
}

