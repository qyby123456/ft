package net.farmingtales.ftboss.client.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 黑切导出动画与四层发光数据的共同采样器；2026-09-16 接入，时间统一使用秒。 */
public final class CleaverMotionData {
    public static final String PREFIX = "animation.black_cleaver.";
    public static final List<String> WINGS = List.of("blade_upper_wing", "blade_lower_wing", "blade_right_upper_wing", "blade_right_lower_wing");
    public static final List<String> BONES = List.of("weapon", "ember_core", "blade_upper_wing", "blade_lower_wing", "blade_right_upper_wing", "blade_right_lower_wing");
    public static final List<String> EFFECTS = List.of("core", "rune", "edge", "seam");
    public static final List<String> REQUIRED = List.of("idle", "charge", "slam", "recover", "ready", "blood_enter", "blood_idle", "blood_slam", "blood_recover", "blood_exit");
    public static final Vec ZERO = new Vec(0, 0, 0);
    public static final Vec ONE = new Vec(1, 1, 1);
    public static final Pose IDENTITY = new Pose(ZERO, ONE);
    private final Map<String, Clip> clips;

    public record Vec(double axisX, double axisY, double axisZ) {
        public Vec mix(Vec target, double amount) {
            return new Vec(axisX + (target.axisX - axisX) * amount, axisY + (target.axisY - axisY) * amount, axisZ + (target.axisZ - axisZ) * amount);
        }

        public Vec offset(Vec origin, Vec reference, double amount) {
            return new Vec(axisX + (origin.axisX - reference.axisX) * amount, axisY + (origin.axisY - reference.axisY) * amount,
                    axisZ + (origin.axisZ - reference.axisZ) * amount);
        }
    }

    public record Pose(Vec rotation, Vec scale) {
        public Pose mix(Pose target, double amount) {
            return new Pose(rotation.mix(target.rotation, amount), scale.mix(target.scale, amount));
        }
    }

    public record Frame(Map<String, Pose> bones, Map<String, Double> effects) {
        public Frame {
            bones = Map.copyOf(bones);
            effects = Map.copyOf(effects);
        }

        public Pose bone(String name) {
            return bones.getOrDefault(name, IDENTITY);
        }

        public double effect(String name) {
            return effects.getOrDefault(name, 0.0);
        }
    }

    private record Track(double[] times, double[][] values) {
        static Track read(JsonElement source, int dimensions) {
            if (source.isJsonArray()) return new Track(new double[]{0}, new double[][]{vector(source, dimensions)});
            JsonObject object = source.getAsJsonObject();
            if (object.size() == 0) throw new IllegalArgumentException("Empty cleaver track");
            double[] times = new double[object.size()];
            double[][] values = new double[object.size()][];
            int index = 0;
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                times[index] = Double.parseDouble(entry.getKey());
                if (!Double.isFinite(times[index]) || times[index] < 0 || (index > 0 && times[index] <= times[index - 1])) {
                    throw new IllegalArgumentException("Unordered cleaver keyframes");
                }
                values[index] = vector(entry.getValue(), dimensions);
                index++;
            }
            return new Track(times, values);
        }

        private static double[] vector(JsonElement source, int dimensions) {
            double[] result = new double[dimensions];
            if (dimensions > 1 && source.getAsJsonArray().size() != dimensions) throw new IllegalArgumentException("Invalid cleaver vector");
            for (int axis = 0; axis < dimensions; axis++) {
                result[axis] = dimensions == 1 ? source.getAsDouble() : source.getAsJsonArray().get(axis).getAsDouble();
                if (!Double.isFinite(result[axis])) throw new IllegalArgumentException("Non-finite cleaver value");
            }
            return result;
        }

