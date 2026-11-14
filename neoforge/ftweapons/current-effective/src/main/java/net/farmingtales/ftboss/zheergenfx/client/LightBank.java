package net.farmingtales.ftboss.zheergenfx.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import net.farmingtales.ftboss.zheergenfx.LightClock;

public final class LightBank {
    private static final Map<String, LightBank.Shape> SHAPES = read();

    private LightBank() {
    }

    public static LightBank.Shape shape(int var0, int var1) {
        return SHAPES.get(var0 + "/" + LightClock.variant(var0, var1));
    }

    private static int bounded(DataInputStream var0, int var1, int var2) throws IOException {
        int var3 = var0.readInt();
        if (var3 >= var1 && var3 <= var2) {
            return var3;
        } else {
            throw new IllegalArgumentException("Invalid light bank integer");
        }
    }

    private static Map<String, LightBank.Shape> read() {
        try (InputStream var0 = LightBank.class.getResourceAsStream("/assets/ftboss/fx/zheergen_remaining_lights.bin.gz")) {
            if (var0 == null) {
                throw new IllegalStateException("Missing remaining-skill light bank");
            } else {
                try (DataInputStream var1 = new DataInputStream(new GZIPInputStream(var0))) {
                    if (var1.readInt() == 1514948145 && var1.readInt() == 1 && var1.readInt() == 41) {
                        int var2 = bounded(var1, 13, 32);
                        int[] var3 = new int[3];

                        for (int var4 = 0; var4 < 3; var4++) {
                            var3[var4] = bounded(var1, 0, 16777215);
                        }

                        LinkedHashMap var30 = new LinkedHashMap();
                        long var5 = 0L;

                        for (int var7 = 0; var7 < var2; var7++) {
                            int var8 = bounded(var1, 0, 12);
                            int var9 = bounded(var1, 0, 5);
                            boolean var10 = var1.readBoolean();
                            int var11 = bounded(var1, 1, 24);
                            ArrayList var12 = new ArrayList();
                            int var13 = 0;

                            for (int var14 = 0; var14 < var11; var14++) {
                                int var15 = var3[bounded(var1, 0, 2)];
                                boolean var16 = var1.readBoolean();
                                boolean var17 = var1.readBoolean();
                                int var18 = bounded(var1, 6, 100);
                                var5 += 41L * (long)var18 * 12L;
                                if (var5 > 20000000L) {
                                    throw new IllegalArgumentException("Excessive light bank");
                                }

                                double[][][][] var19 = new double[41][var18][4][3];
                                float[] var20 = new float[41];

                                for (int var21 = 0; var21 < 41; var21++) {
                                    var20[var21] = (float)bounded(var1, 0, 1000000) / 1000000.0F;

                                    for (int var22 = 0; var22 < var18; var22++) {
                                        for (int var23 = 0; var23 < 4; var23++) {
                                            for (int var24 = 0; var24 < 3; var24++) {
                                                var19[var21][var22][var23][var24] = (double)bounded(var1, -16000000, 16000000) / 1000000.0;
                                            }
                                        }
                                    }
                                }

                                var12.add(new LightBank.Part(var15, var16, var17, var20, var19));
                                var13 += var18;
                            }

                            if (var13 > 1200 || var30.put(var8 + "/" + var9, new LightBank.Shape(var10, List.copyOf(var12), var13)) != null) {
                                throw new IllegalArgumentException("Invalid light variant");
                            }
                        }

                        if (var1.read() != -1) {
                            throw new IllegalArgumentException("Trailing bank data");
                        } else {
                            for (int var31 = 0; var31 < 13; var31++) {
                                if (!var30.containsKey(var31 + "/0")) {
                                    throw new IllegalArgumentException("Missing light state");
                                }
                            }

                            return Map.copyOf(var30);
                        }
                    } else {
                        throw new IllegalStateException("Light schema mismatch");
                    }
                }
            }
        } catch (Exception var29) {
            throw new IllegalStateException("Invalid remaining-skill bank", var29);
        }
    }

    public static void point(LightBank.Part var0, double var1, int var3, int var4, double[] var5) {
        double var6 = Math.max(0.0, Math.min(40.0, var1 * 40.0));
        int var8 = (int)var6;
        int var9 = Math.min(40, var8 + 1);
        double var10 = var6 - (double)var8;

        for (int var12 = 0; var12 < 3; var12++) {
            var5[var12] = var0.points[var8][var3][var4][var12] * (1.0 - var10) + var0.points[var9][var3][var4][var12] * var10;
        }
    }

    public static float alpha(LightBank.Part var0, double var1) {
        double var3 = Math.max(0.0, Math.min(40.0, var1 * 40.0));
        int var5 = (int)var3;
        int var6 = Math.min(40, var5 + 1);
        return (float)((double)var0.alpha[var5] * (1.0 - var3 + (double)var5) + (double)var0.alpha[var6] * (var3 - (double)var5));
    }

    public static record Part(int tint, boolean detail, boolean ground, float[] alpha, double[][][][] points) {
    }

    public static record Shape(boolean rangeAnchored, List<LightBank.Part> parts, int faces) {
    }
}
