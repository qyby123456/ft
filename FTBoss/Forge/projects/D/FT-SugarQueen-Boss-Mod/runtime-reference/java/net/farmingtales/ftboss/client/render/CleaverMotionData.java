/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.client.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CleaverMotionData {
    public static final String PREFIX = "animation.black_cleaver.";
    public static final List<String> WINGS = List.of("blade_upper_wing", "blade_lower_wing", "blade_right_upper_wing", "blade_right_lower_wing");
    public static final List<String> BONES = List.of("weapon", "ember_core", "blade_upper_wing", "blade_lower_wing", "blade_right_upper_wing", "blade_right_lower_wing");
    public static final List<String> EFFECTS = List.of("core", "rune", "edge", "seam");
    public static final List<String> REQUIRED = List.of("idle", "charge", "slam", "recover", "ready", "blood_enter", "blood_idle", "blood_slam", "blood_recover", "blood_exit");
    public static final Vec ZERO = new Vec(0.0, 0.0, 0.0);
    public static final Vec ONE = new Vec(1.0, 1.0, 1.0);
    public static final Pose IDENTITY = new Pose(ZERO, ONE);
    private final Map<String, Clip> clips;

    public CleaverMotionData(Reader animationReader, Reader effectReader) {
        JsonObject animations = JsonParser.parseReader((Reader)animationReader).getAsJsonObject().getAsJsonObject("animations");
        JsonObject effects = JsonParser.parseReader((Reader)effectReader).getAsJsonObject().getAsJsonObject("clips");
        LinkedHashMap<String, Clip> loaded = new LinkedHashMap<String, Clip>();
        for (Map.Entry entry : animations.entrySet()) {
            boolean loop;
            JsonObject animation = ((JsonElement)entry.getValue()).getAsJsonObject();
            JsonObject effect = effects.getAsJsonObject((String)entry.getKey());
            double duration = animation.get("animation_length").getAsDouble();
            boolean bl = loop = animation.has("loop") && animation.get("loop").getAsBoolean();
            if (!(duration > 0.0) || !Double.isFinite(duration) || effect == null || Math.abs(effect.get("duration").getAsDouble() - duration) > 1.0E-5) {
                throw new IllegalArgumentException("Mismatched cleaver clip " + (String)entry.getKey());
            }
            LinkedHashMap boneTracks = new LinkedHashMap();
            for (Map.Entry bone : animation.getAsJsonObject("bones").entrySet()) {
                if (!BONES.contains(bone.getKey())) {
                    throw new IllegalArgumentException("Unsupported animated bone " + (String)bone.getKey());
                }
                LinkedHashMap<String, Track> channels = new LinkedHashMap<String, Track>();
                for (Map.Entry channel : ((JsonElement)bone.getValue()).getAsJsonObject().entrySet()) {
                    if (!((String)channel.getKey()).equals("rotation") && !((String)channel.getKey()).equals("scale")) {
                        throw new IllegalArgumentException("Unsupported cleaver channel");
                    }
                    channels.put((String)channel.getKey(), Track.read((JsonElement)channel.getValue(), 3));
                }
                boneTracks.put((String)bone.getKey(), Map.copyOf(channels));
            }
            LinkedHashMap<String, Track> effectTracks = new LinkedHashMap<String, Track>();
            for (String name : EFFECTS) {
                effectTracks.put(name, Track.read(effect.getAsJsonObject("tracks").get(name), 1));
            }
            loaded.put((String)entry.getKey(), new Clip(duration, loop, Map.copyOf(boneTracks), Map.copyOf(effectTracks)));
        }
        for (String name : REQUIRED) {
            if (loaded.containsKey(PREFIX + name)) continue;
            throw new IllegalArgumentException("Missing cleaver clip " + name);
        }
        this.clips = Map.copyOf(loaded);
    }

    private Clip clip(String name) {
        Clip clip = this.clips.get(name.startsWith(PREFIX) ? name : PREFIX + name);
        if (clip == null) {
            throw new IllegalArgumentException("Unknown cleaver clip " + name);
        }
        return clip;
    }

    public double duration(String name) {
        return this.clip((String)name).duration;
    }

    public Frame sample(String name, double seconds) {
        Clip clip = this.clip(name);
        double time = Math.max(0.0, seconds);
        time = clip.loop ? time % clip.duration : Math.min(time, clip.duration);
        LinkedHashMap<String, Pose> bones = new LinkedHashMap<String, Pose>();
        for (String bone : BONES) {
            Map channels = clip.bones.getOrDefault(bone, Map.of());
            bones.put(bone, new Pose(CleaverMotionData.sampleVector((Track)channels.get("rotation"), time, ZERO), CleaverMotionData.sampleVector((Track)channels.get("scale"), time, ONE)));
        }
        LinkedHashMap<String, Double> effects = new LinkedHashMap<String, Double>();
        for (String effect : EFFECTS) {
            effects.put(effect, clip.effects.get(effect).sample(time)[0]);
        }
        return new Frame(bones, effects);
    }

    private static Vec sampleVector(Track track, double seconds, Vec fallback) {
        if (track == null) {
            return fallback;
        }
        double[] values = track.sample(seconds);
        return new Vec(values[0], values[1], values[2]);
    }

    public Frame skill(Frame base, String name, double seconds, double bloodAmount) {
        Frame action = this.sample(name, seconds);
        boolean blood = name.contains("blood_");
        double weight = blood ? CleaverMotionData.clamp(bloodAmount) : 1.0;
        LinkedHashMap<String, Pose> bones = new LinkedHashMap<String, Pose>(base.bones);
        bones.put("weapon", action.bone("weapon"));
        bones.put("ember_core", base.bone("ember_core").mix(action.bone("ember_core"), weight));
        if (blood) {
            for (String wing : WINGS) {
                double angle = base.bone((String)wing).rotation.axisZ * Math.abs(action.bone((String)wing).rotation.axisZ) / 11.0;
                bones.put(wing, new Pose(new Vec(0.0, 0.0, angle), ONE));
            }
        }
        LinkedHashMap<String, Double> effects = new LinkedHashMap<String, Double>(base.effects);
        if (blood) {
            for (String effect : EFFECTS) {
                effects.put(effect, base.effect(effect) + (action.effect(effect) - base.effect(effect)) * weight);
            }
        }
        return new Frame(bones, effects);
    }

    public static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    public static double smooth(double value) {
        double amount = CleaverMotionData.clamp(value);
        return amount * amount * (3.0 - 2.0 * amount);
    }

    private record Track(double[] times, double[][] values) {
        static Track read(JsonElement source, int dimensions) {
            if (source.isJsonArray()) {
                return new Track(new double[]{0.0}, new double[][]{Track.vector(source, dimensions)});
            }
            JsonObject object = source.getAsJsonObject();
            if (object.size() == 0) {
                throw new IllegalArgumentException("Empty cleaver track");
            }
            double[] times = new double[object.size()];
            double[][] values = new double[object.size()][];
            int index = 0;
            for (Map.Entry entry : object.entrySet()) {
                times[index] = Double.parseDouble((String)entry.getKey());
                if (!Double.isFinite(times[index]) || times[index] < 0.0 || index > 0 && times[index] <= times[index - 1]) {
                    throw new IllegalArgumentException("Unordered cleaver keyframes");
                }
                values[index] = Track.vector((JsonElement)entry.getValue(), dimensions);
                ++index;
            }
            return new Track(times, values);
        }

        private static double[] vector(JsonElement source, int dimensions) {
            double[] result = new double[dimensions];
            if (dimensions > 1 && source.getAsJsonArray().size() != dimensions) {
                throw new IllegalArgumentException("Invalid cleaver vector");
            }
            for (int axis = 0; axis < dimensions; ++axis) {
                double d = result[axis] = dimensions == 1 ? source.getAsDouble() : source.getAsJsonArray().get(axis).getAsDouble();
                if (Double.isFinite(result[axis])) continue;
                throw new IllegalArgumentException("Non-finite cleaver value");
            }
            return result;
        }

        double[] sample(double seconds) {
            int found = Arrays.binarySearch(this.times, seconds);
            if (found >= 0) {
                return (double[])this.values[found].clone();
            }
            int upper = -found - 1;
            if (upper == 0) {
                return (double[])this.values[0].clone();
            }
            if (upper == this.times.length) {
                return (double[])this.values[this.times.length - 1].clone();
            }
            double amount = (seconds - this.times[upper - 1]) / (this.times[upper] - this.times[upper - 1]);
            double[] result = new double[this.values[upper].length];
            for (int axis = 0; axis < result.length; ++axis) {
                result[axis] = this.values[upper - 1][axis] + (this.values[upper][axis] - this.values[upper - 1][axis]) * amount;
            }
            return result;
        }
    }

    private record Clip(double duration, boolean loop, Map<String, Map<String, Track>> bones, Map<String, Track> effects) {
    }

    public record Pose(Vec rotation, Vec scale) {
        public Pose mix(Pose target, double amount) {
            return new Pose(this.rotation.mix(target.rotation, amount), this.scale.mix(target.scale, amount));
        }
    }

    public record Vec(double axisX, double axisY, double axisZ) {
        public Vec mix(Vec target, double amount) {
            return new Vec(this.axisX + (target.axisX - this.axisX) * amount, this.axisY + (target.axisY - this.axisY) * amount, this.axisZ + (target.axisZ - this.axisZ) * amount);
        }

        public Vec offset(Vec origin, Vec reference, double amount) {
            return new Vec(this.axisX + (origin.axisX - reference.axisX) * amount, this.axisY + (origin.axisY - reference.axisY) * amount, this.axisZ + (origin.axisZ - reference.axisZ) * amount);
        }
    }

    public record Frame(Map<String, Pose> bones, Map<String, Double> effects) {
        public Frame {
            bones = Map.copyOf(bones);
            effects = Map.copyOf(effects);
        }

        public Pose bone(String name) {
            return this.bones.getOrDefault(name, IDENTITY);
        }

        public double effect(String name) {
            return this.effects.getOrDefault(name, 0.0);
        }
    }
}

