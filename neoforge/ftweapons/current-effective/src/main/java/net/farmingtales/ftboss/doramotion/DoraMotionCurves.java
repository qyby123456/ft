package net.farmingtales.ftboss.doramotion;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public final class DoraMotionCurves {
    public static final String[] BONES = new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    public static final String[] IDS = new String[]{
        "ready", "suppress", "sniper_charge", "sniper_fire", "spear", "reload", "arcane_fusillade", "breach_shell", "prism_battery"
    };
    private final Map<String, DoraMotionCurves.Clip> clips = new HashMap<>();

    public DoraMotionCurves(String var1) {
        DoraMotionCurves.Library var2 = new Gson().fromJson(var1, DoraMotionCurves.Library.class);
        if (var2 != null && var2.version == 1 && var2.clips != null && var2.clips.length == IDS.length) {
            DoraMotionCurves.Clip[] var3 = var2.clips;
            int var4 = var3.length;
            int var5 = 0;

            while (var5 < var4) {
                DoraMotionCurves.Clip var6 = var3[var5];
                if (var6.id != null && var6.ticks >= 1 && var6.keys != null && var6.keys.length >= 2 && this.clips.put(var6.id, var6) == null) {
                    double var7 = -1.0;

                    for (DoraMotionCurves.Key var12 : var6.keys) {
                        if (!Double.isFinite(var12.time)
                            || var12.time <= var7
                            || var12.time < 0.0
                            || var12.time > 1.0
                            || var12.rotation == null
                            || var12.rotation.length != 6) {
                            throw new IllegalArgumentException("Dora key time");
                        }

                        for (double[] var16 : var12.rotation) {
                            if (var16 == null || var16.length != 3) {
                                throw new IllegalArgumentException("Dora skeleton");
                            }

                            for (double var20 : var16) {
                                if (!Double.isFinite(var20) || Math.abs(var20) > 180.0) {
                                    throw new IllegalArgumentException("Dora angle");
                                }
                            }
                        }

                        var7 = var12.time;
                    }

                    if (var6.keys[0].time == 0.0 && var7 == 1.0) {
                        var5++;
                        continue;
                    }

                    throw new IllegalArgumentException("Dora key endpoints");
                }

                throw new IllegalArgumentException("Dora clip");
            }

            for (String var25 : IDS) {
                if (!this.clips.containsKey(var25)) {
                    throw new IllegalArgumentException("Missing Dora clip " + var25);
                }
            }
        } else {
            throw new IllegalArgumentException("Dora motion inventory");
        }
    }

    public static double smooth(double var0) {
        double var2 = Math.max(0.0, Math.min(1.0, var0));
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public double[][] sample(String var1, double var2, boolean var4) {
        DoraMotionCurves.Clip var5 = this.clips.get(var1);
        if (var5 != null && Double.isFinite(var2)) {
            double var6 = Math.max(0.0, Math.min(1.0, var2));
            int var8 = 0;

            while (var8 + 1 < var5.keys.length - 1 && var5.keys[var8 + 1].time < var6) {
                var8++;
            }

            DoraMotionCurves.Key var9 = var5.keys[var8];
            DoraMotionCurves.Key var10 = var5.keys[var8 + 1];
            double var11 = smooth((var6 - var9.time) / (var10.time - var9.time));
            double[][] var13 = new double[6][3];

            for (int var14 = 0; var14 < 6; var14++) {
                int var15 = var4 && var14 >= 2 ? (var14 % 2 == 0 ? var14 + 1 : var14 - 1) : var14;

                for (int var16 = 0; var16 < 3; var16++) {
                    var13[var15][var16] = (var9.rotation[var14][var16] + (var10.rotation[var14][var16] - var9.rotation[var14][var16]) * var11)
                        * (double)(var4 && var16 > 0 ? -1 : 1);
                }
            }

            return var13;
        } else {
            throw new IllegalArgumentException("Invalid Dora sample");
        }
    }

    public static void blend(double[][] var0, double[][] var1, double var2) {
        double var4 = Math.max(0.0, Math.min(1.0, var2));

        for (int var6 = 0; var6 < 6; var6++) {
            for (int var7 = 0; var7 < 3; var7++) {
                var0[var6][var7] = var0[var6][var7] + (var1[var6][var7] - var0[var6][var7]) * var4;
            }
        }
    }

    public static final class Clip {
        public String id;
        public String label;
        public int ticks;
        public DoraMotionCurves.Key[] keys;
    }

    public static final class Key {
        public double time;
        public double[][] rotation;
    }

    public static final class Library {
        public int version;
        public DoraMotionCurves.Clip[] clips;
    }
}
