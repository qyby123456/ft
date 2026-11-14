/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.zheergenfx.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import net.farmingtales.ftboss.zheergenfx.LightClock;

public final class LightBank {
    private static final Map<String, Shape> SHAPES = LightBank.read();

    private LightBank() {
    }

    public static Shape shape(int n, int n2) {
        return SHAPES.get(n + "/" + LightClock.variant(n, n2));
    }

    private static int bounded(DataInputStream dataInputStream, int n, int n2) throws IOException {
        int n3 = dataInputStream.readInt();
        if (n3 < n || n3 > n2) {
            throw new IllegalArgumentException("Invalid light bank integer");
        }
        return n3;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static Map<String, Shape> read() {
        try (InputStream inputStream = LightBank.class.getResourceAsStream("/assets/ftboss/fx/zheergen_remaining_lights.bin.gz");){
            Map<String, Shape> map;
            if (inputStream == null) {
                throw new IllegalStateException("Missing remaining-skill light bank");
            }
            try (DataInputStream dataInputStream = new DataInputStream(new GZIPInputStream(inputStream));){
                int n;
                if (dataInputStream.readInt() != 1514948145 || dataInputStream.readInt() != 1 || dataInputStream.readInt() != 41) {
                    throw new IllegalStateException("Light schema mismatch");
                }
                int n2 = LightBank.bounded(dataInputStream, 13, 32);
                int[] nArray = new int[3];
                for (int i = 0; i < 3; ++i) {
                    nArray[i] = LightBank.bounded(dataInputStream, 0, 0xFFFFFF);
                }
                LinkedHashMap<CallSite, Shape> linkedHashMap = new LinkedHashMap<CallSite, Shape>();
                long l = 0L;
                for (n = 0; n < n2; ++n) {
                    int n3 = LightBank.bounded(dataInputStream, 0, 12);
                    int n4 = LightBank.bounded(dataInputStream, 0, 5);
                    boolean bl = dataInputStream.readBoolean();
                    int n5 = LightBank.bounded(dataInputStream, 1, 24);
                    ArrayList<Part> arrayList = new ArrayList<Part>();
                    int n6 = 0;
                    for (int i = 0; i < n5; ++i) {
                        int n7 = nArray[LightBank.bounded(dataInputStream, 0, 2)];
                        boolean bl2 = dataInputStream.readBoolean();
                        boolean bl3 = dataInputStream.readBoolean();
                        int n8 = LightBank.bounded(dataInputStream, 6, 100);
                        if ((l += 41L * (long)n8 * 12L) > 20000000L) {
                            throw new IllegalArgumentException("Excessive light bank");
                        }
                        double[][][][] dArray = new double[41][n8][4][3];
                        float[] fArray = new float[41];
                        for (int j = 0; j < 41; ++j) {
                            fArray[j] = (float)LightBank.bounded(dataInputStream, 0, 1000000) / 1000000.0f;
                            for (int k = 0; k < n8; ++k) {
                                for (int i2 = 0; i2 < 4; ++i2) {
                                    for (int i3 = 0; i3 < 3; ++i3) {
                                        dArray[j][k][i2][i3] = (double)LightBank.bounded(dataInputStream, -16000000, 16000000) / 1000000.0;
                                    }
                                }
                            }
                        }
                        arrayList.add(new Part(n7, bl2, bl3, fArray, dArray));
                        n6 += n8;
                    }
                    if (n6 <= 1200 && linkedHashMap.put((CallSite)((Object)(n3 + "/" + n4)), new Shape(bl, List.copyOf(arrayList), n6)) == null) continue;
                    throw new IllegalArgumentException("Invalid light variant");
                }
                if (dataInputStream.read() != -1) {
                    throw new IllegalArgumentException("Trailing bank data");
                }
                for (n = 0; n < 13; ++n) {
                    if (linkedHashMap.containsKey(n + "/0")) continue;
                    throw new IllegalArgumentException("Missing light state");
                }
                map = Map.copyOf(linkedHashMap);
            }
            return map;
        }
        catch (Exception exception) {
            throw new IllegalStateException("Invalid remaining-skill bank", exception);
        }
    }

    public static void point(Part part, double d, int n, int n2, double[] dArray) {
        double d2 = Math.max(0.0, Math.min(40.0, d * 40.0));
        int n3 = (int)d2;
        int n4 = Math.min(40, n3 + 1);
        double d3 = d2 - (double)n3;
        for (int i = 0; i < 3; ++i) {
            dArray[i] = part.points[n3][n][n2][i] * (1.0 - d3) + part.points[n4][n][n2][i] * d3;
        }
    }

    public static float alpha(Part part, double d) {
        double d2 = Math.max(0.0, Math.min(40.0, d * 40.0));
        int n = (int)d2;
        int n2 = Math.min(40, n + 1);
        return (float)((double)part.alpha[n] * (1.0 - d2 + (double)n) + (double)part.alpha[n2] * (d2 - (double)n));
    }

    public record Shape(boolean rangeAnchored, List<Part> parts, int faces) {
    }

    public record Part(int tint, boolean detail, boolean ground, float[] alpha, double[][][][] points) {
    }
}

