/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 */
package net.farmingtales.ftboss.body;

import com.google.gson.Gson;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class BodyMotion {
    private BodyMotion() {
    }

    public static Library read(String string) {
        Library library = (Library)new Gson().fromJson(string, Library.class);
        if (library == null || !"ftboss.xuanlan.body/1".equals(library.schema) || library.clips == null || !Arrays.equals(library.bones, new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"})) {
            throw new IllegalArgumentException("Invalid body library");
        }
        HashSet<String> hashSet = new HashSet<String>();
        for (Clip clip : library.clips) {
            if (!hashSet.add(clip.id) || clip.sections.length == 0 || clip.keys.length < 2) {
                throw new IllegalArgumentException("Invalid body clip");
            }
            for (Section section : clip.sections) {
                if (Double.isFinite(section.ticks) && Double.isFinite(section.hard) && !(section.ticks <= 0.0) && !(section.hard <= 0.0)) continue;
                throw new IllegalArgumentException("Invalid body section");
            }
            double d = -1.0;
            for (Key key : clip.keys) {
                if (key.at.length != 2 || key.at[0] != (double)((int)key.at[0]) || key.at[0] < 0.0 || key.at[0] >= (double)clip.sections.length || key.at[1] < 0.0 || key.at[1] > 1.0 || !Double.isFinite(key.spread) || !Double.isFinite(key.shift) || key.angles.length != 6) {
                    throw new IllegalArgumentException("Invalid body key");
                }
                double d2 = key.at[0] + key.at[1];
                if (d2 < d) {
                    throw new IllegalArgumentException("Unordered body key");
                }
                d = d2;
                for (double[] dArray : key.angles) {
                    if (dArray.length != 3) {
                        throw new IllegalArgumentException("Invalid bone");
                    }
                    for (double d3 : dArray) {
                        if (Double.isFinite(d3) && !(Math.abs(d3) > 180.0)) continue;
                        throw new IllegalArgumentException("Invalid angle");
                    }
                }
            }
        }
        library.index = new HashMap<String, Clip>();
        for (Clip clip : library.clips) {
            library.index.put(clip.id, clip);
        }
        return library;
    }

    public static Clip clip(Library library, String string) {
        Clip clip = library.index.get(string);
        if (clip == null) {
            throw new IllegalArgumentException("Unknown body clip " + string);
        }
        return clip;
    }

    public static double smooth(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double[] lengths(Clip clip, boolean bl, int n) {
        double[] dArray = new double[clip.sections.length];
        for (int i = 0; i < dArray.length; ++i) {
            dArray[i] = i == 0 && n > 0 ? (double)n : (bl ? clip.sections[i].hard : clip.sections[i].ticks);
        }
        return dArray;
    }

    public static Pose sample(Clip clip, double d, boolean bl, int n, double d2, double d3, boolean bl2) {
        double d4;
        Key key;
        double[] dArray = BodyMotion.lengths(clip, bl, n);
        double[] dArray2 = new double[dArray.length + 1];
        for (int i = 0; i < dArray.length; ++i) {
            dArray2[i + 1] = dArray2[i] + dArray[i];
        }
        double d5 = dArray2[dArray2.length - 1];
        double d6 = clip.loop ? (d % d5 + d5) % d5 : Math.max(0.0, Math.min(d5, d));
        Key key2 = key = clip.keys[0];
        for (Key key3 : clip.keys) {
            double d7 = BodyMotion.keyTime(key3, dArray, dArray2);
            if (d7 <= d6) {
                key = key3;
            }
            if (d7 >= d6) {
                key2 = key3;
                break;
            }
            key2 = key3;
        }
        double d8 = BodyMotion.smooth((d6 - BodyMotion.keyTime(key, dArray, dArray2)) / Math.max(1.0E-6, BodyMotion.keyTime(key2, dArray, dArray2) - BodyMotion.keyTime(key, dArray, dArray2)));
        double[][] dArray3 = new double[6][3];
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 3; ++j) {
                dArray3[i][j] = key.angles[i][j] + (key2.angles[i][j] - key.angles[i][j]) * d8;
            }
        }
        double d9 = key.spread + (key2.spread - key.spread) * d8;
        double d10 = key.shift + (key2.shift - key.shift) * d8;
        int n2 = dArray.length - 1;
        for (int i = 0; i < dArray.length; ++i) {
            if (!(d6 < dArray2[i + 1])) continue;
            n2 = i;
            break;
        }
        double d11 = clip.id.equals("ready") ? 0.0 : (d4 = BodyMotion.smooth(d6 / Math.min(4.0, dArray[0])) * (clip.air ? 1.0 : BodyMotion.smooth((d5 - d6) / Math.min(6.0, dArray[dArray.length - 1]))));
        if (clip.pulses > 0 && n2 == (clip.id.equals("flywheel") ? 0 : 1)) {
            double d12 = clip.id.equals("flywheel") ? dArray[0] / 6.0 : (clip.id.equals("rail") ? 12.0 : 10.0);
            double d13 = d6 - dArray2[n2];
            double d14 = Math.max(0.0, Math.sin(Math.PI * Math.min(1.0, (d13 % d12 + d12) % d12 / 4.0))) * d4;
            double[] dArray4 = dArray3[1];
            dArray4[0] = dArray4[0] + (clip.id.equals("ring") ? 2.0 : -1.6) * d14;
            double[] dArray5 = dArray3[clip.id.equals("flywheel") ? 3 : 2];
            dArray5[0] = dArray5[0] - 2.5 * d14;
        }
        if (bl2) {
            double[] dArray6 = dArray3[2];
            dArray3[2] = dArray3[3];
            dArray3[3] = dArray6;
            double[] dArray7 = dArray3[4];
            dArray3[4] = dArray3[5];
            dArray3[5] = dArray7;
            for (double[] dArray8 : dArray3) {
                dArray8[1] = -dArray8[1];
                dArray8[2] = -dArray8[2];
            }
        }
        return new Pose(clip.id, d6, d5, n2, dArray3, d9, d10, d4, clip.aim * d4, Math.max(-85.0, Math.min(85.0, d2)), Math.max(-180.0, Math.min(180.0, d3)), bl2, clip.air);
    }

    private static double keyTime(Key key, double[] dArray, double[] dArray2) {
        return dArray2[(int)key.at[0]] + dArray[(int)key.at[0]] * key.at[1];
    }

    public static final class Library {
        public String schema;
        public String[] bones;
        public Clip[] clips;
        private transient Map<String, Clip> index;
    }

    public static final class Clip {
        public String id;
        public String title;
        public String meaning;
        public Section[] sections;
        public Key[] keys;
        public double aim;
        public boolean air;
        public boolean loop;
        public int pulses;
    }

    public static final class Section {
        public String name;
        public double ticks;
        public double hard;
    }

    public static final class Key {
        public double[] at;
        public double[][] angles;
        public double spread;
        public double shift;
    }

    public record Pose(String id, double time, double life, int section, double[][] angles, double spread, double shift, double activity, double aim, double pitch, double yaw, boolean mirror, boolean air) {
    }
}

