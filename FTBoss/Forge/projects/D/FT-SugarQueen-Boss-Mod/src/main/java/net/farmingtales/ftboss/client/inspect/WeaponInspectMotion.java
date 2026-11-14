package net.farmingtales.ftboss.client.inspect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/** 武器视检关键帧库；2026-09-17，位移单位为格、角度为度、时间为秒，五次平滑插值不越界。 */
public final class WeaponInspectMotion {
    public static final WeaponInspectMotion EMPTY = new WeaponInspectMotion(Map.of(), Map.of(), List.of());
    public static final Pose REST = new Pose(0, 0, 0, 0, 0, 0);

    public record Pose(double moveX, double moveY, double moveZ, double pitch, double yaw, double roll) {
        public Pose blend(Pose target, double amount) {
            double weight = smooth(amount);
            return new Pose(lerp(moveX, target.moveX, weight), lerp(moveY, target.moveY, weight),
                    lerp(moveZ, target.moveZ, weight), lerp(pitch, target.pitch, weight),
                    lerp(yaw, target.yaw, weight), lerp(roll, target.roll, weight));
        }

        public Pose mirror() {
            return new Pose(-moveX, moveY, moveZ, pitch, -yaw, -roll);
        }
    }

    public record Keyframe(double seconds, Pose pose) {}

    public record Variant(String item, String tag, String type, String value, String clip) {}

    public record Clip(List<Keyframe> frames) {
        public Clip {
            frames = List.copyOf(frames);
            if (frames.size() < 3 || frames.get(0).seconds() != 0 || !frames.get(0).pose().equals(REST)
                    || !frames.get(frames.size() - 1).pose().equals(REST)) {
                throw new IllegalArgumentException("Inspect clips must begin and end at rest");
            }
            double previous = -1;
            for (Keyframe frame : frames) {
                if (!Double.isFinite(frame.seconds()) || frame.seconds() <= previous || frame.seconds() > 10) {
                    throw new IllegalArgumentException("Inspect keyframe times must increase within 10 seconds");
                }
                previous = frame.seconds();
            }
        }

        public double duration() {
            return frames.get(frames.size() - 1).seconds();
        }

        public Pose sample(double seconds) {
            if (!Double.isFinite(seconds) || seconds <= 0 || seconds >= duration()) return REST;
            for (int index = 1; index < frames.size(); index++) {
                Keyframe next = frames.get(index);
                if (seconds <= next.seconds()) {
                    Keyframe previous = frames.get(index - 1);
                    return previous.pose().blend(next.pose(), (seconds - previous.seconds()) / (next.seconds() - previous.seconds()));
                }
            }
            return REST;
        }
    }

    private final Map<String, Clip> clips;
    private final Map<String, String> items;
    private final List<Variant> variants;

    private WeaponInspectMotion(Map<String, Clip> clips, Map<String, String> items, List<Variant> variants) {
        this.clips = Map.copyOf(clips);
        this.items = Map.copyOf(items);
        this.variants = List.copyOf(variants);
    }

    public Clip forItem(String identifier) {
        String profile = items.get(identifier);
        return profile == null ? null : clips.get(profile);
    }

    public Clip forItem(String identifier, BiFunction<String, String, String> tags) {
        for (Variant variant : variants) {
            if (variant.item().equals(identifier) && variant.value().equals(tags.apply(variant.tag(), variant.type()))) {
                return clips.get(variant.clip());
            }
        }
        return forItem(identifier);
    }

    public List<Variant> variants() {
        return variants;
    }

    public Map<String, Clip> clips() {
        return clips;
    }

    public Map<String, String> items() {
        return items;
    }

    public static WeaponInspectMotion read(Reader reader) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        if (root.get("format").getAsInt() != 1) throw new IllegalArgumentException("Unsupported inspect format");
        Map<String, Clip> clips = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("clips").entrySet()) {
            List<Keyframe> frames = new ArrayList<>();
            for (JsonElement element : entry.getValue().getAsJsonArray()) {
                JsonArray values = element.getAsJsonArray();
                if (values.size() != 7) throw new IllegalArgumentException("Expected time, position XYZ, rotation XYZ");
                double[] axes = new double[7];
                for (int index = 0; index < axes.length; index++) {
                    axes[index] = values.get(index).getAsDouble();
                    double limit = index == 0 ? 10 : index < 4 ? 2 : 180;
                    if (!Double.isFinite(axes[index]) || Math.abs(axes[index]) > limit) {
                        throw new IllegalArgumentException("Unsafe inspect transform in " + entry.getKey());
                    }
                }
                frames.add(new Keyframe(axes[0], new Pose(axes[1], axes[2], axes[3], axes[4], axes[5], axes[6])));
            }
            clips.put(entry.getKey(), new Clip(frames));
        }
        Map<String, String> items = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("items").entrySet()) {
            String profile = entry.getValue().getAsString();
            if (!validIdentifier(entry.getKey()) || !clips.containsKey(profile)) {
                throw new IllegalArgumentException("Invalid inspect item/profile: " + entry.getKey());
            }
            items.put(entry.getKey(), profile);
        }
        List<Variant> variants = new ArrayList<>();
        if (root.has("variants")) {
            for (JsonElement element : root.getAsJsonArray("variants")) {
                JsonObject object = element.getAsJsonObject();
                String item = object.get("item").getAsString();
                String tag = object.get("tag").getAsString();
                String profile = object.get("clip").getAsString();
                if (!validIdentifier(item) || tag.isBlank() || !clips.containsKey(profile)) {
                    throw new IllegalArgumentException("Invalid inspect variant: " + item);
                }
                var value = object.getAsJsonPrimitive("value");
                String type;
                String expected;
                if (value.isBoolean()) {
                    type = "boolean";
                    expected = Boolean.toString(value.getAsBoolean());
                } else if (value.isNumber()) {
                    if (value.getAsDouble() != value.getAsInt()) throw new IllegalArgumentException("Expected integer model data");
                    type = "int";
                    expected = Integer.toString(value.getAsInt());
                } else if (value.isString()) {
                    type = "string";
                    expected = value.getAsString();
                } else {
                    throw new IllegalArgumentException("Unsupported inspect variant value");
                }
                Variant variant = new Variant(item, tag, type, expected, profile);
                if (variants.stream().anyMatch(previous -> previous.item().equals(item) && previous.tag().equals(tag)
                        && previous.type().equals(type) && previous.value().equals(expected))) {
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
        double weight = Math.max(0, Math.min(1, amount));
        return weight * weight * weight * (weight * (weight * 6 - 15) + 10);
    }
}
