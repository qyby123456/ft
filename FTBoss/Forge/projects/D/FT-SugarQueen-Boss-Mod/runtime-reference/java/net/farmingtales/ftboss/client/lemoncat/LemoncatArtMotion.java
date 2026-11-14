/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.client.lemoncat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.client.lemoncat.LemoncatArtAssets;

public final class LemoncatArtMotion {
    private static final Reference NATIVE = LemoncatArtMotion.reference();

    private LemoncatArtMotion() {
    }

    public static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    public static double smooth(double d) {
        double d2 = LemoncatArtMotion.clamp(d);
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    private static Key[] keys(JsonArray jsonArray) {
        Key[] keyArray = new Key[jsonArray.size()];
        for (int i = 0; i < keyArray.length; ++i) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            double[] dArray = new double[3];
            for (int j = 0; j < 3; ++j) {
                dArray[j] = jsonObject.getAsJsonArray("value").get(j).getAsDouble();
            }
            keyArray[i] = new Key(jsonObject.get("seconds").getAsDouble(), dArray, jsonObject.get("easing").getAsString());
        }
        return keyArray;
    }

    private static Reference reference() {
        Reference reference;
        block10: {
            InputStream inputStream = LemoncatArtMotion.class.getResourceAsStream("/assets/ftboss/lemoncat_v3/motion.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing native animation references");
                }
                JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                LinkedHashMap<String, Key[]> linkedHashMap = new LinkedHashMap<String, Key[]>();
                for (Map.Entry entry : jsonObject.getAsJsonObject("formation").entrySet()) {
                    linkedHashMap.put((String)entry.getKey(), LemoncatArtMotion.keys(((JsonElement)entry.getValue()).getAsJsonArray()));
                }
                reference = new Reference(LemoncatArtMotion.keys(jsonObject.getAsJsonArray("strikeRotation")), LemoncatArtMotion.keys(jsonObject.getAsJsonArray("strikePosition")), LemoncatArtMotion.keys(jsonObject.getAsJsonArray("wispBreath")), Map.copyOf(linkedHashMap));
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
                    throw new IllegalStateException("Cannot read native Lemoncat references", exception);
                }
            }
            inputStream.close();
        }
        return reference;
    }

    private static double easing(double d, String string) {
        return switch (string) {
            case "easeInSine" -> 1.0 - Math.cos(d * Math.PI / 2.0);
            case "easeOutQuart" -> 1.0 - Math.pow(1.0 - d, 4.0);
            case "easeInOutSine" -> (1.0 - Math.cos(Math.PI * d)) / 2.0;
            case "easeInOutQuad" -> {
                if (d < 0.5) {
                    yield 2.0 * d * d;
                }
                yield 1.0 - Math.pow(-2.0 * d + 2.0, 2.0) / 2.0;
            }
            case "easeInOutCubic" -> {
                if (d < 0.5) {
                    yield 4.0 * Math.pow(d, 3.0);
                }
                yield 1.0 - Math.pow(-2.0 * d + 2.0, 3.0) / 2.0;
            }
            case "easeInElastic" -> {
                if (d == 0.0) {
                    yield 0.0;
                }
                if (d == 1.0) {
                    yield 1.0;
                }
                yield -Math.pow(2.0, 10.0 * d - 10.0) * Math.sin((d * 10.0 - 10.75) * 2.0 * Math.PI / 3.0);
            }
            default -> d;
        };
    }

    private static double[] channel(Key[] keyArray, double d) {
        if (d <= keyArray[0].seconds()) {
            return keyArray[0].value();
        }
        for (int i = 1; i < keyArray.length; ++i) {
            if (!(d <= keyArray[i].seconds())) continue;
            Key key = keyArray[i];
            Key key2 = keyArray[i - 1];
            double d2 = LemoncatArtMotion.easing((d - key2.seconds()) / (key.seconds() - key2.seconds()), key.easing());
            double[] dArray = new double[3];
            for (int j = 0; j < 3; ++j) {
                dArray[j] = key2.value()[j] + (key.value()[j] - key2.value()[j]) * d2;
            }
            return dArray;
        }
        return keyArray[keyArray.length - 1].value();
    }

    public static double[] pose(int n, double d, boolean bl) {
        double d2 = Math.max(0.0, d);
        double d3 = LemoncatArtMotion.smooth(d2 / 9.0);
        if (n == 201) {
            double d4 = bl ? 0.5 + Math.min(1.0, d2 / 12.0) * 0.75 : Math.min(1.0, d2 / 10.0) * 0.5;
            double[] dArray = LemoncatArtMotion.channel(NATIVE.position(), d4);
            double[] dArray2 = LemoncatArtMotion.channel(NATIVE.rotation(), d4);
            return new double[]{0.0, dArray[1] * 0.005, -dArray[2] * 0.003, dArray2[0] * Math.PI / 180.0 * 0.06, 0.0, 0.0, 1.0};
        }
        if (n >= 202 && n <= 206) {
            double d5 = (double)(n - 202) * 1.2;
            return new double[]{0.012 * Math.sin(d2 * 0.27 + d5) * d3, 0.015 * Math.sin(d2 * 0.31 + d5) * d3, bl ? d3 * 0.075 : 0.0, 0.0, 0.0, 0.0, (bl ? 0.6 : 0.32) + d3 * (bl ? 0.4 : 0.22)};
        }
        if (n == 207) {
            return new double[]{0.0, 0.04 * Math.sin(Math.min(1.0, d2 / 15.0) * Math.PI), d3 * 0.07, 0.0, 0.1 * Math.sin(d2 * 0.13), 0.0, 1.0};
        }
        if (n == 211) {
            double d6 = LemoncatArtMotion.channel(NATIVE.breath(), d2 / 20.0 % 2.0833)[0];
            return new double[]{0.0, (d6 - 1.0) * 0.015, 0.0, 0.0, 0.0, 0.0, 1.0 + (d6 - 1.0) * 0.08};
        }
        if (n >= 212 && n <= 219) {
            double d7 = (double)(n - 212) * 0.39;
            double d8 = LemoncatArtMotion.channel(NATIVE.breath(), (d2 / 20.0 + d7) % 2.0833)[0];
            return new double[]{0.0, (d8 - 1.0) * 0.065, 0.0, 0.0, 0.0, 0.0, 1.0 + (d8 - 1.0) * 0.05};
        }
        if (n >= 231 && n <= 242) {
            int n2 = (n - 231) % 6;
            double d9 = (double)n2 * Math.PI / 3.0;
            Key[] keyArray = NATIVE.formation().get((new String[]{"bone2", "bone3", "bone4", "bone5", "bone4", "bone3"})[n2]);
            double d10 = LemoncatArtMotion.clamp(LemoncatArtMotion.channel(keyArray, Math.min(keyArray[keyArray.length - 1].seconds(), d2 / 12.0 * 1.4))[0]);
            double d11 = bl ? d3 * 0.16 : 0.0;
            return new double[]{Math.cos(d9) * d11, bl ? Math.sin(d3 * Math.PI) * 0.07 : 0.0, Math.sin(d9) * d11, 0.0, 0.0, 0.0, bl ? 1.0 : 0.68 + 0.32 * d10};
        }
        return new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
    }

    public static void move(double[] dArray, double[] dArray2, double[] dArray3, double[] dArray4) {
        int n;
        for (n = 0; n < 3; ++n) {
            dArray4[n] = (dArray[n] - dArray2[n]) * dArray3[6];
        }
        for (n = 0; n < 3; ++n) {
            int n2 = (n + 1) % 3;
            int n3 = (n + 2) % 3;
            double d = dArray4[n2];
            double d2 = dArray4[n3];
            dArray4[n2] = d * Math.cos(dArray3[n + 3]) - d2 * Math.sin(dArray3[n + 3]);
            dArray4[n3] = d * Math.sin(dArray3[n + 3]) + d2 * Math.cos(dArray3[n + 3]);
        }
        for (n = 0; n < 3; ++n) {
            int n4 = n;
            dArray4[n4] = dArray4[n4] + (dArray2[n] + dArray3[n]);
        }
    }

    public static float opacity(LemoncatArtAssets.Sculpture sculpture, int n, double d, boolean bl) {
        double d2 = LemoncatArtMotion.smooth(d / (double)(sculpture.id().equals("searing_pan") ? 4 : 6));
        double d3 = 1.0 - LemoncatArtMotion.smooth((d - (double)(sculpture.life() - 6)) / 6.0);
        if (sculpture.id().equals("searing_pan")) {
            if (n == 201) {
                return (float)(bl ? 1.0 - LemoncatArtMotion.smooth((d - 5.0) / 7.0) : d2 * 0.82);
            }
            return (float)(bl ? d2 * d3 : d2 * 0.35);
        }
        if (!bl) {
            return (float)(d2 * (n == 0 ? 0.7 : 0.55));
        }
        return (float)(d2 * d3);
    }

    public static double[] layout(LemoncatArtAssets.Sculpture sculpture, double d, double d2) {
        return LemoncatArtMotion.layout(sculpture, d, d2, 1.0);
    }

    public static double[] layout(LemoncatArtAssets.Sculpture sculpture, double d, double d2, double d3) {
        d3 = Math.max(0.5, Math.min(3.0, d3));
        double d4 = Math.min(sculpture.scale() * d3, (sculpture.ceiling() - 0.055) / (sculpture.envelopeMax()[1] - sculpture.envelopeMin()[1]));
        d4 = sculpture.anchor().equals("FRONT") ? Math.min(d4, Math.max(0.1, d - Math.max(sculpture.front(), d2 / 2.0 + 0.42) - 0.06) / (sculpture.envelopeMax()[2] - sculpture.envelopeMin()[2])) : Math.min(d4, Math.max(0.4, (d *= d3) - 0.12) / Math.max(Math.max(Math.abs(sculpture.envelopeMin()[0]), Math.abs(sculpture.envelopeMax()[0])), Math.max(Math.abs(sculpture.envelopeMin()[2]), Math.abs(sculpture.envelopeMax()[2]))));
        double d5 = Math.max(0.055 - sculpture.envelopeMin()[1] * d4, Math.min(sculpture.height() - (sculpture.minimum()[1] + sculpture.maximum()[1]) * d4 / 2.0, sculpture.ceiling() - sculpture.envelopeMax()[1] * d4));
        return new double[]{d4, -(sculpture.minimum()[0] + sculpture.maximum()[0]) * d4 / 2.0 + sculpture.lateral(), d5, sculpture.anchor().equals("FRONT") ? Math.max(sculpture.front(), d2 / 2.0 + 0.42) - sculpture.envelopeMin()[2] * d4 : -(sculpture.minimum()[2] + sculpture.maximum()[2]) * d4 / 2.0};
    }

    private record Key(double seconds, double[] value, String easing) {
    }

    private record Reference(Key[] rotation, Key[] position, Key[] breath, Map<String, Key[]> formation) {
    }
}

