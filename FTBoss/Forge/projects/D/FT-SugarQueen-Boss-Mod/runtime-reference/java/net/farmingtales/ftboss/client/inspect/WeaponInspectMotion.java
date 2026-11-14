/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.google.gson.JsonPrimitive
 */
package net.farmingtales.ftboss.client.inspect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public final class WeaponInspectMotion {
    public static final WeaponInspectMotion EMPTY = new WeaponInspectMotion(Map.of(), Map.of(), List.of());
    public static final Pose REST = new Pose(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    private final Map<String, Clip> clips;
    private final Map<String, String> items;
    private final List<Variant> variants;

    private WeaponInspectMotion(Map<String, Clip> clips, Map<String, String> items, List<Variant> variants) {
        this.clips = Map.copyOf(clips);
        this.items = Map.copyOf(items);
        this.variants = List.copyOf(variants);
    }

    public Clip forItem(String identifier) {
        String profile = this.items.get(identifier);
        return profile == null ? null : this.clips.get(profile);
    }

    public Clip forItem(String identifier, BiFunction<String, String, String> tags) {
        for (Variant variant : this.variants) {
            if (!variant.item().equals(identifier) || !variant.value().equals(tags.apply(variant.tag(), variant.type()))) continue;
            return this.clips.get(variant.clip());
        }
        return this.forItem(identifier);
    }

    public List<Variant> variants() {
        return this.variants;
    }

    public Map<String, Clip> clips() {
        return this.clips;
    }

    public Map<String, String> items() {
        return this.items;
    }

    public static WeaponInspectMotion read(Reader reader) {
        JsonObject root = JsonParser.parseReader((Reader)reader).getAsJsonObject();
        if (root.get("format").getAsInt() != 1) {
            throw new IllegalArgumentException("Unsupported inspect format");
        }
        HashMap<String, Clip> clips = new HashMap<String, Clip>();
        for (Object entry : root.getAsJsonObject("clips").entrySet()) {
            ArrayList<Keyframe> frames = new ArrayList<Keyframe>();
            for (JsonElement element : ((JsonElement)entry.getValue()).getAsJsonArray()) {
                JsonArray values = element.getAsJsonArray();
                if (values.size() != 7) {
                    throw new IllegalArgumentException("Expected time, position XYZ, rotation XYZ");
                }
                double[] axes = new double[7];
                for (int index = 0; index < axes.length; ++index) {
                    double limit;
                    axes[index] = values.get(index).getAsDouble();
                    double d = index == 0 ? 10.0 : (limit = index < 4 ? 2.0 : 180.0);
                    if (Double.isFinite(axes[index]) && !(Math.abs(axes[index]) > limit)) continue;
                    throw new IllegalArgumentException("Unsafe inspect transform in " + (String)entry.getKey());
                }
                frames.add(new Keyframe(axes[0], new Pose(axes[1], axes[2], axes[3], axes[4], axes[5], axes[6])));
            }
            clips.put((String)entry.getKey(), new Clip(frames));
        }
        HashMap<String, String> items = new HashMap<String, String>();
        for (Map.Entry entry : root.getAsJsonObject("items").entrySet()) {
            String profile = ((JsonElement)entry.getValue()).getAsString();
            if (!WeaponInspectMotion.validIdentifier((String)entry.getKey()) || !clips.containsKey(profile)) {
                throw new IllegalArgumentException("Invalid inspect item/profile: " + (String)entry.getKey());
            }
            items.put((String)entry.getKey(), profile);
        }
        ArrayList<Variant> variants = new ArrayList<Variant>();
        if (root.has("variants")) {
            for (JsonElement element : root.getAsJsonArray("variants")) {
                String expected;
                String type;
                JsonObject object = element.getAsJsonObject();
                String item = object.get("item").getAsString();
                String tag = object.get("tag").getAsString();
                String profile = object.get("clip").getAsString();
                if (!WeaponInspectMotion.validIdentifier(item) || tag.isBlank() || !clips.containsKey(profile)) {
                    throw new IllegalArgumentException("Invalid inspect variant: " + item);
                }
                JsonPrimitive value = object.getAsJsonPrimitive("value");
                if (value.isBoolean()) {
                    type = "boolean";
                    expected = Boolean.toString(value.getAsBoolean());
                } else if (value.isNumber()) {
                    if (value.getAsDouble() != (double)value.getAsInt()) {
                        throw new IllegalArgumentException("Expected integer model data");
                    }
                    type = "int";
                    expected = Integer.toString(value.getAsInt());
                } else if (value.isString()) {
                    type = "string";
                    expected = value.getAsString();
                } else {
                    throw new IllegalArgumentException("Unsupported inspect variant value");
                }
                Variant variant = new Variant(item, tag, type, expected, profile);
                if (variants.stream().anyMatch(previous -> previous.item().equals(item) && previous.tag().equals(tag) && previous.type().equals(type) && previous.value().equals(expected))) {
                    throw new IllegalArgumentException("Duplicate inspect variant: " + item + "/" + tag);
                }
                variants.add(variant);
            }
        }
        return new WeaponInspectMotion(clips, items, variants);
    }

    private static boolean validIdentifier(String identifier) {
        return identifier.matches("[a-z0-9_.-]+:[a-z0-9/._-]+");
    }

    private static double lerp(double start, double end, double weight) {
        return start + (end - start) * weight;
    }

    private static double smooth(double amount) {
        double weight = Math.max(0.0, Math.min(1.0, amount));
        return weight * weight * weight * (weight * (weight * 6.0 - 15.0) + 10.0);
    }

    public record Clip(List<Keyframe> frames) {
        public Clip {
            frames = List.copyOf(frames);
            if (frames.size() < 3 || frames.get(0).seconds() != 0.0 || !frames.get(0).pose().equals(REST) || !frames.get(frames.size() - 1).pose().equals(REST)) {
                throw new IllegalArgumentException("Inspect clips must begin and end at rest");
            }
            double previous = -1.0;
            for (Keyframe frame : frames) {
                if (!Double.isFinite(frame.seconds()) || frame.seconds() <= previous || frame.seconds() > 10.0) {
                    throw new IllegalArgumentException("Inspect keyframe times must increase within 10 seconds");
                }
                previous = frame.seconds();
            }
        }

        public double duration() {
            return this.frames.get(this.frames.size() - 1).seconds();
        }

        public Pose sample(double seconds) {
            if (!Double.isFinite(seconds) || seconds <= 0.0 || seconds >= this.duration()) {
                return REST;
            }
            for (int index = 1; index < this.frames.size(); ++index) {
                Keyframe next = this.frames.get(index);
                if (!(seconds <= next.seconds())) continue;
                Keyframe previous = this.frames.get(index - 1);
                return previous.pose().blend(next.pose(), (seconds - previous.seconds()) / (next.seconds() - previous.seconds()));
            }
            return REST;
        }
    }

    public record Variant(String item, String tag, String type, String value, String clip) {
    }

    public record Keyframe(double seconds, Pose pose) {
    }

    public record Pose(double moveX, double moveY, double moveZ, double pitch, double yaw, double roll) {
        public Pose blend(Pose target, double amount) {
            double weight = WeaponInspectMotion.smooth(amount);
            return new Pose(WeaponInspectMotion.lerp(this.moveX, target.moveX, weight), WeaponInspectMotion.lerp(this.moveY, target.moveY, weight), WeaponInspectMotion.lerp(this.moveZ, target.moveZ, weight), WeaponInspectMotion.lerp(this.pitch, target.pitch, weight), WeaponInspectMotion.lerp(this.yaw, target.yaw, weight), WeaponInspectMotion.lerp(this.roll, target.roll, weight));
        }

        public Pose mirror() {
            return new Pose(-this.moveX, this.moveY, this.moveZ, this.pitch, -this.yaw, -this.roll);
        }
    }
}

