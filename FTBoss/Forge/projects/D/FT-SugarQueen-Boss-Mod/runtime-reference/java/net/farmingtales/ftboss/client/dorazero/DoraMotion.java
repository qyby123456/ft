/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.dorazero;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DoraMotion {
    private DoraMotion() {
    }

    public static Prepared prepare(Model model) {
        if (model == null || model.id() == null || model.parts() == null || model.parts().length > 256 || model.tracks() == null || model.phaseDurations() == null) {
            throw new IllegalArgumentException("Invalid Dora model");
        }
        HashMap<String, List<Part>> hashMap = new HashMap<String, List<Part>>();
        HashSet<String> hashSet = new HashSet<String>();
        int n = 0;
        for (String string : List.of("CHARGE", "RELEASE", "PULSE", "BEAM")) {
            if (model.phaseDurations().getOrDefault(string, 0) <= 0) {
                throw new IllegalArgumentException("Invalid phase duration");
            }
            hashMap.put(string, new ArrayList());
        }
        for (Part part2 : model.parts()) {
            if (part2 == null || part2.name() == null || !hashSet.add(part2.name()) || part2.vertices() == null || part2.vertices().length > 256 || part2.faces() == null || part2.material() < 0 || part2.material() > 11 || part2.phases() == null || !Set.of("body", "radius", "formation", "beam").contains(part2.scaleMode())) {
                throw new IllegalArgumentException("Invalid Dora part");
            }
            DoraMotion.vector(part2.pivot());
            for (double[] dArray : part2.vertices()) {
                DoraMotion.vector(dArray);
            }
            for (double[] dArray : (Object)part2.faces()) {
                ++n;
                if (dArray == null || dArray.vertices() == null || dArray.vertices().length < 3 || dArray.vertices().length > 4 || dArray.uv() == null || dArray.uv().length != dArray.vertices().length) {
                    throw new IllegalArgumentException("Invalid Dora face");
                }
                DoraMotion.vector(dArray.normal());
                for (int i = 0; i < dArray.vertices().length; ++i) {
                    if (dArray.vertices()[i] < 0 || dArray.vertices()[i] >= part2.vertices().length || dArray.uv()[i] == null || dArray.uv()[i].length != 2) {
                        throw new IllegalArgumentException("Invalid Dora face index");
                    }
                    for (double d : dArray.uv()[i]) {
                        if (Double.isFinite(d) && !(d < 0.0) && !(d > 256.0)) continue;
                        throw new IllegalArgumentException("Invalid Dora UV");
                    }
                }
            }
            Object object = model.tracks().get(part2.name());
            if (object == null) {
                throw new IllegalArgumentException("Missing Dora tracks");
            }
            for (String string : part2.phases()) {
                if (!hashMap.containsKey(string)) {
                    throw new IllegalArgumentException("Unknown Dora phase");
                }
                Frame[] frameArray = (Frame[])object.get(string);
                if (frameArray == null || frameArray.length < 2 || frameArray.length > 64) {
                    throw new IllegalArgumentException("Invalid Dora track");
                }
                double d = -1.0;
                for (Frame frame : frameArray) {
                    if (frame == null || !Double.isFinite(frame.time()) || frame.time() < d || frame.time() < 0.0 || frame.time() > 1.0 || !Double.isFinite(frame.alpha()) || frame.alpha() < 0.0 || frame.alpha() > 1.0) {
                        throw new IllegalArgumentException("Invalid Dora keyframe");
                    }
                    DoraMotion.vector(frame.position());
                    DoraMotion.vector(frame.rotation());
                    DoraMotion.vector(frame.scale());
                    for (double d2 : frame.scale()) {
                        if (!(d2 <= 0.0)) continue;
                        throw new IllegalArgumentException("Invalid Dora scale");
                    }
                    d = frame.time();
                }
                if (frameArray[0].time() != 0.0 || frameArray[frameArray.length - 1].time() != 1.0) {
                    throw new IllegalArgumentException("Incomplete Dora track");
                }
                ((List)hashMap.get(string)).add(part2);
            }
        }
        if (n > 4096) {
            throw new IllegalArgumentException("Dora mesh budget exceeded");
        }
        for (List list : hashMap.values()) {
            list.sort(Comparator.comparingInt(part -> part.scaleMode().equals("radius") ? 0 : 1));
        }
        return new Prepared(model, hashMap);
    }

    private static void vector(double[] dArray) {
        if (dArray == null || dArray.length != 3) {
            throw new IllegalArgumentException("Invalid Dora vector");
        }
        for (double d : dArray) {
            if (Double.isFinite(d) && !(Math.abs(d) > 10000.0)) continue;
            throw new IllegalArgumentException("Invalid Dora coordinate");
        }
    }

    public static Pose sample(Frame[] frameArray, double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        Frame frame = frameArray[0];
        Frame frame2 = frameArray[frameArray.length - 1];
        for (int i = 1; i < frameArray.length; ++i) {
            if (d2 <= frameArray[i].time()) {
                frame = frameArray[i - 1];
                frame2 = frameArray[i];
                break;
            }
            frame = frameArray[i];
        }
        double d3 = frame2.time() == frame.time() ? 0.0 : Math.max(0.0, Math.min(1.0, (d2 - frame.time()) / (frame2.time() - frame.time())));
        return new Pose(DoraMotion.interpolate(frame.position(), frame2.position(), d3), DoraMotion.interpolate(frame.rotation(), frame2.rotation(), d3), DoraMotion.interpolate(frame.scale(), frame2.scale(), d3), frame.alpha() + (frame2.alpha() - frame.alpha()) * d3);
    }

    public static double[] interpolate(double[] dArray, double[] dArray2, double d) {
        return new double[]{dArray[0] + (dArray2[0] - dArray[0]) * d, dArray[1] + (dArray2[1] - dArray[1]) * d, dArray[2] + (dArray2[2] - dArray[2]) * d};
    }

    public static double[] localScale(String string, double d) {
        double[] dArray;
        double d2 = Math.min(1.25, d);
        if (string.equals("radius")) {
            double[] dArray2 = new double[3];
            dArray2[0] = d;
            dArray2[1] = 1.0;
            dArray = dArray2;
            dArray2[2] = d;
        } else {
            double[] dArray3 = new double[3];
            dArray3[0] = d2;
            dArray3[1] = d2;
            dArray = dArray3;
            dArray3[2] = d2;
        }
        return dArray;
    }

    public static double[] pivotScale(String string, double d) {
        double[] dArray;
        if (string.equals("radius") || string.equals("formation")) {
            double[] dArray2 = new double[3];
            dArray2[0] = d;
            dArray2[1] = 1.0;
            dArray = dArray2;
            dArray2[2] = d;
        } else {
            double[] dArray3 = new double[3];
            dArray3[0] = 1.0;
            dArray3[1] = 1.0;
            dArray = dArray3;
            dArray3[2] = 1.0;
        }
        return dArray;
    }

    public record Model(String id, Part[] parts, Map<String, Map<String, Frame[]>> tracks, Map<String, Integer> phaseDurations) {
    }

    public record Part(String name, String bone, double[] pivot, double[][] vertices, Face[] faces, int material, String[] phases, String scaleMode) {
    }

    public record Face(int[] vertices, double[][] uv, double[] normal) {
    }

    public record Frame(double time, double[] position, double[] rotation, double[] scale, double alpha) {
    }

    public record Prepared(Model model, Map<String, List<Part>> phases) {
    }

    public record Pose(double[] position, double[] rotation, double[] scale, double alpha) {
    }
}

