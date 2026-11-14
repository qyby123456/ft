/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 */
package net.farmingtales.ftboss.doramotion;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public final class DoraMotionCurves {
    public static final String[] BONES = new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    public static final String[] IDS = new String[]{"ready", "suppress", "sniper_charge", "sniper_fire", "spear", "reload", "arcane_fusillade", "breach_shell", "prism_battery"};
    private final Map<String, Clip> clips = new HashMap<String, Clip>();

    public DoraMotionCurves(String string) {
        Library library = (Library)new Gson().fromJson(string, Library.class);
        if (library == null || library.version != 1 || library.clips == null || library.clips.length != IDS.length) {
            throw new IllegalArgumentException("Dora motion inventory");
        }
        for (Clip object : library.clips) {
            if (object.id == null || object.ticks < 1 || object.keys == null || object.keys.length < 2 || this.clips.put(object.id, object) != null) {
                throw new IllegalArgumentException("Dora clip");
            }
            double d = -1.0;
            for (Key key : object.keys) {
                if (!Double.isFinite(key.time) || key.time <= d || key.time < 0.0 || key.time > 1.0 || key.rotation == null || key.rotation.length != 6) {
                    throw new IllegalArgumentException("Dora key time");
                }
                for (double[] dArray : key.rotation) {
                    if (dArray == null || dArray.length != 3) {
                        throw new IllegalArgumentException("Dora skeleton");
                    }
                    for (double d2 : dArray) {
                        if (Double.isFinite(d2) && !(Math.abs(d2) > 180.0)) continue;
                        throw new IllegalArgumentException("Dora angle");
                    }
                }
                d = key.time;
            }
            if (object.keys[0].time == 0.0 && d == 1.0) continue;
            throw new IllegalArgumentException("Dora key endpoints");
        }
        for (String string2 : IDS) {
            if (this.clips.containsKey(string2)) continue;
            throw new IllegalArgumentException("Missing Dora clip " + (String)string2);
        }
    }

    public static double smooth(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public double[][] sample(String string, double d, boolean bl) {
        Clip clip = this.clips.get(string);
        if (clip == null || !Double.isFinite(d)) {
            throw new IllegalArgumentException("Invalid Dora sample");
        }
        double d2 = Math.max(0.0, Math.min(1.0, d));
        int n = 0;
        while (n + 1 < clip.keys.length - 1 && clip.keys[n + 1].time < d2) {
            ++n;
        }
        Key key = clip.keys[n];
        Key key2 = clip.keys[n + 1];
        double d3 = DoraMotionCurves.smooth((d2 - key.time) / (key2.time - key.time));
        double[][] dArray = new double[6][3];
        for (int i = 0; i < 6; ++i) {
            int n2 = bl && i >= 2 ? (i % 2 == 0 ? i + 1 : i - 1) : i;
            for (int j = 0; j < 3; ++j) {
                dArray[n2][j] = (key.rotation[i][j] + (key2.rotation[i][j] - key.rotation[i][j]) * d3) * (double)(bl && j > 0 ? -1 : 1);
            }
        }
        return dArray;
    }

    public static void blend(double[][] dArray, double[][] dArray2, double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 3; ++j) {
                double[] dArray3 = dArray[i];
                int n = j;
                dArray3[n] = dArray3[n] + (dArray2[i][j] - dArray[i][j]) * d2;
            }
        }
    }

    public static final class Library {
        public int version;
        public Clip[] clips;
    }

    public static final class Clip {
        public String id;
        public String label;
        public int ticks;
        public Key[] keys;
    }

    public static final class Key {
        public double time;
        public double[][] rotation;
    }
}