        double[] sample(double seconds) {
            int found = Arrays.binarySearch(times, seconds);
            if (found >= 0) return values[found].clone();
            int upper = -found - 1;
            if (upper == 0) return values[0].clone();
            if (upper == times.length) return values[times.length - 1].clone();
            double amount = (seconds - times[upper - 1]) / (times[upper] - times[upper - 1]);
            double[] result = new double[values[upper].length];
            for (int axis = 0; axis < result.length; axis++) result[axis] = values[upper - 1][axis] + (values[upper][axis] - values[upper - 1][axis]) * amount;
            return result;
        }
    }

    private record Clip(double duration, boolean loop, Map<String, Map<String, Track>> bones, Map<String, Track> effects) {}

    public CleaverMotionData(Reader animationReader, Reader effectReader) {
        JsonObject animations = JsonParser.parseReader(animationReader).getAsJsonObject().getAsJsonObject("animations");
        JsonObject effects = JsonParser.parseReader(effectReader).getAsJsonObject().getAsJsonObject("clips");
        Map<String, Clip> loaded = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : animations.entrySet()) {
            JsonObject animation = entry.getValue().getAsJsonObject();
            JsonObject effect = effects.getAsJsonObject(entry.getKey());
            double duration = animation.get("animation_length").getAsDouble();
            boolean loop = animation.has("loop") && animation.get("loop").getAsBoolean();
            if (!(duration > 0) || !Double.isFinite(duration) || effect == null || Math.abs(effect.get("duration").getAsDouble() - duration) > 0.00001) {
                throw new IllegalArgumentException("Mismatched cleaver clip " + entry.getKey());
            }
            Map<String, Map<String, Track>> boneTracks = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> bone : animation.getAsJsonObject("bones").entrySet()) {
                if (!BONES.contains(bone.getKey())) throw new IllegalArgumentException("Unsupported animated bone " + bone.getKey());
                Map<String, Track> channels = new LinkedHashMap<>();
                for (Map.Entry<String, JsonElement> channel : bone.getValue().getAsJsonObject().entrySet()) {
                    if (!channel.getKey().equals("rotation") && !channel.getKey().equals("scale")) throw new IllegalArgumentException("Unsupported cleaver channel");
                    channels.put(channel.getKey(), Track.read(channel.getValue(), 3));
                }
                boneTracks.put(bone.getKey(), Map.copyOf(channels));
            }
            Map<String, Track> effectTracks = new LinkedHashMap<>();
            for (String name : EFFECTS) effectTracks.put(name, Track.read(effect.getAsJsonObject("tracks").get(name), 1));
            loaded.put(entry.getKey(), new Clip(duration, loop, Map.copyOf(boneTracks), Map.copyOf(effectTracks)));
        }
        for (String name : REQUIRED) if (!loaded.containsKey(PREFIX + name)) throw new IllegalArgumentException("Missing cleaver clip " + name);
        clips = Map.copyOf(loaded);
    }

    private Clip clip(String name) {
        Clip clip = clips.get(name.startsWith(PREFIX) ? name : PREFIX + name);
        if (clip == null) throw new IllegalArgumentException("Unknown cleaver clip " + name);
        return clip;
    }

    public double duration(String name) {
        return clip(name).duration;
    }

    public Frame sample(String name, double seconds) {
        Clip clip = clip(name);
        double time = Math.max(0, seconds);
        time = clip.loop ? time % clip.duration : Math.min(time, clip.duration);
        Map<String, Pose> bones = new LinkedHashMap<>();
        for (String bone : BONES) {
            Map<String, Track> channels = clip.bones.getOrDefault(bone, Map.of());
            bones.put(bone, new Pose(sampleVector(channels.get("rotation"), time, ZERO), sampleVector(channels.get("scale"), time, ONE)));
        }
        Map<String, Double> effects = new LinkedHashMap<>();
        for (String effect : EFFECTS) effects.put(effect, clip.effects.get(effect).sample(time)[0]);
        return new Frame(bones, effects);
    }

    private static Vec sampleVector(Track track, double seconds, Vec fallback) {
        if (track == null) return fallback;
        double[] values = track.sample(seconds);
        return new Vec(values[0], values[1], values[2]);
    }

    public Frame skill(Frame base, String name, double seconds, double bloodAmount) {
        Frame action = sample(name, seconds);
        boolean blood = name.contains("blood_");
        double weight = blood ? clamp(bloodAmount) : 1;
        Map<String, Pose> bones = new LinkedHashMap<>(base.bones);
        bones.put("weapon", action.bone("weapon"));
        bones.put("ember_core", base.bone("ember_core").mix(action.bone("ember_core"), weight));
        if (blood) {
            for (String wing : WINGS) {
                double angle = base.bone(wing).rotation.axisZ * Math.abs(action.bone(wing).rotation.axisZ) / 11;
                bones.put(wing, new Pose(new Vec(0, 0, angle), ONE));
            }
        }
        Map<String, Double> effects = new LinkedHashMap<>(base.effects);
        if (blood) for (String effect : EFFECTS) effects.put(effect, base.effect(effect) + (action.effect(effect) - base.effect(effect)) * weight);
        return new Frame(bones, effects);
    }

    public static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    public static double smooth(double value) {
        double amount = clamp(value);
        return amount * amount * (3 - 2 * amount);
    }
}
