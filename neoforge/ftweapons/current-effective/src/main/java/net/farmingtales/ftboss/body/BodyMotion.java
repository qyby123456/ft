package net.farmingtales.ftboss.body;

import com.google.gson.Gson;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class BodyMotion {
    private BodyMotion() {
    }

    public static BodyMotion.Library read(String var0) {
        BodyMotion.Library var1 = new Gson().fromJson(var0, BodyMotion.Library.class);
        if (var1 != null
            && "ftboss.xuanlan.body/1".equals(var1.schema)
            && var1.clips != null
            && Arrays.equals((Object[])var1.bones, (Object[])(new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"}))) {
            HashSet var2 = new HashSet();

            for (BodyMotion.Clip var6 : var1.clips) {
                if (!var2.add(var6.id) || var6.sections.length == 0 || var6.keys.length < 2) {
                    throw new IllegalArgumentException("Invalid body clip");
                }

                for (BodyMotion.Section var10 : var6.sections) {
                    if (!Double.isFinite(var10.ticks) || !Double.isFinite(var10.hard) || var10.ticks <= 0.0 || var10.hard <= 0.0) {
                        throw new IllegalArgumentException("Invalid body section");
                    }
                }

                double var28 = -1.0;

                for (BodyMotion.Key var12 : var6.keys) {
                    if (var12.at.length != 2
                        || var12.at[0] != (double)((int)var12.at[0])
                        || var12.at[0] < 0.0
                        || var12.at[0] >= (double)var6.sections.length
                        || var12.at[1] < 0.0
                        || var12.at[1] > 1.0
                        || !Double.isFinite(var12.spread)
                        || !Double.isFinite(var12.shift)
                        || var12.angles.length != 6) {
                        throw new IllegalArgumentException("Invalid body key");
                    }

                    double var13 = var12.at[0] + var12.at[1];
                    if (var13 < var28) {
                        throw new IllegalArgumentException("Unordered body key");
                    }

                    var28 = var13;

                    for (double[] var18 : var12.angles) {
                        if (var18.length != 3) {
                            throw new IllegalArgumentException("Invalid bone");
                        }

                        for (double var22 : var18) {
                            if (!Double.isFinite(var22) || Math.abs(var22) > 180.0) {
                                throw new IllegalArgumentException("Invalid angle");
                            }
                        }
                    }
                }
            }

            var1.index = new HashMap<>();

            for (BodyMotion.Clip var27 : var1.clips) {
                var1.index.put(var27.id, var27);
            }

            return var1;
        } else {
            throw new IllegalArgumentException("Invalid body library");
        }
    }

    public static BodyMotion.Clip clip(BodyMotion.Library var0, String var1) {
        BodyMotion.Clip var2 = var0.index.get(var1);
        if (var2 == null) {
            throw new IllegalArgumentException("Unknown body clip " + var1);
        } else {
            return var2;
        }
    }

    public static double smooth(double var0) {
        double var2 = Math.max(0.0, Math.min(1.0, var0));
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static double[] lengths(BodyMotion.Clip var0, boolean var1, int var2) {
        double[] var3 = new double[var0.sections.length];

        for (int var4 = 0; var4 < var3.length; var4++) {
            var3[var4] = var4 == 0 && var2 > 0 ? (double)var2 : (var1 ? var0.sections[var4].hard : var0.sections[var4].ticks);
        }

        return var3;
    }

    public static BodyMotion.Pose sample(BodyMotion.Clip var0, double var1, boolean var3, int var4, double var5, double var7, boolean var9) {
        double[] var10 = lengths(var0, var3, var4);
        double[] var11 = new double[var10.length + 1];

        for (int var12 = 0; var12 < var10.length; var12++) {
            var11[var12 + 1] = var11[var12] + var10[var12];
        }

        double var34 = var11[var11.length - 1];
        double var14 = var0.loop ? (var1 % var34 + var34) % var34 : Math.max(0.0, Math.min(var34, var1));
        BodyMotion.Key var16 = var0.keys[0];
        BodyMotion.Key var17 = var16;

        for (BodyMotion.Key var21 : var0.keys) {
            double var22 = keyTime(var21, var10, var11);
            if (var22 <= var14) {
                var16 = var21;
            }

            if (var22 >= var14) {
                var17 = var21;
                break;
            }

            var17 = var21;
        }

        double var35 = smooth((var14 - keyTime(var16, var10, var11)) / Math.max(1.0E-6, keyTime(var17, var10, var11) - keyTime(var16, var10, var11)));
        double[][] var36 = new double[6][3];

        for (int var37 = 0; var37 < 6; var37++) {
            for (int var39 = 0; var39 < 3; var39++) {
                var36[var37][var39] = var16.angles[var37][var39] + (var17.angles[var37][var39] - var16.angles[var37][var39]) * var35;
            }
        }

        double var38 = var16.spread + (var17.spread - var16.spread) * var35;
        double var23 = var16.shift + (var17.shift - var16.shift) * var35;
        int var25 = var10.length - 1;

        for (int var26 = 0; var26 < var10.length; var26++) {
            if (var14 < var11[var26 + 1]) {
                var25 = var26;
                break;
            }
        }

        double var40 = var0.id.equals("ready")
            ? 0.0
            : smooth(var14 / Math.min(4.0, var10[0])) * (var0.air ? 1.0 : smooth((var34 - var14) / Math.min(6.0, var10[var10.length - 1])));
        if (var0.pulses > 0 && var25 == (var0.id.equals("flywheel") ? 0 : 1)) {
            double var28 = var0.id.equals("flywheel") ? var10[0] / 6.0 : (var0.id.equals("rail") ? 12.0 : 10.0);
            double var30 = var14 - var11[var25];
            double var32 = Math.max(0.0, Math.sin(Math.PI * Math.min(1.0, (var30 % var28 + var28) % var28 / 4.0))) * var40;
            var36[1][0] = var36[1][0] + (var0.id.equals("ring") ? 2.0 : -1.6) * var32;
            var36[var0.id.equals("flywheel") ? 3 : 2][0] -= 2.5 * var32;
        }

        if (var9) {
            double[] var41 = var36[2];
            var36[2] = var36[3];
            var36[3] = var41;
            double[] var29 = var36[4];
            var36[4] = var36[5];
            var36[5] = var29;

            for (double[] var33 : var36) {
                var33[1] = -var33[1];
                var33[2] = -var33[2];
            }
        }

        return new BodyMotion.Pose(
            var0.id,
            var14,
            var34,
            var25,
            var36,
            var38,
            var23,
            var40,
            var0.aim * var40,
            Math.max(-85.0, Math.min(85.0, var5)),
            Math.max(-180.0, Math.min(180.0, var7)),
            var9,
            var0.air
        );
    }

    private static double keyTime(BodyMotion.Key var0, double[] var1, double[] var2) {
        return var2[(int)var0.at[0]] + var1[(int)var0.at[0]] * var0.at[1];
    }

    public static final class Clip {
        public String id;
        public String title;
        public String meaning;
        public BodyMotion.Section[] sections;
        public BodyMotion.Key[] keys;
        public double aim;
        public boolean air;
        public boolean loop;
        public int pulses;
    }

    public static final class Key {
        public double[] at;
        public double[][] angles;
        public double spread;
        public double shift;
    }

    public static final class Library {
        public String schema;
        public String[] bones;
        public BodyMotion.Clip[] clips;
        private transient Map<String, BodyMotion.Clip> index;
    }

    public static record Pose(
        String id,
        double time,
        double life,
        int section,
        double[][] angles,
        double spread,
        double shift,
        double activity,
        double aim,
        double pitch,
        double yaw,
        boolean mirror,
        boolean air
    ) {
    }

    public static final class Section {
        public String name;
        public double ticks;
        public double hard;
    }
}
